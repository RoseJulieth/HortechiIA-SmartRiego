package com.hortechia.smartriego.ui

import android.content.Intent
import com.hortechia.smartriego.HistorialActivity
import com.hortechia.smartriego.ProgramacionActivity
import com.hortechia.smartriego.ConfiguracionActivity
import com.hortechia.smartriego.ui.DashboardActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.hortechia.smartriego.R
import com.hortechia.smartriego.adapter.ControlZoneAdapter
import com.hortechia.smartriego.model.Zone

/**
 * ControlManualActivity - Control manual del sistema de riego
 *
 * Funcionalidad:
 * - Activar/desactivar riego por zona
 * - Guardar estado en Firebase
 * - Sincronizar con ESP32 en tiempo real
 *
 * @author Jennifer Astudillo & Carlos Velásquez
 */
class ControlManualActivity : AppCompatActivity() {

    private lateinit var toolbar: MaterialToolbar
    private lateinit var rvZones: RecyclerView
    private lateinit var btnApply: Button
    private lateinit var progressBar: ProgressBar

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var devicesRef: DatabaseReference

    private lateinit var deviceId: String

    private val zones = mutableListOf<Zone>()
    private lateinit var zoneAdapter: ControlZoneAdapter

    // Mapa para guardar cambios pendientes
    private val pendingChanges = mutableMapOf<String, Boolean>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_control_manual)

        // Inicializar Firebase
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        // Usar Device ID del ESP32
        deviceId = "0qe3XVfFnWREiNqQMNpz4tlYKi2"

        // Usar deviceId en lugar de userId para conectar con Firebase
        devicesRef = database.reference.child("devices").child(deviceId)

        // Inicializar vistas
        initViews()

        // Configurar toolbar
        setupToolbar()

        // Configurar RecyclerView
        setupRecyclerView()

        // Cargar zonas
        loadZones()

        // Configurar listeners
        setupListeners()

        // Configurar bottom navigation
        setupBottomNavigation()

        // Configurar manejo del botón Atrás
        setupBackHandler()
    }

    /**
     * Inicializa las vistas
     */
    private fun initViews() {
        toolbar = findViewById(R.id.toolbar)
        rvZones = findViewById(R.id.rvZones)
        btnApply = findViewById(R.id.btnApply)
        progressBar = findViewById(R.id.progressBar)
    }

    /**
     * Configura el toolbar
     */
    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {
            // Volver al Dashboard correctamente
            volverAlDashboard()
        }
    }

    /**
     * Configura el RecyclerView
     */
    private fun setupRecyclerView() {
        zoneAdapter = ControlZoneAdapter(zones) { zone, isActive ->
            onZoneStatusChanged(zone, isActive)
        }

        rvZones.apply {
            layoutManager = LinearLayoutManager(this@ControlManualActivity)
            adapter = zoneAdapter
        }
    }

    /**
     * Carga las zonas desde Firebase (simuladas por ahora)
     */
    private fun loadZones() {
        showLoading(true)

        // Datos simulados iniciales
        zones.clear()
        zones.addAll(listOf(
            Zone(
                id = "zone_tomatoes",
                name = getString(R.string.zone_tomatoes),
                humidity = 45,
                temperature = 22.5,
                isActive = false,
                lastIrrigation = System.currentTimeMillis() - (2 * 60 * 60 * 1000),
                icon = R.drawable.ic_zone_tomato
            ),
            Zone(
                id = "zone_grass",
                name = getString(R.string.zone_grass),
                humidity = 62,
                temperature = 23.0,
                isActive = false,
                lastIrrigation = System.currentTimeMillis() - (5 * 60 * 60 * 1000),
                icon = R.drawable.ic_zone_grass
            )
        ))

        zoneAdapter.notifyDataSetChanged()
        showLoading(false)

        // Cargar estado real desde Firebase
        loadZoneStatesFromFirebase()
    }

    /**
     * Carga el estado actual de las zonas desde Firebase
     */
    private fun loadZoneStatesFromFirebase() {
        // Escuchar cambios en tiempo real desde Firebase

        // Zona tomates
        devicesRef.child("zone_tomatoes").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val estadoValvula = snapshot.child("estado_valvula").getValue(Boolean::class.java) ?: false
                val humedad = snapshot.child("humedad").getValue(Int::class.java) ?: 45
                val temperatura = snapshot.child("temperatura").getValue(Int::class.java)?.toDouble() ?: 22.5

                val zoneIndex = zones.indexOfFirst { it.id == "zone_tomatoes" }
                if (zoneIndex != -1) {
                    zones[zoneIndex] = zones[zoneIndex].copy(
                        isActive = estadoValvula,
                        humidity = humedad,
                        temperature = temperatura
                    )
                    zoneAdapter.notifyItemChanged(zoneIndex)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ControlManualActivity,
                    "Error al cargar zona tomates", Toast.LENGTH_SHORT).show()
            }
        })

        // Zona césped
        devicesRef.child("zone_grass").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val estadoValvula = snapshot.child("estado_valvula").getValue(Boolean::class.java) ?: false
                val humedad = snapshot.child("humedad").getValue(Int::class.java) ?: 62
                val temperatura = snapshot.child("temperatura").getValue(Int::class.java)?.toDouble() ?: 23.0

                val zoneIndex = zones.indexOfFirst { it.id == "zone_grass" }
                if (zoneIndex != -1) {
                    zones[zoneIndex] = zones[zoneIndex].copy(
                        isActive = estadoValvula,
                        humidity = humedad,
                        temperature = temperatura
                    )
                    zoneAdapter.notifyItemChanged(zoneIndex)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ControlManualActivity,
                    "Error al cargar zona césped", Toast.LENGTH_SHORT).show()
            }
        })
    }

    /**
     * Configura los listeners
     */
    private fun setupListeners() {
        btnApply.setOnClickListener {
            applyChanges()
        }
    }

    /**
     * Callback cuando cambia el estado de una zona
     */
    private fun onZoneStatusChanged(zone: Zone, isActive: Boolean) {
        // Guardar cambio pendiente
        pendingChanges[zone.id] = isActive

        // Actualizar el botón
        btnApply.isEnabled = pendingChanges.isNotEmpty()
    }

    /**
     * Aplica los cambios pendientes a Firebase
     */
    private fun applyChanges() {
        if (pendingChanges.isEmpty()) {
            Toast.makeText(this, "No hay cambios pendientes", Toast.LENGTH_SHORT).show()
            return
        }

        showLoading(true)

        // Guardar cada cambio en Firebase
        val updates = mutableMapOf<String, Any>()

        pendingChanges.forEach { (zoneId, isActive) ->
            // Actualizar en Firebase con los campos correctos
            updates["$zoneId/estado_valvula"] = isActive
            updates["$zoneId/timestamp"] = System.currentTimeMillis()
            updates["$zoneId/manual"] = true
        }

        devicesRef.updateChildren(updates)
            .addOnSuccessListener {
                showLoading(false)
                Toast.makeText(
                    this,
                    getString(R.string.control_manual_success),
                    Toast.LENGTH_SHORT
                ).show()

                // Limpiar cambios pendientes
                pendingChanges.clear()
                btnApply.isEnabled = false

                // Actualizar timestamps en las zonas
                updateLocalZoneTimestamps()

                // NO navegar automáticamente - dejar que el usuario explore
                // Si quiere volver, usará el botón Atrás o bottom nav
            }
            .addOnFailureListener { e ->
                showLoading(false)
                Toast.makeText(
                    this,
                    "${getString(R.string.control_manual_error)}: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    /**
     * Actualiza los timestamps locales después de aplicar cambios
     */
    private fun updateLocalZoneTimestamps() {
        zones.forEachIndexed { index, zone ->
            if (zone.isActive) {
                zones[index] = zone.copy(lastIrrigation = System.currentTimeMillis())
            }
        }
        zoneAdapter.notifyDataSetChanged()
    }

    /**
     * Volver al Dashboard correctamente sin ciclos
     */
    private fun volverAlDashboard() {
        // Prevenir múltiples llamadas
        if (isFinishing) return

        val intent = Intent(this, DashboardActivity::class.java).apply {
            // Limpiar todo el back stack hasta Dashboard
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP or
                    Intent.FLAG_ACTIVITY_NO_ANIMATION
        }
        startActivity(intent)
        finish()

        // Animación de salida suave
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

    /**
     * Muestra u oculta el loading
     */
    private fun showLoading(show: Boolean) {
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
        btnApply.isEnabled = !show && pendingChanges.isNotEmpty()
        rvZones.isEnabled = !show
    }

    /**
     * Configura el BottomNavigationView
     */
    private fun setupBottomNavigation() {
        val bottomNavigation = findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottomNavigation)

        bottomNavigation.selectedItemId = R.id.nav_irrigation

        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    val intent = Intent(this, DashboardActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    }
                    startActivity(intent)
                    true
                }
                R.id.nav_irrigation -> {
                    // Ya estamos aquí
                    true
                }
                R.id.nav_history -> {
                    startActivity(Intent(this, HistorialActivity::class.java))
                    finish()
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
}