package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.EmergencyLog
import com.example.data.model.Medication
import com.example.data.model.VoiceMessage
import com.example.data.model.WatchHealthData
import com.example.ui.FamilyCareViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

// Colors matching the Amber-Red Cyber Clean Elderly Visibility Theme
val PrimaryAccent = Color(0xFFFF4511)
val PrimaryDeep = Color(0xFFC32A00)
val CozySlateBg = Color(0xFF0F1524)
val DarkCardBg = Color(0xFF1B243B)
val SaniGreen = Color(0xFF2EC4B6)
val SoftLightBg = Color(0xFFF7F9FC)
val ContrastAmber = Color(0xFFFFB703)

// Localization helper function
fun tr(isTh: Boolean, en: String, th: String): String {
    return if (isTh) th else en
}

@Composable
fun AppNavigator(viewModel: FamilyCareViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val activeNotif by viewModel.activeNotification.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CozySlateBg)
    ) {
        when (currentScreen) {
            "Splash" -> SplashScreen(viewModel)
            "Login" -> LoginScreen(viewModel)
            "Register" -> RegisterScreen(viewModel)
            "GroupSetup" -> GroupSetupScreen(viewModel)
            "ElderDashboard" -> ElderDashboardScreen(viewModel)
            "FamilyDashboard" -> FamilyDashboardScreen(viewModel)
            "HospitalAppointments" -> HospitalAppointmentsScreen(viewModel)
            "Settings" -> SettingsScreen(viewModel)
        }

        // Live notification banner Overlay
        activeNotif?.let { message ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .padding(horizontal = 16.dp, vertical = 40.dp)
                    .shadow(12.dp, RoundedCornerShape(12.dp))
                    .background(PrimaryAccent, RoundedCornerShape(12.dp))
                    .border(2.dp, ContrastAmber, RoundedCornerShape(12.dp))
                    .clickable { viewModel.dismissNotification() }
                    .padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        Icons.Default.Notifications,
                        contentDescription = "Alert",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                    Text(
                        text = message,
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = { viewModel.dismissNotification() },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun SplashScreen(viewModel: FamilyCareViewModel) {
    val isTh by viewModel.isThaiLanguage.collectAsState()

    LaunchedEffect(Unit) {
        delay(2500)
        // Transition based on if we have a default log in session loaded
        if (viewModel.currentUser.value != null) {
            if (viewModel.currentUser.value?.role == "Elderly") {
                viewModel.navigateTo("ElderDashboard")
            } else {
                viewModel.navigateTo("FamilyDashboard")
            }
        } else {
            viewModel.navigateTo("Login")
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(CozySlateBg, Color(0xFF1E293B))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .background(Color(0x11FF5722), CircleShape)
                    .border(3.dp, PrimaryAccent, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Security,
                    contentDescription = "Family Care Link Logo",
                    tint = PrimaryAccent,
                    modifier = Modifier.size(56.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = tr(isTh, "FAMILY CARE LINK", "แฟมิลี่แคร์ ลิงก์"),
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    letterSpacing = 2.sp
                )
            )

            Text(
                text = tr(isTh, "Emergency Companion & Vital Connect", "เพื่อนคู่คิดเพื่อความปลอดภัยและเชื่อมต่อครอบครัว"),
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = Color.LightGray.copy(alpha = 0.8f)
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 24.dp)
            )

            CircularProgressIndicator(
                color = PrimaryAccent,
                strokeWidth = 3.dp,
                modifier = Modifier.padding(top = 24.dp)
            )
        }
    }
}

@Composable
fun LoginScreen(viewModel: FamilyCareViewModel) {
    val isTh by viewModel.isThaiLanguage.collectAsState()
    var email by remember { mutableStateOf("sompong@care.com") }
    var password by remember { mutableStateOf("123456") }
    var selectedRole by remember { mutableStateOf("Elderly") } // "Elderly" or "Family Member"

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CozySlateBg)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        // Floating Language Switcher
        TextButton(
            onClick = { viewModel.toggleLanguage() },
            modifier = Modifier.align(Alignment.TopEnd).padding(top = 8.dp)
        ) {
            Text(
                text = if (isTh) "ENGLISH" else "ภาษาไทย",
                color = ContrastAmber,
                fontWeight = FontWeight.Black,
                fontSize = 15.sp
            )
        }

        LazyColumn(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            item {
                Icon(
                    Icons.Default.Security,
                    contentDescription = "Lock",
                    tint = ContrastAmber,
                    modifier = Modifier.size(64.dp)
                )
                Text(
                    text = tr(isTh, "Log In", "เข้าสู่ระบบ"),
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    ),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = tr(isTh, "Access Family Care Support Portal", "เข้าสู่ระบบการดูแลเชื่อมโยงเพื่อครอบครัวของคุณ"),
                    color = Color.LightGray,
                    fontSize = 14.sp
                )
            }

            item {
                Spacer(modifier = Modifier.height(12.dp))
                // Clean Elder Segment Control
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF1E293B), RoundedCornerShape(12.dp))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val roles = listOf("Elderly", "Family Member")
                    roles.forEach { role ->
                        val isSelected = selectedRole == role
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) PrimaryAccent else Color.Transparent)
                                .clickable {
                                    selectedRole = role
                                    if (role == "Elderly") {
                                        email = "sompong@care.com"
                                    } else {
                                        email = "suda@family.com"
                                    }
                                }
                                .wrapContentSize(Alignment.Center)
                        ) {
                            Text(
                                text = tr(isTh, role, if (role == "Elderly") "ผู้สูงอายุ" else "สมาชิกในครอบครัว"),
                                color = if (isSelected) Color.White else Color.LightGray,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }

            item {
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text(tr(isTh, "Email Address", "ที่อยู่อีเมล"), color = Color.LightGray) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = PrimaryAccent,
                        unfocusedBorderColor = Color.Gray
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("email_input"),
                    singleLine = true
                )
            }

            item {
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text(tr(isTh, "Password", "รหัสผ่าน"), color = Color.LightGray) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = PrimaryAccent,
                        unfocusedBorderColor = Color.Gray
                    ),
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            item {
                Button(
                    onClick = { viewModel.handleLogin(email, selectedRole) },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryAccent),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("submit_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(tr(isTh, "Secure Log In", "เข้าสู่ระบบอย่างปลอดภัย"), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }

            item {
                TextButton(
                    onClick = { viewModel.navigateTo("Register") }
                ) {
                    Text(tr(isTh, "Don't have an account? Sign Up", "ยังไม่มีบัญชีใช่หรือไม่? ลงทะเบียนใหม่"), color = ContrastAmber, fontSize = 16.sp)
                }
            }
        }
    }
}

