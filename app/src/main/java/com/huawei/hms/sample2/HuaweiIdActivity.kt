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
import android.widget.Toast
import com.huawei.agconnect.auth.AGConnectAuth
import com.huawei.agconnect.auth.AGConnectAuthCredential
import com.huawei.agconnect.auth.HwIdAuthProvider
import com.huawei.agconnect.auth.SignInResult
import com.huawei.hms.common.ApiException
import com.huawei.hms.support.api.entity.auth.Scope
import com.huawei.hms.support.api.entity.hwid.HwIDConstant
import com.huawei.hms.support.hwid.HuaweiIdAuthManager
import com.huawei.hms.support.hwid.request.HuaweiIdAuthParams
import com.huawei.hms.support.hwid.request.HuaweiIdAuthParamsHelper
import com.huawei.hms.support.hwid.service.HuaweiIdAuthService
import kotlinx.android.synthetic.main.bottom_info.*
import kotlinx.android.synthetic.main.buttons_lll.*
import java.util.*

//author Ivantsov Alexey
class HuaweiIdActivity : BaseActivity() {
    private val TAG = HuaweiIdActivity::class.simpleName

    private val HUAWEIID_SIGNIN = 8000
    private val LINK_CODE = 8002

    private var mHuaweiIdAuthService: HuaweiIdAuthService? = null
    private lateinit var mHuaweiIdAuthParams: HuaweiIdAuthParams

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        
        var authParams = HuaweiIdAuthParamsHelper(HuaweiIdAuthParams.DEFAULT_AUTH_REQUEST_PARAM)
        val scopeList: MutableList<Scope> = ArrayList()
        scopeList.add(Scope(HwIDConstant.SCOPE.ACCOUNT_BASEPROFILE))
        authParams.setScopeList(scopeList)
        mHuaweiIdAuthParams = authParams.setAccessToken().createParams()
        mHuaweiIdAuthService =
            HuaweiIdAuthManager.getService(this@HuaweiIdActivity, mHuaweiIdAuthParams)

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
        startActivityForResult(mHuaweiIdAuthService?.signInIntent, HUAWEIID_SIGNIN)
    }

    private fun link() {
        if (!isProviderLinked(getAGConnectUser(), AGConnectAuthCredential.HMS_Provider)) {
            startActivityForResult(mHuaweiIdAuthService?.signInIntent, LINK_CODE)
        } else {
            unlink()
        }
    }

    override fun onResume() {
        super.onResume()
        getUserInfoAndSwitchUI(AGConnectAuthCredential.HMS_Provider)
    }

    override fun logout() {
        super.logout()
        getUserInfoAndSwitchUI(AGConnectAuthCredential.HMS_Provider)
    }

    private fun unlink() {
        if (AGConnectAuth.getInstance().currentUser != null) {
            AGConnectAuth.getInstance().currentUser
                .unlink(AGConnectAuthCredential.HMS_Provider)
                .addOnSuccessListener { signInResult ->
                    var user = signInResult.user
                    Toast.makeText(this@HuaweiIdActivity, user.uid, Toast.LENGTH_LONG)
                        .show()
                    getUserInfoAndSwitchUI(AGConnectAuthCredential.HMS_Provider)
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, e.message.toString())
                    val message = checkError(e)
                    Toast.makeText(
                        this@HuaweiIdActivity,
                        message,
                        Toast.LENGTH_LONG
                    ).show()
                    tvResults.text = message
                }
        }
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == HUAWEIID_SIGNIN) {
            val authHuaweiIdTask = HuaweiIdAuthManager.parseAuthResultFromIntent(data)
            if (authHuaweiIdTask.isSuccessful) {
                val huaweiAccount = authHuaweiIdTask.result
                val accessToken: String? = huaweiAccount.accessToken
                val credential = HwIdAuthProvider.credentialWithToken(accessToken)
                if (getAGConnectUser() == null) {
                    AGConnectAuth.getInstance().signIn(credential)
                        .addOnSuccessListener { signInResult: SignInResult ->
                            var user = signInResult.user
                            Toast.makeText(this@HuaweiIdActivity, user.uid, Toast.LENGTH_LONG)
                                .show()
                            getUserInfoAndSwitchUI(AGConnectAuthCredential.HMS_Provider)
                        }
                        .addOnFailureListener { e: java.lang.Exception ->
                            e.printStackTrace()
                            val message = checkError(e)
                            Toast.makeText(
                                this@HuaweiIdActivity,
                                message,
                                Toast.LENGTH_LONG
                            ).show()
                            tvResults.text = message
                        }
                } else {
                    var user = getAGConnectUser()
                    Toast.makeText(this@HuaweiIdActivity, user?.uid, Toast.LENGTH_LONG).show()
                    getUserInfoAndSwitchUI(AGConnectAuthCredential.HMS_Provider)
                }
            } else {
                Toast.makeText(
                    this@HuaweiIdActivity,
                    "HwID signIn failed" + authHuaweiIdTask.exception.message,
                    Toast.LENGTH_LONG
                ).show()
            }
        } else if (requestCode == LINK_CODE) {
            val authHuaweiIdTask = HuaweiIdAuthManager.parseAuthResultFromIntent(data)
            if (authHuaweiIdTask.isSuccessful) {
                val huaweiAccount = authHuaweiIdTask.result
                val credential = HwIdAuthProvider.credentialWithToken(huaweiAccount.accessToken)
                getAGConnectUser()!!.link(credential)
                    .addOnSuccessListener { signInResult ->
                        var user = signInResult.user
                        Toast.makeText(this@HuaweiIdActivity, user.uid, Toast.LENGTH_LONG)
                            .show()
                        getUserInfoAndSwitchUI(AGConnectAuthCredential.HMS_Provider)
                    }
                    .addOnFailureListener { e ->
                        e.printStackTrace()
                        val message = checkError(e)
                        Toast.makeText(
                            this@HuaweiIdActivity,
                            message,
                            Toast.LENGTH_LONG
                        ).show()
                        tvResults.text = message
                    }
            } else {
                Log.e(
                    TAG,
                    "Link is failed : " + (authHuaweiIdTask.exception as ApiException).statusCode
                )
            }
        }
    }
}
