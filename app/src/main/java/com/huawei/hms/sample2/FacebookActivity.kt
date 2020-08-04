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
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.huawei.agconnect.auth.AGConnectAuth
import com.huawei.agconnect.auth.AGConnectAuthCredential
import com.huawei.agconnect.auth.FacebookAuthProvider
import kotlinx.android.synthetic.main.bottom_info.*
import kotlinx.android.synthetic.main.buttons_lll.*

//author Ivantsov Alexey
class FacebookActivity : BaseActivity() {
    private val callbackManager = CallbackManager.Factory.create()
    private val TAG = FacebookActivity::class.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        btnLinkUnlink.visibility = View.GONE

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
        LoginManager.getInstance()
            .logInWithReadPermissions(this, listOf("public_profile", "email"))
        LoginManager.getInstance()
            .registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
                override fun onSuccess(loginResult: LoginResult) {
                    val token = loginResult.accessToken.token
                    val credential = FacebookAuthProvider.credentialWithToken(token)
                    AGConnectAuth.getInstance().signIn(credential)
                        .addOnSuccessListener { signInResult ->
                            var user = signInResult.user
                            Toast.makeText(this@FacebookActivity, user.uid, Toast.LENGTH_LONG)
                                .show()
                            getUserInfoAndSwitchUI(AGConnectAuthCredential.Facebook_Provider)
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this@FacebookActivity, e.message, Toast.LENGTH_LONG)
                                .show()
                            tvResults.text = e.message
                        }
                }

                override fun onCancel() {
                    Toast.makeText(this@FacebookActivity, "Cancel", Toast.LENGTH_LONG)
                        .show()
                }

                override fun onError(error: FacebookException) {
                    Toast.makeText(this@FacebookActivity, error.message, Toast.LENGTH_LONG)
                        .show()
                    tvResults.text = error.message
                }
            })
    }

    private fun link() {
        if (!isProviderLinked(getAGConnectUser(), AGConnectAuthCredential.Facebook_Provider)) {
            LoginManager.getInstance()
                .logInWithReadPermissions(this, listOf("public_profile", "email"))
            LoginManager.getInstance()
                .registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
                    override fun onSuccess(loginResult: LoginResult) {
                        val token = loginResult.accessToken.token
                        val credential = FacebookAuthProvider.credentialWithToken(token)
                        getAGConnectUser()!!.link(credential)
                            .addOnSuccessListener { signInResult ->
                                var user = signInResult.user
                                Toast.makeText(this@FacebookActivity, user.uid, Toast.LENGTH_LONG)
                                    .show()
                                getUserInfoAndSwitchUI(AGConnectAuthCredential.Facebook_Provider)
                            }
                            .addOnFailureListener { e ->
                                Log.e(TAG, e.message.toString())
                                val message = checkError(e)
                                Toast.makeText(
                                    this@FacebookActivity,
                                    message,
                                    Toast.LENGTH_LONG
                                ).show()
                                tvResults.text = message
                            }
                    }

                    override fun onCancel() {
                        Toast.makeText(this@FacebookActivity, "Cancel", Toast.LENGTH_LONG)
                            .show()
                    }

                    override fun onError(error: FacebookException) {
                        Toast.makeText(this@FacebookActivity, error.message, Toast.LENGTH_LONG)
                            .show()
                        tvResults.text = error.message
                    }
                })
        } else {
            unlink()
        }
    }

    override fun logout() {
        super.logout()
        getUserInfoAndSwitchUI(AGConnectAuthCredential.Facebook_Provider)
    }

    private fun unlink() {
        if (AGConnectAuth.getInstance().currentUser != null) {
            AGConnectAuth.getInstance().currentUser
                .unlink(AGConnectAuthCredential.Facebook_Provider)
                .addOnSuccessListener { signInResult ->
                    var user = signInResult.user
                    Toast.makeText(this@FacebookActivity, user.uid, Toast.LENGTH_LONG)
                        .show()
                    getUserInfoAndSwitchUI(AGConnectAuthCredential.Facebook_Provider)
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, e.message.toString())
                    val message = checkError(e)
                    Toast.makeText(
                        this@FacebookActivity,
                        message,
                        Toast.LENGTH_LONG
                    ).show()
                    tvResults.text = message
                }
        }
    }

    override fun onResume() {
        super.onResume()
        getUserInfoAndSwitchUI(AGConnectAuthCredential.Facebook_Provider)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        callbackManager.onActivityResult(requestCode, resultCode, data)
    }
}