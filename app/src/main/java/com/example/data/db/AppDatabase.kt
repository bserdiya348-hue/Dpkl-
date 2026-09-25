package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        UserEntity::class,
        TeamEntity::class,
        PlayerEntity::class,
        BidEntity::class,
        AuctionStateEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun teamDao(): TeamDao
    abstract fun playerDao(): PlayerDao
    abstract fun bidDao(): BidDao
    abstract fun auctionDao(): AuctionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "dpkl_auction_database"
                )
                    .addCallback(DatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        seedDatabase(database)
                    }
                }
            }
        }

        suspend fun seedDatabase(db: AppDatabase) {
            val teamDao = db.teamDao()
            val playerDao = db.playerDao()
            val userDao = db.userDao()
            val auctionDao = db.auctionDao()

            // 1. Teams
            val teams = listOf(
                TeamEntity(
                    id = 1,
                    name = "Haryana Hawks",
                    shortCode = "HAR",
                    logoColor = "#EA580C",
                    captainName = "Manjeet Dahiya",
                    sponsorName = "Khel Ratna Sports",
                    purseBudget = 5000000L,
                    spentAmount = 0L,
                    managerEmail = "manager@hawks.com"
                ),
                TeamEntity(
                    id = 2,
                    name = "Puneri Warriors",
                    shortCode = "PUN",
                    logoColor = "#D97706",
                    captainName = "Aslam Inamdar",
                    sponsorName = "Maharashtra Agro",
                    purseBudget = 5000000L,
                    spentAmount = 0L,
                    managerEmail = "manager@puneri.com"
                ),
                TeamEntity(
                    id = 3,
                    name = "Jaipur Titans",
                    shortCode = "JAI",
                    logoColor = "#E11D48",
                    captainName = "Arjun Deshwal",
                    sponsorName = "Pink City Jewels",
                    purseBudget = 5000000L,
                    spentAmount = 0L,
                    managerEmail = "manager@jaipur.com"
                ),
                TeamEntity(
                    id = 4,
                    name = "Bengaluru Tigers",
                    shortCode = "BLR",
                    logoColor = "#2563EB",
                    captainName = "Bharat Hooda",
                    sponsorName = "TechCity Express",
                    purseBudget = 5000000L,
                    spentAmount = 0L,
                    managerEmail = "manager@bengaluru.com"
                ),
                TeamEntity(
                    id = 5,
                    name = "Tamil Thunders",
                    shortCode = "TAM",
                    logoColor = "#7C3AED",
                    captainName = "Narender Kandola",
                    sponsorName = "Kaveri Feeds",
                    purseBudget = 5000000L,
                    spentAmount = 0L,
                    managerEmail = "manager@tamil.com"
                ),
                TeamEntity(
                    id = 6,
                    name = "Patna Strikers",
                    shortCode = "PAT",
                    logoColor = "#059669",
                    captainName = "Sachin Tanwar",
                    sponsorName = "Ganga Infra",
                    purseBudget = 5000000L,
                    spentAmount = 0L,
                    managerEmail = "manager@patna.com"
                )
            )
            teamDao.insertTeams(teams)

            // 2. Users with explicit roles
            val users = listOf(
                UserEntity(
                    name = "Super Admin (Organizer)",
                    emailOrMobile = "admin@dpkl.com",
                    password = "admin",
                    role = "SUPER_ADMIN"
                ),
                UserEntity(
                    name = "Official Auctioneer",
                    emailOrMobile = "auction@dpkl.com",
                    password = "admin",
                    role = "AUCTION_ADMIN"
                ),
                UserEntity(
                    name = "Vikram Rathore (Hawks Manager)",
                    emailOrMobile = "manager@hawks.com",
                    password = "admin",
                    role = "TEAM_MANAGER",
                    teamId = 1
                ),
                UserEntity(
                    name = "Pawan Sehrawat",
                    emailOrMobile = "player@dpkl.com",
                    password = "admin",
                    role = "PLAYER",
                    playerId = 1
                )
            )
            userDao.insertUsers(users)

            // 3. Registered Players
            val players = listOf(
                PlayerEntity(
                    id = 1,
                    fullName = "Pawan Sehrawat",
                    fatherName = "Ramkishan Sehrawat",
                    mobileNumber = "9876543210",
                    age = 26,
                    villageCity = "Delhi",
                    position = "Raider",
                    jerseyNumber = 18,
                    photoPreset = "raider_1",
                    status = "APPROVED",
                    auctionStatus = "UPCOMING",
                    basePrice = 50000L
                ),
                PlayerEntity(
                    id = 2,
                    fullName = "Naveen Kumar",
                    fatherName = "Sanjay Kumar",
                    mobileNumber = "9876543211",
                    age = 24,
                    villageCity = "Bhiwani, Haryana",
                    position = "Raider",
                    jerseyNumber = 10,
                    photoPreset = "raider_2",
                    status = "APPROVED",
                    auctionStatus = "UPCOMING",
                    basePrice = 50000L
                ),
                PlayerEntity(
                    id = 3,
                    fullName = "Fazel Atrachali",
                    fatherName = "Gholamreza Atrachali",
                    mobileNumber = "9876543212",
                    age = 31,
                    villageCity = "Gorgan",
                    position = "Defender",
                    jerseyNumber = 1,
                    photoPreset = "defender_1",
                    status = "APPROVED",
                    auctionStatus = "UPCOMING",
                    basePrice = 40000L
                ),
                PlayerEntity(
                    id = 4,
                    fullName = "Mohammadreza Shadloui",
                    fatherName = "Ali Chiyaneh",
                    mobileNumber = "9876543213",
                    age = 23,
                    villageCity = "Urmia",
                    position = "All-Rounder",
                    jerseyNumber = 9,
                    photoPreset = "allrounder_1",
                    status = "APPROVED",
                    auctionStatus = "UPCOMING",
                    basePrice = 50000L
                ),
                PlayerEntity(
                    id = 5,
                    fullName = "Sunil Kumar",
                    fatherName = "Subhash Kumar",
                    mobileNumber = "9876543214",
                    age = 27,
                    villageCity = "Sonipat, Haryana",
                    position = "Defender",
                    jerseyNumber = 7,
                    photoPreset = "defender_2",
                    status = "APPROVED",
                    auctionStatus = "UPCOMING",
                    basePrice = 35000L
                ),
                PlayerEntity(
                    id = 6,
                    fullName = "Pradeep Narwal",
                    fatherName = "Dharamvir Narwal",
                    mobileNumber = "9876543215",
                    age = 27,
                    villageCity = "Rindhana, Haryana",
                    position = "Raider",
                    jerseyNumber = 8,
                    photoPreset = "raider_3",
                    status = "APPROVED",
                    auctionStatus = "UPCOMING",
                    basePrice = 40000L
                ),
                PlayerEntity(
                    id = 7,
                    fullName = "Surjeet Singh",
                    fatherName = "Jaspal Singh",
                    mobileNumber = "9876543216",
                    age = 32,
                    villageCity = "Jalandhar, Punjab",
                    position = "Defender",
                    jerseyNumber = 3,
                    photoPreset = "defender_3",
                    status = "APPROVED",
                    auctionStatus = "UPCOMING",
                    basePrice = 30000L
                ),
                PlayerEntity(
                    id = 8,
                    fullName = "Vijay Malik",
                    fatherName = "Rajender Malik",
                    mobileNumber = "9876543217",
                    age = 25,
                    villageCity = "Rohtak, Haryana",
                    position = "All-Rounder",
                    jerseyNumber = 11,
                    photoPreset = "allrounder_2",
                    status = "APPROVED",
                    auctionStatus = "UPCOMING",
                    basePrice = 30000L
                ),
                PlayerEntity(
                    id = 9,
                    fullName = "Nitesh Kumar",
                    fatherName = "Satyawan Kumar",
                    mobileNumber = "9876543218",
                    age = 25,
                    villageCity = "Charkhi Dadri",
                    position = "Defender",
                    jerseyNumber = 2,
                    photoPreset = "defender_4",
                    status = "PENDING",
                    auctionStatus = "UPCOMING",
                    basePrice = 25000L
                ),
                PlayerEntity(
                    id = 10,
                    fullName = "Ashu Malik",
                    fatherName = "Surender Malik",
                    mobileNumber = "9876543219",
                    age = 22,
                    villageCity = "Sonipat, Haryana",
                    position = "Raider",
                    jerseyNumber = 14,
                    photoPreset = "raider_4",
                    status = "PENDING",
                    auctionStatus = "UPCOMING",
                    basePrice = 25000L
                )
            )
            playerDao.insertPlayers(players)

            // 4. Initial Auction State
            val initialAuction = AuctionStateEntity(
                id = 1,
                currentPlayerId = null,
                currentBid = 0L,
                highestTeamId = null,
                highestTeamName = "",
                timerSeconds = 30,
                isTimerRunning = false,
                auctionPhase = "IDLE"
            )
            auctionDao.setAuctionState(initialAuction)
        }
    }
}
