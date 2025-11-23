package com.hortechia.smartriego.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.hortechia.smartriego.R
import com.hortechia.smartriego.adapter.ZoneAdapter
import com.hortechia.smartriego.model.Zone

/**
 * DashboardActivity - Pantalla principal del dashboard
 *
 * Funcionalidad:
 * - Mostrar zonas de riego con datos en tiempo real
 * - Navegación a Control Manual
 * - Bottom Navigation
 * - Menú de perfil
 *
 * @author Jennifer Astudillo & Carlos Velásquez
 */
class DashboardActivity : AppCompatActivity() {

    private lateinit var toolbar: MaterialToolbar
    private lateinit var tvUserName: TextView
    private lateinit var rvZones: RecyclerView
    private lateinit var tvNoZones: TextView
    private lateinit var btnControlManual: Button
    private lateinit var bottomNavigation: BottomNavigationView

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    private val zones = mutableListOf<Zone>()
    private lateinit var zoneAdapter: ZoneAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        // Inicializar Firebase
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        // Inicializar vistas
        initViews()

        // Configurar toolbar
        setupToolbar()

        // Configurar RecyclerView
        setupRecyclerView()

        // Cargar datos del usuario
        loadUserData()

        // Cargar zonas (simuladas por ahora)
        loadZones()

        // Configurar listeners
        setupListeners()
    }

    /**
     * Inicializa las vistas
     */
    private fun initViews() {
        toolbar = findViewById(R.id.toolbar)
        tvUserName = findViewById(R.id.tvUserName)
        rvZones = findViewById(R.id.rvZones)
        tvNoZones = findViewById(R.id.tvNoZones)
        btnControlManual = findViewById(R.id.btnControlManual)
        bottomNavigation = findViewById(R.id.bottomNavigation)
    }

    /**
     * Configura el toolbar
     */
    private fun setupToolbar() {
        setSupportActionBar(toolbar)
    }

    /**
     * Configura el RecyclerView
     */
    private fun setupRecyclerView() {
        zoneAdapter = ZoneAdapter(zones) { zone ->
            onZoneClick(zone)
        }

        rvZones.apply {
            layoutManager = LinearLayoutManager(this@DashboardActivity)
            adapter = zoneAdapter
        }
    }

    /**
     * Carga los datos del usuario desde Firebase
     */
    private fun loadUserData() {
        val userId = auth.currentUser?.uid

        if (userId != null) {
            val userRef = database.reference.child("users").child(userId)

            userRef.get().addOnSuccessListener { snapshot ->
                val name = snapshot.child("name").getValue(String::class.java)
                if (!name.isNullOrEmpty()) {
                    tvUserName.text = name
                }
            }
        }
    }

    /**
     * Carga las zonas (simuladas por ahora)
     * TODO: Cargar desde Firebase cuando esté listo el ESP32
     */
    private fun loadZones() {
        // Zonas simuladas con datos de ejemplo
        zones.clear()
        zones.addAll(listOf(
            Zone(
                id = "zone_tomatoes",
                name = getString(R.string.zone_tomatoes),
                humidity = 45,
                temperature = 22.5,
                isActive = true,
                lastIrrigation = System.currentTimeMillis() - (2 * 60 * 60 * 1000), // Hace 2 horas
                icon = R.drawable.ic_zone_tomato
            ),
            Zone(
                id = "zone_grass",
                name = getString(R.string.zone_grass),
                humidity = 62,
                temperature = 23.0,
                isActive = false,
                lastIrrigation = System.currentTimeMillis() - (5 * 60 * 60 * 1000), // Hace 5 horas
                icon = R.drawable.ic_zone_grass
            )
        ))

        zoneAdapter.notifyDataSetChanged()

        // Mostrar mensaje si no hay zonas
        if (zones.isEmpty()) {
            tvNoZones.visibility = android.view.View.VISIBLE
            rvZones.visibility = android.view.View.GONE
        } else {
            tvNoZones.visibility = android.view.View.GONE
            rvZones.visibility = android.view.View.VISIBLE
        }
    }

    /**
     * Configura los listeners
     */
    private fun setupListeners() {
        // Click en botón Control Manual
        btnControlManual.setOnClickListener {
            startActivity(Intent(this, ControlManualActivity::class.java))
        }

        // Bottom Navigation
        bottomNavigation.selectedItemId = R.id.nav_home
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    // Ya estamos en Home
                    true
                }
                R.id.nav_irrigation -> {
                    startActivity(Intent(this, ControlManualActivity::class.java))
                    true
                }
                R.id.nav_history -> {
                    // TODO: Navegar a Historial (Paso 9)
                    Toast.makeText(this, "Historial - Próximamente", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.nav_settings -> {
                    // TODO: Navegar a Configuración
                    Toast.makeText(this, "Configuración - Próximamente", Toast.LENGTH_SHORT).show()
                    true
                }
                else -> false
            }
        }
    }

    /**
     * Click en una zona
     */
    private fun onZoneClick(zone: Zone) {
        Toast.makeText(
            this,
            "${zone.name} - ${zone.getStatusText()}",
            Toast.LENGTH_SHORT
        ).show()
    }

    /**
     * Infla el menú del toolbar
     */
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar_menu, menu)
        return true
    }

    /**
     * Maneja los clicks en el menú
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_profile -> {
                // TODO: Navegar a Perfil
                Toast.makeText(this, "Perfil - Próximamente", Toast.LENGTH_SHORT).show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}