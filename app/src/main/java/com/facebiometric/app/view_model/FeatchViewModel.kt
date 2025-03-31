package com.facebiometric.app.view_model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.facebiometric.app.model.Attendance
import com.facebiometric.app.model.ImageModelClass
import com.facebiometric.app.model.LocationModel
import com.facebiometric.app.model.UserDataWithCon
import com.facebiometric.app.repository.FeatchRepository
import kotlinx.coroutines.launch

class FeatchViewModel : ViewModel() {
    private val repository = FeatchRepository()

    private val _userData = MutableLiveData<UserDataWithCon?>()
    val userData: LiveData<UserDataWithCon?> get() = _userData

     fun fetchUserData(employeeID: String) {
        viewModelScope.launch {
            repository.featchEmployeeID(employeeID) { userData ->
                _userData.postValue(userData)
            }
        }
    }

    private val _embeddingsLiveData = MutableLiveData<MutableList<String>?>()
    val embeddingsLiveData: LiveData<MutableList<String>?> = _embeddingsLiveData

     fun fetchUserEmbeddings(employeeID: String) {

         viewModelScope.launch {
             repository.fetchUserEmbeddings(employeeID) { embeddings ->
                 _embeddingsLiveData.postValue(embeddings)
             }
         }
    }


    private val _allFeatchData = MutableLiveData<MutableList<UserDataWithCon>?>()
    val allFeatchData :LiveData<MutableList<UserDataWithCon>?> get() = _allFeatchData

    fun featchAllData(){
        viewModelScope.launch {
            repository.fetchAllData {
                val data = it
                _allFeatchData.postValue(data)
            }

        }
    }


    private val _statusLiveData = MutableLiveData<MutableList<String>?>()
    val statusLiveData: LiveData<MutableList<String>?> = _statusLiveData

    fun fetchStatus(employeeID: String) {
        viewModelScope.launch {
            repository.featchStatus(employeeID) { statusList ->
                _statusLiveData.postValue(statusList)
            }
        }
    }


    private val _attendanceLiveData = MutableLiveData<MutableList<Attendance>?>()
    val attendanceLiveData: LiveData<MutableList<Attendance>?> = _attendanceLiveData

    fun fetchAttendance(employeeID: String) {
        viewModelScope.launch {
            repository.featchAttendance(employeeID) { statusList ->
                _attendanceLiveData.postValue(statusList)
            }
        }
    }


    private val _lastCheckIn = MutableLiveData<String?>()
    val lastCheckIn: LiveData<String?> get() = _lastCheckIn

    fun fetchLastCheckIn(userName: String) {
        viewModelScope.launch {
            repository.featchLastCheckIn(userName) { lastCheckIn ->
                _lastCheckIn.postValue(lastCheckIn)
            }
        }
    }

    private val _locationData = MutableLiveData<LocationModel?>()
    val locationData: LiveData<LocationModel?> get() = _locationData
    fun featchLocation(userName: String) {
        viewModelScope.launch {
            repository.featchLocation(userName) {
                _locationData.postValue(it)
            }
        }
    }

    private val _userProfileData = MutableLiveData<ImageModelClass?>()
    val userProfileData: LiveData<ImageModelClass?> get() = _userProfileData
    fun featchUserProfile(userName: String) {
        viewModelScope.launch {
            repository.fetchUserProfile(userName) {
                 _userProfileData.postValue(it)
             }
        }
    }




}