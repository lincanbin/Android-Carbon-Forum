package com.lincanbin.carbonforum;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;

import com.lincanbin.carbonforum.service.ReplyService;
import com.lincanbin.carbonforum.util.markdown.MarkdownProcessor;

public class ReplyActivity extends AppCompatActivity {
    Toolbar mToolbar;
    String mTopicID;
    String mPostID;
    String mPostFloor;
    String mUserName;
    String defaultContent;
    String contentHTML;
    EditText mContent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //取得启动该Activity的Intent对象
        Intent mIntent =getIntent();
        //取出Intent中附加的数据
        mTopicID = mIntent.getStringExtra("TopicID");
        mPostID = mIntent.getStringExtra("PostID");
        mPostFloor = mIntent.getStringExtra("PostFloor");
        mUserName = mIntent.getStringExtra("UserName");
        defaultContent = mIntent.getStringExtra("DefaultContent");
        setContentView(R.layout.activity_reply);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mContent = (EditText) findViewById(R.id.content);
        mContent.setText(defaultContent);
        //自动弹出键盘
        mContent.setFocusable(true);
        mContent.setFocusableInTouchMode(true);
        mContent.requestFocus();
        InputMethodManager mInputManager = (InputMethodManager)mContent.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        mInputManager.showSoftInput(mContent, 0);

        if (mToolbar != null) {
            //mToolbar.setTitle(getString(R.string.title_activity_reply));
            if(Integer.parseInt(mPostFloor) == 0){
                mToolbar.setTitle(getString(R.string.title_activity_reply));
            }else{
                mToolbar.setTitle(getString(R.string.action_reply_to) + " @" + mUserName);
            }
            setSupportActionBar(mToolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

            ImageButton imageButton = (ImageButton) mToolbar.findViewById(R.id.reply_button);
            imageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(mContent.getText().toString().length() > 0) {
                        MarkdownProcessor mMarkdownProcessor = new MarkdownProcessor();
                        int currentPostFloor = Integer.parseInt(mPostFloor);
                        if (currentPostFloor == 0) {
                            contentHTML = mMarkdownProcessor.markdown(mContent.getText().toString());
                        } else {
                            contentHTML = "<p>\n" + getString(R.string.action_reply_to) +
                                    " <a href=\"/t/" + mTopicID + "#Post" + mPostID + "\">#" + (currentPostFloor == -1 ? "0" : mPostFloor) + "</a> @" + mUserName + " :<br/>\n" +
                                    "</p><p>" + mMarkdownProcessor.markdown(mContent.getText().toString()) + "</p>";
                        }
                        Intent intent = new Intent(ReplyActivity.this, ReplyService.class);
                        intent.putExtra("TopicID", mTopicID);
                        intent.putExtra("Content", contentHTML);
                        startService(intent);
                        onBackPressed();
                    }else{
                        Snackbar.make(view, getString(R.string.content_empty), Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                    }
                }
            });
        }
        //TODO: 根据草稿恢复现场
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
