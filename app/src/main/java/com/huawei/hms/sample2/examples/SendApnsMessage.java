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

import com.huawei.hms.sample2.apns.Alert;
import com.huawei.hms.sample2.apns.ApnsHeaders;
import com.huawei.hms.sample2.apns.ApnsHmsOptions;
import com.huawei.hms.sample2.apns.Aps;
import com.huawei.hms.sample2.exception.HuaweiMesssagingException;
import com.huawei.hms.sample2.message.ApnsConfig;
import com.huawei.hms.sample2.message.Message;
import com.huawei.hms.sample2.messaging.HuaweiApp;
import com.huawei.hms.sample2.messaging.HuaweiMessaging;
import com.huawei.hms.sample2.reponse.SendResponse;
import com.huawei.hms.sample2.util.InitAppUtils;

public class SendApnsMessage {
    /**
     * send apns message
     *
     * @throws HuaweiMesssagingException
     */
    public void sendApnsMessage() throws HuaweiMesssagingException {
        HuaweiApp app = InitAppUtils.initializeApp();
        HuaweiMessaging huaweiMessaging = HuaweiMessaging.getInstance(app);

        ApnsHeaders apnsHeaders = ApnsHeaders.builder().setApnsId("21349-141324").build();

        Alert altert = Alert.builder().setTitle("titile")
                .setBody("body")
                .setLaunchImage("image")
                .build();

        Aps aps = Aps.builder().setAlert(altert)
                .setBadge(1)
                .setSound("wtewt.mp4")
                .setContentAvailable(1)
                .setCategory("category")
                .setThreadId("id")
                .build();

        ApnsHmsOptions apnsHmsOptions = ApnsHmsOptions.builder().setTargetUserType(1).build();

        ApnsConfig apns = ApnsConfig.builder().setApnsHeaders(apnsHeaders)
                .addPayloadAps(aps)
                .addPayload("acme_account", "jane.appleseed@apple.com")
                .addPayload("acme_message", "message123456")
                .setHmsOptions(apnsHmsOptions)
                .build();

        Message message = Message.builder().setApns(apns)
                .addToken("9FDA406A04BDE017A2F53EB9831846FBF5308567DE9A4E986D96512136F72C3D")
                .build();

        SendResponse response = huaweiMessaging.sendMessage(message);
    }
}
