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

public class SendTestMessage {
    public void sendTestMessage() throws HuaweiMesssagingException {
        HuaweiApp app = InitAppUtils.initializeApp();
        HuaweiMessaging huaweiMessaging = HuaweiMessaging.getInstance(app);

        Notification notification = Notification.builder().setTitle("sample title")
                .setBody("sample message body")
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

        AndroidNotification androidNotification = AndroidNotification.builder().setIcon("/raw/ic_launcher2")
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
                .setBigTitle("Big Boom Title")
                .setBigBody("Big Boom Body")
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

        Message message = Message.builder().setNotification(notification)
                .setAndroidConfig(androidConfig)
                .addToken("AFeXinCpLVEk3X-nPH2O_scxpbYO20igRhjbr865Vt-KQUFDlXTTgyCfQQVL5MDK9xGKs3HCXCiodra5Wc8buVUVfHPfMekO61f5IW4VeuRbyUTjdY3qoJZwIf5EuK5A3Q")
                .build();
        SendResponse response = huaweiMessaging.sendMessage(message, true);
    }
}
