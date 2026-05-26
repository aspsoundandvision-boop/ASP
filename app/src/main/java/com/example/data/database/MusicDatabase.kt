package com.example.data.database

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

// --- Entities ---

@Entity(tableName = "songs")
data class SongEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val artist: String,
    val duration: String, // e.g. "2:30"
    val category: String, // E.g., "Trending", "Pulse", "Ambient", "Focus"
    val coverColorHex: String = "#FF121212", // Hex color for artwork background
    val synthPreset: String = "Cosmonaut", // "Cosmonaut", "NeonPulse", "DeepVoyage", "MutedRain"
    val coverUrl: String? = "", // HTTP URL for image preview art
    val releaseType: String? = "Single", // "Album" or "Single" to distinguish releases
    val isFeatured: Boolean = false,
    val isLatest: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "branding")
data class BrandingEntity(
    @PrimaryKey val id: Int = 1,
    val appTitle: String = "ASP Sound & Vision",
    val primaryColorHex: String = "#FFFF007F", // Cinematic Neon Pink
    val splashGlowHex: String = "#FFFF007F",
    val splashGlowDurationMs: Long = 4000L,
    val logoText: String = "ASP"
)

// --- DAOs ---

@Dao
interface SongDao {
    @Query("SELECT * FROM songs ORDER BY timestamp DESC")
    fun getAllSongsFlow(): Flow<List<SongEntity>>

    @Query("SELECT * FROM songs WHERE isFeatured = 1 LIMIT 1")
    suspend fun getFeaturedSong(): SongEntity?

    @Query("SELECT * FROM songs WHERE isLatest = 1 ORDER BY timestamp DESC")
    fun getLatestReleasesFlow(): Flow<List<SongEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSong(song: SongEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSongs(songs: List<SongEntity>)

    @Query("DELETE FROM songs WHERE id = :id")
    suspend fun deleteSongById(id: Int)

    @Query("SELECT COUNT(*) FROM songs")
    suspend fun getSongCount(): Int
}

@Dao
interface BrandingDao {
    @Query("SELECT * FROM branding WHERE id = 1 LIMIT 1")
    fun getBrandingFlow(): Flow<BrandingEntity?>

    @Query("SELECT * FROM branding WHERE id = 1 LIMIT 1")
    suspend fun getBrandingSync(): BrandingEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateBranding(branding: BrandingEntity)
}

// --- AppDatabase ---

@Database(entities = [SongEntity::class, BrandingEntity::class], version = 4, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun songDao(): SongDao
    abstract fun brandingDao(): BrandingDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "sound_and_vision_db"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
