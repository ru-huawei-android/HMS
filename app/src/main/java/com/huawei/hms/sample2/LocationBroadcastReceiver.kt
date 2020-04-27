package com.huawei.hms.sample2

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.huawei.hms.location.*


//::created by c7j at 30.03.2020 01:19
open class LocationBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        log("onReceive() hms location broadcast")
        val deliverIntent = Intent(ACTION_DELIVER_LOCATION)
        val action = intent.action
        if (action == ACTION_PROCESS_LOCATION) {
//            val activityConversionResult = ActivityConversionResponse.getDataFromIntent(intent)
//            if (activityConversionResult != null) {
//                val list =
//                        activityConversionResult.activityConversionDatas as ArrayList<ActivityConversionData>
//                deliverIntent.putParcelableArrayListExtra(EXTRA_HMS_LOCATION_CONVERSION, list)
//            }

            val activityRecognitionResult = ActivityIdentificationResponse.getDataFromIntent(intent)
            if (activityRecognitionResult != null && MainActivity.isListenActivityIdentification) {
                val list =
                        activityRecognitionResult.activityIdentificationDatas as ArrayList<ActivityIdentificationData>
                deliverIntent.putParcelableArrayListExtra(EXTRA_HMS_LOCATION_RECOGNITION, list)
            }

//            if (LocationResult.hasResult(intent)) {
//                val result = LocationResult.extractResult(intent)
//                if (result != null) {
//                    val list = result.locations as ArrayList<Location>
//                    deliverIntent.putParcelableArrayListExtra(EXTRA_HMS_LOCATION_RESULT, list)
//                }
//            }
//
//            if (LocationAvailability.hasLocationAvailability(intent)) {
//                val locationAvailability = LocationAvailability.extractLocationAvailability(intent)
//                deliverIntent.putExtra(EXTRA_HMS_LOCATION_AVAILABILITY, locationAvailability?.isLocationAvailable == true)
//            }
        }
        context.sendBroadcast(deliverIntent)
    }


    companion object {
        const val ACTION_PROCESS_LOCATION = "com.huawei.hms.location.ACTION_PROCESS_LOCATION"
        const val ACTION_DELIVER_LOCATION = "ACTION_DELIVER_LOCATION"
        const val EXTRA_HMS_LOCATION_RECOGNITION = "EXTRA_HMS_LOCATION_RECOGNITION"
        const val EXTRA_HMS_LOCATION_CONVERSION = "EXTRA_HMS_LOCATION_CONVERSION"
        const val EXTRA_HMS_LOCATION_RESULT = "EXTRA_HMS_LOCATION_RESULT"
        const val EXTRA_HMS_LOCATION_AVAILABILITY = "EXTRA_HMS_LOCATION_AVAILABILITY"
        const val REQUEST_PERIOD = 5000L


        fun statusFromCode(var1: Int): String? = when (var1) {
                100 -> "VEHICLE"
                101 -> "BIKE"
                102 -> "FOOT"
                103 -> "STILL"
                104 -> "OTHERS"
                105 -> "TILTING"
                107 -> "WALKING"
                108 -> "RUNNING"
                else -> "UNDEFINED"
        }
    }
}
