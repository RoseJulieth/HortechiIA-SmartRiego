package com.hortechia.smartriego

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.hortechia.smartriego.databinding.ActivityConfiguracionBinding
import com.hortechia.smartriego.ui.ControlManualActivity
import com.hortechia.smartriego.ui.DashboardActivity
import com.hortechia.smartriego.ui.LoginActivity
import com.hortechia.smartriego.HistorialActivity

class ConfiguracionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityConfiguracionBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityConfiguracionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        sharedPreferences = getSharedPreferences("SmartRiegoPrefs", MODE_PRIVATE)

        setupToolbar()
        loadUserData()
        loadPreferences()
        setupListeners()
        setupPrivacyButtons()
        setupBottomNavigation()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun loadUserData() {
        val user = auth.currentUser
        if (user != null) {
            binding.tvEmail.text = user.email ?: "No disponible"
        }
    }

    private fun loadPreferences() {
        binding.switchNotificaciones.isChecked = sharedPreferences.getBoolean("notificaciones_activas", true)
        binding.switchRiegosIniciados.isChecked = sharedPreferences.getBoolean("notif_riegos_iniciados", true)
        binding.switchRiegosCompletados.isChecked = sharedPreferences.getBoolean("notif_riegos_completados", true)
        binding.switchAlertasHumedad.isChecked = sharedPreferences.getBoolean("notif_alertas_humedad", true)

        binding.tvHumedadMinima.text = "${sharedPreferences.getInt("umbral_minimo", 30)}%"
        binding.tvHumedadOptima.text = "${sharedPreferences.getInt("umbral_optimo_min", 60)}-${sharedPreferences.getInt("umbral_optimo_max", 70)}%"

        val nombreSistema = sharedPreferences.getString("nombre_sistema", "Mi Jardín")
        binding.tvNombreSistema.text = nombreSistema
    }

    private fun setupListeners() {
        binding.layoutNombreSistema.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Nombre del Sistema")
            val input = EditText(this)
            input.setText(binding.tvNombreSistema.text)
            builder.setView(input)
            builder.setPositiveButton("Guardar") { _, _ ->
                val nuevoNombre = input.text.toString()
                if(nuevoNombre.isNotEmpty()){
                    binding.tvNombreSistema.text = nuevoNombre
                    sharedPreferences.edit().putString("nombre_sistema", nuevoNombre).apply()
                    Toast.makeText(this, "Nombre actualizado", Toast.LENGTH_SHORT).show()
                }
            }
            builder.setNegativeButton("Cancelar", null)
            builder.show()
        }

        binding.layoutDispositivos.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Detalles del Dispositivo")
                .setMessage("🔌 Modelo: ESP32 SmartController\n" +
                        "🆔 ID: ESP-8266-A4F\n" +
                        "🟢 Estado: En Línea\n" +
                        "📶 Señal WiFi: Excelente (-45dBm)\n" +
                        "📅 Última conexión: Hace 2 min")
                .setPositiveButton("Aceptar", null)
                .setNeutralButton("Reiniciar") { _, _ ->
                    Toast.makeText(this, "Reiniciando dispositivo...", Toast.LENGTH_LONG).show()
                }
                .setIcon(android.R.drawable.ic_dialog_info)
                .show()
        }

        binding.switchNotificaciones.setOnCheckedChangeListener { _, isChecked ->
            savePreference("notificaciones_activas", isChecked)
            if (!isChecked) {
                binding.switchRiegosIniciados.isChecked = false
                binding.switchRiegosCompletados.isChecked = false
                binding.switchAlertasHumedad.isChecked = false
            }
        }
        binding.switchRiegosIniciados.setOnCheckedChangeListener { _, isChecked -> savePreference("notif_riegos_iniciados", isChecked) }
        binding.switchRiegosCompletados.setOnCheckedChangeListener { _, isChecked -> savePreference("notif_riegos_completados", isChecked) }
        binding.switchAlertasHumedad.setOnCheckedChangeListener { _, isChecked -> savePreference("notif_alertas_humedad", isChecked) }

        binding.layoutHumedadMinima.setOnClickListener { mostrarDialogHumedadMinima() }
        binding.layoutHumedadOptima.setOnClickListener { mostrarDialogHumedadOptima() }

        binding.layoutCambiarPassword.setOnClickListener { cambiarPassword() }
        binding.layoutCerrarSesion.setOnClickListener { cerrarSesion() }

        binding.layoutTema.setOnClickListener { Toast.makeText(this, "Tema automático (Sistema)", Toast.LENGTH_SHORT).show() }
        binding.layoutIdioma.setOnClickListener { Toast.makeText(this, "Idioma: Español (Chile)", Toast.LENGTH_SHORT).show() }
    }

    private fun setupPrivacyButtons() {
        binding.btnExportarDatos.setOnClickListener { exportarDatos() }
        binding.btnEliminarCuenta.setOnClickListener { eliminarCuenta() }
    }

    // --- CORRECCIÓN: Lógica Real de Exportar ---
    private fun exportarDatos() {
        val user = auth.currentUser
        val report = """
            📋 REPORTE DE DATOS - SMARTRIGO
            --------------------------------
            Usuario: ${user?.email}
            ID: ${user?.uid}
            Fecha: ${java.util.Date()}
            
            Configuración:
            - Notificaciones: ${if (binding.switchNotificaciones.isChecked) "Activas" else "Inactivas"}
            - Humedad Mín: ${binding.tvHumedadMinima.text}
            
            Este archivo se genera conforme al derecho de portabilidad de datos.
        """.trimIndent()

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Mis Datos SmartRiego")
            putExtra(Intent.EXTRA_TEXT, report)
        }
        startActivity(Intent.createChooser(intent, "Guardar o enviar reporte:"))
    }

    // --- CORRECCIÓN: Lógica Real de Eliminar ---
    private fun eliminarCuenta() {
        AlertDialog.Builder(this)
            .setTitle("⚠️ Eliminar cuenta permanentemente")
            .setMessage("Esta acción borrará todos tus datos, historial y configuraciones. No se puede deshacer. ¿Estás seguro?")
            .setPositiveButton("SÍ, ELIMINAR") { _, _ ->
                performDelete()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun performDelete() {
        val user = auth.currentUser
        val uid = user?.uid
        if (uid != null) {
            // 1. Borrar datos de Realtime Database
            FirebaseDatabase.getInstance().reference.child("users").child(uid).removeValue()
        }

        // 2. Borrar usuario de Authentication
        user?.delete()?.addOnSuccessListener {
            Toast.makeText(this, "Cuenta eliminada correctamente", Toast.LENGTH_LONG).show()
            sharedPreferences.edit().clear().apply()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }?.addOnFailureListener { e ->
            Toast.makeText(this, "Error: ${e.message}. Inicia sesión nuevamente para confirmar.", Toast.LENGTH_LONG).show()
        }
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.selectedItemId = R.id.nav_settings
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> { startActivity(Intent(this, DashboardActivity::class.java)); finish(); true }
                R.id.nav_irrigation -> { startActivity(Intent(this, ControlManualActivity::class.java)); finish(); true }
                R.id.nav_history -> { startActivity(Intent(this, HistorialActivity::class.java)); finish(); true }
                R.id.nav_schedule -> { startActivity(Intent(this, ProgramacionActivity::class.java)); finish(); true }
                R.id.nav_settings -> true
                else -> false
            }
        }
    }

    private fun savePreference(key: String, value: Boolean) {
        sharedPreferences.edit().putBoolean(key, value).apply()
    }

    private fun savePreference(key: String, value: Int) {
        sharedPreferences.edit().putInt(key, value).apply()
    }

    private fun mostrarDialogHumedadMinima() {
        val opciones = arrayOf("20%", "25%", "30%", "35%", "40%")
        val valoresActuales = arrayOf(20, 25, 30, 35, 40)
        val valorActual = sharedPreferences.getInt("umbral_minimo", 30)
        val seleccionActual = valoresActuales.indexOf(valorActual)

        AlertDialog.Builder(this)
            .setTitle("Humedad mínima para alerta")
            .setSingleChoiceItems(opciones, seleccionActual) { dialog, which ->
                val nuevoValor = valoresActuales[which]
                savePreference("umbral_minimo", nuevoValor)
                binding.tvHumedadMinima.text = "${nuevoValor}%"
                Toast.makeText(this, "Umbral actualizado a ${nuevoValor}%", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun mostrarDialogHumedadOptima() {
        val opciones = arrayOf("50-60%", "60-70%", "70-80%")
        val rangos = arrayOf(Pair(50, 60), Pair(60, 70), Pair(70, 80))
        val minActual = sharedPreferences.getInt("umbral_optimo_min", 60)
        val maxActual = sharedPreferences.getInt("umbral_optimo_max", 70)
        val seleccionActual = rangos.indexOfFirst { it.first == minActual && it.second == maxActual }.coerceAtLeast(0)

        AlertDialog.Builder(this)
            .setTitle("Rango de humedad óptima")
            .setSingleChoiceItems(opciones, seleccionActual) { dialog, which ->
                val (min, max) = rangos[which]
                savePreference("umbral_optimo_min", min)
                savePreference("umbral_optimo_max", max)
                binding.tvHumedadOptima.text = "$min-$max%"
                Toast.makeText(this, "Rango óptimo actualizado", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun cambiarPassword() {
        val user = auth.currentUser
        val email = user?.email
        if (email.isNullOrEmpty()) {
            Toast.makeText(this, "Error: No se pudo obtener el email", Toast.LENGTH_SHORT).show()
            return
        }
        AlertDialog.Builder(this)
            .setTitle("Cambiar contraseña")
            .setMessage("Se enviará un correo a $email con instrucciones para cambiar tu contraseña.")
            .setPositiveButton("Enviar") { _, _ ->
                auth.sendPasswordResetEmail(email)
                    .addOnSuccessListener { Toast.makeText(this, "Correo enviado. Revisa tu bandeja.", Toast.LENGTH_LONG).show() }
                    .addOnFailureListener { e -> Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show() }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun cerrarSesion() {
        AlertDialog.Builder(this)
            .setTitle("Cerrar sesión")
            .setMessage("¿Estás seguro de que deseas cerrar sesión?")
            .setPositiveButton("Cerrar sesión") { _, _ ->
                auth.signOut()
                sharedPreferences.edit().clear().apply()
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}