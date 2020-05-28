package com.huawei.hms.ads

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.huawei.hms.ads.reward.Reward
import com.huawei.hms.ads.reward.RewardAd
import com.huawei.hms.ads.reward.RewardAdLoadListener
import com.huawei.hms.ads.reward.RewardAdStatusListener
import kotlinx.android.synthetic.main.activity_reward.*

class RewardActivity : AppCompatActivity(R.layout.activity_reward) {

    private lateinit var rewardedAd: RewardAd

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadRewardAd()
        getRewardBtn.setOnClickListener {
            rewardAdShow()
        }
    }

    private fun rewardAdShow() {
        if (rewardedAd.isLoaded) {
            rewardedAd.show(this, rewardAdStatusListener)
        }
    }

    private fun loadRewardAd() {
        rewardedAd = RewardAd(this, getString(R.string.ad_rewarded))
        rewardedAd.loadAd(AdParam.Builder().build(), rewardAdLoadListener)
    }

    private val rewardAdStatusListener = object : RewardAdStatusListener() {
        override fun onRewardAdClosed() {
            loadRewardAd()
        }

        override fun onRewardAdFailedToShow(errorCode: Int) {
            Toast.makeText(applicationContext, Utils.getErrorMessage(errorCode), Toast.LENGTH_SHORT).show()
        }

        override fun onRewardAdOpened() {
            Toast.makeText(applicationContext, "onRewardAdOpened", Toast.LENGTH_SHORT).show()
        }

        override fun onRewarded(reward: Reward) {
            Toast.makeText(applicationContext, "Ad finished, you get a reward", Toast.LENGTH_SHORT).show()
            loadRewardAd()
        }
    }

    private val rewardAdLoadListener = object : RewardAdLoadListener() {
        override fun onRewardAdFailedToLoad(errorCode: Int) {
            Toast.makeText(applicationContext, Utils.getErrorMessage(errorCode), Toast.LENGTH_SHORT).show()
        }

        override fun onRewardedLoaded() {
        }
    }
}