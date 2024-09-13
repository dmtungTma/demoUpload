package com.example.dualscreen.response

import com.google.gson.annotations.SerializedName

data class DogImageResponse(
    @SerializedName("message") val imageUrl: String,
    @SerializedName("status") val status: String
)
data class DogImageResponses(
    @SerializedName("message") val message: List<String>,
    @SerializedName("status") val status: String
)