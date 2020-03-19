package com.rohanbojja.audient.ui.home

import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Bundle
import android.os.Environment.DIRECTORY_DOWNLOADS
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.FileDataPart
import com.github.squti.androidwaverecorder.WaveRecorder
import com.google.firebase.ml.common.modeldownload.FirebaseModelDownloadConditions
import com.google.firebase.ml.common.modeldownload.FirebaseModelManager
import com.google.firebase.ml.custom.*
import com.rohanbojja.audient.R
import kotlinx.android.synthetic.main.fragment_home.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.io.File


class HomeFragment : Fragment() {
    private var output: String? = null
    private lateinit var waveRecorder: WaveRecorder
    private var state: Boolean = false
    private var recordingStopped: Boolean = false
    private lateinit var homeViewModel: HomeViewModel
    private fun startRecording() {
        state = true
        waveRecorder.startRecording()
    }

    private fun stopRecording(){
        Log.d("Husky","STATE: ${state}")
        state = false
        waveRecorder.stopRecording()
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        homeViewModel =
            ViewModelProviders.of(this).get(HomeViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_home, container, false)
        val textView: TextView = root.findViewById(R.id.text_home)
        homeViewModel.text.observe(this, Observer {
            textView.text = it
        })
        return root
    }

    private fun getInference(){
        val remoteModel =
            FirebaseCustomRemoteModel.Builder("Genre-Detector").build()
        val localModel = FirebaseCustomLocalModel.Builder()
            .setAssetFilePath("converted_model.tflite")
            .build()
        val conditions = FirebaseModelDownloadConditions.Builder()
            .requireWifi()
            .build()

        FirebaseModelManager.getInstance().download(remoteModel, conditions)
            .addOnCompleteListener {
                //Enable listening functionality now
                listenButton.alpha = 1f
                listenButton.isClickable = true
            }

        Fuel.upload("https://audient.herokuapp.com/receiveWav")
            .add(FileDataPart(File(output),filename = "jam.wav", name="file"))
            .also { println(it) }
            .response { request, response, result ->
                Log.d("HUSKY", "HTTP 200")
                println("REQ: ${request}")
                println("RES: ${response}")
                val (bytes, error) = result
                if (bytes != null) {
                    println("[response bytes] ${String(bytes)}")
                    val unformattedString = String(bytes)
                    val formattedString = unformattedString.substring(2).reversed().substring(3).reversed()
                    println("[response bytes2] $formattedString")

                    FirebaseModelManager.getInstance().isModelDownloaded(remoteModel)
                        .addOnSuccessListener { isDownloaded ->
                            val options =
                                if (isDownloaded) {
                                    FirebaseModelInterpreterOptions.Builder(remoteModel).build()
                                } else {
                                    FirebaseModelInterpreterOptions.Builder(localModel).build()
                                }
                            Log.d("HUSKY","Downloaded? ${isDownloaded}")
                            val interpreter = FirebaseModelInterpreter.getInstance(options)
                            val inputOutputOptions = FirebaseModelInputOutputOptions.Builder()
                                .setInputFormat(0, FirebaseModelDataType.FLOAT32, intArrayOf(1, 26))
                                .setOutputFormat(0, FirebaseModelDataType.FLOAT32, intArrayOf(1,10))
                                .build()

                            val testSong = formattedString
                            val input = Array(1){FloatArray(26)}
                            val itr =  testSong.split(",").toTypedArray()
                            val preInput = itr.map { it.toFloat() }
                            var x = 0
                            preInput.forEach {
                                input[0][x] = preInput[x]
                                x+=1
                            }
                            //val input = preInput.toTypedArray()
                            Log.d("HUSKY", "IN: ${input[0].contentToString()}")
                            val inputs = FirebaseModelInputs.Builder()
                                .add(input) // add() as many input arrays as your model requires
                                .build()

                            val labelArray = "blues classical country disco hiphop jazz metal pop reggae rock".split(" ").toTypedArray()
                            Log.d("HUSKY2", "GG")
                            interpreter?.run(inputs, inputOutputOptions)?.addOnSuccessListener { result ->
                                Log.d("HUSKY2", "GGWP")
                                val output = result.getOutput<Array<FloatArray>>(0)
                                Log.d("HUSKY2", "OUT: ${output[0].contentToString()}")
                                val probabilities = output[0]
                                var bestMatch = 0f
                                var bestMatchIndex = 0
                                var index = 0
                                for (i in probabilities.indices){
                                    if(probabilities[i]>bestMatch){
                                        bestMatch = probabilities[i]
                                        bestMatchIndex = i
                                    }
                                    Log.d("HUSKY2", "${labelArray[i]} ${probabilities[i]}")
                                    //confidenceLabel.text = "${confidenceLabel.text} ${labelArray[i]} ${probabilities[i]}\n"
                                    //genreLabel.text = labelArray[i]
                                    when (index){
                                        0 -> { bluesProgress.isIndeterminate=false; bluesProgress.secondaryProgress=0; bluesProgress.max = 100; bluesProgress.progress = (probabilities[i]*100).toInt(); }
                                        1 -> { classicalProgress.isIndeterminate=false; classicalProgress.secondaryProgress=0; classicalProgress.max = 100; classicalProgress.progress = (probabilities[i]*100).toInt(); }
                                        2 -> { countryProgress.isIndeterminate=false; countryProgress.secondaryProgress=0; countryProgress.max = 100; countryProgress.progress = (probabilities[i]*100).toInt(); }
                                        3 -> { discoProgress.isIndeterminate=false; discoProgress.secondaryProgress=0; discoProgress.max = 100; discoProgress.progress = (probabilities[i]*100).toInt(); }
                                        4 -> { hiphopProgress.isIndeterminate=false; hiphopProgress.secondaryProgress=0; hiphopProgress.max = 100; hiphopProgress.progress = (probabilities[i]*100).toInt(); }
                                        5 -> { jazzProgress.isIndeterminate=false; jazzProgress.secondaryProgress=0; jazzProgress.max = 100; jazzProgress.progress = (probabilities[i]*100).toInt(); }
                                        6 -> { metalProgress.isIndeterminate=false; metalProgress.secondaryProgress=0; metalProgress.max = 100; metalProgress.progress = (probabilities[i]*100).toInt(); }
                                        7 -> { popProgress.isIndeterminate=false; popProgress.secondaryProgress=0; popProgress.max = 100; popProgress.progress = (probabilities[i]*100).toInt(); }
                                        8 -> { reggaeProgress.isIndeterminate=false; reggaeProgress.secondaryProgress=0; reggaeProgress.max = 100; reggaeProgress.progress = (probabilities[i]*100).toInt(); }
                                        9 -> { rockProgress.isIndeterminate=false; rockProgress.secondaryProgress=0; rockProgress.max = 100; rockProgress.progress = (probabilities[i]*100).toInt(); }
                                    }
                                    println("GENRE: ${labelArray[index]} Progress: ${(probabilities[i]*100).toInt()}")
                                    index+=1
                                }
                                genreLabel.text = labelArray[bestMatchIndex].capitalize()
                                //confidenceLabel.text = probabilities[bestMatchIndex].toString()

                                // ...
                            }?.addOnFailureListener { e ->
                                // Task failed with an exception
                                // ...
                                Log.d("HUSKY2", "GGWP :( ${e.toString()}")
                            }

                        }
                }

            }
    }

