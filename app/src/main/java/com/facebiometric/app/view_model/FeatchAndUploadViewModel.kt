package com.facebiometric.app.view_model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.facebiometric.app.model.BranchDetails
import com.facebiometric.app.model.EmployeeIDAndEmbedding
import com.facebiometric.app.repository.FeatchAndPushRepository
import kotlinx.coroutines.launch

class FeatchAndUploadViewModel:ViewModel() {

    private val repository = FeatchAndPushRepository()

    private val  _embeddingAndName = MutableLiveData<MutableList<EmployeeIDAndEmbedding>?>()
    val embeddingAndName: LiveData<MutableList<EmployeeIDAndEmbedding>?> get() = _embeddingAndName

    fun fetchNameAndEmbedding() {
        viewModelScope.launch {
            repository.featchEmpIDAndEmbedding {
                _embeddingAndName.postValue(it)
            }
        }
    }

    fun syncUserEmbeddings() = viewModelScope.launch {
        repository.syncUserEmbeddings()
    }

    private val _branchDetails = MutableLiveData<Boolean>()
    val branchDetails: LiveData<Boolean> get() = _branchDetails
    fun uploadBranchDetails(branchDetails: BranchDetails){
        viewModelScope.launch {
            repository.uploadBranchDetails(branchDetails){
                _branchDetails.postValue(it)
            }
        }
    }
}