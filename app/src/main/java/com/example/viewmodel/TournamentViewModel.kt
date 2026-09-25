package com.example.viewmodel

import android.app.Application
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.*
import com.example.ui.components.formatRupees
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class TournamentViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application, viewModelScope)
    val repository = TournamentRepository(database)

    // Auth State
    private val _currentUser = MutableStateFlow<UserEntity?>(null)
    val currentUser: StateFlow<UserEntity?> = _currentUser.asStateFlow()

    private val _currentRole = MutableStateFlow("PUBLIC")
    val currentRole: StateFlow<String> = _currentRole.asStateFlow()

    // Navigation state
    private val _activeScreen = MutableStateFlow("LIVE_AUCTION")
    val activeScreen: StateFlow<String> = _activeScreen.asStateFlow()

    // Status message for Snackbars
    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    // Core Data flows
    val teams: StateFlow<List<TeamEntity>> = repository.allTeams.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val players: StateFlow<List<PlayerEntity>> = repository.allPlayers.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val approvedPlayers: StateFlow<List<PlayerEntity>> = repository.approvedPlayers.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val allBids: StateFlow<List<BidEntity>> = repository.allBids.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val auctionState: StateFlow<AuctionStateEntity?> = repository.auctionState.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    val allUsers: StateFlow<List<UserEntity>> = repository.allUsers.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Derived active player in auction
    val currentAuctionPlayer: StateFlow<PlayerEntity?> = combine(auctionState, players) { state, playerList ->
        val playerId = state?.currentPlayerId ?: return@combine null
        playerList.find { it.id == playerId }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    // Timer Job
    private var timerJob: Job? = null

    init {
        // Observe timer in auction state and run countdown if enabled
        viewModelScope.launch {
            auctionState.collectLatest { state ->
                if (state != null && state.isTimerRunning && state.auctionPhase == "LIVE") {
                    startTimerTicker()
                } else {
                    timerJob?.cancel()
                }
            }
        }
    }

    fun navigateTo(screen: String) {
        _activeScreen.value = screen
    }

    fun showMessage(msg: String) {
        _userMessage.value = msg
    }

    fun clearMessage() {
        _userMessage.value = null
    }

    // ----------------- AUTHENTICATION -----------------

    fun login(identifier: String, pass: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val user = repository.login(identifier.trim(), pass.trim())
            if (user != null) {
                _currentUser.value = user
                _currentRole.value = user.role
                when (user.role) {
                    "SUPER_ADMIN" -> _activeScreen.value = "SUPER_ADMIN"
                    "AUCTION_ADMIN" -> _activeScreen.value = "AUCTION_CONTROL"
                    "TEAM_MANAGER" -> _activeScreen.value = "TEAM_DASHBOARD"
                    "PLAYER" -> _activeScreen.value = "PLAYER_PROFILE"
                    else -> _activeScreen.value = "LIVE_AUCTION"
                }
                onResult(true, "Welcome back, ${user.name}!")
            } else {
                onResult(false, "Invalid email/mobile or password. Try demo accounts below!")
            }
        }
    }

    fun quickLoginAs(role: String) {
        viewModelScope.launch {
            val users = allUsers.value
            val match = users.find { it.role == role }
            if (match != null) {
                _currentUser.value = match
                _currentRole.value = match.role
            } else {
                // Fallback default entity
                val dummy = when (role) {
                    "SUPER_ADMIN" -> UserEntity(1, "Tournament Director", "admin@dpkl.com", "admin", "SUPER_ADMIN")
                    "AUCTION_ADMIN" -> UserEntity(2, "Official Auctioneer", "auction@dpkl.com", "admin", "AUCTION_ADMIN")
                    "TEAM_MANAGER" -> UserEntity(3, "Vikram Rathore", "manager@hawks.com", "admin", "TEAM_MANAGER", teamId = 1)
                    "PLAYER" -> UserEntity(4, "Pawan Sehrawat", "player@dpkl.com", "admin", "PLAYER", playerId = 1)
                    else -> UserEntity(0, "Guest Viewer", "public", "", "PUBLIC")
                }
                _currentUser.value = dummy
                _currentRole.value = role
            }

            when (role) {
                "SUPER_ADMIN" -> _activeScreen.value = "SUPER_ADMIN"
                "AUCTION_ADMIN" -> _activeScreen.value = "AUCTION_CONTROL"
                "TEAM_MANAGER" -> _activeScreen.value = "TEAM_DASHBOARD"
                "PLAYER" -> _activeScreen.value = "PLAYER_PROFILE"
                else -> _activeScreen.value = "LIVE_AUCTION"
            }
            showMessage("Logged in as $role")
        }
    }

    fun enterAsGuest() {
        _currentUser.value = null
        _currentRole.value = "PUBLIC"
        _activeScreen.value = "LIVE_AUCTION"
        showMessage("Viewing in Public Live Mode")
    }

    fun logout() {
        _currentUser.value = null
        _currentRole.value = "PUBLIC"
        _activeScreen.value = "LOGIN"
        showMessage("Logged out successfully")
    }

    // ----------------- AUCTION CONTROL -----------------

    fun selectPlayerForAuction(player: PlayerEntity, customBasePrice: Long? = null) {
        viewModelScope.launch {
            val base = customBasePrice ?: player.basePrice
            repository.updateBasePrice(player.id, base)
            repository.updateAuctionResult(player.id, "IN_AUCTION", 0L, null, "")

            val newState = AuctionStateEntity(
                id = 1,
                currentPlayerId = player.id,
                currentBid = base,
                highestTeamId = null,
                highestTeamName = "",
                timerSeconds = 30,
                isTimerRunning = false,
                auctionPhase = "LIVE",
                lastUpdated = System.currentTimeMillis()
            )
            repository.setAuctionState(newState)
            repository.clearBidsForPlayer(player.id)
            showMessage("${player.fullName} brought to the auction block! Base: ₹$base")
        }
    }

    fun placeBid(team: TeamEntity, increment: Long) {
        viewModelScope.launch {
            val currentState = auctionState.value ?: return@launch
            val playerId = currentState.currentPlayerId ?: return@launch
            val player = currentAuctionPlayer.value ?: return@launch

            // Current bid plus increment
            val newAmount = if (currentState.highestTeamId == null && currentState.currentBid == player.basePrice) {
                // First bid is at least base price or base + increment
                player.basePrice + increment
            } else {
                currentState.currentBid + increment
            }

            // Check team purse
            val remainingPurse = team.purseBudget - team.spentAmount
            if (newAmount > remainingPurse) {
                showMessage("Bid exceeds ${team.name}'s remaining purse (${formatRupees(remainingPurse)})")
                return@launch
            }

            // Record Bid
            repository.insertBid(
                BidEntity(
                    playerId = playerId,
                    playerName = player.fullName,
                    teamId = team.id,
                    teamName = team.name,
                    amount = newAmount
                )
            )

            // Update Auction State & reset timer to 20s
            repository.setAuctionState(
                currentState.copy(
                    currentBid = newAmount,
                    highestTeamId = team.id,
                    highestTeamName = team.name,
                    timerSeconds = 20,
                    isTimerRunning = true,
                    auctionPhase = "LIVE",
                    lastUpdated = System.currentTimeMillis()
                )
            )
            vibrateDevice(50)
            showMessage("Bid updated: ₹$newAmount by ${team.name}")
        }
    }

    fun setManualBid(team: TeamEntity, targetAmount: Long) {
        viewModelScope.launch {
            val currentState = auctionState.value ?: return@launch
            val playerId = currentState.currentPlayerId ?: return@launch
            val player = currentAuctionPlayer.value ?: return@launch

            if (targetAmount <= currentState.currentBid) {
                showMessage("Bid must be strictly higher than current ₹${currentState.currentBid}")
                return@launch
            }

            val remainingPurse = team.purseBudget - team.spentAmount
            if (targetAmount > remainingPurse) {
                showMessage("Bid exceeds ${team.name}'s remaining purse (${formatRupees(remainingPurse)})")
                return@launch
            }

            repository.insertBid(
                BidEntity(
                    playerId = playerId,
                    playerName = player.fullName,
                    teamId = team.id,
                    teamName = team.name,
                    amount = targetAmount
                )
            )

            repository.setAuctionState(
                currentState.copy(
                    currentBid = targetAmount,
                    highestTeamId = team.id,
                    highestTeamName = team.name,
                    timerSeconds = 25,
                    isTimerRunning = true,
                    auctionPhase = "LIVE",
                    lastUpdated = System.currentTimeMillis()
                )
            )
            vibrateDevice(50)
            showMessage("Manual Bid: ₹$targetAmount by ${team.name}")
        }
    }

    fun markSold() {
        viewModelScope.launch {
            val currentState = auctionState.value ?: return@launch
            val playerId = currentState.currentPlayerId ?: return@launch
            val player = currentAuctionPlayer.value ?: return@launch

            if (currentState.highestTeamId == null) {
                showMessage("Cannot mark SOLD without any bidding team. Mark UNSOLD instead.")
                return@launch
            }

            val winningTeamId = currentState.highestTeamId
            val finalPrice = currentState.currentBid

            // 1. Update player
            repository.updateAuctionResult(
                playerId = playerId,
                status = "SOLD",
                soldPrice = finalPrice,
                teamId = winningTeamId,
                teamName = currentState.highestTeamName
            )

            // 2. Update team spent amount
            repository.updateTeamSpent(winningTeamId, finalPrice)

            // 3. Update auction state
            repository.setAuctionState(
                currentState.copy(
                    isTimerRunning = false,
                    auctionPhase = "SOLD",
                    lastUpdated = System.currentTimeMillis()
                )
            )
            vibrateDevice(250)
            showMessage("🎉 SOLD! ${player.fullName} sold to ${currentState.highestTeamName} for ₹$finalPrice!")
        }
    }

    fun markUnsold() {
        viewModelScope.launch {
            val currentState = auctionState.value ?: return@launch
            val playerId = currentState.currentPlayerId ?: return@launch
            val player = currentAuctionPlayer.value ?: return@launch

            repository.updateAuctionResult(
                playerId = playerId,
                status = "UNSOLD",
                soldPrice = 0L,
                teamId = null,
                teamName = ""
            )

            repository.setAuctionState(
                currentState.copy(
                    isTimerRunning = false,
                    auctionPhase = "UNSOLD",
                    lastUpdated = System.currentTimeMillis()
                )
            )
            vibrateDevice(100)
            showMessage("${player.fullName} marked as UNSOLD")
        }
    }

    fun resetAuctionState() {
        viewModelScope.launch {
            repository.setAuctionState(
                AuctionStateEntity(
                    id = 1,
                    currentPlayerId = null,
                    currentBid = 0L,
                    highestTeamId = null,
                    highestTeamName = "",
                    timerSeconds = 30,
                    isTimerRunning = false,
                    auctionPhase = "IDLE",
                    lastUpdated = System.currentTimeMillis()
                )
            )
        }
    }

    // Timer controls
    fun startTimer() {
        viewModelScope.launch {
            val state = auctionState.value ?: return@launch
            repository.setAuctionState(state.copy(isTimerRunning = true))
        }
    }

    fun pauseTimer() {
        viewModelScope.launch {
            val state = auctionState.value ?: return@launch
            repository.setAuctionState(state.copy(isTimerRunning = false))
        }
    }

    fun setTimerSeconds(seconds: Int) {
        viewModelScope.launch {
            val state = auctionState.value ?: return@launch
            repository.setAuctionState(state.copy(timerSeconds = seconds))
        }
    }

    private fun startTimerTicker() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000L)
                val state = repository.getAuctionStateDirect() ?: break
                if (!state.isTimerRunning || state.auctionPhase != "LIVE") break
                if (state.timerSeconds > 0) {
                    val nextSec = state.timerSeconds - 1
                    repository.setAuctionState(state.copy(timerSeconds = nextSec))
                    if (nextSec <= 5 && nextSec > 0) {
                        vibrateDevice(40)
                    }
                } else {
                    // Timer reached 0
                    vibrateDevice(400)
                    repository.setAuctionState(state.copy(isTimerRunning = false))
                    break
                }
            }
        }
    }

    // ----------------- TEAM MANAGEMENT -----------------

    fun addTeam(
        name: String,
        shortCode: String,
        logoColor: String,
        captainName: String,
        sponsorName: String,
        purseBudget: Long,
        managerEmail: String
    ) {
        viewModelScope.launch {
            val team = TeamEntity(
                name = name.trim(),
                shortCode = shortCode.trim().uppercase(),
                logoColor = logoColor,
                captainName = captainName.trim(),
                sponsorName = sponsorName.trim(),
                purseBudget = purseBudget,
                spentAmount = 0L,
                managerEmail = managerEmail.trim()
            )
            val teamId = repository.insertTeam(team)

            // If manager email provided, create manager user account
            if (managerEmail.isNotBlank()) {
                repository.insertUser(
                    UserEntity(
                        name = "${name} Manager",
                        emailOrMobile = managerEmail.trim(),
                        password = "admin",
                        role = "TEAM_MANAGER",
                        teamId = teamId
                    )
                )
            }
            showMessage("Team $name added successfully!")
        }
    }

    fun updateTeam(team: TeamEntity) {
        viewModelScope.launch {
            repository.updateTeam(team)
            showMessage("Team ${team.name} updated!")
        }
    }

    fun deleteTeam(teamId: Long) {
        viewModelScope.launch {
            val team = teams.value.find { it.id == teamId }
            repository.deleteTeam(teamId)
            showMessage("Team ${team?.name ?: ""} deleted")
        }
    }

    // ----------------- PLAYER REGISTRATION & MANAGEMENT -----------------

    fun registerPlayer(
        fullName: String,
        fatherName: String,
        mobileNumber: String,
        age: Int,
        villageCity: String,
        position: String,
        jerseyNumber: Int,
        photoPreset: String = "raider_1"
    ) {
        viewModelScope.launch {
            val player = PlayerEntity(
                fullName = fullName.trim(),
                fatherName = fatherName.trim(),
                mobileNumber = mobileNumber.trim(),
                age = age,
                villageCity = villageCity.trim(),
                position = position,
                jerseyNumber = jerseyNumber,
                photoPreset = photoPreset,
                status = "PENDING",
                auctionStatus = "UPCOMING",
                basePrice = 20000L
            )
            val playerId = repository.insertPlayer(player)

            // Automatically create player login account using mobile number
            repository.insertUser(
                UserEntity(
                    name = fullName.trim(),
                    emailOrMobile = mobileNumber.trim(),
                    password = "admin",
                    role = "PLAYER",
                    playerId = playerId
                )
            )

            showMessage("Registration submitted successfully! Status is Pending Approval.")
        }
    }

    fun approvePlayer(playerId: Long) {
        viewModelScope.launch {
            repository.updatePlayerStatus(playerId, "APPROVED")
            showMessage("Player approved for auction!")
        }
    }

    fun rejectPlayer(playerId: Long) {
        viewModelScope.launch {
            repository.updatePlayerStatus(playerId, "REJECTED")
            showMessage("Player registration rejected")
        }
    }

    fun updatePlayerBasePrice(playerId: Long, newBasePrice: Long) {
        viewModelScope.launch {
            repository.updateBasePrice(playerId, newBasePrice)
            showMessage("Base price updated to ₹$newBasePrice")
        }
    }

    fun deletePlayer(playerId: Long) {
        viewModelScope.launch {
            repository.deletePlayer(playerId)
            showMessage("Player removed from database")
        }
    }

    fun resetTournamentData() {
        viewModelScope.launch {
            repository.resetTournament()
            showMessage("Tournament database reset to official default data!")
        }
    }

    fun createAdminUser(name: String, email: String, pass: String, role: String) {
        viewModelScope.launch {
            repository.insertUser(
                UserEntity(
                    name = name.ifBlank { "Admin" },
                    emailOrMobile = email.trim(),
                    password = pass.ifBlank { "admin" },
                    role = role
                )
            )
            showMessage("Admin account created for $name ($role)")
        }
    }

    // Helper vibration
    private fun vibrateDevice(durationMs: Long) {
        try {
            val context = getApplication<Application>()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator?.vibrate(
                    VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE)
                )
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                @Suppress("DEPRECATION")
                vibrator?.vibrate(durationMs)
            }
        } catch (e: Exception) {
            // Ignore vibration error
        }
    }
}
