package com.example.dualscreen.`interface`
import com.example.dualscreen.response.DogImageResponses
import retrofit2.http.GET

interface DogApi {
    @GET("breeds/image/random/5")
    suspend fun getRandomDogImages(): retrofit2.Response<DogImageResponses>
}