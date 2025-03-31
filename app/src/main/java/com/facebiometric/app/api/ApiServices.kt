package com.facebiometric.app.api

import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface ApiService {
    @POST("punch.php") // Update with the correct API endpoint
    fun sendPunch(
        @Header("X-API-KEY") apiKey: String, // API Key in Header
        @Body requestBody: RequestBody // JSON data as RequestBody
    ): Call<PunchResponse>
}