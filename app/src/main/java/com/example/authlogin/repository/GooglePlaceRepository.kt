package com.example.authlogin.repository

import android.util.Log
import com.example.authlogin.model.Snack
import com.example.authlogin.model.SnackCollection
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

// Define Retrofit interface
interface SnackApi {
    @GET("nearby-places") // Replace with your actual endpoint
    fun getSnackItems(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double
    ): Call<List<Snack>>
}

