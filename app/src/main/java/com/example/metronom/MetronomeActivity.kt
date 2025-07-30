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
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.ArrayDeque
import java.util.Deque
import java.util.Queue
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.TimeSource.Monotonic.ValueTimeMark
import kotlin.time.TimeSource.Monotonic.markNow


class LimitDeque<Long>(private val limitSize: Int): ArrayDeque<Long>(){
    override fun push(p0: Long) {
        if (this.size >= limitSize) pollLast()
        super.push(p0)
    }
}
class MetronomeActivity : AppCompatActivity() {

    //Job promenljive za pustanje klika i vizuelnog prikazivanja tempa
    private var loopJob: Job? = null
    private var job2: Job? = null

    //Lista ID-eva od bpm View elemenata
    private val idViewList: MutableList<Int> = mutableListOf()


    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreate(savedInstanceState: Bundle?)  {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()



        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Vaša logika za "back" dugme
                onBackPressedDispatcher.onBackPressed()
                loopJob?.cancel()
            }
        })

        setContentView(R.layout.metronom)

        val seekBar = findViewById<SeekBar>(R.id.bpmSeekBar)
        val bpmEditText = findViewById<EditText>(R.id.bpmText)
        val btnStartStop = findViewById<ImageButton>(R.id.startStopButton)
        val btnMinusOne = findViewById<ImageButton>(R.id.minus_button)
        val btnPlusOne = findViewById<ImageButton>(R.id.plus_button)
        val btnSongPreset = findViewById<Button>(R.id.song_preset_button)
        val btnTapBpm = findViewById<Button>(R.id.tapButton)
        val songName = findViewById<TextView>(R.id.song_name_text_view)
        val bandName = findViewById<TextView>(R.id.band_name_text_view)
        val timeSignatureSpinner = findViewById<Spinner>(R.id.time_signature_spinner)

        var timeSignature: Int

        val timeSignatureArray = resources.getStringArray(R.array.timeSignatures)
        val adapter = ArrayAdapter(this,R.layout.spinner_list,timeSignatureArray)
        adapter.setDropDownViewResource(R.layout.spinner_list)

        timeSignatureSpinner.adapter = adapter
        timeSignatureSpinner.setSelection(3)



        //Dodeljivanje podataka iz RecyclerViewa
        songName.text = intent.getStringExtra("song_name") ?: "none"
        bandName.text = intent.getStringExtra("band_name") ?: "none"
        val bpm = intent.getIntExtra("bpm",120)
        bpmEditText.setText(bpm.toString())


        //Dodeljivanje time_signature vrednosti spinneru iz RecyclerViewa
        val timeSignatureString: String = intent.getStringExtra("time_signature") ?: "3"
        if(timeSignatureString!="3" && timeSignatureString.contains("/")){
            //Potencijalna greska ako se dobije vrednost manje od 0, ali ne bi nikako trebalo to da se desi. Update:dodao sam else
            timeSignature = ((timeSignatureString.subSequence(0, timeSignatureString.indexOf("/"))).toString().toInt() - 1)
        }else if(timeSignatureString=="3"){
            //Ako nema prosledjene vrednosti timeSignature ce dobiti default vrednost 4 (pod indeksom 3)
            timeSignature = timeSignatureArray[timeSignatureString.toInt()].toInt()
        }else{
            timeSignature = 0
        }

        Log.i("spinner","posle timeSignature: $timeSignature")
        addViews(timeSignature)




        val mediaPlayer = MediaPlayer.create(this, R.raw.click)
        var isPlaying = false

        seekBar.progress = bpmEditText.text.toString().toInt()



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
                            //Sprecava crash ukoliko je novi uneseni broj tokom rada manji od prethodnog :)
                            if(i>=idViewList.size)
                                break
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
            loopJob?.cancel()
            finish()
        }

//      Prvo mereno vreme
        var firstTimeMark: ValueTimeMark? = null
        var elapsedDuration: Duration?

        var durationSum: Long = 0
        var durationAverage: Double = 0.0

        var finalBpm: Int

        val durationDeque: Deque<Long> = LimitDeque(4)
        btnTapBpm.setOnClickListener{

            if (firstTimeMark == null) {
                firstTimeMark = markNow()
            }
            else {
                elapsedDuration = firstTimeMark!!.elapsedNow()
                durationDeque.push(elapsedDuration!!.toLong(DurationUnit.MILLISECONDS))
                durationSum = 0
                for (elem in durationDeque)
                {
                    durationSum += elem
                }
//                Prosek izmedju 4 klika
                if(durationDeque.size == 4) {

                    durationAverage = durationSum.toDouble() / 4
                    finalBpm = (1000/durationAverage*60).toInt()
                    seekBar.progress = finalBpm
                }
            }
            firstTimeMark = markNow()
        }

        timeSignatureSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener
        {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                idViewList.clear()
                timeSignature = timeSignatureArray[position].toInt()
                addViews(timeSignature)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

        }

    }

    private fun tapAverage(queue: Queue<Long> ): Int {
        val avg: Double = queue.toList().average()
        return (avg/1000).toInt()
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

//    private fun tapBpmButton(btn: Button, queue: Queue<TimeSource.Monotonic.ValueTimeMark>) {
//         val timeSource: TimeSource.Monotonic
//         val mark = timeSource.markNow()
//         queue.add()
//    }
}