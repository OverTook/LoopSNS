package com.hci.loopsns

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.tabs.TabLayoutMediator
import com.hci.loopsns.databinding.ActivityMainBinding;

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: ViewPageAdapter2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // ViewPager2 어댑터 설정
        adapter = ViewPageAdapter2(this)
        adapter.addFragment(HomeFragment())
        adapter.addFragment(SettingMenuFragment())
        adapter.addFragment(ThirdFragment())
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
        binding.bottomNavigation.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_home -> {
                    binding.viewPager.currentItem = 0
                    true
                }
                R.id.menu_settings -> {
                    binding.viewPager.currentItem = 1
                    true
                }
                R.id.menu_notifications -> {
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
                    0 -> binding.bottomNavigation.selectedItemId = R.id.menu_home
                    1 -> binding.bottomNavigation.selectedItemId = R.id.menu_settings
                    2 -> binding.bottomNavigation.selectedItemId = R.id.menu_notifications
                }
            }
        })
    }
}