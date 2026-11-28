package pl.gov.obywatel.android

/**
 * Schemat pól ewidencyjnych poszczególnych rodzajów dokumentów.
 * Klucze są stabilne między wydaniami i stanowią część formatu archiwum vCard.
 */
internal const val DOCUMENT_EDITOR_SCHEMA_VERSION = "2"

internal data class DocumentEditorFieldDefinition(
    val key: String,
    val label: String,
    val placeholder: String = ""
)

private fun fields(vararg definitions: Triple<String, String, String>) =
    definitions.map { DocumentEditorFieldDefinition(it.first, it.second, it.third) }

private fun internationalDocumentFieldSet(documentId: String, specific: List<DocumentEditorFieldDefinition>): List<DocumentEditorFieldDefinition> =
    internationalDocumentEditorFields(documentId) + specific

internal fun documentEditorFieldDefinitions(documentId: String): List<DocumentEditorFieldDefinition> {
    val specific = when (documentId) {
        in setOf("vatican_passport_ordinary", "holy_see_passport_diplomatic", "holy_see_passport_service", "holy_see_passport_temporary_service") ->
            internationalDocumentEditorFields(documentId)
        in setOf("ecclesiastical_baptism_certificate", "ecclesiastical_marriage_certificate", "ecclesiastical_death_certificate") -> internationalDocumentFieldSet(documentId, fields(
            Triple("sacramentalRegisterType", "Rodzaj księgi parafialnej", "chrztów / małżeństw / zmarłych"),
            Triple("parishName", "Parafia / kościół", ""),
            Triple("dioceseName", "Diecezja / jurysdykcja kościelna", ""),
            Triple("registerVolume", "Księga / tom", ""),
            Triple("registerEntryNumber", "Numer wpisu / aktu", ""),
            Triple("sacramentalEventDate", "Data udzielenia sakramentu / zdarzenia", "RRRR-MM-DD"),
            Triple("ministerName", "Duchowny / osoba dokonująca wpisu", ""),
            Triple("certificatePurpose", "Cel wydania odpisu / zaświadczenia", "")
        ))
        "canonical_status_certificate" -> internationalDocumentFieldSet(documentId, fields(
            Triple("canonicalStatus", "Stwierdzony status kanoniczny", ""),
            Triple("competentOrdinary", "Ordynariusz / przełożony kompetentny", ""),
            Triple("dioceseOrInstitute", "Diecezja / instytut / wspólnota", ""),
            Triple("canonicalRestrictions", "Ograniczenia lub adnotacje kanoniczne", ""),
            Triple("verificationDate", "Data weryfikacji statusu", "RRRR-MM-DD")
        ))
        "clerical_ordination_certificate" -> internationalDocumentFieldSet(documentId, fields(
            Triple("ordinationRank", "Stopień święceń", "diakonat / prezbiterat / episkopat"),
            Triple("ordinationDate", "Data święceń", "RRRR-MM-DD"),
            Triple("ordinationPlace", "Miejsce święceń", ""),
            Triple("ordainingBishop", "Biskup udzielający święceń", ""),
            Triple("ordinandName", "Imię i nazwisko osoby wyświęconej", ""),
            Triple("ordinationRegisterReference", "Księga / numer wpisu", "")
        ))
        "apostolic_nunciature_credential" -> internationalDocumentFieldSet(documentId, fields(
            Triple("nunciatureCountry", "Państwo / placówka nuncjatury", ""),
            Triple("credentialHolder", "Osoba, której dotyczy poświadczenie", ""),
            Triple("diplomaticCapacity", "Funkcja / status dyplomatyczny", ""),
            Triple("accreditingAuthority", "Organ akredytujący / wystawiający", ""),
            Triple("hostStateProtocolReference", "Numer / referencja protokołu państwa przyjmującego", "")
        ))
        "profile", "identity", "passport", "registry", "family", "parliament", "training", "student",
        "disability", "church", "civil", "business", "entity", "bank_accounts", "rod_bank_accounts", "entity_bank_accounts",
        "vaccines", "signed_pdf", "receipts", "payment_transactions", "imported", "children", "branding", "loyalty", "loyalty_cards",
        "professional", "foreign_prescription", DonationLedgerCatalog.PERSON, DonationLedgerCatalog.FOUNDATION,
        DonationLedgerCatalog.ASSOCIATION, DonationLedgerCatalog.CHURCH -> emptyList()
        else -> when {
            churchGraphicDocumentTargets().any { it.first == documentId } ||
                familyEntityGraphicDocumentTargets().any { it.first == documentId } ||
                officialGraphicDocumentTargets().any { it.first == documentId } -> emptyList()
            else -> documentEditorFieldDefinitionsForCatalog(documentId)
        }
    }
    val workflow = if (documentId in internationalDocumentCatalogIds) fields(
        Triple("issuerName", "Nazwa wystawcy / instytucji wydającej", ""),
        Triple("issuerPosition", "Tytuł zawodowy / stanowisko wystawcy", ""),
        Triple("issuingAuthority", "Właściwy organ wydający", ""),
        Triple("verificationAuthority", "Organ / podmiot właściwy do weryfikacji", ""),
        Triple("procedureName", "Właściwa procedura / tryb wniosku", ""),
        Triple("jurisdiction", "Państwo / jurysdykcja", ""),
        Triple("sourceLanguage", "Język dokumentu źródłowego", ""),
        Triple("translationLanguage", "Język tłumaczenia", ""),
        Triple("templateSourceReference", "Źródło / referencja wzoru urzędowego", ""),
        Triple("templateVersion", "Wersja wzoru / data obowiązywania", "")
    ) else emptyList()
    // Keep existing stable field keys and append shared workflow fields once.
    return (specific + workflow).distinctBy { it.key }
}

