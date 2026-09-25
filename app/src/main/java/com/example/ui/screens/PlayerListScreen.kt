package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.viewmodel.TournamentViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerListScreen(
    viewModel: TournamentViewModel,
    modifier: Modifier = Modifier
) {
    val players by viewModel.players.collectAsState()
    val currentRole by viewModel.currentRole.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedPositionFilter by remember { mutableStateOf("ALL") }
    var selectedStatusFilter by remember { mutableStateOf("ALL") }
    var selectedPlayerForDetails by remember { mutableStateOf<PlayerEntity?>(null) }
    var showEditBasePriceDialog by remember { mutableStateOf<PlayerEntity?>(null) }
    var newBasePriceText by remember { mutableStateOf("") }

    val filteredPlayers = remember(players, searchQuery, selectedPositionFilter, selectedStatusFilter) {
        players.filter { player ->
            val matchQuery = searchQuery.isBlank() ||
                player.fullName.contains(searchQuery, ignoreCase = true) ||
                player.fatherName.contains(searchQuery, ignoreCase = true) ||
                player.villageCity.contains(searchQuery, ignoreCase = true) ||
                player.mobileNumber.contains(searchQuery)

            val matchPos = selectedPositionFilter == "ALL" || player.position.equals(selectedPositionFilter, ignoreCase = true)

            val matchStatus = when (selectedStatusFilter) {
                "ALL" -> true
                "PENDING" -> player.status == "PENDING"
                "APPROVED" -> player.status == "APPROVED"
                "REJECTED" -> player.status == "REJECTED"
                "SOLD" -> player.auctionStatus == "SOLD"
                "UNSOLD" -> player.auctionStatus == "UNSOLD"
                else -> true
            }

            matchQuery && matchPos && matchStatus
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Player Database (${filteredPlayers.size})", fontWeight = FontWeight.Bold, color = TextPrimary)
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
                    IconButton(onClick = { viewModel.navigateTo("REGISTER_PLAYER") }) {
                        Icon(Icons.Default.PersonAdd, contentDescription = "Register New", tint = KabaddiOrange)
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Search Input
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search by name, father's name, village or mobile...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondary) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear", tint = TextSecondary)
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().testTag("player_search_bar"),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = KabaddiOrange,
                    unfocusedBorderColor = StadiumBorder
                )
            )

            // Position Filter Chips
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(listOf("ALL", "Raider", "Defender", "All-Rounder")) { pos ->
                    val isSelected = selectedPositionFilter == pos
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedPositionFilter = pos },
                        label = { Text(if (pos == "ALL") "All Positions" else pos, fontSize = 12.sp) },
                        leadingIcon = {
                            if (isSelected) {
                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                            }
                        }
                    )
                }
            }

            // Status Filter Chips
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(listOf("ALL", "PENDING", "APPROVED", "SOLD", "UNSOLD", "REJECTED")) { status ->
                    val isSelected = selectedStatusFilter == status
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedStatusFilter = status },
                        label = { Text(status, fontSize = 11.sp) }
                    )
                }
            }

            // Player List
            if (filteredPlayers.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.PersonSearch, contentDescription = null, tint = TextMuted, modifier = Modifier.size(48.dp))
                        Text("No players found matching current filters", color = TextSecondary, fontSize = 14.sp)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredPlayers) { player ->
                        PlayerCardItem(
                            player = player,
                            currentRole = currentRole,
                            onApprove = { viewModel.approvePlayer(player.id) },
                            onReject = { viewModel.rejectPlayer(player.id) },
                            onAuctionNow = {
                                viewModel.selectPlayerForAuction(player)
                                viewModel.navigateTo("AUCTION_CONTROL")
                            },
                            onEditBasePrice = {
                                showEditBasePriceDialog = player
                                newBasePriceText = player.basePrice.toString()
                            },
                            onCardClick = { selectedPlayerForDetails = player }
                        )
                    }
                }
            }
        }
    }

    // Player Details Dialog
    if (selectedPlayerForDetails != null) {
        val player = selectedPlayerForDetails!!
        AlertDialog(
            onDismissRequest = { selectedPlayerForDetails = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    PlayerAvatar(name = player.fullName, position = player.position, jerseyNumber = player.jerseyNumber, size = 48.dp)
                    Column {
                        Text(player.fullName, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("S/O ${player.fatherName}", fontSize = 12.sp, color = TextSecondary)
                    }
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    DetailRow(label = "Playing Position", value = player.position)
                    DetailRow(label = "Jersey Number", value = "#${player.jerseyNumber}")
                    DetailRow(label = "Age", value = "${player.age} Years")
                    DetailRow(label = "Mobile", value = player.mobileNumber)
                    DetailRow(label = "Village / City", value = player.villageCity)
                    DetailRow(label = "Registration Status", value = player.status)
                    DetailRow(label = "Base Price", value = formatRupees(player.basePrice))
                    DetailRow(label = "Auction Status", value = player.auctionStatus)
                    if (player.auctionStatus == "SOLD") {
                        DetailRow(label = "Purchased By", value = player.soldTeamName)
                        DetailRow(label = "Winning Bid", value = formatRupees(player.soldPrice))
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedPlayerForDetails = null }) {
                    Text("Close", color = KabaddiOrange)
                }
            }
        )
    }

    // Edit Base Price Dialog
    if (showEditBasePriceDialog != null) {
        val p = showEditBasePriceDialog!!
        AlertDialog(
            onDismissRequest = { showEditBasePriceDialog = null },
            title = { Text("Update Base Price") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Set new base price for ${p.fullName}:")
                    OutlinedTextField(
                        value = newBasePriceText,
                        onValueChange = { newBasePriceText = it },
                        label = { Text("Base Price (₹)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amount = newBasePriceText.toLongOrNull()
                        if (amount != null && amount > 0) {
                            viewModel.updatePlayerBasePrice(p.id, amount)
                        }
                        showEditBasePriceDialog = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = KabaddiOrange)
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditBasePriceDialog = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun PlayerCardItem(
    player: PlayerEntity,
    currentRole: String,
    onApprove: () -> Unit,
    onReject: () -> Unit,
    onAuctionNow: () -> Unit,
    onEditBasePrice: () -> Unit,
    onCardClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onCardClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = StadiumSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, StadiumBorder)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                PlayerAvatar(
                    name = player.fullName,
                    position = player.position,
                    jerseyNumber = player.jerseyNumber,
                    size = 50.dp
                )

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = player.fullName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = TextPrimary
                        )
                        StatusChip(status = if (player.status == "PENDING") "PENDING" else player.auctionStatus)
                    }

                    Text(
                        text = "S/O ${player.fatherName} • ${player.villageCity}",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )

                    Row(
                        modifier = Modifier.padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        PositionBadge(position = player.position)
                        Text(
                            text = "Base: ${formatRupees(player.basePrice)}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = KabaddiGold
                        )
                    }
                }
            }

            // Admin action buttons
            if (currentRole == "SUPER_ADMIN" || currentRole == "AUCTION_ADMIN") {
                HorizontalDivider(color = StadiumBorder.copy(alpha = 0.5f))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (player.status == "PENDING") {
                        Button(
                            onClick = onApprove,
                            colors = ButtonDefaults.buttonColors(containerColor = ActionGreen),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Approve", fontSize = 12.sp)
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        OutlinedButton(
                            onClick = onReject,
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, ActionRed)
                        ) {
                            Text("Reject", fontSize = 12.sp, color = ActionRed)
                        }
                    } else if (player.status == "APPROVED" && player.auctionStatus != "SOLD") {
                        TextButton(onClick = onEditBasePrice) {
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Base Price", fontSize = 12.sp, color = TextSecondary)
                        }

                        Button(
                            onClick = onAuctionNow,
                            colors = ButtonDefaults.buttonColors(containerColor = KabaddiOrange),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.Gavel, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Auction Now", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 12.sp, color = TextSecondary)
        Text(value, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
    }
}
