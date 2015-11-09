package com.lincanbin.carbonforum.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.lincanbin.carbonforum.R;
import com.lincanbin.carbonforum.ReplyActivity;
import com.lincanbin.carbonforum.TopicActivity;
import com.lincanbin.carbonforum.application.CarbonForumApplication;
import com.lincanbin.carbonforum.config.APIAddress;
import com.lincanbin.carbonforum.util.TimeUtil;
import com.lincanbin.carbonforum.view.CarbonWebView;

import java.util.List;
import java.util.Map;

/**
 * Created by 灿斌 on 10/13/2015.
 */
public class PostAdapter extends RecyclerView.Adapter{
    private Context context;
    private Boolean isNotification;
    private LayoutInflater layoutInflater;
    public interface OnRecyclerViewListener {
        void onItemClick(int position);
        boolean onItemLongClick(int position);
    }

    private OnRecyclerViewListener onRecyclerViewListener;

    public void setOnRecyclerViewListener(OnRecyclerViewListener onRecyclerViewListener) {
        this.onRecyclerViewListener = onRecyclerViewListener;
    }

    private static final String TAG = PostAdapter.class.getSimpleName();
    private List<Map<String,Object>> list;
    public PostAdapter(Context context, Boolean isNotification){
        this.context = context;
        this.isNotification = isNotification;
        layoutInflater = LayoutInflater.from(context);
    }
    public void setData(List<Map<String,Object>> list) {
        this.list = list;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {

        View view = layoutInflater.inflate(R.layout.item_post_list, null);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        view.setLayoutParams(lp);
        return new postViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int i) {
        final postViewHolder holder = (postViewHolder) viewHolder;
        holder.position = i;
        final Map<String,Object> post = list.get(i);
        holder.UserName.setText(post.get("UserName").toString());
        holder.Time.setText(TimeUtil.formatTime(context, Long.parseLong(post.get("PostTime").toString())));
        if(!isNotification && !post.get("PostFloor").toString().equals("0"))
            holder.PostFloor.setText("#" + post.get("PostFloor").toString());
        String contentHTML = "<style>" +
                "a, a:link, a:visited, a:active {" +
                "   color: #555555;" +
                "   text-decoration: none;" +
                "   word-wrap: break-word;" +
                "}" +
                "a:hover {" +
                "   color: #7aa1b0;" +
                "}" +
                "p, h3{" +
                "   color:#616161;" +
                "}" +
                "img, video{" +
                "   display: inline;" +
                "   height: auto;" +
                "   max-width: 100%;" +
                "}" +
                "</style>";
        if(isNotification){
            contentHTML += "<h3>" + post.get("Subject").toString() + "</h3>";
        }
        //String uploadDomain = APIAddress.WEBSITE_PATH.length() > 0 ? APIAddress.DOMAIN_NAME.replace(APIAddress.WEBSITE_PATH, "") : APIAddress.DOMAIN_NAME;
        //contentHTML += post.get("Content").toString().replace("=\"/", "=\"" + uploadDomain + "/");
        contentHTML += post.get("Content").toString();
        //Log.v("Post"+ post.get("ID").toString(), contentHTML);
        holder.Content.loadDataWithBaseURL(APIAddress.MOBILE_DOMAIN_NAME, contentHTML, "text/html", "utf-8", null);
        Glide.with(context).load(APIAddress.MIDDLE_AVATAR_URL(post.get("UserID").toString(), "middle")).into(holder.Avatar);
        holder.ReplyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, ReplyActivity.class);
                intent.putExtra("TopicID", post.get("TopicID").toString());
                intent.putExtra("PostID", post.get("ID").toString());
                intent.putExtra("PostFloor", post.get("PostFloor").toString());
                intent.putExtra("UserName", post.get("UserName").toString());
                intent.putExtra("DefaultContent", "");
                context.startActivity(intent);
            }
        });
        if(!CarbonForumApplication.isLoggedIn()){
            holder.ReplyButton.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class postViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener
    {
        public View cardView;
        View rootView;
        ImageView Avatar;
        TextView Time;
        TextView UserName;
        TextView PostFloor;
        ImageView ReplyButton;
        CarbonWebView Content;
        public int position;

        public postViewHolder(View itemView) {
            super(itemView);

            UserName = (TextView) itemView.findViewById(R.id.username);
            PostFloor = (TextView) itemView.findViewById(R.id.floor);
            ReplyButton = (ImageView)itemView.findViewById(R.id.reply_button);
            Content = (CarbonWebView) itemView.findViewById(R.id.content);
            if(Build.VERSION.SDK_INT <= 19) {
                // http://stackoverflow.com/questions/15133132/android-webview-doesnt-display-web-page-in-some-cases
                Content.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
            } else {
                // Content.setLayerType(View.LAYER_TYPE_HARDWARE, null);
                Content.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
            }
            // http://stackoverflow.com/questions/5003156/android-webview-style-background-colortransparent-ignored-on-android-2-2
            Content.setBackgroundColor(Color.TRANSPARENT);
            Content.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);//优先使用缓存
            // http://stackoverflow.com/questions/3099344/can-androids-webview-automatically-resize-huge-images/12327010#12327010
            Content.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);//自动缩放图片
            // Use WideViewport and Zoom out if there is no viewport defined
            //Content.getSettings().setUseWideViewPort(true);
            /*
            // Enable remote debugging via chrome://inspect
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                WebView.setWebContentsDebuggingEnabled(true);
            }
            */
            Time = (TextView) itemView.findViewById(R.id.time);
            Avatar = (ImageView)itemView.findViewById(R.id.avatar);
            cardView = itemView.findViewById(R.id.post_card_item);
            rootView = itemView.findViewById(R.id.post_item);
            rootView.setOnClickListener(this);
            rootView.setOnLongClickListener(this);
        }


        @Override
        //点击事件
        public void onClick(View v) {
            //Toast.makeText(context, "onItemClick", Toast.LENGTH_SHORT).show();
            if(isNotification) {
                Intent intent = new Intent(context, TopicActivity.class);
                intent.putExtra("Topic", list.get(position).get("Subject").toString());
                intent.putExtra("TopicID", list.get(position).get("TopicID").toString());
                intent.putExtra("TargetPage", "1");
                context.startActivity(intent);
                //if (null != onRecyclerViewListener) {
                //    onRecyclerViewListener.onItemClick(position);
                //}
            }
        }

        @Override
        //长按事件
        public boolean onLongClick(View v) {
            //ReplyButton.callOnClick();
            if(null != onRecyclerViewListener){
                return onRecyclerViewListener.onItemLongClick(position);
            }
            return false;
        }
    }
}
