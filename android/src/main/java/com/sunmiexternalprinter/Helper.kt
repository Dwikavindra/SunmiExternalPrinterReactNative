package com.sunmiexternalprinter


import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.WritableArray
import com.facebook.react.bridge.WritableMap
import com.sunmiexternalprinter.NSD.NSDDevice
import java.util.SortedSet
import java.util.regex.Pattern

class Helper {

  companion object{
    fun checkBluetoothConnectPermission(context: Context,activity: Activity):Boolean{
      if (ActivityCompat.checkSelfPermission(
          context,
          Manifest.permission.BLUETOOTH_CONNECT
        ) != PackageManager.PERMISSION_GRANTED
      ) {
        ActivityCompat.requestPermissions(activity,
          arrayOf(Manifest.permission.BLUETOOTH_CONNECT),
          1)
      }
      return true

    }
    fun checkBluetoothScanPermission(context: Context,activity: Activity):Boolean{
      if (ActivityCompat.checkSelfPermission(
          context,
          Manifest.permission.BLUETOOTH_SCAN
        ) != PackageManager.PERMISSION_GRANTED
      ) {
        ActivityCompat.requestPermissions(activity,
          arrayOf(Manifest.permission.BLUETOOTH_SCAN),
          1)
      }
      return true

    }
    fun SetBLDevicestoWriteableArray(blDevices:MutableList<BTDevice>,context:Context,activity: Activity): WritableMap {
      val resultUnFiltered: WritableArray = Arguments.createArray()
      val resultFiltered:WritableArray= Arguments.createArray()
      var deviceMap: WritableMap = Arguments.createMap()
      var mapResult:WritableMap= Arguments.createMap()

      if(this.checkBluetoothConnectPermission(context,activity)) {
        for (blDevice in blDevices) {
          deviceMap.putString("name",blDevice.name)
          deviceMap.putString("address",blDevice.address)
          resultUnFiltered.pushMap(deviceMap)
          deviceMap= Arguments.createMap()
        }
        for (blDevice in blDevices) {
          Log.d("Helper","device filtered beforee if statement ${blDevice.name}")
          if(blDevice.majorDeviceClass==1536 || blDevice.deviceClass==1572){
            Log.d("Helper","device filtered ${blDevice.name}")
            deviceMap.putString("name",blDevice.name)
            deviceMap.putString("address",blDevice.address)
            resultFiltered.pushMap(deviceMap)
            deviceMap= Arguments.createMap()
          }

        }
      }
      mapResult.putArray("filtered_result",resultFiltered)
      mapResult.putArray("unfiltered_result", resultUnFiltered)
      return mapResult

    }
    @SuppressLint("MissingPermission")
    fun setBluetoothDevicetoWritableArray(pairedDevices:MutableList<BluetoothDevice>):WritableArray{
      val results: WritableArray = Arguments.createArray()
      var deviceMap: WritableMap = Arguments.createMap()
      for(blDevice in pairedDevices){
        deviceMap.putString("name",blDevice.name)
        deviceMap.putString("address",blDevice.address)
        results.pushMap(deviceMap)
        deviceMap=Arguments.createMap()
      }
      return results
    }

    fun isMacAddress(input: String?): Boolean {
      // Define regular expressions for valid MAC address patterns
      val macAddressPattern = "^([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})$"

      // Compile the regular expression pattern
      val pattern: Pattern = Pattern.compile(macAddressPattern)

      // Check if the input matches the pattern
      return pattern.matcher(input).matches()
    }
    @SuppressLint("MissingPermission")
    fun findBLDevice(address: String, bluetoothAdapter:BluetoothAdapter, blescanResults: SortedSet<BluetoothDeviceComparable>): BluetoothDevice? {
      try {
        println("Here in findBLDevice")
        val pairedDevices = bluetoothAdapter.bondedDevices
        println("This is ${address}")
        pairedDevices.forEach{
          if( address == it.address){
            return it
          }
        }
        blescanResults.forEach{
         if( address == it.bluetoothDevice.address){
          return it.bluetoothDevice
         }
        }
        return null
      }
      catch(e:Error){
        Log.e("Error findBL","BluetoothDevice Not Found")
        throw Exception("Bluetooth Device with the name or address ${address} Not Found")

      }
    }

    fun setNSDDevicesToWritableArray(nsdDevices:MutableList<NSDDevice>):WritableArray{
      val results: WritableArray = Arguments.createArray()
      var deviceMap: WritableMap = Arguments.createMap()
      for(nsdDevice in nsdDevices){
        deviceMap.putString("printername",nsdDevice.serviceName)
        deviceMap.putString("ip",nsdDevice.host)
        deviceMap.putString("port",nsdDevice.port)
        results.pushMap(deviceMap)
        deviceMap=Arguments.createMap()
      }
      return results
    }

  }




}
