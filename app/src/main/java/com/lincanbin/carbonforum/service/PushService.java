package com.lincanbin.carbonforum.service;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;

import com.lincanbin.carbonforum.R;
import com.lincanbin.carbonforum.config.APIAddress;
import com.lincanbin.carbonforum.util.HttpUtil;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class PushService extends IntentService {
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
                    //TODO: 保存当前消息数，每次判断消息数量与之前不一致才发送通知。
                    NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    /*
                    Intent deleteIntent = new Intent(this, IndexActivity.class);
                    deleteIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NEW_TASK);
                    PendingIntent mDeletePendingIntent = PendingIntent.getActivity(
                            getApplicationContext(),
                            R.string.app_name,
                            deleteIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT);
                    */
                    final Notification.Builder builder = new Notification.Builder(getApplicationContext())
                            .setSmallIcon(R.drawable.ic_launcher)
                            .setContentTitle(getString(R.string.app_name))
                            .setContentText(jsonObject.getString("NewMessage") + " New Messages")
                            .setAutoCancel(true);
                            //.setDeleteIntent(mDeletePendingIntent)
                    mNotificationManager.cancel(105);
                    if (Build.VERSION.SDK_INT >= 16) {
                        mNotificationManager.notify(105, builder.build());
                    }else{
                        mNotificationManager.notify(105, builder.getNotification());
                    }
                    //TODO: 根据设置震动手机以及响铃
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
        SharedPreferences mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean notifications_new_message = mSharedPreferences.getBoolean("notifications_new_message", true);
        if(notifications_new_message) {
            startService(new Intent(this, PushService.class));
        }else {
            stopSelf();
        }
    }
}
