package com.huawei.hms.ads

import android.app.Application

class AdsApplication: Application() {

    override fun onCreate() {
        super.onCreate()
        HwAds.init(this)
    }

}