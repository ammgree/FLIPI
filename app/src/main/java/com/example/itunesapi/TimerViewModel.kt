package com.example.itunesapi

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class TimerViewModel : ViewModel() {

    private val _focusTimers = MutableLiveData<MutableMap<String, Int>>(mutableMapOf())
    val focusTimers: LiveData<MutableMap<String, Int>> get() = _focusTimers

    fun updateTime(topic: String, seconds: Int) {
        val current = _focusTimers.value ?: mutableMapOf()
        current[topic] = (current[topic] ?: 0) + seconds
        _focusTimers.value = current
    }

    fun addTopic(topic: String) {
        val current = _focusTimers.value ?: mutableMapOf()
        current[topic] = 0
        _focusTimers.value = current
    }

    fun getTime(topic: String): Int {
        return _focusTimers.value?.get(topic) ?: 0
    }

    // SharedPreferences 저장
    fun saveTimersToPrefs(context: Context) {
        _focusTimers.value?.let { TimerRepository.saveTimers(context, it) }
    }

    // SharedPreferences 복원
    fun loadTimersFromPrefs(context: Context) {
        _focusTimers.value = TimerRepository.loadTimers(context)

    }

}

