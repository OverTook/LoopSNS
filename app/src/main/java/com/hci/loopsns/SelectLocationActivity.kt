package com.hci.loopsns

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.afollestad.materialdialogs.MaterialDialog
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.hci.loopsns.network.AddressResponse
import com.hci.loopsns.network.AddressResult
import com.hci.loopsns.network.NetworkManager
import com.hci.loopsns.storage.NightMode
import com.hci.loopsns.storage.SettingManager
import com.yariksoffice.lingver.Lingver
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class SelectLocationActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnCameraMoveStartedListener, GoogleMap.OnCameraIdleListener, View.OnClickListener {

    private var x: Double = 0.0
    private var y: Double = 0.0

    private lateinit var currentMarker: ImageView
    private lateinit var googleMap: GoogleMap

    private lateinit var submitButton: Button
    private lateinit var locationText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        animationInit()
        setContentView(R.layout.activity_select_location)

        submitButton = findViewById(R.id.submit)
        submitButton.setOnClickListener(this)

        locationText = findViewById(R.id.locationEditText)
        currentMarker = findViewById(R.id.current_marker)

        val mapFragment = (supportFragmentManager.findFragmentById(R.id.googleMap) as SupportMapFragment)
        mapFragment.getMapAsync(this)

        this.x = intent.getDoubleExtra("x", 0.0)
        this.y = intent.getDoubleExtra("y", 0.0)
    }


    fun animationInit() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            overrideActivityTransition(Activity.OVERRIDE_TRANSITION_OPEN, R.anim.enter_from_right, R.anim.exit_to_left)
        } else {
            overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left)
        }
    }

    override fun finish() {
        super.finish()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            overrideActivityTransition(Activity.OVERRIDE_TRANSITION_CLOSE, R.anim.enter_from_left, R.anim.exit_to_right)
        } else {
            overridePendingTransition(R.anim.enter_from_left, R.anim.exit_to_right)
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        this.googleMap = googleMap

        val cameraUpdate = CameraUpdateFactory.newLatLngZoom(LatLng(x, y), 15f)
        googleMap.moveCamera(cameraUpdate)

        googleMap.setMaxZoomPreference(16.9F)

        googleMap.setMapStyle(
            MapStyleOptions.loadRawResourceStyle(
                this, when(SettingManager.getInstance().getNightMode()) {
                    NightMode.NIGHT -> R.raw.google_map_night
                    NightMode.DAY -> R.raw.google_map_day
                    else -> {
                        when(SettingManager.getInstance().isSystemNightMode(this)) {
                            true -> R.raw.google_map_night
                            else -> R.raw.google_map_day
                        }
                    }
                }
            )
        )

        googleMap.setOnCameraIdleListener(this)
        googleMap.setOnCameraMoveStartedListener(this)
    }

    override fun onCameraIdle() {
        getLocation()

        currentMarker.clearAnimation()

        currentMarker.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(100)
            .start()
    }

    override fun onCameraMoveStarted(i: Int) {
        submitButton.isClickable = false

        currentMarker.clearAnimation()

        currentMarker.animate()
            .alpha(0.6f)
            .translationY(-20f)
            .setDuration(100)
            .start()
    }

    fun getLocation() {
        NetworkManager.apiService.getAddress(
            "${googleMap.cameraPosition.target.latitude},${googleMap.cameraPosition.target.longitude}",
            Lingver.getInstance().getLocale().language
        ).enqueue(object: Callback<AddressResponse> {
            override fun onResponse(call: Call<AddressResponse>, response: Response<AddressResponse>) {
                submitButton.isClickable = true

                if(!response.isSuccessful) return

                val body = response.body()!!

                if(body.results.isEmpty()) {
                    return
                }

                body.results.forEach {
                    if(it.types.contains("sublocality_level_4")) {
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
                    if(it.types.contains("sublocality_level_2")) {
                        onGeocode(it)
                        return
                    }
                }
                body.results.forEach {
                    if(it.types.contains("sublocality_level_1")) {
                        onGeocode(it)
                        return
                    }
                }
                onGeocode(body.results[0])
            }

            override fun onFailure(call: Call<AddressResponse>, err: Throwable) {
                submitButton.isClickable = true
                Log.e("EditLocationBottomSheet Geocoding Failed", err.toString())
            }

        })
    }

    fun onGeocode(address: AddressResult) {
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

        runOnUiThread {
            locationText.text = addressText.trim()
        }
    }

    override fun onClick(view: View?) {
        if(view == null) return

        when(view.id) {
            R.id.submit -> {
                MaterialDialog(this).show {
                    title(R.string.location_edit_confirm_head)
                    message(text = buildString {
                        append(getString(R.string.location_edit_confirm_body))
                        append("\n")
                        append(locationText.text)
                    })
                    positiveButton(R.string.location_edit_confirm_yes) { _ ->
                        val intent = Intent()
                        intent.putExtra("x", googleMap.cameraPosition.target.latitude)
                        intent.putExtra("y", googleMap.cameraPosition.target.longitude)
                        intent.putExtra("location", locationText.text.toString())
                        setResult(RESULT_OK, intent)
                        finish()
                    }
                    negativeButton(R.string.location_edit_confirm_no) { dialog ->
                        dialog.dismiss()
                    }
                }
            }
            R.id.backButton -> {
                finish()
            }
        }
    }
}