package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import com.example.data.db.TeamEntity
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.viewmodel.TournamentViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamManagementScreen(
    viewModel: TournamentViewModel,
    modifier: Modifier = Modifier
) {
    val teams by viewModel.teams.collectAsState()
    val players by viewModel.players.collectAsState()
    val currentRole by viewModel.currentRole.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var teamToEdit by remember { mutableStateOf<TeamEntity?>(null) }
    var teamToDelete by remember { mutableStateOf<TeamEntity?>(null) }
    var viewSquadTeam by remember { mutableStateOf<TeamEntity?>(null) }

    // Add / Edit form fields
    var teamName by remember { mutableStateOf("") }
    var shortCode by remember { mutableStateOf("") }
    var captainName by remember { mutableStateOf("") }
    var sponsorName by remember { mutableStateOf("") }
    var purseAmountText by remember { mutableStateOf("5000000") }
    var managerEmail by remember { mutableStateOf("") }
    var selectedColorHex by remember { mutableStateOf("#EA580C") }

    val presetColors = listOf(
        Pair("#EA580C", "Orange"),
        Pair("#D97706", "Amber"),
        Pair("#E11D48", "Crimson"),
        Pair("#2563EB", "Blue"),
        Pair("#7C3AED", "Purple"),
        Pair("#059669", "Emerald")
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Team Management", fontWeight = FontWeight.Bold, color = TextPrimary)
                },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateTo("SUPER_ADMIN") }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    if (currentRole == "SUPER_ADMIN") {
                        IconButton(
                            onClick = {
                                teamName = ""
                                shortCode = ""
                                captainName = ""
                                sponsorName = ""
                                purseAmountText = "5000000"
                                managerEmail = ""
                                selectedColorHex = "#EA580C"
                                showAddDialog = true
                            }
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add Team", tint = KabaddiOrange)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = StadiumSurface)
            )
        },
        floatingActionButton = {
            if (currentRole == "SUPER_ADMIN") {
                FloatingActionButton(
                    onClick = {
                        teamName = ""
                        shortCode = ""
                        captainName = ""
                        sponsorName = ""
                        purseAmountText = "5000000"
                        managerEmail = ""
                        selectedColorHex = "#EA580C"
                        showAddDialog = true
                    },
                    containerColor = KabaddiOrange,
                    contentColor = Color.White,
                    modifier = Modifier.testTag("add_team_fab")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Team")
                }
            }
        },
        containerColor = StadiumDarkBg
    ) { innerPadding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Text(
                    text = "REGISTERED TOURNAMENT TEAMS (${teams.size})",
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
                val spendRatio = (team.spentAmount.toFloat() / team.purseBudget.coerceAtLeast(1).toFloat()).coerceIn(0f, 1f)

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = StadiumSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, parseColorHex(team.logoColor).copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            TeamBadge(
                                teamName = team.name,
                                shortCode = team.shortCode,
                                colorHex = team.logoColor,
                                size = 52.dp
                            )

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = team.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Captain: ${team.captainName.ifBlank { "Unassigned" }}",
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )
                                Text(
                                    text = "Sponsor: ${team.sponsorName.ifBlank { "N/A" }}",
                                    fontSize = 11.sp,
                                    color = TextMuted
                                )
                            }

                            if (currentRole == "SUPER_ADMIN") {
                                IconButton(
                                    onClick = {
                                        teamToEdit = team
                                        teamName = team.name
                                        shortCode = team.shortCode
                                        captainName = team.captainName
                                        sponsorName = team.sponsorName
                                        purseAmountText = team.purseBudget.toString()
                                        managerEmail = team.managerEmail
                                        selectedColorHex = team.logoColor
                                    }
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = TextSecondary)
                                }
                                IconButton(onClick = { teamToDelete = team }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = ActionRed)
                                }
                            }
                        }

                        // Budget & Purse Progress Bar
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Purse Spent: ${formatRupees(team.spentAmount)}", fontSize = 11.sp, color = TextSecondary)
                                Text("Remaining: ${formatRupees(remaining)}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = KabaddiGold)
                            }
                            LinearProgressIndicator(
                                progress = { spendRatio },
                                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                                color = parseColorHex(team.logoColor),
                                trackColor = StadiumBorder
                            )
                        }

                        // Squad count & roster button
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Squad: ${teamPlayers.size} Players Bought",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (teamPlayers.isNotEmpty()) ActionGreen else TextSecondary
                            )

                            OutlinedButton(
                                onClick = { viewSquadTeam = team },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                            ) {
                                Icon(Icons.Default.List, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("View Squad", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(60.dp)) }
        }
    }

    // Add Team Dialog
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Add New Team", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = teamName,
                        onValueChange = { teamName = it },
                        label = { Text("Team Name *") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = shortCode,
                        onValueChange = { shortCode = it.take(4).uppercase() },
                        label = { Text("Short Code (e.g. HAR, PUN) *") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = captainName,
                        onValueChange = { captainName = it },
                        label = { Text("Team Captain Name") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = sponsorName,
                        onValueChange = { sponsorName = it },
                        label = { Text("Sponsor Name") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = purseAmountText,
                        onValueChange = { purseAmountText = it },
                        label = { Text("Purse Budget (₹)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = managerEmail,
                        onValueChange = { managerEmail = it },
                        label = { Text("Manager Account Email/Mobile") },
                        singleLine = true
                    )

                    // Color picker
                    Text("Team Color:", fontSize = 12.sp, color = TextSecondary)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        presetColors.forEach { (hex, _) ->
                            val isSelected = selectedColorHex == hex
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(parseColorHex(hex))
                                    .border(
                                        width = if (isSelected) 3.dp else 1.dp,
                                        color = if (isSelected) Color.White else Color.Transparent,
                                        shape = CircleShape
                                    )
                                    .clickable { selectedColorHex = hex }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (teamName.isNotBlank() && shortCode.isNotBlank()) {
                            val purse = purseAmountText.toLongOrNull() ?: 5000000L
                            viewModel.addTeam(
                                name = teamName,
                                shortCode = shortCode,
                                logoColor = selectedColorHex,
                                captainName = captainName,
                                sponsorName = sponsorName,
                                purseBudget = purse,
                                managerEmail = managerEmail
                            )
                            showAddDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = KabaddiOrange)
                ) {
                    Text("Save Team")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Edit Team Dialog
    if (teamToEdit != null) {
        AlertDialog(
            onDismissRequest = { teamToEdit = null },
            title = { Text("Edit Team", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = teamName,
                        onValueChange = { teamName = it },
                        label = { Text("Team Name") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = shortCode,
                        onValueChange = { shortCode = it.take(4).uppercase() },
                        label = { Text("Short Code") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = captainName,
                        onValueChange = { captainName = it },
                        label = { Text("Captain Name") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = sponsorName,
                        onValueChange = { sponsorName = it },
                        label = { Text("Sponsor Name") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = purseAmountText,
                        onValueChange = { purseAmountText = it },
                        label = { Text("Purse Budget (₹)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        teamToEdit?.let { current ->
                            viewModel.updateTeam(
                                current.copy(
                                    name = teamName.ifBlank { current.name },
                                    shortCode = shortCode.ifBlank { current.shortCode },
                                    captainName = captainName,
                                    sponsorName = sponsorName,
                                    purseBudget = purseAmountText.toLongOrNull() ?: current.purseBudget,
                                    logoColor = selectedColorHex
                                )
                            )
                        }
                        teamToEdit = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = KabaddiOrange)
                ) {
                    Text("Update Team")
                }
            },
            dismissButton = {
                TextButton(onClick = { teamToEdit = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Delete Team Dialog
    if (teamToDelete != null) {
        AlertDialog(
            onDismissRequest = { teamToDelete = null },
            title = { Text("Delete Team?") },
            text = { Text("Are you sure you want to delete ${teamToDelete?.name}? This cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        teamToDelete?.let { viewModel.deleteTeam(it.id) }
                        teamToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ActionRed)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { teamToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Squad Roster Sheet
    if (viewSquadTeam != null) {
        val squad = players.filter { it.soldTeamId == viewSquadTeam?.id }
        AlertDialog(
            onDismissRequest = { viewSquadTeam = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TeamBadge(
                        teamName = viewSquadTeam!!.name,
                        shortCode = viewSquadTeam!!.shortCode,
                        colorHex = viewSquadTeam!!.logoColor,
                        size = 36.dp
                    )
                    Text("${viewSquadTeam!!.name} Squad (${squad.size})", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (squad.isEmpty()) {
                        Text("No players purchased yet in auction.")
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(squad) { p ->
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = StadiumSurfaceVariant,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            PlayerAvatar(name = p.fullName, position = p.position, jerseyNumber = p.jerseyNumber, size = 36.dp)
                                            Column {
                                                Text(p.fullName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                                Text(p.position, fontSize = 11.sp, color = TextSecondary)
                                            }
                                        }
                                        Text(formatRupees(p.soldPrice), fontWeight = FontWeight.Bold, color = KabaddiGold, fontSize = 13.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { viewSquadTeam = null }) {
                    Text("Close", color = KabaddiOrange)
                }
            }
        )
    }
}
