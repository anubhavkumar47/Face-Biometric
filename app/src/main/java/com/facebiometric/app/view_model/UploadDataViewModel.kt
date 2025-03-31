package com.facebiometric.app.view_model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.facebiometric.app.model.Attendance
import com.facebiometric.app.model.ImageModelClass
import com.facebiometric.app.model.LocationModel
import com.facebiometric.app.repository.PushRepository
import kotlinx.coroutines.launch

class UploadDataViewModel:ViewModel() {
    private val repository = PushRepository()



    private val _pushAllData = MutableLiveData<Boolean?>()
    val pushAllData: LiveData<Boolean?>get() = _pushAllData

    fun uploadData(empID:String,nameOfUser: String, emailOfUser: String, passwordOfUser: String, postOfUser: String,department:String, embeddingList: MutableList<String>){
        viewModelScope.launch {
            repository.uploadDataOnFirebase(empID,nameOfUser, emailOfUser, passwordOfUser, postOfUser, department,embeddingList){
                _pushAllData.postValue(it)


            }


        }
    }

    private val _pushAttendanceData =MutableLiveData<Boolean?>()
    val pushAttendanceData:LiveData<Boolean?>get() = _pushAttendanceData

    fun uploadAttendance(attendance: Attendance){
        viewModelScope.launch {
            repository.uploadAttendance(attendance){
                _pushAttendanceData.postValue(it)
            }

        }
    }

    private val _locationData = MutableLiveData<Boolean?>()
    val locationData: LiveData<Boolean?>get() = _locationData
    fun uploadLocation(location: LocationModel){
        viewModelScope.launch {
            repository.uploadLocation(location){
                _locationData.postValue(it)
            }
        }
    }


    private val _pushUserProfile = MutableLiveData<Boolean?>()
    val pushUserProfile: LiveData<Boolean?>get() = _pushUserProfile
    fun uploadUserProfile(imageData: ImageModelClass) {
        viewModelScope.launch {
            _pushUserProfile.value = repository.uploadUserProfile(imageData)


        }
    }


}