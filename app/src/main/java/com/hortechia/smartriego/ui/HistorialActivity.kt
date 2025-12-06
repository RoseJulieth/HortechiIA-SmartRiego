package com.hortechia.smartriego

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.tabs.TabLayoutMediator
import com.hortechia.smartriego.databinding.ActivityHistorialBinding
import com.hortechia.smartriego.ui.DashboardActivity
import com.hortechia.smartriego.ui.ControlManualActivity

class HistorialActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHistorialBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistorialBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupRangoSelector()
        setupViewPager()
        setupBottomNavigation()
        setupBackHandler() // ← NUEVO
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            volverAlDashboard() // ← CAMBIADO de finish()
        }
    }

    private fun setupRangoSelector() {
        val rangos = listOf(
            "Hoy",
            "Última semana",
            "Último mes",
            "Personalizado"
        )

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_dropdown_item_1line,
            rangos
        )

        binding.rangoSelector.setAdapter(adapter)
        binding.rangoSelector.setText("Última semana", false)

        binding.rangoSelector.setOnItemClickListener { _, _, position, _ ->
            // Aquí puedes manejar el cambio de rango
            val rangoSeleccionado = rangos[position]
            // TODO: Actualizar datos según el rango
        }
    }

    private fun setupViewPager() {
        // Configurar el adapter del ViewPager
        val adapter = HistorialPagerAdapter(this)
        binding.viewPager.adapter = adapter

        // Conectar TabLayout con ViewPager2
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Humedad"
                1 -> "Riegos"
                2 -> "Consumo Agua"
                else -> ""
            }
        }.attach()
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.selectedItemId = R.id.nav_history

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    val intent = Intent(this, DashboardActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    }
                    startActivity(intent) // ← ACTUALIZADO con flags
                    finish()
                    true
                }
                R.id.nav_irrigation -> {
                    startActivity(Intent(this, ControlManualActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_history -> {
                    // Ya estamos aquí
                    true
                }
                R.id.nav_schedule -> {
                    startActivity(Intent(this, ProgramacionActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_settings -> {
                    startActivity(Intent(this, ConfiguracionActivity::class.java))
                    finish()
                    true
                }
                else -> false
            }
        }
    }

    /**
     * Volver al Dashboard correctamente sin ciclos
     */
    private fun volverAlDashboard() {
        if (isFinishing) return

        val intent = Intent(this, DashboardActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP or
                    Intent.FLAG_ACTIVITY_NO_ANIMATION
        }
        startActivity(intent)
        finish()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    /**
     * Manejo del botón Atrás del sistema (Android 13+ compatible)
     */
    private fun setupBackHandler() {
        onBackPressedDispatcher.addCallback(this, object : androidx.activity.OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                volverAlDashboard()
            }
        })
    }
}