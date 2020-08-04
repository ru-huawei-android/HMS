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

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.huawei.agconnect.auth.AGConnectAuth
import com.huawei.agconnect.auth.AGConnectAuthCredential
import com.huawei.agconnect.auth.GoogleAuthProvider
import kotlinx.android.synthetic.main.bottom_info.*
import kotlinx.android.synthetic.main.buttons_lll.*
import net.openid.appauth.*

//author Ivantsov Alexey
class GoogleActivity : BaseActivity() {
    private val TAG = GoogleActivity::class.java.simpleName

    private val GOOGLE_SIGN_CODE = 9901
    private val LINK_CODE = 9902
    private var client: GoogleSignInClient? = null

    private val USED_INTENT = "USED_INTENT"

    private val action = "com.example.authservice.HANDLE_AUTHORIZATION_RESPONSE"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_login)
        val options =
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.server_client_id))
                .requestEmail()
                .build()
        client = GoogleSignIn.getClient(this, options)

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

    private fun isGmsAvailable(context: Context): Boolean {
        return ConnectionResult.SUCCESS == GoogleApiAvailability.getInstance()
            .isGooglePlayServicesAvailable(context)
    }

    private fun login() {
        if (isGmsAvailable(this))
        /** тут мы пробуем авторизироваться стандартными средствами GMS*/
            startActivityForResult(client?.signInIntent, GOOGLE_SIGN_CODE)
        else {
            /** этот метод применяем на телефонах HMS-only*/
            loginLinkWithOpenid()
        }
    }

    private fun link() {
        if (!isProviderLinked(getAGConnectUser(), AGConnectAuthCredential.Google_Provider)) {
            if (isGmsAvailable(this))
            /** тут мы пробуем авторизироваться стандартными средствами GMS*/
                startActivityForResult(client?.signInIntent, LINK_CODE)
            else {
                loginLinkWithOpenid()
            }
        } else {
            unlink()
        }
    }

    //этот метод применяем на телефонах HMS-only
    private fun loginLinkWithOpenid() {
        val serviceConfiguration =
            AuthorizationServiceConfiguration(
                Uri.parse("https://accounts.google.com/o/oauth2/v2/auth") /* auth endpoint */,
                Uri.parse("https://www.googleapis.com/oauth2/v4/token") /* token endpoint */
            )
        val redirectUri = Uri.parse("com.example.authservice:/oauth2callback")
        val clientId = getString(R.string.google_app_id)
        val builder = AuthorizationRequest.Builder(
            serviceConfiguration,
            clientId,
            AuthorizationRequest.RESPONSE_TYPE_CODE,
            redirectUri
        )
        builder.setScopes(AuthorizationService.SCOPE_PROFILE)
        val request = builder.build()

        val authorizationService = AuthorizationService(this)

        val postAuthorizationIntent = Intent(action)
        val pendingIntent = PendingIntent.getActivity(
            this,
            request.hashCode(),
            postAuthorizationIntent,
            0
        )
        authorizationService.performAuthorizationRequest(request, pendingIntent)
        finish()
    }

    override fun onResume() {
        super.onResume()
        getUserInfoAndSwitchUI(AGConnectAuthCredential.Google_Provider)
    }

    override fun logout() {
        super.logout()
        getUserInfoAndSwitchUI(AGConnectAuthCredential.Google_Provider)
    }

    private fun unlink() {
        if (AGConnectAuth.getInstance().currentUser != null) {
            AGConnectAuth.getInstance().currentUser
                .unlink(AGConnectAuthCredential.Google_Provider)
                .addOnSuccessListener { signInResult ->
                    val user = signInResult.user
                    Toast.makeText(this@GoogleActivity, user.uid, Toast.LENGTH_LONG)
                        .show()
                    getUserInfoAndSwitchUI(AGConnectAuthCredential.Google_Provider)
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, e.message.toString())
                    val message = checkError(e)
                    Toast.makeText(
                        this@GoogleActivity,
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
        //если мы пробуем авторизироваться стандартными средствами GMS, то ответ будет тут
        if (requestCode == GOOGLE_SIGN_CODE) {
            GoogleSignIn.getSignedInAccountFromIntent(data)
                .addOnSuccessListener { googleSignInAccount: GoogleSignInAccount ->
                    val credential =
                        GoogleAuthProvider.credentialWithToken(googleSignInAccount.idToken)
                    //Ок, мы авторизированы, делаем работу дальше...
                    AGConnectAuthLogin(credential)
                }
                .addOnFailureListener { e: java.lang.Exception ->
                    val error_message = "Google login failed: " + e.message
                    Toast.makeText(
                        this@GoogleActivity,
                        error_message,
                        Toast.LENGTH_LONG
                    ).show()
                    tvResults.text = error_message
                }
        }
        //если мы пробуем авторизироваться стандартными средствами GMS, то ответ будет тут
        else if (requestCode == LINK_CODE) {
            GoogleSignIn.getSignedInAccountFromIntent(data)
                .addOnSuccessListener { googleSignInAccount: GoogleSignInAccount ->
                    val credential =
                        GoogleAuthProvider.credentialWithToken(googleSignInAccount.idToken)
                    //Ок, мы авторизированы, делаем работу дальше...
                    AGConnectAuthLink(credential)
                }
                .addOnFailureListener { e: java.lang.Exception ->
                    val error_message = "Google login failed: " + e.message
                    Toast.makeText(
                        this@GoogleActivity,
                        error_message,
                        Toast.LENGTH_LONG
                    ).show()
                    tvResults.text = error_message
                }
        }
    }

    //перехватываем событие создания нашей Activity
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        checkIntent(intent)
    }

    //и проверяем не вернулась ли нам AuthorizationResponse
    private fun checkIntent(intent: Intent?) {
        when (intent?.action) {
            action ->
                if (!intent.hasExtra(USED_INTENT)) {
                    handleAuthorizationResponse(intent)
                    intent.putExtra(USED_INTENT, true)
                }
        }
    }

    //работаем с полученными данными AuthorizationResponse
    private fun handleAuthorizationResponse(intent: Intent) {
        val response: AuthorizationResponse? = AuthorizationResponse.fromIntent(intent)
        val error: AuthorizationException? = AuthorizationException.fromIntent(intent)
        val authState = AuthState(response, error)
        if (response != null) {
            Log.i(
                TAG, String.format("Handled Authorization Response %s ", authState.toJsonString())
            )
            val service = AuthorizationService(this)
            service.performTokenRequest(response.createTokenExchangeRequest()) { tokenResponse, exception ->
                if (exception != null) {
                    Log.w(TAG, "Token Exchange failed $exception")
                } else {
                    Log.i(
                        TAG,
                        String.format(
                            "Token Response [ Access Token: %s, ID Token: %s ]",
                            tokenResponse?.accessToken,
                            tokenResponse?.idToken
                        )
                    )
                    //AGC will come in screen after getting token
                    //Obtain the **idToken** after login authorization.
                    if (tokenResponse != null) {
                        val credential =
                            GoogleAuthProvider.credentialWithToken(tokenResponse.idToken)
                        if (getAGConnectUser() == null)
                            AGConnectAuthLogin(credential)
                        else {
                            AGConnectAuthLink(credential)
                        }
                    }
                }
            }
        }
    }

    //Авторизируемся через AGConnectAuth с учетными данными из Google
    private fun AGConnectAuthLogin(credential: AGConnectAuthCredential) {
        AGConnectAuth.getInstance().signIn(credential)
            .addOnSuccessListener { signInResult ->
                val user = signInResult.user
                Toast.makeText(
                    this@GoogleActivity,
                    user.uid,
                    Toast.LENGTH_LONG
                ).show()
                getUserInfoAndSwitchUI(AGConnectAuthCredential.Google_Provider)
            }
            .addOnFailureListener { e ->
                e.printStackTrace()
                val message = checkError(e)
                Toast.makeText(
                    this@GoogleActivity,
                    message,
                    Toast.LENGTH_LONG
                ).show()
                tvResults.text = message
            }
    }

    //Линкуемся через AGConnectAuth с учетными данными из Google
    private fun AGConnectAuthLink(credential: AGConnectAuthCredential) {
        getAGConnectUser()!!.link(credential)
            .addOnSuccessListener { signInResult ->
                val user = signInResult.user
                Toast.makeText(
                    this@GoogleActivity,
                    user.uid,
                    Toast.LENGTH_LONG
                ).show()
                getUserInfoAndSwitchUI(AGConnectAuthCredential.Google_Provider)
            }
            .addOnFailureListener { e ->
                e.printStackTrace()
                val message = checkError(e)
                Toast.makeText(
                    this@GoogleActivity,
                    message,
                    Toast.LENGTH_LONG
                ).show()
                tvResults.text = message
            }
    }

    override fun onStart() {
        super.onStart()
        checkIntent(intent)
    }
}
