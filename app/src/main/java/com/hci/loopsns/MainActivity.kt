package com.hci.loopsns

import android.Manifest
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.IntentCompat
import androidx.core.splashscreen.SplashScreen
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging
import com.hci.loopsns.databinding.ActivityMainBinding
import com.hci.loopsns.network.AddFcmTokenRequest
import com.hci.loopsns.network.FcmTokenResponse
import com.hci.loopsns.network.NetworkManager
import com.hci.loopsns.storage.SettingManager
import com.hci.loopsns.storage.models.NotificationComment
import com.hci.loopsns.utils.DoubleBackPressHandler
import com.hci.loopsns.view.fragment.HomeFragment
import com.hci.loopsns.view.fragment.MainViewPageAdapter
import com.hci.loopsns.view.fragment.NotificationsFragment
import com.hci.loopsns.view.fragment.ProfileFragment
import com.luck.picture.lib.basic.PictureSelector
import com.luck.picture.lib.config.PictureConfig
import com.luck.picture.lib.config.SelectMimeType
import com.luck.picture.lib.permissions.PermissionChecker
import com.luck.picture.lib.permissions.PermissionConfig
import com.luck.picture.lib.permissions.PermissionResultCallback
import com.saadahmedev.popupdialog.PopupDialog
import com.saadahmedev.popupdialog.listener.StandardDialogActionListener
import com.yariksoffice.lingver.Lingver
import github.com.st235.lib_expandablebottombar.OnItemClickListener
import org.litepal.LitePal
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class MainActivity : AppCompatActivity(), SplashScreen.KeepOnScreenCondition, OnItemClickListener {

    private lateinit var doubleBackPressHandler: DoubleBackPressHandler
    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: MainViewPageAdapter

    private lateinit var homeFragment: HomeFragment
    private lateinit var profileFragment: ProfileFragment
    private lateinit var notificationsFragment: NotificationsFragment

    private lateinit var locationPermissionLauncher: ActivityResultLauncher<Array<String>>
    private lateinit var notificationPermissionLauncher: ActivityResultLauncher<String>

    private var mAuth: FirebaseAuth? = null

    private var splashScreenKeep = true
    private var notificationComplete = false

    private lateinit var viewPagerPageCallback: ViewPager2.OnPageChangeCallback

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)

        val splashScreen = installSplashScreen()
        splashScreen.setKeepOnScreenCondition(this)

        homeFragment = HomeFragment()
        profileFragment = ProfileFragment()
        notificationsFragment = NotificationsFragment()

        mAuth = FirebaseAuth.getInstance()

        // ViewPager2 어댑터 설정
        adapter = MainViewPageAdapter(this)
        adapter.addFragment(profileFragment)
        adapter.addFragment(homeFragment)
        adapter.addFragment(notificationsFragment)
        binding.viewPager.adapter = adapter
        binding.viewPager.reduceDragSensitivity()

        binding.bottomNavigationView.onItemSelectedListener = this

        viewPagerPageCallback = object : ViewPager2.OnPageChangeCallback() {
            override fun onPageScrollStateChanged(state: Int) {
                super.onPageScrollStateChanged(state)
                if (state == ViewPager2.SCROLL_STATE_IDLE) {
                    if(!splashScreenKeep && notificationComplete) {
                        binding.viewPager.unregisterOnPageChangeCallback(viewPagerPageCallback)
                        return
                    }

                    if(binding.viewPager.currentItem == 1) {
                        if(intent.getStringExtra("type") == null && splashScreenKeep) {
                            splashScreenKeep = false


                        }
                    } else if(binding.viewPager.currentItem == 2) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !notificationComplete) {
                            if (ContextCompat.checkSelfPermission(this@MainActivity, android.Manifest.permission.POST_NOTIFICATIONS)
                                != PackageManager.PERMISSION_GRANTED) {
                                //notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)

                                PopupDialog.getInstance(this@MainActivity)
                                    .standardDialogBuilder()
                                    .createStandardDialog()
                                    .setHeading("권한 요청")
                                    .setDescription(
                                        "댓글 알림, 좋아요 알림 등의 작업을 위해서 알림 승인이 필요합니다."
                                    )
                                    .setIcon(R.drawable.location)
                                    .setPositiveButtonText("허가")
                                    .setNegativeButtonText("거부")
                                    .build(object : StandardDialogActionListener {
                                        override fun onPositiveButtonClicked(dialog: Dialog) {
                                            dialog.dismiss()
                                            notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                                        }

                                        override fun onNegativeButtonClicked(dialog: Dialog) {
                                            dialog.dismiss()
                                            notificationComplete = true
                                            Toast.makeText(this@MainActivity, "알림 수신을 거부하셨습니다.", Toast.LENGTH_SHORT).show()
                                        }
                                    })
                                    .show()
                            }
                        }
                    }
                }
            }

            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                when(position) {
                    0 -> binding.bottomNavigationView.menu.select(R.id.menu_profile)
                    1 -> binding.bottomNavigationView.menu.select(R.id.menu_home)
                    2 -> binding.bottomNavigationView.menu.select(R.id.menu_notification)
                }
            }
        }
        // ViewPager2 페이지 변경 콜백 설정
        binding.viewPager.registerOnPageChangeCallback(viewPagerPageCallback)

        binding.viewPager.offscreenPageLimit = 3
        binding.viewPager.currentItem = 1

        doubleBackPressHandler = DoubleBackPressHandler(this)
        doubleBackPressHandler.enable()

        initPermission()
        checkNotification()
        setContentView(binding.root)
    }

    fun checkNotification() {
        when(intent.getStringExtra("type")) {
            "comment" -> {
                val highlightComment = IntentCompat.getParcelableExtra(intent, "highlight", NotificationComment::class.java) ?: return
                if(!highlightComment.readed) {
                    val highlightCommentRef = LitePal.where(
                        "articleId = ? AND commentId = ? AND subCommentId = ? AND writer = ? AND contents = ? AND userImg = ?",
                        highlightComment.articleId,
                        highlightComment.commentId,
                        highlightComment.subCommentId,
                        highlightComment.writer,
                        highlightComment.contents,
                        highlightComment.userImg
                    ).limit(1).find(NotificationComment::class.java)[0]

                    highlightCommentRef.readed = true
                    highlightCommentRef.save()
                }

                val intent = Intent(
                    this@MainActivity,
                    ArticleDetailActivity::class.java
                )

                intent.putExtra("highlight", highlightComment)
                intent.putExtra("articleId", highlightComment.articleId)
                startActivity(intent)

                splashScreenKeep = false
            }
            else -> {

            }
        }
    }

    fun initPermission() {
        notificationPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()) { isGranted ->
            notificationComplete = true
            if (!isGranted) {
                Snackbar.make(findViewById(R.id.main), "알림 권한이 거부되었습니다.", Snackbar.LENGTH_SHORT).show()
            }
        }

        locationPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()) {
            if(it[android.Manifest.permission.ACCESS_COARSE_LOCATION]!! && !it[android.Manifest.permission.ACCESS_FINE_LOCATION]!!) {
                //대략적 위치
                Snackbar.make(findViewById(R.id.main), "정확한 위치 정보를 받아올 수 없어 정확성이 떨어집니다.", Snackbar.LENGTH_SHORT).show()
            } else if(!it[android.Manifest.permission.ACCESS_COARSE_LOCATION]!! && !it[android.Manifest.permission.ACCESS_FINE_LOCATION]!!) {
                //거부
                Snackbar.make(findViewById(R.id.main), "권한이 거부되어 정상적인 이용이 불가능합니다.", Snackbar.LENGTH_SHORT).show()
                return@registerForActivityResult
            }
            locationPermissionCheckEnd()
        }

        if (ContextCompat.checkSelfPermission(this@MainActivity, android.Manifest.permission.ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {

            PopupDialog.getInstance(this)
                .standardDialogBuilder()
                .createStandardDialog()
                .setHeading("권한 요청")
                .setDescription(
                    "정상적인 서비스 이용을 위해 위치 서비스 권한이 필요합니다."
                )
                .setIcon(R.drawable.location)
                .setPositiveButtonText("허가")
                .setNegativeButtonText("거부")
                .build(object : StandardDialogActionListener {
                    override fun onPositiveButtonClicked(dialog: Dialog) {
                        dialog.dismiss()
                        locationPermissionLauncher.launch(arrayOf(
                            android.Manifest.permission.ACCESS_FINE_LOCATION,
                            android.Manifest.permission.ACCESS_COARSE_LOCATION)
                        )
                    }

                    override fun onNegativeButtonClicked(dialog: Dialog) {
                        dialog.dismiss()
                        Toast.makeText(this@MainActivity, "권한이 거부되어 정상적인 이용이 불가능합니다.", Toast.LENGTH_SHORT).show()
                    }
                })
                .show()
        } else if (ContextCompat.checkSelfPermission(this@MainActivity, android.Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {

            Snackbar.make(findViewById(R.id.main), "정확한 위치 정보를 받아올 수 없어 정확성이 떨어집니다.", Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun sendFcmTokenToServer(fcmToken: String) {
        val request = AddFcmTokenRequest(fcmToken)
        NetworkManager.apiService.addFcmToken(request).enqueue(object : Callback<FcmTokenResponse> {
            override fun onResponse(call: Call<FcmTokenResponse>, response: Response<FcmTokenResponse>) {
                if (call.isCanceled) return
                if (!response.isSuccessful) {
                    Log.e("AddFcmToken", "Failed With HTTP Code: " + response.code())
                    return
                }

                val result = response.body()!!
                if (!result.success) {
                    Log.e("AddFcmToken", "토큰을 서버로 전송하는 과정에서 오류가 발생했습니다: ${result.msg}")
                    return
                }
                Log.d("AddFcmToken", "success")
            }

            override fun onFailure(call: Call<FcmTokenResponse>, err: Throwable) {
                Log.e("AddFcmToken", "Failed: $err")
                Log.e("AddFcmToken", "토큰을 서버로 전송하는 과정에서 오류가 발생했습니다.")
            }
        })
    }

    public override fun onDestroy() {
        super.onDestroy()
        doubleBackPressHandler.disable()
    }

    override fun onResume() {
        super.onResume()

        if(mAuth!!.currentUser == null) {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(
                this,
                LoginActivity::class.java
            )
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(
                intent
            )
            //finish()
            return
        }
    }

    override fun attachBaseContext(newBase: Context) {
        setLocale(newBase)
        super.attachBaseContext(newBase)
    }

    private fun setLocale(ctx: Context) {
        val loc = Lingver.getInstance().getLocale()

        val resources = ctx.resources
        val configuration = resources.configuration
        configuration.setLocale(loc)

        ctx.createConfigurationContext(
            configuration
        )
    }

    fun locationPermissionCheckEnd() {
        homeFragment.initGPS()

        // FCM 토큰 받아오기
        val sharedPreferences = SettingManager.getInstance()
        val savedToken = sharedPreferences.getFcmToken()

//        // 알림 권한 요청
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//            if (ContextCompat.checkSelfPermission(this@MainActivity, android.Manifest.permission.POST_NOTIFICATIONS)
//                != PackageManager.PERMISSION_GRANTED) {
//                notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
//            }
//        }

        // sharedPreference에 토큰 값이 없다면 FCM 토큰을 받아옴
        if (savedToken.isNullOrEmpty()) {
            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val fcmToken = task.result
                    sharedPreferences.saveFcmToken(fcmToken, false)
                    // FCM 토큰을 서버로 전송
                    sendFcmTokenToServer(fcmToken)
                    Log.d("token", "FCM Token: $fcmToken")
                } else {
                    Log.d("token", "FCM 토큰 가져오기 실패: ${task.exception?.message}")
                }
            }
        } else {
            sendFcmTokenToServer(savedToken)
        }
    }

    private fun ViewPager2.reduceDragSensitivity(f: Int = 4) {
        val recyclerViewField = ViewPager2::class.java.getDeclaredField("mRecyclerView")
        recyclerViewField.isAccessible = true
        val recyclerView = recyclerViewField.get(this) as RecyclerView
        val touchSlopField = RecyclerView::class.java.getDeclaredField("mTouchSlop")
        touchSlopField.isAccessible = true
        val touchSlop = touchSlopField.get(recyclerView) as Int
        touchSlopField.set(recyclerView, touchSlop*f) //8기본
    }

    override fun shouldKeepOnScreen(): Boolean {
        return splashScreenKeep
    }

    override fun invoke(v: View, menuItem: github.com.st235.lib_expandablebottombar.MenuItem, byUser: Boolean) {
        when (menuItem.id) {
            R.id.menu_profile -> {
                binding.viewPager.currentItem = 0
                binding.viewPager.isUserInputEnabled = true
            }
            R.id.menu_home -> {
                binding.viewPager.currentItem = 1
                binding.viewPager.isUserInputEnabled = false
            }
            R.id.menu_notification -> {
                binding.viewPager.currentItem = 2
                binding.viewPager.isUserInputEnabled = true
            }
        }
    }
}