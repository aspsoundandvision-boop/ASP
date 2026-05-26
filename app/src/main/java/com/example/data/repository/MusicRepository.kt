package com.example.data.repository

import com.example.data.database.AppDatabase
import com.example.data.database.BrandingEntity
import com.example.data.database.SongEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow

class MusicRepository(private val db: AppDatabase) {
    private val songDao = db.songDao()
    private val brandingDao = db.brandingDao()

    val allSongs: Flow<List<SongEntity>> = songDao.getAllSongsFlow()
    val latestReleases: Flow<List<SongEntity>> = songDao.getLatestReleasesFlow()
    val brandingFlow: Flow<BrandingEntity?> = brandingDao.getBrandingFlow()

    suspend fun initializeDatabase() {
        // Pre-populate songs if empty
        if (songDao.getSongCount() == 0) {
            val defaultSongs = listOf(
                SongEntity(
                    title = "Chrono Glide",
                    artist = "Symphony of Space",
                    duration = "3:25",
                    category = "Ambient",
                    coverColorHex = "#FF1E3A8A", // Deep Navy Blue
                    synthPreset = "Cosmonaut",
                    coverUrl = "https://images.unsplash.com/photo-1614613535308-eb5fbd3d2c17?q=80&w=400&auto=format&fit=crop",
                    releaseType = "Album",
                    isFeatured = true,
                    isLatest = false
                ),
                SongEntity(
                    title = "Solar Wind",
                    artist = "Symphony of Space",
                    duration = "2:50",
                    category = "Ambient",
                    coverColorHex = "#FF312E81", // Indigo
                    synthPreset = "Cosmonaut",
                    coverUrl = "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?q=80&w=400&auto=format&fit=crop",
                    releaseType = "Single",
                    isFeatured = false,
                    isLatest = true
                ),
                SongEntity(
                    title = "Neon Horizon",
                    artist = "Cyber Pulse",
                    duration = "4:12",
                    category = "Pulse",
                    coverColorHex = "#FF8800CC", // Purple/Magenta
                    synthPreset = "NeonPulse",
                    coverUrl = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?q=80&w=400&auto=format&fit=crop",
                    releaseType = "Album",
                    isFeatured = false,
                    isLatest = true
                ),
                SongEntity(
                    title = "Quantum Shift",
                    artist = "Cyber Pulse",
                    duration = "3:40",
                    category = "Pulse",
                    coverColorHex = "#FF1F2937", // Charcoal Gray
                    synthPreset = "NeonPulse",
                    coverUrl = "https://images.unsplash.com/photo-1508700115892-45ecd05ae2ad?q=80&w=400&auto=format&fit=crop",
                    releaseType = "Single",
                    isFeatured = false,
                    isLatest = false
                ),
                SongEntity(
                    title = "Midnight Dust",
                    artist = "Lunar Echo",
                    duration = "2:15",
                    category = "Chill",
                    coverColorHex = "#FF311B92", // Deep Violet
                    synthPreset = "MutedRain",
                    coverUrl = "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?q=80&w=400&auto=format&fit=crop",
                    releaseType = "Single",
                    isFeatured = true,
                    isLatest = true
                ),
                SongEntity(
                    title = "Ethereal Dawn",
                    artist = "Deep Voyage",
                    duration = "5:04",
                    category = "Cinematic",
                    coverColorHex = "#FF004D40", // Dark Teal
                    synthPreset = "DeepVoyage",
                    coverUrl = "https://images.unsplash.com/photo-1506157786151-b8491531f063?q=80&w=400&auto=format&fit=crop",
                    releaseType = "Album",
                    isFeatured = false,
                    isLatest = false
                ),
                SongEntity(
                    title = "Soma Rain",
                    artist = "Aura Glow",
                    duration = "3:30",
                    category = "Chill",
                    coverColorHex = "#FF1E3A8A", // Deep Blue
                    synthPreset = "MutedRain",
                    coverUrl = "https://images.unsplash.com/photo-1487180142328-054b783fc471?q=80&w=400&auto=format&fit=crop",
                    releaseType = "Single",
                    isFeatured = false,
                    isLatest = false
                )
            )
            songDao.insertSongs(defaultSongs)
        }

        // Pre-populate branding if empty
        if (brandingDao.getBrandingSync() == null) {
            brandingDao.updateBranding(BrandingEntity())
        }
    }

    suspend fun getFeaturedSong(): SongEntity? {
        return songDao.getFeaturedSong()
    }

    suspend fun insertSong(song: SongEntity) {
        songDao.insertSong(song)
    }

    suspend fun deleteSong(id: Int) {
        songDao.deleteSongById(id)
    }

    suspend fun updateBranding(branding: BrandingEntity) {
        brandingDao.updateBranding(branding)
    }
}
