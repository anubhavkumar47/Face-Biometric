package com.facebiometric.app.repository

import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.GenericTypeIndicator
import com.google.firebase.database.ValueEventListener
import com.facebiometric.app.model.BranchDetails
import com.facebiometric.app.model.EmployeeIDAndEmbedding

class FeatchAndPushRepository {
    private val databaseReference = FirebaseDatabase.getInstance().getReference("UsersData")
    private val embeddingsRef = FirebaseDatabase.getInstance().getReference("UserEmbeddings")


   suspend fun syncUserEmbeddings() {
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (userSnapshot in snapshot.children) {
                    val userId = userSnapshot.key ?: continue
                    val empID = userSnapshot.child("employeeId").getValue(String::class.java)
                    val embedding = userSnapshot.child("embeddingList")
                        .getValue(object : GenericTypeIndicator<MutableList<String>>() {})

                    if (empID != null && embedding != null) {
                        val userEmbedding = EmployeeIDAndEmbedding(empID, embedding)
                        embeddingsRef.child(userId).setValue(userEmbedding)
                            .addOnSuccessListener {
                                Log.d(TAG, "Data synced successfully")
                            }
                            .addOnFailureListener { error ->
                                Log.e(TAG, "Failed to sync data: ${error.message}")
                            }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Failed to fetch data: ${error.message}")
            }
        })
    }


    suspend fun featchEmpIDAndEmbedding(callback: (MutableList<EmployeeIDAndEmbedding>?) -> Unit) {
        embeddingsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val dataList = mutableListOf<EmployeeIDAndEmbedding>()
                    for (data in snapshot.children) {
                        val userData = data.getValue(EmployeeIDAndEmbedding::class.java) // Convert snapshot to UserData
                        userData?.let { dataList.add(it) }
                    }
                    Log.d(TAG,"DATA $dataList")
                    callback(dataList)
                } else {
                    callback(null) // No data found
                }
            }

            override fun onCancelled(error: DatabaseError) {
                callback(null) // Firebase error
            }
        })
    }

    companion object{
        private const val TAG ="FeatchAndPushRepository"
    }


    private val databaseRef = FirebaseDatabase.getInstance().getReference("BranchDetails")

     fun uploadBranchDetails(branchDetails: BranchDetails, callback: (Boolean) -> Unit) {

           databaseRef.child(branchDetails.BranchID).get().addOnSuccessListener {
               if(it.exists()) {
                   Log.d(TAG, "Data Already Exists")
               }
               else{
                   databaseRef.child(branchDetails.BranchID).setValue(branchDetails).addOnSuccessListener {
                       Log.d(TAG, "Data Uploaded Successfully")
                       callback(true)
                   }.addOnFailureListener {
                       Log.d(TAG, "Not Fetched")
                       callback(false)
                   }
               }
           }


     }

    fun fetchBranchDetails(callback: (MutableList<BranchDetails?>) -> Unit){
        databaseRef.get().addOnSuccessListener {
            if(it.exists()){
                val dataList = mutableListOf<BranchDetails?>()
                for(data in it.children){
                    val branchDetails = data.getValue(BranchDetails::class.java)
                    dataList.add(branchDetails)
                }
                callback(dataList)
            }
            else{
                callback(mutableListOf())
            }
        }.addOnFailureListener {
            Log.d(TAG,"Failed to fetch data")
        }

    }






}