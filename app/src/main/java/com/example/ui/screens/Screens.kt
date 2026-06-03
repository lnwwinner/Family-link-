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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.EmergencyLog
import com.example.data.model.Medication
import com.example.data.model.VoiceMessage
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
                text = "FAMILY CARE LINK",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    letterSpacing = 2.sp
                )
            )

            Text(
                text = "Emergency Companion & Vital Connect",
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = Color.LightGray.copy(alpha = 0.8f)
                )
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
                    text = "Log In",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    ),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = "Access Family Care Support Portal",
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
                                text = role,
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
                    label = { Text("Email Address", color = Color.LightGray) },
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
                    label = { Text("Password", color = Color.LightGray) },
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
                    Text("Secure Log In", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }

            item {
                TextButton(
                    onClick = { viewModel.navigateTo("Register") }
                ) {
                    Text("Don't have an account? Sign Up", color = ContrastAmber, fontSize = 16.sp)
                }
            }
        }
    }
}

@Composable
fun RegisterScreen(viewModel: FamilyCareViewModel) {
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
        LazyColumn(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            item {
                Text(
                    text = "Create Account",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
                Text(
                    text = "Sign up to link with your relatives",
                    color = Color.LightGray,
                    fontSize = 14.sp
                )
            }

            item {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Full Name", color = Color.LightGray) },
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
                    label = { Text("Email Address", color = Color.LightGray) },
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
                    text = "Select Account Role",
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
                                text = role,
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
                    Text("Register & Next", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }

            item {
                TextButton(onClick = { viewModel.navigateTo("Login") }) {
                    Text("Already registered? Secure Log In", color = ContrastAmber, fontSize = 16.sp)
                }
            }
        }
    }
}

@Composable
fun GroupSetupScreen(viewModel: FamilyCareViewModel) {
    var groupCodeInput by remember { mutableStateOf("") }
    var groupNameInput by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CozySlateBg)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Family Group Connect",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )

            Text(
                text = "To sync emergency alerts, join an existing group with a code or create a new family network group.",
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
                    Text("Join Existing Group", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    OutlinedTextField(
                        value = groupCodeInput,
                        onValueChange = { groupCodeInput = it },
                        placeholder = { Text("E.g. FAM-Care-419", color = Color.Gray) },
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
                        Text("Verify & Connect", color = CozySlateBg, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Text("— OR —", color = Color.Gray, fontSize = 14.sp)

            // OPTION 2: CREATE
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1B243B)),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Create New Family Circle", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    OutlinedTextField(
                        value = groupNameInput,
                        onValueChange = { groupNameInput = it },
                        placeholder = { Text("E.g. Sompong Home Care", color = Color.Gray) },
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
                        Text("Create & Launch Group", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ElderDashboardScreen(viewModel: FamilyCareViewModel) {
    val coroutineScope = rememberCoroutineScope()
    val isRecordingVoice by viewModel.isRecordingVoice.collectAsState()
    val isFallCountingDown by viewModel.isFallCountingDown.collectAsState()
    val fallCountSec by viewModel.fallCountdownSeconds.collectAsState()
    val medications by viewModel.medications.collectAsState()
    val medLogsToday by viewModel.medicationLogsToday.collectAsState()
    val userName = viewModel.currentUser.collectAsState().value?.name ?: "Senior User"

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
                        text = "FALL DETECTED!",
                        color = Color.White,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = "Sending SOS alert to all family members in:",
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
                            "I AM OKAY (CANCEL)",
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
                        "Home Care Connect",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 22.sp
                    )
                },
                actions = {
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
                            text = "สวัสดี / Welcome,",
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
                            Text("Elder Mode Screen", color = Color(0xFF2EC4B6), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // SECTION 1: GIANT RED SOS BUTTON (ELDERLY COMPACT)
            item {
                Text(
                    "ปุ่มช่วยเหลือฉุกเฉิน (กดค้างลุย)",
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
                            text = "PRESS FOR HELP",
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
                            text = "ส่งเสียงถึงครอบครัว / Push To Talk",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Text(
                            text = if (isRecordingVoice) "🔊 กำลังบันทึกเสียง... ปล่อยเพื่อส่ง" else "แตะค้างปุ่มไมค์พูดส่งข้อความเสียง",
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
                    "รายการยารับประทานวันนี้ / Medications Today",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    modifier = Modifier.fillMaxWidth().padding(top = 10.dp)
                )
            }

            if (medications.isEmpty()) {
                item {
                    Text(
                        "ยังไม่มีรายการตารางยาสำหรับวันนี้ / No Meds scheduled.",
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
                                text = if (alreadyTaken) "ทิานแล้ว / Taken" else "ทานยาเลย",
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
    val coroutineScope = rememberCoroutineScope()
    val activeSOSLogs by viewModel.activeSOSLogs.collectAsState()
    val allSOSLogs by viewModel.allSOSLogs.collectAsState()
    val voiceMessages by viewModel.voiceMessages.collectAsState()
    val medLogsToday by viewModel.medicationLogsToday.collectAsState()
    val context = LocalContext.current

    var nowPlayingFilepath by remember { mutableStateOf<String?>(null) }
    var showMedScheduleDialog by remember { mutableStateOf(false) }

    // State parameters for adding new medication prescription
    var newMedName by remember { mutableStateOf("") }
    var newMedDosage by remember { mutableStateOf("") }
    var newMedTime by remember { mutableStateOf("") }
    var newMedDesc by remember { mutableStateOf("") }

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
                        "Add Elder Medication",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge
                    )

                    OutlinedTextField(
                        value = newMedName,
                        onValueChange = { newMedName = it },
                        label = { Text("Medicine Name", color = Color.LightGray) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = newMedDosage,
                        onValueChange = { newMedDosage = it },
                        label = { Text("Dosage / Quantity (e.g. 1 Pill)", color = Color.LightGray) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = newMedTime,
                        onValueChange = { newMedTime = it },
                        label = { Text("Intake TimeOfDay (e.g. 08:30 AM)", color = Color.LightGray) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = newMedDesc,
                        onValueChange = { newMedDesc = it },
                        label = { Text("Guidelines / Remarks", color = Color.LightGray) },
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
                            Text("Dismiss", color = Color.White)
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
                            Text("Save Plan", color = Color.White)
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
                        "Caregiver Security Board",
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        fontSize = 20.sp
                    )
                },
                actions = {
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
                        text = "Active Panic Alerts (${activeSOSLogs.size})",
                        color = if (activeSOSLogs.isNotEmpty()) PrimaryAccent else Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp
                    )

                    if (allSOSLogs.isNotEmpty()) {
                        Text(
                            text = "Clear Alarm logs",
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
                                Text("ALL SYSTEM STATUS SECURE", color = Color.White, fontWeight = FontWeight.Bold)
                                Text("No unresolved Elder alerts triggered right now.", color = Color.LightGray, fontSize = 13.sp)
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
                                    text = "🔴 SOS: ${log.elderName}",
                                    color = Color.White,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 18.sp
                                )
                                Text(
                                    text = "LIVE PIN",
                                    color = ContrastAmber,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    modifier = Modifier
                                        .border(1.dp, ContrastAmber, RoundedCornerShape(4.dp))
                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }

                            Text(
                                text = "Incident: ${log.alertMessage}",
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
                                    text = "Lattitude: ${log.latitude} • Longitude: ${log.longitude}",
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
                                        Text("Google Maps", color = PrimaryAccent, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    }
                                }

                                Button(
                                    onClick = { viewModel.resolveAlert(log.id) },
                                    colors = ButtonDefaults.buttonColors(containerColor = CozySlateBg),
                                    modifier = Modifier.weight(1f).height(40.dp)
                                ) {
                                    Text("Mark Resolved", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                            }
                        }
                    }
                }
            }

            // SECTION: ACTIVE INCOMING VOICES MESSAGES (EXCELLENT STREAMING SIMULATION)
            item {
                Text(
                    text = "Elder Voice Recordings (${voiceMessages.size})",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }

            if (voiceMessages.isEmpty()) {
                item {
                    Text("No voice feeds recorded yet.", color = Color.LightGray, modifier = Modifier.padding(8.dp))
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
                                    Box(
                                        modifier = Modifier
                                            .background(tagColor.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                            .border(1.dp, tagColor, RoundedCornerShape(4.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(msg.distressLevel.uppercase(), color = tagColor, fontSize = 10.sp, fontWeight = FontWeight.Black)
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
                                            Text(if (isPlaying) "Playing" else "Listen", color = Color.White, fontSize = 12.sp)
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
                                    Text("🤖 Core AI Speech Analysis", color = ContrastAmber, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    Text("Pulse Check: ${if (msg.detectedPulseBpm > 0) "${msg.detectedPulseBpm} BPM" else "Calculating"}", color = Color.LightGray, fontSize = 10.sp)
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
                    Text("Medication Reminders Status", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Button(
                        onClick = { showMedScheduleDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = SaniGreen),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Text("+ Add Schedule", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            if (medLogsToday.isEmpty()) {
                item {
                    Text("No medicines marked as taken today yet.", color = Color.LightGray)
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
                                    Text("Taken at $checkTime", color = Color.LightGray, fontSize = 12.sp)
                                }
                            }
                            Text("CONFIRMED", color = SaniGreen, fontWeight = FontWeight.Black, fontSize = 11.sp)
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
    val fallEnabled by viewModel.fallDetectionEnabled.collectAsState()
    val sensorX by viewModel.sensorX.collectAsState()
    val sensorY by viewModel.sensorY.collectAsState()
    val sensorZ by viewModel.sensorZ.collectAsState()
    val currentUserState = viewModel.currentUser.collectAsState().value

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings & Sensor Portal", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateBack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
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
                    Text("Session & Role Alignment", color = ContrastAmber, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("User: ${currentUserState?.name ?: "Unknown"}", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text("Email: ${currentUserState?.email ?: "Unknown"}", color = Color.LightGray, fontSize = 14.sp)
                    Text("Linked Group Code: ${currentUserState?.groupCode ?: "No code joined"}", color = Color.LightGray, fontSize = 14.sp)

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
                            Text("Elder Mode", fontSize = 12.sp, color = Color.White)
                        }

                        Button(
                            onClick = { viewModel.handleLogin("suda@family.com", "Family Member") },
                            colors = ButtonDefaults.buttonColors(containerColor = SaniGreen),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Family Mode", fontSize = 12.sp, color = Color.White)
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
                            Text("Fall Detection Standard", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text("Uses Accelerometer G force parameters", color = Color.LightGray, fontSize = 12.sp)
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
                        Text("Live G-Sensor Telemetry Vectors", color = ContrastAmber, fontWeight = FontWeight.Bold, fontSize = 13.sp)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("X-axis (Lateral)", color = Color.LightGray, fontSize = 11.sp)
                                Text(String.format(Locale.getDefault(), "%.2f m/s²", sensorX), color = Color.White, fontSize = 14.sp)
                            }
                            Column {
                                Text("Y-axis (Vertical)", color = Color.LightGray, fontSize = 11.sp)
                                Text(String.format(Locale.getDefault(), "%.2f m/s²", sensorY), color = Color.White, fontSize = 14.sp)
                            }
                            Column {
                                Text("Z-axis (Depth)", color = Color.LightGray, fontSize = 11.sp)
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
                                "Simulation Tip: Shake coordinates rapidly in the emulator, or trigger crash force velocity simulation from gravity drops to trigger the 30-Seconds Countdown automatically.",
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
                    Text("Log Out Safe Session", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}


