package com.hortechia.smartriego.ui

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

        val userId = auth.currentUser?.uid
        if (userId != null) {
            devicesRef = database.reference.child("devices").child(userId)
        }

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
            finish()
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

        // TODO: Cargar desde Firebase cuando esté listo el ESP32
        // Por ahora usamos datos simulados
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
        val userId = auth.currentUser?.uid ?: return

        devicesRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                zones.forEachIndexed { index, zone ->
                    val zoneSnapshot = snapshot.child(zone.id)
                    val status = zoneSnapshot.child("status").getValue(String::class.java)

                    if (status != null) {
                        zones[index] = zone.copy(isActive = status == "on")
                    }
                }
                zoneAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    this@ControlManualActivity,
                    "Error al cargar datos: ${error.message}",
                    Toast.LENGTH_SHORT
                ).show()
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

        val userId = auth.currentUser?.uid
        if (userId == null) {
            showLoading(false)
            Toast.makeText(this, "Error: Usuario no autenticado", Toast.LENGTH_SHORT).show()
            return
        }

        // Guardar cada cambio en Firebase
        val updates = mutableMapOf<String, Any>()

        pendingChanges.forEach { (zoneId, isActive) ->
            val status = if (isActive) "on" else "off"
            val timestamp = System.currentTimeMillis()

            updates["$zoneId/status"] = status
            updates["$zoneId/timestamp"] = timestamp
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
     * Muestra u oculta el loading
     */
    private fun showLoading(show: Boolean) {
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
        btnApply.isEnabled = !show && pendingChanges.isNotEmpty()
        rvZones.isEnabled = !show
    }
}