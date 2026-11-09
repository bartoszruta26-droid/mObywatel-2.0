// SPDX-FileCopyrightText: 2026 bartoszruta26-droid
// SPDX-License-Identifier: CC0-1.0

package pl.gov.obywatel.android

/**
 * OWD application payload profile: RFC 6350 vCard 4.0 transported as the
 * authenticated plaintext inside the existing OWD AES-GCM envelope.
 */
internal object OwdVCardProtocol {
    const val CATEGORY = "vcard"

    fun encryptRecord(
        key: ByteArray,
        sessionId: String,
        record: ShareableRecord,
        nowEpochSeconds: Long = System.currentTimeMillis() / 1000
    ): OwdSecurity.EncryptedTransfer {
        val payload = VCardCodec.encodeOwdRecord(record).toByteArray(Charsets.UTF_8)
        return OwdSecurity.encrypt(key, sessionId, CATEGORY, payload, nowEpochSeconds)
    }

    fun decryptRecord(
        key: ByteArray,
        transfer: OwdSecurity.EncryptedTransfer
    ): ShareableRecord {
        require(transfer.category == CATEGORY) { "Pakiet OWD nie zawiera profilu vCard." }
        val plaintext = OwdSecurity.decrypt(key, transfer)
        return try {
            VCardCodec.decodeOwdRecord(String(plaintext, Charsets.UTF_8))
        } finally {
            plaintext.fill(0)
        }
    }
}
