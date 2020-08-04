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
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Base64
import android.util.Log
import com.huawei.agconnect.auth.AGConnectAuth
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.bottom_info.*
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

//author Ivantsov Alexey
class MainActivity : BaseActivity() {
    /**
     * Срок действия access token составляет два дня, а refresh token валиден два месяца.
     * Если пользователь не входит в приложение в течение двух месяцев подряд,
     * срок действия refresh token истекает, и при пользователь получит код ошибки 203817986.
     * */

    /**
     *  Если при попытке сделать Link() используя аккаунт YYY в ответ получаем ошибку
     *  PROVIDER_USER_HAVE_BEEN_LINKED = 203818038
     *  Это значит что данный аккаунт YYY уже слинкован с другим аккунтом (с другим UID)
     *  Для того что бы сделать UnLink этого аккаунта YYY нужно (два способа):
     *  1. Найти в списке providerInfo аккоунт который идет самым первым (мастер)
     *
     *      а. Осуществить SignIn (логин) с помощью этого мастер аккаунта
     *      б. Осуществить UnLink() для аккаунта YYY
     *
     *  2. Осуществить SignIn (логин) с помощью аккаунта YYY
     *      а. Удалить данного юзера серез метод deleteUser()
     * */

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnPhoneLogin.setOnClickListener {
            startActivity(Intent(this@MainActivity, PhoneActivity::class.java))
            overridePendingTransition(0, 0)
        }

        btnEmailLogin.setOnClickListener {
            startActivity(Intent(this@MainActivity, EmailActivity::class.java))
            overridePendingTransition(0, 0)
        }

        btnAnonymousLogin.setOnClickListener {
            startActivity(Intent(this@MainActivity, AnonimousLogin::class.java))
            overridePendingTransition(0, 0)
        }

        btnHwidLogin.setOnClickListener {
            startActivity(Intent(this@MainActivity, HuaweiIdActivity::class.java))
            overridePendingTransition(0, 0)
        }

        btnHwGameLogin.setOnClickListener {
            startActivity(Intent(this@MainActivity, HuaweiGameIdActivity::class.java))
            overridePendingTransition(0, 0)
        }

        btnGoogleLogin.setOnClickListener {
            startActivity(Intent(this@MainActivity, GoogleActivity::class.java))
            overridePendingTransition(0, 0)
        }

        btnFbLogin.setOnClickListener {
            startActivity(Intent(this@MainActivity, FacebookActivity::class.java))
            overridePendingTransition(0, 0)
        }

        btnTwLogin.setOnClickListener {
            startActivity(Intent(this@MainActivity, TwitterActivity::class.java))
            overridePendingTransition(0, 0)
        }

        btnLogout.setOnClickListener {
            logout()
        }

        btnDeleteUser.setOnClickListener {
            AGConnectAuth.getInstance().deleteUser()
            logout()
        }
        //printKeyHash()
    }

    /** Это нужно что бы узнать хэш-адрес для FB*/
    private fun printKeyHash() {
        try {
            val info = packageManager
                .getPackageInfo(
                    "com.example.authservice",
                    PackageManager.GET_SIGNATURES
                )
            for (signature in info.signatures) {
                val md: MessageDigest = MessageDigest.getInstance("SHA")
                md.update(signature.toByteArray())
                Log.d(
                    MainActivity::class.java.simpleName,
                    "KeyHash: " + Base64.encodeToString(md.digest(), Base64.DEFAULT)
                )
            }
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        }
    }

    override fun onResume() {
        super.onResume()
        //проверяем наличие текущего уже авторизированного пользователя
        if (AGConnectAuth.getInstance().currentUser != null)
            tvResults.text = getUserInfo(AGConnectAuth.getInstance().currentUser)
        else {
            tvResults.text = ""
        }
    }

}
