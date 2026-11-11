// SPDX-FileCopyrightText: 2026 bartoszruta26-droid
// SPDX-License-Identifier: CC0-1.0
package pl.gov.obywatel.android

import android.content.Context
import android.graphics.Bitmap
import java.util.Locale
import java.util.Date
import java.text.SimpleDateFormat
import androidx.compose.ui.draw.clip
import androidx.compose.ui.Alignment
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.border
import androidx.compose.foundation.background
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.EncodeHintType
import com.google.zxing.BarcodeFormat
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.security.KeyStore
import java.util.UUID
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

internal data class BrandAsset(val id: String, val owner: String, val kind: String, val label: String, val file: String)
internal data class BrandAssignment(val documentType: String, val kind: String, val assetId: String)

internal val documentBrandTargets = listOf(
    "identity" to "Dowód tożsamości",
    "passport" to "Paszport",
    "parliament" to "Legitymacja poselska",
    "training" to "Legitymacja uczestnika kursu",
    "student" to "Legitymacja ucznia / studenta",
    "church" to "Dokumenty i legitymacje kościelne",
    "church_bishop" to "Godność biskupia",
    "church_archbishop" to "Godność arcybiskupia",
    "church_cardinal" to "Godność kardynalska",
    "church_abbot" to "Godność opacka",
    "church_minister" to "Posługa / urząd kościelny",
    "church_missionary" to "Posługa misyjna",
    "professional" to "Dokumenty kwalifikacji zawodowych",
    "professional_medical" to "Godność / kwalifikacja medyczna",
    "professional_education" to "Godność / kwalifikacja edukacyjna",
    "professional_therapeutic" to "Godność / kwalifikacja terapeutyczna",
    "professional_other" to "Pozostałe godności zawodowe",
    "organization_foundation" to "Fundacja",
    "organization_church" to "Kościół / wspólnota",
    "organization_company" to "Przedsiębiorstwo / osoba prawna",
    "organization_association" to "Stowarzyszenie / organizacja społeczna",
    "family" to "Dokumenty rodowe",
    "entity" to "Dokumenty organizacji i podmiotów",
    "business" to "Dokumenty firmowe i przedsiębiorstwa",
    "registry" to "Odpisy i dokumenty rejestrowe",
    "signed_pdf" to "Dokumenty PDF i podpisy",
    "imported" to "Dokumenty własne — pliki",
    "loyalty_cards" to "Karty lojalnościowe",
    "official" to "Pozostałe dokumenty urzędowe",
    "birth_extract" to "Akt urodzenia",
    "marriage_extract" to "Akt małżeństwa",
    "death_extract" to "Odpis aktu zgonu",
    "large_family" to "Karta Dużej Rodziny",
    "blood_donor" to "Legitymacja Honorowego Dawcy Krwi",
    "disability_card" to "Legitymacja osoby z niepełnosprawnością",
    "vehicle_registration" to "Dowód rejestracyjny pojazdu",
    "insurance_vehicle" to "Ubezpieczenie OC pojazdu",
    "insurance_professional" to "Ubezpieczenie OC zawodowe",
    "insurance_private" to "Ubezpieczenie OC w życiu prywatnym",
    "insurance_business" to "Ubezpieczenie OC działalności",
    "criminal_record" to "Zaświadczenie o niekaralności",
    "mission_travel" to "Dokument podróży służbowej / misyjnej",
    "ehic" to "Europejska Karta Ubezpieczenia Zdrowotnego",
    "eu_education" to "Zaświadczenie o zgodności kwalifikacji z UE",
    "profile" to "Dane osobowe i zdjęcie profilowe",
    "disability" to "Orzeczenia i zaświadczenia",
    "loyalty" to "Karty lojalnościowe",
    "bank_accounts" to "Konta bankowe",
    "children" to "Dokumentacja dzieci",
    "vaccines" to "Karta szczepień",
    "civil" to "Dokumenty cywilne"
)

