package com.facebiometric.app.ui.walkthrough

import android.view.View
import androidx.viewpager2.widget.ViewPager2
import kotlin.math.abs

class WalkthroughPageTransformer : ViewPager2.PageTransformer {
    override fun transformPage(view: View, position: Float) {
        when {
            position < -1 -> view.alpha = 0f
            position <= 0 -> {
                view.alpha = 1f
                view.translationX = 0f
                view.scaleX = 1f
                view.scaleY = 1f
            }
            position <= 1 -> {
                view.alpha = 1 - position
                view.translationX = view.width * -position
                val scaleFactor = 0.85f + (1 - 0.85f) * (1 - abs(position))
                view.scaleX = scaleFactor
                view.scaleY = scaleFactor
            }
            else -> view.alpha = 0f
        }
    }
}
