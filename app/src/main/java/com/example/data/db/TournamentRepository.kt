package com.example.data.db

import kotlinx.coroutines.flow.Flow

class TournamentRepository(private val database: AppDatabase) {
    private val userDao = database.userDao()
    private val teamDao = database.teamDao()
    private val playerDao = database.playerDao()
    private val bidDao = database.bidDao()
    private val auctionDao = database.auctionDao()

    // Users
    val allUsers: Flow<List<UserEntity>> = userDao.getAllUsers()
    suspend fun login(identifier: String, pass: String): UserEntity? = userDao.login(identifier, pass)
    suspend fun insertUser(user: UserEntity): Long = userDao.insertUser(user)
    suspend fun deleteUser(userId: Long) = userDao.deleteUserById(userId)

    // Teams
    val allTeams: Flow<List<TeamEntity>> = teamDao.getAllTeams()
    fun getTeamById(teamId: Long): Flow<TeamEntity?> = teamDao.getTeamById(teamId)
    suspend fun getTeamByIdDirect(teamId: Long): TeamEntity? = teamDao.getTeamByIdDirect(teamId)
    suspend fun insertTeam(team: TeamEntity): Long = teamDao.insertTeam(team)
    suspend fun updateTeam(team: TeamEntity) = teamDao.updateTeam(team)
    suspend fun deleteTeam(teamId: Long) = teamDao.deleteTeamById(teamId)
    suspend fun updateTeamSpent(teamId: Long, amount: Long) = teamDao.addSpending(teamId, amount)

    // Players
    val allPlayers: Flow<List<PlayerEntity>> = playerDao.getAllPlayers()
    val approvedPlayers: Flow<List<PlayerEntity>> = playerDao.getApprovedPlayers()
    fun getPlayersByAuctionStatus(status: String): Flow<List<PlayerEntity>> = playerDao.getPlayersByAuctionStatus(status)
    fun getPlayerById(playerId: Long): Flow<PlayerEntity?> = playerDao.getPlayerById(playerId)
    suspend fun getPlayerByIdDirect(playerId: Long): PlayerEntity? = playerDao.getPlayerByIdDirect(playerId)
    fun getPlayersByTeam(teamId: Long): Flow<List<PlayerEntity>> = playerDao.getPlayersByTeam(teamId)
    fun getPlayerByMobile(mobile: String): Flow<PlayerEntity?> = playerDao.getPlayerByMobile(mobile)
    suspend fun insertPlayer(player: PlayerEntity): Long = playerDao.insertPlayer(player)
    suspend fun updatePlayer(player: PlayerEntity) = playerDao.updatePlayer(player)
    suspend fun deletePlayer(playerId: Long) = playerDao.deletePlayerById(playerId)
    suspend fun updatePlayerStatus(playerId: Long, status: String) = playerDao.updateRegistrationStatus(playerId, status)
    suspend fun updateBasePrice(playerId: Long, basePrice: Long) = playerDao.updateBasePrice(playerId, basePrice)
    suspend fun updateAuctionResult(playerId: Long, status: String, soldPrice: Long, teamId: Long?, teamName: String) {
        playerDao.updateAuctionResult(playerId, status, soldPrice, teamId, teamName)
    }

    // Bids
    fun getBidsForPlayer(playerId: Long): Flow<List<BidEntity>> = bidDao.getBidsForPlayer(playerId)
    val allBids: Flow<List<BidEntity>> = bidDao.getAllBids()
    suspend fun insertBid(bid: BidEntity): Long = bidDao.insertBid(bid)
    suspend fun clearBidsForPlayer(playerId: Long) = bidDao.deleteBidsForPlayer(playerId)
    suspend fun clearAllBids() = bidDao.clearAllBids()

    // Auction State
    val auctionState: Flow<AuctionStateEntity?> = auctionDao.getAuctionState()
    suspend fun getAuctionStateDirect(): AuctionStateEntity? = auctionDao.getAuctionStateDirect()
    suspend fun setAuctionState(state: AuctionStateEntity) = auctionDao.setAuctionState(state)

    // Reset Tournament Data helper
    suspend fun resetTournament() {
        AppDatabase.seedDatabase(database)
    }
}