internal object DocumentBrandStore {
    private const val ALIAS = "document_brand_assets_aes"
    private const val INDEX = "document-brand-assets.enc"
    private const val ASSIGNMENTS_INDEX = "document-brand-assignments.enc"
    private const val MAX_IMAGE = 12 * 1024 * 1024
    private val kinds = listOf("Fotografia", "Logo", "Godło", "Herb", "Flaga", "Pieczęć", "Wzór podpisu odręcznego", "Tło dokumentu", "Znak wodny", "Grafika hologramu / zabezpieczenia")
    fun assetKinds() = kinds
    private fun dir(context: Context) = File(context.noBackupFilesDir, "document-brand-assets").apply { mkdirs() }
    private fun key(): SecretKey {
        val ks = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        (ks.getKey(ALIAS, null) as? SecretKey)?.let { return it }
        return KeyGenerator.getInstance("AES", "AndroidKeyStore").apply {
            init(android.security.keystore.KeyGenParameterSpec.Builder(ALIAS,
                android.security.keystore.KeyProperties.PURPOSE_ENCRYPT or android.security.keystore.KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(android.security.keystore.KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(android.security.keystore.KeyProperties.ENCRYPTION_PADDING_NONE)
                .setRandomizedEncryptionRequired(true).build())
        }.generateKey()
    }
    private fun crypt(bytes: ByteArray, encrypt: Boolean): ByteArray {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        if (encrypt) { cipher.init(Cipher.ENCRYPT_MODE, key()); return cipher.iv + cipher.doFinal(bytes) }
        require(bytes.size > 28)
        cipher.init(Cipher.DECRYPT_MODE, key(), GCMParameterSpec(128, bytes.copyOfRange(0, 12)))
        return cipher.doFinal(bytes.copyOfRange(12, bytes.size))
    }
    private val builtInAssets = listOf(
        BrandAsset("builtin_ciechomscy", "Katalog heraldyczny", "Herb", "Ciechomscy — wąż i jabłko", "builtin:heraldry/ciechomscy-wez-jablko.svg"),
        BrandAsset("builtin_rodu_dab", "Katalog heraldyczny", "Herb", "Herb rodowy — dąb", "builtin:heraldry/herb-rodu-klasyczny.svg"),
        BrandAsset("builtin_rodu_lilia", "Katalog heraldyczny", "Herb", "Herb rodowy — lilia", "builtin:heraldry/herb-rodu-lilia.svg"),
        BrandAsset("builtin_biskupi", "Katalog heraldyczny", "Herb", "Herb biskupi", "builtin:heraldry/herb-biskupi.svg"),
        BrandAsset("builtin_biskupi_pelny", "Katalog heraldyczny", "Herb", "Herb biskupi — pełny", "builtin:heraldry/herb-biskupi-pelny.svg"),
        BrandAsset("builtin_tarcza_biskupia", "Katalog heraldyczny", "Herb", "Tarcza herbu biskupiego", "builtin:heraldry/tarcza-herbu-biskupiego.svg"),
        BrandAsset("builtin_arcybiskupi", "Katalog heraldyczny", "Herb", "Herb arcybiskupi", "builtin:heraldry/herb-arcybiskupi.svg"),
        BrandAsset("builtin_kardynalski", "Katalog heraldyczny", "Herb", "Herb kardynalski", "builtin:heraldry/herb-kardynalski.svg"),
        BrandAsset("builtin_opacki", "Katalog heraldyczny", "Herb", "Herb opacki", "builtin:heraldry/herb-opacki.svg"),
        BrandAsset("builtin_rycerski", "Katalog heraldyczny", "Herb", "Herb rycerski", "builtin:heraldry/herb-rycerski.svg"),
        BrandAsset("builtin_medyczny", "Katalog heraldyczny", "Logo", "Znak medyczny", "builtin:heraldry/herb-medyczny.svg"),
        BrandAsset("builtin_naukowy", "Katalog heraldyczny", "Logo", "Znak naukowy", "builtin:heraldry/herb-naukowy.svg"),
        BrandAsset("builtin_fundacyjny", "Katalog heraldyczny", "Logo", "Znak fundacyjny", "builtin:heraldry/herb-fundacyjny.svg"),
        BrandAsset("builtin_misjonarski", "Katalog heraldyczny", "Logo", "Znak misyjny", "builtin:heraldry/herb-misjonarski.svg"),
        BrandAsset("builtin_samorzadowy", "Katalog heraldyczny", "Godło", "Znak samorządowy", "builtin:heraldry/herb-samorzadowy.svg"),
        BrandAsset("builtin_zaslugi", "Katalog heraldyczny", "Herb", "Znak zasługi", "builtin:heraldry/herb-zaslugi.svg")
    )
    @Synchronized fun list(context: Context): List<BrandAsset> {
        val stored = runCatching {
            val f = File(dir(context), INDEX)
            if (!f.exists()) emptyList() else {
                val a = JSONArray(String(crypt(f.readBytes(), false), Charsets.UTF_8))
                (0 until a.length()).map { i -> a.getJSONObject(i).let { o ->
                    BrandAsset(o.getString("id"), o.getString("owner"), o.getString("kind"), o.getString("label"), o.getString("file"))
                } }
            }
        }.getOrDefault(emptyList())
        return builtInAssets + stored
    }
    private fun save(context: Context, assets: List<BrandAsset>) {
        val a = JSONArray()
        assets.forEach { x -> a.put(JSONObject().apply { put("id",x.id); put("owner",x.owner); put("kind",x.kind); put("label",x.label); put("file",x.file) }) }
        File(dir(context), INDEX).writeBytes(crypt(a.toString().toByteArray(Charsets.UTF_8), true))
    }
    @Synchronized fun assignments(context: Context): List<BrandAssignment> = runCatching {
        val f = File(dir(context), ASSIGNMENTS_INDEX)
        if (!f.exists()) return emptyList()
        val a = JSONArray(String(crypt(f.readBytes(), false), Charsets.UTF_8))
        (0 until a.length()).map { i -> a.getJSONObject(i).let { o ->
            BrandAssignment(o.getString("documentType"), o.getString("kind"), o.getString("assetId"))
        } }
    }.getOrDefault(emptyList())
    @Synchronized fun assign(context: Context, documentType: String, kind: String, assetId: String) {
        require(documentBrandTargets.any { it.first == documentType }) { "Nieznany rodzaj dokumentu." }
        require(kind in kinds) { "Nieznany rodzaj zasobu." }
        require(list(context).any { it.id == assetId && it.kind == kind }) { "Wybrany zasób nie istnieje lub ma inny rodzaj." }
        val next = assignments(context).filterNot { it.documentType == documentType && it.kind == kind } +
            BrandAssignment(documentType, kind, assetId)
        val a = JSONArray()
        next.forEach { x -> a.put(JSONObject().put("documentType", x.documentType).put("kind", x.kind).put("assetId", x.assetId)) }
        File(dir(context), ASSIGNMENTS_INDEX).writeBytes(crypt(a.toString().toByteArray(Charsets.UTF_8), true))
    }
    @Synchronized fun unassign(context: Context, documentType: String, kind: String) {
        val a = JSONArray()
        assignments(context).filterNot { it.documentType == documentType && it.kind == kind }.forEach { x ->
            a.put(JSONObject().put("documentType", x.documentType).put("kind", x.kind).put("assetId", x.assetId))
        }
        File(dir(context), ASSIGNMENTS_INDEX).writeBytes(crypt(a.toString().toByteArray(Charsets.UTF_8), true))
    }
    private fun imageById(context: Context, assetId: String): android.graphics.Bitmap? = runCatching {
        val asset = list(context).firstOrNull { it.id == assetId } ?: return null
        val bytes = if (asset.file.startsWith("builtin:")) {
            context.assets.open(asset.file.removePrefix("builtin:")).use { it.readBytes() }
        } else {
            crypt(File(dir(context), asset.file).readBytes(), false)
        }
        decodeImage(bytes)
    }.getOrNull()
    private fun assignmentKeys(documentType: String, owner: String): List<String> {
        val lower = owner.lowercase(java.util.Locale.ROOT)
        val role = when {
            documentType.startsWith("church_bishop") || documentType == "church_orders" -> "church_bishop"
            documentType.startsWith("church_archbishop") -> "church_archbishop"
            documentType.startsWith("church_cardinal") -> "church_cardinal"
            documentType.startsWith("church_abbot") -> "church_abbot"
            documentType.startsWith("church_minister") || documentType == "church_jurisdiction" -> "church_minister"
            documentType.startsWith("missionary_") -> "church_missionary"
            documentType == "training" || documentType == "student" -> "professional_education"
            documentType == "professional" && listOf("lekar", "medycz", "fizjoter", "pielęg", "diagnost").any { lower.contains(it) } -> "professional_medical"
            documentType == "professional" && listOf("nauczyc", "pedagog", "wykładow", "szkol").any { lower.contains(it) } -> "professional_education"
            documentType == "professional" && listOf("terapeut", "masaż", "masaz", "naturop", "zielarz").any { lower.contains(it) } -> "professional_therapeutic"
            documentType == "professional" -> "professional_other"
            documentType in setOf("entity", "business", "registry") && listOf("fundac").any { lower.contains(it) } -> "organization_foundation"
            documentType in setOf("entity", "business", "registry") && listOf("koś", "kosci", "parafi", "wspólnot").any { lower.contains(it) } -> "organization_church"
            documentType in setOf("entity", "business", "registry") && listOf("spół", "spol", "firma", "przedsięb").any { lower.contains(it) } -> "organization_company"
            documentType in setOf("entity", "business", "registry") && listOf("stowarz", "organizac").any { lower.contains(it) } -> "organization_association"
            documentType == "family" -> "organization_family"
            else -> ""
        }
        val fallback = when {
            documentType.startsWith("church_") || documentType.startsWith("ecumenical_") || documentType.startsWith("missionary_") -> "church"
            documentType.startsWith("organization_") -> "entity"
            else -> ""
        }
        val candidates = if (documentType == "professional" || documentType in setOf("entity", "business", "registry"))
            listOf(role, documentType, fallback)
        else listOf(documentType, role, fallback)
        return candidates.filter { it.isNotBlank() }.distinct()
    }
    fun imageForDocument(context: Context, documentType: String, owner: String, kind: String): android.graphics.Bitmap? {
        val all = assignments(context)
        val assigned = assignmentKeys(documentType, owner).firstNotNullOfOrNull { key ->
            all.lastOrNull { it.documentType == key && it.kind == kind }
        }
        return assigned?.let { imageById(context, it.assetId) } ?: image(context, owner, kind)
    }
    fun preview(context: Context, assetId: String): android.graphics.Bitmap? = imageById(context, assetId)
    private fun decodeImage(bytes: ByteArray): android.graphics.Bitmap {
        BitmapFactory.decodeByteArray(bytes,0,bytes.size)?.let { return it }
        val svg = com.caverock.androidsvg.SVG.getFromString(String(bytes, Charsets.UTF_8))
        val picture = svg.renderToPicture()
        val bitmap = android.graphics.Bitmap.createBitmap(512, 512, android.graphics.Bitmap.Config.ARGB_8888)
        val sourceWidth = picture.width.coerceAtLeast(1).toFloat()
        val sourceHeight = picture.height.coerceAtLeast(1).toFloat()
        val scale = minOf(512f / sourceWidth, 512f / sourceHeight)
        val canvas = Canvas(bitmap)
        canvas.translate((512f - sourceWidth * scale) / 2f, (512f - sourceHeight * scale) / 2f)
        canvas.scale(scale, scale)
        canvas.drawPicture(picture)
        return bitmap
    }
    @Synchronized fun import(context: Context, uri: Uri, owner: String, kind: String, label: String) {
        val bytes = context.contentResolver.openInputStream(uri)?.use { input ->
            val out = java.io.ByteArrayOutputStream(); val buffer = ByteArray(8192); var total = 0
            while (true) { val n=input.read(buffer); if(n<0) break; total+=n; require(total<=MAX_IMAGE) { "Plik przekracza limit 12 MB." }; out.write(buffer,0,n) }
            out.toByteArray()
        } ?: error("Nie można odczytać pliku.")
        decodeImage(bytes).recycle()
        val isSvg = String(bytes, 0, minOf(bytes.size, 256), Charsets.UTF_8).contains("<svg", ignoreCase = true)
        val id=UUID.randomUUID().toString(); val file=id+(if (isSvg) ".svg.enc" else ".asset.enc")
        File(dir(context),file).writeBytes(crypt(bytes,true))
        save(context,list(context).filterNot { it.file.startsWith("builtin:") }+BrandAsset(id,owner.trim(),kind,label.trim().ifBlank { kind },file))
    }
    fun image(context: Context, owner: String, kind: String) : android.graphics.Bitmap? = runCatching {
        val asset=list(context).lastOrNull { it.owner.equals(owner,true) && it.kind==kind } ?: return null
        val bytes = if (asset.file.startsWith("builtin:")) {
            context.assets.open(asset.file.removePrefix("builtin:")).use { it.readBytes() }
        } else crypt(File(dir(context),asset.file).readBytes(),false)
        decodeImage(bytes)
    }.getOrNull()
    @Synchronized fun delete(context: Context, id: String) {
        val old=list(context)
        val selected=old.firstOrNull { it.id==id } ?: return
        if (selected.file.startsWith("builtin:")) return
        File(dir(context),selected.file).delete()
        save(context,old.filterNot { it.id==id || it.file.startsWith("builtin:") })
        val retained = assignments(context).filterNot { it.assetId == id }
        val index = JSONArray()
        retained.forEach { item -> index.put(JSONObject().put("documentType",item.documentType).put("kind",item.kind).put("assetId",item.assetId)) }
        File(dir(context),ASSIGNMENTS_INDEX).writeBytes(crypt(index.toString().toByteArray(Charsets.UTF_8),true))
    }
}

@Composable
internal fun AssignedDocumentBrandPreview(documentType: String, owner: String, title: String, subtitle: String = "") {
    val context = LocalContext.current
    val brand = remember(documentType, owner) {
        Pair(
            DocumentBrandStore.imageForDocument(context, documentType, owner, "Logo"),
            DocumentBrandStore.imageForDocument(context, documentType, owner, "Herb")
                ?: DocumentBrandStore.imageForDocument(context, documentType, owner, "Godło")
        )
    }
    Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Row(Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            if (brand.first != null) Image(brand.first!!.asImageBitmap(), contentDescription = "Logo przypisane do dokumentu", modifier = Modifier.size(42.dp), contentScale = ContentScale.Fit)
            if (brand.second != null) Image(brand.second!!.asImageBitmap(), contentDescription = "Herb lub godło przypisane do dokumentu", modifier = Modifier.size(42.dp), contentScale = ContentScale.Fit)
            Column(Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.SemiBold, color = Color(0xFF173B58))
                if (subtitle.isNotBlank()) Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Color(0xFF667788))
                if (brand.first == null && brand.second == null) Text("Brak przypisanego logo, herbu lub godła", style = MaterialTheme.typography.bodySmall, color = Color(0xFF667788))
            }
        }
    }
}

