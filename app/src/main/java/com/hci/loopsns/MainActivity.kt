package com.hci.loopsns

import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import com.hci.loopsns.databinding.ActivityMainBinding
import com.hci.loopsns.view.fragment.HomeFragment
import com.hci.loopsns.view.fragment.NotificationsFragment
import com.hci.loopsns.view.fragment.ProfileFragment
import com.hci.loopsns.view.fragment.MainViewPageAdapter
import com.hci.loopsns.network.NetworkManager
import com.hci.loopsns.utils.AuthAppCompatActivity
import com.hci.loopsns.utils.DoubleBackPressHandler

class MainActivity : AuthAppCompatActivity() {

    private lateinit var doubleBackPressHandler: DoubleBackPressHandler
    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: MainViewPageAdapter

    private lateinit var homeFragment: HomeFragment
    private lateinit var profileFragment: ProfileFragment
    private lateinit var notificationsFragment: NotificationsFragment

    private lateinit var locationPermissionLauncher: ActivityResultLauncher<Array<String>>

    private var mAuth: FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


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

        // TabLayout과 ViewPager2를 연결
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            when (position) {
                0 -> tab.text = "Settings"
                1 -> tab.text = "Home"
                2 -> tab.text = "Notifications"
            }
        }.attach()


        // BottomNavigationView의 아이템 선택 리스너 설정
        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.setting_menu -> {
                    binding.viewPager.currentItem = 0
                    binding.viewPager.isUserInputEnabled = true
                    true
                }
                R.id.home -> {
                    binding.viewPager.currentItem = 1
                    binding.viewPager.isUserInputEnabled = false
                    true
                }
                R.id.notification -> {
                    binding.viewPager.currentItem = 2
                    binding.viewPager.isUserInputEnabled = true
                    true
                }
                else -> false
            }
        }

        // ViewPager2 페이지 변경 콜백 설정
        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                when (position) {
                    0 -> {
                        binding.bottomNavigationView.selectedItemId = R.id.setting_menu
                        binding.viewPager.isUserInputEnabled = true
                    }
                    1 -> {
                        binding.bottomNavigationView.selectedItemId = R.id.home
                        binding.viewPager.isUserInputEnabled = false
                    }
                    2 -> {
                        binding.bottomNavigationView.selectedItemId = R.id.notification
                        binding.viewPager.isUserInputEnabled = true
                    }
                }
            }
        })

        binding.bottomNavigationView.setSelectedItemId(R.id.home)
        binding.viewPager.isUserInputEnabled = false

        doubleBackPressHandler = DoubleBackPressHandler(this)
        doubleBackPressHandler.enable()

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
            permissionCheckEnd()
        }

        if (ContextCompat.checkSelfPermission(this@MainActivity, android.Manifest.permission.ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {

            locationPermissionLauncher.launch(arrayOf(
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION)
            )
            return
        } else if (ContextCompat.checkSelfPermission(this@MainActivity, android.Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {

            Snackbar.make(findViewById(R.id.main), "정확한 위치 정보를 받아올 수 없어 정확성이 떨어집니다.", Snackbar.LENGTH_SHORT).show()
        }
    }

    public override fun onDestroy() {
        super.onDestroy()
        doubleBackPressHandler.disable()
    }

    override fun onResume() {
        super.onResume()
        if(mAuth!!.currentUser == null || !NetworkManager.isInitialized) {
            finish()
        }
    }

    fun permissionCheckEnd() {
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
}