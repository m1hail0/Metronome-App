package com.example.metronom.dataClassAPI

import com.google.gson.annotations.SerializedName

data class SongSearchResponse(
    @SerializedName("search") val search: List<SongItem> = emptyList()
)

data class SongItem(
    @SerializedName("id") val id: String,
    @SerializedName("title") val title: String,
    @SerializedName("artist") val artist: Artist,
    @SerializedName("tempo") val tempo: String,
    @SerializedName("time_sig") val timeSig: String
)

data class Artist(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String
)