private fun documentTypeForTitle(title: String): String {
    val value = title.lowercase(Locale.ROOT)
    return when {
        value.contains("akt urodzenia") -> "birth_extract"
        value.contains("akt małżeństwa") || value.contains("akt malzenstwa") -> "marriage_extract"
        value.contains("akt zgonu") || value.contains("odpis aktu zgonu") -> "death_extract"
        value.contains("karta dużej rodziny") || value.contains("karta duzej rodziny") -> "large_family"
        value.contains("honorowego dawcy krwi") || value.contains("dawcy krwi") -> "blood_donor"
        value.contains("legitymacja osoby z niepełnosprawnością") || value.contains("legitymacja osoby z niepelnosprawnoscia") -> "disability_card"
        value.contains("dowód rejestracyjny") || value.contains("dowod rejestracyjny") -> "vehicle_registration"
        value.contains("oc pojazdu") -> "insurance_vehicle"
        value.contains("oc zawod") -> "insurance_professional"
        value.contains("oc w życiu prywatnym") || value.contains("oc w zyciu prywatnym") -> "insurance_private"
        value.contains("oc działalności") || value.contains("oc dzialalnosci") -> "insurance_business"
        value.contains("niekaralności") || value.contains("niekaralnosci") -> "criminal_record"
        value.contains("podróży służbowej") || value.contains("podrozy sluzbowej") || value.contains("misji międzynarodowej") -> "mission_travel"
        value.contains("europejska karta ubezpieczenia zdrowotnego") || value.contains("ekuz") -> "ehic"
        value.contains("zgodności kwalifikacji") || value.contains("zgodnosci kwalifikacji") || value.contains("zgodności wykształcenia") || value.contains("zgodnosci wyksztalcenia") -> "eu_education"
        value.contains("odpis") && value.contains("rejestr") -> "registry"
        value.contains("spół") || value.contains("spol") || value.contains("firm") || value.contains("przedsięb") || value.contains("przedsieb") -> "business"
        value.contains("fundac") -> "organization_foundation"
        value.contains("stowarz") -> "organization_association"
        value.contains("instytucja szkoleniowa") || value.contains("kwalifikac") || value.contains("zawodow") -> "professional_education"
        value.contains("jednostka lokalna") || value.contains("rejestr") -> "registry"
        value.contains("pdf") || value.contains("podpis elektroniczn") -> "signed_pdf"
        value.contains("importowan") || value.contains("własne — pliki") -> "imported"
        value.contains("karta lojal") || value.contains("lojalności") -> "loyalty_cards"
        value.contains("paszport") -> "passport"
        value.contains("rodow") || value.contains("rodu") || value.contains("rodzinn") -> "family"
        value.contains("poselsk") || value.contains("parlament") -> "parliament"
        value.contains("kursant") || value.contains("uczestnika kurs") || value.contains("kursu") -> "training"
        value.contains("student") || value.contains("uczn") || value.contains("szkoln") -> "student"
        value.contains("arcybiskup") -> "church_archbishop"
        value.contains("kardynał") || value.contains("kardynal") -> "church_cardinal"
        value.contains("biskup") -> "church_bishop"
        value.contains("opat") -> "church_abbot"
        value.contains("misjon") || value.contains("pdm") || value.contains("pum") -> "church_missionary"
        value.contains("posług") || value.contains("urząd kościel") || value.contains("urzad kosciel") -> "church_minister"
        value.contains("kościel") || value.contains("kosciel") -> "church"
        value.contains("orzeczen") || value.contains("niepełnospraw") || value.contains("niepelnospraw") ||
            value.contains("zaświadczen") || value.contains("zaswiadczen") -> "disability"
        value.contains("lojalności") || value.contains("lojalnosci") -> "loyalty"
        value.contains("rachunk") || value.contains("bankow") -> "bank_accounts"
        value.contains("szczepien") || value.contains("szczepień") -> "vaccines"
        value.contains("dzieci") || value.contains("dziecka") -> "children"
        value.contains("cywil") || value.contains("urodzen") || value.contains("małżeń") ||
            value.contains("malzen") || value.contains("zgon") || value.contains("meldun") -> "civil"
        value.contains("medycz") || value.contains("fizjoter") || value.contains("pielęg") || value.contains("diagnost") -> "professional_medical"
        value.contains("nauczyc") || value.contains("pedagog") || value.contains("edukac") || value.contains("szkol") -> "professional_education"
        value.contains("terapeut") || value.contains("masaż") || value.contains("masaz") || value.contains("naturop") -> "professional_therapeutic"
        value.contains("zawod") || value.contains("kwalifikac") -> "professional"
        value.contains("fundac") -> "organization_foundation"
        value.contains("stowarz") -> "organization_association"
        value.contains("kośció") || value.contains("koscio") || value.contains("parafi") || value.contains("wspólnot") -> "organization_church"
        value.contains("spół") || value.contains("spol") || value.contains("przedsięb") || value.contains("przedsieb") -> "organization_company"
        value.contains("podmiot") || value.contains("organizac") -> "entity"
        value.contains("tożsamo") || value.contains("tozsamos") || value.contains("dowód") || value.contains("dowod") -> "identity"
        else -> "official"
    }
}

