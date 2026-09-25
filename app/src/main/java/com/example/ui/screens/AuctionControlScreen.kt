package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.PlayerEntity
import com.example.data.db.TeamEntity
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.viewmodel.TournamentViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuctionControlScreen(
    viewModel: TournamentViewModel,
    modifier: Modifier = Modifier
) {
    val auctionState by viewModel.auctionState.collectAsState()
    val currentPlayer by viewModel.currentAuctionPlayer.collectAsState()
    val teams by viewModel.teams.collectAsState()
    val approvedPlayers by viewModel.approvedPlayers.collectAsState()

    var selectedBiddingTeam by remember { mutableStateOf<TeamEntity?>(null) }
    var customBidText by remember { mutableStateOf("") }
    var showPlayerPickerSheet by remember { mutableStateOf(false) }
    var showConfirmSoldDialog by remember { mutableStateOf(false) }
    var showConfirmUnsoldDialog by remember { mutableStateOf(false) }
    var basePriceInput by remember { mutableStateOf("") }

    // Synchronize default selected team
    LaunchedEffect(teams) {
        if (selectedBiddingTeam == null && teams.isNotEmpty()) {
            selectedBiddingTeam = teams.first()
        }
    }

    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Gavel, contentDescription = null, tint = KabaddiOrange)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Auctioneer Control Desk", fontWeight = FontWeight.Bold, color = TextPrimary)
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.navigateTo("LIVE_AUCTION") }) {
                        Icon(Icons.Default.Tv, contentDescription = "View Live Screen", tint = KabaddiGold)
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
            // Player in Auction Banner / Selection
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = StadiumSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, KabaddiOrange)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "PLAYER ON AUCTION BLOCK",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = KabaddiGold,
                            letterSpacing = 1.sp
                        )

                        Button(
                            onClick = { showPlayerPickerSheet = true },
                            colors = ButtonDefaults.buttonColors(containerColor = StadiumSurfaceVariant),
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, StadiumBorder)
                        ) {
                            Icon(Icons.Default.Group, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Change / Select Player", fontSize = 12.sp)
                        }
                    }

                    if (currentPlayer != null) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            PlayerAvatar(
                                name = currentPlayer!!.fullName,
                                position = currentPlayer!!.position,
                                jerseyNumber = currentPlayer!!.jerseyNumber,
                                size = 64.dp
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = currentPlayer!!.fullName,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "S/O ${currentPlayer!!.fatherName} • ${currentPlayer!!.villageCity}",
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    PositionBadge(position = currentPlayer!!.position)
                                    StatusChip(status = auctionState?.auctionPhase ?: "IN_AUCTION")
                                }
                            }
                        }

                        // Current Bid & Base Price Summary Bar
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = StadiumSurfaceVariant,
                            border = androidx.compose.foundation.BorderStroke(1.dp, StadiumBorder)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Base Price", fontSize = 11.sp, color = TextSecondary)
                                    Text(
                                        formatRupees(currentPlayer!!.basePrice),
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                }

                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Current Bid", fontSize = 11.sp, color = KabaddiGold)
                                    Text(
                                        formatRupees(auctionState?.currentBid ?: currentPlayer!!.basePrice),
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Black,
                                        color = KabaddiGold
                                    )
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Leader", fontSize = 11.sp, color = TextSecondary)
                                    Text(
                                        auctionState?.highestTeamName?.ifBlank { "None" } ?: "None",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                }
                            }
                        }
                    } else {
                        // Empty state: No player currently selected
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                "No player on auction block",
                                color = TextSecondary,
                                fontSize = 14.sp
                            )
                            Button(
                                onClick = { showPlayerPickerSheet = true },
                                colors = ButtonDefaults.buttonColors(containerColor = KabaddiOrange),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.PersonAdd, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Select Player to Start Auction")
                            }
                        }
                    }
                }
            }

            // Countdown Timer Controls
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = StadiumSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, StadiumBorder)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "TIMER CONTROL",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = KabaddiGold,
                            letterSpacing = 1.sp
                        )

                        Text(
                            text = "${auctionState?.timerSeconds ?: 30}s remaining",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            color = if ((auctionState?.timerSeconds ?: 30) <= 10) ActionRed else TextPrimary
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val isRunning = auctionState?.isTimerRunning == true
                        Button(
                            onClick = {
                                if (isRunning) viewModel.pauseTimer() else viewModel.startTimer()
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isRunning) ActionRed else ActionGreen
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (isRunning) "Pause" else "Start Timer", fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = { viewModel.setTimerSeconds(20) },
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("20s")
                        }

                        OutlinedButton(
                            onClick = { viewModel.setTimerSeconds(30) },
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("30s")
                        }
                    }
                }
            }

            // Bidding Desk Section (Select Team + Trigger Bid)
            if (currentPlayer != null && auctionState?.auctionPhase == "LIVE") {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = StadiumSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, StadiumBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = "SELECT BIDDING TEAM",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = KabaddiGold,
                            letterSpacing = 1.sp
                        )

                        // Teams picker chips
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            teams.chunked(2).forEach { rowTeams ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    rowTeams.forEach { team ->
                                        val isSelected = selectedBiddingTeam?.id == team.id
                                        val purseLeft = team.purseBudget - team.spentAmount
                                        Surface(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(12.dp))
                                                .clickable { selectedBiddingTeam = team },
                                            shape = RoundedCornerShape(12.dp),
                                            color = if (isSelected) parseColorHex(team.logoColor).copy(alpha = 0.25f) else StadiumSurfaceVariant,
                                            border = androidx.compose.foundation.BorderStroke(
                                                width = if (isSelected) 2.dp else 1.dp,
                                                color = if (isSelected) parseColorHex(team.logoColor) else StadiumBorder
                                            )
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(8.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                TeamBadge(
                                                    teamName = team.name,
                                                    shortCode = team.shortCode,
                                                    colorHex = team.logoColor,
                                                    size = 32.dp
                                                )
                                                Column {
                                                    Text(
                                                        text = team.name,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 12.sp,
                                                        color = TextPrimary,
                                                        maxLines = 1
                                                    )
                                                    Text(
                                                        text = "Purse: ${formatRupees(purseLeft)}",
                                                        fontSize = 10.sp,
                                                        color = KabaddiGold
                                                    )
                                                }
                                            }
                                        }
                                    }
                                    if (rowTeams.size == 1) {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }

                        HorizontalDivider(color = StadiumBorder)

                        // Quick Increment Bid Buttons
                        Text(
                            text = "QUICK BID FOR ${selectedBiddingTeam?.name?.uppercase() ?: "TEAM"}:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(5000L, 10000L, 25000L).forEach { inc ->
                                Button(
                                    onClick = {
                                        selectedBiddingTeam?.let { team ->
                                            viewModel.placeBid(team, inc)
                                        }
                                    },
                                    modifier = Modifier.weight(1f).testTag("bid_btn_$inc"),
                                    colors = ButtonDefaults.buttonColors(containerColor = StadiumSurfaceVariant),
                                    shape = RoundedCornerShape(10.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, KabaddiOrange)
                                ) {
                                    Text("+${formatRupees(inc)}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = KabaddiOrange)
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(50000L, 100000L).forEach { inc ->
                                Button(
                                    onClick = {
                                        selectedBiddingTeam?.let { team ->
                                            viewModel.placeBid(team, inc)
                                        }
                                    },
                                    modifier = Modifier.weight(1f).testTag("bid_btn_$inc"),
                                    colors = ButtonDefaults.buttonColors(containerColor = StadiumSurfaceVariant),
                                    shape = RoundedCornerShape(10.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, KabaddiGold)
                                ) {
                                    Text("+${formatRupees(inc)}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = KabaddiGold)
                                }
                            }
                        }

                        // Custom Bid Input
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = customBidText,
                                onValueChange = { customBidText = it },
                                label = { Text("Custom Bid Amount (₹)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f).testTag("custom_bid_input"),
                                singleLine = true
                            )
                            Button(
                                onClick = {
                                    val amount = customBidText.toLongOrNull()
                                    if (amount != null && selectedBiddingTeam != null) {
                                        viewModel.setManualBid(selectedBiddingTeam!!, amount)
                                        customBidText = ""
                                    }
                                },
                                modifier = Modifier.height(52.dp).testTag("submit_custom_bid"),
                                colors = ButtonDefaults.buttonColors(containerColor = KabaddiOrange),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Bid")
                            }
                        }

                        HorizontalDivider(color = StadiumBorder)

                        // Hammer Down Actions: SOLD or UNSOLD
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = { showConfirmSoldDialog = true },
                                modifier = Modifier.weight(1f).height(50.dp).testTag("mark_sold_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = ActionGreen),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("HAMMER: SOLD", fontWeight = FontWeight.Black, fontSize = 15.sp)
                            }

                            Button(
                                onClick = { showConfirmUnsoldDialog = true },
                                modifier = Modifier.weight(1f).height(50.dp).testTag("mark_unsold_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = ActionRed),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Cancel, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("MARK UNSOLD", fontWeight = FontWeight.Black, fontSize = 15.sp)
                            }
                        }
                    }
                }
            } else if (currentPlayer != null && (auctionState?.auctionPhase == "SOLD" || auctionState?.auctionPhase == "UNSOLD")) {
                // Post-hammer status card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = StadiumSurface),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (auctionState?.auctionPhase == "SOLD") ActionGreen else ActionRed
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = if (auctionState?.auctionPhase == "SOLD") "AUCTION COMPLETED: SOLD!" else "AUCTION COMPLETED: UNSOLD",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = if (auctionState?.auctionPhase == "SOLD") ActionGreen else ActionRed
                        )
                        Button(
                            onClick = { showPlayerPickerSheet = true },
                            colors = ButtonDefaults.buttonColors(containerColor = KabaddiOrange),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.SkipNext, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Bring Next Player to Hammer", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // Modal Sheet to pick an approved player for auction
    if (showPlayerPickerSheet) {
        val eligiblePlayers = approvedPlayers.filter { it.auctionStatus != "SOLD" }
        AlertDialog(
            onDismissRequest = { showPlayerPickerSheet = false },
            title = {
                Text(
                    "Select Player for Auction (${eligiblePlayers.size} Eligible)",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (eligiblePlayers.isEmpty()) {
                        Text("No eligible players pending auction. Approve more players in Player List.")
                    } else {
                        LazyColumn(
                            modifier = Modifier.heightIn(max = 400.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(eligiblePlayers) { player ->
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable {
                                            viewModel.selectPlayerForAuction(player)
                                            showPlayerPickerSheet = false
                                        },
                                    color = StadiumSurfaceVariant,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, StadiumBorder)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        PlayerAvatar(
                                            name = player.fullName,
                                            position = player.position,
                                            jerseyNumber = player.jerseyNumber,
                                            size = 40.dp
                                        )
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(player.fullName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                            Text("${player.position} • Base: ${formatRupees(player.basePrice)}", fontSize = 12.sp, color = TextSecondary)
                                        }
                                        StatusChip(status = player.auctionStatus)
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showPlayerPickerSheet = false }) {
                    Text("Close", color = KabaddiOrange)
                }
            }
        )
    }

    // Confirmation for SOLD
    if (showConfirmSoldDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmSoldDialog = false },
            title = { Text("Confirm Hammer Down (SOLD)") },
            text = {
                Text(
                    "Are you sure you want to sell ${currentPlayer?.fullName} to ${auctionState?.highestTeamName} for ${formatRupees(auctionState?.currentBid ?: 0L)}?"
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showConfirmSoldDialog = false
                        viewModel.markSold()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ActionGreen)
                ) {
                    Text("Confirm SOLD")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmSoldDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Confirmation for UNSOLD
    if (showConfirmUnsoldDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmUnsoldDialog = false },
            title = { Text("Confirm UNSOLD") },
            text = {
                Text("Mark ${currentPlayer?.fullName} as UNSOLD? The player can be brought back in secondary rounds.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showConfirmUnsoldDialog = false
                        viewModel.markUnsold()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ActionRed)
                ) {
                    Text("Mark UNSOLD")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmUnsoldDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
