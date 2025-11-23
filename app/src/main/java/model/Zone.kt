package com.hortechia.smartriego.model

/**
 * Modelo de datos para una zona de riego
 *
 * @param id ID único de la zona
 * @param name Nombre de la zona
 * @param humidity Nivel de humedad (0-100)
 * @param temperature Temperatura en °C
 * @param isActive Estado del riego (activo/inactivo)
 * @param lastIrrigation Timestamp del último riego
 * @param icon Recurso del ícono
 *
 * @author Jennifer Astudillo & Carlos Velásquez
 */
data class Zone(
    val id: String = "",
    val name: String = "",
    val humidity: Int = 0,
    val temperature: Double = 0.0,
    val isActive: Boolean = false,
    val lastIrrigation: Long = 0,
    val icon: Int = 0
) {
    /**
     * Retorna el estado como string
     */
    fun getStatusText(): String {
        return if (isActive) "Activo" else "Inactivo"
    }

    /**
     * Retorna el nivel de humedad como string
     */
    fun getHumidityText(): String {
        return "$humidity%"
    }

    /**
     * Retorna la temperatura como string
     */
    fun getTemperatureText(): String {
        return "${temperature}°C"
    }
    /**
     * Retorna el tiempo transcurrido desde el último riego
     */
    fun getLastIrrigationText(): String {
        if (lastIrrigation == 0L) return "Nunca"

        val diff = System.currentTimeMillis() - lastIrrigation
        val hours = diff / (1000 * 60 * 60)
        val minutes = (diff / (1000 * 60)) % 60

        return when {
            hours > 24 -> "${hours / 24} días"
            hours > 0 -> "$hours horas"
            minutes > 0 -> "$minutes minutos"
            else -> "Hace unos segundos"
        }
    }
}