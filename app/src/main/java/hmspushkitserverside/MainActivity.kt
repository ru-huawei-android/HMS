package com.example.hmspushkitserverside

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.huawei.agconnect.config.AGConnectServicesConfig
import com.huawei.hms.aaid.HmsInstanceId

class MainActivity : AppCompatActivity() {

    private val TAG: String = "PUSH_TEST"
    private lateinit var pushToken: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val navView: BottomNavigationView = findViewById(R.id.nav_view)

        val navController = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        getIntentData(intent, "onCreate()")
        obtainToken()
        val i = Intent(this, PushService::class.java)
        startService(i)
    }

    private fun getIntentData(intent: Intent?, method: String) {
        if (intent != null) {
            // Developers can use the following three lines of code to obtain the values for dotting statistics.
            val msgid = intent.getStringExtra("_push_msgid")
            val cmdType = intent.getStringExtra("_push_cmd_type")
            val notifyId = intent.getIntExtra("_push_notifyid", -1)
            val bundle = intent.extras
            if (bundle != null) {
                for (key in bundle.keySet()) {
                    val content = bundle.get(key)
                    Log.i(TAG + " " + method, "key = $key, content = $content")
                }
            }
            Log.i(
                TAG + " " + method,
                "_push_msgid = $msgid, _push_cmd_type = $cmdType, _push_notifyid = $notifyId"
            )
        } else {
            Log.i(TAG + " " + method, "intent is null")
        }
    }

    /**
     * Получить токен
     */
    private fun obtainToken() {
        Log.i(TAG, "get token: begin")
        // get token
        object : Thread() {
            override fun run() {
                try {
                    val appId =
                        AGConnectServicesConfig.fromContext(this@MainActivity)
                            .getString("client/app_id")
                    Log.i(TAG, "appId:$appId")
                    pushToken = HmsInstanceId.getInstance(this@MainActivity).getToken(appId, "HCM")
                    if (!TextUtils.isEmpty(pushToken)) {
                        Log.i(TAG, "get token:$pushToken")
                    }
                } catch (e: Exception) {
                    Log.i(TAG, "getToken failed, $e")
                }
            }
        }.start()
    }
}