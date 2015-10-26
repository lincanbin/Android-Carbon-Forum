package com.lincanbin.carbonforum;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

public class ReplyActivity extends AppCompatActivity {
    Toolbar mToolbar;
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
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        if (mToolbar != null) {
            mToolbar.setTitle(getString(R.string.action_reply_to) + "#" + mPostFloor + " @" + mUserName);
            setSupportActionBar(mToolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // API 5+ solution
                onBackPressed();
                //NavUtils.navigateUpFromSameTask(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
