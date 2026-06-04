package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.GoldPrimary

import com.example.R

@Composable
fun OpeningSplashScreen(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .systemBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        // Ambient backdrop radial gold glow
        Box(
            modifier = Modifier
                .size(320.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            GoldPrimary.copy(alpha = 0.12f),
                            GoldPrimary.copy(alpha = 0.03f),
                            Color.Transparent
                        )
                    )
                )
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 40.dp)
        ) {
            // Animated Bible Lottie Container
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .clip(RoundedCornerShape(32.dp))
                    .background(MaterialTheme.colorScheme.surface) // Deep premium dark gray card background
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                DotLottieAnimation(
                    source = DotLottieSource.RawRes(R.raw.splash_animation),
                    autoplay = true,
                    loop = true,
                    speed = 2.2f,
                    useFrameInterpolation = false,
                    playMode = Mode.Forward,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(36.dp))

            // Brand App Name
            Text(
                text = "Sacred Word",
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.headlineLarge,
                textAlign = TextAlign.Center,
                letterSpacing = 1.5.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Scripture Subheading or Loading Tip
            Text(
                text = "Thy word is a lamp unto my feet",
                color = GoldPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                letterSpacing = 0.5.sp,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Spacer(modifier = Modifier.height(60.dp))
        }
    }
}
