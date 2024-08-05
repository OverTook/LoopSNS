package com.hci.loopsns

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.AppCompatImageButton
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import com.google.firebase.auth.FirebaseAuth
import com.hci.loopsns.storage.SettingManager

class SettingsActivity : AppCompatActivity(), View.OnClickListener {

    val setting: SettingManager = SettingManager.getInstance(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        findViewById<AppCompatImageButton>(R.id.backButton).setOnClickListener(this)
        findViewById<ConstraintLayout>(R.id.setting_logout).setOnClickListener(this)
        findViewById<ConstraintLayout>(R.id.setting_unregister).setOnClickListener(this)
        findViewById<ConstraintLayout>(R.id.setting_dark_mode).setOnClickListener(this)
    }

    override fun onClick(view: View?) {
        if(view == null) return

        when(view.id) {
            R.id.backButton -> {
                finish()
            }
            R.id.setting_dark_mode -> {
                MaterialDialog(this).listItemsSingleChoice(items = listOf("꺼짐", "켜짐", "시스템 기본값"), initialSelection = setting.getNightMode()) { _, index, _ ->
                    Log.e("SELECT", index.toString())
                    setting.setNightMode(this, index)
                }.title(R.string.setting_dark_mode).show()
            }
            R.id.setting_logout -> {
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(
                    this@SettingsActivity,
                    LoginActivity::class.java
                )
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(
                    intent
                )
            }
        }
    }


}