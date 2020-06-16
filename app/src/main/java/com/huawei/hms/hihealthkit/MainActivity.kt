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

package com.huawei.hms.hihealthkit

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.huawei.hihealth.error.HiHealthError
import com.huawei.hihealthkit.HiHealthDataQuery
import com.huawei.hihealthkit.HiHealthDataQueryOption
import com.huawei.hihealthkit.auth.HiHealthAuth
import com.huawei.hihealthkit.auth.HiHealthOpenPermissionType
import com.huawei.hihealthkit.data.HiHealthPointData
import com.huawei.hihealthkit.data.store.HiHealthDataStore
import com.huawei.hihealthkit.data.type.HiHealthPointType
import kotlinx.android.synthetic.main.activity_main.*
import timber.log.Timber
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.util.*
import java.util.concurrent.TimeUnit

@SuppressLint("SetTextI18n")
class MainActivity : AppCompatActivity(R.layout.activity_main) {

    /*
        Массив пермишенов на чтение данных, которые мы хотим получить
     */
    private var readPermissions = intArrayOf(
            HiHealthOpenPermissionType.HEALTH_OPEN_PERMISSION_TYPE_READ_USER_PROFILE_INFORMATION,
            HiHealthOpenPermissionType.HEALTH_OPEN_PERMISSION_TYPE_READ_USER_PROFILE_FEATURE,
            HiHealthOpenPermissionType.HEALTH_OPEN_PERMISSION_TYPE_READ_DATA_SET_HEART,
            HiHealthOpenPermissionType.HEALTH_OPEN_PERMISSION_TYPE_READ_DATA_SET_WALK_METADATA,
            HiHealthOpenPermissionType.HEALTH_OPEN_PERMISSION_TYPE_READ_DATA_SET_RUN_METADATA,
            HiHealthOpenPermissionType.HEALTH_OPEN_PERMISSION_TYPE_READ_DATA_SET_RIDE_METADATA,
            HiHealthOpenPermissionType.HEALTH_OPEN_PERMISSION_TYPE_READ_DATA_SET_WEIGHT,
            HiHealthOpenPermissionType.HEALTH_OPEN_PERMISSION_TYPE_READ_DATA_SET_CORE_SLEEP,
            HiHealthOpenPermissionType.HEALTH_OPEN_PERMISSION_TYPE_READ_DATA_POINT_STEP_SUM,
            HiHealthOpenPermissionType.HEALTH_OPEN_PERMISSION_TYPE_READ_DATA_POINT_DISTANCE_SUM,
            HiHealthOpenPermissionType.HEALTH_OPEN_PERMISSION_TYPE_READ_DATA_POINT_INTENSITY,
            HiHealthOpenPermissionType.HEALTH_OPEN_PERMISSION_TYPE_READ_DATA_POINT_CALORIES_SUM,
            HiHealthOpenPermissionType.HEALTH_OPEN_PERMISSION_TYPE_READ_REALTIME_HEARTRATE,
            HiHealthOpenPermissionType.HEALTH_OPEN_PERMISSION_TYPE_READ_DATA_REAL_TIME_SPORT
    )

