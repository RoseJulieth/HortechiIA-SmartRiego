package com.hortechia.smartriego.ui

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
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
import com.hortechia.smartriego.HistorialActivity
import com.hortechia.smartriego.ProgramacionActivity
import com.hortechia.smartriego.ConfiguracionActivity
import com.hortechia.smartriego.ui.ControlManualActivity
import com.hortechia.smartriego.databinding.ActivityDashboardBinding
import com.hortechia.smartriego.utils.PermisosHelper
import com.hortechia.smartriego.utils.InterconexionHelper

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

    private lateinit var permisosHelper: PermisosHelper
    private lateinit var interconexionHelper: InterconexionHelper
    private lateinit var toolbar: MaterialToolbar
    private lateinit var tvUserName: TextView
    private lateinit var rvZones: RecyclerView
    private lateinit var tvNoZones: TextView
    private lateinit var btnControlManual: Button
    private lateinit var bottomNavigation: BottomNavigationView

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var deviceId: String

    private val zones = mutableListOf<Zone>()
    private lateinit var zoneAdapter: ZoneAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        // Inicializar Firebase
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        // Usar Device ID del ESP32
        deviceId = "0qe3XVfFnWREiNqQMNpz4tlYKi2"

        // Inicializar helpers
        permisosHelper = PermisosHelper(this)
        permisosHelper.solicitarPermisosNecesarios()

        interconexionHelper = InterconexionHelper(this)

        // Inicializar vistas
        initViews()

        // Configurar toolbar
        setupToolbar()

        // Configurar RecyclerView
        setupRecyclerView()

        // Cargar datos del usuario
        loadUserData()

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
     * Carga las zonas desde Firebase en tiempo real
     */
    private fun loadZones() {
        // Mantener las zonas simuladas como base
        zones.clear()
        zones.addAll(listOf(
            Zone(
                id = "zone_tomatoes",
                name = getString(R.string.zone_tomatoes),
                humidity = 45,
                temperature = 22.5,
                isActive = true,
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

        // Mostrar/ocultar mensaje
        if (zones.isEmpty()) {
            tvNoZones.visibility = android.view.View.VISIBLE
            rvZones.visibility = android.view.View.GONE
        } else {
            tvNoZones.visibility = android.view.View.GONE
            rvZones.visibility = android.view.View.VISIBLE
        }

        // Conectar con Firebase para actualizar datos en tiempo real
        loadZonesFromFirebase()
    }

    /**
     * Conecta con Firebase y actualiza los datos de las zonas en tiempo real
     */
    private fun loadZonesFromFirebase() {
        // Escuchar zona tomates
        database.reference.child("devices").child(deviceId).child("zone_tomatoes")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val humedad = snapshot.child("humedad").getValue(Int::class.java) ?: 45
                    val temperatura = snapshot.child("temperatura").getValue(Int::class.java)?.toDouble() ?: 22.5
                    val estadoValvula = snapshot.child("estado_valvula").getValue(Boolean::class.java) ?: false

                    // Actualizar zona en la lista
                    val zoneIndex = zones.indexOfFirst { it.id == "zone_tomatoes" }
                    if (zoneIndex != -1) {
                        zones[zoneIndex] = zones[zoneIndex].copy(
                            humidity = humedad,
                            temperature = temperatura,
                            isActive = estadoValvula
                        )
                        zoneAdapter.notifyItemChanged(zoneIndex)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@DashboardActivity,
                        "Error al cargar datos de tomates", Toast.LENGTH_SHORT).show()
                }
            })

        // Escuchar zona césped
        database.reference.child("devices").child(deviceId).child("zone_grass")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val humedad = snapshot.child("humedad").getValue(Int::class.java) ?: 62
                    val temperatura = snapshot.child("temperatura").getValue(Int::class.java)?.toDouble() ?: 23.0
                    val estadoValvula = snapshot.child("estado_valvula").getValue(Boolean::class.java) ?: false

                    // Actualizar zona en la lista
                    val zoneIndex = zones.indexOfFirst { it.id == "zone_grass" }
                    if (zoneIndex != -1) {
                        zones[zoneIndex] = zones[zoneIndex].copy(
                            humidity = humedad,
                            temperature = temperatura,
                            isActive = estadoValvula
                        )
                        zoneAdapter.notifyItemChanged(zoneIndex)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@DashboardActivity,
                        "Error al cargar datos de césped", Toast.LENGTH_SHORT).show()
                }
            })
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
                    finish()
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
                Toast.makeText(this, "Perfil - Próximamente", Toast.LENGTH_SHORT).show()
                true
            }
            R.id.action_share -> {
                compartirReporte()
                true
            }
            R.id.action_location -> {
                interconexionHelper.abrirUbicacionEnMaps()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    /**
     * Maneja el resultado de solicitudes de permisos
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            PermisosHelper.REQUEST_LOCATION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Permiso de ubicación concedido", Toast.LENGTH_SHORT).show()
                }
            }
            PermisosHelper.REQUEST_NOTIFICATIONS -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Permiso de notificaciones concedido", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    /**
     * Compartir reporte con datos actuales
     */
    private fun compartirReporte() {
        if (zones.size >= 2) {
            val zona1 = zones.find { it.id == "zone_tomatoes" }
            val zona2 = zones.find { it.id == "zone_grass" }

            if (zona1 != null && zona2 != null) {
                interconexionHelper.compartirReporteRiego(
                    zona1Humedad = zona1.humidity,
                    zona2Humedad = zona2.humidity,
                    zona1Estado = if (zona1.isActive) "Activo" else "Inactivo",
                    zona2Estado = if (zona2.isActive) "Activo" else "Inactivo"
                )
            }
        } else {
            Toast.makeText(this, "Esperando datos de zonas...", Toast.LENGTH_SHORT).show()
        }
    }

}