package com.huawei.hms.ads

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.huawei.hms.ads.VideoOperator.VideoLifecycleListener
import com.huawei.hms.ads.nativead.*
import kotlinx.android.synthetic.main.activity_native.*

class NativeActivity : AppCompatActivity(R.layout.activity_native) {

    private val largeAdId = "testu7m3hc4gvm"
    private val smallAdId = "testb65czjivt9"
    private val videoAdId = "testy63txaom86"

    private var nativeAd: NativeAd? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadAdBtn.setOnClickListener(loadAd)
        loadAdBtn.performClick()
    }

    private val loadAd = View.OnClickListener {
        // NativeAdConfiguration.Builder используется для настроек рекламы
        // подробности: https://developer.huawei.com/consumer/en/doc/development/HMS-References/ads-api-nativeadconfiguration-builder
        val adConfiguration = NativeAdConfiguration.Builder().build()

        // Создаем Builder в конструктуре которого передаем контекст и id рекламы
        // Далеее устанавливаем слушателей и настройки созданыне ранее -> загружаем рекламу
        NativeAdLoader.Builder(this, getAdId())
                .setNativeAdLoadedListener(nativeAdLoadedListener)
                .setAdListener(adListener)
                .setNativeAdOptions(adConfiguration)
                .build()
                .loadAd(AdParam.Builder().build())
    }

    private val adListener = object : AdListener() {
        override fun onAdFailed(errorCode: Int) {
            Toast.makeText(applicationContext, errorCode, Toast.LENGTH_LONG).show()
        }
    }

    private val nativeAdLoadedListener = NativeAd.NativeAdLoadedListener {
        showNativeAd(it)
    }

    private fun showNativeAd(nativeAd: NativeAd) {

        if (this.nativeAd != null) {
            this.nativeAd?.destroy()
        }
        this.nativeAd = nativeAd

        val nativeView = layoutInflater.inflate(getLayoutType(), null) as NativeView

        // инициализируем данные загруженной рекламой
        initNativeAdView(this.nativeAd, nativeView)

        // добавляем рекламу на UI
        nativeAdScrollView.removeAllViews()
        nativeAdScrollView.addView(nativeView)
    }

    private fun initNativeAdView(nativeAd: NativeAd?, nativeView: NativeView) {
        // инициализируем view в классе NativeView
        nativeView.titleView = nativeView.findViewById(R.id.ad_title)
        nativeView.mediaView = nativeView.findViewById<View>(R.id.ad_media) as MediaView
        nativeView.adSourceView = nativeView.findViewById(R.id.ad_source)
        nativeView.callToActionView = nativeView.findViewById(R.id.ad_call_to_action)

        // заполняем данными
        (nativeView.titleView as TextView).text = nativeAd?.title
        nativeView.mediaView.setMediaContent(nativeAd?.mediaContent)

        when {
            nativeAd?.adSource != null -> {
                (nativeView.adSourceView as TextView).text = nativeAd.adSource
                nativeView.adSourceView.visibility = View.VISIBLE
            }
            else -> nativeView.adSourceView.visibility = View.INVISIBLE

        }

        when {
            nativeAd?.callToAction != null -> {
                (nativeView.callToActionView as Button).text = nativeAd.callToAction
                nativeView.callToActionView.visibility = View.VISIBLE
            }
            else -> nativeView.callToActionView.visibility = View.INVISIBLE
        }

        // если реклама содержит видео добавляем слушатель VideoLifecycleListener
        val videoOperator = nativeAd?.videoOperator
        if (videoOperator!!.hasVideo()) {
            videoOperator.videoLifecycleListener = videoLifecycleListener
        }

        nativeView.setNativeAd(nativeAd)
    }

    private val videoLifecycleListener: VideoLifecycleListener = object : VideoLifecycleListener() {
        override fun onVideoStart() {
        }

        override fun onVideoPlay() {
        }

        override fun onVideoEnd() {
        }
    }

    // получаем layout для отображения рекламы (для большого размера и видео использовается один тип, для маленькой рекламы другой)
    private fun getLayoutType(): Int {
        return when (typeRadioGroup.checkedRadioButtonId) {
            R.id.radio_button_small -> R.layout.native_small_template
            else -> R.layout.native_video_template
        }
    }

    // получаем id рекламы в зависимости от выбранного типа
    private fun getAdId(): String {
        return when (typeRadioGroup.checkedRadioButtonId) {
            R.id.radio_button_small -> smallAdId
            R.id.radio_button_large -> largeAdId
            R.id.radio_button_video -> videoAdId
            else -> ""
        }
    }
}