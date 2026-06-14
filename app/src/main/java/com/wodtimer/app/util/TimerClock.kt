package com.wodtimer.app.util

import android.os.SystemClock

interface TimerClock {
    fun elapsedRealtime(): Long
}

class RealTimerClock : TimerClock {
    override fun elapsedRealtime(): Long = SystemClock.elapsedRealtime()
}