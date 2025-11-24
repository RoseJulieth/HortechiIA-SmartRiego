package com.hortechia.smartriego.models

data class HumedadData(
    val timestamp: Long,
    val zona1Humedad: Float,
    val zona2Humedad: Float
)

data class RiegoData(
    val id: String,
    val zona: String,
    val fechaHora: Long,
    val duracion: Int, // minutos
    val tipo: TipoRiego,
    val litrosEstimados: Float
)

enum class TipoRiego {
    MANUAL,
    AUTOMATICO
}