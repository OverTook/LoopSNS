package com.hci.loopsns

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.Resources
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.Spannable
import android.text.SpannableString
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageButton
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.WhichButton
import com.afollestad.materialdialogs.actions.setActionButtonEnabled
import com.afollestad.materialdialogs.input.getInputField
import com.afollestad.materialdialogs.input.input
import com.afollestad.materialdialogs.list.listItemsMultiChoice
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.hci.loopsns.event.ProfileListener
import com.hci.loopsns.event.ProfileManager
import com.hci.loopsns.network.DeleteFcmTokenRequest
import com.hci.loopsns.network.FcmTokenResponse
import com.hci.loopsns.network.NetworkManager
import com.hci.loopsns.network.UnregisterResponse
import com.hci.loopsns.storage.NotificationType
import com.hci.loopsns.storage.SettingManager
import com.hci.loopsns.utils.hideDarkOverlay
import com.hci.loopsns.utils.showDarkOverlay
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Locale
import kotlin.math.abs

class SettingsActivity : AppCompatActivity(), View.OnClickListener, ProfileListener {

    private val setting: SettingManager = SettingManager.getInstance()
    private lateinit var user: FirebaseUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        user = FirebaseAuth.getInstance().currentUser!!
        findViewById<TextView>(R.id.nickname).text = user.displayName

        if(user.photoUrl != null) {
            Glide.with(this)
                .load(user.photoUrl!!.toString())
                .into(findViewById(R.id.profile_image))
        }

        ProfileManager.getInstance().registerProfileListener(this)

