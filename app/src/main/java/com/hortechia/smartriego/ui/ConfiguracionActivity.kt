package com.hortechia.smartriego

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.hortechia.smartriego.databinding.ActivityConfiguracionBinding
import com.hortechia.smartriego.ui.ControlManualActivity
import com.hortechia.smartriego.ui.DashboardActivity
import com.hortechia.smartriego.ui.LoginActivity

class ConfiguracionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityConfiguracionBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityConfiguracionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inicializar Firebase
        auth = FirebaseAuth.getInstance()

        // Inicializar SharedPreferences
        sharedPreferences = getSharedPreferences("SmartRiegoPrefs", MODE_PRIVATE)

        setupToolbar()
        loadUserData()
        loadPreferences()
        setupListeners()
        setupPrivacyButtons()
        setupBottomNavigation()
        setupBackHandler() // ← NUEVO
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            volverAlDashboard() // ← CAMBIADO de finish()
        }
    }

    private fun loadUserData() {
        val user = auth.currentUser
        if (user != null) {
            binding.tvEmail.text = user.email ?: "No disponible"
        }
    }

    private fun loadPreferences() {
        // Cargar preferencias guardadas
        binding.switchNotificaciones.isChecked =
            sharedPreferences.getBoolean("notificaciones_activas", true)
        binding.switchRiegosIniciados.isChecked =
            sharedPreferences.getBoolean("notif_riegos_iniciados", true)
        binding.switchRiegosCompletados.isChecked =
            sharedPreferences.getBoolean("notif_riegos_completados", true)
        binding.switchAlertasHumedad.isChecked =
            sharedPreferences.getBoolean("notif_alertas_humedad", true)

        binding.tvHumedadMinima.text =
            "${sharedPreferences.getInt("umbral_minimo", 30)}%"
        binding.tvHumedadOptima.text =
            "${sharedPreferences.getInt("umbral_optimo_min", 60)}-${sharedPreferences.getInt("umbral_optimo_max", 70)}%"
    }

    private fun setupListeners() {
        // Mi Sistema
        binding.layoutNombreSistema.setOnClickListener {
            Toast.makeText(this, "Editar nombre del sistema - Próximamente", Toast.LENGTH_SHORT).show()
        }

        binding.layoutDispositivos.setOnClickListener {
            Toast.makeText(this, "Gestionar dispositivos - Próximamente", Toast.LENGTH_SHORT).show()
        }

        // Notificaciones
        binding.switchNotificaciones.setOnCheckedChangeListener { _, isChecked ->
            savePreference("notificaciones_activas", isChecked)

            // Deshabilitar todos si se desactiva el principal
            if (!isChecked) {
                binding.switchRiegosIniciados.isChecked = false
                binding.switchRiegosCompletados.isChecked = false
                binding.switchAlertasHumedad.isChecked = false
            }
        }

        binding.switchRiegosIniciados.setOnCheckedChangeListener { _, isChecked ->
            savePreference("notif_riegos_iniciados", isChecked)
        }

        binding.switchRiegosCompletados.setOnCheckedChangeListener { _, isChecked ->
            savePreference("notif_riegos_completados", isChecked)
        }

        binding.switchAlertasHumedad.setOnCheckedChangeListener { _, isChecked ->
            savePreference("notif_alertas_humedad", isChecked)
        }

        // Umbrales
        binding.layoutHumedadMinima.setOnClickListener {
            mostrarDialogHumedadMinima()
        }

        binding.layoutHumedadOptima.setOnClickListener {
            mostrarDialogHumedadOptima()
        }

        // Cuenta
        binding.layoutCambiarPassword.setOnClickListener {
            cambiarPassword()
        }

        binding.layoutCerrarSesion.setOnClickListener {
            cerrarSesion()
        }

        // Aplicación
        binding.layoutTema.setOnClickListener {
            Toast.makeText(this, "Cambiar tema - Próximamente", Toast.LENGTH_SHORT).show()
        }

        binding.layoutIdioma.setOnClickListener {
            Toast.makeText(this, "Cambiar idioma - Próximamente", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupPrivacyButtons() {
        // Botones de Privacy by Design
        binding.btnExportarDatos.setOnClickListener {
            exportarDatos()
        }

        binding.btnEliminarCuenta.setOnClickListener {
            eliminarCuenta()
        }
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.selectedItemId = R.id.nav_settings

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    val intent = Intent(this, DashboardActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    }
                    startActivity(intent) // ← ACTUALIZADO con flags
                    finish()
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
                    // Ya estamos aquí
                    true
                }
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
        val rangos = arrayOf(
            Pair(50, 60),
            Pair(60, 70),
            Pair(70, 80)
        )

        val minActual = sharedPreferences.getInt("umbral_optimo_min", 60)
        val maxActual = sharedPreferences.getInt("umbral_optimo_max", 70)

        val seleccionActual = rangos.indexOfFirst { it.first == minActual && it.second == maxActual }

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
                    .addOnSuccessListener {
                        Toast.makeText(
                            this,
                            "Correo enviado. Revisa tu bandeja de entrada.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(
                            this,
                            "Error al enviar correo: ${e.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun cerrarSesion() {
        AlertDialog.Builder(this)
            .setTitle("Cerrar sesión")
            .setMessage("¿Estás seguro de que deseas cerrar sesión?")
            .setPositiveButton("Cerrar sesión") { _, _ ->
                // Cerrar sesión en Firebase
                auth.signOut()

                // Limpiar preferencias si es necesario
                sharedPreferences.edit().clear().apply()

                // Ir a Login (esta navegación está bien, no modificar)
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()

                Toast.makeText(this, "Sesión cerrada", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    /**
     * Eliminar cuenta del usuario (Derecho al olvido - GDPR Art. 17)
     */
    private fun eliminarCuenta() {
        AlertDialog.Builder(this)
            .setTitle("⚠️ Eliminar Cuenta")
            .setMessage("¿Estás seguro de eliminar tu cuenta?\n\nEsta acción es PERMANENTE e IRREVERSIBLE.\n\nSe eliminarán:\n• Tu perfil de usuario\n• Historial completo de riego\n• Todas las configuraciones\n• Datos de dispositivos vinculados\n• Programaciones guardadas")
            .setPositiveButton("Eliminar Permanentemente") { _, _ ->
                eliminarCuentaConfirmado()
            }
            .setNegativeButton("Cancelar", null)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show()
    }

    /**
     * Eliminar cuenta confirmada
     */
    private fun eliminarCuentaConfirmado() {
        val user = auth.currentUser
        val userId = user?.uid

        if (userId != null) {
            // Mostrar loading
            Toast.makeText(this, "Eliminando cuenta...", Toast.LENGTH_SHORT).show()

            // Eliminar datos de Firebase Realtime Database
            val database = FirebaseDatabase.getInstance()

            database.reference.child("users").child(userId).removeValue()
            database.reference.child("devices").child(userId).removeValue()
            database.reference.child("irrigation_history").child(userId).removeValue()
            database.reference.child("zones").child(userId).removeValue()

            // Eliminar autenticación Firebase (esta navegación está bien, no modificar)
            user.delete().addOnSuccessListener {
                Toast.makeText(this, "✅ Cuenta eliminada correctamente", Toast.LENGTH_LONG).show()

                // Limpiar preferencias locales
                sharedPreferences.edit().clear().apply()

                // Ir a Login
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }.addOnFailureListener { e ->
                Toast.makeText(this, "❌ Error al eliminar cuenta: ${e.message}", Toast.LENGTH_LONG).show()
            }
        } else {
            Toast.makeText(this, "Error: Usuario no autenticado", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Exportar datos del usuario (Portabilidad - GDPR Art. 20)
     */
    private fun exportarDatos() {
        val user = auth.currentUser
        val userId = user?.uid

        if (userId != null) {
            Toast.makeText(this, "📤 Exportando datos...", Toast.LENGTH_SHORT).show()

            val database = FirebaseDatabase.getInstance()
            database.reference.child("users").child(userId).get().addOnSuccessListener { snapshot ->

                val timestamp = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())

                val datos = """
═══════════════════════════════════════════════
    DATOS DE USUARIO - HORTECHIA SMARTRIEGO
═══════════════════════════════════════════════

📧 EMAIL: ${snapshot.child("email").value ?: "N/A"}
👤 NOMBRE: ${snapshot.child("name").value ?: "N/A"}
🔑 ROL: ${snapshot.child("role").value ?: "usuario"}
📅 FECHA CREACIÓN: ${snapshot.child("createdAt").value ?: "N/A"}
🆔 USER ID: $userId

═══════════════════════════════════════════════
             INFORMACIÓN LEGAL
═══════════════════════════════════════════════

✅ Datos exportados según GDPR (UE) 2016/679
✅ Derecho a la portabilidad (Art. 20)
✅ Formato legible y portable

📅 Fecha de exportación: $timestamp
🏢 Organización: HortechIA SmartRiego
📧 Soporte: soporte@hortechia.com

═══════════════════════════════════════════════

Estos son TODOS los datos personales que 
almacenamos sobre tu cuenta. Puedes usar esta 
información para migrar a otro sistema o como 
respaldo personal.

Para más información sobre privacidad:
https://hortechia.com/privacy
                """.trimIndent()

                // Compartir datos mediante Intent
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_SUBJECT, "Mis Datos Personales - SmartRiego")
                    putExtra(Intent.EXTRA_TEXT, datos)
                }

                startActivity(Intent.createChooser(intent, "📤 Exportar datos vía"))

            }.addOnFailureListener { e ->
                Toast.makeText(this, "❌ Error al exportar datos: ${e.message}", Toast.LENGTH_LONG).show()
            }
        } else {
            Toast.makeText(this, "Error: Usuario no autenticado", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Volver al Dashboard correctamente sin ciclos
     */
    private fun volverAlDashboard() {
        if (isFinishing) return

        val intent = Intent(this, DashboardActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP or
                    Intent.FLAG_ACTIVITY_NO_ANIMATION
        }
        startActivity(intent)
        finish()
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
}