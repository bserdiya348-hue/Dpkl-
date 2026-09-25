package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.viewmodel.TournamentViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamManagerDashboard(
    viewModel: TournamentViewModel,
    modifier: Modifier = Modifier
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val teams by viewModel.teams.collectAsState()
    val players by viewModel.players.collectAsState()

    // Determine the manager's team
    val managerTeam = remember(currentUser, teams) {
        val teamId = currentUser?.teamId
        if (teamId != null) {
            teams.find { it.id == teamId }
        } else {
            teams.firstOrNull()
        }
    }

    val purchasedPlayers = remember(players, managerTeam?.id) {
        if (managerTeam != null) {
            players.filter { it.soldTeamId == managerTeam.id }
        } else emptyList()
    }

    val remainingPurse = (managerTeam?.purseBudget ?: 5000000L) - (managerTeam?.spentAmount ?: 0L)
    val spendRatio = ((managerTeam?.spentAmount ?: 0L).toFloat() / (managerTeam?.purseBudget ?: 5000000L).coerceAtLeast(1).toFloat()).coerceIn(0f, 1f)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(managerTeam?.name ?: "Team Manager", fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text("Official Manager Desk", fontSize = 11.sp, color = KabaddiGold)
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.navigateTo("LIVE_AUCTION") }) {
                        Icon(Icons.Default.Tv, contentDescription = "Watch Live", tint = KabaddiGold)
                    }
                    IconButton(onClick = { viewModel.logout() }) {
                        Icon(Icons.Default.Logout, contentDescription = "Logout", tint = ActionRed)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = StadiumSurface)
            )
        },
        containerColor = StadiumDarkBg
    ) { innerPadding ->
        if (managerTeam == null) {
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text("No team assigned to this manager account.", color = TextSecondary)
            }
        } else {
            LazyColumn(
                modifier = modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Team Profile Header Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = StadiumSurface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, parseColorHex(managerTeam.logoColor))
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                TeamBadge(
                                    teamName = managerTeam.name,
                                    shortCode = managerTeam.shortCode,
                                    colorHex = managerTeam.logoColor,
                                    size = 56.dp
                                )
                                Column {
                                    Text(managerTeam.name, fontSize = 20.sp, fontWeight = FontWeight.Black, color = TextPrimary)
                                    Text("Captain: ${managerTeam.captainName.ifBlank { "Unassigned" }}", fontSize = 13.sp, color = TextSecondary)
                                    Text("Sponsor: ${managerTeam.sponsorName.ifBlank { "Official Sponsor" }}", fontSize = 12.sp, color = TextMuted)
                                }
                            }

                            HorizontalDivider(color = StadiumBorder)

                            // Purse Budget Overview
                            Text("TEAM PURSE FINANCIALS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = KabaddiGold, letterSpacing = 1.sp)

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("Total Budget", fontSize = 11.sp, color = TextSecondary)
                                    Text(formatRupees(managerTeam.purseBudget), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Total Spent", fontSize = 11.sp, color = TextSecondary)
                                    Text(formatRupees(managerTeam.spentAmount), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = ActionRed)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Remaining Purse", fontSize = 11.sp, color = KabaddiGold)
                                    Text(formatRupees(remainingPurse), fontSize = 18.sp, fontWeight = FontWeight.Black, color = KabaddiGold)
                                }
                            }

                            LinearProgressIndicator(
                                progress = { spendRatio },
                                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                                color = parseColorHex(managerTeam.logoColor),
                                trackColor = StadiumBorder
                            )
                        }
                    }
                }

                // Live Auction Shortcut
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = StadiumSurfaceVariant,
                        border = androidx.compose.foundation.BorderStroke(1.dp, KabaddiOrange.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(Icons.Default.LiveTv, contentDescription = null, tint = ActionRed)
                                Column {
                                    Text("Live Auction in Progress", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                                    Text("Follow bids and timer on big stage", fontSize = 11.sp, color = TextSecondary)
                                }
                            }
                            Button(
                                onClick = { viewModel.navigateTo("LIVE_AUCTION") },
                                colors = ButtonDefaults.buttonColors(containerColor = KabaddiOrange),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Watch", fontSize = 12.sp)
                            }
                        }
                    }
                }

                // Purchased Players Roster
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "PURCHASED SQUAD PLAYERS (${purchasedPlayers.size})",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = KabaddiGold,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Total Spent: ${formatRupees(managerTeam.spentAmount)}",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                }

                if (purchasedPlayers.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = StadiumSurface),
                            border = androidx.compose.foundation.BorderStroke(1.dp, StadiumBorder)
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.GroupAdd, contentDescription = null, tint = TextMuted, modifier = Modifier.size(36.dp))
                                Text("No players purchased yet.", color = TextPrimary, fontWeight = FontWeight.SemiBold)
                                Text("Your winning bids during the live auction will appear here immediately.", fontSize = 12.sp, color = TextSecondary)
                            }
                        }
                    }
                } else {
                    items(purchasedPlayers) { player ->
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = StadiumSurface,
                            border = androidx.compose.foundation.BorderStroke(1.dp, StadiumBorder)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    PlayerAvatar(
                                        name = player.fullName,
                                        position = player.position,
                                        jerseyNumber = player.jerseyNumber,
                                        size = 44.dp
                                    )
                                    Column {
                                        Text(player.fullName, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                                        Text("S/O ${player.fatherName} • ${player.villageCity}", fontSize = 11.sp, color = TextSecondary)
                                        PositionBadge(position = player.position)
                                    }
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Bought For", fontSize = 10.sp, color = TextSecondary)
                                    Text(
                                        formatRupees(player.soldPrice),
                                        fontWeight = FontWeight.Black,
                                        fontSize = 15.sp,
                                        color = KabaddiGold
                                    )
                                }
                            }
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(30.dp)) }
            }
        }
    }
}
