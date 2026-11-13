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
  val cases = listOf(
   Triple("church","Osoba / duchowny / członek wspólnoty: JAN KOWALSKI","doc_church_clergyName"),
   Triple("family","Osoba, której dotyczy wpis: ANNA KOWALSKA","doc_family_familyMember"),
   Triple("business","Pełna nazwa podmiotu: FUNDACJA ARIA","doc_business_entityName"),
   Triple("bank_accounts","Posiadacz rachunku / podmiot: JAN KOWALSKI","doc_bank_accounts_accountOwner"),
   Triple("civil","Rodzaj aktu / zaświadczenia: AKT URODZENIA","doc_civil_actOrCertificateType")
  )
  cases.forEach { (category, text, expectedKey) ->
   val result = DocumentOcrParser.extract(category,text)
   assertTrue("category=$category result=$result", result.containsKey(expectedKey))
  }
 }
}