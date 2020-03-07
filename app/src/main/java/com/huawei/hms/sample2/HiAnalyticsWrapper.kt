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

import android.annotation.TargetApi
import android.app.Activity
import android.content.Context
import android.os.Build
import android.os.Bundle
import com.huawei.hms.aaid.HmsInstanceId
import com.huawei.hms.analytics.HiAnalytics
import com.huawei.hms.analytics.HiAnalyticsInstance
import com.huawei.hms.api.ConnectionResult
import com.huawei.hms.api.HuaweiApiAvailability

//::created by c7j at 18.02.2020 13:08
class HiAnalyticsWrapper(var context: Context?) {

    private var hiAnalyticsInstance: HiAnalyticsInstance? = null

    init {
        hiAnalyticsInstance = if (isHmsAvailable(context)) HiAnalytics.getInstance(context) else null
//        if (BuildConfig.DEBUG) HiAnalyticsTools.enableLog()
//        if (BuildConfig.DEBUG) hiAnalyticsInstance?.setAnalyticsEnabled(false)
//        if (BuildConfig.DEBUG) hiAnalyticsInstance?.setAutoCollectionEnabled(false)
    }


    private fun isHmsAvailable(context: Context?) = HuaweiApiAvailability.getInstance()
            .isHuaweiMobileServicesAvailable(context) == ConnectionResult.SUCCESS

    // Ограничение по событиям: 500 на приложение, имя события ограничено 256 символами
    fun reportEventNoBundle() = hiAnalyticsInstance?.onEvent(EVENT_NO_BUNDLE, null)

    // Аналитика не отображает bundle событий пока вы не добавите их и их параметры в
    // Анализ -> Расширенная Аналитика -> Мета-менеджмент -> Мероприятие
    // Ограничение по параметрам 25 на событие, 100 на одно приложение, имя параметра не более 256 символов
    fun reportEventStringBundle() {
        val bundle = Bundle()
        bundle.putString(PARAM_1, "PEW PEW")
        bundle.putString(PARAM_2, "PEW PEW")
        hiAnalyticsInstance?.onEvent(EVENT_WITH_BUNDLE, bundle)
    }

    @TargetApi(4)
    fun reportEventDeviceInfoBundle() {
        val bundle = Bundle()
        bundle.putString(PARAM_DEVICE, Build.DEVICE)
        bundle.putString(PARAM_MANUFACTURER, Build.MANUFACTURER)
        bundle.putString(PARAM_MODEL, Build.MODEL)
        hiAnalyticsInstance?.onEvent(EVENT_WITH_DEVICE_INFO_BUNDLE, bundle)
    }

    // Передает информацию о том, на каком экране пользователь проводит свое время
    // Если событие задано, то Web-интерфейсе можно посмотреть наиболее популярные у пользователей экраны
    fun reportScreen(activity: Activity?, name: String) {
        hiAnalyticsInstance?.setCurrentActivity(activity, name, name)
    }

    // Связывает сессию клиента с некоторым его идентификатором, например его AAID - подробнее о AAID
    // по этой ссылке: https://developer.huawei.com/consumer/en/doc/development/HMS-Guides/Development-Guide#h1-6-accessing-analytics
    fun setUpUserId() {
        hiAnalyticsInstance?.setUserId(HmsInstanceId.getInstance(context).id)
        log("AAID: " + HmsInstanceId.getInstance(context).id)
    }

    companion object {
        const val EVENT_NO_BUNDLE = "EVENT_NO_BUNDLE"
        const val EVENT_WITH_BUNDLE = "EVENT_WITH_BUNDLE"
        const val EVENT_WITH_DEVICE_INFO_BUNDLE = "EVENT_WITH_DEVICE_INFO_BUNDLE"

        const val PARAM_1 = "ONE"
        const val PARAM_2 = "TWO"
        const val PARAM_DEVICE = "Device"
        const val PARAM_MANUFACTURER = "Manufacturer"
        const val PARAM_MODEL = "Model"
    }
}