        findViewById<AppCompatImageButton>(R.id.backButton).setOnClickListener(this)
        findViewById<ConstraintLayout>(R.id.setting_logout).setOnClickListener(this)
        findViewById<ConstraintLayout>(R.id.setting_unregister).setOnClickListener(this)
        findViewById<ConstraintLayout>(R.id.setting_dark_mode).setOnClickListener(this)
        findViewById<ConstraintLayout>(R.id.setting_language).setOnClickListener(this)
        findViewById<ConstraintLayout>(R.id.setting_notification_setting).setOnClickListener(this)
        findViewById<ConstraintLayout>(R.id.setting_edit_profile_top).setOnClickListener(this)
        findViewById<ConstraintLayout>(R.id.setting_edit_profile).setOnClickListener(this)
        findViewById<ConstraintLayout>(R.id.setting_terms_of_use).setOnClickListener(this)
        findViewById<ConstraintLayout>(R.id.setting_terms_of_information).setOnClickListener(this)
    }

    @SuppressLint("CheckResult")
    override fun onClick(view: View?) {
        if(view == null) return

        when(view.id) {
            R.id.backButton -> {
                finish()
            }
            R.id.setting_terms_of_information -> {
                val intent = Intent(
                    this,
                    TermsActivity::class.java
                )
                intent.putExtra("type", "information")
                startActivity(intent)
            }
            R.id.setting_terms_of_use -> {
                val intent = Intent(
                    this,
                    TermsActivity::class.java
                )
                intent.putExtra("type", "use")
                startActivity(intent)
            }
            R.id.setting_notification_setting -> {
                val entries = NotificationType.entries
                val items = entries.map { getString(it.string) }.toList()
                val disabledIndices = entries
                    .mapIndexedNotNull { index, notificationType -> if(!notificationType.enable) index else null }
                    .toIntArray()

                val currentNotification = setting.getNotifications()
                Log.e("Current Notification", currentNotification.toString())

                val initialSelection = entries
                    .filter { notificationType -> (currentNotification and notificationType.code) == notificationType.code }
                    .map { notificationType -> entries.indexOf(notificationType) }
                    .toIntArray()

                Log.e("Initial Notification", initialSelection.joinToString())

                MaterialDialog(this).show {
                    noAutoDismiss().listItemsMultiChoice(
                        items = items,
                        disabledIndices = disabledIndices,
                        initialSelection = initialSelection
                    ) { _, indices, _ ->
                        Log.e("ALL", indices.joinToString())
                        for(i in items.indices) {
                            Log.e("I", i.toString() + " - " + (i in indices).toString())
                            setting.setNotification(entries[i], i in indices)
                        }
                    }

                    title(R.string.settings_notification_head)
                }
            }
            R.id.setting_edit_profile, R.id.setting_edit_profile_top -> {
                val intent = Intent(
                    this,
                    EditProfileActivity::class.java
                )
                startActivity(intent)
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
                        positiveButton(R.string.settings_ask_app_restart_yes) { _ ->
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
                        positiveButton(R.string.settings_ask_app_restart_yes) { _ ->
                            setting.setLocale(this@SettingsActivity, SettingManager.SupportedLanguage[index].language)
                        }
                        negativeButton(R.string.settings_ask_app_restart_no) { dialog ->
                            dialog.dismiss()
                        }
                    }
                }.title(R.string.settings_language).show()
            }
            R.id.setting_logout -> {
                MaterialDialog(this).show {
                    title(R.string.settings_logout_head)
                    message(R.string.settings_logout_body)
                    positiveButton(R.string.settings_logout_yes) { _ ->
                        val token = setting.getFcmToken()

                        if(token != null) {
                            NetworkManager.apiService.deleteFcmToken(
                                DeleteFcmTokenRequest(
                                    token
                                )
                            ).enqueue(object : Callback<FcmTokenResponse> {
                                override fun onResponse(
                                    p0: Call<FcmTokenResponse>,
                                    p1: Response<FcmTokenResponse>
                                ) {
                                }

                                override fun onFailure(p0: Call<FcmTokenResponse>, p1: Throwable) {
                                }
                            }
                            )
                        }

                        setting.removeFcmToken()
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
                    negativeButton(R.string.settings_logout_no) { dialog ->
                        dialog.dismiss()
                    }
                }
            }
            R.id.setting_unregister -> {
                val warningText = getString(R.string.settings_unregister_warning)
                val originString = buildString {
                    append(warningText)
                    append("\n\n")
                    append(getString(R.string.settings_unregister_body))
                }

                val spannableString = SpannableString(originString)
                var end = warningText.length

                spannableString.setSpan(
                    ForegroundColorSpan(Color.RED),
                    0,
                    end,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )

                spannableString.setSpan(
                    RelativeSizeSpan(0.8f),
                    0,
                    end,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )

                val repeatWord = getString(R.string.settings_unregister_repeat_word)

                val start = originString.lastIndexOf(repeatWord)
                end = start + repeatWord.length

                if (start >= 0) {
                    spannableString.setSpan(
                        ForegroundColorSpan(Color.RED),
                        start,
                        end,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }

                MaterialDialog(this).show {
                    title(R.string.settings_unregister_head)
                    message(text = spannableString)
                    input()
                    positiveButton(R.string.settings_unregister_yes) { _ ->
                        this@SettingsActivity.showDarkOverlay()
                        NetworkManager.apiService.unregister().enqueue(object : Callback<UnregisterResponse> {
                            override fun onResponse(
                                call: Call<UnregisterResponse>,
                                response: Response<UnregisterResponse>
                            ) {
                                this@SettingsActivity.hideDarkOverlay()
                                if(!response.isSuccessful) {
                                    Snackbar.make(findViewById(R.id.main), "계정 삭제에 실패했습니다.", Snackbar.LENGTH_SHORT).show()
                                    return
                                }

                                Toast.makeText(this@SettingsActivity, "계정이 삭제되었습니다.", Toast.LENGTH_SHORT).show()
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

                            override fun onFailure(call: Call<UnregisterResponse>, err: Throwable) {
                            }
                        })
                    }
                    negativeButton(R.string.settings_unregister_no) { dialog ->
                        dialog.dismiss()
                    }

                    getInputField().addTextChangedListener(object : TextWatcher {
                        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                        }

                        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                            Log.e("MaterialDialogInput", "Current text: $s")
                            setActionButtonEnabled(WhichButton.POSITIVE, s.toString() == repeatWord)
                        }

                        override fun afterTextChanged(s: Editable?) {
                        }
                    })
                }
            }
        }
    }

    override fun onChangedProfile() {
        user = FirebaseAuth.getInstance().currentUser!!
        findViewById<TextView>(R.id.nickname).text = user.displayName

        if(user.photoUrl != null) {
            Glide.with(this)
                .load(user.photoUrl!!.toString())
                .into(findViewById(R.id.profile_image))
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        ProfileManager.getInstance().removeProfileListener(this)
    }

}