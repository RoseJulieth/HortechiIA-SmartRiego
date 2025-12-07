package com.hortechia.smartriego.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.hortechia.smartriego.R
import com.hortechia.smartriego.adapter.ControlZoneAdapter
import com.hortechia.smartriego.model.Zone
import com.hortechia.smartriego.HistorialActivity
import com.hortechia.smartriego.ProgramacionActivity
import com.hortechia.smartriego.ConfiguracionActivity

class ControlManualActivity : AppCompatActivity() {

    private lateinit var toolbar: MaterialToolbar
    private lateinit var rvZones: RecyclerView
    private lateinit var btnApply: Button
    private lateinit var progressBar: ProgressBar

    private lateinit var database: FirebaseDatabase
    private lateinit var devicesRef: DatabaseReference
    private val deviceId = "0qe3XVfFnWREiNqQMNpz4tlYKi2"

    private val zones = mutableListOf<Zone>()
    private lateinit var zoneAdapter: ControlZoneAdapter
    private val pendingChanges = mutableMapOf<String, Boolean>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_control_manual)

        database = FirebaseDatabase.getInstance()
        devicesRef = database.reference.child("devices").child(deviceId)

        initViews()
        setupToolbar()
        setupRecyclerView()
        loadZones()
        setupListeners()
        setupBottomNavigation()

        // Manejo del botón atrás físico
        setupBackHandler()
    }

    private fun initViews() {
        toolbar = findViewById(R.id.toolbar)
        rvZones = findViewById(R.id.rvZones)
        btnApply = findViewById(R.id.btnApply)
        progressBar = findViewById(R.id.progressBar)
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupRecyclerView() {
        zoneAdapter = ControlZoneAdapter(zones) { zone, isActive ->
            onZoneStatusChanged(zone, isActive)
        }
        rvZones.apply {
            layoutManager = LinearLayoutManager(this@ControlManualActivity)
            adapter = zoneAdapter
        }
    }

    private fun loadZones() {
        showLoading(true)
        zones.clear()
        zones.addAll(listOf(
            Zone("zone_tomatoes", "Zona Tomates", 0, 0.0, false, 0, R.drawable.ic_zone_tomato),
            Zone("zone_grass", "Zona Pasto", 0, 0.0, false, 0, R.drawable.ic_zone_grass)
        ))
        zoneAdapter.notifyDataSetChanged()
        showLoading(false)
        listenToFirebase()
    }

    private fun listenToFirebase() {
        devicesRef.child("zone_tomatoes").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) { updateZoneUI("zone_tomatoes", snapshot) }
            override fun onCancelled(e: DatabaseError) {}
        })

        devicesRef.child("zone_grass").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) { updateZoneUI("zone_grass", snapshot) }
            override fun onCancelled(e: DatabaseError) {}
        })
    }

    private fun updateZoneUI(zoneId: String, snapshot: DataSnapshot) {
        val estado = snapshot.child("estado_valvula").getValue(Boolean::class.java) ?: false
        val hum = snapshot.child("humedad").getValue(Int::class.java) ?: 0
        val temp = snapshot.child("temperatura").getValue(Int::class.java)?.toDouble() ?: 0.0

        val index = zones.indexOfFirst { it.id == zoneId }
        if (index != -1) {
            if (!pendingChanges.containsKey(zoneId)) {
                zones[index] = zones[index].copy(isActive = estado, humidity = hum, temperature = temp)
                zoneAdapter.notifyItemChanged(index)
            }
        }
    }

    private fun setupListeners() {
        btnApply.setOnClickListener { applyChanges() }
    }

    private fun onZoneStatusChanged(zone: Zone, isActive: Boolean) {
        pendingChanges[zone.id] = isActive
        btnApply.isEnabled = true
    }

    private fun applyChanges() {
        if (pendingChanges.isEmpty()) return
        showLoading(true)

        val updates = mutableMapOf<String, Any>()

        pendingChanges.forEach { (zoneId, isActive) ->
            updates["$zoneId/estado_valvula"] = isActive
            updates["$zoneId/manual"] = true

            if (isActive) {
                val otherZone = if (zoneId == "zone_tomatoes") "zone_grass" else "zone_tomatoes"
                updates["$otherZone/estado_valvula"] = false
            }
        }

        devicesRef.updateChildren(updates).addOnSuccessListener {
            showLoading(false)
            Toast.makeText(this, "Orden enviada al dispositivo 📡", Toast.LENGTH_SHORT).show()
            pendingChanges.clear()
            btnApply.isEnabled = false
        }.addOnFailureListener {
            showLoading(false)
            Toast.makeText(this, "Error de conexión", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showLoading(show: Boolean) {
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
        rvZones.isEnabled = !show
    }

    private fun setupBackHandler() {
        onBackPressedDispatcher.addCallback(this) {
            finish()
        }
    }

    private fun setupBottomNavigation() {
        val bottomNav = findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottomNavigation)
        bottomNav.selectedItemId = R.id.nav_irrigation

        // ESTA ERA LA PARTE QUE DABA ERROR: Corregida con las llaves y retorno correctos
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    finish()
                    true
                }
                R.id.nav_irrigation -> true
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