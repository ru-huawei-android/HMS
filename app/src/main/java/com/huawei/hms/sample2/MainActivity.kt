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

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.huawei.agconnect.remoteconfig.AGConnectConfig
import com.huawei.agconnect.remoteconfig.ConfigValues
import kotlinx.android.synthetic.main.activity_main.*


//::created by c7j at 15.03.2020 21:08
class MainActivity : AppCompatActivity(R.layout.activity_main) {

    private lateinit var hiAnalyticsWrapper: HiAnalyticsWrapper
    private lateinit var remoteConfig: AGConnectConfig


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        updateRemoteConfig()

        hiAnalyticsWrapper = HiAnalyticsWrapper(applicationContext)
        hiAnalyticsWrapper.setUpUserId()
    }


    @SuppressLint("SetTextI18n")
    private fun updateRemoteConfig() {
        tvRemoteConfig.text = "loading..."
        remoteConfig = AGConnectConfig.getInstance()
//        remoteConfig.applyDefault(R.xml.remote_config_huawei_default)   //set defaults from resources file
        // Or set defaults from map object (only string, boolean and numeral values; else are ignored)
        setUpDefaultsFromMap()

        // Can get last fetched values like:
        val isOffline = false
        if (isOffline) {
            val last: ConfigValues = remoteConfig.loadLastFetched()
            remoteConfig.apply(last)
        }

        /**
         * If the getValueAs() conversion fails, the default value of the requested data type is returned:
         * @see com.huawei.agconnect.remoteconfig.AGConnectConfig.DEFAULT
         */
        log(    "Before: \n" +
                remoteConfig.getValueAsString("test1") + "\n" +
                remoteConfig.getValueAsBoolean("test2") + "\n" +
                remoteConfig.getValueAsLong("test3") + "\n" +
                remoteConfig.getValueAsDouble("test4") + "\n" +
                remoteConfig.getValueAsString("test5") + "\n" +
                "source: " + remoteConfig.getSource("test1")
        )

        // All supported values:
        // https://developer.huawei.com/consumer/en/doc/development/AppGallery-connect-References/configvalues
        // How fetch() works:
        // https://developer.huawei.com/consumer/en/doc/development/AppGallery-connect-References/agconnectconfig#h1-1577525487219
        remoteConfig.fetch()  //fetch(0)
                .addOnSuccessListener { configValues ->
                    remoteConfig.apply(configValues)
                    log("After: \n" +
                            remoteConfig.getValueAsString("test1") + "\n" +
                            remoteConfig.getValueAsBoolean("test2") + "\n" +
                            remoteConfig.getValueAsLong("test3") + "\n" +
                            remoteConfig.getValueAsDouble("test4") + "\n" +
                            remoteConfig.getValueAsString("test5") + "\n" +
                            "source: " + remoteConfig.getSource("test1"))
                    tvRemoteConfig.text = "success"
                }
                .addOnFailureListener {
                    exception -> log(exception)
                    tvRemoteConfig.text = "exception: $exception"
                }


    }

    private fun setUpDefaultsFromMap() {
        val map: MutableMap<String, Any> = HashMap()
        map["test1"] = "test1"
        map["test2"] = "false"
        map["test3"] = 123
        map["test4"] = 123.456
        map["test5"] = "test-test-from-map"
        remoteConfig.applyDefault(map)
    }

}


fun log(message: Any?) = Log.e("#TEST", "$message")