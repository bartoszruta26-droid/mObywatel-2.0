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

internal fun documentEditorFieldDefinitions(documentId: String): List<DocumentEditorFieldDefinition> {
    val personal = fields(
        Triple("documentName", "Nazwa / rodzaj dokumentu", "np. zaświadczenie, decyzja, legitymacja"),
        Triple("personName", "Osoba, której dotyczy dokument", "Imię i nazwisko"),
        Triple("referenceNumber", "Numer ewidencyjny / znak sprawy", ""),
        Triple("issuingAuthority", "Organ lub podmiot prowadzący ewidencję", ""),
        Triple("issueDate", "Data wydania / sporządzenia", "DD-MM-RRRR"),
        Triple("validity", "Okres obowiązywania / data ważności", "DD-MM-RRRR lub bezterminowo"),
        Triple("legalBasis", "Podstawa prawna lub regulaminowa", ""),
        Triple("annotations", "Adnotacje urzędowe i uwagi", "")
    )
    return when (documentId) {
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
        "professional", "professional_medical", "professional_education",
        "professional_therapeutic", "professional_other" -> fields(
            Triple("qualificationHolder", "Osoba posiadająca kwalifikację", ""),
            Triple("profession", "Zawód / specjalność", ""),
            Triple("qualificationName", "Nazwa kwalifikacji lub tytułu", ""),
            Triple("qualificationLevel", "Poziom / stopień kwalifikacji", ""),
            Triple("awardingInstitution", "Instytucja nadająca kwalifikację", ""),
            Triple("certificateNumber", "Numer dyplomu / certyfikatu", ""),
            Triple("issueDate", "Data uzyskania", "DD-MM-RRRR"),
            Triple("validUntil", "Termin ważności / recertyfikacji", "DD-MM-RRRR / bezterminowo"),
            Triple("professionalScope", "Zakres uprawnień / kompetencji", ""),
            Triple("legalBasis", "Podstawa prawna / standard kwalifikacji", ""),
            Triple("continuingEducation", "Doskonalenie zawodowe / punkty edukacyjne", "")
        )
        "organization_foundation", "organization_church", "organization_company",
        "organization_association", "entity", "business" -> fields(
            Triple("entityName", "Pełna nazwa podmiotu", ""),
            Triple("legalForm", "Forma prawna / ustrojowa", ""),
            Triple("registrationNumber", "Numer KRS / rejestru / ewidencji", ""),
            Triple("taxNumber", "NIP", ""),
            Triple("statisticalNumber", "REGON / inny identyfikator", ""),
            Triple("registeredOffice", "Siedziba i adres", ""),
            Triple("establishmentDate", "Data utworzenia / wpisu", "DD-MM-RRRR"),
            Triple("representation", "Zasady reprezentacji", ""),
            Triple("management", "Organ zarządzający / osoby uprawnione", ""),
            Triple("supervision", "Organ nadzorczy / kontrolny", ""),
            Triple("legalBasis", "Statut / umowa / akt założycielski", ""),
            Triple("registryStatus", "Status wpisu / podmiotu", "")
        )
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
            else -> fields(
                Triple("recordSubject", "Przedmiot dokumentu", ""),
                Triple("recordHolder", "Osoba / podmiot, którego dotyczy", ""),
                Triple("recordingInstitution", "Instytucja prowadząca / wystawca", ""),
                Triple("recordReference", "Numer ewidencyjny / identyfikator", ""),
                Triple("recordDate", "Data wpisu / wydania", "DD-MM-RRRR"),
                Triple("recordValidity", "Okres obowiązywania", ""),
                Triple("recordScope", "Zakres potwierdzanych danych / uprawnień", ""),
                Triple("recordSource", "Dokument źródłowy / podstawa wpisu", "")
            )
        }
    }
}

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

internal fun documentEditorImportableFieldKeys(): Set<String> =
    documentEditorFieldKeys() + legacyDocumentEditorFieldKeys +
        documentDisplayOptions.distinctBy { it.first }.map { (id, _, _) -> "${id}OcrText" } +
        setOf("identity_cardOcrText", "loyalty_cardsOcrText", "profileOcrText")

internal fun documentEditorFieldKeys(): Set<String> = documentDisplayOptions
    .distinctBy { it.first }
    .flatMap { (id, _, _) -> documentEditorFieldDefinitions(id).map { field -> "doc_${id}_${field.key}" } }
    .toSet()
