package com.facebiometric.app.ui.login

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.facebiometric.app.R
import com.facebiometric.app.databinding.FragmentSignUpBinding
import com.facebiometric.app.model.BranchDetails
import com.facebiometric.app.model.ImageModelClass
import com.facebiometric.app.model.LocationModel
import com.facebiometric.app.view_model.FeatchAndUploadViewModel
import com.facebiometric.app.view_model.UploadDataViewModel


class SignUpFragment : Fragment() {

    private lateinit var binding: FragmentSignUpBinding
    private lateinit var embeddingList: MutableList<String>
    private lateinit var postOfUser:String
    private lateinit var department:String
    private lateinit var viewModel: UploadDataViewModel
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var imageData:String
    private lateinit var  fetchAndUploadDataViewModel :FeatchAndUploadViewModel
    private  var branchDetails: MutableList<BranchDetails> = mutableListOf(BranchDetails("COM20250001","LORD BUDDHA KOSHI MEDICAL COLLEGE & HOSPITAL","8b9929769424fd6b2b443ca325ab88fd982c94b8878b1275234bb2736aa962b8"))

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        binding = FragmentSignUpBinding.inflate(inflater, container, false)
        setSpinner()
        embeddingList = mutableListOf()
        viewModel =ViewModelProvider(this)[UploadDataViewModel::class.java]
        fetchAndUploadDataViewModel = ViewModelProvider(this)[FeatchAndUploadViewModel::class.java]

        for (data in branchDetails) {
            fetchAndUploadDataViewModel.uploadBranchDetails(data)
        }

