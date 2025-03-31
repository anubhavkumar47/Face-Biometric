package com.facebiometric.app.ui.attendance_result

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.facebiometric.app.databinding.FragmentFaceScanSuccessBinding


class FaceScanSuccessFragment : Fragment() {

   private lateinit var binding: FragmentFaceScanSuccessBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentFaceScanSuccessBinding.inflate(inflater, container, false)





        return binding.root
    }



    companion object {
        private const val TAG = "FaceScanSuccessFragment"


    }
}