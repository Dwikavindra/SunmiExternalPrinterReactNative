package com.sunmiexternalprinter

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.util.Base64
import com.dantsu.escposprinter.EscPosPrinterCommands
import com.dantsu.escposprinter.connection.DeviceConnection
import com.dantsu.escposprinter.connection.tcp.TcpConnection
import com.dantsu.escposprinter.exceptions.EscPosConnectionException
import com.facebook.react.bridge.*
import com.facebook.react.modules.core.DeviceEventManagerModule
import com.github.anastaciocintra.escpos.EscPos
import com.github.anastaciocintra.escpos.image.BitonalOrderedDither
import com.github.anastaciocintra.escpos.image.EscPosImage
import com.github.anastaciocintra.escpos.image.RasterBitImageWrapper
import com.github.anastaciocintra.output.TcpIpOutputStream
import java.net.InetAddress
import kotlin.math.floor

class SunmiExternalPrinterReactNativeModule(reactContext: ReactApplicationContext) :
  ReactContextBaseJavaModule(reactContext) {

  override fun getName(): String {
    return NAME
  }

  // Example method
  // See https://reactnative.dev/docs/native-modules-android
  private var promise: Promise? = null
  private var nsdManager:NsdManager?=null

  private fun sendEvent(reactContext: ReactContext, eventName: String, params: WritableMap?) {
    reactContext
      .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
      .emit(eventName, params)
  }

  private val resolveListener = object : NsdManager.ResolveListener {

    override fun onResolveFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
      // Called when the resolve fails. Use the error code to debug.

    }

    override fun onServiceResolved(serviceInfo: NsdServiceInfo) {

      val port: Int = serviceInfo.port
      val host: InetAddress = serviceInfo.host
      val serviceName= serviceInfo.serviceName
      val payload=Arguments.createMap().apply{
        putString("printername",serviceName)
        putString("ip",host.hostAddress)
        putString("port",port.toString())
      }
      sendEvent(reactContext,"OnPrinterFound",payload)
    }
  }

  private val discoveryListener: NsdManager.DiscoveryListener = object :NsdManager.DiscoveryListener{
    override fun onStartDiscoveryFailed(p0: String?, p1: Int) {
      nsdManager?.stopServiceDiscovery(this)

    }

    override fun onStopDiscoveryFailed(p0: String?, p1: Int) {

      nsdManager?.stopServiceDiscovery(this)
    }

    override fun onDiscoveryStarted(p0: String?) {

    }

    override fun onDiscoveryStopped(p0: String?) {

    }

    override fun onServiceFound(p0: NsdServiceInfo?) {
      nsdManager?.resolveService(p0,resolveListener)

    }

    override fun onServiceLost(p0: NsdServiceInfo?) {

    }

  }
  @ReactMethod
  fun multiply(a: Double, b: Double, promise: Promise) {
    promise.resolve(a * b)
  }


  @ReactMethod
  fun printImageWithTCP(base64Image:String,ipAddress:String,port:String,promise: Promise) {
    this.promise=promise

    Thread {
      try {
     val encodedBase64 = Base64.decode(base64Image, Base64.DEFAULT)
        val bitmap = BitmapFactory.decodeByteArray(encodedBase64, 0, encodedBase64.size)
        val scaledBitmap= Bitmap.createScaledBitmap(bitmap,bitmap.width-40,bitmap.height,true)
        val  stream = TcpIpOutputStream(ipAddress,port.toInt())
        val escpos= EscPos(stream)
        val algorithm= BitonalOrderedDither()
        val imageWrapper = RasterBitImageWrapper()
        val escposImage = EscPosImage(CoffeeImageAndroidImpl(scaledBitmap), algorithm)
        escpos.write(imageWrapper, escposImage)
        escpos.feed(5).cut(EscPos.CutMode.FULL)
        promise.resolve("Print Successfully")
      } catch (e: java.lang.Exception) {
        e.printStackTrace()
        promise.reject("Error",e.toString())
      }
    }.start()

  }

  @ReactMethod
  fun startDiscovery(promise:Promise){
    try {
      nsdManager = reactApplicationContext.getSystemService(Context.NSD_SERVICE) as NsdManager
      nsdManager?.stopServiceDiscovery(discoveryListener);
      nsdManager?.discoverServices(
        "_afpovertcp._tcp",
        1,
        discoveryListener
      )
      promise.resolve("Discovery Started")
    }catch (e:Exception){
      promise.reject("Error",e.toString())
    }


  }
  @ReactMethod
  fun stopDiscovery(promise:Promise){
    try {
      if(nsdManager!==null){
        nsdManager?.stopServiceDiscovery(discoveryListener)
      }
      else{
        throw Exception ("nsdManager cannot be null")
      }
      promise.resolve("Network Discovery stopped")
    }catch (e:Exception){
      promise.reject("Error",e.toString())
    }
  }

  @ReactMethod
  fun printImageWithTCP2(base64Image:String,ipAddress:String,port:String,promise: Promise){
    this.promise=promise

    Thread {
      try {
        val encodedBase64 = Base64.decode(base64Image, Base64.DEFAULT)
        val bitmap = BitmapFactory.decodeByteArray(encodedBase64, 0, encodedBase64.size)
        val scaledBitmap= Bitmap.createScaledBitmap(bitmap,bitmap.width-40,bitmap.height,true)
        val  stream = TcpIpOutputStream(ipAddress,port.toInt())
        val escpos= EscPos(stream)
        val algorithm= BitonalOrderedDither()
        val imageWrapper = RasterBitImageWrapper()
        val escposImage = EscPosImage(CoffeeImageAndroidImpl(scaledBitmap), algorithm)
        escpos.write(imageWrapper, escposImage)
        escpos.feed(5).cut(EscPos.CutMode.FULL)
        promise.resolve("Print Successfully")



      } catch (e: java.lang.Exception) {
        e.printStackTrace()
        promise.reject("Error",e.toString())
      }
    }.start()


  }


  companion object {
    const val NAME = "SunmiExternalPrinterReactNative"
  }

}


