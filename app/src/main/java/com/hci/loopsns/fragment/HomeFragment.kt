package com.hci.loopsns.fragment

import android.Manifest
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.VisibleRegion
import com.google.android.material.snackbar.Snackbar
import com.hci.loopsns.ArticleCreateActivity
import com.hci.loopsns.ArticleDetailActivity
import com.hci.loopsns.R
import com.hci.loopsns.TimelineActivity
import com.hci.loopsns.network.Article
import com.hci.loopsns.network.ArticleDetailResponse
import com.hci.loopsns.network.ArticleMarkersResponse
import com.hci.loopsns.network.ArticleTimelineResponse
import com.hci.loopsns.network.NetworkManager
import com.hci.loopsns.network.geocode.AddressResponse
import com.hci.loopsns.network.geocode.AddressResult
import com.hci.loopsns.network.geocode.ReverseGeocodingManager
import com.hci.loopsns.utils.hideDarkOverlay
import com.hci.loopsns.utils.showDarkOverlay
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.concurrent.Executors


class HomeFragment : Fragment(), OnMapReadyCallback, GoogleMap.OnCameraIdleListener, GoogleMap.OnMarkerClickListener {

    private lateinit var googleMap: GoogleMap

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var locationRequest: LocationRequest
    private lateinit var currentLocation: LatLng

    private val executor = Executors.newSingleThreadScheduledExecutor()

    private var markers: ArrayList<Marker>? = null
    private var bottomSheetFragment: HotArticleSheetFragment? = null

    private lateinit var viewOfLayout: View

    private var articleMarkerRequest: Call<ArticleMarkersResponse>? = null
    private var articleCount: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewOfLayout = inflater.inflate(R.layout.activity_map_overview, container, false)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        viewOfLayout.findViewById<ImageButton>(R.id.gps_move_to_current_btn).setOnClickListener {
            val cameraUpdate = CameraUpdateFactory.newLatLngZoom(currentLocation, 15f)
            googleMap.animateCamera(cameraUpdate)
        }

        viewOfLayout.findViewById<ImageButton>(R.id.article_write_btn).setOnClickListener {
            val intent = Intent(
                requireActivity(),
                ArticleCreateActivity::class.java
            )
            intent.putExtra("x", currentLocation.latitude)
            intent.putExtra("y", currentLocation.longitude)
            startActivity(intent)
        }

