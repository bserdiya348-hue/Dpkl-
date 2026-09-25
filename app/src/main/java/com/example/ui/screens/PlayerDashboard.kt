package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.viewmodel.TournamentViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerDashboard(
    viewModel: TournamentViewModel,
    modifier: Modifier = Modifier
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val players by viewModel.players.collectAsState()
    val teams by viewModel.teams.collectAsState()

    // Find the logged-in player's profile
    val player = remember(currentUser, players) {
        val pId = currentUser?.playerId
        if (pId != null) {
            players.find { it.id == pId }
        } else {
            players.find { it.mobileNumber == currentUser?.emailOrMobile } ?: players.firstOrNull()
        }
    }

    val winningTeam = remember(player?.soldTeamId, teams) {
        teams.find { it.id == player?.soldTeamId }
    }

    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(player?.fullName ?: "Player Profile", fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text("DPKL Player Portal", fontSize = 11.sp, color = KabaddiGold)
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
        if (player == null) {
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text("No player record linked to this account.", color = TextSecondary)
            }
        } else {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(scrollState)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // If SOLD, display Celebration Trophy Banner
                if (player.auctionStatus == "SOLD") {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = StadiumSurface),
                        border = androidx.compose.foundation.BorderStroke(2.dp, ActionGreen)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(CircleShape)
                                    .background(ActionGreen.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = ActionGreen, modifier = Modifier.size(36.dp))
                            }
                            Text(
                                text = "CONGRATULATIONS! YOU ARE SOLD!",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = ActionGreen,
                                textAlign = TextAlign.Center
                            )
                            if (winningTeam != null) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    TeamBadge(
                                        teamName = winningTeam.name,
                                        shortCode = winningTeam.shortCode,
                                        colorHex = winningTeam.logoColor,
                                        size = 40.dp
                                    )
                                    Column {
                                        Text("Winning Team: ${winningTeam.name}", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextPrimary)
                                        Text("Final Price: ${formatRupees(player.soldPrice)}", fontWeight = FontWeight.Black, fontSize = 16.sp, color = KabaddiGold)
                                    }
                                }
                            }
                        }
                    }
                }

                // Player ID Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = StadiumSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, StadiumBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        PlayerAvatar(
                            name = player.fullName,
                            position = player.position,
                            jerseyNumber = player.jerseyNumber,
                            size = 90.dp
                        )

                        Text(
                            text = player.fullName,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            PositionBadge(position = player.position)
                            StatusChip(status = player.status)
                        }

                        HorizontalDivider(color = StadiumBorder)

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            DetailRow(label = "Father's Name", value = player.fatherName)
                            DetailRow(label = "Village / City", value = player.villageCity)
                            DetailRow(label = "Mobile Number", value = player.mobileNumber)
                            DetailRow(label = "Age", value = "${player.age} Years")
                            DetailRow(label = "Jersey Number", value = "#${player.jerseyNumber}")
                            DetailRow(label = "Base Price", value = formatRupees(player.basePrice))
                            DetailRow(label = "Auction Status", value = player.auctionStatus)
                        }
                    }
                }

                // Live Auction Link
                Button(
                    onClick = { viewModel.navigateTo("LIVE_AUCTION") },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = KabaddiOrange),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.LiveTv, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Watch Live Tournament Auction", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}
