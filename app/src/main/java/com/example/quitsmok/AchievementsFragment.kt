package com.example.quitsmok

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.quitsmok.databinding.FragmentAchievementsBinding

class AchievementsFragment : Fragment() {

    private var _binding: FragmentAchievementsBinding? = null
    private val binding get() = _binding!!
    private lateinit var prefs: Prefs

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAchievementsBinding.inflate(inflater, container, false)
        prefs = Prefs(requireContext()) // Инициализируем Prefs
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        updateAchievements()
    }

    private fun updateAchievements() {
        // --- Получаем исходные данные из настроек ---
        // ИСПРАВЛЕНО: Обращаемся через prefs. и Prefs.
        val initialCigsPerDay = prefs.getInt(Prefs.KEY_CIGS_PER_DAY_SETTING, 20)
        val initialMoneyPerDay = prefs.getFloat(Prefs.KEY_MONEY_SPENT_PER_DAY, 0f)
        val initialInterval = prefs.getInt(Prefs.KEY_INTERVAL_MINUTES, 60)

        // --- Получаем текущую статистику ---
        // ИСПРАВЛЕНО: Обращаемся через prefs. и Prefs.
        val totalCigsSaved = prefs.getInt(Prefs.KEY_CIGS_TOTAL, 0)
        val totalMoneySaved = prefs.getFloat(Prefs.KEY_MONEY_SAVED_TOTAL, 0f)

        // --- Логика для достижений по сигаретам ---
        val cigsLevels = listOf(10, 50, 100, 500, 1000) // Уровни достижений
        val cigsProgress = (totalCigsSaved.toFloat() / initialCigsPerDay.coerceAtLeast(1)).toInt() // Сколько дней не курил по старой норме
        populateMedals(binding.cigsMedalContainer, cigsLevels, cigsProgress)

        // --- Логика для достижений по деньгам ---
        val moneyLevels = listOf(100, 500, 1000, 5000, 10000)
        val moneyProgress = totalMoneySaved
        populateMedals(binding.moneyMedalContainer, moneyLevels, moneyProgress.toInt())

        // --- Логика для достижений по интервалу ---
        // Сравниваем текущий интервал с начальным
        val cigsToday = prefs.getInt(Prefs.KEY_CIGS_TODAY, 0)
        val extraMinutesPerCig = prefs.getInt(Prefs.KEY_EXTRA_MINUTES_PER_CIG, 2)
        val currentInterval = initialInterval + (cigsToday * extraMinutesPerCig)
        val intervalIncrease = currentInterval - initialInterval
        val intervalLevels = listOf(10, 30, 60, 120, 180) // Увеличение на X минут
        populateMedals(binding.intervalMedalContainer, intervalLevels, intervalIncrease)
    }

    private fun populateMedals(container: ViewGroup, levels: List<Int>, currentProgress: Int) {
        container.removeAllViews() // Очищаем контейнер перед заполнением
        for (level in levels) {
            val medalView = layoutInflater.inflate(R.layout.medal_item, container, false) as ImageView
            if (currentProgress >= level) {
                // Достижение получено
                medalView.setColorFilter(ContextCompat.getColor(requireContext(), android.R.color.holo_green_light))
            } else {
                // Достижение не получено
                medalView.setColorFilter(ContextCompat.getColor(requireContext(), android.R.color.darker_gray))
            }
            container.addView(medalView)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
