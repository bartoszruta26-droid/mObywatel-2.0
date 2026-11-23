// SPDX-FileCopyrightText: 2026 mObywatel-2.0 contributors
// SPDX-License-Identifier: EUPL-1.2
package pl.gov.obywatel.pades;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class OfflineEvidenceStoreTest {

    @Test
    public void evidenceIsEncryptedStoredAndReadBackWithFreshness() throws Exception {
        OfflineEvidenceStore store = new OfflineEvidenceStore(
                InstrumentationRegistry.getInstrumentation().getTargetContext());
        String key = "test-" + System.nanoTime();
        byte[] payload = "signed-trusted-list-fixture".getBytes(java.nio.charset.StandardCharsets.UTF_8);
        long now = System.currentTimeMillis();
        try {
            store.save(OfflineEvidenceStore.EvidenceType.EU_LOTL, key, payload, now, now + 60_000L);
            OfflineEvidenceStore.Evidence evidence =
                    store.load(OfflineEvidenceStore.EvidenceType.EU_LOTL, key);
            assertNotNull(evidence);
            assertTrue(evidence.fresh());
            assertArrayEquals(payload, evidence.payload());
            assertEquals(now, evidence.fetchedAtEpochMillis());
        } finally {
            store.delete(OfflineEvidenceStore.EvidenceType.EU_LOTL, key);
        }
    }

    @Test
    public void expiredEvidenceRemainsAvailableButIsMarkedStale() throws Exception {
        OfflineEvidenceStore store = new OfflineEvidenceStore(
                InstrumentationRegistry.getInstrumentation().getTargetContext());
        String key = "stale-" + System.nanoTime();
        byte[] payload = new byte[] { 1, 2, 3, 4 };
        long now = System.currentTimeMillis();
        try {
            store.save(OfflineEvidenceStore.EvidenceType.CRL, key, payload, now - 120_000L, now - 60_000L);
            OfflineEvidenceStore.Evidence evidence =
                    store.load(OfflineEvidenceStore.EvidenceType.CRL, key);
            assertNotNull(evidence);
            assertFalse(evidence.fresh());
            assertArrayEquals(payload, evidence.payload());
        } finally {
            store.delete(OfflineEvidenceStore.EvidenceType.CRL, key);
        }
    }

    @Test
    public void rejectsOversizedEvidenceBeforeWriting() throws Exception {
        OfflineEvidenceStore store = new OfflineEvidenceStore(
                InstrumentationRegistry.getInstrumentation().getTargetContext());
        byte[] payload = new byte[OfflineEvidenceStore.MAX_ITEM_BYTES + 1];
        assertThrows(IllegalArgumentException.class, () -> store.save(
                OfflineEvidenceStore.EvidenceType.OCSP_RESPONSE,
                "oversized-" + System.nanoTime(), payload,
                System.currentTimeMillis(), System.currentTimeMillis() + 60_000L));
    }
}
