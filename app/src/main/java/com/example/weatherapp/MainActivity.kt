package com.example.weatherapp

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.weatherapp.network.AppConstants
import com.example.weatherapp.network.RetrofitClient
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import java.nio.file.WatchEvent
import kotlinx.coroutines.Dispatchers  // thread types: Main (UI), IO (network/disk)
import kotlinx.coroutines.withContext  // switches thread context inside a coroutine

//CLASS TWO IMPORTS
import android.util.Log
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.AssistChip

// class 3 imports
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.TooltipScope
import com.example.weatherapp.data.FeedbackRequest


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        RetrofitClient.init(this)
        //before SetContent since both are by lazy and builds the cache first
        setContent {

            MaterialTheme {
                Surface{
                    WeatherScreen()
                }
            }
        }
    }
}

// Inside WeatherScreen(), find the Submit Feedback Button's onClick.
// Right before "scope.launch {" is where Assignment 4 goes:

// Steps to complete:
// 1. Declare "var feedbackLoading by remember { mutableStateOf(false) }"
//    near the top of WeatherScreen(), next to the other state variables
// 2. Set feedbackLoading = true right before the feedback scope.launch { }
// 3. Set feedbackLoading = false inside a finally { } wrapped around the
//    feedback try/catch -- same shape as Get Weather's finally block
// 4. On the Submit Feedback Button: add enabled = !feedbackLoading,
//    and swap its Text to "Submitting..." while feedbackLoading is true

