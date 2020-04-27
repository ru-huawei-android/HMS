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
import android.app.Activity
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
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
import com.huawei.hms.location.*
import com.huawei.hms.sample2.LocationBroadcastReceiver.Companion.ACTION_DELIVER_LOCATION
import com.huawei.hms.sample2.LocationBroadcastReceiver.Companion.ACTION_PROCESS_LOCATION
import com.huawei.hms.sample2.LocationBroadcastReceiver.Companion.EXTRA_HMS_LOCATION_AVAILABILITY
import com.huawei.hms.sample2.LocationBroadcastReceiver.Companion.EXTRA_HMS_LOCATION_RECOGNITION
import com.huawei.hms.sample2.LocationBroadcastReceiver.Companion.EXTRA_HMS_LOCATION_CONVERSION
import com.huawei.hms.sample2.LocationBroadcastReceiver.Companion.EXTRA_HMS_LOCATION_RESULT
import com.huawei.hms.sample2.LocationBroadcastReceiver.Companion.REQUEST_PERIOD
import kotlinx.android.synthetic.main.activity_main.*
import java.util.ArrayList


//::created by c7j at 09.03.2020 19:32
class MainActivity : AppCompatActivity(R.layout.activity_main) {

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var activityIdentificationService: ActivityIdentificationService
    private var pendingIntent: PendingIntent? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        requestPermission()

        pendingIntent = getPendingIntent()
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        activityIdentificationService = ActivityIdentification.getService(this)

        btnCheckLocation.setOnClickListener { requestLastLocation() }

        toggleRecognition.setOnCheckedChangeListener { _: CompoundButton, enabled: Boolean ->
            requestActivityRecognitionPermission(this)
            if (enabled) startUserActivityTracking() else stopUserActivityTracking()
        }
    }


    @SuppressLint("SetTextI18n")
    private fun requestLastLocation() {
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


    private val gpsReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == ACTION_DELIVER_LOCATION) {
                updateActivityIdentificationUI(intent.extras?.getParcelableArrayList(EXTRA_HMS_LOCATION_RECOGNITION))
                updateActivityConversionUI(intent.extras?.getParcelableArrayList(EXTRA_HMS_LOCATION_CONVERSION))
                updateLocationsUI(intent.extras?.getParcelableArrayList(EXTRA_HMS_LOCATION_RESULT))
            }
        }
    }


    private fun getPendingIntent(): PendingIntent? {
        val intent = Intent(this, LocationBroadcastReceiver::class.java)
        intent.action = ACTION_PROCESS_LOCATION
        return PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }


    override fun onDestroy() {
        super.onDestroy()
        toggleRecognition.isChecked = false
    }


    fun updateActivityIdentificationUI(statuses: ArrayList<ActivityIdentificationData>?) {
        statuses?.let {
            var out = ""
            statuses.forEach {
                out += LocationBroadcastReceiver.statusFromCode(it.identificationActivity)
                out += " "
            }
            tvRecognition.text = out
        } ?: run { tvRecognition.text = getString(R.string.str_activity_recognition_failed) }
    }


    fun updateActivityConversionUI(statuses: ArrayList<ActivityConversionData>?) {
        statuses?.let {
            var out = ""
            statuses.forEach {
                out += LocationBroadcastReceiver.statusFromCode(it.conversionType)
                out += " "
            }
            tvConversion.text = out
        } ?: run { tvConversion.text = getString(R.string.str_activity_conversion_failed) }
    }


    fun updateLocationsUI(locations: ArrayList<Location>?) {
        locations?.let {
            var out = ""
            locations.forEach {
                out += it.toString()
                out += " "
            }
            tvLocations.text = out
        } ?: run { tvLocations.text = getString(R.string.str_activity_locations_failed) }
    }


    private fun startUserActivityTracking() {
        registerReceiver(gpsReceiver, IntentFilter(ACTION_DELIVER_LOCATION))
        requestActivityUpdates(REQUEST_PERIOD)
    }


    private fun stopUserActivityTracking() {
        unregisterReceiver(gpsReceiver)
        removeActivityUpdates()
    }

    private fun requestActivityUpdates(detectionIntervalMillis: Long) {
        try {
            if (pendingIntent != null) removeActivityUpdates()
            pendingIntent = getPendingIntent()
            isListenActivityIdentification = true
            activityIdentificationService.createActivityIdentificationUpdates(detectionIntervalMillis, pendingIntent)
                    .addOnSuccessListener { log("createActivityIdentificationUpdates onSuccess") }
                    .addOnFailureListener { e -> log("createActivityIdentificationUpdates onFailure:" + e.message) }
        } catch (e: java.lang.Exception) {
            log("createActivityIdentificationUpdates exception:" + e.message)
        }
    }

    private fun removeActivityUpdates() {
        try {
            isListenActivityIdentification = false
            log("start to removeActivityUpdates")
            activityIdentificationService.deleteActivityIdentificationUpdates(pendingIntent)
                    .addOnSuccessListener { log("deleteActivityIdentificationUpdates onSuccess") }
                    .addOnFailureListener { e -> log("deleteActivityIdentificationUpdates onFailure:" + e.message) }
        } catch (e: java.lang.Exception) {
            log("removeActivityUpdates exception:" + e.message)
        }
    }


    //-------------------------------------------
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
                requestLastLocation()
            } else {
                log("onRequestPermissionsResult: apply LOCATION PERMISSION failed")
            }
        }
        if (requestCode == 2) {
            if (grantResults.size > 2 && grantResults[2] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                log("onRequestPermissionsResult: apply ACCESS_BACKGROUND_LOCATION successful")
                requestLastLocation()
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

    companion object {
        var isListenActivityIdentification = true
    }
}


fun log(message: Any?) = Log.e("#TEST", "$message")