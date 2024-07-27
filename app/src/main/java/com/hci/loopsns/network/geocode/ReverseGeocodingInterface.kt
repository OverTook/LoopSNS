package com.hci.loopsns.network.geocode

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface ReverseGeocodingInterface {

    @GET("geocode/json")
    fun getAddress(
        @Query("latlng") latlng: String,
        @Query("key") apiKey: String,
        @Query("language") language: String
        //@Query("region") region: String
    ): Call<AddressResponse>
}