private fun documentEditorFieldDefinitionsForCatalog(documentId: String): List<DocumentEditorFieldDefinition> = when (documentId) {
        in paymentAccountDocumentTargets().map { it.first }.toSet() -> fields(*paymentAccountDocumentFields(documentId).toTypedArray())
        in internationalDocumentCatalogIds -> internationalDocumentEditorFields(documentId)
        "fund_purpose_disposition" -> fields(
            Triple("documentNumber", "Numer aktu powierzenia", ""),
            Triple("issueDate", "Data sporządzenia aktu", "RRRR-MM-DD"),
            Triple("issuingAuthority", "Organ / podmiot powierzający środki", ""),
            Triple("fundRecipientLegalName", "Prawny odbiorca środków", ""),
            Triple("fundRecipientLegalForm", "Forma prawna odbiorcy i podstawa jego udziału", ""),
            Triple("fundTransferLegalInstrument", "Forma prawna przekazania: umowa / porozumienie / inny właściwy instrument", ""),
            Triple("fundTransferAgreementNumber", "Numer i data umowy lub innego aktu stanowiącego podstawę przekazania", ""),
            Triple("authorityRepresentative", "Osoba uprawniona do reprezentacji powierzającego", ""),
            Triple("legalBasis", "Podstawa prawna i kompetencja do powierzenia", ""),
            Triple("fundName", "Nazwa państwowego funduszu celowego", ""),
            Triple("fundCreatingAct", "Ustawa tworząca Fundusz / właściwe przepisy", ""),
            Triple("financialPlanReference", "Pozycja planu finansowego / wyodrębnione zadanie", ""),
            Triple("purposeName", "Nazwa Celu służby Narodowi", ""),
            Triple("purposeDescription", "Opis Celu, rezultatów i grupy odbiorców", ""),
            Triple("purposeScope", "Zakres rzeczowy i terytorialny realizacji", ""),
            Triple("disponentName", "Imię i nazwisko Dyspozytora Celu", ""),
            Triple("disponentIdentifier", "Dane identyfikacyjne Dyspozytora (opcjonalnie)", ""),
            Triple("dispositionStartDate", "Początek okresu powierzenia", "RRRR-MM-DD"),
            Triple("dispositionEndDate", "Koniec okresu powierzenia", "RRRR-MM-DD"),
            Triple("fundingAmount", "Kwota powierzonych środków", "PLN"),
            Triple("fundingSource", "Źródło środków: PFC / darowizny / inne", ""),
            Triple("transferDate", "Data przekazania środków", "RRRR-MM-DD"),
            Triple("paymentAccountHolder", "Posiadacz wyodrębnionego rachunku płatniczego", ""),
            Triple("paymentAccountBank", "Bank / dostawca usług płatniczych", ""),
            Triple("paymentAccountNumber", "Numer rachunku / IBAN", ""),
            Triple("paymentAccountCurrency", "Waluta rachunku", "PLN"),
            Triple("paymentAccountEvidence", "Dowód prowadzenia rachunku / identyfikator dokumentu", ""),
            Triple("accountSeparationRules", "Zasady oddzielenia środków Celu od innych środków", ""),
            Triple("disponentAuthority", "Zakres powierzonych uprawnień decyzyjnych", ""),
            Triple("legalEffectNotice", "Zastrzeżenie: zakres skutków prawnych i ograniczenia umocowania", "Wzór ewidencyjny nie stanowi samodzielnej podstawy przekazania środków publicznych ani nie nadaje kompetencji dysponenta PFC. Skuteczność powierzenia wymaga właściwej podstawy prawnej, działania uprawnionego organu oraz odpowiedniej umowy lub aktu."),
            Triple("eligibleCosts", "Koszty kwalifikowalne i ograniczenia wydatkowania", ""),
            Triple("procurementRules", "Zasady wyboru wykonawców i dokonywania zakupów", ""),
            Triple("recordKeepingRules", "Zasady ewidencji, dokumentowania i kontroli", ""),
            Triple("reportingDeadlines", "Terminy i zakres sprawozdań", ""),
            Triple("unusedFundsRules", "Zasady zwrotu środków niewykorzystanych", ""),
            Triple("conflictOfInterest", "Zasady konfliktu interesów i jawności", ""),
            Triple("attachments", "Załączniki: plan, umowa, potwierdzenie rachunku, harmonogram", ""),
            Triple("signingStatus", "Status aktu: projekt / podpisany / wykonany / zakończony", "projekt"),
            Triple("issuerSignature", "Podpis osoby uprawnionej po stronie powierzającego", ""),
            Triple("disponentSignature", "Podpis Dyspozytora Celu", "")
        )
        "international_medical_certificate" -> fields(
            Triple("documentNumber", "Document Number", ""),
            Triple("issueDate", "Issue Date", "YYYY-MM-DD"),
            Triple("purpose", "Purpose of Certificate", ""),
            Triple("issuingCountry", "Issuing Country", ""),
            Triple("destinationCountry", "Destination Country", ""),
            Triple("sourceLanguage", "Original Language", ""),
            Triple("targetLanguages", "Target Languages", ""),
            Triple("patientGivenName", "Patient Given Name", ""),
            Triple("patientFamilyName", "Patient Family Name", ""),
            Triple("patientDateOfBirth", "Date of Birth", "YYYY-MM-DD"),
            Triple("patientNationality", "Nationality", ""),
            Triple("patientAddress", "Patient Address", ""),
            Triple("identityDocument", "Identity Document Type and Number", ""),
            Triple("insuranceIdentifier", "Insurance Identifier", ""),
            Triple("patientConsent", "Patient Consent / Authorization", ""),
            Triple("clinicianName", "Clinician Full Name", ""),
            Triple("clinicianProfession", "Clinician Profession", ""),
            Triple("clinicianSpecialty", "Clinical Specialty", ""),
            Triple("clinicianLicense", "Professional License Number", ""),
            Triple("facilityName", "Medical Facility", ""),
            Triple("facilityAddress", "Facility Address", ""),
            Triple("chiefComplaint", "Chief Complaint", ""),
            Triple("medicalHistory", "Relevant Medical History", ""),
            Triple("occupationalHistory", "Occupational History", ""),
            Triple("allergies", "Allergies", ""),
            Triple("currentMedication", "Current Medication and Dosage", ""),
            Triple("diagnosis", "Diagnosis and ICD Code", ""),
            Triple("symptomsOnset", "Symptoms Onset Date", "YYYY-MM-DD"),
            Triple("examinationDate", "Examination Date", "YYYY-MM-DD"),
            Triple("height", "Height", "cm"),
            Triple("weight", "Weight", "kg"),
            Triple("bloodPressure", "Blood Pressure", "mmHg"),
            Triple("pulse", "Pulse", "bpm"),
            Triple("temperature", "Body Temperature", "°C"),
            Triple("oxygenSaturation", "Oxygen Saturation", "%"),
            Triple("physicalExamination", "Physical Examination Findings", ""),
            Triple("systemsReview", "Review of Systems", ""),
            Triple("functionalLimitations", "Functional Limitations", ""),
            Triple("workCapacity", "Work Capacity and Restrictions", ""),
            Triple("hospitalizations", "Hospitalizations", ""),
            Triple("treatmentAndRehabilitation", "Treatment and Rehabilitation", ""),
            Triple("prognosis", "Prognosis", ""),
            Triple("recommendations", "Recommendations and Follow-up", ""),
            Triple("clinicianDeclaration", "Clinician Declaration", ""),
            Triple("clinicianSignRef", "Clinician Signature - to be handwritten after printing", ""),
            Triple("patientSignRef", "Patient Signature - to be handwritten after printing", ""),
            Triple("medicalSealReference", "Medical Stamp - applied to the printed document", "")
        )
        "birth_extract" -> fields(
            Triple("childName", "Imię i nazwisko dziecka", ""),
            Triple("birthDate", "Data i godzina urodzenia", "DD-MM-RRRR"),
            Triple("birthPlace", "Miejsce urodzenia", ""),
            Triple("sexAtRegistration", "Płeć wpisana w akcie", ""),
            Triple("motherDetails", "Dane matki", "Imiona, nazwisko, nazwisko rodowe"),
            Triple("fatherDetails", "Dane ojca", "Imiona, nazwisko"),
            Triple("registryOffice", "Urząd stanu cywilnego", ""),
            Triple("actNumber", "Numer aktu urodzenia", ""),
            Triple("issueDate", "Data sporządzenia odpisu", "DD-MM-RRRR"),
            Triple("copyType", "Rodzaj odpisu", "zupełny / skrócony / wielojęzyczny"),
            Triple("marginalNotes", "Wzmianki dodatkowe", "")
        )
        "marriage_extract" -> fields(
            Triple("spouseOne", "Pierwszy małżonek", "Imiona, nazwisko, nazwisko rodowe"),
            Triple("spouseTwo", "Drugi małżonek", "Imiona, nazwisko, nazwisko rodowe"),
            Triple("marriageDate", "Data zawarcia małżeństwa", "DD-MM-RRRR"),
            Triple("marriagePlace", "Miejsce zawarcia małżeństwa", ""),
            Triple("registryOffice", "Urząd stanu cywilnego", ""),
            Triple("actNumber", "Numer aktu małżeństwa", ""),
            Triple("propertyRegime", "Ustrój majątkowy / umowy", ""),
            Triple("issueDate", "Data sporządzenia odpisu", "DD-MM-RRRR"),
            Triple("copyType", "Rodzaj odpisu", "zupełny / skrócony / wielojęzyczny"),
            Triple("marginalNotes", "Wzmianki dodatkowe", "")
        )
        "death_extract" -> fields(
            Triple("deceasedName", "Imię i nazwisko osoby zmarłej", ""),
            Triple("birthDate", "Data urodzenia", "DD-MM-RRRR"),
            Triple("deathDate", "Data i godzina zgonu", "DD-MM-RRRR"),
            Triple("deathPlace", "Miejsce zgonu", ""),
            Triple("registryOffice", "Urząd stanu cywilnego", ""),
            Triple("actNumber", "Numer aktu zgonu", ""),
            Triple("issueDate", "Data sporządzenia odpisu", "DD-MM-RRRR"),
            Triple("copyType", "Rodzaj odpisu", "zupełny / skrócony"),
            Triple("marginalNotes", "Wzmianki dodatkowe", "")
        )
        "vehicle_registration" -> fields(
            Triple("registrationNumber", "Numer rejestracyjny", ""),
            Triple("vin", "Numer VIN", ""),
            Triple("makeModel", "Marka i model pojazdu", ""),
            Triple("vehicleType", "Rodzaj / przeznaczenie pojazdu", ""),
            Triple("yearOfProduction", "Rok produkcji", "RRRR"),
            Triple("owner", "Właściciel / współwłaściciele", ""),
            Triple("registeredAddress", "Adres rejestracji", ""),
            Triple("firstRegistration", "Data pierwszej rejestracji", "DD-MM-RRRR"),
            Triple("technicalInspection", "Termin badania technicznego", "DD-MM-RRRR"),
            Triple("registrationAuthority", "Organ rejestrujący", ""),
            Triple("documentSeries", "Seria i numer dowodu rejestracyjnego", "")
        )
        "insurance_vehicle", "insurance_professional", "insurance_private", "insurance_business" -> fields(
            Triple("policyNumber", "Numer polisy", ""),
            Triple("policyholder", "Ubezpieczający", ""),
            Triple("insuredPersonOrEntity", "Ubezpieczony / podmiot ubezpieczony", ""),
            Triple("insurer", "Zakład ubezpieczeń", ""),
            Triple("riskSubject", when (documentId) {
                "insurance_vehicle" -> "Pojazd objęty ochroną"
                "insurance_professional" -> "Zawód i zakres czynności objętych ochroną"
                "insurance_business" -> "Rodzaj działalności i zakres ryzyka"
                else -> "Przedmiot ochrony w życiu prywatnym"
            }, ""),
            Triple("coverageScope", "Zakres ochrony i suma gwarancyjna", ""),
            Triple("deductible", "Udział własny / franszyza", ""),
            Triple("validFrom", "Początek ochrony", "DD-MM-RRRR"),
            Triple("validTo", "Koniec ochrony", "DD-MM-RRRR"),
            Triple("premium", "Składka i częstotliwość płatności", ""),
            Triple("claimsContact", "Kontakt do zgłoszenia szkody", "")
        )
        "criminal_record" -> fields(
            Triple("applicantName", "Imię i nazwisko osoby, której dotyczy zaświadczenie", ""),
            Triple("applicantIdentifier", "PESEL / identyfikator wnioskodawcy", ""),
            Triple("certificateNumber", "Numer zaświadczenia", ""),
            Triple("issuingAuthority", "Organ wydający", "np. Krajowy Rejestr Karny"),
            Triple("applicationDate", "Data złożenia wniosku", "DD-MM-RRRR"),
            Triple("issueDate", "Data wydania", "DD-MM-RRRR"),
            Triple("resultScope", "Zakres informacji / wynik", ""),
            Triple("purpose", "Cel uzyskania zaświadczenia", ""),
            Triple("verificationCode", "Kod / sposób weryfikacji dokumentu", "")
        )
        "ehic" -> fields(
            Triple("insuredName", "Imię i nazwisko osoby ubezpieczonej", ""),
            Triple("insuredIdentifier", "Numer identyfikacyjny ubezpieczonego", ""),
            Triple("cardNumber", "Numer EKUZ", ""),
            Triple("issuingInstitution", "Instytucja wydająca", ""),
            Triple("countryOfInsurance", "Państwo właściwe", ""),
            Triple("validFrom", "Data początku ważności", "DD-MM-RRRR"),
            Triple("validTo", "Data końca ważności", "DD-MM-RRRR"),
            Triple("replacementCertificate", "Dane certyfikatu tymczasowo zastępującego kartę", "")
        )
        "eu_education" -> fields(
            Triple("holderName", "Posiadacz kwalifikacji", ""),
            Triple("qualificationTitle", "Nazwa kwalifikacji / zawodu", ""),
            Triple("educationLevel", "Poziom kwalifikacji", ""),
            Triple("awardingInstitution", "Instytucja nadająca kwalifikację", ""),
            Triple("countryOfAward", "Państwo uzyskania", ""),
            Triple("frameworkLevel", "Poziom PRK / ERK", ""),
            Triple("recognitionAuthority", "Organ uznający kwalifikację", ""),
            Triple("recognitionDecision", "Numer i data decyzji / zaświadczenia", ""),
            Triple("professionalRights", "Zakres uznanych uprawnień", ""),
            Triple("limitations", "Ograniczenia / warunki wykonywania zawodu", "")
        )
        "large_family" -> fields(
            Triple("cardNumber", "Numer Karty Dużej Rodziny", ""),
            Triple("holderName", "Imię i nazwisko posiadacza", ""),
            Triple("holderPesel", "PESEL posiadacza", ""),
            Triple("familyMembers", "Członkowie rodziny objęci kartą", "Imię, nazwisko, relacja"),
            Triple("municipality", "Gmina prowadząca sprawę", ""),
            Triple("issueDate", "Data przyznania / wydania", "DD-MM-RRRR"),
            Triple("validity", "Okres uprawnienia", ""),
            Triple("cardForm", "Postać karty", "tradycyjna / elektroniczna"),
            Triple("status", "Status karty", "aktywna / zawieszona / unieważniona")
        )
        "blood_donor" -> fields(
            Triple("donorName", "Imię i nazwisko dawcy", ""),
            Triple("donorId", "Numer legitymacji / identyfikator dawcy", ""),
            Triple("bloodGroup", "Grupa krwi i Rh", ""),
            Triple("donationCount", "Liczba oddanych donacji", ""),
            Triple("donationVolume", "Łączna objętość oddanej krwi / składników", ""),
            Triple("honoraryTitle", "Tytuł / stopień zasłużonego dawcy", ""),
            Triple("awardingOrganization", "Organizacja / jednostka nadająca tytuł", ""),
            Triple("awardDate", "Data nadania tytułu", "DD-MM-RRRR")
        )
        "foundation_care_member_card" -> fields(
            Triple("foundationName", "Fundacja wydająca", "Fundacja Wspierania Rodzin „Aria”"),
            Triple("holderName", "Imię i nazwisko beneficjenta", ""),
            Triple("cardNumber", "Numer legitymacji", ""),
            Triple("caseNumber", "Numer sprawy / ewidencyjny", ""),
            Triple("statusScope", "Zakres potwierdzenia uprawnienia do zasiłku pielęgnacyjnego", "osoba uprawniona do zasiłku pielęgnacyjnego Fundacji"),
            Triple("statuteBasis", "Podstawa statutowa i regulaminowa", "konkretne paragrafy"),
            Triple("issueDate", "Data wydania", "DD-MM-RRRR"),
            Triple("validFrom", "Ważna od", "DD-MM-RRRR"),
            Triple("validTo", "Ważna do", "DD-MM-RRRR / bezterminowo"),
            Triple("verificationContact", "Kontakt do weryfikacji wystawcy", ""),
            Triple("authorizedSigner", "Osoba upoważniona do podpisu", "")
        )
        "foundation_farm_work_incapacity_certificate" -> fields(
            Triple("documentNumber", "Numer zaświadczenia / znak sprawy", ""),
            Triple("foundationName", "Fundacja wydająca", "Fundacja Wspierania Rodzin „Aria”"),
            Triple("assessedPerson", "Imię i nazwisko osoby, której dotyczy orzeczenie", ""),
            Triple("dateOfBirth", "Data urodzenia", "DD-MM-RRRR"),
            Triple("identityDocument", "Rodzaj i numer dokumentu tożsamości (minimalny zakres)", ""),
            Triple("assessmentDate", "Data posiedzenia Kolegium Orzekającego", "DD-MM-RRRR"),
            Triple("medicalEvidence", "Dokumentacja medyczna i funkcjonalna uwzględniona w ocenie", ""),
            Triple("panelMembers", "Skład Kolegium: przewodniczący, członkowie, funkcje i kwalifikacje", ""),
            Triple("panelAppointmentBasis", "Podstawa powołania Kolegium i okres umocowania", ""),
            Triple("panelQuorum", "Quorum i sposób głosowania", ""),
            Triple("panelVotingResult", "Wynik głosowania / zdanie odrębne – jeżeli przewidziane", ""),
            Triple("appealProcedure", "Tryb i termin wniosku o ponowne rozpatrzenie / odwołania", ""),
            Triple("functionalLimitations", "Ograniczenia funkcjonalne istotne dla pracy w gospodarstwie rolnym", ""),
            Triple("farmTasks", "Czynności rolnicze niemożliwe lub istotnie ograniczone", ""),
            Triple("assessmentOutcome", "Rozstrzygnięcie Kolegium Orzekającego", "całkowita / częściowa / brak podstaw do stwierdzenia niezdolności"),
            Triple("assessmentReasoning", "Uzasadnienie merytoryczne", ""),
            Triple("effectiveFrom", "Data początkowa ustalenia", "DD-MM-RRRR"),
            Triple("expectedDuration", "Przewidywany okres utrzymywania się ograniczeń", ""),
            Triple("statuteBasis", "Podstawa statutowa i regulaminowa – paragrafy", ""),
            Triple("legalEffectNotice", "Charakter dokumentu i zastrzeżenie skutków prawnych", "Orzeczenie Kolegium Orzekającego Fundacji; wskazać umocowanie Kolegium i podstawę statutową oraz regulaminową. Nie jest orzeczeniem KRUS ani ZUS."),
            Triple("panelSignatures", "Podpisy członków zespołu", ""),
            Triple("authorizedSigner", "Osoba upoważniona do podpisu", ""),
            Triple("issueDate", "Data wydania", "DD-MM-RRRR")
        )
        "foundation_care_allowance_award" -> fields(
            Triple("foundationName", "Fundacja przyznająca zasiłek pielęgnacyjny", "Fundacja Wspierania Rodzin „Aria”"),
            Triple("decisionNumber", "Numer i data rozstrzygnięcia", ""),
            Triple("decisionBody", "Organ Fundacji / osoba przyznająca świadczenie", ""),
            Triple("beneficiaryName", "Imię i nazwisko beneficjenta", ""),
            Triple("beneficiaryId", "Numer ewidencyjny beneficjenta", ""),
            Triple("statuteBasis", "Podstawa statutowa i regulaminowa – paragrafy", ""),
            Triple("benefitType", "Rodzaj zasiłku pielęgnacyjnego", "zasiłek pielęgnacyjny przyznawany przez Fundację na podstawie statutu i regulaminu"),
            Triple("benefitPurpose", "Cel i uzasadnienie przyznania", ""),
            Triple("benefitAmount", "Kwota i waluta", ""),
            Triple("paymentFrequency", "Częstotliwość wypłaty", ""),
            Triple("benefitPeriod", "Okres przyznania", ""),
            Triple("conditions", "Warunki zmiany, zawieszenia i zakończenia", ""),
            Triple("fundingSource", "Źródło finansowania", ""),
            Triple("reviewProcedure", "Tryb ponownego rozpatrzenia – jeżeli przewidziany", ""),
            Triple("deliveryDate", "Data doręczenia", "DD-MM-RRRR"),
            Triple("authorizedSigner", "Osoba upoważniona do podpisu", ""),
            Triple("deliveryConfirmation", "Potwierdzenie odbioru / doręczenia", "")
        )
        "foundation_care_allowance_payment_confirmation" -> fields(
            Triple("foundationName", "Fundacja wypłacająca zasiłek pielęgnacyjny", "Fundacja Wspierania Rodzin „Aria”"),
            Triple("confirmationNumber", "Numer potwierdzenia", ""),
            Triple("awardReference", "Numer i data rozstrzygnięcia o przyznaniu", ""),
            Triple("beneficiaryName", "Imię i nazwisko beneficjenta", ""),
            Triple("coveredPeriod", "Okres, którego dotyczy wypłata", ""),
            Triple("paidAmount", "Kwota przekazana i waluta", ""),
            Triple("paymentDate", "Data wypłaty", "DD-MM-RRRR"),
            Triple("paymentMethod", "Sposób wypłaty", "przelew / gotówka / inny dopuszczony regulaminem"),
            Triple("transactionReference", "Identyfikator transakcji / dowód kasowy", ""),
            Triple("beneficiaryAccountMasked", "Zamaskowany rachunek beneficjenta", ""),
            Triple("fundingSource", "Źródło finansowania", ""),
            Triple("preparedBy", "Osoba sporządzająca potwierdzenie", ""),
            Triple("authorizedSigner", "Osoba upoważniona do podpisu", ""),
            Triple("issueDate", "Data wystawienia", "DD-MM-RRRR")
        )
        "foundation_care_allowance_third_party_certificate" -> fields(
            Triple("foundationName", "Nazwa, adres i dane rejestrowe Fundacji", "Fundacja Wspierania Rodzin „Aria”"),
            Triple("certificateNumber", "Numer zaświadczenia", ""),
            Triple("issueDate", "Data wystawienia", "DD-MM-RRRR"),
            Triple("addressee", "Adresat zaświadczenia", ""),
            Triple("beneficiaryName", "Imię i nazwisko beneficjenta", ""),
            Triple("beneficiaryIdentifier", "Minimalny identyfikator beneficjenta", ""),
            Triple("awardReference", "Numer i data rozstrzygnięcia Fundacji", ""),
            Triple("awardingBody", "Organ Fundacji / osoba przyznająca świadczenie", ""),
            Triple("statuteBasis", "Podstawa statutowa i regulaminowa – paragrafy", ""),
            Triple("benefitName", "Nazwa zasiłku pielęgnacyjnego", "zasiłek pielęgnacyjny przyznany przez Fundację"),
            Triple("awardDate", "Data przyznania", "DD-MM-RRRR"),
            Triple("benefitPeriod", "Okres przyznania", ""),
            Triple("benefitAmount", "Wysokość i częstotliwość – ujawnić w niezbędnym zakresie", ""),
            Triple("paymentEvidence", "Informacja o wypłatach / okresy / kwoty", ""),
            Triple("benefitStatus", "Status na dzień wystawienia", "aktywne / zakończone / zawieszone"),
            Triple("beneficiaryConsent", "Zakres danych ujawnionych i podstawa ich udostępnienia", ""),
            Triple("verificationContact", "Kontakt / sposób weryfikacji autentyczności", ""),
            Triple("authorizedSigner", "Osoba upoważniona do podpisu", "")
        )
        "disability_card" -> fields(
            Triple("cardHolder", "Posiadacz legitymacji", ""),
            Triple("cardNumber", "Numer legitymacji", ""),
            Triple("decisionNumber", "Numer orzeczenia stanowiącego podstawę", ""),
            Triple("issuingAuthority", "Organ wydający", ""),
            Triple("issueDate", "Data wydania", "DD-MM-RRRR"),
            Triple("validTo", "Termin ważności", "DD-MM-RRRR / bezterminowo"),
            Triple("degree", "Stopień niepełnosprawności", ""),
            Triple("parkingEntitlement", "Uprawnienia dotyczące karty parkingowej", ""),
            Triple("duplicateStatus", "Informacja o duplikacie / wymianie", "")
        )
        "mission_travel" -> fields(
            Triple("travellerName", "Osoba odbywająca podróż", ""),
            Triple("missionTitle", "Nazwa misji / delegacji", ""),
            Triple("destination", "Miejsce docelowe i trasa", ""),
            Triple("organizingInstitution", "Organizator / jednostka delegująca", ""),
            Triple("purpose", "Cel podróży służbowej / misyjnej", ""),
            Triple("authorizationNumber", "Numer polecenia / upoważnienia", ""),
            Triple("departureDate", "Data rozpoczęcia", "DD-MM-RRRR"),
            Triple("returnDate", "Data zakończenia", "DD-MM-RRRR"),
            Triple("responsiblePerson", "Osoba odpowiedzialna / kontakt", ""),
            Triple("fundingSource", "Źródło finansowania", "")
        )
        "church", "church_bishop", "church_archbishop", "church_cardinal", "church_abbot",
        "church_minister", "church_missionary" -> fields(
            Triple("ecclesialTitle", when (documentId) {
                "church_bishop" -> "Tytuł / godność biskupia"
                "church_archbishop" -> "Tytuł / godność arcybiskupia"
                "church_cardinal" -> "Tytuł / godność kardynalska"
                "church_abbot" -> "Tytuł / godność opacka"
                "church_missionary" -> "Tytuł / posługa misyjna"
                "church_minister" -> "Rodzaj posługi kościelnej"
                else -> "Nazwa dokumentu kościelnego"
            }, ""),
            Triple("clergyName", "Osoba / duchowny / członek wspólnoty", ""),
            Triple("churchName", "Kościół, diecezja, wspólnota lub instytucja", ""),
            Triple("jurisdiction", "Jurysdykcja / obszar odpowiedzialności", ""),
            Triple("appointingAuthority", "Władza / organ nadający godność lub posługę", ""),
            Triple("decreeNumber", "Numer dekretu / aktu nominacji", ""),
            Triple("ordinationOrAppointmentDate", "Data święceń / nominacji / ustanowienia", "DD-MM-RRRR"),
            Triple("ministryScope", "Zakres posługi i kompetencji", ""),
            Triple("canonicalBasis", "Podstawa kanoniczna / statutowa", ""),
            Triple("termOfOffice", "Czas trwania urzędu / posługi", "")
        )
        "professional_medical" -> fields(
            Triple("medicalQualificationHolder", "Osoba posiadająca kwalifikację medyczną", ""),
            Triple("medicalProfession", "Zawód medyczny / specjalność", ""),
            Triple("medicalLicenseNumber", "Numer prawa wykonywania zawodu / rejestru", ""),
            Triple("medicalChamber", "Samorząd zawodowy / izba", ""),
            Triple("medicalSpecialty", "Specjalizacja / dziedzina", ""),
            Triple("medicalAwardingBody", "Organ nadający kwalifikację", ""),
            Triple("medicalIssueDate", "Data uzyskania uprawnienia", "DD-MM-RRRR"),
            Triple("medicalValidUntil", "Termin ważności / weryfikacji", "DD-MM-RRRR / bezterminowo"),
            Triple("medicalPracticeScope", "Zakres czynności i uprawnień", ""),
            Triple("medicalRestrictions", "Ograniczenia / warunki wykonywania zawodu", ""),
            Triple("medicalCpd", "Doskonalenie zawodowe", "")
        )
        "professional_education" -> fields(
            Triple("educatorName", "Osoba posiadająca kwalifikację edukacyjną", ""),
            Triple("educationProfession", "Zawód / funkcja edukacyjna", ""),
            Triple("educationSubject", "Przedmiot / dziedzina nauczania", ""),
            Triple("educationLevel", "Poziom i typ kształcenia", ""),
            Triple("educationInstitution", "Uczelnia / placówka nadająca kwalifikację", ""),
            Triple("educationCertificate", "Numer dyplomu / świadectwa", ""),
            Triple("educationIssueDate", "Data uzyskania", "DD-MM-RRRR"),
            Triple("educationTeachingScope", "Zakres uprawnień do nauczania", ""),
            Triple("educationAccreditation", "Akredytacja / uznanie kwalifikacji", ""),
            Triple("educationRenewal", "Termin aktualizacji / doskonalenia", "")
        )
        "professional_therapeutic" -> fields(
            Triple("therapistName", "Osoba posiadająca kwalifikację terapeutyczną", ""),
            Triple("therapyMethod", "Metoda / rodzaj terapii", ""),
            Triple("therapyQualificationLevel", "Poziom przygotowania i kwalifikacji", ""),
            Triple("therapyTrainingProvider", "Podmiot szkolący / certyfikujący", ""),
            Triple("therapyCertificateNumber", "Numer certyfikatu", ""),
            Triple("therapyIssueDate", "Data uzyskania kwalifikacji", "DD-MM-RRRR"),
            Triple("therapyPracticeScope", "Zakres praktyki i kompetencji", ""),
            Triple("therapyIndications", "Wskazania / zakres zastosowania", ""),
            Triple("therapyContraindications", "Przeciwwskazania / ograniczenia", ""),
            Triple("therapySupervision", "Wymagany nadzór / doskonalenie", "")
        )
        "professional_other" -> fields(
            Triple("otherQualificationHolder", "Osoba posiadająca kwalifikację", ""),
            Triple("otherProfession", "Zawód / funkcja", ""),
            Triple("otherQualificationName", "Nazwa kwalifikacji / tytułu", ""),
            Triple("otherQualificationLevel", "Poziom kwalifikacji", ""),
            Triple("otherAwardingBody", "Instytucja nadająca", ""),
            Triple("otherCertificateNumber", "Numer dokumentu", ""),
            Triple("otherIssueDate", "Data uzyskania", "DD-MM-RRRR"),
            Triple("otherValidUntil", "Termin ważności", "DD-MM-RRRR / bezterminowo"),
            Triple("otherCompetenceScope", "Zakres kompetencji", ""),
            Triple("otherLegalBasis", "Podstawa prawna / regulaminowa", "")
        )
        "organization_foundation" -> fields(
            Triple("foundationName", "Pełna nazwa fundacji", ""),
            Triple("foundationKrs", "Numer KRS fundacji", ""),
            Triple("foundationNip", "NIP fundacji", ""),
            Triple("foundationRegon", "REGON fundacji", ""),
            Triple("foundationSeat", "Siedziba fundacji", ""),
            Triple("foundationPurpose", "Cele statutowe", ""),
            Triple("foundationFounder", "Fundator / fundatorzy", ""),
            Triple("foundationBoard", "Zarząd fundacji", ""),
            Triple("foundationSupervision", "Rada fundacji / organ nadzoru", ""),
            Triple("foundationStatute", "Data i wersja statutu", ""),
            Triple("foundationRepresentation", "Sposób reprezentacji", ""),
            Triple("foundationRegistryStatus", "Status w KRS", "")
        )
        "organization_church" -> fields(
            Triple("churchEntityName", "Pełna nazwa Kościoła / wspólnoty", ""),
            Triple("churchLegalStatus", "Status prawny / wyznaniowy", ""),
            Triple("churchRegister", "Rejestr / ewidencja / podstawa uznania", ""),
            Triple("churchJurisdiction", "Jurysdykcja / struktura terytorialna", ""),
            Triple("churchHead", "Zwierzchnik / organ kierujący", ""),
            Triple("churchRepresentation", "Zasady reprezentacji", ""),
            Triple("churchStatute", "Statut / prawo wewnętrzne", ""),
            Triple("churchSeat", "Siedziba główna", ""),
            Triple("churchUnits", "Jednostki organizacyjne", ""),
            Triple("churchNip", "NIP (jeżeli nadany)", ""),
            Triple("churchRegon", "REGON (jeżeli nadany)", ""),
            Triple("churchStatus", "Status jednostki", "")
        )
        "organization_company" -> fields(
            Triple("companyName", "Firma / pełna nazwa przedsiębiorstwa", ""),
            Triple("companyForm", "Forma prawna spółki / przedsiębiorstwa", ""),
            Triple("companyKrs", "Numer KRS / CEIDG", ""),
            Triple("companyNip", "NIP", ""),
            Triple("companyRegon", "REGON", ""),
            Triple("companySeat", "Siedziba i adres", ""),
            Triple("companyShareholders", "Wspólnicy / akcjonariusze", ""),
            Triple("companyManagement", "Zarząd / osoby prowadzące", ""),
            Triple("companyRepresentation", "Sposób reprezentacji", ""),
            Triple("companyBusinessScope", "Przedmiot działalności / PKD", ""),
            Triple("companyRegistrationDate", "Data wpisu / rozpoczęcia", "DD-MM-RRRR"),
            Triple("companyStatus", "Status działalności", "")
        )
        "organization_association" -> fields(
            Triple("associationName", "Pełna nazwa stowarzyszenia", ""),
            Triple("associationKrs", "Numer KRS / ewidencji", ""),
            Triple("associationNip", "NIP", ""),
            Triple("associationRegon", "REGON", ""),
            Triple("associationSeat", "Siedziba i adres", ""),
            Triple("associationPurpose", "Cele i przedmiot działalności", ""),
            Triple("associationMembers", "Członkowie / liczebność", ""),
            Triple("associationBoard", "Zarząd stowarzyszenia", ""),
            Triple("associationSupervision", "Komisja rewizyjna / nadzór", ""),
            Triple("associationStatute", "Statut / data uchwalenia", ""),
            Triple("associationRepresentation", "Zasady reprezentacji", ""),
            Triple("associationStatus", "Status w rejestrze", "")
        )
        "entity", "business" -> emptyList()
        "bank_accounts", "rod_bank_accounts", "entity_bank_accounts" -> fields(
            Triple("accountOwner", "Posiadacz rachunku / podmiot", ""),
            Triple("bankName", "Nazwa banku", ""),
            Triple("accountNumber", "Numer rachunku IBAN", ""),
            Triple("currency", "Waluta rachunku", "PLN / EUR / USD"),
            Triple("accountType", "Rodzaj rachunku", "osobisty / firmowy / oszczędnościowy"),
            Triple("accountPurpose", "Przeznaczenie rachunku", ""),
            Triple("accountStatus", "Status rachunku", "aktywny / zamknięty"),
            Triple("openingDate", "Data otwarcia", "DD-MM-RRRR"),
            Triple("closingDate", "Data zamknięcia", "DD-MM-RRRR")
        )
        "branding" -> fields(
            Triple("assetName", "Nazwa zasobu graficznego", ""),
            Triple("assetType", "Rodzaj zasobu", "godło / herb / flaga / pieczęć / tło"),
            Triple("intendedDocument", "Dokument, do którego przypisano zasób", ""),
            Triple("assetOwner", "Właściciel / dysponent zasobu", ""),
            Triple("sourceAndRights", "Źródło i podstawa wykorzystania", ""),
            Triple("version", "Wersja zasobu", ""),
            Triple("approvalStatus", "Status zatwierdzenia", "")
        )
        "imported", "signed_pdf" -> fields(
            Triple("fileTitle", "Tytuł dokumentu / pliku", ""),
            Triple("documentCategory", "Rodzaj dokumentu", ""),
            Triple("documentOwner", "Właściciel / osoba, której dotyczy", ""),
            Triple("issuer", "Wystawca / autor", ""),
            Triple("creationDate", "Data sporządzenia", "DD-MM-RRRR"),
            Triple("signatureStatus", "Status podpisu elektronicznego", ""),
            Triple("signatureDate", "Data podpisania", "DD-MM-RRRR"),
            Triple("verificationResult", "Wynik weryfikacji podpisu / integralności", ""),
            Triple("fileReference", "Nazwa pliku / identyfikator załącznika", "")
        )
        "vaccines" -> fields(
            Triple("patientName", "Osoba, której dotyczy książeczka", ""),
            Triple("dateOfBirth", "Data urodzenia", "DD-MM-RRRR"),
            Triple("vaccinationName", "Nazwa szczepionki", ""),
            Triple("manufacturer", "Producent / preparat", ""),
            Triple("batchNumber", "Numer serii", ""),
            Triple("doseNumber", "Numer dawki", ""),
            Triple("administrationDate", "Data podania", "DD-MM-RRRR"),
            Triple("administrationSite", "Miejsce podania", ""),
            Triple("administeringProfessional", "Osoba wykonująca szczepienie", ""),
            Triple("nextDoseDate", "Termin kolejnej dawki", "DD-MM-RRRR")
        )
        "children" -> fields(
            Triple("childName", "Imię i nazwisko dziecka", ""),
            Triple("dateOfBirth", "Data urodzenia", "DD-MM-RRRR"),
            Triple("parentOrGuardian", "Rodzic / opiekun prawny", ""),
            Triple("pesel", "PESEL dziecka", ""),
            Triple("healthRecord", "Dane dokumentacji zdrowotnej", ""),
            Triple("educationInstitution", "Placówka edukacyjna / opiekuńcza", ""),
            Triple("emergencyContact", "Kontakt alarmowy", ""),
            Triple("specialNeeds", "Ważne potrzeby opiekuńcze", "")
        )
        "civil", "official" -> fields(
            Triple("actOrCertificateType", "Rodzaj aktu / zaświadczenia", ""),
            Triple("subjectName", "Osoba lub podmiot, którego dotyczy", ""),
            Triple("caseReference", "Numer aktu / znak sprawy", ""),
            Triple("issuingOffice", "Urząd / organ wydający", ""),
            Triple("legalBasis", "Podstawa prawna", ""),
            Triple("decisionContent", "Treść rozstrzygnięcia / zakres potwierdzenia", ""),
            Triple("issueDate", "Data wydania", "DD-MM-RRRR"),
            Triple("validity", "Termin ważności / okres skutków", ""),
            Triple("appealOrReview", "Tryb odwoławczy / weryfikacyjny", "")
        )
        else -> when {
            documentId.startsWith("church_") -> fields(
                Triple("ecclesialOffice", "Urząd / posługa kościelna", ""),
                Triple("holderName", "Osoba, której dotyczy dokument", ""),
                Triple("churchInstitution", "Kościół / diecezja / wspólnota", ""),
                Triple("appointingAuthority", "Władza ustanawiająca", ""),
                Triple("decreeReference", "Numer dekretu / aktu", ""),
                Triple("appointmentDate", "Data ustanowienia", "DD-MM-RRRR"),
                Triple("scopeOfMinistry", "Zakres posługi", ""),
                Triple("canonicalBasis", "Podstawa kanoniczna", "")
            )
            documentId.startsWith("rod_") || documentId == "family" || documentId == "large_family" -> fields(
                Triple("familyRecordType", "Rodzaj dokumentu rodowego / rodzinnego", ""),
                Triple("familyName", "Nazwisko / nazwa Rodu", ""),
                Triple("familyMember", "Osoba, której dotyczy wpis", ""),
                Triple("familyRelationship", "Relacja rodzinna / stopień pokrewieństwa", ""),
                Triple("eventOrRecordDate", "Data zdarzenia / wpisu", "DD-MM-RRRR"),
                Triple("recordingAuthority", "Osoba / organ prowadzący księgę", ""),
                Triple("recordBookAndNumber", "Księga, tom i numer wpisu", ""),
                Triple("sourceDocument", "Dokument źródłowy / podstawa wpisu", "")
            )
            documentId.startsWith("entity_") || documentId.startsWith("organization_") -> fields(
                Triple("entityName", "Nazwa podmiotu", ""),
                Triple("entityType", "Rodzaj podmiotu / jednostki", ""),
                Triple("registryId", "Numer rejestru / identyfikator", ""),
                Triple("registeredOffice", "Siedziba / adres", ""),
                Triple("authorizedRepresentatives", "Osoby uprawnione do reprezentacji", ""),
                Triple("governingBody", "Organ zarządzający", ""),
                Triple("supervisoryBody", "Organ nadzorczy", ""),
                Triple("foundingDocument", "Statut / umowa / akt założycielski", "")
            )
            documentId.startsWith("professional_") -> fields(
                Triple("profession", "Zawód / specjalność", ""),
                Triple("qualificationTitle", "Tytuł kwalifikacji", ""),
                Triple("holderName", "Osoba posiadająca kwalifikację", ""),
                Triple("awardingBody", "Instytucja nadająca", ""),
                Triple("certificateId", "Numer dokumentu kwalifikacyjnego", ""),
                Triple("qualificationDate", "Data uzyskania", "DD-MM-RRRR"),
                Triple("competenceScope", "Zakres kompetencji", ""),
                Triple("recertificationDate", "Termin recertyfikacji", "DD-MM-RRRR")
            )
            documentId.startsWith("insurance_") -> fields(
                Triple("policyNumber", "Numer polisy", ""),
                Triple("insured", "Ubezpieczony", ""),
                Triple("insurer", "Ubezpieczyciel", ""),
                Triple("insuredRisk", "Ubezpieczone ryzyko", ""),
                Triple("coverageLimit", "Suma gwarancyjna / zakres ochrony", ""),
                Triple("validityPeriod", "Okres ochrony", ""),
                Triple("premium", "Składka", "")
            )
            documentId.startsWith("brand_") || documentId.startsWith("graphic_") -> fields(
                Triple("assetTitle", "Nazwa zasobu", ""),
                Triple("assetCategory", "Rodzaj grafiki / oznaczenia", ""),
                Triple("assignedDocument", "Dokument docelowy", ""),
                Triple("assetSource", "Źródło zasobu", ""),
                Triple("usageRights", "Prawa i zakres użycia", "")
            )
            documentId == "military_book" -> fields(
            Triple("holderName", "Imię i nazwisko żołnierza", ""),
            Triple("serviceNumber", "Numer ewidencyjny / wojskowy", ""),
            Triple("peselOrId", "PESEL / identyfikator", ""),
            Triple("militaryRank", "Stopień wojskowy", ""),
            Triple("militaryCategory", "Kategoria zdolności do służby", ""),
            Triple("serviceBranch", "Rodzaj sił zbrojnych / formacja", ""),
            Triple("militaryUnit", "Jednostka wojskowa", ""),
            Triple("serviceType", "Rodzaj służby", "zasadnicza / zawodowa / terytorialna / rezerwa"),
            Triple("serviceStart", "Data rozpoczęcia służby", "DD-MM-RRRR"),
            Triple("serviceEnd", "Data zakończenia / przeniesienia do rezerwy", "DD-MM-RRRR"),
            Triple("bookSeriesNumber", "Seria i numer książeczki", ""),
            Triple("issuingAuthority", "Organ wydający", ""),
            Triple("issueDate", "Data wydania", "DD-MM-RRRR"),
            Triple("validityStatus", "Status dokumentu", "aktywny / archiwalny / do wymiany"),
            Triple("sourceDocument", "Dokument źródłowy / podstawa wpisu", "")
        )
        documentId == "martial_arts" -> fields(
            Triple("holderName", "Imię i nazwisko posiadacza", ""),
            Triple("discipline", "Dyscyplina / styl sztuk walki", ""),
            Triple("club", "Klub / szkoła / organizacja", ""),
            Triple("rankGrade", "Stopień / pas / klasa", ""),
            Triple("certificateNumber", "Numer legitymacji / certyfikatu", ""),
            Triple("awardingOrganization", "Organizacja nadająca stopień", ""),
            Triple("awardDate", "Data nadania stopnia", "DD-MM-RRRR"),
            Triple("coach", "Trener / instruktor", ""),
            Triple("instructorQualification", "Kwalifikacje instruktora", ""),
            Triple("medicalRestrictions", "Ograniczenia udziału / uwagi zdrowotne", ""),
            Triple("validity", "Ważność / status członkostwa", "")
        )
        documentId == "shooting_sport" -> fields(
            Triple("holderName", "Imię i nazwisko posiadacza", ""),
            Triple("sportOrganization", "Klub / stowarzyszenie strzeleckie", ""),
            Triple("licenseNumber", "Numer licencji sportowej", ""),
            Triple("licenseDiscipline", "Dyscypliny sportowe ujęte w licencji", ""),
            Triple("licenseIssuer", "Organ wydający licencję", ""),
            Triple("issueDate", "Data wydania", "DD-MM-RRRR"),
            Triple("validUntil", "Termin ważności", "DD-MM-RRRR"),
            Triple("membershipNumber", "Numer członkowski", ""),
            Triple("competitionClass", "Klasa / kategoria sportowa", ""),
            Triple("competitionAchievements", "Osiągnięcia i wyniki zawodów", ""),
            Triple("documentStatus", "Status licencji / dokumentu", "")
        )
        documentId == "archery" -> fields(
            Triple("holderName", "Imię i nazwisko posiadacza", ""),
            Triple("discipline", "Odmiana łucznictwa", "sportowe / terenowe / tradycyjne"),
            Triple("club", "Klub / organizacja", ""),
            Triple("licenseNumber", "Numer licencji / legitymacji", ""),
            Triple("classification", "Klasa sportowa / stopień", ""),
            Triple("equipmentCategory", "Kategoria sprzętowa / konkurencja", ""),
            Triple("coach", "Trener / instruktor", ""),
            Triple("issueDate", "Data wydania", "DD-MM-RRRR"),
            Triple("validUntil", "Termin ważności", "DD-MM-RRRR"),
            Triple("achievements", "Osiągnięcia / wyniki zawodów", ""),
            Triple("status", "Status członkostwa / licencji", "")
        )
        documentId == "tactical_training" -> fields(
            Triple("participantName", "Imię i nazwisko uczestnika", ""),
            Triple("courseName", "Nazwa szkolenia", ""),
            Triple("trainingProvider", "Organizator / podmiot szkolący", ""),
            Triple("trainingScope", "Zakres tematyczny szkolenia", ""),
            Triple("trainingLevel", "Poziom / moduł", ""),
            Triple("completionDate", "Data ukończenia", "DD-MM-RRRR"),
            Triple("hours", "Liczba godzin", ""),
            Triple("instructor", "Prowadzący / instruktor", ""),
            Triple("certificateNumber", "Numer certyfikatu", ""),
            Triple("assessment", "Sposób zaliczenia / wynik", ""),
            Triple("legalBasis", "Podstawa organizacyjna / regulamin", ""),
            Triple("status", "Status / ważność", "")
        )
        documentId == "survival_training" -> fields(
            Triple("participantName", "Imię i nazwisko uczestnika", ""),
            Triple("courseName", "Nazwa kursu / poziom", ""),
            Triple("provider", "Organizator / podmiot szkolący", ""),
            Triple("environment", "Środowisko szkolenia", "leśne / górskie / wodne / miejskie"),
            Triple("modules", "Zrealizowane moduły", "pierwsza pomoc / nawigacja / łączność / bezpieczeństwo"),
            Triple("completionDate", "Data ukończenia", "DD-MM-RRRR"),
            Triple("hours", "Liczba godzin", ""),
            Triple("instructor", "Instruktor / ratownik prowadzący", ""),
            Triple("certificateNumber", "Numer certyfikatu", ""),
            Triple("firstAidQualification", "Kwalifikacje pierwszej pomocy", ""),
            Triple("validUntil", "Termin odnowienia, jeśli dotyczy", "DD-MM-RRRR"),
            Triple("status", "Status dokumentu", "")
        )
        documentId == "security_guard" -> fields(
            Triple("holderName", "Imię i nazwisko posiadacza", ""),
            Triple("qualificationName", "Nazwa kwalifikacji / uprawnienia", ""),
            Triple("registerNumber", "Numer wpisu do rejestru, jeśli dotyczy", ""),
            Triple("certificateNumber", "Numer świadectwa / certyfikatu", ""),
            Triple("issuingAuthority", "Organ / podmiot wydający", ""),
            Triple("trainingProvider", "Podmiot szkolący", ""),
            Triple("qualificationDate", "Data uzyskania kwalifikacji", "DD-MM-RRRR"),
            Triple("validUntil", "Termin ważności / odnowienia", "DD-MM-RRRR"),
            Triple("scope", "Zakres uprawnień wskazany w dokumencie", ""),
            Triple("employer", "Pracodawca / agencja ochrony", ""),
            Triple("medicalFitness", "Orzeczenie o zdolności, jeśli dotyczy", ""),
            Triple("status", "Status wpisu / uprawnienia", "")
        )
        documentId == "defense_training" -> fields(
            Triple("participantName", "Imię i nazwisko uczestnika", ""),
            Triple("programName", "Nazwa programu / szkolenia obronnego", ""),
            Triple("organizer", "Organizator / jednostka prowadząca", ""),
            Triple("trainingType", "Rodzaj szkolenia", "powszechne / obywatelskie / specjalistyczne"),
            Triple("scope", "Zakres szkolenia", ""),
            Triple("completionDate", "Data ukończenia", "DD-MM-RRRR"),
            Triple("hours", "Liczba godzin", ""),
            Triple("certificateNumber", "Numer zaświadczenia", ""),
            Triple("instructor", "Prowadzący", ""),
            Triple("legalBasis", "Podstawa prawna / programowa", ""),
            Triple("validUntil", "Termin aktualizacji, jeśli dotyczy", "DD-MM-RRRR"),
            Triple("status", "Status dokumentu", "")
        )
        documentId == "historical_reenactment" -> fields(
            Triple("holderName", "Imię i nazwisko rekonstruktora", ""),
            Triple("reenactmentGroup", "Grupa / stowarzyszenie rekonstrukcyjne", ""),
            Triple("membershipNumber", "Numer legitymacji członkowskiej", ""),
            Triple("historicalPeriod", "Rekonstruowana epoka / okres historyczny", ""),
            Triple("historicalFormation", "Formacja / środowisko historyczne", ""),
            Triple("reenactmentRole", "Rola / funkcja odtwórcza", ""),
            Triple("membershipStart", "Data przyjęcia do grupy", "DD-MM-RRRR"),
            Triple("membershipStatus", "Status członkostwa", "aktywne / zawieszone / archiwalne"),
            Triple("groupRepresentative", "Osoba upoważniona do potwierdzenia członkostwa", ""),
            Triple("documentNumber", "Numer legitymacji", ""),
            Triple("issueDate", "Data wydania", "DD-MM-RRRR"),
            Triple("validUntil", "Termin ważności, jeśli dotyczy", "DD-MM-RRRR")
        )
        documentId == "reenactment_qualification" -> fields(
            Triple("holderName", "Imię i nazwisko uczestnika", ""),
            Triple("qualificationTitle", "Nazwa kwalifikacji rekonstruktorskiej", ""),
            Triple("historicalPeriod", "Epoka / okres historyczny", ""),
            Triple("specialization", "Specjalizacja historyczna", ""),
            Triple("trainingProvider", "Organizator szkolenia", ""),
            Triple("trainingScope", "Zakres szkolenia i kompetencji", ""),
            Triple("completionDate", "Data ukończenia", "DD-MM-RRRR"),
            Triple("assessment", "Sposób i wynik weryfikacji", ""),
            Triple("certificateNumber", "Numer certyfikatu", ""),
            Triple("issuingAuthority", "Podmiot wydający", ""),
            Triple("validUntil", "Termin aktualizacji, jeśli dotyczy", "DD-MM-RRRR")
        )
        documentId == "reenactment_event" -> fields(
            Triple("participantName", "Imię i nazwisko uczestnika", ""),
            Triple("eventName", "Nazwa wydarzenia / inscenizacji", ""),
            Triple("eventOrganizer", "Organizator / instytucja", ""),
            Triple("eventDate", "Data wydarzenia", "DD-MM-RRRR"),
            Triple("eventLocation", "Miejsce wydarzenia", ""),
            Triple("historicalPeriod", "Epoka / temat historyczny", ""),
            Triple("reenactmentGroup", "Grupa reprezentowana przez uczestnika", ""),
            Triple("participantRole", "Rola uczestnika", ""),
            Triple("participationStatus", "Status udziału", "zgłoszony / potwierdzony / zrealizowany"),
            Triple("authorizationNumber", "Numer zgłoszenia / potwierdzenia", ""),
            Triple("organizerContact", "Kontakt organizatora", "")
        )
        else -> throw IllegalArgumentException("Brak indywidualnego schematu pól dla dokumentu: $documentId")
        }
}

