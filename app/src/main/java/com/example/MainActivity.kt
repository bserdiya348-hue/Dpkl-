package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.example.viewmodel.TournamentViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: TournamentViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainAppContainer(viewModel = viewModel)
            }
        }
    }
}

data class NavItem(
    val route: String,
    val title: String,
    val icon: ImageVector
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppContainer(viewModel: TournamentViewModel) {
    val activeScreen by viewModel.activeScreen.collectAsState()
    val currentRole by viewModel.currentRole.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val userMessage by viewModel.userMessage.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(userMessage) {
        userMessage?.let { msg ->
            snackbarHostState.showSnackbar(
                message = msg,
                duration = SnackbarDuration.Short
            )
            viewModel.clearMessage()
        }
    }

    // Handle back button: if in a sub-screen, return to main role dashboard or live auction
    BackHandler(enabled = activeScreen != "LIVE_AUCTION" && activeScreen != "LOGIN") {
        when (currentRole) {
            "SUPER_ADMIN" -> viewModel.navigateTo("SUPER_ADMIN")
            "AUCTION_ADMIN" -> viewModel.navigateTo("AUCTION_CONTROL")
            "TEAM_MANAGER" -> viewModel.navigateTo("TEAM_DASHBOARD")
            "PLAYER" -> viewModel.navigateTo("PLAYER_PROFILE")
            else -> viewModel.navigateTo("LIVE_AUCTION")
        }
    }

    // Role-dependent bottom navigation tabs
    val navItems = remember(currentRole) {
        when (currentRole) {
            "SUPER_ADMIN" -> listOf(
                NavItem("SUPER_ADMIN", "Admin", Icons.Default.Dashboard),
                NavItem("AUCTION_CONTROL", "Desk", Icons.Default.Gavel),
                NavItem("LIVE_AUCTION", "Live", Icons.Default.LiveTv),
                NavItem("PLAYER_LIST", "Players", Icons.Default.People),
                NavItem("TEAM_MANAGEMENT", "Teams", Icons.Default.Shield),
                NavItem("AUCTION_HISTORY", "History", Icons.Default.History)
            )
            "AUCTION_ADMIN" -> listOf(
                NavItem("AUCTION_CONTROL", "Desk", Icons.Default.Gavel),
                NavItem("LIVE_AUCTION", "Live", Icons.Default.LiveTv),
                NavItem("PLAYER_LIST", "Players", Icons.Default.People),
                NavItem("AUCTION_HISTORY", "History", Icons.Default.History)
            )
            "TEAM_MANAGER" -> listOf(
                NavItem("TEAM_DASHBOARD", "My Squad", Icons.Default.Shield),
                NavItem("LIVE_AUCTION", "Live", Icons.Default.LiveTv),
                NavItem("PLAYER_LIST", "Players", Icons.Default.People),
                NavItem("AUCTION_HISTORY", "History", Icons.Default.History)
            )
            "PLAYER" -> listOf(
                NavItem("PLAYER_PROFILE", "My Profile", Icons.Default.Person),
                NavItem("LIVE_AUCTION", "Live", Icons.Default.LiveTv),
                NavItem("AUCTION_HISTORY", "Results", Icons.Default.History)
            )
            else -> listOf(
                NavItem("LIVE_AUCTION", "Live Auction", Icons.Default.LiveTv),
                NavItem("PLAYER_LIST", "Players", Icons.Default.People),
                NavItem("AUCTION_HISTORY", "Results", Icons.Default.History),
                NavItem("LOGIN", "Sign In", Icons.Default.AccountCircle)
            )
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isWideScreen = maxWidth >= 600.dp

        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                if (activeScreen != "LOGIN") {
                    TopAppBar(
                        title = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .border(1.dp, KabaddiGold, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Image(
                                        painter = painterResource(id = R.drawable.dpkl_logo),
                                        contentDescription = "DPKL Logo",
                                        modifier = Modifier.fillMaxSize().clip(CircleShape)
                                    )
                                }
                                Column {
                                    Text(
                                        text = "DPKL AUCTION",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Black,
                                        color = KabaddiGold,
                                        letterSpacing = 1.sp
                                    )
                                    Text(
                                        text = "Pro Kabaddi Tournament",
                                        fontSize = 10.sp,
                                        color = TextSecondary
                                    )
                                }
                            }
                        },
                        actions = {
                            // Current User / Role indicator
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = StadiumSurfaceVariant,
                                border = androidx.compose.foundation.BorderStroke(1.dp, StadiumBorder),
                                modifier = Modifier.padding(end = 4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(
                                                when (currentRole) {
                                                    "SUPER_ADMIN" -> KabaddiOrange
                                                    "AUCTION_ADMIN" -> ActionGreen
                                                    "TEAM_MANAGER" -> ActionBlue
                                                    "PLAYER" -> KabaddiGold
                                                    else -> TextSecondary
                                                }
                                            )
                                    )
                                    Text(
                                        text = when (currentRole) {
                                            "SUPER_ADMIN" -> "SUPER ADMIN"
                                            "AUCTION_ADMIN" -> "AUCTIONEER"
                                            "TEAM_MANAGER" -> "MANAGER"
                                            "PLAYER" -> "PLAYER"
                                            else -> "VIEWER"
                                        },
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                }
                            }

                            if (currentUser != null) {
                                IconButton(onClick = { viewModel.logout() }) {
                                    Icon(Icons.Default.Logout, contentDescription = "Sign Out", tint = TextSecondary)
                                }
                            } else {
                                IconButton(onClick = { viewModel.navigateTo("LOGIN") }) {
                                    Icon(Icons.Default.Login, contentDescription = "Sign In", tint = KabaddiOrange)
                                }
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = StadiumSurface)
                    )
                }
            },
            bottomBar = {
                if (activeScreen != "LOGIN" && !isWideScreen) {
                    NavigationBar(
                        containerColor = StadiumSurface,
                        contentColor = TextPrimary
                    ) {
                        navItems.forEach { item ->
                            val isSelected = activeScreen == item.route
                            NavigationBarItem(
                                selected = isSelected,
                                onClick = { viewModel.navigateTo(item.route) },
                                icon = {
                                    Icon(
                                        item.icon,
                                        contentDescription = item.title,
                                        tint = if (isSelected) KabaddiOrange else TextSecondary
                                    )
                                },
                                label = {
                                    Text(
                                        text = item.title,
                                        fontSize = 10.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) KabaddiOrange else TextSecondary
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    indicatorColor = KabaddiOrange.copy(alpha = 0.2f)
                                ),
                                modifier = Modifier.testTag("nav_tab_${item.route.lowercase()}")
                            )
                        }
                    }
                }
            },
            containerColor = StadiumDarkBg
        ) { innerPadding ->
            Row(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                // Tablet / TV / Large screen Navigation Rail
                if (activeScreen != "LOGIN" && isWideScreen) {
                    NavigationRail(
                        containerColor = StadiumSurface,
                        contentColor = TextPrimary
                    ) {
                        Spacer(modifier = Modifier.height(16.dp))
                        navItems.forEach { item ->
                            val isSelected = activeScreen == item.route
                            NavigationRailItem(
                                selected = isSelected,
                                onClick = { viewModel.navigateTo(item.route) },
                                icon = {
                                    Icon(
                                        item.icon,
                                        contentDescription = item.title,
                                        tint = if (isSelected) KabaddiOrange else TextSecondary
                                    )
                                },
                                label = {
                                    Text(
                                        item.title,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) KabaddiOrange else TextSecondary
                                    )
                                },
                                colors = NavigationRailItemDefaults.colors(
                                    indicatorColor = KabaddiOrange.copy(alpha = 0.2f)
                                )
                            )
                        }
                    }
                }

                // Screen Destination Container with Animated Transitions
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(StadiumDarkBg)
                ) {
                    AnimatedContent(
                        targetState = activeScreen,
                        label = "ScreenTransition",
                        transitionSpec = {
                            fadeIn() togetherWith fadeOut()
                        }
                    ) { target ->
                        when (target) {
                            "LOGIN" -> LoginScreen(viewModel = viewModel)
                            "REGISTER_PLAYER" -> PlayerRegistrationScreen(viewModel = viewModel)
                            "LIVE_AUCTION" -> LiveAuctionScreen(viewModel = viewModel)
                            "AUCTION_CONTROL" -> AuctionControlScreen(viewModel = viewModel)
                            "SUPER_ADMIN" -> SuperAdminDashboard(viewModel = viewModel)
                            "TEAM_MANAGEMENT" -> TeamManagementScreen(viewModel = viewModel)
                            "PLAYER_LIST" -> PlayerListScreen(viewModel = viewModel)
                            "TEAM_DASHBOARD" -> TeamManagerDashboard(viewModel = viewModel)
                            "PLAYER_PROFILE" -> PlayerDashboard(viewModel = viewModel)
                            "AUCTION_HISTORY" -> AuctionHistoryScreen(viewModel = viewModel)
                            else -> LiveAuctionScreen(viewModel = viewModel)
                        }
                    }
                }
            }
        }
    }
}
