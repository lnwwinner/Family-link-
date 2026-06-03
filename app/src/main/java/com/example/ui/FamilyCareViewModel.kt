package com.example.ui

import android.app.Application
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.os.CountDownTimer
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.model.*
import com.example.data.repository.FamilyCareRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.math.sqrt

class FamilyCareViewModel(application: Application) : AndroidViewModel(application), SensorEventListener {

    private val db = AppDatabase.getDatabase(application)
    private val repository = FamilyCareRepository(db.familyCareDao(), application)

    // Current logged-in user profile simulation
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    // Language state default to true for Thai language
    private val _isThaiLanguage = MutableStateFlow(true)
    val isThaiLanguage: StateFlow<Boolean> = _isThaiLanguage.asStateFlow()

    fun toggleLanguage() {
        _isThaiLanguage.value = !_isThaiLanguage.value
    }

    // Navigation state simulation: "Splash", "Login", "Register", "GroupSetup", "ElderDashboard", "FamilyDashboard", "Settings"
    private val _currentScreen = MutableStateFlow("Splash")
    val currentScreen: StateFlow<String> = _currentScreen.asStateFlow()

    // Screen navigation stack history for simple back action
    private val navStack = mutableListOf<String>()

    // App alert banner simulation (displays alerts instantly on top of the dashboard UI)
    private val _activeNotification = MutableStateFlow<String?>(null)
    val activeNotification: StateFlow<String?> = _activeNotification.asStateFlow()

    // Voice record state for Push-To-Talk
    private val _isRecordingVoice = MutableStateFlow(false)
    val isRecordingVoice: StateFlow<Boolean> = _isRecordingVoice.asStateFlow()

    // Flow listings
    val activeSOSLogs: StateFlow<List<EmergencyLog>> = repository.activeLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allSOSLogs: StateFlow<List<EmergencyLog>> = repository.allLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val voiceMessages: StateFlow<List<VoiceMessage>> = repository.voiceMessages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val medications: StateFlow<List<Medication>> = repository.medications
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val medicationLogsToday: StateFlow<List<MedicationLog>> = repository.getMedicationLogsToday()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Sound alert variables
    private var simulatedRingtone: Ringtone? = null

    // Fall Detection State configuration
    private var sensorManager: SensorManager? = null
    private var accelerometer: Sensor? = null
    private val _fallDetectionEnabled = MutableStateFlow(true)
    val fallDetectionEnabled: StateFlow<Boolean> = _fallDetectionEnabled.asStateFlow()

    private val _isFallCountingDown = MutableStateFlow(false)
    val isFallCountingDown: StateFlow<Boolean> = _isFallCountingDown.asStateFlow()

    private val _fallCountdownSeconds = MutableStateFlow(30)
    val fallCountdownSeconds: StateFlow<Int> = _fallCountdownSeconds.asStateFlow()

    private var fallTimer: CountDownTimer? = null

    // Sensor raw indicators (for beautiful Settings metric views)
    private val _sensorX = MutableStateFlow(0f)
    val sensorX: StateFlow<Float> = _sensorX.asStateFlow()
    private val _sensorY = MutableStateFlow(0f)
    val sensorY: StateFlow<Float> = _sensorY.asStateFlow()
    private val _sensorZ = MutableStateFlow(0f)
    val sensorZ: StateFlow<Float> = _sensorZ.asStateFlow()

    init {
        // Initialize preset default logs and mock groups for best showcase experience
        viewModelScope.launch {
            repository.initializeDefaultData()
            // Pull initial user to check session (simulate previously saved sompong sessions)
            val sompong = db.familyCareDao().getUserById("elder_id_101")
            if (sompong != null) {
                // Set sompong as starting default session for immediate evaluation
                _currentUser.value = sompong
            }
        }

        // Register accelerometer sensors for Fall Detection
        setupSensors()
    }

