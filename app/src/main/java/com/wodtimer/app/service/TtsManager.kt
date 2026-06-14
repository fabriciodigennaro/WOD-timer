package com.wodtimer.app.service

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import com.wodtimer.app.domain.model.AppSettings
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TtsManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var tts: TextToSpeech? = null
    private var isInitialized = false
    private var settings: AppSettings = AppSettings()
    private val pendingUtterances = mutableListOf<String>()

    init {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.US
                tts?.setSpeechRate(1.1f)
                isInitialized = true
                pendingUtterances.forEach { speakInternal(it) }
                pendingUtterances.clear()
            }
        }
    }

    fun updateSettings(newSettings: AppSettings) {
        settings = newSettings
    }

    fun speak(text: String) {
        if (!settings.ttsEnabled) return
        if (isInitialized) {
            speakInternal(text)
        } else {
            pendingUtterances.add(text)
        }
    }

    private fun speakInternal(text: String) {
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, text)
    }

    fun sayCountdown(seconds: Int) {
        when (seconds) {
            3 -> speak("3")
            2 -> speak("2")
            1 -> speak("1")
            0 -> speak("Go")
        }
    }

    fun sayRest() = speak("Rest")
    fun sayLastRound() = speak("Last round")
    fun sayFinished() = speak("Workout complete")

    fun release() {
        tts?.stop()
        tts?.shutdown()
        tts = null
    }
}
