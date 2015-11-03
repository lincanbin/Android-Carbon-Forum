package com.lincanbin.carbonforum.service;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
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
        final Map<String, String> parameter = new HashMap<>();
        parameter.put("TopicID", mTopicID);
        parameter.put("Content", mContent);
        //显示“回复中”提示
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        final Notification.Builder builder = new Notification.Builder(getApplicationContext())
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(getString(R.string.replying))
                .setAutoCancel(false);
        if (Build.VERSION.SDK_INT >= 16) {
            mNotificationManager.notify(102, builder.build());
        }else{
            mNotificationManager.notify(102, builder.getNotification());
        }
        //TODO: 保存草稿
        final JSONObject jsonObject = HttpUtil.postRequest(getApplicationContext(), APIAddress.REPLY_URL, parameter, false, true);
        try {
            if(jsonObject != null) {
                if (jsonObject.getInt("Status") == 1){
                    //回帖成功，移除“发送中”通知，并发送广播告知成功
                    mNotificationManager.cancel(102);
                    Toast.makeText(getApplicationContext(), getString(R.string.reply_success), Toast.LENGTH_SHORT).show();
                    //发送广播刷新帖子（如果还在看那个帖子的话）
                    Intent intent = new Intent();
                    intent.putExtra("TargetPage", jsonObject.getInt("Page"));
                    intent.setAction("action.refreshTopic");
                    sendBroadcast(intent);
                    //TODO 移除草稿
                }else{
                    //TODO: 回帖不成功，Toast并StartActivity，返回回复页面。备选方案，报警。
                    //Toast.makeText(getApplicationContext(), jsonObject.getString("ErrorMessage"), Toast.LENGTH_SHORT).show();
                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Toast.makeText(getApplicationContext(), jsonObject.getString("ErrorMessage"), Toast.LENGTH_SHORT).show();
                            }catch(JSONException e){
                                e.printStackTrace();
                            }
                        }
                    });
                }
            }
        }catch(JSONException e){
            e.printStackTrace();
        }
    }


}
