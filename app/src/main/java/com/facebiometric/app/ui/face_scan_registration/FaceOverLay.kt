package com.facebiometric.app.ui.face_scan_registration

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View

class FaceOverLay(context: Context, attrs: AttributeSet?) : View(context, attrs) {
    private var faceBounds: RectF? = null
    private val paint = Paint().apply {
        color = Color.WHITE
        strokeWidth = 5f
        style = Paint.Style.STROKE
    }

    fun setFaceBounds(bounds: RectF?) {
        faceBounds = bounds
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        faceBounds?.let {
            canvas.drawRect(it, paint)
        }
    }
}
