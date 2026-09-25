package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.example.data.db.BidEntity
import com.example.data.db.PlayerEntity
import com.example.data.db.TeamEntity
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.viewmodel.TournamentViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuctionHistoryScreen(
    viewModel: TournamentViewModel,
    modifier: Modifier = Modifier
) {
    val players by viewModel.players.collectAsState()
    val teams by viewModel.teams.collectAsState()
    val allBids by viewModel.allBids.collectAsState()
    val currentRole by viewModel.currentRole.collectAsState()

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("SOLD Players", "Team Spending", "UNSOLD", "Bid Logs")

    val soldPlayers = remember(players) {
        players.filter { it.auctionStatus == "SOLD" }.sortedByDescending { it.soldPrice }
    }

    val unsoldPlayers = remember(players) {
        players.filter { it.auctionStatus == "UNSOLD" }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Auction Results & History", fontWeight = FontWeight.Bold, color = TextPrimary)
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (currentRole == "SUPER_ADMIN") viewModel.navigateTo("SUPER_ADMIN")
                        else viewModel.navigateTo("LIVE_AUCTION")
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.navigateTo("LIVE_AUCTION") }) {
                        Icon(Icons.Default.Tv, contentDescription = "Live Display", tint = KabaddiGold)
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
        ) {
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = StadiumSurface,
                contentColor = KabaddiOrange,
                divider = { HorizontalDivider(color = StadiumBorder) }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = {
                            Text(
                                text = title,
                                fontSize = 12.sp,
                                fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTabIndex == index) KabaddiOrange else TextSecondary
                            )
                        }
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                when (selectedTabIndex) {
                    0 -> SoldPlayersList(soldPlayers = soldPlayers)
                    1 -> TeamSpendingList(teams = teams, players = players)
                    2 -> UnsoldPlayersList(unsoldPlayers = unsoldPlayers)
                    3 -> BidLogsList(bids = allBids)
                }
            }
        }
    }
}

@Composable
fun SoldPlayersList(soldPlayers: List<PlayerEntity>) {
    if (soldPlayers.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.HourglassEmpty, contentDescription = null, tint = TextMuted, modifier = Modifier.size(48.dp))
                Text("No players sold yet in this auction.", color = TextSecondary)
            }
        }
    } else {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            item {
                Text(
                    text = "TOP AUCTION BUYS (${soldPlayers.size} SOLD)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = KabaddiGold,
                    letterSpacing = 1.sp
                )
            }
            items(soldPlayers) { player ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = StadiumSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ActionGreen.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
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
                                size = 48.dp
                            )
                            Column {
                                Text(player.fullName, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextPrimary)
                                Text("Team: ${player.soldTeamName}", fontSize = 12.sp, color = TextSecondary)
                                PositionBadge(position = player.position)
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text("Winning Bid", fontSize = 10.sp, color = TextSecondary)
                            Text(formatRupees(player.soldPrice), fontWeight = FontWeight.Black, fontSize = 16.sp, color = KabaddiGold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TeamSpendingList(teams: List<TeamEntity>, players: List<PlayerEntity>) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Text(
                text = "TEAM-WISE AUCTION SPENDING & SQUADS",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = KabaddiGold,
                letterSpacing = 1.sp
            )
        }

        items(teams) { team ->
            val teamPlayers = remember(players, team.id) {
                players.filter { it.soldTeamId == team.id }
            }
            val remaining = team.purseBudget - team.spentAmount

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = StadiumSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, parseColorHex(team.logoColor).copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        TeamBadge(
                            teamName = team.name,
                            shortCode = team.shortCode,
                            colorHex = team.logoColor,
                            size = 46.dp
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(team.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
                            Text("Sponsor: ${team.sponsorName}", fontSize = 11.sp, color = TextSecondary)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Spent", fontSize = 10.sp, color = TextSecondary)
                            Text(formatRupees(team.spentAmount), fontWeight = FontWeight.Black, fontSize = 15.sp, color = KabaddiGold)
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Players: ${teamPlayers.size}", fontSize = 12.sp, color = ActionGreen, fontWeight = FontWeight.SemiBold)
                        Text("Purse Left: ${formatRupees(remaining)}", fontSize = 12.sp, color = TextSecondary)
                    }

                    // Purchased squad names
                    if (teamPlayers.isNotEmpty()) {
                        HorizontalDivider(color = StadiumBorder.copy(alpha = 0.5f))
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            teamPlayers.forEach { p ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("• ${p.fullName} (${p.position})", fontSize = 12.sp, color = TextPrimary)
                                    Text(formatRupees(p.soldPrice), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = KabaddiGold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun UnsoldPlayersList(unsoldPlayers: List<PlayerEntity>) {
    if (unsoldPlayers.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No players marked unsold currently.", color = TextSecondary)
        }
    } else {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            item {
                Text(
                    text = "UNSOLD PLAYERS POOL (${unsoldPlayers.size})",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = ActionRed,
                    letterSpacing = 1.sp
                )
            }
            items(unsoldPlayers) { player ->
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = StadiumSurface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, ActionRed.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            PlayerAvatar(name = player.fullName, position = player.position, jerseyNumber = player.jerseyNumber, size = 42.dp)
                            Column {
                                Text(player.fullName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("S/O ${player.fatherName} • ${player.villageCity}", fontSize = 11.sp, color = TextSecondary)
                            }
                        }
                        Text("Base: ${formatRupees(player.basePrice)}", fontSize = 12.sp, color = TextSecondary)
                    }
                }
            }
        }
    }
}

@Composable
fun BidLogsList(bids: List<BidEntity>) {
    val dateFormat = remember { SimpleDateFormat("hh:mm:ss a", Locale.getDefault()) }
    if (bids.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No bids recorded yet.", color = TextSecondary)
        }
    } else {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            item {
                Text(
                    text = "COMPLETE BID ACTIVITY STREAM (${bids.size} BIDS)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = KabaddiGold,
                    letterSpacing = 1.sp
                )
            }
            items(bids) { bid ->
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = StadiumSurface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, StadiumBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(bid.playerName, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)
                            Text("Team: ${bid.teamName}", fontSize = 11.sp, color = KabaddiOrange)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(formatRupees(bid.amount), fontWeight = FontWeight.Black, fontSize = 15.sp, color = KabaddiGold)
                            Text(dateFormat.format(Date(bid.timestamp)), fontSize = 10.sp, color = TextMuted)
                        }
                    }
                }
            }
        }
    }
}
