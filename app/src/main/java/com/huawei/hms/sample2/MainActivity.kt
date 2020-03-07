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

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.huawei.agconnect.config.AGConnectServicesConfig
import com.huawei.hms.aaid.HmsInstanceId
import com.huawei.hms.common.ApiException
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

//::created by c7j at 18.02.2020 13:07
class MainActivity : AppCompatActivity(R.layout.activity_main) {

    private lateinit var hiAnalyticsWrapper: HiAnalyticsWrapper
    private lateinit var receiver: PushReceiver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setUpAnalytics()
        initPushReceiver()
    }


    private fun initPushReceiver() {
        receiver = PushReceiver()
        val filter = IntentFilter()
        filter.addAction(HuaweiPushService.HUAWEI_PUSH_ACTION)
        registerReceiver(receiver, filter)
        log("Token is: " + getToken())
    }


    /**
     * This method is used to obtain a token required for accessing HUAWEI Push Kit.
     * If there is no local AAID, this method will automatically generate an AAID
     * when it is called because the Huawei Push server needs to generate a token based on the AAID.
     * This method is a synchronous method, and you cannot call it in the main thread.
     * Otherwise, the main thread may be blocked.
     *
     * If the EMUI version is 10.0 or later on a Huawei device, a token is using through the getToken method.
     * If the getToken method fails to be called, HUAWEI Push Kit automatically caches the token request
     * and calls the method again. A token will then be returned through the onNewToken method.
     * If the EMUI version on a Huawei device is earlier than 10.0 and no token is returned
     * using the getToken method, a token will be returned using the onNewToken method.
     * (Из официальной документации пункта 2.1.2)
     * https://developer.huawei.com/consumer/en/doc/development/HMS-Guides/push-basic-capability#h2-1576218800370
     * Версию EMUI можно посмотреть в константе Build.DISPLAY
     *
     * Если сервер постоянно возвращает ошибку "token expired"
     * УДАЛИТЕ из полученного пуш токена "\" эскйеп-символ прежде чем отправлять запрос
     * Список символов допустимых в push token указан здесь (Пункт 2.1.1.4):
     * https://developer.huawei.com/consumer/en/doc/development/HMS-Guides/push-basic-capability#h2-1576218800370
     */
    private fun getToken() {
        log(Build.DISPLAY)
        CoroutineScope(Dispatchers.IO).launch(handler) {
            try { // read from agconnect-services.json
                val appId = AGConnectServicesConfig.fromContext(this@MainActivity).getString("client/app_id")
                val token = HmsInstanceId.getInstance(this@MainActivity).getToken(appId, "HCM")
                log("get token: $token")
                if (!TextUtils.isEmpty(token)) {
                    sendRegTokenToServer(token)
                }
                log("get token: $token")
            } catch (e: ApiException) {
                log("get token failed, $e")
            }
        }
    }


    private fun sendRegTokenToServer(token: String) {
        //TODO: send token to your server
    }


    private fun setUpAnalytics() {
        hiAnalyticsWrapper = HiAnalyticsWrapper(applicationContext)
        with(hiAnalyticsWrapper) {
            setUpUserId()
            reportScreen(this@MainActivity, javaClass.simpleName)
        }
    }


    private val handler = CoroutineExceptionHandler { _, exception ->
        //TODO: handle error
    }


    class PushReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val bundle = intent.extras
            if (bundle?.getString("msg") != null) {
                val content = bundle.getString("msg")
                log(content)
            }
        }
    }
}


fun log(message: Any?) = Log.e("#TEST", "$message")