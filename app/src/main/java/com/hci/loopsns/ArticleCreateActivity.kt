package com.hci.loopsns

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.afollestad.materialdialogs.MaterialDialog
import com.airbnb.lottie.LottieAnimationView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.snackbar.Snackbar
import com.hci.loopsns.network.AddressResponse
import com.hci.loopsns.network.AddressResult
import com.hci.loopsns.network.ArticleCreateResponse
import com.hci.loopsns.network.CategoryResponse
import com.hci.loopsns.network.NetworkManager
import com.hci.loopsns.utils.GlideEngine
import com.hci.loopsns.utils.dp
import com.hci.loopsns.utils.factory.MyArticleFactory
import com.hci.loopsns.utils.fadeIn
import com.hci.loopsns.utils.fadeOut
import com.hci.loopsns.view.bottomsheet.SelectCategoryBottomSheet
import com.luck.picture.lib.PictureSelectorFragment
import com.luck.picture.lib.basic.PictureSelectionModel
import com.luck.picture.lib.basic.PictureSelector
import com.luck.picture.lib.config.SelectMimeType
import com.luck.picture.lib.engine.CompressFileEngine
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.entity.LocalMediaFolder
import com.luck.picture.lib.interfaces.OnCallbackListener
import com.luck.picture.lib.interfaces.OnPermissionDeniedListener
import com.luck.picture.lib.interfaces.OnPermissionDescriptionListener
import com.luck.picture.lib.interfaces.OnPermissionsInterceptListener
import com.luck.picture.lib.interfaces.OnQueryDataSourceListener
import com.luck.picture.lib.interfaces.OnRequestPermissionListener
import com.luck.picture.lib.interfaces.OnResultCallbackListener
import com.luck.picture.lib.permissions.PermissionChecker
import com.luck.picture.lib.permissions.PermissionResultCallback
import com.yalantis.ucrop.UCrop
import com.yariksoffice.lingver.Lingver
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import top.zibin.luban.Luban
import top.zibin.luban.OnNewCompressListener
import java.io.File


class ArticleCreateActivity : AppCompatActivity(), View.OnClickListener {

    private var picture: String = "" //지금은 한 장만 처리

    private var x: Double = 0.0
    private var y: Double = 0.0

