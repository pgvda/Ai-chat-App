package com.cometchat.pro.androiduikit.ComponentFragments


import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface WitAiApi {
    @GET("message")
    suspend fun getMessage(
        @Query("q") query: String,
        @Header("Authorization") authorization: String
    ): WitAiResponse

    // Add other API methods if needed
}
