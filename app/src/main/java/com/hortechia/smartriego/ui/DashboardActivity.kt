package com.hortechia.smartriego.ui

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
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
import com.hortechia.smartriego.utils.PermisosHelper
import com.hortechia.smartriego.utils.InterconexionHelper
import com.hortechia.smartriego.network.WeatherRepository
import com.hortechia.smartriego.models.WeatherData
import com.hortechia.smartriego.models.Recommendation
import kotlinx.coroutines.launch

/**
 * DashboardActivity - Pantalla principal del dashboard
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

    // Weather variables
    private lateinit var weatherRepository: WeatherRepository
    private lateinit var cardClima: View
    private lateinit var tvCiudad: TextView
    private lateinit var tvIconoClima: TextView
    private lateinit var tvTemperatura: TextView
    private lateinit var tvDescripcion: TextView
    private lateinit var tvHumedad: TextView
    private lateinit var tvViento: TextView
    private lateinit var llRecomendaciones: LinearLayout
    private lateinit var tvActualizacion: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        // Inicializar Firebase
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        deviceId = "0qe3XVfFnWREiNqQMNpz4tlYKi2"

        // Inicializar helpers
        permisosHelper = PermisosHelper(this)
        permisosHelper.solicitarPermisosNecesarios()
        interconexionHelper = InterconexionHelper(this)

        // Inicializar vistas
        initViews()
        initWeatherViews()

        // Configurar
        setupToolbar()
        setupRecyclerView()

        // Cargar datos
        loadUserData()
        loadWeather()
        loadZones()

        // Listeners
        setupListeners()
    }

    private fun initViews() {
        toolbar = findViewById(R.id.toolbar)
        tvUserName = findViewById(R.id.tvUserName)
        rvZones = findViewById(R.id.rvZones)
        tvNoZones = findViewById(R.id.tvNoZones)
        btnControlManual = findViewById(R.id.btnControlManual)
        bottomNavigation = findViewById(R.id.bottomNavigation)
    }

    private fun initWeatherViews() {
        cardClima = findViewById(R.id.cardClima)
        tvCiudad = cardClima.findViewById(R.id.tvCiudad)
        tvIconoClima = cardClima.findViewById(R.id.tvIconoClima)
        tvTemperatura = cardClima.findViewById(R.id.tvTemperatura)
        tvDescripcion = cardClima.findViewById(R.id.tvDescripcion)
        tvHumedad = cardClima.findViewById(R.id.tvHumedad)
        tvViento = cardClima.findViewById(R.id.tvViento)
        llRecomendaciones = cardClima.findViewById(R.id.llRecomendaciones)
        tvActualizacion = cardClima.findViewById(R.id.tvActualizacion)
        weatherRepository = WeatherRepository()
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
    }

    private fun setupRecyclerView() {
        zoneAdapter = ZoneAdapter(zones) { zone ->
            onZoneClick(zone)
        }
        rvZones.apply {
            layoutManager = LinearLayoutManager(this@DashboardActivity)
            adapter = zoneAdapter
        }
    }

    private fun loadUserData() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            database.reference.child("users").child(userId).get().addOnSuccessListener { snapshot ->
                val name = snapshot.child("name").getValue(String::class.java)
                if (!name.isNullOrEmpty()) {
                    tvUserName.text = name
                }
            }
        }
    }

    private fun loadWeather() {
        lifecycleScope.launch {
            try {
                val response = weatherRepository.getCopiapóWeather()
                if (response != null) {
                    val weatherData = WeatherData(
                        city = "Copiapó, Atacama",
                        temperature = response.main.temp.toInt(),
                        description = response.weather.firstOrNull()?.description?.capitalize() ?: "Despejado",
                        humidity = response.main.humidity,
                        windSpeed = (response.wind.speed * 3.6).toInt(),
                        icon = getWeatherEmoji(response.weather.firstOrNull()?.main ?: "Clear"),
                        recommendations = generateRecommendations(
                            response.main.temp.toInt(),
                            response.main.humidity,
                            response.wind.speed
                        )
                    )
                    updateWeatherUI(weatherData)
                } else {
                    showMockWeather()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                showMockWeather()
            }
        }
    }

    private fun updateWeatherUI(weather: WeatherData) {
        tvCiudad.text = weather.city
        tvIconoClima.text = weather.icon
        tvTemperatura.text = "${weather.temperature}°C"
        tvDescripcion.text = weather.description
        tvHumedad.text = "Humedad: ${weather.humidity}%"
        tvViento.text = "Viento: ${weather.windSpeed} km/h"

        llRecomendaciones.removeAllViews()
        weather.recommendations.forEach { recommendation ->
            addRecommendationView(recommendation)
        }

        tvActualizacion.text = "Actualizado ahora"
    }

    private fun showMockWeather() {
        val mockWeather = WeatherData(
            city = "Copiapó, Atacama",
            temperature = 24,
            description = "Soleado",
            humidity = 65,
            windSpeed = 12,
            icon = "☀️",
            recommendations = listOf(
                Recommendation("Regar", true, "✅"),
                Recommendation("Cosechar", true, "✅"),
                Recommendation("Plantar", false, "❌")
            )
        )
        updateWeatherUI(mockWeather)
    }

    private fun generateRecommendations(temp: Int, humidity: Int, windSpeed: Double): List<Recommendation> {
        val recommendations = mutableListOf<Recommendation>()

        val shouldWater = temp > 22 && humidity < 70
        recommendations.add(Recommendation("Regar", shouldWater, if (shouldWater) "✅" else "❌"))

        val shouldHarvest = temp in 18..28 && humidity in 50..80
        recommendations.add(Recommendation("Cosechar", shouldHarvest, if (shouldHarvest) "✅" else "❌"))

        val shouldPlant = temp in 15..25 && humidity > 60 && windSpeed < 5
        recommendations.add(Recommendation("Plantar", shouldPlant, if (shouldPlant) "✅" else "❌"))

        return recommendations
    }

    private fun addRecommendationView(recommendation: Recommendation) {
        val textView = TextView(this).apply {
            text = "${recommendation.icon} ${recommendation.title}"
            textSize = 14f
            setTextColor(
                if (recommendation.isRecommended)
                    ContextCompat.getColor(this@DashboardActivity, R.color.verde_primary)
                else
                    ContextCompat.getColor(this@DashboardActivity, R.color.texto_secundario)
            )
            setPadding(0, 8, 0, 8)
        }
        llRecomendaciones.addView(textView)
    }

    private fun getWeatherEmoji(condition: String): String {
        return when (condition.lowercase()) {
            "clear" -> "☀️"
            "clouds" -> "☁️"
            "rain", "drizzle" -> "🌧️"
            "thunderstorm" -> "⛈️"
            "snow" -> "❄️"
            "mist", "fog", "haze" -> "🌫️"
            else -> "🌤️"
        }
    }

    private fun loadZones() {
        // Cargar zonas mock SOLO si está vacío
        if (zones.isEmpty()) {
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
        }

        // Siempre mostrar RecyclerView
        tvNoZones.visibility = View.GONE
        rvZones.visibility = View.VISIBLE
        zoneAdapter.notifyDataSetChanged()

        // CORRECCIÓN: Eliminado rvZones.minimumHeight = 400
        // Ya no es necesario con NestedScrollView

        // Conectar Firebase
        loadZonesFromFirebase()
    }

    private fun loadZonesFromFirebase() {
        // TOMATES - Listener
        database.reference
            .child("devices")
            .child(deviceId)
            .child("zone_tomatoes")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    try {
                        val humedad = snapshot.child("humedad").getValue(Int::class.java) ?: 45
                        val temperatura = snapshot.child("temperatura").getValue(Int::class.java)?.toDouble() ?: 22.5
                        val estadoValvula = snapshot.child("estado_valvula").getValue(Boolean::class.java) ?: false

                        android.util.Log.d("Dashboard", "Tomates - Humedad: $humedad, Temp: $temperatura, Válvula: $estadoValvula")

                        val zoneIndex = zones.indexOfFirst { it.id == "zone_tomatoes" }
                        if (zoneIndex != -1) {
                            zones[zoneIndex] = zones[zoneIndex].copy(
                                humidity = humedad,
                                temperature = temperatura,
                                isActive = estadoValvula
                            )

                            runOnUiThread {
                                zoneAdapter.notifyItemChanged(zoneIndex)
                                android.util.Log.d("Dashboard", "✅ Tomates actualizado en UI")
                            }
                        } else {
                            android.util.Log.e("Dashboard", "❌ Zona tomates NO encontrada")
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("Dashboard", "Error en tomates: ${e.message}")
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    android.util.Log.w("Dashboard", "Listener tomates cancelado: ${error.message}")
                }
            })

        // CÉSPED - Listener
        database.reference
            .child("devices")
            .child(deviceId)
            .child("zone_grass")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    try {
                        val humedad = snapshot.child("humedad").getValue(Int::class.java) ?: 62
                        val temperatura = snapshot.child("temperatura").getValue(Int::class.java)?.toDouble() ?: 23.0
                        val estadoValvula = snapshot.child("estado_valvula").getValue(Boolean::class.java) ?: false

                        android.util.Log.d("Dashboard", "Césped - Humedad: $humedad, Temp: $temperatura, Válvula: $estadoValvula")

                        val zoneIndex = zones.indexOfFirst { it.id == "zone_grass" }
                        if (zoneIndex != -1) {
                            zones[zoneIndex] = zones[zoneIndex].copy(
                                humidity = humedad,
                                temperature = temperatura,
                                isActive = estadoValvula
                            )

                            runOnUiThread {
                                zoneAdapter.notifyItemChanged(zoneIndex)
                                android.util.Log.d("Dashboard", "✅ Césped actualizado en UI")
                            }
                        } else {
                            android.util.Log.e("Dashboard", "❌ Zona césped NO encontrada")
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("Dashboard", "Error en césped: ${e.message}")
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    android.util.Log.w("Dashboard", "Listener césped cancelado: ${error.message}")
                }
            })
    }

    private fun setupListeners() {
        btnControlManual.setOnClickListener {
            startActivity(Intent(this, ControlManualActivity::class.java))
        }

        bottomNavigation.selectedItemId = R.id.nav_home
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> true
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

    private fun onZoneClick(zone: Zone) {
        Toast.makeText(this, "${zone.name} - ${zone.getStatusText()}", Toast.LENGTH_SHORT).show()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar_menu, menu)
        return true
    }

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

    // CORRECCIÓN: Método onResume limpio y eficiente
    override fun onResume() {
        super.onResume()
        android.util.Log.d("Dashboard", "=== onResume() ===")
        android.util.Log.d("Dashboard", "Zonas en lista: ${zones.size}")

        // NO volvemos a cargar las zonas aquí porque el listener de Firebase ya está activo
        // y la lista 'zones' persiste en memoria mientras la Activity viva.

        if (zones.isNotEmpty()) {
            rvZones.visibility = View.VISIBLE
            tvNoZones.visibility = View.GONE

            // Simplemente notificamos al adaptador por si hubo cambios visuales rápidos
            zoneAdapter.notifyDataSetChanged()

            android.util.Log.d("Dashboard", "✅ Vista actualizada (sin recrear Adapter)")
        }
    }
}