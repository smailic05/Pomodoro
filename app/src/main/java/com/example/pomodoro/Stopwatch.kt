package com.example.pomodoro

import android.os.CountDownTimer

data class Stopwatch(
    val id: Int,
    val startTime: Long,
    var currentTime: Long,
    var isStarted: Boolean = false,
    var timer: CountDownTimer? = null,
    var isFinished:Boolean=false
)
