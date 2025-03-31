package com.facebiometric.app.ui.network

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.NetworkInfo
import androidx.navigation.NavController
import com.facebiometric.app.R

class NetworkReceiver(private val navController: NavController) : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {

        //for internet
        val connectivityManager = context?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork: NetworkInfo? = connectivityManager.activeNetworkInfo
        val isConnected = activeNetwork?.isConnectedOrConnecting == true

        //for location
        val locationManager = context?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val isLocationEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

        if (!isConnected) {
            // If internet is lost and NoInternetFragment is NOT already visible, navigate to it
            if (navController.currentDestination?.id != R.id.noInternetFragment) {
                navController.navigate(R.id.noInternetFragment)
            }
        }

        else if(!isLocationEnabled){
            // If location is not enabled and NLocationIFragment is NOT already visible, navigate to it
            if (navController.currentDestination?.id != R.id.noLocationFragment) {
                navController.navigate(R.id.noLocationFragment)
            }
        }
        else if(!isConnected && !isLocationEnabled){
            // If internet is lost and NoInternetFragment is NOT already visible, navigate to it
            if (navController.currentDestination?.id != R.id.noInternetFragment) {
                navController.navigate(R.id.noInternetFragment)
            }
        }
        else {
            // If internet is restored and NoInternetFragment is visible, pop it from the stack
            if (navController.currentDestination?.id == R.id.noInternetFragment) {
                navController.popBackStack()
            }

            // If location is restored and NoLocationFragment is visible, pop it from the stack
            if (navController.currentDestination?.id == R.id.noLocationFragment) {
                navController.popBackStack()
            }
        }


        if (!isLocationEnabled) {
            // Navigate to NoLocationFragment if location is disabled
            if (navController.currentDestination?.id != R.id.noLocationFragment) {
                navController.navigate(R.id.noLocationFragment)
            }
        } else {
            // If location is enabled and NoLocationFragment is visible, pop it from the stack
            if (navController.currentDestination?.id == R.id.noLocationFragment) {
                navController.popBackStack()
            }
        }

    }


}
