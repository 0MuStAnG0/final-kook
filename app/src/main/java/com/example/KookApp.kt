package com.example

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.feed.FeedScreen
import com.example.utils.toPersianNumber
import com.example.ui.profile.ProfileScreen
import com.example.ui.theme.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight

import com.example.ui.discover.DiscoverScreen
import com.example.ui.profile.EditProfileScreen
import com.example.ui.settings.SettingsScreen
import com.example.ui.wallet.WalletScreen
import com.example.ui.add.AddScreen
import com.example.ui.map.MapScreen
import com.example.ui.auth.AuthScreen
import com.example.ui.store.StoreApplicationScreen

import com.example.data.AppState
import com.example.data.UserMode
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.ui.Alignment
import androidx.compose.material3.CircularProgressIndicator

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Discover : Screen("discover")
    object Add : Screen("add")
    object Map : Screen("map")
    object Wallet : Screen("wallet")
    object Profile : Screen("profile")
    object Settings : Screen("settings")
    object EditProfile : Screen("edit_profile")
    object Auth : Screen("auth?initialTab={initialTab}") {
        fun createRoute(initialTab: Int = 0) = "auth?initialTab=$initialTab"
    }
    object StoreApplication : Screen("store_application")
    object Inbox : Screen("inbox")
    object Chat : Screen("chat/{userId}") {
        fun createRoute(userId: String) = "chat/$userId"
    }
}

