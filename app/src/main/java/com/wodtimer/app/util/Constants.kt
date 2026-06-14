package com.wodtimer.app.util

object Constants {
    const val APP_NAME = "WODTimer"
    const val PREPARE_COUNTDOWN_DEFAULT = 10
    const val MINIMUM_SDK = 29

    // Timer default values
    const val TABATA_WORK_DEFAULT = 20
    const val TABATA_REST_DEFAULT = 10
    const val TABATA_ROUNDS_DEFAULT = 8
    const val EMOM_INTERVAL_DEFAULT = 60
    const val EMOM_ROUNDS_DEFAULT = 10
    const val AMRAP_DEFAULT_MINUTES = 10
    const val INTERVAL_WORK_DEFAULT = 45
    const val INTERVAL_REST_DEFAULT = 15
    const val INTERVAL_SETS_DEFAULT = 3

    // Intent actions
    const val ACTION_TIMER_UPDATE = "com.wodtimer.app.TIMER_UPDATE"
    const val ACTION_TIMER_FINISHED = "com.wodtimer.app.TIMER_FINISHED"
    const val ACTION_PHASE_CHANGE = "com.wodtimer.app.PHASE_CHANGE"
}
