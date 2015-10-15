package com.lincanbin.carbonforum.service;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.lincanbin.carbonforum.IndexActivity;
import com.lincanbin.carbonforum.R;
import com.lincanbin.carbonforum.config.APIAddress;
import com.lincanbin.carbonforum.util.HttpUtil;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class PushService extends IntentService {
    private NotificationManager mNotificationManager;
    public PushService() {
        super("PushService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        getNotification();

    }
    private void getNotification(){
        int sleepTime = 3000;
        final Map<String, String> parameter = new HashMap<>();
        JSONObject jsonObject = HttpUtil.postRequest(getApplicationContext(), APIAddress.PUSHSERVICE_URL, parameter, false, true);
        try {
            if(jsonObject != null && jsonObject.getInt("Status") == 1){
                //请求成功，延长请求间隔
                if(jsonObject.getInt("NewMessage") > 0){
                    //消息数量大于0，发送通知栏消息
                    mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    Intent deleteIntent = new Intent(this, IndexActivity.class);
                    deleteIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NEW_TASK);
                    PendingIntent mDeletePendingIntent = PendingIntent.getActivity(
                            getApplicationContext(),
                            R.string.app_name,
                            deleteIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT);
                    final Notification.Builder builder = new Notification.Builder(getApplicationContext())
                            .setSmallIcon(R.drawable.ic_launcher)
                            .setContentTitle(getString(R.string.app_name))
                            .setContentText(jsonObject.getString("NewMessage") + " New Messages")
                            .setAutoCancel(true)
                            .setDeleteIntent(mDeletePendingIntent);

                    final Notification notification = builder.build();
                    mNotificationManager.cancelAll();
                    mNotificationManager.notify(0, notification);
                    //请求成功，延长请求间隔
                    sleepTime = 30000;
                }
            }else{
                //请求失败，延长请求间隔
                sleepTime = 30000;
            }
            Thread.sleep(sleepTime);
        }catch(Exception e){
            e.printStackTrace();
        }
        startService(new Intent(this, PushService.class));
    }
}
