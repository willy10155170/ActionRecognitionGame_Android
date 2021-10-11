package com.test.gesture_recognition_game

import android.os.Bundle

/**
 * Created by stephenvinouze on 18/05/2017.
 */
interface RecognitionCallback {
    fun onPrepared(status: RecognitionStatus)
    fun onBeginningOfSpeech()
    fun onKeywordDetected()
    fun onReadyForSpeech(params: Bundle)
    fun onBufferReceived(buffer: ByteArray)
    fun onRmsChanged(rmsdB: Float)
    fun onPartialResults(results: List<String>)
    fun onResults(results: List<String>, scores: FloatArray?)
    fun onError(errorCode: Int)
    fun onEvent(eventType: Int, params: Bundle)
    fun onEndOfSpeech()
}