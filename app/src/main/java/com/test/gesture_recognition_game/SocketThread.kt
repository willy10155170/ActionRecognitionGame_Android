 package com.test.gesture_recognition_game

import android.os.Handler
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.Socket
//import java.util.logging.Handler

class SocketThread:Thread() {

    private lateinit var handler: Handler
    var writer : PrintWriter? = null
    private lateinit var ip_address:String

    override fun run() {
        super.run()
        val socket = Socket(ip_address, 6969)
        val input = socket.getInputStream()
        val reader = BufferedReader(InputStreamReader(input))
        var text: String
        val output = socket.getOutputStream()
        writer = PrintWriter(output, true)
        while (true){
            text = reader.readLine()
            println(text)
            handler.sendMessage(handler.obtainMessage(0, text))
        }
    }

    fun setHandler(handler:Handler) {
        this.handler = handler
    }

    fun setIP(ip:String){
        this.ip_address = ip
    }

    fun sendMessage(string: String) {
        writer?.println(string)
    }

    override fun destroy() {

    }
}