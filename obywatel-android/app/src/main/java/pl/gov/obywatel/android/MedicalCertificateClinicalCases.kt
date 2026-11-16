// SPDX-FileCopyrightText: 2026 bartoszruta26-droid
// SPDX-License-Identifier: CC0-1.0

package pl.gov.obywatel.android

internal data class MedicalCertificateClinicalCase(
    val id: String,
    val title: String,
    val fields: Map<String, String>,
    val imagingStudy: MedicalImagingStudy
)

/**
 * Structured clinical scenarios used to populate the editor for workflow review.
 * They contain no real patient identifiers and must be verified against source records
 * before any clinical document is issued.
 */
internal object MedicalCertificateClinicalCases {
    val all: List<MedicalCertificateClinicalCase> = listOf(
        MedicalCertificateClinicalCase(
            id = "sciatica_oncology",
            title = "Rwa kulszowa — diagnostyka neurologiczna i onkologiczna",
            fields = mapOf(
                "purpose" to "Clinical summary for continuity of care and coordinated specialist assessment.",
                "chiefComplaint" to "Severe low-back pain radiating through the right buttock and posterolateral thigh to the calf and dorsum of the foot. Pain increases with prolonged sitting, forward flexion, coughing and walking. The patient reports paraesthesia over the right dorsum of the foot and reduced walking and sleep tolerance.",
                "medicalHistory" to "Previous episodes of mechanical low-back pain. Current progressive right-sided lumbosacral radicular symptoms. Multilevel degenerative disc disease has been described in prior imaging. Further neurological assessment is indicated. Oncological investigations are being coordinated because of clinical risk factors; no malignancy is established in the available record.",
                "occupationalHistory" to "Predominantly sedentary work with prolonged sitting. Current symptoms limit sustained sitting, driving and usual work duties.",
                "allergies" to "Allergy status requires confirmation from the patient and available medical records.",
                "currentMedication" to "Medication reconciliation required: confirm all prescribed and over-the-counter medicines, doses, frequency, duration, adherence and adverse effects directly with the patient and prescribing clinicians.",
                "diagnosis" to "Right-sided lumbosacral radicular pain (sciatica), ICD-10-CM M54.16 / ICD-10 M54.4 depending on coding system. Degenerative lumbar disc disease; correlate level and nerve-root involvement with the original MRI report. Oncological diagnostic pathway ongoing; malignancy is not confirmed.",
                "symptomsOnset" to "2026-11-10",
                "examinationDate" to "2026-11-15",
                "physicalExamination" to "Lumbar flexion and extension are limited by pain. Right lumbosacral paraspinal and gluteal tenderness is present. Right straight-leg raise reproduces radicular pain at approximately 40 degrees. Right ankle dorsiflexion and great-toe extension are mildly reduced on manual testing; remaining tested lower-limb myotomes are preserved. Light-touch sensation is reduced over the right dorsum of the foot. Patellar reflexes are symmetric; right Achilles reflex is reduced. Gait is antalgic with reduced right stance time. Record exact measured strength and neurological findings at the clinical examination.",
                "systemsReview" to "No additional peripheral joint swelling reported. Ask and document saddle sensation, bladder and bowel function, progressive weakness, fever, unexplained weight loss, previous malignancy and persistent night/rest pain. Red-flag status must be established directly at each assessment.",
                "functionalLimitations" to "Reduced tolerance for prolonged sitting, standing and walking. Dressing the lower body, putting on shoes, sit-to-stand transfers and sleep are limited by pain. Walking distance and activity tolerance require measurement and documentation.",
                "workCapacity" to "Temporary modification of duties may be required, including alternating positions and limiting repetitive loaded trunk flexion and heavy lifting. Fitness for work and duration of restrictions require an individual clinical assessment.",
                "hospitalizations" to "Review previous admissions, spinal procedures and emergency assessments against the available medical record.",
                "treatmentAndRehabilitation" to "Correlate neurological examination with original MRI images and report. Continue coordinated neurological and indicated oncological assessment. If no contraindication is identified, use education, symptom-guided mobility, graded walking and progressive trunk and hip conditioning. Review analgesic strategy and medication safety with the treating clinician. Escalate urgently for progressive motor deficit, saddle sensory change or new bladder/bowel dysfunction.",
                "prognosis" to "Dependent on neurological course, imaging-clinical correlation, response to conservative management and completion of the indicated diagnostic pathway.",
                "recommendations" to "Arrange timely neurological review and review of original MRI images. Complete the indicated diagnostic work-up. Document neurological status and red flags serially. Urgent assessment is required for progressive weakness, saddle anaesthesia, urinary retention or incontinence, bowel dysfunction or rapidly worsening symptoms."
            ),
            imagingStudy = MedicalImagingStudy(
                id = "case-sciatica-mri",
                modality = MedicalImagingModality.MRI,
                bodyRegion = "Lumbar spine",
                studyDate = "2026-11-12",
                facility = "Imaging department",
                indication = "Right-sided lumbar radicular pain with reduced walking tolerance.",
                technique = "Sagittal and axial T1- and T2-weighted sequences; verify protocol against the original report.",
                contrast = "Contrast status to be confirmed from the original report.",
                findings = "Multilevel disc desiccation and degenerative endplate change. At L4-L5, broad-based posterior disc bulge with right subarticular protrusion and narrowing of the right lateral recess, contacting the traversing right L5 nerve root. At L5-S1, disc height loss and right foraminal component with right foraminal narrowing. No high-grade central canal stenosis described.",
                impression = "Multilevel lumbar spondylosis and disc disease, most pronounced at L4-L5 and L5-S1, with right-sided nerve-root contact. Correlate with neurological examination.",
                radiologistName = "",
                reportNumber = "MRI-LS-2026-001"
            )
        ),
        MedicalCertificateClinicalCase(
            id = "severe_depression",
            title = "Ciężki epizod depresyjny — ocena psychiatryczna",
            fields = mapOf(
                "purpose" to "Clinical summary for psychiatric assessment, treatment planning and continuity of care.",
                "chiefComplaint" to "Persistent depressed mood, marked loss of interest and pleasure, reduced energy, early-morning awakening, impaired concentration, psychomotor slowing and substantial reduction in daily functioning.",
                "medicalHistory" to "Symptoms have progressively intensified over several weeks. The patient reports recurrent depressive symptoms and a marked deterioration in occupational and social functioning. Previous psychiatric diagnoses, treatment episodes, psychotherapy, medication trials, family history and substance use require confirmation from the clinical record and direct interview.",
                "occupationalHistory" to "Work attendance and task completion have deteriorated because of fatigue, reduced concentration, sleep disturbance and impaired motivation.",
                "allergies" to "Confirm medication and other allergies before prescribing.",
                "currentMedication" to "Reconcile current and previous psychotropic medicines, doses, duration, adherence, response, adverse effects and non-prescription substances with the patient and prescribing clinician.",
                "diagnosis" to "Severe depressive episode, provisional clinical formulation pending structured psychiatric assessment and exclusion of bipolar disorder, substance-related causes and relevant medical conditions. ICD-10 F32.2 when criteria for a severe depressive episode without psychotic symptoms are confirmed; use F32.3 if psychotic symptoms are established.",
                "symptomsOnset" to "2026-10-01",
                "examinationDate" to "2026-11-15",
                "physicalExamination" to "General appearance tired, with reduced spontaneous activity. Speech is coherent and slowed. Affect is constricted and congruent with reported mood. Attention and concentration are reduced. No focal neurological deficit documented in the current assessment. Assess hydration, nutrition, weight change, sleep and relevant physical causes; record objective findings.",
                "systemsReview" to "Assess depressive symptoms, anxiety, panic, trauma symptoms, manic or hypomanic episodes, psychotic symptoms, sleep, appetite, weight change, pain, endocrine symptoms and substance use. Conduct and document a direct suicide and self-harm risk assessment, including ideation, intent, plan, access to means, previous attempts and protective factors. Risk status must be established in person and reassessed.",
                "functionalLimitations" to "Marked difficulty initiating and completing routine activities, maintaining personal and household responsibilities, sustaining attention, making decisions and participating in social and occupational roles.",
                "workCapacity" to "Current symptoms may substantially impair reliable performance and safe completion of usual duties. Fitness for work, accommodations and duration require an individual assessment by the responsible clinician.",
                "hospitalizations" to "Confirm previous psychiatric admissions, crisis interventions, emergency presentations and treatment history.",
                "treatmentAndRehabilitation" to "Arrange prompt psychiatric assessment and an individualized treatment plan. Establish a documented safety plan and crisis contacts according to the assessed risk. Consider evidence-based psychotherapy and pharmacotherapy after diagnostic review, shared decision-making and assessment of contraindications. Review sleep, nutrition, activity, substance use and social supports. Escalate immediately to emergency psychiatric care if imminent risk to self or others is identified.",
                "prognosis" to "Dependent on diagnostic clarification, risk assessment, treatment engagement, symptom severity, comorbidity and response to the individualized treatment plan.",
                "recommendations" to "Complete structured psychiatric assessment, including bipolar-spectrum screening and direct suicide-risk evaluation. Establish follow-up and safety arrangements. Assess medical contributors and medication interactions. Arrange urgent specialist or emergency intervention when risk assessment indicates."
            ),
            imagingStudy = MedicalImagingStudy(
                id = "case-depression-other",
                modality = MedicalImagingModality.OTHER,
                bodyRegion = "Not applicable",
                studyDate = "",
                facility = "",
                indication = "No imaging examination indicated by the clinical scenario.",
                technique = "",
                contrast = "",
                findings = "",
                impression = "No imaging report attached.",
                radiologistName = "",
                reportNumber = ""
            )
        ),
        MedicalCertificateClinicalCase(
            id = "tibia_fracture",
            title = "Złamanie kości piszczelowej — leczenie urazowe i rehabilitacja",
            fields = mapOf(
                "purpose" to "Clinical summary for orthopaedic follow-up, rehabilitation planning and assessment of functional capacity.",
                "chiefComplaint" to "Pain, swelling and reduced weight-bearing capacity of the lower leg following a traumatic injury. Mobility is restricted and the patient requires an assistive device for transfers and ambulation.",
                "medicalHistory" to "Closed fracture of the tibial shaft following a fall. Initial trauma assessment and orthopaedic management completed. Fracture pattern, associated fibular injury, operative versus conservative treatment, immobilization period and weight-bearing instructions must be confirmed from the original discharge summary and imaging reports.",
                "occupationalHistory" to "Usual duties involve prolonged standing and walking. Current mobility restriction prevents safe completion of these tasks.",
                "allergies" to "Confirm medication and material allergies, including prior reactions to analgesics and antibiotics.",
                "currentMedication" to "Reconcile current analgesia, thromboprophylaxis if prescribed, bone-health medicines and other regular medication against the discharge documentation.",
                "diagnosis" to "Closed fracture of shaft of tibia, side and fracture morphology to be confirmed from the original radiology and orthopaedic records. ICD-10 S82.2-; specify laterality and displacement according to the applicable coding system.",
                "symptomsOnset" to "2026-11-02",
                "examinationDate" to "2026-11-15",
                "physicalExamination" to "Lower-leg swelling and localized tenderness over the tibial shaft. Inspect skin condition, surgical wounds or cast integrity as applicable. Assess distal pulses, capillary refill, skin temperature, sensation and motor function of the foot. Document pain, compartment tension, range of motion of adjacent joints and exact weight-bearing status. Compare with the unaffected limb.",
                "systemsReview" to "Assess pain progression, fever, wound discharge, calf swelling, chest pain, dyspnoea, new paraesthesia and changes in foot colour or temperature. Evaluate for infection, venous thromboembolism and neurovascular compromise when clinically indicated.",
                "functionalLimitations" to "Restricted transfers, stair negotiation, walking and standing. The patient currently requires an assistive device and follows the weight-bearing restrictions prescribed by the orthopaedic team.",
                "workCapacity" to "Unable to perform duties requiring prolonged standing, walking, carrying loads or rapid movement until reassessment confirms safe functional recovery. Restrictions and duration must follow the treating orthopaedic plan.",
                "hospitalizations" to "Trauma assessment and orthopaedic treatment following the injury; confirm admission dates, procedure details and discharge instructions from source records.",
                "treatmentAndRehabilitation" to "Follow the orthopaedic team's immobilization and weight-bearing instructions. Arrange clinical and radiographic follow-up to assess alignment and fracture healing. Rehabilitation should progress according to fracture stability and the treating team's clearance, including safe transfers, adjacent-joint mobility, strength and gait retraining. Provide urgent reassessment for escalating pain, tense swelling, new sensory or motor deficit, cool/pale foot, fever, wound discharge or symptoms suggestive of thromboembolism.",
                "prognosis" to "Dependent on fracture pattern, stability, treatment method, soft-tissue condition, radiographic union, complications and adherence to the prescribed loading protocol.",
                "recommendations" to "Obtain the original radiology report and orthopaedic discharge summary. Confirm laterality, fracture morphology, treatment and exact loading restrictions. Continue scheduled orthopaedic review and rehabilitation only within the authorized weight-bearing plan."
            ),
            imagingStudy = MedicalImagingStudy(
                id = "case-tibia-xray",
                modality = MedicalImagingModality.X_RAY,
                bodyRegion = "Lower leg — tibia and fibula; laterality to be confirmed",
                studyDate = "2026-11-02",
                facility = "Emergency imaging department",
                indication = "Traumatic lower-leg pain, swelling and inability to bear weight.",
                technique = "Anteroposterior and lateral radiographs; confirm views and laterality from source images.",
                contrast = "Not applicable.",
                findings = "Fracture line through the tibial diaphysis with surrounding soft-tissue swelling. Alignment and displacement must be measured from the original images. Assess the fibula, ankle and knee for associated injury.",
                impression = "Tibial shaft fracture. Fracture configuration, displacement and laterality require confirmation from the original radiology report.",
                radiologistName = "",
                reportNumber = "XR-TIBIA-2026-001"
            )
        ),
        MedicalCertificateClinicalCase(
            id = "acute_pancreatitis",
            title = "Ostre zapalenie trzustki — diagnostyka i obserwacja",
            fields = mapOf(
                "purpose" to "Clinical summary for gastroenterology follow-up and continuity of care after an acute abdominal presentation.",
                "chiefComplaint" to "Acute persistent epigastric pain radiating to the back, accompanied by nausea and repeated vomiting. Oral intake has been reduced because of pain and nausea.",
                "medicalHistory" to "Acute pancreatitis diagnosed during hospital assessment. Aetiology requires confirmation from the documented evaluation, including gallstone disease, alcohol exposure, triglyceride level, calcium, medication history and other relevant causes. Previous episodes, biliary symptoms and prior abdominal procedures should be reviewed.",
                "occupationalHistory" to "Usual activity is currently limited by abdominal pain, fatigue and reduced oral intake. Return to work depends on clinical recovery and follow-up assessment.",
                "allergies" to "Confirm drug and food allergies before treatment decisions.",
                "currentMedication" to "Reconcile medicines used before admission and during hospital treatment, including analgesics, antiemetics, fluids and any aetiology-specific treatment. Verify discharge prescriptions and duration from the discharge summary.",
                "diagnosis" to "Acute pancreatitis. Confirm diagnostic criteria and severity classification using symptoms, serum lipase/amylase, organ function and imaging where indicated. ICD-10 K85.-; specify aetiology and subtype only when supported by the record.",
                "symptomsOnset" to "2026-11-08",
                "examinationDate" to "2026-11-15",
                "physicalExamination" to "Assess general condition, hydration, temperature, heart rate, blood pressure, respiratory status and abdominal findings. Document epigastric tenderness, guarding or peritoneal signs, bowel sounds and signs of jaundice. Review urine output and relevant laboratory trends. Record objective findings from the actual examination.",
                "systemsReview" to "Review fever, jaundice, persistent vomiting, dyspnoea, reduced urine output, confusion, gastrointestinal bleeding and worsening abdominal pain. Evaluate for biliary obstruction, systemic inflammatory response, organ dysfunction and complications according to clinical status.",
                "functionalLimitations" to "Reduced oral intake, sleep disturbance, fatigue and limited tolerance for physical activity. Daily activity and mobility should be reassessed as pain, hydration and nutrition improve.",
                "workCapacity" to "Temporary work restriction may be required during active symptoms and early recovery. Fitness for work should be based on symptom resolution, nutritional recovery, laboratory and clinical course, and the treating clinician's assessment.",
                "hospitalizations" to "Hospital assessment and treatment for acute pancreatitis; confirm admission dates, severity, organ support, procedures and discharge plan from the hospital record.",
                "treatmentAndRehabilitation" to "Follow the documented gastroenterology and hospital discharge plan. Monitor hydration, pain, oral or enteral nutrition tolerance, organ function and relevant laboratory results. Investigate and address the underlying cause, including gallstone disease, hypertriglyceridaemia, alcohol exposure or medication-related causes where supported. Arrange follow-up and nutrition advice. Urgent reassessment is required for worsening pain, persistent vomiting, fever, jaundice, dyspnoea, confusion or reduced urine output.",
                "prognosis" to "Dependent on aetiology, severity, organ dysfunction, complications, response to treatment and recurrence prevention.",
                "recommendations" to "Review hospital discharge documentation, laboratory trends and imaging. Confirm aetiology and severity classification. Arrange gastroenterology follow-up and address modifiable causes. Escalate urgently for signs of organ dysfunction, infection, biliary obstruction or clinical deterioration."
            ),
            imagingStudy = MedicalImagingStudy(
                id = "case-pancreatitis-us",
                modality = MedicalImagingModality.ULTRASOUND,
                bodyRegion = "Abdomen — hepatobiliary system and pancreas",
                studyDate = "2026-11-09",
                facility = "Abdominal imaging department",
                indication = "Acute epigastric pain; assessment for biliary aetiology.",
                technique = "Transabdominal ultrasound; visualization of the pancreas may be limited by bowel gas.",
                contrast = "Not applicable.",
                findings = "Assess gallbladder for calculi and wall thickening, common bile duct calibre and intrahepatic bile ducts. Pancreatic visualization is limited; document only structures adequately visualized and correlate with laboratory results.",
                impression = "Ultrasound assessment for biliary aetiology. A normal or limited ultrasound does not exclude acute pancreatitis; correlate with clinical findings, enzymes and other imaging when indicated.",
                radiologistName = "",
                reportNumber = "US-ABD-2026-001"
            )
        )
    )

    fun find(id: String): MedicalCertificateClinicalCase? = all.firstOrNull { it.id == id }
}
