package com.facebiometric.app.ui.face_scan_recognition

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.common.util.concurrent.ListenableFuture
import com.facebiometric.app.R
import com.facebiometric.app.databinding.FragmentFaceRecognitionBinding
import com.facebiometric.app.ml_model.FaceEmbeddingExtractor
import com.facebiometric.app.model.Attendance
import com.facebiometric.app.model.EmployeeIDAndEmbedding
import com.facebiometric.app.view_model.FeatchAndUploadViewModel
import com.facebiometric.app.view_model.FeatchViewModel
import com.facebiometric.app.view_model.UploadDataViewModel
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.math.sqrt
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.location.Location
import android.widget.Toast
import com.facebiometric.app.api.PunchRequest
import com.facebiometric.app.model.BranchDetails
import com.facebiometric.app.model.LocationModel
import com.facebiometric.app.view_model.ApiViewModel


class FaceRecognitionFragment : Fragment() {
    private lateinit var binding: FragmentFaceRecognitionBinding
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>
    private val capturedFaces = mutableListOf<Bitmap>()
    private lateinit var sharedPreferences: SharedPreferences
    private var currentEmbeddingList: MutableList<String> = mutableListOf()
    private lateinit var employeeID: String
    private var similarity: MutableList<Float> = mutableListOf()
    private val storeEmbeddingList: MutableList<MutableList<String>?> = mutableListOf()
    private val storeNameList: MutableList<String> = mutableListOf()
    private val nameAndEmbedding: MutableList<EmployeeIDAndEmbedding> = mutableListOf()
    private lateinit var viewModel: FeatchAndUploadViewModel
    private lateinit var viewModelPush: UploadDataViewModel
    private lateinit var featchViewModel: FeatchViewModel
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var storeLocation: LocationModel
    private lateinit var apiViewModel :ApiViewModel
    private  var branchDetails: MutableList<BranchDetails> = mutableListOf(BranchDetails("COM20250001","LORD BUDDHA KOSHI MEDICAL COLLEGE & HOSPITAL","8b9929769424fd6b2b443ca325ab88fd982c94b8878b1275234bb2736aa962b8"))


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentFaceRecognitionBinding.inflate(inflater, container, false)
        sharedPreferences = requireActivity().getSharedPreferences("UserData", Context.MODE_PRIVATE)
        employeeID = sharedPreferences.getString("nameOfUser", "").toString()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Ensure fetchData() returns List<List<Float>>

        cameraExecutor = Executors.newSingleThreadExecutor()
        startCamera()

        viewModel = ViewModelProvider(this)[FeatchAndUploadViewModel::class.java]
        viewModelPush = ViewModelProvider(this)[UploadDataViewModel::class.java]
        featchViewModel = ViewModelProvider(this)[FeatchViewModel::class.java]
        apiViewModel =ViewModelProvider(this)[ApiViewModel::class.java]

