package com.facebiometric.app.ui.dashboard

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.facebiometric.app.R
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import android.graphics.Color
import android.graphics.Matrix
import android.location.Location
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.facebiometric.app.databinding.FragmentDashBoardBinding
import com.facebiometric.app.model.Attendance
import com.facebiometric.app.model.ImageModelClass
import com.facebiometric.app.model.LocationModel
import com.facebiometric.app.view_model.FeatchAndUploadViewModel
import com.facebiometric.app.view_model.FeatchViewModel
import java.text.SimpleDateFormat
import java.time.YearMonth
import java.util.Date
import java.util.Locale


class HomeFragment : Fragment() {
   private lateinit var binding: FragmentDashBoardBinding
   private lateinit var sharedPreferences: SharedPreferences
    private lateinit var employeeID:String
    private lateinit var viewModel: FeatchViewModel
    private lateinit var viewModel2:FeatchAndUploadViewModel
    private  var attendanceData:MutableList<Attendance> = mutableListOf()
    private lateinit var imageModelClass: ImageModelClass
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private var storeLocation: LocationModel = LocationModel(0.0,0.0,"")
    private val radiusOfLocation  =100.00



    companion object{
       private const val TAG="DashBoardFragment"
   }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true) // Required for menu handling in Fragments
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        // Inflate the layout for this fragment

        binding = FragmentDashBoardBinding.inflate(inflater, container, false)
        sharedPreferences = requireActivity().getSharedPreferences("UserData", android.content.Context.MODE_PRIVATE)
        employeeID = sharedPreferences.getString("nameOfUser", "").toString()
        Log.d(TAG, "Username: $employeeID")

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireContext())


        viewModel = ViewModelProvider(this)[FeatchViewModel::class.java]
        viewModel.fetchUserData(employeeID)
        viewModel.userData.observe(viewLifecycleOwner) { emp ->
            Log.d(TAG, "Fetched User Data: $emp")
            if (emp != null) {
                binding.userName.text = "Hi,${emp.nameOfUser}"
                binding.empID.text = "Employee ID: ${emp.employeeId}"
                binding.department.text = "Department: ${emp.department}"
            }
        }


        viewModel.featchLocation(employeeID)
        viewModel.locationData.observe(viewLifecycleOwner) {
            if (it != null) {
                storeLocation = it
                Log.d(TAG, "StoreLocation $storeLocation")
                getLocationOfUser(employeeID){loc->
                    if(loc !=null){
                        val distance = calculateDistanceAndroid(
                            loc.latitude,
                            loc.longitude,
                            storeLocation.latitude,
                            storeLocation.longitude
                        )
                        Log.d(TAG,"Distance: $distance")
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



        (activity as? AppCompatActivity)?.setSupportActionBar(binding.toolbar)
        setHasOptionsMenu(true)



            viewModel.featchUserProfile(employeeID)

            viewModel.fetchAttendance(employeeID)

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

            viewModel.fetchStatus(employeeID)
            viewModel.attendanceLiveData.observe(viewLifecycleOwner) { it ->
                if (it != null) {
                    attendanceData = it
                    Log.d(TAG, "AttendanceData$attendanceData")
                    val statusList = it.map { it.status }

                    val currentDate =getCurrentDate()
                    var isCheckedIn = false
                    for (data in attendanceData) {
                        Log.d(TAG, "CurrentDate: $currentDate and DataDate: ${data.checkedIn}")
                        if (data.date == currentDate) {
                            binding.checkIn.text = data.checkedIn
                            binding.checkOut.text = data.checkedOut
                            if(data.status =="Late"){
                                binding.status.setTextColor(Color.RED)
                            }
                            else{
                                binding.status.setTextColor(Color.GREEN)
                            }
                            binding.status.text =data.status
                            isCheckedIn = true
                            break
                        }
                    }
                    if (!isCheckedIn) {
                        binding.checkIn.text = "Not Checked In"
                        binding.checkOut.text = "Not Checked Out"
                    }

                    // Total days of month
                    val currentMonth = getCurrentDate().substring(5, 7) // Fixed extraction
                    val currentYear = getCurrentDate().substring(0, 4)  // Fixed extraction
                    val totalDays = getDaysInMonth(currentYear.toInt(), currentMonth.toInt())

                    val presentDays = statusList.count { it == "Present" }
                    val lateDays = statusList.count { it == "Late" }
                    val totalDaysAttendance = presentDays + lateDays

                    binding.totalAttendance.text = "$totalDaysAttendance Days"
                    binding.currentDay.text = currentDate
                    val entries = ArrayList<PieEntry>()

                    Log.d(TAG, "PresentDays: $presentDays")
                    binding.lateLeave.text = "$lateDays Days"


                    val monthMap = mapOf(
                        "01" to "January", "02" to "February", "03" to "March",
                        "04" to "April", "05" to "May", "06" to "June",
                        "07" to "July", "08" to "August", "09" to "September",
                        "10" to "October", "11" to "November", "12" to "December"
                    )

                    val nameOfMonth = monthMap[currentMonth] ?: "Unknown Month" // Prevents null
                    val daysOfMonth = getCurrentDate().takeLast(2)
                    binding.absentDays.text = "${daysOfMonth.toInt() - totalDaysAttendance.toInt()} Days"
                    Log.d(TAG, "NameOfMonth: $nameOfMonth")
                    binding.currentMonth.text = "$nameOfMonth $currentYear"
                    binding.monthName.text = "$nameOfMonth $currentYear"

                    // Pie Chart Fix
                    val remainingDay = totalDays - (presentDays + lateDays)
                    entries.add(PieEntry(remainingDay.toFloat() ))
                    entries.add(PieEntry(totalDaysAttendance.toFloat()))

                    val dataSet = PieDataSet(entries, "Attendance")
                    dataSet.colors = listOf(Color.YELLOW, Color.GREEN)

                    val data = PieData(dataSet)
                    binding.pieChart.data = data
                    binding.pieChart.invalidate() // Refresh chart

                    Log.d(TAG, "StatusList: $attendanceData")
                }
            }


            viewModel.fetchLastCheckIn(employeeID)

            viewModel2 = ViewModelProvider(this)[FeatchAndUploadViewModel::class.java]
            viewModel2.syncUserEmbeddings()


        binding.scanFace.setOnClickListener {
            showBottomSheet()
        }




        return binding.root
    }

    @Deprecated("Deprecated in Java")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.logOut -> {
                showLogoutDialog() // Show logout dialog
                return true
            }
            R.id.rateUs -> {
                    Toast.makeText(requireContext(),"Under Development",Toast.LENGTH_SHORT).show()
                return true
            }
            R.id.aboutUs -> {
                findNavController().navigate(R.id.action_dashBoardFragment_to_aboutUsFragment)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }


    @Deprecated("Deprecated in Java")
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.home_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    private fun getDaysInMonth(year: Int, month: Int): Int {
        return YearMonth.of(year, month).lengthOfMonth()
    }
    private fun getCurrentDate(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.format(Date()) // Returns "2025-03-18"
    }

    private fun rotateBitmap(source: Bitmap, angle: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(angle)
        return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
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
            findNavController().navigate(R.id.action_dashBoardFragment_to_faceRecognitionFragment)
            bottomSheetDialog.dismiss()
        }
        bottomSheetDialog.setOnShowListener { blurBackground(true) }
        bottomSheetDialog.setOnDismissListener { blurBackground(false) }
        bottomSheetDialog.show()
    }

    private fun blurBackground(enable: Boolean) {
        binding.root.alpha = if (enable) 0.3f else 1.0f // Reduce transparency when dialog is open
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
                Log.d(TAG, "Error: ${it.message}")
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




    private fun showLogoutDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Logout")
        builder.setMessage("Are you sure you want to log out?")

        // Positive Button: Navigate to another fragment and clear back stack
        builder.setPositiveButton("Logout") { _, _ ->
            val editor = sharedPreferences.edit()
            editor.putBoolean("isLoggedIn",false)

            editor.apply()
            val navOptions = NavOptions.Builder()
                .setPopUpTo(R.id.dashBoardFragment, true) // Clears backstack
                .build()
            findNavController().navigate(R.id.action_dashBoardFragment_to_signInFragment,null,navOptions)
            findNavController().popBackStack(R.id.dashBoardFragment, true)
            Toast.makeText(requireContext(),"Logout Successfully",Toast.LENGTH_SHORT).show()



        }

        // Cancel Button: Just dismiss the dialog
        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }

        val dialog = builder.create()
        dialog.show()
    }


}