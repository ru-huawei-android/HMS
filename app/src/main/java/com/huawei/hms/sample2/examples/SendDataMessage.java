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

import android.util.Log;

import com.huawei.hms.sample2.exception.HuaweiMesssagingException;
import com.huawei.hms.sample2.message.AndroidConfig;
import com.huawei.hms.sample2.message.Message;
import com.huawei.hms.sample2.messaging.HuaweiApp;
import com.huawei.hms.sample2.messaging.HuaweiMessaging;
import com.huawei.hms.sample2.model.Urgency;
import com.huawei.hms.sample2.reponse.SendResponse;
import com.huawei.hms.sample2.util.InitAppUtils;

import java.util.ResourceBundle;

import static com.huawei.hms.sample2.util.Constants.SENT_OK_CODE;

public class SendDataMessage {
    private String[] tokenList;

    private String getDefaultToken() {
        // TODO: test token is taken from test_token in /resources/url.properties file
        return ResourceBundle.getBundle("url").getString("test_token");
    }

    public SendDataMessage() {
        tokenList = new String[] { getDefaultToken() };
    }

    public SendDataMessage(String token) {
        if (token == null || token.isEmpty())
            token = getDefaultToken();
        tokenList = new String[] { token };
    }

    public SendDataMessage(String[] tokenList) {
        if (tokenList == null || tokenList.length <= 0)
            tokenList = new String[] { getDefaultToken() };
        this.tokenList = tokenList;
    }
    /**
     * send data message
     *
     * @throws HuaweiMesssagingException
     */
    public boolean sendTransparent() throws HuaweiMesssagingException {
        HuaweiApp app = InitAppUtils.initializeApp();
        HuaweiMessaging huaweiMessaging = HuaweiMessaging.getInstance(app);

        AndroidConfig androidConfig = AndroidConfig.builder().setCollapseKey(-1)
                .setData("{'p1':'p1', 'p2':'p2'}")
                .setUrgency(Urgency.HIGH.getValue())
                .setTtl("10000s")
                .setBiTag("the_sample_bi_tag_for_receipt_service")
                .build();


        Message.Builder msgBuilder = Message.builder().setData("{'k1':'v1', 'k2':'v2'}")
                .setAndroidConfig(androidConfig);

        for (String token : tokenList) {
            msgBuilder.addToken(token);
        }
        Message message = msgBuilder.build();

        SendResponse response = huaweiMessaging.sendMessage(message);
        Log.i("RESPONSE", response.getMsg());
        return response.getCode().equals(SENT_OK_CODE);
    }
}
