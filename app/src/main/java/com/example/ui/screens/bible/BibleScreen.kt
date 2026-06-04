package com.example.ui.screens.bible

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.BookEntity
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BibleScreen(
    viewModel: BibleViewModel,
    onNavigateToChapters: (translationId: String, bookId: String) -> Unit,
    onNavigateToLibrary: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableStateOf(0) } // 0 = OT, 1 = NT
    var dropdownExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Scriptures",
                        style = MaterialTheme.typography.displayLarge.copy(color = GoldPrimary, fontSize = 22.sp)
                    )
                },
                actions = {
                    val state = uiState
                    if (state is BibleUiState.Success) {
                        Box(modifier = Modifier.padding(end = 8.dp)) {
                            FilterChip(
                                selected = true,
                                onClick = { dropdownExpanded = true },
                                label = {
                                    Text(
                                        text = state.activeTranslation.shortName,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.background,
                                        fontSize = 12.sp
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Translate,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.background,
                                        modifier = Modifier.size(14.dp)
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = GoldPrimary,
                                    selectedLabelColor = MaterialTheme.colorScheme.background
                                ),
                                border = null,
                                modifier = Modifier.testTag("translation_selector_chip")
                            )
                            
                            DropdownMenu(
                                expanded = dropdownExpanded,
                                onDismissRequest = { dropdownExpanded = false },
                                modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                            ) {
                                state.downloadedTranslations.forEach { trans ->
                                    DropdownMenuItem(
                                        text = {
                                            Column {
                                                Text(trans.englishName, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                                                Text(trans.shortName, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), fontSize = 12.sp)
                                            }
                                        },
                                        onClick = {
                                            viewModel.setActiveTranslation(trans.id)
                                            dropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
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
            when (val state = uiState) {
                is BibleUiState.Empty -> {
                    // First Run / Empty State
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Book,
                                contentDescription = null,
                                tint = GoldPrimary,
                                modifier = Modifier.size(72.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Download a Bible to get started",
                                style = MaterialTheme.typography.displayMedium.copy(color = MaterialTheme.colorScheme.onBackground, fontSize = 20.sp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Access the Scriptures completely offline anytime, anywhere.",
                                style = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f), fontSize = 14.sp),
                                modifier = Modifier.padding(horizontal = 12.dp)
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            Button(
                                onClick = onNavigateToLibrary,
                                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .testTag("empty_state_go_to_library")
                            ) {
                                Text(
                                    text = "Go to Library",
                                    color = MaterialTheme.colorScheme.background,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            }
                        }
                    }
                }
                is BibleUiState.Success -> {
                    // OT vs NT Tabs
                    TabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = MaterialTheme.colorScheme.background,
                        contentColor = GoldPrimary,
                        indicator = { tabPositions ->
                            TabRowDefaults.SecondaryIndicator(
                                modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                                color = GoldPrimary
                            )
                        }
                    ) {
                        Tab(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            text = { Text("OLD TESTAMENT", fontWeight = FontWeight.Bold) }
                        )
                        Tab(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            text = { Text("NEW TESTAMENT", fontWeight = FontWeight.Bold) }
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Grid Layout
                    val currentBooks = if (selectedTab == 0) state.otBooks else state.ntBooks
                    
                    if (currentBooks.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Loading books...", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))
                        }
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(bottom = 24.dp)
                        ) {
                            items(currentBooks, key = { it.id }) { book ->
                                BookCard(
                                    book = book,
                                    onClick = { onNavigateToChapters(state.activeTranslation.id, book.bookId) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BookCard(
    book: BookEntity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, GoldPrimary.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .testTag("book_card_${book.bookId}")
    ) {
        // Gold left border accent strip matches parent size perfectly
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .fillMaxHeight()
                .width(4.dp)
                .background(GoldPrimary)
        )
        
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 16.dp, top = 20.dp, bottom = 20.dp)
        ) {
            Text(
                text = book.name,
                style = MaterialTheme.typography.displayMedium.copy(color = MaterialTheme.colorScheme.onSurface, fontSize = 18.sp),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${book.totalChapters} Chapters",
                style = MaterialTheme.typography.labelMedium.copy(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), fontSize = 12.sp)
            )
        }
    }
}
