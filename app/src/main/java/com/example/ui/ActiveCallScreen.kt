package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.CyberCard
import com.example.ui.components.CyberHeader
import com.example.ui.components.CyberWaveform
import kotlinx.coroutines.launch

@Composable
fun ActiveCallScreen(
    viewModel: CallAssistantViewModel,
    onCallEnded: () -> Unit
) {
    val isCallActive by viewModel.isCallActive.collectAsState()
    val callerName by viewModel.simCallerName.collectAsState()
    val callerPhone by viewModel.simCallerPhone.collectAsState()
    val callLanguage by viewModel.simLanguage.collectAsState()
    val callDuration by viewModel.callDuration.collectAsState()
    val transcriptList by viewModel.transcriptTurns.collectAsState()
    val isAiResponding by viewModel.isAiResponding.collectAsState()
    val isListening by viewModel.isSttListening.collectAsState()
    val partialText by viewModel.sttPartialText.collectAsState()

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // Automatically navigate out when call simulation ends
    LaunchedEffect(isCallActive) {
        if (!isCallActive) {
            onCallEnded()
        }
    }

    // Scroll to bottom whenever a new transcript item is added
    LaunchedEffect(transcriptList.size) {
        if (transcriptList.isNotEmpty()) {
            coroutineScope.launch {
                listState.animateScrollToItem(transcriptList.size - 1)
            }
        }
    }

    // Start listening automatically on first launch
    LaunchedEffect(Unit) {
        // Automatically start microphone monitoring loop
        viewModel.startListeningToCaller()
    }

    // Call Duration Formatter (MM:SS)
    val minutes = (callDuration / 60).toString().padStart(2, '0')
    val seconds = (callDuration % 60).toString().padStart(2, '0')
    val durationText = "$minutes:$seconds"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        CyberHeader(
            title = "OVOZLI ALOQA",
            subtitle = "AI Virtual muloqot tizimi"
        )

        // CALLER INFO HEADER
        CyberCard(
            modifier = Modifier.fillMaxWidth(),
            glowColor = MaterialTheme.colorScheme.secondary,
            borderColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = callerName.uppercase(),
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = callerPhone,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    fontSize = 13.sp,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "TILI: ${callLanguage.uppercase()}",
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Text(
                        text = durationText,
                        color = MaterialTheme.colorScheme.secondary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        // WAVEFORM MONITOR
        CyberCard(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                // If AI is typing or speaking, we pulse magenta; if listening, we pulse cyan; else we idle
                val waveColor = when {
                    isAiResponding -> MaterialTheme.colorScheme.secondary
                    isListening -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                }
                
                CyberWaveform(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    color = waveColor,
                    isActive = isListening || isAiResponding
                )

                // High tech scan line overlay
                Text(
                    text = when {
                        isAiResponding -> "AI JAVOB BERMOQDA..."
                        isListening -> "SOHIBINGIZNI ESHITMOQDAMAN..."
                        else -> "KUTILMOQDA"
                    }.uppercase(),
                    color = waveColor,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 6.dp)
                )
            }
        }

        // LIVE TRANSCRIPT CHAT VIEW
        CyberCard(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp)
            ) {
                Text(
                    text = "FAOL SUHBAT TRANSKRIPTI:",
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .testTag("transcript_feed"),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(transcriptList) { turn ->
                        val isAi = turn.sender == "AI Assistant"
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = if (isAi) Arrangement.Start else Arrangement.End
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(
                                        RoundedCornerShape(
                                            topStart = 12.dp,
                                            topEnd = 12.dp,
                                            bottomStart = if (isAi) 0.dp else 12.dp,
                                            bottomEnd = if (isAi) 12.dp else 0.dp
                                        )
                                    )
                                    .background(
                                        if (isAi) MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)
                                        else MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = if (isAi) MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                                        shape = RoundedCornerShape(
                                            topStart = 12.dp,
                                            topEnd = 12.dp,
                                            bottomStart = if (isAi) 0.dp else 12.dp,
                                            bottomEnd = if (isAi) 12.dp else 0.dp
                                        )
                                    )
                                    .padding(12.dp)
                                    .widthIn(max = 240.dp)
                            ) {
                                Column {
                                    Text(
                                        text = turn.sender.uppercase(),
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isAi) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = turn.text,
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                }
                            }
                        }
                    }
                }

                // Micro live typing bar
                if (partialText.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                            .padding(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = "Mic",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Ovoz aniqlanmoqda: $partialText",
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }

        // FOOTER CONTROLS
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Interactive Mic controller button
            IconButton(
                onClick = {
                    if (isListening) {
                        viewModel.stopListeningToCaller()
                    } else {
                        viewModel.startListeningToCaller()
                    }
                },
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(
                        if (isListening) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) 
                        else MaterialTheme.colorScheme.surfaceVariant
                    )
                    .border(1.dp, if (isListening) MaterialTheme.colorScheme.primary else Color.Gray, CircleShape)
            ) {
                Icon(
                    imageVector = if (isListening) Icons.Default.Mic else Icons.Default.MicOff,
                    contentDescription = "Toggle listening",
                    tint = if (isListening) MaterialTheme.colorScheme.primary else Color.Gray
                )
            }

            Spacer(modifier = Modifier.width(32.dp))

            // RED CALL END BUTTON
            IconButton(
                onClick = { viewModel.endCallSimulation() },
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondary)
                    .testTag("end_call_button")
            ) {
                Icon(
                    imageVector = Icons.Default.CallEnd,
                    contentDescription = "Disconnect Call",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}
