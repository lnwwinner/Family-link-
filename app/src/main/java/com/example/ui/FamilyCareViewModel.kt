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

    // Identity State tracking
    private val _identityState = MutableStateFlow<com.example.domain.identity.IdentityState>(com.example.domain.identity.IdentityState.Idle)
    val identityState: StateFlow<com.example.domain.identity.IdentityState> = _identityState.asStateFlow()
    private val thaiDService = com.example.service.ThaiDAuthService()

    // MOPH Integration placeholders
    private val mophAppointmentRepository = com.example.data.moph.MophAppointmentRepository()
    
    // Facility Locator state
    private val facilityDao = com.example.data.database.AppDatabase.getDatabase(getApplication()).medicalFacilityDao()
    private val facilityRepository = com.example.data.medical.LocalFacilityRepository(facilityDao)
    private val _nearbyFacilities = MutableStateFlow<List<com.example.domain.medical.MedicalFacility>>(emptyList())
    val nearbyFacilities: StateFlow<List<com.example.domain.medical.MedicalFacility>> = _nearbyFacilities.asStateFlow()

    // Language state default to true for Thai language
    private val _isThaiLanguage = MutableStateFlow(true)
    val isThaiLanguage: StateFlow<Boolean> = _isThaiLanguage.asStateFlow()

    // Accessibility state
    private val _isHighContrast = MutableStateFlow(false)
    val isHighContrast: StateFlow<Boolean> = _isHighContrast.asStateFlow()

    private val _fontSizeMultiplier = MutableStateFlow(1.0f)
    val fontSizeMultiplier: StateFlow<Float> = _fontSizeMultiplier.asStateFlow()

    fun toggleLanguage() {
        _isThaiLanguage.value = !_isThaiLanguage.value
    }

    fun toggleHighContrast() {
        _isHighContrast.value = !_isHighContrast.value
    }

    fun setFontSizeMultiplier(multiplier: Float) {
        _fontSizeMultiplier.value = multiplier
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

    // Smart Watch State Variables
    val allWatchHealthData: StateFlow<List<WatchHealthData>> = repository.allWatchHealthData
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val latestWatchHealthData: StateFlow<WatchHealthData?> = repository.latestWatchHealthData
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val appointments: StateFlow<List<HospitalAppointment>> = repository.allAppointments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val medicalDocuments: StateFlow<List<MedicalDocument>> = repository.allMedicalDocuments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val emergencyProfile: StateFlow<EmergencyProfile?> = repository.emergencyProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _selectedWatchBrand = MutableStateFlow("Samsung Galaxy Watch")
    val selectedWatchBrand: StateFlow<String> = _selectedWatchBrand.asStateFlow()

    private val _isSimulatingWatchData = MutableStateFlow(true)
    val isSimulatingWatchData: StateFlow<Boolean> = _isSimulatingWatchData.asStateFlow()

    private val _watchBatteryLevel = MutableStateFlow(88)
    val watchBatteryLevel: StateFlow<Int> = _watchBatteryLevel.asStateFlow()

    private val _isSyncingWatchData = MutableStateFlow(false)
    val isSyncingWatchData: StateFlow<Boolean> = _isSyncingWatchData.asStateFlow()

    private val _lastWatchSyncTime = MutableStateFlow<Long>(System.currentTimeMillis())
    val lastWatchSyncTime: StateFlow<Long> = _lastWatchSyncTime.asStateFlow()

    init {
        // Start real-time watch health physiological metrics simulation stream
        viewModelScope.launch {
            while (true) {
                if (_isSimulatingWatchData.value) {
                    try {
                        val currentLatest = repository.latestWatchHealthData.first()
                        val brand = _selectedWatchBrand.value
                        
                        // Realistic biological physiological range variation
                        val baseHr = if (currentLatest != null && currentLatest.watchType == brand) currentLatest.heartRate else (70..80).random()
                        val hrDelta = (-2..2).random()
                        val newHr = (baseHr + hrDelta).coerceIn(58, 108)

                        val baseOxygen = if (currentLatest != null && currentLatest.watchType == brand) currentLatest.oxygenLevel else (97..100).random()
                        val o2Delta = if (Math.random() > 0.8) (-1..1).random() else 0
                        val newO2 = (baseOxygen + o2Delta).coerceIn(94, 100)

                        val baseStress = if (currentLatest != null && currentLatest.watchType == brand) currentLatest.stressLevel else (25..40).random()
                        val stressDelta = (-2..2).random()
                        val newStress = (baseStress + stressDelta).coerceIn(10, 80)

                        val newSystolic = (118..126).random()
                        val newDiastolic = (76..82).random()

                        val ecgOptions = listOf("Normal Sinus Rhythm", "Sinus Rhythm (Rest)", "Normal Rhythm Trend")
                        val newEcg = ecgOptions.random()

                        val battery = _watchBatteryLevel.value
                        val newBattery = if (battery > 5) battery - 1 else 98
                        _watchBatteryLevel.value = newBattery

                        val simulatedMetric = WatchHealthData(
                            watchType = brand,
                            timestamp = System.currentTimeMillis(),
                            heartRate = newHr,
                            oxygenLevel = newO2,
                            sleepDurationHours = currentLatest?.sleepDurationHours ?: 7.2,
                            sleepQuality = currentLatest?.sleepQuality ?: "Deep & Healthy",
                            stressLevel = newStress,
                            bloodPressureSystolic = newSystolic,
                            bloodPressureDiastolic = newDiastolic,
                            ecgResult = newEcg,
                            batteryLevel = newBattery,
                            connectionStatus = "Connected",
                            isSynced = false
                        )
                        repository.saveWatchHealthData(simulatedMetric)
                    } catch (e: Exception) {
                        Log.e("WatchSimulation", "Failed to stream watch metric: ${e.message}")
                    }
                }
                kotlinx.coroutines.delay(4000) // update every 4 seconds for a active lively appearance
            }
        }
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
            val app = getApplication<Application>()
            val sensorContext = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                app.createAttributionContext("sensors")
            } else {
                app
            }
            sensorManager = sensorContext.getSystemService(Context.SENSOR_SERVICE) as SensorManager
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
                val updatedUser = currentUserVal.copy(groupCode = group.groupCode)
                repository.registerOrUpdateUser(updatedUser)
                _currentUser.value = updatedUser
            } else {
                val group = repository.findGroup(groupNameCode)
                if (group != null) {
                    val updatedUser = currentUserVal.copy(groupCode = group.groupCode)
                    repository.registerOrUpdateUser(updatedUser)
                    _currentUser.value = updatedUser
                } else {
                    _activeNotification.value = "Invalid Group Code! Created a new temporary group for you."
                    val groupAlt = repository.createGroup("My Family Link", currentUserVal.id)
                    val updatedUser = currentUserVal.copy(groupCode = groupAlt.groupCode)
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

    // ThaiD Integration Actions
    fun initiateThaiDAuth() {
        _identityState.value = com.example.domain.identity.IdentityState.Loading
        // จำลองการเปิด Intent ไปยังแอป ThaiD
        val intent = thaiDService.getAuthIntent()
        Log.d("ThaiDAuth", "Initiating OIDC Auth: $intent")
        
        // จำลองการ callback กลับมาใน 2 วินาทีหลังจาก init
        viewModelScope.launch {
            kotlinx.coroutines.delay(2000)
            handleThaiDCallback("mock_callback_data")
        }
    }

    private fun handleThaiDCallback(data: String) {
        val result = thaiDService.handleCallback(data)
        when (result) {
            is com.example.domain.auth.AuthResult.Success -> {
                _identityState.value = com.example.domain.identity.IdentityState.Success(result.identity)
                // อัปเดตข้อมูลผู้ใช้หลักถ้าจำเป็น
                val currentUserVal = _currentUser.value
                if (currentUserVal != null) {
                    // Update user profile to include identity info
                }
            }
            is com.example.domain.auth.AuthResult.Error -> {
                _identityState.value = com.example.domain.identity.IdentityState.Error(result.message)
            }
            is com.example.domain.auth.AuthResult.Loading -> _identityState.value = com.example.domain.identity.IdentityState.Loading
        }
    }

    // Search for facilities
    fun searchNearbyFacilities(lat: Double, lon: Double) {
        val allFacilities = facilityRepository.getNearbyFacilities(lat, lon)
        _nearbyFacilities.value = allFacilities
            .map { facility ->
                val dist = calculateDistance(lat, lon, facility.latitude, facility.longitude)
                facility.copy(distanceKm = dist)
            }
            .sortedBy { it.distanceKm }
    }

    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val earthRadius = 6371.0 // km
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return earthRadius * c
    }

    // Voice & Assistant
    fun processVoiceCommand(text: String, apiKey: String) {
        viewModelScope.launch {
            if (text.contains("ฉุกเฉิน", ignoreCase = true) || text.contains("SOS", ignoreCase = true)) {
                repository.triggerSOS("Elderly User")
            } else {
                val request = com.example.data.service.GenerateContentRequest(
                    listOf(com.example.data.service.Content(listOf(com.example.data.service.Part("ประเมินสุขภาพจากข้อความนี้: $text"))))
                )
                val response = com.example.data.service.RetrofitClient.service.generateContent(apiKey, request)
                _voiceAssistantResponse.value = response.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: "ไม่สามารถประเมินได้"
            }
        }
    }

    fun triggerVoiceReminder(activityName: String) {
        com.example.util.VoiceAnnouncementManager.announce(activityName)
    }
    
    private val _voiceAssistantResponse = MutableStateFlow("")
    val voiceAssistantResponse: StateFlow<String> = _voiceAssistantResponse.asStateFlow()

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

    // Smartwatch specific operations
    fun selectWatchBrand(brand: String) {
        _selectedWatchBrand.value = brand
        viewModelScope.launch {
            val currentLatest = repository.latestWatchHealthData.first()
            val newMetric = WatchHealthData(
                watchType = brand,
                timestamp = System.currentTimeMillis(),
                heartRate = (68..75).random(),
                oxygenLevel = (97..99).random(),
                sleepDurationHours = when (brand) {
                    "Wear OS" -> 6.8
                    "Samsung Galaxy Watch" -> 7.2
                    "Xiaomi Watch" -> 7.5
                    "Huawei Watch" -> 7.8
                    else -> 7.0
                },
                sleepQuality = when (brand) {
                    "Wear OS" -> "Light & Restless"
                    "Samsung Galaxy Watch" -> "Deep & Healthy"
                    else -> "REM Cycle Peak"
                },
                stressLevel = (20..40).random(),
                bloodPressureSystolic = (118..124).random(),
                bloodPressureDiastolic = (76..81).random(),
                ecgResult = "Normal Sinus Rhythm",
                batteryLevel = 92,
                connectionStatus = "Connected",
                isSynced = false
            )
            repository.saveWatchHealthData(newMetric)
            _activeNotification.value = "Successfully paired & connected with $brand"
        }
    }

    fun toggleSimulateWatchData(simulate: Boolean) {
        _isSimulatingWatchData.value = simulate
    }

    fun triggerWatchSOS() {
        viewModelScope.launch {
            val brand = _selectedWatchBrand.value
            val hr = repository.latestWatchHealthData.first()?.heartRate ?: 95
            val bpSys = repository.latestWatchHealthData.first()?.bloodPressureSystolic ?: 124
            val bpDia = repository.latestWatchHealthData.first()?.bloodPressureDiastolic ?: 80
            val o2 = repository.latestWatchHealthData.first()?.oxygenLevel ?: 97

            val customSosMessage = "WATCH SOS BUTTON PRESSED on $brand! Elder is calling for urgent response. Live Health: HR: $hr bpm, SpO2: $o2%, BP: $bpSys/$bpDia."
            triggerEmergencySOS(customSosMessage)
        }
    }

    fun triggerWatchFall() {
        val brand = _selectedWatchBrand.value
        viewModelScope.launch {
            // Trigger 30 seconds count-down in app
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
                    viewModelScope.launch {
                        val hr = repository.latestWatchHealthData.first()?.heartRate ?: 102
                        val bpSys = repository.latestWatchHealthData.first()?.bloodPressureSystolic ?: 135
                        val bpDia = repository.latestWatchHealthData.first()?.bloodPressureDiastolic ?: 88
                        val o2 = repository.latestWatchHealthData.first()?.oxygenLevel ?: 94
                        
                        val msg = "WATCH FALL DETECTED AUTOMATICALLY by critical sensors on $brand! Elder did not respond to countdown. Live Health: HR: $hr bpm, SpO2: $o2%, BP: $bpSys/$bpDia."
                        triggerEmergencySOS(msg)
                    }
                }
            }.start()
            
            _activeNotification.value = "CRITICAL: Watch registered a fall! 30s Countdown started."
        }
    }

    fun syncWatchDataWithCloud() {
        viewModelScope.launch {
            _isSyncingWatchData.value = true
            repository.syncWatchData()
            _isSyncingWatchData.value = false
            _lastWatchSyncTime.value = System.currentTimeMillis()
            _activeNotification.value = "Synchronization completed! All smart watch health metrics uploaded safely to Family Link cloud database."
        }
    }

    // NEW HOSPITAL APPOINTMENT MODULE VM OPERATIONS

    private val _isSyncingCalendar = MutableStateFlow(false)
    val isSyncingCalendar: StateFlow<Boolean> = _isSyncingCalendar.asStateFlow()

    private val _isProcessingOcrSlips = MutableStateFlow(false)
    val isProcessingOcrSlips: StateFlow<Boolean> = _isProcessingOcrSlips.asStateFlow()

    fun addNewAppointment(
        patient: String,
        hospital: String,
        doctor: String,
        department: String,
        timestamp: Long,
        reminderMins: Int,
        isReminderEnabled: Boolean,
        notes: String,
        qrcode: String? = null,
        docPath: String? = null,
        ocrText: String? = null,
        isShared: Boolean = true,
        treatment: String = "",
        diag: String = ""
    ) {
        viewModelScope.launch {
            val appt = HospitalAppointment(
                patientName = patient.ifBlank { "Sompong Somdee" },
                hospitalName = hospital,
                doctorName = doctor,
                department = department,
                appointmentTimestamp = timestamp,
                reminderMinutesBefore = reminderMins,
                isReminderSet = isReminderEnabled,
                notes = notes,
                qrCodeData = qrcode,
                documentPath = docPath,
                ocrExtractedText = ocrText,
                isFamilyShared = isShared,
                isSynced = false,
                treatmentSuggested = treatment,
                diagnosis = diag
            )
            repository.addAppointment(appt)
            _activeNotification.value = "Appointment for $doctor scheduled successfully."
        }
    }

    fun deleteAppointment(appt: HospitalAppointment) {
        viewModelScope.launch {
            repository.deleteAppointmentItem(appt)
            _activeNotification.value = "Appointment deleted successfully."
        }
    }

    fun addMedicalDocument(title: String, type: String, appointmentId: Int?, detail: String? = null) {
        viewModelScope.launch {
            val doc = MedicalDocument(
                appointmentId = appointmentId,
                title = title,
                documentType = type,
                filePath = "Uploaded Document File - simulated_${System.currentTimeMillis()}.pdf",
                timestamp = System.currentTimeMillis(),
                extractedDetails = detail,
                isSynced = false
            )
            repository.saveMedicalDocument(doc)
            _activeNotification.value = "Medical document '$title' uploaded safely."
        }
    }

    fun deleteMedicalDocument(doc: MedicalDocument) {
        viewModelScope.launch {
            repository.deleteMedicalDocumentItem(doc)
            _activeNotification.value = "Document deleted from secure health vault."
        }
    }

    fun updateEmergencyMedicalCard(
        blood: String,
        allergies: String,
        chronic: String,
        prescription: String,
        contactName: String,
        contactPhone: String,
        hospital: String,
        insurance: String,
        notes: String
    ) {
        viewModelScope.launch {
            val profile = EmergencyProfile(
                id = 1,
                patientName = "Sompong Somdee",
                bloodType = blood,
                allergies = allergies,
                chronicConditions = chronic,
                regularPrescriptions = prescription,
                emergencyContactName = contactName,
                emergencyContactPhone = contactPhone,
                hospitalPreference = hospital,
                insuranceDetails = insurance,
                additionalNotes = notes
            )
            repository.updateEmergencyProfile(profile)
            _activeNotification.value = "Emergency Medical Contact Card updated."
        }
    }

    fun syncAppointmentWithGoogleCalendar(appt: HospitalAppointment) {
        viewModelScope.launch {
            _isSyncingCalendar.value = true
            val success = repository.syncGoogleCalendarEvent(appt)
            _isSyncingCalendar.value = false
            if (success) {
                _activeNotification.value = "Event '${appt.doctorName} Checkup' synchronized cleanly with Google Calendar API!"
            }
        }
    }

    fun triggerOcrScanSimulation(onResult: (HospitalAppointment) -> Unit) {
        viewModelScope.launch {
            _isProcessingOcrSlips.value = true
            kotlinx.coroutines.delay(2000) // Simulated scanner OCR processing delay
            _isProcessingOcrSlips.value = false

            // Randomize beautiful dynamic simulation cases
            val templates = listOf(
                HospitalAppointment(
                    patientName = "Sompong Somdee",
                    hospitalName = "Vajira General Hospital",
                    doctorName = "Dr. Kittichai (Heart Specialism)",
                    department = "EKG Room / คลินิกประสาทวิทยา",
                    appointmentTimestamp = System.currentTimeMillis() + 5 * 24 * 60 * 60 * 1000L,
                    reminderMinutesBefore = 60,
                    isReminderSet = true,
                    notes = "Regular neural screening. Bring blood records.",
                    qrCodeData = "VJR_APPT_S_9821_XYZ",
                    ocrExtractedText = "VAJIRA MEDICAL SLIP\nPATIENT: SOMPONG SOMDEE\nDEPT: NEUROLOGY\nDOCTOR: DR. KITTICHAI\nTIME: 09:30 AM",
                    isFamilyShared = true,
                    isSynced = false
                ),
                HospitalAppointment(
                    patientName = "Sompong Somdee",
                    hospitalName = "Bangkok General Hospital",
                    doctorName = "Dr. Suda (Audiology Section)",
                    department = "Ear & Nose / ศูนย์หูคอจมูก",
                    appointmentTimestamp = System.currentTimeMillis() + 8 * 24 * 60 * 60 * 1000L,
                    reminderMinutesBefore = 120,
                    isReminderSet = true,
                    notes = "Hearing aid recalibration and middle ear audit.",
                    qrCodeData = "BGH_AUD_90312",
                    ocrExtractedText = "BANGKOK GENERAL HOSPITAL APPT\nPATIENT: SOMPONG\nSTAMP: 2026-06-11 13:00\nDEPT: AUDIOLOGY\nFEE_PAID",
                    isFamilyShared = true,
                    isSynced = false
                )
            )
            val selected = templates.random()
            onResult(selected)
            _activeNotification.value = "AI OCR Camera Scan: Appointment details extracted successfully!"
        }
    }

    override fun onCleared() {
        super.onCleared()
        sensorManager?.unregisterListener(this)
        stopAlertSound()
    }
}
