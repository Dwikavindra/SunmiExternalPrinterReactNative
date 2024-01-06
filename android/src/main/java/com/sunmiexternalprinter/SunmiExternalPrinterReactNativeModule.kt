package com.sunmiexternalprinter


import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.os.Handler
import android.os.Looper
import android.util.Base64
import android.util.Log
import androidx.core.content.ContextCompat
import com.facebook.react.bridge.*
import com.facebook.react.modules.core.DeviceEventManagerModule
import com.github.anastaciocintra.escpos.EscPos
import com.github.anastaciocintra.escpos.image.*
import com.github.anastaciocintra.output.TcpIpOutputStream
import com.izettle.html2bitmap.Html2Bitmap
import com.izettle.html2bitmap.content.WebViewContent
import java.io.ByteArrayOutputStream
import java.net.InetAddress
import java.util.Collections
import java.util.SortedSet
import java.util.TreeSet


class SunmiExternalPrinterReactNativeModule(reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {
  private val SCAN_PERIOD: Long = 10000
  private var promise: Promise? = null
  private var nsdManager: NsdManager? = null
  val bluetoothManager: BluetoothManager = ContextCompat.getSystemService(
    this.reactApplicationContext,
    BluetoothManager::class.java
  )!!
  val bluetoothAdapter: BluetoothAdapter? = bluetoothManager.adapter
  private var scanning = false
  private val handler = Handler(Looper.getMainLooper())
  private val bleScanResults:SortedSet<BluetoothDeviceComparable> = TreeSet()
  private val bleScanResultsClassChanged= mutableListOf<BluetoothDeviceComparable>()
  private val bleScanResultsDataClass= mutableListOf<BTDevice>()
  var stream: BluetoothStream? = null
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
      val serviceName = serviceInfo.serviceName
      val payload = Arguments.createMap().apply {
        putString("Service Discovery", serviceName)
        putString("ip", host.hostAddress)
        putString("port", port.toString())
      }
      sendEvent(reactContext, "OnPrinterFound", payload)
    }
  }

  private val discoveryListener: NsdManager.DiscoveryListener =
    object : NsdManager.DiscoveryListener {
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
        println("Found")
        nsdManager?.resolveService(p0, resolveListener)


      }

      override fun onServiceLost(p0: NsdServiceInfo?) {

      }

    }

  override fun getName(): String {
    return NAME
  }

  @ReactMethod
  fun convertHTMLtoBase64(htmlString: String, width: Int, promise: Promise) {
    this.promise = promise
    Thread {
      try {
        val bitmap: Bitmap? =
          Html2Bitmap.Builder().setContext(reactApplicationContext.applicationContext)
            .setContent(WebViewContent.html(htmlString)).setBitmapWidth(width)
            .build().bitmap
//        val resizedBitmap = Bitmap.createScaledBitmap(
//          bitmap as Bitmap,
//          631,
//          bitmap.height,
//          true
//        )/// what works the best so far 80mm
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap?.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        val base64String = Base64.encodeToString(byteArray, Base64.DEFAULT)
        promise.resolve(base64String)

      } catch (e: java.lang.Exception) {
        e.printStackTrace()
        promise.reject("Error", e.toString())

      }

    }.start()


  }

  @ReactMethod
  fun printImageWithTCPRasterBitImageWrapper(
    base64Image: String,
    ipAddress: String,
    port: String,
    promise: Promise
  ) {
    this.promise = promise

    Thread {
      try {
        val encodedBase64 = Base64.decode(base64Image, Base64.DEFAULT)
        val bitmap = BitmapFactory.decodeByteArray(encodedBase64, 0, encodedBase64.size)
        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, bitmap.width, bitmap.height, true)
        val stream = TcpIpOutputStream(ipAddress, port.toInt())

        val escpos = EscPos(stream)
        val algorithm = BitonalOrderedDither()
        val imageWrapper = RasterBitImageWrapper()
        val escposImage = EscPosImage(CoffeeImageAndroidImpl(scaledBitmap), algorithm)
        ImageHelper(scaledBitmap.width, scaledBitmap.height).write(
          escpos,
          CoffeeImageAndroidImpl(scaledBitmap),
          imageWrapper,
          BitonalThreshold()
        )
        escpos.feed(5).cut(EscPos.CutMode.FULL)
        escpos.close()
        promise.resolve("Print Successfully")
      } catch (e: java.lang.Exception) {
        e.printStackTrace()
        promise.reject("Error", e.toString())
      }
    }.start()

  }

  @ReactMethod
  fun printImageWithTCPBitImageWrapper(
    base64Image: String,
    ipAddress: String,
    port: String,
    promise: Promise
  ) {
    this.promise = promise

    Thread {
      try {
        val encodedBase64 = Base64.decode(base64Image, Base64.DEFAULT)
        val bitmap = BitmapFactory.decodeByteArray(encodedBase64, 0, encodedBase64.size)
        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, bitmap.width, bitmap.height, true)
        val stream = TcpIpOutputStream(ipAddress, port.toInt())
        val escpos = EscPos(stream)
        val algorithm = BitonalOrderedDither()
        val imageWrapper = BitImageWrapper()
        val escposImage = EscPosImage(CoffeeImageAndroidImpl(scaledBitmap), algorithm)
        ImageHelper(scaledBitmap.width, scaledBitmap.height).write(
          escpos,
          CoffeeImageAndroidImpl(scaledBitmap),
          imageWrapper,
          BitonalThreshold()
        )
        escpos.cut(EscPos.CutMode.FULL)
        escpos.close()
        promise.resolve("Print Successfully")

      } catch (e: java.lang.Exception) {
        e.printStackTrace()
        promise.reject("Error", e.toString())
      }
    }.start()


  }

  @ReactMethod
  fun printImageWithGraphicsImageWrapper(
    base64Image: String,
    ipAddress: String,
    port: String,
    promise: Promise
  ) {
    this.promise = promise

    Thread {
      try {
        val encodedBase64 = Base64.decode(base64Image, Base64.DEFAULT)
        val bitmap = BitmapFactory.decodeByteArray(encodedBase64, 0, encodedBase64.size)
        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, bitmap.width, bitmap.height, true)
        val stream = TcpIpOutputStream(ipAddress, port.toInt())
        val escpos = EscPos(stream)
        val algorithm = BitonalOrderedDither()
        val imageWrapper = GraphicsImageWrapper()
        val escposImage = EscPosImage(CoffeeImageAndroidImpl(scaledBitmap), algorithm)
        ImageHelper(scaledBitmap.width, scaledBitmap.height).write(
          escpos,
          CoffeeImageAndroidImpl(scaledBitmap),
          imageWrapper,
          BitonalThreshold()
        )
        escpos.cut(EscPos.CutMode.FULL)
        escpos.close()
        promise.resolve("Print Successfully")

      } catch (e: java.lang.Exception) {
        e.printStackTrace()
        promise.reject("Error", e.toString())
      }
    }.start()


  }

  @ReactMethod
  fun startDiscovery(promise: Promise) {
    try {
      nsdManager =
        reactApplicationContext.applicationContext.getSystemService(Context.NSD_SERVICE) as NsdManager
      nsdManager?.discoverServices(
        "_afpovertcp._tcp",
        NsdManager.PROTOCOL_DNS_SD,
        discoveryListener
      )
      promise.resolve("Discovery Started")
    } catch (e: Exception) {
      promise.reject("Error", e.toString())
    }


  }

  @ReactMethod
  fun openDrawer(ipAddress: String, port: String, promise: Promise) {
    this.promise = promise
    Thread {
      try {
        val stream = TcpIpOutputStream(ipAddress, port.toInt())
        val escpos = EscPos(stream)
        escpos.write(27).write(112).write(0).write(25).write(250);
        escpos.write(27).write(0).write(-56).write(-56)
        escpos.close()
        promise.resolve(true)
      } catch (e: Exception) {
        promise.reject("Error", e.toString())
      }
    }.start()

  }

  @ReactMethod
  fun stopDiscovery(promise: Promise) {
    try {
      if (nsdManager !== null) {
        nsdManager?.stopServiceDiscovery(discoveryListener)
      } else {
        throw Exception("nsdManager cannot be null")
      }
      promise.resolve("Network Discovery stopped")
    } catch (e: Exception) {
      promise.reject("Error", e.toString())
    }
  }

  private val receiver = object : BroadcastReceiver() {
    @SuppressWarnings("MissingPermission")
    override fun onReceive(context: Context, intent: Intent) {
      val action: String? = intent.action
      when (action) {
        BluetoothDevice.ACTION_FOUND -> {
            val device: BluetoothDevice =
              intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)!!
            val majorDeviceClass = device.bluetoothClass.majorDeviceClass
            val deviceClass= device.bluetoothClass.deviceClass
            val deviceComparable: BluetoothDeviceComparable = BluetoothDeviceComparable(device)
              this@SunmiExternalPrinterReactNativeModule.bleScanResults.add(deviceComparable)
            val find=  this@SunmiExternalPrinterReactNativeModule.bleScanResultsDataClass.find { it.address==deviceComparable.bluetoothDevice.address }
            if(find==null){
              Log.d("Discovery"," On Device Found where find ==null \n Device Info \n Name:${device.name} \n Address:${device.address} \n MajorDeviceClass: ${device.bluetoothClass.majorDeviceClass}\n Device Class ${device.bluetoothClass.deviceClass}")
              this@SunmiExternalPrinterReactNativeModule.bleScanResultsDataClass.add(BTDevice(device.name,device.address,device.bluetoothClass.majorDeviceClass,device.bluetoothClass.deviceClass))
            }



        }
        BluetoothDevice.ACTION_CLASS_CHANGED->{
            Log.d("Discovery","Device Action Class Changed ")
            //find the BL device and then change
            val device: BluetoothDevice =intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)!!
            val deviceComparable:BluetoothDeviceComparable= BluetoothDeviceComparable(device)
            if(device.name.contains("Cloud")){
              Log.d("Discovery","On Action Class Changed Before entering if statement \n" +
                " Device Info \n" +
                " Name:${device.name} \n" +
                " Address:${device.address} \n" +
                " MajorDeviceClass: ${device.bluetoothClass.majorDeviceClass}\n" +
                " Device Class ${device.bluetoothClass.deviceClass} ")
            }

            if(device.bluetoothClass.majorDeviceClass!=7936 || device.bluetoothClass.deviceClass!=7936) {

              Log.d("Discovery","on Action Class Changed \n Passed if statement \n" +
                " Device Info \n" +
                " Name:${device.name} \n" +
                " Address:${device.address} \n" +
                " MajorDeviceClass: ${device.bluetoothClass.majorDeviceClass}\n" +
                " Device Class ${device.bluetoothClass.deviceClass}")
              val findDataClass=this@SunmiExternalPrinterReactNativeModule.bleScanResultsDataClass.find{it.address==device.address}
              if(findDataClass!=null){
                val result=this@SunmiExternalPrinterReactNativeModule.bleScanResultsDataClass.remove(findDataClass)
                Log.d("Discovery","on Action Class Changed : Changed Remove Scan Data Class Result ${result}")
              }
              this@SunmiExternalPrinterReactNativeModule.bleScanResultsDataClass.add(
                BTDevice(device.name,device.address,device.bluetoothClass.majorDeviceClass,device.bluetoothClass.deviceClass)
              )

            }


          }

        BluetoothAdapter.ACTION_DISCOVERY_FINISHED-> {
  val result: WritableMap = Helper.SetBLDevicestoWriteableArray(
    this@SunmiExternalPrinterReactNativeModule.bleScanResultsDataClass,
    this@SunmiExternalPrinterReactNativeModule.reactApplicationContext,
    this@SunmiExternalPrinterReactNativeModule.currentActivity!!
  )
  Log.d("Discovery","Discovery Finished")
  this@SunmiExternalPrinterReactNativeModule.bleScanResults.forEach{it->
    Log.d("Discovery"," On Discovery Finished \n Device Info \n Name:${it.bluetoothDevice.name} \n Address:${it.bluetoothDevice.address} \n MajorDeviceClass: ${it.bluetoothDevice.bluetoothClass.majorDeviceClass}\n Device Class ${it.bluetoothDevice.bluetoothClass.deviceClass}")
  }
  this@SunmiExternalPrinterReactNativeModule.bleScanResultsClassChanged.forEach{it->
    Log.d("Discovery"," On Discovery Finished Class Changed \n Device Info \n Name:${it.bluetoothDevice.name} \n Address:${it.bluetoothDevice.address} \n MajorDeviceClass: ${it.bluetoothDevice.bluetoothClass.majorDeviceClass}\n Device Class ${it.bluetoothDevice.bluetoothClass.deviceClass}")
  }
  this@SunmiExternalPrinterReactNativeModule.bleScanResultsDataClass.forEach{it->
    Log.d("Discovery"," On Discovery Finished Data Class Changed \n Device Info \n Name:${it.name} \n Address:${it.address} \n MajorDeviceClass: ${it.majorDeviceClass}\n Device Class ${it.deviceClass}")
  }
  Log.d("Discovery","On Discovery Finished Size of BleScanResult ${bleScanResults.size}, and size of ${bleScanResultsDataClass.size}")

  this@SunmiExternalPrinterReactNativeModule.promise!!.resolve(result)



        }

      }
    }
  }

  @SuppressLint("MissingPermission")
  @ReactMethod
  private fun scanBLDevice(promise: Promise) {
    bleScanResults.clear()
    bleScanResultsDataClass.clear()
    Log.d("Size:","${bleScanResults.size}")
    Log.d("Size:","${bleScanResultsDataClass.size}")
    this.promise = promise
    if (Helper.checkBluetoothScanPermission(this.reactApplicationContext, this.currentActivity!!)) {
      Thread {
          val filter = IntentFilter()
          filter.addAction(BluetoothDevice.ACTION_FOUND)
          filter.addAction(BluetoothDevice.ACTION_CLASS_CHANGED)
          filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
          this.reactApplicationContext.registerReceiver(receiver, filter)

          bluetoothAdapter?.startDiscovery()
          Log.d("Printer Module", " Bluetooth Discovery Started")


      }.start()
    }
  }
  @SuppressLint("MissingPermission")
  @ReactMethod
  private fun printImageByBluetooth(
    nameOraddress: String,
    base64Image: String,
    addresspromise: Promise
  ) {
    this.promise = addresspromise
    println("Here Inside the function in android  ")
    val pairedDevices:Set<BluetoothDevice> = bluetoothAdapter!!.bondedDevices
      Thread {
        try {
          println("Here Inside the try Thread ")
          Log.d("Printing","BL Device nameoraddress ${nameOraddress}")
          val blDevice = Helper.findBLDevice(nameOraddress, bluetoothAdapter!!, bleScanResults)!!

          Log.d("Printing","BL Device Found \n Name: ${blDevice.name} \n Address:${blDevice.address} \nMajor Device Class: ${blDevice.bluetoothClass.majorDeviceClass} \n Device Class:${blDevice.bluetoothClass.deviceClass}")
          if(stream!=null){
            stream!!.closeSocket()
          }
          stream = BluetoothStream(blDevice, this.promise!!)
          val escpos = EscPos(stream)
          val encodedBase64 = Base64.decode(base64Image, Base64.DEFAULT)
          val bitmap = BitmapFactory.decodeByteArray(encodedBase64, 0, encodedBase64.size)
          val scaledBitmap =
            Bitmap.createScaledBitmap(bitmap, bitmap.width - 40, bitmap.height, true)
          val algorithm = BitonalOrderedDither()
          val imageWrapper = RasterBitImageWrapper()
          val escposImage = EscPosImage(CoffeeImageAndroidImpl(scaledBitmap), algorithm)
          escpos.write(imageWrapper, escposImage).feed(5).cut(EscPos.CutMode.FULL).close()
        } catch (e: java.lang.Exception) {
          e.printStackTrace()
          promise?.reject("Error", e.toString())
        }
      }.start()


    }


  companion object {
    const val NAME = "SunmiExternalPrinterReactNative"
  }
}


