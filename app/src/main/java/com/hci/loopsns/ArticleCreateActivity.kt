package com.hci.loopsns

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.airbnb.lottie.LottieAnimationView
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import com.hci.loopsns.view.bottomsheet.SelectCategoryBottomSheet
import com.hci.loopsns.network.ArticleCreateResponse
import com.hci.loopsns.network.CategoryResponse
import com.hci.loopsns.network.Comment
import com.hci.loopsns.network.NetworkManager
import com.hci.loopsns.network.geocode.AddressResponse
import com.hci.loopsns.network.geocode.AddressResult
import com.hci.loopsns.network.geocode.ReverseGeocodingManager
import com.hci.loopsns.utils.AuthAppCompatActivity
import com.hci.loopsns.utils.GlideEngine
import com.hci.loopsns.utils.fadeIn
import com.hci.loopsns.utils.fadeOut
import com.luck.picture.lib.basic.PictureSelector
import com.luck.picture.lib.config.SelectMimeType
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.interfaces.OnResultCallbackListener
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.util.Locale


class ArticleCreateActivity : AuthAppCompatActivity() {

    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>> //이미지 요청 목적
    private var permissionCallback: ((Map<String, Boolean>) -> Unit)? = null

    private var picture: String = "" //지금은 한 장만 처리

    private var x: Double = 0.0
    private var y: Double = 0.0

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_article)

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
            if(inputText.text.length <= 15) {
                Snackbar.make(findViewById(R.id.main), "내용이 충분하지 않습니다.", Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            animation.fadeIn(0.85F, 200)

            hideKeyboard()

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

        x = intent.getDoubleExtra("x", 0.0)
        y = intent.getDoubleExtra("y", 0.0)
        getLocation()
    }

    private fun hideKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
    }

    fun createArticle(categories: List<String>, keywords: List<String>) {
        val animation = findViewById<LottieAnimationView>(R.id.loadingAnim)
        val inputText = findViewById<EditText>(R.id.contentText)

        animation.fadeIn(0.85F, 200)

        var image: MultipartBody.Part? = null
        val description = inputText.text.toString().toRequestBody("text/plain".toMediaTypeOrNull())

        if(picture.isNotEmpty()) {
            val file = File(getRealPathFromUri(Uri.parse(picture), this@ArticleCreateActivity)!!)

            val requestFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
            image = MultipartBody.Part.createFormData("images", file.name, requestFile)
        }

        NetworkManager.apiService.createArticle(
            listOf(image),
            listOf(
                categories[0].toRequestBody("text/plain".toMediaTypeOrNull()),
                categories[1].toRequestBody("text/plain".toMediaTypeOrNull())
            ),
            listOf(
                keywords[0].toRequestBody("text/plain".toMediaTypeOrNull()),
                keywords[1].toRequestBody("text/plain".toMediaTypeOrNull()),
                keywords[2].toRequestBody("text/plain".toMediaTypeOrNull()),
                keywords[3].toRequestBody("text/plain".toMediaTypeOrNull())
            ),
            description,
            x.toString().toRequestBody("text/plain".toMediaTypeOrNull()),
            y.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        ).enqueue(object : Callback<ArticleCreateResponse> {
            override fun onResponse(call: Call<ArticleCreateResponse>, response: Response<ArticleCreateResponse>) {
                animation.fadeOut(200)
                if(!response.isSuccessful) {
                    Snackbar.make(findViewById(R.id.main), "게시글 작성에 실패했습니다. 오류 코드 " + response.code(), Snackbar.LENGTH_SHORT).show()
                    return
                }

                val result = response.body()!!

                val intent = Intent(
                    this@ArticleCreateActivity,
                    ArticleDetailActivity::class.java
                )
                intent.putExtra("article", result.article)
                intent.putParcelableArrayListExtra("comments", ArrayList<Comment>())
                startActivity(intent)
            }

            override fun onFailure(call: Call<ArticleCreateResponse>, err: Throwable) {

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

    fun getLocation() {
        val ai: ApplicationInfo = packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA)

        ReverseGeocodingManager.apiService.getAddress(
            "$x,$y",
            ai.metaData.getString("com.google.android.geo.API_KEY")!!,
            Locale.getDefault().language).enqueue(object:Callback<AddressResponse> {
            override fun onResponse(call: Call<AddressResponse>, response: Response<AddressResponse>) {
                if(!response.isSuccessful) return

                val body = response.body()!!

                if(body.results.isEmpty()) {
                    return
                }

                body.results.forEach {
                    if(it.types.contains("sublocality_level_4")) {
                        onGeocode(it)
                        return
                    }
                }
                body.results.forEach {
                    if(it.types.contains("sublocality_level_3")) {
                        onGeocode(it)
                        return
                    }
                }
                body.results.forEach {
                    if(it.types.contains("sublocality_level_2")) {
                        onGeocode(it)
                        return
                    }
                }
                body.results.forEach {
                    if(it.types.contains("sublocality_level_1")) {
                        onGeocode(it)
                        return
                    }
                }
                onGeocode(body.results[0])
            }

            override fun onFailure(call: Call<AddressResponse>, err: Throwable) {
                Log.e("ArticleCreate Geocoding Failed", err.toString())
            }

        })
    }

    fun onGeocode(address: AddressResult) {
        var addressText = ""

        var country = ""
        address.addressComponents.forEach {
            if (it.types.contains("country")) {
                country = it.longName
                return@forEach
            }
        }
        addressText = address.formattedAddress

        if(country.isNotBlank() && addressText.contains(country)) {
            addressText = addressText.split(country)[1].trim()
        }

        runOnUiThread {
            findViewById<TextView>(R.id.locationEditText).text = addressText
        }
    }
}