package com.lincanbin.carbonforum.service;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.widget.Toast;

import com.lincanbin.carbonforum.IndexActivity;
import com.lincanbin.carbonforum.R;
import com.lincanbin.carbonforum.config.APIAddress;
import com.lincanbin.carbonforum.util.HttpUtil;

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
        //发送通知提醒
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
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
                .setContentText(getString(R.string.replying))
                .setAutoCancel(true)
                .setDeleteIntent(mDeletePendingIntent);
        if (Build.VERSION.SDK_INT >= 16) {
            mNotificationManager.notify(102, builder.build());
        }else{
            mNotificationManager.notify(102, builder.getNotification());
        }

        JSONObject jsonObject = HttpUtil.postRequest(getApplicationContext(), APIAddress.REPLY_URL, parameter, false, true);
        try {
            if(jsonObject != null && jsonObject.getInt("Status") == 1){
                //回帖成功，移除“发送中”通知，并发送广播告知成功
                mNotificationManager.cancel(102);
                Toast.makeText(getApplicationContext(), getString(R.string.reply_success), Toast.LENGTH_SHORT).show();
                //发送广播刷新
                Intent intent = new Intent();
                intent.putExtra("TargetPage", jsonObject.getInt("Page"));
                intent.setAction("action.refreshTopic");
                sendBroadcast(intent);
            }else{
                //TODO: 回帖不成功，Toast并StartActivity
                Toast.makeText(getApplicationContext(), jsonObject.getString("ErrorMessage"), Toast.LENGTH_SHORT).show();
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }


}
