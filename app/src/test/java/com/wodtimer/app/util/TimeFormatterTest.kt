package com.wodtimer.app.util

import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class TimeFormatterFormatMillisTest(
    private val millis: Long,
    private val expected: String
) {
    companion object {
        @JvmStatic
        @Parameterized.Parameters
        fun data(): Collection<Array<Any>> = listOf(
            arrayOf<Any>(0L, "0:00"),
            arrayOf<Any>(1000L, "0:01"),
            arrayOf<Any>(59000L, "0:59"),
            arrayOf<Any>(60000L, "1:00"),
            arrayOf<Any>(3599000L, "59:59"),
            arrayOf<Any>(3600000L, "1:00:00"),
            arrayOf<Any>(3601000L, "1:00:01"),
            arrayOf<Any>(3661000L, "1:01:01"),
            arrayOf<Any>(7200000L, "2:00:00"),
            arrayOf<Any>(86399000L, "23:59:59"),
            arrayOf<Any>(86400000L, "24:00:00"),
        )
    }

    @Test
    fun testFormatMillis() {
        assertEquals(expected, TimeFormatter.formatMillis(millis))
    }
}

@RunWith(Parameterized::class)
class TimeFormatterFormatMillisWithTenthsTest(
    private val millis: Long,
    private val expected: String
) {
    companion object {
        @JvmStatic
        @Parameterized.Parameters
        fun data(): Collection<Array<Any>> = listOf(
            arrayOf<Any>(0L, "0:00.0"),
            arrayOf<Any>(99L, "0:00.0"),
            arrayOf<Any>(100L, "0:00.1"),
            arrayOf<Any>(999L, "0:00.9"),
            arrayOf<Any>(1000L, "0:01.0"),
            arrayOf<Any>(1100L, "0:01.1"),
            arrayOf<Any>(59000L, "0:59.0"),
            arrayOf<Any>(60000L, "1:00.0"),
            arrayOf<Any>(61000L, "1:01.0"),
            arrayOf<Any>(61100L, "1:01.1"),
            arrayOf<Any>(3599000L, "59:59.0"),
            arrayOf<Any>(3600000L, "60:00.0"),
            arrayOf<Any>(3600100L, "60:00.1"),
        )
    }

    @Test
    fun testFormatMillisWithTenths() {
        assertEquals(expected, TimeFormatter.formatMillisWithTenths(millis))
    }
}

@RunWith(Parameterized::class)
class TimeFormatterFormatCountdownTest(
    private val seconds: Int,
    private val expected: String
) {
    companion object {
        @JvmStatic
        @Parameterized.Parameters
        fun data(): Collection<Array<Any>> = listOf(
            arrayOf<Any>(-1, "GO!"),
            arrayOf<Any>(0, "GO!"),
            arrayOf<Any>(1, "1"),
            arrayOf<Any>(59, "59"),
            arrayOf<Any>(60, "60"),
            arrayOf<Any>(61, "1:01"),
            arrayOf<Any>(120, "2:00"),
            arrayOf<Any>(3599, "59:59"),
            arrayOf<Any>(3600, "60:00"),
        )
    }

    @Test
    fun testFormatCountdown() {
        assertEquals(expected, TimeFormatter.formatCountdown(seconds))
    }
}

@RunWith(Parameterized::class)
class TimeFormatterSecondsToMillisTest(
    private val seconds: Int,
    private val expected: Long
) {
    companion object {
        @JvmStatic
        @Parameterized.Parameters
        fun data(): Collection<Array<Any>> = listOf(
            arrayOf<Any>(0, 0L),
            arrayOf<Any>(1, 1000L),
            arrayOf<Any>(-1, -1000L),
        )
    }

    @Test
    fun testSecondsToMillis() {
        assertEquals(expected, TimeFormatter.secondsToMillis(seconds))
    }
}

@RunWith(Parameterized::class)
class TimeFormatterMinutesToMillisTest(
    private val minutes: Int,
    private val expected: Long
) {
    companion object {
        @JvmStatic
        @Parameterized.Parameters
        fun data(): Collection<Array<Any>> = listOf(
            arrayOf<Any>(0, 0L),
            arrayOf<Any>(1, 60000L),
            arrayOf<Any>(-1, -60000L),
        )
    }

    @Test
    fun testMinutesToMillis() {
        assertEquals(expected, TimeFormatter.minutesToMillis(minutes))
    }
}
