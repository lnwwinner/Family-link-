package com.example.ui.screens

import android.content.Intent
import java.util.Locale
import android.net.Uri
import android.telephony.SmsManager
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.example.data.model.User
import com.example.ui.FamilyCareViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmergencyCenterScreen(viewModel: FamilyCareViewModel) {
    val isTh by viewModel.isThaiLanguage.collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    // GPS Position Simulator State
    var liveLatitude by remember { mutableStateOf(13.7563) }
    var liveLongitude by remember { mutableStateOf(100.5018) }
    var isLocating by remember { mutableStateOf(false) }

    // Simulating GPS Hot Jitter update on button click
    fun refreshGPS() {
        isLocating = true
        coroutineScope.launch {
            kotlinx.coroutines.delay(1000)
            liveLatitude = 13.7563 + ((1..100).random() - 50) / 10000.0
            liveLongitude = 100.5018 + ((1..100).random() - 50) / 10000.0
            isLocating = false
            Toast.makeText(context, tr(isTh, "GPS Coordinates Refreshed!", "อัปเดตพิกัดดาวเทียมเสร็จสิ้น!"), Toast.LENGTH_SHORT).show()
        }
    }

    val mapUrl = "https://www.google.com/maps/search/?api=1&query=$liveLatitude,$liveLongitude"

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = tr(isTh, "Emergency Gateway", "พอร์ทัลกู้ชีพฉุกเฉิน"),
                            fontWeight = FontWeight.Black,
                            fontSize = 20.sp,
                            color = Color.White
                        )
                        Text(
                            text = tr(isTh, "Active SOS Router • Dual-Band Backup", "เชื่อมโยงกู้ภัยสด • ระบบสำรองสัญญาณแบบคู่"),
                            fontSize = 11.sp,
                            color = PrimaryAccent,
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
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CozySlateBg)
            )
        },
        containerColor = CozySlateBg
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            // CRITICAL HEADER ACTIVE MESSAGE
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFC30000)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().testTag("sos_giant_banner")
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Emergency, contentDescription = "SOS", tint = Color.White, modifier = Modifier.size(24.dp))
                            Text(
                                text = tr(isTh, "ONE-TOUCH IMMEDIATE BROADCAST", "กดเพื่อแจ้งพิกัดวิกฤติถึงทุกคนทันที"),
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 12.sp
                            )
                        }

                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    viewModel.triggerEmergencySOS("CRITICAL MANUAL TRIGGER FROM EMERGENCY WINDOW! Current GPS: $liveLatitude, $liveLongitude")
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                            shape = CircleShape,
                            modifier = Modifier
                                .size(96.dp)
                                .border(4.dp, ContrastAmber, CircleShape)
                                .testTag("one_touch_sos_button"),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(
                                text = "SOS",
                                color = Color(0xFFC30000),
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Black
                            )
                        }

                        Text(
                            text = tr(isTh, "Transmits medical profile state & GPS link via cloud & SMS", "จะส่งประวัติสาระสำคัญแพทย์และพิกัดแผนที่ผ่าน คลาวด์และ SMS"),
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // GPS GEO POSITION SENSOR BAR
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkCardBg),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.Default.GpsFixed, contentDescription = "GPS", tint = SaniGreen, modifier = Modifier.size(18.dp))
                                Text(tr(isTh, "High Precision GPS Tracker", "ระบบวัดพิกัดระบุตำแหน่งดาวเทียม"), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }

                            IconButton(
                                onClick = { refreshGPS() },
                                modifier = Modifier.size(24.dp)
                            ) {
                                if (isLocating) {
                                    CircularProgressIndicator(modifier = Modifier.size(14.dp), color = SaniGreen, strokeWidth = 2.dp)
                                } else {
                                    Icon(Icons.Default.Refresh, contentDescription = "Refresh GPS", tint = SaniGreen)
                                }
                            }
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(CozySlateBg, RoundedCornerShape(8.dp))
                                .padding(10.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Latitude:", color = Color.LightGray, fontSize = 11.sp)
                                    Text(String.format(Locale.getDefault(), "%.6f", liveLatitude), color = Color.White, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                                }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Longitude:", color = Color.LightGray, fontSize = 11.sp)
                                    Text(String.format(Locale.getDefault(), "%.6f", liveLongitude), color = Color.White, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    val mapIntent = Intent(Intent.ACTION_VIEW, Uri.parse(mapUrl))
                                    context.startActivity(mapIntent)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = CozySlateBg),
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Map, contentDescription = "Map", tint = Color.White, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(tr(isTh, "View Map", "เปิดดูแผนที่"), color = Color.White, fontSize = 11.sp)
                            }

                            Button(
                                onClick = {
                                    try {
                                        val smsIntent = Intent(Intent.ACTION_VIEW, Uri.parse("sms:0812345678"))
                                        smsIntent.putExtra("sms_body", "CRITICAL EMERGENCY! Please check Grandpa's live GPS location: $mapUrl")
                                        context.startActivity(smsIntent)
                                        Toast.makeText(context, "SMS dispatcher ready", Toast.LENGTH_SHORT).show()
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Failed to launch SMS", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = SaniGreen),
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Sms, contentDescription = "SMS", tint = CozySlateBg, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(tr(isTh, "Dispatch SMS", "ส่งข้อความด่วน"), color = CozySlateBg, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // CRITICAL PRIORITY RESPONSIBILITY CHAIN
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkCardBg),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = tr(isTh, "EMERGENCY RESPONSE PROTOCOL CHAIN", "ลำดับขั้นตอนดูแลคุ้มครองแบบสเต็ป"),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = ContrastAmber
                        )

                        val chainSteps = listOf(
                            Triple("1", tr(isTh, "Family Group Broadcasts", "ส่งสัญญาณวิกฤตถึงคนในกลุ่มบ้าน"), tr(isTh, "Delivers Instant notification & voice alert streams to all family members", "ยิงแจ้งเตือนเปิดเสียงดังทลายโหมดห้ามรบกวนมือถือครอบครัวทุกคน")),
                            Triple("2", tr(isTh, "Caregiver Hotlines", "ส่งสายตรงไปถึงโทรศัพท์ผู้ดูแลหลัก"), tr(isTh, "Auto-dials primary caregiver mobile contact immediately", "ตัดโทรออกแบบความคมชัดสูงพร้อมข้อมูลอาการเบื้องต้น")),
                            Triple("3", tr(isTh, "State Ambulance & Emergency Hub", "ประสานระบบกู้ชีพแห่งชาติสายการแพทย์"), tr(isTh, "Triggers localized priority response lines corresponding to GPS country", "ต่อโทรสายกู้ชีพแพทย์สนามความเร็วสูงตามพื้นที่ของท่าน"))
                        )

                        chainSteps.forEach { (step, header, details) ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .background(PrimaryAccent, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = step, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = header, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    Text(text = details, color = Color.LightGray, fontSize = 10.sp)
                                }
                            }

                            if (step != "3") {
                                Divider(color = CozySlateBg.copy(alpha = 0.5f), modifier = Modifier.padding(start = 34.dp))
                            }
                        }
                    }
                }
            }

            // ONE TOUCH DIRECT EMERGENCY TELEPHONY
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkCardBg),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = tr(isTh, "GOVERNMENT EMERGENCY HOTLINES", "สายด่วนสืบราชการฉุกเฉินและอุบัติเหตุ"),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = ContrastAmber
                        )

                        Text(
                            text = tr(isTh, "Thailand Operations", "ศูนย์ปฏิบัติการประเทศไทย"),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )

                        val thLines = listOf(
                            Triple("1669", tr(isTh, "National Paramedic Service", "สายการแพทย์และกู้ชีพฉุกเฉินแห่งชาติ (สพฉ.)"), Icons.Default.MedicalServices),
                            Triple("191", tr(isTh, "Police Emergency Force", "เหตุด่วนเหตุร้าย กองบังคับการตำรวจ"), Icons.Default.LocalPolice),
                            Triple("199", tr(isTh, "Fire & Rescue Operations", "เหตุอัคคีภัย คลังดับเพลิงและกู้ภัย"), Icons.Default.FireTruck),
                            Triple("112", tr(isTh, "National Tourist & Universal Aid", "ตำรวจท่องเที่ยวเพื่อการปกป้องครอบสากล"), Icons.Default.Public)
                        )

                        thLines.forEach { (number, desc, icon) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(CozySlateBg, RoundedCornerShape(8.dp))
                                    .clickable {
                                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$number"))
                                        context.startActivity(intent)
                                    }
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(icon, contentDescription = desc, tint = PrimaryAccent, modifier = Modifier.size(18.dp))
                                    Column {
                                        Text(text = number, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Text(text = desc, color = Color.LightGray, fontSize = 10.sp)
                                    }
                                }

                                Icon(
                                    imageVector = Icons.Default.Call,
                                    contentDescription = "Dial",
                                    tint = SaniGreen,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = tr(isTh, "International Protocols", "ศูนย์กู้วิกฤติต่างประเทศ"),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )

                        val intLines = listOf(
                            Triple("911", "United States & Canada Standard Line", Icons.Default.Security),
                            Triple("999", "United Kingdom, Hong Kong & Singapore", Icons.Default.Shield),
                            Triple("112", "European Union Universal Rescuer", Icons.Default.Language)
                        )

                        intLines.forEach { (number, desc, icon) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(CozySlateBg, RoundedCornerShape(8.dp))
                                    .clickable {
                                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$number"))
                                        context.startActivity(intent)
                                    }
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(icon, contentDescription = desc, tint = ContrastAmber, modifier = Modifier.size(18.dp))
                                    Column {
                                        Text(text = number, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Text(text = desc, color = Color.LightGray, fontSize = 10.sp)
                                    }
                                }

                                Icon(
                                    imageVector = Icons.Default.Call,
                                    contentDescription = "Dial",
                                    tint = SaniGreen,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccessibilitySettingsScreen(viewModel: FamilyCareViewModel) {
    val isTh by viewModel.isThaiLanguage.collectAsState()
    val context = LocalContext.current
    var isPowerTripleEnabled by remember { mutableStateOf(true) }
    var isVolumeUpLongEnabled by remember { mutableStateOf(true) }
    var isVolumeDownLongEnabled by remember { mutableStateOf(true) }
    var isFloatingSosEnabled by remember { mutableStateOf(true) }
    var isLockscreenCardEnabled by remember { mutableStateOf(true) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(tr(isTh, "Accessibility Key Bindings", "บริการช่วยเหลือและการเข้าถึงแบบคีย์"), color = Color.White, fontWeight = FontWeight.Bold) },
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
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = tr(isTh, "PHYSICAL HARDWARE HOTKEYS", "ตั้งค่าปุ่มข้างและทางลัดฮาร์ดแวร์จริง"),
                color = ContrastAmber,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
            )

            // TRIPLE PRESS POWER SOS
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkCardBg, RoundedCornerShape(10.dp))
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(tr(isTh, "Triple-Press Power SOS", "กดปุ่ม Power ติดต่อกัน 3 ครั้ง"), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(tr(isTh, "Launches direct SOS broadcast with audio captures even when locked", "เปิดใช้งาน SOS แบบทลายความเงียบพร้อมรูป และเสียงแม้มือถือล็อค"), color = Color.LightGray, fontSize = 11.sp)
                }
                Switch(checked = isPowerTripleEnabled, onCheckedChange = { isPowerTripleEnabled = it })
            }

            // VOLUME UP LONG PRESS
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkCardBg, RoundedCornerShape(10.dp))
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(tr(isTh, "Volume Up LONG PRESS", "ปุ่มเพิ่มเสียงค้างไว้เพื่อส่งเครือข่าย"), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(tr(isTh, "Keep pressed for 3.5 seconds to dispatch family security text", "กดแช่ยาว 3.5 วินาทีเพื่อเปิดระบบติดต่อคลาวด์กู้ชีพ"), color = Color.LightGray, fontSize = 11.sp)
                }
                Switch(checked = isVolumeUpLongEnabled, onCheckedChange = { isVolumeUpLongEnabled = it })
            }

            // VOLUME DOWN LONG PRESS CALL CAREGIVER
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkCardBg, RoundedCornerShape(10.dp))
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(tr(isTh, "Volume Down LONG PRESS", "ปุ่มลดเสียงค้างเพื่อต่อสายผู้ดูแล"), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(tr(isTh, "Speed dials the primary designated caregiver", "ตัดสายด่วนความร้อนสูงไปถึงผู้มีหน้าที่ดูแลหลักด่วน"), color = Color.LightGray, fontSize = 11.sp)
                }
                Switch(checked = isVolumeDownLongEnabled, onCheckedChange = { isVolumeDownLongEnabled = it })
            }

            Text(
                text = tr(isTh, "SCREEN LOCK & OVERLAY PORTALS", "บริการโอเวอร์เลย์และล็อคหน้าจอเซพตี้"),
                color = ContrastAmber,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
            )

            // FLOATING SOS BUTTON
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkCardBg, RoundedCornerShape(10.dp))
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(tr(isTh, "Floating Draggable SOS Icon", "ปุ่มช่วยเหลือแบบลอยข้างจอ"), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(tr(isTh, "Draws a high contrast SOS trigger button always on top of other apps", "แสดงผลปุ่มสีแดงตลอดเวลา เลื่อนได้ เพื่อกู้ชีวิตทันท่วงที"), color = Color.LightGray, fontSize = 11.sp)
                }
                Switch(checked = isFloatingSosEnabled, onCheckedChange = { isFloatingSosEnabled = it })
            }

            // LOCKSCREEN ACCESS CARD
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkCardBg, RoundedCornerShape(10.dp))
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(tr(isTh, "Lock Screen Emergency Card", "แสดงบัตรข้อมูลกู้ภัยบนหน้าจอล็อค"), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(tr(isTh, "Permit paramedic responders to read crucial profile stats without unlock code", "อนุญาตให้แพทย์กังวลใจสแกนอ่านข้อมูลโรค ยาแพ้ ข้อมูลประสาน ได้โดยไม่ต้องผ่านรหัสเข้าโทรศัพท์มือถือ"), color = Color.LightGray, fontSize = 11.sp)
                }
                Switch(checked = isLockscreenCardEnabled, onCheckedChange = { isLockscreenCardEnabled = it })
            }

            Button(
                onClick = {
                    Toast.makeText(context, tr(isTh, "Accessibility parameters committed dynamically!", "อัปเดตโมดูลปุ่มลัดช่วยเหลือความปลอดภัยสำเร็จ!"), Toast.LENGTH_SHORT).show()
                    viewModel.navigateBack()
                },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryAccent),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(tr(isTh, "Save Settings", "บันทึกข้อมูล"), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}
