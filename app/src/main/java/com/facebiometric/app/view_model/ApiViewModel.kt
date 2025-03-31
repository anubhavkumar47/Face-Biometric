package com.facebiometric.app.view_model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.facebiometric.app.api.PunchRequest
import com.facebiometric.app.api.PunchResponse
import com.facebiometric.app.api.SendRepository

class ApiViewModel():ViewModel() {
    private val apiRepository =SendRepository()

    private val _punchResponse = MutableLiveData<PunchResponse>()
    val punchResponse: LiveData<PunchResponse> get() = _punchResponse

    fun sendPunchData(punchRequest: PunchRequest) {
        apiRepository.sendPunchData(punchRequest) { response ->
            _punchResponse.postValue(response)
        }

    }
}