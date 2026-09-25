package com.example.weatherapp.network

import com.example.weatherapp.data.FeedbackRequest
import retrofit2.Response
import retrofit2.http.Body  //send this object as a JSON request body how we store the info we post
import retrofit2.http.POST

interface FeedbackApiService {
    @POST("feedback")  //tells retrofit make a post request
    suspend fun submitFeedback(
        @Body request: FeedbackRequest
        // Gson converts our Feedback object to JSON
        // AND places it in HTTP request body
    ): Response<Unit>
}