package com.example.ui.screens.library

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.TranslationEntity
import com.example.ui.theme.*
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    viewModel: LibraryViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val progressMap by viewModel.downloadProgressMap.collectAsState()
    
    val snackbarHostState = remember { SnackbarHostState() }
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(Unit) {
        viewModel.snackbarMessage.collectLatest { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Bible Translations",
                        style = MaterialTheme.typography.displayLarge.copy(color = GoldPrimary, fontSize = 24.sp)
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                placeholder = { Text("Search by name, code or language...", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = GoldPrimary) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .testTag("library_search_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GoldPrimary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.surface,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            when (val state = uiState) {
                is LibraryUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = GoldPrimary)
                    }
                }
                is LibraryUiState.Success -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        // Section 1: Installed Translations
                        if (state.downloadedTranslations.isNotEmpty() && searchQuery.isBlank()) {
                            item {
                                Text(
                                    text = "Downloaded Bibles",
                                    style = MaterialTheme.typography.displayMedium.copy(color = GoldPrimary, fontSize = 18.sp),
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            }
                            items(state.downloadedTranslations, key = { "down_${it.id}" }) { trans ->
                                TranslationRowItem(
                                    translation = trans,
                                    downloadProgress = progressMap[trans.id],
                                    onDownload = { viewModel.downloadTranslation(trans.id) },
                                    onDelete = { viewModel.deleteTranslation(trans.id) }
                                )
                            }
                            item {
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "All Translations",
                                    style = MaterialTheme.typography.displayMedium.copy(color = GoldPrimary, fontSize = 18.sp),
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            }
                        }

                        // Section 2: All Available Translations
                        val nonDownloaded = state.translations.filter { !it.isDownloaded }
                        val downloadedResults = if (searchQuery.isNotBlank()) {
                            state.translations.filter { it.isDownloaded }
                        } else emptyList()

                        if (downloadedResults.isNotEmpty()) {
                            items(downloadedResults, key = { "search_down_${it.id}" }) { trans ->
                                TranslationRowItem(
                                    translation = trans,
                                    downloadProgress = progressMap[trans.id],
                                    onDownload = { viewModel.downloadTranslation(trans.id) },
                                    onDelete = { viewModel.deleteTranslation(trans.id) }
                                )
                            }
                        }

                        if (nonDownloaded.isEmpty() && downloadedResults.isEmpty() && state.translations.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(40.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "No translations matched your query",
                                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }
                            }
                        } else {
                            items(nonDownloaded, key = { "non_${it.id}" }) { trans ->
                                TranslationRowItem(
                                    translation = trans,
                                    downloadProgress = progressMap[trans.id],
                                    onDownload = { viewModel.downloadTranslation(trans.id) },
                                    onDelete = { viewModel.deleteTranslation(trans.id) }
                                )
                            }
                        }
                    }
                }
                is LibraryUiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = state.message, color = Color.Red, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        }
    }
}

@Composable
fun TranslationRowItem(
    translation: TranslationEntity,
    downloadProgress: Float?,
    onDownload: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, GoldPrimary.copy(alpha = 0.15f), RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = translation.shortName,
                        fontWeight = FontWeight.Bold,
                        color = GoldPrimary,
                        fontSize = 18.sp,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Box(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.background, RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = translation.language.uppercase(),
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    val isApiBible = translation.id.contains("-") && translation.id.length > 8
                    if (isApiBible) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .background(GoldPrimary.copy(alpha = 0.12f), RoundedCornerShape(4.dp))
                                .border(0.5.dp, GoldPrimary.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "ONLINE / CLOUD",
                                color = GoldPrimary,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = translation.englishName,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 14.sp
                )
                if (translation.localName != translation.englishName) {
                    Text(
                        text = translation.localName,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Action / Status Side
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(64.dp)
            ) {
                if (downloadProgress != null) {
                    // Display current parsing percentage progress
                    val percentage = (downloadProgress * 100).toInt().coerceIn(1, 100)
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(
                            progress = { downloadProgress },
                            color = GoldPrimary,
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$percentage%",
                            color = GoldPrimary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else if (translation.isDownloaded) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            IconButton(
                                onClick = onDelete,
                                modifier = Modifier
                                    .testTag("delete_${translation.id}_button")
                                    .size(48.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete Bible",
                                    tint = HighlightRose
                                )
                            }
                        }
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Downloaded",
                            tint = HighlightMint,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                } else {
                    Button(
                        onClick = onDownload,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.background,
                            contentColor = GoldPrimary
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        modifier = Modifier
                            .height(38.dp)
                            .testTag("download_${translation.id}_button")
                            .border(1.dp, GoldPrimary, RoundedCornerShape(8.dp))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                            Text("Get", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
