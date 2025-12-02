package com.example.quitsmok

import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.quitsmok.databinding.FragmentSettingsBinding
import java.util.*

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private lateinit var prefs: Prefs

    private var startSleepHour = 22
    private var startSleepMinute = 30
    private var endSleepHour = 7
    private var endSleepMinute = 30

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        prefs = Prefs(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadSavedData()

        binding.btnTimeStart.setOnClickListener {
            TimePickerDialog(requireContext(), { _, hour, minute ->
                startSleepHour = hour
                startSleepMinute = minute
                binding.btnTimeStart.text = String.format("%02d:%02d", hour, minute)
            }, startSleepHour, startSleepMinute, true).show()
        }

        binding.btnTimeEnd.setOnClickListener {
            TimePickerDialog(requireContext(), { _, hour, minute ->
                endSleepHour = hour
                endSleepMinute = minute
                binding.btnTimeEnd.text = String.format("%02d:%02d", hour, minute)
            }, endSleepHour, endSleepMinute, true).show()
        }

        binding.btnSave.setOnClickListener {
            saveSettings()
        }

        binding.btnReset.setOnClickListener {
            prefs.clearAll()
            binding.etPrice.text?.clear()
            binding.etCigs.text?.clear()
            binding.etExtraTime.setText("2")
            binding.btnTimeStart.text = "22:30"
            binding.btnTimeEnd.text = "07:30"
            Toast.makeText(requireContext(), "Прогресс сброшен", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadSavedData() {
        val savedPrice = prefs.getFloat(Prefs.KEY_PRICE_PACK, 0f)
        if (savedPrice > 0) binding.etPrice.setText(String.format(Locale.US, "%.2f", savedPrice))

        val savedCigs = prefs.getInt(Prefs.KEY_CIGS_PER_DAY_SETTING, 0)
        if (savedCigs > 0) binding.etCigs.setText(savedCigs.toString())

        val savedExtraTime = prefs.getInt(Prefs.KEY_EXTRA_MINUTES_PER_CIG, 2)
        binding.etExtraTime.setText(savedExtraTime.toString())

        prefs.getString(Prefs.KEY_TIME_START_SLEEP)?.let {
            binding.btnTimeStart.text = it
            val p = it.split(":")
            if (p.size == 2) {
                startSleepHour = p[0].toIntOrNull() ?: 22
                startSleepMinute = p[1].toIntOrNull() ?: 30
            }
        }

        prefs.getString(Prefs.KEY_TIME_END_SLEEP)?.let {
            binding.btnTimeEnd.text = it
            val p = it.split(":")
            if (p.size == 2) {
                endSleepHour = p[0].toIntOrNull() ?: 7
                endSleepMinute = p[1].toIntOrNull() ?: 30
            }
        }
    }

    private fun saveSettings() {
        val priceStr = binding.etPrice.text.toString().trim()
        val cigsStr = binding.etCigs.text.toString().trim()
        val extraTimeStr = binding.etExtraTime.text.toString().trim()

        if (priceStr.isEmpty() || cigsStr.isEmpty() || extraTimeStr.isEmpty()) {
            Toast.makeText(requireContext(), "Заполните все поля!", Toast.LENGTH_LONG).show()
            return
        }

        val price = priceStr.toFloatOrNull() ?: 0f
        val cigs = cigsStr.toIntOrNull() ?: 0
        val extraTime = extraTimeStr.toIntOrNull() ?: 2

        if (price <= 0 || cigs <= 0) {
            Toast.makeText(requireContext(), "Введите корректные значения", Toast.LENGTH_LONG).show()
            return
        }

        // Сохраняем основные настройки
        prefs.saveFloat(Prefs.KEY_PRICE_PACK, price)
        prefs.saveInt(Prefs.KEY_CIGS_PER_DAY_SETTING, cigs)
        prefs.saveInt(Prefs.KEY_EXTRA_MINUTES_PER_CIG, extraTime)
        prefs.saveString(Prefs.KEY_TIME_START_SLEEP, binding.btnTimeStart.text.toString())
        prefs.saveString(Prefs.KEY_TIME_END_SLEEP, binding.btnTimeEnd.text.toString())

        // Деньги в день
        val cigsInPack = prefs.getInt(Prefs.KEY_CIGS_IN_PACK, 20).coerceAtLeast(1)
        val moneyPerDay = (price / cigsInPack) * cigs
        prefs.saveFloat(Prefs.KEY_MONEY_SPENT_PER_DAY, moneyPerDay)

        // Расчёт интервала бодрствования
        val startMins = startSleepHour * 60 + startSleepMinute
        val endMins = endSleepHour * 60 + endSleepMinute
        val sleepMins = if (endMins >= startMins) endMins - startMins else (24 * 60 - startMins) + endMins
        val awakeMins = 24 * 60 - sleepMins

        val intervalMinutes = if (cigs > 0) awakeMins / cigs else 60
        prefs.saveInt(Prefs.KEY_INTERVAL_MINUTES, intervalMinutes)

        // Важно: сохраняем время первого запуска (только один раз)
        if (prefs.getBoolean(Prefs.KEY_FIRST_LAUNCH, true)) {
            prefs.saveLong(Prefs.KEY_FIRST_LAUNCH_TIME, System.currentTimeMillis())
            prefs.saveBoolean(Prefs.KEY_FIRST_LAUNCH, false)
        }

        Toast.makeText(requireContext(), "Настройки сохранены!", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}