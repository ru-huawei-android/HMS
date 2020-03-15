/*
 * Copyright 2020. Huawei Technologies Co., Ltd. All rights reserved.

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/

package com.huawei.hms.sample2

import android.Manifest
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.CompoundButton
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.huawei.hmf.tasks.OnSuccessListener
import com.huawei.hmf.tasks.Task
import com.huawei.hms.location.FusedLocationProviderClient
import com.huawei.hms.location.LocationServices
import kotlinx.android.synthetic.main.activity_main.*
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method


//::created by c7j at 09.03.2020 19:32
class MainActivity : AppCompatActivity(R.layout.activity_main) {

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        requestPermission()

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        btnCheckLocation.setOnClickListener {
            requestLocation()
        }

        toggleRecognition.setOnCheckedChangeListener { _: CompoundButton, enabled: Boolean ->
            requestActivityRecognitionPermission(this)
            if (enabled) {
                log("enabled")
            } else {
                log("disabled")
                stopUserActivityTracking()
            }
        }

        log("EMUI: " + readEMUIVersion())
        log("EMUI: " + extractEmuiVersion())
    }


    @SuppressLint("PrivateApi")
    private fun Any?.readEMUIVersion() : String {
        try {
            val propertyClass = Class.forName("android.os.SystemProperties")
            val method: Method = propertyClass.getMethod("get", String::class.java)
            var versionEmui = method.invoke(propertyClass, "ro.build.version.emui") as String
            if (versionEmui.startsWith("EmotionUI_")) {
                versionEmui = versionEmui.substring(10, versionEmui.length)
            }
            return versionEmui
        } catch (e: ClassNotFoundException) {
            e.printStackTrace()
        } catch (e: NoSuchMethodException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        } catch (e: InvocationTargetException) {
            e.printStackTrace()
        }
        return ""
    }

    @TargetApi(3)
    fun Any?.extractEmuiVersion() : String {
        return try {
            val line: String = Build.DISPLAY
            log(line)
            val spaceIndex = line.indexOf(" ")
            val lastIndex = line.indexOf("#")
            if (lastIndex != -1) {
                line.substring(spaceIndex, lastIndex)
            } else line.substring(spaceIndex)
        } catch (e: Exception) { "" }
    }


    @SuppressLint("SetTextI18n")
    private fun requestLocation() {
        try {
            tvPosition.text = getString(R.string.get_last_searching)
            val lastLocation: Task<Location> = fusedLocationProviderClient.lastLocation
            lastLocation.addOnSuccessListener(OnSuccessListener { location ->
                if (location == null) {
                    tvPosition.text = getString(R.string.get_last_failed)
                    log("location is null - did you grant the required permission?")
                    requestPermission()
                    return@OnSuccessListener
                }
                with(location) { tvPosition.text = "$longitude, $latitude" }
                return@OnSuccessListener
            }).addOnFailureListener { e ->
                tvPosition.text = getString(R.string.get_last_null)
                log("failed: $e")
            }
        } catch (e: Exception) {
            log("exception: $e")
        }
    }


    private fun startUserActivityTracking() {

    }

    private fun stopUserActivityTracking() {

    }

    private fun requestPermission() {
        // You must have the ACCESS_COARSE_LOCATION or ACCESS_FINE_LOCATION permission.
        // Otherwise, the location service is unavailable.
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            log("sdk < 28 Q")
            if (ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                        ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
            ) {
                val strings = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
                ActivityCompat.requestPermissions(this, strings, 1)
            }
        } else {
            log("sdk >= 28 Q")
            if (ActivityCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED
            ) {
                val strings = arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                ActivityCompat.requestPermissions(this, strings, 2)
            }
        }
    }

    private fun requestActivityRecognitionPermission(context: Context?) {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            if (ActivityCompat.checkSelfPermission(this,
            "com.huawei.hms.permission.ACTIVITY_RECOGNITION") != PackageManager.PERMISSION_GRANTED) {
                val permissions = arrayOf("com.huawei.hms.permission.ACTIVITY_RECOGNITION")
                ActivityCompat.requestPermissions((context as Activity?)!!, permissions, 3)
                log("requestActivityRecognitionPermission: apply permission")
            }
        } else {
            if (ActivityCompat.checkSelfPermission(this,
                            Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED) {
                val permissions = arrayOf(Manifest.permission.ACTIVITY_RECOGNITION)
                ActivityCompat.requestPermissions((context as Activity?)!!, permissions, 4)
                log("requestActivityRecognitionPermission: apply permission")
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
            if (grantResults.size > 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                log("onRequestPermissionsResult: apply LOCATION PERMISSION successful")
                requestLocation()
            } else {
                log("onRequestPermissionsResult: apply LOCATION PERMISSION failed")
            }
        }
        if (requestCode == 2) {
            if (grantResults.size > 2 && grantResults[2] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                log("onRequestPermissionsResult: apply ACCESS_BACKGROUND_LOCATION successful")
                requestLocation()
            } else {
                log("onRequestPermissionsResult: apply ACCESS_BACKGROUND_LOCATION  failed")
            }
        }
        if (requestCode == 3) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                log("onRequestPermissionsResult: apply com.huawei.hms.permission.ACTIVITY_RECOGNITION successful")
                startUserActivityTracking()
            } else {
                log("onRequestPermissionsResult: apply com.huawei.hms.permission.ACTIVITY_RECOGNITION  failed")
            }
        }
        if (requestCode == 4 && Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                log("onRequestPermissionsResult: apply " + Manifest.permission.ACTIVITY_RECOGNITION + " successful")
                startUserActivityTracking()
            } else {
                log("onRequestPermissionsResult: apply " + Manifest.permission.ACTIVITY_RECOGNITION + " failed")
            }
        }
    }
}


fun log(message: Any?) = Log.e("#TEST", "$message")