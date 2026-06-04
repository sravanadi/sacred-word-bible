package com.example.ui.components

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.airbnb.lottie.compose.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.BufferedInputStream
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.util.zip.ZipInputStream

sealed class DotLottieSource {
    data class Url(val url: String) : DotLottieSource()
    data class RawRes(@androidx.annotation.RawRes val resId: Int) : DotLottieSource()
}

enum class Mode {
    Forward
}

private val downloadMutexMap = java.util.concurrent.ConcurrentHashMap<String, Mutex>()

@Composable
fun DotLottieAnimation(
    source: DotLottieSource,
    autoplay: Boolean = true,
    loop: Boolean = true,
    speed: Float = 1f,
    useFrameInterpolation: Boolean = false,
    playMode: Mode = Mode.Forward,
    modifier: Modifier = Modifier
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var jsonString by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var isError by remember { mutableStateOf(false) }

    LaunchedEffect(source) {
        if (source is DotLottieSource.Url) {
            isLoading = true
            isError = false
            try {
                val downloadedJson = withContext(Dispatchers.IO) {
                    downloadAndExtractLottieJson(context, source.url)
                }
                if (downloadedJson != null) {
                    jsonString = downloadedJson
                } else {
                    isError = true
                }
            } catch (e: Exception) {
                e.printStackTrace()
                isError = true
            } finally {
                isLoading = false
            }
        } else {
            isLoading = false
            isError = false
        }
    }

    val compositionResult = when (source) {
        is DotLottieSource.Url -> {
            if (jsonString != null) {
                rememberLottieComposition(LottieCompositionSpec.JsonString(jsonString!!))
            } else {
                null
            }
        }
        is DotLottieSource.RawRes -> {
            rememberLottieComposition(LottieCompositionSpec.RawRes(source.resId))
        }
    }

    val composition = compositionResult?.value

    if (composition != null) {
        val progress by animateLottieCompositionAsState(
            composition = composition,
            isPlaying = autoplay,
            iterations = if (loop) LottieConstants.IterateForever else 1,
            speed = speed
        )
        LottieAnimation(
            composition = composition,
            progress = { progress },
            modifier = modifier
        )
    } else if (compositionResult?.isFailure == true || isError) {
        Box(
            modifier = modifier,
            contentAlignment = Alignment.Center
        ) {
            androidx.compose.material3.Text(
                text = "Lottie failed to load",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    } else {
        Box(
            modifier = modifier,
            contentAlignment = Alignment.Center
        ) {
            if (isLoading || compositionResult == null) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

private suspend fun downloadAndExtractLottieJson(context: Context, urlString: String): String? {
    val mutex = downloadMutexMap.computeIfAbsent(urlString) { Mutex() }
    return mutex.withLock {
        val cacheFile = File(context.cacheDir, "lottie_cache_${urlString.hashCode()}.json")
        val tempFile = File(context.cacheDir, "lottie_cache_${urlString.hashCode()}.tmp")

        if (cacheFile.exists()) {
            try {
                val cachedContent = cacheFile.readText()
                val trimmed = cachedContent.trim()
                if (trimmed.startsWith("{") && trimmed.endsWith("}") && trimmed.contains("\"layers\"")) {
                    return@withLock cachedContent
                } else {
                    cacheFile.delete()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        var connection: HttpURLConnection? = null
        try {
            var currentUrl = urlString
            var redirectCount = 0
            val maxRedirects = 5

            while (redirectCount < maxRedirects) {
                val url = URL(currentUrl)
                connection = url.openConnection() as HttpURLConnection
                connection.connectTimeout = 15_000
                connection.readTimeout = 15_000
                connection.instanceFollowRedirects = true
                
                val status = connection.responseCode
                if (status == HttpURLConnection.HTTP_MOVED_TEMP || 
                    status == HttpURLConnection.HTTP_MOVED_PERM || 
                    status == 307 || status == 308) {
                    val newUrl = connection.getHeaderField("Location")
                    if (newUrl != null) {
                        currentUrl = newUrl
                        redirectCount++
                        connection.disconnect()
                        continue
                    }
                }
                break
            }

            if (connection?.responseCode == 200) {
                val bytes = connection.inputStream.use { it.readBytes() }
                if (bytes.size > 4 && 
                    bytes[0] == 0x50.toByte() && 
                    bytes[1] == 0x4B.toByte() && 
                    bytes[2] == 0x03.toByte() && 
                    bytes[3] == 0x04.toByte()) {
                    // ZIP Magic Number PK\u0003\u0004 - Extract .json from ZIP
                    ZipInputStream(java.io.ByteArrayInputStream(bytes)).use { zipInputStream ->
                        var entry = zipInputStream.nextEntry
                        while (entry != null) {
                            if (entry.name.endsWith(".json") && 
                                !entry.name.contains("__MACOSX") && 
                                !entry.name.endsWith("manifest.json")) {
                                val jsonContent = zipInputStream.reader(Charsets.UTF_8).readText()
                                val trimmed = jsonContent.trim()
                                if (trimmed.startsWith("{") && trimmed.endsWith("}") && trimmed.contains("\"layers\"")) {
                                    try {
                                        tempFile.writeText(jsonContent)
                                        tempFile.renameTo(cacheFile)
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                    return@withLock jsonContent
                                }
                            }
                            entry = zipInputStream.nextEntry
                        }
                    }
                } else {
                    // Plain JSON string format
                    val jsonContent = String(bytes, Charsets.UTF_8)
                    val trimmed = jsonContent.trim()
                    if (trimmed.startsWith("{") && trimmed.endsWith("}") && trimmed.contains("\"layers\"")) {
                        try {
                            tempFile.writeText(jsonContent)
                            tempFile.renameTo(cacheFile)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                        return@withLock jsonContent
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            connection?.disconnect()
            try {
                if (tempFile.exists()) {
                    tempFile.delete()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        null
    }
}
