package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.HospitalAppointment
import com.example.data.model.MedicalDocument
import com.example.data.model.EmergencyProfile
import com.example.ui.FamilyCareViewModel



import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

// Sanitized locally-unique styled variables
private val CozySlate = Color(0xFF1E293B)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HospitalAppointmentsScreen(viewModel: FamilyCareViewModel) {
    val isTh by viewModel.isThaiLanguage.collectAsState()
    val appointments by viewModel.appointments.collectAsState()
    val documents by viewModel.medicalDocuments.collectAsState()
    val emergencyCard by viewModel.emergencyProfile.collectAsState()
    val isSyncingCalendar by viewModel.isSyncingCalendar.collectAsState()
    val isProcessingOcr by viewModel.isProcessingOcrSlips.collectAsState()

    val coroutineScope = rememberCoroutineScope()
    var selectedTab by remember { mutableStateOf(0) }
    var selectedAppointment by remember { mutableStateOf<HospitalAppointment?>(null) }

    // On start, if selectedAppointment is null, default to the closest upcoming one
    LaunchedEffect(appointments) {
        if (selectedAppointment == null && appointments.isNotEmpty()) {
            selectedAppointment = appointments.firstOrNull { it.appointmentTimestamp >= System.currentTimeMillis() }
                ?: appointments.firstOrNull()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = tr(isTh, "Hospital & Health Hub", "ศูนย์ประสานนัดหมายและเวชศาสตร์ด่วน"),
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp,
                            color = Color.White
                        )
                        Text(
                            text = tr(isTh, "AI Smart Care Link • Active Cloud Sync", "ระบบวิเคราะห์เชื่อมต่ออัจฉริยะ • ซิงค์คลาวด์ตลอดเวลา"),
                            fontSize = 11.sp,
                            color = SaniGreen,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.navigateBack() },
                        modifier = Modifier.testTag("back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    Box(
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .background(PrimaryAccent.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                            .border(1.dp, PrimaryAccent.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = tr(isTh, "HOSPITAL SECURE", "ความคุ้มครองระดับโรงพยาบาล"),
                            color = PrimaryAccent,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CozySlateBg)
            )
        },
        bottomBar = {
            // M3 Styled modern Navigation tab bar
            NavigationBar(
                containerColor = CozySlateBg,
                tonalElevation = 8.dp,
                windowInsets = WindowInsets.navigationBars
            ) {
                val navItems = listOf(
                    Triple(0, Icons.Default.Event, tr(isTh, "Appointments", "ตารางนัด")),
                    Triple(1, Icons.Default.Assignment, tr(isTh, "Info & QR", "ดีเทลนัด")),
                    Triple(2, Icons.Default.Folder, tr(isTh, "Files", "คลังเวชระเบียน")),
                    Triple(3, Icons.Default.Timeline, tr(isTh, "History", "ประวัติหน้างาน")),
                    Triple(4, Icons.Default.HealthAndSafety, tr(isTh, "Emergency", "บัตรโรคประจำตัว")),
                    Triple(5, Icons.Default.LocationOn, tr(isTh, "Locator", "ค้นหาโรงพยาบาล"))
                )

                navItems.forEach { (index, icon, label) ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        icon = {
                            Icon(
                                imageVector = icon,
                                contentDescription = label,
                                tint = if (selectedTab == index) CozySlateBg else Color.LightGray
                            )
                        },
                        label = {
                            Text(
                                text = label,
                                fontSize = 10.sp,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = CozySlateBg,
                            unselectedIconColor = Color.LightGray,
                            selectedTextColor = ContrastAmber,
                            unselectedTextColor = Color.LightGray,
                            indicatorColor = ContrastAmber
                        ),
                        modifier = Modifier.testTag("nav_tab_$index")
                    )
                }
            }
        },
        containerColor = CozySlateBg
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = {
                    fadeIn() togetherWith fadeOut()
                },
                label = "tab_fade_animation"
            ) { tab ->
                when (tab) {
                    0 -> AppointmentListTab(
                        viewModel = viewModel,
                        isTh = isTh,
                        appointments = appointments,
                        isProcessingOcr = isProcessingOcr,
                        onSelectAppointment = { appt ->
                            selectedAppointment = appt
                            selectedTab = 1 // Auto redirect to details tab for interactive feel
                        }
                    )
                    1 -> AppointmentDetailsTab(
                        viewModel = viewModel,
                        isTh = isTh,
                        selectedAppt = selectedAppointment,
                        isSyncingCalendar = isSyncingCalendar
                    )
                    2 -> MedicalDocumentsTab(
                        viewModel = viewModel,
                        isTh = isTh,
                        documents = documents,
                        appointments = appointments
                    )
                    3 -> MedicalTimelineTab(
                        viewModel = viewModel,
                        isTh = isTh,
                        appointments = appointments,
                        viewModelLogs = viewModel
                    )
                    4 -> EmergencyMedicalCardTab(
                        viewModel = viewModel,
                        isTh = isTh,
                        emergencyProfile = emergencyCard
                    )
                    5 -> FacilityLocatorTab(
                        viewModel = viewModel,
                        isTh = isTh
                    )
                }
            }
        }
    }
}

@Composable
fun FacilityLocatorTab(
    viewModel: FamilyCareViewModel,
    isTh: Boolean
) {
    val facilities by viewModel.nearbyFacilities.collectAsState()
    LaunchedEffect(Unit) {
        viewModel.searchNearbyFacilities(13.75, 100.5)
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(facilities) { facility ->
            Card(
                colors = CardDefaults.cardColors(containerColor = CozySlate),
                modifier = Modifier.fillMaxWidth().padding(8.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(facility.name, fontWeight = FontWeight.Bold, color = Color.White)
                    Text(facility.type, color = Color.LightGray)
                    Text("Distance: ${facility.distanceKm} km", color = Color.White)
                }
            }
        }
    }
}

