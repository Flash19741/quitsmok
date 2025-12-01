package com.example.quitsmok

import android.content.Context
import android.content.SharedPreferences

class Prefs(context: Context) {
    private val preferences: SharedPreferences = context.getSharedPreferences("quit_smok_prefs", Context.MODE_PRIVATE)

    companion object {
        // --- Настройки, задаваемые пользователем ---
        const val KEY_FIRST_LAUNCH = "first_launch"
        const val KEY_LANGUAGE = "language"
        const val KEY_TIME_START_SLEEP = "time_start_sleep"   // Начало сна
        const val KEY_TIME_END_SLEEP = "time_end_sleep"       // Конец сна
        const val KEY_PRICE_PACK = "price_pack"               // Цена пачки
        const val KEY_CIGS_IN_PACK = "cigs_in_pack"           // Сигарет в пачке (добавил, т.к. обычно нужно)
        const val KEY_CIGS_PER_DAY_SETTING = "cigs_per_day_setting" // Сигарет в день (из настроек)
        const val KEY_MONEY_SPENT_PER_DAY = "money_spent_per_day" // Денег потрачено
        const val KEY_MONEY_TOTAL = "key_money_total"

        // --- Статистика и внутренние данные ---
        const val KEY_INTERVAL_MINUTES = "interval_minutes"   // Текущий расчетный интервал
        const val KEY_LAST_SMOKE_TIME = "last_smoke_time"     // Время последнего перекура (Long)
        const val KEY_LAST_SMOKE_DATE = "last_smoke_date"     // Дата для сброса суточных счетчиков

        // --- Суточная статистика ---
        const val KEY_CIGS_TODAY = "cigs_today"               // Выкурено сегодня
        const val KEY_CIGS_YESTERDAY = "cigs_yesterday"         // Выкурено вчера

        // --- Общая статистика ---
        const val KEY_CIGS_TOTAL = "cigs_total"               // Всего сэкономлено сигарет
        const val KEY_MONEY_SAVED_TOTAL = "money_saved_total" // Всего сэкономлено денег
    }

    // --- Методы для сохранения и получения данных ---

    fun saveString(key: String, value: String) = preferences.edit().putString(key, value).apply()
    fun getString(key: String, defValue: String? = null): String? = preferences.getString(key, defValue)

    fun saveInt(key: String, value: Int) = preferences.edit().putInt(key, value).apply()
    fun getInt(key: String, defValue: Int = 0): Int = preferences.getInt(key, defValue)

    fun saveFloat(key: String, value: Float) = preferences.edit().putFloat(key, value).apply()
    // ИСПРАВЛЕНО: Добавлен параметр для значения по умолчанию
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
