package com.hci.loopsns

import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageButton
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import com.google.firebase.auth.FirebaseAuth
import com.hci.loopsns.storage.SettingManager
import java.util.Locale
import kotlin.math.abs

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
        findViewById<ConstraintLayout>(R.id.setting_language).setOnClickListener(this)
    }

    override fun onClick(view: View?) {
        if(view == null) return

        when(view.id) {
            R.id.backButton -> {
                finish()
            }
            R.id.setting_dark_mode -> {
                val from = setting.getNightMode()
                MaterialDialog(this).listItemsSingleChoice(items = listOf(getString(R.string.settings_off), getString(R.string.settings_on), getString(R.string.settings_system_default)), initialSelection = from) { _, index, _ ->
                    if(from == index) return@listItemsSingleChoice

                    val isSystemNightMode = setting.isSystemNightMode(this)
                    if((index == 2 || from == 2) && (abs(from - index) == 1 && isSystemNightMode) || (abs(from - index) == 2 && !isSystemNightMode)) {
                        setting.setNightMode(this, index)
                        return@listItemsSingleChoice
                    }

                    MaterialDialog(this).show {
                        title(R.string.settings_ask_app_restart_head)
                        message(R.string.settings_ask_app_restart_body)
                        positiveButton(R.string.settings_ask_app_restart_ok) { _ ->
                            setting.setNightMode(this@SettingsActivity, index)
                        }
                        negativeButton(R.string.settings_ask_app_restart_no) { dialog ->
                            dialog.dismiss()
                        }
                    }
                }.title(R.string.settings_dark_mode).show()
            }
            R.id.setting_language -> {
                val list = ArrayList<String>(SettingManager.SupportedLanguage.size)
                SettingManager.SupportedLanguage.forEach {
                    list.add(it.displayLanguage)
                }
                list.add(getString(R.string.settings_system_default))

                val from = setting.getLocaleIndex()
                MaterialDialog(this).listItemsSingleChoice(items = list, initialSelection = from) { _, index, _ ->
                    if(from == index) return@listItemsSingleChoice

                    if(index == list.size - 1) { //선택지가 Auto라면
                        if(setting.isSameLanguage(Locale.getDefault().language)) {
                            setting.setLocaleAuto(this) //기존 언어와 Auto 언어가 같음
                            return@listItemsSingleChoice
                        }
                    }

                    if(from == list.size - 1) { //
                        if(Resources.getSystem().configuration.locales[0].language == SettingManager.SupportedLanguage[index].language) { //새로운 언어와 기존 Auto 언어가 같음
                            setting.setLocale(this, SettingManager.SupportedLanguage[index].language)
                            return@listItemsSingleChoice
                        }
                    }

                    MaterialDialog(this).show {
                        title(R.string.settings_ask_app_restart_head)
                        message(R.string.settings_ask_app_restart_body)
                        positiveButton(R.string.settings_ask_app_restart_ok) { _ ->
                            setting.setLocale(this@SettingsActivity, SettingManager.SupportedLanguage[index].language)
                        }
                        negativeButton(R.string.settings_ask_app_restart_no) { dialog ->
                            dialog.dismiss()
                        }
                    }
                }.title(R.string.settings_language).show()
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