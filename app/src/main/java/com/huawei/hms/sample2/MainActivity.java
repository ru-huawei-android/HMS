package com.huawei.hms.sample2;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.huawei.hms.sample2.examples.SendDataMessage;
import com.huawei.hms.sample2.examples.SendNotifyMessage;
import com.huawei.hms.sample2.examples.SendRawMessage;
import com.huawei.hms.sample2.exception.HuaweiMesssagingException;
import com.huawei.agconnect.config.AGConnectServicesConfig;
import com.huawei.hms.aaid.HmsInstanceId;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ResourceBundle;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "MainActivity";
    private String pushToken;

    @Override
    protected void onCreate(Bundle savedInstanceState)  {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnToken = findViewById(R.id.getTokenBtn);
        Button sendNotificationPushBtn = findViewById(R.id.sendNotificationMessageBtn);
        Button sendRawPushBtn = findViewById(R.id.sendRawMessageBtn);
        Button sendDataMessageBtn = findViewById(R.id.sendDataMessageBtn);

        btnToken.setOnClickListener(this);
        sendNotificationPushBtn.setOnClickListener(this);
        sendRawPushBtn.setOnClickListener(this);
        sendDataMessageBtn.setOnClickListener(this);

        getIntentData(getIntent());
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.getTokenBtn:
                obtainToken();
                break;
            case R.id.sendNotificationMessageBtn:
                sendNotificationPushBtnClicked();
                break;
            case R.id.sendRawMessageBtn:
                sendRawPushBtnClicked();
                break;
            case R.id.sendDataMessageBtn:
                sendDataPushBtnClicked();
            default:
                break;
        }
    }

    /**
     * Отправить notification тип PUSH сообщения
     * по-умолчанию система их обрабатывает автоматически
     */
    public void sendNotificationPushBtnClicked() {
        try {
            SendNotifyMessage pushMsg = new SendNotifyMessage(pushToken);
            showSendingResult(pushMsg.sendNotification());
        } catch (HuaweiMesssagingException ex) {
            Toast.makeText(MainActivity.this, ex.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            Log.i(TAG, ex.getLocalizedMessage());
        }
    }

    /**
     * Отправить PUSH сообщение, составленное из json в строчной переменной
     * для упрощения тестирования голого json-а
     */
    public void sendRawPushBtnClicked() {
        // TODO: test json is taken from test_json in /resources/url.properties file
        String testJsonMessage = ResourceBundle.getBundle("url").getString("test_json");
        if (pushToken != null) {
            JSONObject jsonObject;
            try {
                jsonObject = new JSONObject(testJsonMessage);
                JSONObject jsonObjectMessage = jsonObject.getJSONObject("message");
                JSONArray tokenJsonArray = jsonObjectMessage.getJSONArray("token");
                tokenJsonArray.put(0, pushToken);
                testJsonMessage = jsonObject.toString();
            } catch (JSONException ex){
                Toast.makeText(MainActivity.this, ex.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                Log.d("Error", ex.toString());
                return;
            }
        }

        try {
            SendRawMessage rawMsg = new SendRawMessage(testJsonMessage);
            showSendingResult(rawMsg.sendRawMessage());
        } catch (HuaweiMesssagingException ex) {
            Toast.makeText(MainActivity.this, ex.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            Log.i(TAG, ex.getLocalizedMessage());
        }
    }

    /**
     * Отправить data тип PUSH сообщения
     * по-умолчанию данные получает сервис в методе onMessageReceived()
     */
    public void sendDataPushBtnClicked() {
        try {
            SendDataMessage pushMsg = new SendDataMessage(pushToken);
            showSendingResult(pushMsg.sendTransparent());
        } catch (HuaweiMesssagingException ex) {
            Toast.makeText(MainActivity.this, ex.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            Log.i(TAG, ex.getLocalizedMessage());
        }
    }

    private void showSendingResult(boolean success) {
        if (success) {
            Toast.makeText(MainActivity.this, "Push message successfully sent", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(MainActivity.this, "Could not send the message", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Получить токен
     */
    private void obtainToken() {
        Log.i(TAG, "get token: begin");

        // get token
        new Thread() {
            @Override
            public void run() {
                try {
                    String appId = AGConnectServicesConfig.fromContext(MainActivity.this).getString("client/app_id");
                    pushToken = HmsInstanceId.getInstance(MainActivity.this).getToken(appId, "HCM");
                    if (!TextUtils.isEmpty(pushToken)) {
                        Log.i(TAG, "get token:" + pushToken);
                        showToastOnUi("the token received successfully");
                    }
                } catch (Exception e) {
                    Log.i(TAG,"getToken failed, " + e);
                    showToastOnUi("could not receive the token");
                }
            }
        }.start();
    }

    void showToastOnUi(String string) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, string, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getIntentData(Intent intent) {
        if (null != intent) {
            // Developers can use the following three lines of code to obtain the values for dotting statistics.
            String msgid = intent.getStringExtra("_push_msgid");
            String cmdType = intent.getStringExtra("_push_cmd_type");
            int notifyId = intent.getIntExtra("_push_notifyid", -1);
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                for (String key : bundle.keySet()) {
                    String content = bundle.getString(key);
                    Log.i(TAG, "PUSH_CLICKED data from push, key = " + key + ", content = " + content);
                }
            }
            Log.i(TAG, "receive data from push, msgId = " + msgid + ", cmd = " + cmdType + ", notifyId = " + notifyId);
        } else {
            Log.i(TAG, "intent is null");
        }
    }
}
