package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.BidEntity
import com.example.data.db.PlayerEntity
import com.example.data.db.TeamEntity
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.viewmodel.TournamentViewModel

@Composable
fun LiveAuctionScreen(
    viewModel: TournamentViewModel,
    modifier: Modifier = Modifier
) {
    val auctionState by viewModel.auctionState.collectAsState()
    val currentPlayer by viewModel.currentAuctionPlayer.collectAsState()
    val teams by viewModel.teams.collectAsState()
    val allBids by viewModel.allBids.collectAsState()
    val currentRole by viewModel.currentRole.collectAsState()

    val highestTeam = remember(auctionState?.highestTeamId, teams) {
        teams.find { it.id == auctionState?.highestTeamId }
    }

    val currentBidsForPlayer = remember(currentPlayer?.id, allBids) {
        if (currentPlayer != null) {
            allBids.filter { it.playerId == currentPlayer?.id }.take(5)
        } else emptyList()
    }

    val scrollState = rememberScrollState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(StadiumDarkBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Live Broadcast Stage Banner Header
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = StadiumSurfaceVariant,
                border = androidx.compose.foundation.BorderStroke(1.dp, StadiumBorder)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Pulsing LIVE indicator
                        val infiniteTransition = rememberInfiniteTransition(label = "live_pulse")
                        val alpha by infiniteTransition.animateFloat(
                            initialValue = 0.3f,
                            targetValue = 1f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(600, easing = LinearEasing),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "liveAlpha"
                        )
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(ActionRed.copy(alpha = alpha))
                        )
                        Text(
                            text = "LIVE AUCTION STAGE",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            color = ActionRed,
                            letterSpacing = 1.sp
                        )
                    }

                    Text(
                        text = "DPKL SEASON 1",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = KabaddiGold
                    )
                }
            }

            if (currentPlayer == null || auctionState?.auctionPhase == "IDLE") {
                // Idle Stage waiting for auctioneer
                IdleAuctionStage(
                    currentRole = currentRole,
                    onGoToControl = { viewModel.navigateTo("AUCTION_CONTROL") }
                )
            } else {
                // Active Auction Display
                when (auctionState?.auctionPhase) {
                    "SOLD" -> {
                        SoldCelebrationCard(
                            player = currentPlayer!!,
                            finalBid = auctionState?.currentBid ?: 0L,
                            winningTeam = highestTeam
                        )
                    }
                    "UNSOLD" -> {
                        UnsoldNotificationCard(player = currentPlayer!!)
                    }
                    else -> {
                        // LIVE AUCTION IN PROGRESS
                        LivePlayerAuctionCard(
                            player = currentPlayer!!,
                            currentBid = auctionState?.currentBid ?: currentPlayer!!.basePrice,
                            basePrice = currentPlayer!!.basePrice,
                            highestTeam = highestTeam,
                            secondsRemaining = auctionState?.timerSeconds ?: 30,
                            isTimerRunning = auctionState?.isTimerRunning ?: false
                        )
                    }
                }
            }

            // Real-Time Bids Activity Stream
            if (currentBidsForPlayer.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = StadiumSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, StadiumBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.TrendingUp, contentDescription = null, tint = KabaddiOrange, modifier = Modifier.size(20.dp))
                            Text(
                                text = "RECENT BIDS FOR THIS PLAYER",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                letterSpacing = 0.5.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        currentBidsForPlayer.forEachIndexed { index, bid ->
                            BidRowItem(bid = bid, isHighest = index == 0)
                            if (index < currentBidsForPlayer.size - 1) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    color = StadiumBorder.copy(alpha = 0.5f)
                                )
                            }
                        }
                    }
                }
            }

            // Teams Remaining Purse Ticker
            TeamsPurseTicker(teams = teams)

            Spacer(modifier = Modifier.height(40.dp))
        }

        // Quick shortcut for admins to jump to auction control
        if (currentRole == "SUPER_ADMIN" || currentRole == "AUCTION_ADMIN") {
            FloatingActionButton(
                onClick = { viewModel.navigateTo("AUCTION_CONTROL") },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(20.dp)
                    .testTag("live_auction_desk_fab"),
                containerColor = KabaddiOrange,
                contentColor = Color.White
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Gavel, contentDescription = "Auction Control")
                    Text("Auction Desk", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun LivePlayerAuctionCard(
    player: PlayerEntity,
    currentBid: Long,
    basePrice: Long,
    highestTeam: TeamEntity?,
    secondsRemaining: Int,
    isTimerRunning: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(12.dp, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = StadiumSurface),
        border = androidx.compose.foundation.BorderStroke(2.dp, KabaddiOrange.copy(alpha = 0.6f))
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Row: Timer & Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatusChip(status = "IN_AUCTION")
                CountdownTimerWidget(
                    secondsRemaining = secondsRemaining,
                    isRunning = isTimerRunning
                )
            }

            // Player Avatar & Bio (Hero presentation for TV/Projector)
            Box(
                modifier = Modifier.padding(vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                PlayerAvatar(
                    name = player.fullName,
                    position = player.position,
                    jerseyNumber = player.jerseyNumber,
                    size = 110.dp
                )
            }

            Text(
                text = player.fullName,
                fontSize = 26.sp,
                fontWeight = FontWeight.Black,
                color = TextPrimary,
                textAlign = TextAlign.Center
            )

            Text(
                text = "S/O ${player.fatherName} • ${player.villageCity} • Age: ${player.age}",
                fontSize = 13.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                PositionBadge(position = player.position)
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = StadiumSurfaceVariant,
                    border = androidx.compose.foundation.BorderStroke(1.dp, StadiumBorder)
                ) {
                    Text(
                        text = "Jersey #${player.jerseyNumber}",
                        color = KabaddiGold,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            HorizontalDivider(color = StadiumBorder, thickness = 1.dp)

            // Live Bid Stage Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Base Price
                Column {
                    Text(
                        text = "BASE PRICE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = formatRupees(basePrice),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                // Current Highest Bid
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "CURRENT HIGHEST BID",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = KabaddiGold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = formatRupees(currentBid),
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Black,
                        color = KabaddiGold
                    )
                }
            }

            // Highest Bidding Team Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = if (highestTeam != null) StadiumSurfaceVariant else StadiumSurfaceVariant.copy(alpha = 0.5f),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (highestTeam != null) parseColorHex(highestTeam.logoColor) else StadiumBorder
                )
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (highestTeam != null) {
                        TeamBadge(
                            teamName = highestTeam.name,
                            shortCode = highestTeam.shortCode,
                            colorHex = highestTeam.logoColor,
                            size = 46.dp
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "HIGHEST BIDDER",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = KabaddiOrange,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = highestTeam.name,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Sponsor: ${highestTeam.sponsorName}",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(StadiumBorder),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.HourglassEmpty, contentDescription = null, tint = TextSecondary)
                        }
                        Column {
                            Text(
                                text = "Awaiting Opening Bid",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextSecondary
                            )
                            Text(
                                text = "Any team can open bid at base price",
                                fontSize = 12.sp,
                                color = TextMuted
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SoldCelebrationCard(
    player: PlayerEntity,
    finalBid: Long,
    winningTeam: TeamEntity?
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(16.dp, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = StadiumSurface),
        border = androidx.compose.foundation.BorderStroke(2.dp, ActionGreen)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Celebratory SOLD Banner
            Surface(
                shape = RoundedCornerShape(30.dp),
                color = ActionGreen,
                modifier = Modifier.padding(bottom = 4.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = Color.White)
                    Text(
                        text = "SOLD!",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        letterSpacing = 2.sp
                    )
                }
            }

            PlayerAvatar(
                name = player.fullName,
                position = player.position,
                jerseyNumber = player.jerseyNumber,
                size = 90.dp
            )

            Text(
                text = player.fullName,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Text(
                text = "S/O ${player.fatherName} • ${player.villageCity} • ${player.position}",
                fontSize = 13.sp,
                color = TextSecondary
            )

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = StadiumSurfaceVariant,
                border = androidx.compose.foundation.BorderStroke(1.dp, ActionGreen.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "PURCHASED BY",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary,
                        letterSpacing = 1.sp
                    )

                    if (winningTeam != null) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            TeamBadge(
                                teamName = winningTeam.name,
                                shortCode = winningTeam.shortCode,
                                colorHex = winningTeam.logoColor,
                                size = 48.dp
                            )
                            Column {
                                Text(
                                    text = winningTeam.name,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    color = TextPrimary
                                )
                                Text(
                                    text = winningTeam.sponsorName,
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )
                            }
                        }
                    }

                    Text(
                        text = "FINAL WINNING BID",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = KabaddiGold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = formatRupees(finalBid),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        color = KabaddiGold
                    )
                }
            }
        }
    }
}

