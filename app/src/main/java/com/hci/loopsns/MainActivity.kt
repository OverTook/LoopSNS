package com.hci.loopsns

import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.get
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayoutMediator
import com.hci.loopsns.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: ViewPageAdapter2

    private lateinit var locationPermissionLauncher: ActivityResultLauncher<Array<String>>

    private lateinit var homeFragment: HomeFragment
    private lateinit var settingMenuFragment: SettingMenuFragment
    private lateinit var notificationsFragment: NotificationsFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        homeFragment = HomeFragment()
        settingMenuFragment = SettingMenuFragment()
        notificationsFragment = NotificationsFragment()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // ViewPager2 어댑터 설정
        adapter = ViewPageAdapter2(this)
        adapter.addFragment(settingMenuFragment)
        adapter.addFragment(homeFragment)
        adapter.addFragment(notificationsFragment)
        binding.viewPager.adapter = adapter

        // 슬라이드 동작 제어를 위한 페이지 전환 콜백 설정
        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageScrollStateChanged(state: Int) {
                super.onPageScrollStateChanged(state)
                // 현재 페이지가 설정 메뉴일 때 왼쪽 슬라이드 막기
                if (binding.viewPager.currentItem == 0 && state == ViewPager2.SCROLL_STATE_DRAGGING) {
                    binding.viewPager.isUserInputEnabled = false
                }
                // 현재 페이지가 알림창일 때 오른쪽 슬라이드 막기
                else if (binding.viewPager.currentItem == 2 && state == ViewPager2.SCROLL_STATE_DRAGGING) {
                    binding.viewPager.isUserInputEnabled = false
                } else {
                    binding.viewPager.isUserInputEnabled = true
                }
            }
        })

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
                    true
                }
                R.id.home -> {
                    binding.viewPager.currentItem = 1
                    true
                }
                R.id.notification -> {
                    binding.viewPager.currentItem = 2
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
                    0 -> binding.bottomNavigationView.selectedItemId = R.id.setting_menu
                    1 -> binding.bottomNavigationView.selectedItemId = R.id.home
                    2 -> binding.bottomNavigationView.selectedItemId = R.id.notification
                }
            }
        })

        // 기본 선택 항목 설정
        binding.bottomNavigationView.selectedItemId = R.id.home
        binding.viewPager.currentItem = 1 // 첫 화면을 홈으로 설정

        locationPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()) {
            if(it[android.Manifest.permission.ACCESS_COARSE_LOCATION]!! && !it[android.Manifest.permission.ACCESS_FINE_LOCATION]!!) {
                //대략적 위치
                Snackbar.make(findViewById(R.id.main), "정확한 위치 정보를 받아올 수 없어 정확성이 떨어집니다.", Snackbar.LENGTH_SHORT).show()
            }
            homeFragment.getCurrentLocation()
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
        } else {
            homeFragment.getCurrentLocation()
        }
    }
}