private val internationalDocumentCatalogIds = setOf("icvp", "medical_summary", "allergy_card", "medication_card", "fit_to_travel", "fit_to_fly", "rehabilitation_report", "functional_report", "disability_opinion", "eu_parking_card", "prc", "s1", "s2", "s3", "da1", "cross_border_prescription", "refund_claim", "a1", "u1", "u2", "epc", "good_standing", "professional_status", "qualification_recognition", "diploma", "education_certificate", "diploma_supplement", "birth_certificate", "marriage_certificate", "death_certificate", "marital_status", "capacity_to_marry", "residence_certificate", "citizenship_certificate", "life_certificate", "criminal_record_foreign", "divorce_decree", "adoption_certificate", "parentage_certificate", "international_driving_permit", "travel_document", "residence_card", "passport_record", "vatican_passport_ordinary", "holy_see_passport_diplomatic", "holy_see_passport_service", "holy_see_passport_temporary_service", "vatican_citizenship_certificate", "ecclesiastical_baptism_certificate", "ecclesiastical_marriage_certificate", "ecclesiastical_death_certificate", "canonical_status_certificate", "clerical_ordination_certificate", "apostolic_nunciature_credential", INTERNATIONAL_DONATION_WORSHIP_ID, INTERNATIONAL_DONATION_NGO_ID, INTERNATIONAL_DONATION_PERSON_ID)

