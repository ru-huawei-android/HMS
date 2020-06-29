package com.example.hmspushkitserverside

import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.huawei.hms.push.HmsMessageService
import com.huawei.hms.push.RemoteMessage
import java.lang.Exception

class PushService: HmsMessageService() {

    val TAG: String = "PUSH_SEVICE"

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "onNewToken")
    }

    override fun onTokenError(p0: Exception?) {
        super.onTokenError(p0)
        Log.d(TAG, "onTokenError")
    }

    override fun onMessageReceived(p0: RemoteMessage?) {
        super.onMessageReceived(p0)
        Log.d(TAG, "onMessageReceived")
    }

}