package com.example.pomodoro.stopwatch

import android.content.res.Resources
import android.graphics.drawable.AnimationDrawable
import android.os.CountDownTimer
import androidx.core.view.isInvisible
import androidx.recyclerview.widget.RecyclerView
import com.example.pomodoro.R
import com.example.pomodoro.TEN_MS
import com.example.pomodoro.databinding.StopwatchItemBinding
import com.example.pomodoro.displayTime

class StopwatchViewHolder(
    private val binding: StopwatchItemBinding,
    private val listener: StopwatchListener,
    private val resources: Resources
) : RecyclerView.ViewHolder(binding.root) {

    private var timer: CountDownTimer? = null

    fun bind(stopwatch: Stopwatch) {
        binding.stopwatchTimer.text = stopwatch.currentTime.displayTime()
        if(stopwatch.isStarted) setIsRecyclable(false)
        else setIsRecyclable(true)
        binding.progressView.setPeriod(stopwatch.startTime)
        binding.progressView.setCurrent(stopwatch.currentTime)
        if (!stopwatch.isFinished)
            binding.root.setBackgroundColor(resources.getColor(R.color.white))
        else
            binding.root.setBackgroundColor(resources.getColor(R.color.red))
        if (stopwatch.isStarted)
            startTimer(stopwatch)
        else
            pauseTimer()

        initButtonsListeners(stopwatch)
    }

    private fun initButtonsListeners(stopwatch: Stopwatch) {
        binding.startPauseButton.setOnClickListener {
            if (stopwatch.isStarted) {
                listener.stop(stopwatch.id, stopwatch.currentTime,null)
            } else {
                listener.start(stopwatch.id)
            }
        }

        binding.deleteButton.setOnClickListener {
            setIsRecyclable(true)
            listener.delete(stopwatch.id)
        }
    }

    private fun startTimer(stopwatch: Stopwatch) {
        binding.startPauseButton.text="STOP"

        timer?.cancel()
        timer = getCountDownTimer(stopwatch)
        stopwatch.timer =timer
        timer?.start()

        binding.blinkingIndicator.isInvisible = false
        (binding.blinkingIndicator.background as? AnimationDrawable)?.start()
        binding.progressView.setPeriod(stopwatch.startTime)
        stopwatch.isFinished=false
    }

    private fun pauseTimer() {
        binding.startPauseButton.text="START"
        timer?.cancel()
        binding.blinkingIndicator.isInvisible = true
        (binding.blinkingIndicator.background as? AnimationDrawable)?.stop()
    }

    private fun getCountDownTimer(stopwatch: Stopwatch): CountDownTimer {
        return object : CountDownTimer(stopwatch.currentTime, TEN_MS) {

            override fun onTick(millisUntilFinished: Long) {
                stopwatch.currentTime = millisUntilFinished
                binding.stopwatchTimer.text = stopwatch.currentTime.displayTime()
                binding.progressView.setCurrent(stopwatch.currentTime)
            }

            override fun onFinish() {
                pauseTimer()
                binding.stopwatchTimer.text = stopwatch.currentTime.displayTime()
                binding.root.setBackgroundColor(resources.getColor(R.color.red))
                stopwatch.isFinished=true
                listener.stop(stopwatch.id, stopwatch.currentTime,true)
            }
        }
    }

}