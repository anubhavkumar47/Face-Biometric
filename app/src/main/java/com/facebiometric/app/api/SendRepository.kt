package com.facebiometric.app.api

import android.util.Log
import com.google.gson.Gson
import okhttp3.MediaType

import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SendRepository {
    companion object{
        private const val TAG ="SendRepository"
    }

    fun sendPunchData(punchRequest: PunchRequest, callback: (PunchResponse) -> Unit) {
        val apiKey = punchRequest.ApiKey // Replace with your actual API key
        val jsonBody = Gson().toJson(punchRequest)
        val requestBody = RequestBody.create(MediaType.parse("application/json"), jsonBody)
        RetrofitClient.instance.sendPunch(apiKey, requestBody).enqueue(object : Callback<PunchResponse> {
            override fun onResponse(call: Call<PunchResponse>, response: Response<PunchResponse>) {
                if (response.isSuccessful) {
                    Log.d(TAG, "Response: ${response.body()}")
                    callback(response.body() ?: PunchResponse(false, "Unknown error"))
                } else {
                    Log.d(TAG, "Error: ${response.errorBody()?.string()}")
                    callback(PunchResponse(false, response.message()))
                }
            }

            override fun onFailure(call: Call<PunchResponse>, t: Throwable) {
                Log.d(TAG, "Failed: ${t.message}")
                callback(PunchResponse(false, t.message ?: "Network error"))
            }
        })
    }
}