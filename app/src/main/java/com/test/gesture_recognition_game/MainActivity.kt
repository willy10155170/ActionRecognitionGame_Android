package com.test.gesture_recognition_game

import android.Manifest
import android.app.Service
import android.content.Context
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.os.Vibrator
import android.speech.SpeechRecognizer
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import java.lang.ref.WeakReference
import com.test.gesture_recognition_game.SocketThread
import com.test.gesture_recognition_game.SensorThread
import org.w3c.dom.Text


class MainActivity : AppCompatActivity(), RecognitionCallback {
    private var sockethandler: Handler
    init {
        val outerClass = WeakReference(this)
        sockethandler = SocketHandler(outerClass, this)
    }

    private var sensorhandler: Handler
    init {
        val outerClass = WeakReference(this)
        sensorhandler = SensorHandler(outerClass)
    }

    companion object {
        /**
         * Put any keyword that will trigger the speech recognition
         */
        private const val ACTIVATION_KEYWORD = "攻擊"
        private const val RECORD_AUDIO_REQUEST_CODE = 101
    }
    private val recognitionManager: KontinuousRecognitionManager by lazy {
        KontinuousRecognitionManager(this, activationKeyword = ACTIVATION_KEYWORD, callback = this)
    }

    private lateinit var progressBar: ProgressBar
    lateinit var tv_result:TextView
    lateinit var voice_result:TextView
    lateinit var connect: Button
    lateinit var IPText: EditText
    lateinit var send: Button
    lateinit var msgText: EditText
    lateinit var start:Button
    lateinit var stop:Button
    var socketThread = SocketThread()
    private lateinit var sensorThread:SensorThread

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), RECORD_AUDIO_REQUEST_CODE)
        }
        progressBar = findViewById(R.id.progressBar1)
        tv_result = findViewById(R.id.tv_result)
        tv_result.isSingleLine = false
        voice_result = findViewById(R.id.voice_result)
        IPText = findViewById(R.id.ip_input)
        connect = findViewById(R.id.btn_connect)
        send = findViewById(R.id.btn_send)
        msgText = findViewById(R.id.msg_input)
        start = findViewById(R.id.btn_voice_start)
        stop = findViewById(R.id.btn_voice_stop)
        progressBar.visibility = View.INVISIBLE
        progressBar.max = 10
        //recognitionManager.createRecognizer()
        //socketThread = SocketThread()
        sensorThread = SensorThread()
        sensorThread.set_context(this)
        sensorThread.setHandler(sensorhandler)
        sensorThread.start()

        //val socketThread = SocketThread()
        start.setOnClickListener { startRecognition() }
        stop.setOnClickListener { stopRecognition() }
        connect.setOnClickListener{connection(socketThread as SocketThread)}
        send.setOnClickListener{sendMessage(socketThread as SocketThread)}
    }


    fun connection(socketThread:SocketThread){
//        Thread{
//            socketThread.sendMessage(IPText.text.toString())
//        }.start()
        socketThread.setHandler(sockethandler)
        socketThread.setIP(IPText.text.toString())
        socketThread.start()
    }

    fun sendMessage(socketThread:SocketThread){
        Thread{
            socketThread.sendMessage(msgText.text.toString())
        }.start()
        msgText.setText("")
    }

    fun sendVoice(send_text: String, socketThread:SocketThread){
        Thread{
            socketThread.sendMessage(send_text)
        }.start()
    }

    fun sendSensor(sensor_value:String){
        Thread{
            socketThread.sendMessage("1 $sensor_value")
        }.start()
    }

    // Declare the Handler as a static class.
    class SocketHandler(private val outerClass: WeakReference<MainActivity>, private var mcontext: Context) : Handler() {
        override fun handleMessage(msg: Message) {
            outerClass.get()?.tv_result?.append(msg?.obj.toString()+"\n")
            var vib: Vibrator? = null
            vib = mcontext.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator?
            vib!!.vibrate(10)
        }
    }

    class SensorHandler(private val outerClass: WeakReference<MainActivity>) : Handler() {
        override fun handleMessage(msg: Message) {

            outerClass.get()?.tv_result?.append(msg?.obj.toString()+"\n")
            //outerClass.get()?.tv_result?.text = msg?.obj.toString()+"\n"
            outerClass.get()?.sendSensor(msg?.obj.toString()+"\n")
        }
    }

    override fun onDestroy() {
        recognitionManager.destroyRecognizer()
        super.onDestroy()
    }

    override fun onResume() {
        super.onResume()

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            startRecognition()
        }
    }

    override fun onPause() {
        stopRecognition()
        super.onPause()
    }

    private fun startRecognition() {
        progressBar.isIndeterminate = false
        progressBar.visibility = View.VISIBLE
        recognitionManager.createRecognizer()
        recognitionManager.startRecognition()

        voice_result.text = "start recognition"
    }

    private fun stopRecognition() {
        progressBar.isIndeterminate = true
        progressBar.visibility = View.INVISIBLE
        recognitionManager.stopRecognition()
        recognitionManager.destroyRecognizer()
        voice_result.text = "stop recognition"
    }

    private fun getErrorText(errorCode: Int): String = when (errorCode) {
        SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
        SpeechRecognizer.ERROR_CLIENT -> "Client side error"
        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Insufficient permissions"
        SpeechRecognizer.ERROR_NETWORK -> "Network error"
        SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
        SpeechRecognizer.ERROR_NO_MATCH -> "No match"
        SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "RecognitionService busy"
        SpeechRecognizer.ERROR_SERVER -> "Error from server"
        SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech input"
        else -> "Didn't understand, please try again."
    }

    override fun onBeginningOfSpeech() {
        Log.i("Recognition","onBeginningOfSpeech")
    }
    //
    override fun onBufferReceived(buffer: ByteArray) {
        Log.i("Recognition", "onBufferReceived: $buffer")
    }
    //
    override fun onEndOfSpeech() {
        Log.i("Recognition","onEndOfSpeech")
    }

    override fun onError(errorCode: Int) {
        val errorMessage = getErrorText(errorCode)
        Log.i("Recognition","onError: $errorMessage")
        voice_result.text = errorMessage
    }

    override fun onEvent(eventType: Int, params: Bundle) {
        Log.i("Recognition","onEvent")
    }

    override fun onReadyForSpeech(params: Bundle) {
        Log.i("Recognition","onReadyForSpeech")
    }

    override fun onRmsChanged(rmsdB: Float) {
        progressBar.progress = rmsdB.toInt()
    }

    override fun onPrepared(status: RecognitionStatus) {
        when (status) {
            RecognitionStatus.SUCCESS -> {
                Log.i("Recognition","onPrepared: Success")
                voice_result.text = "Recognition ready"
            }
            RecognitionStatus.UNAVAILABLE -> {
                Log.i("Recognition", "onPrepared: Failure or unavailable")
                AlertDialog.Builder(this)
                        .setTitle("Speech Recognizer unavailable")
                        .setMessage("Your device does not support Speech Recognition. Sorry!")
                        //.setPositiveButton(R.string.ok, null)
                        .show()
            }
        }
    }

    override fun onKeywordDetected() {
        Log.i("Recognition","keyword detected !!!")
        voice_result.text = "Keyword detected"
    }

    override fun onPartialResults(results: List<String>) {}

    override fun onResults(results: List<String>, scores: FloatArray?) {
        val text = "0 " + results.joinToString(separator = "\n")
        Log.i("Recognition","onResults : $text")
        voice_result.text = text
        sendVoice(text, socketThread as SocketThread)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            RECORD_AUDIO_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startRecognition()
                }
            }
        }
    }
}