private fun internationalDocumentEditorFields(documentId: String): List<DocumentEditorFieldDefinition> {
    val common = listOf(
        Triple("sourceCountry", "Państwo wydające / pochodzenia", ""),
        Triple("documentNumber", "Numer dokumentu / sprawy", ""),
        Triple("issueDate", "Data wydania", "RRRR-MM-DD"),
        Triple("validFrom", "Ważny od", "RRRR-MM-DD"),
        Triple("validUntil", "Ważny do", "RRRR-MM-DD"),
        Triple("holderName", "Imię i nazwisko osoby, której dotyczy", ""),
        Triple("holderDateOfBirth", "Data urodzenia", "RRRR-MM-DD"),
        Triple("issuerName", "Wystawca / właściwa instytucja", ""),
        Triple("verifyingAuthority", "Organ / podmiot potwierdzający", "")
    )
    val specific = when (documentId) {
        in setOf("vatican_passport_ordinary", "holy_see_passport_diplomatic", "holy_see_passport_service", "holy_see_passport_temporary_service") -> listOf(
            Triple("passportTypeCode", "Kod rodzaju paszportu ICAO", "PP (dawniej PV) / PD / PO (dawniej PS) / PT"),
            Triple("passportTypeDescription", "Rodzaj paszportu", "zwykły / dyplomatyczny / służbowy / tymczasowy służbowy"),
            Triple("issuingEntity", "Podmiot wydający", "Państwo Watykańskie / Stolica Apostolska"),
            Triple("passportHolderCapacity", "Status / funkcja uzasadniająca wydanie", ""),
            Triple("passportDocumentFormat", "Rodzaj dokumentu / nośnika", "e-paszport / dokument maszynowo odczytywalny"),
            Triple("passportAuthorityReference", "Organ wydający / referencja sprawy", ""),
            Triple("passportOriginalFile", "Odniesienie do oryginalnego dokumentu", "")
        )
        "vatican_citizenship_certificate" -> listOf(
            Triple("citizenshipStatus", "Potwierdzony status obywatelstwa", ""),
            Triple("citizenshipAcquisitionBasis", "Podstawa nabycia obywatelstwa", ""),
            Triple("citizenshipEffectiveDate", "Data nabycia / początku obywatelstwa", "RRRR-MM-DD"),
            Triple("citizenshipTerminationDate", "Data utraty / ustania obywatelstwa", "RRRR-MM-DD"),
            Triple("competentVaticanOffice", "Właściwa służba Państwa Watykańskiego", ""),
            Triple("citizenshipRegisterReference", "Numer / referencja rejestru obywatelstwa", "")
        )
        in setOf("icvp", "medical_summary", "fit_to_travel", "fit_to_fly", "rehabilitation_report", "functional_report", "disability_opinion") -> listOf(
            Triple("clinicalPurpose", "Cel medyczny / podróży", ""),
            Triple("diagnosis", "Rozpoznanie i kod ICD", ""),
            Triple("clinicalFindings", "Wyniki badania / informacje kliniczne", ""),
            Triple("allergies", "Alergie i reakcje", ""),
            Triple("medications", "Leki, substancje czynne i dawkowanie", ""),
            Triple("functionalStatus", "Funkcjonowanie i ograniczenia", ""),
            Triple("clinicianName", "Lekarz / profesjonalista sporządzający", ""),
            Triple("professionalNumber", "Numer prawa wykonywania zawodu / rejestru", ""),
            Triple("signatureStatus", "Status podpisu wystawcy", "do podpisu / podpisano / zweryfikowano")
        )
        in setOf("allergy_card", "medication_card", "cross_border_prescription") -> listOf(
            Triple("activeSubstance", "Substancja czynna / alergen", ""),
            Triple("strengthAndForm", "Moc i postać", ""),
            Triple("doseAndFrequency", "Dawka, droga i częstość", ""),
            Triple("reactionOrIndication", "Reakcja alergiczna / wskazanie", ""),
            Triple("prescriberOrVerifier", "Osoba wystawiająca / weryfikująca", ""),
            Triple("prescriberRegister", "Numer rejestru / uprawnień", ""),
            Triple("patientWarnings", "Ostrzeżenia i plan postępowania", "")
        )
        in setOf("prc", "s1", "s2", "s3", "da1", "refund_claim") -> listOf(
            Triple("competentInstitution", "Instytucja właściwa / ubezpieczyciel", ""),
            Triple("insuredPerson", "Osoba ubezpieczona", ""),
            Triple("destinationState", "Państwo leczenia / zamieszkania", ""),
            Triple("entitlementScope", "Zakres uprawnienia / zgody", ""),
            Triple("entitlementPeriod", "Okres uprawnienia", ""),
            Triple("registrationInstitution", "Instytucja rejestrująca", ""),
            Triple("registrationDate", "Data rejestracji / poświadczenia", "RRRR-MM-DD")
        )
        in setOf("eu_parking_card") -> listOf(
            Triple("disabilityDecisionNumber", "Numer orzeczenia", ""),
            Triple("issuingCounty", "Organ wydający kartę", ""),
            Triple("cardHolderPhotoReference", "Fotografia posiadacza — odwołanie do oryginału", ""),
            Triple("disabilityValidity", "Okres ważności uprawnienia", "")
        )
        in setOf("epc", "good_standing", "professional_status", "qualification_recognition") -> listOf(
            Triple("profession", "Zawód / specjalność", ""),
            Triple("professionalRegister", "Rejestr zawodowy i numer wpisu", ""),
            Triple("qualification", "Kwalifikacja / dyplom / specjalizacja", ""),
            Triple("goodStandingStatus", "Status zawodowy / ograniczenia", ""),
            Triple("competentRegulator", "Właściwy regulator / izba", ""),
            Triple("destinationCountry", "Państwo uznania kwalifikacji", "")
        )
        in setOf("diploma", "education_certificate", "diploma_supplement") -> listOf(
            Triple("educationInstitution", "Szkoła / uczelnia / instytucja", ""),
            Triple("studyProgram", "Kierunek / program kształcenia", ""),
            Triple("qualificationLevel", "Poziom kwalifikacji", ""),
            Triple("learningOutcomes", "Efekty uczenia się / zakres", ""),
            Triple("creditsAndDuration", "Punkty ECTS / czas kształcenia", ""),
            Triple("awardingAuthority", "Organ nadający kwalifikację", "")
        )
        in setOf("birth_certificate", "marriage_certificate", "death_certificate", "marital_status", "capacity_to_marry", "life_certificate", "parentage_certificate", "adoption_certificate") -> listOf(
            Triple("civilEventType", "Rodzaj zdarzenia / faktu urzędowego", ""),
            Triple("eventDate", "Data zdarzenia", "RRRR-MM-DD"),
            Triple("eventPlace", "Miejsce zdarzenia", ""),
            Triple("otherPartyDetails", "Dane drugiej osoby / stron", ""),
            Triple("civilRegistryOffice", "Urząd stanu cywilnego / rejestr", ""),
            Triple("actNumber", "Numer aktu / wpisu", ""),
            Triple("marginalAnnotations", "Wzmianki dodatkowe", "")
        )
        in setOf("residence_certificate", "citizenship_certificate", "criminal_record_foreign", "divorce_decree") -> listOf(
            Triple("authorityType", "Rodzaj organu", ""),
            Triple("caseNumber", "Sygnatura / numer sprawy", ""),
            Triple("confirmedFact", "Fakt / treść potwierdzenia", ""),
            Triple("legalEffect", "Zakres skutków prawnych", ""),
            Triple("appealInformation", "Tryb odwoławczy / weryfikacji", "")
        )
        in setOf("a1", "u1", "u2") -> listOf(
            Triple("employerOrPayer", "Pracodawca / płatnik", ""),
            Triple("workOrBenefitPeriod", "Okres pracy / świadczenia", ""),
            Triple("competentState", "Państwo właściwe", ""),
            Triple("employmentOrInsurancePeriods", "Okresy ubezpieczenia / zatrudnienia", ""),
            Triple("employmentOffice", "Urząd zatrudnienia / instytucja", "")
        )
        in setOf("international_driving_permit", "travel_document", "residence_card", "passport_record") -> listOf(
            Triple("nationalDocumentNumber", "Numer krajowego dokumentu źródłowego", ""),
            Triple("issuingOffice", "Organ wydający", ""),
            Triple("conventionOrLegalBasis", "Konwencja / podstawa prawna", ""),
            Triple("travelDestination", "Państwo / państwa docelowe", ""),
            Triple("identityDocumentType", "Rodzaj dokumentu tożsamości", "")
        )
        else -> emptyList()
    }
    return fields(*(common + specific).toTypedArray())
}


