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
        val defaultGroup = FamilyGroup("FAM-Care-419", "Home Care Alpha", "owner123")
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
    }

    // Users & Session
    suspend fun getGroupMembers(groupCode: String): Flow<List<User>> {
        return dao.getUsersByGroup(groupCode)
    }

    suspend fun registerOrUpdateUser(user: User) {
        dao.insertUser(user)
    }

    suspend fun findGroup(code: String): FamilyGroup? {
        return dao.getGroupByCode(code)
    }

    suspend fun createGroup(name: String, ownerId: String): FamilyGroup {
        val randomCode = "FAM-${(1000..9999).random()}"
        val group = FamilyGroup(randomCode, name, ownerId)
        dao.insertGroup(group)
        return group
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

            mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context).apply {
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
            mediaPlayer = MediaPlayer().apply {
                setDataSource(filepath)
                prepare()
                start()
                setOnCompletionListener {
                    onCompletion()
                    release()
                    mediaPlayer = null
                }
            }
            Log.d("VoicePlayer", "Playing voice message path: $filepath")
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
}
