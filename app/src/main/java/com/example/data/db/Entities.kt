package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val emailOrMobile: String,
    val password: String,
    val role: String, // "SUPER_ADMIN", "AUCTION_ADMIN", "TEAM_MANAGER", "PLAYER", "PUBLIC"
    val teamId: Long? = null,
    val playerId: Long? = null
)

@Entity(tableName = "teams")
data class TeamEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val shortCode: String,
    val logoColor: String = "#FF6B00",
    val captainName: String = "",
    val sponsorName: String = "",
    val purseBudget: Long = 5000000L, // 50 Lakhs default
    val spentAmount: Long = 0L,
    val managerEmail: String = ""
)

@Entity(tableName = "players")
data class PlayerEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fullName: String,
    val fatherName: String,
    val mobileNumber: String,
    val age: Int,
    val villageCity: String,
    val position: String, // "Raider", "Defender", "All-Rounder"
    val jerseyNumber: Int,
    val photoPreset: String = "avatar_1", // avatar preset or file URI
    val status: String = "APPROVED", // "PENDING", "APPROVED", "REJECTED"
    val auctionStatus: String = "UPCOMING", // "UPCOMING", "IN_AUCTION", "SOLD", "UNSOLD"
    val basePrice: Long = 20000L,
    val soldPrice: Long = 0L,
    val soldTeamId: Long? = null,
    val soldTeamName: String = "",
    val registeredAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "bids")
data class BidEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val playerId: Long,
    val playerName: String,
    val teamId: Long,
    val teamName: String,
    val amount: Long,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "auction_state")
data class AuctionStateEntity(
    @PrimaryKey val id: Int = 1,
    val currentPlayerId: Long? = null,
    val currentBid: Long = 0L,
    val highestTeamId: Long? = null,
    val highestTeamName: String = "",
    val timerSeconds: Int = 30,
    val isTimerRunning: Boolean = false,
    val auctionPhase: String = "IDLE", // "IDLE", "LIVE", "SOLD", "UNSOLD"
    val lastUpdated: Long = System.currentTimeMillis()
)
