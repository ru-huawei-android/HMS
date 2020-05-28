package com.huawei.hms.sample2.examples;

import com.huawei.hms.sample2.exception.HuaweiMesssagingException;
import com.huawei.hms.sample2.messaging.HuaweiApp;
import com.huawei.hms.sample2.messaging.HuaweiMessaging;
import com.huawei.hms.sample2.reponse.SendResponse;
import com.huawei.hms.sample2.util.InitAppUtils;

import java.util.ResourceBundle;

import static com.huawei.hms.sample2.util.Constants.SENT_OK_CODE;

public class SendRawMessage {

    private String jsonString;
    public SendRawMessage() {
        // TODO: it is taken from test_json in /resources/url.properties file
        jsonString = ResourceBundle.getBundle("url").getString("test_json");
    }

    public SendRawMessage(String rawMessage) {
        jsonString = rawMessage;
    }

    /**
     * send raw json message
     *
     * @throws HuaweiMesssagingException
     */
    public boolean sendRawMessage() throws HuaweiMesssagingException {
        HuaweiApp app = InitAppUtils.initializeApp();
        HuaweiMessaging huaweiMessaging = HuaweiMessaging.getInstance(app);
        SendResponse response = huaweiMessaging.sendMessage(jsonString, false);
        return response.getCode().equals(SENT_OK_CODE);
    }
}
