// SPDX-FileCopyrightText: 2026 bartoszruta26-droid
// SPDX-License-Identifier: CC0-1.0
package pl.gov.obywatel.android

/**
 * Five independent payment-account and donation records.
 *
 * These are document-catalog forms, not bank integrations or proof that an
 * account is open, belongs to the named holder, or is authorized for fundraising.
 */
internal data class PaymentAccountDocumentDefinition(
    val id: String,
    val title: String,
    val group: String,
    val fields: List<Triple<String, String, String>>,
    val note: String
)

private fun commonPaymentAccountFields() = listOf(
    Triple("accountHolderName", "Pełna nazwa / imię i nazwisko posiadacza rachunku", ""),
    Triple("accountHolderAddress", "Adres posiadacza rachunku", ""),
    Triple("accountBankName", "Bank / dostawca usług płatniczych", ""),
    Triple("accountBankCountry", "Państwo prowadzenia rachunku", ""),
    Triple("accountName", "Nazwa własna rachunku", ""),
    Triple("accountIban", "Numer rachunku / IBAN", ""),
    Triple("accountBicSwift", "Kod BIC / SWIFT", ""),
    Triple("accountCurrency", "Waluta rachunku (kod ISO 4217)", "PLN / EUR / USD"),
    Triple("accountType", "Rodzaj rachunku", "bieżący / oszczędnościowy / płatniczy / walutowy"),
    Triple("accountPaymentProvider", "Dostawca prowadzący rachunek / instytucja płatnicza", ""),
    Triple("accountPurpose", "Przeznaczenie rachunku", ""),
    Triple("accountStatus", "Status rachunku", "aktywny / nieaktywny / zamknięty / w trakcie weryfikacji"),
    Triple("accountOpeningDate", "Data otwarcia rachunku", "RRRR-MM-DD"),
    Triple("accountClosingDate", "Data zamknięcia rachunku", "RRRR-MM-DD"),
    Triple("accountHolderVerification", "Sposób weryfikacji posiadacza rachunku", ""),
    Triple("accountSourceDocument", "Dokument źródłowy potwierdzający dane rachunku", ""),
    Triple("accountAuthorizedPersons", "Osoby uprawnione do dysponowania rachunkiem", ""),
    Triple("accountRepresentationBasis", "Podstawa reprezentacji / umocowania", ""),
    Triple("donationAcceptanceBasis", "Podstawa przyjmowania darowizn / wpłat", ""),
    Triple("donationCategory", "Rodzaj wpływu", "darowizna / ofiara / wpłata celowa / zbiórka / inny"),
    Triple("donationPurpose", "Cel darowizny / przeznaczenie środków", ""),
    Triple("donationCampaign", "Nazwa zbiórki / kampanii / projektu", ""),
    Triple("donationDate", "Data otrzymania darowizny", "RRRR-MM-DD"),
    Triple("donationAmount", "Kwota darowizny", "wartość dziesiętna"),
    Triple("donationCurrency", "Waluta darowizny", "PLN / EUR / USD"),
    Triple("donationMethod", "Sposób przekazania", "przelew / wpłata gotówkowa / płatność elektroniczna / inny"),
    Triple("donationTransferTitle", "Tytuł przelewu / opis wpłaty", ""),
    Triple("donationTransactionReference", "Identyfikator transakcji / numer potwierdzenia", ""),
    Triple("donorName", "Dane darczyńcy", "opcjonalnie, gdy istnieje podstawa do zapisania"),
    Triple("donorAnonymous", "Darowizna anonimowa", "tak / nie"),
    Triple("donationRestrictions", "Warunki / ograniczenia darowizny", ""),
    Triple("donationReceiptNumber", "Numer pokwitowania / potwierdzenia", ""),
    Triple("donationReceivedBy", "Osoba przyjmująca / ewidencjonująca wpłatę", ""),
    Triple("donationDepositDate", "Data wpłaty gotówki na rachunek", "RRRR-MM-DD"),
    Triple("donationSettlementStatus", "Status rozliczenia", "oczekuje / zaksięgowana / rozliczona / skorygowana"),
    Triple("donationAccountingReference", "Odniesienie do ewidencji księgowej", ""),
    Triple("donationEvidence", "Dokument / załącznik potwierdzający", ""),
    Triple("recordPreparedBy", "Osoba sporządzająca wpis", ""),
    Triple("recordDate", "Data sporządzenia / aktualizacji wpisu", "RRRR-MM-DD"),
    Triple("recordNotes", "Uwagi", "")
)

