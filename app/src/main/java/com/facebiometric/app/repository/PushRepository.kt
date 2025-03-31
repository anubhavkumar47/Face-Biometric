package com.facebiometric.app.repository

import android.util.Log
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.facebiometric.app.model.Attendance
import com.facebiometric.app.model.ImageModelClass
import com.facebiometric.app.model.LocationModel
import com.facebiometric.app.model.UserData
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class PushRepository {
    companion object{
        private const val TAG="PushRepository"
    }

    private val databaseReference: DatabaseReference = FirebaseDatabase.getInstance().getReference("UsersData")
    fun uploadDataOnFirebase(employeeId: String,nameOfUser: String, emailOfUser: String, passwordOfUser: String,
                                     postOfUser: String,department:String, embeddingList: MutableList<String>,callback: (Boolean) -> Unit){

        val userData = UserData(employeeId,nameOfUser,emailOfUser,passwordOfUser,postOfUser,department,embeddingList)
        databaseReference.child(employeeId).setValue(userData).addOnSuccessListener {
            Log.d(TAG,"Data uploaded successfully")
           callback(true)
        }.addOnFailureListener {
            callback(false)
            Log.d(TAG,"Failed to upload data")

        }



    }

    fun uploadAttendance(attendance: Attendance, callback: (Boolean) -> Unit) {
        val monthKey = attendance.date.substring(0, 7)  // Extract "YYYY-MM"
        val dateKey = attendance.date  // Full date "YYYY-MM-DD"
        val databaseReferenceAttendance: DatabaseReference =
            FirebaseDatabase.getInstance().getReference("Attendance")
                .child(attendance.employeeID)
                .child(monthKey)
                .child(dateKey)

        databaseReferenceAttendance.get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                // Attendance already exists, update check-out time
                val existingAttendance = snapshot.getValue(Attendance::class.java)

                existingAttendance?.let {
                    val checkInTime = it.checkedIn
                    val previousCheckOut = it.checkedOut

                    // Ensure check-in is recorded and check-out is empty before updating
                    if (previousCheckOut.isNullOrEmpty()) {
                        val currentTime = getCurrentTime() // Implement this function to get HH:mm format

                        // Ensure user can check out only after a certain time (e.g., 240 minutes = 4 hours)
                        val minutesDiff = getMinutesDifference(checkInTime, currentTime)
                        if (minutesDiff >= 2) {
                            databaseReferenceAttendance.child("checkedOut").setValue(currentTime)
                                .addOnSuccessListener {
                                    Log.d(TAG, "Check-out time updated successfully")
                                    callback(true)
                                }.addOnFailureListener {
                                    Log.d(TAG, "Failed to update check-out time")
                                    callback(false)
                                }
                        } else {
                            Log.d(TAG, "Check-out not allowed before 240 minutes")
                            callback(false)
                        }
                    } else {
                        Log.d(TAG, "User already checked out")
                        callback(false)
                    }
                }
            } else {
                // First-time marking attendance
                val newAttendance = attendance.copy(checkedOut = "") // Ensure checkedOut is null
                databaseReferenceAttendance.setValue(newAttendance).addOnSuccessListener {
                    Log.d(TAG, "Attendance marked successfully")
                    callback(true)
                }.addOnFailureListener {
                    Log.d(TAG, "Failed to upload attendance")
                    callback(false)
                }
            }
        }.addOnFailureListener {
            Log.d(TAG, "Failed to connect with database")
            callback(false)
        }
    }



    private val databaseRefLocation: DatabaseReference = FirebaseDatabase.getInstance().getReference("UsersLocation")
    fun uploadLocation(location: LocationModel, callback: (Boolean) -> Unit){
        databaseRefLocation.child(location.employeeID).setValue(location).addOnSuccessListener {
            Log.d(TAG,"Location uploaded successfully")
            callback(true)
        }.addOnFailureListener {
            callback(false)
            Log.d(TAG,"Failed to upload location")
        }

    }

    suspend fun uploadUserProfile(imageData: ImageModelClass): Boolean {
        Log.d(TAG,"ImageData ${imageData.imageByteList}")
        return try {
            val databaseRefUserProfile = FirebaseDatabase.getInstance().getReference("UserImageData")
            databaseRefUserProfile.child(imageData.employeeID).setValue(imageData).addOnSuccessListener {
                Log.d(TAG,"Profile uploaded successfully")
            }.addOnFailureListener {
                Log.d(TAG,"Failed to upload profile")
            }.await()
            true
        }
        catch (e:Exception){
            Log.e("Firebase", "Failed to fetch profile: ${e.message}")
            false

        }
    }

    private fun getMinutesDifference(startTime: String, endTime: String): Long {
        val format = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        val start = format.parse(startTime)
        val end = format.parse(endTime)

        return if (start != null && end != null) {
            val diff = end.time - start.time
            TimeUnit.MILLISECONDS.toMinutes(diff) // Convert milliseconds to minutes
        } else {
            0
        }
    }

    private fun getCurrentTime(): String {
        val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        return timeFormat.format(Date()) // Returns "14:30:00"
    }





}