@Composable
fun RegisterScreen(viewModel: FamilyCareViewModel) {
    val isTh by viewModel.isThaiLanguage.collectAsState()
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf("Elderly") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CozySlateBg)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        // Floating Language Switcher
        TextButton(
            onClick = { viewModel.toggleLanguage() },
            modifier = Modifier.align(Alignment.TopEnd).padding(top = 8.dp)
        ) {
            Text(
                text = if (isTh) "ENGLISH" else "ภาษาไทย",
                color = ContrastAmber,
                fontWeight = FontWeight.Black,
                fontSize = 15.sp
            )
        }

        LazyColumn(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            item {
                Text(
                    text = tr(isTh, "Create Account", "สร้างบัญชีใหม่"),
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
                Text(
                    text = tr(isTh, "Sign up to link with your relatives", "สมัครใช้งานเพื่อเชื่อมต่อกับกลุ่มเครือญาติของคุณ"),
                    color = Color.LightGray,
                    fontSize = 14.sp
                )
            }

            item {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(tr(isTh, "Full Name", "ชื่อ-นามสกุลจริง"), color = Color.LightGray) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = PrimaryAccent,
                        unfocusedBorderColor = Color.Gray
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            item {
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text(tr(isTh, "Email Address", "ที่อยู่อีเมล"), color = Color.LightGray) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = PrimaryAccent,
                        unfocusedBorderColor = Color.Gray
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            item {
                Text(
                    text = tr(isTh, "Select Account Role", "ระบุบทบาทผู้สมัคร"),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val roles = listOf("Elderly", "Family Member", "Caregiver")
                    roles.forEach { role ->
                        val isSelected = selectedRole == role
                        val roleText = when (role) {
                            "Elderly" -> tr(isTh, "Elderly", "ผู้สูงอายุ")
                            "Family Member" -> tr(isTh, "Family Member", "คนในครอบครัว")
                            else -> tr(isTh, "Caregiver", "ผู้ดูแลทั่วไป")
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) PrimaryAccent else Color(0xFF1E293B))
                                .clickable { selectedRole = role }
                                .wrapContentSize(Alignment.Center)
                        ) {
                            Text(
                                text = roleText,
                                color = if (isSelected) Color.White else Color.LightGray,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }

            item {
                Button(
                    onClick = {
                        if (name.isNotBlank() && email.isNotBlank()) {
                            viewModel.handleRegister(name, email, selectedRole)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryAccent),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .padding(top = 16.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(tr(isTh, "Register & Next", "ลงทะเบียนและถัดไป"), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }

            item {
                TextButton(onClick = { viewModel.navigateTo("Login") }) {
                    Text(tr(isTh, "Already registered? Secure Log In", "ลงทะเบียนแล้ว? เข้าสู่ระบบอย่างปลอดภัย"), color = ContrastAmber, fontSize = 16.sp)
                }
            }
        }
    }
}

@Composable
fun GroupSetupScreen(viewModel: FamilyCareViewModel) {
    val isTh by viewModel.isThaiLanguage.collectAsState()
    var groupCodeInput by remember { mutableStateOf("") }
    var groupNameInput by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CozySlateBg)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        // Floating Language Switcher
        TextButton(
            onClick = { viewModel.toggleLanguage() },
            modifier = Modifier.align(Alignment.TopEnd).padding(top = 8.dp)
        ) {
            Text(
                text = if (isTh) "ENGLISH" else "ภาษาไทย",
                color = ContrastAmber,
                fontWeight = FontWeight.Black,
                fontSize = 15.sp
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = tr(isTh, "Family Group Connect", "ตั้งกลุ่มเชื่อมต่อครอบครัว"),
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )

            Text(
                text = tr(isTh, 
                    "To sync emergency alerts, join an existing group with a code or create a new family network group.",
                    "เพื่อเชื่อมต่อระบบแจ้งเตือนฉุกเฉิน กรุณาเข้าร่วมกลุ่มเดิมผ่านรหัสเชิญ หรือสร้างกลุ่มครอบครัวชุดใหม่ขึ้นมา"
                ),
                color = Color.LightGray,
                textAlign = TextAlign.Center,
                fontSize = 15.sp,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            // OPTION 1: JOIN
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1B243B)),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = tr(isTh, "Join Existing Group", "เข้าร่วมกลุ่มที่มีอยู่เดิม"),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    OutlinedTextField(
                        value = groupCodeInput,
                        onValueChange = { groupCodeInput = it },
                        placeholder = { Text(tr(isTh, "E.g. FAM-Care-419", "เช่น FAM-Care-419"), color = Color.Gray) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = ContrastAmber
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Button(
                        onClick = { viewModel.handleGroupJoinOrCreate(isCreate = false, groupCodeInput) },
                        colors = ButtonDefaults.buttonColors(containerColor = ContrastAmber),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(tr(isTh, "Verify & Connect", "ตรวจสอบ & เชื่อมต่อกลุ่ม"), color = CozySlateBg, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Text(tr(isTh, "— OR —", "— หรือ —"), color = Color.Gray, fontSize = 14.sp)

            // OPTION 2: CREATE
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1B243B)),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = tr(isTh, "Create New Family Circle", "จัดตั้งวงล้อมครอบครัวใหม่"),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    OutlinedTextField(
                        value = groupNameInput,
                        onValueChange = { groupNameInput = it },
                        placeholder = { Text(tr(isTh, "E.g. Sompong Home Care", "เช่น ส้มปอง โฮมแคร์"), color = Color.Gray) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = PrimaryAccent
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Button(
                        onClick = {
                            if (groupNameInput.isNotBlank()) {
                                viewModel.handleGroupJoinOrCreate(isCreate = true, groupNameInput)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryAccent),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(tr(isTh, "Create & Launch Group", "จัดตั้งกลุ่มครอบครัวและเปิดใช้งาน"), color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ElderDashboardScreen(viewModel: FamilyCareViewModel) {
    val isTh by viewModel.isThaiLanguage.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    val isRecordingVoice by viewModel.isRecordingVoice.collectAsState()
    val isFallCountingDown by viewModel.isFallCountingDown.collectAsState()
    val fallCountSec by viewModel.fallCountdownSeconds.collectAsState()
    val medications by viewModel.medications.collectAsState()
    val medLogsToday by viewModel.medicationLogsToday.collectAsState()
    val userName = viewModel.currentUser.collectAsState().value?.name ?: "Senior User"

    val selectedWatchBrand by viewModel.selectedWatchBrand.collectAsState()
    val latestWatchHealth by viewModel.latestWatchHealthData.collectAsState()
    val watchBattery by viewModel.watchBatteryLevel.collectAsState()
    val isSyncingData by viewModel.isSyncingWatchData.collectAsState()
    val lastSyncTime by viewModel.lastWatchSyncTime.collectAsState()

    // Fall Detection Modal
    if (isFallCountingDown) {
        Dialog(onDismissRequest = { }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = PrimaryAccent),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(4.dp, Color.White, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = "Fall Warning",
                        tint = Color.White,
                        modifier = Modifier.size(72.dp).scale(1.2f)
                    )
                    Text(
                        text = tr(isTh, "FALL DETECTED!", "ตรวจพบการล้มกระแทก!"),
                        color = Color.White,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = tr(isTh, "Sending SOS alert to all family members in:", "กำลังแจ้งวิกฤตช่วยเหลือทุกคนในครอบครัวภายใน:"),
                        color = Color.White,
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "$fallCountSec",
                        color = ContrastAmber,
                        fontSize = 64.sp,
                        fontWeight = FontWeight.Black
                    )
                    Button(
                        onClick = { viewModel.cancelFallCountdown() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp)
                            .testTag("cancel_fall"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            tr(isTh, "I AM OKAY (CANCEL)", "ฉันปลอดภัยดี (ยกเลิกแก้อลาร์ม)"),
                            color = PrimaryAccent,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        tr(isTh, "Home Care Connect", "ศูนย์ดูแลครอบครัว"),
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 22.sp
                    )
                },
                actions = {
                    TextButton(onClick = { viewModel.toggleLanguage() }) {
                        Text(
                            text = if (isTh) "ENGLISH" else "ภาษาไทย",
                            color = ContrastAmber,
                            fontWeight = FontWeight.Black,
                            fontSize = 15.sp
                        )
                    }
                    IconButton(onClick = { viewModel.navigateTo("HospitalAppointments") }) {
                        Icon(Icons.Default.LocalHospital, contentDescription = "Hospital Appointments", tint = SaniGreen)
                    }
                    IconButton(onClick = { viewModel.navigateTo("Settings") }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CozySlateBg)
            )
        },
        containerColor = CozySlateBg
    ) { padValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padValues)
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = tr(isTh, "Welcome,", "ขอต้อนรับยินดีต้อนรับ,"),
                            color = Color.LightGray,
                            fontSize = 16.sp
                        )
                        Text(
                            text = userName,
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                    Box(
                        modifier = Modifier
                            .background(Color(0xFF2EC4B6).copy(alpha = 0.2f), RoundedCornerShape(20.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = "Mode", tint = Color(0xFF2EC4B6), modifier = Modifier.size(16.dp))
                            Text(tr(isTh, "Elder Mode Screen", "โหมดผู้สูงอายุ"), color = Color(0xFF2EC4B6), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // SMARTWATCH COMPANION SIMULATOR PANEL
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkCardBg),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Header with status indicator
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Icons.Default.Devices,
                                    contentDescription = "Watch",
                                    tint = ContrastAmber,
                                    modifier = Modifier.size(24.dp)
                                )
                                Text(
                                    text = tr(isTh, "Companion Smart Watch", "แท่นจำลองสวิตช์นาฬิกาอัจฉริยะ"),
                                    color = Color.White,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 16.sp
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .background(SaniGreen.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "🔋 $watchBattery%",
                                    color = SaniGreen,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Connected Watch Info block
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(CozySlateBg.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                                .border(1.dp, Color.Gray.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = tr(isTh, "Connected Watch Brand:", "ยี่ห้อนาฬิกาผู้สวมใส่:"),
                                        color = Color.LightGray,
                                        fontSize = 12.sp
                                    )
                                    Text(
                                        text = selectedWatchBrand,
                                        color = ContrastAmber,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 13.sp
                                    )
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = tr(isTh, "Data Streaming Status:", "สถานะการเชื่อมต่อ:"),
                                        color = Color.LightGray,
                                        fontSize = 12.sp
                                    )
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Box(modifier = Modifier.size(8.dp).background(SaniGreen, CircleShape))
                                        Text(
                                            text = tr(isTh, "Sensors Streaming Live", "วิทยุสตรีมส่งพิกัดตรวจจับสด"),
                                            color = SaniGreen,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }
                        }

                        // Watch Brands Grid selector
                        Text(
                            text = tr(isTh, "Pair & Simulator Another Watch Brand:", "สวมจับคู่จำลองระบบด้วยนาฬิกายี่ห้ออื่น:"),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )

                        // Wrap raw buttons of pairing options
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            val watchBrands = listOf("Wear OS", "Samsung", "Xiaomi", "Huawei", "Amazfit")
                            watchBrands.forEach { shortBrand ->
                                val fullBrandName = when (shortBrand) {
                                    "Samsung" -> "Samsung Galaxy Watch"
                                    "Xiaomi" -> "Xiaomi Watch"
                                    "Huawei" -> "Huawei Watch"
                                    else -> shortBrand
                                }
                                val isSelected = selectedWatchBrand == fullBrandName
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) PrimaryAccent else Color(0xFF1E293B))
                                        .border(
                                            1.dp,
                                            if (isSelected) ContrastAmber else Color.Transparent,
                                            RoundedCornerShape(8.dp)
                                        )
                                        .clickable { viewModel.selectWatchBrand(fullBrandName) }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = shortBrand,
                                        color = if (isSelected) Color.White else Color.LightGray,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Divider(color = Color.Gray.copy(alpha = 0.2f))

                        // Live Physiological Telemetry Grid
                        Text(
                            text = tr(isTh, "Dynamic Biometric Telemetry Sensors:", "ข้อมูลสัญญาณชีพผู้ป่วยสะท้อนตรวจจับได้:"),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )

                        latestWatchHealth?.let { health ->
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // Row 1 item 1: Heart Rate
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = CozySlateBg.copy(alpha = 0.4f)),
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(10.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Icon(
                                                    Icons.Default.Favorite,
                                                    contentDescription = "HR",
                                                    tint = Color.Red,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Text(
                                                    text = tr(isTh, "Heart Rate", "ชีพจรหัวใจ"),
                                                    color = Color.LightGray,
                                                    fontSize = 11.sp
                                                )
                                            }
                                            Text(
                                                text = "${health.heartRate} BPM",
                                                color = Color.White,
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = 16.sp
                                            )
                                        }
                                    }

                                    // Row 1 item 2: SpO2
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = CozySlateBg.copy(alpha = 0.4f)),
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(10.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Icon(
                                                    Icons.Default.Opacity,
                                                    contentDescription = "SpO2",
                                                    tint = Color(0xFF2EA2FF),
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Text(
                                                    text = tr(isTh, "SpO2 (Oxygen)", "ระดับออกซิเจน"),
                                                    color = Color.LightGray,
                                                    fontSize = 11.sp
                                                )
                                            }
                                            Text(
                                                text = "${health.oxygenLevel}%",
                                                color = Color.White,
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = 16.sp
                                            )
                                        }
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // Row 2 item 1: Sleep
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = CozySlateBg.copy(alpha = 0.4f)),
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(10.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Icon(
                                                    Icons.Default.NightsStay,
                                                    contentDescription = "Sleep",
                                                    tint = Color(0xFFA55EEA),
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Text(
                                                    text = tr(isTh, "Sleep Data", "ชั่วโมงกาลหลับ"),
                                                    color = Color.LightGray,
                                                    fontSize = 11.sp
                                                )
                                            }
                                            Text(
                                                text = "${health.sleepDurationHours}H",
                                                color = Color.White,
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = 15.sp
                                            )
                                            Text(
                                                text = health.sleepQuality,
                                                color = SaniGreen,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 9.sp
                                            )
                                        }
                                    }

                                    // Row 2 item 2: Stress
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = CozySlateBg.copy(alpha = 0.4f)),
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(10.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Icon(
                                                    Icons.Default.TrendingUp,
                                                    contentDescription = "Stress",
                                                    tint = ContrastAmber,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Text(
                                                    text = tr(isTh, "Stress Level", "ความเครียดสะสม"),
                                                    color = Color.LightGray,
                                                    fontSize = 11.sp
                                                )
                                            }
                                            Text(
                                                text = "${health.stressLevel}/100",
                                                color = Color.White,
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = 15.sp
                                            )
                                            val stressCat = when {
                                                health.stressLevel > 60 -> tr(isTh, "Severe Stress", "เครียดตึงติง")
                                                health.stressLevel > 35 -> tr(isTh, "Moderate", "ปานกลาง")
                                                else -> tr(isTh, "Relaxed", "ผ่อนคลาย")
                                            }
                                            val stressCol = when {
                                                health.stressLevel > 60 -> Color.Red
                                                health.stressLevel > 35 -> ContrastAmber
                                                else -> SaniGreen
                                            }
                                            Text(
                                                text = stressCat,
                                                color = stressCol,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 9.sp
                                            )
                                        }
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // Row 3 item 1: Blood Pressure
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = CozySlateBg.copy(alpha = 0.4f)),
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(10.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Icon(
                                                    Icons.Default.Bolt,
                                                    contentDescription = "BP",
                                                    tint = Color(0xFFFFB300),
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Text(
                                                    text = tr(isTh, "Blood Pressure", "ความดันโลหิต"),
                                                    color = Color.LightGray,
                                                    fontSize = 11.sp
                                                )
                                            }
                                            Text(
                                                text = "${health.bloodPressureSystolic}/${health.bloodPressureDiastolic}",
                                                color = Color.White,
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = 15.sp
                                            )
                                            Text(
                                                text = tr(isTh, "mmHg (Normal)", "มม.ปรอท (ปกติ)"),
                                                color = SaniGreen,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 9.sp
                                            )
                                        }
                                    }

                                    // Row 3 item 2: ECG Status
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = CozySlateBg.copy(alpha = 0.4f)),
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(10.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Icon(
                                                    Icons.Default.FavoriteBorder,
                                                    contentDescription = "ECG",
                                                    tint = Color(0xFFFF4D4D),
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Text(
                                                    text = tr(isTh, "ECG Analysis", "ตรวจ ECG เคลื่อนไฟ"),
                                                    color = Color.LightGray,
                                                    fontSize = 11.sp
                                                )
                                            }
                                            Text(
                                                text = tr(isTh, "SINUS NORMAL", "คลื่นปกติสมบูรณ์"),
                                                color = SaniGreen,
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = 13.sp
                                            )
                                            Text(
                                                text = health.ecgResult,
                                                color = Color.LightGray,
                                                fontSize = 9.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Divider(color = Color.Gray.copy(alpha = 0.2f))

                        // Watch Simulated Interactive Action Buttons
                        Text(
                            text = tr(isTh, "Simulate Watch Physical Keys / Events:", "กระตุ้นตรวจวิบากเหตุฉุกเฉินตัวเรือน (คีย์สาธิต):"),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { viewModel.triggerWatchSOS() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC30000)),
                                modifier = Modifier
                                    .weight(1.5f)
                                    .height(44.dp),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Warning,
                                        contentDescription = "SOS",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = tr(isTh, "Trigger Watch SOS", "กด SOS เม็ดมะยม"),
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            Button(
                                onClick = { viewModel.triggerWatchFall() },
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryAccent),
                                modifier = Modifier
                                    .weight(1.5f)
                                    .height(44.dp),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Accessibility,
                                        contentDescription = "Fall",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = tr(isTh, "Trigger Watch Fall", "จำลองลื่นล้มฉุกเฉิน"),
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }

                        // Cloud Syncer Controls
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF1E293B), RoundedCornerShape(10.dp))
                                .padding(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    val formattedSync = SimpleDateFormat("HH:mm:ss a", Locale.getDefault()).format(Date(lastSyncTime))
                                    Text(
                                        text = tr(isTh, "Cloud Sync Service", "การนำขึ้นฐานข้อมูลคลาวด์"),
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                    Text(
                                        text = tr(isTh, "Synced on: $formattedSync", "อัปโหลดแล้วเวลา: $formattedSync"),
                                        color = Color.LightGray,
                                        fontSize = 9.sp
                                    )
                                }

                                Button(
                                    onClick = { viewModel.syncWatchDataWithCloud() },
                                    colors = ButtonDefaults.buttonColors(containerColor = SaniGreen),
                                    modifier = Modifier.height(36.dp),
                                    shape = RoundedCornerShape(6.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp)
                                ) {
                                    if (isSyncingData) {
                                        CircularProgressIndicator(
                                            color = Color.White,
                                            strokeWidth = 2.dp,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    } else {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.Sync,
                                                contentDescription = "Sync",
                                                tint = Color.White,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Text(
                                                text = tr(isTh, "Sync Now", "คลาวด์ซิงค์"),
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // SECTION 1: GIANT RED SOS BUTTON (ELDERLY COMPACT)
            item {
                Text(
                    tr(isTh, "SOS Emergency Assistance (Hold or Tap)", "ปุ่มช่วยเหลือฉุกเฉินวิกฤต (กดแช่/กดแตะ)"),
                    color = Color.LightGray,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(
                            Brush.radialGradient(
                                colors = listOf(Color(0xFFFF2E2E), Color(0xFF990000))
                            )
                        )
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onLongPress = {
                                    coroutineScope.launch {
                                        viewModel.triggerEmergencySOS("SOS BUTTON PANIC DETECTED!")
                                    }
                                },
                                onTap = {
                                    coroutineScope.launch {
                                        viewModel.triggerEmergencySOS("SOS BUTTON PRESSED")
                                    }
                                }
                            )
                        }
                        .testTag("sos_giant_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = "SOS Alert Symbol",
                            tint = Color.White,
                            modifier = Modifier.size(72.dp)
                        )
                        Text(
                            text = "SOS",
                            color = Color.White,
                            fontSize = 48.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = tr(isTh, "PRESS FOR HELP", "กดเพื่อของความช่วยเหลือด่วน"),
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }

            // SECTION 2: PUSH TO TALK VOICE MESSAGE BUTTON
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkCardBg),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = tr(isTh, "Send Voice Note / Push To Talk", "ส่งวิทยุสาส์นเสียงหาครอบครัว (Push To Talk)"),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Text(
                            text = if (isRecordingVoice) tr(isTh, "🔊 Recording voice... Release to send", "🔊 กำลังอัดเสียง... ปล่อยนิ้วเพื่อส่ง") 
                                   else tr(isTh, "Hold microphone button to speak, release to transmit", "แตะไอคอนรูปไมโครโฟนค้างเพื่อพูดคุย"),
                            color = if (isRecordingVoice) PrimaryAccent else Color.LightGray,
                            fontSize = 14.sp,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // Microphone Button with tap detection hold actions
                        Box(
                            modifier = Modifier
                                .size(96.dp)
                                .shadow(8.dp, CircleShape)
                                .background(if (isRecordingVoice) Color.Red else PrimaryAccent, CircleShape)
                                .pointerInput(Unit) {
                                    detectTapGestures(
                                        onPress = {
                                            try {
                                                viewModel.startPTTRecording()
                                                awaitRelease()
                                            } finally {
                                                viewModel.stopPTTRecording()
                                            }
                                        }
                                    )
                                }
                                .testTag("ptt_mic_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Mic,
                                contentDescription = "Microphone record",
                                tint = Color.White,
                                modifier = Modifier.size(48.dp)
                            )
                        }
                    }
                }
            }

            // SECTION 3: MEDICATION TODAY CHECKLIST (PRESETS REMINDERS)
            item {
                Text(
                    tr(isTh, "Daily Medication Schedule", "ตารางยารับประทานประจำวันวันนี้"),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    modifier = Modifier.fillMaxWidth().padding(top = 10.dp)
                )
            }

            if (medications.isEmpty()) {
                item {
                    Text(
                        tr(isTh, "No medication reminders arranged today.", "ยังคงไม่มีตารางจัดยารับประทานสำหรับวันวันนี้"),
                        color = Color.LightGray,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            items(medications) { med ->
                val alreadyTaken = medLogsToday.any { it.medicationId == med.id }
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (alreadyTaken) Color(0xFF132A29) else DarkCardBg
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                if (alreadyTaken) Icons.Default.CheckCircle else Icons.Default.Info,
                                contentDescription = "Med icon",
                                tint = if (alreadyTaken) SaniGreen else ContrastAmber,
                                modifier = Modifier.size(36.dp)
                            )
                            Column {
                                Text(
                                    text = med.name,
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${med.dosage} • ${med.timeOfDay}",
                                    color = Color.LightGray,
                                    fontSize = 14.sp
                                )
                            }
                        }

                        Button(
                            onClick = {
                                if (!alreadyTaken) {
                                    viewModel.takeMedication(med.id, med.name)
                                }
                            },
                            enabled = !alreadyTaken,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SaniGreen,
                                disabledContainerColor = Color.Gray.copy(alpha = 0.4f)
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = if (alreadyTaken) tr(isTh, "Taken", "ทานแล้ว") else tr(isTh, "Take Now", "รับประทานยาเลย"),
                                color = if (alreadyTaken) Color.LightGray else Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FamilyDashboardScreen(viewModel: FamilyCareViewModel) {
    val isTh by viewModel.isThaiLanguage.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    val activeSOSLogs by viewModel.activeSOSLogs.collectAsState()
    val allSOSLogs by viewModel.allSOSLogs.collectAsState()
    val voiceMessages by viewModel.voiceMessages.collectAsState()
    val medLogsToday by viewModel.medicationLogsToday.collectAsState()
    val medications by viewModel.medications.collectAsState()
    val context = LocalContext.current

    val selectedWatchBrand by viewModel.selectedWatchBrand.collectAsState()
    val latestWatchHealth by viewModel.latestWatchHealthData.collectAsState()
    val watchBattery by viewModel.watchBatteryLevel.collectAsState()
    val isSyncingData by viewModel.isSyncingWatchData.collectAsState()
    val lastSyncTime by viewModel.lastWatchSyncTime.collectAsState()

    var showHealthReportDialog by remember { mutableStateOf(false) }

    var nowPlayingFilepath by remember { mutableStateOf<String?>(null) }
    var showMedScheduleDialog by remember { mutableStateOf(false) }

    // State parameters for adding new medication prescription
    var newMedName by remember { mutableStateOf("") }
    var newMedDosage by remember { mutableStateOf("") }
    var newMedTime by remember { mutableStateOf("") }
    var newMedDesc by remember { mutableStateOf("") }

    if (showHealthReportDialog) {
        Dialog(onDismissRequest = { showHealthReportDialog = false }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkCardBg),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .wrapContentHeight()
                    .fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = tr(isTh, "Emergency Health Report", "รายงานสรุปด่วนเตรียมการแพทย์"),
                            color = ContrastAmber,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        IconButton(onClick = { showHealthReportDialog = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.LightGray)
                        }
                    }

                    Text(
                        text = tr(
                            isTh,
                            "FAMILY CARE LINK • OFFICIAL EMERGENCY ADVICE",
                            "ระบบสะพานลิ้งก์คุ้มครอง • ข้อมูลสัญญาณชีพสรุปด่วน"
                        ),
                        color = Color.LightGray,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 280.dp)
                            .background(Color(0xFF1E293B), RoundedCornerShape(12.dp))
                            .verticalScroll(rememberScrollState())
                            .padding(12.dp)
                    ) {
                        val reportText = buildString {
                            appendLine("=== FAMILY CARE LINK SUMMARY ===")
                            appendLine("Generated At: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())}")
                            appendLine("Subject/Patient: Sompong (Senior)")
                            appendLine("Current Location Coords: Lat: 13.7563, Lng: 100.5018 (Bangkok)")
                            appendLine("Connected Hardware: $selectedWatchBrand")
                            appendLine("Battery Capacity: $watchBattery%")
                            appendLine("--------------------------------------------")
                            appendLine("LIVE BIOMETRIC TELEMETRY SENSORS:")
                            latestWatchHealth?.let { health ->
                                appendLine("- Heart Rate: ${health.heartRate} BPM (Normal range)")
                                appendLine("- SpO2 (Blood Oxygen): ${health.oxygenLevel}%")
                                appendLine("- Blood Pressure: ${health.bloodPressureSystolic}/${health.bloodPressureDiastolic} mmHg")
                                appendLine("- ECG Analysis: Normal Sinus Rhythm")
                                appendLine("- Stress Level: ${health.stressLevel}/100")
                                appendLine("- Sleep Summary: ${health.sleepDurationHours} Hours (${health.sleepQuality})")
                            } ?: appendLine("- No smartwatch telemetry records saved yet.")
                            appendLine("--------------------------------------------")
                            appendLine("MEDICATION PRESCRIPTIONS:")
                            if (medications.isNotEmpty()) {
                                medications.forEach { med ->
                                    appendLine("- ${med.name} (${med.dosage}) at ${med.timeOfDay}")
                                }
                            } else {
                                appendLine("- No medications listed in database.")
                            }
                            appendLine("--------------------------------------------")
                            appendLine("TODAY'S MEDICINE COMPLIANCE STATUS:")
                            if (medLogsToday.isNotEmpty()) {
                                medLogsToday.forEach { log ->
                                    val formattedTime = java.text.SimpleDateFormat("HH:mm a", java.util.Locale.getDefault()).format(java.util.Date(log.intakeTimestamp))
                                    appendLine("- ${log.medName} [${if (log.isTaken) "TAKEN" else "PENDING"}] at $formattedTime")
                                }
                            } else {
                                appendLine("- No logs recorded for today's medicines yet.")
                            }
                            appendLine("============================================")
                        }

                        Text(
                            text = reportText,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFF50FA7B),
                            fontSize = 11.sp
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                showHealthReportDialog = false
                                coroutineScope.launch {
                                    viewModel.triggerEmergencySOS("EXPORTER REPORT SHARED: Medical synthesis summary copy requested for SOMPONG. Primary contact: Caregiver alerts enabled.")
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SaniGreen),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(tr(isTh, "Share Report (SOS Alert)", "ส่งแชร์สรุปด่วนสัญญาณกู้ชีพ"), color = Color.White)
                        }
                    }
                }
            }
        }
    }

    if (showMedScheduleDialog) {
        Dialog(onDismissRequest = { showMedScheduleDialog = false }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkCardBg),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.wrapContentHeight().fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        tr(isTh, "Add Elder Medication", "เพิ่มกำหนดการยาทานผู้สูงอายุ"),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge
                    )

                    OutlinedTextField(
                        value = newMedName,
                        onValueChange = { newMedName = it },
                        label = { Text(tr(isTh, "Medicine Name", "ชื่อยารับประทาน"), color = Color.LightGray) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        placeholder = { Text(tr(isTh, "E.g. Paracetamol", "เช่น พาราเซตามอล"), color = Color.Gray) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = newMedDosage,
                        onValueChange = { newMedDosage = it },
                        label = { Text(tr(isTh, "Dosage / Quantity (e.g. 1 Pill)", "ขนาดยาที่ทาน (เช่น 1 เม็ด)"), color = Color.LightGray) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = newMedTime,
                        onValueChange = { newMedTime = it },
                        label = { Text(tr(isTh, "Intake TimeOfDay (e.g. 08:30 AM)", "ช่วงเวลาที่รับประทาน (เช่น 08:30 น.)"), color = Color.LightGray) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = newMedDesc,
                        onValueChange = { newMedDesc = it },
                        label = { Text(tr(isTh, "Guidelines / Remarks", "คำแนะนำในการทานยา / หมายเหตุ"), color = Color.LightGray) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = false
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { showMedScheduleDialog = false },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(tr(isTh, "Dismiss", "ปิดหน้าต่าง"), color = Color.White)
                        }

                        Button(
                            onClick = {
                                if (newMedName.isNotBlank() && newMedDosage.isNotBlank()) {
                                    viewModel.addMedicationItem(newMedName, newMedDosage, newMedTime, newMedDesc)
                                    newMedName = ""
                                    newMedDosage = ""
                                    newMedTime = ""
                                    newMedDesc = ""
                                    showMedScheduleDialog = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryAccent),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(tr(isTh, "Save Plan", "บันทึกจัดยา"), color = Color.White)
                        }
                    }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        tr(isTh, "Caregiver Security Board", "กระดานติดตามครอบครัว"),
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        fontSize = 20.sp
                    )
                },
                actions = {
                    TextButton(onClick = { viewModel.toggleLanguage() }) {
                        Text(
                            text = if (isTh) "ENGLISH" else "ภาษาไทย",
                            color = ContrastAmber,
                            fontWeight = FontWeight.Black,
                            fontSize = 15.sp
                        )
                    }
                    IconButton(onClick = { viewModel.navigateTo("HospitalAppointments") }) {
                        Icon(Icons.Default.LocalHospital, contentDescription = "Hospital Appointments", tint = SaniGreen)
                    }
                    IconButton(onClick = { viewModel.navigateTo("Settings") }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CozySlateBg)
            )
        },
        containerColor = CozySlateBg
    ) { padValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // HIGH CONTRAST SOS ALERT NOTIFIER WINDOWS
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = tr(isTh, "Active Panic Alerts (${activeSOSLogs.size})", "สายด่วนแจ้งเหตุฉุกเฉินขณะนี้ (${activeSOSLogs.size})"),
                        color = if (activeSOSLogs.isNotEmpty()) PrimaryAccent else Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp
                    )

                    if (allSOSLogs.isNotEmpty()) {
                        Text(
                            text = tr(isTh, "Clear Alarm logs", "ล้างบันทึกสัญญาณเตือน"),
                            color = Color.LightGray,
                            fontSize = 13.sp,
                            modifier = Modifier
                                .border(1.dp, Color.Gray, RoundedCornerShape(8.dp))
                                .clickable { viewModel.clearAlerts() }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            if (activeSOSLogs.isEmpty()) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF132A29)),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Check, contentDescription = "Status safe", tint = SaniGreen, modifier = Modifier.size(28.dp))
                            Column {
                                Text(tr(isTh, "ALL SYSTEM STATUS SECURE", "สถานะครอบครัวคุณปกติและปลอดภัย"), color = Color.White, fontWeight = FontWeight.Bold)
                                Text(tr(isTh, "No unresolved Elder alerts triggered right now.", "ไม่มีสัญญาณแจ้งเตือนตกค้างจากผู้สูงอายุในขณะนี้"), color = Color.LightGray, fontSize = 13.sp)
                            }
                        }
                    }
                }
            } else {
                items(activeSOSLogs) { log ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = PrimaryAccent),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = tr(isTh, "🔴 SOS: ${log.elderName}", "🔴 ด่วน SOS: ${log.elderName}"),
                                    color = Color.White,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 18.sp
                                )
                                Text(
                                    text = tr(isTh, "LIVE PIN", "แสดงพิกัดนำทาง"),
                                    color = ContrastAmber,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    modifier = Modifier
                                        .border(1.dp, ContrastAmber, RoundedCornerShape(4.dp))
                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }

                            Text(
                                text = tr(isTh, "Incident: ${log.alertMessage}", "สาเหตุเหตุการณ์: ${log.alertMessage}"),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.padding(vertical = 4.dp)
                            ) {
                                Icon(Icons.Default.Place, contentDescription = "Coords", tint = Color.White, modifier = Modifier.size(16.dp))
                                Text(
                                    text = tr(isTh, "Latitude: ${log.latitude} • Longitude: ${log.longitude}", "ละติจูด: ${log.latitude} • ลองจิจูด: ${log.longitude}"),
                                    color = Color.White.copy(alpha = 0.9f),
                                    fontSize = 12.sp
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                // REDIRECT BUTTON TO LIVE GOOGLE MAPS
                                Button(
                                    onClick = {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(log.googleMapsUrl))
                                        context.startActivity(intent)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                                    modifier = Modifier.weight(1f).height(40.dp),
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(Icons.Default.Place, contentDescription = "gmaps", tint = PrimaryAccent, modifier = Modifier.size(16.dp))
                                        Text(tr(isTh, "Google Maps", "นำทางแผนที่"), color = PrimaryAccent, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    }
                                }

                                Button(
                                    onClick = { viewModel.resolveAlert(log.id) },
                                    colors = ButtonDefaults.buttonColors(containerColor = CozySlateBg),
                                    modifier = Modifier.weight(1f).height(40.dp)
                                ) {
                                    Text(tr(isTh, "Mark Resolved", "ช่วยเหลือเสร็จสิ้น"), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                            }
                        }
                    }
                }
            }

            // CAREGIVER LIVE WEARABLES MONITORING HUB
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkCardBg),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Title header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Icons.Default.Devices,
                                    contentDescription = "Watch Icon",
                                    tint = ContrastAmber,
                                    modifier = Modifier.size(24.dp)
                                )
                                Text(
                                    text = tr(isTh, "Live Wearable Health Hub", "ศูนย์ติดตามตรวจประเมินสัญญาณชีพนาฬิกา"),
                                    color = Color.White,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 16.sp
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .background(SaniGreen.copy(alpha = 0.2f), RoundedCornerShape(10.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = tr(isTh, "CLOUDSYNC ACTIVE", "คลาวด์ทำงานสตรีมอยู่"),
                                    color = SaniGreen,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Hardware info summary
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF1E293B), RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(
                                        text = tr(isTh, "Equipped Watch Model:", "รุ่นนาฬิกาประจำตัวคนไข้:"),
                                        color = Color.LightGray,
                                        fontSize = 11.sp
                                    )
                                    Text(
                                        text = selectedWatchBrand,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(
                                        text = tr(isTh, "Watch Battery status:", "แบตเตอรี่หน้าปัด:"),
                                        color = Color.LightGray,
                                        fontSize = 11.sp
                                    )
                                    Text(
                                        text = "🔋 $watchBattery% Charged",
                                        color = if (watchBattery < 20) Color.Red else SaniGreen,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }

                        // Grid representing the live indicators from patient
                        latestWatchHealth?.let { health ->
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // HR item with alerts
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = CozySlateBg.copy(alpha = 0.5f)),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(10.dp),
                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                Icon(Icons.Default.Favorite, contentDescription = "HR", tint = Color.Red, modifier = Modifier.size(16.dp))
                                                Text(tr(isTh, "Pulse (HR)", "จุดพิกัดชีพจร"), color = Color.LightGray, fontSize = 11.sp)
                                            }
                                            Text("${health.heartRate} BPM", color = Color.White, fontWeight = FontWeight.Black, fontSize = 16.sp)
                                            val isHrWarning = health.heartRate < 60 || health.heartRate > 100
                                            Text(
                                                text = if (isHrWarning) tr(isTh, "⚠️ ABNORMAL", "⚠️ ชีพจรอ่อนไหว") else tr(isTh, "✓ Safe Range", "✓ พิกัดระดับปกติ"),
                                                color = if (isHrWarning) Color.Red else SaniGreen,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }

                                    // SpO2 item
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = CozySlateBg.copy(alpha = 0.5f)),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(10.dp),
                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                Icon(Icons.Default.Opacity, contentDescription = "SpO2", tint = Color(0xFF2EA2FF), modifier = Modifier.size(16.dp))
                                                Text(tr(isTh, "Blood Oxygen (SpO2)", "ออกซิเจนในเลือด"), color = Color.LightGray, fontSize = 11.sp)
                                            }
                                            Text("${health.oxygenLevel}%", color = Color.White, fontWeight = FontWeight.Black, fontSize = 16.sp)
                                            val isOxygenWarning = health.oxygenLevel < 95
                                            Text(
                                                text = if (isOxygenWarning) tr(isTh, "⚠️ DECREASED SpO2", "⚠️ ต่ำกว่าเกณฑ์") else tr(isTh, "✓ Optimal Oxygenation", "✓ ออกซิเจนสมบูรณ์ตามเกณฑ์"),
                                                color = if (isOxygenWarning) Color.Red else SaniGreen,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // Blood pressure
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = CozySlateBg.copy(alpha = 0.5f)),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(10.dp),
                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                Icon(Icons.Default.Bolt, contentDescription = "BP", tint = Color(0xFFFFB300), modifier = Modifier.size(16.dp))
                                                Text(tr(isTh, "Blood Pressure (BP)", "แรงความดันโลหิต"), color = Color.LightGray, fontSize = 11.sp)
                                            }
                                            Text("${health.bloodPressureSystolic}/${health.bloodPressureDiastolic}", color = Color.White, fontWeight = FontWeight.Black, fontSize = 16.sp)
                                            Text(tr(isTh, "mmHg (Steady State)", "มม.ปรอท (ค่าสถิตปกติ)"), color = SaniGreen, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }

                                    // Stress and ECG
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = CozySlateBg.copy(alpha = 0.5f)),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(10.dp),
                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                Icon(Icons.Default.FavoriteBorder, contentDescription = "ECG", tint = Color(0xFFFF4D4D), modifier = Modifier.size(16.dp))
                                                Text(tr(isTh, "ECG Pulse Quality", "จังหวะกล้ามเนื้อหัวใจ"), color = Color.LightGray, fontSize = 11.sp)
                                            }
                                            Text(tr(isTh, "NORMAL RHYTHM", "สัญญาสัญญาณปกติ"), color = SaniGreen, fontWeight = FontWeight.Black, fontSize = 14.sp)
                                            Text(health.ecgResult, color = Color.LightGray, fontSize = 9.sp)
                                        }
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // Sleep summary
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = CozySlateBg.copy(alpha = 0.5f)),
                                        modifier = Modifier.weight(1.2f)
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(10.dp),
                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                Icon(Icons.Default.NightsStay, contentDescription = "Sleep", tint = Color(0xFFA55EEA), modifier = Modifier.size(16.dp))
                                                Text(tr(isTh, "Sleep Data History", "ชั่วโมงนอนหลับ"), color = Color.LightGray, fontSize = 11.sp)
                                            }
                                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                Text("${health.sleepDurationHours} Hours", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                                Text(health.sleepQuality, color = SaniGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }

                                    // Stress index
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = CozySlateBg.copy(alpha = 0.5f)),
                                        modifier = Modifier.weight(0.8f)
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(10.dp),
                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                Icon(Icons.Default.TrendingUp, contentDescription = "Stress", tint = ContrastAmber, modifier = Modifier.size(16.dp))
                                                Text(tr(isTh, "Stress Level", "ความเครียดเค้นสมอง"), color = Color.LightGray, fontSize = 11.sp)
                                            }
                                            Text("${health.stressLevel}/100 Index", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        }
                                    }
                                }
                            }
                        }

                        // Action area: Emergency Health Report Generation
                        Button(
                            onClick = { showHealthReportDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = SaniGreen),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.Share, contentDescription = "Report", tint = Color.White, modifier = Modifier.size(18.dp))
                                Text(
                                    text = tr(isTh, "Generate Emergency Health Summary", "จัดทำสรุปสุขภาพเวชศาสตร์ด่วนส่งหมอ"),
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            }

            // SECTION: ACTIVE INCOMING VOICES MESSAGES (EXCELLENT STREAMING SIMULATION)
            item {
                Text(
                    text = tr(isTh, "Elder Voice Recordings (${voiceMessages.size})", "บันทึกข้อความเสียงผู้สูงอายุและประเมินสุขภาพ (${voiceMessages.size})"),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }

            if (voiceMessages.isEmpty()) {
                item {
                    Text(tr(isTh, "No voice feeds recorded yet.", "ยังไม่ได้รับข้อความเสียงบันทึกเข้ามา"), color = Color.LightGray, modifier = Modifier.padding(8.dp))
                }
            } else {
                items(voiceMessages) { msg ->
                    val isPlaying = nowPlayingFilepath == msg.filepath
                    Card(
                        colors = CardDefaults.cardColors(containerColor = DarkCardBg),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(msg.elderName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    val readableTime = SimpleDateFormat("HH:mm:ss a • MMM dd", Locale.getDefault()).format(Date(msg.timestamp))
                                    Text(readableTime, color = Color.Gray, fontSize = 12.sp)
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                    // PREPARED FOR FUTURE AI VOICE HEALTH MONITORING INSIGHT PILLS
                                    val tagColor = when (msg.distressLevel) {
                                        "Severe distress" -> Color(0xFFFF2A2A)
                                        "Slight distress" -> Color(0xFFFFA502)
                                        else -> SaniGreen
                                    }
                                    val localDistressLevel = when (msg.distressLevel) {
                                        "Severe distress" -> tr(isTh, "Severe distress", "วิกฤตรุนแรง")
                                        "Slight distress" -> tr(isTh, "Slight distress", "กังวลเล็กน้อย")
                                        else -> tr(isTh, msg.distressLevel, "เป็นปกติ")
                                    }
                                    Box(
                                        modifier = Modifier
                                            .background(tagColor.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                            .border(1.dp, tagColor, RoundedCornerShape(4.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(localDistressLevel.uppercase(), color = tagColor, fontSize = 10.sp, fontWeight = FontWeight.Black)
                                    }

                                    Button(
                                        onClick = {
                                            if (isPlaying) {
                                                // Release or stop is handled natively. Standard tap plays path
                                                nowPlayingFilepath = null
                                            } else {
                                                nowPlayingFilepath = msg.filepath
                                                viewModel.playPTTVoice(msg.filepath) {
                                                    nowPlayingFilepath = null
                                                }
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = if (isPlaying) SaniGreen else PrimaryAccent),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.height(36.dp).wrapContentWidth()
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            Icon(
                                                if (isPlaying) Icons.Default.Refresh else Icons.Default.PlayArrow,
                                                contentDescription = "play/stop",
                                                tint = Color.White,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Text(if (isPlaying) tr(isTh, "Playing", "กำลังฟัง") else tr(isTh, "Listen", "คลิกเพื่อฟัง"), color = Color.White, fontSize = 12.sp)
                                        }
                                    }
                                }
                            }

                            // EXPANDABLE VOICE HEALTH AI ARCHITECTURE SHOWCASE
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(CozySlateBg.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                                    .border(0.5.dp, Color.Gray.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                    .padding(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(tr(isTh, "🤖 Core AI Speech Analysis", "🤖 ระบบวิเคราะห์ระดับสุขภาพทางเสียงด้วย AI"), color = ContrastAmber, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    Text(tr(isTh, "Pulse Check: ${if (msg.detectedPulseBpm > 0) "${msg.detectedPulseBpm} BPM" else "Calculating"}", "ตรวจสัญญาณชีพจรสะท้อน: ${if (msg.detectedPulseBpm > 0) "${msg.detectedPulseBpm} ครั้ง/นาที" else "กำลังคาดคะเน"}"), color = Color.LightGray, fontSize = 10.sp)
                                }
                                Text(
                                    text = msg.comments,
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                        }
                    }
                }
            }

            // SECTION: MEDICATION PROGRESS TODAY LISTS
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(tr(isTh, "Medication Reminders Status", "สถานะความคืบหน้าการรับประทานยา"), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Button(
                        onClick = { showMedScheduleDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = SaniGreen),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Text(tr(isTh, "+ Add Schedule", "+ เพิ่มจัดตารางยา"), color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            if (medLogsToday.isEmpty()) {
                item {
                    Text(tr(isTh, "No medicines marked as taken today yet.", "ผู้สูงอายุยังไม่มีประทานทานยาวันนี้"), color = Color.LightGray)
                }
            } else {
                items(medLogsToday) { log ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF132A29)),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CheckCircle, contentDescription = "done", tint = SaniGreen)
                                Column {
                                    Text(log.medName, color = Color.White, fontWeight = FontWeight.Bold)
                                    val checkTime = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(log.intakeTimestamp))
                                    Text(tr(isTh, "Taken at $checkTime", "รับประทานยาแล้วเสร็จเมื่อเวลา $checkTime"), color = Color.LightGray, fontSize = 12.sp)
                                }
                            }
                            Text(tr(isTh, "CONFIRMED", "ยืนยันแล้ว"), color = SaniGreen, fontWeight = FontWeight.Black, fontSize = 11.sp)
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: FamilyCareViewModel) {
    val isTh by viewModel.isThaiLanguage.collectAsState()
    val fallEnabled by viewModel.fallDetectionEnabled.collectAsState()
    val sensorX by viewModel.sensorX.collectAsState()
    val sensorY by viewModel.sensorY.collectAsState()
    val sensorZ by viewModel.sensorZ.collectAsState()
    val currentUserState = viewModel.currentUser.collectAsState().value

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(tr(isTh, "Settings & Sensor Portal", "ตั้งค่าระบบ & มอนิเตอร์เซนเซอร์"), color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateBack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    TextButton(onClick = { viewModel.toggleLanguage() }) {
                        Text(
                            text = if (isTh) "ENGLISH" else "ภาษาไทย",
                            color = ContrastAmber,
                            fontWeight = FontWeight.Black,
                            fontSize = 15.sp
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CozySlateBg)
            )
        },
        containerColor = CozySlateBg
    ) { padValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // PROFILE DETAILS
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkCardBg),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(tr(isTh, "Session & Role Alignment", "จำลองเปลี่ยนเข้าสู่โหมดประเมินสถานภาพ"), color = ContrastAmber, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(tr(isTh, "User: ${currentUserState?.name ?: "Unknown"}", "บัญชีผู้ใช้: ${currentUserState?.name ?: "ไม่ระบุชื่อ"}"), color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text(tr(isTh, "Email: ${currentUserState?.email ?: "Unknown"}", "อีเมล: ${currentUserState?.email ?: "ไม่ระบุอีเมล์"}"), color = Color.LightGray, fontSize = 14.sp)
                    Text(tr(isTh, "Linked Group Code: ${currentUserState?.groupCode ?: "No code joined"}", "รหัสเข้าร่วมกลุ่มบ้าน: ${currentUserState?.groupCode ?: "ไม่มีรหัสกลุ่มเชื่อมโยง"}"), color = Color.LightGray, fontSize = 14.sp)

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { viewModel.handleLogin("sompong@care.com", "Elderly") },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryAccent),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(tr(isTh, "Elder Mode", "โหมดผู้สูงอายุ"), fontSize = 12.sp, color = Color.White)
                        }

                        Button(
                            onClick = { viewModel.handleLogin("suda@family.com", "Family Member") },
                            colors = ButtonDefaults.buttonColors(containerColor = SaniGreen),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(tr(isTh, "Family Mode", "โหมดครอบครัว"), fontSize = 12.sp, color = Color.White)
                        }
                    }
                }
            }

            // FALL DETECTION CONFIGURATION
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkCardBg),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(tr(isTh, "Fall Detection Standard", "เปิดโหมดเซนเซอร์ล้มฉุกเฉิน"), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text(tr(isTh, "Uses Accelerometer G force parameters", "ใช้โมดูลวัดผลแรงโน้มถ่วงเรียลไทม์เพื่อกันความเสี่ยง"), color = Color.LightGray, fontSize = 12.sp)
                        }
                        Switch(
                            checked = fallEnabled,
                            onCheckedChange = { viewModel.toggleFallDetection(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = PrimaryAccent,
                                checkedTrackColor = PrimaryAccent.copy(alpha = 0.4f)
                            )
                        )
                    }

                    // LIVE SENSOR METRICS FOR PREMIUM SHOWCASE
                    if (fallEnabled) {
                        Divider(color = Color.Gray.copy(alpha = 0.2f))
                        Text(tr(isTh, "Live G-Sensor Telemetry Vectors", "ข้อมูลผลลัพธ์พิกัดเซนเซอร์แรงโน้มถ่วงตัวรับผล"), color = ContrastAmber, fontWeight = FontWeight.Bold, fontSize = 13.sp)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(tr(isTh, "X-axis (Lateral)", "แกนหนีศูนย์แนวขวาง X"), color = Color.LightGray, fontSize = 11.sp)
                                Text(String.format(Locale.getDefault(), "%.2f m/s²", sensorX), color = Color.White, fontSize = 14.sp)
                            }
                            Column {
                                Text(tr(isTh, "Y-axis (Vertical)", "แกนหนีศูนย์แนวติ่งตั้ง Y"), color = Color.LightGray, fontSize = 11.sp)
                                Text(String.format(Locale.getDefault(), "%.2f m/s²", sensorY), color = Color.White, fontSize = 14.sp)
                            }
                            Column {
                                Text(tr(isTh, "Z-axis (Depth)", "แกนหนีศูนย์แนวดิ่งลึก Z"), color = Color.LightGray, fontSize = 11.sp)
                                Text(String.format(Locale.getDefault(), "%.2f m/s²", sensorZ), color = Color.White, fontSize = 14.sp)
                            }
                        }

                        // Static Fall Vector simulation instruction info
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(CozySlateBg.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                .padding(10.dp)
                        ) {
                            Text(
                                tr(isTh, 
                                    "Simulation Tip: Shake coordinates rapidly in the emulator, or trigger crash force velocity simulation from gravity drops to trigger the 30-Seconds Countdown automatically.",
                                    "คำแนะนำกระตุ้นแบบจำลอง: ทำการเหวี่ยงเคลื่อนแรงสั่นไหวในเซนเซอร์อย่างรวดเร็ว เพื่อตรวจจับแรงกระแทกดิ่ง ต้านภัยเพื่อให้นาฬิกานับถอยหลัง 30 วินาทีทำงานจำลองเพื่อทดสอบกลุ่มทันที"
                                ),
                                color = Color.LightGray,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // LOG OUT
            Button(
                onClick = { viewModel.handleLogout() },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.PowerSettingsNew, contentDescription = "logout", tint = Color.White)
                    Text(tr(isTh, "Log Out Safe Session", "ออกจากระบบควบคุมอย่างปลอดภัย"), color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}


