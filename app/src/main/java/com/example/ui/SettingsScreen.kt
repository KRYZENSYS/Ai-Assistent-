package com.example.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.CyberCard
import com.example.ui.components.CyberHeader
import com.example.ui.components.CyberButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: CallAssistantViewModel
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // Retrieve live preference states from ViewModel
    val apiKeyVal by viewModel.apiKey.collectAsState()
    val systemPromptVal by viewModel.systemPrompt.collectAsState()
    val speechLangVal by viewModel.speechLanguage.collectAsState()
    val speechVolVal by viewModel.speechVolume.collectAsState()
    val speechRateVal by viewModel.speechRate.collectAsState()
    val batterySavingVal by viewModel.batterySavingMode.collectAsState()

    // Local state for edits
    var localKey by remember(apiKeyVal) { mutableStateOf(apiKeyVal) }
    var localPrompt by remember(systemPromptVal) { mutableStateOf(systemPromptVal) }
    var localLang by remember(speechLangVal) { mutableStateOf(speechLangVal) }
    var localVol by remember(speechVolVal) { mutableStateOf(speechVolVal) }
    var localRate by remember(speechRateVal) { mutableStateOf(speechRateVal) }
    var localBatterySave by remember(batterySavingVal) { mutableStateOf(batterySavingVal) }

    var isKeyVisible by remember { mutableStateOf(false) }
    var showResetDbConfirm by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        CyberHeader(
            title = "Sozlamalar",
            subtitle = "Suhbat tizimi va ovoz sinxronizatsiyasi"
        )

        // SECTION 1: SECURE API CREDENTIALS
        CyberCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "GROQ API KEY CONFIGURATION",
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                )
                
                OutlinedTextField(
                    value = localKey,
                    onValueChange = { localKey = it },
                    label = { Text("Groq API Kaliti (GROQ_API_KEY)", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)) },
                    textStyle = TextStyle(color = MaterialTheme.colorScheme.onBackground, fontFamily = FontFamily.Monospace),
                    visualTransformation = if (isKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth().testTag("api_key_field"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f)
                    ),
                    trailingIcon = {
                        IconButton(onClick = { isKeyVisible = !isKeyVisible }) {
                            Icon(
                                imageVector = if (isKeyVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = "Toggle visibility",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    },
                    singleLine = true
                )

                Text(
                    text = "Diqqat: API kalitingiz shifrlangan holda xavfsiz SharedPreferences'da saqlanadi va bevosita Groq Chat Completions API serveriga yuboriladi.",
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                    fontSize = 10.sp
                )
            }
        }

        // SECTION 2: SYSTEM PROMPT EDITOR
        CyberCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "AI SYSTEM PROMPT TEMPLATE",
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                )

                OutlinedTextField(
                    value = localPrompt,
                    onValueChange = { localPrompt = it },
                    label = { Text("Yordamchi Prompti", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)) },
                    textStyle = TextStyle(color = MaterialTheme.colorScheme.onBackground),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                        .testTag("system_prompt_field"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f)
                    ),
                    maxLines = 5
                )
            }
        }

        // SECTION 3: TTS SYNTHESIZER CALIBRATION
        CyberCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "SPEECH VOICE CALIBRATION",
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                )

                // Voice Language Selection Dropdown
                Column {
                    Text(
                        text = "OVOZ MATNILI TILI:",
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("auto" to "AUTO", "uz" to "O'ZBEK", "en" to "ENGLISH", "ru" to "РУССКИЙ").forEach { (code, label) ->
                            val isSelected = localLang == code
                            Box(
                                modifier = Modifier
                                    .weight(1.5f)
                                    .background(if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable { localLang = code }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }

                // Volume Slider
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "OVOZ BALANDLIGI:",
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "${(localVol * 100).toInt()}%",
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Slider(
                        value = localVol,
                        onValueChange = { localVol = it },
                        valueRange = 0.1f..1.0f,
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.primary,
                            activeTrackColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }

                // Speed Slider
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "OVOZ TEZLIGI:",
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "${"%.1f".format(localRate)}x",
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Slider(
                        value = localRate,
                        onValueChange = { localRate = it },
                        valueRange = 0.5f..2.0f,
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.primary,
                            activeTrackColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }
        }

        // SECTION 4: POWER OPTIMIZATION
        CyberCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "BATTAREYA TEJASH REJIMINI FAOL",
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Yoqilsa, fon vizual animatsiyalar chastotasi kamayadi va batareya sarfi optimallashadi.",
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                        fontSize = 10.sp
                    )
                }
                Switch(
                    checked = localBatterySave,
                    onCheckedChange = { localBatterySave = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.background,
                        checkedTrackColor = MaterialTheme.colorScheme.primary
                    )
                )
            }
        }

        // MASTER CONTROL ACTIONS
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            CyberButton(
                text = "Sozlamalarni saqlash",
                onClick = {
                    viewModel.saveSettings(
                        key = localKey,
                        prompt = localPrompt,
                        lang = localLang,
                        vol = localVol,
                        rate = localRate,
                        batterySave = localBatterySave
                    )
                    Toast.makeText(context, "Sozlamalar saqlandi", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.fillMaxWidth(),
                testTag = "save_settings_button"
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = {
                        viewModel.resetSettings()
                        Toast.makeText(context, "Barcha sozlamalar asliga qaytarildi", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Text("SOZLAMALARNI TIKLASH", fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                }

                Button(
                    onClick = { showResetDbConfirm = true },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f))
                ) {
                    Text("BAZANI TOZALASH", color = MaterialTheme.colorScheme.secondary, fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                }
            }
        }
    }

    // RESET DB CONFIRMED
    if (showResetDbConfirm) {
        AlertDialog(
            onDismissRequest = { showResetDbConfirm = false },
            title = { Text("Tarixni butunlay o'chirish?", color = MaterialTheme.colorScheme.secondary, fontFamily = FontFamily.Monospace) },
            text = { Text("Ushbu amal barcha saqlangan qo'ng'iroq suhbatlari transkriptlarini va tizim jurnallarini butunlay o'chirib tashlaydi. Ushbu amalni qaytarib bo'lmaydi.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearAllConversations()
                        viewModel.clearAllLogs()
                        showResetDbConfirm = false
                        Toast.makeText(context, "Muloqotlar tarixi tozalandi", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("TOZALASH", color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDbConfirm = false }) {
                    Text("BEKOR QILISH", color = MaterialTheme.colorScheme.onBackground)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface
        )
    }
}
