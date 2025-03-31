package com.facebiometric.app.ui.network

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.facebiometric.app.databinding.FragmentNoInternetBinding


class NoInternetFragment : Fragment() {

    private lateinit var binding: FragmentNoInternetBinding
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        binding =FragmentNoInternetBinding.inflate(inflater,container,false)

        return binding.root
    }



}