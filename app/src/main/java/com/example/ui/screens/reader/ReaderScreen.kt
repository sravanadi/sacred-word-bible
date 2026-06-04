package com.example.ui.screens.reader

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.VerseEntity
import com.example.ui.theme.*
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ReaderScreen(
    viewModel: ReaderViewModel,
    translationId: String,
    bookId: String,
    initialChapter: Int,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Initialize ViewModel once
    LaunchedEffect(translationId, bookId) {
        viewModel.initialize(translationId, bookId, initialChapter)
    }

    val uiState by viewModel.uiState.collectAsState()
    val currentChapter by viewModel.currentChapter.collectAsState()
    val verses by viewModel.verses.collectAsState()

    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    // Modal Bottom Sheet State for selected verse
    var selectedVerseForActions by remember { mutableStateOf<VerseEntity?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val book = uiState.book
    val totalChapters = book?.totalChapters ?: 1

    // Setup Horizontal Pager for swiping chapters
    val pagerState = rememberPagerState(
        initialPage = (initialChapter - 1).coerceAtLeast(0),
        pageCount = { totalChapters }
    )

    // Sync pager swipe back to active chapter state
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }
            .distinctUntilChanged()
            .collect { page ->
                viewModel.setChapter(page + 1)
            }
    }

    // Sync active chapter state of ViewModel (from arrows/selectors) to pager page
    LaunchedEffect(currentChapter) {
        if (pagerState.currentPage != currentChapter - 1) {
            pagerState.scrollToPage(currentChapter - 1)
        }
    }

    // Determine layout direction for RTL support
    val layoutDirection = if (uiState.textDirection == "rtl") LayoutDirection.Rtl else LayoutDirection.Ltr

    CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = "${book?.name ?: ""} $currentChapter",
                            style = MaterialTheme.typography.displayMedium.copy(color = GoldPrimary, fontSize = 20.sp)
                        )
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = GoldPrimary
                            )
                        }
                    },
                    actions = {
                        // Quick toggle bookmark for the chapter (Verse 1 as fallback delegate)
                        val v1 = verses.firstOrNull()
                        if (v1 != null) {
                            val isV1Bookmarked = uiState.isBookmarkedMap[v1.id] ?: false
                            IconButton(
                                onClick = { viewModel.toggleBookmark(v1) },
                                modifier = Modifier
                                    .testTag("top_bar_bookmark_button")
                                    .size(48.dp)
                            ) {
                                Icon(
                                    imageVector = if (isV1Bookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                    contentDescription = "Bookmark First Verse",
                                    tint = GoldPrimary
                                )
                            }
                        }
                        
                        // Font controls
                        IconButton(
                            onClick = { viewModel.adjustFontSize(increment = false) },
                            enabled = uiState.fontSize > 14,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Text("A-", color = if (uiState.fontSize > 14) GoldPrimary else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                        IconButton(
                            onClick = { viewModel.adjustFontSize(increment = true) },
                            enabled = uiState.fontSize < 26,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Text("A+", color = if (uiState.fontSize < 26) GoldPrimary else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
            },
            bottomBar = {
                BottomAppBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = GoldPrimary,
                    modifier = Modifier.height(64.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Previous chapter trigger
                        IconButton(
                            onClick = { viewModel.previousChapter() },
                            enabled = currentChapter > 1,
                            modifier = Modifier
                                .testTag("prev_chapter_button")
                                .size(48.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Previous Chapter"
                            )
                        }

                        // Label status
                        Text(
                            text = "Chapter $currentChapter of $totalChapters",
                            color = MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.labelMedium
                        )

                        // Next chapter trigger
                        IconButton(
                            onClick = { viewModel.nextChapter() },
                            enabled = currentChapter < totalChapters,
                            modifier = Modifier
                                    .testTag("next_chapter_button")
                                    .size(48.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Next Chapter"
                            )
                        }
                    }
                }
            },
            containerColor = MaterialTheme.colorScheme.background,
            modifier = modifier.fillMaxSize()
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (verses.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = GoldPrimary)
                    }
                } else {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize()
                    ) { page ->
                        if (page == currentChapter - 1) {
                            // Load the chapter column contents dynamically for the active page
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 16.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp),
                                contentPadding = PaddingValues(top = 4.dp, bottom = 32.dp)
                            ) {
                                items(verses, key = { it.id }) { verse ->
                                    val highlightColorHex = remember(verse.id, uiState.highlightMap) {
                                        uiState.highlightMap[verse.id]
                                    }
                                    val highlightColor = remember(highlightColorHex) {
                                        getHighlightColor(highlightColorHex)
                                    }
                                    val isBookmarked = remember(verse.id, uiState.isBookmarkedMap) {
                                        uiState.isBookmarkedMap[verse.id] ?: false
                                    }
                                    val onLongClickLambda = remember(verse) {
                                        { selectedVerseForActions = verse }
                                    }

                                    VerseRow(
                                        verse = verse,
                                        fontSize = uiState.fontSize,
                                        highlightColor = highlightColor,
                                        isBookmarked = isBookmarked,
                                        onLongClick = onLongClickLambda
                                    )
                                }
                            }
                        } else {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = GoldPrimary)
                            }
                        }
                    }
                }
            }

            // Modal Action Bottom Sheet triggers
            if (selectedVerseForActions != null) {
                val activeVerse = selectedVerseForActions!!
                val isBookmarked = uiState.isBookmarkedMap[activeVerse.id] ?: false
                val activeHighlightHex = uiState.highlightMap[activeVerse.id]

                ModalBottomSheet(
                    onDismissRequest = { selectedVerseForActions = null },
                    sheetState = sheetState,
                    containerColor = MaterialTheme.colorScheme.surface,
                    dragHandle = { BottomSheetDefaults.DragHandle(color = GoldPrimary) }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(bottom = 24.dp, start = 16.dp, end = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Verse ${activeVerse.verseNumber}",
                            style = MaterialTheme.typography.displayMedium.copy(color = GoldPrimary, fontSize = 20.sp),
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        // Row 1: Highlighting circles
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 24.dp)
                        ) {
                            // Circle 1: Purple
                            HighlightCircle(color = HighlightPurple, isSelected = activeHighlightHex == HEX_HighlightPurple) {
                                viewModel.highlightVerse(activeVerse.id, HEX_HighlightPurple)
                                coroutineScope.launch { sheetState.hide() }.invokeOnCompletion { selectedVerseForActions = null }
                            }
                            // Circle 2: Amber
                            HighlightCircle(color = HighlightAmber, isSelected = activeHighlightHex == HEX_HighlightAmber) {
                                viewModel.highlightVerse(activeVerse.id, HEX_HighlightAmber)
                                coroutineScope.launch { sheetState.hide() }.invokeOnCompletion { selectedVerseForActions = null }
                            }
                            // Circle 3: Mint
                            HighlightCircle(color = HighlightMint, isSelected = activeHighlightHex == HEX_HighlightMint) {
                                viewModel.highlightVerse(activeVerse.id, HEX_HighlightMint)
                                coroutineScope.launch { sheetState.hide() }.invokeOnCompletion { selectedVerseForActions = null }
                            }
                            // Circle 4: Rose
                            HighlightCircle(color = HighlightRose, isSelected = activeHighlightHex == HEX_HighlightRose) {
                                viewModel.highlightVerse(activeVerse.id, HEX_HighlightRose)
                                coroutineScope.launch { sheetState.hide() }.invokeOnCompletion { selectedVerseForActions = null }
                            }
                            // Circle 5: Sky
                            HighlightCircle(color = HighlightSky, isSelected = activeHighlightHex == HEX_HighlightSky) {
                                viewModel.highlightVerse(activeVerse.id, HEX_HighlightSky)
                                coroutineScope.launch { sheetState.hide() }.invokeOnCompletion { selectedVerseForActions = null }
                            }

                            // Eraser to clear highlight
                            if (activeHighlightHex != null) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .border(1.dp, HighlightRose, CircleShape)
                                        .combinedClickable {
                                            viewModel.removeHighlight(activeVerse.id)
                                            coroutineScope.launch { sheetState.hide() }.invokeOnCompletion { selectedVerseForActions = null }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.HighlightOff,
                                        contentDescription = "Clear Highlight",
                                        tint = HighlightRose,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.background, modifier = Modifier.padding(bottom = 16.dp))

                        // Row 2: Actions bookmarks, copy, share
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            // Bookmark Icon Action
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable {
                                viewModel.toggleBookmark(activeVerse)
                                coroutineScope.launch { sheetState.hide() }.invokeOnCompletion { selectedVerseForActions = null }
                            }) {
                                Icon(
                                    imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                    tint = GoldPrimary,
                                    contentDescription = "Bookmark"
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(if (isBookmarked) "Unbookmark" else "Bookmark", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
                            }

                            // Copy Action
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable {
                                val textToCopy = "${book?.name} ${activeVerse.chapterNumber}:${activeVerse.verseNumber} - ${activeVerse.text}"
                                clipboardManager.setText(AnnotatedString(textToCopy))
                                coroutineScope.launch { sheetState.hide() }.invokeOnCompletion { 
                                    selectedVerseForActions = null
                                }
                            }) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    tint = GoldPrimary,
                                    contentDescription = "Copy"
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Copy", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
                            }

                            // Share Action
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable {
                                try {
                                    val shareText = "${book?.name} ${activeVerse.chapterNumber}:${activeVerse.verseNumber} - ${activeVerse.text} (${translationId})"
                                    val sendIntent = Intent().apply {
                                        action = Intent.ACTION_SEND
                                        putExtra(Intent.EXTRA_TEXT, shareText)
                                        type = "text/plain"
                                    }
                                    val shareIntent = Intent.createChooser(sendIntent, "Share verse reference").apply {
                                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    }
                                    context.startActivity(shareIntent)
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    android.widget.Toast.makeText(
                                        context,
                                        "Sharing is not available on this device",
                                        android.widget.Toast.LENGTH_SHORT
                                    ).show()
                                }
                                coroutineScope.launch { sheetState.hide() }.invokeOnCompletion { selectedVerseForActions = null }
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    tint = GoldPrimary,
                                    contentDescription = "Share"
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Share", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HighlightCircle(
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(color)
            .border(
                if (isSelected) 3.dp else 0.dp,
                if (isSelected) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                CircleShape
            )
            .clickable(onClick = onClick)
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun VerseRow(
    verse: VerseEntity,
    fontSize: Int,
    highlightColor: Color?,
    isBookmarked: Boolean,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = highlightColor?.copy(alpha = 0.25f) ?: Color.Transparent

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .combinedClickable(
                onLongClick = onLongClick,
                onClick = {} // defined to enable tactile feedback on ripples
            )
            .padding(vertical = 12.dp, horizontal = 8.dp)
            .testTag("verse_row_${verse.verseNumber}"),
        verticalAlignment = Alignment.Top
    ) {
        // Verse Number in gold
        Text(
            text = "${verse.verseNumber} ",
            color = GoldPrimary,
            fontWeight = FontWeight.Bold,
            fontSize = (fontSize - 4).coerceAtLeast(10).sp,
            textAlign = TextAlign.Start,
            modifier = Modifier.width(28.dp)
        )

        // Verse Text
        Text(
            text = verse.text,
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontSize = fontSize.sp,
                lineHeight = (fontSize * 1.6).sp
            ),
            modifier = Modifier.weight(1f)
        )

        if (isBookmarked) {
            Icon(
                imageVector = Icons.Default.Bookmark,
                contentDescription = "Bookmarked",
                tint = GoldPrimary,
                modifier = Modifier
                    .size(16.dp)
                    .padding(start = 4.dp)
            )
        }
    }
}
