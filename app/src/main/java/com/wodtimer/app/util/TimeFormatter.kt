package com.wodtimer.app.util

object TimeFormatter {
    fun formatMillis(millis: Long): String {
        val totalSeconds = millis / 1000
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60

        return if (hours > 0) {
            String.format("%d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format("%d:%02d", minutes, seconds)
        }
    }

    fun formatMillisWithTenths(millis: Long): String {
        val totalSeconds = millis / 1000
        val tenths = (millis % 1000) / 100
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60

        return String.format("%d:%02d.%d", minutes, seconds, tenths)
    }

    fun formatCountdown(seconds: Int): String {
        return when {
            seconds > 60 -> {
                val min = seconds / 60
                val sec = seconds % 60
                String.format("%d:%02d", min, sec)
            }
            seconds > 0 -> seconds.toString()
            else -> "GO!"
        }
    }

    fun secondsToMillis(seconds: Int): Long = seconds * 1000L
    fun minutesToMillis(minutes: Int): Long = minutes * 60000L
}