    private fun setupSensors() {
        try {
            sensorManager = getApplication<Application>().getSystemService(Context.SENSOR_SERVICE) as SensorManager
            accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
            if (accelerometer != null && _fallDetectionEnabled.value) {
                sensorManager?.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI)
            }
        } catch (e: Exception) {
            Log.e("FallDetection", "Failed setup: ${e.message}")
        }
    }

    fun toggleFallDetection(enabled: Boolean) {
        _fallDetectionEnabled.value = enabled
        if (enabled) {
            sensorManager?.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI)
        } else {
            sensorManager?.unregisterListener(this)
            _isFallCountingDown.value = false
            fallTimer?.cancel()
        }
    }

    // Accelerometer listener method to compute G forces for Fall Detection
    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null || !_fallDetectionEnabled.value) return
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]

            _sensorX.value = x
            _sensorY.value = y
            _sensorZ.value = z

            // Compute net force vector: magnitude = sqrt(x^2 + y^2 + z^2)
            val magnitude = sqrt(x * x + y * y + z * z)
            // standard gravity is ~9.8. Fall involves short freefall (zero G) followed by sudden hard collision spike (> 28 m/s^2)
            if (magnitude > 28.0f && !_isFallCountingDown.value && _currentScreen.value == "ElderDashboard") {
                Log.d("FallDetection", "HARD IMPACT CRASH DETECTED: magnitude=$magnitude")
                triggerFallCountdown()
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    // 30 seconds count-down helper
    private fun triggerFallCountdown() {
        _isFallCountingDown.value = true
        _fallCountdownSeconds.value = 30
        playMockNotificationSound()

        fallTimer?.cancel()
        fallTimer = object : CountDownTimer(30000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                _fallCountdownSeconds.value = (millisUntilFinished / 1000).toInt()
            }

            override fun onFinish() {
                _isFallCountingDown.value = false
                // Auto trigger SOS on timer completion
                viewModelScope.launch {
                    triggerEmergencySOS("FALL DETECTED AUTOMATICALLY - Elderly user did not cancel countdown!")
                }
            }
        }.start()
    }

    fun cancelFallCountdown() {
        fallTimer?.cancel()
        _isFallCountingDown.value = false
        stopAlertSound()
    }

    // Simulated sound players
    fun playMockNotificationSound() {
        try {
            val alert: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            simulatedRingtone = RingtoneManager.getRingtone(getApplication(), alert)
            simulatedRingtone?.play()
        } catch (e: Exception) {
            Log.e("Sound", "Failed to play mock notification: ${e.message}")
        }
    }

    fun stopAlertSound() {
        simulatedRingtone?.stop()
    }

    // Route transitions
    fun navigateTo(screen: String) {
        navStack.add(_currentScreen.value)
        _currentScreen.value = screen
    }

    fun navigateBack() {
        if (navStack.isNotEmpty()) {
            _currentScreen.value = navStack.removeAt(navStack.lastIndex)
        } else {
            _currentScreen.value = "Login"
        }
    }

    // Auth Simulation Actions
    fun handleLogin(email: String, role: String) {
        viewModelScope.launch {
            // Find existing mock user Sompong or create a generic profile
            val name = if (role == "Elderly") "Sompong Somdee" else "Suda Somdee"
            val id = if (role == "Elderly") "elder_id_101" else "daughter_id_102"
            val user = User(
                id = id,
                name = name,
                email = email,
                role = role,
                groupCode = "FAM-Care-419"
            )
            repository.registerOrUpdateUser(user)
            _currentUser.value = user

            // Redirect automatically based on choice
            if (user.role == "Elderly") {
                navigateTo("ElderDashboard")
            } else {
                navigateTo("FamilyDashboard")
            }
        }
    }

    fun handleRegister(name: String, email: String, role: String) {
        viewModelScope.launch {
            val user = User(
                id = "usr_${System.currentTimeMillis()}",
                name = name,
                email = email,
                role = role,
                groupCode = null
            )
            repository.registerOrUpdateUser(user)
            _currentUser.value = user
            navigateTo("GroupSetup")
        }
    }

    fun handleGroupJoinOrCreate(isCreate: Boolean, groupNameCode: String) {
        viewModelScope.launch {
            val currentUserVal = _currentUser.value ?: return@launch
            if (isCreate) {
                val group = repository.createGroup(groupNameCode, currentUserVal.id)
                val updatedUser = currentUserVal.copy(groupCode = group.code)
                repository.registerOrUpdateUser(updatedUser)
                _currentUser.value = updatedUser
            } else {
                val group = repository.findGroup(groupNameCode)
                if (group != null) {
                    val updatedUser = currentUserVal.copy(groupCode = group.code)
                    repository.registerOrUpdateUser(updatedUser)
                    _currentUser.value = updatedUser
                } else {
                    _activeNotification.value = "Invalid Group Code! Created a new temporary group for you."
                    val groupAlt = repository.createGroup("My Family Link", currentUserVal.id)
                    val updatedUser = currentUserVal.copy(groupCode = groupAlt.code)
                    repository.registerOrUpdateUser(updatedUser)
                    _currentUser.value = updatedUser
                }
            }

            if (_currentUser.value?.role == "Elderly") {
                navigateTo("ElderDashboard")
            } else {
                navigateTo("FamilyDashboard")
            }
        }
    }

    fun handleLogout() {
        _currentUser.value = null
        navigateTo("Login")
    }

    // Emergency Action Execution
    suspend fun triggerEmergencySOS(customMessage: String? = null) {
        val userName = _currentUser.value?.name ?: "Elderly Sompong"
        val message = customMessage ?: "EMERGENCY SOS BUTTON PRESSED!"

        // Trigger local notification sound
        playMockNotificationSound()

        // GPS simulator - slight random jittering to make location look fully hot & live!
        val randomLatOffset = ((1..100).random() - 50) / 10000.0
        val randomLngOffset = ((1..100).random() - 50) / 10000.0
        val baseLat = 13.7563 + randomLatOffset
        val baseLng = 100.5018 + randomLngOffset

        repository.triggerSOS(
            elderName = userName,
            lat = baseLat,
            lng = baseLng,
            message = message
        )

        // Simulate instant cloud notify trigger and broad warning on screen!
        _activeNotification.value = "SOS BROADCAST SENT ACTIVE TO FAMILY MEMBERS!"
    }

    fun resolveAlert(logId: Int) {
        viewModelScope.launch {
            val resolverName = _currentUser.value?.name ?: "Suda (Daughter)"
            repository.resolveLog(logId, resolverName)
            stopAlertSound()
            _activeNotification.value = "Emergency status marked as safe and resolved."
        }
    }

    fun clearAlerts() {
        viewModelScope.launch {
            repository.clearLogs()
        }
    }

    // PTT Recording triggers
    fun startPTTRecording() {
        _isRecordingVoice.value = true
        val userName = _currentUser.value?.name ?: "Elderly Sompong"
        repository.startRecording(userName)
    }

    fun stopPTTRecording() {
        _isRecordingVoice.value = false
        viewModelScope.launch {
            val userName = _currentUser.value?.name ?: "Elderly Sompong"
            val msg = repository.stopAndSaveRecording(userName)
            if (msg != null) {
                _activeNotification.value = "PTT Voice sent successfully to Family Feed."
            }
        }
    }

    fun playPTTVoice(filepath: String, onComplete: () -> Unit = {}) {
        repository.playVoiceMessage(filepath, onComplete)
    }

    // Medication log checklist transactions
    fun addMedicationItem(name: String, dosage: String, time: String, desc: String) {
        viewModelScope.launch {
            repository.addMedication(name, dosage, time, desc)
            _activeNotification.value = "Medication '$name' added to daily schedule."
        }
    }

    fun deleteMedicationItem(med: Medication) {
        viewModelScope.launch {
            repository.deleteMedicationItem(med)
        }
    }

    fun takeMedication(medId: Int, medName: String) {
        viewModelScope.launch {
            repository.logMedicationIntake(medId, medName, isTaken = true)
            _activeNotification.value = "Marked '$medName' as TAKEN successfully."
        }
    }

    fun dismissNotification() {
        _activeNotification.value = null
    }

    override fun onCleared() {
        super.onCleared()
        sensorManager?.unregisterListener(this)
        stopAlertSound()
    }
}
