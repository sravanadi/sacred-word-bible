package com.example.ui.screens.bible

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChaptersScreen(
    viewModel: ChaptersViewModel,
    onNavigateToReader: (chapter: Int) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val book by viewModel.book.collectAsState()
    val highlightedChapters by viewModel.highlightedChapters.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = book?.name ?: "Chapters",
                        style = MaterialTheme.typography.displayLarge.copy(color = GoldPrimary, fontSize = 24.sp)
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Go Back",
                            tint = GoldPrimary
                        )
                    }
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
            Spacer(modifier = Modifier.height(4.dp))

            val totalChapters = book?.totalChapters ?: 0
            if (totalChapters == 0) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = GoldPrimary)
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(6),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(totalChapters) { index ->
                        val chapterNum = index + 1
                        val isHighlighted = highlightedChapters.contains(chapterNum)
                        
                        ChapterButton(
                            chapterNumber = chapterNum,
                            isHighlighted = isHighlighted,
                            onClick = { onNavigateToReader(chapterNum) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ChapterButton(
    chapterNumber: Int,
    isHighlighted: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderStroke = if (isHighlighted) {
        BorderStroke(1.5.dp, HighlightPurple)
    } else {
        BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.12f))
    }

    Surface(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .testTag("chapter_button_$chapterNumber"),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(12.dp),
        border = borderStroke
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = chapterNumber.toString(),
                fontWeight = FontWeight.Bold,
                color = GoldPrimary,
                fontSize = 16.sp
            )
        }
    }
}
