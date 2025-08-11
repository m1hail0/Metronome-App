package com.example.metronom.song_preset
import com.google.firebase.Timestamp
data class SongPreset(
    val title: String = "",
    val artist: String = "",
    val bpm: Int = 0,
    val timeSignature: String = "",
    val timeStamp: Timestamp? = null
) {
}