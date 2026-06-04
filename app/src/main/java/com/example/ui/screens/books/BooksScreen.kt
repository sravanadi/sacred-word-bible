package com.example.ui.screens.books

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
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
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.GoldPrimary

data class AffiliateBook(
    val title: String,
    val author: String,
    val description: String,
    val price: String,
    val oldPrice: String?,
    val rating: Float,
    val affiliateUrl: String,
    val category: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BooksScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uriHandler = LocalUriHandler.current
    var selectedCategory by remember { mutableStateOf("All") }
    
    val categories = listOf("All", "Study Bibles", "Spiritual Wisdom", "Wall Art & Decor")
    
    val books = remember {
        listOf(
            AffiliateBook(
                title = "ESV Study Bible (Deep Brown Cowhide Leather)",
                author = "Crossway Bibles",
                description = "The English Standard Version Study Bible is an incredibly comprehensive, high-quality reference with 20,000+ notes, 200+ detailed charts, and stunning visual layouts for deep scriptural exploration.",
                price = "$79.99",
                oldPrice = "$99.99",
                rating = 4.9f,
                affiliateUrl = "https://amzn.to/436Wzh0",
                category = "Study Bibles",
                icon = Icons.Outlined.Book
            ),
            AffiliateBook(
                title = "KJV Holy Bible (Larger Print, Paperback)",
                author = "Thomas Nelson (Comfort Print)",
                description = "King James Version paperback Bible with beautiful comfort typography, featuring clear larger print and standard study features designed for daily devotion and easy reading.",
                price = "$7.99",
                oldPrice = "$9.99",
                rating = 4.8f,
                affiliateUrl = "https://amzn.to/3PP1z6M",
                category = "Study Bibles",
                icon = Icons.Outlined.Book
            ),
            AffiliateBook(
                title = "Life Application Study Bible - NIV (Personal Size)",
                author = "Tyndale House Publishers",
                description = "A best-selling New International Version personal study study companion designed to help you discover how to apply the truth of God's Word to everyday life situations.",
                price = "$34.95",
                oldPrice = "$44.99",
                rating = 4.9f,
                affiliateUrl = "https://amzn.to/3RrXFS5",
                category = "Study Bibles",
                icon = Icons.Outlined.Book
            ),
            AffiliateBook(
                title = "Chronological Study Bible - NKJV",
                author = "Thomas Nelson Bibles",
                description = "The first study Bible to present scriptures in the chronological flow of historical occurrence, enriched with full-color articles, timelines, and archaeological context.",
                price = "$44.99",
                oldPrice = "$59.99",
                rating = 4.8f,
                affiliateUrl = "https://amzn.to/43zbJvB",
                category = "Study Bibles",
                icon = Icons.Outlined.Book
            ),
            AffiliateBook(
                title = "The MacArthur Study Bible - NASB",
                author = "Pastor John MacArthur",
                description = "New American Standard Bible study platform incorporating extensive theological references, historical introductions, and Pastor MacArthur's exhaustive verse-by-verse notes.",
                price = "$48.50",
                oldPrice = "$64.99",
                rating = 4.9f,
                affiliateUrl = "https://amzn.to/4u2FnUU",
                category = "Study Bibles",
                icon = Icons.Outlined.Book
            ),
            AffiliateBook(
                title = "The Ten Commandments Bible Verses Wall Frame",
                author = "SELIGMANN (Exodus 20 Premium Decor)",
                description = "Inspirational, beautifully designed premium acrylic wall hanging frame (17 x 12 inches) with the Ten Commandments scripture. Perfect for spiritual homes and offices.",
                price = "$24.99",
                oldPrice = "$34.99",
                rating = 4.7f,
                affiliateUrl = "https://amzn.to/4dT8eF2",
                category = "Wall Art & Decor",
                icon = Icons.Outlined.Home
            ),
            AffiliateBook(
                title = "The Power of Now",
                author = "Eckhart Tolle",
                description = "A Guide to Spiritual Enlightenment. An extraordinary, widely read modern classic teaching readers to live in the present moment, master anxiety, and uncover profound inner peace.",
                price = "$10.50",
                oldPrice = "$16.00",
                rating = 4.8f,
                affiliateUrl = "https://amzn.to/4nZhfRI",
                category = "Spiritual Wisdom",
                icon = Icons.Outlined.LightMode
            ),
            AffiliateBook(
                title = "The Four Agreements",
                author = "Don Miguel Ruiz",
                description = "A Practical Guide to Personal Freedom. Drawing upon ancient Toltec wisdom, this powerful handbook presents four elegant self-commitments to remove suffering and build authentic joy.",
                price = "$7.20",
                oldPrice = "$12.95",
                rating = 4.9f,
                affiliateUrl = "https://amzn.to/4fNmLof",
                category = "Spiritual Wisdom",
                icon = Icons.Outlined.Bookmark
            ),
            AffiliateBook(
                title = "The Kybalion (Hermetic Philosophy Study)",
                author = "Three Initiates",
                description = "An exploration of the core Hermetic Philosophy of ancient Egypt and Greece. Expounds upon the seven foundational cosmic laws that govern spiritual and material reality.",
                price = "$8.99",
                oldPrice = "$11.95",
                rating = 4.7f,
                affiliateUrl = "https://amzn.to/432Nw0E",
                category = "Spiritual Wisdom",
                icon = Icons.Outlined.Bookmark
            ),
            AffiliateBook(
                title = "The Seven Spiritual Laws of Success",
                author = "Deepak Chopra (One Hour of Wisdom Guide)",
                description = "A Pocketbook Guide to Fulfilling Your Dreams. Outlines core spiritual insights to easily align your daily efforts with universal harmony to attract true fulfillment and abundance.",
                price = "$9.40",
                oldPrice = "$14.00",
                rating = 4.8f,
                affiliateUrl = "https://amzn.to/4dQwNCK",
                category = "Spiritual Wisdom",
                icon = Icons.Outlined.Info
            )
        )
    }
    
    val filteredBooks = if (selectedCategory == "All") {
        books
    } else {
        books.filter { it.category == selectedCategory }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Study Resources",
                        style = MaterialTheme.typography.displayMedium.copy(color = GoldPrimary, fontSize = 22.sp),
                        modifier = Modifier.testTag("books_title")
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Back",
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            // Affiliate Disclosure Banner
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = GoldPrimary.copy(alpha = 0.08f)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.2f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Info,
                            contentDescription = "Disclosure Info",
                            tint = GoldPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Supporting Sacred Word: When you purchase any study book through our recommended links below, a small affiliate commission helps support our ongoing translation database expansions, with no additional cost to you!",
                            fontSize = 12.sp,
                            lineHeight = 18.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            // Horizontal Category Selector Chips
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    categories.forEach { category ->
                        val isSelected = category == selectedCategory
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedCategory = category },
                            label = { Text(text = category, fontSize = 13.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = GoldPrimary,
                                selectedLabelColor = MaterialTheme.colorScheme.background,
                                containerColor = MaterialTheme.colorScheme.surface,
                                labelColor = MaterialTheme.colorScheme.onSurface
                            ),
                            border = null,
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
            }

            // Feed of Affiliate Books
            items(filteredBooks) { book ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(GoldPrimary.copy(alpha = 0.12f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = book.icon,
                                        contentDescription = "Book icon",
                                        tint = GoldPrimary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = book.title,
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp,
                                            lineHeight = 22.sp
                                        ),
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            text = "By ${book.author}",
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                                fontSize = 13.sp
                                            ),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f, fill = false)
                                        )
                                        Box(
                                            modifier = Modifier
                                                .background(GoldPrimary.copy(alpha = 0.12f), RoundedCornerShape(4.dp))
                                                .border(0.5.dp, GoldPrimary.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "AFFILIATE LINK",
                                                color = GoldPrimary,
                                                fontSize = 8.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Product Description
                        Text(
                            text = book.description,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 14.sp,
                                lineHeight = 20.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.82f)
                            )
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Price & Rating Section
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = book.price,
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        color = GoldPrimary,
                                        fontSize = 18.sp
                                    )
                                )
                                if (book.oldPrice != null) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = book.oldPrice,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                                            fontSize = 13.sp,
                                            textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
                                        )
                                    )
                                }
                                Spacer(modifier = Modifier.width(14.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Star,
                                        contentDescription = "Rating Star",
                                        tint = GoldPrimary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = book.rating.toString(),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }

                            // Dynamic Gold Button to Open Affiliate Link
                            Button(
                                onClick = {
                                    try {
                                        uriHandler.openUri(book.affiliateUrl)
                                    } catch (e: Exception) {
                                        // safety fallback
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = GoldPrimary
                                ),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.ShoppingCart,
                                    contentDescription = "Get Book",
                                    tint = MaterialTheme.colorScheme.background,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Get Book",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.background,
                                        fontSize = 13.sp
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
