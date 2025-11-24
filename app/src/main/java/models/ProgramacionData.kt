package com.hortechia.smartriego.models

data class ProgramacionData(
    val id: String = "",
    val nombre: String = "",
    val zona: String = "",
    val dias: List<String> = emptyList(), // ["L", "M", "X", "J", "V", "S", "D"]
    val hora: String = "", // "06:00"
    val duracion: Int = 0, // minutos
    val activo: Boolean = true,
    val proximaEjecucion: String = "" // "Mañana 6:00 AM"
)

data class ModoInteligente(
    val activo: Boolean = false,
    val descripcion: String = "El sistema ajustará automáticamente los horarios según la humedad"
)