private fun isFamilyDocument(title: String): Boolean {
    val t = title.lowercase(Locale.ROOT)
    return t.contains("rodow") || t.contains("rodu") || t.contains("rodzinn")
}

private fun isChurchDocument(title: String): Boolean {
    val t = title.lowercase(Locale.ROOT)
    return listOf("kościel", "kosciel", "papiesk", "papież", "papiez", "misjon", "sakrament", "kanonicz", "biskup", "parafi", "szafarz", "liturg", "święce", "swiece", "konsekr", "jurysdykc", "diecez", "ekumen").any { t.contains(it) }
}

private fun defaultDocumentFlag(context: Context, title: String): Bitmap? =
    BitmapFactory.decodeResource(context.resources, R.drawable.institutions_polish_flag)

private fun defaultDocumentEmblem(context: Context, title: String): Bitmap? {
    val normalized = title.lowercase(Locale.ROOT)
    val assetId = when {
        isFamilyDocument(title) -> "builtin_rodu_dab"
        isChurchDocument(title) && listOf("biskup", "arcybiskup", "kardynał", "kardynal").any { normalized.contains(it) } -> "builtin_biskupi_pelny"
        isChurchDocument(title) -> "builtin_misjonarski"
        normalized.contains("medycz") || normalized.contains("pacjent") || normalized.contains("zdrow") -> "builtin_medyczny"
        normalized.contains("nauk") || normalized.contains("doktor") -> "builtin_naukowy"
        normalized.contains("fundac") || normalized.contains("organizac") -> "builtin_fundacyjny"
        else -> ""
    }
    val selected = assetId.takeIf { it.isNotBlank() }?.let { DocumentBrandStore.preview(context, it) }
    return selected ?: BitmapFactory.decodeResource(context.resources, R.drawable.institutions_eagle_emblem)
}

private fun defaultDocumentBackground(context: Context, title: String): Bitmap? {
    val normalized = title.lowercase(Locale.ROOT)
    val resourceId = when {
        // Dokumenty rodowe i rodzinne.
        isFamilyDocument(title) && (normalized.contains("paszport") || normalized.contains("podróż") || normalized.contains("podroz")) ->
            R.drawable.doc_02_paszport_rodowy_jasny
        isFamilyDocument(title) && (normalized.contains("orzeczenie") || normalized.contains("niepełnospraw") || normalized.contains("niepelnospraw")) ->
            R.drawable.doc_03_orzeczenie_rodowe_jasne
        isFamilyDocument(title) -> R.drawable.doc_01_karta_rodowa_jasna

        // Dokumenty tożsamości i podróży.
        normalized.contains("paszport") || normalized.contains("passport") -> R.drawable.doc_02_paszport_rodowy_jasny
        normalized.contains("dowód") || normalized.contains("dowod") ||
            normalized.contains("tożsamo") || normalized.contains("tozsamos") ||
            normalized.contains("profil") || normalized.contains("obywatel") ->
            R.drawable.doc_01_dowod_tozsamosci_obywatelski

        // Legitymacje i dokumenty zawodowe.
        normalized.contains("poselsk") || normalized.contains("senatorsk") || normalized.contains("parlament") ->
            R.drawable.documents_deputy_legitimacy
        normalized.contains("uczn") || normalized.contains("student") || normalized.contains("szkoln") ||
            normalized.contains("doktorant") ->
            R.drawable.documents_course_legitimacy
        normalized.contains("kursant") || normalized.contains("kursu") || normalized.contains("szkoleni") ||
            normalized.contains("ukończenia kurs") || normalized.contains("ukonczenia kurs") ->
            R.drawable.documents_student_legitimacy
        normalized.contains("zawodow") || normalized.contains("pracown") ||
            normalized.contains("wykonywania zawodu") || normalized.contains("kwalifikac") ->
            R.drawable.doc_02_legitymacja_zawodowa

        // Zdrowie, niepełnosprawność i dokumentacja medyczna.
        normalized.contains("niepełnospraw") || normalized.contains("niepelnospraw") ||
            normalized.contains("orzeczenie") || normalized.contains("orzeczenia") ||
            normalized.contains("disability") ->
            R.drawable.doc_02_orzeczenie_niepelnosprawnosc_niebieskie
        normalized.contains("szczep") || normalized.contains("vaccin") ->
            R.drawable.doc_04_ksiazeczka_szczepien
        normalized.contains("pacjent") || normalized.contains("medycz") || normalized.contains("zdrow") ->
            R.drawable.art_05_karta_pacjenta

        // Dokumenty kościelne i misyjne.
        normalized.contains("misjon") || normalized.contains("papiesk") || normalized.contains("pdm") ||
            normalized.contains("pum") -> R.drawable.doc_03_misjonarstwo_pdm_pum
        isChurchDocument(title) || normalized.contains("kościo") || normalized.contains("kosció") ->
            R.drawable.church_communities

        // Dokumenty cywilne, rejestrowe, organizacyjne i zaświadczenia.
        normalized.contains("fundac") || normalized.contains("stowarz") || normalized.contains("firm") ||
            normalized.contains("spół") || normalized.contains("spol") || normalized.contains("organizac") ||
            normalized.contains("rejestr podmiot") || normalized.contains("odpis rejestru") ->
            R.drawable.doc_06_przynaleznosc_rodowa_organizacyjna
        normalized.contains("cywil") || normalized.contains("urodzen") || normalized.contains("małżeń") ||
            normalized.contains("malzen") || normalized.contains("zgon") || normalized.contains("meldun") ||
            normalized.contains("urzęd") || normalized.contains("urzed") || normalized.startsWith("akt ") ||
            normalized.startsWith("odpis ") || normalized.contains("rejestr") ->
            R.drawable.bg_document_civic_identity
        normalized.contains("certyfikat") || normalized.contains("ukończenia") || normalized.contains("ukonczenia") ->
            R.drawable.doc_03_certyfikat_ukonczenia_kursu
        normalized.contains("zaświadczen") || normalized.contains("zaswiadczen") ->
            R.drawable.doc_04_zaswiadczenie
        normalized.contains("lojalno") || normalized.contains("karta") ->
            R.drawable.documents_document_wallet_banner
        else -> R.drawable.documents_document_wallet_banner
    }
    return BitmapFactory.decodeResource(context.resources, resourceId)
}