    private lateinit var locationEditLauncher: ActivityResultLauncher<Intent>

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_create_article)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        findViewById<ImageButton>(R.id.backButton).setOnClickListener(this)
        findViewById<ConstraintLayout>(R.id.edit_location_layout).setOnClickListener(this)
        findViewById<ConstraintLayout>(R.id.add_photo_layout).setOnClickListener(this)
        findViewById<Button>(R.id.submit).setOnClickListener(this)
        findViewById<ImageButton>(R.id.attachment_delete).setOnClickListener(this)

        locationEditLauncher = registerForActivityResult(
                ActivityResultContracts.StartActivityForResult()
                ) { result ->
            if (result.resultCode == RESULT_OK) {
                val data: Intent = result.data ?: return@registerForActivityResult

                this@ArticleCreateActivity.x = data.getDoubleExtra("x", 0.0)
                this@ArticleCreateActivity.y = data.getDoubleExtra("y", 0.0)
                findViewById<TextView>(R.id.locationEditText).text = data.getStringExtra("location")!!
            }
        }
        parseIntentData()
    }

    fun parseIntentData() {
        x = intent.getDoubleExtra("x", 0.0)
        y = intent.getDoubleExtra("y", 0.0)
        getLocation()
    }

    private fun hideKeyboard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
    }

    fun createArticle(categories: List<String>, keywords: List<String>) {
        val animation = findViewById<LottieAnimationView>(R.id.loadingAnim)
        val inputText = findViewById<EditText>(R.id.contentText)

        animation.fadeIn(0.85F, 200)

        var image: MultipartBody.Part? = null
        val description = inputText.text.toString().toRequestBody("text/plain".toMediaTypeOrNull())

        if(picture.isNotEmpty()) {
            val file = File(picture)

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

                MyArticleFactory.addCreatedArticle(result.article)

                val intent = Intent(
                    this@ArticleCreateActivity,
                    ArticleDetailActivity::class.java
                )
                intent.putExtra("article", result.article)
                startActivity(intent)
                finish()
            }

            override fun onFailure(call: Call<ArticleCreateResponse>, err: Throwable) {
                Snackbar.make(findViewById(R.id.main), "게시글 작성 요청 중 오류가 발생했습니다. $err", Snackbar.LENGTH_SHORT).show()
            }
        })
    }

    fun getLocation() {
        NetworkManager.apiService.getAddress(
            "$x,$y",
            Lingver.getInstance().getLocale().language
        ).enqueue(object:Callback<AddressResponse> {
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
            val splited = addressText.split(country)
            if(splited[1].isBlank()) { //이게 비어있으면 영문 주소일 가능성이 있음
                val lastCommaIndex = addressText.lastIndexOf(',') //따라서 영문 기준으로 잡고 마지막 국가를 지워준다.
                addressText = addressText.substring(0, lastCommaIndex)
            } else {
                addressText = splited[1]
            }
        }

        runOnUiThread {
            findViewById<TextView>(R.id.locationEditText).text = addressText.trim()
        }
    }

    override fun onClick(view: View?) {
        if(view == null) return

        when(view.id) {
            R.id.attachment_delete -> {
                picture = ""
                findViewById<ImageView>(R.id.attachment_picture).setImageDrawable(null)
                findViewById<ImageButton>(R.id.attachment_delete).visibility = View.GONE
            }
            R.id.backButton -> {
                finish()
            }
            R.id.edit_location_layout -> {
                MaterialDialog(this).show {
                    title(R.string.location_edit_ask_warning_head)
                    message(R.string.location_edit_ask_warning_body)
                    positiveButton(R.string.location_edit_ask_warning_yes) { _ ->
                        val intent = Intent(
                            this@ArticleCreateActivity,
                            SelectLocationActivity::class.java
                        )
                        intent.putExtra("x", this@ArticleCreateActivity.x)
                        intent.putExtra("y", this@ArticleCreateActivity.y)
                        locationEditLauncher.launch(intent)
                    }
                    negativeButton(R.string.location_edit_ask_warning_no) { dialog ->
                        dialog.dismiss()
                    }
                }
            }
            R.id.add_photo_layout -> {
//                PictureSelector.create(this)
//                    .openSystemGallery(SelectMimeType.ofImage())
//                    .setCropEngine { fragment, srcUri, destinationUri, dataSource, requestCode ->
//                        UCrop.of(srcUri, destinationUri, dataSource).withAspectRatio(1F, 1F).start(fragment.requireContext(), fragment, requestCode)
//                    }
//                    .setCompressEngine(CompressFileEngine { context, source, call ->
//                        Luban.with(context).load(source).ignoreBy(100)
//                            .setCompressListener(object  : OnNewCompressListener {
//                                override fun onStart() {
//                                    Log.e("Compress Started", "Start!")
//                                }
//
//                                override fun onSuccess(source: String?, compressFile: File?) {
//                                    if(source == null || compressFile == null) {
//                                        Snackbar.make(findViewById(R.id.main), "이미지 압축 엔진에 이상이 있습니다. 파일이 없습니다.", Snackbar.LENGTH_SHORT).show()
//                                        Log.e("Compress Failed", "File Null")
//                                        return
//                                    }
//                                    Log.e("Compress Success", "Good " + source + " - " + compressFile.absolutePath)
//                                    call.onCallback(source, compressFile.absolutePath)
//                                }
//
//                                override fun onError(source: String?, e: Throwable?) {
//                                    if(e == null) {
//                                        return
//                                    }
//                                    Snackbar.make(findViewById(R.id.main), "이미지 압축 엔진에 이상이 있습니다.", Snackbar.LENGTH_SHORT).show()
//                                    Log.e("Compress Failed", e.toString())
//                                }
//                            }).launch()
//                    })
//
//                    .forSystemResultActivity(object : OnResultCallbackListener<LocalMedia?> {
//                        override fun onResult(result: ArrayList<LocalMedia?>) {
//                            if(result.size > 1) {
//                                Snackbar.make(findViewById(R.id.main), "사진은 한 장을 넘길 수 없습니다.", Snackbar.LENGTH_SHORT).show()
//                                return
//                            }
//                            result.forEach {
//                                if(it != null) {
//                                    picture = it.availablePath
//                                    Glide.with(this@ArticleCreateActivity)
//                                        .load(it.availablePath)
//                                        .thumbnail(Glide.with(this@ArticleCreateActivity).load(R.drawable.picture_placeholder))
//                                        .override(250.dp)
//                                        .apply(RequestOptions.bitmapTransform(RoundedCorners(30)))
//                                        .into(findViewById(R.id.attachment_picture))
//
//                                    findViewById<ImageButton>(R.id.attachment_delete).visibility = View.VISIBLE
//
//                                    return
//                                }
//                            }
//                        }
//
//                        override fun onCancel() {
//                        }
//                    })

                PictureSelector.create(this)
                    .openGallery(SelectMimeType.ofImage())
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
                                    picture = it.availablePath
                                    Glide.with(this@ArticleCreateActivity)
                                        .load(it.availablePath)
                                        .thumbnail(Glide.with(this@ArticleCreateActivity).load(R.drawable.picture_placeholder))
                                        .override(250.dp)
                                        .apply(RequestOptions.bitmapTransform(RoundedCorners(30)))
                                        .into(findViewById(R.id.attachment_picture))

                                    findViewById<ImageButton>(R.id.attachment_delete).visibility = View.VISIBLE

                                    return
                                }
                            }
                        }

                        override fun onCancel() {
                        }
                    })
            }
            R.id.submit -> {
                val animation = findViewById<LottieAnimationView>(R.id.loadingAnim)
                val inputText = findViewById<EditText>(R.id.contentText)

                if(inputText.text.length <= 15) {
                    Snackbar.make(findViewById(R.id.main), "내용이 충분하지 않습니다.", Snackbar.LENGTH_SHORT).show()
                    return
                }

                animation.fadeIn(0.85F, 200)

                hideKeyboard()

                var image: MultipartBody.Part? = null
                val description = inputText.text.toString().toRequestBody("text/plain".toMediaTypeOrNull())

                if(picture.isNotEmpty()) {
                    val file = File(picture)

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
        }
    }
}