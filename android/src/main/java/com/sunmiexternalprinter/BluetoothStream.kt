package com.sunmiexternalprinter

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.util.Log
import com.facebook.react.bridge.Promise
import java.io.OutputStream
import java.io.PipedInputStream
import java.io.PipedOutputStream
import java.util.UUID
import java.util.logging.Level
import java.util.logging.Logger


@SuppressLint("MissingPermission")
class BluetoothStream(private val device:BluetoothDevice, private val promise: Promise): PipedOutputStream() {
    private var pipedInputStream: PipedInputStream? = null
    private val MY_UUID= "00001101-0000-1000-8000-00805F9B34FB"
    private var threadPrint: Thread? = null
    private var mmSocket: BluetoothSocket?=null
    var uncaughtException =
        Thread.UncaughtExceptionHandler { t: Thread?, e: Throwable ->
            Logger.getLogger(
                this.javaClass.name
            ).log(Level.SEVERE, e.message, e)
        }

    private fun checkConnect():Boolean{
        return try {
            mmSocket!!.connect()
            Log.d("Socket Connect","Socket Connect Successful")
            true
        }catch(error:Error){
            promise.reject("Error",error.toString())
            Log.e("Socket Connect","Error",error)
            false

        }
    }

    init{
        mmSocket= device.createInsecureRfcommSocketToServiceRecord(UUID.fromString(MY_UUID))
        pipedInputStream = PipedInputStream()
        super.connect(pipedInputStream)
        val printRunnable=Runnable{
        //connect to BlDevice first
          if(checkConnect()){
            println("Here")
              val mmOutStream: OutputStream = mmSocket!!.outputStream
              val mmBuffer: ByteArray = ByteArray(1024)
              while (true) {
                  val n = pipedInputStream!!.read(mmBuffer)
                  if (n < 0) {
                      break;}
                  mmOutStream.write(mmBuffer, 0, n)
                  mmOutStream.flush()
              }
              pipedInputStream!!.close()
              promise?.resolve("Print Successfully")
          }
        }
        threadPrint = Thread(printRunnable)
        threadPrint!!.setUncaughtExceptionHandler(uncaughtException)
        threadPrint!!.start()
    }
    fun closeSocket() {
        mmSocket!!.close()
        // ... Close the BluetoothStream and any other cleanup ...
    }
}
