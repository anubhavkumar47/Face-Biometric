package com.facebiometric.app.ui.face_scan_registration

import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.camera.core.*
import com.facebiometric.app.R
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.common.util.concurrent.ListenableFuture
import com.facebiometric.app.databinding.FragmentFaceRegistrationBinding
import com.facebiometric.app.ml_model.FaceEmbeddingExtractor
import com.facebiometric.app.view_model.FeatchViewModel
import java.io.ByteArrayOutputStream
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class FaceRegistrationFragment : Fragment() {

    private var _binding: FragmentFaceRegistrationBinding? = null
    private val binding get() = _binding!!

    private lateinit var cameraExecutor: ExecutorService
    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>

    private val capturedFaces = mutableListOf<Bitmap>()

    private lateinit var viewModel: FeatchViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentFaceRegistrationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        cameraExecutor = Executors.newSingleThreadExecutor()
        startCamera()

    }

    private fun startCamera() {
        cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener({
            try {
                val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

                val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

                val preview = Preview.Builder()
                    .build()
                    .also {
                        it.setSurfaceProvider(binding.cameraPreview.surfaceProvider)
                    }

                val imageAnalyzer = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .also {
                        it.setAnalyzer(cameraExecutor, FaceAnalyzer(
                            overlay = binding.faceOverlay,
                            onFaceCaptured = { bitmap -> storeCapturedFace(bitmap) },
                            onProgressUpdate = { progress, message -> updateUI(progress, message) },
                            isFrontCamera = true
                        ))
                    }

                // ✅ Unbind any previous use cases & Bind new ones
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(viewLifecycleOwner, cameraSelector, preview, imageAnalyzer)

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
            Log.d("FaceRegistrationFragment", "All 4 face images captured! Proceeding to embeddings.")
            extractFaceEmbeddings()
            showScanCompleteDialog()  // 🔥 Show bottom sheet instead of navigating back
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
        val embeddingList: MutableList<String> = mutableListOf()
        val embeddingRef :MutableList<FloatArray> = mutableListOf()
        val extractor = FaceEmbeddingExtractor(requireContext())

        for (bitmap in capturedFaces) {
            val embedding = extractor.generateEmbedding(bitmap)
            embeddingRef.add(embedding)
        }
        for (i in embeddingRef.indices) {
            val embedding = embeddingRef[i].joinToString(",")
            embeddingList.add(embedding)
            Log.d("FaceRegistrationFragment", "Embedding $i: ${embeddingRef[i].joinToString(", ")}")
        }
        val imageByteAddress = bitmapToByteArray(capturedFaces[0])
        Log.d("RegistrationFragment", "Image Data: ${imageByteAddress.joinToString(",")}")
        val imageData =imageByteAddress.joinToString(",")
        val result = Bundle()
        result.putSerializable("embeddings", ArrayList(embeddingList))
        result.putString("imageData", imageData)
        parentFragmentManager.setFragmentResult("faceEmbeddings", result)

        Log.d("FaceRegistrationFragment", "Face embedding process completed!")
    }

    private fun showScanCompleteDialog() {
        val bottomSheetDialog = BottomSheetDialog(requireActivity())
        val view = layoutInflater.inflate(R.layout.bottom_sheet_scan_complete, null)
        bottomSheetDialog.setContentView(view)
        bottomSheetDialog.show()

        val cancelButton =view.findViewById<ImageView>(R.id.cancel_bottom_layout)
        val goBackButton = view.findViewById<Button>(R.id.goBack)
        val textView =view.findViewById<TextView>(R.id.title_bottom_layout)
        textView.text ="Face Registration Completed!"

        goBackButton.setOnClickListener {
            bottomSheetDialog.dismiss()
            findNavController().navigateUp() // 🔥 Go back after user clicks "Go Back"
        }

        cancelButton.setOnClickListener {
            bottomSheetDialog.dismiss()
            resetCaptureProcess()
        }

        bottomSheetDialog.setOnShowListener { blurBackground(true) }
        bottomSheetDialog.setOnDismissListener { blurBackground(false) }
        bottomSheetDialog.show()
    }

    private fun resetCaptureProcess() {
        capturedFaces.clear()
        updateUI(0, "Please align your face properly")
        startCamera() // Restart camera
    }

    private fun blurBackground(enable: Boolean) {
        binding.root.alpha = if (enable) 0.3f else 1.0f // Reduce transparency when dialog is open
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        cameraExecutor.shutdown()
    }

    private fun bitmapToByteArray(bitmap: Bitmap, format: Bitmap.CompressFormat = Bitmap.CompressFormat.PNG, quality: Int = 100): ByteArray {
        val stream = ByteArrayOutputStream()
        bitmap.compress(format, quality, stream)
        return stream.toByteArray()
    }
}
