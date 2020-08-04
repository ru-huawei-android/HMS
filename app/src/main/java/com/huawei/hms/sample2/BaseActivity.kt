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

import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import coil.api.clear
import coil.api.load
import com.huawei.agconnect.auth.AGCAuthException
import com.huawei.agconnect.auth.AGConnectAuth
import com.huawei.agconnect.auth.AGConnectUser
import kotlinx.android.synthetic.main.bottom_info.*
import kotlinx.android.synthetic.main.buttons_lll.*

//author Ivantsov Alexey
open class BaseActivity : AppCompatActivity() {

    fun getUserInfo(user: AGConnectUser, iv_profile: ImageView? = null): String {
        var info = "displayName: " + user.displayName
        info += "\n"
        info += "UID: " + user.uid
        info += "\n"
        info += "email: " + user.email
        info += "\n"
        info += "emailVerified: " + user.emailVerified
        info += "\n"
        info += "isAnonymous: " + user.isAnonymous
        info += "\n"
        info += "passwordSetted: " + user.passwordSetted
        info += "\n"
        info += "phone: " + user.phone
        info += "\n"
        info += "providerId: " + providersMap[user.providerId?.toInt()] + " [id: " + user.providerId + "]"
        info += "\n"
        info += "providerInfo:\n" + user.providerInfo?.toString()

        iv_profile?.load(user.photoUrl) {
            crossfade(true)
        }
        return info
    }

    fun isProviderLinked(user: AGConnectUser?, providerId: Int): Boolean {
        for (provider in user?.providerInfo!!) {
            if (provider.containsKey("provider") && provider.getValue("provider")
                    .toInt() == providerId
            )
                return true
        }
        return false
    }

    fun getAGConnectUser(): AGConnectUser? {
        return AGConnectAuth.getInstance().currentUser
    }

    fun checkError(exception: Exception): String? {
        var message: String?
        if (exception is AGCAuthException) {
            message = exception.localizedMessage
            when (exception.code) {
                AGCAuthException.INVALID_PHONE -> message = "Invalid mobile number."
                AGCAuthException.PASSWORD_VERIFICATION_CODE_OVER_LIMIT -> message =
                    "The number of verification code inputs for password-based sign-in exceeds the upper limit."
                AGCAuthException.PASSWORD_VERIFY_CODE_ERROR -> message =
                    "Incorrect password or verification code."
                AGCAuthException.VERIFY_CODE_ERROR -> message = "Incorrect verification code."
                AGCAuthException.VERIFY_CODE_FORMAT_ERROR -> message =
                    "Incorrect verification code format."
                AGCAuthException.VERIFY_CODE_AND_PASSWORD_BOTH_NULL -> message =
                    "The verification code or password cannot be empty."
                AGCAuthException.VERIFY_CODE_EMPTY -> message = "The verification code is empty."
                AGCAuthException.VERIFY_CODE_LANGUAGE_EMPTY -> message =
                    "The language for sending a verification code is empty."
                AGCAuthException.VERIFY_CODE_RECEIVER_EMPTY -> message =
                    "The verification code receiver is empty."
                AGCAuthException.VERIFY_CODE_ACTION_ERROR -> message =
                    "The verification code type is empty."
                AGCAuthException.VERIFY_CODE_TIME_LIMIT -> message =
                    "The number of times for sending verification codes exceeds the upper limit."
                AGCAuthException.ACCOUNT_PASSWORD_SAME -> message =
                    "The password cannot be the same as the user name."
                AGCAuthException.USER_NOT_REGISTERED -> message =
                    "The user has not been registered."
                AGCAuthException.USER_HAVE_BEEN_REGISTERED -> message =
                    "The user already exists."
                AGCAuthException.PROVIDER_USER_HAVE_BEEN_LINKED -> message =
                    "The authentication mode has been associated with another user."
                AGCAuthException.PROVIDER_HAVE_LINKED_ONE_USER -> message =
                    "The authentication mode has already been associated with the user."
                AGCAuthException.CANNOT_UNLINK_ONE_PROVIDER_USER -> message =
                    "Cannot disassociate a single authentication mode."
                AGCAuthException.AUTH_METHOD_IS_DISABLED -> message =
                    "The authentication mode is not supported."
                AGCAuthException.FAIL_TO_GET_THIRD_USER_INFO -> message =
                    "Failed to obtain the third-party user information."
            }
        } else {
            message = exception.localizedMessage
        }
        return message
    }

    open fun logout() {
        if (AGConnectAuth.getInstance().currentUser != null) {
            AGConnectAuth.getInstance().signOut()
        }
        tvResults.text = ""
        ivProfile.clear()
    }

    fun getUserInfoAndSwitchUI(providerId: Int) {
        /** Проверяем наличие текущего уже авторизированного пользователя*/
        if (getAGConnectUser() != null) {
            /** Выводим инфу о пользователе*/
            tvResults.text = getUserInfo(AGConnectAuth.getInstance().currentUser, ivProfile)
            /** проверяем кол-во привязанных провайдеров*/
            if (getAGConnectUser()?.providerInfo != null && getAGConnectUser()!!.providerInfo!!.size > 1
                /** Если один из них = providerId*/
                && isProviderLinked(getAGConnectUser(), providerId)
            ) {
                /** то меняем текст кнопки*/
                btnLogin.visibility = View.GONE
                btnLogout.visibility = View.VISIBLE
                btnLinkUnlink.apply {
                    text = getString(R.string.unlink)
                    visibility = View.VISIBLE
                }
            }
            /** Если у нас всего один провайдер и он = providerId*/
            else if (getAGConnectUser()?.providerInfo != null && getAGConnectUser()!!.providerInfo!!.size == 1
                && isProviderLinked(getAGConnectUser(), providerId)
            ) {
                /** Скрываем кнопку Login & LinkUnlink*/
                btnLogin.visibility = View.GONE
                btnLogout.apply {
                    visibility = View.VISIBLE
                    text = getString(R.string.logout)
                }
                btnLinkUnlink.visibility = View.GONE
            } else {
                /** Стандартный режим для Link/Unlink*/
                btnLogin.visibility = View.GONE
                btnLogout.visibility = View.VISIBLE
                btnLinkUnlink.apply {
                    text = getString(R.string.link)
                    visibility = View.VISIBLE
                }
            }
        } else {
            /** Стандартный режим для Login*/
            btnLogin.visibility = View.VISIBLE
            btnLogout.visibility = View.GONE
            btnLinkUnlink.apply {
                text = getString(R.string.link)
                visibility = View.GONE
            }
        }
    }

    private val providersMap = mapOf(
        0 to "Anonymous_Provider",
        1 to "Huawei_Provider",
        2 to "Facebook_Provider",
        3 to "Twitter_Provider",
        4 to "WeiXin_Provider",
        5 to "Huawei_Game_Provider",
        6 to "QQ_Provider",
        7 to "WeiBo_Provider",
        8 to "Google_Provider",
        9 to "GoogleGame_Provider",
        10 to "SelfBuild_Provider",
        11 to "Phone_Provider",
        12 to "Email_Provider"
    )
}