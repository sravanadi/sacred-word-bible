package com.example.ui.screens.bookmarks

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.BookmarkEntity
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookmarksScreen(
    viewModel: BookmarksViewModel,
    onNavigateToReader: (translationId: String, bookId: String, chapter: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val bookmarks by viewModel.bookmarks.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Bookmarks",
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            if (bookmarks.isEmpty()) {
                // Empty state
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Bookmark,
                        contentDescription = null,
                        tint = GoldPrimary,
                        modifier = Modifier.size(72.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No bookmarks yet",
                        style = MaterialTheme.typography.displayMedium.copy(color = MaterialTheme.colorScheme.onBackground, fontSize = 20.sp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Long-press on any verse in the reader to bookmark your favorite passages.",
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp)
                ) {
                    items(bookmarks, key = { it.id }) { bookmark ->
                        BookmarkItemCard(
                            bookmark = bookmark,
                            onClick = {
                                onNavigateToReader(
                                    bookmark.translationId,
                                    bookmark.bookId,
                                    bookmark.chapterNumber
                                )
                            },
                            onDelete = { viewModel.removeBookmark(bookmark.verseId) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BookmarkItemCard(
    bookmark: BookmarkEntity,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, GoldPrimary.copy(alpha = 0.15f), RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .testTag("bookmark_item_${bookmark.verseId}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(14.dp)
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
                        text = "${bookmark.bookName} ${bookmark.chapterNumber}:${bookmark.bookmarkVerseNumber()}",
                        style = MaterialTheme.typography.displayMedium.copy(
                            color = GoldPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    
                    // Simple Translation Tag / Badge
                    Surface(
                        color = MaterialTheme.colorScheme.background,
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.padding(horizontal = 4.dp)
                    ) {
                        Text(
                            text = bookmark.translationId.uppercase(),
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = bookmark.verseText,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = 15.sp,
                        lineHeight = 22.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Quick delete bookmark button (48dp accessibility touch target)
            IconButton(
                onClick = onDelete,
                modifier = Modifier
                    .testTag("delete_bookmark_${bookmark.verseId}")
                    .size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    tint = HighlightRose,
                    contentDescription = "Remove Bookmark"
                )
            }
        }
    }
}

// Extension to retrieve verse number safely from bookmark reference format
fun BookmarkEntity.bookmarkVerseNumber(): Int {
    return this.verseNumber
}
