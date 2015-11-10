package com.lincanbin.carbonforum.service;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import com.lincanbin.carbonforum.R;
import com.lincanbin.carbonforum.TopicActivity;
import com.lincanbin.carbonforum.config.APIAddress;
import com.lincanbin.carbonforum.util.HttpUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class NewService extends IntentService {
    public String mTitle = "";
    public String mTag = "";
    public String mContent = "";

    public NewService() {
        super("NewService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            mTitle = intent.getStringExtra("Title");
            mTag = intent.getStringExtra("Tag");
            mContent = intent.getStringExtra("Content");
            newTopic();
        }
    }

    private void newTopic(){
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        final Map<String, String> parameter = new HashMap<>();
        String[] TagsArray= mTag.replace("，",",").split(",");
        parameter.put("Title", mTitle);
        for(String mTagItem:TagsArray) {
            parameter.put("Tag[]#" + mTagItem, mTagItem);
        }
        parameter.put("Content", mContent);

        //显示“发送中”提示
        String shortContent = mContent.replaceAll("<!--.*?-->", "").replaceAll("<[^>]+>", "");//移除HTML标签
        final Notification.Builder builder = new Notification.Builder(getApplicationContext())
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(getString(R.string.sending))
                .setContentInfo(shortContent.subSequence(0, shortContent.length()))
                .setOngoing(true);
        if (Build.VERSION.SDK_INT >= 16) {
            mNotificationManager.notify(102001, builder.build());
        }else{
            mNotificationManager.notify(102001, builder.getNotification());
        }
        final JSONObject jsonObject = HttpUtil.postRequest(getApplicationContext(), APIAddress.NEW_URL, parameter, false, true);
        // 移除“发送中”通知
        mNotificationManager.cancel(102001);
        try {
            if(jsonObject != null && jsonObject.getInt("Status") == 1) {
                //发帖成功，并跳转Activity
                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), getString(R.string.send_success), Toast.LENGTH_SHORT).show();
                    }
                });
                //跳转Activity
                Intent intent = new Intent(getApplicationContext(), TopicActivity.class);
                intent.putExtra("Topic", mTitle );
                intent.putExtra("TopicID", jsonObject.getString("TopicID"));
                intent.putExtra("TargetPage", "1");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);

            } else {
                //回帖不成功，Toast，并添加重发的通知栏通知
                PendingIntent mPendingIntent = PendingIntent.getService(
                        getApplicationContext(),
                        0,
                        new Intent(getApplicationContext(), NewService.class)
                                .putExtra("Title", mTitle)
                                .putExtra("Tag", mTag)
                                .putExtra("Content", mContent),
                        0
                );
                final Notification.Builder failBuilder = new Notification.Builder(getApplicationContext())
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle(getString(R.string.app_name))
                        .setContentText(getString(R.string.resend_topic))
                        .setContentIntent(mPendingIntent)
                        .setAutoCancel(true);
                if (Build.VERSION.SDK_INT >= 16) {
                    mNotificationManager.notify(102003, failBuilder.build());
                }else{
                    mNotificationManager.notify(102003, failBuilder.getNotification());
                }
                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if(jsonObject != null) {
                            try {
                                Toast.makeText(getApplicationContext(), jsonObject.getString("ErrorMessage"), Toast.LENGTH_SHORT).show();
                            }catch(JSONException e){
                                e.printStackTrace();
                            }
                        }else{
                            Toast.makeText(getApplicationContext(), getApplicationContext().getString(R.string.network_error), Toast.LENGTH_SHORT).show();
                        }

                    }
                });
            }
        }catch(JSONException e){
            e.printStackTrace();
        }
    }
}
