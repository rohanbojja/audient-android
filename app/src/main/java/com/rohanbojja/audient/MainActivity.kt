package com.rohanbojja.audient

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.ml.common.modeldownload.FirebaseModelDownloadConditions
import com.google.firebase.ml.common.modeldownload.FirebaseModelManager
import com.google.firebase.ml.custom.*
import kotlinx.android.synthetic.main.fragment_home.*


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val navView: BottomNavigationView = findViewById(R.id.nav_view)
        val navController = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }
    fun generateFeatures(){
        //Function to generate features from the recorded audio
    }
    override fun onStart() {
        super.onStart()
        listenButton.alpha = .5f
        listenButton.isClickable = false
        incorrecttagButton.alpha = 0.5f
        incorrecttagButton.isClickable = false
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

       listenButton.setOnClickListener {
            //Code for listening to music
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
                       .setOutputFormat(0, FirebaseModelDataType.FLOAT32, intArrayOf(1, 10))
                       .build()
                   val testSong = "0.33459249215419645,0.12434733659029007,1778.396170947293,1975.7320297932633,3744.334920247396,0.0839527271412037,-119.049612844565,124.37451838677069,-22.895319632587196,42.13384217571392,-7.052344958189589,20.022586754004806,-16.100138122467385,17.927587615118913,-12.58575780489375,12.421998395852821,-9.123081786396094,9.024045397643759,-5.630365636872576,7.129667588112892,-8.72794173170328,-2.0566334709104157,-3.953863403449407,0.6971841099817211,-2.858019985312612,2.8180257647488314"
                   val input = FloatArray(26)
                   val itr =  testSong.split(",").toTypedArray()
                   val preInput = itr.map { it.toFloat() }
                   var x = 0
                   preInput.forEach {
                       input[x] = preInput[x]
                       x+=1
                   }
                   //val input = preInput.toTypedArray()
                   Log.d("HUSKY", "${input[0]}")
                   val inputs = FirebaseModelInputs.Builder()
                       .add(input) // add() as many input arrays as your model requires
                       .build()

                   val labelArray = "blues classical country disco hiphop jazz metal pop reggae rock".split(" ").toTypedArray()
                   Log.d("HUSKY2", "GG")
                   interpreter?.run(inputs, inputOutputOptions)?.addOnSuccessListener { result ->
                       Log.d("HUSKY2", "GGWP")
                       val output = result.getOutput<Array<FloatArray>>(0)
                       val probabilities = output[0]
                       for (i in probabilities.indices){
                           Log.d("HUSKY2", "${labelArray[i]} ${probabilities[i]}")
                       }
                       // ...
                   }?.addOnFailureListener { e ->
                       // Task failed with an exception
                       // ...
                       Log.d("HUSKY2", "GGWP :(")
                   }

               }
        }
        }
    }
