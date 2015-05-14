package com.lincanbin.carbonforum.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.lincanbin.carbonforum.R;
import com.lincanbin.carbonforum.config.ApiAddress;
import com.lincanbin.carbonforum.tools.DownImage;

import java.util.List;
import java.util.Map;

public class IndexAdapter extends BaseAdapter {
	private Context context;
	private LayoutInflater layoutInflater;
	private List<Map<String,Object>> list;
	
	public IndexAdapter(Context context) {
		
		this.context = context;
		layoutInflater = layoutInflater.from(context);
	}
	
	public List getData(){
		return list;
	}

	public void setData(List<Map<String,Object>> data){
		this.list = data;
	}
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return list.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return list.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		View view = null;
		final ViewHolder viewHolder;
		
		if (convertView == null ) {
			convertView = layoutInflater.inflate(R.layout.topic_list_item, null);
			viewHolder = new ViewHolder();
			viewHolder.ID = (TextView)convertView.findViewById(R.id.id);
			viewHolder.Title = (TextView)convertView.findViewById(R.id.title);
			viewHolder.Description = (TextView)convertView.findViewById(R.id.description);
			viewHolder.Avatar = (ImageView)convertView.findViewById(R.id.avatar);
			
			convertView.setTag(viewHolder);
			
		}else{
			viewHolder = (ViewHolder)convertView.getTag();
		}
		
		viewHolder.ID.setText(list.get(position).get("ID").toString());
		viewHolder.Title.setText(list.get(position).get("Topic").toString());
		viewHolder.Description.setText(list.get(position).get("UserName").toString()+" 最后来自 "+list.get(position).get("LastName").toString());

		//Log.v("UserID", ApiAddress.MIDDLE_AVATAR_URL + (list.get(position).get("UserID").toString()) + ".png");
		
		//接口回调的方法，完成图片的异步读取
		DownImage downImage = new DownImage(ApiAddress.MIDDLE_AVATAR_URL + (list.get(position).get("UserID").toString()) + ".png");
		downImage.loadImage(new DownImage.ImageCallBack() {

			@Override
			public void getDrawable(Drawable drawable) {
				// TODO Auto-generated method stub
				viewHolder.Avatar.setImageDrawable(drawable);
			}
		});
		
		return convertView;
	}
	
	public class ViewHolder{
	    ImageView Avatar;
	    TextView ID;
	    TextView Title;
	    TextView Description;
	}
}