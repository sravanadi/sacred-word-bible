package com.example.ui.screens.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.DotLottieAnimation
import com.example.ui.components.DotLottieSource
import com.example.R
import kotlinx.coroutines.launch

data class OnboardingPageData(
    val title: String,
    val description: String
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    onOnboardingComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val pages = remember {
        listOf(
            OnboardingPageData(
                title = "Welcome to Sacred Word",
                description = "Discover and cherish ancient biblical wisdom in one elegant place. Study, meditate, and grow daily in faith."
            ),
            OnboardingPageData(
                title = "Immersive Bible Reader",
                description = "Enjoy a pure black, distraction-free reader designed for eye comfort, with custom typography and elegant line spacing."
            ),
            OnboardingPageData(
                title = "Verses & Sharing",
                description = "Save your favorite verses, study offline, and share beautifully designed layouts of the Verse of the Day with family and friends."
            )
        )
    }

    val pagerState = rememberPagerState(pageCount = { pages.size })
    val coroutineScope = rememberCoroutineScope()

    // Pure black back-drop like apple.com color palette
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .systemBarsPadding()
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top Bar with Skip Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                AnimatedVisibility(
                    visible = pagerState.currentPage < pages.lastIndex,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    TextButton(
                        onClick = { onOnboardingComplete() },
                        modifier = Modifier.testTag("onboarding_skip_button"),
                        colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFF8E8E93))
                    ) {
                        Text(
                            text = "Skip",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Animation & Sliders (Horizontal Pager)
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) { pageIndex ->
                val page = pages[pageIndex]
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Custom Lottie Animation Container styled correctly 
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(24.dp))
                            .background(MaterialTheme.colorScheme.surface) // Lighter Ash Gray background for the animation card
                            .padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // Using provided dotlottie code specifications
                        DotLottieAnimation(
                            source = DotLottieSource.RawRes(R.raw.splash_animation),
                            autoplay = true,
                            loop = true,
                            speed = 3f,
                            useFrameInterpolation = false,
                            playMode = com.example.ui.components.Mode.Forward,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Spacer(modifier = Modifier.height(48.dp))

                    // Title
                    Text(
                        text = page.title,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        lineHeight = 36.sp,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Description
                    Text(
                        text = page.description,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f), // Soft gray
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Normal,
                        textAlign = TextAlign.Center,
                        lineHeight = 24.sp,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Bottom Navigation & Actions Row
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp, vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Page Indicator Bubbles
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 32.dp)
                ) {
                    repeat(pages.size) { index ->
                        val isSelected = pagerState.currentPage == index
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 5.dp)
                                .size(width = if (isSelected) 24.dp else 8.dp, height = 8.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f)) // Gold accent / soft ash grey
                        )
                    }
                }

                // CTA Button
                Button(
                    onClick = {
                        if (pagerState.currentPage == pages.lastIndex) {
                            onOnboardingComplete()
                        } else {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("onboarding_cta_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary, // Dynamic Gold Primary
                        contentColor = MaterialTheme.colorScheme.onSecondary
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = if (pagerState.currentPage == pages.lastIndex) "Get Started" else "Continue",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondary,
                        fontSize = 17.sp
                    )
                }
            }
        }
    }
}
