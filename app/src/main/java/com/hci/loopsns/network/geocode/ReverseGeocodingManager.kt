package com.hci.loopsns.network.geocode

import com.hci.loopsns.network.NetworkInterface
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ReverseGeocodingManager {

    private const val BASE_URL = "https://maps.googleapis.com/maps/api/"

    private var retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    var apiService: ReverseGeocodingInterface = retrofit.create(ReverseGeocodingInterface::class.java)

}