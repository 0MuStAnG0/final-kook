package com.example.utils

import android.content.Context
import android.content.SharedPreferences
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DailyRewardManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("daily_reward_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_LAST_REWARD_DATE = "last_daily_reward_date"
        private const val KEY_LAST_REWARD_TIMESTAMP = "last_daily_reward_timestamp"
        var sessionShownToday: Boolean = false
    }

    private fun getTodayDateString(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        return sdf.format(Date())
    }

    fun shouldShowDailyReward(): Boolean {
        if (sessionShownToday) return false
        val lastDate = prefs.getString(KEY_LAST_REWARD_DATE, null)
        val today = getTodayDateString()
        val lastTimestamp = prefs.getLong(KEY_LAST_REWARD_TIMESTAMP, 0L)
        val now = System.currentTimeMillis()
        val hoursPassed = (now - lastTimestamp) / (1000 * 60 * 60)
        
        if (lastDate == today || (lastTimestamp > 0 && hoursPassed < 24)) {
            sessionShownToday = true
            return false
        }
        return true
    }

    fun markRewardAsSeenToday() {
        sessionShownToday = true
        val today = getTodayDateString()
        prefs.edit()
            .putString(KEY_LAST_REWARD_DATE, today)
            .putLong(KEY_LAST_REWARD_TIMESTAMP, System.currentTimeMillis())
            .apply()
    }
}
