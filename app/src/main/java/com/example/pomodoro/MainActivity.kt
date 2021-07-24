package com.example.pomodoro

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.pomodoro.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(),StopwatchListener,LifecycleObserver {

    private lateinit var binding: ActivityMainBinding
    private val stopwatchAdapter = StopwatchAdapter(this)
    private val stopwatches = mutableListOf<Stopwatch>()
    private var nextId = 0
    private var activeTimer=-1


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.recycler.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = stopwatchAdapter
        }


        binding.addNewStopwatchButton.setOnClickListener {
            val startTime= binding.editTime.text.toString().toLongOrNull()?.times(60000) ?: 0
            stopwatches.add(Stopwatch(nextId++, startTime, currentTime = startTime))
            stopwatchAdapter.submitList(stopwatches.toList())
        }
    }

    override fun start(id: Int) {
        activeTimer=id
        changeStopwatch(id, true, null)
    }

    override fun stop(id: Int, currentMs: Long,isFinish:Boolean?) {
        if(id==activeTimer)
            activeTimer=-1
        changeStopwatch(id, false, isFinish)
    }


    override fun delete(id: Int) {
        if(id==activeTimer)
            activeTimer=-1
        stopwatches.remove(stopwatches.find { it.id == id })
        stopwatchAdapter.submitList(stopwatches.toList())
    }

    private fun changeStopwatch(id: Int, isStarted: Boolean, isFinished: Boolean?) {
        val newTimers = mutableListOf<Stopwatch>()
        stopwatches.forEach {
            if (it.id == id) {
                newTimers.add(Stopwatch(it.id, it.startTime, it.currentTime, isStarted, it.timer, isFinished
                    ?:it.isFinished))
            }
            else if (it.isStarted){
                //если запущен, останавливаем
            it.timer?.cancel()
                newTimers.add(Stopwatch(it.id, it.startTime, it.currentTime, false, it.timer,it.isFinished))
            }
            else {
                newTimers.add(it)
            }
        }
        stopwatchAdapter.submitList(newTimers)
        stopwatches.clear()
        stopwatches.addAll(newTimers)
    }


    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onAppBackgrounded() {
        val startTime = stopwatchAdapter.currentList.find { it.id == activeTimer }?.currentTime ?: -1
        if(startTime>0) {
            val startIntent = Intent(this, ForegroundService::class.java)
            startIntent.putExtra(COMMAND_ID, COMMAND_START)
            startIntent.putExtra(STARTED_TIMER_TIME_MS, startTime)
            startService(startIntent)
        }
    }
    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onAppForegrounded() {
        val stopIntent = Intent(this, ForegroundService::class.java)
        stopIntent.putExtra(COMMAND_ID, COMMAND_STOP)
        startService(stopIntent)
    }

}