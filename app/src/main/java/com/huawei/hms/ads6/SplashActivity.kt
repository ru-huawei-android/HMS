package com.huawei.hms.ads6

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.os.Handler
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.huawei.hms.ads.AdParam
import com.huawei.hms.ads.AudioFocusType
import com.huawei.hms.ads.R
import com.huawei.hms.ads.splash.SplashAdDisplayListener
import com.huawei.hms.ads.splash.SplashView.SplashAdLoadListener
import kotlinx.android.synthetic.main.activity_splash.*

class SplashActivity: AppCompatActivity(R.layout.activity_splash) {

    private val AD_TIMEOUT = 5000
    private val MSG_AD_TIMEOUT = 1001
    private var hasPaused = false
    private val timeoutHandler = Handler(Handler.Callback {
        if (this.hasWindowFocus()) {
            jump()
        }
        false
    })

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadSplashAd()
    }

    private fun loadSplashAd() {
        splashAdView.setAdDisplayListener(adDisplayListener)
        splashAdView.setLogoResId(R.mipmap.ic_launcher)
        splashAdView.setMediaNameResId(R.string.app_name)
        splashAdView.setAudioFocusType(AudioFocusType.NOT_GAIN_AUDIO_FOCUS_WHEN_MUTE)
        splashAdView.load(getString(R.string.ad_splash), ActivityInfo.SCREEN_ORIENTATION_PORTRAIT, AdParam.Builder()
            .build(), splashAdLoadListener)
        timeoutHandler.removeMessages(MSG_AD_TIMEOUT)
        timeoutHandler.sendEmptyMessageDelayed(MSG_AD_TIMEOUT, AD_TIMEOUT.toLong())
    }

    // Переходим к основному окну приложения после того как завершился показ рекламы
    private fun jump() {
        if (!hasPaused) {
            hasPaused = true
            startActivity(Intent(this, MainActivity::class.java))
            Handler().postDelayed({ finish() }, 1000)
        }
    }

    private val splashAdLoadListener: SplashAdLoadListener = object : SplashAdLoadListener() {
        override fun onAdLoaded() {
            Toast.makeText(applicationContext, getString(R.string.status_load_ad_success), Toast.LENGTH_SHORT).show()
        }

        override fun onAdFailedToLoad(errorCode: Int) {
            Toast.makeText(applicationContext, getString(R.string.status_load_ad_fail) + errorCode, Toast.LENGTH_SHORT).show()
            jump()
        }

        override fun onAdDismissed() {
            Toast.makeText(applicationContext, getString(R.string.status_ad_dismissed), Toast.LENGTH_SHORT).show()
            jump()
        }
    }

    private val adDisplayListener: SplashAdDisplayListener = object : SplashAdDisplayListener() {
        override fun onAdShowed() {
        }

        override fun onAdClick() {
        }
    }

    override fun onStop() {
        timeoutHandler.removeMessages(MSG_AD_TIMEOUT)
        hasPaused = true
        super.onStop()
    }

    override fun onRestart() {
        super.onRestart()
        hasPaused = false
        jump()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (splashAdView != null) {
            splashAdView.destroyView()
        }
    }

    override fun onPause() {
        super.onPause()
        if (splashAdView != null) {
            splashAdView.pauseView()
        }
    }

    override fun onResume() {
        super.onResume()
        if (splashAdView != null) {
            splashAdView.resumeView()
        }
    }
}