package com.hci.loopsns.view.fragment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageButton
import androidx.fragment.app.Fragment
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bumptech.glide.Glide
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.hci.loopsns.R
import com.hci.loopsns.SettingsActivity
import com.hci.loopsns.event.ProfileListener
import com.hci.loopsns.event.ProfileManager
import com.hci.loopsns.view.fragment.profile.ProfileMyArticleFragment
import com.hci.loopsns.view.fragment.profile.ProfileMyLikeFragment


class ProfileFragment : Fragment(), View.OnClickListener, ProfileListener, SwipeRefreshLayout.OnRefreshListener {

    private lateinit var myArticleFragment: ProfileMyArticleFragment
    private lateinit var myLikeFragment: ProfileMyLikeFragment

    private lateinit var user: FirebaseUser
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        ProfileManager.getInstance().registerProfileListener(this)
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if(!this::myArticleFragment.isInitialized) {
            myArticleFragment = ProfileMyArticleFragment()
            myLikeFragment = ProfileMyLikeFragment()
        }

        user = FirebaseAuth.getInstance().currentUser!!
        view.findViewById<TextView>(R.id.name_text).text = user.displayName

        if(user.photoUrl != null) {
            Glide.with(requireActivity())
                .load(user.photoUrl!!.toString())
                .into(view.findViewById(R.id.profile_image))
        }

        if(childFragmentManager.fragments.size < 2) {
            childFragmentManager.beginTransaction()
                .add(R.id.containers, myArticleFragment)
                .add(R.id.containers, myLikeFragment)
                .hide(myLikeFragment).commit()
        }

        view.findViewById<TabLayout>(R.id.tabLayout).addOnTabSelectedListener(object : OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                val position = tab.position
                when(position) {
                    0 -> {
                        if(childFragmentManager.fragments.contains(myArticleFragment)) {
                            childFragmentManager.beginTransaction()
                                .hide(myLikeFragment)
                                .show(myArticleFragment)
                                .commit()
                            return
                        }
                        //childFragmentManager.beginTransaction().add(R.id.containers, myArticleFragment).commit()
                    }
                    else -> {
                        if(childFragmentManager.fragments.contains(myLikeFragment)) {
                            childFragmentManager.beginTransaction()
                                .hide(myArticleFragment)
                                .show(myLikeFragment)
                                .commit()
                            return
                        }
                        //childFragmentManager.beginTransaction().add(R.id.containers, myLikeFragment).commit()
                    }
                }

            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
            }

            override fun onTabReselected(tab: TabLayout.Tab) {
            }
        })

        view.findViewById<SwipeRefreshLayout>(R.id.swipeLayout).setOnRefreshListener(this)
        view.findViewById<AppCompatImageButton>(R.id.settingBtn).setOnClickListener(this)
    }

    override fun onDestroy() {
        super.onDestroy()

        ProfileManager.getInstance().removeProfileListener(this)
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

    override fun onChangedProfile() {
        user = FirebaseAuth.getInstance().currentUser!!
        Log.e("user", user.displayName.toString())
        requireView().findViewById<TextView>(R.id.name_text).text = user.displayName

        if(user.photoUrl != null) {
            Glide.with(requireActivity())
                .load(user.photoUrl!!.toString())
                .into(requireView().findViewById(R.id.profile_image))
        }
    }

    override fun onRefresh() {
        requireView().findViewById<SwipeRefreshLayout>(R.id.swipeLayout).isRefreshing = false
        myLikeFragment.onInitializeArticle()
        myArticleFragment.onInitializeArticle()
    }
}