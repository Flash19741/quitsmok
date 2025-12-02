package com.example.quitsmok

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.quitsmok.databinding.FragmentTimerBinding
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class TimerFragment : Fragment() {

    private var _binding: FragmentTimerBinding? = null
    private val binding get() = _binding!!

    private lateinit var prefs: Prefs

    // Handler для обновления таймера каждую секунду
    private val handler = Handler(Looper.getMainLooper())
    private val updateTimerRunnable: Runnable = object : Runnable {
        override fun run() {
            updateUI()
            handler.postDelayed(this, 1000) // Повторять каждую секунду
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTimerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        prefs = Prefs(requireContext())

        checkNewDay()
        updateUI()

        binding.btnSmoke.setOnClickListener {
            performSmoke()
        }
    }

    override fun onResume() {
        super.onResume()
        handler.post(updateTimerRunnable)
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(updateTimerRunnable)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun performSmoke() {
        val currentTime = System.currentTimeMillis()

        prefs.saveLong(Prefs.KEY_LAST_SMOKE_TIME, currentTime)

        val currentCigs = prefs.getInt(Prefs.KEY_CIGS_TODAY, 0)
        prefs.saveInt(Prefs.KEY_CIGS_TODAY, currentCigs + 1)

        val sdfDate = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        prefs.saveString(Prefs.KEY_LAST_SMOKE_DATE, sdfDate.format(Date()))

        updateUI()
    }

    private fun updateUI() {
        // --- 1. Считываем данные ---
        val lastSmokeTime = prefs.getLong(Prefs.KEY_LAST_SMOKE_TIME, 0L)
        val baseIntervalMinutes = prefs.getInt(Prefs.KEY_INTERVAL_MINUTES, 60)
        val cigsToday = prefs.getInt(Prefs.KEY_CIGS_TODAY, 0)

        val extraMinutesPerCig = prefs.getInt(Prefs.KEY_EXTRA_MINUTES_PER_CIG, 2)
        val extraMinutes = cigsToday * extraMinutesPerCig

        val totalIntervalMinutes = baseIntervalMinutes + extraMinutes
        val totalIntervalMillis = totalIntervalMinutes * 60 * 1000L

        val nextSmokeTime = lastSmokeTime + totalIntervalMillis
        val currentTime = System.currentTimeMillis()
        val timeRemaining = nextSmokeTime - currentTime

        // --- 2. Обновляем текстовые метки ---
        val sdfTime = SimpleDateFormat("HH:mm", Locale.getDefault())
        if (lastSmokeTime == 0L) {
            binding.tvLastSmokeLabel.text = "Предыдущий перекур: --:--"
            binding.tvNextSmokeLabel.text = "Следующий перекур: --:--"
        } else {
            binding.tvLastSmokeLabel.text = "Предыдущий перекур: ${sdfTime.format(Date(lastSmokeTime))}"
            binding.tvNextSmokeLabel.text = "Следующий перекур: ${sdfTime.format(Date(nextSmokeTime))}"
        }

        // --- 3. Логика Таймера и Кнопки ---
        if (timeRemaining <= 0) {
            // МОЖНО КУРИТЬ
            binding.tvTimer.text = "00:00:00"
            binding.tvTimer.setTextColor(Color.GREEN)
            binding.btnSmoke.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#4CAF50"))
            binding.btnSmoke.isEnabled = true
            binding.progressBarCircle.progress = 100
        } else {
            // НЕЛЬЗЯ КУРИТЬ
            val h = TimeUnit.MILLISECONDS.toHours(timeRemaining)
            val m = TimeUnit.MILLISECONDS.toMinutes(timeRemaining) % 60
            val s = TimeUnit.MILLISECONDS.toSeconds(timeRemaining) % 60

            val timeString = String.format("%02d:%02d:%02d", h, m, s)
            binding.tvTimer.text = timeString
            binding.tvTimer.setTextColor(Color.RED)
            binding.btnSmoke.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#F44336"))
            binding.btnSmoke.isEnabled = false

            val timePassed = currentTime - lastSmokeTime
            val progress = ((timePassed.toFloat() / totalIntervalMillis.toFloat()) * 100).toInt()
            binding.progressBarCircle.progress = progress.coerceIn(0, 100)
        }
    }

    private fun checkNewDay() {
        val lastDate = prefs.getString(Prefs.KEY_LAST_SMOKE_DATE)
        val sdfDate = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        val currentDate = sdfDate.format(Date())

        if (lastDate == null) {
            prefs.saveString(Prefs.KEY_LAST_SMOKE_DATE, currentDate)
            return
        }

        if (lastDate != currentDate) {
            val cigsSmokedYesterday = prefs.getInt(Prefs.KEY_CIGS_TODAY, 0)
            prefs.saveInt(Prefs.KEY_CIGS_YESTERDAY, cigsSmokedYesterday)

            val currentTotalCigs = prefs.getInt(Prefs.KEY_CIGS_TOTAL, 0)
            prefs.saveInt(Prefs.KEY_CIGS_TOTAL, currentTotalCigs + cigsSmokedYesterday)

            val packPrice = prefs.getFloat(Prefs.KEY_PRICE_PACK, 0f)
            val cigsInPack = prefs.getInt(Prefs.KEY_CIGS_IN_PACK, 20).coerceAtLeast(1)
            val pricePerCig = if (cigsInPack > 0) packPrice / cigsInPack else 0f
            val moneySmokedYesterday = cigsSmokedYesterday * pricePerCig

            val currentTotalMoney = prefs.getFloat(Prefs.KEY_MONEY_SAVED_TOTAL, 0f)
            prefs.saveFloat(Prefs.KEY_MONEY_SAVED_TOTAL, currentTotalMoney + moneySmokedYesterday)

            prefs.saveInt(Prefs.KEY_CIGS_TODAY, 0)
            prefs.saveString(Prefs.KEY_LAST_SMOKE_DATE, currentDate)
        }
    }
}
