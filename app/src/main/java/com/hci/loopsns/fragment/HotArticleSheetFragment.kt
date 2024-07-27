package com.hci.loopsns.fragment

import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.hci.loopsns.R
import com.hci.loopsns.network.Article
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.hci.loopsns.network.geocode.AddressResponse
import com.hci.loopsns.network.geocode.AddressResult
import com.hci.loopsns.network.geocode.ReverseGeocodingManager
import com.hci.loopsns.utils.formatTo
import com.hci.loopsns.utils.toDate
import java.util.Locale

class HotArticleSheetFragment(private val articleClickAction: (Article) -> Unit, private val intent: Intent, private val article: Article, private val lat: Double, private val lng: Double) : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.bottom_sheet_map_overview_timeline_short, container, false)


        view.findViewById<TextView>(R.id.tag_1).text = article.cat1
        view.findViewById<TextView>(R.id.tag_2).text = article.cat2

        val keywordIds = listOf(
            R.id.keyword_1,
            R.id.keyword_2,
            R.id.keyword_3,
            R.id.keyword_4
        )

        for (i in 0 until article.keywords.size) {
            if (i < keywordIds.size) {
                if(article.keywords[i].isNotBlank()) {
                    view.findViewById<TextView>(keywordIds[i]).visibility = View.VISIBLE
                    view.findViewById<TextView>(keywordIds[i]).text = article.keywords[i]
                }
            }
        }

        view.findViewById<Button>(R.id.all_view_btn).setOnClickListener {
            dismiss()
            intent.putExtra("name", view.findViewById<TextView>(R.id.location_name).text)
            intent.putExtra("point", view.findViewById<TextView>(R.id.point_of_interest).text)
            startActivity(intent)
        }

        view.findViewById<TextView>(R.id.content_text).text = article.contents

        view.findViewById<TextView>(R.id.article_time).text = article.time.toDate().formatTo("yyyy-MM-dd HH:mm")

        view.findViewById<TextView>(R.id.like_count).text = article.likeCount.toString()
        view.findViewById<TextView>(R.id.comment_count).text = article.commentCount.toString()
        view.findViewById<ConstraintLayout>(R.id.hot_article_overview).setOnClickListener {
            articleClickAction(article)
        }

        if(article.images.isNotEmpty()) {
            Glide.with(requireContext())
                .load(article.images[0]) //TODO 한장만?
                .thumbnail(Glide.with(requireContext()).load(R.drawable.picture_placeholder))
                .into(view.findViewById<ImageView>(R.id.content_image))
        }


        (dialog as? BottomSheetDialog)?.behavior?.state = BottomSheetBehavior.STATE_EXPANDED
        getLocation()
        return view
    }

    fun getLocation() {
        val ai: ApplicationInfo = requireContext().packageManager.getApplicationInfo(requireActivity().packageName, PackageManager.GET_META_DATA)

        ReverseGeocodingManager.apiService.getAddress(
            "$lat,$lng",
            ai.metaData.getString("com.google.android.geo.API_KEY")!!,
            Locale.getDefault().language
        ).enqueue(object: retrofit2.Callback<AddressResponse> {
            override fun onResponse(call: retrofit2.Call<AddressResponse>, response: retrofit2.Response<AddressResponse>) {
                if(!response.isSuccessful) return

                val body = response.body()!!

                if(body.results.isEmpty()) {
                    return
                }

                body.results.forEach {
                    if(it.types.contains("point_of_interest")) {
                        onGeocodePointOfInterest(it)
                        return@forEach
                    }
                }

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

    fun onGeocodePointOfInterest(address: AddressResult) {
        var addressText = ""

        address.addressComponents.forEach {
            if (it.types.contains("point_of_interest")) {
                addressText = it.longName
                return@forEach
            }
        }

        requireActivity().runOnUiThread {
            view?.findViewById<TextView>(R.id.point_of_interest)?.text = addressText
        }
    }

    fun onGeocodeLocation(address: AddressResult) {
        var addressText = ""

        var country = ""
        address.addressComponents.forEach {
            if (it.types.contains("country")) {
                country = it.longName
                return@forEach
            }
        }
        addressText = address.formattedAddress

        if(country.isNotBlank() && addressText.contains(country)) {
            addressText = addressText.split(country)[1].trim()
        }

        requireActivity().runOnUiThread {
            view?.findViewById<TextView>(R.id.location_name)?.text = addressText
        }
    }
}