private fun createDocumentQr(payload: String): Bitmap? = runCatching {
    val hints = mapOf(
        EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.M,
        EncodeHintType.CHARACTER_SET to "UTF-8",
        EncodeHintType.MARGIN to 1
    )
    val matrix = QRCodeWriter().encode(payload, BarcodeFormat.QR_CODE, 360, 360, hints)
    Bitmap.createBitmap(matrix.width, matrix.height, Bitmap.Config.ARGB_8888).also { bitmap ->
        for (y in 0 until matrix.height) for (x in 0 until matrix.width) {
            bitmap.setPixel(x, y, if (matrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
        }
    }
}.getOrNull()

@Composable
internal fun DocumentIdentityTemplate(
    title: String,
    values: Map<String, String>,
    owner: String = "",
    photoOverride: Bitmap? = null,
    validity: String = values.entries.firstOrNull { it.key.contains("status", true) }?.value.orEmpty(),
    documentType: String = documentTypeForTitle(title),
    showReverse: Boolean = false,
    assetRevision: Int = 0
) {
    val context = LocalContext.current
    val settings = rememberLegitimacyPreviewSettings(documentType).value
    val photo = remember(owner, photoOverride, assetRevision) {
        photoOverride ?: DocumentBrandStore.image(context, owner, "Fotografia")
    }
    val fieldOptions = credentialFieldOptions(documentType)
    fun stableFieldKey(label: String): String {
        val value = label.lowercase(Locale.ROOT)
        return when {
            value.contains("miejsce w rodzie") -> "familyRole"
            value.contains("nazwa rodu") -> "familyName"
            value.contains("linia") || value.contains("gałąź") -> "familyLine"
            value.contains("nazwa programu") -> "loyaltyName"
            value.contains("nazwa rachunku") -> "accountName"
            value.contains("relacja") -> "childRelationship"
            value.contains("opiekun") -> "guardianName"
            value.contains("data szczepienia") -> "vaccineDate"
            value.contains("dawka") -> "vaccineDose"
            value.contains("seria") -> "vaccineBatch"
            value.contains("podmiot wykonujący") -> "vaccineIssuer"
            value.contains("specjalność") -> "professionalSpeciality"
            value.contains("zakres kwalifikacji") -> "professionalQualification"
            value.contains("stanowisko") -> "contactTitle"
            value.contains("strona internetowa") -> "contactUrl"
            value.contains("nazwa organizacji") && documentType == "profile" -> "contactOrganization"
            value.contains("rodzaj aktu") -> "civilActType"
            value.contains("rodzaj odpisu") -> "civilExtractType"
            value.contains("data i miejsce zawarcia małżeństwa") || value.contains("data zawarcia małżeństwa") -> "marriageDate"
            value.contains("data i miejsce zgonu") || value.contains("data zgonu") -> "deathDate"
            value.contains("osoba zmarła") || value.contains("osoba zmarla") -> "deceasedName"
            value.contains("małżonkowie") || value.contains("malzonkowie") -> "spouseNames"
            value.contains("liczba oddanych litrów") || value.contains("liczba oddanych litrow") -> "bloodDonorLitres"
            value.contains("stopień") -> if (documentType == "blood_donor") "bloodDonorTitle" else "disabilityDegree"
            value.contains("data przyznania") -> "bloodDonorIssueDate"
            value.contains("numer rejestracyjny") -> "vehicleRegistrationNumber"
            value == "vin" || value.contains(" vin") -> "vehicleVin"
            value.contains("marka i model") -> "vehicleMakeModel"
            value.contains("numer dowodu") -> "vehicleDocumentNumber"
            value.contains("posiadacz") && documentType in setOf("loyalty", "loyalty_cards") -> "loyaltyHolder"
            value.contains("właściciel") || value.contains("współwłaściciel") -> if (documentType == "vehicle_registration") "vehicleOwner" else "accountOwner"
            value.contains("data pierwszej rejestracji") -> "vehicleFirstRegistration"
            value.contains("termin badania technicznego") -> "vehicleInspectionDue"
            value.contains("ubezpieczyciel") -> "insuranceProvider"
            value.contains("numer polisy") -> "insurancePolicyNumber"
            value.contains("ubezpieczony") -> "insuranceInsured"
            value.contains("zakres ochrony") || value.contains("zakres czynności objętych ochroną") -> "insuranceScope"
            value.contains("okres ochrony od") -> "insuranceStart"
            value.contains("okres ochrony do") -> "insuranceEnd"
            value.contains("suma gwarancyjna") -> "insuranceCoverageAmount"
            value.contains("wyłączenia") -> "insuranceExclusions"
            value.contains("data opłacenia składki") -> "insurancePremiumPaidDate"
            value.contains("podstawa delegowania") -> "missionBasis"
            value.contains("cel i zakres misji") -> "missionScope"
            value.contains("ubezpieczenie i osoba kontaktowa") -> "missionInsurance"
            value.contains("wizy") || value.contains("akredytacje") -> "missionPermits"
            value.contains("delegowany") -> "missionHolder"
            value.contains("numer decyzji") || value.contains("numer delegacji") -> "missionNumber"
            value.contains("funkcja") && documentType == "mission_travel" -> "missionRole"
            value.contains("organizacja") && documentType == "mission_travel" -> "missionOrganization"
            value.contains("państwo docelowe") -> if (documentType == "eu_education") "educationDestination" else "missionDestination"
            value.contains("numer dyplomu") || value.contains("numer świadectwa") -> "educationDiplomaNumber"
            value.contains("instytucja kształcąca") -> "educationInstitution"
            value.contains("poziom i czas kształcenia") || value.contains("poziom kształcenia") -> "educationLevel"
            value.contains("podstawa prawna") || value.contains("dyrektywa") -> "educationLegalBasis"
            value.contains("zakres potwierdzenia") -> "educationScope"
            value.contains("numer sprawy") -> if (documentType == "eu_education") "educationCaseNumber" else "disabilityCaseNumber"
            value.contains("numer zaświadczenia") -> "criminalRecordNumber"
            value.contains("zakres informacji") -> "criminalRecordScope"
            value.contains("punkt informacyjny") || value.contains("organ rejestrujący") -> "issuer"
            value.contains("kierunek") -> "studentProgramme"
            value.contains("wydział") -> "studentFaculty"
            value.contains("rok akademicki") -> "studentAcademicYear"
            value.contains("rok i semestr") -> "studentYear"
            value.contains("poziom studiów") -> "studentLevel"
            value.contains("profil") || value.contains("forma studiów") -> "studentProfile"
            value.contains("status uczestnictwa") -> "trainingStatus"
            value.contains("status studenta") -> "studentStatus"
            value.contains("status weryfikacji") || value == "status" || value.startsWith("status ") -> if (documentType.startsWith("insurance")) "insuranceStatus" else "status"
            value.contains("miejsce w rodzie") -> "familyRole"
            value.contains("nazwa rodu") || value.contains("linia") -> "familyName"
            value.contains("początek mandatu") -> "mpStart"
            value.contains("koniec mandatu") -> "mpEnd"
            value.contains("kadencj") -> "mpTerm"
            value.contains("okręg") -> "mpDistrict"
            value.contains("numer karty") -> when (documentType) {
                "loyalty", "loyalty_cards" -> "loyaltyNumber" "large_family" -> "largeFamilyNumber"; "blood_donor" -> "bloodDonorNumber"; "disability_card" -> "disabilityCardNumber"; "ehic" -> "ehicNumber"; else -> "documentNumber" }
            value.contains("typ karty") || value.startsWith("typ:") -> "largeFamilyType"
            value.contains("krs") -> "entityKrs"
            value.contains("nip") -> "entityNip"
            value.contains("regon") -> "entityRegon"
            value.contains("reprezent") || value.contains("zarząd") || value.contains("zarzad") -> "entityRepresentative"
            value.contains("imię") || value.contains("imie") -> if (documentType == "student") "studentNames" else "firstNames"
            value.contains("nazwisko") -> "lastName"
            value.contains("pesel") || value.contains("numer identyfikacyjny") -> "pesel"
            value.contains("data urodzenia") -> "birthDate"
            value.contains("data waż") || value.contains("ważna do") || value.contains("termin waż") || value.contains("okres obowiązywania") || value.contains("okres ochrony do") -> when (documentType) {
                "parliament" -> "mpEnd"
                "training" -> "trainingValidUntil"
                "student" -> "studentValidity"
                "identity" -> "idExpiryDate"
                "passport" -> "passportExpiryDate"
                "family" -> "familyExpiryDate"
                "disability" -> "disabilityValidity"
                "large_family" -> "largeFamilyExpiry"
                "disability_card" -> "disabilityCardExpiry"
                "ehic" -> "ehicExpiry"
                "insurance_vehicle", "insurance_professional", "insurance_private", "insurance_business" -> "insuranceEnd"
                else -> if (documentType.startsWith("church")) "churchValidity" else "expiryDate"
            }
            value.contains("data wyd") || value.contains("data sporządz") || value.contains("data sporząd") -> when (documentType) {
                "parliament" -> "mpCardIssueDate"
                "training" -> "trainingIssueDate"
                "student" -> "studentIssueDate"
                "identity" -> "idIssueDate"
                "passport" -> "passportIssueDate"
                "family" -> "familyIssueDate"
                "birth_extract", "marriage_extract", "death_extract" -> "civilIssueDate"
                "large_family" -> "largeFamilyIssueDate"
                "blood_donor" -> "bloodDonorIssueDate"
                "disability_card" -> "disabilityCardIssueDate"
                "ehic" -> "ehicIssueDate"
                "eu_education" -> "educationIssueDate"
                "criminal_record" -> "criminalRecordIssueDate"
                else -> if (documentType.startsWith("church")) "churchIssueDate" else "issueDate"
            }
            value.contains("data rozpoczęcia") -> if (documentType.startsWith("insurance")) "insuranceStart" else "missionStart"
            value.contains("data zakończenia") -> if (documentType.startsWith("insurance")) "insuranceEnd" else "missionEnd"
            value.contains("numer") || value.startsWith("nr ") -> when (documentType) {
                "parliament" -> "mpCardNumber"
                "training" -> "trainingCardNumber"
                "student" -> "studentCardNumber"
                "identity" -> "idNumber"
                "passport" -> "passportNumber"
                "family" -> "familyDocNumber"
                "birth_extract", "marriage_extract", "death_extract" -> "civilDocumentNumber"
                "large_family" -> "largeFamilyNumber"
                "blood_donor" -> "bloodDonorNumber"
                "disability_card" -> "disabilityCardNumber"
                "vehicle_registration" -> "vehicleDocumentNumber"
                "criminal_record" -> "criminalRecordNumber"
                "ehic" -> "ehicNumber"
                else -> if (documentType.startsWith("church")) "churchDocumentNumber" else "documentNumber"
            }
            value.contains("wystawc") || value.contains("organ wydaj") || value.contains("instytucja wydająca") || value.contains("urząd stanu cywilnego") -> when {
                documentType in setOf("birth_extract", "marriage_extract", "death_extract") -> "civilIssuer"
                documentType == "disability" -> "disabilityIssuerName"
                documentType.startsWith("professional") -> "professionalIssuer"
                documentType.startsWith("church") -> "churchIssuer"
                documentType == "parliament" -> "mpIssuer"
                else -> "issuer"
            }
            value.contains("kurs") -> "trainingCourseName"
            value.contains("kwalifikac") -> if (documentType.startsWith("professional")) "professionalTitle" else "trainingCourseName"
            value.contains("uczeln") || value.contains("student") -> "studentUniversity"
            value.contains("nazwa") || value.contains("firma") || value.contains("podmiot") -> when {
                documentType == "family" -> "familyName"
                documentType.startsWith("church") -> "churchRole"
                documentType == "mission_travel" -> "missionOrganization"
                else -> "entityName"
            }
            value.contains("posług") || value.contains("godnoś") -> "churchRole"
            value.contains("telefon") -> "contactPhone"
            value.contains("e-mail") || value.contains("email") -> "contactEmail"
            value.contains("adres") -> "contactAddress"
            value.contains("bank") -> "bankName"
            value.contains("rachun") || value.contains("iban") -> "accountNumber"
            value.contains("szczep") || value.contains("preparat") -> "vaccineName"
            value.contains("tytuł") -> "professionalTitle"
            else -> "details"
        }
    }
    val optionLabels = fieldOptions.toMap()
    val cardFields = values.entries.filter { it.value.isNotBlank() }.map { (key, value) ->
        val stableKey = stableFieldKey(key)
        Triple(stableKey, optionLabels[stableKey] ?: key, value)
    }
    val personHolder = values.entries.firstOrNull {
        it.key.contains("imię", true) || it.key.contains("imie", true) || it.key.contains("nazw", true) ||
            it.key.contains("posiadacz", true) || it.key.contains("osoba", true)
    }?.value.orEmpty()
    val holder = personHolder.ifBlank {
        values.entries.firstOrNull {
            it.key.contains("nazwa", true) || it.key.contains("firma", true) || it.key.contains("podmiot", true)
        }?.value.orEmpty().ifBlank { owner }
    }
    val subtitle = if (personHolder.isBlank() && holder.isNotBlank()) "DANE PODMIOTU" else owner
    val expiry = values.entries.firstOrNull {
        it.key.contains("ważn", true) || it.key.contains("do dnia", true) ||
            it.key.contains("obowiązywania", true) || it.key.contains("ochrony do", true) || it.key.contains("ważna do", true) || it.key.contains("expiry", true) || it.key.contains("validity", true)
    }?.value.orEmpty()
    val explicitlyInvalid = validity.contains("nieważ", true) || validity.contains("unvalid", true) ||
        validity.contains("utrac", true) || validity.contains("zwróc", true) ||
        validity.contains("expired", true) || validity.contains("invalid", true)
    val valid = !explicitlyInvalid && isCredentialValidByExpiry(expiry)
    val qrPayload = remember(title, values) { buildString { append("DEMO|"); append(title); values.forEach { (key, value) -> append("|").append(key).append("=").append(value) } } }
    val qrBitmap = remember(qrPayload) { createDocumentQr(qrPayload) }

    if (settings.showCard) {
        UnifiedCredentialCard(
            documentKey = documentType,
            title = title,
            subtitle = subtitle,
            holderName = holder,
            fields = cardFields,
            settings = settings,
            photo = photo
        )
        if (showReverse || settings.showReverse) {
            LegitimacyReverseSide(
                title = "$title — rewers",
                issuer = owner,
                number = values.entries.firstOrNull { it.key.contains("numer", true) || it.key.contains("number", true) }?.value.orEmpty(),
                validity = expiry,
                orientation = settings.orientation
            )
        } else if (qrBitmap != null) {
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Image(qrBitmap.asImageBitmap(), contentDescription = "Kod QR dokumentu",
                    modifier = Modifier.size(112.dp))
            }
        }
    }
    CredentialValidityTile(valid)
}

@Composable
internal fun DocumentBrandAssetsScreen(onBack: () -> Unit, fixedTarget: String? = null) {
    val context= LocalContext.current
    var assignments by remember { mutableStateOf(DocumentBrandStore.assignments(context)) }
    var owner by remember { mutableStateOf("") }
    var kind by remember { mutableStateOf(DocumentBrandStore.assetKinds().first()) }
    var label by remember { mutableStateOf("") }
    var assets by remember { mutableStateOf(DocumentBrandStore.list(context)) }
    var status by remember { mutableStateOf("") }
    var assetRevision by remember { mutableIntStateOf(0) }
    val legitimacySettingsState = rememberLegitimacyPreviewSettings(fixedTarget ?: "asset-library")
    val picker= rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if(uri!=null) runCatching { DocumentBrandStore.import(context,uri,owner,kind,label) }
            .onSuccess { assets=DocumentBrandStore.list(context); status="Zapisano zasób identyfikacji wizualnej." }
            .onFailure { status="Nie udało się zapisać pliku: "+(it.message ?: "błąd") }
    }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(18.dp), verticalArrangement=Arrangement.spacedBy(12.dp)) {
        Text("‹  Powrót", color=Color(0xFF1769AA), modifier=Modifier.clickable(onClick=onBack).padding(vertical=5.dp))
        Text(if (fixedTarget == null) "Biblioteka zasobów graficznych" else "Wygląd dokumentu: " + (documentBrandTargets.firstOrNull { it.first == fixedTarget }?.second ?: fixedTarget), style=MaterialTheme.typography.headlineSmall, fontWeight=FontWeight.Bold)
        Text(if (fixedTarget == null) "Dodawaj zasoby do biblioteki. Flagi, logo, godła, herby i tła przypisuj wyłącznie w ustawieniach konkretnego dokumentu. Obsługiwane są SVG, PNG, JPEG i WebP; pliki są przechowywane lokalnie i szyfrowane." else "Dodawaj i przypisuj flagi, logo, godła, herby oraz tła wyłącznie dla wybranego dokumentu. Obsługiwane są SVG, PNG, JPEG i WebP; pliki są przechowywane lokalnie i szyfrowane.", style=MaterialTheme.typography.bodySmall)
        OutlinedTextField(owner,{owner=it},label={Text("Podmiot / właściciel zestawu")},modifier=Modifier.fillMaxWidth())
        if (fixedTarget != null) {
            Text("Dokument",fontWeight=FontWeight.SemiBold)
            Text(documentBrandTargets.firstOrNull { it.first == fixedTarget }?.second ?: fixedTarget, color=Color(0xFF173B58), fontWeight=FontWeight.Medium)
        }
        Text("Rodzaj zasobu",fontWeight=FontWeight.SemiBold)
        DocumentBrandStore.assetKinds().forEach { k ->
            Row(Modifier.fillMaxWidth().clickable { kind=k }, horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                RadioButton(selected=kind==k,onClick={kind=k})
                Text(if (k == "Wzór podpisu odręcznego") "Podpis odręczny" else k, modifier=Modifier.padding(top=12.dp))
            }
        }
        OutlinedTextField(label,{label=it},label={Text("Opis / nazwa pliku (opcjonalnie)")},modifier=Modifier.fillMaxWidth())
        Button(onClick={picker.launch(arrayOf("image/png","image/jpeg","image/webp","image/svg+xml"))},enabled=owner.isNotBlank(),modifier=Modifier.fillMaxWidth()) { Text("Wgraj wybrany zasób") }
        if(status.isNotBlank()) Text(status,color=Color(0xFF1769AA))
        if (fixedTarget != null) {
            Text("Ustawienia wyglądu i zawartości dokumentu", fontWeight=FontWeight.SemiBold)
            LegitimacyPreviewSettingsPanel(fixedTarget!!, legitimacySettingsState.value, onChange = {
                legitimacySettingsState.value = it
            }, assetRevision = assetRevision)
        }
        HorizontalDivider()
        Text("Zapisane zasoby",style=MaterialTheme.typography.titleMedium,fontWeight=FontWeight.Bold)
        if (fixedTarget != null) {
            assignments.filter { it.documentType == fixedTarget }.forEach { assignment ->
            val asset = assets.firstOrNull { it.id == assignment.assetId }
            Row(Modifier.fillMaxWidth(), horizontalArrangement=Arrangement.SpaceBetween) {
                Text("Przypisanie: ${documentBrandTargets.firstOrNull { it.first==assignment.documentType }?.second ?: assignment.documentType} · ${if (assignment.kind == "Wzór podpisu odręcznego") "Podpis odręczny" else assignment.kind} · ${asset?.label ?: "brak zasobu"}", style=MaterialTheme.typography.bodySmall, modifier=Modifier.weight(1f))
                TextButton(onClick={ DocumentBrandStore.unassign(context,assignment.documentType,assignment.kind); assignments=DocumentBrandStore.assignments(context); assetRevision++ }) { Text("Odłącz") }
            }
        }
        }
        assets.forEach { asset ->
            val assetPreview = remember(asset.id) { DocumentBrandStore.preview(context,asset.id) }
            Card(Modifier.fillMaxWidth()) {
                Row(Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement=Arrangement.spacedBy(10.dp)) {
                    if (assetPreview != null) Image(assetPreview.asImageBitmap(), contentDescription=asset.label, modifier=Modifier.size(54.dp), contentScale=ContentScale.Fit)
                    Column(Modifier.weight(1f), verticalArrangement=Arrangement.spacedBy(4.dp)) {
                    Text(if (asset.kind == "Wzór podpisu odręcznego") "Podpis odręczny" else asset.kind,fontWeight=FontWeight.SemiBold)
                    Text(asset.owner+" · "+asset.label,style=MaterialTheme.typography.bodySmall)
                    Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                        if (fixedTarget != null) TextButton(onClick={
                            runCatching { DocumentBrandStore.assign(context,fixedTarget,asset.kind,asset.id) }
                                .onSuccess { assignments=DocumentBrandStore.assignments(context); assetRevision++; status="Zapisano zasób dla wybranego dokumentu." }
                                .onFailure { status="Nie udało się przypisać zasobu: " + (it.message ?: "błąd") }
                        }) { Text("Ustaw dla tego dokumentu") }
                        if (asset.file.startsWith("builtin:")) {
                            Text("Wbudowany SVG", color=Color(0xFF667788), style=MaterialTheme.typography.labelSmall, modifier=Modifier.padding(top=10.dp))
                        } else TextButton(onClick={
                            DocumentBrandStore.delete(context,asset.id)
                            assets=DocumentBrandStore.list(context)
                            assignments=DocumentBrandStore.assignments(context)
                        }) { Text("Usuń zasób") }
                    }
                    }
                }
            }
        }
        Text("Hologram i znak wodny w eksporcie są elementami graficznymi. Nie stanowią zabezpieczenia antyfałszerskiego ani urzędowego poświadczenia. Wgrywaj wyłącznie znaki, do których używania masz uprawnienie.",style=MaterialTheme.typography.labelSmall,color=Color(0xFF9A5B00))
    }
}

