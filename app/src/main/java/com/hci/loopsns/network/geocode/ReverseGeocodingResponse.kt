package com.hci.loopsns.network.geocode

import com.google.gson.annotations.SerializedName

data class AddressResponse(
    val results: List<AddressResult>,
    val status: String
)

data class AddressResult(
    @SerializedName("address_components")
    val addressComponents: List<AddressComponent>,
    @SerializedName("formatted_address")
    val formattedAddress: String,
    @SerializedName("place_id")
    val placeId: String,
    val types: List<String>
)

data class AddressComponent(
    @SerializedName("long_name")
    val longName: String,
    @SerializedName("short_name")
    val shortName: String,
    val types: List<String>
)