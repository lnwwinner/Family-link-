package com.example.data.dao

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface FamilyCareDao {

    // Users
    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    suspend fun getUserById(userId: String): User?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)

    @Query("SELECT * FROM users WHERE groupCode = :groupCode")
    fun getUsersByGroup(groupCode: String): Flow<List<User>>

    // Emergency Logs
    @Query("SELECT * FROM emergency_logs ORDER BY timestamp DESC")
    fun getAllEmergencyLogs(): Flow<List<EmergencyLog>>

    @Query("SELECT * FROM emergency_logs WHERE isResolved = 0 ORDER BY timestamp DESC")
    fun getActiveEmergencyLogs(): Flow<List<EmergencyLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEmergencyLog(log: EmergencyLog)

    @Query("UPDATE emergency_logs SET isResolved = 1, resolvedBy = :username WHERE id = :logId")
    suspend fun resolveEmergency(logId: Int, username: String)

    @Query("DELETE FROM emergency_logs")
    suspend fun clearAllEmergencyLogs()

    // Voice Messages
    @Query("SELECT * FROM voice_messages ORDER BY timestamp DESC")
    fun getAllVoiceMessages(): Flow<List<VoiceMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVoiceMessage(msg: VoiceMessage)

    // Medications
    @Query("SELECT * FROM medications ORDER BY id DESC")
    fun getAllMedications(): Flow<List<Medication>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedication(med: Medication)

    @Delete
    suspend fun deleteMedication(med: Medication)

    // Medication Logs
    @Query("SELECT * FROM medication_logs ORDER BY intakeTimestamp DESC")
    fun getAllMedicationLogs(): Flow<List<MedicationLog>>

    // Get logs entered today
    @Query("SELECT * FROM medication_logs WHERE intakeTimestamp >= :startOfDay ORDER BY intakeTimestamp DESC")
    fun getMedicationLogsSince(startOfDay: Long): Flow<List<MedicationLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedicationLog(log: MedicationLog)

    // Family Groups
    @Query("SELECT * FROM family_groups WHERE groupId = :groupId LIMIT 1")
    suspend fun getGroupById(groupId: String): FamilyGroup?
    
    @Query("SELECT * FROM family_groups WHERE groupCode = :groupCode LIMIT 1")
    suspend fun getGroupByCode(groupCode: String): FamilyGroup?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroup(group: FamilyGroup)

    // Family Members
    @Query("SELECT * FROM family_members WHERE groupId = :groupId")
    fun getMembersByGroup(groupId: String): Flow<List<FamilyMember>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMember(member: FamilyMember)

    @Delete
    suspend fun removeMember(member: FamilyMember)

    // Join Requests
    @Query("SELECT * FROM join_requests WHERE groupId = :groupId AND status = 'PENDING'")
    fun getPendingJoinRequests(groupId: String): Flow<List<JoinRequest>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJoinRequest(request: JoinRequest)

    @Query("UPDATE join_requests SET status = :status WHERE requestId = :requestId")
    suspend fun updateJoinRequestStatus(requestId: Int, status: String)

    // Trusted Devices
    @Query("SELECT * FROM trusted_devices WHERE userId = :userId")
    fun getTrustedDevices(userId: String): Flow<List<TrustedDevice>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrustedDevice(device: TrustedDevice)

    // Watch Health Data
    @Query("SELECT * FROM watch_health_data ORDER BY timestamp DESC")
    fun getAllWatchHealthData(): Flow<List<WatchHealthData>>

    @Query("SELECT * FROM watch_health_data ORDER BY timestamp DESC LIMIT 1")
    fun getLatestWatchHealthData(): Flow<WatchHealthData?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWatchHealthData(data: WatchHealthData)

    @Query("UPDATE watch_health_data SET isSynced = 1")
    suspend fun markAllWatchHealthDataSynced()

    @Query("DELETE FROM watch_health_data")
    suspend fun clearWatchHealthData()

    // Hospital Appointments
    @Query("SELECT * FROM hospital_appointments ORDER BY appointmentTimestamp ASC")
    fun getAllAppointments(): Flow<List<HospitalAppointment>>

    @Query("SELECT * FROM hospital_appointments WHERE id = :id LIMIT 1")
    suspend fun getAppointmentById(id: Int): HospitalAppointment?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAppointment(appointment: HospitalAppointment): Long

    @Delete
    suspend fun deleteAppointment(appointment: HospitalAppointment)

    @Query("UPDATE hospital_appointments SET isSynced = 1 WHERE id = :id")
    suspend fun markAppointmentSynced(id: Int)

    // Medical Documents
    @Query("SELECT * FROM medical_documents ORDER BY timestamp DESC")
    fun getAllMedicalDocuments(): Flow<List<MedicalDocument>>

    @Query("SELECT * FROM medical_documents WHERE appointmentId = :appointmentId ORDER BY timestamp DESC")
    fun getDocumentsForAppointment(appointmentId: Int): Flow<List<MedicalDocument>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedicalDocument(doc: MedicalDocument): Long

    @Delete
    suspend fun deleteMedicalDocument(doc: MedicalDocument)

    // Emergency Profiles
    @Query("SELECT * FROM emergency_profiles WHERE id = 1 LIMIT 1")
    fun getEmergencyProfile(): Flow<EmergencyProfile?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEmergencyProfile(profile: EmergencyProfile)
}
