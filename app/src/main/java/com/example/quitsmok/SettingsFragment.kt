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

    // Переменные для хранения выбранного времени сна
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
            // Сбрасываем UI к дефолтным значениям
            binding.etPrice.setText("")
            binding.etCigs.setText("")
            binding.etExtraTime.setText("2") // Сбрасываем и новое поле
            binding.btnTimeStart.text = "22:30"
            binding.btnTimeEnd.text = "07:30"
            Toast.makeText(requireContext(), "Настройки сброшены", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadSavedData() {
        val savedPrice = prefs.getFloat(Prefs.KEY_PRICE_PACK, 0f)
        if (savedPrice > 0) binding.etPrice.setText(savedPrice.toString())

        val savedCigs = prefs.getInt(Prefs.KEY_CIGS_PER_DAY_SETTING, 0)
        if (savedCigs > 0) binding.etCigs.setText(savedCigs.toString())

        // ИЗМЕНЕНО: Загружаем значение увеличения интервала
        val savedExtraTime = prefs.getInt(Prefs.KEY_EXTRA_MINUTES_PER_CIG, 2) // По умолчанию 2
        binding.etExtraTime.setText(savedExtraTime.toString())

        val timeStart = prefs.getString(Prefs.KEY_TIME_START_SLEEP)
        if (!timeStart.isNullOrEmpty()) {
            binding.btnTimeStart.text = timeStart
            val parts = timeStart.split(":")
            if(parts.size == 2) {
                startSleepHour = parts[0].toIntOrNull() ?: 22
                startSleepMinute = parts[1].toIntOrNull() ?: 30
            }
        }

        val timeEnd = prefs.getString(Prefs.KEY_TIME_END_SLEEP)
        if (!timeEnd.isNullOrEmpty()) {
            binding.btnTimeEnd.text = timeEnd
            val parts = timeEnd.split(":")
            if(parts.size == 2) {
                endSleepHour = parts[0].toIntOrNull() ?: 7
                endSleepMinute = parts[1].toIntOrNull() ?: 30
            }
        }
    }

    private fun saveSettings() {
        val priceStr = binding.etPrice.text.toString()
        val cigsStr = binding.etCigs.text.toString()
        // ИЗМЕНЕНО: Считываем новое поле
        val extraTimeStr = binding.etExtraTime.text.toString()

        // ИЗМЕНЕНО: Проверяем, что все поля, включая новое, заполнены
        if (priceStr.isEmpty() || cigsStr.isEmpty() || extraTimeStr.isEmpty()) {
            Toast.makeText(requireContext(), "Пожалуйста, заполните все поля!", Toast.LENGTH_LONG).show()
            return
        }

        val price = priceStr.toFloatOrNull() ?: 0f
        val cigs = cigsStr.toIntOrNull() ?: 0
        // ИЗМЕНЕНО: Сохраняем новое значение
        val extraTime = extraTimeStr.toIntOrNull() ?: 2
        prefs.saveInt(Prefs.KEY_EXTRA_MINUTES_PER_CIG, extraTime)

        prefs.saveFloat(Prefs.KEY_PRICE_PACK, price)
        prefs.saveInt(Prefs.KEY_CIGS_PER_DAY_SETTING, cigs)
        prefs.saveString(Prefs.KEY_TIME_START_SLEEP, binding.btnTimeStart.text.toString())
        prefs.saveString(Prefs.KEY_TIME_END_SLEEP, binding.btnTimeEnd.text.toString())

        // --- Расчёты ---
        val cigsInPack = prefs.getInt(Prefs.KEY_CIGS_IN_PACK, 20).coerceAtLeast(1)
        val moneyPerDay = (price / cigsInPack) * cigs
        prefs.saveFloat(Prefs.KEY_MONEY_SPENT_PER_DAY, moneyPerDay)

        val startSleepMins = startSleepHour * 60 + startSleepMinute
        val endSleepMins = endSleepHour * 60 + endSleepMinute

        val sleepDurationMins = if (endSleepMins >= startSleepMins) {
            endSleepMins - startSleepMins
        } else {
            (24 * 60 - startSleepMins) + endSleepMins
        }

        val awakeDurationMins = (24 * 60) - sleepDurationMins

        if (cigs > 0) {
            val intervalMinutes = awakeDurationMins / cigs
            prefs.saveInt(Prefs.KEY_INTERVAL_MINUTES, intervalMinutes)
        }

        prefs.saveBoolean(Prefs.KEY_FIRST_LAUNCH, false)

        Toast.makeText(requireContext(), "Данные сохранены!", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}