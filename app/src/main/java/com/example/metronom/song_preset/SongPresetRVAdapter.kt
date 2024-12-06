package com.example.metronom.song_preset

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.metronom.R

class SongPresetRVAdapter(private var itemList: List<SongPreset>) : RecyclerView.Adapter<SongPresetRVViewHolder>(){
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongPresetRVViewHolder {
        return SongPresetRVViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.preset_rw_layout,parent,false))
    }

    override fun getItemCount()= itemList.size

    override fun onBindViewHolder(holder: SongPresetRVViewHolder, position: Int) {
        holder.bind(itemList[position])
    }
}