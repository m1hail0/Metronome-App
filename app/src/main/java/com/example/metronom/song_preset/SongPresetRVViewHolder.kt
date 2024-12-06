package com.example.metronom.song_preset

import android.app.Activity
import android.content.Intent
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.metronom.MetronomeActivity
import com.example.metronom.R
import com.example.metronom.SongPresetActivity

class SongPresetRVViewHolder(view: View) : RecyclerView.ViewHolder(view){

    fun bind(item: SongPreset){
        val songName = itemView.findViewById<TextView>(R.id.song_name_text)
        val bandName = itemView.findViewById<TextView>(R.id.band_name)
        val duration = itemView.findViewById<TextView>(R.id.song_duration)
        val bpmCount = itemView.findViewById<TextView>(R.id.bpm_number)
        val btnChoose = itemView.findViewById<Button>(R.id.choose_button)

        songName.text = item.songName
        bandName.text = item.bandName
        duration.text = item.duration.toString()
        bpmCount.text = item.bpmCount.toString()

        val name = item.songName
        val band = item.bandName
        val bpm = item.bpmCount
        itemView.setOnClickListener(){
            val intent = Intent(itemView.context, SongPresetActivity::class.java)
            intent.putExtra("key_data", name)
            itemView.context.startActivity(intent)
        }

        btnChoose.setOnClickListener{
            val intent = Intent(itemView.context, MetronomeActivity::class.java)
            intent.putExtra("song_name", name)
            intent.putExtra("band_name", band)
            intent.putExtra("bpm", bpm)

            itemView.context.startActivity(intent)
            (itemView.context as Activity).finish()
        }
    }
}