@Composable
fun AppointmentListTab(
    viewModel: FamilyCareViewModel,
    isTh: Boolean,
    appointments: List<HospitalAppointment>,
    isProcessingOcr: Boolean,
    onSelectAppointment: (HospitalAppointment) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var filterStatus by remember { mutableStateOf("ALL") } // "ALL", "UPCOMING", "PAST"
    var showAddDialog by remember { mutableStateOf(false) }

    // Dialog form state variables
    var formHospital by remember { mutableStateOf("") }
    var formDoctor by remember { mutableStateOf("") }
    var formDepartment by remember { mutableStateOf("") }
    var formDateString by remember { mutableStateOf("") } // e.g. "2026-06-15 09:30"
    var formReminderMinutes by remember { mutableStateOf(60) }
    var formIsReminderEnabled by remember { mutableStateOf(true) }
    var formNotes by remember { mutableStateOf("") }
    var formIsFamilyShared by remember { mutableStateOf(true) }

    val filteredList = appointments.filter { appt ->
        val timestamp = appt.appointmentTimestamp
        val isFuture = timestamp >= System.currentTimeMillis()
        
        val matchesSearch = appt.doctorName.contains(searchQuery, ignoreCase = true) ||
                appt.hospitalName.contains(searchQuery, ignoreCase = true) ||
                appt.department.contains(searchQuery, ignoreCase = true)

        val matchesFilter = when (filterStatus) {
            "UPCOMING" -> isFuture
            "PAST" -> !isFuture
            else -> true
        }

        matchesSearch && matchesFilter
    }

    // Helper functions for formatting time inside layout
    val formatter = remember { SimpleDateFormat("EEE, d MMM yyyy 'at' hh:mm a", Locale.getDefault()) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            // Header actions & Quick description
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = tr(isTh, "Hospital Appointments", "กำหนดนัดหมายโรงพยาบาล"),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = tr(isTh, "Check lists & extract slip details", "ตรวจเช็คกำหนดการ และ สแกนสลิปนัดอัจฉริยะ"),
                        fontSize = 12.sp,
                        color = Color.LightGray
                    )
                }

                // Add button directly
                IconButton(
                    onClick = { showAddDialog = true },
                    modifier = Modifier
                        .background(PrimaryAccent, CircleShape)
                        .size(38.dp)
                        .testTag("add_appt_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Appointment", tint = Color.White)
                }
            }

            // Quick Scan Banner incorporating Camera Scan & OCR Feature
            Card(
                colors = CardDefaults.cardColors(containerColor = CozySlate),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, SaniGreen.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .background(SaniGreen.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.PhotoCamera,
                            contentDescription = "Camera Scan",
                            tint = SaniGreen,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = tr(isTh, "Scan Appointment Slip", "แสกนตรวจจับสลิปนัดใบนำทาง"),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color.White
                        )
                        Text(
                            text = tr(isTh, "Use AI Camera scan for instant OCR extraction", "แสกนรูปสลิปนัดเพื่อสกัดข้อมูลนัดหมายเข้าฐานข้อมูลทันที"),
                            fontSize = 11.sp,
                            color = Color.LightGray
                        )
                    }

                    Button(
                        onClick = {
                            viewModel.triggerOcrScanSimulation { ocrAppt ->
                                // Populated form with values
                                formHospital = ocrAppt.hospitalName
                                formDoctor = ocrAppt.doctorName
                                formDepartment = ocrAppt.department
                                val calendar = Calendar.getInstance()
                                calendar.timeInMillis = ocrAppt.appointmentTimestamp
                                formDateString = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(calendar.time)
                                formReminderMinutes = ocrAppt.reminderMinutesBefore
                                formIsReminderEnabled = ocrAppt.isReminderSet
                                formNotes = ocrAppt.notes + " [OCR_COMPLETED_SUCCESSFULLY]"
                                formIsFamilyShared = ocrAppt.isFamilyShared
                                showAddDialog = true
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SaniGreen),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(8.dp),
                        enabled = !isProcessingOcr,
                        modifier = Modifier.testTag("ocr_camera_scan_button")
                    ) {
                        if (isProcessingOcr) {
                            CircularProgressIndicator(modifier = Modifier.size(14.dp), color = Color.White, strokeWidth = 2.dp)
                        } else {
                            Text(
                                text = tr(isTh, "Scan Slip", "สแกนสลิป"),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            // Interactive live Scanner visual effects during processing
            if (isProcessingOcr) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                        .border(1.dp, SaniGreen, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = tr(isTh, "AI CONVOLUTIONAL SCANNING SENSORS ACTIVE...", "เซ็นเซอร์ประมวลผลดวงตา AI คัดกรองเวชภัณฑ์..."),
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFF50FA7B),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        LinearProgressIndicator(color = SaniGreen, trackColor = Color.DarkGray, modifier = Modifier.width(200.dp))
                    }
                }
            }

            // Search Bar & Filter tabs
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text(tr(isTh, "Search doctor, department or hospital...", "ค้นหา ชื่อแพทย์ คลินิก หรือ โรงพยาบาล..."), color = Color.LightGray) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.LightGray) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedContainerColor = DarkCardBg,
                    unfocusedContainerColor = DarkCardBg,
                    focusedBorderColor = ContrastAmber,
                    unfocusedBorderColor = Color.LightGray.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("search_appointments_input"),
                singleLine = true
            )

            // Filtering Tab strip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val filters = listOf(
                    "ALL" to tr(isTh, "All Schedules", "ทุกรายการ"),
                    "UPCOMING" to tr(isTh, "Upcoming Checks", "นัดถัดไป"),
                    "PAST" to tr(isTh, "Past Diagnoses", "นัดในประวัติ")
                )

                filters.forEach { (status, text) ->
                    val isSelected = filterStatus == status
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) ContrastAmber else DarkCardBg)
                            .clickable { filterStatus = status }
                            .padding(vertical = 8.dp, horizontal = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = text,
                            color = if (isSelected) CozySlateBg else Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Results summary check
            Text(
                text = tr(isTh, "Search Results: ${filteredList.size} items", "ผลการกรองค้นหา: ${filteredList.size} รายการ"),
                fontSize = 11.sp,
                color = SaniGreen,
                fontWeight = FontWeight.Bold
            )

            // Dynamic appointment listing
            if (filteredList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Icon(
                            Icons.Default.EventNote,
                            contentDescription = "Empty",
                            tint = Color.LightGray.copy(alpha = 0.4f),
                            modifier = Modifier.size(64.dp)
                        )
                        Text(
                            text = tr(isTh, "No Matching Appointments Found", "ไม่พบนัดหมายการรักษาที่ต้องการ"),
                            color = Color.LightGray,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    items(filteredList) { appt ->
                        val isFuture = appt.appointmentTimestamp >= System.currentTimeMillis()
                        
                        Card(
                            colors = CardDefaults.cardColors(containerColor = DarkCardBg),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelectAppointment(appt) }
                                .testTag("appointment_card_${appt.id}")
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Department Badge/Theme tag
                                    Box(
                                        modifier = Modifier
                                            .background(
                                                if (isFuture) SaniGreen.copy(alpha = 0.15f) else Color.LightGray.copy(alpha = 0.1f),
                                                RoundedCornerShape(6.dp)
                                            )
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = appt.department,
                                            color = if (isFuture) SaniGreen else Color.LightGray,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        )
                                    }

                                    // Display status badge (Upcoming, Past, Google Calendar Link pill)
                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                                        if (appt.isSynced) {
                                            Icon(
                                                Icons.Default.Sync,
                                                contentDescription = "Calendar Sync",
                                                tint = SaniGreen,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }

                                        Box(
                                            modifier = Modifier
                                                .background(
                                                    if (isFuture) PrimaryAccent.copy(alpha = 0.15f) else Color.DarkGray,
                                                    RoundedCornerShape(6.dp)
                                                )
                                                .padding(horizontal = 8.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = if (isFuture) tr(isTh, "UPCOMING", "สแตนบาย") else tr(isTh, "COMPLETED", "เสร็จสิ้น"),
                                                color = if (isFuture) PrimaryAccent else Color.LightGray,
                                                fontWeight = FontWeight.Black,
                                                fontSize = 9.sp
                                            )
                                        }
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .background(CozySlate, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Default.Person,
                                            contentDescription = "Doctor",
                                            tint = ContrastAmber,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = appt.doctorName,
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(Icons.Default.LocalHospital, contentDescription = "Hospital", tint = Color.LightGray, modifier = Modifier.size(12.dp))
                                            Text(
                                                appt.hospitalName,
                                                color = Color.LightGray,
                                                fontSize = 11.sp,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }

                                Divider(color = CozySlate.copy(alpha = 0.5f), thickness = 1.dp)

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Appointment Timing
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(Icons.Default.AccessTime, contentDescription = "Time", tint = ContrastAmber, modifier = Modifier.size(14.dp))
                                        Text(
                                            text = formatter.format(Date(appt.appointmentTimestamp)),
                                            color = Color.White,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }

                                    // Action to redirect
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = tr(isTh, "Details", "ชมดีเทลนัด"),
                                            color = ContrastAmber,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        )
                                        Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Details", tint = ContrastAmber, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal dialog to add a new appointment checklist record
    if (showAddDialog) {
        Dialog(onDismissRequest = { showAddDialog = false }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = CozySlate),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = tr(isTh, "Add Appointment Slip Record", "เพิ่มทะเบียนบันทึกและพิกัดนัดแพทย์"),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color.White
                        )
                        IconButton(onClick = { showAddDialog = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.LightGray)
                        }
                    }

                    Divider(color = Color.LightGray.copy(alpha = 0.2f))

                    // Fields
                    Text(tr(isTh, "Hospital Name *", "ชื่อโรงพยาบาล/คลินิกเวชกรรม *"), color = ContrastAmber, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = formHospital,
                        onValueChange = { formHospital = it },
                        modifier = Modifier.fillMaxWidth().testTag("add_appt_hospital_input"),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )

                    Text(tr(isTh, "Doctor's Name & Title *", "ชื่อของคุรหมอผู้ตรวจวินิจฉัย *"), color = ContrastAmber, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = formDoctor,
                        onValueChange = { formDoctor = it },
                        modifier = Modifier.fillMaxWidth().testTag("add_appt_doctor_input"),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )

                    Text(tr(isTh, "Specialized Clinic Department *", "แผนกตรวจสุขภาพ / ศูนย์วินิจฉัยโรค *"), color = ContrastAmber, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = formDepartment,
                        onValueChange = { formDepartment = it },
                        modifier = Modifier.fillMaxWidth().testTag("add_appt_dept_input"),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )

                    Text(tr(isTh, "Fictional Stamp (Format: yyyy-MM-dd HH:mm)", "เวลาและวันที่ (รูปแบบ: ปี-เดือน-วัน ชั่วโมง:นาที)"), color = ContrastAmber, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = formDateString,
                        onValueChange = { formDateString = it },
                        placeholder = { Text("e.g. 2026-06-15 09:30", color = Color.Gray) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(tr(isTh, "Activate Guard Reminder Alert:", "เปิดใช้งานระฆังแจ้งเตือนความคุ้มครอง:"), color = Color.White, fontSize = 12.sp)
                        Switch(
                            checked = formIsReminderEnabled,
                            onCheckedChange = { formIsReminderEnabled = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = SaniGreen, checkedTrackColor = SaniGreen.copy(alpha = 0.5f))
                        )
                    }

                    if (formIsReminderEnabled) {
                        Text(tr(isTh, "Reminder Settings:", "ส่งสัญญาณล่วงหน้าก่อนตรวจกี่นาที:"), color = Color.White, fontSize = 12.sp)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(30, 60, 120, 1440).forEach { mins ->
                                val textLabel = when (mins) {
                                    1440 -> tr(isTh, "1 Day Before", "1 วันล่วงหน้า")
                                    else -> "$mins mins"
                                }
                                val active = formReminderMinutes == mins
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(if (active) ContrastAmber else DarkCardBg, RoundedCornerShape(6.dp))
                                        .clickable { formReminderMinutes = mins }
                                        .padding(vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = textLabel,
                                        fontSize = 10.sp,
                                        color = if (active) CozySlateBg else Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    Text(tr(isTh, "Consultant Doctor's Instructions & Notes", "คำตักเตือนเพิ่มเติม และ รายละเอียดข้อจำกัด"), color = Color.LightGray, fontSize = 11.sp)
                    OutlinedTextField(
                        value = formNotes,
                        onValueChange = { formNotes = it },
                        modifier = Modifier.fillMaxWidth().height(80.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )

                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Checkbox(
                            checked = formIsFamilyShared,
                            onCheckedChange = { formIsFamilyShared = it ?: true },
                            colors = CheckboxDefaults.colors(checkedColor = SaniGreen)
                        )
                        Text(tr(isTh, "Share globally with family caregivers", "แชร์ให้ผู้ดูแลทุกคนในครอบครัวดูแลร่วมกัน"), color = Color.LightGray, fontSize = 11.sp)
                    }

                    Button(
                        onClick = {
                            if (formHospital.isNotBlank() && formDoctor.isNotBlank() && formDepartment.isNotBlank()) {
                                // Parse date safely
                                val parsedTime = try {
                                    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                                    sdf.parse(formDateString)?.time ?: System.currentTimeMillis()
                                } catch (e: Exception) {
                                    System.currentTimeMillis() + 3 * 24 * 60 * 60 * 1000L // Default to in 3 days in case of error
                                }

                                viewModel.addNewAppointment(
                                    patient = "Sompong Somdee",
                                    hospital = formHospital,
                                    doctor = formDoctor,
                                    department = formDepartment,
                                    timestamp = parsedTime,
                                    reminderMins = formReminderMinutes,
                                    isReminderEnabled = formIsReminderEnabled,
                                    notes = formNotes,
                                    isShared = formIsFamilyShared
                                )
                                showAddDialog = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryAccent),
                        modifier = Modifier.fillMaxWidth().testTag("add_appt_submit_button")
                    ) {
                        Text(tr(isTh, "Confirm Scheduling", "ปักนัดรักษาระบบเสถียร"), color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ==========================================
// TAB 1: APPOINTMENT DETAILS & SPECIFICS
// ==========================================
@Composable
fun AppointmentDetailsTab(
    viewModel: FamilyCareViewModel,
    isTh: Boolean,
    selectedAppt: HospitalAppointment?,
    isSyncingCalendar: Boolean
) {
    val formatter = remember { SimpleDateFormat("EEEE, d MMMM yyyy 'at' hh:mm a", Locale.getDefault()) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        if (selectedAppt == null) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(40.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    Icons.Default.HourglassEmpty,
                    contentDescription = "Empty selection",
                    tint = Color.LightGray,
                    modifier = Modifier.size(64.dp)
                )
                Text(
                    text = tr(isTh, "Select an appointment from the list tab", "กรุณาเลือกตารางนัดแพทย์เพื่อตรวจสอบที่สัญลักษณ์นัด"),
                    color = Color.LightGray,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold
                )
            }
        } else {
            val isUpcoming = selectedAppt.appointmentTimestamp >= System.currentTimeMillis()

            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Return navigation hints
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = tr(isTh, "Consultation Voucher Card", "บัตรตรวจคลินิกและข้อมูลเวชกรรม"),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )

                    Box(
                        modifier = Modifier
                            .background(
                                if (selectedAppt.isSynced) SaniGreen.copy(alpha = 0.15f) else Color.DarkGray,
                                RoundedCornerShape(10.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (selectedAppt.isSynced) tr(isTh, "SYNCED TO CALENDAR", "ซิงค์ปฏิทินสำเร็จ") else tr(isTh, "NOT SYNCED", "สแตนบายคลาวด์"),
                            color = if (selectedAppt.isSynced) SaniGreen else Color.LightGray,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Core details card
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkCardBg),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(6.dp, RoundedCornerShape(16.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = selectedAppt.department,
                                color = SaniGreen,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )

                            Icon(
                                imageVector = if (selectedAppt.isFamilyShared) Icons.Default.Groups else Icons.Default.Lock,
                                contentDescription = "Shared Status",
                                tint = Color.LightGray,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // Medical institution title
                        Text(
                            text = selectedAppt.hospitalName,
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .background(CozySlate, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Healing, contentDescription = "Specialist", tint = ContrastAmber, modifier = Modifier.size(16.dp))
                            }
                            Column {
                                Text(
                                    text = selectedAppt.doctorName,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = tr(isTh, "Assigned Medical Consultant", "อาจารย์แพทย์ผู้รับผิดชอบประเมิน"),
                                    color = Color.LightGray,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Divider(color = CozySlate, thickness = 1.dp)

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .background(CozySlate, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.CalendarToday, contentDescription = "Date", tint = ContrastAmber, modifier = Modifier.size(16.dp))
                            }
                            Column {
                                Text(
                                    text = formatter.format(Date(selectedAppt.appointmentTimestamp)),
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = tr(isTh, "Consultation Schedule", "วันเวลาพิกัดจองตรวจ"),
                                    color = Color.LightGray,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }

                // QR Code appointment support using custom Canvas
                Card(
                    colors = CardDefaults.cardColors(containerColor = CozySlate),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = tr(isTh, "Clinic Smart QR-Code Token", "ระบบสัญญาณคิวอาร์สลิปอัจฉริยะ"),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = tr(isTh, "Scan at the nurse triage triage point for automatic registration", "ใช้สแกนที่ตู้ทะเบียนโรงพยาบาลเพื่อเข้าแถวคัดกรองอัตโนมัติ"),
                            color = Color.LightGray,
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center
                        )

                        // QR Code canvas simulation
                        Box(
                            modifier = Modifier
                                .size(160.dp)
                                .background(Color.White, RoundedCornerShape(8.dp))
                                .padding(10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val gridCount = 8
                                val cellWidth = size.width / gridCount
                                val cellHeight = size.height / gridCount
                                val seed = (selectedAppt.qrCodeData ?: "DEFAULT").hashCode()
                                val random = Random(seed.toLong())

                                for (row in 0 until gridCount) {
                                    for (col in 0 until gridCount) {
                                        // Corner anchor modules are always simulated black for classic QR look
                                        val isAnchor = (row < 3 && col < 3) ||
                                                (row > gridCount - 4 && col < 3) ||
                                                (row < 3 && col > gridCount - 4)

                                        if (isAnchor || random.nextBoolean()) {
                                            drawRect(
                                                color = Color.Black,
                                                topLeft = androidx.compose.ui.geometry.Offset(col * cellWidth, row * cellHeight),
                                                size = androidx.compose.ui.geometry.Size(cellWidth + 1, cellHeight + 1)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Text(
                            text = "TOKEN: ${selectedAppt.qrCodeData ?: "HOSP_AP_SOMPONG_${selectedAppt.id}"}",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp,
                            color = ContrastAmber
                        )
                    }
                }

                // Specific Diagnosis Notes and treatments Suggested
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkCardBg),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.MenuBook, contentDescription = "Notes", tint = ContrastAmber, modifier = Modifier.size(18.dp))
                            Text(
                                text = tr(isTh, "Clinical Case Records", "บันทึกผลการคัดกรอง"),
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 13.sp
                            )
                        }

                        if (selectedAppt.diagnosis.isNotBlank()) {
                            Text(
                                text = "${tr(isTh, "Diagnosis / โรคประจำตัว:", "ผลตรวจกรอง: ")} ${selectedAppt.diagnosis}",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        if (selectedAppt.treatmentSuggested.isNotBlank()) {
                            Text(
                                text = "${tr(isTh, "Suggested Protocol / ข้อสั่งกระตุ้น:", "ข้อบังคับรักษา: ")} ${selectedAppt.treatmentSuggested}",
                                color = SaniGreen,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Text(
                            text = selectedAppt.notes.ifBlank { tr(isTh, "No specific medical remarks entered yet.", "ยังไม่มีข้อความสั่งการรักษาพิเศษระบุไว้") },
                            color = Color.LightGray,
                            fontSize = 12.sp
                        )

                        // If OCR Slip was previously processed, show extracted metadata container
                        selectedAppt.ocrExtractedText?.let { rawText ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(CozySlate, RoundedCornerShape(8.dp))
                                    .padding(10.dp)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(
                                        text = tr(isTh, "AI OCR RAW METADATA EXTRACTION:", "ข้อมูลดิบ AI ที่ตรวจพบในสลิป:"),
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color(0xFF50FA7B)
                                    )
                                    Text(
                                        text = rawText,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 10.sp,
                                        color = Color.LightGray
                                    )
                                }
                            }
                        }
                    }
                }

                // Google Calendar Synchronization Action Card
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { viewModel.syncAppointmentWithGoogleCalendar(selectedAppt) },
                        colors = ButtonDefaults.buttonColors(containerColor = SaniGreen),
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .testTag("sync_google_calendar_button"),
                        shape = RoundedCornerShape(10.dp),
                        enabled = !isSyncingCalendar
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            if (isSyncingCalendar) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                            } else {
                                Icon(Icons.Default.CloudSync, contentDescription = "Sync", tint = Color.White, modifier = Modifier.size(18.dp))
                                Text(
                                    text = tr(isTh, "Sync to Google Calendar", "ประสานปฏิทินโรงพยาบาล"),
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }

                    Button(
                        onClick = { viewModel.deleteAppointment(selectedAppt) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC30000)),
                        modifier = Modifier
                            .weight(0.8f)
                            .height(46.dp)
                            .testTag("delete_appt_button"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.DeleteSweep, contentDescription = "Delete", tint = Color.White, modifier = Modifier.size(18.dp))
                            Text(
                                text = tr(isTh, "Cancel Appointment", "ลบยกเลิกรายการ"),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

// ==========================================
// TAB 2: MEDICAL DOCUMENTS VAULT (Document Storage)
// ==========================================
@Composable
fun MedicalDocumentsTab(
    viewModel: FamilyCareViewModel,
    isTh: Boolean,
    documents: List<MedicalDocument>,
    appointments: List<HospitalAppointment>
) {
    var showAddDocDialog by remember { mutableStateOf(false) }
    var filterCategory by remember { mutableStateOf("ALL") } // "ALL", "Lab Report", "Prescription", "X-Ray Diagnosis", "Other"
    var activeViewerDoc by remember { mutableStateOf<MedicalDocument?>(null) }

    // Form inputs state
    var docTitle by remember { mutableStateOf("") }
    var docType by remember { mutableStateOf("Lab Report") }
    var docSelectionApptId by remember { mutableStateOf<Int?>(null) }
    var docDetails by remember { mutableStateOf("") }

    val filteredDocs = documents.filter { doc ->
        filterCategory == "ALL" || doc.documentType == filterCategory
    }

    val formatter = remember { SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = tr(isTh, "Medical Document Vault", "คลังเก็บสาระสำคัญเวชระเบียนพยาบาล"),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = tr(isTh, "Securely catalog lab and x-ray files", "จัดหมวดหมู่เอกสารยาสามัญ ผลตรวจเลือด ฟิล์มเอ็กซเรย์ ปลอดภัย"),
                        fontSize = 11.sp,
                        color = Color.LightGray
                    )
                }

                IconButton(
                    onClick = { showAddDocDialog = true },
                    modifier = Modifier
                        .background(SaniGreen, CircleShape)
                        .size(36.dp)
                        .testTag("upload_document_button")
                ) {
                    Icon(Icons.Default.CloudUpload, contentDescription = "Upload Document", tint = CozySlateBg)
                }
            }

            // Quick Category select row
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val types = listOf(
                    "ALL" to tr(isTh, "All Dossiers", "เอกสารทั้งหมด"),
                    "Lab Report" to tr(isTh, "Lab Reports", "ผลวิเคราะห์แล็บ"),
                    "Prescription" to tr(isTh, "Prescriptions", "ใบสั่งแพทย์"),
                    "X-Ray Diagnosis" to tr(isTh, "Radiology / X-Ray", "เอ็กซ์เรย์พิกัดฟิล์ม"),
                    "Other" to tr(isTh, "Others", "ประกัน / อื่นๆ")
                )

                types.forEach { (typeVal, name) ->
                    val isSelected = filterCategory == typeVal
                    Box(
                        modifier = Modifier
                            .background(if (isSelected) ContrastAmber else DarkCardBg, RoundedCornerShape(8.dp))
                            .clickable { filterCategory = typeVal }
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = name,
                            color = if (isSelected) CozySlateBg else Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // File items grid listing
            if (filteredDocs.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.FolderOpen,
                            contentDescription = "Empty Folder",
                            tint = Color.LightGray.copy(alpha = 0.4f),
                            modifier = Modifier.size(64.dp)
                        )
                        Text(
                            text = tr(isTh, "No Medical documents uploaded in this category.", "ยังไม่ได้มีการบันทึกไฟล์เวชระเบียนโรงพยาบาลในกลุ่มนี้"),
                            color = Color.LightGray,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    items(filteredDocs) { doc ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = DarkCardBg),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { activeViewerDoc = doc }
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .background(PrimaryAccent.copy(alpha = 0.15f), CircleShape)
                                                .size(34.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            val iconDoc = when (doc.documentType) {
                                                "Lab Report" -> Icons.Default.Analytics
                                                "Prescription" -> Icons.Default.Medication
                                                "X-Ray Diagnosis" -> Icons.Default.Visibility
                                                else -> Icons.Default.Description
                                            }
                                            Icon(iconDoc, contentDescription = "Type", tint = PrimaryAccent, modifier = Modifier.size(16.dp))
                                        }

                                        Column {
                                            Text(
                                                text = doc.title,
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = doc.documentType,
                                                color = Color.LightGray,
                                                fontSize = 11.sp
                                            )
                                        }
                                    }

                                    IconButton(onClick = { viewModel.deleteMedicalDocument(doc) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.LightGray)
                                    }
                                }

                                Divider(color = CozySlate.copy(alpha = 0.5f))

                                // Document summary AI extraction
                                doc.extractedDetails?.let { detail ->
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(CozySlate, RoundedCornerShape(6.dp))
                                            .padding(10.dp)
                                    ) {
                                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                Icon(Icons.Default.AutoAwesome, contentDescription = "AI", tint = SaniGreen, modifier = Modifier.size(12.dp))
                                                Text(
                                                    text = tr(isTh, "Smart OCR Data Abstracted:", "ข้อมูลเวชระเบียนที่ AI ตรวจวิเคราะห์:"),
                                                    fontSize = 10.sp,
                                                    color = SaniGreen,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                            Text(
                                                text = detail,
                                                color = Color.LightGray,
                                                fontSize = 11.sp
                                            )
                                        }
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = tr(isTh, "File URI: ${doc.filePath}", "ที่อยู่ประวัติ: ${doc.filePath}"),
                                        fontSize = 9.sp,
                                        color = Color.LightGray,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Box(
                                        modifier = Modifier
                                            .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "${doc.fileSizeKb} KB",
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 9.sp,
                                            color = ContrastAmber,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDocDialog) {
        Dialog(onDismissRequest = { showAddDocDialog = false }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = CozySlate),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = tr(isTh, "Upload Medical Record File", "บันทึกนำฝากไฟล์รายงานเวชระเบียน"),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color.White
                        )
                        IconButton(onClick = { showAddDocDialog = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.LightGray)
                        }
                    }

                    Divider(color = Color.LightGray.copy(alpha = 0.2f))

                    Text(tr(isTh, "Document Title *", "หัวข้อเอกสารทางการแพทย์ *"), color = ContrastAmber, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = docTitle,
                        onValueChange = { docTitle = it },
                        modifier = Modifier.fillMaxWidth().testTag("add_doc_title_input"),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )

                    Text(tr(isTh, "Document Classification Category *", "จัดกลุ่มประเภทเอกสารคลังนัด *"), color = ContrastAmber, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf("Lab Report", "Prescription", "X-Ray Diagnosis", "Other").forEach { type ->
                            val active = docType == type
                            Box(
                                modifier = Modifier
                                    .background(if (active) SaniGreen else DarkCardBg, RoundedCornerShape(6.dp))
                                    .clickable { docType = type }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = type,
                                    fontSize = 10.sp,
                                    color = if (active) CozySlateBg else Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Text(tr(isTh, "Link to specific doctor appointment?", "เชื่อมปักนัดตรวจที่เลือกของคุรหมอไหม?"), color = Color.White, fontSize = 12.sp)
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        // Option for null
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(if (docSelectionApptId == null) SaniGreen.copy(alpha = 0.2f) else Color.Transparent)
                                .clickable { docSelectionApptId = null }
                                .padding(8.dp)
                        ) {
                            RadioButton(selected = docSelectionApptId == null, onClick = { docSelectionApptId = null })
                            Text(tr(isTh, "No Link (General Medical Library)", "ไม่เชื่อมโยง (เป็นห้องสมุดเอกสารทั่วไป)"), color = Color.White, fontSize = 11.sp)
                        }

                        appointments.forEach { appt ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                        // Highlight selected
                                    .background(if (docSelectionApptId == appt.id) SaniGreen.copy(alpha = 0.2f) else Color.Transparent)
                                    .clickable { docSelectionApptId = appt.id }
                                    .padding(8.dp)
                            ) {
                                RadioButton(selected = docSelectionApptId == appt.id, onClick = { docSelectionApptId = appt.id })
                                Text(
                                    text = "${appt.doctorName} (${appt.hospitalName})",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    Text(tr(isTh, "Abstract Core Details or OCR Text", "บันทึกข้อสรุปทางการรักษาย่อ หรือข้อมูลยา"), color = Color.LightGray, fontSize = 11.sp)
                    OutlinedTextField(
                        value = docDetails,
                        onValueChange = { docDetails = it },
                        modifier = Modifier.fillMaxWidth().height(80.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )

                    Button(
                        onClick = {
                            if (docTitle.isNotBlank()) {
                                viewModel.addMedicalDocument(
                                    title = docTitle,
                                    type = docType,
                                    appointmentId = docSelectionApptId,
                                    detail = docDetails
                                )
                                showAddDocDialog = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryAccent),
                        modifier = Modifier.fillMaxWidth().testTag("add_doc_submit_button")
                    ) {
                        Text(tr(isTh, "Secure to Cloud Vault", "ล็อคนำเซฟข้อมูลเวชศาสตร์ด่วน"), color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    activeViewerDoc?.let { doc ->
        androidx.compose.ui.window.Dialog(onDismissRequest = { activeViewerDoc = null }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = CozySlate),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            ) {
                var zoomLevel by remember { mutableStateOf(1f) }
                var isNightVision by remember { mutableStateOf(false) }
                var currentPage by remember { mutableStateOf(1) }

                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = doc.title,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "Category: ${doc.documentType}",
                                color = Color.LightGray,
                                fontSize = 11.sp
                            )
                        }
                        IconButton(onClick = { activeViewerDoc = null }) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.LightGray)
                        }
                    }

                    Divider(color = Color.LightGray.copy(alpha = 0.2f))

                    // Simulated Viewer Canvas Viewport
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp)
                            .background(if (isNightVision) Color.Black else Color.White, RoundedCornerShape(8.dp))
                            .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            if (doc.documentType == "X-Ray Diagnosis") {
                                // Draw a high contrast skeleton image simulation using custom canvas
                                androidx.compose.foundation.Canvas(modifier = Modifier.size(120.dp)) {
                                    val canvasWidth = size.width
                                    val canvasHeight = size.height
                                    val strokeColor = if (isNightVision) Color(0xFF00FF00) else Color(0xFF222222)
                                    // Custom visual render representing an X-ray (lungs / spine ribs)
                                    drawCircle(
                                        color = strokeColor.copy(alpha = 0.15f),
                                        radius = canvasWidth * 0.4f * zoomLevel
                                    )
                                    // Spine vertical line
                                    drawLine(
                                        color = strokeColor,
                                        start = androidx.compose.ui.geometry.Offset(canvasWidth / 2, canvasHeight * 0.1f),
                                        end = androidx.compose.ui.geometry.Offset(canvasWidth / 2, canvasHeight * 0.9f),
                                        strokeWidth = 6f
                                    )
                                    // Rib cages arcs
                                    for (i in 2..5) {
                                        val y = canvasHeight * 0.16f * i
                                        drawArc(
                                            color = strokeColor,
                                            startAngle = 180f,
                                            sweepAngle = 100f,
                                            useCenter = false,
                                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 4f),
                                            size = androidx.compose.ui.geometry.Size(canvasWidth * 0.35f * zoomLevel, canvasHeight * 0.15f * zoomLevel),
                                            topLeft = androidx.compose.ui.geometry.Offset(canvasWidth * 0.1f, y)
                                        )
                                        drawArc(
                                            color = strokeColor,
                                            startAngle = 260f,
                                            sweepAngle = 100f,
                                            useCenter = false,
                                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 4f),
                                            size = androidx.compose.ui.geometry.Size(canvasWidth * 0.35f * zoomLevel, canvasHeight * 0.15f * zoomLevel),
                                            topLeft = androidx.compose.ui.geometry.Offset(canvasWidth * 0.55f, y)
                                        )
                                    }
                                }
                                Text(
                                    text = tr(isTh, "HIGH-CONTRAST CHEST RADIOGRAPH (ZOOM: ${String.format(Locale.getDefault(), "%.1f", zoomLevel)}x)", "ภาพถ่ายรังสีหน้าอกสเปกตรัมสูง (ซูม: ${String.format(Locale.getDefault(), "%.1f", zoomLevel)}เท่า)"),
                                    color = if (isNightVision) Color(0xFF00FF00) else Color.DarkGray,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            } else {
                                // PDF / Document sheet style layout render
                                Icon(
                                    imageVector = if (doc.documentType == "Prescription") Icons.Default.MedicalServices else Icons.Default.Description,
                                    contentDescription = "Doc",
                                    tint = if (isNightVision) Color(0xFF00FF00) else PrimaryAccent,
                                    modifier = Modifier.size(48.dp)
                                )
                                Text(
                                    text = doc.title,
                                    color = if (isNightVision) Color(0xFF00FF00) else Color.Black,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = tr(
                                        isTh,
                                        "REPORT PAGE $currentPage OF 3\nDigital SHA-256 Verified Seal",
                                        "รายงานหน้า $currentPage จากทั้งหมด 3 หน้า\nลงเครื่องหมายดิจิทัลยืนยันความถูกต้อง"
                                    ),
                                    color = if (isNightVision) Color(0xFF00FF00) else Color.Gray,
                                    fontSize = 10.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    // ZOOM & VIEW CONTROLS ROW
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = tr(isTh, "Zoom Focus: ${String.format(Locale.getDefault(), "%.1f", zoomLevel)}x", "ระยะซูมภาพ: ${String.format(Locale.getDefault(), "%.1f", zoomLevel)}เท่า"),
                            color = Color.LightGray,
                            fontSize = 11.sp
                        )
                        Slider(
                            value = zoomLevel,
                            onValueChange = { zoomLevel = it },
                            valueRange = 1f..3f,
                            modifier = Modifier.width(140.dp),
                            colors = SliderDefaults.colors(
                                thumbColor = ContrastAmber,
                                activeTrackColor = ContrastAmber.copy(alpha = 0.5f)
                            )
                        )
                    }

                    // INTERACTIVE OPTIONS FOR PARAMEDIC/CARE VIEW
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Night Mode Toggler (High Contrast Color Inversion)
                        Button(
                            onClick = { isNightVision = !isNightVision },
                            colors = ButtonDefaults.buttonColors(containerColor = if (isNightVision) Color.DarkGray else CozySlateBg),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Visibility, contentDescription = "Visual Filter", tint = Color.White, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(tr(isTh, "Invert Filter", "สลับโทนสี"), color = Color.White, fontSize = 10.sp)
                        }

                        // Paginated PDF Simulation Controller
                        Button(
                            onClick = {
                                currentPage = if (currentPage == 3) 1 else currentPage + 1
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CozySlateBg),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.MenuBook, contentDescription = "Next Page", tint = Color.White, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(tr(isTh, "Next (PDF)", "หน้าถัดไป (PDF)"), color = Color.White, fontSize = 10.sp)
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(tr(isTh, "Extracted Metadata Stream:", "ข้อมูลเวชระเบียนสืบค้น:"), color = ContrastAmber, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text(
                                text = doc.extractedDetails ?: "No embedded summaries captured.",
                                color = Color.LightGray,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// TAB 3: VISITS & DIETARY HISTORIC TIMELINE (Timeline)
// ==========================================
@Composable
fun MedicalTimelineTab(
    viewModel: FamilyCareViewModel,
    isTh: Boolean,
    appointments: List<HospitalAppointment>,
    viewModelLogs: FamilyCareViewModel
) {
    // Collect active elements from database to form a beautifully combined history timeline feed
    val medLogsToday by viewModelLogs.medicationLogsToday.collectAsState()
    val sosLogs by viewModelLogs.allSOSLogs.collectAsState()

    // Create custom history models
    data class TimelineItem(
        val time: Long,
        val category: String, // "APPOINTMENT", "MEDICATION_TAKEN", "HEALTH_REPORT_ALERT"
        val header: String,
        val subheader: String,
        val details: String,
        val tagColor: Color
    )

    val mergedTimeline = remember(appointments, medLogsToday, sosLogs) {
        val list = mutableListOf<TimelineItem>()

        // 1. Add hospital appointments
        appointments.forEach { appt ->
            val timestamp = appt.appointmentTimestamp
            val isUpcoming = timestamp >= System.currentTimeMillis()
            list.add(
                TimelineItem(
                    time = timestamp,
                    category = "APPOINTMENT",
                    header = if (isUpcoming) "[UPCOMING ROUTE] nนัดแพทย์" else "[PAST CHECK] ประวัติเคยตรวจพบคลิก",
                    subheader = "${appt.doctorName} - ${appt.hospitalName}",
                    details = "${appt.department}. Notes: ${appt.notes}",
                    tagColor = if (isUpcoming) ContrastAmber else SaniGreen
                )
            )
        }

        // 2. Add medication checklist intake events
        medLogsToday.forEach { log ->
            list.add(
                TimelineItem(
                    time = log.intakeTimestamp,
                    category = "MEDICATION_TAKEN",
                    header = "[MED WORK] ทานยารายวัน",
                    subheader = "${log.medName} [Intake compliance mark]",
                    details = "Taken check successfully. ${log.notes}",
                    tagColor = SaniGreen
                )
            )
        }

        // 3. Add emergency alerts
        sosLogs.forEach { alert ->
            list.add(
                TimelineItem(
                    time = alert.timestamp,
                    category = "HEALTH_REPORT_ALERT",
                    header = tr(isTh, "[CRITICAL WARNING] SOS ALERT TRIGGERED", "[สัญญาณเตือนด่วน] ระบบจับกู้ชีพพบวิกฤต"),
                    subheader = alert.alertMessage,
                    details = "Registered coords: [${alert.latitude}, ${alert.longitude}]. Resolution status: ${if (alert.isResolved) "RESOLVED BY ${alert.resolvedBy}" else "STILL ACTIVE"}",
                    tagColor = Color.Red
                )
            )
        }

        // Sort descending
        list.sortedByDescending { it.time }
    }

    val formatter = remember { SimpleDateFormat("EEE, d MMM yyyy, hh:mm a", Locale.getDefault()) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Column {
                Text(
                    text = tr(isTh, "Multi-Channel Medical Timeline", "ไทม์ไลน์ภาพรวมการประเมินรักษาสะสม"),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = tr(isTh, "Chronological record of doctor visits, drug takes and alerts", "เส้นเวลาประสานยารายวัน บันทึกการพบแพทย์ และระบบแจ้งพิกัดสัญญาณกู้ชีพ"),
                    fontSize = 11.sp,
                    color = Color.LightGray
                )
            }

            if (mergedTimeline.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = tr(isTh, "No historical traces found yet.", "ยังไม่มีหลักการบันทึกประวัติการรักษาในคลาวด์"),
                        color = Color.LightGray
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    items(mergedTimeline) { item ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Column representing the vertical timeline bullet line-link
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.width(20.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .background(item.tagColor, CircleShape)
                                )
                                Box(
                                    modifier = Modifier
                                        .width(2.dp)
                                        .height(100.dp)
                                        .background(
                                            Brush.verticalGradient(
                                                listOf(item.tagColor, Color.Transparent)
                                            )
                                        )
                                )
                            }

                            // Dynamic timeline data block card
                            Card(
                                colors = CardDefaults.cardColors(containerColor = DarkCardBg),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(bottom = 12.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = item.header,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = item.tagColor
                                        )
                                        Text(
                                            text = formatter.format(Date(item.time)),
                                            fontSize = 10.sp,
                                            color = Color.LightGray
                                        )
                                    }

                                    Text(
                                        text = item.subheader,
                                        color = Color.White,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 13.sp
                                    )

                                    Text(
                                        text = item.details,
                                        color = Color.LightGray.copy(alpha = 0.8f),
                                        fontSize = 11.sp,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// TAB 4: EDITABLE EMERGENCY CARD (Emergency Card)
// ==========================================
@Composable
fun EmergencyMedicalCardTab(
    viewModel: FamilyCareViewModel,
    isTh: Boolean,
    emergencyProfile: EmergencyProfile?
) {
    var editMode by remember { mutableStateOf(false) }

    // Backup states for editable fields
    var bloodType by remember { mutableStateOf("") }
    var allergies by remember { mutableStateOf("") }
    var chronicConditions by remember { mutableStateOf("") }
    var regularPrescriptions by remember { mutableStateOf("") }
    var emergencyContactName by remember { mutableStateOf("") }
    var emergencyContactPhone by remember { mutableStateOf("") }
    var hospitalPreference by remember { mutableStateOf("") }
    var insuranceDetails by remember { mutableStateOf("") }
    var additionalNotes by remember { mutableStateOf("") }

    // Init values once from room
    LaunchedEffect(emergencyProfile) {
        emergencyProfile?.let { ep ->
            bloodType = ep.bloodType
            allergies = ep.allergies
            chronicConditions = ep.chronicConditions
            regularPrescriptions = ep.regularPrescriptions
            emergencyContactName = ep.emergencyContactName
            emergencyContactPhone = ep.emergencyContactPhone
            hospitalPreference = ep.hospitalPreference
            insuranceDetails = ep.insuranceDetails
            additionalNotes = ep.additionalNotes
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = tr(isTh, "Emergency Medical Profile", "บัตรข้อมูลเวชกรและพิกัดฉุกเฉินผู้สูงอายุ"),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = tr(isTh, "Vital statistics always accessible to paramedics", "ข้อมูลสำคัญประจำตัวเพื่อนำทางแพทย์สนามและรถกู้ภัยกู้ชีพ"),
                        fontSize = 11.sp,
                        color = Color.LightGray
                    )
                }

                Button(
                    onClick = {
                        if (editMode) {
                            // Save update to Database
                            viewModel.updateEmergencyMedicalCard(
                                blood = bloodType,
                                allergies = allergies,
                                chronic = chronicConditions,
                                prescription = regularPrescriptions,
                                contactName = emergencyContactName,
                                contactPhone = emergencyContactPhone,
                                hospital = hospitalPreference,
                                insurance = insuranceDetails,
                                notes = additionalNotes
                            )
                        }
                        editMode = !editMode
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = if (editMode) SaniGreen else ContrastAmber),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = if (editMode) tr(isTh, "Save Card", "บันทึกฟอร์ม") else tr(isTh, "Edit Profile", "แก้ไขข้อมูล"),
                        color = CozySlateBg,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Striking Emergency visual banner
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFC30000)),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = tr(isTh, "PARAMEDIC RESCUE CRITICAL INFO", "ข้อมูลจำเพาะกู้ชีพแพทย์สนามด่วน"),
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 11.sp
                        )

                        Icon(
                            Icons.Default.HealthAndSafety,
                            contentDescription = "Medical",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Blood Highlight Badge
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .background(Color.White, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = bloodType.ifBlank { "O+" },
                                color = Color(0xFFC30000),
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Black
                            )
                        }

                        Column {
                            Text(
                                text = "SOMPONG SOMDEE (Grandpa)",
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp
                            )
                            Text(
                                text = "Emergency Contact: $emergencyContactName • $emergencyContactPhone",
                                color = Color.White.copy(alpha = 0.9f),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Form Fields
            if (editMode) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(tr(isTh, "Blood Type", "หมู่กรณีกรุ๊ปเลือด"), color = ContrastAmber, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = bloodType,
                        onValueChange = { bloodType = it },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )

                    Text(tr(isTh, "Allergies (Severe Warning)", "อาการยาแพ้และสิ่งกระตุ้นรุนแรง"), color = ContrastAmber, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = allergies,
                        onValueChange = { allergies = it },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )

                    Text(tr(isTh, "Chronic Medical Conditions", "โรคอาการประจำตัวเรื้อรังสะสม"), color = ContrastAmber, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = chronicConditions,
                        onValueChange = { chronicConditions = it },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )

                    Text(tr(isTh, "Regular Medicines / Daily", "ยาสามัญกระตุ้นความคลุมเครือรายวัน"), color = ContrastAmber, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = regularPrescriptions,
                        onValueChange = { regularPrescriptions = it },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )

                    Text(tr(isTh, "Primary Emergency Coordinator Name", "ชื่อผู้ประสานพิกัดฉุกเฉินและฟอกตัวแทน"), color = ContrastAmber, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = emergencyContactName,
                        onValueChange = { emergencyContactName = it },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )

                    Text(tr(isTh, "Coordinator Contact Phone Number", "เบอร์โทรผู้ดูแลฉุกเฉินกู้ภัย"), color = ContrastAmber, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = emergencyContactPhone,
                        onValueChange = { emergencyContactPhone = it },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )

                    Text(tr(isTh, "Preferred Medical Institution", "สถาบันการแพทย์ / โรงพยาบาลปักนัดหลัก"), color = ContrastAmber, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = hospitalPreference,
                        onValueChange = { hospitalPreference = it },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )

                    Text(tr(isTh, "Active Medical Insurance & Policy #", "ประกันภัยโรคนอน และกรมธรรม์ที่เปิดใช้"), color = ContrastAmber, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = insuranceDetails,
                        onValueChange = { insuranceDetails = it },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )

                    Text(tr(isTh, "Special paramedic cautions", "คำสั่งการช่วยเหลือด่วนพิเศษเฉพาะวิกฤติ"), color = Color.LightGray, fontSize = 11.sp)
                    OutlinedTextField(
                        value = additionalNotes,
                        onValueChange = { additionalNotes = it },
                        modifier = Modifier.fillMaxWidth().height(100.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )
                }
            } else {
                // Read-Only Clean Medical report card structure layouts
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    val staticCards = listOf(
                        Triple(Icons.Default.Warning, tr(isTh, "SEVERE ALLERGIES", "ประวัติอาการแพ้ยา / สารรุนแรง"), allergies),
                        Triple(Icons.Default.ListAlt, tr(isTh, "CHRONIC DISEASES", "โรคอาการประจำตัวเรื้อรังสะสม"), chronicConditions),
                        Triple(Icons.Default.Vaccines, tr(isTh, "DAILY MANDATED PRESCRIPTIONS", "ตารางยาสำคัญประคองชีพ"), regularPrescriptions),
                        Triple(Icons.Default.Phone, tr(isTh, "CARE PROMOTER CONTACT", "ฝ่ายประสานงานสิทธิ์ผู้ป่วย"), "$emergencyContactName ($emergencyContactPhone)"),
                        Triple(Icons.Default.LocalHospital, tr(isTh, "PRIMARY DESTINATION HOSPITAL", "พิกัดส่งตัวตรวจโรงพยาบาล"), hospitalPreference),
                        Triple(Icons.Default.SafetyCheck, tr(isTh, "ACTIVE HEALTH INSURANCE cover", "เอกสารความคุ้มครองสิทธิ์ประกัน"), insuranceDetails)
                    )

                    staticCards.forEach { (icon, label, value) ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = DarkCardBg),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(icon, contentDescription = label, tint = ContrastAmber, modifier = Modifier.size(16.dp))
                                    Text(
                                        text = label,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = ContrastAmber
                                    )
                                }
                                Text(
                                    text = value.ifBlank { tr(isTh, "Not specified", "ไม่ได้ระบุ") },
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    // Special Notes card
                    Card(
                        colors = CardDefaults.cardColors(containerColor = CozySlate),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color.Red.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Icon(Icons.Default.Announcement, contentDescription = "Special Notes", tint = Color.Red)
                                Text(
                                    text = tr(isTh, "CRITICAL TREATMENT WARNINGS / NOTES", "วิเคราะห์ด่วนข้อจำกัดป้องปัดอันตราย"),
                                    color = Color.Red,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 11.sp
                                )
                            }
                            Text(
                                text = additionalNotes.ifBlank { tr(isTh, "No paramedic notes declared.", "ไม่มีเจตนารมณ์แพทย์ตรวจพิเศษอื่นใด") },
                                color = Color.White,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
