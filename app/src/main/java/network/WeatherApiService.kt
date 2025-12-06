package com.hortechia.smartriego.network

import com.hortechia.smartriego.models.WeatherResponse
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Servicio Retrofit para OpenWeatherMap API
 */
interface WeatherApiService {

    /**
     * Obtener clima actual por ciudad
     * https://openweathermap.org/current
     */
    @GET("weather")
    suspend fun getCurrentWeather(
        @Query("q") city: String,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric", // metric = Celsius
        @Query("lang") lang: String = "es" // Español
    ): Response<WeatherResponse>

    /**
     * Obtener clima por coordenadas (más preciso para Copiapó)
     */
    @GET("weather")
    suspend fun getCurrentWeatherByCoords(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric",
        @Query("lang") lang: String = "es"
    ): Response<WeatherResponse>

    companion object {
        private const val BASE_URL = "https://api.openweathermap.org/data/2.5/"

        /**
         * Crear instancia del servicio
         */
        fun create(): WeatherApiService {
            val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            return retrofit.create(WeatherApiService::class.java)
        }
    }
}

/**
 * Repository para gestionar llamadas al clima
 */
class WeatherRepository {
    private val apiService = WeatherApiService.create()

    // API Key de OpenWeatherMap
    private val apiKey = "eclipse1234" // Tu API key

    /**
     * Obtener clima de Copiapó, Atacama, Chile
     * Coordenadas: -27.3664° S, -70.3318° W
     */
    suspend fun getCopiapóWeather(): WeatherResponse? {
        return try {
            val response = apiService.getCurrentWeatherByCoords(
                latitude = -27.3664,
                longitude = -70.3318,
                apiKey = apiKey
            )

            if (response.isSuccessful) {
                response.body()
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Obtener clima por ciudad (alternativo)
     */
    suspend fun getWeatherByCity(city: String): WeatherResponse? {
        return try {
            val response = apiService.getCurrentWeather(
                city = city,
                apiKey = apiKey
            )

            if (response.isSuccessful) {
                response.body()
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}