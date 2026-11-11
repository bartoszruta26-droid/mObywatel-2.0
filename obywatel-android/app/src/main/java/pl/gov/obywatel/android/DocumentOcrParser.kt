// SPDX-FileCopyrightText: 2026 bartoszruta26-droid
// SPDX-License-Identifier: CC0-1.0
package pl.gov.obywatel.android

/** Conservative field suggestions from ML Kit OCR. Never treat suggestions as verified data. */
internal object DocumentOcrParser {
    private data class Rule(val key:String,val labels:List<String>)
    private fun norm(s:String)=java.text.Normalizer.normalize(s.lowercase().replace('ł','l'),java.text.Normalizer.Form.NFD)
        .replace(Regex("\\p{Mn}+"),"").replace(Regex("[^a-z0-9 ]")," ").replace(Regex("\\s+")," ").trim()
    fun extract(categoryId:String,text:String):Map<String,String> {
        if(text.isBlank()) return emptyMap()
        val lines=text.lines().map{it.trim().replace(Regex("\\s+")," ")}.filter{it.isNotBlank()}
        val common=listOf(
            Rule("firstNames",listOf("imiona","imie","given names")),Rule("lastName",listOf("nazwisko","surname")),
            Rule("birthDate",listOf("data urodzenia","date of birth")),Rule("birthPlace",listOf("miejsce urodzenia","place of birth")),
            Rule("citizenship",listOf("obywatelstwo","nationality")),Rule("pesel",listOf("pesel")),
            Rule("idNumber",listOf("seria i numer","numer dowodu","numer dokumentu","document no")),
            Rule("idIssueDate",listOf("data wydania","date of issue")),Rule("idExpiryDate",listOf("data waznosci","termin waznosci","expiry date","valid until")),
            Rule("idIssuer",listOf("organ wydajacy","wydany przez","issuing authority"))
        )
        val rules=when(categoryId) {
            "identity_card"->common
            "passport"->listOf(
                Rule("passportHolderNames",listOf("imiona i nazwisko","surname and given names","given names")),
                Rule("passportNumber",listOf("numer paszportu","passport no","passport number","document no")),
                Rule("passportCountry",listOf("panstwo wydania","country code")),Rule("passportIssueDate",listOf("data wydania","date of issue")),
                Rule("passportExpiryDate",listOf("data waznosci","date of expiry","expiry date")),Rule("passportIssuer",listOf("organ wydajacy","authority")),
                Rule("passportBirthPlace",listOf("miejsce urodzenia","place of birth")),Rule("passportCitizenship",listOf("obywatelstwo","nationality")),
                Rule("passportSex",listOf("plec","sex"))
            )
            "parliament"->listOf(Rule("firstNames",listOf("imiona")),Rule("lastName",listOf("nazwisko")),
                Rule("mpCardNumber",listOf("numer legitymacji","numer karty")),Rule("mpCardIssueDate",listOf("data wydania")),
                Rule("mpIssuer",listOf("wystawca","organ wydajacy")),Rule("mpTerm",listOf("kadencja")))
            "training"->listOf(Rule("firstNames",listOf("imiona")),Rule("lastName",listOf("nazwisko")),
                Rule("trainingInstitution",listOf("nazwa instytucji","instytucja")),Rule("trainingCardNumber",listOf("numer legitymacji","numer karty")),
                Rule("trainingCourseName",listOf("nazwa kursu","kierunek")),Rule("trainingIssueDate",listOf("data wydania")),
                Rule("trainingValidUntil",listOf("wazna do","wazny do","valid until")))
            "student"->listOf(Rule("studentNames",listOf("imie i nazwisko","imiona i nazwisko")),Rule("studentUniversity",listOf("nazwa uczelni","uczelnia")),
                Rule("studentCardNumber",listOf("numer legitymacji","numer albumu")),Rule("studentFaculty",listOf("wydzial")),
                Rule("studentProgramme",listOf("kierunek")),Rule("studentIssueDate",listOf("data wydania")),Rule("studentValidity",listOf("wazna do","valid until")))
            "disability"->listOf(Rule("disabilityDecisionNumber",listOf("numer orzeczenia","nr orzeczenia")),
                Rule("disabilityDecisionDate",listOf("data wydania")),Rule("disabilityPersonName",listOf("imie i nazwisko","osoba")),
                Rule("disabilityBirthDate",listOf("data urodzenia")),Rule("disabilityPeselOrId",listOf("pesel","identyfikator")),
                Rule("disabilityDegree",listOf("stopien niepelnosprawnosci","stopien")),Rule("disabilityCauseCodes",listOf("symbol przyczyny")),
                Rule("disabilityValidity",listOf("okres","wazne do","data waznosci")),Rule("disabilityAuthorityName",listOf("nazwa organu","zespol do spraw")))
            else->common
        }
        val out=linkedMapOf<String,String>()
        fun put(k:String,v:String){val x=v.trim().trim(':','-',' ','|');if(x.length in 2..160&&!x.equals("POLSKA",true))out.putIfAbsent(k,x)}
        rules.forEach{r->r.labels.forEach{label->
            val n=norm(label);val i=lines.indexOfFirst{norm(it)==n||norm(it).startsWith("$n ")}
            if(i>=0){val re=Regex("(?i)^\\s*\${Regex.escape(label)}\\s*[:：-]?\\s*(.*)$")
                put(r.key,re.find(lines[i])?.groupValues?.getOrNull(1).orEmpty().ifBlank{lines.getOrNull(i+1).orEmpty()})}
        }}
        val all=lines.joinToString(" ")
        Regex("\\b\\d{11}\\b").find(all)?.value?.let{put(if(categoryId=="disability")"disabilityPeselOrId" else "pesel",it)}
        val dates=Regex("\\b(?:[0-3]?\\d[.\\-/][01]?\\d[.\\-/](?:19|20)?\\d{2}|(?:19|20)\\d{2}-[01]\\d-[0-3]\\d)\\b").findAll(all).map{it.value}.toList()
        if(dates.isNotEmpty()){
            val issue=when(categoryId){"passport"->"passportIssueDate";"training"->"trainingIssueDate";"student"->"studentIssueDate";"parliament"->"mpCardIssueDate";"disability"->"disabilityDecisionDate";else->"idIssueDate"}
            val expiry=when(categoryId){"passport"->"passportExpiryDate";"training"->"trainingValidUntil";"student"->"studentValidity";"disability"->"disabilityValidity";else->"idExpiryDate"}
            put(issue,dates.first());if(dates.size>1)put(expiry,dates.last())
        }
        val mrz=lines.map{it.replace(" ","").uppercase()}
        val name=mrz.firstOrNull{it.startsWith("P<")&&it.length>=20}
        val data=mrz.firstOrNull{it.length>=28&&it.take(9).all{c->c.isLetterOrDigit()||c=='<'}&&it.substring(13,19).all(Char::isDigit)}
        if(categoryId=="passport"&&name!=null){val p=name.drop(5).split("<<",limit=2);if(p.size==2)put("passportHolderNames",(p[1]+" "+p[0]).replace('<',' ').trim())}
        if(categoryId=="passport"&&data!=null){
            put("passportNumber",data.substring(0,9).trimEnd('<'));put("passportCitizenship",data.substring(10,13));put("passportSex",data.substring(20,21))
            fun date(s:String):String{val y=s.substring(0,2).toInt();return "%02d-%02d-%04d".format(s.substring(4,6).toInt(),s.substring(2,4).toInt(),if(y<=49)2000+y else 1900+y)}
            runCatching{put("birthDate",date(data.substring(13,19)))};runCatching{put("passportExpiryDate",date(data.substring(21,27)))}
        }
        return out
    }
}