// SPDX-FileCopyrightText: 2026 mObywatel-2.0 contributors
// SPDX-License-Identifier: EUPL-1.2
package pl.gov.obywatel.pades;

import android.content.Context;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.AtomicFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.util.Arrays;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

/**
 * Encrypted, app-private cache for trust and revocation evidence used by offline
 * PAdES validation. Stale evidence can be retrieved, but is always explicitly
 * marked stale and MUST NOT be reported as current.
 */
public final class OfflineEvidenceStore {
    public static final int MAX_ITEM_BYTES = 12 * 1024 * 1024;
    private static final int MAX_CACHE_BYTES = 100 * 1024 * 1024;
    private static final int MAGIC = 0x4F574450; // OWDP
    private static final int FORMAT_VERSION = 1;
    private static final int GCM_TAG_BITS = 128;
    private static final String KEY_ALIAS = "owd-pades-evidence-aes-gcm-v1";
    private static final String KEYSTORE = "AndroidKeyStore";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";

    public enum EvidenceType {
        EU_LOTL, MEMBER_STATE_TRUSTED_LIST, CRL, OCSP_RESPONSE,
        TSA_TOKEN, VALIDATION_POLICY, TRUST_ANCHOR_BUNDLE
    }

    public record Evidence(
            EvidenceType type,
            String cacheKey,
            byte[] payload,
            long fetchedAtEpochMillis,
            long expiresAtEpochMillis,
            boolean fresh) {
        public Evidence {
            payload = payload.clone();
        }
        @Override public byte[] payload() { return payload.clone(); }
    }

    private final File directory;

    public OfflineEvidenceStore(Context context) {
        Context app = context.getApplicationContext();
        directory = new File(app.getNoBackupFilesDir(), "pades-evidence-v1");
        if (!directory.exists() && !directory.mkdirs()) {
            throw new IllegalStateException("Cannot create private PAdES evidence cache");
        }
    }

    public synchronized void save(
            EvidenceType type,
            String cacheKey,
            byte[] payload,
            long fetchedAtEpochMillis,
            long expiresAtEpochMillis) throws Exception {
        if (type == null || cacheKey == null || cacheKey.isBlank() || cacheKey.length() > 512) {
            throw new IllegalArgumentException("Invalid evidence type or cache key");
        }
        if (payload == null || payload.length == 0 || payload.length > MAX_ITEM_BYTES) {
            throw new IllegalArgumentException("Evidence payload is empty or exceeds 12 MiB");
        }
        if (fetchedAtEpochMillis <= 0 || expiresAtEpochMillis <= fetchedAtEpochMillis) {
            throw new IllegalArgumentException("Invalid evidence validity interval");
        }

        byte[] plaintext = encode(type, cacheKey, payload, fetchedAtEpochMillis, expiresAtEpochMillis);
        byte[] nonce = new byte[12];
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey());
        nonce = cipher.getIV();
        cipher.updateAAD(aad(type, cacheKey));
        byte[] ciphertext = cipher.doFinal(plaintext);

