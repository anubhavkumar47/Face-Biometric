package com.facebiometric.app.ui.walkthrough

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.facebiometric.app.R

class WalkthroughContainerFragment : Fragment() {

    private lateinit var viewPager: ViewPager2
    private lateinit var btnNext: Button
    private lateinit var btnSkip: Button
    private lateinit var tabLayout: TabLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_walkthrough_container, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewPager = view.findViewById(R.id.viewPager)
        tabLayout = view.findViewById(R.id.tabLayout)
        btnNext = view.findViewById(R.id.btnNext)
        btnSkip = view.findViewById(R.id.btnSkip)

        val adapter = WalkthroughPagerAdapter(this)
        viewPager.adapter = adapter

        // Link TabLayout with ViewPager2
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            // Optional: You can set tab titles for each page
           // tab.text = "Page ${position + 1}" // or customize it
        }.attach()

        btnNext.setOnClickListener {
            if (viewPager.currentItem < adapter.itemCount - 1) {
                viewPager.currentItem += 1
            } else {
                goToLogINScreen()
            }
        }

        btnSkip.setOnClickListener {
           goToLogINScreen()
        }
    }

   private fun goToLogINScreen() {
       findNavController().navigate(R.id.action_walkthroughContainerFragment_to_signInFragment)

    }


}

