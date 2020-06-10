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

/* created by Evgeny Sobko 6 June 2020 */

package com.huawei.hms.sample2

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.huawei.agconnect.config.AGConnectServicesConfig
import com.huawei.hms.aaid.HmsInstanceId
import com.huawei.hms.common.ApiException
import com.huawei.hms.push.HmsMessaging
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

class MainActivity : AppCompatActivity(R.layout.activity_main), CoroutineScope {

    override val coroutineContext: CoroutineContext
        get() = Job()

    private lateinit var messaging: HmsMessaging
    private lateinit var pushToken: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()

        messaging = HmsMessaging.getInstance(applicationContext)

        buttonGetToken.setOnClickListener { getToken() }
    }

    /* Получаем токен для отправки Push-уведомлений */
    private fun getToken() {
        launch(Dispatchers.IO) {
            try {
                val appId = AGConnectServicesConfig.fromContext(applicationContext).getString("client/app_id")
                pushToken = HmsInstanceId.getInstance(this@MainActivity).getToken(appId, "HCM")
                if (pushToken.isNotEmpty()) {

                    /* После того, как токен был получен, можем подписываться на топики */
                    messaging.subscribe("push_test").addOnCompleteListener {
                        log("subscribe complete, " + it.isSuccessful + ", " + it.exception)
                    }.addOnSuccessListener {

                        /* Если все прошло успешно */
                        log("subscribe success")
                        toast("subscribed")

                    }.addOnFailureListener {

                        /* При ошибке */
                        log("subscribe fail," + it.message)
                    }
                }
            } catch (e: ApiException) {
                log(e.message.toString())
            }
        }
    }

    /* Отписываемся от топиков, на которые подписаны */
    override fun onDestroy() {
        messaging.unsubscribe("push_test")
        super.onDestroy()
    }
}