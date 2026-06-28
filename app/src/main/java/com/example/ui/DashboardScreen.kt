package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.ui.text.TextStyle
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.*

@Composable
fun DashboardScreen(
    viewModel: CallAssistantViewModel,
    onNavigateToCall: () -> Unit
) {
    val context = LocalContext.current
    val isRunning by viewModel.isServiceRunning.collectAsState()
    val callState by viewModel.currentCallState.collectAsState()
    val activeNumber by viewModel.activeNumber.collectAsState()
    val conversations by viewModel.conversations.collectAsState()
    val favorites by viewModel.favoriteConversations.collectAsState()

    var inputName by remember { mutableStateOf("Nodir Aliyev") }
    var inputPhone by remember { mutableStateOf("+998 90 123-4567") }
    var selectedLang by remember { mutableStateOf("uz") } // uz, ru, en

    val scrollState = rememberScrollState()

    // Observe if a call becomes active (manually triggered or system receiver triggered), navigate immediately to Call screen
    val isCallActive by viewModel.isCallActive.collectAsState()
    LaunchedEffect(isCallActive) {
        if (isCallActive) {
            onNavigateToCall()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        CyberHeader(
            title = "AI Call Center",
            subtitle = "Tizim holati va qo'ng'iroqlar monitoringi"
        )

        // SECTION 1: MASTER SWITCH
        CyberCard(
            modifier = Modifier.fillMaxWidth(),
            glowColor = if (isRunning) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.secondary,
            borderColor = (if (isRunning) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.secondary).copy(alpha = 0.4f)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isRunning) "AI MONITORING FAOL" else "MONITORING TO'XTATILGAN",
                        color = if (isRunning) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.secondary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isRunning) 
                            "Tizim fonda qo'ng'iroqlarni kuzatmoqda..." 
                            else "AI Mode yoqilsa, qo'ng'iroqlarni monitoring qilish boshlanadi.",
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                        fontSize = 12.sp
                    )
                }

                Switch(
                    checked = isRunning,
                    onCheckedChange = { viewModel.toggleAiMode(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.background,
                        checkedTrackColor = MaterialTheme.colorScheme.tertiary,
                        uncheckedThumbColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                        uncheckedTrackColor = MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier.testTag("service_toggle")
                )
            }
        }

        // SECTION 2: TELEMETRY LED INDICATORS
        CyberCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "DASHBOARD SENSOR PANEL",
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    CyberStatusIndicator(label = "Monitoring", isActive = isRunning)
                    CyberStatusIndicator(label = "Kanal", isActive = callState != "IDLE", activeColor = MaterialTheme.colorScheme.secondary)
                    CyberStatusIndicator(label = "AI Engine", isActive = isRunning && viewModel.apiKey.value.isNotBlank())
                }

                if (callState != "IDLE") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f))
                            .border(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PhoneInTalk,
                                contentDescription = "Active Call",
                                tint = MaterialTheme.colorScheme.secondary
                            )
                            Column {
                                Text(
                                    text = "FAOL ALOQA TARMOQDA!",
                                    color = MaterialTheme.colorScheme.secondary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = "Raqam: $activeNumber | Status: $callState",
                                    color = MaterialTheme.colorScheme.onBackground,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // SECTION 3: QUICK TELEMETRY STATS
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CyberCard(modifier = Modifier.weight(1f)) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = "JAMI CHATLAR",
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${conversations.size}",
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
            CyberCard(modifier = Modifier.weight(1f)) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = "SARALANGANLAR",
                        color = MaterialTheme.colorScheme.secondary,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${favorites.size}",
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        // SECTION 4: INCOMING CALL VOICE SIMULATOR (COMPREHENSIVE SANDBOX)
        CyberCard(
            modifier = Modifier.fillMaxWidth(),
            glowColor = MaterialTheme.colorScheme.primary,
            borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "QO'NG'IROQ MULTI-SIMULATORI",
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                )
                
                Text(
                    text = "Bu bo'lim orqali AI Call Assistant bilan turli tillarda ovozli telefon muloqotini to'liq simulyatsiya qilishingiz mumkin.",
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    fontSize = 11.sp
                )

                OutlinedTextField(
                    value = inputName,
                    onValueChange = { inputName = it },
                    label = { Text("Qo'ng'iroq qiluvchi ismi", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)) },
                    textStyle = TextStyle(color = MaterialTheme.colorScheme.onBackground, fontFamily = FontFamily.Monospace),
                    modifier = Modifier.fillMaxWidth().testTag("sim_name_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f)
                    ),
                    singleLine = true
                )

                OutlinedTextField(
                    value = inputPhone,
                    onValueChange = { inputPhone = it },
                    label = { Text("Telefon raqami", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)) },
                    textStyle = TextStyle(color = MaterialTheme.colorScheme.onBackground, fontFamily = FontFamily.Monospace),
                    modifier = Modifier.fillMaxWidth().testTag("sim_phone_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f)
                    ),
                    singleLine = true
                )

                // Language selection pills
                Column {
                    Text(
                        text = "MULOQOT TILI:",
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("uz" to "O'ZBEKCHA", "en" to "ENGLISH", "ru" to "РУССКИЙ").forEach { (code, display) ->
                            val isSelected = selectedLang == code
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else Color.Transparent)
                                    .border(
                                        width = 1.dp,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f),
                                        shape = RoundedCornerShape(6.dp)
                                    )
                                    .clickable { selectedLang = code }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = display,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                CyberButton(
                    text = "Simulyatsiyani boshlash",
                    onClick = {
                        viewModel.startCallSimulation(
                            callerName = inputName,
                            callerPhone = inputPhone,
                            language = selectedLang
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "start_sim_button"
                )
            }
        }
    }
}
