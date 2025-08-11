package com.example.metronom.song_preset

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.metronom.R
import com.example.metronom.getSongBpmAPI.RetrofitClient
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions


class SongPresetActivity : AppCompatActivity() {

    private lateinit var songPresetRV: RecyclerView
    private var list: MutableList<SongPreset> = mutableListOf()

    //  Firebase firestore baza podataka
    private val user = Firebase.auth.currentUser
    private var songCollectionRef = Firebase.firestore
    private var recentSongsList: MutableList<SongPreset> = mutableListOf()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_song_preset)

        songPresetRV = findViewById(R.id.recyclerView)

        val searchView = findViewById<SearchView>(R.id.searchView)

        retrieveRecentSongs()



        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {

                //Default vrednost
                val songName = query ?: "Enter Sandman"

                if (query != null) {
                    fetchBpmForSong(songName) { songList ->
                        if (songList.isNotEmpty()) {

                            list.clear()

                            songList.forEach { song ->
                                Log.d(
                                    "SONG",
                                    "${song.title} by ${song.artist} - ${song.bpm} (${song.timeSignature})"
                                )
                                list.add(
                                    SongPreset(
                                        song.title,
                                        song.artist,
                                        song.bpm,
                                        song.timeSignature
                                    )
                                )
                            }
                            setUpRV(list)


                        } else {
                            Log.d("SONG", "ERROR")
                            Toast.makeText(
                                this@SongPresetActivity,
                                "No song found",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }
        })

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Vaša logika za "back" dugme
                finish()
            }
        })
    }


    private fun setUpRV(songPreset: List<SongPreset>) {
        songPresetRV.adapter = SongPresetRVAdapter(songPreset)
        songPresetRV.layoutManager = LinearLayoutManager(this)
    }

    private fun fetchBpmForSong(songName: String, onResult: (List<SongPreset>) -> Unit) {
        val apiKey = "e2bb214e7f8cbc6be4850f93f4301cd9"

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.api.searchSong(apiKey, lookup = songName)


                val songList = response.search.mapNotNull { item ->
                    val bpm = item.tempo?.toIntOrNull()
                    val title = item.title
                    val artist = item.artist.name
                    val timeSignature = item.timeSig

                    if (bpm != null && title != null && artist != null && timeSignature != null) {
                        SongPreset(
                            bpm = bpm,
                            title = title,
                            artist = artist,
                            timeSignature = timeSignature
                        )
                    } else {
                        null
                    }
                }
                Log.d("API_RESPONSE", response.toString())
                Log.d("API_RESPONSE2", songList.toString())

                withContext(Dispatchers.Main) {
                    onResult(songList)
                }

            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("API_RESPONSE", "Error during API call", e)
                Log.e("API_CALL_ERROR", "Exception: ${e.localizedMessage}", e)


                withContext(Dispatchers.Main) {
                    onResult(emptyList())
                }
            }
        }
    }

    private fun retrieveRecentSongs() = CoroutineScope(Dispatchers.IO).launch {
        try {
            if (user != null) {
                val querySnapshot = songCollectionRef.collection("users")
                    .document(user.uid)
                    .collection("songs")
                    .orderBy("timeStamp", Query.Direction.DESCENDING)
                    .get()
                    .await()
                Log.d("Firestore", "Found ${querySnapshot.size()} songs")

                recentSongsList.clear()
                for (document in querySnapshot.documents) {
                    val song = document.toObject<SongPreset>()
                    if (song != null) {
                        //uvek postoji jer je u drugoj klasi ubacena u Firestore pre nego sto je doslo do ovde. Stari problem
                        recentSongsList.add(song)
                        Log.d("pesma", "${song}")
                    }
                }
                withContext(Dispatchers.Main) {
                    if (recentSongsList.isNotEmpty()) {
                        setUpRV(recentSongsList)
                        Log.d("pesma/recent", "$recentSongsList")
                    }
                }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(this@SongPresetActivity, e.message, Toast.LENGTH_LONG).show()
                Log.d("greska", "${e.message}")
            }
        }
    }


}