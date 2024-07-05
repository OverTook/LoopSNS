package com.contest.kdbstartup.gps

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresApi

class GPSPermission {

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun backgroundPermission(activity: Activity){
//        ActivityCompat.requestPermissions(
//            activity,
//            arrayOf(
//                android.Manifest.permission.ACCESS_BACKGROUND_LOCATION,
//            ), 2)
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun permissionDialog(activity: Activity){
        var builder = AlertDialog.Builder(activity.applicationContext)
        builder.setTitle("백그라운드 위치 권한을 위해 항상 허용으로 설정해주세요.")

        var listener = DialogInterface.OnClickListener { _, p1 ->
            when (p1) {
                DialogInterface.BUTTON_POSITIVE ->
                    backgroundPermission(activity)
            }
        }
        builder.setPositiveButton("네", listener)
        builder.setNegativeButton("아니오", null)

        builder.show()
    }

    private fun requestPermission(activity: Activity){
        // 이미 권한이 있으면 그냥 리턴
//        if(ContextCompat.checkSelfPermission(activity, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
//            && ContextCompat.checkSelfPermission(activity, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
//            && ContextCompat.checkSelfPermission(activity, android.Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED){
//
//            return
//        }
//
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//            ActivityCompat.requestPermissions(
//                activity,
//                arrayOf(
//                    android.Manifest.permission.ACCESS_COARSE_LOCATION,
//                    android.Manifest.permission.ACCESS_FINE_LOCATION
//                ), 1)
//            permissionDialog(activity)
//        }
//        // API 23 미만 버전에서는 ACCESS_BACKGROUND_LOCATION X
//        else {
//            ActivityCompat.requestPermissions(
//                activity,
//                arrayOf(
//                    android.Manifest.permission.ACCESS_COARSE_LOCATION,
//                    android.Manifest.permission.ACCESS_FINE_LOCATION
//                ), 1)
//        }
    }
}