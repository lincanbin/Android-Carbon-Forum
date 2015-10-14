package com.lincanbin.carbonforum;

import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.lincanbin.carbonforum.adapter.PostAdapter;
import com.lincanbin.carbonforum.config.APIAddress;
import com.lincanbin.carbonforum.util.HttpUtil;
import com.lincanbin.carbonforum.util.JSONUtil;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TopicActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {
    private  Toolbar mToolbar;
    private RecyclerView mRecyclerView ;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private FloatingActionButton mFloatingActionButton;
    private PostAdapter mAdapter;
    private String mTopic;
    private String mTopicID;
    private String mTopicPage;
    private int currentPage = 0;
    private int totalPage = 65536;
    private Boolean enableScrollListener = true;
    private List<Map<String,Object>> postList = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //取得启动该Activity的Intent对象
        Intent intent =getIntent();
        //取出Intent中附加的数据
        mTopic = intent.getStringExtra("Topic");
        mTopicID = intent.getStringExtra("TopicID");
        mTopicPage = intent.getStringExtra("TargetPage");
        setContentView(R.layout.activity_topic);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(mTopic);

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.activity_topic_swipe_refresh_layout);
        mSwipeRefreshLayout.setColorSchemeResources(
                R.color.material_light_blue_700,
                R.color.material_red_700,
                R.color.material_orange_700,
                R.color.material_light_green_700
        );
        mSwipeRefreshLayout.setOnRefreshListener(this);
        //RecyclerView
        mRecyclerView = (RecyclerView) findViewById(R.id.post_list);
        mRecyclerView.setHasFixedSize(true);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    int lastVisibleItem = layoutManager.findLastCompletelyVisibleItemPosition();
                    int totalItemCount = layoutManager.getItemCount();
                    if (lastVisibleItem == (totalItemCount - 1) && enableScrollListener && currentPage < totalPage) {
                        loadPost(currentPage + 1);
                    }
                }
            }
        });
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mAdapter = new PostAdapter(this);
        mAdapter.setData(postList);
        mRecyclerView.setAdapter(mAdapter);

        mFloatingActionButton = (FloatingActionButton) findViewById(R.id.fab);
        mFloatingActionButton.setImageDrawable(new IconicsDrawable(this).icon(GoogleMaterial.Icon.gmd_reply).color(Color.WHITE));
        mFloatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        loadPost(1);
    }
    //加载帖子
    private void loadPost(int targetPage) {
        new GetPostTask(targetPage).execute();
    }
    //下拉刷新事件
    @Override
    public void onRefresh() {
        loadPost(1);
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
    public class GetPostTask extends AsyncTask<Void, Void, JSONObject> {
        private int targetPage;
        private int positionStart;
        public GetPostTask(int targetPage) {
            this.targetPage = targetPage;
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            enableScrollListener = false;
            mSwipeRefreshLayout.post(new Runnable(){
                @Override
                public void run(){
                    mSwipeRefreshLayout.setRefreshing(true);
                }
            });
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            super.onPostExecute(jsonObject);
            //Log.v("JSON", str);
            if(jsonObject != null){
                try {
                    totalPage = jsonObject.getInt("TotalPage");
                }catch(JSONException e){
                    e.printStackTrace();
                }
            }
            List<Map<String,Object>> list = JSONUtil.json2List(jsonObject, "PostsArray");
            //Log.v("List", list.toString());
            if(list!=null && !list.isEmpty()) {
                if (targetPage > 1) {
                    positionStart = postList.size() - 1;
                    postList.addAll(list);
                    mAdapter.setData(postList);
                    mAdapter.notifyItemRangeChanged(positionStart, mAdapter.getItemCount());
                } else {
                    postList = list;
                    mAdapter.setData(postList);
                    mAdapter.notifyDataSetChanged();
                }
                //更新当前页数
                currentPage = targetPage;
            }else{
                Snackbar.make(mFloatingActionButton, R.string.network_error, Snackbar.LENGTH_LONG).setAction("Action", null).show();
                //Toast.makeText(TopicActivity.this, R.string.network_error, Toast.LENGTH_SHORT).show();
            }
            //移除刷新控件
            mSwipeRefreshLayout.setRefreshing(false);
            enableScrollListener = true;
            //Toast.makeText(IndexActivity.this, "AsyncTask End", Toast.LENGTH_SHORT).show();
        }

        @Override
        protected JSONObject doInBackground(Void... params) {
            JSONObject temp = HttpUtil.getRequest(TopicActivity.this, APIAddress.TOPIC_URL(Integer.parseInt(mTopicID), targetPage), false, true);
            //Log.v("TopicJSON", temp.toString());
            return temp;
        }

    }
}
