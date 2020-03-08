package com.rohanbojja.audient

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.ml.common.modeldownload.FirebaseModelDownloadConditions
import com.google.firebase.ml.common.modeldownload.FirebaseModelManager
import com.google.firebase.ml.custom.*
import com.loopj.android.http.AsyncHttpClient
import com.loopj.android.http.AsyncHttpResponseHandler
import com.loopj.android.http.RequestParams
import cz.msebera.android.httpclient.Header
import kotlinx.android.synthetic.main.fragment_home.*
import java.io.File
import java.io.IOException
import java.util.*


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
        incorrecttagButton.setOnClickListener {
            //val bytes = File(output!!).inputStream()
//            Fuel.post("http://d6ea6102.ngrok.io/receiveWav/sav")
//                .body("Hello!")
//                .also { println(it) }
//                .response { result ->
//                    Log.d("HUSKY", "${result.get()}")
//                }

            val client = AsyncHttpClient()
            val params = RequestParams()
            val output = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)?.absolutePath + "/iam.wav"
            params.put("iam.wav", File(output!!))
//            params.put("profile_picture", new File("pic.jpg")); // Upload a File
//            params.put("profile_picture2", someInputStream); // Upload an InputStream
//            params.put("profile_picture3", new ByteArrayInputStream(someBytes)); // Upload some bytes
            client.post("https://audient.herokuapp.com/receiveWav/sav", params,object : AsyncHttpResponseHandler() {
                override fun onStart() {
                    // called before request is started
                }

                override fun onSuccess(
                    statusCode: Int,
                    headers: Array<Header?>?,
                    response: ByteArray?
                ) {
                    println("HTTP STATUS 200 OK")
                }

                override fun onFailure(
                    statusCode: Int,
                    headers: Array<Header?>?,
                    errorResponse: ByteArray?,
                    e: Throwable?
                ) {
                    // called when response HTTP status is "4XX" (eg. 401, 403, 404)
                    println("HTTP STATUS 400/500 NOT OK")
                }

                override fun onRetry(retryNo: Int) {
                    // called when request is retried
                }
            })
        }
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ) {
            val permissions = arrayOf(android.Manifest.permission.RECORD_AUDIO, android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.READ_EXTERNAL_STORAGE)
            ActivityCompat.requestPermissions(this, permissions,0)
        }else{
            listenButton.alpha = 1f
            listenButton.isClickable = true
        }
        }

    }
