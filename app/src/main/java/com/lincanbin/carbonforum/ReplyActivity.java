package com.lincanbin.carbonforum;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class ReplyActivity extends AppCompatActivity {
    String mTopicID;
    String mPostID;
    String mPostFloor;
    String mUserName;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //取得启动该Activity的Intent对象
        Intent intent =getIntent();
        //取出Intent中附加的数据
        mTopicID = intent.getStringExtra("TopicID");
        mPostID = intent.getStringExtra("PostID");
        mPostFloor = intent.getStringExtra("PostFloor");
        mUserName = intent.getStringExtra("UserName");
        setContentView(R.layout.activity_reply);
    }
}
