package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.formatRupees
import com.example.ui.theme.*
import com.example.viewmodel.TournamentViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SuperAdminDashboard(
    viewModel: TournamentViewModel,
    modifier: Modifier = Modifier
) {
    val teams by viewModel.teams.collectAsState()
    val players by viewModel.players.collectAsState()
    val users by viewModel.allUsers.collectAsState()

    val pendingCount = remember(players) { players.count { it.status == "PENDING" } }
    val approvedCount = remember(players) { players.count { it.status == "APPROVED" } }
    val soldCount = remember(players) { players.count { it.auctionStatus == "SOLD" } }
    val totalTournamentSpending = remember(teams) { teams.sumOf { it.spentAmount } }

    var showResetDialog by remember { mutableStateOf(false) }
    var showAddAdminDialog by remember { mutableStateOf(false) }
    var newAdminName by remember { mutableStateOf("") }
    var newAdminEmail by remember { mutableStateOf("") }
    var newAdminPassword by remember { mutableStateOf("") }
    var newAdminRole by remember { mutableStateOf("AUCTION_ADMIN") }

    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Super Admin Organizer", fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text("DPKL Tournament Management", fontSize = 11.sp, color = KabaddiGold)
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.logout() }) {
                        Icon(Icons.Default.Logout, contentDescription = "Logout", tint = ActionRed)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = StadiumSurface)
            )
        },
        containerColor = StadiumDarkBg
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Tournament Metrics Grid
            Text(
                text = "TOURNAMENT OVERVIEW",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = KabaddiGold,
                letterSpacing = 1.sp
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard(
                    title = "Teams",
                    value = "${teams.size}",
                    icon = Icons.Default.Shield,
                    color = ActionBlue,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Players",
                    value = "${players.size}",
                    icon = Icons.Default.People,
                    color = KabaddiOrange,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Sold",
                    value = "$soldCount",
                    icon = Icons.Default.CheckCircle,
                    color = ActionGreen,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard(
                    title = "Pending Approvals",
                    value = "$pendingCount",
                    icon = Icons.Default.PendingActions,
                    color = if (pendingCount > 0) KabaddiGold else TextSecondary,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Total Auction Spending",
                    value = formatRupees(totalTournamentSpending),
                    icon = Icons.Default.CurrencyRupee,
                    color = KabaddiGold,
                    modifier = Modifier.weight(1f)
                )
            }

            // Quick Operations Navigation Grid
            Text(
                text = "TOURNAMENT ACTIONS",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = KabaddiGold,
                letterSpacing = 1.sp
            )

            ActionTile(
                title = "Live Auction Control Desk",
                subtitle = "Select players, control countdown, manage bids & mark SOLD",
                icon = Icons.Default.Gavel,
                badge = "AUCTIONEER",
                badgeColor = KabaddiOrange,
                onClick = { viewModel.navigateTo("AUCTION_CONTROL") }
            )

            ActionTile(
                title = "Live Broadcast Screen",
                subtitle = "Full-screen projector display with live bidder & countdown",
                icon = Icons.Default.Tv,
                badge = "PUBLIC VIEW",
                badgeColor = ActionBlue,
                onClick = { viewModel.navigateTo("LIVE_AUCTION") }
            )

            ActionTile(
                title = "Player Management & Approvals",
                subtitle = "$pendingCount pending applications • Total ${players.size} players",
                icon = Icons.Default.AssignmentInd,
                badge = if (pendingCount > 0) "$pendingCount PENDING" else "READY",
                badgeColor = if (pendingCount > 0) KabaddiGold else ActionGreen,
                onClick = { viewModel.navigateTo("PLAYER_LIST") }
            )

            ActionTile(
                title = "Team Management",
                subtitle = "Manage ${teams.size} teams, budgets, captains & sponsors",
                icon = Icons.Default.Groups,
                badge = "${teams.size} TEAMS",
                badgeColor = ActionBlue,
                onClick = { viewModel.navigateTo("TEAM_MANAGEMENT") }
            )

            ActionTile(
                title = "Auction History & Team Spending",
                subtitle = "View SOLD players, UNSOLD pool & team squad rosters",
                icon = Icons.Default.History,
                badge = "STATS",
                badgeColor = KabaddiGold,
                onClick = { viewModel.navigateTo("AUCTION_HISTORY") }
            )

            // Admin Accounts Management Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = StadiumSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, StadiumBorder)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "ADMIN USERS & MANAGERS",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = KabaddiGold,
                            letterSpacing = 1.sp
                        )

                        TextButton(onClick = { showAddAdminDialog = true }) {
                            Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add Admin", fontSize = 12.sp, color = KabaddiOrange)
                        }
                    }

                    users.filter { it.role != "PUBLIC" }.forEach { user ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = StadiumSurfaceVariant,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(user.name, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)
                                    Text(user.emailOrMobile, fontSize = 11.sp, color = TextSecondary)
                                }
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = when (user.role) {
                                        "SUPER_ADMIN" -> KabaddiOrange.copy(alpha = 0.2f)
                                        "AUCTION_ADMIN" -> ActionGreen.copy(alpha = 0.2f)
                                        "TEAM_MANAGER" -> ActionBlue.copy(alpha = 0.2f)
                                        else -> StadiumBorder
                                    }
                                ) {
                                    Text(
                                        text = user.role.replace("_", " "),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Database Management & Reset
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = StadiumSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, ActionRed.copy(alpha = 0.4f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "RESET TOURNAMENT DATABASE",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = ActionRed,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Resets all teams, registered players, and initial auction states to official demo data. Useful between test rounds.",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                    Button(
                        onClick = { showResetDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = ActionRed.copy(alpha = 0.2f)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, ActionRed),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, tint = ActionRed)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Reset Tournament to Seed Data", color = ActionRed, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Reset Tournament Data?") },
            text = { Text("This will reseed teams, players, and auction accounts back to initial tournament state.") },
            confirmButton = {
                Button(
                    onClick = {
                        showResetDialog = false
                        viewModel.resetTournamentData()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ActionRed)
                ) {
                    Text("Confirm Reset")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showAddAdminDialog) {
        AlertDialog(
            onDismissRequest = { showAddAdminDialog = false },
            title = { Text("Add Admin User") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = newAdminName,
                        onValueChange = { newAdminName = it },
                        label = { Text("Full Name") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = newAdminEmail,
                        onValueChange = { newAdminEmail = it },
                        label = { Text("Email / Mobile") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = newAdminPassword,
                        onValueChange = { newAdminPassword = it },
                        label = { Text("Password") },
                        singleLine = true
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = newAdminRole == "AUCTION_ADMIN",
                            onClick = { newAdminRole = "AUCTION_ADMIN" },
                            label = { Text("Auction Admin", fontSize = 11.sp) }
                        )
                        FilterChip(
                            selected = newAdminRole == "SUPER_ADMIN",
                            onClick = { newAdminRole = "SUPER_ADMIN" },
                            label = { Text("Super Admin", fontSize = 11.sp) }
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newAdminEmail.isNotBlank()) {
                            viewModel.createAdminUser(
                                name = newAdminName.ifBlank { "Admin" },
                                email = newAdminEmail,
                                pass = newAdminPassword.ifBlank { "admin" },
                                role = newAdminRole
                            )
                            showAddAdminDialog = false
                            newAdminName = ""
                            newAdminEmail = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = KabaddiOrange)
                ) {
                    Text("Save Admin")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddAdminDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = StadiumSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, StadiumBorder)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
            }
            Text(value, fontSize = 18.sp, fontWeight = FontWeight.Black, color = TextPrimary)
            Text(title, fontSize = 11.sp, color = TextSecondary, maxLines = 1)
        }
    }
}

@Composable
fun ActionTile(
    title: String,
    subtitle: String,
    icon: ImageVector,
    badge: String,
    badgeColor: Color,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        color = StadiumSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, StadiumBorder)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(KabaddiOrange.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = KabaddiOrange, modifier = Modifier.size(24.dp))
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextPrimary)
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = badgeColor.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = badge,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = badgeColor,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Text(subtitle, fontSize = 12.sp, color = TextSecondary)
            }

            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextSecondary)
        }
    }
}
