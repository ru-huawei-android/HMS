/*
 * Copyright 2020. Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.huawei.hms.sample2

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.huawei.agconnect.auth.AGConnectAuth
import com.huawei.agconnect.auth.AGConnectAuthCredential
import com.huawei.agconnect.auth.TwitterAuthProvider
import com.twitter.sdk.android.core.*
import com.twitter.sdk.android.core.identity.TwitterAuthClient
import kotlinx.android.synthetic.main.bottom_info.*
import kotlinx.android.synthetic.main.buttons_lll.*

//author Ivantsov Alexey
class TwitterActivity : BaseActivity() {
    private var twitterAuthClient: TwitterAuthClient? = null
    private val TAG = TwitterActivity::class.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val authConfig = TwitterAuthConfig(
            getString(R.string.twitter_app_id),
            getString(R.string.twitter_app_secret)
        )
        val twitterConfig = TwitterConfig.Builder(this)
            .twitterAuthConfig(authConfig)
            .build()
        Twitter.initialize(twitterConfig)
        twitterAuthClient = TwitterAuthClient()

        setContentView(R.layout.activity_login)

        btnLinkUnlink.visibility=View.GONE

        btnLogin.setOnClickListener {
            login()
        }

        btnLinkUnlink.setOnClickListener {
            link()
        }

        btnLogout.setOnClickListener {
            logout()
        }
    }

    private fun login() {
        twitterAuthClient!!.authorize(
            this,
            object : Callback<TwitterSession>() {
                override fun success(result: Result<TwitterSession>) {
                    val token = result.data.authToken.token
                    val secret = result.data.authToken.secret
                    val credential =
                        TwitterAuthProvider.credentialWithToken(token, secret)
                    AGConnectAuth.getInstance().signIn(credential)
                        .addOnSuccessListener { signInResult ->
                            var user = signInResult.user
                            Toast.makeText(this@TwitterActivity, user.uid, Toast.LENGTH_LONG)
                                .show()
                            getUserInfoAndSwitchUI(AGConnectAuthCredential.Twitter_Provider)
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this@TwitterActivity, e.message, Toast.LENGTH_LONG)
                                .show()
                            tvResults.text = e.message
                        }
                }

                override fun failure(exception: TwitterException) {
                    Toast.makeText(this@TwitterActivity, exception.message, Toast.LENGTH_LONG)
                        .show()
                    tvResults.text = exception.message
                }
            })
    }

    private fun link() {
        if (!isProviderLinked(getAGConnectUser(), AGConnectAuthCredential.Facebook_Provider)) {
        twitterAuthClient!!.authorize(
            this,
            object : Callback<TwitterSession>() {
                override fun success(result: Result<TwitterSession>) {
                    val token = result.data.authToken.token
                    val secret = result.data.authToken.secret
                    val credential =
                        TwitterAuthProvider.credentialWithToken(token, secret)
                    getAGConnectUser()!!.link(credential)
                        .addOnSuccessListener { signInResult ->
                            var user = signInResult.user
                            Toast.makeText(this@TwitterActivity, user.uid, Toast.LENGTH_LONG)
                                .show()
                            getUserInfoAndSwitchUI(AGConnectAuthCredential.Twitter_Provider)
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this@TwitterActivity, e.message, Toast.LENGTH_LONG)
                                .show()
                            tvResults.text = e.message
                        }
                }

                override fun failure(exception: TwitterException) {
                    Toast.makeText(this@TwitterActivity, exception.message, Toast.LENGTH_LONG)
                        .show()
                    tvResults.text = exception.message
                }
            })
        } else {
            unlink()
        }
    }

    override fun logout() {
        super.logout()
        getUserInfoAndSwitchUI(AGConnectAuthCredential.Twitter_Provider)
    }

    private fun unlink() {
        if (AGConnectAuth.getInstance().currentUser != null) {
            AGConnectAuth.getInstance().currentUser
                .unlink(AGConnectAuthCredential.Facebook_Provider)
                .addOnSuccessListener { signInResult ->
                    var user = signInResult.user
                    Toast.makeText(this@TwitterActivity, user.uid, Toast.LENGTH_LONG)
                        .show()
                    getUserInfoAndSwitchUI(AGConnectAuthCredential.Twitter_Provider)
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, e.message.toString())
                    val message = checkError(e)
                    Toast.makeText(
                        this@TwitterActivity,
                        message,
                        Toast.LENGTH_LONG
                    ).show()
                    tvResults.text = message
                }
        }
    }

    override fun onResume() {
        super.onResume()
        getUserInfoAndSwitchUI(AGConnectAuthCredential.Twitter_Provider)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        twitterAuthClient!!.onActivityResult(requestCode, resultCode, data)
    }
}