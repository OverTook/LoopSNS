package com.hci.loopsns.view.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageButton
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.hci.loopsns.R
import com.hci.loopsns.SettingsActivity
import com.hci.loopsns.storage.SharedPreferenceManager
import com.hci.loopsns.view.fragment.profile.ProfileMyArticleFragment
import com.hci.loopsns.view.fragment.profile.ProfileMyLikeFragment


class ProfileFragment : Fragment(), View.OnClickListener {

    private lateinit var myArticleFragment: ProfileMyArticleFragment
    private lateinit var myLikeFragment: ProfileMyLikeFragment

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val profileManager = SharedPreferenceManager(requireContext())

        myArticleFragment = ProfileMyArticleFragment()
        myLikeFragment = ProfileMyLikeFragment()

        view.findViewById<TextView>(R.id.name_text).text = profileManager.getNickname()

        if(!profileManager.getImageURL().isNullOrBlank()) {
            Glide.with(requireActivity())
                .load(profileManager.getImageURL())
                .into(view.findViewById(R.id.profile_image))
        }

        childFragmentManager.beginTransaction()
            .add(R.id.containers, myArticleFragment)
            .add(R.id.containers, myLikeFragment)
            .hide(myLikeFragment).commit()

        view.findViewById<TabLayout>(R.id.tabLayout).addOnTabSelectedListener(object : OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                val position = tab.position
                when(position) {
                    0 -> {
                        if(childFragmentManager.fragments.contains(myArticleFragment)) {
                            childFragmentManager.beginTransaction()
                                .show(myArticleFragment)
                                .hide(myLikeFragment)
                                .commit()
                            return
                        }
                        childFragmentManager.beginTransaction().add(R.id.containers, myArticleFragment).commit()
                    }
                    else -> {
                        if(childFragmentManager.fragments.contains(myLikeFragment)) {
                            childFragmentManager.beginTransaction()
                                .show(myLikeFragment)
                                .hide(myArticleFragment)
                                .commit()
                            return
                        }
                        childFragmentManager.beginTransaction().add(R.id.containers, myLikeFragment).commit()
                    }
                }

            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
            }

            override fun onTabReselected(tab: TabLayout.Tab) {
            }
        })

        myArticleFragment.onInitializeArticle()
        myLikeFragment.onInitializeArticle()

        view.findViewById<AppCompatImageButton>(R.id.settingBtn).setOnClickListener(this)
    }

    override fun onResume() {
        super.onResume()

        myLikeFragment.onResumeSelf()
        myArticleFragment.onResumeSelf()
    }

    override fun onClick(view: View?) {
        if(view == null) return
        when(view.id) {
            R.id.settingBtn -> {
                startActivity(
                    Intent(
                        requireContext(),
                        SettingsActivity::class.java
                    )
                )
            }
        }
    }
}