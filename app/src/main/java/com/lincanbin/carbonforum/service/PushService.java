package com.lincanbin.carbonforum.service;

import android.app.Activity;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;

import com.lincanbin.carbonforum.NotificationsActivity;
import com.lincanbin.carbonforum.R;
import com.lincanbin.carbonforum.application.CarbonForumApplication;
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
        if(CarbonForumApplication.isLoggedIn()) {
            getNotification();
        }
    }
    private void getNotification(){
        int sleepTime = 3000;
        final Map<String, String> parameter = new HashMap<>();
        SharedPreferences mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences cacheSharedPreferences = getSharedPreferences("MainCache", Activity.MODE_PRIVATE);
        int notificationsNumber = Integer.parseInt(cacheSharedPreferences.getString("notificationsNumber", "0"));

        JSONObject jsonObject = HttpUtil.postRequest(getApplicationContext(), APIAddress.PUSH_SERVICE_URL, parameter, false, true);
        try {
            if(jsonObject != null && jsonObject.getInt("Status") == 1){
                int newMessageNumber = jsonObject.getInt("NewMessage");
                //请求成功，延长请求间隔
                if(newMessageNumber > 0){
                    //消息数量大于0，发送通知栏消息
                    NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    //跳转到通知页的intent
                    PendingIntent mPendingIntent = PendingIntent.getActivity(
                            getApplicationContext(),
                            0,
                            new Intent(getApplicationContext(), NotificationsActivity.class),
                            0
                    );
                    final Notification.Builder builder = new Notification.Builder(getApplicationContext())
                            .setSmallIcon(R.drawable.ic_launcher)
                            .setContentTitle(getString(R.string.app_name))
                            .setContentText(newMessageNumber + " New Messages")
                            .setContentIntent(mPendingIntent)
                            .setAutoCancel(true);
                    //有新通知的话才振动与响铃
                    if(newMessageNumber != notificationsNumber){
                        //设置振动
                        if(mSharedPreferences.getBoolean("notifications_new_message_vibrate", true)){
                            builder.setLights(Color.BLUE, 500, 500);
                            long[] pattern = {500,500,500,500,500};
                            builder.setVibrate(pattern);
                        }
                        //设置铃声
                        String ringtoneURI = mSharedPreferences.getString("notifications_new_message_ringtone", "content://settings/system/notification_sound");
                        if(!ringtoneURI.isEmpty()){
                            //Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                            Uri alarmSound =  Uri.parse(ringtoneURI);
                            builder.setSound(alarmSound);
                        }
                    }
                    mNotificationManager.cancel(105);
                    if (Build.VERSION.SDK_INT >= 16) {
                        mNotificationManager.notify(105, builder.build());
                    }else{
                        mNotificationManager.notify(105, builder.getNotification());
                    }
                    //请求成功，延长请求间隔
                    sleepTime = 30000;
                }
                //发送广播刷新Drawer
                Intent intent = new Intent();
                intent.setAction("action.refreshDrawer");
                sendBroadcast(intent);
                //保存当前消息数，每次判断消息数量与之前不一致才发送通知。
                try {
                    SharedPreferences.Editor cacheEditor = cacheSharedPreferences.edit();
                    cacheEditor.putString("notificationsNumber", Integer.toString(newMessageNumber));
                    cacheEditor.apply();
                }catch(Exception e){
                    e.printStackTrace();
                }
            }else{
                //请求失败，延长请求间隔
                sleepTime = 30000;
            }

            Thread.sleep(sleepTime);
        }catch(Exception e){
            e.printStackTrace();
        }
        boolean notifications_new_message = mSharedPreferences.getBoolean("notifications_new_message", false);
        if(notifications_new_message) {
            startService(new Intent(this, PushService.class));
        }else {
            stopSelf();
        }
    }
}
