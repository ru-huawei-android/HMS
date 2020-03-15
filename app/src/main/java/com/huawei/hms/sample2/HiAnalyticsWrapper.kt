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

import android.content.Context
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

    // Подробнее по аналитике в ветке v4-analytics-crash-kotlin

    // Связывает сессию клиента с некоторым его идентификатором, например его AAID - подробнее о AAID
    // по этой ссылке: https://developer.huawei.com/consumer/en/doc/development/HMS-Guides/Development-Guide#h1-6-accessing-analytics
    fun setUpUserId() {
        hiAnalyticsInstance?.setUserId(HmsInstanceId.getInstance(context).id)
        log("AAID: " + HmsInstanceId.getInstance(context).id)
    }

}