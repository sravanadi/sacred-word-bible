package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.Bookmark
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.ui.screens.AppViewModelProvider
import com.example.ui.screens.bible.BibleScreen
import com.example.ui.screens.bible.BibleViewModel
import com.example.ui.screens.bible.ChaptersScreen
import com.example.ui.screens.bible.ChaptersViewModel
import com.example.ui.screens.bookmarks.BookmarksScreen
import com.example.ui.screens.bookmarks.BookmarksViewModel
import com.example.ui.screens.home.HomeScreen
import com.example.ui.screens.home.HomeViewModel
import com.example.ui.screens.library.LibraryScreen
import com.example.ui.screens.library.LibraryViewModel
import com.example.ui.screens.reader.ReaderScreen
import com.example.ui.screens.reader.ReaderViewModel
import com.example.ui.screens.settings.SettingsScreen
import com.example.ui.screens.settings.SettingsViewModel
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.MutedLabel
import com.example.ui.theme.SacredWordTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import com.example.ui.screens.onboarding.OnboardingScreen
import androidx.compose.material.icons.outlined.Settings
import com.example.ui.components.*

enum class AppStartupState {
    SPLASH,
    ONBOARDING,
    CONTENT
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Retrieve application instance safely directly from Activity's inherited property
        val sacredWordApp = (application as? SacredWordApplication) ?: SacredWordApplication.instance
        val userRepo = sacredWordApp.container.userDataRepository

        setContent {
            val isDarkPref by userRepo.isDarkTheme.collectAsState(initial = true)
            val fontFamilyPref by userRepo.fontFamilyName.collectAsState(initial = "sans")

            SacredWordTheme(isDark = isDarkPref, fontFamilyName = fontFamilyPref) {
                MainAppScreen(sacredWordApp)
            }
        }
    }
}

