package com.hci.loopsns

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.airbnb.lottie.LottieAnimationView
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.hci.loopsns.network.CategoryResponse
import com.hci.loopsns.network.KakaoResponse
import com.hci.loopsns.network.NetworkManager
import com.hci.loopsns.utils.GlideEngine
import com.hci.loopsns.utils.fadeIn
import com.hci.loopsns.utils.fadeOut
import com.luck.picture.lib.basic.PictureSelector
import com.luck.picture.lib.config.SelectMimeType
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.interfaces.OnResultCallbackListener
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.IOException


class ArticleCreateActivity : AppCompatActivity() {

    lateinit var permissionLauncher: ActivityResultLauncher<Array<String>> //이미지 요청 목적
    private var permissionCallback: ((Map<String, Boolean>) -> Unit)? = null

    private var picture: String = "" //지금은 한 장만 처리

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_writing_wpicture_category)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        permissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()) {

            permissionCallback?.invoke(it)
            permissionCallback = null
        }

        findViewById<ImageButton>(R.id.backButton).setOnClickListener {
            finish()
        }

        findViewById<Button>(R.id.add_picture_btn).setOnClickListener {
            PictureSelector.create(this)
                .openGallery(SelectMimeType.ofImage())
                .setImageEngine(GlideEngine.createGlideEngine())
                .forResult(object : OnResultCallbackListener<LocalMedia?> {
                    override fun onResult(result: ArrayList<LocalMedia?>) {
                        if(result.size > 1) {
                            Snackbar.make(findViewById(R.id.main), "사진은 한 장을 넘길 수 없습니다.", Snackbar.LENGTH_SHORT).show()
                            return
                        }
                        result.forEach {
                            if(it != null) {
                                picture = it.availablePath
                                Glide.with(this@ArticleCreateActivity)
                                    .load(it.availablePath)
                                    .into(findViewById(R.id.attachment_picture))


                                return
                            }
                        }
                    }

                    override fun onCancel() {
                    }
                })
        }

        val animation = findViewById<LottieAnimationView>(R.id.loadingAnim)
        val inputText = findViewById<EditText>(R.id.contentText)
        findViewById<Button>(R.id.submit).setOnClickListener {
            animation.fadeIn(0.85F, 200)

            var image: MultipartBody.Part? = null
            val description = inputText.text.toString().toRequestBody("text/plain".toMediaTypeOrNull())

            if(picture.isNotEmpty()) {
                val file = File(getRealPathFromUri(Uri.parse(picture), this@ArticleCreateActivity)!!)

                val requestFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
                image = MultipartBody.Part.createFormData("images", file.name, requestFile)
            }


            NetworkManager.apiService.retrieveCategory(listOf(image), description).enqueue(object :
                Callback<CategoryResponse> {
                override fun onResponse(call: Call<CategoryResponse>, response: Response<CategoryResponse>) {
                    animation.fadeOut(200)

                    if(call.isCanceled) return
                    if(!response.isSuccessful) {
                        Log.e("RetrieveCategory", "Failed With HTTP Code" + response.code())
                        return
                    }
                    val result = response.body()!!
                    if(result.categories == null || result.categories.size != 2) {
                        Snackbar.make(findViewById(R.id.main), "서버 내부 오류로 카테고리 분석이 불가능합니다.", Snackbar.LENGTH_SHORT).show()
                        return
                    }

                    SelectCategoryBottomSheet()
                        .setData(result.categories, result.keywords!!, ::createArticle)
                        .show(supportFragmentManager, "SelectCategoryBottomSheetTag")
                }

                override fun onFailure(call: Call<CategoryResponse>, err: Throwable) {
                    Log.e("RetrieveCategory", "Failed $err")
                }
            })
        }

        val x = intent.getDoubleExtra("x", 0.0)
        val y = intent.getDoubleExtra("y", 0.0)
        getLocation(x, y)
    }

    fun createArticle(categories: List<String>, keywords: List<String>) {
        
    }

    fun getLocation(x: Double, y: Double) {
        val baseUrl = "https://dapi.kakao.com/v2/local/geo/coord2address"
        val url = baseUrl.toHttpUrlOrNull()?.newBuilder()?.apply {
            addQueryParameter("x", x.toString())
            addQueryParameter("y", y.toString())
        }?.build()

        // 요청 생성
        val request = Request.Builder()
            .url(url!!)
            .header("Authorization", "KakaoAK ecac7e74ea9e428b92e9edaa3786e729")
            .build()

        // 요청 실행
        val client = OkHttpClient()

        client.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                Log.e("Kakao Local Error", e.toString())
            }

            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()?.trimIndent() ?: return

                    val gson = Gson()
                    val kakaoResponse = gson.fromJson(responseBody, KakaoResponse::class.java)

                    runOnUiThread {
                        findViewById<TextView>(R.id.locationEditText).text = kakaoResponse.documents[0].address!!.addressName
                    }
                } else {
                    Log.e("Kakao Local Error", "Request failed: ${response.code}")
                }
            }
        })
    }

    fun getRealPathFromUri(contentUri: Uri, context: Context): String? {
        var cursor: Cursor? = null
        try {
            val proj = arrayOf(MediaStore.Video.Media.DATA)
            cursor = context.contentResolver.query(contentUri, proj, null, null, null)
            val columnIndex = cursor?.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)
            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getString(columnIndex!!)
            }
        } finally {
            cursor?.close()
        }
        return null
    }
}