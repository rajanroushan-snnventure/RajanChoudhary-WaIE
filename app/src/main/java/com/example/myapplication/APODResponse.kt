package com.example.myapplication.ui

import android.content.SharedPreferences
import com.google.gson.annotations.SerializedName
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

data class APODResponse(
    @field:SerializedName("title")
    val title: String,
    @field:SerializedName("explanation")
    val explanation: String,
    @field:SerializedName("url")
    val url: String,
    @field:SerializedName("media_type")
    val mediaType: String
)

interface NASAAPODService {
    @GET("planetary/apod")
    fun getAPOD(@Query("api_key") apiKey: String): Call<APODResponse>
}

object RetrofitClient {
    private const val BASE_URL = "https://api.nasa.gov/"

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val service: NASAAPODService = retrofit.create(NASAAPODService::class.java)
}

