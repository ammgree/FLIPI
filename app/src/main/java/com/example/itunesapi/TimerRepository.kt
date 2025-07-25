package com.example.itunesapi

import android.content.Context
import org.json.JSONObject

object TimerRepository {
    private const val PREF_NAME = "timer_prefs"
    private const val KEY_TIMERS = "focus_timers"

    fun saveTimers(context: Context, map: Map<String, Int>) {
        val json = JSONObject(map).toString()
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_TIMERS, json)
            .apply()
    }

    fun loadTimers(context: Context): MutableMap<String, Int> {
        val jsonStr = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getString(KEY_TIMERS, null) ?: return mutableMapOf()
        val json = JSONObject(jsonStr)
        val map = mutableMapOf<String, Int>()
        for (key in json.keys()) {
            map[key] = json.getInt(key)
        }
        return map
    }
}
