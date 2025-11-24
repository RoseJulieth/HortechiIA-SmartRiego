package com.hortechia.smartriego.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast

class InterconexionHelper(private val context: Context) {

    /**
     * Compartir reporte de riego por WhatsApp, Email, etc.
     * Cumple: "Demuestra interconexión efectiva entre aplicaciones móviles"
     */
    fun compartirReporteRiego(
        zona1Humedad: Int,
        zona2Humedad: Int,
        zona1Estado: String,
        zona2Estado: String
    ) {
        val reporte = """
            📊 REPORTE SMARTRIEGO 🌱
            
            === Zona 1: Césped ===
            💧 Humedad: $zona1Humedad%
            🚿 Estado: $zona1Estado
            
            === Zona 2: Tomates ===
            💧 Humedad: $zona2Humedad%
            🚿 Estado: $zona2Estado
            
            🕐 Fecha: ${java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault()).format(java.util.Date())}
            
            Generado por SmartRiego - Sistema de Riego Inteligente
        """.trimIndent()

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, reporte)
            putExtra(Intent.EXTRA_SUBJECT, "Reporte SmartRiego")
        }

        val chooser = Intent.createChooser(intent, "Compartir reporte vía:")
        context.startActivity(chooser)
    }

    /**
     * Compartir por WhatsApp específicamente
     */
    fun compartirPorWhatsApp(mensaje: String) {
        try {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                setPackage("com.whatsapp")
                putExtra(Intent.EXTRA_TEXT, mensaje)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "WhatsApp no está instalado", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Abrir Google Maps con ubicación del sistema
     * Cumple: "Conexión inalámbrica entre apps"
     */
    fun abrirUbicacionEnMaps(latitud: Double = -27.3667, longitud: Double = -70.3333) {
        // Coordenadas de ejemplo: Copiapó, Atacama
        val uri = "geo:$latitud,$longitud?q=$latitud,$longitud(Mi Sistema SmartRiego)"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri)).apply {
            setPackage("com.google.android.apps.maps")
        }

        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            // Si Google Maps no está instalado, abrir en navegador
            val webUri = "https://www.google.com/maps/search/?api=1&query=$latitud,$longitud"
            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(webUri))
            context.startActivity(webIntent)
        }
    }

    /**
     * Enviar reporte por Email
     */
    fun enviarReportePorEmail(
        emailDestino: String = "",
        zona1Humedad: Int,
        zona2Humedad: Int
    ) {
        val asunto = "Reporte SmartRiego - ${java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault()).format(java.util.Date())}"

        val cuerpo = """
            Estimado usuario,
            
            Adjunto el reporte automático de su sistema SmartRiego:
            
            ZONA 1 (Césped): $zona1Humedad% de humedad
            ZONA 2 (Tomates): $zona2Humedad% de humedad
            
            Sistema operando correctamente.
            
            Saludos,
            SmartRiego IoT
        """.trimIndent()

        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:$emailDestino")
            putExtra(Intent.EXTRA_SUBJECT, asunto)
            putExtra(Intent.EXTRA_TEXT, cuerpo)
        }

        try {
            context.startActivity(Intent.createChooser(intent, "Enviar email vía:"))
        } catch (e: Exception) {
            Toast.makeText(context, "No hay app de email instalada", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Abrir navegador con pronóstico del clima
     * Integración con servicio web externo
     */
    fun abrirPronosticoClima() {
        val ciudad = "Copiapo,CL"
        val url = "https://openweathermap.org/city/$ciudad"

        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(intent)
    }

    /**
     * Llamar a soporte técnico
     */
    fun llamarSoporte(telefono: String = "+56912345678") {
        val intent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:$telefono")
        }
        context.startActivity(intent)
    }

    /**
     * Compartir app con otros usuarios
     */
    fun compartirApp() {
        val mensaje = """
            🌱 Te recomiendo SmartRiego - Sistema de Riego Inteligente IoT
            
            Controla tu riego automáticamente desde tu celular.
            
            Descarga: [Link a Play Store cuando esté publicada]
        """.trimIndent()

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, mensaje)
        }

        context.startActivity(Intent.createChooser(intent, "Compartir SmartRiego vía:"))
    }
}