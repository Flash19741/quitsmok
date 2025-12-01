// Файл: AchievementsFragment.kt
package com.example.quitsmok

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.fragment.app.Fragment
import com.example.quitsmok.databinding.FragmentAchievementsBinding
import kotlin.math.roundToInt

class AchievementsFragment : Fragment() {

    private var _binding: FragmentAchievementsBinding? = null
    private val binding get() = _binding!!
    private lateinit var prefs: Prefs

    // Карта ресурсов медалей
    private val medalResources = mapOf(
        "bronze" to R.drawable.ic_medal_bronze, // Замени на фактический ID ресурса
        "silver" to R.drawable.ic_medal_silver, // Замени на фактический ID ресурса
        "gold" to R.drawable.ic_medal_gold     // Замени на фактический ID ресурса
    )

    // Размер медали в пикселях (DP)
    private val MEDAL_SIZE_DP = 60
    // Смещение для наложения (25% видимой части, значит перекрытие 75%)
    private val OVERLAP_PERCENT = 0.75f

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAchievementsBinding.inflate(inflater, container, false)
        prefs = Prefs(requireContext())
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        calculateAndDisplayAchievements()
    }

    // --- ОСНОВНОЙ АЛГОРИТМ РАСЧЕТА ДОСТИЖЕНИЙ ---
    private fun calculateAndDisplayAchievements() {
        // Убедимся, что контейнеры пустые перед добавлением
        binding.cigsMedalContainer.removeAllViews()
        binding.moneyMedalContainer.removeAllViews()
        binding.intervalMedalContainer.removeAllViews()

        // 1. Исходные данные (настройки)
        val initialCigsPerDay = prefs.getInt(Prefs.KEY_CIGS_PER_DAY_SETTING, 20)
        val initialMoneyPerDay = prefs.getFloat(Prefs.KEY_MONEY_SPENT_PER_DAY, 50.00f)
        val initialIntervalMins = prefs.getInt(Prefs.KEY_INTERVAL_MINUTES, 30) // Базовый интервал

        // 2. Текущие данные (из статистики)
        // Сигареты и Деньги: сравниваем, сколько не выкурено/сэкономлено, относительно того, сколько тратилось.
        val totalCigsSmoked = prefs.getInt(Prefs.KEY_CIGS_TOTAL, 0)
        val totalMoneySpent = prefs.getFloat(Prefs.KEY_MONEY_TOTAL, 0f)

        // Для упрощения, считаем, сколько сигарет было "пропущено"
        // (Это сложная метрика, но для примера упростим, используя текущее количество,
        // предполагая, что ты курил по плану, если не нажимал "Курить").
        // Вместо этого, используем разницу между потенциальными и фактическими расходами.

        // 3. Расчет для "Интервал" (легче всего, используем текущий интервал из Таймера)
        val cigsToday = prefs.getInt(Prefs.KEY_CIGS_TODAY, 0)
        val currentIntervalMins = initialIntervalMins + (cigsToday * 2) // Текущий увеличенный интервал

        // --- 4. Вычисление процентов ---

        // Процент увеличения интервала (процент достижения)
        val intervalIncrease = currentIntervalMins - initialIntervalMins
        val percentIntervalAchieved = ((intervalIncrease.toFloat() / initialIntervalMins.toFloat()) * 100).roundToInt()

        // Процент экономии Сигарет (СЛОЖНО: Требует подсчета дней. Используем упрощенную метрику)
        // Здесь потребуется более сложная логика в Statistics, чтобы считать *не выкуренные* сигареты.
        // Для MVP-кода: берем сколько сигарет в день мы *должны* были выкурить и сравниваем с тем, сколько выкурили.
        // Пусть "потенциальные сигареты" = initialCigsPerDay * DaysUsed

        // Так как мы не считаем DaysUsed, для демо-целей пропустим Cigs и Money,
        // сфокусировавшись на Интервале, который легко считается из текущих данных:

        // ДЕМО-ДАННЫЕ для Сигарет и Денег, пока не реализована полная логика "сэкономлено"
        val percentCigsAchieved = 35 // Например, 35% сигарет пропущено
        val percentMoneyAchieved = 40 // Например, 40% денег сэкономлено

        // --- 5. Отображение ---

        // Достижения по Сигаретам
        drawMedals(binding.cigsMedalContainer, percentCigsAchieved, initialCigsPerDay)

        // Достижения по Деньгам
        drawMedals(binding.moneyMedalContainer, percentMoneyAchieved, initialMoneyPerDay)

        // Достижения по Интервалу (используем реальный расчет)
        drawMedals(binding.intervalMedalContainer, percentIntervalAchieved, initialIntervalMins)
    }

    /**
     * Динамически рисует медали в контейнере с эффектом наложения.
     * @param container Контейнер ConstraintLayout
     * @param achievementPercent Достигнутый процент (0-100)
     * @param initialValue Исходное значение (для подписи)
     */
    private fun drawMedals(container: ConstraintLayout, achievementPercent: Int, initialValue: Any) {
        // Проценты для медалей
        val bronzeThresholds = (5..50 step 5).toList()
        val silverThresholds = (55..75 step 5).toList()
        val goldThresholds = (80..100 step 5).toList()

        // Объединяем все пороги достижения
        val allThresholds = bronzeThresholds + silverThresholds + goldThresholds

        var previousViewId = ConstraintSet.PARENT_ID
        var currentOffsetPx = 0

        // DP to PX conversion
        val density = resources.displayMetrics.density
        val medalSizePx = (MEDAL_SIZE_DP * density).roundToInt()
        val overlapPx = (medalSizePx * OVERLAP_PERCENT).roundToInt() // Смещение на 75% размера

        for (i in allThresholds.indices) {
            val percent = allThresholds[i]
            if (achievementPercent >= percent) {

                val medalType = when {
                    percent <= 50 -> "bronze"
                    percent <= 75 -> "silver"
                    else -> "gold"
                }

                val medalResId = medalResources[medalType] ?: R.drawable.ic_medal_bronze

                val imageView = ImageView(context).apply {
                    id = View.generateViewId() // Генерируем уникальный ID
                    setImageResource(medalResId)
                    // Устанавливаем размеры
                    layoutParams = ViewGroup.LayoutParams(medalSizePx, medalSizePx)
                    // Для красоты можно добавить подпись процента
                    contentDescription = "$percent%"
                    setBackgroundColor(Color.TRANSPARENT)
                }

                container.addView(imageView)

                // --- ЛОГИКА НАЛОЖЕНИЯ С ИСПОЛЬЗОВАНИЕМ ConstraintSet ---
                val set = ConstraintSet()
                set.clone(container)

                if (previousViewId == ConstraintSet.PARENT_ID) {
                    // Первая медаль - привязываем к левому краю
                    set.connect(imageView.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
                } else {
                    // Последующие медали - привязываем к правой части предыдущей с наложением
                    // Мы смещаем следующую медаль на (Размер - Смещение)
                    set.connect(imageView.id, ConstraintSet.START, previousViewId, ConstraintSet.START, medalSizePx - overlapPx)
                }

                // Привязываем к центру по вертикали
                set.connect(imageView.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
                set.connect(imageView.id, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)

                set.applyTo(container)

                previousViewId = imageView.id
            } else {
                // Если процент достижения ниже порога, прекращаем рисовать
                break
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}