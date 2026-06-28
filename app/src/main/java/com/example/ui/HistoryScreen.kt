package com.example.ui

import android.content.Intent
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.example.data.local.ConversationEntity
import com.example.ui.components.CyberCard
import com.example.ui.components.CyberHeader
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: CallAssistantViewModel
) {
    val context = LocalContext.current
    val searchQuery by viewModel.searchQuery.collectAsState()
    val conversations by viewModel.conversations.collectAsState()

    var selectedConversation by remember { mutableStateOf<ConversationEntity?>(null) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        CyberHeader(
            title = "Ovozli Tarix",
            subtitle = "Saqlangan suhbatlar va transkriptlar ro'yxati"
        )

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.updateSearchQuery(it) },
            placeholder = { Text("Ism, raqam yoki matn orqali qidirish...", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)) },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("search_history_bar"),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f),
                focusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
            ),
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                        Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear search")
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )

        if (conversations.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = "No history",
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f)
                    )
                    Text(
                        text = "Suhbatlar topilmadi",
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                        fontSize = 14.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .testTag("conversations_list"),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(conversations, key = { it.id }) { item ->
                    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                    val dateString = dateFormat.format(Date(item.timestamp))

                    CyberCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedConversation = item },
                        glowColor = if (item.isFavorite) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary,
                        borderColor = (if (item.isFavorite) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary).copy(alpha = 0.2f)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = item.callerName,
                                        color = MaterialTheme.colorScheme.onBackground,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = item.language.uppercase(),
                                            color = MaterialTheme.colorScheme.primary,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = item.phoneNumber,
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                                    fontSize = 13.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.AccessTime,
                                            contentDescription = "Time",
                                            modifier = Modifier.size(12.dp),
                                            tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = dateString,
                                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                                            fontSize = 11.sp
                                        )
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.HourglassEmpty,
                                            contentDescription = "Duration",
                                            modifier = Modifier.size(12.dp),
                                            tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "${item.durationSeconds}s",
                                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                IconButton(onClick = { viewModel.toggleFavorite(item) }) {
                                    Icon(
                                        imageVector = if (item.isFavorite) Icons.Filled.Star else Icons.Default.StarBorder,
                                        contentDescription = "Favorite",
                                        tint = if (item.isFavorite) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f)
                                    )
                                }
                                IconButton(onClick = { 
                                    selectedConversation = item
                                    showDeleteConfirmDialog = true
                                }) {
                                    Icon(
                                        imageVector = Icons.Outlined.Delete,
                                        contentDescription = "Delete",
                                        tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // DETAIL TRANSCRIPT VIEW DIALOG
    selectedConversation?.let { conversation ->
        if (!showDeleteConfirmDialog) {
            AlertDialog(
                onDismissRequest = { selectedConversation = null },
                properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .fillMaxHeight(0.85f),
                confirmButton = {
                    TextButton(onClick = { selectedConversation = null }) {
                        Text("YOPISH", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    // SHARE/EXPORT CHAT
                    IconButton(
                        onClick = {
                            val file = viewModel.exportChatAsText(conversation)
                            if (file != null) {
                                val uri = FileProvider.getUriForFile(
                                    context,
                                    "${context.packageName}.provider",
                                    file
                                )
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_STREAM, uri)
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Suhbatni ulashish"))
                            }
                        }
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(imageVector = Icons.Outlined.Share, contentDescription = "Export", tint = MaterialTheme.colorScheme.primary)
                            Text("ULASHISH", color = MaterialTheme.colorScheme.primary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                },
                title = {
                    Column {
                        Text(
                            text = conversation.callerName.uppercase(),
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = conversation.phoneNumber,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                },
                text = {
                    val turns = viewModel.deserializeTurns(conversation.transcriptJson)
                    Divider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(turns) { turn ->
                            val isAi = turn.sender == "AI Assistant"
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = if (isAi) Alignment.Start else Alignment.End
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
                                            if (isAi) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                        )
                                        .border(
                                            width = 1.dp,
                                            color = if (isAi) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f),
                                            shape = RoundedCornerShape(
                                                topStart = 12.dp,
                                                topEnd = 12.dp,
                                                bottomStart = if (isAi) 0.dp else 12.dp,
                                                bottomEnd = if (isAi) 12.dp else 0.dp
                                            )
                                        )
                                        .padding(12.dp)
                                        .widthIn(max = 260.dp)
                                ) {
                                    Column {
                                        Text(
                                            text = turn.sender.uppercase(),
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isAi) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
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
                },
                containerColor = MaterialTheme.colorScheme.surface
            )
        }
    }

    // DELETE CONFIRMATION DIALOG
    if (showDeleteConfirmDialog && selectedConversation != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("Muloqotni o'chirish?", color = MaterialTheme.colorScheme.secondary, fontFamily = FontFamily.Monospace) },
            text = { Text("Ushbu suhbat transkripti butunlay o'chiriladi. Ushbu amalni qaytarib bo'lmaydi.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        selectedConversation?.id?.let { viewModel.deleteConversation(it) }
                        selectedConversation = null
                        showDeleteConfirmDialog = false
                    }
                ) {
                    Text("O'CHIRISH", color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("BEKOR QILISH", color = MaterialTheme.colorScheme.onBackground)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface
        )
    }
}