        initGPS()
        return viewOfLayout
    }

    fun initGPS(){
        if(ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        locationRequest = LocationRequest.Builder(5000)
            .setIntervalMillis(5000)
            .setMinUpdateIntervalMillis(1000)
            .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
            .build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                locationResult.let {
                    val lastLocation = locationResult.lastLocation
                    lastLocation?.let {
                        currentLocation = LatLng(it.latitude, it.longitude)
                        return
                    }
                }

                Snackbar.make(viewOfLayout.findViewById(R.id.main), "현재 위치를 받아올 수 없습니다.", Snackbar.LENGTH_LONG).show()
            }
        }


        val mapFragment = (childFragmentManager.findFragmentById(R.id.googleMap) as SupportMapFragment)
        mapFragment.getMapAsync(this)

        getCurrentLocation()
    }

    fun getCurrentLocation() {
        try {
            fusedLocationClient.requestLocationUpdates(locationRequest, executor, locationCallback)

            fusedLocationClient.lastLocation.addOnSuccessListener { location : Location? ->
                location?.let {
                    currentLocation = LatLng(it.latitude, it.longitude)

                    val cameraUpdate = CameraUpdateFactory.newLatLngZoom(LatLng(it.latitude, it.longitude), 15f)
                    googleMap.animateCamera(cameraUpdate)
                    return@addOnSuccessListener
                }

                Snackbar.make(viewOfLayout.findViewById(R.id.main), "현재 위치를 받아올 수 없습니다.", Snackbar.LENGTH_LONG).show();
            }
        } catch (e: SecurityException) {
            Log.e("GPS Error", "Security Exception: ${e.message}")
            Snackbar.make(viewOfLayout.findViewById(R.id.main), "현재 위치를 받아올 수 없습니다.", Snackbar.LENGTH_LONG).show()
        } catch (e: Exception) {
            Log.e("GPS Error", "Exception: ${e.message}")
        }
    }

    private fun requestMaker(){

        val visibleRegion = googleMap.projection.visibleRegion
        val latLngBounds = visibleRegion.latLngBounds

        val rightTop = latLngBounds.northeast // 우측 상단 좌표, 제일 높음
        val leftBottom = latLngBounds.southwest // 좌측 하단 좌표, 제일 낮음

        articleMarkerRequest?.cancel()
        articleMarkerRequest = NetworkManager.apiService.retrieveArticleMarker(
            leftBottom.latitude,
            leftBottom.longitude,
            rightTop.latitude,
            rightTop.longitude
        )
        articleMarkerRequest?.enqueue(object :
            Callback<ArticleMarkersResponse> {
            override fun onResponse(call: Call<ArticleMarkersResponse>, response: Response<ArticleMarkersResponse>) {
                if(call.isCanceled) {
                    return
                }

                if(!response.isSuccessful) {
                    Log.e("Marker Request Error", "HTTP CODE" + response.code().toString())
                    Snackbar.make(viewOfLayout.findViewById(R.id.main), "마커 요청에 실패했습니다.", Snackbar.LENGTH_LONG).show();
                    return
                }

                if(!response.body()!!.success) {
                    Log.e("Marker Request Error", response.body()!!.msg)
                    Snackbar.make(viewOfLayout.findViewById(R.id.main), "마커 요청에 실패했습니다.", Snackbar.LENGTH_LONG).show();
                    return
                }

                val list = response.body()!!.markers

                markers?.forEach {
                    it.remove()
                }
                //TODO 7월 9일 기존 마커 중 중복 마커는 제거 & 생성 방지

                markers = ArrayList()

                var count = 0

                list.forEach { item ->
                    val customMarkerIcon = BitmapDescriptorFactory.fromResource(R.drawable.location_without_shadow)

                    val markerOption = MarkerOptions()
                        .position(LatLng(item.lat, item.lng))
                        .icon(customMarkerIcon)

                    val marker = googleMap.addMarker(markerOption)

                    if(marker == null) {
                        return
                    }

                    marker.tag = item.articles

                    markers!!.add(marker)
                    count += item.articles.size
                }

                articleCount = count
                updateLocationText()
            }

            override fun onFailure(call: Call<ArticleMarkersResponse>, err: Throwable) {
                Log.e("Marker Request Error", err.toString())
                //Snackbar.make(viewOfLayout.findViewById(R.id.main), "마커 요청에 실패했습니다.", Snackbar.LENGTH_LONG).show();
            }
        })
    }

    fun updateLocationText() {
        val context = requireContext()

//        Log.e("Send latlng", googleMap.cameraPosition.target.latitude.toString() +
//                "," +
//                googleMap.cameraPosition.target.longitude.toString())

        val ai: ApplicationInfo = context.packageManager.getApplicationInfo(context.packageName, PackageManager.GET_META_DATA)

        ReverseGeocodingManager.apiService.getAddress(
            googleMap.cameraPosition.target.latitude.toString() +
                    "," +
                    googleMap.cameraPosition.target.longitude.toString(),
            ai.metaData.getString("com.google.android.geo.API_KEY")!!,
            Locale.getDefault().language).enqueue(object:Callback<AddressResponse>{
            override fun onResponse(call: Call<AddressResponse>, response: Response<AddressResponse>) {
                if(!response.isSuccessful) return

                val body = response.body()!!

                if(body.results.isEmpty()) {
                   return
                }

//                body.results.forEach { item ->
//                    Log.e("Item", item.formattedAddress)
//                    Log.e("Item Tag", item.types.joinToString(", "))
//                    item.addressComponents.forEach { item2 ->
//                        Log.e("Component", item2.longName + "  " + item2.types.joinToString(", "))
//                    }
//                }

                body.results.forEach {
                    if(it.types.contains("sublocality_level_1")) {
                        onGeocode(it)
                        return
                    }
                }
                body.results.forEach {
                    if(it.types.contains("sublocality_level_2")) {
                        onGeocode(it)
                        return
                    }
                }
                body.results.forEach {
                    if(it.types.contains("sublocality_level_3")) {
                        onGeocode(it)
                        return
                    }
                }
                body.results.forEach {
                    if(it.types.contains("sublocality_level_4")) {
                        onGeocode(it)
                        return
                    }
                }
                onGeocode(body.results[0])
            }

            override fun onFailure(call: Call<AddressResponse>, err: Throwable) {
                Log.e("GeoCode Failed", err.toString())
            }
        })
    }

    fun onGeocode(address: AddressResult) {
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

        val locationString = buildString {
            append(addressText)
            append(" 인근 ")
            append(getMapVisibleRadius())
            append("\n")
        }
        val endString = " 개의 제보가 올라왔어요."

        val spannable: Spannable = SpannableString(buildString {
            append(locationString)
            append(articleCount)
            append(endString)
        })

        spannable.setSpan(RelativeSizeSpan(0.6f), 0, locationString.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannable.setSpan(
            ForegroundColorSpan(Color.rgb(0, 156, 255)), locationString.length,
            (locationString.length + articleCount.toString().length), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        val currentDateTime = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")


        requireActivity().runOnUiThread {
            viewOfLayout.findViewById<TextView>(R.id.overview_text).text = spannable
            viewOfLayout.findViewById<TextView>(R.id.overview_time).text = buildString {
                append(currentDateTime.format(formatter))
                append(" 기준")
            }
        }
    }

    private fun onClickArticle(article: Article) {
        requireActivity().showDarkOverlay()
        bottomSheetFragment?.dismiss()

        NetworkManager.apiService.retrieveArticleDetail(article.uid).enqueue(object :
            Callback<ArticleDetailResponse> {
            override fun onResponse(call: Call<ArticleDetailResponse>, response: Response<ArticleDetailResponse>) {
                requireActivity().hideDarkOverlay()
                if(!response.isSuccessful){
                    Snackbar.make(viewOfLayout.findViewById(R.id.main), "게시글 정보를 불러올 수 없습니다.", Snackbar.LENGTH_SHORT).show()
                    return
                }

                val articleDetail = response.body()!!.article
                val comments = response.body()!!.comments


                val intent = Intent(
                    requireActivity(),
                    ArticleDetailActivity::class.java
                )
                intent.putExtra("article", articleDetail)
                intent.putParcelableArrayListExtra("comments", ArrayList(comments))
                startActivity(intent)
            }

            override fun onFailure(call: Call<ArticleDetailResponse>, err: Throwable) {
                requireActivity().hideDarkOverlay()
                Log.e("ArticleDetailActivity", "게시글 불러오기 실패$err")
            }
        })
    }

    override fun onResume() {
        super.onResume()
        if (this::locationRequest.isInitialized && ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest, executor, locationCallback)
        }

        requestMaker()
    }

    override fun onPause() {
        super.onPause()
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        this.googleMap = googleMap
        googleMap.setOnCameraIdleListener(this)
        googleMap.setOnMarkerClickListener(this)

        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        googleMap.uiSettings.isMyLocationButtonEnabled = false
        googleMap.isMyLocationEnabled = true;
    }

    override fun onCameraIdle() {
        requestMaker()
        //updateLocationText()
    }

    @Suppress("UNCHECKED_CAST")
    override fun onMarkerClick(marker: Marker): Boolean {
        requireActivity().showDarkOverlay() //요청 중 다크

        val tag = marker.tag ?: return false
        if(!(tag is List<*> && tag.all { it is String })) {
            return false
        }

        NetworkManager.apiService.retrieveArticleTimeline(tag as List<String>).enqueue(object :
            Callback<ArticleTimelineResponse> {
            override fun onResponse(call: Call<ArticleTimelineResponse>, response: Response<ArticleTimelineResponse>) {
                requireActivity().hideDarkOverlay()

                if(!response.isSuccessful) {
                    Snackbar.make(viewOfLayout.findViewById(R.id.main), "타임라인 요청에 실패했습니다.", Snackbar.LENGTH_LONG).show();
                    return
                }

                if(!response.body()!!.success) {
                    Log.e("Marker Request Error", response.body()!!.msg)
                    Snackbar.make(viewOfLayout.findViewById(R.id.main), "타임라인 요청에 실패했습니다.", Snackbar.LENGTH_LONG).show();
                    return
                }

                val list = response.body()!!.articles

                val intent = Intent(requireActivity(), TimelineActivity::class.java)
                intent.putParcelableArrayListExtra("articles", ArrayList(list))

                bottomSheetFragment = HotArticleSheetFragment(::onClickArticle, intent, list[0], marker.position.latitude, marker.position.longitude)
                bottomSheetFragment!!.show(requireActivity().supportFragmentManager, bottomSheetFragment!!.tag)
            }

            override fun onFailure(call: Call<ArticleTimelineResponse>, err: Throwable) {
                requireActivity().hideDarkOverlay()

                Log.e("Marker Request Error", err.toString())
                Snackbar.make(viewOfLayout.findViewById(R.id.main), "타임라인 요청에 실패했습니다.", Snackbar.LENGTH_LONG).show();
            }

        })
        return true
    }

    private fun getMapVisibleRadius(): String {
        val visibleRegion: VisibleRegion = googleMap.projection.visibleRegion

        val diagonalDistance = FloatArray(1)

        val farLeft = visibleRegion.farLeft
        val nearRight = visibleRegion.nearRight

        Location.distanceBetween(
            farLeft.latitude,
            farLeft.longitude,
            nearRight.latitude,
            nearRight.longitude,
            diagonalDistance
        )

        return formatDistance(diagonalDistance[0].toDouble())
    }

    fun formatDistance(distance: Double): String {
        return if (distance < 1000) {
            // 1000미터 미만일 때는 미터 단위로
            "%.2fm".format(distance)
        } else {
            // 1000미터 이상일 때는 킬로미터 단위로
            "%.2fkm".format(distance / 1000)
        }
    }
}
