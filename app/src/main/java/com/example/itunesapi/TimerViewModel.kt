package com.example.itunesapi

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class TimerViewModel : ViewModel() {
    private val _focusTimers = MutableLiveData<MutableMap<String, Int>>(mutableMapOf())
    val focusTimers: LiveData<MutableMap<String, Int>> get() = _focusTimers

    fun updateTime(topic: String, minutes: Int) {
        val current = _focusTimers.value ?: mutableMapOf()
        current[topic] = (current[topic] ?: 0) + minutes
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
}
