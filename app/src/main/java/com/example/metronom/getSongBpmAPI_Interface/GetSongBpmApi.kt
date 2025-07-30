package com.example.metronom.getSongBpmAPI_Interface

import com.example.metronom.dataClassAPI.SongSearchResponse
import retrofit2.http.GET
import retrofit2.http.Query


interface GetSongBpmApi {

    @GET("search/")
    suspend fun searchSong(
        @Query("api_key") apiKey: String,
        @Query("type") type: String = "song",
        @Query("lookup") lookup: String
    ): SongSearchResponse
}