@Composable
fun WeatherScreen() {
    var city by remember { mutableStateOf("") }
    var cityText by remember { mutableStateOf("city: --") }
    var temperatureText by remember { mutableStateOf("Temperature: --") }
    var descriptionText by remember { mutableStateOf("Temperature: --") }
    var windResult by remember { mutableStateOf("Wind Speed --") }
    var humidityResult by remember { mutableStateOf("Humidity --") }
    var isLoading by remember { mutableStateOf(false) }
    // for the Post
    var currentCity by remember { mutableStateOf("") }
    var rating by remember { mutableStateOf(3) }
    var comment by remember { mutableStateOf("") }
    var feedbackResult by remember { mutableStateOf("") }
    //CACHE
    var recentSearches by remember { mutableStateOf(listOf<String>()) }
    var feedbackLoading by remember { mutableStateOf(false) } //1

    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    Column(modifier = Modifier.fillMaxSize().padding(all = 16.dp)) {
        TextField(
            value = city,
            onValueChange = { city = it},
            label = {Text("Enter city name") },
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            enabled = !isLoading, //enable the button mid-request stop duplicate calls
            onClick = {
                // This block runs every time the "Get Weather" button is tapped
                val trimmedCity = city.trim()
                // .trim() removes spaces typed at the start or end

                if (trimmedCity.isEmpty()) {
                    // .isEmpty() = true if the string has 0 characters
                    Toast.makeText(context, "Please enter a city name", Toast.LENGTH_SHORT).show()
                } else {
                    scope.launch {
                        try {
                            val response = withContext(Dispatchers.IO) {
                                RetrofitClient.weatherApiService.getWeather(
                                    city = trimmedCity,
                                    apiKey = AppConstants.API_KEY,
                                    units = AppConstants.UNITS
                                )

                            }
                            Log.d("WeatherApp ","Request URL: ${response.raw().request.url}")
                            Log.d("WeatherApp ","Request code: ${response.code()}")

                            if (response.isSuccessful) {
                                val weather = response.body()
                                if (weather != null) {
                                    cityText = "City: ${weather.name}"
                                    temperatureText = "Temperature: ${weather.main.temp}"
                                    descriptionText = "Description: ${weather.weather[0].description}"
                                    windResult = "Wind Speed: ${weather.wind.speed}MPH"
                                    humidityResult = "Humidity: ${weather.main.humidity}%"
                                    currentCity = trimmedCity

                                    recentSearches = (listOf(weather.name) + recentSearches.filter { it != weather.name}).take(5)
                                    //filter removes duplicates then places new searches at hte front of the list. caps list at 5
                                    // Steps to complete:
                                    // 1. Set windResult from weather.wind.speed (append "MPH")
                                    // 2. Set humidityResult from weather.main.humidity (append "%")
                                    // 3. Below in the else branch, replace the single Toast with a
                                    //    when (response.code()) --

                                }
                            } else{

                                // This Toast gets replaced by step
                                // with different messages for 404, 401, and anything else
                                //response.code(context, "404", Toast.LENGTH_SHORT).show()
                                when (response.code()) {
                                    404 -> Toast.makeText(context, "City not found. Check the name and try again", Toast.LENGTH_SHORT).show()
                                    401 -> Toast.makeText(context, "Invalid API Key, Check AppConstants.kt", Toast.LENGTH_SHORT).show()

                                    else -> Toast.makeText(context, "Something went wrong: ${response.code()}", Toast.LENGTH_SHORT).show()
                                }
                            }
                        } catch (e: Exception) {
                            Toast.makeText(context, "Network error. Check your connection",
                                Toast.LENGTH_SHORT).show()
                        }
                        finally {
                            isLoading = false   //feedbackLoading = false
                        }
                    }

                }
            },
            modifier =
                Modifier.fillMaxWidth().padding(top = 8.dp)
        ) {
            Text(if (isLoading)"loading.." else "Get Weather")
        }

        Text(cityText, fontSize = 20.sp, modifier = Modifier.padding(top = 24.dp))
        Text(temperatureText, fontSize = 18.sp, modifier = Modifier.padding(top = 8.dp))
        Text(descriptionText, fontSize = 18.sp, modifier = Modifier.padding(top = 8.dp))
        Text(windResult, fontSize = 18.sp, modifier = Modifier.padding(top = 8.dp))
        Text(humidityResult, fontSize = 18.sp, modifier = Modifier.padding(top = 8.dp))


        //handling the cache control and recent searches
        OutlinedButton(
            onClick = {
                RetrofitClient.clearCache()
                Toast.makeText(context, "Cache Cleared",Toast.LENGTH_SHORT).show()
            }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
        ){
            Text("Clear Cache")
        }
        if (recentSearches.isNotEmpty()) {
            Text("Recent Searches", fontSize = 14.sp, modifier = Modifier.padding(top = 16.dp))
            Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(top = 4.dp)) {
                recentSearches.forEach { recentCity ->
                    AssistChip(
                        onClick = { city = recentCity },
                        label= { Text(recentCity) },
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }
            }
        }




        HorizontalDivider(modifier = Modifier.padding(top = 24.dp, bottom = 16.dp))

        Text("How do you feel about today's weather?", fontSize = 16.sp)

        Slider(
            value = rating.toFloat(),
            onValueChange = { rating = it.toInt() },
            valueRange = 1f..5f,
            steps = 3 //stops between 1 and 5 whole numbers
        )
        Text("Rating: $rating/5") //slider has no built-in label

        TextField(
            value = comment,
            onValueChange = { comment = it },
            label = { Text("Leave a comment about the weather...") },
            minLines = 3,
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)

        )

        Button(
            enabled = !feedbackLoading,
            onClick = {
                if (currentCity.isEmpty()) {
                    Toast.makeText(context, "Please fetch a weather for a city first", Toast.LENGTH_SHORT).show()
                } else if (comment.isBlank()) {
                    Toast.makeText(context, "Please leave a comment", Toast.LENGTH_SHORT).show()
                } else {
                    feedbackLoading = true
                    scope.launch {
                        try {                        //Gson converts this to JSON
                            val request = FeedbackRequest(city = currentCity, rating = rating, comment = comment)

                            val response = withContext(Dispatchers.IO) {
                                RetrofitClient.feedbackApiService.submitFeedback(request)
                            }

                            // ASSIGNMENT 3
                            // 1. If response.isSuccessful: set feedbackResult to a success message, then
                            //    clear the comment field and reset rating back to 3
                            // 2. If NOT successful: set feedbackResult to a failure message
                            if (response.isSuccessful) {
                                feedbackResult = "Feedback submitted successfully!"
                                comment = ""
                                rating = 3
                            } else {
                                feedbackResult = "Failed to submit feedback. Try again"
                            }

                        } catch (e: Exception) {
                            feedbackResult = "Error submitting feedback. Check your connection"
                        }
                        finally {
                            feedbackLoading = false
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
        ) {
            Text(if (feedbackLoading)"Submitting" else "Submit Feedback")
        }
        Text(feedbackResult, fontSize = 14.sp, modifier = Modifier.padding(top = 8.dp))
    }
}