    /*
        Массив пермишенов на запись данных, которые мы хотим получить
    */
    private var writeWeightPermissions = intArrayOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestAuthorizationBtn.setOnClickListener(requestAuthorization)
        genderBtn.setOnClickListener(getGender)
        weightBtn.setOnClickListener(getWeight)
        stepBtn.setOnClickListener(getSteps)
    }


    private val getSteps = View.OnClickListener {
        val timeout = 0
        // получаем время
        val localDateTime = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);
        val zoneTime = localDateTime.atZone(ZoneId.systemDefault());
        val endTime = zoneTime.toInstant().toEpochMilli();
        var startTime: Long = 0


        val firstDayOfWeek = getFirstDayOfWeek(Calendar.DAY_OF_WEEK)
        val firstDayOfMonth = getFirstDayOfWeek(Calendar.DAY_OF_MONTH)
        val firstDayOfYear = getFirstDayOfWeek(Calendar.DAY_OF_YEAR)

        when (radioGroup.checkedRadioButtonId) {
            R.id.day -> startTime = endTime - TimeUnit.DAYS.toMillis(1)
            R.id.week -> startTime = firstDayOfWeek
            R.id.month -> startTime = firstDayOfMonth
            R.id.year -> startTime = firstDayOfYear
        }

        // Получаем ArrayList<HiHealthPointData>, который представляет значения для каждого дня
        val hiHealthDataQuery = HiHealthDataQuery(
                HiHealthPointType.DATA_POINT_STEP_SUM,
                startTime,
                endTime,
                HiHealthDataQueryOption())
        HiHealthDataStore.execQuery(applicationContext, hiHealthDataQuery, timeout) { resultCode, data ->
            if (data != null) {
                val dataList: List<HiHealthPointData> = data as ArrayList<HiHealthPointData>
                var steps = 0;
                for (obj in dataList) {
                    steps += obj.value;
                }
                result.text = "Steps: $steps"
            } else {
                showMessage("\"Step count\"")
            }
        }
    }

    private val getGender = View.OnClickListener {
        HiHealthDataStore.getGender(applicationContext) { errorCode, gender ->
            if (errorCode == HiHealthError.SUCCESS) {
                result.text = when (gender) {
                    0 -> "Gender: Female"
                    1 -> "Gender: Male"
                    else -> "Gender: Undefined"
                }
            } else {
                showMessage("\"Basic personal information\"")
            }
        }
    }


    private val getWeight = View.OnClickListener {
        HiHealthDataStore.getWeight(applicationContext) { errorCode, weight ->
            if (errorCode == HiHealthError.SUCCESS) {
                result.text = "Weight: $weight"
            } else {
                showMessage("\"Basic measurement\"")
            }
        }
    }

    private val requestAuthorization = View.OnClickListener {
        HiHealthAuth.requestAuthorization(applicationContext, writeWeightPermissions, readPermissions) { resultCode, resultDesc ->
            when (resultCode) {
                HiHealthError.SUCCESS -> result.text = "Request Authorization success"
                HiHealthError.FAILED -> result.text = "Request Authorization failed"
                HiHealthError.PARAM_INVALIED -> result.text = "Request Authorization failed due to invalid param"
                HiHealthError.ERR_API_EXECEPTION -> result.text = "Request Authorization failed due to api exception"
                HiHealthError.ERR_PERMISSION_EXCEPTION -> result.text = "Request Authorization failed due to permission exception"
                HiHealthError.ERR_SCOPE_EXCEPTION -> result.text = "Request Authorization failed due to error scope exception"
                else -> result.text = "Undefined error"
            }

            Timber.d("requestAuthorization onResult:$resultCode")
        }
    }

    private fun showMessage(dataType: String) {
        result.text = "Something goes wrong, probably you haven't requested authorization yet. Or maybe you haven't allowed $dataType type of data"
    }

    private fun getFirstDayOfWeek(dayOf: Int): Long {
        val cal = Calendar.getInstance()
        cal[Calendar.HOUR_OF_DAY] = 0 // ! clear would not reset the hour of day !
        cal.clear(Calendar.MINUTE)
        cal.clear(Calendar.SECOND)
        cal.clear(Calendar.MILLISECOND)

        return when (dayOf) {
            Calendar.DAY_OF_WEEK -> {
                cal[dayOf] = cal.firstDayOfWeek
                return cal.timeInMillis
            }
            Calendar.DAY_OF_MONTH -> {
                cal[dayOf] = 1
                return cal.timeInMillis
            }
            Calendar.DAY_OF_YEAR -> {
                cal[dayOf] = 1
                return cal.timeInMillis
            }
            else -> 0
        }
    }
}
