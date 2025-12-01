// Файл: StatsFragment.kt
package com.example.quitsmok

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.quitsmok.databinding.FragmentStatsBinding

class StatsFragment : Fragment() {

    private var _binding: FragmentStatsBinding? = null
    private val binding get() = _binding!!
    private lateinit var prefs: Prefs

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStatsBinding.inflate(inflater, container, false)
        prefs = Prefs(requireContext())
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        // Обновляем статистику каждый раз при открытии окна
        displayStatistics()
    }

    private fun displayStatistics() {
        // Получаем значение "В начале" из Настроек
        val initialCigs = prefs.getInt(Prefs.KEY_CIGS_PER_DAY_SETTING, 20)
        val initialMoney = prefs.getFloat(Prefs.KEY_MONEY_SPENT_PER_DAY, 50.00f)

        // Получаем значение "Вчера"
        val yesterdayCigs = prefs.getInt(Prefs.KEY_CIGS_YESTERDAY, 0)
        // Для денег "Вчера" используем базовую стоимость одной сигареты (если данные есть)
        val pricePerCig = initialMoney / initialCigs.toFloat().coerceAtLeast(1f)
        val yesterdayMoney = yesterdayCigs * pricePerCig

        // Получаем значение "Сегодня"
        val todayCigs = prefs.getInt(Prefs.KEY_CIGS_TODAY, 0)
        val todayMoney = todayCigs * pricePerCig

        // Получаем значение "Всего"
        val totalCigs = prefs.getInt(Prefs.KEY_CIGS_TOTAL, 0)
        val totalMoney = prefs.getFloat(Prefs.KEY_MONEY_TOTAL, 0f)


        // --- Отображение данных в UI ---

        // 1. В начале (Неизменные)
        binding.tvCigsStart.text = initialCigs.toString()
        binding.tvMoneyStart.text = String.format("%.2f", initialMoney)

        // 2. Вчера
        binding.tvCigsYesterday.text = yesterdayCigs.toString()
        binding.tvMoneyYesterday.text = String.format("%.2f", yesterdayMoney)

        // 3. Сегодня
        binding.tvCigsToday.text = todayCigs.toString()
        binding.tvMoneyToday.text = String.format("%.2f", todayMoney)

        // 4. Всего
        binding.tvCigsTotal.text = totalCigs.toString()
        binding.tvMoneyTotal.text = String.format("%.2f", totalMoney)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}