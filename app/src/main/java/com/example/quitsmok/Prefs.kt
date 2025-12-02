package com.example.quitsmok

import android.content.Context
import android.content.SharedPreferences

class Prefs(context: Context) {
    // Делаем SharedPreferences приватным свойством класса
    private val preferences: SharedPreferences = context.getSharedPreferences("quit_smok_prefs", Context.MODE_PRIVATE)

    // ИСПРАВЛЕНО: Все константы перенесены внутрь companion object
    companion object {
        // --- Настройки, задаваемые пользователем ---
        const val KEY_FIRST_LAUNCH = "first_launch"
        const val KEY_PRICE_PACK = "price_pack"
        const val KEY_CIGS_IN_PACK = "cigs_in_pack"
        const val KEY_CIGS_PER_DAY_SETTING = "cigs_per_day_setting"
        const val KEY_TIME_START_SLEEP = "time_start_sleep"
        const val KEY_TIME_END_SLEEP = "time_end_sleep"
        const val KEY_EXTRA_MINUTES_PER_CIG = "extra_minutes_per_cig"

        // --- Статистика и внутренние данные ---
        const val KEY_INTERVAL_MINUTES = "interval_minutes"
        const val KEY_LAST_SMOKE_TIME = "last_smoke_time"
        const val KEY_LAST_SMOKE_DATE = "last_smoke_date"

        // --- Суточная статистика ---
        const val KEY_CIGS_TODAY = "cigs_today"
        const val KEY_CIGS_YESTERDAY = "cigs_yesterday"
        const val KEY_MONEY_SPENT_PER_DAY = "money_spent_per_day"

        // --- Общая статистика ---
        const val KEY_CIGS_TOTAL = "cigs_total"
        const val KEY_MONEY_SAVED_TOTAL = "money_saved_total"
        const val KEY_FIRST_LAUNCH_TIME = "first_launch_time"
        const val KEY_DAYS_PASSED = "days_passed"
    }

    // --- Методы для сохранения и получения данных ---

    fun saveString(key: String, value: String) = preferences.edit().putString(key, value).apply()
    fun getString(key: String, defValue: String? = null): String? = preferences.getString(key, defValue)

    fun saveInt(key: String, value: Int) = preferences.edit().putInt(key, value).apply()
    fun getInt(key: String, defValue: Int = 0): Int = preferences.getInt(key, defValue)

    fun saveFloat(key: String, value: Float) = preferences.edit().putFloat(key, value).apply()
    fun getFloat(key: String, defValue: Float = 0f): Float = preferences.getFloat(key, defValue)

    fun saveLong(key: String, value: Long) = preferences.edit().putLong(key, value).apply()
    fun getLong(key: String, defValue: Long = 0L): Long = preferences.getLong(key, defValue)

    fun saveBoolean(key: String, value: Boolean) = preferences.edit().putBoolean(key, value).apply()
    fun getBoolean(key: String, defValue: Boolean = false): Boolean = preferences.getBoolean(key, defValue)

    // Очистка всех данных (для сброса прогресса)
    fun clearAll() {
        preferences.edit().clear().apply()
    }
}