@Composable
internal fun DocumentPdfExportButton(title: String, values: Map<String,String>, modifier: Modifier = Modifier, documentType: String = "official", ownerDefault: String = "") {
    val context= LocalContext.current
    var owner by remember(title, ownerDefault) { mutableStateOf(ownerDefault) }
    var status by remember { mutableStateOf("") }
    val create= rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/pdf")) { uri ->
        if(uri!=null) runCatching { DocumentPdfRenderer.write(context,uri,title,owner,documentType,values) }
            .onSuccess { status="Utworzono PDF. Plik można teraz podpisać obsługiwanym podpisem elektronicznym." }
            .onFailure { status="Eksport PDF nie powiódł się: "+(it.message ?: "błąd") }
    }
    OutlinedTextField(owner,{owner=it},label={Text("Zestaw identyfikacji podmiotu (nazwa)")},modifier=Modifier.fillMaxWidth())
    Button(onClick={create.launch(title.replace(Regex("[^A-Za-z0-9_-]"),"_").take(50).ifBlank { "dokument" }+".pdf")},modifier=modifier.fillMaxWidth()) { Text("Eksportuj dokument do PDF") }
    if(status.isNotBlank()) Text(status,style=MaterialTheme.typography.bodySmall,color=Color(0xFF1769AA))
}

