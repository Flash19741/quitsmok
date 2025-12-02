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
        updateStats()
    }

    private fun updateStats() {
        val initialCigsPerDay = prefs.getInt(Prefs.KEY_CIGS_PER_DAY_SETTING, 20)
        val packPrice = prefs.getFloat(Prefs.KEY_PRICE_PACK, 0f)
        val cigsInPack = prefs.getInt(Prefs.KEY_CIGS_IN_PACK, 20)

        val pricePerCig = if (cigsInPack > 0 && packPrice > 0) packPrice / cigsInPack else 0f

        // Сегодня и вчера
        val cigsToday = prefs.getInt(Prefs.KEY_CIGS_TODAY, 0)
        val cigsYesterday = prefs.getInt(Prefs.KEY_CIGS_YESTERDAY, 0)

        // Общее количество выкуренных сигарет (прошлые дни + сегодня)
        val baseTotalCigs = prefs.getInt(Prefs.KEY_CIGS_TOTAL, 0)
        val actualTotalCigs = baseTotalCigs + cigsToday

        // Общая сумма потраченных денег (самый точный способ)
        val actualTotalMoney = actualTotalCigs * pricePerCig

        // Деньги за сегодня/вчера
        val moneySpentToday = cigsToday * pricePerCig
        val moneySpentYesterday = cigsYesterday * pricePerCig
        val initialMoneyPerDay = initialCigsPerDay * pricePerCig

        // === РАСЧЁТ СЭКОНОМЛЕННОГО ===
        val firstLaunchTime = prefs.getLong(Prefs.KEY_FIRST_LAUNCH_TIME, 0L)
        val daysPassed = if (firstLaunchTime > 0) {
            val days = (System.currentTimeMillis() - firstLaunchTime) / (1000L * 60 * 60 * 24)
            days.toInt()
        } else 0

        // Сколько бы выкурил, если бы не бросал
        val expectedCigs = initialCigsPerDay * (daysPassed + 1) // +1 — включая сегодняшний день
        val savedCigs = (expectedCigs - actualTotalCigs).coerceAtLeast(0)
        val savedMoney = savedCigs * pricePerCig

        // === УВЕЛИЧЕНИЕ ИНТЕРВАЛА ===
        val initialInterval = prefs.getInt(Prefs.KEY_INTERVAL_MINUTES, 60)
        val extraPerCig = prefs.getInt(Prefs.KEY_EXTRA_MINUTES_PER_CIG, 2)
        val currentInterval = initialInterval + (actualTotalCigs * extraPerCig)
        val intervalIncreased = currentInterval - initialInterval

        // === ЗАПОЛНЕНИЕ UI ===
        binding.tvCigsStart.text = "$initialCigsPerDay"
        binding.tvMoneyStart.text = String.format(Locale.US, "%.2f", initialMoneyPerDay)

        binding.tvCigsYesterday.text = "$cigsYesterday"
        binding.tvMoneyYesterday.text = String.format(Locale.US, "%.2f", moneySpentYesterday)

        binding.tvCigsToday.text = "$cigsToday"
        binding.tvMoneyToday.text = String.format(Locale.US, "%.2f", moneySpentToday)

        binding.tvCigsTotal.text = "$actualTotalCigs"
        binding.tvMoneyTotal.text = String.format(Locale.US, "%.2f", actualTotalMoney)

        // Новые строки
        binding.tvSavedCigs.text = "$savedCigs"
        binding.tvSavedMoney.text = String.format(Locale.US, "%.2f ₽", savedMoney)

        binding.tvIntervalIncreased.text = if (intervalIncreased > 0) {
            "+$intervalIncreased мин"
        } else {
            "0 мин"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}