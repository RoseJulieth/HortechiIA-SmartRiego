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
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            finish()
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
        // Marcar el item actual como seleccionado
        binding.bottomNavigation.selectedItemId = R.id.nav_history

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, DashboardActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_history -> {
                    // Ya estamos aquí
                    true
                }
                R.id.nav_irrigation -> {
                    startActivity(Intent(this, ControlManualActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_settings -> {
                    // TODO: Implementar ConfiguracionActivity
                    true
                }
                else -> false
            }
        }
    }
}