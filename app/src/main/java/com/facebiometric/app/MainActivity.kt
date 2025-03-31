package com.facebiometric.app

import android.annotation.SuppressLint
import android.content.IntentFilter
import android.location.LocationManager
import android.net.ConnectivityManager
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.facebiometric.app.ui.network.NetworkReceiver

class MainActivity : AppCompatActivity() {
    private lateinit var navController: NavController
    private lateinit var networkReceiver: NetworkReceiver
    private lateinit var bottomNavigationView: BottomNavigationView

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        bottomNavigationView =findViewById(R.id.bottomNavigation)

        // Get NavController from NavHostFragment
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as? NavHostFragment
        navController = navHostFragment?.navController ?: throw IllegalStateException("NavHostFragment not found")

        // Set up BottomNavigationView with NavController
        bottomNavigationView.setupWithNavController(navController)

        // Handle item clicks
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.homeFragment -> {
                    navController.navigate(R.id.dashBoardFragment)
                    true
                }
                R.id.videoFragment -> {
                    navController.navigate(R.id.attendanceFragment)
                    true
                }
                R.id.calenderFragment -> {
                    navController.navigate(R.id.historyFragment)
                    true
                }
                R.id.reportFragment -> {
                    navController.navigate(R.id.reportsFragment)
                    true
                }
                else -> false
            }
        }

        // Hide BottomNavigationView in unwanted fragments

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.dashBoardFragment, R.id.attendanceFragment, R.id.historyFragment ,R.id.reportsFragment-> {
                    bottomNavigationView.visibility = View.VISIBLE
                }
                else -> {
                    bottomNavigationView.visibility = View.GONE
                }
            }
        }


        // Pass NavController to NetworkReceiver
        val intentFilter = IntentFilter().apply {
            addAction(ConnectivityManager.CONNECTIVITY_ACTION)  // Internet
            addAction(LocationManager.PROVIDERS_CHANGED_ACTION) // GPS/Location
        }
        networkReceiver = NetworkReceiver(navController)
        registerReceiver(networkReceiver, intentFilter)
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(networkReceiver)
    }
}
