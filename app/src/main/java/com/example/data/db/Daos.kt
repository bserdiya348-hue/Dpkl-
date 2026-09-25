package com.example.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users ORDER BY id ASC")
    fun getAllUsers(): Flow<List<UserEntity>>

    @Query("SELECT * FROM users WHERE emailOrMobile = :identifier LIMIT 1")
    fun getUserByEmailOrMobile(identifier: String): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE emailOrMobile = :identifier AND password = :password LIMIT 1")
    suspend fun login(identifier: String, password: String): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsers(users: List<UserEntity>)

    @Delete
    suspend fun deleteUser(user: UserEntity)

    @Query("DELETE FROM users WHERE id = :userId")
    suspend fun deleteUserById(userId: Long)
}

@Dao
interface TeamDao {
    @Query("SELECT * FROM teams ORDER BY name ASC")
    fun getAllTeams(): Flow<List<TeamEntity>>

    @Query("SELECT * FROM teams WHERE id = :teamId LIMIT 1")
    fun getTeamById(teamId: Long): Flow<TeamEntity?>

    @Query("SELECT * FROM teams WHERE id = :teamId LIMIT 1")
    suspend fun getTeamByIdDirect(teamId: Long): TeamEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTeam(team: TeamEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTeams(teams: List<TeamEntity>)

    @Update
    suspend fun updateTeam(team: TeamEntity)

    @Query("DELETE FROM teams WHERE id = :teamId")
    suspend fun deleteTeamById(teamId: Long)

    @Query("UPDATE teams SET spentAmount = spentAmount + :amount WHERE id = :teamId")
    suspend fun addSpending(teamId: Long, amount: Long)

    @Query("UPDATE teams SET spentAmount = :amount WHERE id = :teamId")
    suspend fun setSpentAmount(teamId: Long, amount: Long)
}

@Dao
interface PlayerDao {
    @Query("SELECT * FROM players ORDER BY id DESC")
    fun getAllPlayers(): Flow<List<PlayerEntity>>

    @Query("SELECT * FROM players WHERE status = 'APPROVED' ORDER BY id ASC")
    fun getApprovedPlayers(): Flow<List<PlayerEntity>>

    @Query("SELECT * FROM players WHERE auctionStatus = :status ORDER BY id ASC")
    fun getPlayersByAuctionStatus(status: String): Flow<List<PlayerEntity>>

    @Query("SELECT * FROM players WHERE id = :playerId LIMIT 1")
    fun getPlayerById(playerId: Long): Flow<PlayerEntity?>

    @Query("SELECT * FROM players WHERE id = :playerId LIMIT 1")
    suspend fun getPlayerByIdDirect(playerId: Long): PlayerEntity?

    @Query("SELECT * FROM players WHERE soldTeamId = :teamId ORDER BY soldPrice DESC")
    fun getPlayersByTeam(teamId: Long): Flow<List<PlayerEntity>>

    @Query("SELECT * FROM players WHERE mobileNumber = :mobile LIMIT 1")
    fun getPlayerByMobile(mobile: String): Flow<PlayerEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlayer(player: PlayerEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlayers(players: List<PlayerEntity>)

    @Update
    suspend fun updatePlayer(player: PlayerEntity)

    @Query("DELETE FROM players WHERE id = :playerId")
    suspend fun deletePlayerById(playerId: Long)

    @Query("UPDATE players SET status = :status WHERE id = :playerId")
    suspend fun updateRegistrationStatus(playerId: Long, status: String)

    @Query("UPDATE players SET basePrice = :basePrice WHERE id = :playerId")
    suspend fun updateBasePrice(playerId: Long, basePrice: Long)

    @Query("UPDATE players SET auctionStatus = :auctionStatus, soldPrice = :soldPrice, soldTeamId = :teamId, soldTeamName = :teamName WHERE id = :playerId")
    suspend fun updateAuctionResult(playerId: Long, auctionStatus: String, soldPrice: Long, teamId: Long?, teamName: String)
}

@Dao
interface BidDao {
    @Query("SELECT * FROM bids WHERE playerId = :playerId ORDER BY timestamp DESC")
    fun getBidsForPlayer(playerId: Long): Flow<List<BidEntity>>

    @Query("SELECT * FROM bids ORDER BY timestamp DESC")
    fun getAllBids(): Flow<List<BidEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBid(bid: BidEntity): Long

    @Query("DELETE FROM bids WHERE playerId = :playerId")
    suspend fun deleteBidsForPlayer(playerId: Long)

    @Query("DELETE FROM bids")
    suspend fun clearAllBids()
}

@Dao
interface AuctionDao {
    @Query("SELECT * FROM auction_state WHERE id = 1 LIMIT 1")
    fun getAuctionState(): Flow<AuctionStateEntity?>

    @Query("SELECT * FROM auction_state WHERE id = 1 LIMIT 1")
    suspend fun getAuctionStateDirect(): AuctionStateEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setAuctionState(state: AuctionStateEntity)
}
