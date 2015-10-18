package com.lincanbin.carbonforum;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
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
import android.view.View;

import com.lincanbin.carbonforum.adapter.TopicAdapter;
import com.lincanbin.carbonforum.config.APIAddress;
import com.lincanbin.carbonforum.service.PushService;
import com.lincanbin.carbonforum.util.HttpUtil;
import com.lincanbin.carbonforum.util.JSONUtil;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileSettingDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;
import com.mikepenz.materialdrawer.util.RecyclerViewCacheUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

//http://stackoverflow.com/questions/28150100/setsupportactionbar-throws-error/28150167
public class IndexActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {
    private Toolbar mToolbar;
    //save our header or result
    private AccountHeader headerResult = null;
    private Drawer mDrawer = null;
    private RecyclerView mRecyclerView ;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private FloatingActionButton mFloatingActionButton;
    private TopicAdapter mAdapter;
    private SharedPreferences mSharedPreferences;
    //private ActionBarDrawerToggle mDrawerToggle;
    private int currentPage = 0;
    private int totalPage = 65536;
    private Boolean enableScrollListener = true;
    private List<Map<String,Object>> topicList = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_index);
        //注册一个广播用来登录和退出时刷新Drawer
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("action.refreshDrawer");
        registerReceiver(mRefreshDrawerBroadcastReceiver, intentFilter);
        // 设置ToolBar
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        if (mToolbar != null) {
            setSupportActionBar(mToolbar);//把Toolbar当做ActionBar给设置了
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setTitle(R.string.app_name);
            //mToolbar.bringToFront();
            //toolbar.setLogo(R.drawable.ic_launcher);
            // toolbar.setSubtitle("Sub title");
            mSharedPreferences = this.getSharedPreferences("UserInfo", Activity.MODE_PRIVATE);
            refreshDrawer(savedInstanceState);
        }
        //下拉刷新监听器
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.activity_index_swipe_refresh_layout);
        //设置刷新时动画的颜色，可以设置4个
        mSwipeRefreshLayout.setColorSchemeResources(
                R.color.material_light_blue_700,
                R.color.material_red_700,
                R.color.material_orange_700,
                R.color.material_light_green_700
        );
        mSwipeRefreshLayout.setOnRefreshListener(this);
        //RecyclerView
        mRecyclerView = (RecyclerView) findViewById(R.id.topic_list);
        //使RecyclerView保持固定的大小，这样会提高RecyclerView的性能
        mRecyclerView.setHasFixedSize(true);
        // 创建一个线性布局管理器
        final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        //setOnScrollListener已废弃，使用addOnScrollListener需要在使用后用clearOnScrollListeners()移除监听器
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                // 当不滚动时
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    //获取最后一个完全显示的ItemPosition
                    int lastVisibleItem = layoutManager.findLastCompletelyVisibleItemPosition();
                    int totalItemCount = layoutManager.getItemCount();
                    // 判断是否滚动到底部，并且是向右滚动
                    if (lastVisibleItem >= (totalItemCount - 5) && enableScrollListener && currentPage < totalPage) {
                        //加载更多功能的代码
                        loadTopic(currentPage + 1, false);
                    }
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                //dx用来判断横向滑动方向，dy用来判断纵向滑动方向
                /*
                if (dx > 0) {
                    //大于0表示，正在向右滚动
                } else {
                    //小于等于0 表示停止或向左滚动
                }
                */
            }
        });
        // 设置布局管理器
        mRecyclerView.setLayoutManager(layoutManager);
        //设置Item默认动画
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        //指定数据集
        mAdapter = new TopicAdapter(this);
        mAdapter.setData(topicList);
        //设置Adapter
        mRecyclerView.setAdapter(mAdapter);
        /*
        //添加事件监听器
        mAdapter.setOnRecyclerViewListener(new TopicAdapter.OnRecyclerViewListener() {
            @Override
            public void onItemClick(int position) {
                Toast.makeText(IndexActivity.this, "onItemClick" + topicList.get(position).get("Topic").toString(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public boolean onItemLongClick(int position) {
                Toast.makeText(IndexActivity.this, "onItemLongClick" + topicList.get(position).get("Topic").toString(), Toast.LENGTH_SHORT).show();
                return true;
            }
        });
        */
        mFloatingActionButton = (FloatingActionButton) findViewById(R.id.fab);
        mFloatingActionButton.setImageDrawable(new IconicsDrawable(this).icon(GoogleMaterial.Icon.gmd_add).color(Color.WHITE));
        mFloatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        //Activity渲染完毕时加载帖子，使用缓存
        loadTopic(1, true);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mRefreshDrawerBroadcastReceiver);
    }
    //加载帖子列表
    private void loadTopic(int targetPage, Boolean enableCache) {
            new GetTopicsTask(targetPage, enableCache).execute();
    }
    // broadcast receiver
    private BroadcastReceiver mRefreshDrawerBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals("action.refreshDrawer")) {
                refreshDrawer(null);
            }
        }
    };
        private void refreshDrawer(Bundle savedInstanceState){
        try{
            //Log.v("UserID", mSharedPreferences.getString("UserID", "0"));
            if(Integer.parseInt(mSharedPreferences.getString("UserID", "0")) == 0){
                final IProfile profile = new ProfileDrawerItem()
                        .withName("Not logged in")
                        .withIcon(R.drawable.profile)
                        .withIdentifier(0);
                // Create the AccountHeader
                headerResult = new AccountHeaderBuilder()
                        .withActivity(this)
                        .withHeaderBackground(R.drawable.header)
                        .withSelectionListEnabledForSingleProfile(false)
                        .addProfiles(
                                profile
                        )
                        .withSavedInstance(savedInstanceState)
                        .build();
            }else{
                final IProfile profile = new ProfileDrawerItem()
                        .withName(mSharedPreferences.getString("UserName", "lincanbin"))
                        .withEmail(mSharedPreferences.getString("UserMail", mSharedPreferences.getString("UserName", "lincanbin")))
                        .withIcon(Uri.parse(APIAddress.MIDDLE_AVATAR_URL(mSharedPreferences.getString("UserID", "0"), "large")))
                                .withIdentifier(Integer.parseInt(mSharedPreferences.getString("UserID", "0")));
                // Create the AccountHeader
                headerResult = new AccountHeaderBuilder()
                        .withActivity(this)
                        .withHeaderBackground(R.drawable.header)
                        .withSelectionListEnabledForSingleProfile(false)
                        //.withTranslucentStatusBar(false)
                        .addProfiles(
                                profile,
                                //don't ask but google uses 14dp for the add account icon in gmail but 20dp for the normal icons (like manage account)
                                new ProfileSettingDrawerItem()
                                        .withName("Change Account")
                                        .withIcon(GoogleMaterial.Icon.gmd_person_add)
                                        .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                                            @Override
                                            public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                                                    IndexActivity.this.startActivity(new Intent(IndexActivity.this, LoginActivity.class));
                                                return false;
                                            }
                                        }),
                                new ProfileSettingDrawerItem()
                                        .withName("Exit")
                                        .withIcon(GoogleMaterial.Icon.gmd_remove_circle_outline)
                                        .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                                            @Override
                                            public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                                                mSharedPreferences.edit().clear().apply();
                                                refreshDrawer(null);
                                                return false;
                                            }
                                        })
                        )
                        .withSavedInstance(savedInstanceState)
                        .build();
                //开启推送
                startService(new Intent(IndexActivity.this, PushService.class));
            }
        } catch (ClassCastException e) {
            e.printStackTrace();
        }

        //Create the drawer
        mDrawer = new DrawerBuilder()
                .withActivity(this)
                .withActionBarDrawerToggle(true)
                .withToolbar(mToolbar)
                .withHasStableIds(true)
                .withAccountHeader(headerResult) //set the AccountHeader we created earlier for the header
                // .withTranslucentStatusBar(false)
                .addDrawerItems(
                        new PrimaryDrawerItem().
                                withName(R.string.app_name).
                                withIcon(GoogleMaterial.Icon.gmd_home).
                                withSetSelected(true).
                                withIdentifier(1).
                                withSelectable(true),
                        new PrimaryDrawerItem().
                                withName(R.string.refresh).
                                withIcon(GoogleMaterial.Icon.gmd_autorenew).
                                withIdentifier(2).
                                withSelectable(false),
                        new DividerDrawerItem(),
                        new PrimaryDrawerItem().
                                withName(R.string.login).
                                withIcon(GoogleMaterial.Icon.gmd_person_add).
                                withIdentifier(3).
                                withSelectable(false),
                        new PrimaryDrawerItem().
                                withName(R.string.title_activity_settings).
                                withIcon(GoogleMaterial.Icon.gmd_settings).
                                withIdentifier(4).
                                withSelectable(false)
                ) // add the items we want to use with our Drawer
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        //check if the drawerItem is set.
                        //there are different reasons for the drawerItem to be null
                        //--> click on the header
                        //--> click on the footer
                        //those items don't contain a drawerItem

                        if (drawerItem != null) {
                            Intent intent = null;
                            if (drawerItem.getIdentifier() == 2) {
                                loadTopic(1, false);
                            } else if (drawerItem.getIdentifier() == 3) {
                                intent = new Intent(IndexActivity.this, LoginActivity.class);
                            } else if (drawerItem.getIdentifier() == 4) {
                                intent = new Intent(IndexActivity.this, SettingsActivity.class);
                            }
                            if (intent != null) {
                                IndexActivity.this.startActivity(intent);
                            }
                        }

                        return false;
                    }
                })
                .withSavedInstance(savedInstanceState)
                .withShowDrawerOnFirstLaunch(true)
                .build();
        //if you have many different types of DrawerItems you can magically pre-cache those items to get a better scroll performance
        //make sure to init the cache after the DrawerBuilder was created as this will first clear the cache to make sure no old elements are in
        RecyclerViewCacheUtil.getInstance().withCacheSize(2).init(mDrawer);

        //only set the active selection or active profile if we do not recreate the activity
        if (savedInstanceState == null) {
            // set the selection to the item with the identifier 1
            mDrawer.setSelection(1, false);

            //set the active profile
            //headerResult.setActiveProfile(profile);
        }
        //添加消息通知
        //mDrawer.updateBadge(4, new StringHolder(10 + ""));
    }
    //下拉刷新事件
    @Override
    public void onRefresh() {

        //if(!mSwipeRefreshLayout.isRefreshing()){
            loadTopic(1, false);
        //}
    }
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggls
    }
    @Override
    public void onBackPressed() {
        //handle the back press :D close the drawer first and if the drawer is closed close the activity
        if (mDrawer != null && mDrawer.isDrawerOpen()) {
            mDrawer.closeDrawer();
        } else {
            super.onBackPressed();
        }
    }
    public class GetTopicsTask extends AsyncTask<Void, Void, List<Map<String,Object>>> {
        private int targetPage;
        private Boolean enableCache;
        private int positionStart;
        SharedPreferences cacheSharedPreferences = IndexActivity.this.getSharedPreferences("MainCache", Activity.MODE_PRIVATE);
		public GetTopicsTask(int targetPage, Boolean enableCache) {
			this.targetPage = targetPage;
            this.enableCache = enableCache;
		}
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            enableScrollListener = false;
            if(enableCache){
                topicList = JSONUtil.json2List(JSONUtil.json2Object(cacheSharedPreferences.getString("topicsCache", "{\"TopicsArray\":[]}")), "TopicsArray");
                if(topicList != null){
                    mAdapter.setData(topicList);
                    mAdapter.notifyDataSetChanged();
                }
            }
            mSwipeRefreshLayout.post(new Runnable(){
                @Override
                public void run(){
                	mSwipeRefreshLayout.setRefreshing(true);
                }
            });
            
            //Toast.makeText(IndexActivity.this, "Before AsyncTask", Toast.LENGTH_SHORT).show();
            
        }

        @Override
        protected void onPostExecute(List<Map<String, Object>> result) {
            super.onPostExecute(result);
            if(result!=null && !result.isEmpty()) {
                if (targetPage > 1) {
                    positionStart = topicList.size() - 1;
                    topicList.addAll(result);
                    mAdapter.setData(topicList);
                    //局部刷新，更好的性能
                    mAdapter.notifyItemRangeChanged(positionStart, mAdapter.getItemCount());
                } else {
                    topicList = result;
                    mAdapter.setData(topicList);
                    //全部刷新
                    mAdapter.notifyDataSetChanged();
                }
                //更新当前页数
                currentPage = targetPage;
            }else{
                Snackbar.make(mFloatingActionButton, R.string.network_error, Snackbar.LENGTH_LONG).setAction("Action", null).show();
                //Toast.makeText(IndexActivity.this, R.string.network_error, Toast.LENGTH_SHORT).show();
            }
            //移除刷新控件
            mSwipeRefreshLayout.setRefreshing(false);
            enableScrollListener = true;
            //Toast.makeText(IndexActivity.this, "AsyncTask End", Toast.LENGTH_SHORT).show();
        }

        @Override
        protected List<Map<String, Object>> doInBackground(Void... params) {
            List<Map<String,Object>> list;
            JSONObject jsonObject = HttpUtil.getRequest(IndexActivity.this, APIAddress.HOME_URL(targetPage), false, false);
            //Log.v("JSON", str);
            if(jsonObject != null){
                try {
                    totalPage = jsonObject.getInt("TotalPage");
                }catch(JSONException e){
                    e.printStackTrace();
                }
                if(targetPage == 1){
                    try {
                        SharedPreferences.Editor cacheEditor = cacheSharedPreferences.edit();
                        cacheEditor.putString("topicsCache", jsonObject.toString(0));
                        cacheEditor.apply();
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                }
            }
            list = JSONUtil.json2List(jsonObject, "TopicsArray");
            //Log.v("List", list.toString());
            return list;
        }

    }
}