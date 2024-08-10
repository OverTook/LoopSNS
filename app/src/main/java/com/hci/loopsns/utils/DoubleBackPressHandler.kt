package com.hci.loopsns.utils

import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import com.hci.loopsns.MainActivity
import com.hci.loopsns.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.system.exitProcess

class DoubleBackPressHandler(private val activity: ComponentActivity) {

    private var doubleBackToExitPressedOnce = false

    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            if (doubleBackToExitPressedOnce) {
                if(activity is MainActivity) {
                    activity.finishAffinity()
                    exitProcess(0)
                }
                activity.finish()
            } else {
                doubleBackToExitPressedOnce = true
                //activity.toast("Press back again to exit")
                Toast.makeText(activity, activity.getString(R.string.double_back_to_exit), Toast.LENGTH_SHORT).show()
                activity.lifecycleScope.launch {
                    delay(2000)
                    doubleBackToExitPressedOnce = false
                }
            }
        }
    }

    fun enable() {
        activity.onBackPressedDispatcher.addCallback(onBackPressedCallback)
    }

    fun disable() {
        onBackPressedCallback.remove()
    }
}