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
package com.huawei.hms.sample2.examples;

import com.alibaba.fastjson.JSONObject;
import com.huawei.hms.sample2.android.AndroidNotification;
import com.huawei.hms.sample2.android.BadgeNotification;
import com.huawei.hms.sample2.android.ClickAction;
import com.huawei.hms.sample2.android.Color;
import com.huawei.hms.sample2.android.LigthSettings;
import com.huawei.hms.sample2.exception.HuaweiMesssagingException;
import com.huawei.hms.sample2.message.AndroidConfig;
import com.huawei.hms.sample2.message.Message;
import com.huawei.hms.sample2.message.Notification;
import com.huawei.hms.sample2.messaging.HuaweiApp;
import com.huawei.hms.sample2.messaging.HuaweiMessaging;
import com.huawei.hms.sample2.model.Importance;
import com.huawei.hms.sample2.model.Urgency;
import com.huawei.hms.sample2.model.Visibility;
import com.huawei.hms.sample2.reponse.SendResponse;
import com.huawei.hms.sample2.util.InitAppUtils;

import java.util.ResourceBundle;

import static com.huawei.hms.sample2.util.Constants.SENT_OK_CODE;

public class SendNotifyMessage {

    private String[] tokenList;

    private String getDefaultToken() {
        // TODO: test token is taken from test_token in /resources/url.properties file
        return ResourceBundle.getBundle("url").getString("test_token");
    }

    public SendNotifyMessage() {
        tokenList = new String[] {
                getDefaultToken()
        };
    }

    public SendNotifyMessage(String token) {
        if (token == null || token.isEmpty()) {
            token = getDefaultToken();
        }
        tokenList = new String[] { token };
    }

    public SendNotifyMessage(String[] tokenList) {
        if (tokenList == null || tokenList.length <= 0) {
            tokenList = new String[] { getDefaultToken() };
        }
        this.tokenList = tokenList;
    }

    /**
     * send notification message
     *
     * @throws HuaweiMesssagingException
     */
    public boolean sendNotification() throws HuaweiMesssagingException {
        HuaweiApp app = InitAppUtils.initializeApp();
        HuaweiMessaging huaweiMessaging = HuaweiMessaging.getInstance(app);

        Notification notification = Notification.builder().setTitle("sample title")
                .setBody("sample message body wft")
                .build();

        JSONObject multiLangKey = new JSONObject();
        JSONObject titleKey = new JSONObject();
        titleKey.put("en","好友请求");
        JSONObject bodyKey = new JSONObject();
        titleKey.put("en","My name is %s, I am from %s.");
        multiLangKey.put("key1", titleKey);
        multiLangKey.put("key2", bodyKey);

        LigthSettings lightSettings = LigthSettings.builder().setColor(Color.builder().setAlpha(0f).setRed(0f).setBlue(1f).setGreen(1f).build())
                .setLightOnDuration("3.5")
                .setLightOffDuration("5S")
                .build();

        AndroidNotification androidNotification = AndroidNotification.builder()
                .setIcon("/raw/ic_notification")
                .setColor("#AACCDD")
                .setSound("/raw/shake")
                .setDefaultSound(true)
                .setTag("tagBoom")
                .setClickAction(ClickAction.builder().setType(2).setUrl("https://www.huawei.com").build())
                .setBodyLocKey("M.String.body")
                .addBodyLocArgs("boy").addBodyLocArgs("dog")
                .setTitleLocKey("M.String.title")
                .addTitleLocArgs("Girl").addTitleLocArgs("Cat")
                .setChannelId("Your Channel ID")
                .setNotifySummary("some summary")
                .setMultiLangkey(multiLangKey)
                .setStyle(1)
                .setBigTitle("TEST NOTIFY PUSH MESSAGE TITLE")
                .setBigBody("TEST NOTIFY PUSH MESSAGE BODY")
                .setAutoClear(86400000)
                .setNotifyId(486)
                .setGroup("Group1")
                .setImportance(Importance.LOW.getValue())
                .setLightSettings(lightSettings)
                .setBadge(BadgeNotification.builder().setAddNum(1).setBadgeClass("Classic").build())
                .setVisibility(Visibility.PUBLIC.getValue())
                .setForegroundShow(true)
                .build();

        AndroidConfig androidConfig = AndroidConfig.builder().setCollapseKey(-1)
                .setUrgency(Urgency.HIGH.getValue())
                .setTtl("10000s")
                .setBiTag("the_sample_bi_tag_for_receipt_service")
                .setNotification(androidNotification)
                .build();

        Message.Builder msgBuilder = Message.builder().setNotification(notification)
                .setAndroidConfig(androidConfig);

        for (String token : tokenList) {
            msgBuilder.addToken(token);
        }
        Message message = msgBuilder.build();
        SendResponse response = huaweiMessaging.sendMessage(message);
        return response.getCode().equals(SENT_OK_CODE);
    }
}