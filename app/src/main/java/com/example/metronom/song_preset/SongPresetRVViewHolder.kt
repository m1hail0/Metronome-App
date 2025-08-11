package com.example.metronom.song_preset

import android.content.Intent
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.metronom.MetronomeActivity
import com.example.metronom.R
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class SongPresetRVViewHolder(view: View, private val listener: (() -> Unit)? = null) :
    RecyclerView.ViewHolder(view) {

    //  Firebase firestore baza podataka
    private val user = Firebase.auth.currentUser
    private var songCollectionRef = Firebase.firestore

    fun bind(item: SongPreset) {
        val songName = itemView.findViewById<TextView>(R.id.song_name_text)
        val bandName = itemView.findViewById<TextView>(R.id.band_name)
        val timeSignature = itemView.findViewById<TextView>(R.id.time_signature)
        val bpmCount = itemView.findViewById<TextView>(R.id.bpm_number)
        val btnChoose = itemView.findViewById<Button>(R.id.choose_button)

        songName.text = item.title
        bandName.text = item.artist
        bpmCount.text = item.bpm.toString()
        timeSignature.text = item.timeSignature


        val name = item.title
        val artist = item.artist
        val bpm = item.bpm
        val timeSig = item.timeSignature



        btnChoose.setOnClickListener {
            val intent = Intent(itemView.context, MetronomeActivity::class.java)
            intent.putExtra("song_name", name)
            intent.putExtra("band_name", artist)
            intent.putExtra("bpm", bpm)
            intent.putExtra("time_signature", timeSig)

            itemView.context.startActivity(intent)
            listener?.invoke()

            //Cuvanje pesme na Firebase Firestore bazu podataka
            if (name != "none" && name != "N/A" && artist != "none" && artist != "N/A" && bpm != 0) {
                val song = SongPreset(name, artist, bpm, timeSig, com.google.firebase.Timestamp.now())
                saveSong(song)
            }
        }


    }



    private fun saveSong(song: SongPreset) = CoroutineScope(Dispatchers.IO).launch {
        try {
            if (user != null) {
                if (containsSong(song)){
                    val songUpdate = SongPreset("", "", 0, "", com.google.firebase.Timestamp.now())
                    updateSameSong(song, getNewSongMap(songUpdate))
                }else {
                    songCollectionRef
                        .collection("users")
                        .document(user.uid)
                        .collection("songs")
                        .add(song)
                }
            }

        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Log.d("GRESKA", "${e.message}")
            }
        }
    }

    private fun getNewSongMap(song: SongPreset): Map<String, Any> {
        val title = song.title
        val artist = song.artist
        val bpm = song.bpm
        val timeSignature = song.timeSignature
        val timeStamp = song.timeStamp
        val map = mutableMapOf<String, Any>()
        if (title.isNotEmpty()) {
            map["title"] = title
        }
        if (artist.isNotEmpty()) {
            map["artist"] = artist
        }
        if (bpm != 0) {
            map["bpm"] = bpm
        }
        if (timeSignature.isNotEmpty()) {
            map["timeSignature"] = timeSignature
        }
        if (timeStamp != null) {
            map["timeStamp"] = timeStamp
        }

        return map
    }

    private fun updateSameSong(song: SongPreset, newSongMap: Map<String, Any>) =
        CoroutineScope(Dispatchers.IO).launch {
            if (user != null) {
                val songQuery =
                    songCollectionRef.collection("users").document(user.uid).collection("songs")
                        .whereEqualTo("title", song.title)
                        .whereEqualTo("artist", song.artist)
                        .whereEqualTo("bpm", song.bpm)
                        .whereEqualTo("timeSignature", song.timeSignature)
                        .get()
                        .await()
                if (songQuery.documents.isNotEmpty()) {
                    for (document in songQuery) {
                        try {
                            songCollectionRef.collection("users").document(user.uid)
                                .collection("songs").document(document.id).set(
                                    newSongMap,
                                    SetOptions.merge()
                                ).await()
                        } catch (e: Exception) {
                            withContext(Dispatchers.Main) {
                                Log.d(
                                    "FIRE",
                                    "doc.id=${document.id}, doc.path=${document.reference.path}"
                                )

                                Log.d("greskaUpdateSong", e.message.toString())
                            }
                        }
                    }
                } else {
                    Log.d("greska","Error with changing timeStamp of an again used recent song.")
                }
            }

        }

    private suspend fun containsSong(song: SongPreset): Boolean {
        if (user != null) {
            val songQuery = songCollectionRef
                .collection("users")
                .document(user.uid)
                .collection("songs")
                .whereEqualTo("title", song.title)
                .whereEqualTo("artist", song.artist)
                .whereEqualTo("bpm", song.bpm)
                .whereEqualTo("timeSignature", song.timeSignature)
                .get()
                .await()

            return songQuery.documents.isNotEmpty()
        }
        return false
    }
}