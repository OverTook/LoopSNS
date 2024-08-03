package com.hci.loopsns

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.GestureDetector
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.IntentCompat
import androidx.core.splashscreen.SplashScreen
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.material.navigation.NavigationBarView
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging
import com.hci.loopsns.databinding.ActivityMainBinding
import com.hci.loopsns.network.AddFcmTokenRequest
import com.hci.loopsns.network.ArticleDetailResponse
import com.hci.loopsns.network.FcmTokenResponse
import com.hci.loopsns.view.fragment.HomeFragment
import com.hci.loopsns.view.fragment.NotificationsFragment
import com.hci.loopsns.view.fragment.ProfileFragment
import com.hci.loopsns.view.fragment.MainViewPageAdapter
import com.hci.loopsns.network.NetworkManager
import com.hci.loopsns.storage.SharedPreferenceManager
import com.hci.loopsns.storage.models.NotificationComment
import com.hci.loopsns.utils.DoubleBackPressHandler
import com.hci.loopsns.utils.hideDarkOverlay
import com.hci.loopsns.utils.showDarkOverlay
import github.com.st235.lib_expandablebottombar.OnItemClickListener
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.litepal.LitePal
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.math.abs

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

        // ViewPager2 페이지 변경 콜백 설정
        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageScrollStateChanged(state: Int) {
                super.onPageScrollStateChanged(state)
                if (state == ViewPager2.SCROLL_STATE_IDLE) {
                    if(binding.viewPager.currentItem == 1) {
                        if(intent.getStringExtra("type") == null && splashScreenKeep) {
                            splashScreenKeep = false
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
        })

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

            locationPermissionLauncher.launch(arrayOf(
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION)
            )
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
        if(mAuth!!.currentUser == null || NetworkManager.token.token.isEmpty()) {
            finish()
        }
    }

    fun locationPermissionCheckEnd() {
        homeFragment.initGPS()

        // FCM 토큰 받아오기
        val sharedPreferences = SharedPreferenceManager(this)
        val (savedToken, tokenChanged) = sharedPreferences.getFcmToken()

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
            // 알림 권한 요청
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(this@MainActivity, android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                    notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        } else if(tokenChanged) {
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