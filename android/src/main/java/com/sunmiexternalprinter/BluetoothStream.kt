package com.sunmiexternalprinter

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.util.Log
import com.facebook.react.bridge.Promise
import java.io.OutputStream
import java.io.PipedInputStream
import java.io.PipedOutputStream
import java.util.UUID
import java.util.logging.Level
import java.util.logging.Logger


@SuppressLint("MissingPermission")
class  BluetoothStream(private val device:BluetoothDevice, private val promise: Promise): PipedOutputStream() {
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
      if(!mmSocket!!.isConnected){
        println("Not Connected to socket")
        mmSocket?.connect()
      }
      Log.d("Socket Connect","Socket Connect Successful")
      true
    }catch(error:Error){
      mmSocket?.close()
      promise.reject("Error",error.toString())
      Log.e("Socket Connect","Error",error)
      false

    }
  }

  public fun openSocketThread(){
    mmSocket= device.createInsecureRfcommSocketToServiceRecord(UUID.fromString(MY_UUID))
    pipedInputStream = PipedInputStream()
    super.connect(pipedInputStream)
    val printRunnable=Runnable{
      //connect to BlDevice first
      if(checkConnect()){
        val mmOutStream: OutputStream = mmSocket!!.outputStream
        val mmBuffer= ByteArray(1024)
        while (true) {
          val n = pipedInputStream!!.read(mmBuffer)
          if(n<0){
            break;
          }
          mmOutStream.write(mmBuffer, 0, n)
          mmOutStream.flush()

        }

        pipedInputStream!!.close()

        promise.resolve("Print Successfully")
      }
    }
    threadPrint = Thread(printRunnable)
    threadPrint!!.uncaughtExceptionHandler = uncaughtException
    threadPrint!!.start()

  }

  fun setCustomUncaughtException(uncaughtException: Thread.UncaughtExceptionHandler?) {
    threadPrint!!.uncaughtExceptionHandler = uncaughtException
  }
    fun closeSocket() {
      // For bluetooth it's a bit different to TCP IP based on this now no more red timeout occurs
      // https://stackoverflow.com/questions/18657427/ioexception-read-failed-socket-might-closed-bluetooth-on-android-4-3?rq=1
        mmSocket!!.outputStream.close()
        mmSocket!!.inputStream.close()
        mmSocket!!.close()
        // ... Close the BluetoothStream and any other cleanup ...
    }
}
