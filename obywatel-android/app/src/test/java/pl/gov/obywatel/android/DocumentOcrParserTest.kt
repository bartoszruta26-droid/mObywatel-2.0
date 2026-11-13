// SPDX-FileCopyrightText: 2026 bartoszruta26-droid
// SPDX-License-Identifier: CC0-1.0
package pl.gov.obywatel.android
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
class DocumentOcrParserTest {
 @Test fun identityFieldsAreMapped(){val m=DocumentOcrParser.extract("identity_card","IMIONA: JAN ADAM\nNAZWISKO: KOWALSKI\nDATA URODZENIA: 12.03.1980\nPESEL: 80031212345\nNUMER DOKUMENTU: ABC123456")
  assertEquals("JAN ADAM",m["firstNames"]);assertEquals("KOWALSKI",m["lastName"]);assertEquals("12.03.1980",m["birthDate"]);assertEquals("80031212345",m["pesel"]);assertEquals("ABC123456",m["idNumber"])}
 @Test fun passportMrzIsParsed(){val m=DocumentOcrParser.extract("passport","P<POLKOWALSKI<<JAN<ADAM<<<<<<<<<<<<<<<<\nABC1234567POL8003121M3001012<<<<<<<<<<<<")
  assertEquals("map=$m", "JAN ADAM KOWALSKI",m["passportHolderNames"]);assertEquals("map=$m", "ABC123456",m["passportNumber"]);assertEquals("map=$m", "12-03-1980",m["birthDate"]);assertEquals("map=$m", "01-01-2030",m["passportExpiryDate"]);assertEquals("map=$m", "POL",m["passportCitizenship"]);assertEquals("map=$m", "M",m["passportSex"])}
 @Test fun identityCardMrzMapsNumberAndNames(){
  val m=DocumentOcrParser.extract("identity_card","IDPOLABC1234567<<<<<<<<<<<<<<<\n8003121M3001012POL<<<<<<<<<<<<\nKOWALSKI<<JAN<ADAM<<<<<<<<<<<<")
  assertEquals("ABC123456",m["idNumber"]);assertEquals("12-03-1980",m["birthDate"]);assertEquals("01-01-2030",m["idExpiryDate"])
  assertEquals("KOWALSKI",m["lastName"]);assertEquals("JAN ADAM",m["firstNames"])
 }
 @Test fun blankTextHasNoSuggestions(){assertTrue(DocumentOcrParser.extract("identity_card"," ").isEmpty())}

 @Test fun documentSpecificCategoriesMapOcrIntoTheirOwnFields(){
  val bank = DocumentOcrParser.extract("bank_accounts","Posiadacz rachunku / podmiot: JAN KOWALSKI")
  assertTrue("bank=$bank", bank.containsKey("account_0_5"))

  val insurance = DocumentOcrParser.extract("insurance_vehicle","Numer polisy: OC-2026-123")
  assertTrue("insurance=$insurance", insurance.containsKey("field_1"))

  val foundation = DocumentOcrParser.extract("organization_foundation","Pełna nazwa fundacji: FUNDACJA ARIA")
  assertTrue("foundation=$foundation", foundation.containsKey("doc_organization_foundation_foundationName"))

  val churchId = churchGraphicDocumentTargets().first().first
  val churchField = churchGraphicDocumentFields(churchId).first()
  val church = DocumentOcrParser.extract(churchId,"${churchField.second}: JAN KOWALSKI")
  assertTrue("church=$church", church.containsKey(churchField.first))

  val familyId = familyEntityGraphicDocumentTargets().first().first
  val familyField = familyEntityGraphicDocumentFields(familyId).first()
  val family = DocumentOcrParser.extract(familyId,"${familyField.second}: ANNA KOWALSKA")
  assertTrue("family=$family", family.containsKey(familyField.first))

  val officialId = officialGraphicDocumentTargets().first().first
  val officialField = officialGraphicDocumentFields(officialId).first()
  val official = DocumentOcrParser.extract(officialId,"${officialField.second}: AKT")
  assertTrue("official=$official", official.containsKey(officialField.first))

 @Test fun everySchemaBackedCatalogDocumentMapsItsOwnFieldLabelToItsStableKey() {
  documentDisplayOptions.distinctBy { it.first }.forEach { (documentId, _, _) ->
   val definitions = documentEditorFieldDefinitions(documentId)
   if (definitions.isNotEmpty()) {
    val definition = definitions.first()
    val value = "WARTOSC_TESTOWA"
    val mapped = DocumentOcrParser.extract(documentId, "${definition.label}: $value")
    val expectedKey = "doc_${documentId}_${definition.key}"
    assertEquals("$documentId must map ${definition.label} to $expectedKey", value, mapped[expectedKey])
   }
  }
 }
 }
}