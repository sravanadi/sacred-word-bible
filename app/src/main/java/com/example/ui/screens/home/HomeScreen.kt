package com.example.ui.screens.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.VerseEntity
import com.example.ui.screens.reader.HighlightCircle
import com.example.ui.theme.*

// Gorgeous golden glow effect to match the web design's box-shadow: 0 0 20px rgba(201, 168, 76, 0.15)
fun Modifier.verseGlow(
    color: Color = Color(0xFFC9A84C),
    alpha: Float = 0.15f,
    borderRadius: Dp = 32.dp,
    glowRadius: Dp = 20.dp
) = this.shadow(
    elevation = glowRadius,
    shape = RoundedCornerShape(borderRadius),
    clip = false,
    ambientColor = color.copy(alpha = alpha),
    spotColor = color.copy(alpha = alpha)
)

fun formatTime(timestamp: Long): String {
    val sdf = java.text.SimpleDateFormat("MMM dd, yyyy  h:mm a", java.util.Locale.getDefault())
    return sdf.format(java.util.Date(timestamp))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToReader: (translationId: String, bookId: String, chapter: Int) -> Unit,
    onNavigateToLibrary: () -> Unit,
    onNavigateToBooks: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val notes by viewModel.allNotes.collectAsState()
    val prayers by viewModel.allPrayers.collectAsState()

    var showAddNoteDialog by remember { mutableStateOf(false) }
    var noteTitle by remember { mutableStateOf("") }
    var noteContent by remember { mutableStateOf("") }

    var showAddPrayerDialog by remember { mutableStateOf(false) }
    var prayerTitle by remember { mutableStateOf("") }
    var prayerContent by remember { mutableStateOf("") }

    var selectedNoteForViewing by remember { mutableStateOf<com.example.data.local.entity.NoteEntity?>(null) }

    // --- DIALOGS ---
    if (showAddNoteDialog) {
        AlertDialog(
            onDismissRequest = { 
                showAddNoteDialog = false
                noteTitle = ""
                noteContent = ""
            },
            title = {
                Text(
                    text = "Reflections & Notes",
                    style = MaterialTheme.typography.titleLarge.copy(color = GoldPrimary, fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = noteTitle,
                        onValueChange = { noteTitle = it },
                        label = { Text("Title") },
                        modifier = Modifier.fillMaxWidth().testTag("note_title_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GoldPrimary,
                            focusedLabelColor = GoldPrimary
                        ),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = noteContent,
                        onValueChange = { noteContent = it },
                        label = { Text("Reflections / Notes text") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .testTag("note_content_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GoldPrimary,
                            focusedLabelColor = GoldPrimary
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (noteTitle.isNotBlank() || noteContent.isNotBlank()) {
                            viewModel.addNote(
                                title = noteTitle.ifBlank { "Journal Entry" },
                                content = noteContent
                            )
                        }
                        showAddNoteDialog = false
                        noteTitle = ""
                        noteContent = ""
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary)
                ) {
                    Text("Save Note", color = MaterialTheme.colorScheme.background)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { 
                        showAddNoteDialog = false 
                        noteTitle = ""
                        noteContent = ""
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onSurface)
                ) {
                    Text("Cancel")
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(24.dp)
        )
    }

    if (selectedNoteForViewing != null) {
        val note = selectedNoteForViewing!!
        AlertDialog(
            onDismissRequest = { selectedNoteForViewing = null },
            title = {
                Text(
                    text = note.title,
                    style = MaterialTheme.typography.titleLarge.copy(color = GoldPrimary, fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = formatTime(note.createdAt),
                        style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = note.content,
                        style = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface, lineHeight = 24.sp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { selectedNoteForViewing = null },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary)
                ) {
                    Text("Close", color = MaterialTheme.colorScheme.background)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(24.dp)
        )
    }

    if (showAddPrayerDialog) {
        AlertDialog(
            onDismissRequest = { 
                showAddPrayerDialog = false
                prayerTitle = ""
                prayerContent = ""
            },
            title = {
                Text(
                    text = "New Prayer Request",
                    style = MaterialTheme.typography.titleLarge.copy(color = GoldPrimary, fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = prayerTitle,
                        onValueChange = { prayerTitle = it },
                        label = { Text("Prayer Topic") },
                        modifier = Modifier.fillMaxWidth().testTag("prayer_title_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GoldPrimary,
                            focusedLabelColor = GoldPrimary
                        ),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = prayerContent,
                        onValueChange = { prayerContent = it },
                        label = { Text("Details / Scripture focus") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .testTag("prayer_content_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GoldPrimary,
                            focusedLabelColor = GoldPrimary
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (prayerTitle.isNotBlank() || prayerContent.isNotBlank()) {
                            viewModel.addPrayer(
                                title = prayerTitle.ifBlank { "Daily Prayer" },
                                content = prayerContent
                            )
                        }
                        showAddPrayerDialog = false
                        prayerTitle = ""
                        prayerContent = ""
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary)
                ) {
                    Text("Add Prayer", color = MaterialTheme.colorScheme.background)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { 
                        showAddPrayerDialog = false 
                        prayerTitle = ""
                        prayerContent = ""
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onSurface)
                ) {
                    Text("Cancel")
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(24.dp)
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Sacred Word",
                        style = MaterialTheme.typography.displayLarge.copy(color = GoldPrimary, fontSize = 28.sp)
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
        ) {
            when (val state = uiState) {
                is HomeUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = GoldPrimary)
                    }
                }
                is HomeUiState.Empty -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Book,
                            contentDescription = null,
                            tint = GoldPrimary,
                            modifier = Modifier.size(80.dp)
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Text(
                            text = "Welcome to Sacred Word",
                            style = MaterialTheme.typography.displayMedium.copy(color = MaterialTheme.colorScheme.onBackground)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "To get started, please install a Bible translation from our collection.",
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                            textAlign = TextAlign.Center,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        Spacer(modifier = Modifier.height(32.dp))
                        Button(
                            onClick = onNavigateToLibrary,
                            colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("home_empty_library_nav")
                        ) {
                            Text(
                                "Download Bible",
                                color = MaterialTheme.colorScheme.background,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
                is HomeUiState.Success -> {
                    val verse = state.verse
                    val bookName = state.bookName
                    val isBookmarked = state.isBookmarked
                    val activeHighlight = state.activeHighlightColorHex

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        contentPadding = PaddingValues(top = 12.dp, bottom = 40.dp)
                    ) {
                        item {
                            // Custom Display Card with Elegant Gold border, 32dp rounded corners, and glowing ambient shadow
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .verseGlow(borderRadius = 32.dp)
                                    .clickable {
                                        onNavigateToReader(state.activeTranslation.id, verse.bookId, verse.chapterNumber)
                                    },
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                shape = RoundedCornerShape(32.dp),
                                border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.3f))
                            ) {
                                Box(modifier = Modifier.fillMaxWidth()) {
                                    // Top-end action buttons (Share and Bookmark)
                                    Row(
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(top = 12.dp, end = 12.dp),
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        val context = androidx.compose.ui.platform.LocalContext.current
                                        IconButton(
                                            onClick = {
                                                try {
                                                    val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                                        type = "text/plain"
                                                        putExtra(android.content.Intent.EXTRA_SUBJECT, "Verse of the Day")
                                                        putExtra(android.content.Intent.EXTRA_TEXT, "“${verse.text}”\n\n— $bookName ${verse.chapterNumber}:${verse.verseNumber} (${state.activeTranslation.shortName})\nShared via Sacred Word Bible App")
                                                    }
                                                    val chooser = android.content.Intent.createChooser(shareIntent, "Share Verse").apply {
                                                        addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                                                    }
                                                    context.startActivity(chooser)
                                                } catch (e: Exception) {
                                                    e.printStackTrace()
                                                    android.widget.Toast.makeText(
                                                        context,
                                                        "Sharing is not available on this device",
                                                        android.widget.Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                            },
                                            modifier = Modifier
                                                .testTag("home_verse_share")
                                                .size(48.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Share,
                                                contentDescription = "Share",
                                                tint = GoldPrimary,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }

                                        IconButton(
                                            onClick = { viewModel.toggleBookmark(verse, bookName) },
                                            modifier = Modifier
                                                .testTag("home_bookmark_toggle")
                                                .size(48.dp)
                                        ) {
                                            Icon(
                                                imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                                contentDescription = "Bookmark",
                                                tint = GoldPrimary,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                    }

                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 24.dp, vertical = 24.dp),
                                        horizontalAlignment = Alignment.Start
                                    ) {
                                        // Section Label
                                        Text(
                                            text = "VERSE OF THE DAY",
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                                fontWeight = FontWeight.Bold,
                                                letterSpacing = 1.6.sp,
                                                fontSize = 13.sp
                                            )
                                        )

                                        Spacer(modifier = Modifier.height(16.dp))

                                        // Scripture Passage
                                        Text(
                                            text = "“${verse.text}”",
                                            style = MaterialTheme.typography.bodyLarge.copy(
                                                color = MaterialTheme.colorScheme.onSurface,
                                                fontSize = 19.sp,
                                                fontWeight = FontWeight.Normal,
                                                lineHeight = 32.sp,
                                                textAlign = TextAlign.Start
                                            )
                                        )
                                        
                                        Spacer(modifier = Modifier.height(20.dp))
                                        
                                        // Citation Block
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "$bookName ${verse.chapterNumber}:${verse.verseNumber}",
                                                style = MaterialTheme.typography.displayMedium.copy(
                                                    color = GoldPrimary,
                                                    fontSize = 18.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = state.activeTranslation.shortName,
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                                fontWeight = FontWeight.Normal,
                                                fontSize = 14.sp
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(24.dp))

                                        // Bottom Quick Highlight Section
                                        Text(
                                            text = "QUICK HIGHLIGHT",
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 1.2.sp
                                        )

                                        Spacer(modifier = Modifier.height(8.dp))

                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            HighlightCircle(color = HighlightPurple, isSelected = activeHighlight == HEX_HighlightPurple) {
                                                viewModel.highlightVerse(verse.id, HEX_HighlightPurple)
                                            }
                                            HighlightCircle(color = HighlightAmber, isSelected = activeHighlight == HEX_HighlightAmber) {
                                                viewModel.highlightVerse(verse.id, HEX_HighlightAmber)
                                            }
                                            HighlightCircle(color = HighlightMint, isSelected = activeHighlight == HEX_HighlightMint) {
                                                viewModel.highlightVerse(verse.id, HEX_HighlightMint)
                                            }
                                            HighlightCircle(color = HighlightRose, isSelected = activeHighlight == HEX_HighlightRose) {
                                                viewModel.highlightVerse(verse.id, HEX_HighlightRose)
                                            }
                                            HighlightCircle(color = HighlightSky, isSelected = activeHighlight == HEX_HighlightSky) {
                                                viewModel.highlightVerse(verse.id, HEX_HighlightSky)
                                            }

                                            if (activeHighlight != null) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(36.dp)
                                                        .clip(CircleShape)
                                                        .border(1.dp, HighlightRose.copy(alpha = 0.5f), CircleShape)
                                                        .clickable { viewModel.removeHighlight(verse.id) },
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.HighlightOff,
                                                        contentDescription = "Clear Highlight",
                                                        tint = HighlightRose,
                                                        modifier = Modifier.size(22.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(16.dp))

                            // Bottom CTA "Read Bible" Button (styled with rounded-2xl & custom primary container colors)
                            Button(
                                onClick = { 
                                    onNavigateToReader(state.activeTranslation.id, verse.bookId, verse.chapterNumber) 
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp)
                                    .testTag("home_read_bible_button")
                            ) {
                                Text(
                                    text = "Read Bible",
                                    color = MaterialTheme.colorScheme.background,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            }
                        }

                        // Affiliate Promotional Banner/Button for Recommended Books
                        if (onNavigateToBooks != null) {
                            item {
                                Spacer(modifier = Modifier.height(14.dp))
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onNavigateToBooks() }
                                        .testTag("home_books_promo_card"),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surface
                                    ),
                                    shape = RoundedCornerShape(20.dp),
                                    border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.25f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(40.dp)
                                                    .clip(CircleShape)
                                                    .background(GoldPrimary.copy(alpha = 0.12f)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Book,
                                                    contentDescription = "Study Books",
                                                    tint = GoldPrimary,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Column {
                                                Text(
                                                    text = "Scripture Study Resources",
                                                    style = MaterialTheme.typography.bodyLarge.copy(
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 15.sp
                                                    )
                                                )
                                                Text(
                                                    text = "Highly-rated study Bibles, commentaries & guides",
                                                    style = MaterialTheme.typography.bodySmall.copy(
                                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                                        fontSize = 12.sp
                                                    )
                                                )
                                            }
                                        }
                                        
                                        Icon(
                                            imageVector = Icons.Default.ChevronRight,
                                            contentDescription = "Navigate to Study Resources",
                                            tint = GoldPrimary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // NOTE SECTION HEADER
                        item {
                            Spacer(modifier = Modifier.height(28.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "JOURNAL & REFLECTIONS",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            color = GoldPrimary,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 1.8.sp
                                        )
                                    )
                                    Text(
                                        text = "Capture your study revelations",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                        )
                                    )
                                }
                                IconButton(
                                    onClick = { showAddNoteDialog = true },
                                    modifier = Modifier
                                        .testTag("add_note_button")
                                        .size(44.dp)
                                        .background(GoldPrimary.copy(alpha = 0.12f), CircleShape)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Add Journal Note",
                                        tint = GoldPrimary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                        }

                        if (notes.isEmpty()) {
                            item {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 12.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.3f)),
                                    shape = RoundedCornerShape(16.dp),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(20.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.NoteAlt,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                                            modifier = Modifier.size(36.dp)
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "No journal reflections yet.",
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                                textAlign = TextAlign.Center
                                            )
                                        )
                                        Text(
                                            text = "Tap the + button to write notes about your scripture study.",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                                                textAlign = TextAlign.Center
                                            ),
                                            modifier = Modifier.padding(horizontal = 12.dp)
                                        )
                                    }
                                }
                            }
                        } else {
                            items(notes, key = { "note_${it.id}" }) { note ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 12.dp)
                                        .clickable { selectedNoteForViewing = note },
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    shape = RoundedCornerShape(20.dp),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = note.title,
                                                style = MaterialTheme.typography.titleMedium.copy(
                                                    color = GoldPrimary,
                                                    fontWeight = FontWeight.Bold
                                                ),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = note.content,
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                                                ),
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(
                                                text = formatTime(note.createdAt),
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                                )
                                            )
                                        }
                                        IconButton(
                                            onClick = { viewModel.deleteNote(note.id) },
                                            modifier = Modifier
                                                .testTag("delete_note_${note.id}")
                                                .size(40.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Delete Note",
                                                tint = HighlightRose.copy(alpha = 0.8f),
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // PRAYER SECTION HEADER
                        item {
                            Spacer(modifier = Modifier.height(28.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "PRAYER WALKS",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            color = GoldPrimary,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 1.8.sp
                                        )
                                    )
                                    Text(
                                        text = "Bring your prayers to communion",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                        )
                                    )
                                }
                                IconButton(
                                    onClick = { showAddPrayerDialog = true },
                                    modifier = Modifier
                                        .testTag("add_prayer_button")
                                        .size(44.dp)
                                        .background(GoldPrimary.copy(alpha = 0.12f), CircleShape)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Add Prayer Request",
                                        tint = GoldPrimary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                        }

                        if (prayers.isEmpty()) {
                            item {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 12.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.3f)),
                                    shape = RoundedCornerShape(16.dp),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(20.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Hearing,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                                            modifier = Modifier.size(36.dp)
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "No prayer requests added yet.",
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                                textAlign = TextAlign.Center
                                            )
                                        )
                                        Text(
                                            text = "Tap + to add a request and track God's answers.",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                                                textAlign = TextAlign.Center
                                            ),
                                            modifier = Modifier.padding(horizontal = 12.dp)
                                        )
                                    }
                                }
                            }
                        } else {
                            items(prayers, key = { "prayer_${it.id}" }) { prayer ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 12.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (prayer.isAnswered) {
                                            MaterialTheme.colorScheme.surface.copy(alpha = 0.4f)
                                        } else {
                                            MaterialTheme.colorScheme.surface
                                        }
                                    ),
                                    shape = RoundedCornerShape(20.dp),
                                    border = BorderStroke(
                                        1.dp,
                                        if (prayer.isAnswered) GoldPrimary.copy(alpha = 0.4f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                                    )
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                if (prayer.isAnswered) {
                                                    Box(
                                                        modifier = Modifier
                                                            .background(GoldPrimary.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                                    ) {
                                                        Text(
                                                            text = "HEARD & ANSWERED",
                                                            style = MaterialTheme.typography.labelSmall.copy(
                                                                color = GoldPrimary,
                                                                fontWeight = FontWeight.Bold,
                                                                fontSize = 10.sp
                                                            )
                                                        )
                                                    }
                                                } else {
                                                    Box(
                                                        modifier = Modifier
                                                            .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                                    ) {
                                                        Text(
                                                            text = "SEEKING",
                                                            style = MaterialTheme.typography.labelSmall.copy(
                                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                                                fontSize = 10.sp
                                                            )
                                                        )
                                                    }
                                                }
                                            }

                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Checkbox(
                                                    checked = prayer.isAnswered,
                                                    onCheckedChange = { isChecked ->
                                                        viewModel.togglePrayerAnswered(prayer.id, isChecked)
                                                    },
                                                    colors = CheckboxDefaults.colors(
                                                        checkedColor = GoldPrimary,
                                                        uncheckedColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                                    ),
                                                    modifier = Modifier.size(36.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                IconButton(
                                                    onClick = { viewModel.deletePrayer(prayer.id) },
                                                    modifier = Modifier
                                                        .testTag("delete_prayer_${prayer.id}")
                                                        .size(36.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Delete,
                                                        contentDescription = "Delete Prayer request",
                                                        tint = HighlightRose.copy(alpha = 0.8f),
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(10.dp))

                                        Text(
                                            text = prayer.title,
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                color = if (prayer.isAnswered) {
                                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                                } else {
                                                    MaterialTheme.colorScheme.onSurface
                                                },
                                                fontWeight = FontWeight.Bold,
                                                textDecoration = if (prayer.isAnswered) androidx.compose.ui.text.style.TextDecoration.LineThrough else null
                                            )
                                        )

                                        Spacer(modifier = Modifier.height(4.dp))

                                        Text(
                                            text = prayer.content,
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                color = if (prayer.isAnswered) {
                                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                                } else {
                                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                                                },
                                                textDecoration = if (prayer.isAnswered) androidx.compose.ui.text.style.TextDecoration.LineThrough else null
                                            )
                                        )

                                        Spacer(modifier = Modifier.height(8.dp))

                                        Text(
                                            text = "Created: " + formatTime(prayer.createdAt),
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                                                fontSize = 11.sp
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
