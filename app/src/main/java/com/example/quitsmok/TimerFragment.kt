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

    // Безопасный способ использования View Binding во фрагментах
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

        // Инициализируем prefs здесь, когда контекст гарантированно доступен
        prefs = Prefs(requireContext())

        checkNewDay() // Проверяем, наступил ли новый день
        updateUI()    // Первичное обновление интерфейса

        binding.btnSmoke.setOnClickListener {
            performSmoke()
        }
    }

    override fun onResume() {
        super.onResume()
        // Запускаем таймер при возвращении на экран
        handler.post(updateTimerRunnable)
    }

    override fun onPause() {
        super.onPause()
        // Останавливаем таймер при уходе с экрана для экономии ресурсов
        handler.removeCallbacks(updateTimerRunnable)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Очищаем ссылку на binding, чтобы избежать утечек памяти
        _binding = null
    }

    private fun performSmoke() {
        val currentTime = System.currentTimeMillis()

        // 1. Сохраняем время этого перекура
        prefs.saveString(Prefs.KEY_LAST_SMOKE_TIME, currentTime.toString())

        // 2. Увеличиваем счетчик сигарет за сегодня
        val currentCigs = prefs.getInt(Prefs.KEY_CIGS_TODAY, 0)
        prefs.saveInt(Prefs.KEY_CIGS_TODAY, currentCigs + 1)

        // 3. Сохраняем дату для проверки "нового дня"
        val sdfDate = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        prefs.saveString(Prefs.KEY_LAST_SMOKE_DATE, sdfDate.format(Date()))

        // 4. Обновляем UI мгновенно, чтобы пользователь видел результат
        updateUI()
    }

    private fun updateUI() {
        // --- 1. Считываем данные ---
        val lastSmokeTimeStr = prefs.getString(Prefs.KEY_LAST_SMOKE_TIME)
        val lastSmokeTime = lastSmokeTimeStr?.toLongOrNull() ?: 0L

        val baseIntervalMinutes = prefs.getInt(Prefs.KEY_INTERVAL_MINUTES, 60)
        val cigsToday = prefs.getInt(Prefs.KEY_CIGS_TODAY, 0)

        // Рассчитываем полный интервал для СЛЕДУЮЩЕГО перекура
        val extraMinutes = cigsToday * 2
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
            binding.btnSmoke.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#4CAF50")) // Зеленый
            binding.btnSmoke.isEnabled = true // Кнопка активна
            binding.progressBarCircle.progress = 100
        } else {
            // НЕЛЬЗЯ КУРИТЬ (Таймер тикает)
            val h = TimeUnit.MILLISECONDS.toHours(timeRemaining)
            val m = TimeUnit.MILLISECONDS.toMinutes(timeRemaining) % 60
            val s = TimeUnit.MILLISECONDS.toSeconds(timeRemaining) % 60

            val timeString = String.format("%02d:%02d:%02d", h, m, s)
            binding.tvTimer.text = timeString
            binding.tvTimer.setTextColor(Color.RED)
            binding.btnSmoke.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#F44336")) // Красный
            binding.btnSmoke.isEnabled = false // Кнопка неактивна, пока идет таймер

            // Расчет прогресса для кругового индикатора
            val timePassed = currentTime - lastSmokeTime
            val progress = ((timePassed.toFloat() / totalIntervalMillis.toFloat()) * 100).toInt()
            binding.progressBarCircle.progress = progress.coerceIn(0, 100) // Убедимся, что значение в пределах 0-100
        }
    }

    // Проверяет, наступил ли новый день, и обновляет статистику
    private fun checkNewDay() {
        val lastDate = prefs.getString(Prefs.KEY_LAST_SMOKE_DATE)
        val sdfDate = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        val currentDate = sdfDate.format(Date())

        // Если lastDate == null, это первый запуск. Просто сохраняем дату.
        if (lastDate == null) {
            prefs.saveString(Prefs.KEY_LAST_SMOKE_DATE, currentDate)
            return
        }

        // Если даты не совпадают, значит, наступил новый день
        if (lastDate != currentDate) {
            val cigsSmokedYesterday = prefs.getInt(Prefs.KEY_CIGS_TODAY, 0)

            // 1. Переносим статистику "Сегодня" в "Вчера"
            prefs.saveInt(Prefs.KEY_CIGS_YESTERDAY, cigsSmokedYesterday)

            // 2. Обновляем общую статистику
            val currentTotalCigs = prefs.getInt(Prefs.KEY_CIGS_TOTAL, 0)
            prefs.saveInt(Prefs.KEY_CIGS_TOTAL, currentTotalCigs + cigsSmokedYesterday)

            // 3. Рассчитываем и добавляем потраченные вчера деньги
            val initialCigs = prefs.getInt(Prefs.KEY_CIGS_PER_DAY_SETTING, 20)
            val initialMoney = prefs.getFloat(Prefs.KEY_MONEY_SPENT_PER_DAY, 0f)
            val pricePerCig = if (initialCigs > 0) initialMoney / initialCigs.toFloat() else 0f
            val moneySmokedYesterday = cigsSmokedYesterday * pricePerCig

            val currentTotalMoney = prefs.getFloat(Prefs.KEY_MONEY_TOTAL, 0f)
            prefs.saveFloat(Prefs.KEY_MONEY_TOTAL, currentTotalMoney + moneySmokedYesterday)

            // 4. Сбрасываем счетчик сигарет за Сегодня
            prefs.saveInt(Prefs.KEY_CIGS_TODAY, 0)

            // 5. Обновляем дату последнего входа на сегодня
            prefs.saveString(Prefs.KEY_LAST_SMOKE_DATE, currentDate)
        }
    }
}
