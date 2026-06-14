package com.wodtimer.app.service

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import com.wodtimer.app.domain.model.AppSettings
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.PI
import kotlin.math.sin

@Singleton
class SoundManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var settings: AppSettings = AppSettings()
    private var cachedTracks = mutableListOf<AudioTrack>()
    private val sampleRate = 44100

    fun updateSettings(newSettings: AppSettings) {
        settings = newSettings
    }

    fun playBeep() {
        if (!settings.soundEnabled) return
        playTone(880.0, 150)
    }

    fun playFinalBeep() {
        if (!settings.soundEnabled) return
        playTone(660.0, 500)
        Thread.sleep(200)
        playTone(880.0, 800)
    }

    fun playRestBeep() {
        if (!settings.soundEnabled) return
        playTone(440.0, 200)
    }

    fun playGoBeep() {
        if (!settings.soundEnabled) return
        playTone(1047.0, 300)
    }

    private fun playTone(freq: Double, durationMs: Int) {
        try {
            val numSamples = sampleRate * durationMs / 1000
            val samples = ShortArray(numSamples)
            for (i in 0 until numSamples) {
                val sample = (sin(2.0 * PI * i / (sampleRate / freq)) * Short.MAX_VALUE).toInt()
                samples[i] = (sample.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())).toShort()
            }

            val audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(numSamples * 2)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            audioTrack.write(samples, 0, numSamples)
            audioTrack.setVolume(settings.soundVolume)
            audioTrack.play()

            cachedTracks.add(audioTrack)

            audioTrack.setNotificationMarkerPosition(numSamples - 1)
            audioTrack.setPlaybackPositionUpdateListener(object : AudioTrack.OnPlaybackPositionUpdateListener {
                override fun onMarkerReached(track: AudioTrack) {
                    track.release()
                    cachedTracks.remove(track)
                }
                override fun onPeriodicNotification(track: AudioTrack) {}
            })
        } catch (e: Exception) {
            // fallback silently
        }
    }

    fun release() {
        cachedTracks.forEach { it.release() }
        cachedTracks.clear()
    }
}
