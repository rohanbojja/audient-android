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
        var songNum = 0
        val testsongs = mutableListOf("0.3595172803692916,0.04380025714635849,1365.710742222286,1643.935571084307,2725.445556640625,0.06513807508680555,-273.0061247040518,132.66331747988934,-31.86709317807114,44.21442952318603,4.335704872427025,32.32360339344842,-2.4662076330637714,20.458242724823684,-4.760171779927926,20.413702740993585,3.69545905318442,8.581128171784677,-15.601809275025104,5.295758930950924,-5.270195074271744,5.895109210872318,-6.1406603018722645,-2.9278519508415286,-1.9189588023091468,5.954495267889836",
        "0.33413845747468385,0.02121247537434101,2187.1177960161585,2371.037017119126,5059.505208333333,0.07946551287615741,-327.8964191763646,88.49354372352491,-4.651917114478277,60.494855099855265,-0.8843921224915671,13.540373024489472,-26.250884769365662,21.29933733836654,-20.82160186297754,12.975581144949778,-12.47863163597561,1.859163451964088,-14.698766821443108,-2.736799565521362,-7.542365477476807,-0.5018977706247959,-6.519690112502688,-0.49418747693106657,-5.660400223403326,-4.495506836183844",
        "0.49056045454508124,0.11272696405649185,2794.5571939659444,2859.1574633363884,5991.61376953125,0.10219319661458333,-101.35060367185021,76.69411223351757,9.330434037137854,14.829476860483213,10.506611375993648,16.688794095716382,3.4474442569555634,3.752170627213399,6.738799578115864,6.721023203559076,3.265134443860023,2.0129993261317947,3.0729342788104543,3.895392488506994,2.96256376218654,1.2687852378409676,-2.597564045132193,2.6010964058282413,-1.2407959708305987,-0.5437329496439187",
        "0.42811474395211957,0.1460435688495636,2830.3270754224327,3309.5695048388525,7233.7107340494795,0.07817925347222222,-103.63169726700575,78.46499508906538,28.566530265820568,22.532835026929547,20.78831805726373,1.430328640092866,9.116652158794523,3.4421905599252427,9.084889066694501,2.129821570193592,3.6334116215583747,5.5311464659349205,4.202789823137807,3.7352261330569707,0.20625945490756975,1.2238635179321025,1.851048338564436,0.6192016809083627,-1.0554453937439026,-1.633621077534561",
        "0.48093542013732904,0.10466829687356949,2887.7616718338472,2559.3482083164376,6086.0707600911455,0.14599609375,-109.35235827786713,71.7150370373459,-13.072355173356527,54.02192425043272,-4.226015240930529,38.18503834480247,-16.48365214313494,32.12343193453901,-11.466521260614627,17.516115897800294,-10.253031441104529,16.054891700039047,-6.406599013860771,6.666546220177415,-8.461471036220596,6.495552805836917,-6.002727515699104,-0.3304619167889349,-5.373598135610952,2.394150681582516",
        "0.27703011878074557,0.11200398206710815,2227.849146074581,2429.276432187305,4469.9839274088545,0.09056712962962964,-162.8954241260934,79.15280368186008,-2.417423175316254,39.59947883099553,3.973107110755953,-8.471402165675235,4.521493184141745,6.652590547557736,-13.863104681839442,-7.441324622180053,4.681828637450696,-1.247808583453883,2.345332130880664,0.4074968979537844,-7.44969040015772,-8.537668204654247,-4.797973977072894,-5.280747362279262,-7.780363951600931,-5.898098436483338")
        incorrecttagButton.setOnClickListener {
            songNum+=1
        }
        listenButton.setOnClickListener {
            incorrecttagButton.alpha = 1f
            incorrecttagButton.isClickable = true
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
                       .setOutputFormat(0, FirebaseModelDataType.FLOAT32, intArrayOf(1,10))
                       .build()
                   if(songNum==5){
                       songNum=0
                   }
                   val testSong = testsongs[songNum]
                   Log.d("HUSKY", "Song num = ${songNum} F = ${testSong} ")
                   val input = Array(1){FloatArray(26)}
                   val itr =  testSong.split(",").toTypedArray()
                   val preInput = itr.map { it.toFloat() }
                   var x = 0
                   preInput.forEach {
                       input[0][x] = preInput[x]
                       x+=1
                   }
                   //val input = preInput.toTypedArray()
                   Log.d("HUSKY", "${input[0][1]}")
                   val inputs = FirebaseModelInputs.Builder()
                       .add(input) // add() as many input arrays as your model requires
                       .build()

                   val labelArray = "blues classical country disco hiphop jazz metal pop reggae rock".split(" ").toTypedArray()
                   Log.d("HUSKY2", "GG")
                   interpreter?.run(inputs, inputOutputOptions)?.addOnSuccessListener { result ->
                       Log.d("HUSKY2", "GGWP")
                       val output = result.getOutput<Array<FloatArray>>(0)
                       val probabilities = output[0]
                       var bestMatch = 0f
                       var bestMatchIndex = 0
                       for (i in probabilities.indices){
                           if(probabilities[i]>bestMatch){
                               bestMatch = probabilities[i]
                               bestMatchIndex = i
                           }
                           Log.d("HUSKY2", "${labelArray[i]} ${probabilities[i]}")
                           genreLabel.text = labelArray[i]
                       }
                       genreLabel.text = labelArray[bestMatchIndex].capitalize()
                       confidenceLabel.text = probabilities[bestMatchIndex].toString()

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
