package com.facebiometric.app.ui.login

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.facebiometric.app.R
import com.facebiometric.app.databinding.FragmentSignInBinding
import com.facebiometric.app.model.UserDataWithCon
import com.facebiometric.app.view_model.FeatchAndUploadViewModel
import com.facebiometric.app.view_model.FeatchViewModel


class SignInFragment : Fragment() {
    private lateinit var binding: FragmentSignInBinding
    private lateinit var viewModel: FeatchViewModel
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var userName: String
    private lateinit var allUserData: MutableList<UserDataWithCon>
    private lateinit var viewModel2:FeatchAndUploadViewModel

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        binding = FragmentSignInBinding.inflate(inflater, container, false)

        viewModel = ViewModelProvider(this)[FeatchViewModel::class.java]
        viewModel2 = ViewModelProvider(this)[FeatchAndUploadViewModel::class.java]
        viewModel2.syncUserEmbeddings()
        sharedPreferences = requireActivity().getSharedPreferences("UserData", Context.MODE_PRIVATE)
        val isLoggedIn = sharedPreferences.getBoolean("isLoggedIn",false)
        if(isLoggedIn){
            findNavController().navigate(R.id.action_signInFragment_to_dashBoardFragment)
        }
        requestCameraPermission()
        requestLocationPermission()

        viewModel.featchAllData()
        viewModel.allFeatchData.observe(viewLifecycleOwner) { userData ->
            if (userData != null) {
                allUserData = userData
            }
        }


        binding.tvSignup.setOnClickListener {
            findNavController().navigate(R.id.action_signInFragment_to_signUpFragment)
        }
        binding.btnSignIN.setOnClickListener {
            if(ContextCompat.checkSelfPermission(requireContext(),android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
                Toast.makeText(requireContext(), "Permission Denied", Toast.LENGTH_SHORT).show()
                requestCameraPermission()
            }
            else if(ContextCompat.checkSelfPermission(requireContext(),android.Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
                Toast.makeText(requireContext(), "Permission Denied", Toast.LENGTH_SHORT).show()
                requestLocationPermission()
            }
            else {
                validateInput()
            }


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

        binding.termAndCOn.setOnClickListener {

                findNavController().navigate(R.id.action_signInFragment_to_termAndConditionFragment)

        }


        return binding.root
        }


        private fun  validateInput() {
            binding.progressBar.visibility =View.VISIBLE
            val listOfRegisteredEmployee = mutableListOf<String>()
            for (userData in allUserData) {
                listOfRegisteredEmployee.add(userData.employeeId)
            }
            val passwordOfAllUser = mutableListOf<String>()
            for (userData in allUserData) {
                passwordOfAllUser.add(userData.passwordOfUser)
            }
            val listOfNameOfUser = mutableListOf<String>()
            for (userData in allUserData) {
                listOfNameOfUser.add(userData.nameOfUser)
            }
            val empId = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()
            if(empId.isEmpty() ){
                binding.etEmail.error = "EmployeeID is required"
                binding.progressBar.visibility =View.GONE

            }
            else if(password.isEmpty()){
                binding.etPassword.error = "Password is required"
                binding.progressBar.visibility =View.GONE

            }
            else if (!binding.checkBox.isChecked){
                Toast.makeText(requireContext(),"Please accept our term & condition ! ",Toast.LENGTH_SHORT).show()
                binding.progressBar.visibility =View.GONE


            }

            else{
                if(empId in listOfRegisteredEmployee && password in passwordOfAllUser){
                    val index = listOfRegisteredEmployee.indexOf(empId)
                    val nameOfUser = listOfNameOfUser[index]
                    Log.d("SignINFragment","Name of User: $nameOfUser")
                    sharedPreferences.edit().putBoolean("isLoggedIn", true).apply()
                    sharedPreferences.edit().putString("nameOfUser", empId).apply()
                    findNavController().navigate(R.id.action_signInFragment_to_dashBoardFragment)
                    binding.progressBar.visibility =View.GONE

                }
                else{
                    Toast.makeText(requireContext(),"Invalid EmployeeID or Password",Toast.LENGTH_SHORT).show()
                    binding.progressBar.visibility =View.GONE

                }
            }

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
            validateInput()
        }
        else if(requestCode == 2 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            validateInput()
        }
        else{
            Toast.makeText(requireContext(), "Permission Denied", Toast.LENGTH_SHORT).show()
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

}