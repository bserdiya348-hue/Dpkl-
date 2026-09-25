package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import java.text.NumberFormat
import java.util.Locale

fun formatRupees(amount: Long): String {
    return if (amount >= 10000000L) {
        val cr = amount / 10000000.0
        "₹%.2f Cr".format(cr)
    } else if (amount >= 100000L) {
        val lakhs = amount / 100000.0
        "₹%.2f Lakh".format(lakhs)
    } else {
        val formatter = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
        formatter.maximumFractionDigits = 0
        formatter.format(amount)
    }
}

fun parseColorHex(hex: String, defaultColor: Color = KabaddiOrange): Color {
    return try {
        val clean = hex.removePrefix("#")
        if (clean.length == 6) {
            Color(android.graphics.Color.parseColor("#$clean"))
        } else {
            defaultColor
        }
    } catch (e: Exception) {
        defaultColor
    }
}

@Composable
fun PlayerAvatar(
    name: String,
    position: String,
    jerseyNumber: Int,
    modifier: Modifier = Modifier,
    size: Dp = 64.dp,
    showBadge: Boolean = true
) {
    val positionColor = when (position.lowercase()) {
        "raider" -> Color(0xFFEA580C)
        "defender" -> Color(0xFF0284C7)
        else -> Color(0xFF16A34A) // All-Rounder
    }

    val initials = name.split(" ")
        .mapNotNull { it.firstOrNull()?.toString() }
        .take(2)
        .joinToString("")
        .ifEmpty { "K" }

    Box(
        modifier = modifier
            .size(size)
            .shadow(6.dp, CircleShape)
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        positionColor.copy(alpha = 0.85f),
                        StadiumSurfaceVariant
                    )
                )
            )
            .border(2.dp, positionColor, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = initials,
                color = Color.White,
                fontWeight = FontWeight.Black,
                fontSize = (size.value * 0.35f).sp
            )
            if (jerseyNumber > 0) {
                Text(
                    text = "#$jerseyNumber",
                    color = KabaddiGold,
                    fontWeight = FontWeight.Bold,
                    fontSize = (size.value * 0.20f).sp
                )
            }
        }

        if (showBadge) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(size * 0.35f)
                    .clip(CircleShape)
                    .background(positionColor)
                    .border(1.dp, Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = when (position.lowercase()) {
                        "raider" -> "R"
                        "defender" -> "D"
                        else -> "AR"
                    },
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = (size.value * 0.16f).sp
                )
            }
        }
    }
}

@Composable
fun TeamBadge(
    teamName: String,
    shortCode: String,
    colorHex: String,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp
) {
    val teamColor = parseColorHex(colorHex)
    Box(
        modifier = modifier
            .size(size)
            .shadow(4.dp, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .background(
                Brush.linearGradient(
                    listOf(teamColor, teamColor.copy(alpha = 0.7f))
                )
            )
            .border(1.5.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = shortCode.ifEmpty { teamName.take(3).uppercase() },
            color = Color.White,
            fontWeight = FontWeight.ExtraBold,
            fontSize = (size.value * 0.35f).sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun StatusChip(
    status: String,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor, label) = when (status.uppercase()) {
        "APPROVED" -> Triple(ActionGreen.copy(alpha = 0.2f), ActionGreen, "Approved")
        "PENDING" -> Triple(KabaddiGold.copy(alpha = 0.2f), KabaddiGold, "Pending Approval")
        "REJECTED" -> Triple(ActionRed.copy(alpha = 0.2f), ActionRed, "Rejected")
        "SOLD" -> Triple(ActionGreen.copy(alpha = 0.25f), ActionGreen, "SOLD")
        "UNSOLD" -> Triple(ActionRed.copy(alpha = 0.25f), ActionRed, "UNSOLD")
        "IN_AUCTION", "LIVE" -> Triple(KabaddiOrange.copy(alpha = 0.25f), KabaddiOrange, "IN AUCTION")
        else -> Triple(StadiumBorder, TextSecondary, "Upcoming")
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = bgColor,
        border = androidx.compose.foundation.BorderStroke(1.dp, textColor.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(textColor)
            )
            Text(
                text = label,
                color = textColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun PositionBadge(
    position: String,
    modifier: Modifier = Modifier
) {
    val (bg, icon) = when (position.lowercase()) {
        "raider" -> Pair(Color(0xFFEA580C), Icons.Default.Bolt)
        "defender" -> Pair(Color(0xFF0284C7), Icons.Default.Shield)
        else -> Pair(Color(0xFF16A34A), Icons.Default.Star)
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = bg.copy(alpha = 0.2f),
        border = androidx.compose.foundation.BorderStroke(1.dp, bg.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = bg,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = position,
                color = bg,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun CountdownTimerWidget(
    secondsRemaining: Int,
    totalSeconds: Int = 30,
    isRunning: Boolean = true,
    modifier: Modifier = Modifier
) {
    val progress = (secondsRemaining.toFloat() / totalSeconds.coerceAtLeast(1).toFloat()).coerceIn(0f, 1f)
    val isUrgent = secondsRemaining <= 10

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isUrgent && isRunning) 1.08f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    val timerColor by animateColorAsState(
        targetValue = when {
            secondsRemaining <= 5 -> ActionRed
            secondsRemaining <= 10 -> KabaddiOrange
            else -> KabaddiGold
        },
        label = "timerColor"
    )

    Box(
        modifier = modifier
            .size(90.dp)
            .shadow(8.dp, CircleShape)
            .clip(CircleShape)
            .background(StadiumSurface)
            .border(3.dp, timerColor.copy(alpha = 0.8f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxSize().padding(4.dp),
            color = timerColor,
            trackColor = StadiumBorder,
            strokeWidth = 5.dp
        )
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "$secondsRemaining",
                color = timerColor,
                fontSize = (28 * pulseScale).sp,
                fontWeight = FontWeight.Black
            )
            Text(
                text = "SEC",
                color = TextSecondary,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }
    }
}
