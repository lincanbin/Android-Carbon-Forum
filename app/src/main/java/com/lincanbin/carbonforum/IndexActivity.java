package com.lincanbin.carbonforum;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.lincanbin.carbonforum.adapter.TopicAdapter;
import com.lincanbin.carbonforum.config.APIAddress;
import com.lincanbin.carbonforum.util.HttpUtil;
import com.lincanbin.carbonforum.util.JSONUtil;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.holder.StringHolder;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileSettingDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;
import com.mikepenz.materialdrawer.util.RecyclerViewCacheUtil;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

//http://stackoverflow.com/questions/28150100/setsupportactionbar-throws-error/28150167
public class IndexActivity extends AppCompatActivity  implements SwipeRefreshLayout.OnRefreshListener {
    private Toolbar mToolbar;
    //save our header or result
    private AccountHeader headerResult = null;
    private Drawer mDrawer = null;
    private RecyclerView mRecyclerView ;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private TopicAdapter MAdapter;
    private SharedPreferences mSharedPreferences;
    //private ActionBarDrawerToggle mDrawerToggle;
    private int currentPage = 0;
    private List<Map<String,Object>> topicList = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_index);
        //注册一个广播用来登录时刷新Drawer
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("action.refreshDrawer");
        registerReceiver(mRefreshDrawerBroadcastReceiver, intentFilter);
        // 设置ToolBar
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        if (mToolbar != null) {
            // Title
            mToolbar.setTitle(R.string.app_name);
            //mToolbar.bringToFront();
            // App Logo
            //toolbar.setLogo(R.drawable.ic_launcher);
            // Sub Title
            // toolbar.setSubtitle("Sub title");
            setSupportActionBar(mToolbar);//把Toolbar当做ActionBar给设置了
            getSupportActionBar().setHomeButtonEnabled(true);
            /* 菜单的监听可以在toolbar里设置，也可以像ActionBar那样，通过Activity的onOptionsItemSelected回调方法来处理 */

            mToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.action_search:
                            Toast.makeText(IndexActivity.this, "action_search", Toast.LENGTH_SHORT).show();
                            break;
                        case R.id.action_settings:
                            Toast.makeText(IndexActivity.this, "action_settings", Toast.LENGTH_SHORT).show();
                            break;
                        case R.id.action_share:
                            Toast.makeText(IndexActivity.this, "action_share", Toast.LENGTH_SHORT).show();
                            break;
                        default:
                            break;
                    }
                    return true;
                }
            });
            mSharedPreferences = (SharedPreferences) this.getSharedPreferences("UserInfo", Activity.MODE_PRIVATE);
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
                    if (lastVisibleItem == (totalItemCount - 1)) {
                        //加载更多功能的代码
                        loadTopic(currentPage + 1);
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
        MAdapter = new TopicAdapter(this);
        MAdapter.setData(topicList);
        //设置Adapter
        mRecyclerView.setAdapter(MAdapter);
        //添加事件监听器
        MAdapter.setOnRecyclerViewListener(new TopicAdapter.OnRecyclerViewListener() {
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
        //Activity渲染完毕时加载帖子
        loadTopic(1);
    }
    //加载帖子
    public void loadTopic(int TargetPage) {
            new GetTopicsTask(TargetPage).execute();
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
                        .withIdentifier(100);
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
                final IProfile profile = new ProfileDrawerItem().withName(mSharedPreferences.getString("UserName", "lincanbin"))
                        //.withEmail("i@lincanbin.com")
                        .withIcon(APIAddress.MIDDLE_AVATAR_URL(mSharedPreferences.getString("UserID", "0"), "middle"))
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
                                        .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                                            @Override
                                            public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                                                if (drawerItem != null) {
                                                    IndexActivity.this.startActivity(new Intent(IndexActivity.this, LoginActivity.class));
                                                }
                                                return false;
                                            }
                                        }),
                                new ProfileSettingDrawerItem()
                                        .withName("Exit")
                                        .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                                            @Override
                                            public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                                                if (drawerItem != null) {
                                                    mSharedPreferences.edit().clear().apply();
                                                    refreshDrawer(null);
                                                }
                                                return false;
                                            }
                                        })
                        )
                        .withSavedInstance(savedInstanceState)
                        .build();
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
                                withName(R.string.login).
                                withDescription(R.string.login).
                                withIcon(R.drawable.ic_perm_identity_grey600_24dp).
                                withIdentifier(1).
                                withSelectable(false),
                        new PrimaryDrawerItem().
                                withName(R.string.refresh).
                                withDescription(R.string.refresh).
                                withIcon(R.drawable.ic_perm_identity_grey600_24dp).
                                withIdentifier(1).
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
                            if (drawerItem.getIdentifier() == 1) {
                                intent = new Intent(IndexActivity.this, LoginActivity.class);
                            } else if (drawerItem.getIdentifier() == 2) {
                                loadTopic(1);
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
            // set the selection to the item with the identifier 11
            mDrawer.setSelection(21, false);

            //set the active profile
            //headerResult.setActiveProfile(profile);
        }

        mDrawer.updateBadge(4, new StringHolder(10 + ""));
    }
    //下拉刷新事件
    @Override
    public void onRefresh() {
        loadTopic(1);
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
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_index, menu);
        /*
        MenuItem shareItem = menu.findItem(R.id.action_share);
        MenuItemCompat.setShowAsAction(shareItem,
                MenuItemCompat.SHOW_AS_ACTION_ALWAYS);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        MenuItemCompat.setShowAsAction(searchItem,
            MenuItemCompat.SHOW_AS_ACTION_WITH_TEXT);
        */
        return true;
    }
    @Override
     public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id) {
            case R.id.action_search:
                Toast.makeText(IndexActivity.this, "action_search", Toast.LENGTH_SHORT).show();
                break;
            case R.id.action_settings:
                Toast.makeText(IndexActivity.this, "action_settings", Toast.LENGTH_SHORT).show();
                break;
            case R.id.action_share:
                Toast.makeText(IndexActivity.this, "action_share", Toast.LENGTH_SHORT).show();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }
    public class GetTopicsTask extends AsyncTask<Void, Void, List<Map<String,Object>>> {
    	public int targetPage;
        private int positionStart;
		public GetTopicsTask(int targetPage) {
			this.targetPage = targetPage;
		}
        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();
            mSwipeRefreshLayout.setRefreshing(true);
            //Toast.makeText(IndexActivity.this, "Before AsyncTask", Toast.LENGTH_SHORT).show();
        }

        @Override
        protected void onPostExecute(List<Map<String, Object>> result) {
            // TODO Auto-generated method stub
            super.onPostExecute(result);
            if(result!=null && !result.isEmpty()) {
                if (targetPage > 1) {
                    positionStart = topicList.size() - 1;
                    topicList.addAll(result);
                    MAdapter.setData(topicList);
                    //局部刷新，更好的性能
                    MAdapter.notifyItemRangeChanged(positionStart, MAdapter.getItemCount());
                } else {
                    topicList = result;
                    MAdapter.setData(topicList);
                    //全部刷新
                    MAdapter.notifyDataSetChanged();
                }
                //更新当前页数
                currentPage = targetPage;
            }else{
                Toast.makeText(IndexActivity.this, R.string.network_error, Toast.LENGTH_SHORT).show();
            }
            //移除刷新控件
            mSwipeRefreshLayout.setRefreshing(false);
            //Toast.makeText(IndexActivity.this, "AsyncTask End", Toast.LENGTH_SHORT).show();
        }

        @Override
        protected List<Map<String, Object>> doInBackground(Void... params) {
            // TODO Auto-generated method stub
            List<Map<String,Object>> list;
            JSONObject jsonObject = HttpUtil.getRequest(IndexActivity.this, APIAddress.HOME_URL + targetPage, false, false);
            //Log.v("JSON", str);
            list = JSONUtil.jsonDecode(jsonObject, "TopicsArray");
            //Log.v("List", list.toString());
            return list;
        }

    }
}