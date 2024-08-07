package com.hci.loopsns

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.afollestad.materialdialogs.MaterialDialog
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GetTokenResult
import com.google.firebase.auth.UserProfileChangeRequest
import com.hci.loopsns.event.ProfileManager
import com.hci.loopsns.network.AddressResponse
import com.hci.loopsns.network.AddressResult
import com.hci.loopsns.network.NetworkManager
import com.hci.loopsns.network.TermsOfAnyResponse
import com.hci.loopsns.network.UpdateProfileImageResponse
import com.hci.loopsns.network.UpdateProfileResponse
import com.hci.loopsns.storage.NightMode
import com.hci.loopsns.storage.SettingManager
import com.hci.loopsns.utils.GlideEngine
import com.hci.loopsns.utils.dp
import com.luck.picture.lib.basic.PictureSelector
import com.luck.picture.lib.config.SelectMimeType
import com.luck.picture.lib.engine.CompressFileEngine
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.interfaces.OnResultCallbackListener
import com.skydoves.androidveil.VeilLayout
import com.yalantis.ucrop.UCrop
import com.yariksoffice.lingver.Lingver
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import top.zibin.luban.Luban
import top.zibin.luban.OnNewCompressListener
import java.io.File


class TermsActivity : AppCompatActivity(), View.OnClickListener {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        animationInit()
        setContentView(R.layout.activity_terms)

        findViewById<ImageButton>(R.id.backButton).setOnClickListener(this)
        findViewById<VeilLayout>(R.id.veilLayout).veil()
        requestDocument()
    }

    fun requestDocument() {
        var request: Call<TermsOfAnyResponse>? = null
        when(intent.getStringExtra("type") ?: "null") {
            "use" -> {
                request = NetworkManager.apiService.getTermsOfUse(Lingver.getInstance().getLanguage())
            }
            "information" -> {
                request = NetworkManager.apiService.getTermsOfInformation(Lingver.getInstance().getLanguage())
            }
            else -> {
                Toast.makeText(this, "유효하지 않은 요청입니다.", Toast.LENGTH_SHORT).show()
                finish()
            }
        }

        request!!.enqueue(object : Callback<TermsOfAnyResponse> {
            override fun onResponse(
                call: Call<TermsOfAnyResponse>,
                response: Response<TermsOfAnyResponse>
            ) {
                if(!response.isSuccessful) {
                    Toast.makeText(this@TermsActivity, "약관을 받아오는 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
                    finish()
                    return
                }

                findViewById<VeilLayout>(R.id.veilLayout).unVeil()
                findViewById<TextView>(R.id.body).text = Html.fromHtml(response.body()!!.data, Html.FROM_HTML_MODE_COMPACT)
            }

            override fun onFailure(p0: Call<TermsOfAnyResponse>, p1: Throwable) {
                Toast.makeText(this@TermsActivity, "약관을 받아오는 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
                finish()
            }
        })
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

    override fun onClick(view: View?) {
        if(view == null) return

        when(view.id) {
            R.id.backButton -> {
                finish()
            }
        }
    }
}