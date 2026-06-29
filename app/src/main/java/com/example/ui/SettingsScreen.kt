package com.example.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
    val aiProviderVal by viewModel.aiProvider.collectAsState()
    val aiModelVal by viewModel.aiModel.collectAsState()
    val groqKeyVal by viewModel.groqApiKey.collectAsState()
    val geminiKeyVal by viewModel.geminiApiKey.collectAsState()
    val openaiKeyVal by viewModel.openaiApiKey.collectAsState()
    val openrouterKeyVal by viewModel.openrouterApiKey.collectAsState()

    val systemPromptVal by viewModel.systemPrompt.collectAsState()
    val speechLangVal by viewModel.speechLanguage.collectAsState()
    val speechVolVal by viewModel.speechVolume.collectAsState()
    val speechRateVal by viewModel.speechRate.collectAsState()
    val batterySavingVal by viewModel.batterySavingMode.collectAsState()

    // Local state for edits
    var localProvider by remember(aiProviderVal) { mutableStateOf(aiProviderVal) }
    var localModel by remember(aiModelVal) { mutableStateOf(aiModelVal) }
    var localGroqKey by remember(groqKeyVal) { mutableStateOf(groqKeyVal) }
    var localGeminiKey by remember(geminiKeyVal) { mutableStateOf(geminiKeyVal) }
    var localOpenaiKey by remember(openaiKeyVal) { mutableStateOf(openaiKeyVal) }
    var localOpenrouterKey by remember(openrouterKeyVal) { mutableStateOf(openrouterKeyVal) }

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

        // SECTION 1: AI PROVIDER & API KEY CONFIGURATION
        CyberCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "AI PROVIDER SELECTOR",
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(
                        "groq" to "GROQ",
                        "gemini" to "GEMINI",
                        "openai" to "OPENAI",
                        "openrouter" to "OPENROUTER"
                    ).forEach { (providerId, label) ->
                        val isSelected = localProvider == providerId
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant)
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                    shape = RoundedCornerShape(4.dp)
                                )
                                .clickable { 
                                    localProvider = providerId
                                    // Switch local model defaults to keep layout clean
                                    localModel = when (providerId) {
                                        "gemini" -> "gemini-3.5-flash"
                                        "openai" -> "gpt-4o-mini"
                                        "openrouter" -> "google/gemini-2.5-flash"
                                        else -> "llama-3.3-70b-versatile"
                                    }
                                }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Render matching secure API Key input
                val labelText = when (localProvider) {
                    "gemini" -> "Gemini API Kaliti (GEMINI_API_KEY)"
                    "openai" -> "OpenAI API Kaliti (OPENAI_API_KEY)"
                    "openrouter" -> "OpenRouter API Kaliti (OPENROUTER_API_KEY)"
                    else -> "Groq API Kaliti (GROQ_API_KEY)"
                }

                val currentKeyValue = when (localProvider) {
                    "gemini" -> localGeminiKey
                    "openai" -> localOpenaiKey
                    "openrouter" -> localOpenrouterKey
                    else -> localGroqKey
                }

                OutlinedTextField(
                    value = currentKeyValue,
                    onValueChange = { newValue ->
                        when (localProvider) {
                            "gemini" -> localGeminiKey = newValue
                            "openai" -> localOpenaiKey = newValue
                            "openrouter" -> localOpenrouterKey = newValue
                            else -> localGroqKey = newValue
                        }
                    },
                    label = { Text(labelText, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)) },
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

                Spacer(modifier = Modifier.height(4.dp))

                // Render dynamic model configuration
                Text(
                    text = "ACTIVE MODEL CONFIGURATION",
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                )

                // Recommended model preset buttons
                val presets = when (localProvider) {
                    "gemini" -> listOf("gemini-3.5-flash", "gemini-3.1-pro-preview")
                    "openai" -> listOf("gpt-4o-mini", "gpt-4o")
                    "openrouter" -> listOf("google/gemini-2.5-flash", "meta-llama/llama-3.3-70b-instruct")
                    else -> listOf("llama-3.3-70b-versatile", "llama3-8b-8192", "mixtral-8x7b-32768")
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    presets.forEach { presetName ->
                        val isPresetSelected = localModel == presetName
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(if (isPresetSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant)
                                .border(
                                    width = 1.dp,
                                    color = if (isPresetSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                    shape = RoundedCornerShape(4.dp)
                                )
                                .clickable { localModel = presetName }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = presetName.take(18),
                                color = if (isPresetSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }

                // Custom Model Name Input Field
                OutlinedTextField(
                    value = localModel,
                    onValueChange = { localModel = it },
                    label = { Text("Custom model nomi (tanlash yoki yozish)", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)) },
                    textStyle = TextStyle(color = MaterialTheme.colorScheme.onBackground, fontFamily = FontFamily.Monospace),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f)
                    ),
                    singleLine = true
                )

                Text(
                    text = "Diqqat: API kalitlaringiz AES-GCM shifrlash orqali Android Keystore'da xavfsiz saqlanadi va bevosita tanlangan provayder API serveriga yuboriladi.",
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
                        provider = localProvider,
                        model = localModel,
                        groqKey = localGroqKey,
                        geminiKey = localGeminiKey,
                        openaiKey = localOpenaiKey,
                        openrouterKey = localOpenrouterKey,
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
