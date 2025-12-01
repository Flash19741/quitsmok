package com.example.quitsmok

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.quitsmok.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var prefs: Prefs

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefs = Prefs(this)

        // Устанавливаем слушатель для BottomNavigationView ДО того, как выбираем элемент.
        // Это стандартная и надежная практика.
        binding.bottomNavView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_timer -> loadFragment(TimerFragment())
                R.id.nav_stats -> loadFragment(StatsFragment())
                R.id.nav_achieve -> loadFragment(AchievementsFragment())
                R.id.nav_settings -> loadFragment(SettingsFragment())
            }
            true
        }

        // Логика первого запуска.
        // Вместо прямой загрузки фрагмента, мы просто выбираем нужный пункт в меню.
        // Это автоматически вызовет слушатель setOnItemSelectedListener и загрузит нужный фрагмент.
        if (prefs.getBoolean(Prefs.KEY_FIRST_LAUNCH, true)) {
            // Если это первый запуск, выбираем вкладку "Настройки"
            binding.bottomNavView.selectedItemId = R.id.nav_settings
        } else {
            // Если запуск не первый, выбираем вкладку "Таймер"
            binding.bottomNavView.selectedItemId = R.id.nav_timer
        }
    }

    /**
     * Функция для замены текущего фрагмента в контейнере.
     * @param fragment Экземпляр фрагмента для отображения.
     */
    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}
