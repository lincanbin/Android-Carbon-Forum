package com.lincanbin.carbonforum;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;

public class TopicActivity extends AppCompatActivity {
    private  Toolbar mToolbar;
    private FloatingActionButton mFloatingActionButton;
    private String mTopic;
    private String mTopicID;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //取得启动该Activity的Intent对象
        Intent intent =getIntent();
        //取出Intent中附加的数据
        mTopic = intent.getStringExtra("Topic");
        mTopicID = intent.getStringExtra("TopicID");

        //Toast.makeText(this, mTopic, Toast.LENGTH_SHORT).show();

        setContentView(R.layout.activity_topic);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(mTopic);

        mFloatingActionButton = (FloatingActionButton) findViewById(R.id.fab);
        mFloatingActionButton.setImageDrawable(new IconicsDrawable(this).icon(GoogleMaterial.Icon.gmd_reply).color(Color.WHITE));
        mFloatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

    }

}
