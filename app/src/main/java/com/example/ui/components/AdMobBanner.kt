package com.example.ui.components

import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.SacredWordApplication
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError

@Composable
fun AdMobBanner(
    modifier: Modifier = Modifier,
    adUnitId: String = "ca-app-pub-8571668306319206/3809263972"
) {
    val context = LocalContext.current
    var isAdLoaded by remember { mutableStateOf(false) }
    var adLoadError by remember { mutableStateOf<String?>(null) }

    if (adLoadError != null) {
        // Completely collapse and hide the banner view when there is no fill (Error 3) or other ad load failures
        return
    }
    
    // Check if WebView package is completely installed and functional on this device
    val isWebViewOk = remember(context) {
        try {
            // 1. Direct CookieManager resolution check
            android.webkit.CookieManager.getInstance()
            
            // 2. Direct WebView instantiation check to verify full system-level engine functionality
            val testWebView = android.webkit.WebView(context)
            testWebView.destroy()
            true
        } catch (t: Throwable) {
            android.util.Log.e("AdMobBanner", "WebView check failed, disabling live ads to prevent unrecoverable crash.", t)
            false
        }
    }

    if (LocalInspectionMode.current || !isWebViewOk) {
        // Safe preview state only if WebView is completely missing (prevents crash on headless emulator)
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(56.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "AdMob WebView Engine Offline",
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        }
    } else {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(56.dp)
                .background(MaterialTheme.colorScheme.surface),
            contentAlignment = Alignment.Center
        ) {
            AndroidView(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                factory = { ctx ->
                    try {
                        // Lazily initialize Mobile Ads SDK safely before loading the first ad
                        try {
                            com.google.android.gms.ads.MobileAds.initialize(ctx) {}
                        } catch (ex: Throwable) {
                            ex.printStackTrace()
                        }

                        AdView(ctx).apply {
                            setAdSize(AdSize.BANNER)
                            setAdUnitId(adUnitId)
                            layoutParams = ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                            adListener = object : AdListener() {
                                override fun onAdLoaded() {
                                    super.onAdLoaded()
                                    isAdLoaded = true
                                    adLoadError = null
                                    android.util.Log.d("AdMobBanner", "AdMob Ad loaded successfully!")
                                }

                                override fun onAdFailedToLoad(error: LoadAdError) {
                                    super.onAdFailedToLoad(error)
                                    isAdLoaded = false
                                    adLoadError = "Code ${error.code} (${error.message})"
                                    android.util.Log.w(
                                        "AdMobBanner",
                                        "AdMob failed to load: Message='${error.message}', Code=${error.code}"
                                    )
                                }
                            }
                            loadAd(AdRequest.Builder().build())
                        }
                    } catch (t: Throwable) {
                        t.printStackTrace()
                        android.view.View(ctx)
                    }
                },
                update = { adView ->
                    // AndroidView handles internal updates automatically
                }
            )

            // Show real-time status overlay when the live network ad hasn't displayed successfully yet
            if (!isAdLoaded) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surface),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    ) {
                        Text(
                            text = if (adLoadError != null) "Google AdMob Response: $adLoadError" else "Connecting to real-time Google AdMob...",
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (adLoadError != null) {
                            Text(
                                text = "Real-time AdMob integrated. Checking active ad match inventory...",
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Normal,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}
