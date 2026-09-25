package com.example.ui.screens

import androidx.compose.foundation.Image
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.*
import com.example.viewmodel.TournamentViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    viewModel: TournamentViewModel,
    modifier: Modifier = Modifier
) {
    var identifier by remember { mutableStateOf("admin@dpkl.com") }
    var password by remember { mutableStateOf("admin") }
    var rememberMe by remember { mutableStateOf(true) }
    var passwordVisible by remember { mutableStateOf(false) }
    var showForgotPasswordDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(StadiumDarkBg)
            .verticalScroll(scrollState)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        // Tournament Brand Emblem
        Box(
            modifier = Modifier
                .size(90.dp)
                .shadow(12.dp, CircleShape)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(KabaddiOrange, StadiumSurfaceVariant)
                    )
                )
                .border(2.dp, KabaddiGold, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.dpkl_logo),
                contentDescription = "DPKL Logo",
                modifier = Modifier.size(70.dp).clip(CircleShape)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "DPKL KABADDI AUCTION",
            fontSize = 24.sp,
            fontWeight = FontWeight.Black,
            color = KabaddiGold,
            letterSpacing = 1.sp,
            textAlign = TextAlign.Center
        )
        Text(
            text = "Official Player Registration & Live Auction Portal",
            fontSize = 13.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(28.dp))

        // Main Login Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 500.dp)
                .shadow(8.dp, RoundedCornerShape(20.dp)),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = StadiumSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, StadiumBorder)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Sign In to Your Account",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                if (errorMessage != null) {
                    Surface(
                        color = ActionRed.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, ActionRed)
                    ) {
                        Text(
                            text = errorMessage ?: "",
                            color = ActionRed,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }

                OutlinedTextField(
                    value = identifier,
                    onValueChange = {
                        identifier = it
                        errorMessage = null
                    },
                    label = { Text("Email / Mobile Number") },
                    leadingIcon = {
                        Icon(Icons.Default.AccountCircle, contentDescription = null, tint = KabaddiOrange)
                    },
                    modifier = Modifier.fillMaxWidth().testTag("login_identifier_input"),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = KabaddiOrange,
                        unfocusedBorderColor = StadiumBorder,
                        focusedLabelColor = KabaddiOrange
                    )
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        errorMessage = null
                    },
                    label = { Text("Password") },
                    leadingIcon = {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = KabaddiOrange)
                    },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = "Toggle password",
                                tint = TextSecondary
                            )
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth().testTag("login_password_input"),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = KabaddiOrange,
                        unfocusedBorderColor = StadiumBorder,
                        focusedLabelColor = KabaddiOrange
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { rememberMe = !rememberMe }
                    ) {
                        Checkbox(
                            checked = rememberMe,
                            onCheckedChange = { rememberMe = it },
                            colors = CheckboxDefaults.colors(checkedColor = KabaddiOrange)
                        )
                        Text(
                            text = "Remember Me",
                            fontSize = 13.sp,
                            color = TextSecondary
                        )
                    }

                    TextButton(onClick = { showForgotPasswordDialog = true }) {
                        Text(
                            text = "Forgot Password?",
                            color = KabaddiOrangeLight,
                            fontSize = 13.sp
                        )
                    }
                }

                Button(
                    onClick = {
                        if (identifier.isBlank() || password.isBlank()) {
                            errorMessage = "Please enter both credentials"
                            return@Button
                        }
                        viewModel.login(identifier, password) { success, msg ->
                            if (!success) {
                                errorMessage = msg
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("login_submit_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = KabaddiOrange),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Login, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "LOGIN",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }

                // Public Guest Viewer Access
                OutlinedButton(
                    onClick = { viewModel.enterAsGuest() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("login_guest_button"),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, KabaddiGold)
                ) {
                    Icon(Icons.Default.Visibility, contentDescription = null, tint = KabaddiGold)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Continue as Public Viewer (No Login)",
                        color = KabaddiGold,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Quick Role Login Selector for Easy Tournament Testing
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 500.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = StadiumSurfaceVariant),
            border = androidx.compose.foundation.BorderStroke(1.dp, StadiumBorder)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.FlashOn, contentDescription = null, tint = KabaddiGold, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "1-Tap Demo Role Login:",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = KabaddiGold
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SuggestionChip(
                        onClick = {
                            identifier = "admin@dpkl.com"
                            password = "admin"
                            viewModel.quickLoginAs("SUPER_ADMIN")
                        },
                        label = { Text("Super Admin", fontSize = 11.sp) },
                        modifier = Modifier.weight(1f)
                    )
                    SuggestionChip(
                        onClick = {
                            identifier = "auction@dpkl.com"
                            password = "admin"
                            viewModel.quickLoginAs("AUCTION_ADMIN")
                        },
                        label = { Text("Auctioneer", fontSize = 11.sp) },
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SuggestionChip(
                        onClick = {
                            identifier = "manager@hawks.com"
                            password = "admin"
                            viewModel.quickLoginAs("TEAM_MANAGER")
                        },
                        label = { Text("Team Manager", fontSize = 11.sp) },
                        modifier = Modifier.weight(1f)
                    )
                    SuggestionChip(
                        onClick = {
                            identifier = "player@dpkl.com"
                            password = "admin"
                            viewModel.quickLoginAs("PLAYER")
                        },
                        label = { Text("Player Login", fontSize = 11.sp) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // New Player Registration CTA
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text("Are you a player? ", color = TextSecondary, fontSize = 14.sp)
            Text(
                text = "Register for Auction",
                color = KabaddiOrange,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                modifier = Modifier
                    .clickable { viewModel.navigateTo("REGISTER_PLAYER") }
                    .padding(4.dp)
            )
        }

        Spacer(modifier = Modifier.height(30.dp))
    }

    if (showForgotPasswordDialog) {
        AlertDialog(
            onDismissRequest = { showForgotPasswordDialog = false },
            title = { Text("Reset Password") },
            text = {
                Text(
                    "For local tournament security, password resets are handled directly by the Super Admin Organizer. You can also use the default demo password 'admin' or contact the tournament desk."
                )
            },
            confirmButton = {
                TextButton(onClick = { showForgotPasswordDialog = false }) {
                    Text("OK", color = KabaddiOrange)
                }
            }
        )
    }
}
