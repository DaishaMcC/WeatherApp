package com.example.weatherapp.data

data class WeatherResponse(
    val name: String,      //city name
    val main: Main,        //nested object
    val weather: List<Weather>,      //array of weather
    val wind: Wind        //nested objects wind speed
)

data class Main(
    val temp: Double,     // current temp
    val humidity: Int     //humidity
)

data class Weather(
    val description: String
)

data class Wind(
    val speed: Double          //wind speed in meters per second
)