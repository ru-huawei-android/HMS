package com.huawei.agc.clouddb.myquickstart.ui

import android.app.Application
import com.huawei.agc.clouddb.myquickstart.util.CloudDB

/**
 * 1st step!
 *
 * Global initialization CloudDB (it could be done without this, but for me it does not work).
 * Do not forget to add name to the manifest
 */
class CloudDBQuickStartApplication: Application() {

    override fun onCreate() {
        super.onCreate()
            CloudDB.initAGConnectCloudDB(this)
    }
}