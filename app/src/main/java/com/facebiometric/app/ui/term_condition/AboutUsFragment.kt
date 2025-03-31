package com.facebiometric.app.ui.term_condition

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.facebiometric.app.R

class AboutUsFragment : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view =inflater.inflate(R.layout.fragment_about_us, container, false)

        // Open Website on Click
        val websiteBtn: Button = view.findViewById(R.id.btn_website)
        websiteBtn.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://facebiometric.com/"))
            startActivity(intent)
        }

        // Open Email on Click
        val emailBtn: Button = view.findViewById(R.id.btn_email)
        emailBtn.setOnClickListener {
            val intent = Intent(Intent.ACTION_SENDTO)
            intent.data = Uri.parse("mailto:contact@facebiometric.com") // Correct format
            intent.putExtra(Intent.EXTRA_SUBJECT, "")
            startActivity(intent)
        }


        return view
    }

    companion object {

    }
}