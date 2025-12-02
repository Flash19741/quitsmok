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

        binding.bottomNavView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_timer -> loadFragment(TimerFragment())
                R.id.nav_stats -> loadFragment(StatsFragment())
                R.id.nav_achieve -> loadFragment(AchievementsFragment())
                R.id.nav_settings -> loadFragment(SettingsFragment())
            }
            true
        }

        // Логика первого запуска: открываем настройки, иначе - таймер.
        if (prefs.getBoolean(Prefs.KEY_FIRST_LAUNCH, true)) {
            // Чтобы при первом запуске была подсвечена нужная иконка
            binding.bottomNavView.selectedItemId = R.id.nav_settings
        } else {
            // Для последующих запусков по умолчанию открываем таймер
            if (savedInstanceState == null) { // Избегаем пересоздания фрагмента при повороте экрана
                binding.bottomNavView.selectedItemId = R.id.nav_timer
            }
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}
