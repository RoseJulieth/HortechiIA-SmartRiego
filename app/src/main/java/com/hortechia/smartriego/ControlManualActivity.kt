package com.hortechia.smartriego

import android.content.Intent
import android.os.Bundle
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

class ControlManualActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_control_manual)

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { finish() }

        setupSeekBar()
        setupBottomNavigation()
    }

    private fun setupSeekBar() {
        val seekBar = findViewById<SeekBar>(R.id.seekBarDuracion1)
        val tvDuracion = findViewById<TextView>(R.id.tvDuracion1)

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                tvDuracion.text = "$progress minutos"
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        findViewById<android.widget.Button>(R.id.btnIniciarRiego1).setOnClickListener {
            Toast.makeText(this, "Iniciando riego Zona 1...", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupBottomNavigation() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        bottomNav.selectedItemId = R.id.nav_control

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, DashboardActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_control -> true
                R.id.nav_historial -> {
                    startActivity(Intent(this, HistorialActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_programar -> {
                    startActivity(Intent(this, ProgramacionActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_mas -> {
                    startActivity(Intent(this, ConfiguracionActivity::class.java))
                    finish()
                    true
                }
                else -> false
            }
        }
    }
}
