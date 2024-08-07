package com.hci.loopsns.utils

import android.content.res.Resources

val Float.dp: Float
    get() = (this * Resources.getSystem().displayMetrics.density + 0.5f)
val Int.dp: Int
    get() = (this * Resources.getSystem().displayMetrics.density + 0.5f).toInt()