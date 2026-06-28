package com.example

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.*
import com.example.ui.components.CyberButton
import com.example.ui.components.CyberCard
import com.example.ui.theme.MyApplicationTheme
import com.google.accompanist.permissions.*

// Route IDs
const val ROUTE_DASHBOARD = "dashboard"
const val ROUTE_HISTORY = "history"
const val ROUTE_CALL = "active_call"
const val ROUTE_DIAGNOSTICS = "diagnostics"
const val ROUTE_SETTINGS = "settings"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainAppContainer()
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MainAppContainer() {
    val viewModel: CallAssistantViewModel = viewModel()
    val navController = rememberNavController()

    // 1. DYNAMIC SYSTEM PERMISSIONS GATEWAY
    val permissions = mutableListOf(
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.READ_PHONE_STATE
    ).apply {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            add(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    val permissionState = rememberMultiplePermissionsState(permissions = permissions)

    if (!permissionState.allPermissionsGranted) {
        // Show Cyberpunk Permissions Gateway
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            CyberCard(
                modifier = Modifier.fillMaxWidth(),
                glowColor = MaterialTheme.colorScheme.secondary,
                borderColor = MaterialTheme.colorScheme.secondary
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = "Shield Security",
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = "RUXSATLAR INTEGRATSIYASI",
                        color = MaterialTheme.colorScheme.secondary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "AI Call Assistant ilovasi to'g'ri ishlashi uchun mikrofon (Speech-to-Text) va telefon aloqalarini (monitoring) kuzatish ruxsatnomalarini talab qiladi.",
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                        fontSize = 13.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    CyberButton(
                        text = "Ruxsat berish",
                        onClick = { permissionState.launchMultiplePermissionRequest() },
                        modifier = Modifier.fillMaxWidth(),
                        neonColor = MaterialTheme.colorScheme.secondary,
                        testTag = "grant_permissions_btn"
                    )
                }
            }
        }
    } else {
        // All permissions granted, render main application with navigation
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route
        val isCallActive by viewModel.isCallActive.collectAsState()

        Scaffold(
            bottomBar = {
                // Hide bottom navigation if simulated active call is ongoing
                if (!isCallActive && currentRoute != ROUTE_CALL) {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.background)
                            .border(
                                width = 1.dp,
                                brush = Brush.verticalGradient(
                                    colors = listOf(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), Color.Transparent)
                                ),
                                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                            )
                            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                            .windowInsetsPadding(WindowInsets.navigationBars)
                    ) {
                        NavigationBarItem(
                            selected = currentRoute == ROUTE_DASHBOARD,
                            onClick = { navController.navigate(ROUTE_DASHBOARD) { popUpTo(ROUTE_DASHBOARD) { inclusive = true } } },
                            icon = { Icon(imageVector = Icons.Default.Dashboard, contentDescription = "Console") },
                            label = { Text("Pult", fontFamily = FontFamily.Monospace, fontSize = 10.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                unselectedIconColor = Color.Gray,
                                unselectedTextColor = Color.Gray,
                                indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                            )
                        )
                        NavigationBarItem(
                            selected = currentRoute == ROUTE_HISTORY,
                            onClick = { navController.navigate(ROUTE_HISTORY) },
                            icon = { Icon(imageVector = Icons.Default.CallReceived, contentDescription = "History") },
                            label = { Text("Tarix", fontFamily = FontFamily.Monospace, fontSize = 10.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                unselectedIconColor = Color.Gray,
                                unselectedTextColor = Color.Gray,
                                indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                            )
                        )
                        NavigationBarItem(
                            selected = currentRoute == ROUTE_DIAGNOSTICS,
                            onClick = { navController.navigate(ROUTE_DIAGNOSTICS) },
                            icon = { Icon(imageVector = Icons.Default.Assessment, contentDescription = "Diagnostics") },
                            label = { Text("Log", fontFamily = FontFamily.Monospace, fontSize = 10.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                unselectedIconColor = Color.Gray,
                                unselectedTextColor = Color.Gray,
                                indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                            )
                        )
                        NavigationBarItem(
                            selected = currentRoute == ROUTE_SETTINGS,
                            onClick = { navController.navigate(ROUTE_SETTINGS) },
                            icon = { Icon(imageVector = Icons.Default.Settings, contentDescription = "Settings") },
                            label = { Text("Sozlamalar", fontFamily = FontFamily.Monospace, fontSize = 10.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                unselectedIconColor = Color.Gray,
                                unselectedTextColor = Color.Gray,
                                indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                            )
                        )
                    }
                }
            },
            contentWindowInsets = WindowInsets.safeDrawing,
            modifier = Modifier.fillMaxSize()
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = ROUTE_DASHBOARD,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                composable(ROUTE_DASHBOARD) {
                    DashboardScreen(
                        viewModel = viewModel,
                        onNavigateToCall = {
                            navController.navigate(ROUTE_CALL) {
                                launchSingleTop = true
                            }
                        }
                    )
                }
                composable(ROUTE_HISTORY) {
                    HistoryScreen(viewModel = viewModel)
                }
                composable(ROUTE_CALL) {
                    ActiveCallScreen(
                        viewModel = viewModel,
                        onCallEnded = {
                            navController.popBackStack(ROUTE_DASHBOARD, false)
                        }
                    )
                }
                composable(ROUTE_DIAGNOSTICS) {
                    DiagnosticsScreen(viewModel = viewModel)
                }
                composable(ROUTE_SETTINGS) {
                    SettingsScreen(viewModel = viewModel)
                }
            }
        }
    }
}