@Composable
fun UnsoldNotificationCard(player: PlayerEntity) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(12.dp, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = StadiumSurface),
        border = androidx.compose.foundation.BorderStroke(2.dp, ActionRed)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(30.dp),
                color = ActionRed
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Cancel, contentDescription = null, tint = Color.White)
                    Text(
                        text = "UNSOLD",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        letterSpacing = 2.sp
                    )
                }
            }

            PlayerAvatar(
                name = player.fullName,
                position = player.position,
                jerseyNumber = player.jerseyNumber,
                size = 85.dp
            )

            Text(
                text = player.fullName,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Text(
                text = "${player.position} • Base: ${formatRupees(player.basePrice)} • ${player.villageCity}",
                fontSize = 13.sp,
                color = TextSecondary
            )

            Text(
                text = "No team opened a bid for this player. Player may be recalled in the secondary accelerated auction round.",
                fontSize = 13.sp,
                color = TextMuted,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun IdleAuctionStage(
    currentRole: String,
    onGoToControl: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = StadiumSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, StadiumBorder)
    ) {
        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(StadiumSurfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Gavel,
                    contentDescription = null,
                    tint = KabaddiOrange,
                    modifier = Modifier.size(44.dp)
                )
            }

            Text(
                text = "AUCTION STAGE ON STANDBY",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                letterSpacing = 1.sp
            )

            Text(
                text = "The Auctioneer will bring the next player to the block shortly. Live bids and timer will appear on this screen automatically.",
                fontSize = 14.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )

            if (currentRole == "SUPER_ADMIN" || currentRole == "AUCTION_ADMIN") {
                Button(
                    onClick = onGoToControl,
                    colors = ButtonDefaults.buttonColors(containerColor = KabaddiOrange),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Select Next Player to Auction", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun BidRowItem(bid: BidEntity, isHighest: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (isHighest) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = ActionGreen
                ) {
                    Text(
                        text = "LEAD",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
            Text(
                text = bid.teamName,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
        }

        Text(
            text = formatRupees(bid.amount),
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = if (isHighest) KabaddiGold else TextSecondary
        )
    }
}

@Composable
fun TeamsPurseTicker(teams: List<TeamEntity>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = StadiumSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, StadiumBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "TEAM PURSE BALANCES",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = KabaddiGold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(10.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(teams) { team ->
                    val remaining = team.purseBudget - team.spentAmount
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = StadiumSurfaceVariant,
                        border = androidx.compose.foundation.BorderStroke(1.dp, StadiumBorder)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(parseColorHex(team.logoColor))
                                )
                                Text(
                                    text = team.shortCode,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 13.sp,
                                    color = TextPrimary
                                )
                            }
                            Text(
                                text = formatRupees(remaining),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = KabaddiGold
                            )
                            Text(
                                text = "Spent: ${formatRupees(team.spentAmount)}",
                                fontSize = 10.sp,
                                color = TextMuted
                            )
                        }
                    }
                }
            }
        }
    }
}
