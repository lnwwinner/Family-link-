package com.example.ui.screens

import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.FamilyCareViewModel
import com.example.ui.theme.MyApplicationTheme

class LockScreenEmergencyActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Native Android LockScreen Bypass Window Flags
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }

        setContent {
            MyApplicationTheme(dynamicColor = false, darkTheme = true) {
                val viewModel: FamilyCareViewModel = viewModel()
                LockScreenEmergencyCardContent(
                    viewModel = viewModel,
                    onBack = { finish() }
                )
            }
        }
    }
}

@Composable
fun LockScreenEmergencyCardContent(
    viewModel: FamilyCareViewModel,
    onBack: () -> Unit
) {
    val isTh by viewModel.isThaiLanguage.collectAsState()
    val emergencyProfile by viewModel.emergencyProfile.collectAsState()

    // Grab states or fallbacks to safeguard render
    val name = emergencyProfile?.patientName ?: "Sompong Somdee"
    val blood = emergencyProfile?.bloodType ?: "O+"
    val allergies = emergencyProfile?.allergies ?: "Sulfide Antibiotics, Penicillin"
    val chronic = emergencyProfile?.chronicConditions ?: "Hypertension, Stage-2 Diabetes, Arrhythmia"
    val dailyMeds = emergencyProfile?.regularPrescriptions ?: "Aspirin 81mg (Daily), Metformin 500mg, Atorvastatin 20mg"
    val contactName = emergencyProfile?.emergencyContactName ?: "Suda Somdee (Daughter)"
    val contactPhone = emergencyProfile?.emergencyContactPhone ?: "081-234-5678"
    val hospital = emergencyProfile?.hospitalPreference ?: "Bangkok General Hospital"
    val doctor = "Dr. Anon (Cardiology)"
    val notes = emergencyProfile?.additionalNotes ?: "No paramedic notes declared."

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F1524))
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // RED EMERGENCY HEADER
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFC30000)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.HealthAndSafety,
                        contentDescription = "Medical Emergency",
                        tint = Color.White,
                        modifier = Modifier.size(48.dp)
                    )
                    Text(
                        text = tr(isTh, "ICE: IN CASE OF EMERGENCY", "ข้อมูลวิกฤติกู้ชีพแพทย์สนามด่วน"),
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = tr(isTh, "LOCK SCREEN ACCESS ALLOWED BY SYSTEM", "พอร์ทัลความปลอดภัยเปิดเผยพิเศษโดยไม่ต้องรหัสล็อค"),
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // PATIENT PRIMARY IDENTIFIER BADGE
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1B243B)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .background(Color.White, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = blood,
                            color = Color(0xFFC30000),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black
                        )
                    }

                    Column {
                        Text(
                            text = name,
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 20.sp
                        )
                        Text(
                            text = tr(isTh, "Blood Type: $blood", "หมู่กรุ๊ปเลือด: $blood"),
                            color = Color.LightGray,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            // SEVERE LIFE WARNING BAR
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1B243B)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Default.Warning, contentDescription = "Allergies", tint = Color(0xFFFFB703), modifier = Modifier.size(18.dp))
                        Text(tr(isTh, "SEVERE DRUG & SEAFOOD ALLERGIES", "ประวัติแพ้ยา / วัสดุต้านกู้แก้วิกฤติ"), color = Color(0xFFFFB703), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                    Text(text = allergies, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }

            // CHRONIC ILLNESSES CARD
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1B243B)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Default.ListAlt, contentDescription = "Chronic", tint = Color(0xFFFFB703), modifier = Modifier.size(18.dp))
                        Text(tr(isTh, "CHRONIC DISEASES", "กลุ่มโรคประจำตัวและอาการแสดงเรื้อรัง"), color = Color(0xFFFFB703), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                    Text(text = chronic, color = Color.White, fontSize = 14.sp)
                }
            }

            // CURRENT PRESCRIPTION DAILY MEDICINES
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1B243B)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Default.Vaccines, contentDescription = "Medicines", tint = Color(0xFFFFB703), modifier = Modifier.size(18.dp))
                        Text(tr(isTh, "MANDATORY DAILY MEDICATIONS", "ตารางรายการยาสามัญคุมโรค"), color = Color(0xFFFFB703), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                    Text(text = dailyMeds, color = Color.White, fontSize = 14.sp)
                }
            }

            // ATTENDING MEDICAL CENTER & DOCTORS
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1B243B)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Default.LocalHospital, contentDescription = "Doctor", tint = Color(0xFFFFB703), modifier = Modifier.size(18.dp))
                        Text(tr(isTh, "ATTENDING HOSPITAL & PHYSICIAN", "โรงพยาบาลรักษาร่วมและคุณหมอผู้ดูแลนัด"), color = Color(0xFFFFB703), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                    Text(text = "$hospital • $doctor", color = Color.White, fontSize = 14.sp)
                }
            }

            // PRIMARY EMERGENCY COORDINATOR CONTACT
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF2EC4B6).copy(alpha = 0.15f)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFF2EC4B6).copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Default.Phone, contentDescription = "Contact", tint = Color(0xFF2EC4B6), modifier = Modifier.size(18.dp))
                        Text(tr(isTh, "PRIMARY COORDINATOR CONTACT", "เบอร์โทรศัพท์ผู้ประสานงานหลักสิทธิ์ป่วย"), color = Color(0xFF2EC4B6), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                    Text(text = "$contactName: $contactPhone", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }

            // SPECIALLY WRITTEN NOTES
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().border(1.dp, Color.Red.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Default.Feedback, contentDescription = "Special Caution", tint = Color.Red, modifier = Modifier.size(16.dp))
                        Text(tr(isTh, "CRITICAL TREATMENT PARAMETERS / NOTES", "ขอบเจตจำนงพิเศษ และจุดพึงกังวลในการรักษาด่วน"), color = Color.Red, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                    Text(text = notes, color = Color.White, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                }
            }

            Button(
                onClick = onBack,
                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.1f)),
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(tr(isTh, "Dismiss (Close)", "ปิดหน้านี้"), color = Color.White)
            }
        }
    }
}
