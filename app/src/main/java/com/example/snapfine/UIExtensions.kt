package com.example.snapfine

import android.annotation.SuppressLint
import android.app.Activity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar

/**
 * Extension to add scale animation on touch to any view.
 */
@SuppressLint("ClickableViewAccessibility")
fun View.applyScaleAnimation() {
    this.setOnTouchListener { v, event ->
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                v.animate().scaleX(0.95f).scaleY(0.95f).setDuration(100).start()
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                v.animate().scaleX(1f).scaleY(1f).setDuration(100).start()
            }
        }
        false
    }
}

/**
 * Shows a themed Success Snackbar.
 */
fun Activity.showSuccessSnackbar(message: String, anchor: View? = null) {
    val root = findViewById<View>(android.R.id.content)
    val snackbar = Snackbar.make(root, message, Snackbar.LENGTH_LONG)
    anchor?.let { snackbar.anchorView = it }
    snackbar.view.setBackgroundColor(ContextCompat.getColor(this, R.color.statusSuccess))
    val tv = snackbar.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
    tv.setTextColor(ContextCompat.getColor(this, R.color.white))
    snackbar.show()
}

/**
 * Shows a themed Error Snackbar.
 */
fun Activity.showErrorSnackbar(message: String, anchor: View? = null) {
    val root = findViewById<View>(android.R.id.content)
    val snackbar = Snackbar.make(root, message, Snackbar.LENGTH_LONG)
    anchor?.let { snackbar.anchorView = it }
    snackbar.view.setBackgroundColor(ContextCompat.getColor(this, R.color.statusError))
    val tv = snackbar.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
    tv.setTextColor(ContextCompat.getColor(this, R.color.white))
    snackbar.show()
}

/**
 * Sets visibility of all children in a ViewGroup.
 */
fun ViewGroup.setChildrenVisibility(visibility: Int) {
    for (i in 0 until childCount) {
        getChildAt(i).visibility = visibility
    }
}
