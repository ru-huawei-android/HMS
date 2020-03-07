/*
 * Copyright 2020. Huawei Technologies Co., Ltd. All rights reserved.

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/

package com.huawei.hms.sample2

import android.annotation.TargetApi
import android.content.Intent
import android.os.Build
import android.text.TextUtils
import com.huawei.hms.push.HmsMessageService
import com.huawei.hms.push.RemoteMessage
import com.huawei.hms.push.SendException

//::created by c7j at 24.02.2020 19:38
class HuaweiPushService : HmsMessageService() {

    /**
     * When an app calls the getToken method to apply for a token from the server,
     * if the server does not return the token during current method calling,
     * the server can return the token through this method later.
     *
     * If the EMUI version is 10.0 or later on a Huawei device, a token is using through the getToken method.
     * If the getToken method fails to be called, HUAWEI Push Kit automatically caches the token request
     * and calls the method again. A token will then be returned through the onNewToken method.
     * If the EMUI version on a Huawei device is earlier than 10.0 and no token is returned
     * using the getToken method, a token will be returned using the onNewToken method.
     * (Из официальной документации пункта 2.1.2)
     * https://developer.huawei.com/consumer/en/doc/development/HMS-Guides/push-basic-capability#h2-1576218800370
     * Версию EMUI можно посмотреть в константе Build.DISPLAY
     */
    @TargetApi(3)
    override fun onNewToken(token: String) {
        log(Build.DISPLAY)
        log("received refresh token:$token")
        // send the token to your app server.
        if (!TextUtils.isEmpty(token)) {
            // This method callback must be completed in 10 seconds.
            // Otherwise, you need to start a new Job for callback processing.
            refreshedTokenToServer(token)
        }
        val intent = Intent()
        intent.action = HUAWEI_PUSH_ACTION
        intent.putExtra("method", "onNewToken")
        intent.putExtra("msg", "onNewToken called, token: $token")
        sendBroadcast(intent)
    }

    private fun refreshedTokenToServer(token: String) {
        log("token refreshed, send it to server. token: $token")
    }

    /**
     * This method is used to receive downstream data messages.
     * This method callback must be completed in 10 seconds.
     * Otherwise, you need to start a new Job for callback processing.
     *
     * @param message RemoteMessage
     */
    override fun onMessageReceived(message: RemoteMessage?) {
        log("onMessageReceived is called")
        if (message == null) {
            log("Received message entity is null!")
            return
        }

        // getCollapseKey() Obtains the classification identifier (collapse key) of a message.
        // getData() Obtains valid content data of a message.
        // getMessageId() Obtains the ID of a message.
        // getMessageType() Obtains the type of a message.
        // getNotification() Obtains the notification data instance from a message.
        // getOriginalUrgency() Obtains the original priority of a message.
        // getSentTime() Obtains the time when a message is sent from the server.
        // getTo() Obtains the recipient of a message.
        with(message) {
            log("""
                collapseKey: $collapseKey
                data: $data
                from: $from
                to: $message
                messageId: $messageId
                originalUrgency: $originalUrgency
                getUrgency: $urgency
                getSendTime: $sentTime
                getMessageType: $messageType
                getTtl: $ttl
            """)
        }

        // getBody() Obtains the displayed content of a message
        // getTitle() Obtains the title of a message
        // getTitleLocalizationKey() Obtains the key of the displayed title of a notification message
        // getTitleLocalizationArgs() Obtains variable parameters of the displayed title of a message
        // getBodyLocalizationKey() Obtains the key of the displayed content of a message
        // getBodyLocalizationArgs() Obtains variable parameters of the displayed content of a message
        // getIcon() Obtains icons from a message
        // getSound() Obtains the sound from a message
        // getTag() Obtains the tag from a message for message overwriting
        // getColor() Obtains the colors of icons in a message
        // getClickAction() Obtains actions triggered by message tapping
        // getChannelId() Obtains IDs of channels that support the display of messages
        // getImageUrl() Obtains the image URL from a message
        // getLink() Obtains the URL to be accessed from a message
        // getNotifyId() Obtains the unique ID of a message
        val notification: RemoteMessage.Notification = message.notification
        with (notification) {
            log("""
                    getImageUrl: $imageUrl
                    getTitle: $title
                    getTitleLocalizationKey: $titleLocalizationKey
                    getTitleLocalizationArgs: $titleLocalizationArgs
                    getBody: $body
                    getBodyLocalizationKey: $bodyLocalizationKey
                    getBodyLocalizationArgs: $bodyLocalizationArgs
                    getIcon: $icon
                    getSound: $sound
                    getTag: $tag
                    getColor: $color
                    getClickAction: $clickAction
                    getChannelId: $channelId
                    getLink: $link
                    getNotifyId: $notifyId
            """)
        }

        val intent = Intent()
        intent.action = HUAWEI_PUSH_ACTION
        intent.putExtra("method", "onMessageReceived")
        intent.putExtra("msg", "onMessageReceived called, message id:" +
                message.messageId + ", payload data:" + message.data)
        sendBroadcast(intent)

        // If the messages are not processed in 10 seconds, the app needs to use WorkManager
        val judgeWhetherIn10s = false
        if (judgeWhetherIn10s) {
            startWorkManagerJob(message)
        } else { // Process message within 10s
            processWithin10s(message)
        }
    }

    private fun startWorkManagerJob(message: RemoteMessage) {
        log("Start new Job processing.")
    }

    private fun processWithin10s(message: RemoteMessage) {
        log("Processing now.")
    }

    override fun onMessageSent(msgId: String) {
        log("onMessageSent called, Message id:$msgId")
        val intent = Intent()
        intent.action = HUAWEI_PUSH_ACTION
        intent.putExtra("method", "onMessageSent")
        intent.putExtra("msg", "onMessageSent called, Message id:$msgId")
        sendBroadcast(intent)
    }

    override fun onSendError(msgId: String, exception: Exception) {
        log("onSendError called, message id: $msgId, ErrCode:"
                    + (exception as SendException).errorCode + "description:" + exception.message)

        val intent = Intent()
        intent.action = HUAWEI_PUSH_ACTION
        intent.putExtra("method", "onSendError")
        intent.putExtra("msg", "onSendError called, message id:" + msgId + ", ErrCode:"
                    + exception.errorCode + ", description:" + exception.message)
        sendBroadcast(intent)
    }

    companion object {
        const val HUAWEI_PUSH_ACTION = "ru.huawei.push.action"
    }
}