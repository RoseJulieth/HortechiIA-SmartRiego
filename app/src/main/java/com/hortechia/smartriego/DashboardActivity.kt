package com.hortechia.smartriego

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

class DashboardActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        setSupportActionBar(findViewById(R.id.toolbar))

        setupBottomNavigation()
    }

    private fun setupBottomNavigation() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        bottomNav.selectedItemId = R.id.nav_home

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> true
                R.id.nav_historial -> {
                    startActivity(Intent(this, HistorialActivity::class.java))
                    overridePendingTransition(0, 0)
                    true
                }
                R.id.nav_control -> {
                    startActivity(Intent(this, ControlManualActivity::class.java))
                    overridePendingTransition(0, 0)
                    true
                }
                R.id.nav_programar -> {
                    startActivity(Intent(this, ProgramacionActivity::class.java))
                    overridePendingTransition(0, 0)
                    true
                }
                R.id.nav_mas -> {
                    startActivity(Intent(this, ConfiguracionActivity::class.java))
                    overridePendingTransition(0, 0)
                    true
                }
                else -> false
            }
        }
    }
}
