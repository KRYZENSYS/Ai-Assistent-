package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.CyberCard
import com.example.ui.components.CyberHeader
import com.example.ui.components.CyberButton
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DiagnosticsScreen(
    viewModel: CallAssistantViewModel
) {
    val isConnected by viewModel.isInternetConnected.collectAsState()
    val isRunning by viewModel.isServiceRunning.collectAsState()
    val apiStatus by viewModel.apiTestStatus.collectAsState()
    val systemLogs by viewModel.systemLogs.collectAsState()

    val listState = rememberLazyListState()

    // Automatically check connection on start
    LaunchedEffect(Unit) {
        viewModel.checkConnectivity()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        CyberHeader(
            title = "DIAGNOSTIKA",
            subtitle = "Tizim auditi va integratsiya testi"
        )

        // SECTION 1: HARDWARE AND PERMISSIONS STATUS
        CyberCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "HARDWARE AND MODULE SIGNALS",
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                )

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
                            imageVector = if (isConnected) Icons.Default.Wifi else Icons.Default.WifiOff,
                            contentDescription = "Wifi",
                            tint = if (isConnected) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.secondary
                        )
                        Text(
                            text = "INTERNET ALOQA",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                (if (isConnected) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.secondary).copy(alpha = 0.15f)
                            )
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (isConnected) "ONLINE" else "OFFLINE",
                            color = if (isConnected) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.secondary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

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
                            imageVector = Icons.Default.CircleNotifications,
                            contentDescription = "Service Status",
                            tint = if (isRunning) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.secondary
                        )
                        Text(
                            text = "FOREGROUND XIZMATI",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                (if (isRunning) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.secondary).copy(alpha = 0.15f)
                            )
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (isRunning) "ISHLAMOQDA" else "TO'XTATILGAN",
                            color = if (isRunning) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.secondary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        // SECTION 2: REMOTE API TEST CONSOLE
        CyberCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "GROQ API PING TEST CONSOLE",
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                        .padding(12.dp)
                ) {
                    Text(
                        text = apiStatus,
                        color = when {
                            apiStatus.startsWith("MUVAFFAQIYATLI") -> MaterialTheme.colorScheme.tertiary
                            apiStatus.startsWith("XATO") -> MaterialTheme.colorScheme.secondary
                            apiStatus.startsWith("KUTILMOQDA") -> MaterialTheme.colorScheme.primary
                            else -> MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                        },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CyberButton(
                        text = "Internet tekshirish",
                        onClick = { viewModel.checkConnectivity() },
                        modifier = Modifier.weight(1f)
                    )
                    CyberButton(
                        text = "API Test qilish",
                        onClick = { viewModel.testApiConnection() },
                        modifier = Modifier.weight(1f),
                        testTag = "api_test_btn"
                    )
                }
            }
        }

        // SECTION 3: LIVE LOG MONITOR (LOGCAT GRID)
        CyberCard(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "LIVE DIAGNOSTICS LOG TERMINAL",
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 0.5.sp
                    )

                    IconButton(
                        onClick = { viewModel.clearAllLogs() },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = "Clear logs",
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Divider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))

                if (systemLogs.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "TERMINAL JURNALI BO'SH",
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f),
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .testTag("logs_terminal_feed"),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(systemLogs, key = { it.id }) { log ->
                            val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                            val timeText = timeFormat.format(Date(log.timestamp))

                            // Highlight color coded by Log Level
                            val levelColor = when (log.level.uppercase()) {
                                "ERROR" -> MaterialTheme.colorScheme.secondary // Hot pink
                                "WARN" -> Color(0xFFFFB300) // Orange/Yellow
                                "INFO" -> MaterialTheme.colorScheme.tertiary // Lime green
                                else -> MaterialTheme.colorScheme.primary // Cyan
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Text(
                                    text = "[$timeText]",
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = log.level.uppercase().take(1),
                                    color = levelColor,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = "${log.tag}:",
                                    color = levelColor.copy(alpha = 0.8f),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = log.message,
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.85f),
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    lineHeight = 14.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
