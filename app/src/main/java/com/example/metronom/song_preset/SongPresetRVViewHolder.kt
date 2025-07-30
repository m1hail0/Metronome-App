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

class SongPresetRVViewHolder(view: View, private val listener: (()->Unit)? = null) : RecyclerView.ViewHolder(view){

    fun bind(item: SongPreset){
        val songName = itemView.findViewById<TextView>(R.id.song_name_text)
        val bandName = itemView.findViewById<TextView>(R.id.band_name)
        val timeSignature = itemView.findViewById<TextView>(R.id.time_signature)
        val bpmCount = itemView.findViewById<TextView>(R.id.bpm_number)
        val btnChoose = itemView.findViewById<Button>(R.id.choose_button)

        songName.text = item.songName
        bandName.text = item.bandName
        bpmCount.text = item.bpmCount.toString()
        timeSignature.text = item.timeSignature


        val name = item.songName
        val band = item.bandName
        val bpm = item.bpmCount
        val timeSig = item.timeSignature
        


        btnChoose.setOnClickListener{
            val intent = Intent(itemView.context, MetronomeActivity::class.java)
            intent.putExtra("song_name", name)
            intent.putExtra("band_name", band)
            intent.putExtra("bpm", bpm)
            intent.putExtra("time_signature", timeSig)

            itemView.context.startActivity(intent)
            listener?.invoke()
        }
    }
}