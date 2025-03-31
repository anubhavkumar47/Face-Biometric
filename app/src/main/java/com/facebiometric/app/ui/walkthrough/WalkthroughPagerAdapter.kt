package com.facebiometric.app.ui.walkthrough

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.facebiometric.app.R

class WalkthroughPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    private val fragments = listOf(
        WalkthroughFragment.newInstance(
            "Biometric Authentication",
            "A futuristic biometric authentication system featuring fingerprint and facial recognition for secure access.",
            R.drawable.face_scan
        ),
        WalkthroughFragment.newInstance(
            "Real-Time Attendance Monitoring",
            "A live dashboard displaying real-time attendance data, absentee alerts, and notifications.",
            R.drawable.real_time
        ),
        WalkthroughFragment.newInstance(
            "Smart Attendance Management",
            "An integrated system that connects with HR software, logs attendance, and automates payroll.",
            R.drawable.smart_hr
        ),
        WalkthroughFragment.newInstance(
            "Location-Based Attendance Management",
            "A GPS-enabled system for verifying employee check-ins and attendance through biometric authentication.",
            R.drawable.location_based
        )
    )

    override fun getItemCount(): Int = fragments.size

    override fun createFragment(position: Int): Fragment = fragments[position]
}