        File target = targetFile(type, cacheKey);
        AtomicFile atomic = new AtomicFile(target);
        FileOutputStream output = null;
        try {
            output = atomic.startWrite();
            DataOutputStream data = new DataOutputStream(output);
            data.writeInt(MAGIC);
            data.writeInt(FORMAT_VERSION);
            data.writeInt(nonce.length);
            data.write(nonce);
            data.writeInt(ciphertext.length);
            data.write(ciphertext);
            data.flush();
            atomic.finishWrite(output);
            output = null;
        } catch (Exception error) {
            if (output != null) atomic.failWrite(output);
            throw error;
        } finally {
            Arrays.fill(plaintext, (byte) 0);
        }
        enforceCacheBound();
    }

    /** Returns stale evidence too, with fresh=false, so offline reports can disclose its age. */
    public synchronized Evidence load(EvidenceType type, String cacheKey) throws Exception {
        File target = targetFile(type, cacheKey);
        if (!target.isFile()) return null;
        AtomicFile atomic = new AtomicFile(target);
        byte[] nonce;
        byte[] ciphertext;
        try (FileInputStream input = atomic.openRead();
             DataInputStream data = new DataInputStream(input)) {
            if (data.readInt() != MAGIC || data.readInt() != FORMAT_VERSION) {
                throw new SecurityException("Unknown evidence cache format");
            }
            int nonceLength = data.readInt();
            if (nonceLength != 12) throw new SecurityException("Invalid AES-GCM nonce");
            nonce = new byte[nonceLength];
            data.readFully(nonce);
            int cipherLength = data.readInt();
            if (cipherLength < 16 || cipherLength > MAX_ITEM_BYTES + 4096) {
                throw new SecurityException("Invalid evidence cache payload size");
            }
            ciphertext = new byte[cipherLength];
            data.readFully(ciphertext);
            if (ciphertext.length != cipherLength || data.read() != -1) {
                throw new SecurityException("Truncated or trailing evidence cache data");
            }
        }

        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.DECRYPT_MODE, getOrCreateKey(), new GCMParameterSpec(GCM_TAG_BITS, nonce));
        cipher.updateAAD(aad(type, cacheKey));
        byte[] plaintext = cipher.doFinal(ciphertext);
        try {
            Evidence evidence = decode(plaintext);
            if (evidence.type() != type || !evidence.cacheKey().equals(cacheKey)) {
                throw new SecurityException("Evidence cache identity mismatch");
            }
            return new Evidence(
                    evidence.type(), evidence.cacheKey(), evidence.payload(),
                    evidence.fetchedAtEpochMillis(), evidence.expiresAtEpochMillis(),
                    System.currentTimeMillis() <= evidence.expiresAtEpochMillis());
        } finally {
            Arrays.fill(plaintext, (byte) 0);
        }
    }

    public synchronized boolean delete(EvidenceType type, String cacheKey) {
        File target = targetFile(type, cacheKey);
        boolean existed = target.exists();
        new AtomicFile(target).delete();
        return existed && !target.exists();
    }

    private static byte[] encode(
            EvidenceType type, String key, byte[] payload, long fetchedAt, long expiresAt)
            throws Exception {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream(payload.length + 256);
        try (DataOutputStream out = new DataOutputStream(bytes)) {
            out.writeUTF(type.name());
            out.writeUTF(key);
            out.writeLong(fetchedAt);
            out.writeLong(expiresAt);
            out.writeInt(payload.length);
            out.write(payload);
        }
        return bytes.toByteArray();
    }

    private static Evidence decode(byte[] bytes) throws Exception {
        try (DataInputStream in = new DataInputStream(new ByteArrayInputStream(bytes))) {
            EvidenceType type = EvidenceType.valueOf(in.readUTF());
            String key = in.readUTF();
            long fetchedAt = in.readLong();
            long expiresAt = in.readLong();
            int length = in.readInt();
            if (length <= 0 || length > MAX_ITEM_BYTES || length > in.available()) {
                throw new SecurityException("Invalid cached evidence length");
            }
            byte[] payload = new byte[length];
            in.readFully(payload);
            if (payload.length != length || in.read() != -1) {
                throw new SecurityException("Invalid cached evidence envelope");
            }
            return new Evidence(type, key, payload, fetchedAt, expiresAt,
                    System.currentTimeMillis() <= expiresAt);
        }
    }

    private File targetFile(EvidenceType type, String key) {
        if (type == null || key == null || key.isBlank() || key.length() > 512) {
            throw new IllegalArgumentException("Invalid evidence cache identity");
        }
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest((type.name() + "\u0000" + key).getBytes(StandardCharsets.UTF_8));
            StringBuilder name = new StringBuilder();
            for (byte value : digest) name.append(String.format("%02x", value));
            return new File(directory, name + ".bin");
        } catch (Exception error) {
            throw new IllegalStateException(error);
        }
    }

    private static byte[] aad(EvidenceType type, String key) {
        return (FORMAT_VERSION + "|" + type.name() + "|" + key).getBytes(StandardCharsets.UTF_8);
    }

    private static SecretKey getOrCreateKey() throws Exception {
        KeyStore store = KeyStore.getInstance(KEYSTORE);
        store.load(null);
        if (store.containsAlias(KEY_ALIAS)) {
            return (SecretKey) store.getKey(KEY_ALIAS, null);
        }
        KeyGenerator generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, KEYSTORE);
        generator.init(new KeyGenParameterSpec.Builder(
                KEY_ALIAS, KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .setRandomizedEncryptionRequired(true)
                .build());
        return generator.generateKey();
    }

    private void enforceCacheBound() {
        File[] files = directory.listFiles((dir, name) -> name.endsWith(".bin"));
        if (files == null) return;
        long total = 0;
        for (File file : files) total += file.length();
        if (total <= MAX_CACHE_BYTES) return;
        Arrays.sort(files, (left, right) -> Long.compare(left.lastModified(), right.lastModified()));
        for (File file : files) {
            if (total <= MAX_CACHE_BYTES) break;
            long size = file.length();
            new AtomicFile(file).delete();
            if (!file.exists()) total -= size;
        }
    }
}
