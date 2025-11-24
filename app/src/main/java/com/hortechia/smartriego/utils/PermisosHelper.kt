package com.hortechia.smartriego.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class PermisosHelper(private val activity: Activity) {

    companion object {
        const val REQUEST_LOCATION = 100
        const val REQUEST_NOTIFICATIONS = 101
    }

    /**
     * Solicitar todos los permisos necesarios
     */
    fun solicitarPermisosNecesarios() {
        // Permisos de ubicación (para WiFi scanning)
        if (!tienePermisoUbicacion()) {
            solicitarPermisoUbicacion()
        }

        // Permisos de notificaciones (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!tienePermisoNotificaciones()) {
                solicitarPermisoNotificaciones()
            }
        }
    }

    // ===== UBICACIÓN =====

    private fun tienePermisoUbicacion(): Boolean {
        return ContextCompat.checkSelfPermission(
            activity,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun solicitarPermisoUbicacion() {
        // Mostrar explicación antes de solicitar
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                activity,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        ) {
            mostrarDialogoExplicacionUbicacion()
        } else {
            // Solicitar directamente
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION
            )
        }
    }

    private fun mostrarDialogoExplicacionUbicacion() {
        AlertDialog.Builder(activity)
            .setTitle("Permiso de Ubicación")
            .setMessage(
                "SmartRiego necesita acceso a la ubicación para:\n\n" +
                        "• Detectar redes WiFi cercanas\n" +
                        "• Conectar con tu dispositivo ESP32\n" +
                        "• Optimizar el riego según tu zona geográfica\n\n" +
                        "No se guardará tu ubicación exacta."
            )
            .setPositiveButton("Permitir") { _, _ ->
                ActivityCompat.requestPermissions(
                    activity,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    REQUEST_LOCATION
                )
            }
            .setNegativeButton("Ahora no", null)
            .show()
    }

    // ===== NOTIFICACIONES =====

    private fun tienePermisoNotificaciones(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true // No necesario en versiones anteriores
        }
    }

    private fun solicitarPermisoNotificaciones() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    activity,
                    Manifest.permission.POST_NOTIFICATIONS
                )
            ) {
                mostrarDialogoExplicacionNotificaciones()
            } else {
                ActivityCompat.requestPermissions(
                    activity,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    REQUEST_NOTIFICATIONS
                )
            }
        }
    }

    private fun mostrarDialogoExplicacionNotificaciones() {
        AlertDialog.Builder(activity)
            .setTitle("Permiso de Notificaciones")
            .setMessage(
                "SmartRiego te enviará notificaciones para:\n\n" +
                        "• Alertas de humedad baja\n" +
                        "• Inicio y fin de riegos programados\n" +
                        "• Problemas de conexión con tu sistema\n\n" +
                        "Puedes desactivarlas en Configuración."
            )
            .setPositiveButton("Permitir") { _, _ ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    ActivityCompat.requestPermissions(
                        activity,
                        arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                        REQUEST_NOTIFICATIONS
                    )
                }
            }
            .setNegativeButton("Ahora no", null)
            .show()
    }

    /**
     * Verificar si todos los permisos necesarios están concedidos
     */
    fun tienePermisosNecesarios(): Boolean {
        val ubicacion = tienePermisoUbicacion()
        val notificaciones = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            tienePermisoNotificaciones()
        } else {
            true
        }
        return ubicacion && notificaciones
    }
}