        viewModel.pushAllData.observe(viewLifecycleOwner){
            if (it == true){
                findNavController().navigate(R.id.action_signUpFragment_to_dashBoardFragment)
            }

        }

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireContext())

        viewModel.locationData.observe(viewLifecycleOwner){
            if(it == true){
                Toast.makeText(requireContext(), "Location Uploaded", Toast.LENGTH_SHORT).show()
            }
        }
        requestCameraPermission()
        requestLocationPermission()

        val sharedPreferences =requireContext().getSharedPreferences("UserData", Context.MODE_PRIVATE)
        val isLoggedIn = sharedPreferences.getBoolean("isLoggedIn",false)
        if(isLoggedIn){
            findNavController().navigate(R.id.action_signUpFragment_to_dashBoardFragment)
        }

        parentFragmentManager.setFragmentResultListener("faceEmbeddings", this) { _, bundle ->
            val embeddingArrayList = bundle.getSerializable("embeddings") as? ArrayList<String>
            if (embeddingArrayList != null) {
                embeddingList.clear()
                binding.btnScanFace.setCompoundDrawablesWithIntrinsicBounds(R.drawable.face_recognition,0,0,R.drawable.done)
                for (i in embeddingArrayList) {
                    embeddingList.add(i)
                   // embeddingList.add({embeddingArrayList[i].joinToString (",") })
                    Log.d("SignUpFragment", "Embedding $i: $i")
                }
                val imageDataPrevious = bundle.getString("imageData")
                if (imageDataPrevious != null) {
                    imageData = imageDataPrevious
                }
            }
            else{
                binding.btnScanFace.setCompoundDrawablesWithIntrinsicBounds(R.drawable.face_recognition,0,0,R.drawable.cancel)
            }
        }
        binding.btnScanFace.setOnClickListener {
            if(ContextCompat.checkSelfPermission(requireContext(),android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
                Toast.makeText(requireContext(), "Permission Denied", Toast.LENGTH_SHORT).show()
                requestCameraPermission()
            }
            else if(ContextCompat.checkSelfPermission(requireContext(),android.Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED){
                Toast.makeText(requireContext(), "Permission Denied", Toast.LENGTH_SHORT).show()
                requestLocationPermission()
            }
            else {
                showBottomSheet()
            }

        }
        binding.btnSignUp.setOnClickListener {
            takeUserInput()
        }
        binding.tvLogin.setOnClickListener {
            findNavController().navigate(R.id.action_signUpFragment_to_signInFragment)
        }

        binding.etPassword.setOnTouchListener { _, event ->
            if (event.action == android.view.MotionEvent.ACTION_UP) {
                val drawableEnd = binding.etPassword.compoundDrawablesRelative[2] // Get right drawable
                if (drawableEnd != null) {
                    val drawableWidth = drawableEnd.bounds.width()
                    if (event.rawX >= (binding.etPassword.right - drawableWidth - binding.etPassword.paddingEnd)) {
                        togglePasswordVisibility(binding.etPassword)
                        return@setOnTouchListener true
                    }
                }
            }
            false
        }
        binding.etConfirmPassword.setOnTouchListener { _, event ->
            if (event.action == android.view.MotionEvent.ACTION_UP) {
                val drawableEnd = binding.etConfirmPassword.compoundDrawablesRelative[2] // Get right drawable
                if (drawableEnd != null) {
                    val drawableWidth = drawableEnd.bounds.width()
                    if (event.rawX >= (binding.etConfirmPassword.right - drawableWidth - binding.etConfirmPassword.paddingEnd)) {
                        togglePasswordVisibility(binding.etConfirmPassword)
                        return@setOnTouchListener true
                    }
                }
            }
            false
        }
        binding.termAndCOn.setOnClickListener {
            if((it as CheckBox).isPressed){
                findNavController().navigate(R.id.action_signUpFragment_to_termAndConditionFragment)
            }
        }


        return binding.root
    }

    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(requireActivity(), arrayOf(android.Manifest.permission.CAMERA),1)
    }
    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(requireActivity(),arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),2)
    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if(requestCode == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            Toast.makeText(requireContext(), "Permission Granted", Toast.LENGTH_SHORT).show()
            showBottomSheet()
        }
        else if(requestCode == 2 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            showBottomSheet()
        }
        else{
            Toast.makeText(requireContext(), "Permission Denied", Toast.LENGTH_SHORT).show()
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun setSpinner(){
        val designations = listOf("Select Designation", "Software Engineer", "Project Manager", "HR Manager", "Sales Executive", "Accountant")
        val departments = listOf("Select Department","IT", "Human Resources", "Sales", "Marketing", "Finance", "Operations", "Customer Support")
        // Step 3: Create an ArrayAdapter
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, designations)
        val adapter2 = ArrayAdapter(requireContext(),android.R.layout.simple_spinner_item,departments)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        // Step 4: Attach adapter to Spinner
        binding.spinnerDestination.adapter = adapter
        binding.spinnerDepartment.adapter = adapter2
        // Step 5: Handle Spinner Selection
        binding.spinnerDestination.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedDestination = designations[position]
                if (position != 0) {
                    postOfUser = selectedDestination // Ignore "Select Destination"
                    //Toast.makeText(requireContext(), "Selected: $selectedDestination", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do nothing
            }
        }

        binding.spinnerDepartment.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedDepartment = departments[position]
                if (position != 0) {
                    department = selectedDepartment // Ignore "Select Destination"
                    //Toast.makeText(requireContext(), "Selected: $selectedDestination", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do nothing
            }
        }

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
            findNavController().navigate(R.id.action_signUpFragment_to_markAttendanceFragment)
            bottomSheetDialog.dismiss()
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun takeUserInput() {
        startLoading()
        val nameOfUser = binding.etFullName.text.toString()
        val emailOfUser = binding.etEmail.text.toString()
        val passwordOfUser = binding.etPassword.text.toString()
        val confirmPasswordOfUser = binding.etConfirmPassword.text.toString()
        val employeeId =binding.etEmpID.text.toString()
        val branchID =binding.etBranchID.text.toString()
        if(nameOfUser.isEmpty()){
            binding.etFullName.error = "Required field"
            stopLoading()
        }
        else if(emailOfUser.isEmpty()){
            binding.etEmail.error = "Required field"
            stopLoading()

        }
        else if(passwordOfUser.isEmpty()){
            binding.etPassword.error = "Required field"
            stopLoading()
        }
        else if(confirmPasswordOfUser.isEmpty()){
            binding.etConfirmPassword.error = "Required field"
            stopLoading()
        }
        else if(postOfUser .isEmpty()){
            Toast.makeText(requireContext(), "Please select a designation", Toast.LENGTH_SHORT).show()
            stopLoading()
        }
        else if(embeddingList.isEmpty()){
            Toast.makeText(requireContext(), "Please scan your face", Toast.LENGTH_SHORT).show()
            stopLoading()

        }
        else if(employeeId.isEmpty()){
            binding.etEmpID.error = "Required field"
            stopLoading()
        }
        else if (!binding.checkBox.isChecked){
            Toast.makeText(requireContext(),"Please accept our term & condition ! ",Toast.LENGTH_SHORT).show()
            stopLoading()


        }

        else{
            val listOfBranchId = branchDetails.map { it.BranchID }
            if(listOfBranchId.contains(branchID)){


                if(passwordOfUser == confirmPasswordOfUser){
                    viewModel.uploadData(employeeId,nameOfUser,emailOfUser,passwordOfUser,postOfUser,department,embeddingList)
                    getLocationOfUser(employeeId)
                    uploadImage(userName = employeeId,imageData)
                    binding.etFullName.text?.clear()
                    binding.etEmail.text?.clear()
                    binding.etPassword.text?.clear()
                    binding.etConfirmPassword.text?.clear()
                    binding.etEmpID.text?.clear()
                    binding.spinnerDestination.setSelection(0)
                    binding.spinnerDepartment.setSelection(0)
                    Toast.makeText(requireContext(), "Registration Successful", Toast.LENGTH_SHORT).show()
                    val sharedPreferences  =requireContext().getSharedPreferences("UserData", Context.MODE_PRIVATE)
                    val editor = sharedPreferences.edit()
                    editor.putString("nameOfUser",employeeId)
                    editor.putBoolean("isLoggedIn",true)
                    editor.apply()
                    stopLoading()

                }
            }


        }


    }

   private fun uploadImage(userName: String,imageData:String){
       if (imageData != null){
         //  val byteList: List<Int> = imageData.toList()
           val profileData = ImageModelClass(imageData,userName)
           viewModel.uploadUserProfile(profileData)
       }
   }
    private fun startLoading() {

        binding.progressBar.visibility = View.VISIBLE
    }

    private fun stopLoading() {

        binding.progressBar.visibility = View.GONE
    }

    private fun togglePasswordVisibility(editText: EditText) {
        if (editText.inputType == android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD) {
            // Hide Password
            editText.inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
            editText.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.eye_close, 0)
        } else {
            // Show Password
            editText.inputType = android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            editText.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.eye_open, 0)
        }
        editText.setSelection(editText.text.length) // Keep cursor at the end
    }

    @SuppressLint("MissingPermission")
    private fun getLocationOfUser(empId:String){
        val locationRequest =com.google.android.gms.location.CurrentLocationRequest.Builder()
            .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
            .build()
        fusedLocationProviderClient.getCurrentLocation(locationRequest.priority,null).addOnSuccessListener {
            val latitude = it.latitude
            val longitude = it.longitude
            val location = LocationModel(latitude,longitude,empId)
            viewModel.uploadLocation(location)
            Log.d("SignUpFragment","Latitude: $latitude, Longitude: $longitude")

        }.addOnFailureListener {
            Log.d("SignUpFragment","Error: ${it.message}")
        }
    }



}