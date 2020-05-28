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
package com.huawei.hms.sample2.messaging;

import android.os.StrictMode;

import com.alibaba.fastjson.JSONObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ResourceBundle;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import cz.msebera.android.httpclient.client.methods.CloseableHttpResponse;
import cz.msebera.android.httpclient.client.methods.HttpPost;
import cz.msebera.android.httpclient.entity.StringEntity;
import cz.msebera.android.httpclient.impl.client.CloseableHttpClient;
import cz.msebera.android.httpclient.impl.client.HttpClients;
import cz.msebera.android.httpclient.util.EntityUtils;

/**
 * Accept the appId and appSecret given by the user and build the credential
 * Every app has a credential which is for certification
 */
public class HuaweiCredential {
    private static final Logger logger = LoggerFactory.getLogger(HuaweiCredential.class);
    // TODO: it is taken from token_server in /resources/url.properties file
    private final String PUSH_AT_URL = ResourceBundle.getBundle("url").getString("token_server");

    private String appId;
    private String appSecret;

    private String accessToken;
    private long expireIn;
    private Lock lock;

    private final CloseableHttpClient httpClient = HttpClients.createDefault();

    private HuaweiCredential(Builder builder) {
        this.lock = new ReentrantLock();
        this.appId = builder.appId;
        this.appSecret = builder.appSecret;
    }

    /**
     * Refresh accessToken via HCM manually.
     */
    public final void refreshToken() {
        try {
            executeRefresh();
        } catch (IOException e) {
            logger.debug("Fail to refresh token!", e);
        }
    }

    private void executeRefresh() throws IOException {
        String requestBody = createRequestBody(appId, appSecret);

        //TODO this is very bad for test only
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        //

        HttpPost httpPost = new HttpPost(PUSH_AT_URL);
        StringEntity entity = new StringEntity(requestBody);

        httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");
        httpPost.setEntity(entity);
        CloseableHttpResponse response = httpClient.execute(httpPost);
        String jsonStr = EntityUtils.toString(response.getEntity());
        int statusCode = response.getStatusLine().getStatusCode();
        if (statusCode == 200) {
            JSONObject jsonObject = JSONObject.parseObject(jsonStr);
            this.accessToken = jsonObject.getString("access_token");
            this.expireIn = jsonObject.getLong("expires_in") * 1000L;
        } else {
            logger.debug("Fail to refresh token!");
        }
    }

    private String createRequestBody(String appId, String appSecret) {
        return MessageFormat.format("grant_type=client_credentials&client_secret={0}&client_id={1}", appSecret, appId);
    }

    /**
     * getter
     */
    public final String getAccessToken() {
        this.lock.lock();

        String tmp;
        try {
            tmp = this.accessToken;
        } finally {
            this.lock.unlock();
        }

        return tmp;
    }

    public final long getExpireIn() {
        this.lock.lock();

        long tmp;
        try {
            tmp = this.expireIn;
        } finally {
            this.lock.unlock();
        }

        return tmp;
    }

    protected CloseableHttpClient getHttpClient() {
        return httpClient;
    }

    public String getAppId() {
        return appId;
    }

    /**
     * Builder for constructing {@link HuaweiCredential}.
     */
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String appId;
        private String appSecret;

        private Builder() {
        }

        public Builder setAppId(String appId) {
            this.appId = appId;
            return this;
        }

        public Builder setAppSecret(String appSecret) {
            this.appSecret = appSecret;
            return this;
        }

        public HuaweiCredential build() {
            return new HuaweiCredential(this);
        }
    }
}
