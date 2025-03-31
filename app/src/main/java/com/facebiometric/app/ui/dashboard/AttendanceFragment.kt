package com.facebiometric.app.ui.dashboard

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.location.Location
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.facebiometric.app.R
import com.facebiometric.app.databinding.FragmentAttendanceBinding
import com.facebiometric.app.model.ImageModelClass
import com.facebiometric.app.model.LocationModel
import com.facebiometric.app.view_model.FeatchViewModel


class AttendanceFragment : Fragment() {

    private lateinit var binding :FragmentAttendanceBinding
    private lateinit var viewModel: FeatchViewModel
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var employeeID:String
    private lateinit var imageModelClass: ImageModelClass
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private var storeLocation: LocationModel = LocationModel(0.0,0.0,"")
    private val radiusOfLocation  =100.00


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
       binding = FragmentAttendanceBinding.inflate(inflater, container, false)

        sharedPreferences = requireActivity().getSharedPreferences("UserData", android.content.Context.MODE_PRIVATE)
        employeeID = sharedPreferences.getString("nameOfUser", "").toString()
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireContext())

        viewModel = ViewModelProvider(this)[FeatchViewModel::class.java]
        viewModel.fetchUserData(employeeID)
        viewModel.fetchAttendance(employeeID)

        viewModel.userData.observe(viewLifecycleOwner) { emp ->
            Log.d(TAG, "Fetched User Data: $emp")
            if (emp != null) {
                binding.userName.text = "Hi,${emp.nameOfUser}"
                binding.empID.text = "Employee ID: ${emp.employeeId}"
                binding.department.text = "Department: ${emp.department}"
            }
        }

        viewModel.featchUserProfile(employeeID)
        viewModel.userProfileData.observe(viewLifecycleOwner) { data ->
            if (data != null) {
                imageModelClass = data
                val imageByteList = imageModelClass.imageByteList
                val byteArray = imageByteList.split(",").map { it.toByte() }.toByteArray()
                val bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
                // val bitmap = byteArrayToBitmap(byteArray)
                val rotatedBitmap = rotateBitmap(bitmap, 270f) // Rotate the image
                binding.profileImage.setImageBitmap(rotatedBitmap)
            }
        }

        binding.faceScan.setOnClickListener {
            showBottomSheet()
        }

        viewModel.featchLocation(employeeID)
        viewModel.locationData.observe(viewLifecycleOwner) {
            if (it != null) {
                storeLocation = it

                getLocationOfUser(employeeID){loc->
                    if(loc !=null){
                        val distance = calculateDistanceAndroid(
                            loc.latitude,
                            loc.longitude,
                            storeLocation.latitude,
                            storeLocation.longitude
                        )

                        if(distance>radiusOfLocation){
                            binding.location.text = "NotVerified"
                        }
                        else{
                            binding.location.text ="HeadOffice"
                        }



                    }
                }
            }

        }


        return binding.root
    }

    companion object {
        private const val  TAG ="HistoryFragment"
    }

    private fun showBottomSheet() {
        val bottomSheetDialog = BottomSheetDialog(requireActivity())
        val view = layoutInflater.inflate(R.layout.bottom_action_layout, null)

        bottomSheetDialog.setContentView(view)
        bottomSheetDialog.show()

        val bottomActionLayout = view.findViewById<View>(R.id.cancel_bottom_layout)
        bottomActionLayout.setOnClickListener {
            bottomSheetDialog.dismiss()
        }
        val scanFaceID = view.findViewById<Button>(R.id.faceID)
        scanFaceID.setOnClickListener {
            findNavController().navigate(R.id.action_attendanceFragment_to_faceRecognitionFragment)
            bottomSheetDialog.dismiss()
        }
        bottomSheetDialog.setOnShowListener { blurBackground(true) }
        bottomSheetDialog.setOnDismissListener { blurBackground(false) }
        bottomSheetDialog.show()
    }

    private fun blurBackground(enable: Boolean) {
        binding.root.alpha = if (enable) 0.3f else 1.0f // Reduce transparency when dialog is open
    }

    private fun rotateBitmap(source: Bitmap, angle: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(angle)
        return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
    }

    @SuppressLint("MissingPermission")
    private fun getLocationOfUser(userName: String, callback: (LocationModel?) -> Unit) {

        val locationRequest = CurrentLocationRequest.Builder()
            .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
            .build()
        fusedLocationProviderClient.getCurrentLocation(locationRequest.priority, null)
            .addOnSuccessListener {
                val latitude = it.latitude
                val longitude = it.longitude
                val location = LocationModel(latitude, longitude, userName)
                callback(location)
                Log.d(TAG, " Current Location Latitude: $latitude, Longitude: $longitude")

            }.addOnFailureListener {
                callback(null)

            }
    }

    private fun calculateDistanceAndroid(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Float {
        Log.d(TAG, "lat1: $lat1, lon1: $lon1, lat2: $lat2, lon2: $lon2")
        val startLocation = Location("").apply {
            latitude = lat1
            longitude = lon1
        }

        val endLocation = Location("").apply {
            latitude = lat2
            longitude = lon2
        }
        val distance = startLocation.distanceTo(endLocation)
        Log.d(TAG,"Distance $distance")
        return distance
    }
}