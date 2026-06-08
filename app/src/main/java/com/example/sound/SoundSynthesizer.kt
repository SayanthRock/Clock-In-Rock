package com.example.sound

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.sin

class SoundSynthesizer {
    private var audioTrack: AudioTrack? = null
    private var synthJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default)

    // Current operating parameters (can be dynamically altered while playing)
    @Volatile var frequency: Float = 600f
    @Volatile var waveType: String = "SINE" // SINE, SQUARE, TRIANGLE, SAWTOOTH
    @Volatile var pulseSpeedMs: Long = 400L // Duration of pulse cycle (milliseconds)
    @Volatile var vibratoDepth: Float = 0.15f // Depth of LFO frequency modulation
    @Volatile var vibratoSpeed: Float = 6f // Frequency of LFO (Hz)
    @Volatile var isVibrationEnabled: Boolean = true
    @Volatile var volumeMultiplier: Float = 0.5f

    @Volatile var fadeStartTimeMs: Long = 0L
    @Volatile var fadeInDurationMs: Long = 0L

    @Volatile var isRunning: Boolean = false
        private set

    fun start(
        freq: Float = 600f,
        wave: String = "SINE",
        pulseSpeed: Long = 400L,
        vibDepth: Float = 0.15f,
        vibSpeed: Float = 6.0f,
        volume: Float = 0.5f,
        fadeInDuration: Long = 0L
    ) {
        if (isRunning) {
            // If already running, dynamic adjustment is carried out by updating fields
            frequency = freq
            waveType = wave
            pulseSpeedMs = pulseSpeed
            vibratoDepth = vibDepth
            vibratoSpeed = vibSpeed
            volumeMultiplier = volume
            fadeInDurationMs = fadeInDuration
            fadeStartTimeMs = System.currentTimeMillis()
            return
        }

        frequency = freq
        waveType = wave
        pulseSpeedMs = pulseSpeed
        vibratoDepth = vibDepth
        vibratoSpeed = vibSpeed
        volumeMultiplier = volume
        fadeInDurationMs = fadeInDuration
        fadeStartTimeMs = System.currentTimeMillis()
        isRunning = true

        synthJob = scope.launch {
            val sampleRate = 22050
            val bufferSize = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )

            try {
                audioTrack = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ALARM)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(bufferSize)
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .build()

                audioTrack?.play()
            } catch (e: Exception) {
                Log.e("SoundSynthesizer", "Error creating or starting AudioTrack", e)
                isRunning = false
                return@launch
            }

            var phase = 0f
            var lfoPhase = 0f
            val sampleCount = 512
            val buffer = ShortArray(sampleCount)

            while (isActive && isRunning) {
                val timeMs = System.currentTimeMillis()
                // Alarm pattern: Beep for pulseSpeedMs, then silence for pulseSpeedMs
                val speed = pulseSpeedMs.coerceAtLeast(50L)
                val inBeepPhase = (timeMs % (speed * 2)) < speed

                if (inBeepPhase) {
                    for (i in 0 until sampleCount) {
                        // 1. Calculate LFO state for Vibrato
                        val lfoVal = sin(2 * Math.PI * lfoPhase)
                        lfoPhase += vibratoSpeed / sampleRate
                        if (lfoPhase > 1f) lfoPhase -= 1f

                        // 2. Modulate frequency
                        val modulatedFrequency = frequency + (frequency * vibratoDepth * lfoVal).toFloat()

                        // 3. Generate wave shape based on phase
                        val angle = 2 * Math.PI * modulatedFrequency / sampleRate
                        phase += 1f
                        if (phase > sampleRate) phase -= sampleRate

                        val t = (phase * angle) % (2 * Math.PI)
                        val rawSample = when (waveType.uppercase()) {
                            "SQUARE" -> if (sin(t) >= 0) 1.0 else -1.0
                            "TRIANGLE" -> {
                                val normalizedVal = t / (2 * Math.PI)
                                if (normalizedVal < 0.5) {
                                    4 * normalizedVal - 1
                                } else {
                                    3 - 4 * normalizedVal
                                }
                            }
                            "SAWTOOTH" -> {
                                val normalizedVal = t / (2 * Math.PI)
                                2 * normalizedVal - 1
                            }
                            else -> sin(t) // SINE as default
                        }

                        // Apply fade in logic
                        val fadeMultiplier = if (fadeInDurationMs > 0L) {
                            val elapsedTime = timeMs - fadeStartTimeMs
                            (elapsedTime.toFloat() / fadeInDurationMs.toFloat()).coerceIn(0f, 1f)
                        } else {
                            1f
                        }

                        // Scale to short size and apply volume limit multiplier
                        val currentVolume = volumeMultiplier.coerceIn(0f, 1f) * fadeMultiplier
                        val amplitude = (rawSample * 32767 * currentVolume).toInt()
                        buffer[i] = amplitude.coerceIn(-32768, 32767).toShort()
                    }
                    audioTrack?.write(buffer, 0, sampleCount)
                } else {
                    // Output silence phase smoothly to prevent clipping or popping clicks
                    for (i in 0 until sampleCount) {
                        buffer[i] = 0
                    }
                    audioTrack?.write(buffer, 0, sampleCount)
                    delay(15L) // yields thread to conserve processor energy during silence
                }
            }
        }
    }

    fun stop() {
        isRunning = false
        synthJob?.cancel()
        synthJob = null
        try {
            audioTrack?.stop()
            audioTrack?.release()
        } catch (e: Exception) {
            Log.e("SoundSynthesizer", "Error stopping AudioTrack", e)
        }
        audioTrack = null
    }

    fun playPreview(
        freq: Float = 600f,
        wave: String = "SINE",
        pulseSpeed: Long = 400L,
        vibDepth: Float = 0.15f,
        vibSpeed: Float = 6.0f,
        durationMs: Long = 1200L,
        volume: Float = 0.5f,
        onDone: () -> Unit = {}
    ) {
        scope.launch {
            stop() // Stop any current sound
            delay(100L) // Wait a brief instant for cleanup safety
            start(freq, wave, pulseSpeed, vibDepth, vibSpeed, volume)
            delay(durationMs)
            stop()
            onDone()
        }
    }
}

data class SoundPresetData(
    val name: String,
    val wave: String,
    val freq: Float,
    val pulse: Long,
    val vibDepth: Float,
    val vibSpeed: Float
)

