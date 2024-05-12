package com.example.authlogin.repository

import android.util.Log
import com.example.authlogin.model.Snack
import com.example.authlogin.model.SnackCollection
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

// Define Retrofit interface
interface SnackApi {
    @GET("/nearby-places") // Replace with your actual endpoint
    suspend fun getSnackItems(): List<Snack>
}

