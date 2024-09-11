package com.hci.loopsns

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
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
import com.afollestad.materialdialogs.actions.getActionButton
import com.afollestad.materialdialogs.actions.setActionButtonEnabled
import com.afollestad.materialdialogs.callbacks.onShow
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
import com.hci.loopsns.network.RegisterLicenseResponse
import com.hci.loopsns.network.UnregisterResponse
import com.hci.loopsns.storage.NotificationType
import com.hci.loopsns.storage.SettingManager
import com.hci.loopsns.utils.hideDarkOverlay
import com.hci.loopsns.utils.showDarkOverlay
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Locale
import java.util.regex.Pattern
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

        findViewById<ConstraintLayout>(R.id.setting_license).setOnClickListener(this)
        findViewById<ConstraintLayout>(R.id.setting_logout).setOnClickListener(this)
        findViewById<ConstraintLayout>(R.id.setting_unregister).setOnClickListener(this)
        findViewById<ConstraintLayout>(R.id.setting_dark_mode).setOnClickListener(this)
        findViewById<ConstraintLayout>(R.id.setting_language).setOnClickListener(this)
        findViewById<ConstraintLayout>(R.id.setting_notification_setting).setOnClickListener(this)
        findViewById<ConstraintLayout>(R.id.setting_edit_profile_top).setOnClickListener(this)
        findViewById<ConstraintLayout>(R.id.setting_edit_profile).setOnClickListener(this)
        findViewById<ConstraintLayout>(R.id.setting_terms_of_use).setOnClickListener(this)
        findViewById<ConstraintLayout>(R.id.setting_terms_of_information).setOnClickListener(this)
        findViewById<ConstraintLayout>(R.id.setting_faq).setOnClickListener(this)
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
            R.id.setting_faq -> {
                val intent = Intent(
                    this,
                    TermsActivity::class.java
                )
                intent.putExtra("type", "faq")
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
                            if(index == list.size - 1) {
                                setting.setLocaleAuto(this@SettingsActivity) //언어 설정
                                return@positiveButton
                            }

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
            R.id.setting_license -> {
                val currentLocale = Locale.getDefault()
                if(currentLocale != Locale.KOREA && currentLocale != Locale.KOREAN) {
                    Snackbar.make(findViewById(R.id.main), "This feature is not available in countries other than South Korea.", Snackbar.LENGTH_SHORT).show()
                    return
                }

                MaterialDialog(this).show {
                    title(text = "관리자 라이센스 입력")
                    message(text = "관리자 라이센스 키를 입력해주세요.\r\n등록이 완료된 후 앱이 재시작됩니다.\r\n(이미 등록된 키가 있는 경우 새로운 키로 대체됩니다.)")
                    input(hint = "XXXX-XXXX-XXXX-XXXX", maxLength = 19)
                    onShow {
                        setActionButtonEnabled(WhichButton.POSITIVE, false)
                    }

                    positiveButton(text = "등록") { _ ->
                        this@SettingsActivity.showDarkOverlay()
                        NetworkManager.apiService.registerLicense(getInputField().text.toString()).enqueue(object  : Callback<RegisterLicenseResponse> {
                            override fun onResponse(
                                call: Call<RegisterLicenseResponse>,
                                response: Response<RegisterLicenseResponse>
                            ) {
                                this@SettingsActivity.hideDarkOverlay()
                                if(!response.isSuccessful) {
                                    Toast.makeText(this@SettingsActivity, "올바르지 않은 라이센스 키 입니다.", Toast.LENGTH_LONG).show()
                                    return
                                }

                                Toast.makeText(this@SettingsActivity, "관리자 등록이 완료되었습니다.", Toast.LENGTH_LONG).show()

                                val refresh = Intent(context, MainActivity::class.java)
                                refresh.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                                startActivity(refresh)
                            }

                            override fun onFailure(
                                call: Call<RegisterLicenseResponse>,
                                err: Throwable
                            ) {
                                Toast.makeText(this@SettingsActivity, "라이센스 키 등록 중 오류가 발생했습니다.", Toast.LENGTH_LONG).show()
                                Log.e("라이센스 등록 오류", err.toString())
                                return
                            }

                        })
                    }
                    negativeButton(text = "취소")
                    getInputField().apply {
                        var isEditing: Boolean = false

                        filters = arrayOf(InputFilter { source, _, _, _, _, _ ->
                            val allowedChars = "[a-zA-Z0-9-]+"
                            if ((source.isNullOrBlank() || source.toString().matches(Regex(allowedChars))) || isEditing) {
                                if(source.isNotEmpty() && source[0] == '-' && source.length == 1) {
                                    ""
                                } else {
                                    source.toString().uppercase() // 입력을 대문자로 변환
                                }
                            } else {
                                "" // 허용되지 않은 문자일 경우 빈 문자열 반환
                            }
                        })

                        addTextChangedListener(object : TextWatcher {

                            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                            }

                            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                            }

                            override fun afterTextChanged(s: Editable?) {
                                if (isEditing) return

                                isEditing = true

                                clearComposingText()
                                // 입력된 텍스트에서 기존 하이픈 제거
                                val original = s.toString().replace("-", "")

                                // 4자리마다 하이픈을 추가
                                val formatted = StringBuilder()
                                for (i in original.indices) {
                                    formatted.append(original[i])
                                    if ((i + 1) % 4 == 0 && i != original.lastIndex) {
                                        formatted.append("-")
                                    }
                                }

                                // 새롭게 포맷팅한 텍스트로 설정
                                s?.replace(0, s.length, formatted.toString())

                                setActionButtonEnabled(WhichButton.POSITIVE, original.length == 16)

                                isEditing = false
                            }
                        })
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
                    onShow {
                        setActionButtonEnabled(WhichButton.POSITIVE, false)
                    }

                    positiveButton(R.string.settings_unregister_yes) { _ ->
                        this@SettingsActivity.showDarkOverlay()
                        NetworkManager.apiService.unregister().enqueue(object : Callback<UnregisterResponse> {
                            override fun onResponse(
                                call: Call<UnregisterResponse>,
                                response: Response<UnregisterResponse>
                            ) {
                                this@SettingsActivity.hideDarkOverlay()
                                if(!response.isSuccessful) {
                                    Snackbar.make(findViewById(R.id.main), getString(R.string.fail_account_deletion), Snackbar.LENGTH_SHORT).show()
                                    return
                                }

                                Toast.makeText(this@SettingsActivity, getString(R.string.account_delete), Toast.LENGTH_SHORT).show()
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

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(SettingManager.getInstance().getCurrentLocaleContext(base))
    }
}