    override fun onStart() {
        progressBar.max = 20000
        progressBar.isIndeterminate = true
        progressBar.visibility = View.GONE
        progressBar.progress = 0
        output = context!!.getExternalFilesDir(DIRECTORY_DOWNLOADS)?.absolutePath + "/iam.wav"
        Log.d("HUSKY","${output}")
        waveRecorder = WaveRecorder(output!!)
        waveRecorder.noiseSuppressorActive = true
        waveRecorder.onAmplitudeListener = {
            Log.i("AMPCHANGE", "Amplitude : $it")
            progressBar.progress = it
        }
        super.onStart()
//        listenButton.alpha = .5f
//        listenButton.isClickable = false
//        incorrecttagButton.alpha = 0.5f
//        incorrecttagButton.isClickable = false



        playbackButton.setOnClickListener {
            doAsync {
                getInference()
                uiThread {
                    val mediaPlayer = MediaPlayer()
                    mediaPlayer.setDataSource(output!!)
                    mediaPlayer.prepare()
                    mediaPlayer.start()
                }
            }
        }
        listenButton.setOnClickListener {
            if(!state){
                progressBar.visibility = View.VISIBLE
                startRecording()
            }else{
                doAsync {
                    stopRecording()
                    progressBar.isIndeterminate = true
                    progressBar.progress = 0
                    uiThread {
                        doAsync {
                            getInference()
                            uiThread {
                                progressBar.visibility = View.GONE
                            }
                        }
                    }
                }
            }
        }
    }
}