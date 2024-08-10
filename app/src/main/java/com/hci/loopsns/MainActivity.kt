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
import com.hci.loopsns.storage.models.NotificationFavorite
import com.hci.loopsns.utils.DoubleBackPressHandler
import com.hci.loopsns.view.fragment.HomeFragment
import com.hci.loopsns.view.fragment.MainViewPageAdapter
import com.hci.loopsns.view.fragment.NotificationsFragment
import com.hci.loopsns.view.fragment.ProfileFragment
import com.saadahmedev.popupdialog.PopupDialog
import com.saadahmedev.popupdialog.listener.StandardDialogActionListener
import github.com.st235.lib_expandablebottombar.MenuItem
import github.com.st235.lib_expandablebottombar.OnItemClickListener
import org.litepal.LitePal
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Date


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
                            if (ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.POST_NOTIFICATIONS)
                                != PackageManager.PERMISSION_GRANTED) {
                                //notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)

                                PopupDialog.getInstance(this@MainActivity)
                                    .standardDialogBuilder()
                                    .createStandardDialog()
                                    .setHeading(getString(R.string.permission_request_notification_head))
                                    .setDescription(
                                        getString(R.string.permission_request_notification_body)
                                    )
                                    .setIcon(R.drawable.location)
                                    .setPositiveButtonText(getString(R.string.permission_request_notification_yes))
                                    .setNegativeButtonText(getString(R.string.permission_request_notification_no))
                                    .build(object : StandardDialogActionListener {
                                        override fun onPositiveButtonClicked(dialog: Dialog) {
                                            dialog.dismiss()
                                            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                        }

                                        override fun onNegativeButtonClicked(dialog: Dialog) {
                                            dialog.dismiss()
                                            notificationComplete = true
                                            Toast.makeText(this@MainActivity, getString(R.string.opting_out_of_alarm), Toast.LENGTH_SHORT).show()
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

                val newIntent = Intent(
                    this@MainActivity,
                    ArticleDetailActivity::class.java
                )

                newIntent.putExtra("highlight", highlightComment)
                newIntent.putExtra("articleId", highlightComment.articleId)
                startActivity(newIntent)

                intent.extras?.clear()
                splashScreenKeep = false
            }
            "likes" -> {
                val favorite = IntentCompat.getParcelableExtra(intent, "favorite", NotificationFavorite::class.java) ?: return
                if(!favorite.readed) {
                    val favoriteRef = LitePal.where(
                        "articleId = ? AND likeCount = ?",
                        favorite.articleId,
                        favorite.likeCount.toString()
                    ).limit(1).find(NotificationFavorite::class.java)[0]

                    favoriteRef.readed = true
                    favoriteRef.save()
                }

                val newIntent = Intent(
                    this@MainActivity,
                    ArticleDetailActivity::class.java
                )
                newIntent.putExtra("articleId", favorite.articleId)
                startActivity(newIntent)

                intent.extras?.clear()
                splashScreenKeep = false
            }
            "deeplink" -> {
                val newIntent = Intent(
                    this@MainActivity,
                    ArticleDetailActivity::class.java
                )

                val commentId = intent.getStringExtra("commentId") ?: ""
                if(commentId.isEmpty()) {
                    newIntent.putExtra("articleId", intent.getStringExtra("articleId") ?: "")
                    startActivity(newIntent)

                    intent.extras?.clear()
                    splashScreenKeep = false
                    return
                }

                newIntent.putExtra("highlight", NotificationComment(
                    articleId = intent.getStringExtra("articleId") ?: "",
                    commentId = intent.getStringExtra("commentId") ?: "",
                    subCommentId = intent.getStringExtra("subCommentId") ?: "",
                    writer = "",
                    contents = "",
                    userImg = "",
                    time = Date(),
                    readed = false
                ))
                newIntent.putExtra("articleId", intent.getStringExtra("articleId") ?: "")
                startActivity(newIntent)

                intent.extras?.clear()
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
                Snackbar.make(findViewById(R.id.main), getString(R.string.deny_alarm_permission), Snackbar.LENGTH_SHORT).show()
            }
        }

        // FCM 토큰 받아오기
        val sharedPreferences = SettingManager.getInstance()
        val savedToken = sharedPreferences.getFcmToken()
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

        locationPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()) {
            if(it[Manifest.permission.ACCESS_COARSE_LOCATION]!! && !it[Manifest.permission.ACCESS_FINE_LOCATION]!!) {
                //대략적 위치
                Snackbar.make(findViewById(R.id.main), getString(R.string.less_accurate_no_precise_location_information), Snackbar.LENGTH_SHORT).show()
            } else if(!it[Manifest.permission.ACCESS_COARSE_LOCATION]!! && !it[Manifest.permission.ACCESS_FINE_LOCATION]!!) {
                //거부
                Snackbar.make(findViewById(R.id.main), getString(R.string.deny_permission_not_available), Snackbar.LENGTH_SHORT).show()
            }
            locationPermissionCheckEnd()
        }

        if (ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {

            PopupDialog.getInstance(this)
                .standardDialogBuilder()
                .createStandardDialog()
                .setHeading(getString(R.string.permission_request_gps_head))
                .setDescription(
                    getString(R.string.permission_request_gps_body)
                )
                .setIcon(R.drawable.location)
                .setPositiveButtonText(getString(R.string.permission_request_gps_yes))
                .setNegativeButtonText(getString(R.string.permission_request_gps_no))
                .build(object : StandardDialogActionListener {
                    override fun onPositiveButtonClicked(dialog: Dialog) {
                        dialog.dismiss()
                        locationPermissionLauncher.launch(arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION)
                        )
                    }

                    override fun onNegativeButtonClicked(dialog: Dialog) {
                        dialog.dismiss()
                        Toast.makeText(this@MainActivity, getString(R.string.deny_permission_not_available), Toast.LENGTH_SHORT).show()
                        locationPermissionCheckEnd()
                    }
                })
                .show()
        } else if (ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {

            Snackbar.make(findViewById(R.id.main), getString(R.string.less_accurate_no_precise_location_information), Snackbar.LENGTH_SHORT).show()
            locationPermissionCheckEnd()
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

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(SettingManager.getInstance().getCurrentLocaleContext(base))
    }

    fun locationPermissionCheckEnd() {
        homeFragment.initGPS()
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

    override fun invoke(v: View, menuItem: MenuItem, byUser: Boolean) {
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