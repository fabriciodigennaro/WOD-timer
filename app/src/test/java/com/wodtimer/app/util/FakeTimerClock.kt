package com.wodtimer.app.util

class FakeTimerClock : TimerClock {
    private var currentTime: Long = 0L

    override fun elapsedRealtime(): Long = currentTime

    fun advance(millis: Long) {
        currentTime += millis
    }

    fun setTime(millis: Long) {
        currentTime = millis
    }

    fun reset() {
        currentTime = 0L
    }
}