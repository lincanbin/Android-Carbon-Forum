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
import com.lincanbin.carbonforum.config.APIAddress;
import com.lincanbin.carbonforum.util.HttpUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ReplyService extends IntentService {
    public String mTopicID = "0";
    public String mContent = "";

    public ReplyService() {
        super("ReplyService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            mTopicID = intent.getStringExtra("TopicID");
            mContent = intent.getStringExtra("Content");
            reply();
        }
    }

    private void reply() {
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        final Map<String, String> parameter = new HashMap<>();
        parameter.put("TopicID", mTopicID);
        parameter.put("Content", mContent);
        //显示“回复中”提示
        String shortContent = mContent.replaceAll("<!--.*?-->", "").replaceAll("<[^>]+>", "");//移除HTML标签
        final Notification.Builder builder = new Notification.Builder(getApplicationContext())
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(getString(R.string.replying))
                .setContentInfo(shortContent.subSequence(0, shortContent.length()))
                .setOngoing(true);
        if (Build.VERSION.SDK_INT >= 16) {
            mNotificationManager.notify(102001, builder.build());
        }else{
            mNotificationManager.notify(102001, builder.getNotification());
        }
        final JSONObject jsonObject = HttpUtil.postRequest(getApplicationContext(), APIAddress.REPLY_URL, parameter, false, true);
        // 移除“回复中”通知
        mNotificationManager.cancel(102001);
        try {
            if(jsonObject != null && jsonObject.getInt("Status") == 1) {
                //回帖成功，并发送广播告知成功
                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), getString(R.string.reply_success), Toast.LENGTH_SHORT).show();
                    }
                });
                //发送广播刷新帖子（如果还在看那个帖子的话）
                Intent intent = new Intent();
                intent.putExtra("TargetPage", jsonObject.getInt("Page"));
                intent.setAction("action.refreshTopic");
                sendBroadcast(intent);

            } else {
                //回帖不成功，Toast，并添加重发的通知栏通知
                PendingIntent mPendingIntent = PendingIntent.getService(
                        getApplicationContext(),
                        0,
                        new Intent(getApplicationContext(), ReplyService.class).putExtra("TopicID", mTopicID).putExtra("Content", mContent),
                        0
                );
                final Notification.Builder failBuilder = new Notification.Builder(getApplicationContext())
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle(getString(R.string.app_name))
                        .setContentText(getString(R.string.resend_reply))
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