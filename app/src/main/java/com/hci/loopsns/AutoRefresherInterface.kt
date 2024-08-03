package com.hci.loopsns

import android.view.View

interface AutoRefresherInterface {

    fun getRequestAnimation(): View
    fun requestMoreData()

    var requested: Boolean
    var noMoreData: Boolean
    var requestAnimationView: View
}