/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2019-2024. All rights reserved.
 */
package com.huawei.hms.sample2.util;

import com.huawei.hms.sample2.messaging.HuaweiApp;
import com.huawei.hms.sample2.messaging.HuaweiCredential;
import com.huawei.hms.sample2.messaging.HuaweiOption;

import java.util.ResourceBundle;

public class InitAppUtils {
    /**
     * @return HuaweiApp
     */
    private static HuaweiApp app;
    public static HuaweiApp initializeApp() {
        if (app != null)
            return app;

        // TODO: it needs to change appid and appsecret in /resources/url.properties file
        String appId = ResourceBundle.getBundle("url").getString("appid");
        String appSecret = ResourceBundle.getBundle("url").getString("appsecret");

        // Create HuaweiCredential
        // This appId and appSecret come from Huawei Developer Alliance
        app = initializeApp(appId, appSecret);
        return app;
    }

    private static HuaweiApp initializeApp(String appId, String appSecret) {
        HuaweiCredential credential = HuaweiCredential.builder()
                .setAppId(appId)
                .setAppSecret(appSecret)
                .build();

        // Create HuaweiOption
        HuaweiOption option = HuaweiOption.builder()
                .setCredential(credential)
                .build();

        // Initialize HuaweiApp
        return HuaweiApp.initializeApp(option);
    }
}
