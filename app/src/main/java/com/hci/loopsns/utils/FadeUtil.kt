package com.hci.loopsns.utils

import android.view.View
import android.view.ViewGroup

fun View.fadeIn(alpha: Float, duration: Long) {
    this.clearAnimation()

    this.alpha = 0F
    this.visibility = View.VISIBLE

    this.animate()
        .alpha(alpha)
        .setDuration(duration) // 0.2초
        .start()
}

fun View.fadeOut(duration: Long) {
    this.clearAnimation()

    this.animate().alpha(0f).setDuration(duration).withEndAction {
        this.visibility = View.GONE
    }.start()
}