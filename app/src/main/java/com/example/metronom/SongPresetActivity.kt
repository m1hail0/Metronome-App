package com.example.metronom

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.metronom.song_preset.SongPreset
import com.example.metronom.song_preset.SongPresetRVAdapter

class SongPresetActivity : AppCompatActivity() {

    private lateinit var songPresetRV: RecyclerView
    private var lista: MutableList<SongPreset> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_song_preset)

        songPresetRV = findViewById<RecyclerView>(R.id.recyclerView)
        fetchData()
    }

    private fun setUpRV(songPreset: List<SongPreset>){
        songPresetRV.adapter = SongPresetRVAdapter(songPreset)
        songPresetRV.layoutManager = LinearLayoutManager(this)
    }

    private fun fetchData(){
        for (i in 1..5){
            val d = SongPreset("Unforgiven$i", "Metallica", 360, 120+i)
            lista.add(d)
        }
        setUpRV(lista)
    }
}