@Composable
fun KookApp() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val context = androidx.compose.ui.platform.LocalContext.current

    LaunchedEffect(Unit) {
        try {
            kotlinx.coroutines.withTimeoutOrNull(2500) {
                val authRepository = com.example.network.AuthRepository(context)
                authRepository.restoreSession()
            }
        } catch (_: Throwable) {}
        try {
            kotlinx.coroutines.withTimeoutOrNull(2500) {
                val handshakeManager = com.example.network.UiHandshakeManager(context)
                handshakeManager.performHandshake()
            }
        } catch (_: Throwable) {}
        try {
            kotlinx.coroutines.withTimeoutOrNull(2500) {
                AppState.testServerConnection(context)
            }
        } catch (_: Throwable) {}
    }
    
    // Hide bottom bar on auth, store application, settings, chat, and edit profile screens
    val showBottomBar = currentRoute in listOf(
        Screen.Home.route, Screen.Discover.route, Screen.Add.route, Screen.Map.route, Screen.Wallet.route, Screen.Profile.route
    )

    val startDestination = Screen.Home.route

    Box(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Scaffold(
            modifier = Modifier
                .widthIn(max = 600.dp)
                .fillMaxHeight(),
            bottomBar = { if (showBottomBar) BottomNavigationBar(navController) }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = startDestination,
                modifier = Modifier.padding(innerPadding)
            ) {
                composable(Screen.Home.route) { 
                    FeedScreen(
                        onNavigateToInbox = { navController.navigate(Screen.Inbox.route) },
                        onNavigateToProfile = { userId -> 
                            navController.navigate("profile/$userId") {
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        onNavigateToAuth = { navController.navigate(Screen.Auth.createRoute(0)) },
                        onNavigateToRegister = { navController.navigate(Screen.Auth.createRoute(1)) }
                    ) 
                }
                composable(Screen.Discover.route) { 
                    DiscoverScreen(onNavigateToStore = { storeId ->
                        navController.navigate("profile/$storeId") {
                            launchSingleTop = true
                        }
                    }) 
                }
                composable(Screen.Add.route) { AddScreen() }
                composable(Screen.Map.route) { 
                    MapScreen(onNavigateToStore = { storeId ->
                        navController.navigate("profile/$storeId") {
                            launchSingleTop = true
                        }
                    }) 
                }
                composable(Screen.Wallet.route) { 
                    WalletScreen(
                        onNavigateToChat = { uid -> navController.navigate(Screen.Chat.createRoute(uid)) },
                        onNavigateToAuth = { navController.navigate(Screen.Auth.createRoute(0)) },
                        onNavigateToRegister = { navController.navigate(Screen.Auth.createRoute(1)) }
                    ) 
                }
                composable("profile/{userId}") { backStackEntry ->
                    val userId = backStackEntry.arguments?.getString("userId")
                    ProfileScreen(
                        userId = userId,
                        onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                        onNavigateToEditProfile = { navController.navigate(Screen.EditProfile.route) },
                        onNavigateToStoreApplication = { navController.navigate(Screen.StoreApplication.route) },
                        onNavigateToChat = { uid -> navController.navigate(Screen.Chat.createRoute(uid)) },
                        onNavigateToStore = { storeId ->
                            navController.navigate("profile/$storeId") {
                                launchSingleTop = true
                            }
                        },
                        onNavigateToAuth = { navController.navigate(Screen.Auth.createRoute(0)) },
                        onNavigateToRegister = { navController.navigate(Screen.Auth.createRoute(1)) }
                    )
                }
                composable(Screen.Profile.route) { 
                    ProfileScreen(
                        onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                        onNavigateToEditProfile = { navController.navigate(Screen.EditProfile.route) },
                        onNavigateToStoreApplication = { navController.navigate(Screen.StoreApplication.route) },
                        onNavigateToStore = { storeId ->
                            navController.navigate("profile/$storeId") {
                                launchSingleTop = true
                            }
                        },
                        onNavigateToAuth = { navController.navigate(Screen.Auth.createRoute(0)) },
                        onNavigateToRegister = { navController.navigate(Screen.Auth.createRoute(1)) }
                    ) 
                }
                composable(Screen.Settings.route) {
                    SettingsScreen(
                        onBack = { navController.popBackStack() },
                        onNavigateToAuth = { navController.navigate(Screen.Auth.createRoute(0)) },
                        onNavigateToStoreApplication = { navController.navigate(Screen.StoreApplication.route) }
                    )
                }
                composable(
                    route = Screen.Auth.route,
                    arguments = listOf(
                        androidx.navigation.navArgument("initialTab") {
                            type = androidx.navigation.NavType.IntType
                            defaultValue = 0
                        }
                    )
                ) { backStackEntry ->
                    val initialTab = backStackEntry.arguments?.getInt("initialTab") ?: 0
                    AuthScreen(
                        initialTab = initialTab,
                        onAuthSuccess = {
                            if (navController.previousBackStackEntry != null) {
                                navController.popBackStack()
                            } else {
                                navController.navigate(Screen.Home.route) {
                                    popUpTo(Screen.Auth.route) { inclusive = true }
                                }
                            }
                        },
                        onSkipOrClose = {
                            if (navController.previousBackStackEntry != null) {
                                navController.popBackStack()
                            } else {
                                navController.navigate(Screen.Home.route) {
                                    popUpTo(Screen.Auth.route) { inclusive = true }
                                }
                            }
                        }
                    )
                }
                composable(Screen.StoreApplication.route) {
                    StoreApplicationScreen(
                        onBack = { navController.popBackStack() },
                        onApplicationApproved = {
                            navController.navigate(Screen.Profile.route) {
                                popUpTo(Screen.Home.route)
                            }
                        }
                    )
                }
                composable(Screen.Inbox.route) {
                    com.example.ui.chat.InboxScreen(
                        onBack = { navController.popBackStack() },
                        onNavigateToChat = { userId -> navController.navigate(Screen.Chat.createRoute(userId)) }
                    )
                }
                composable(Screen.Chat.route) { backStackEntry ->
                    val userId = backStackEntry.arguments?.getString("userId") ?: ""
                    com.example.ui.chat.ChatScreen(
                        userId = userId,
                        onBack = { navController.popBackStack() },
                        onNavigateToAuth = { navController.navigate(Screen.Auth.createRoute(0)) },
                        onNavigateToRegister = { navController.navigate(Screen.Auth.createRoute(1)) }
                    )
                }
                composable(Screen.EditProfile.route) {
                    EditProfileScreen(onBack = { navController.popBackStack() })
                }
            }
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavHostController) {
    val currentMode = AppState.currentUserMode
    // Center item is Screen.Add for STORE, and Screen.Map for CUSTOMER
    val centerScreen = if (currentMode == UserMode.STORE) Screen.Add else Screen.Map
    val items = listOf(Screen.Home, Screen.Discover, centerScreen, Screen.Wallet, Screen.Profile)
    
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
    ) {
        Surface(
            color = MaterialTheme.colorScheme.background.copy(alpha = 0.95f),
            shadowElevation = 20.dp,
            shape = RoundedCornerShape(28.dp),
            border = BorderStroke(1.dp, GlassBorder)
        ) {
            NavigationBar(
                containerColor = Color.Transparent,
                contentColor = SlateText,
                tonalElevation = 0.dp,
                modifier = Modifier.height(72.dp),
                windowInsets = WindowInsets(0.dp)
            ) {
                items.forEach { screen ->
                    val label = when (screen) {
                        Screen.Home -> stringResource(R.string.feed_tab)
                        Screen.Discover -> stringResource(R.string.discover_tab)
                        Screen.Add -> ""
                        Screen.Map -> ""
                        Screen.Wallet -> if (currentMode == UserMode.STORE) stringResource(R.string.orders_tab) else stringResource(R.string.wallet_tab)
                        Screen.Profile -> stringResource(R.string.profile_tab)
                        else -> ""
                    }

                    val iconComposable: @Composable () -> Unit = when (screen) {
                        Screen.Home -> { { Icon(Icons.Default.Home, contentDescription = "Home") } }
                        Screen.Discover -> { { Icon(Icons.Default.Search, contentDescription = "Discover") } }
                        Screen.Add -> {
                            {
                                Box(
                                    modifier = Modifier
                                        .offset(y = (-12).dp)
                                        .size(54.dp)
                                        .shadow(elevation = 12.dp, shape = RoundedCornerShape(16.dp), spotColor = FuchsiaAccent)
                                        .fluidAnimatedGradient(shape = RoundedCornerShape(16.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.AddCircle, contentDescription = "افزودن محصول", tint = Color.White, modifier = Modifier.size(30.dp))
                                }
                            }
                        }
                        Screen.Map -> {
                            {
                                Box(
                                    modifier = Modifier
                                        .offset(y = (-12).dp)
                                        .size(54.dp)
                                        .shadow(elevation = 12.dp, shape = RoundedCornerShape(16.dp), spotColor = IndigoPrimary)
                                        .background(
                                            Brush.linearGradient(
                                                listOf(IndigoPrimary, BluePrimary, EmeraldSuccess)
                                            ),
                                            shape = RoundedCornerShape(16.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Map, contentDescription = "نقشه فروشگاه‌ها", tint = Color.White, modifier = Modifier.size(28.dp))
                                }
                            }
                        }
                        Screen.Wallet -> {
                            {
                                if (currentMode == UserMode.STORE) {
                                    val storePendingOrders = AppState.customerOrders.count {
                                        it.status == com.example.data.OrderStatus.PENDING ||
                                            it.status == com.example.data.OrderStatus.PREPARING
                                    }
                                    if (storePendingOrders > 0) {
                                        BadgedBox(
                                            badge = {
                                                Badge(containerColor = FuchsiaAccent, contentColor = Color.White) {
                                                    Text(storePendingOrders.toPersianNumber(), style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        ) {
                                            Icon(Icons.Default.Assignment, contentDescription = "Orders")
                                        }
                                    } else {
                                        Icon(Icons.Default.Assignment, contentDescription = "Orders")
                                    }
                                } else {
                                    val cartCount = AppState.cartItems.size
                                    if (cartCount > 0) {
                                        BadgedBox(
                                            badge = {
                                                Badge(containerColor = IndigoPrimary, contentColor = Color.White) {
                                                    Text(cartCount.toPersianNumber(), style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        ) {
                                            Icon(Icons.Default.ShoppingCart, contentDescription = "Shopping Cart")
                                        }
                                    } else {
                                        Icon(Icons.Default.ShoppingCart, contentDescription = "Shopping Cart")
                                    }
                                }
                            }
                        }
                        Screen.Profile -> { { Icon(Icons.Default.Person, contentDescription = "Profile") } }
                        else -> { {} }
                    }

                    val isSelected = currentRoute == screen.route

                    NavigationBarItem(
                        icon = iconComposable,
                        label = { 
                            if (label.isNotEmpty()) {
                                Text(
                                    text = label, 
                                    style = MaterialTheme.typography.labelSmall, 
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                ) 
                            }
                        },
                        selected = isSelected,
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = if (screen == Screen.Wallet && currentMode == UserMode.STORE) FuchsiaAccent else IndigoPrimary,
                            unselectedIconColor = SlateMuted,
                            selectedTextColor = if (screen == Screen.Wallet && currentMode == UserMode.STORE) FuchsiaAccent else IndigoPrimary,
                            unselectedTextColor = SlateMuted,
                            indicatorColor = (if (screen == Screen.Wallet && currentMode == UserMode.STORE) FuchsiaAccent else IndigoPrimary).copy(alpha = 0.15f)
                        ),
                        onClick = {
                            if (currentRoute != screen.route) {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}
