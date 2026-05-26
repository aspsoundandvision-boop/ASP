package com.example.playback

import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Random
import kotlin.math.PI
import kotlin.math.sin

class SynthAudioPlayer {
    private val TAG = "SynthAudioPlayer"
    private var audioTrack: AudioTrack? = null
    private var synthesisJob: Job? = null
    private val sampleRate = 22050
    
    private val exceptionHandler = kotlinx.coroutines.CoroutineExceptionHandler { _, throwable ->
        Log.e(TAG, "Uncaught exception in sound synthesis coroutine", throwable)
    }
    private val coroutineScope = CoroutineScope(Dispatchers.Default + kotlinx.coroutines.SupervisorJob() + exceptionHandler)

    // Current live analyzer values for the audio visualizer (16 bands)
    private val _visualizerFlow = MutableStateFlow(FloatArray(16) { 0.05f })
    val visualizerFlow = _visualizerFlow.asStateFlow()

    // Flag for global mute
    var isMuted: Boolean = false
        set(value) {
            field = value
            try {
                if (value) {
                    audioTrack?.setVolume(0f)
                } else {
                    audioTrack?.setVolume(1f)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error setting mute state", e)
            }
        }

    /**
     * Synthesize and play a short 2.5s introductory jingle
     * Synchronized with the splash screen visual glow.
     */
    fun playIntroJingle() {
        if (synthesisJob?.isActive == true) return

        synthesisJob = coroutineScope.launch {
            try {
                val durationSamples = (sampleRate * 2.5).toInt()
                val minBufferSize = AudioTrack.getMinBufferSize(
                    sampleRate,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT
                )
                val bufferSize = maxOf(minBufferSize, 4096)

                val track = AudioTrack(
                    AudioManager.STREAM_MUSIC,
                    sampleRate,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    bufferSize,
                    AudioTrack.MODE_STREAM
                )
                audioTrack = track
                if (isMuted) track.setVolume(0f) else track.setVolume(1f)
                track.play()

                val buffer = ShortArray(1024)
                var sampleIndex = 0

                // A beautiful C Major 9 chord sweep: C3 (130.81Hz), E3 (164.81Hz), G3 (196.00Hz), B3 (246.94Hz), D4 (293.66Hz)
                val freqs = doubleArrayOf(130.81, 164.81, 196.00, 246.94, 293.66)

                while (sampleIndex < durationSamples && isActive) {
                    val remaining = durationSamples - sampleIndex
                    val chunkSize = minOf(buffer.size, remaining)

                    for (i in 0 until chunkSize) {
                        val t = (sampleIndex + i).toDouble() / sampleRate
                        
                        // Volume envelope (fade in first 0.5s, fade out last 1.0s)
                        var envelope = 1.0
                        if (t < 0.5) {
                            envelope = t / 0.5
                        } else if (t > 1.5) {
                            envelope = maxOf(0.0, 1.0 - (t - 1.5) / 1.0)
                        }

                        // Combine frequencies
                        var sum = 0.0
                        for (fIdx in freqs.indices) {
                            val freq = freqs[fIdx]
                            // Arpeggiated entry: sweep frequency entries over different t offsets
                            val entryT = fIdx * 0.15
                            if (t >= entryT) {
                                // Add sine wave
                                sum += sin(2.0 * PI * freq * t) * 0.2
                            }
                        }

                        val sampleValue = (sum * envelope * Short.MAX_VALUE).toInt()
                        buffer[i] = sampleValue.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                    }

                    track.write(buffer, 0, chunkSize)
                    sampleIndex += chunkSize

                    // Feed a beautiful symmetric waveshape into visualizer
                    val currentT = sampleIndex.toDouble() / sampleRate
                    var env = 1.0
                    if (currentT < 0.5) {
                        env = currentT / 0.5
                    } else if (currentT > 1.5) {
                        env = maxOf(0.0, 1.0 - (currentT - 1.5) / 1.0)
                    }

                    val visualizerData = FloatArray(16) { idx ->
                        val amp = sin(sampleIndex.toDouble() * 0.001 + idx * 0.2).toFloat() * 0.4f + 0.5f
                        val envAdj = (amp * env).toFloat().coerceIn(0.05f, 1.0f)
                        envAdj
                    }
                    _visualizerFlow.value = visualizerData
                }

                track.stop()
                track.release()
            } catch (e: Exception) {
                Log.e(TAG, "Error in jingle playing", e)
            } finally {
                audioTrack = null
                _visualizerFlow.value = FloatArray(16) { 0.05f }
            }
        }
    }

    /**
     * Start loop continuous playback based on preset synth styles
     */
    fun startSynthPlayback(presetName: String) {
        stopPlayback()

        synthesisJob = coroutineScope.launch {
            try {
                val minBufferSize = AudioTrack.getMinBufferSize(
                    sampleRate,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT
                )
                val bufferSize = maxOf(minBufferSize, 4096)

                val track = AudioTrack(
                    AudioManager.STREAM_MUSIC,
                    sampleRate,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    bufferSize,
                    AudioTrack.MODE_STREAM
                )
                audioTrack = track
                if (isMuted) track.setVolume(0f) else track.setVolume(1f)
                track.play()

                val buffer = ShortArray(1024)
                var globalSampleIndex = 0L
                val rand = Random()

                // Melody variables
                var lastNoteChange = 0L
                val noteDurationSamples = (sampleRate * 0.4).toLong() // 400ms steps
                var currentFreqIndex = 0

                // Ambient scales (Major / Minor Lydian / Aeolian)
                val ambientScale = doubleArrayOf(130.81, 146.83, 164.81, 196.00, 220.00, 246.94, 293.66, 329.63, 392.00) // C Maj
                val neonScale = doubleArrayOf(110.00, 130.81, 146.83, 164.81, 196.00, 220.00, 261.63, 293.66, 329.63) // A Min

                while (isActive) {
                    val scale = if (presetName == "NeonPulse") neonScale else ambientScale

                    for (i in buffer.indices) {
                        val currentSample = globalSampleIndex + i
                        val t = currentSample.toDouble() / sampleRate

                        // Chord scale arpeggiator logic for rhythmic presets
                        if (presetName == "NeonPulse" || presetName == "MutedRain") {
                            if (currentSample - lastNoteChange > noteDurationSamples) {
                                currentFreqIndex = rand.nextInt(scale.size)
                                lastNoteChange = currentSample
                            }
                        }

                        val sampleValue = when (presetName) {
                            "Cosmonaut" -> {
                                // Beautiful sweeping warm giant pads (Sine + Triangle modulation)
                                // slow LFO modulating frequencies
                                val lfo1 = sin(2.0 * PI * 0.05 * t) * 0.5 + 0.5 // 0 to 1 over 20s
                                val lfo2 = sin(2.0 * PI * 0.08 * t) * 0.5 + 0.5

                                val f1 = 130.81 // C3
                                val f2 = 196.00 // G3
                                val f3 = 246.94 // B3
                                val f4 = 329.63 * (1.0 + lfo1 * 0.02) // Sweeping E4

                                val v1 = sin(2.0 * PI * f1 * t) * 0.3
                                val v2 = sin(2.0 * PI * f2 * t) * 0.25
                                val v3 = sin(2.0 * PI * f3 * t) * 0.2
                                val v4 = sin(2.0 * PI * f4 * t) * 0.15 * lfo2

                                val rawSig = v1 + v2 + v3 + v4
                                (rawSig * Short.MAX_VALUE * 0.7).toInt()
                            }
                            "NeonPulse" -> {
                                // Energetic synthwave sawtooth + heavy pulsing rhythm
                                val baseFreq = scale[currentFreqIndex % scale.size]
                                val subFreq = baseFreq / 2.0 // Deep sub base

                                // Square/saw wave for the lead, modulated by rhythmic gate
                                val gate = if ((t * 4).toInt() % 2 == 0) 1.0 else 0.2
                                val pulseVal = if ((t * 220.0) % 1.0 > 0.5) 0.15 else -0.15

                                // Sawtooth base synthesizer
                                val sawVal = ((t * baseFreq) % 1.0) * 2.0 - 1.0

                                val rawSig = (sawVal * 0.25 * gate) + (sin(2.0 * PI * subFreq * t) * 0.45)
                                (rawSig * Short.MAX_VALUE * 0.6).toInt()
                            }
                            "DeepVoyage" -> {
                                // Immersive submarine oceanic sweeps
                                val f1 = 55.0  // A1
                                val f2 = 82.41 // E2
                                val f3 = 110.0 // A2
                                val lfo = sin(2.0 * PI * 0.02 * t) * 0.5 + 0.5

                                val v1 = sin(2.0 * PI * f1 * t) * 0.4
                                val v2 = sin(2.0 * PI * f2 * t) * 0.3 * lfo
                                val v3 = sin(2.0 * PI * f3 * t) * 0.2 * (1.0 - lfo)

                                val rawSig = v1 + v2 + v3
                                (rawSig * Short.MAX_VALUE * 0.75).toInt()
                            }
                            "MutedRain" -> {
                                // Beautiful soft chimes over rain noise
                                val noise = (rand.nextDouble() * 2.0 - 1.0) * 0.12 // White noise (rain)
                                val bandpassNoise = sin(2.0 * PI * 180.0 * t) * noise // Simple low pass mold

                                // Cozy bell tones playing arpeggios fading out
                                val pitch = scale[currentFreqIndex % scale.size] * 1.5
                                val chimeAge = ((currentSample - lastNoteChange) / sampleRate.toDouble())
                                val chimeEnvelope = maxOf(0.0, 1.0 - chimeAge / 0.4) // Decay over 400ms

                                val chime = sin(2.0 * PI * pitch * t) * chimeEnvelope * 0.22

                                val rawSig = bandpassNoise + chime
                                (rawSig * Short.MAX_VALUE * 0.65).toInt()
                            }
                            else -> {
                                // Fallback empty
                                0
                            }
                        }

                        buffer[i] = sampleValue.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                    }

                    track.write(buffer, 0, buffer.size)
                    globalSampleIndex += buffer.size

                    // Compute real analyzer frequencies to push to visualizer StateFlow
                    // Let's create varying visualizer ranges based on the currently running preset!
                    val visualizerData = FloatArray(16) { idx ->
                        when (presetName) {
                            "Cosmonaut" -> {
                                val base = sin(globalSampleIndex.toDouble() * 0.0006 + idx * 0.26).toFloat() * 0.3f + 0.4f
                                val noise = (rand.nextFloat() * 0.1f - 0.05f)
                                (base + noise).coerceIn(0.1f, 0.9f)
                            }
                            "NeonPulse" -> {
                                // Fast reactive jumping levels
                                val pulsePhase = (globalSampleIndex / (sampleRate * 0.25).toLong()) % 2
                                val spike = if (pulsePhase == 0L) 0.8f else 0.2f
                                val base = sin(globalSampleIndex.toDouble() * 0.002 + idx * 0.4).toFloat() * 0.2f + 0.3f
                                val finalF = if (idx in 2..7) base + spike * 0.35f else base
                                finalF.coerceIn(0.12f, 0.98f)
                            }
                            "DeepVoyage" -> {
                                // Smooth low frequency swells
                                val sweepPhase = sin(globalSampleIndex.toDouble() * 0.0001).toFloat() * 0.5f + 0.5f
                                val height = if (idx < 6) 0.7f - (idx * 0.08f) + sweepPhase * 0.25f else 0.1f
                                height.coerceIn(0.08f, 0.92f)
                            }
                            "MutedRain" -> {
                                // High frequencies ticking slightly (chimes), subtle rumble base (rain)
                                val baseVal = 0.2f + rand.nextFloat() * 0.15f
                                val chimePulse = currentFreqIndex % 16
                                val finalHeight = if (idx == chimePulse) baseVal + 0.5f else baseVal
                                finalHeight.coerceIn(0.05f, 0.95f)
                            }
                            else -> 0.1f
                        }
                    }
                    _visualizerFlow.value = visualizerData
                }

                track.stop()
                track.release()
            } catch (e: Exception) {
                Log.e(TAG, "Exception in synth player", e)
            } finally {
                _visualizerFlow.value = FloatArray(16) { 0.05f }
            }
        }
    }

    /**
     * Terminate currently playing synthesizer
     */
    fun stopPlayback() {
        synthesisJob?.cancel()
        synthesisJob = null
        try {
            audioTrack?.stop()
            audioTrack?.release()
        } catch (e: Exception) {
            // Safe ignore
        }
        audioTrack = null
        _visualizerFlow.value = FloatArray(16) { 0.05f }
    }
}
