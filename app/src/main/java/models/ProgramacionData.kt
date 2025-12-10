package com.hortechia.smartriego.models

data class ProgramacionData(
    var id: String = "",
    var nombre: String = "",
    var zona: String = "",
    var dias: List<String> = emptyList(), // ["L", "M", "X", "J", "V", "S", "D"]
    var hora: String = "", // "06:00"
    var duracion: Int = 0, // minutos
    var activo: Boolean = true,
    var proximaEjecucion: String = "" // "Mañana 6:00 AM"
) {
    // Constructor vacío necesario para Firebase
    constructor() : this("", "", "", emptyList(), "", 0, false, "")
}

data class ModoInteligente(
    val activo: Boolean = false,
    val descripcion: String = "El sistema ajustará automáticamente los horarios según la humedad"
)