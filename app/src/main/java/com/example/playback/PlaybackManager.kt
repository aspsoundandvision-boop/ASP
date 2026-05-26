package com.example.playback

import com.example.data.database.SongEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

object PlaybackManager {
    private val exceptionHandler = kotlinx.coroutines.CoroutineExceptionHandler { _, throwable ->
        android.util.Log.e("PlaybackManager", "Uncaught exception in playback coroutine", throwable)
    }
    private val scope = CoroutineScope(Dispatchers.Main + kotlinx.coroutines.SupervisorJob() + exceptionHandler)
    private val synthAudioPlayer = SynthAudioPlayer()

    // --- State Observables ---
    private val _currentSong = MutableStateFlow<SongEntity?>(null)
    val currentSong = _currentSong.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying = _isPlaying.asStateFlow()

    private val _progress = MutableStateFlow(0f) // 0 to 1
    val progress = _progress.asStateFlow()

    private val _playbackQueue = MutableStateFlow<List<SongEntity>>(emptyList())
    val playbackQueue = _playbackQueue.asStateFlow()

    private val _currentTimeSec = MutableStateFlow(0)
    val currentTimeString = MutableStateFlow("0:00")

    val visualizerFlow = synthAudioPlayer.visualizerFlow

    private var currentQueueIndex = -1
    private var progressJob: Job? = null

    var isMuted: Boolean = false
        set(value) {
            field = value
            synthAudioPlayer.isMuted = value
        }

    fun playIntroJingle() {
        synthAudioPlayer.playIntroJingle()
    }

    fun setQueue(songs: List<SongEntity>, startId: Int = -1) {
        _playbackQueue.value = songs
        if (startId != -1) {
            currentQueueIndex = songs.indexOfFirst { it.id == startId }
            if (currentQueueIndex != -1) {
                playSong(songs[currentQueueIndex])
            }
        }
    }

    fun playSong(song: SongEntity) {
        // Stop currently playing jobs
        progressJob?.cancel()
        synthAudioPlayer.stopPlayback()

        _currentSong.value = song
        _isPlaying.value = true
        _progress.value = 0f
        _currentTimeSec.value = 0
        currentTimeString.value = "0:00"

        // Locate in queue
        val q = _playbackQueue.value
        currentQueueIndex = q.indexOfFirst { it.id == song.id }
        if (currentQueueIndex == -1 && q.isNotEmpty()) {
            // Append to queue
            _playbackQueue.value = q + song
            currentQueueIndex = _playbackQueue.value.lastIndex
        } else if (q.isEmpty()) {
            _playbackQueue.value = listOf(song)
            currentQueueIndex = 0
        }

        // Start synth
        synthAudioPlayer.startSynthPlayback(song.synthPreset)

        // Launch progress ticker job
        startProgressTicker(song)
    }

    fun togglePlayPause() {
        val song = _currentSong.value ?: return
        if (_isPlaying.value) {
            // Pause
            _isPlaying.value = false
            synthAudioPlayer.stopPlayback()
            progressJob?.cancel()
        } else {
            // Resume
            _isPlaying.value = true
            synthAudioPlayer.startSynthPlayback(song.synthPreset)
            startProgressTicker(song)
        }
    }

    fun nextTrack() {
        val queue = _playbackQueue.value
        if (queue.isEmpty()) return

        if (currentQueueIndex != -1 && currentQueueIndex < queue.lastIndex) {
            currentQueueIndex++
            playSong(queue[currentQueueIndex])
        } else {
            // Loop back to start
            currentQueueIndex = 0
            playSong(queue[0])
        }
    }

    fun previousTrack() {
        val queue = _playbackQueue.value
        if (queue.isEmpty()) return

        if (currentQueueIndex > 0) {
            currentQueueIndex--
            playSong(queue[currentQueueIndex])
        } else {
            // Loop to end
            currentQueueIndex = queue.lastIndex
            playSong(queue[queue.lastIndex])
        }
    }

    fun stop() {
        _isPlaying.value = false
        synthAudioPlayer.stopPlayback()
        progressJob?.cancel()
    }

    private fun startProgressTicker(song: SongEntity) {
        progressJob?.cancel()
        progressJob = scope.launch {
            val totalSec = parseDurationToSeconds(song.duration)
            while (_isPlaying.value && _currentTimeSec.value < totalSec) {
                delay(1000)
                _currentTimeSec.value++
                val curTime = _currentTimeSec.value
                _progress.value = curTime.toFloat() / totalSec
                currentTimeString.value = formatSeconds(curTime)

                if (curTime >= totalSec) {
                    // Song completed, trigger next track automatically!
                    nextTrack()
                    break
                }
            }
        }
    }

    private fun parseDurationToSeconds(duration: String): Int {
        return try {
            val parts = duration.split(":")
            if (parts.size == 2) {
                parts[0].toInt() * 60 + parts[1].toInt()
            } else {
                180 // Fallback 3 minutes
            }
        } catch (e: Exception) {
            180
        }
    }

    private fun formatSeconds(seconds: Int): String {
        val m = seconds / 60
        val s = seconds % 60
        return String.format("%d:%02d", m, s)
    }
}
