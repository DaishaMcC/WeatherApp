package com.example.weatherapp.data

import com.example.weatherapp.data.WeatherResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

//interface a contract/blueprint
//we write what calls want, retrofit generates how they will actually work

interface WeatherApiService {
    // @GET = make an HTTP GET request to: WEATHER_BASE_URL + "data/2.5/weather"
    // Full URL example: https://api.openweathermap.org/data/2.5/weather?q=London&appid=KEY&units=metric
    @GET(value = "data/2.5/weather")
    suspend fun getWeather(
        @Query("q") city: String,
        @Query(value = "appid") apiKey: String,
        @Query(value = "units") units: String

    ): Response<WeatherResponse>
}