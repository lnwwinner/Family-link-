package com.example.data.repository

import android.content.Context
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import com.example.data.dao.FamilyCareDao
import com.example.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Calendar

class FamilyCareRepository(
    private val dao: FamilyCareDao,
    private val context: Context
) {
    // Flows exposed to ViewModel
    val activeLogs: Flow<List<EmergencyLog>> = dao.getActiveEmergencyLogs()
    val allLogs: Flow<List<EmergencyLog>> = dao.getAllEmergencyLogs()
    val voiceMessages: Flow<List<VoiceMessage>> = dao.getAllVoiceMessages()
    val medications: Flow<List<Medication>> = dao.getAllMedications()
    val medicationLogs: Flow<List<MedicationLog>> = dao.getAllMedicationLogs()
    val allWatchHealthData: Flow<List<WatchHealthData>> = dao.getAllWatchHealthData()
    val latestWatchHealthData: Flow<WatchHealthData?> = dao.getLatestWatchHealthData()
    val allAppointments: Flow<List<HospitalAppointment>> = dao.getAllAppointments()
    val allMedicalDocuments: Flow<List<MedicalDocument>> = dao.getAllMedicalDocuments()
    val emergencyProfile: Flow<EmergencyProfile?> = dao.getEmergencyProfile()

    // Real stateful local helpers
    private var mediaRecorder: MediaRecorder? = null
    private var activeRecordFile: File? = null
    private var mediaPlayer: MediaPlayer? = null
    private var recordingStartTime: Long = 0

    // Feed logs since start of today
    fun getMedicationLogsToday(): Flow<List<MedicationLog>> {
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return dao.getMedicationLogsSince(cal.timeInMillis)
    }

    // Provision default group & users for premium startup experience
    suspend fun initializeDefaultData() = withContext(Dispatchers.IO) {
        // Mock default elder
        val defaultGroup = FamilyGroup("FAM-Care-419", "Home Care Alpha", "FAM-Care-419", "owner123")
        dao.insertGroup(defaultGroup)

        val elderUser = User(
            id = "elder_id_101",
            name = "Sompong Somdee (Grandpa)",
            email = "sompong@care.com",
            role = "Elderly",
            groupCode = "FAM-Care-419"
        )
        dao.insertUser(elderUser)

        val daughterUser = User(
            id = "daughter_id_102",
            name = "Suda (Daughter & Caregiver)",
            email = "suda@family.com",
            role = "Family Member",
            groupCode = "FAM-Care-419"
        )
        dao.insertUser(daughterUser)

        // Seed some medications if none exist
        val currentMeds = dao.getAllMedications().first()
        if (currentMeds.isEmpty()) {
            dao.insertMedication(
                Medication(
                    name = "Aspirin (Heart)",
                    dosage = "81 mg - 1 Tablet",
                    timeOfDay = "08:00 AM",
                    description = "Take after breakfast to prevent clots"
                )
            )
            dao.insertMedication(
                Medication(
                    name = "Metformin (Blood Sugar)",
                    dosage = "500 mg - 1 Tablet",
                    timeOfDay = "12:30 PM",
                    description = "Take with lunch"
                )
            )
            dao.insertMedication(
                Medication(
                    name = "Atorvastatin (Cholesterol)",
                    dosage = "20 mg - 1 Tablet",
                    timeOfDay = "08:00 PM",
                    description = "Take before bed. Avoid grapefruit"
                )
            )
        }

        // Seed some logs just for visual look
        val currentLogs = dao.getAllMedicationLogs().first()
        if (currentLogs.isEmpty()) {
            dao.insertMedicationLog(
                MedicationLog(
                    medicationId = 1,
                    medName = "Aspirin (Heart)",
                    intakeTimestamp = System.currentTimeMillis() - 1000 * 60 * 60 * 5, // 5 hours ago
                    isTaken = true,
                    notes = "Taken on time"
                )
            )
        }

        // Seed default watch data for connected watch demo
        val watchData = dao.getLatestWatchHealthData().first()
        if (watchData == null) {
            dao.insertWatchHealthData(
                WatchHealthData(
                    watchType = "Samsung Galaxy Watch",
                    timestamp = System.currentTimeMillis() - 1000 * 60 * 10,
                    heartRate = 74,
                    oxygenLevel = 98,
                    sleepDurationHours = 7.2,
                    sleepQuality = "Deep & Healthy",
                    stressLevel = 35,
                    bloodPressureSystolic = 122,
                    bloodPressureDiastolic = 81,
                    ecgResult = "Normal Sinus Rhythm",
                    batteryLevel = 88,
                    connectionStatus = "Connected",
                    isSynced = true
                )
            )
        }

        // Seed default emergency profile if none exists
        val currentProfile = dao.getEmergencyProfile().first()
        if (currentProfile == null) {
            dao.insertEmergencyProfile(
                EmergencyProfile(
                    id = 1,
                    patientName = "Sompong Somdee",
                    bloodType = "O+",
                    allergies = "Sulfide Antibiotics, Penicillin, Seafood",
                    chronicConditions = "Hypertension, Stage-2 Diabetes, Arrhythmia, Gout",
                    regularPrescriptions = "Aspirin 81mg (Daily), Metformin 500mg (Lunch), Atorvastatin 20mg (Bedtime)",
                    emergencyContactName = "Suda Somdee (Daughter)",
                    emergencyContactPhone = "081-234-5678",
                    hospitalPreference = "Bangkok General Hospital",
                    insuranceDetails = "AIA Health Lifetime Cover - Policy #912-88X-CC-26",
                    additionalNotes = "Always verify oxygen saturation and heart rhythm before administering general anesthesia. Carry emergency asthma inhaler in pocket."
                )
            )
        }

        // Seed default hospital appointments
        val currentAppointments = dao.getAllAppointments().first()
        if (currentAppointments.isEmpty()) {
            val now = System.currentTimeMillis()
            val dayMillis = 24 * 60 * 60 * 1000L

            // 1. Past Appt (2 days ago)
            val pastId = dao.insertAppointment(
                HospitalAppointment(
                    patientName = "Sompong Somdee",
                    hospitalName = "Bangkok General Hospital",
                    doctorName = "Dr. Anon (Cardiology)",
                    department = "Heart Center / ศูนย์โรคหัวใจ",
                    appointmentTimestamp = now - 2 * dayMillis,
                    reminderMinutesBefore = 60,
                    isReminderSet = false,
                    notes = "Finished regular ECG check. Heart rhythm looks stable but doctor advised to continue Atorvastatin daily.",
                    qrCodeData = "HOSP_AP_SOMPONG_2026_06_01",
                    documentPath = "Simulated Blood Report Document",
                    ocrExtractedText = "PATIENT: SOMPONG SOMDEE\nHOSPITAL: BANGKOK GENERAL\nDEPT: CARDIOLOGY\nDATE: JUN 01, 2026\nCHOL: 185 mg/dL (Normal < 200)\nECG: STABLE SINUS RHYTHM_RECOMMENDED_FOLLOW_UP",
                    isFamilyShared = true,
                    isSynced = true,
                    treatmentSuggested = "Continue low-sodium diet and daily walking.",
                    diagnosis = "Ischemic Heart Disease (Follow-up)"
                )
            )

            // 2. Upcoming Appt (in 3 days)
            dao.insertAppointment(
                HospitalAppointment(
                    patientName = "Sompong Somdee",
                    hospitalName = "Bangkok General Hospital",
                    doctorName = "Dr. Somchai (Orthopedics)",
                    department = "Spine & Joint Center / ศูนย์ศัลยกรรมกระดูก",
                    appointmentTimestamp = now + 3 * dayMillis,
                    reminderMinutesBefore = 1440, // 1 day before
                    isReminderSet = true,
                    notes = "Post-operative knee recovery session and joint flexibility screening.",
                    qrCodeData = "HOSP_AP_SOMPONG_2026_06_06",
                    isFamilyShared = true,
                    isSynced = false,
                    treatmentSuggested = "Perform mild knee exercises daily. Avoid climbing steep steps.",
                    diagnosis = "Osteoarthritis Knee (Post-op Monitoring)"
                )
            )

            // 3. Upcoming Appt (in 14 days)
            dao.insertAppointment(
                HospitalAppointment(
                    patientName = "Sompong Somdee",
                    hospitalName = "Vajira Hospital",
                    doctorName = "Dr. Somsri (Endocrinology)",
                    department = "Diabetes Clinic / คลินิกเบาหวาน",
                    appointmentTimestamp = now + 14 * dayMillis,
                    reminderMinutesBefore = 120,
                    isReminderSet = true,
                    notes = "Routine fasting blood sugar level check. Fasting is required for 8 hours before appointment.",
                    qrCodeData = "HOSP_AP_SOMPONG_2026_06_17",
                    isFamilyShared = true,
                    isSynced = true
                )
            )

            // Seed default medical documents linked to past appointments or general
            dao.insertMedicalDocument(
                MedicalDocument(
                    appointmentId = pastId.toInt(),
                    title = "Blood Biomarker Panel Report - 2026",
                    documentType = "Lab Report",
                    filePath = "Simulated Blood Work file.pdf",
                    timestamp = now - 2 * dayMillis,
                    extractedDetails = "Fasting Blood Glucose: 110 mg/dL (Slightly high), HbA1c: 6.4% (Pre-diabetic), LDL: 85 mg/dL (Good), Potassium: 4.1 mEq/L (Normal)",
                    isSynced = true,
                    fileSizeKb = 850
                )
            )
            dao.insertMedicalDocument(
                MedicalDocument(
                    appointmentId = pastId.toInt(),
                    title = "Cardiac ECG Graph Synthesis",
                    documentType = "X-Ray Diagnosis",
                    filePath = "Simulated Electrocardiogram scan.png",
                    timestamp = now - 2 * dayMillis - 100 * 60 * 60 * 1000L, // several days ago
                    extractedDetails = "ECG rhythm strip reveals Normal Sinus Rhythm at 72 bpm. PR interval: 160ms, QRS duration: 90ms. No ST-segment elevation detected.",
                    isSynced = true,
                    fileSizeKb = 1200
                )
            )
            dao.insertMedicalDocument(
                MedicalDocument(
                    appointmentId = null,
                    title = "AIA Health Lifetime Insurance Cover",
                    documentType = "Other",
                    filePath = "Simulated Insurance Policy.pdf",
                    timestamp = now - 50 * dayMillis,
                    extractedDetails = "Policy Number: 912-88X-CC-26. Coverage: Elderly Intensive Inpatient Care, up to 1,500,000 THB per year. Active Status.",
                    isSynced = true,
                    fileSizeKb = 2450
                )
            )
        }
    }

    // Users & Session
    suspend fun registerOrUpdateUser(user: User) {
        dao.insertUser(user)
    }

    suspend fun findGroup(code: String): FamilyGroup? {
        return dao.getGroupByCode(code)
    }

    suspend fun createGroup(name: String, ownerId: String): FamilyGroup {
        val randomCode = "FAM-${(1000..9999).random()}"
        val group = FamilyGroup(java.util.UUID.randomUUID().toString(), name, randomCode, ownerId)
        dao.insertGroup(group)
        return group
    }

    // Family Management
    suspend fun createFamilyGroup(groupId: String, groupName: String, groupCode: String, ownerId: String): FamilyGroup {
        val group = FamilyGroup(groupId, groupName, groupCode, ownerId)
        dao.insertGroup(group)
        return group
    }
    
    suspend fun getFamilyGroup(groupId: String): FamilyGroup? {
        return dao.getGroupById(groupId)
    }
    
    suspend fun addFamilyMember(groupId: String, userId: String, role: String, relationship: String) {
        val member = FamilyMember(groupId = groupId, userId = userId, role = role, relationship = relationship)
        dao.insertMember(member)
    }
    
    suspend fun removeFamilyMember(member: FamilyMember) {
        dao.removeMember(member)
    }
    
    fun getFamilyMembers(groupId: String): Flow<List<FamilyMember>> {
        return dao.getMembersByGroup(groupId)
    }
    
    suspend fun requestToJoinGroup(groupId: String, userId: String) {
        val request = JoinRequest(groupId = groupId, userId = userId, status = "PENDING")
        dao.insertJoinRequest(request)
    }
    
    suspend fun updateJoinRequest(requestId: Int, status: String) {
        dao.updateJoinRequestStatus(requestId, status)
    }
    
    suspend fun addTrustedDevice(deviceId: String, userId: String, deviceName: String, isTrusted: Boolean) {
        val device = TrustedDevice(deviceId, userId, deviceName, isTrusted)
        dao.insertTrustedDevice(device)
    }
    
    fun getTrustedDevicesFlow(userId: String): Flow<List<TrustedDevice>> {
        return dao.getTrustedDevices(userId)
    }

    // Emergency Logging (SOS triggers)
    suspend fun triggerSOS(elderName: String, lat: Double = 13.7563, lng: Double = 100.5018, message: String = "EMERGENCY SOS REQUESTED") = withContext(Dispatchers.IO) {
        // High fidelity simulated GPS coords defaults to central Bangkok if device has mock locations
        val log = EmergencyLog(
            elderName = elderName,
            latitude = lat,
            longitude = lng,
            alertMessage = message
        )
        dao.insertEmergencyLog(log)
        Log.d("SOS", "SOS Inserted - Map location: ${log.googleMapsUrl}")
    }

    suspend fun resolveLog(id: Int, username: String) {
        dao.resolveEmergency(id, username)
    }

    suspend fun clearLogs() {
        dao.clearAllEmergencyLogs()
    }

    // Voice Recorder implementation (PTT feature)
    fun startRecording(elderName: String) {
        try {
            val file = File(context.cacheDir, "ptt_temp_${System.currentTimeMillis()}.3gp")
            activeRecordFile = file
            recordingStartTime = System.currentTimeMillis()

            val recordingContext = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                context.createAttributionContext("microphone")
            } else {
                context
            }

            mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(recordingContext).apply {
                    setAudioSource(MediaRecorder.AudioSource.MIC)
                    setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                    setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                    setOutputFile(file.absolutePath)
                    prepare()
                    start()
                }
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder().apply {
                    setAudioSource(MediaRecorder.AudioSource.MIC)
                    setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                    setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                    setOutputFile(file.absolutePath)
                    prepare()
                    start()
                }
            }
            Log.d("VoiceRecorder", "Started recording into: ${file.absolutePath}")
        } catch (e: Exception) {
            Log.e("VoiceRecorder", "Failed to initialize recorder: ${e.message}", e)
            activeRecordFile = null
        }
    }

    suspend fun stopAndSaveRecording(elderName: String): VoiceMessage? = withContext(Dispatchers.IO) {
        val recorder = mediaRecorder ?: return@withContext null
        val recordFile = activeRecordFile ?: return@withContext null

        try {
            recorder.stop()
            recorder.release()
            mediaRecorder = null

            val durationMs = System.currentTimeMillis() - recordingStartTime
            val durationSec = (durationMs / 1000).toInt().coerceAtLeast(1)

            // Dynamic voice features mock analytic assessment ready for Future AI Analysis
            // Analyzing simulated frequency peaks, silence counts or speed to classify distress
            val randomVal = (1..100).random()
            val simulatedDistress = when {
                randomVal > 85 -> "Severe distress"
                randomVal > 60 -> "Slight distress"
                else -> "Normal"
            }
            val simulatedPulse = if (simulatedDistress != "Normal") (90..130).random() else (65..85).random()
            val simulatedTranscription = when (simulatedDistress) {
                "Severe distress" -> "I had a sudden pain, please look at the dashboard."
                "Slight distress" -> "I am feeling a bit weak, please call."
                else -> "I'm okay, just checking in with you."
            }

            val msg = VoiceMessage(
                elderName = elderName,
                filepath = recordFile.absolutePath,
                durationSec = durationSec,
                distressLevel = simulatedDistress,
                detectedPulseBpm = simulatedPulse,
                comments = "Speech Analysis Output: '$simulatedTranscription'. Ready for full AI model execution."
            )
            dao.insertVoiceMessage(msg)
            Log.d("VoiceRecorder", "Saved voice recording record: ${msg.id}")
            activeRecordFile = null
            return@withContext msg
        } catch (e: Exception) {
            Log.e("VoiceRecorder", "Failed to safely stop recording: ${e.message}", e)
            mediaRecorder = null
            activeRecordFile = null
            return@withContext null
        }
    }

    fun playVoiceMessage(filepath: String, onCompletion: () -> Unit = {}) {
        try {
            mediaPlayer?.release()
            
            val playingContext = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                context.createAttributionContext("microphone")
            } else {
                context
            }

            mediaPlayer = MediaPlayer().apply {
                setDataSource(playingContext, android.net.Uri.fromFile(java.io.File(filepath)))
                prepare()
                start()
                setOnCompletionListener {
                    onCompletion()
                    release()
                    mediaPlayer = null
                }
            }
            Log.d("VoicePlayer", "Playing voice message path: $filepath with attributed context")
        } catch (e: Exception) {
            Log.e("VoicePlayer", "Failed to play voice message: ${e.message}")
        }
    }

    // Medication CRUD
    suspend fun addMedication(name: String, dosage: String, timeOfDay: String, description: String) {
        val med = Medication(
            name = name,
            dosage = dosage,
            timeOfDay = timeOfDay,
            description = description
        )
        dao.insertMedication(med)
    }

    suspend fun deleteMedicationItem(med: Medication) {
        dao.deleteMedication(med)
    }

    suspend fun logMedicationIntake(medId: Int, medName: String, isTaken: Boolean, notes: String = "") {
        val log = MedicationLog(
            medicationId = medId,
            medName = medName,
            isTaken = isTaken,
            notes = notes
        )
        dao.insertMedicationLog(log)
    }

    // New Watch Data operations
    suspend fun saveWatchHealthData(data: WatchHealthData) = withContext(Dispatchers.IO) {
        dao.insertWatchHealthData(data)
    }

    suspend fun syncWatchData() = withContext(Dispatchers.IO) {
        kotlinx.coroutines.delay(1000) // simulated internet sync delays
        dao.markAllWatchHealthDataSynced()
    }

    suspend fun clearWatchData() = withContext(Dispatchers.IO) {
        dao.clearWatchHealthData()
    }

    // New Hospital Appointments & Documents Helper Actions
    suspend fun addAppointment(appt: HospitalAppointment): Long = withContext(Dispatchers.IO) {
        dao.insertAppointment(appt)
    }

    suspend fun deleteAppointmentItem(appt: HospitalAppointment) = withContext(Dispatchers.IO) {
        dao.deleteAppointment(appt)
    }

    suspend fun saveMedicalDocument(doc: MedicalDocument): Long = withContext(Dispatchers.IO) {
        dao.insertMedicalDocument(doc)
    }

    suspend fun deleteMedicalDocumentItem(doc: MedicalDocument) = withContext(Dispatchers.IO) {
        dao.deleteMedicalDocument(doc)
    }

    suspend fun updateEmergencyProfile(profile: EmergencyProfile) = withContext(Dispatchers.IO) {
        dao.insertEmergencyProfile(profile)
    }

    suspend fun syncGoogleCalendarEvent(appt: HospitalAppointment): Boolean = withContext(Dispatchers.IO) {
        kotlinx.coroutines.delay(1200) // simulated REST API Google Calendar synchronization
        dao.markAppointmentSynced(appt.id)
        return@withContext true
    }
}
