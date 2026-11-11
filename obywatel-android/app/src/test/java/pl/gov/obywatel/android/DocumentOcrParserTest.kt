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
  assertEquals("JAN ADAM KOWALSKI",m["passportHolderNames"]);assertEquals("ABC123456",m["passportNumber"]);assertEquals("12-03-1980",m["birthDate"]);assertEquals("01-01-2030",m["passportExpiryDate"]);assertEquals("POL",m["passportCitizenship"])}
 @Test fun identityCardMrzMapsNumberAndNames(){
  val m=DocumentOcrParser.extract("identity_card","IDPOLABC1234567<<<<<<<<<<<<<<<\n8003121M3001012POL<<<<<<<<<<<<\nKOWALSKI<<JAN<ADAM<<<<<<<<<<<<")
  assertEquals("ABC123456",m["idNumber"]);assertEquals("12-03-1980",m["birthDate"]);assertEquals("01-01-2030",m["idExpiryDate"])
  assertEquals("KOWALSKI",m["lastName"]);assertEquals("JAN ADAM",m["firstNames"])
 }
 @Test fun blankTextHasNoSuggestions(){assertTrue(DocumentOcrParser.extract("identity_card"," ").isEmpty())}
}