package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import com.example.ui.components.PlayerAvatar
import com.example.ui.components.PositionBadge
import com.example.ui.theme.*
import com.example.viewmodel.TournamentViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerRegistrationScreen(
    viewModel: TournamentViewModel,
    modifier: Modifier = Modifier
) {
    var fullName by remember { mutableStateOf("") }
    var fatherName by remember { mutableStateOf("") }
    var mobileNumber by remember { mutableStateOf("") }
    var ageText by remember { mutableStateOf("") }
    var villageCity by remember { mutableStateOf("") }
    var position by remember { mutableStateOf("Raider") }
    var jerseyNumberText by remember { mutableStateOf("") }
    var selectedAvatarPreset by remember { mutableStateOf("raider_1") }
    var validationError by remember { mutableStateOf<String?>(null) }
    var registrationSuccess by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    val avatarOptions = listOf(
        Pair("raider_1", "Athletic Raider"),
        Pair("defender_1", "Iron Defender"),
        Pair("allrounder_1", "All-Round Star"),
        Pair("raider_2", "Pacer Pro")
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Player Registration Form",
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateTo("LOGIN") }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
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
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (registrationSuccess) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = StadiumSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ActionGreen)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(70.dp)
                                .clip(CircleShape)
                                .background(ActionGreen.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = ActionGreen,
                                modifier = Modifier.size(48.dp)
                            )
                        }

                        Text(
                            text = "Registration Submitted!",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )

                        Text(
                            text = "Thank you, $fullName. Your profile has been sent to the Tournament Organizer for verification and approval. Once approved, you will be eligible for the live auction.",
                            fontSize = 14.sp,
                            color = TextSecondary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )

                        Button(
                            onClick = {
                                registrationSuccess = false
                                viewModel.navigateTo("LOGIN")
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = KabaddiOrange),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Go to Login / Dashboard")
                        }
                    }
                }
            } else {
                // Live Player Card Preview
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = StadiumSurfaceVariant),
                    border = androidx.compose.foundation.BorderStroke(1.dp, StadiumBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "AUCTION CARD PREVIEW",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = KabaddiGold,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            PlayerAvatar(
                                name = fullName.ifBlank { "Player Name" },
                                position = position,
                                jerseyNumber = jerseyNumberText.toIntOrNull() ?: 7,
                                size = 64.dp
                            )
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = fullName.ifBlank { "Your Name" },
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = TextPrimary
                                )
                                Text(
                                    text = if (fatherName.isNotBlank()) "S/O $fatherName" else "Father's Name",
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    PositionBadge(position = position)
                                    Text(
                                        text = villageCity.ifBlank { "City / Village" },
                                        fontSize = 12.sp,
                                        color = TextSecondary,
                                        modifier = Modifier.align(Alignment.CenterVertically)
                                    )
                                }
                            }
                        }
                    }
                }

                // Registration Form Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = StadiumSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, StadiumBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = "Enter Player Details",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = TextPrimary
                        )

                        if (validationError != null) {
                            Text(
                                text = validationError ?: "",
                                color = ActionRed,
                                fontSize = 13.sp
                            )
                        }

                        // Full Name
                        OutlinedTextField(
                            value = fullName,
                            onValueChange = { fullName = it },
                            label = { Text("Player Full Name *") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                            modifier = Modifier.fillMaxWidth().testTag("reg_full_name"),
                            singleLine = true
                        )

                        // Father's Name
                        OutlinedTextField(
                            value = fatherName,
                            onValueChange = { fatherName = it },
                            label = { Text("Father's Name *") },
                            leadingIcon = { Icon(Icons.Default.FamilyRestroom, contentDescription = null) },
                            modifier = Modifier.fillMaxWidth().testTag("reg_father_name"),
                            singleLine = true
                        )

                        // Mobile Number
                        OutlinedTextField(
                            value = mobileNumber,
                            onValueChange = { mobileNumber = it },
                            label = { Text("Mobile Number (Will be used to login) *") },
                            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            modifier = Modifier.fillMaxWidth().testTag("reg_mobile"),
                            singleLine = true
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Age
                            OutlinedTextField(
                                value = ageText,
                                onValueChange = { ageText = it },
                                label = { Text("Age (Years) *") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f).testTag("reg_age"),
                                singleLine = true
                            )

                            // Jersey Number
                            OutlinedTextField(
                                value = jerseyNumberText,
                                onValueChange = { jerseyNumberText = it },
                                label = { Text("Jersey # *") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f).testTag("reg_jersey"),
                                singleLine = true
                            )
                        }

                        // Village / City
                        OutlinedTextField(
                            value = villageCity,
                            onValueChange = { villageCity = it },
                            label = { Text("Village / City / District *") },
                            leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                            modifier = Modifier.fillMaxWidth().testTag("reg_village"),
                            singleLine = true
                        )

                        // Playing Position selector
                        Text(
                            text = "Playing Position *",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("Raider", "Defender", "All-Rounder").forEach { pos ->
                                val selected = position == pos
                                FilterChip(
                                    selected = selected,
                                    onClick = { position = pos },
                                    label = { Text(pos, fontSize = 12.sp) },
                                    leadingIcon = {
                                        if (selected) {
                                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                        }
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        // Photo Preset Avatar Selection
                        Text(
                            text = "Choose Athletic Avatar *",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            avatarOptions.forEach { (key, label) ->
                                val isSelected = selectedAvatarPreset == key
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) KabaddiOrange.copy(alpha = 0.2f) else StadiumSurfaceVariant)
                                        .border(
                                            width = if (isSelected) 2.dp else 1.dp,
                                            color = if (isSelected) KabaddiOrange else StadiumBorder,
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .clickable { selectedAvatarPreset = key }
                                        .padding(8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        PlayerAvatar(
                                            name = fullName.ifBlank { "P" },
                                            position = position,
                                            jerseyNumber = jerseyNumberText.toIntOrNull() ?: 1,
                                            size = 36.dp,
                                            showBadge = false
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = label.take(8),
                                            fontSize = 9.sp,
                                            color = if (isSelected) KabaddiOrange else TextSecondary
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = {
                                if (fullName.isBlank() || fatherName.isBlank() || mobileNumber.isBlank() ||
                                    ageText.isBlank() || villageCity.isBlank() || jerseyNumberText.isBlank()
                                ) {
                                    validationError = "Please fill in all required fields"
                                    return@Button
                                }
                                val age = ageText.toIntOrNull()
                                val jersey = jerseyNumberText.toIntOrNull()
                                if (age == null || age < 12 || age > 60) {
                                    validationError = "Please enter a valid age between 12 and 60"
                                    return@Button
                                }
                                if (jersey == null || jersey < 0 || jersey > 99) {
                                    validationError = "Jersey number must be between 0 and 99"
                                    return@Button
                                }

                                validationError = null
                                viewModel.registerPlayer(
                                    fullName = fullName,
                                    fatherName = fatherName,
                                    mobileNumber = mobileNumber,
                                    age = age,
                                    villageCity = villageCity,
                                    position = position,
                                    jerseyNumber = jersey,
                                    photoPreset = selectedAvatarPreset
                                )
                                registrationSuccess = true
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("reg_submit_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = KabaddiOrange),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Send, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "SUBMIT REGISTRATION",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
