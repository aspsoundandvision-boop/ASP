package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.database.BrandingEntity
import com.example.data.database.SongEntity
import com.example.data.repository.MusicRepository
import com.example.playback.PlaybackManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MusicViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: MusicRepository

    val allSongs: StateFlow<List<SongEntity>>
    val latestReleases: StateFlow<List<SongEntity>>
    
    private val _branding = MutableStateFlow(BrandingEntity())
    val branding: StateFlow<BrandingEntity> = _branding.asStateFlow()

    init {
        val database = AppDatabase.getDatabase(application)
        repository = MusicRepository(database)

        // Initialize DB asynchronously on launch
        viewModelScope.launch {
            repository.initializeDatabase()
        }

        allSongs = repository.allSongs
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

        latestReleases = repository.latestReleases
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

        viewModelScope.launch {
            repository.brandingFlow.collect { brandingEntity ->
                if (brandingEntity != null) {
                    _branding.value = brandingEntity
                }
            }
        }
    }

    // --- Admin Operations ---

    fun addSong(
        title: String,
        artist: String,
        duration: String,
        category: String,
        coverColorHex: String,
        synthPreset: String,
        coverUrl: String = "",
        releaseType: String = "Single",
        isFeatured: Boolean = false,
        isLatest: Boolean = false
    ) {
        viewModelScope.launch {
            val s = SongEntity(
                title = title,
                artist = artist,
                duration = duration,
                category = category,
                coverColorHex = coverColorHex,
                synthPreset = synthPreset,
                coverUrl = coverUrl,
                releaseType = releaseType,
                isFeatured = isFeatured,
                isLatest = isLatest
            )
            repository.insertSong(s)
        }
    }

    fun deleteSong(id: Int) {
        viewModelScope.launch {
            repository.deleteSong(id)
        }
    }

    fun updateBranding(
        appTitle: String,
        primaryColorHex: String,
        splashGlowHex: String,
        logoText: String
    ) {
        viewModelScope.launch {
            val b = BrandingEntity(
                appTitle = appTitle,
                primaryColorHex = primaryColorHex,
                splashGlowHex = splashGlowHex,
                logoText = logoText
            )
            repository.updateBranding(b)
        }
    }
}
