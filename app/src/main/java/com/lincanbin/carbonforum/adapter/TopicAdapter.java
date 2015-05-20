package com.lincanbin.carbonforum.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.lincanbin.carbonforum.R;
import com.lincanbin.carbonforum.config.ApiAddress;
import com.lincanbin.carbonforum.tools.DownImage;
import com.lincanbin.carbonforum.util.TimeUtil;

import java.util.List;
import java.util.Map;

/**
 * Created by 灿斌 on 5/14/2015.
 */
public class TopicAdapter extends RecyclerView.Adapter{
    private Context context;
    private LayoutInflater layoutInflater;
    public interface OnRecyclerViewListener {
        void onItemClick(int position);
        boolean onItemLongClick(int position);
    }

    private OnRecyclerViewListener onRecyclerViewListener;

    public void setOnRecyclerViewListener(OnRecyclerViewListener onRecyclerViewListener) {
        this.onRecyclerViewListener = onRecyclerViewListener;
    }

    private static final String TAG = TopicAdapter.class.getSimpleName();
    private List<Map<String,Object>> list;
    public TopicAdapter(Context context){
        this.context = context;
        layoutInflater = LayoutInflater.from(context);
    }
    public void setData(List<Map<String,Object>> list) {
        this.list = list;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {

        View view = layoutInflater.inflate(R.layout.item_topic_list, null);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        view.setLayoutParams(lp);
        return new topicViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int i) {
        final topicViewHolder holder = (topicViewHolder) viewHolder;
        holder.position = i;
        Map<String,Object> topic = list.get(i);
        holder.ID.setText(topic.get("ID").toString());
        holder.Title.setText(topic.get("Topic").toString());
        holder.Description.setText(topic.get("UserName").toString() + " · " + topic.get("LastName").toString());
        holder.Time.setText(TimeUtil.formatTime(Long.parseLong(topic.get("LastTime").toString())));
        //接口回调的方法，完成头像的异步读取与显示
        DownImage downImage = new DownImage(ApiAddress.MIDDLE_AVATAR_URL + (topic.get("UserID").toString()) + ".png");
        downImage.loadImage(new DownImage.ImageCallBack() {
            @Override
            public void getDrawable(Drawable drawable) {
                // TODO Auto-generated method stub
                holder.Avatar.setImageDrawable(drawable);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class topicViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener
    {
        public View rootView;
        ImageView Avatar;
        TextView ID;
        TextView Title;
        TextView Description;
        TextView Time;
        public int position;

        public topicViewHolder(View itemView) {
            super(itemView);

            ID = (TextView) itemView.findViewById(R.id.id);
            Title = (TextView) itemView.findViewById(R.id.title);
            Description = (TextView) itemView.findViewById(R.id.description);
            Time = (TextView) itemView.findViewById(R.id.time);
            Avatar = (ImageView)itemView.findViewById(R.id.avatar);

            rootView = itemView.findViewById(R.id.topic_item);
            rootView.setOnClickListener(this);
            rootView.setOnLongClickListener(this);
        }
        @Override
        //点击事件
        public void onClick(View v) {
            if (null != onRecyclerViewListener) {
                onRecyclerViewListener.onItemClick(position);
            }
        }

        @Override
        //长按事件
        public boolean onLongClick(View v) {
            if(null != onRecyclerViewListener){
                return onRecyclerViewListener.onItemLongClick(position);
            }
            return false;
        }
    }
}
