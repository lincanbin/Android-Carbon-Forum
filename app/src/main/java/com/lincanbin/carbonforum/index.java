package com.lincanbin.carbonforum;

import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.lincanbin.carbonforum.adapter.TopicAdapter;
import com.lincanbin.carbonforum.config.ApiAddress;
import com.lincanbin.carbonforum.util.HttpUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class index extends AppCompatActivity  implements SwipeRefreshLayout.OnRefreshListener {//http://stackoverflow.com/questions/28150100/setsupportactionbar-throws-error/28150167
    private Toolbar mToolbar;
    private DrawerLayout mDrawerLayout;
    private RecyclerView mRecyclerView ;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private TopicAdapter MAdapter;
    private ActionBarDrawerToggle mDrawerToggle;
    private int currentPage = 0;
    private List<Map<String,Object>> topicList = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_index);
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
                            Toast.makeText(index.this, "action_search", Toast.LENGTH_SHORT).show();
                            break;
                        case R.id.action_settings:
                            Toast.makeText(index.this, "action_settings", Toast.LENGTH_SHORT).show();
                            break;
                        case R.id.action_share:
                            Toast.makeText(index.this, "action_share", Toast.LENGTH_SHORT).show();
                            break;
                        default:
                            break;
                    }
                    return true;
                }
            });
            mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer);
            mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
            mDrawerToggle = new ActionBarDrawerToggle(
                    this,
                    mDrawerLayout,
                    mToolbar,
                    R.string.drawer_open,
                    R.string.drawer_close
            ){
                public void onDrawerClosed(View view) {
                    //getActionBar().setTitle("Open");
                    invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
                }

                public void onDrawerOpened(View drawerView) {
                    //getActionBar().setTitle("Close");
                    invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
                }
            };
            mDrawerLayout.setDrawerListener(mDrawerToggle);
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
        //底部要显示的进度条
        final View footerView = LayoutInflater.from(this).inflate(R.layout.progress_bar, null);
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
                        loadTopic(currentPage+1);
                        footerView.setVisibility(View.VISIBLE);
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
                Toast.makeText(index.this, "onItemClick" + topicList.get(position).get("Topic").toString(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public boolean onItemLongClick(int position) {
                Toast.makeText(index.this, "onItemLongClick" + topicList.get(position).get("Topic").toString(), Toast.LENGTH_SHORT).show();
                return true;
            }
        });
        loadTopic(1);
    }
    //加载帖子
    public void loadTopic(int TargetPage) {
            new indexModel(TargetPage).execute(ApiAddress.HOME_URL + TargetPage);
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
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggls
        mDrawerToggle.onConfigurationChanged(newConfig);
    }
    @Override
    public void onBackPressed() {
        if(mDrawerLayout.isDrawerOpen(Gravity.START|Gravity.LEFT)){
            mDrawerLayout.closeDrawers();
            return;
        }
        super.onBackPressed();
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
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        int id = item.getItemId();
        switch (id) {
            case R.id.action_search:
                Toast.makeText(index.this, "action_search", Toast.LENGTH_SHORT).show();
                break;
            case R.id.action_settings:
                Toast.makeText(index.this, "action_settings", Toast.LENGTH_SHORT).show();
                break;
            case R.id.action_share:
                Toast.makeText(index.this, "action_share", Toast.LENGTH_SHORT).show();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }
    public class indexModel extends AsyncTask<String, Void, List<Map<String,Object>>> {
    	public int targetPage;
        private int positionStart;
		public indexModel(int targetPage) {
			this.targetPage = targetPage;
		}
        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();
            mSwipeRefreshLayout.setRefreshing(true);
            Toast.makeText(index.this, "Before AsyncTask", Toast.LENGTH_SHORT).show();
        }

        @Override
        protected void onPostExecute(List<Map<String, Object>> result) {
            // TODO Auto-generated method stub
            super.onPostExecute(result);
            if(targetPage>1){
                positionStart = topicList.size()-1;
                topicList.addAll(result);
                MAdapter.setData(topicList);
                //局部刷新，更好的性能
                MAdapter.notifyItemRangeChanged(positionStart, MAdapter.getItemCount());
            }else{
                topicList = result;
                MAdapter.setData(topicList);
                //全部刷新
                MAdapter.notifyDataSetChanged();
            }
            //移除刷新控件
            mSwipeRefreshLayout.setRefreshing(false);
            //更新当前页数
            currentPage = targetPage;
            Toast.makeText(index.this, "AsyncTask End", Toast.LENGTH_SHORT).show();
        }

        @Override
        protected List<Map<String, Object>> doInBackground(String... params) {
            // TODO Auto-generated method stub
            List<Map<String,Object>> list ;

            String str = HttpUtil.getRequest(params[0]);
            //Log.v("JSON", str);
            list = HttpUtil.getRequest2List(str, "TopicsArray");
            //Log.v("List", list.toString());
            return list;
        }

    }
}