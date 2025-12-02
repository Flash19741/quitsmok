package com.example.quitsmok

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.quitsmok.databinding.FragmentStatsBinding
import java.util.*

class StatsFragment : Fragment() {

    private var _binding: FragmentStatsBinding? = null
    private val binding get() = _binding!!
    private lateinit var prefs: Prefs

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStatsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prefs = Prefs(requireContext())
    }

    override fun onResume() {
        super.onResume()
        // Обновляем статистику каждый раз, когда пользователь открывает этот экран
        updateStats()
    }

    private fun updateStats() {
        // --- 1. Получаем базовые данные из настроек ---
        val initialCigsPerDay = prefs.getInt(Prefs.KEY_CIGS_PER_DAY_SETTING, 20)
        val packPrice = prefs.getFloat(Prefs.KEY_PRICE_PACK, 0f)
        val cigsInPack = prefs.getInt(Prefs.KEY_CIGS_IN_PACK, 20)

        val pricePerCig = if (cigsInPack > 0 && packPrice > 0) {
            packPrice / cigsInPack
        } else {
            0f
        }

        // --- 2. Получаем статистику по дням ---
        val cigsToday = prefs.getInt(Prefs.KEY_CIGS_TODAY, 0)
        val cigsYesterday = prefs.getInt(Prefs.KEY_CIGS_YESTERDAY, 0)

        // --- 3. Рассчитываем потраченные деньги "на лету" ---
        val moneySpentToday = cigsToday * pricePerCig
        val moneySpentYesterday = cigsYesterday * pricePerCig
        val initialMoneyPerDay = initialCigsPerDay * pricePerCig

        // --- 4. Получаем "базовый" итог по сигаретам (за все ПРОШЕДШИЕ дни) ---
        val baseTotalCigs = prefs.getInt(Prefs.KEY_CIGS_TOTAL, 0)

        // --- 5. Рассчитываем АКТУАЛЬНЫЕ итоги ---
        // Считаем общее количество выкуренных сигарет
        val actualTotalCigs = baseTotalCigs + cigsToday

        // ======================= ГЛАВНОЕ ИСПРАВЛЕНИЕ ЗДЕСЬ =======================
        // Рассчитываем общую сумму потраченных денег, умножая ОБЩЕЕ количество сигарет на цену одной.
        // Это самый надежный способ, который исключает ошибки сложения.
        val actualTotalMoney = actualTotalCigs * pricePerCig
        // =====================================================================

        // --- 6. Отображаем все данные на экране ---
        // В начале
        binding.tvCigsStart.text = "$initialCigsPerDay"
        binding.tvMoneyStart.text = String.format(Locale.US, "%.2f", initialMoneyPerDay)

        // Вчера
        binding.tvCigsYesterday.text = "$cigsYesterday"
        binding.tvMoneyYesterday.text = String.format(Locale.US, "%.2f", moneySpentYesterday)

        // Сегодня
        binding.tvCigsToday.text = "$cigsToday"
        binding.tvMoneyToday.text = String.format(Locale.US, "%.2f", moneySpentToday)

        // Всего (отображаем актуальные, рассчитанные итоги)
        binding.tvCigsTotal.text = "$actualTotalCigs"
        binding.tvMoneyTotal.text = String.format(Locale.US, "%.2f", actualTotalMoney)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}