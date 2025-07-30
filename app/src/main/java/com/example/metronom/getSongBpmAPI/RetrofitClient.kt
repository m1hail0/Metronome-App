package com.example.metronom.getSongBpmAPI
import com.example.metronom.getSongBpmAPI_Interface.GetSongBpmApi
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "https://api.getsongbpm.com/"

    val api: GetSongBpmApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(GetSongBpmApi::class.java)
    }
}


