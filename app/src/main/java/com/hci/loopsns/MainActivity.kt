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
        adapter.addFragment(HomeFragment())
        adapter.addFragment(SettingMenuFragment())
        adapter.addFragment(NotificationsFragment())
        binding.viewPager.adapter = adapter

        // TabLayout과 ViewPager2를 연결
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            when (position) {
                0 -> tab.text = "Home"
                1 -> tab.text = "Settings"
                2 -> tab.text = "Notifications"
            }
        }.attach()

        // BottomNavigationView의 아이템 선택 리스너 설정
        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.home -> {
                    binding.viewPager.currentItem = 0
                    supportFragmentManager.beginTransaction().replace(R.id.containers, homeFragment).commit()
                    true
                }
                R.id.setting_menu -> {
                    binding.viewPager.currentItem = 1
                    supportFragmentManager.beginTransaction().replace(R.id.containers, settingMenuFragment).commit()
                    true
                }
                R.id.notification -> {
                    binding.viewPager.currentItem = 2
                    supportFragmentManager.beginTransaction().replace(R.id.containers, notificationsFragment).commit()
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
                    0 -> binding.bottomNavigationView.selectedItemId = R.id.home
                    1 -> binding.bottomNavigationView.selectedItemId = R.id.setting_menu
                    2 -> binding.bottomNavigationView.selectedItemId = R.id.notification
                }
            }
        })

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