private fun organizationIdentityFields(prefix: String, entityLabel: String) = listOf(
    Triple("${prefix}Name", "Pełna nazwa ${entityLabel}", ""),
    Triple("${prefix}LegalForm", "Forma prawna / status", ""),
    Triple("${prefix}Registry", "KRS / CEIDG / rejestr / ewidencja", ""),
    Triple("${prefix}Nip", "NIP (jeżeli nadany)", ""),
    Triple("${prefix}Regon", "REGON (jeżeli nadany)", ""),
    Triple("${prefix}Seat", "Siedziba / adres", ""),
    Triple("${prefix}StatutePurpose", "Cele statutowe / przedmiot działalności", ""),
    Triple("${prefix}Representation", "Sposób reprezentacji", ""),
    Triple("${prefix}GoverningBody", "Właściwy organ / osoby uprawnione", "")
)

private val paymentAccountDocumentCatalog = listOf(
    PaymentAccountDocumentDefinition(
        id = "payment_account_foundation",
        title = "Rachunek płatniczy i ewidencja darowizn — Fundacja",
        group = "Służba fundacyjna",
        fields = organizationIdentityFields("foundation", "fundacji") + commonPaymentAccountFields() + listOf(
            Triple("foundationOppStatus", "Status OPP / uprawnienie do otrzymywania 1,5% PIT", "wpis w KRS i aktualny status — jeżeli dotyczy"),
            Triple("foundationDonationStatutoryPurpose", "Powiązanie darowizny z celem statutowym", ""),
            Triple("foundationBoardResolution", "Uchwała / upoważnienie Zarządu do prowadzenia rachunku lub zbiórki", ""),
            Triple("foundationDonationReportPeriod", "Okres sprawozdawczy darowizn", "")
        ),
        note = "Formularz ewidencyjny. Status OPP i możliwość otrzymywania 1,5% podatku wymagają odrębnego potwierdzenia w aktualnym rejestrze. Nie każda wpłata na rachunek fundacji jest automatycznie darowizną ani przychodem zwolnionym z podatku."
    ),
    PaymentAccountDocumentDefinition(
        id = "payment_account_association",
        title = "Rachunek płatniczy i ewidencja darowizn — Stowarzyszenie",
        group = "Służba stowarzyszeniowa",
        fields = organizationIdentityFields("association", "stowarzyszenia") + commonPaymentAccountFields() + listOf(
            Triple("associationMembershipDuesSeparate", "Oddzielna ewidencja składek członkowskich", "tak / nie; składki nie są automatycznie darowiznami"),
            Triple("associationDonationResolution", "Uchwała / upoważnienie właściwego organu", ""),
            Triple("associationDonationStatutoryPurpose", "Zgodność darowizny z celami statutowymi", ""),
            Triple("associationDonorRestrictions", "Ograniczenia przyjmowania i wykorzystania darowizn", "")
        ),
        note = "Formularz ewidencyjny stowarzyszenia. Składki członkowskie, darowizny, dotacje i wpływy z działalności należy klasyfikować oddzielnie zgodnie ze statutem i zasadami rachunkowości."
    ),
    PaymentAccountDocumentDefinition(
        id = "payment_account_church",
        title = "Rachunek płatniczy i ewidencja ofiar oraz darowizn — Kościół",
        group = "Służba kościelna",
        fields = organizationIdentityFields("church", "Kościoła / jednostki kościelnej") + commonPaymentAccountFields() + listOf(
            Triple("churchLegalBasis", "Podstawa statusu prawnego / kościelnego jednostki", ""),
            Triple("churchJurisdiction", "Diecezja / jurysdykcja / struktura kościelna", ""),
            Triple("churchUnitType", "Rodzaj jednostki", "Kościół / związek wyznaniowy / parafia / wspólnota / jednostka organizacyjna"),
            Triple("churchDonationType", "Rodzaj ofiary / darowizny", "ofiara na cele kultu / darowizna / ofiara misyjna / pomoc charytatywna / inna"),
            Triple("churchPurposeRestriction", "Cel kościelny / misyjny / charytatywny i ograniczenia", ""),
            Triple("churchFinancialAuthority", "Osoba / organ uprawniony do czynności finansowych", ""),
            Triple("churchSettlementReport", "Protokół / raport rozliczenia ofiar", "")
        ),
        note = "Formularz ewidencyjny rachunku i wpływów kościelnych. Status prawny jednostki, reprezentację oraz zasady gospodarowania środkami należy potwierdzić w dokumentach właściwych dla danej wspólnoty i przepisach mających do niej zastosowanie."
    ),
    PaymentAccountDocumentDefinition(
        id = "payment_account_business",
        title = "Rachunek płatniczy i ewidencja darowizn — Przedsiębiorstwo",
        group = "Służba gospodarcza",
        fields = organizationIdentityFields("business", "przedsiębiorstwa") + commonPaymentAccountFields() + listOf(
            Triple("businessOwnerType", "Rodzaj przedsiębiorcy / właściciela", "osoba fizyczna / spółka / inna osoba prawna"),
            Triple("businessDonationLegalTitle", "Tytuł prawny otrzymania darowizny", ""),
            Triple("businessDonationTaxTreatment", "Sposób kwalifikacji podatkowej — do weryfikacji", ""),
            Triple("businessDonationRelatedParty", "Powiązanie darczyńcy z przedsiębiorstwem", ""),
            Triple("businessDonationSeparateFromSales", "Oddzielono od zapłat za sprzedaż i usług", "tak / nie")
        ),
        note = "Wpłaty od klientów za towary i usługi nie są darowiznami. Przyjęcie darowizny przez przedsiębiorcę wymaga ustalenia właściwego tytułu prawnego i skutków podatkowych; formularz nie przesądza o kwalifikacji księgowej ani podatkowej."
    ),
    PaymentAccountDocumentDefinition(
        id = "payment_account_person",
        title = "Rachunek płatniczy i ewidencja darowizn — Osoba fizyczna",
        group = "Służba osobista",
        fields = listOf(
            Triple("personFullName", "Imię i nazwisko posiadacza rachunku", ""),
            Triple("personAddress", "Adres zamieszkania / korespondencyjny", ""),
            Triple("personPeselOptional", "PESEL (opcjonalnie, jeżeli niezbędny)", ""),
            Triple("personTaxResidence", "Państwo rezydencji podatkowej", ""),
            Triple("personIdentityVerification", "Sposób weryfikacji tożsamości / posiadacza", ""),
            Triple("personDonationRelationship", "Relacja darczyńcy z obdarowanym (jeżeli istotna podatkowo)", ""),
            Triple("personDonationTaxGroup", "Grupa podatkowa / stopień pokrewieństwa — jeżeli dotyczy", ""),
            Triple("personDonationTaxNote", "Informacja o obowiązkach podatkowych / zgłoszeniu — do weryfikacji", "")
        ) + commonPaymentAccountFields(),
        note = "Dane osoby fizycznej należy ograniczyć do niezbędnego zakresu. Obowiązki podatkowe zależą m.in. od relacji stron, wartości i sumy darowizn oraz daty nabycia; wpis w aplikacji nie stanowi deklaracji podatkowej ani potwierdzenia zwolnienia."
    )
)

internal fun paymentAccountDocumentTargets(): List<Triple<String, String, String>> =
    paymentAccountDocumentCatalog.map { Triple(it.id, it.title, it.group) }

internal fun paymentAccountDocumentFields(documentId: String): List<Triple<String, String, String>> =
    paymentAccountDocumentCatalog.firstOrNull { it.id == documentId }?.fields.orEmpty()

internal fun paymentAccountDocumentDefinition(documentId: String): PaymentAccountDocumentDefinition? =
    paymentAccountDocumentCatalog.firstOrNull { it.id == documentId }
