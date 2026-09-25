package com.example.weatherapp.network

import com.example.weatherapp.data.WeatherApiService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create

object RetrofitClient {
    //by lazy create this only when it is first accessed
    val weatherApiService: WeatherApiService by lazy {
        Retrofit.Builder()
            //Retrofit prepends this to ever @GET path in WeatherApiService
            .baseUrl(AppConstants.WEATHER_BASE_URL)
            // automatic JSON-TO-DATA-CLASS conversion when api returns JSON
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            // retrofit generates the real HTTP implementation
            // java tells retrofit which interface to implement
            .create(WeatherApiService::class.java)
    }
    val feedbackApiService: FeedbackApiService by lazy {
        Retrofit.Builder()
            .baseUrl(AppConstants.FEEDBACK_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(FeedbackApiService::class.java)
    }
}