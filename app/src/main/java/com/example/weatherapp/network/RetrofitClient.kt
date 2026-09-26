package com.example.weatherapp.network

import com.example.weatherapp.data.WeatherApiService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import android.content.Context
import okhttp3.Cache
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response


import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit

//automatically retries failed network call up to MaxRetries
class RetryInterceptor(private val maxRetries: Int = 3) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        var lastException: IOException? = null

        repeat(maxRetries ) { attempt->
            try {
                return chain.proceed(chain.request())
            } catch (e: IOException) {  // network failure (no internet, timeout... )
                lastException = e
            }
        }
        throw lastException ?: IOException("Request failed after $maxRetries retries")
    }
}



object RetrofitClient {
   private lateinit var appContext: Context
   private lateinit var cache: Cache

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    fun clearCache() {
        if (::cache.isInitialized){
            cache.evictAll()  // OkHttp that deletes every cache response
        }
    }


    //by lazy create this only when it is first accessed
    val weatherApiService: WeatherApiService by lazy {  //Dir = apps private cache directory
        val cacheDir = File(appContext.cacheDir, "http_cache")
        cache = Cache(cacheDir, 5L * 1021 * 1024) //5MB max disc space

        val okHttpClient = OkHttpClient.Builder()
            .cache(cache)
            .addNetworkInterceptor { chain ->
                chain.proceed(chain.request()).newBuilder()
                    .header("Cache-Control", "public, max-age=300")
                    .build()               //cache is valid for 300 seconds (5 minutes)
            }
            .addInterceptor (RetryInterceptor(maxRetries = 3))
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()


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
        val okHttpClient = OkHttpClient.Builder()
            // no Cache(POST requests should not be cached)
            .addInterceptor (RetryInterceptor(maxRetries = 3))
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()

        Retrofit.Builder()
            .baseUrl(AppConstants.FEEDBACK_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(FeedbackApiService::class.java)
    }
}