package com.hortechia.smartriego.ui

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.hortechia.smartriego.ConfiguracionActivity
import com.hortechia.smartriego.HistorialActivity
import com.hortechia.smartriego.ProgramacionActivity
import com.hortechia.smartriego.R
import com.hortechia.smartriego.adapter.ZoneAdapter
import com.hortechia.smartriego.databinding.ActivityDashboardBinding
import com.hortechia.smartriego.model.Zone
import com.hortechia.smartriego.utils.PermisosHelper
import com.hortechia.smartriego.utils.InterconexionHelper
import com.hortechia.smartriego.network.WeatherRepository
import com.hortechia.smartriego.models.WeatherData
import com.hortechia.smartriego.models.Recommendation
import kotlinx.coroutines.launch

class DashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var devicesRef: DatabaseReference
    private lateinit var deviceId: String

    // Helpers
    private lateinit var permisosHelper: PermisosHelper
    private lateinit var interconexionHelper: InterconexionHelper
    private lateinit var weatherRepository: WeatherRepository

    private val zones = mutableListOf<Zone>()
    private lateinit var zoneAdapter: ZoneAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Inflar diseño
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 2. Configurar Toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = "HortechIA"

        // 3. Inicializar Firebase
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        deviceId = "0qe3XVfFnWREiNqQMNpz4tlYKi2"
        devicesRef = database.reference.child("devices").child(deviceId)

        // 4. Inicializar Helpers
        interconexionHelper = InterconexionHelper(this)
        weatherRepository = WeatherRepository()

        // 5. Permisos
        permisosHelper = PermisosHelper(this)
        permisosHelper.solicitarPermisosNecesarios()

        // 6. Configuración UI
        setupRecyclerView()
        setupListeners()
        setupBottomNavigation()

        // 7. Cargar Datos
        loadUserData()
        loadWeather()
        loadZones()
    }

    override fun onResume() {
        super.onResume()
        updateZonesFromFirebase()
    }

    // --- MENÚ ---
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_profile -> {
                startActivity(Intent(this, ProfileActivity::class.java))
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

    // --- LÓGICA UI ---
    private fun setupRecyclerView() {
        zones.clear()
        zones.add(Zone("zone_tomatoes", "Zona Tomates", 0, 0.0, false, 0, R.drawable.ic_zone_tomato))
        zones.add(Zone("zone_grass", "Zona Pasto", 0, 0.0, false, 0, R.drawable.ic_zone_grass))

        zoneAdapter = ZoneAdapter(zones) { zone ->
            irAControlManual()
        }

        binding.rvZones.apply {
            layoutManager = LinearLayoutManager(this@DashboardActivity)
            adapter = zoneAdapter
        }
    }

    private fun loadZones() {
        devicesRef.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) { updateZoneData(snapshot) }
            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) { updateZoneData(snapshot) }
            override fun onChildRemoved(snapshot: DataSnapshot) {}
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun updateZonesFromFirebase() {
        devicesRef.get().addOnSuccessListener { snapshot ->
            snapshot.children.forEach { updateZoneData(it) }
        }
    }

    private fun updateZoneData(snapshot: DataSnapshot) {
        if (isFinishing || isDestroyed) return
        try {
            val key = snapshot.key ?: return
            if (key == "zone_tomatoes" || key == "zone_grass") {
                val humedad = snapshot.child("humedad").getValue(Int::class.java) ?: 0
                val temp = snapshot.child("temperatura").getValue(Int::class.java)?.toDouble() ?: 0.0
                val estado = snapshot.child("estado_valvula").getValue(Boolean::class.java) ?: false

                val index = zones.indexOfFirst { it.id == key }
                if (index != -1) {
                    zones[index] = zones[index].copy(humidity = humedad, temperature = temp, isActive = estado)
                    zoneAdapter.notifyItemChanged(index)
                }
                binding.tvNoZones.visibility = View.GONE
                binding.rvZones.visibility = View.VISIBLE
            }
        } catch (e: Exception) { Log.e("Dashboard", "Error actualizando: ${e.message}") }
    }

    private fun loadUserData() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            database.reference.child("users").child(userId).get().addOnSuccessListener { snapshot ->
                val name = snapshot.child("name").getValue(String::class.java)
                if (!name.isNullOrEmpty()) binding.tvUserName.text = name
            }
        }
    }

    // --- CLIMA (AQUÍ ESTÁ LA CORRECCIÓN) ---
    private fun loadWeather() {
        lifecycleScope.launch {
            try {
                val response = weatherRepository.getCopiapóWeather()
                if (response != null) {
                    val weatherData = WeatherData(
                        "Copiapó, Atacama",
                        response.main.temp.toInt(),
                        response.weather.firstOrNull()?.description?.capitalize() ?: "Despejado",
                        response.main.humidity,
                        (response.wind.speed * 3.6).toInt(),
                        "☀️",
                        generateRecommendations(response.main.temp.toInt(), response.main.humidity)
                    )
                    updateWeatherUI(weatherData)
                }
            } catch (e: Exception) { Log.e("Clima", "Error cargando clima") }
        }
    }

    private fun updateWeatherUI(weather: WeatherData) {
        // CORRECCIÓN: Accedemos directamente a las propiedades del binding
        // No usamos findViewById porque ViewBinding ya hizo el trabajo
        with(binding.cardClima) {
            tvCiudad.text = weather.city
            tvTemperatura.text = "${weather.temperature}°C"
            tvHumedad.text = "Humedad: ${weather.humidity}%"
            tvViento.text = "Viento: ${weather.windSpeed} km/h"

            // Para las recomendaciones dinámicas, limpiamos y agregamos
            llRecomendaciones.removeAllViews()
            weather.recommendations.forEach {
                val tv = TextView(this@DashboardActivity)
                tv.text = "${it.icon} ${it.title}"
                tv.textSize = 14f
                tv.setTextColor(Color.DKGRAY) // Usamos color estándar para evitar errores
                llRecomendaciones.addView(tv)
            }
        }
    }

    private fun generateRecommendations(temp: Int, humidity: Int): List<Recommendation> {
        val list = mutableListOf<Recommendation>()
        val shouldWater = temp > 22 && humidity < 70
        list.add(Recommendation("Regar", shouldWater, if (shouldWater) "✅" else "❌"))
        return list
    }

    // --- NAVEGACIÓN ---
    private fun setupListeners() {
        binding.btnControlManual.setOnClickListener { irAControlManual() }
    }

    private fun irAControlManual() {
        val intent = Intent(this, ControlManualActivity::class.java)
        startActivity(intent)
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.selectedItemId = R.id.nav_home
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> true
                R.id.nav_irrigation -> { irAControlManual(); true }
                R.id.nav_history -> { startActivity(Intent(this, HistorialActivity::class.java)); true }
                R.id.nav_schedule -> { startActivity(Intent(this, ProgramacionActivity::class.java)); true }
                R.id.nav_settings -> { startActivity(Intent(this, ConfiguracionActivity::class.java)); true }
                else -> false
            }
        }
    }

    private fun compartirReporte() {
        if (zones.size >= 2) {
            interconexionHelper.compartirReporteRiego(zones[0].humidity, zones[1].humidity, "Activo", "Inactivo")
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PermisosHelper.REQUEST_LOCATION && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Ubicación Permitida", Toast.LENGTH_SHORT).show()
        }
    }
}