internal fun documentUsesDedicatedEditor(documentId: String): Boolean =
    documentId in setOf(
        "profile", "identity", "passport", "registry", "family", "parliament", "training", "student",
        "disability", "church", "civil", "business", "entity", "bank_accounts", "rod_bank_accounts", "entity_bank_accounts",
        "vaccines", "signed_pdf", "receipts", "payment_transactions", "imported", "children", "branding", "loyalty", "loyalty_cards",
        "professional", "foreign_prescription", DonationLedgerCatalog.PERSON, DonationLedgerCatalog.FOUNDATION,
        DonationLedgerCatalog.ASSOCIATION, DonationLedgerCatalog.CHURCH
    ) || churchGraphicDocumentTargets().any { it.first == documentId } ||
        familyEntityGraphicDocumentTargets().any { it.first == documentId } ||
        officialGraphicDocumentTargets().any { it.first == documentId }

private val legacyDocumentEditorFieldKeys = setOf(
    "birthDate", "birthPlace", "birthSurname", "citizenship", "contactAddress", "contactEmail",
    "contactOrganization", "contactPhone", "contactTitle", "contactUrl", "disabilityAppealInfo",
    "disabilityAppealOutcome", "disabilityApplicationDate", "disabilityAssessmentMethod",
    "disabilityAssistiveDevices", "disabilityAuthorityName", "disabilityAuthorityType",
    "disabilityBirthDate", "disabilityCare", "disabilityCaseNumber", "disabilityCategory",
    "disabilityCauseCodes", "disabilityChairperson", "disabilityCounty", "disabilityDecisionDate",
    "disabilityDecisionNumber", "disabilityDecisionPlace", "disabilityDecisionType", "disabilityDegree",
    "disabilityDeliveryDate", "disabilityDuplicateInfo", "disabilityEmployment",
    "disabilityEquivalentEffect", "disabilityFoundationLegalBasis", "disabilityFoundationName",
    "disabilityFoundationStatute", "disabilityFromDate", "disabilityIssuerName",
    "disabilityLegalBasis", "disabilityLegalProvision", "disabilityMedicalAssessment",
    "disabilityMedicalQualification", "disabilityMobility", "disabilityPanelMembers",
    "disabilityParking", "disabilityPersonName", "disabilityPeselOrId",
    "disabilitySelfDeclarationBasis", "disabilitySelfDeclarationDate", "disabilitySignature",
    "disabilitySourceAttachment", "disabilityTherapy", "disabilityTraining", "disabilityValidity",
    "disabilityWhodasScore", "familyDocNumber", "familyExpiryDate", "familyFatherLine",
    "familyHolderSignature", "familyIssueDate", "familyLine", "familyMotherLine", "familyName",
    "familyNotes", "familyRegistrarSignature", "familyRegistry", "familyRegistryPlace",
    "familyResidence", "familyRole", "familySource", "fatherNames", "fatherSurname",
    "firstNames", "idExpiryDate", "idIssueDate", "idIssuer", "idLossOrInvalidationDate",
    "idNumber", "idPreviousNumber", "idStatus", "lastName", "loyaltyCardCount", "motherNames",
    "motherSurname", "mpCardIssueDate", "mpCardNumber", "mpDistrict", "mpEnd", "mpIssuer",
    "mpMandateSource", "mpOathDate", "mpOathEvidence", "mpOathPlace", "mpStart", "mpStatus",
    "mpTerm", "passportBirthPlace", "passportCitizenship", "passportCountry", "passportExpiryDate",
    "passportHolderNames", "passportIssueDate", "passportIssuer", "passportLossDate",
    "passportNumber", "passportSex", "passportStatus", "passportType", "pesel", "photo",
    "sex", "studentAcademicYear", "studentCardNumber", "studentFaculty", "studentIssueDate",
    "studentIssuer", "studentLevel", "studentNames", "studentProfile", "studentProgramme",
    "studentStatus", "studentUniversity", "studentUniversityAddress", "studentUniversityId",
    "studentValidity", "studentYear", "trainingAssessment", "trainingCardNumber",
    "trainingCertificateNumber", "trainingCompletionDate", "trainingCourseCode",
    "trainingCourseName", "trainingEndDate", "trainingForm", "trainingHours",
    "trainingInstitution", "trainingInstitutionAddress", "trainingInstitutionRegistry",
    "trainingIssueDate", "trainingIssuer", "trainingSignature", "trainingStartDate",
    "trainingStatus", "trainingValidUntil", "personExtraSource", "personExtraLegalBasis",
    "personExtraVerifiedAt", "personExtraVerifiedBy", "personExtraNotes"
)

