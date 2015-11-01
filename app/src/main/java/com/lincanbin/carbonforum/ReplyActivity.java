package com.lincanbin.carbonforum;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import com.lincanbin.carbonforum.service.ReplyService;

public class ReplyActivity extends AppCompatActivity {
    Toolbar mToolbar;
    String mTopicID;
    String mPostID;
    String mPostFloor;
    String mUserName;
    String defaultContent;
    EditText mContent;
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
        defaultContent = intent.getStringExtra("DefaultContent");
        setContentView(R.layout.activity_reply);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mContent = (EditText) findViewById(R.id.content);
        mContent.setText(defaultContent);
        if (mToolbar != null) {
            mToolbar.setTitle(getString(R.string.action_reply_to) + "#" + mPostFloor + " @" + mUserName);
            setSupportActionBar(mToolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

            ImageButton imageButton = (ImageButton) mToolbar.findViewById(R.id.reply_button);
            imageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(ReplyActivity.this, ReplyService.class);
                    intent.putExtra("TopicID", mTopicID);
                    intent.putExtra("Content", mContent.getText().toString());
                    startService(intent);
                    onBackPressed();
                }
            });
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
