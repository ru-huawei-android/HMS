package com.huawei.hms.ads

import android.os.Bundle
import android.os.PersistableBundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_interstitial.*
import timber.log.Timber

class InterstitialActivity : AppCompatActivity(R.layout.activity_interstitial) {

    private val imageAdId: String = "teste9ih9j0rc3"
    private val videoAdId: String = "testb4znbuh3n2"

    private lateinit var interstitialAd: InterstitialAd

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadInterstitialAdBtn.setOnClickListener(loadAd)
    }

    private val loadAd = View.OnClickListener {
        interstitialAd = InterstitialAd(this)
        interstitialAd.adId = getAdId(interstitialRadioGroup.checkedRadioButtonId)
        interstitialAd.adListener = adListener
        interstitialAd.loadAd(AdParam.Builder().build())
    }

    private fun getAdId(id: Int): String? {
        return when (id) {
            R.id.display_image -> imageAdId
            else -> videoAdId
        }
    }

    private fun showInterstitial() {
        when {
            // показываем рекламу только если она загрузилась
            interstitialAd.isLoaded -> interstitialAd.show()
            else -> Toast.makeText(this, "Ad did not load", Toast.LENGTH_SHORT).show()
        }
    }


    private val adListener: AdListener = object : AdListener() {
        override fun onAdLoaded() {
            Timber.d("onAdLoaded")
            showInterstitial()
        }

        override fun onAdFailed(errorCode: Int) {
            Timber.d("onAdFailed %s", errorCode)
            Toast.makeText(applicationContext, Utils.getErrorMessage(errorCode), Toast.LENGTH_LONG).show()
        }

        override fun onAdClosed() {
            Timber.d("onAdClosed")
        }
    }

}