package com.test.gesture_recognition_game

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Handler
import java.util.*
import kotlin.math.abs
import kotlin.math.sqrt

class SensorThread:Thread() {
    lateinit var mcontext: Context
    private lateinit var handler: Handler
    lateinit var sensorManager: SensorManager
    private var acceleration = 0f
    private var currentAcceleration = 0f
    private var lastAcceleration = 0f
    private var shakecounter:Int = 0
    private var motionstatus = arrayOf(0, 0, 0) // x y z
    //0.1244, 9.8, 0.2873
    override fun run() {
        super.run()
        //sensorManager = getSystemService(mcontext.SENSOR_SERVICE) as SensorManager
        sensorManager = mcontext.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        Objects.requireNonNull(sensorManager)!!.registerListener(sensorListener, sensorManager!!
            .getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL)
    }

    private val sensorListener: SensorEventListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            var x = event.values[0]
            var y = event.values[1] - 9.5
            var z = event.values[2]

            if (y > 7 && motionstatus[1] == 0){
                handler.sendMessage(handler.obtainMessage(0,"jump"))
                motionstatus[1] = 1
            }
            else if (y > 0 && y < 2){
                motionstatus[1] = 0
            }
            else if (y < 0 && y > -2){
                motionstatus[1] = 0
            }
        }
        override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
    }

    fun set_context(context: Context){
        this.mcontext = context
        this.shakecounter = 0
    }

    fun stop_sensor(){
        sensorManager!!.unregisterListener(sensorListener)

    }

    fun setHandler(handler: Handler) {
        this.handler = handler
    }
}