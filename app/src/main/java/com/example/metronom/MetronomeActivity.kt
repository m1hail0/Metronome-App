package com.example.metronom


import android.content.Intent
import android.content.res.Resources
import android.media.MediaPlayer
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.Spinner
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class MetronomeActivity : AppCompatActivity() {

    //Job promenljive za pustanje klika i vizuelnog prikazivanja tempa
    private var loopJob: Job? = null
    private var job2: Job? = null

    //Lista ID-eva od bpm View elemenata
    private val idViewList: MutableList<Int> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?)  {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()


        setContentView(R.layout.metronom)

        val seekBar = findViewById<SeekBar>(R.id.bpmSeekBar)
        val bpmEditText = findViewById<EditText>(R.id.bpmText)
        val btnStartStop = findViewById<ImageButton>(R.id.startStopButton)
        val btnMinusOne = findViewById<ImageButton>(R.id.minus_button)
        val btnPlusOne = findViewById<ImageButton>(R.id.plus_button)
        val btnSongPreset = findViewById<Button>(R.id.song_preset_button)
        val songName = findViewById<TextView>(R.id.song_name_text_view)
        val bandName = findViewById<TextView>(R.id.band_name_text_view)
        val spinner = findViewById<Spinner>(R.id.time_signature_spinner)

        var tempo: Int

        val tempos = resources.getStringArray(R.array.timeSignatures)
        val adapter = ArrayAdapter(this,android.R.layout.simple_spinner_item,tempos)
        spinner.adapter = adapter

        tempo = tempos[3].toInt()
        Log.i("spinner","posle tempo: $tempo")
        addViews(tempo)

        //Dodeljivanje podataka iz RecyclerViewa
        songName.text = intent.getStringExtra("song_name") ?: "Custom"
        bandName.text = intent.getStringExtra("band_name") ?: "/"
        val bpm = intent.getIntExtra("bpm",120)
        bpmEditText.setText(bpm.toString())


        val mediaPlayer = MediaPlayer.create(this, R.raw.click)
        var isPlaying = false

        seekBar.progress = bpmEditText.text.toString().toInt()

        spinner.setSelection(3)

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(
                seekBar: SeekBar?,
                progress: Int,
                fromUser: Boolean
            ) {
                bpmEditText.setText(progress.toString())

            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }

        })

        bpmEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
            override fun afterTextChanged(s: Editable?) {
                if(s.toString() == "")
                {
                    seekBar.progress=20
                }
                else
                {
                    seekBar.progress = s.toString().toInt()
                }
            }
        })


        btnStartStop.setOnClickListener {

            if(isPlaying)
            {
                isPlaying= false
                loopJob?.cancel()
                btnStartStop.setImageResource(R.drawable.play_button)
            }
            else
            {
                isPlaying=true
                btnStartStop.setImageResource(R.drawable.stop_button)
                loopJob = CoroutineScope(Dispatchers.Default).launch{
                    while (isPlaying){
                        for(i in 0..<idViewList.size) {
                            val size = idViewList.size
                            mediaPlayer.start()
                            Log.i("brojac","i:$i---size:$size")
                            playBpmView(i)
                            delay(getIntervalInMilliseconds(seekBar))
                            job2?.cancel()
                        }
                    }
                }
            }

        }

        btnMinusOne.setOnClickListener{
            val currentBpm = seekBar.progress
            if (currentBpm!=20) {
                bpmEditText.setText((currentBpm - 1).toString())
            }
        }

        btnPlusOne.setOnClickListener{
            val currentBpm = seekBar.progress
            if (currentBpm!=240) {
                bpmEditText.setText((currentBpm + 1).toString())
            }
        }

        btnSongPreset.setOnClickListener{
            val i = Intent(this@MetronomeActivity, SongPresetActivity::class.java)
            startActivity(i)
        }

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener
        {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                idViewList.clear()
                tempo = tempos[position].toInt()
                addViews(tempo)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

        }

    }



    private fun getIntervalInMilliseconds(seekBar: SeekBar): Long{
        var interval: Double = 60.0/getCurrentBpm(seekBar).toDouble()
        interval *= 1000.0
        Log.i("interval","$interval")

        return interval.toLong()
    }

    private fun getCurrentBpm(seekBar: SeekBar):Long{
        return seekBar.progress.toLong()
    }

    private fun playBpmView(i: Int){
        job2 = CoroutineScope(Dispatchers.IO).launch {
            val view = findViewById<View>(idViewList[i])
            view.setBackgroundResource(R.drawable.bpm_square_active)
            delay(80)
            view.setBackgroundResource(R.drawable.bpm_square_empty)
        }
    }

    private fun addViews(maxViewNumber: Int)
    {
        val bpmLayout = findViewById<LinearLayout>(R.id.bpmImageLayout)
        bpmLayout.removeAllViews()
        for(i in 1..maxViewNumber)
        {
            val newView = createBpmView(i,maxViewNumber)
            bpmLayout.addView(newView)
        }
    }

    private fun createBpmView(viewIndex: Int, maxViewNumber: Int): View {
        val context = this // Ili koristite odgovarajući Context ako ste u drugoj klasi

        // Kreiranje View-a
        var generatedId: Int
        val view = View(context).apply {
            generatedId = View.generateViewId()// Generiše jedinstveni ID za View
            id = generatedId
            idViewList.add(generatedId)
            setBackgroundResource(R.drawable.bpm_square_empty) // Postavljanje pozadine
        }

        // Postavljanje LayoutParams (LinearLayout.LayoutParams)
        val layoutParams = LinearLayout.LayoutParams(0, 120.dpToPx()).apply {
            weight = 1f // Ekvivalent android:layout_weight="1"
            if(viewIndex==1){
                marginEnd = 5.dpToPx()
            }
            else if (viewIndex==maxViewNumber)
            {
                marginStart = 5.dpToPx()
            }
            else
            {
                marginStart = 5.dpToPx()
                marginEnd = 5.dpToPx()
            }
        }
        view.layoutParams = layoutParams

        return view
    }


    private fun Int.dpToPx(): Int {
        return (this * Resources.getSystem().displayMetrics.density).toInt()
    }
}