package com.huawei.hms.ads6

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.huawei.hms.ads.R
import com.huawei.hms.ads.consent.bean.AdProvider
import com.huawei.hms.ads.consent.constant.ConsentStatus
import com.huawei.hms.ads.consent.constant.DebugNeedConsent
import com.huawei.hms.ads.consent.inter.Consent
import com.huawei.hms.ads.consent.inter.ConsentUpdateListener
import kotlinx.android.synthetic.main.activity_main.*
import timber.log.Timber
import java.util.*

class MainActivity : AppCompatActivity(R.layout.activity_main) {


    var dataset: List<AdFormat> = listOf(
        AdFormat(
            "Banner Ad",
            BannerActivity::class.java
        ),
        AdFormat(
            "Interstitial Ad",
            InterstitialActivity::class.java
        ),
        AdFormat(
            "Native Ad",
            NativeActivity::class.java
        ),
        AdFormat(
            "Reward Ad",
            RewardActivity::class.java
        )
//            AdFormat("Consent", ConsentActivity::class.java)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        rv.layoutManager = LinearLayoutManager(applicationContext)
        rv.adapter = RvAdapter(dataset, this)
        rv.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))

        checkConsentStatus()
    }

    private fun checkConsentStatus() {
        val adProviderList: MutableList<AdProvider> = ArrayList()
        val consentInfo = Consent.getInstance(this)
        consentInfo.addTestDeviceId("********")
        consentInfo.setDebugNeedConsent(DebugNeedConsent.DEBUG_NEED_CONSENT)
        consentInfo.requestConsentUpdate(object : ConsentUpdateListener {
            override fun onSuccess(consentStatus: ConsentStatus, isNeedConsent: Boolean, adProviders: List<AdProvider>) {
                Timber.d("ConsentStatus: $consentStatus, isNeedConsent: $isNeedConsent")
                if (isNeedConsent) {
                    if (adProviders.isNotEmpty()) {
                        adProviderList.addAll(adProviders)
                    }
                    showConsentDialog(adProviderList)
                }
            }

            override fun onFail(errorDescription: String) {
                Timber.d("User's consent status failed to update: $errorDescription")
                if (getPreferences(
                        SP_CONSENT_KEY,
                        DEFAULT_SP_CONSENT_VALUE
                    ) < 0) {
                    // In this example, if the request fails, the consent dialog box is still displayed. In this case, the ad publisher list is empty.
                    showConsentDialog(adProviderList)
                }
            }
        })
    }

    private fun getPreferences(key: String, defValue: Int): Int {
        val preferences = getSharedPreferences(SP_NAME, Context.MODE_PRIVATE)
        val value = preferences.getInt(key, defValue)
        Timber.d("Key:$key, Preference value is: $value")
        return value
    }

    private fun showConsentDialog(providers: MutableList<AdProvider>) {
        val dialog = ConsentDialog(this, providers)
        dialog.setCanceledOnTouchOutside(false)
        dialog.show()
    }


}

class AdFormat(val title: String, val targetClass: Class<*>)