        apiViewModel.punchResponse.observe(viewLifecycleOwner){
            if(it != null){
                Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
            }
        }

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireContext())

        featchViewModel.fetchUserData(employeeID)

        featchViewModel.featchLocation(employeeID)
        featchViewModel.locationData.observe(viewLifecycleOwner) {
            if (it != null) {
                storeLocation = it
                Log.d(TAG, "StoreLocation $storeLocation")
            }

        }

        // Fetch Data
        viewModel.fetchNameAndEmbedding()

        // Observe Data
        viewModel.embeddingAndName.observe(viewLifecycleOwner) { embeddings ->
            if (embeddings != null) {
                nameAndEmbedding.addAll(embeddings)
                Log.d(TAG, "StoreNameAndEmbedding$nameAndEmbedding")
                for (embedding in embeddings) {
                    storeEmbeddingList.add(embedding.embeddingList)
                    Log.d(TAG, "StoreEmbedding$storeEmbeddingList")
                    storeNameList.add(embedding.employeeID)
                    Log.d(TAG, "StoreName $storeNameList")
                }
                Log.d("FaceRecognition", "Embeddings fetched size: ${embeddings.size}")
            } else {
                Log.e("FaceRecognition", "Failed to fetch embeddings")
            }
        }

        viewModelPush.pushAttendanceData.observe(viewLifecycleOwner) {
            if (it == true) {
                Log.d(TAG, "Attendance Uploaded Successfully")
            }
        }
    }

    private fun startCamera() {
        cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener({
            try {
                val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

                // ✅ Select Front Camera
                val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

                // ✅ Create Preview
                val preview = Preview.Builder()
                    .build()
                    .also {
                        it.setSurfaceProvider(binding.cameraPreview.surfaceProvider)
                    }

                // ✅ Create Image Analyzer
                val imageAnalyzer = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .also {
                        it.setAnalyzer(
                            cameraExecutor, FaceAnalyzerSecondary(
                                overlay = binding.faceOverlay,
                                onFaceCaptured = { bitmap -> storeCapturedFace(bitmap) },
                                onProgressUpdate = { progress, message ->
                                    updateUI(
                                        progress,
                                        message
                                    )
                                },
                                isFrontCamera = true
                            )
                        )
                    }

                // ✅ Unbind any previous use cases & Bind new ones
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    viewLifecycleOwner,
                    cameraSelector,
                    preview,
                    imageAnalyzer
                )

            } catch (e: Exception) {
                Log.e("FaceRegistrationFragment", "Camera initialization failed", e)
            }
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun storeCapturedFace(bitmap: Bitmap) {
        if (capturedFaces.size < 4) {
            capturedFaces.add(bitmap)
        }

        updateUI(capturedFaces.size * 25, "Capturing face ${capturedFaces.size}/4...")

        if (capturedFaces.size == 4) {
            Log.d(
                "FaceRegistrationFragment",
                "All 4 face images captured! Proceeding to embeddings."
            )
            extractFaceEmbeddings() // 🔥 Auto-start embedding process
        }
    }

    private fun updateUI(progress: Int, message: String) {
        activity?.runOnUiThread {
            binding.progressBar.setProgress(progress.toFloat())  // Update progress bar
            binding.progressBar.setText("$progress%")  // Update progress text
            binding.instructionText.text = message    // Update instructions
        }
    }

    private fun extractFaceEmbeddings() {
        currentEmbeddingList = mutableListOf()
        val embeddingRef: MutableList<FloatArray> = mutableListOf()
        val extractor = FaceEmbeddingExtractor(requireContext())

        for (bitmap in capturedFaces) {
            val embedding = extractor.generateEmbedding(bitmap)
            embeddingRef.add(embedding)
        }
        currentEmbeddingList.clear()
        for (i in embeddingRef.indices) {
            val embedding = embeddingRef[i].joinToString(",")
            currentEmbeddingList.add(embedding)
            Log.d("FaceRegistrationFragment", "Embedding $i: ${embeddingRef[i].joinToString(", ")}")
        }

        compareToEmbedding(currentEmbeddingList)
        Log.d("FaceRegistrationFragment", "Face embedding process completed! ")
    }

    private fun compareToEmbedding(currentEmbeddingList: MutableList<String>) {
        if (currentEmbeddingList.isEmpty()) {
            Log.e(TAG, "Error: Current embedding list is empty!")
            return
        }
        if (storeEmbeddingList.isEmpty()) {
            Log.e(TAG, "Error: Stored embedding list is empty!")
            return
        }

        val similarity = mutableListOf<Float>()
        val indexMap = mutableMapOf<Int, String>()
        var index1D = 0

        // Iterate over all embeddings in currentEmbeddingList
        for (currentEmbeddingStr in currentEmbeddingList) {
            val currentEmbedding = currentEmbeddingStr.split(",").map { it.toFloatOrNull() ?: 0f }

            Log.d(TAG, "Processing current embedding: $currentEmbeddingStr")
            Log.d(TAG, "Current embedding size: ${currentEmbedding.size}")

            // Compare with stored embeddings
            for ((nameIndex, embeddingList) in storeEmbeddingList.withIndex()) {
                Log.d(TAG, "StoreEmbedding: $embeddingList")

                if (embeddingList != null) {
                    for (storedEmbedding in embeddingList) {
                        val cosineSim = cosineSimilarity(currentEmbedding.toString(), storedEmbedding)
                        indexMap[index1D] = storeNameList[nameIndex]
                        similarity.add(cosineSim)
                        index1D++
                    }
                }
            }
        }

        // Find the maximum similarity and its corresponding name
        val maxSimilarity = similarity.maxOrNull()
        val maxIndex = similarity.indexOf(maxSimilarity)
        val recognizedName = indexMap[maxIndex]
        Log.d(TAG, "Similarity: $similarity")

        Log.d(TAG, "Name of person with highest similarity: $recognizedName (Similarity: $maxSimilarity)")
        showScanCompleteDialog(recognizedName)  // 🔥 Show bottom sheet instead of navigating back
    }



    override fun onDestroyView() {
        super.onDestroyView()
        cameraExecutor.shutdown()
    }

    private fun cosineSimilarity(embedding1: String, embedding2: String): Float {
        val array1 = embedding1.toFloatArray()
        val array2 = embedding2.toFloatArray()

        require(array1.size == array2.size) { "Embeddings must have the same size" }

        var dotProduct = 0f
        var normA = 0f
        var normB = 0f

        for (i in array1.indices) {
            dotProduct += array1[i] * array2[i]
            normA += array1[i] * array1[i]
            normB += array2[i] * array2[i]
        }

        return if (normA == 0f || normB == 0f) {
            0f // Avoid division by zero
        } else {
            dotProduct / (sqrt(normA.toDouble()) * sqrt(normB.toDouble())).toFloat()
        }
    }

    // Helper function to convert string to FloatArray
    private fun String.toFloatArray(): FloatArray {
        return this
            .removeSurrounding("[", "]")  // Remove brackets if present
            .split(",")                    // Split by commas
            .map { it.trim().toFloat() }    // Convert to float
            .toFloatArray()
    }

    private fun showScanCompleteDialog(nameDetected: String?) {
        val bottomSheetDialog = BottomSheetDialog(requireActivity())
        val view = layoutInflater.inflate(R.layout.bottom_sheet_scan_complete, null)
        bottomSheetDialog.setContentView(view)
        bottomSheetDialog.show()

        val cancelButton = view.findViewById<ImageView>(R.id.cancel_bottom_layout)
        val goBackButton = view.findViewById<Button>(R.id.goBack)
        cancelButton.visibility = View.INVISIBLE
        val textView = view.findViewById<TextView>(R.id.title_bottom_layout)
        if (nameDetected == employeeID) {
            var currentLocation: LocationModel
            getLocationOfUser(employeeID) {
                if (it != null) {
                    currentLocation = it
                    if (storeLocation !=null) {
                        val distance = calculateDistanceAndroid(
                            currentLocation.latitude,
                            currentLocation.longitude,
                            storeLocation.latitude,
                            storeLocation.longitude
                        )
                        Log.d(TAG, "Distance $distance")

                        val branchDet =branchDetails[0]
                        val apiData =PunchRequest(branchDet.BranchID,employeeID,currentLocation.latitude.toString(),currentLocation.longitude.toString(),branchDet.ApiKey)
                        apiViewModel.sendPunchData(apiData)
                        featchViewModel.userData.observe(viewLifecycleOwner){emp ->
                            if (emp != null){
                                val name =emp.nameOfUser
                            textView.text = "Thank You $name!\nYour attendance is marked for this day \uD83D\uDE0A.\nDistance from verified location is $distance meters"
                            uploadAttendanceDetails(nameDetected)
                                }


                        }


                    }
                }
            }


        } else {
            textView.setTextColor(ContextCompat.getColor(requireContext(), R.color.textRed))
            textView.text =
                "Close your mobile camera to near to your face.\n Or \nPlease LogIn with your account.Your attendance not marked!"
        }

        goBackButton.setOnClickListener {
            bottomSheetDialog.dismiss()
            findNavController().navigateUp() // 🔥 Go back after user clicks "Go Back"
        }

        bottomSheetDialog.setOnShowListener { blurBackground(true) }
        bottomSheetDialog.setOnDismissListener { blurBackground(false) }
        bottomSheetDialog.show()
    }


    private fun uploadAttendanceDetails(nameDetected: String?) {
        val date = getCurrentDate()
        val checkedIn = getCurrentTime()
        val checkedOut =getCurrentTime()
        var status: String = ""
        if (checkedIn <= "5:00:00") {
            status = "Late"
            val attendance = Attendance(nameDetected!!, date, checkedIn,checkedOut, status)
            viewModelPush.uploadAttendance(attendance)
        } else {
            status = "Present"
            val attendance = Attendance(nameDetected!!, date, checkedIn,checkedOut, status)
            viewModelPush.uploadAttendance(attendance)
        }


    }

    private fun blurBackground(enable: Boolean) {
        binding.root.alpha = if (enable) 0.1f else 1.0f // Reduce transparency when dialog is open
    }


    companion object {
        private const val TAG = "FaceRecognition"
    }

    private fun getCurrentDate(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.format(Date()) // Returns "2025-03-18"
    }

    private fun getCurrentTime(): String {
        val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        return timeFormat.format(Date()) // Returns "14:30:45"
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
            Log.d("SignUpFragment", "Error: ${it.message}")
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