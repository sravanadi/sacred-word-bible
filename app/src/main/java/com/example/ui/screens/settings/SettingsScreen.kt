package com.example.ui.screens.settings

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.AmaranteFont
import com.example.ui.theme.SourGummyFont

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onBack: (() -> Unit)? = null,
    onNavigateToBooks: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val isDark by viewModel.isDarkTheme.collectAsState()
    val fontFamilyName by viewModel.fontFamilyName.collectAsState()
    
    val uriHandler = LocalUriHandler.current
    val context = LocalContext.current
    
    var showPrivacyDialog by remember { mutableStateOf(false) }
    var showContactDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Settings",
                        style = MaterialTheme.typography.displayMedium.copy(color = GoldPrimary, fontSize = 22.sp),
                        modifier = Modifier.testTag("settings_title")
                    )
                },
                navigationIcon = {
                    if (onBack != null) {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                                contentDescription = "Back",
                                tint = GoldPrimary
                            )
                        }
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            
            // SECTION 1: VISUAL THEMING
            item {
                Text(
                    text = "Appearance",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = GoldPrimary,
                        letterSpacing = 1.sp
                    ),
                    modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isDark) Icons.Outlined.DarkMode else Icons.Outlined.LightMode,
                                contentDescription = "Theme Icon",
                                tint = GoldPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = "Theme Mode",
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 16.sp
                                    )
                                )
                                Text(
                                    text = if (isDark) "Dark mode active" else "Light mode active",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                )
                            }
                        }
                        Switch(
                            checked = isDark,
                            onCheckedChange = { viewModel.setDarkTheme(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = GoldPrimary,
                                checkedTrackColor = GoldPrimary.copy(alpha = 0.4f),
                                uncheckedThumbColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                uncheckedTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                            ),
                            modifier = Modifier.testTag("theme_toggle_switch")
                        )
                    }
                }
            }


            // SECTION 2: TYPOGRAPHY
            item {
                Text(
                    text = "Typography",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = GoldPrimary,
                        letterSpacing = 1.sp
                    ),
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.TextFields,
                                contentDescription = "Font Icon",
                                tint = GoldPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = "Choose Font Style",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 16.sp
                                )
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))

                        // Font Selector Radio-like list
                        val fontOptions = listOf(
                            "sans" to "Modern Sans-Serif",
                            "amarante" to "Amarante (Serif)",
                            "sourgummy" to "Sour Gummy"
                        )

                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            fontOptions.forEach { (key, label) ->
                                val isSelected = fontFamilyName == key
                                val optionFont = when (key) {
                                    "amarante" -> AmaranteFont
                                    "sourgummy" -> SourGummyFont
                                    else -> FontFamily.SansSerif
                                }
                                
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .border(
                                            width = 1.dp,
                                            color = if (isSelected) GoldPrimary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .background(
                                            if (isSelected) GoldPrimary.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface
                                        )
                                        .clickable { viewModel.setFontFamilyName(key) }
                                        .padding(horizontal = 16.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                    
                                ) {
                                    Text(
                                        text = label,
                                        style = TextStyle(
                                            fontFamily = optionFont,
                                            fontSize = 15.sp,
                                            color = if (isSelected) GoldPrimary else MaterialTheme.colorScheme.onSurface
                                        )
                                    )
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Selected",
                                            tint = GoldPrimary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // SECTION 2.5: AFFILIATE STUDY RESOURCES
            if (onNavigateToBooks != null) {
                item {
                    Text(
                        text = "Scripture Study Resources",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = GoldPrimary,
                            letterSpacing = 1.sp
                        ),
                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                    )
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth().testTag("settings_books_nav_card"),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onNavigateToBooks() }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Outlined.AutoStories,
                                    contentDescription = "Books Icon",
                                    tint = GoldPrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text(
                                        text = "Recommended Study Books",
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 16.sp
                                        )
                                    )
                                    Text(
                                        text = "Promoted and affiliate bible resources",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                        )
                                    )
                                }
                            }
                            Icon(
                                imageVector = Icons.Outlined.ChevronRight,
                                contentDescription = ">",
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                            )
                        }
                    }
                }
            }

            // SECTION 3: LEGAL & CONTACT
            item {
                Text(
                    text = "Information & Support",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = GoldPrimary,
                        letterSpacing = 1.sp
                    ),
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column {
                        // Row 1: Privacy Policies
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showPrivacyDialog = true }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Outlined.Lock,
                                    contentDescription = "Privacy Icon",
                                    tint = GoldPrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Text(
                                    text = "Privacy Policy",
                                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp)
                                )
                            }
                            Icon(
                                imageVector = Icons.Outlined.ChevronRight,
                                contentDescription = ">",
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                            )
                        }

                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                            thickness = 1.dp,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )

                        // Row 2: Contact Page
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showContactDialog = true }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Outlined.Email,
                                    contentDescription = "Contact Icon",
                                    tint = GoldPrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Text(
                                    text = "Contact Support",
                                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp)
                                )
                            }
                            Icon(
                                imageVector = Icons.Outlined.ChevronRight,
                                contentDescription = ">",
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                            )
                        }
                    }
                }
            }

            // SECTION 4: DONATIONS ("BUY ME COFFEE" CARD)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = GoldPrimary.copy(alpha = 0.12f)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.5.dp, GoldPrimary.copy(alpha = 0.4f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        var copied by remember { mutableStateOf(false) }
                        val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
                        
                        LaunchedEffect(copied) {
                            if (copied) {
                                kotlinx.coroutines.delay(2000)
                                copied = false
                            }
                        }

                        Icon(
                            imageVector = Icons.Outlined.FavoriteBorder,
                            contentDescription = "Heart Icon",
                            tint = GoldPrimary,
                            modifier = Modifier.size(44.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Buy Me a Coffee",
                            style = MaterialTheme.typography.displayMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = GoldPrimary
                            )
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "If this Offline Bible app has blessed you, consider supporting further development with a coffee donation!",
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontSize = 14.sp,
                                lineHeight = 20.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                            )
                        )
                        
                        Spacer(modifier = Modifier.height(10.dp))
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
                                .clickable {
                                    clipboardManager.setText(androidx.compose.ui.text.buildAnnotatedString { append("sravanabishek9000@gmail.com") })
                                    copied = true
                                }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.ContentCopy,
                                contentDescription = "Copy Email",
                                tint = GoldPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "sravanabishek9000@gmail.com",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )
                            if (copied) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Copied!",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = GoldPrimary
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                try {
                                    uriHandler.openUri("https://www.paypal.com/cgi-bin/webscr?cmd=_donations&business=sravanabishek9000@gmail.com&item_name=Sacred%20Word%20Bible%20App%20Donation&currency_code=USD")
                                } catch (e: Exception) {
                                    // standard fallback
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = GoldPrimary
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Favorite,
                                contentDescription = "Heart",
                                tint = MaterialTheme.colorScheme.background,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Donate via PayPal",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.background
                                )
                            )
                        }
                    }
                }
            }
        }
    }

    // PRIVACY POLICY DIALOG
    if (showPrivacyDialog) {
        Dialog(
            onDismissRequest = { showPrivacyDialog = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.85f)
                    .padding(24.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp)
                ) {
                    Text(
                        text = "Privacy Policy",
                        style = MaterialTheme.typography.displayMedium.copy(
                            color = GoldPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .background(
                                MaterialTheme.colorScheme.background,
                                RoundedCornerShape(12.dp)
                            )
                            .padding(14.dp)
                    ) {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            item {
                                Text(
                                    text = "Last Updated: May 2026\n\n" +
                                            "Welcome to Sacred Word. Your privacy is critically important to us.\n\n" +
                                            "1. Offline Data Storage:\n" +
                                            "Sacred Word is designed to function completely offline. Your personal study preferences, bookmark highlights, saved notes, or translation states are kept strictly on your local device. We do not transmit this data to any remote cloud databases.\n\n" +
                                            "2. Third-Party Web APIs:\n" +
                                            "If you choose to download additional bible translations, standard network HTTP inquiries are sent to the verified Public Bible API servers (helloao.org). These queries do not contain any personally identifying tags.\n\n" +
                                            "3. Optional Ad Services:\n" +
                                            "Sacred Word contains optional integration with Google AdMob to help sponsor further developer study tools. By default, live network ads are disabled to provide a seamless, purely offline and privacy-respecting environment. If you wish to support us, you can toggle live ads on inside the Settings menu.\n\n" +
                                            "4. Contact Us:\n" +
                                            "For questions about our operations, feel free to reach out directly to sravanabishek9000@gmail.com.",
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontSize = 14.sp,
                                        lineHeight = 22.sp
                                    )
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { showPrivacyDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                        modifier = Modifier.align(Alignment.End),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(text = "Close", color = MaterialTheme.colorScheme.background)
                    }
                }
            }
        }
    }

    // CONTACT DIALOG
    if (showContactDialog) {
        Dialog(
            onDismissRequest = { showContactDialog = false }
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Contact Support",
                        style = MaterialTheme.typography.displayMedium.copy(
                            color = GoldPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Start
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text(
                        text = "If you have any feedback, bug reports, translation request or question about Sacred Word, we would love to hear from you!",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontSize = 14.sp,
                            lineHeight = 20.sp
                        )
                    )
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.background)
                            .clickable {
                                try {
                                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                                        data = Uri.parse("mailto:")
                                        putExtra(Intent.EXTRA_EMAIL, arrayOf("sravanabishek9000@gmail.com"))
                                        putExtra(Intent.EXTRA_SUBJECT, "Sacred Word Offline Feedback")
                                    }
                                    val chooser = Intent.createChooser(intent, "Send Email").apply {
                                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    }
                                    context.startActivity(chooser)
                                } catch (e: Exception) {
                                    // Fallback to PayPal or similar handler if system doesn't support
                                }
                            }
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Email,
                            contentDescription = "Email Support",
                            tint = GoldPrimary
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = "Email Support",
                                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "sravanabishek9000@gmail.com",
                                style = MaterialTheme.typography.labelMedium.copy(color = GoldPrimary)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.background)
                            .clickable {
                                try {
                                    uriHandler.openUri("https://github.com")
                                } catch (e: Exception) {}
                            }
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Share,
                            contentDescription = "Website Support",
                            tint = GoldPrimary
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = "Official Repository",
                                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "github.com/sacredword",
                                style = MaterialTheme.typography.labelMedium.copy(color = GoldPrimary)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                    
                    Button(
                        onClick = { showContactDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                        modifier = Modifier.align(Alignment.End),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(text = "Dismiss", color = MaterialTheme.colorScheme.background)
                    }
                }
            }
        }
    }
}