@Composable
fun MainAppScreen(application: SacredWordApplication) {
    val userRepo = application.container.userDataRepository
    val completedOnboarding by userRepo.completedOnboarding.collectAsState(initial = null)

    var showSplash by androidx.compose.runtime.remember {
        androidx.compose.runtime.mutableStateOf(true)
    }

    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(2200)
        showSplash = false
    }

    if (showSplash) {
        OpeningSplashScreen(modifier = Modifier.fillMaxSize())
        return
    }

    if (completedOnboarding == null) {
        Box(
            modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = GoldPrimary)
        }
        return
    }

    if (completedOnboarding == false) {
        val coroutineScope = rememberCoroutineScope()
        OnboardingScreen(
            onOnboardingComplete = {
                coroutineScope.launch {
                    userRepo.setCompletedOnboarding(true)
                }
            },
            modifier = Modifier.fillMaxSize()
        )
        return
    }

    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val tabRoutes = listOf("home", "bible", "bookmarks", "library", "settings")
    val showBottomBar = currentRoute in tabRoutes

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                // Real-time AdMob Banner is on the UPPER side of the navigation menu bar
                AdMobBanner(
                    modifier = Modifier.fillMaxWidth()
                )

                if (showBottomBar) {
                    NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surface,
                            modifier = Modifier
                                .testTag("bottom_nav_bar")
                                .navigationBarsPadding()
                        ) {
                            val transparentColors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color.Unspecified,
                                unselectedIconColor = Color.Unspecified,
                                indicatorColor = Color.Transparent
                            )

                            // Item 1: Home
                            NavigationBarItem(
                                selected = currentRoute == "home",
                                colors = transparentColors,
                                onClick = {
                                    if (currentRoute != "home") {
                                        navController.navigate("home") {
                                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                },
                                icon = {
                                    HomeNavIcon(
                                        selected = currentRoute == "home"
                                    )
                                }
                            )

                            // Item 2: Bible Browser
                            NavigationBarItem(
                                selected = currentRoute == "bible",
                                colors = transparentColors,
                                onClick = {
                                    if (currentRoute != "bible") {
                                        navController.navigate("bible") {
                                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                },
                                icon = {
                                    BibleNavIcon(
                                        selected = currentRoute == "bible"
                                    )
                                }
                            )

                            // Item 3: Bookmarks
                            NavigationBarItem(
                                selected = currentRoute == "bookmarks",
                                colors = transparentColors,
                                onClick = {
                                    if (currentRoute != "bookmarks") {
                                        navController.navigate("bookmarks") {
                                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                },
                                icon = {
                                    BookmarksNavIcon(
                                        selected = currentRoute == "bookmarks"
                                    )
                                }
                            )

                            // Item 4: Library
                            NavigationBarItem(
                                selected = currentRoute == "library",
                                colors = transparentColors,
                                onClick = {
                                    if (currentRoute != "library") {
                                        navController.navigate("library") {
                                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                },
                                icon = {
                                    LibraryNavIcon(
                                        selected = currentRoute == "library"
                                    )
                                }
                            )

                            // Item 5: Settings
                            NavigationBarItem(
                                selected = currentRoute == "settings",
                                colors = transparentColors,
                                onClick = {
                                    if (currentRoute != "settings") {
                                        navController.navigate("settings") {
                                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                },
                                icon = {
                                    SettingsNavIcon(
                                        selected = currentRoute == "settings"
                                    )
                                }
                            )
                        }
                } else {
                    Spacer(modifier = Modifier.navigationBarsPadding())
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("home") {
                val homeViewModel: HomeViewModel = viewModel(factory = AppViewModelProvider.Factory)
                HomeScreen(
                    viewModel = homeViewModel,
                    onNavigateToReader = { tId, bId, chapter ->
                        navController.navigate("reader/$tId/$bId/$chapter")
                    },
                    onNavigateToLibrary = {
                        navController.navigate("library")
                    },
                    onNavigateToBooks = {
                        navController.navigate("books")
                    }
                )
            }

            composable("bible") {
                val bibleViewModel: BibleViewModel = viewModel(factory = AppViewModelProvider.Factory)
                BibleScreen(
                    viewModel = bibleViewModel,
                    onNavigateToChapters = { tId, bId ->
                        navController.navigate("chapters/$tId/$bId")
                    },
                    onNavigateToLibrary = {
                        navController.navigate("library")
                    }
                )
            }

            composable(
                route = "chapters/{translationId}/{bookId}",
                arguments = listOf(
                    navArgument("translationId") { type = NavType.StringType },
                    navArgument("bookId") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val tId = backStackEntry.arguments?.getString("translationId") ?: ""
                val bId = backStackEntry.arguments?.getString("bookId") ?: ""
                
                val chaptersViewModel: ChaptersViewModel = viewModel(
                    factory = ChaptersViewModel.Factory(
                        application.container.bibleRepository,
                        tId,
                        bId
                    )
                )

                ChaptersScreen(
                    viewModel = chaptersViewModel,
                    onNavigateToReader = { ch ->
                        navController.navigate("reader/$tId/$bId/$ch")
                    },
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }

            composable(
                route = "reader/{translationId}/{bookId}/{chapter}",
                arguments = listOf(
                    navArgument("translationId") { type = NavType.StringType },
                    navArgument("bookId") { type = NavType.StringType },
                    navArgument("chapter") { type = NavType.IntType }
                )
            ) { backStackEntry ->
                val tId = backStackEntry.arguments?.getString("translationId") ?: ""
                val bId = backStackEntry.arguments?.getString("bookId") ?: ""
                val chapter = backStackEntry.arguments?.getInt("chapter") ?: 1

                val readerViewModel: ReaderViewModel = viewModel(factory = AppViewModelProvider.Factory)
                ReaderScreen(
                    viewModel = readerViewModel,
                    translationId = tId,
                    bookId = bId,
                    initialChapter = chapter,
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }

            composable("bookmarks") {
                val bookmarksViewModel: BookmarksViewModel = viewModel(factory = AppViewModelProvider.Factory)
                BookmarksScreen(
                    viewModel = bookmarksViewModel,
                    onNavigateToReader = { tId, bId, chapter ->
                        navController.navigate("reader/$tId/$bId/$chapter")
                    }
                )
            }

            composable("library") {
                val libraryViewModel: LibraryViewModel = viewModel(factory = AppViewModelProvider.Factory)
                LibraryScreen(
                    viewModel = libraryViewModel
                )
            }

            composable("settings") {
                val settingsViewModel: SettingsViewModel = viewModel(factory = AppViewModelProvider.Factory)
                SettingsScreen(
                    viewModel = settingsViewModel,
                    onNavigateToBooks = {
                        navController.navigate("books")
                    }
                )
            }

            composable("books") {
                com.example.ui.screens.books.BooksScreen(
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}

@Composable
fun ThreeDNavigationIcon(
    imageVector: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    contentDescription: String
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(44.dp)
    ) {
        if (selected) {
            // 1. Core Ambient Glow Underneath
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                GoldPrimary.copy(alpha = 0.5f),
                                GoldPrimary.copy(alpha = 0.15f),
                                Color.Transparent
                            )
                        ),
                        shape = CircleShape
                    )
            )

            // 2. Realistic 3D Cast Shadow (deep black offset down-right)
            Icon(
                imageVector = imageVector,
                contentDescription = null,
                tint = Color.Black.copy(alpha = 0.6f),
                modifier = Modifier
                    .size(28.dp)
                    .offset(x = 1.8.dp, y = 2.2.dp)
            )

            // 3. Realistic 3D Ambient Shadow (warm darker gold outline/back bevel)
            Icon(
                imageVector = imageVector,
                contentDescription = null,
                tint = Color(0xFF453612).copy(alpha = 0.8f),
                modifier = Modifier
                    .size(28.dp)
                    .offset(x = 1.0.dp, y = 1.2.dp)
            )

            // 4. Realistic 3D Specular Highlight Bevel (bright white-gold offset top-left)
            Icon(
                imageVector = imageVector,
                contentDescription = null,
                tint = Color(0xFFFFF7E0),
                modifier = Modifier
                    .size(28.dp)
                    .offset(x = (-1.0).dp, y = (-1.0).dp)
            )

            // 5. Foreground Solid Embossed Dynamic Gold Metallic Icon
            Icon(
                imageVector = imageVector,
                contentDescription = contentDescription,
                tint = GoldPrimary,
                modifier = Modifier.size(28.dp)
            )
        } else {
            // Inactive tactile sunken state
            Icon(
                imageVector = imageVector,
                contentDescription = null,
                tint = Color.Black.copy(alpha = 0.16f),
                modifier = Modifier
                    .size(28.dp)
                    .offset(x = 1.0.dp, y = 1.2.dp)
            )

            Icon(
                imageVector = imageVector,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.06f),
                modifier = Modifier
                    .size(28.dp)
                    .offset(x = (-0.5).dp, y = (-0.5).dp)
            )

            Icon(
                imageVector = imageVector,
                contentDescription = contentDescription,
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.42f),
                modifier = Modifier.size(28.dp)
            )
        }
    }
}
