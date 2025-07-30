package com.example.metronom

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.metronom.getSongBpmAPI.RetrofitClient
import com.example.metronom.song_preset.SongPreset
import com.example.metronom.song_preset.SongPresetRVAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Dispatcher


class SongPresetActivity : AppCompatActivity() {

    private lateinit var songPresetRV: RecyclerView
    private var list: MutableList<SongPreset> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_song_preset)

        songPresetRV = findViewById(R.id.recyclerView)
//        fetchData()

        fetchBpmForSong("Noche acosador") { songInfo ->
            if (songInfo != null){
                Log.d("SONG","Time Signature: ${songInfo.timeSignature}")

                val song = SongPreset(songInfo.title, songInfo.artist, songInfo.bpm, songInfo.timeSignature)
                list.add(song)
                setUpRV(list)
            } else {
                Log.d("SONG","ERROR")

            }
        }

    }



    private fun setUpRV(songPreset: List<SongPreset>){
        songPresetRV.adapter = SongPresetRVAdapter(songPreset)
        songPresetRV.layoutManager = LinearLayoutManager(this)
    }

    private fun fetchData(){
        for (i in 1..5){
            val d = SongPreset("Unforgiven$i", "Metallica", 120+i, "4/4")
            list.add(d)
        }
        setUpRV(list)
    }


    private fun fetchBpmForSong(songName: String, onResult: (SongInfo?) -> Unit){
        val apiKey = "e2bb214e7f8cbc6be4850f93f4301cd9"



        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.api.searchSong(apiKey, lookup = songName)
                val bpm = response.search.firstOrNull()?.tempo?.toIntOrNull()
                val title = response.search.firstOrNull()?.title.toString()
                val artist = response.search.firstOrNull()?.artist?.name.toString()
                val timeSignature = response.search.firstOrNull()?.timeSig.toString()

                var songInfo: SongInfo? = null
                if(bpm != null && title != null && artist != null && timeSignature != null){
                    songInfo = SongInfo(bpm = bpm, title = title, artist = artist, timeSignature = timeSignature)
                }

                Log.d("API_RESPONSE", response.toString())

                withContext(Dispatchers.Main){
                    onResult(songInfo)
                }
            } catch (e: Exception){
                e.printStackTrace()
                Log.e("API_RESPONSE", "Error during API call", e)

                withContext(Dispatchers.Main){
                    onResult(null)
                }
            }
        }
    }

}

data class SongInfo(
    val title: String,
    val artist: String,
    val bpm: Int,
    val timeSignature: String
)