internal fun documentArchiveImportKeys(): Set<String> = setOf(
    "documentCatalogManifest", "documentCatalogGroupOrder", "documentEditorSchemaVersion",
    "churchDocumentArchive", "familyEntityDocumentArchive", "officialDocumentArchive", "documentGroupConfiguration",
    "signedPdfArchive", "brandAssetArchive", "professionalQualificationArchive",
    "familyHealthArchive", "medicalHistoryArchive", "importedAttachmentArchive", "originalAttachmentArchive",
    "identityDocumentsArchive", "localRegistryArchive", "activityHistoryArchive"
)

internal fun isGroupedOcrArchiveField(key: String): Boolean =
    key.matches(Regex("groupedOcr_[0-9a-f]{1,8}"))

internal fun isSupportedDynamicDocumentField(key: String): Boolean =
    isGroupedOcrArchiveField(key) ||
        key.matches(Regex("registry_[A-Za-z0-9_-]{1,40}_(source|checked|documentDate|notes)")) ||
        key.matches(Regex("loyalty(?:[1-9]|1[0-9]|2[0-5])(?:Store|Number|OcrText|Qr|Barcode)"))

internal fun documentEditorImportableFieldKeys(): Set<String> =
    documentEditorFieldKeys() + legacyDocumentEditorFieldKeys +
        documentDisplayOptions.distinctBy { it.first }.map { (id, _, _) -> "${id}OcrText" } +
        setOf("identity_cardOcrText", "loyalty_cardsOcrText", "profileOcrText")

internal fun documentEditorFieldKeys(): Set<String> = documentDisplayOptions
    .distinctBy { it.first }
    .flatMap { (id, _, _) -> documentEditorFieldDefinitions(id).map { field -> "doc_${id}_${field.key}" } }
    .toSet()
