package com.hci.loopsns.view.bottomsheet

import android.animation.ValueAnimator
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import android.widget.Button
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.marginBottom
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.ChangeBounds
import androidx.transition.TransitionManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.hci.loopsns.R
import com.hci.loopsns.network.ArticleDetail
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.hci.loopsns.network.AddressResponse
import com.hci.loopsns.network.AddressResult
import com.hci.loopsns.network.NetworkManager
import com.hci.loopsns.recyclers.timeline.ArticleRecyclerViewAdapter
import com.skydoves.androidveil.VeilRecyclerFrameView
import java.util.Locale

class ArticleBottomSheet() : BottomSheetDialogFragment(), View.OnClickListener {

    private lateinit var adapter: ArticleRecyclerViewAdapter
    private lateinit var recyclerView: RecyclerView

    private lateinit var articles: List<ArticleDetail>
    private lateinit var hotArticles: List<ArticleDetail>

    private lateinit var articleIntent: Intent

    private var lat: Double = 0.0
    private var lng: Double = 0.0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.bottom_sheet_map_overview_timeline_short, container, false)

        adapter = ArticleRecyclerViewAdapter(requireContext(), ::onClickArticle)

        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        adapter.setItemInCollapse(articles[0])

        if(articles.size <= 1) {
            view.findViewById<Button>(R.id.all_view_btn).visibility = View.GONE
            return view
        }

        view.findViewById<Button>(R.id.all_view_btn).setOnClickListener(this)
        //(dialog as? BottomSheetDialog)?.behavior?.state = BottomSheetBehavior.STATE_EXPANDED


        return view
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        getLocation()
    }

    fun onClickArticle(article: ArticleDetail) {
        articleIntent.putExtra("articleId", article.uid)
        startActivity(articleIntent)
    }

    fun setData(articles: List<ArticleDetail>, hotArticles: List<ArticleDetail>, articleIntent: Intent, lat: Double, lng: Double): ArticleBottomSheet {
        this.articles = articles
        this.hotArticles = hotArticles
        this.articleIntent = articleIntent
        this.lat = lat
        this.lng = lng
        return this
    }

    fun getLocation() {
        NetworkManager.apiService.getAddress(
            "$lat,$lng",
            Locale.getDefault().language
        ).enqueue(object: retrofit2.Callback<AddressResponse> {
            override fun onResponse(call: retrofit2.Call<AddressResponse>, response: retrofit2.Response<AddressResponse>) {
                if(!response.isSuccessful) return

                val body = response.body()!!

                if(body.results.isEmpty()) {
                    return
                }

//                body.results.forEach {
//                    if(it.types.contains("point_of_interest")) {
//                        onGeocodePointOfInterest(it)
//                        return@forEach
//                    }
//                }

                body.results.forEach {
                    if(it.types.contains("sublocality_level_4")) {
                        onGeocodeLocation(it)
                        return
                    }
                }
                body.results.forEach {
                    if(it.types.contains("sublocality_level_3")) {
                        onGeocodeLocation(it)
                        return
                    }
                }
                body.results.forEach {
                    if(it.types.contains("sublocality_level_2")) {
                        onGeocodeLocation(it)
                        return
                    }
                }
                body.results.forEach {
                    if(it.types.contains("sublocality_level_1")) {
                        onGeocodeLocation(it)
                        return
                    }
                }

                onGeocodeLocation(body.results[0])
            }

            override fun onFailure(call: retrofit2.Call<AddressResponse>, err: Throwable) {
                Log.e("HotArticle Geocoding Failed", err.toString())
            }

        })
    }

//    fun onGeocodePointOfInterest(address: AddressResult) {
//        var addressText = ""
//
//        address.addressComponents.forEach {
//            if (it.types.contains("point_of_interest")) {
//                addressText = it.longName
//                return@forEach
//            }
//        }
//
//        requireActivity().runOnUiThread {
//            view?.findViewById<TextView>(R.id.point_of_interest)?.text = addressText
//        }
//    }

    fun onGeocodeLocation(address: AddressResult) {
        var addressText: String

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

        requireActivity().runOnUiThread {
            view?.findViewById<TextView>(R.id.location_name)?.text = addressText
        }
    }

    override fun onClick(view: View?) {
        if (view == null)
            return

        when(view.id) {
            R.id.all_view_btn -> {
                dialog?.let {
                    val bottomSheet = it.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
                    bottomSheet?.let { sheet ->
                        val behavior = BottomSheetBehavior.from(sheet)
                        behavior.state = BottomSheetBehavior.STATE_EXPANDED
                        behavior.skipCollapsed = true
                    }
                }


                adapter.setItems(articles, hotArticles)

                val bottom = requireView().findViewById<View>(R.id.bottom)
                bottom.animate()
                    .alpha(0f)
                    .withEndAction {
                        bottom.visibility = View.GONE
                    }
                    .start()


                val main = requireView().findViewById<ConstraintLayout>(R.id.main)
                val transition = ChangeBounds()
                transition.duration = 300
                transition.interpolator = FastOutSlowInInterpolator()
                TransitionManager.beginDelayedTransition(main, transition)

                val layoutParams = recyclerView.layoutParams as ViewGroup.MarginLayoutParams
                layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
                layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
                recyclerView.layoutParams = layoutParams
                recyclerView.requestFocus()
                recyclerView.bringToFront()

                val bottomSheetView = requireView().parent as? ViewGroup
                bottomSheetView?.let {
                    TransitionManager.beginDelayedTransition(it, transition)
                    val bottomSheetLayoutParams = it.layoutParams
                    bottomSheetLayoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT

                    it.layoutParams = bottomSheetLayoutParams
                    main.setPadding(
                        0,
                        0,
                        0,
                        10
                    )
                }

                TransitionManager.endTransitions(main)
            }
        }
    }
}