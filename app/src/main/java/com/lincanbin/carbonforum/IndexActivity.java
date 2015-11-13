package com.lincanbin.carbonforum;

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
import com.lincanbin.carbonforum.application.CarbonForumApplication;
import com.lincanbin.carbonforum.config.APIAddress;
import com.lincanbin.carbonforum.service.PushService;
import com.lincanbin.carbonforum.util.HttpUtil;
import com.lincanbin.carbonforum.util.JSONUtil;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.holder.BadgeStyle;
import com.mikepenz.materialdrawer.holder.StringHolder;
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
    //private SharedPreferences mSharedPreferences;
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
        //mSharedPreferences = getSharedPreferences("UserInfo", Activity.MODE_PRIVATE);
        // 设置ToolBar
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mFloatingActionButton = (FloatingActionButton) findViewById(R.id.fab);
        mFloatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(IndexActivity.this, NewActivity.class);
                startActivity(intent);

            }
        });
        if (mToolbar != null) {
            setSupportActionBar(mToolbar);//把Toolbar当做ActionBar给设置了
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setTitle(R.string.app_name);
            //mToolbar.bringToFront();
            //toolbar.setLogo(R.drawable.ic_launcher);
            // toolbar.setSubtitle("Sub title");

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
            if(!CarbonForumApplication.isLoggedIn()){ //未登录
                //隐藏发帖按钮
                mFloatingActionButton.setVisibility(View.INVISIBLE);
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
            }else{ //已登录
                //显示发帖按钮
                mFloatingActionButton.setVisibility(View.VISIBLE);
                final IProfile profile = new ProfileDrawerItem()
                        .withName(CarbonForumApplication.userInfo.getString("UserName", "lincanbin"))
                        .withEmail(CarbonForumApplication.userInfo.getString("UserMail", CarbonForumApplication.userInfo.getString("UserName", "lincanbin")))
                        .withIcon(Uri.parse(APIAddress.MIDDLE_AVATAR_URL(CarbonForumApplication.userInfo.getString("UserID", "0"), "large")))
                        .withIdentifier(Integer.parseInt(CarbonForumApplication.userInfo.getString("UserID", "0")));
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
                                        .withName(getString(R.string.change_account))
                                        .withIcon(GoogleMaterial.Icon.gmd_accounts)
                                        .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                                            @Override
                                            public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                                                IndexActivity.this.startActivity(new Intent(IndexActivity.this, LoginActivity.class));
                                                return false;
                                            }
                                        }),
                                new ProfileSettingDrawerItem()
                                        .withName(getString(R.string.log_out))
                                        .withIcon(GoogleMaterial.Icon.gmd_close)
                                        .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                                            @Override
                                            public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                                                CarbonForumApplication.userInfo.edit().clear().apply();
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
        DrawerBuilder mDrawerBuilder = new DrawerBuilder()
                .withActivity(this)
                .withActionBarDrawerToggle(true)
                .withToolbar(mToolbar)
                .withHasStableIds(true)
                .withAccountHeader(headerResult) //set the AccountHeader we created earlier for the header
                .withSavedInstance(savedInstanceState)
                .withShowDrawerOnFirstLaunch(true)
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
                                withIcon(GoogleMaterial.Icon.gmd_refresh).
                                withIdentifier(2).
                                withSelectable(false),
                        new DividerDrawerItem()
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
                                intent = new Intent(IndexActivity.this, RegisterActivity.class);
                            } else if (drawerItem.getIdentifier() == 5) {
                                intent = new Intent(IndexActivity.this, NotificationsActivity.class);
                            } else if (drawerItem.getIdentifier() == 6) {
                                intent = new Intent(IndexActivity.this, SettingsActivity.class);
                            }
                            if (intent != null) {
                                IndexActivity.this.startActivity(intent);
                            }
                        }

                        return false;
                    }
                });


        if(!CarbonForumApplication.isLoggedIn()) { //未登录
            mDrawerBuilder.addDrawerItems(
                    new PrimaryDrawerItem().
                            withName(R.string.title_activity_login).
                            withIcon(GoogleMaterial.Icon.gmd_account).
                            withIdentifier(3).
                            withSelectable(false),
                    new PrimaryDrawerItem().
                            withName(R.string.title_activity_register).
                            withIcon(GoogleMaterial.Icon.gmd_account_add).
                            withIdentifier(4).
                            withSelectable(false)
            );
        }else{ //已登录
            mDrawerBuilder.addDrawerItems(
                    new PrimaryDrawerItem()
                            .withName(R.string.title_activity_notifications)
                            .withIcon(GoogleMaterial.Icon.gmd_notifications)
                            .withIdentifier(5)
                            .withBadgeStyle(new BadgeStyle().withTextColor(Color.WHITE).withColorRes(R.color.md_red_700))
                            .withSelectable(false),
                    new PrimaryDrawerItem()
                            .withName(R.string.title_activity_settings)
                            .withIcon(GoogleMaterial.Icon.gmd_settings)
                            .withIdentifier(6)
                            .withSelectable(false)
            );
        }
        mDrawer = mDrawerBuilder.build();
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
        //TODO:根据消息数量刷新Notification
        int notificationsNumber = Integer.parseInt(CarbonForumApplication.cacheSharedPreferences.getString("notificationsNumber", "0"));
        if(notificationsNumber>0){
            //添加消息通知
            mDrawer.updateBadge(4, new StringHolder(notificationsNumber + ""));
        }

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
        public GetTopicsTask(int targetPage, Boolean enableCache) {
            this.targetPage = targetPage;
            this.enableCache = enableCache;
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            enableScrollListener = false;
            if(enableCache){
                topicList = JSONUtil.jsonObject2List(JSONUtil.jsonString2Object(
                        CarbonForumApplication.cacheSharedPreferences.getString("topicsCache", "{\"Status\":1, \"TopicsArray\":[]}"))
                        , "TopicsArray");
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
            }
            //移除刷新控件
            mSwipeRefreshLayout.setRefreshing(false);
            enableScrollListener = true;
            //Toast.makeText(IndexActivity.this, "AsyncTask End", Toast.LENGTH_SHORT).show();
        }

        @Override
        protected List<Map<String, Object>> doInBackground(Void... params) {
            List<Map<String,Object>> list;
            JSONObject jsonObject = HttpUtil.postRequest(IndexActivity.this, APIAddress.HOME_URL(targetPage), null, false, false);
            //Log.v("JSON", str);
            if(jsonObject != null){
                try {
                    totalPage = jsonObject.getInt("TotalPage");
                }catch(JSONException e){
                    e.printStackTrace();
                }
                if(targetPage == 1){
                    try {
                        SharedPreferences.Editor cacheEditor = CarbonForumApplication.cacheSharedPreferences.edit();
                        cacheEditor.putString("topicsCache", jsonObject.toString(0));
                        cacheEditor.apply();
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                }
            }
            list = JSONUtil.jsonObject2List(jsonObject, "TopicsArray");
            //Log.v("List", list.toString());
            return list;
        }

    }
}