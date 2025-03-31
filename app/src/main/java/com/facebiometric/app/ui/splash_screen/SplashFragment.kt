package com.facebiometric.app.ui.splash_screen

import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.facebiometric.app.R

class SplashFragment : Fragment() {

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_splash, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedPreferences = requireActivity().getSharedPreferences("UserData", android.content.Context.MODE_PRIVATE)
        val isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false)

        Handler(Looper.getMainLooper()).postDelayed({
            if(isLoggedIn){
                findNavController().navigate(R.id.action_splashFragment_to_dashBoardFragment)
            }

            else{
                findNavController().navigate(R.id.action_splashFragment_to_walkthroughContainerFragment)
            }
        }, 2000) // 2 seconds delay





    }
}
