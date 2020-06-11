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

package com.huawei.hms.dtmfirebase

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.huawei.hms.analytics.HiAnalytics
import com.huawei.hms.analytics.HiAnalyticsInstance
import com.huawei.hms.analytics.HiAnalyticsTools
import kotlinx.android.synthetic.main.activity_main.*

private const val EVENT_ID = "Purchase"

class MainActivity : AppCompatActivity(R.layout.activity_main) {

    private lateinit var instance: HiAnalyticsInstance

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Для настройки сервиса лучше всего воспользоваться пошаговой инструкцией
        // https://developer.huawei.com/consumer/en/codelab/HMSDTMKit/index.html#0
        // За некоторым исключением: при создании Тэга в поле "Extension" нужно выбрать Google Analytics (Firebase).
        // На шаге "Creating a Condition" обязательно должно быть создано условие для предзаданной переменной "Event Name",
        // в котором сравниваемое значение должно совпадать с EVENT_ID.
        // Необходимо сгенерировать и заменить следующие файлы:
        // 1. app\agconnect-services.json
        // 2. app\google-services.json
        // 3. app\src\main\assets\containers\DTM-...

        instance = HiAnalytics.getInstance(this)
        HiAnalyticsTools.enableLog()

        send_event_button.setOnClickListener { sendEvent() }
    }

    private fun sendEvent(){
        val bundle = Bundle().apply {
            putLong("quantity", 100L)
            putDouble("price", 999.0)
            putString("currency", "CNY")
        }

        instance.onEvent(EVENT_ID, bundle)
    }
}