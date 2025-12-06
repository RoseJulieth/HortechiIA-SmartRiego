package com.hortechia.smartriego.models

import com.google.gson.annotations.SerializedName

/**
 * Modelo de respuesta de OpenWeatherMap API
 * https://openweathermap.org/current
 */
data class WeatherResponse(
    @SerializedName("weather")
    val weather: List<Weather>,

    @SerializedName("main")
    val main: Main,

    @SerializedName("wind")
    val wind: Wind,

    @SerializedName("name")
    val cityName: String,

    @SerializedName("dt")
    val timestamp: Long
)

data class Weather(
    @SerializedName("id")
    val id: Int,

    @SerializedName("main")
    val main: String, // e.g., "Clear", "Clouds", "Rain"

    @SerializedName("description")
    val description: String, // e.g., "clear sky"

    @SerializedName("icon")
    val icon: String // e.g., "01d"
)

data class Main(
    @SerializedName("temp")
    val temp: Double, // Temperatura en Kelvin o Celsius según parámetros

    @SerializedName("feels_like")
    val feelsLike: Double,

    @SerializedName("humidity")
    val humidity: Int, // Porcentaje

    @SerializedName("pressure")
    val pressure: Int
)

data class Wind(
    @SerializedName("speed")
    val speed: Double, // m/s

    @SerializedName("deg")
    val deg: Int // Dirección en grados
)

/**
 * Modelo simplificado para UI
 */
data class WeatherData(
    val city: String,
    val temperature: Int, // °C
    val description: String,
    val humidity: Int,
    val windSpeed: Int, // km/h
    val icon: String,
    val recommendations: List<Recommendation>
)

data class Recommendation(
    val title: String,
    val isRecommended: Boolean,
    val icon: String
)