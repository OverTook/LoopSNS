package com.hci.loopsns.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.hci.loopsns.R

// 어두운 오버레이를 추가하는 확장 함수

@SuppressLint("InflateParams")
fun Activity.showDarkOverlay() {
    // 이미 오버레이가 있는지 확인
    if (findViewById<View>(R.id.overlay_dark) != null) {
        return
    }

    val overlayView = LayoutInflater.from(this).inflate(R.layout.overlay_dark, null).apply {
        id = R.id.overlay_dark
    }
    addContentView(
        overlayView,
        ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
    )

    overlayView.animate()
        .alpha(1f)
        .setDuration(200) // 0.2초
        .start()
}

// 어두운 오버레이를 제거하는 확장 함수
fun Activity.hideDarkOverlay() {
    val overlayView = findViewById<View>(R.id.overlay_dark)
    overlayView?.animate()?.alpha(0f)?.setDuration(200)?.withEndAction {
        (overlayView.parent as ViewGroup).removeView(overlayView)
    }?.start()
}

@SuppressLint("InflateParams")
fun Activity.showGeneratingOverlay() {
    if (findViewById<View>(R.id.overlay_report_generating) != null) {
        return
    }

    val overlayView = LayoutInflater.from(this).inflate(R.layout.overlay_report_generating, null).apply {
        id = R.id.overlay_report_generating
    }
    addContentView(
        overlayView,
        ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
    )

    overlayView.animate()
        .alpha(1f)
        .setDuration(200) // 0.2초
        .start()
}

fun Activity.hideGeneratingOverlay() {
    val overlayView = findViewById<View>(R.id.overlay_report_generating)
    overlayView?.animate()?.alpha(0f)?.setDuration(200)?.withEndAction {
        (overlayView.parent as ViewGroup).removeView(overlayView)
    }?.start()
}