private object DocumentPdfRenderer {
    fun write(context: Context, uri: Uri, title: String, owner: String, documentType: String, values: Map<String,String>) {
        val pdf=PdfDocument()
        val pageW=595; val pageH=842; val margin=48f
        val body=Paint(Paint.ANTI_ALIAS_FLAG).apply { color=android.graphics.Color.rgb(35,48,60); textSize=10.5f; typeface=Typeface.create("sans-serif",Typeface.NORMAL) }
        val heading=Paint(Paint.ANTI_ALIAS_FLAG).apply { color=android.graphics.Color.rgb(20,45,78); textSize=19f; typeface=Typeface.create("sans-serif",Typeface.BOLD) }
        val small=Paint(Paint.ANTI_ALIAS_FLAG).apply { color=android.graphics.Color.rgb(95,108,120); textSize=8f }
        val line=Paint(Paint.ANTI_ALIAS_FLAG).apply { color=android.graphics.Color.rgb(210,220,230); strokeWidth=0.7f }
        val logo=DocumentBrandStore.imageForDocument(context,documentType,owner,"Logo")
        val crest=DocumentBrandStore.imageForDocument(context,documentType,owner,"Herb") ?: DocumentBrandStore.imageForDocument(context,documentType,owner,"Godło")
        val seal=DocumentBrandStore.image(context,owner,"Pieczęć")
        val signature=DocumentBrandStore.image(context,owner,"Wzór podpisu odręcznego")
        val background=DocumentBrandStore.image(context,owner,"Tło dokumentu")
        val watermark=DocumentBrandStore.image(context,owner,"Znak wodny") ?: DocumentBrandStore.image(context,owner,"Grafika hologramu / zabezpieczenia")
        var pageNo=0; var y=0f; var page: PdfDocument.Page?=null
        fun startPage() {
            pageNo++
            page=pdf.startPage(PdfDocument.PageInfo.Builder(pageW,pageH,pageNo).create())
            val canvas=page!!.canvas
            background?.let { canvas.drawBitmap(it,null,Rect(0,0,pageW,pageH),Paint(Paint.ANTI_ALIAS_FLAG).apply { alpha=42 }) }
            watermark?.let { canvas.save(); canvas.rotate(-28f,pageW/2f,pageH/2f); canvas.drawBitmap(it,null,Rect(95,330,500,735),Paint(Paint.ANTI_ALIAS_FLAG).apply { alpha=24 }); canvas.restore() }
            logo?.let { canvas.drawBitmap(it,null,Rect(margin.toInt(),28,margin.toInt()+58,86),Paint(Paint.ANTI_ALIAS_FLAG)) }
            crest?.let { canvas.drawBitmap(it,null,Rect(pageW-105,28,pageW-48,86),Paint(Paint.ANTI_ALIAS_FLAG)) }
            canvas.drawText(owner.ifBlank { "Dokument" },margin,105f,small)
            canvas.drawText(title,margin,137f,heading)
            canvas.drawLine(margin,151f,pageW-margin,151f,line)
            y=176f
        }
        fun finishPage() {
            val c=page!!.canvas
            seal?.let { c.drawBitmap(it,null,Rect(pageW-150,pageH-125,pageW-70,pageH-45),Paint(Paint.ANTI_ALIAS_FLAG).apply { alpha=220 }) }
            signature?.let { c.drawBitmap(it,null,Rect(margin.toInt(),pageH-125,margin.toInt()+130,pageH-72),Paint(Paint.ANTI_ALIAS_FLAG)) }
            c.drawLine(margin,pageH-145f,pageW-margin,pageH-145f,line)
            c.drawText("Miejsce na podpis / podpis elektroniczny · strona "+pageNo,margin,pageH-35f,small)
            pdf.finishPage(page!!)
        }
        startPage()
        val entries=values.filterValues { it.isNotBlank() }.entries.toList()
        entries.forEach { entry ->
            val label=entry.key
            val text=entry.value
            val labelPaint=Paint(body).apply { typeface=Typeface.create("sans-serif",Typeface.BOLD); color=android.graphics.Color.rgb(65,82,98) }
            if(y>pageH-180f) { finishPage(); startPage() }
            page!!.canvas.drawText(label.take(82),margin,y,labelPaint); y+=16f
            var rest=text
            while(rest.isNotEmpty()) {
                val available=(pageW-2*margin).toInt()
                val count=body.breakText(rest,true,available.toFloat(),null).coerceAtLeast(1)
                page!!.canvas.drawText(rest.substring(0,count),margin,y,body)
                rest=rest.substring(count).trimStart(); y+=14f
                if(y>pageH-180f && rest.isNotEmpty()) { finishPage(); startPage() }
            }
            y+=8f; page!!.canvas.drawLine(margin,y,pageW-margin,y,line); y+=14f
        }
        if(entries.isEmpty()) page!!.canvas.drawText("Brak wprowadzonych danych.",margin,y,body)
        finishPage()
        context.contentResolver.openOutputStream(uri)?.use { pdf.writeTo(it) } ?: error("Nie można zapisać wybranego pliku PDF.")
        pdf.close()
    }
}
