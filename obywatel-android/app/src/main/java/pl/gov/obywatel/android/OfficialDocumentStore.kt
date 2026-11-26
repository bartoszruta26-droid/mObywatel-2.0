// SPDX-FileCopyrightText: 2026 bartoszruta26-droid
// SPDX-License-Identifier: CC0-1.0
package pl.gov.obywatel.android

import android.content.Context
import android.net.Uri
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.security.KeyStore
import java.util.UUID
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

internal object OfficialDocumentStore {
    private const val ALIAS = "obywatel_official_docs_aes"
    private const val INDEX = "official-documents.enc"
    private const val DIR = "official-document-files"
    private fun key(): SecretKey {
        val ks=KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        (ks.getKey(ALIAS,null) as? SecretKey)?.let { return it }
        return KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES,"AndroidKeyStore").apply {
            init(KeyGenParameterSpec.Builder(ALIAS,KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM).setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE).setRandomizedEncryptionRequired(true).build())
        }.generateKey()
    }
    private fun crypt(bytes:ByteArray, encrypt:Boolean):ByteArray {
        val c=Cipher.getInstance("AES/GCM/NoPadding")
        if(encrypt){ c.init(Cipher.ENCRYPT_MODE,key()); return c.iv+c.doFinal(bytes) }
        require(bytes.size>12); c.init(Cipher.DECRYPT_MODE,key(),GCMParameterSpec(128,bytes.copyOfRange(0,12))); return c.doFinal(bytes.copyOfRange(12,bytes.size))
    }
    private fun file(context:Context)=File(context.noBackupFilesDir,INDEX)
    fun load(context:Context):Map<String,Map<String,String>> = runCatching {
        val f=file(context); if(!f.exists()) return emptyMap()
        val o=JSONObject(String(crypt(f.readBytes(),false),Charsets.UTF_8)); val out=mutableMapOf<String,Map<String,String>>()
        o.keys().forEach { id -> val item=o.getJSONObject(id); val m=mutableMapOf<String,String>(); item.keys().forEach { k->m[k]=item.getString(k) }; out[id]=m }
        out
    }.getOrDefault(emptyMap())
    fun save(context:Context, data:Map<String,Map<String,String>>) {
        val root=JSONObject(); data.forEach { (id,values)-> val item=JSONObject(); values.forEach { (k,v)->item.put(k,v) }; root.put(id,item) }
        val target=file(context); val tmp=File(target.parentFile,target.name+".tmp"); tmp.writeBytes(crypt(root.toString().toByteArray(Charsets.UTF_8),true))
        if(!tmp.renameTo(target)){tmp.copyTo(target,true);tmp.delete()}
    }
    fun addAttachment(context:Context, uri:Uri, docId:String):String {
        val bytes=context.contentResolver.openInputStream(uri)?.use { input ->
            val out=java.io.ByteArrayOutputStream(); val buf=ByteArray(8192); var total=0
            while(true){val n=input.read(buf);if(n<0)break;total+=n;require(total<=25*1024*1024){"Plik przekracza limit 25 MB."};out.write(buf,0,n)}
            out.toByteArray()
        } ?: error("Nie można odczytać pliku.")
        require(bytes.isNotEmpty())
        val name=runCatching { context.contentResolver.query(uri,arrayOf(android.provider.OpenableColumns.DISPLAY_NAME),null,null,null)?.use { if(it.moveToFirst()) it.getString(0) else null } }.getOrNull() ?: "zalacznik"
        val lower=name.lowercase()
        require(lower.endsWith(".pdf") || lower.endsWith(".xml")) { "Do tego formularza można dołączyć pliki PDF lub XML." }
        if(lower.endsWith(".pdf")) require(bytes.size>=5 && String(bytes,0,5,Charsets.US_ASCII)=="%PDF-") { "Nieprawidłowy nagłówek PDF." }
        if(lower.endsWith(".xml")) {
            val text=String(bytes,Charsets.UTF_8)
            require(text.trimStart().startsWith("<?xml") || text.trimStart().startsWith("<")) { "Plik XML ma nieprawidłową strukturę początkową." }
            require(!text.contains("<!DOCTYPE",true) && !text.contains("<!ENTITY",true)) { "XML z deklaracją DTD/ENTITY jest odrzucany ze względów bezpieczeństwa." }
        }
        val dir=File(context.noBackupFilesDir,DIR).apply { mkdirs() }; val id=UUID.randomUUID().toString()
        File(dir,"$id.enc").writeBytes(crypt(bytes,true))
        val index=File(dir,"index.enc")
        val current=runCatching { if(index.exists()) JSONArray(String(crypt(index.readBytes(),false))) else JSONArray() }.getOrDefault(JSONArray())
        current.put(JSONObject().put("id",id).put("docId",docId).put("name",name).put("size",bytes.size))
        val tmp=File(dir,"index.tmp"); tmp.writeBytes(crypt(current.toString().toByteArray(Charsets.UTF_8),true)); if(!tmp.renameTo(index)){tmp.copyTo(index,true);tmp.delete()}
        return name
    }
    fun attachments(context:Context, docId:String):List<String> = runCatching {
        val f=File(File(context.noBackupFilesDir,DIR),"index.enc"); if(!f.exists()) return emptyList()
        val a=JSONArray(String(crypt(f.readBytes(),false))); (0 until a.length()).map { a.getJSONObject(it) }.filter { it.optString("docId")==docId }.map { it.optString("name") }
    }.getOrDefault(emptyList())

    fun exportArchive(context: Context): String {
        val root = JSONObject()
        load(context).forEach { (documentId, fields) ->
            val item = JSONObject()
            fields.forEach { (key, value) -> item.put(key, value) }
            root.put(documentId, item)
        }
        return root.toString()
    }

    fun importArchive(context: Context, raw: String) {
        val root = JSONObject(raw)
        val restored = linkedMapOf<String, Map<String, String>>()
        root.keys().forEach { documentId ->
            require(documentId.matches(Regex("[A-Za-z0-9_-]{1,96}"))) { "Nieprawidłowy identyfikator dokumentu." }
            val item = root.getJSONObject(documentId)
            val fields = linkedMapOf<String, String>()
            item.keys().forEach { key ->
                require(key.isNotBlank() && key.length <= 256) { "Nieprawidłowa nazwa pola dokumentu." }
                fields[key] = item.getString(key)
            }
            restored[documentId] = fields
        }
        save(context, restored)
    }
}

