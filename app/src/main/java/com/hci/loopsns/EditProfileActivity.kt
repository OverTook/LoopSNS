package com.hci.loopsns

import android.app.Activity
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageButton
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GetTokenResult
import com.google.firebase.auth.UserProfileChangeRequest
import com.hci.loopsns.event.ProfileManager
import com.hci.loopsns.network.NetworkManager
import com.hci.loopsns.network.UpdateProfileImageResponse
import com.hci.loopsns.storage.SettingManager
import com.hci.loopsns.utils.GlideEngine
import com.hci.loopsns.utils.dp
import com.hci.loopsns.utils.hideDarkOverlay
import com.hci.loopsns.utils.showDarkOverlay
import com.luck.picture.lib.basic.PictureSelector
import com.luck.picture.lib.config.SelectMimeType
import com.luck.picture.lib.engine.CompressFileEngine
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.interfaces.OnResultCallbackListener
import com.yalantis.ucrop.UCrop
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import top.zibin.luban.Luban
import top.zibin.luban.OnNewCompressListener
import java.io.File


class EditProfileActivity : AppCompatActivity(), View.OnClickListener,
    OnCompleteListener<GetTokenResult> {

        private lateinit var user: FirebaseUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        animationInit()
        setContentView(R.layout.activity_edit_profile)

        user = FirebaseAuth.getInstance().currentUser!!

        if(user.photoUrl != null) {
            Glide.with(this@EditProfileActivity)
                .load(user.photoUrl!!.toString())
                .thumbnail(Glide.with(this@EditProfileActivity).load(R.drawable.picture_placeholder))
                .override(100.dp)
                .into(findViewById(R.id.profile_image))
        }

        findViewById<AppCompatImageButton>(R.id.backButton).setOnClickListener(this)

        findViewById<TextView>(R.id.profile_name_edit_text).text = user.displayName
        findViewById<TextView>(R.id.profile_email_edit_text).text = user.email

        findViewById<Button>(R.id.cancel_button).setOnClickListener(this)
        findViewById<Button>(R.id.save_button).setOnClickListener(this)
        findViewById<ImageButton>(R.id.profile_change_profile_image).setOnClickListener(this)
        findViewById<ImageView>(R.id.profile_image).setOnClickListener(this)

        user.getIdToken(true).addOnCompleteListener(this)
    }

    override fun onComplete(task: Task<GetTokenResult>) {
        if (task.isSuccessful) {
            val claims = task.result?.claims

            val platform = claims?.get("platform") as? String ?: ""

            if(platform.isNotBlank()) {
                findViewById<EditText>(R.id.profile_email_edit_text).isEnabled = false
            }
        } else {
            Snackbar.make(findViewById(R.id.main), getString(R.string.fail_get_token), Snackbar.LENGTH_SHORT).show()
        }
    }

    fun animationInit() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            overrideActivityTransition(Activity.OVERRIDE_TRANSITION_OPEN, R.anim.enter_from_right, R.anim.exit_to_left)
        } else {
            overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left)
        }
    }

    override fun finish() {
        super.finish()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            overrideActivityTransition(Activity.OVERRIDE_TRANSITION_CLOSE, R.anim.enter_from_left, R.anim.exit_to_right)
        } else {
            overridePendingTransition(R.anim.enter_from_left, R.anim.exit_to_right)
        }
    }

    fun uploadProfileImage(filePath: String) {
        val file = File(filePath)

        val requestFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
        val image = MultipartBody.Part.createFormData("image", file.name, requestFile)

        NetworkManager.apiService.updateProfileImage(image).enqueue(object : Callback<UpdateProfileImageResponse> {
            override fun onResponse(
                call: Call<UpdateProfileImageResponse>,
                response: Response<UpdateProfileImageResponse>
            ) {
                if(!response.isSuccessful) {
                    Snackbar.make(findViewById(R.id.main), "프로필 사진 변경에 실패했습니다.", Snackbar.LENGTH_SHORT).show()

                    if(user.photoUrl != null) {
                        Glide.with(this@EditProfileActivity)
                            .load(user.photoUrl!!.toString())
                            .thumbnail(Glide.with(this@EditProfileActivity).load(R.drawable.picture_placeholder))
                            .override(100.dp)
                            .into(findViewById(R.id.profile_image))

                        return
                    }

                    Glide.with(this@EditProfileActivity)
                        .load(R.drawable.loop_logo)
                        .override(100.dp)
                        .into(findViewById(R.id.profile_image))
                    return
                }

                user.updateProfile(
                    UserProfileChangeRequest.Builder().apply {
                        this.photoUri = Uri.parse(response.body()!!.pictureUrl)
                    }.build()
                ).addOnCompleteListener {
                    ProfileManager.getInstance().onProfileChanged()
                }
            }

            override fun onFailure(p0: Call<UpdateProfileImageResponse>, p1: Throwable) {
                TODO("Not yet implemented")
            }

        })
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(SettingManager.getInstance().getCurrentLocaleContext(base))
    }

    override fun onClick(view: View?) {
        if(view == null) return

        when(view.id) {
            R.id.profile_change_profile_image, R.id.profile_image -> {
                PictureSelector.create(this)
                    .openGallery(SelectMimeType.ofImage())
                    .setMaxSelectNum(1)
                    .setCompressEngine(CompressFileEngine { context, source, call ->
                        Luban.with(context).load(source).ignoreBy(100)
                            .setCompressListener(object  : OnNewCompressListener {
                                override fun onStart() {
                                    Log.e("Compress Started", "Start!")
                                }

                                override fun onSuccess(source: String?, compressFile: File?) {
                                    if(source == null || compressFile == null) {
                                        Snackbar.make(findViewById(R.id.main), "이미지 압축 엔진에 이상이 있습니다. 파일이 없습니다.", Snackbar.LENGTH_SHORT).show()
                                        Log.e("Compress Failed", "File Null")
                                        return
                                    }
                                    Log.e("Compress Success", "Good " + source + " - " + compressFile.absolutePath)
                                    call.onCallback(source, compressFile.absolutePath)
                                }

                                override fun onError(source: String?, e: Throwable?) {
                                    if(e == null) {
                                        return
                                    }
                                    Snackbar.make(findViewById(R.id.main), "이미지 압축 엔진에 이상이 있습니다.", Snackbar.LENGTH_SHORT).show()
                                    Log.e("Compress Failed", e.toString())
                                }
                            }).launch()
                    })
                    .setCropEngine { fragment, srcUri, destinationUri, dataSource, requestCode ->
                        UCrop.of(srcUri, destinationUri, dataSource).withAspectRatio(1F, 1F).start(fragment.requireContext(), fragment, requestCode)
                    }
                    .setImageEngine(GlideEngine.createGlideEngine())
                    .forResult(object : OnResultCallbackListener<LocalMedia?> {
                        override fun onResult(result: ArrayList<LocalMedia?>) {
                            if(result.size > 1) {
                                Snackbar.make(findViewById(R.id.main), "사진은 한 장을 넘길 수 없습니다.", Snackbar.LENGTH_SHORT).show()
                                return
                            }
                            result.forEach {
                                if(it != null) {
                                    uploadProfileImage(it.availablePath)

                                    Glide.with(this@EditProfileActivity)
                                        .load(it.availablePath)
                                        .thumbnail(Glide.with(this@EditProfileActivity).load(R.drawable.picture_placeholder))
                                        .override(100.dp)
                                        .into(findViewById(R.id.profile_image))

                                    return
                                }
                            }
                        }

                        override fun onCancel() {
                        }
                    })

            }
            R.id.backButton, R.id.cancel_button -> {
                finish()
            }
            R.id.save_button -> {
                showDarkOverlay()
                try {
                    user.updateProfile(
                        UserProfileChangeRequest.Builder().apply {
                            displayName = findViewById<TextView>(R.id.profile_name_edit_text).text.toString()
                        }.build()
                    ).addOnCompleteListener {
                        hideDarkOverlay()
                        ProfileManager.getInstance().onProfileChanged()
                        finish()
                    }
                } catch (err: FirebaseAuthInvalidUserException) {
                    hideDarkOverlay()
                    Snackbar.make(findViewById(R.id.main), "저장할 수 없습니다. $err", Snackbar.LENGTH_SHORT).show()
                }
                
                //TODO 이메일
            }
        }
    }
}