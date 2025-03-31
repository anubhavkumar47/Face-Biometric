package com.facebiometric.app.repository

import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.facebiometric.app.model.Attendance
import com.facebiometric.app.model.ImageModelClass
import com.facebiometric.app.model.LocationModel
import com.facebiometric.app.model.UserDataWithCon
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class FeatchRepository {

    companion object{
        private const val TAG="FeatchRepository"
    }

    private val databaseReference: DatabaseReference = FirebaseDatabase.getInstance().getReference("UsersData")

   suspend fun fetchUserEmbeddings(employeID: String, callback: (MutableList<String>?) -> Unit) {
        databaseReference.child(employeID).child("embeddingList").get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.exists()){
                    val embeddingList = snapshot.value as? MutableList<String>
                    Log.d(TAG, "Fetched embeddings: ${embeddingList?.size}")
                    callback(embeddingList)
                }else{
                    callback(null)
                }


            }
            .addOnFailureListener {
                callback(null)  // Error occurred
            }
    }


    suspend fun featchEmployeeID(employeeID: String, callback: (UserDataWithCon?) -> Unit){
        databaseReference.child(employeeID).get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                     val name = snapshot.getValue(UserDataWithCon::class.java)
                    callback(name)
                    Log.d(TAG, "Data fetched successfully $name")

                }
            }
            .addOnFailureListener {
                callback(null)
                Log.d(TAG, "Failed to fetch data")
            }





    }

    suspend fun fetchAllData(callback: (MutableList<UserDataWithCon>?) -> Unit) {
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val dataList = mutableListOf<UserDataWithCon>()
                    for (data in snapshot.children) {
                        val userData = data.getValue(UserDataWithCon::class.java) // Convert snapshot to UserData
                        userData?.let { dataList.add(it) }
                    }
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

    suspend fun featchStatus(employeeID: String, callback: (MutableList<String>?) -> Unit){
        val statusList:MutableList<String> = mutableListOf()
        val currentMonth =getCurrentDate().substring(0,7)//"2025-03"
        val databaseReferenceAttendance: DatabaseReference = FirebaseDatabase.getInstance().getReference("Attendance")
            .child(employeeID).child(currentMonth)
        databaseReferenceAttendance.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    for(data in snapshot.children){
                        val value = data.getValue(Attendance::class.java)
                        if (value != null) {
                            statusList.add(value.status)
                        }

                    }
                    callback(statusList)
                }
                else{
                    callback(null)

                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })




    }

    suspend fun featchAttendance(employeeID: String, callback: (MutableList<Attendance>?) -> Unit){
        val attendanceList:MutableList<Attendance> = mutableListOf()
        val currentMonth =getCurrentDate().substring(0,7)//"2025-03"
        val databaseReferenceAttendance: DatabaseReference = FirebaseDatabase.getInstance().getReference("Attendance")
            .child(employeeID).child(currentMonth)
        databaseReferenceAttendance.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    for(data in snapshot.children){
                        val value = data.getValue(Attendance::class.java)
                        if (value != null) {
                            attendanceList.add(value)
                        }

                    }
                    callback(attendanceList)
                }
                else{
                    callback(null)

                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })




    }

    private fun getCurrentDate(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.format(Date()) // Returns "2025-03-18"
    }

    suspend fun featchLastCheckIn(employeeID: String, callback: (String?) -> Unit){
        val statusList:MutableList<String> = mutableListOf()
        val currentMonth =getCurrentDate().substring(0,7)//"2025-03"
        val currentDay = getCurrentDate()
        val databaseReferenceAttendance: DatabaseReference = FirebaseDatabase.getInstance().getReference("Attendance")
            .child(employeeID).child(currentMonth).child(currentDay)
        databaseReferenceAttendance.get().addOnSuccessListener {
            if(it.exists()){
                val time = it.child("time").value.toString()
                callback(time)
            }
            else{
                callback(null)
            }
        }.addOnFailureListener {
            Log.d(TAG, "Failed to fetch data")
        }



    }


    private val databaseRefLocation: DatabaseReference = FirebaseDatabase.getInstance().getReference("UsersLocation")
    suspend fun featchLocation(employeeID: String, callback: (LocationModel?) -> Unit){
        databaseRefLocation.child(employeeID).get().addOnSuccessListener {
            if (it.exists()){
                val location =it.getValue(LocationModel::class.java)
                callback(location)
            }
            else{
                callback(null)
            }


        }.addOnFailureListener {
            callback(null)
            Log.d(TAG,"Failed to upload location")
        }

    }

    suspend fun fetchUserProfile(employeeID: String, callback: (ImageModelClass?) -> Unit)  {
        val databaseRefUserProfile = FirebaseDatabase.getInstance().getReference("UserImageData")
         databaseRefUserProfile.child(employeeID).get().addOnSuccessListener {
             if (it.exists()){
                 val imageModelClass = it.getValue(ImageModelClass::class.java)
                 Log.d(TAG,"Profile image Data $imageModelClass")
                 callback(imageModelClass)
             }
             else{
                 callback(null)
             }
         }

    }


}