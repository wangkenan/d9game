package me.key.appmarket.adapter;

import java.util.List;

import com.market.d9game.R;

import me.key.appmarket.utils.AppInfo;
import me.key.appmarket.utils.CategoryInfo;
import me.key.appmarket.utils.LogUtils;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AbsListView.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class MenuCategoryAdapter extends BaseAdapter {
	private Context context;
	private List<CategoryInfo> classInfos;
	private LayoutInflater mInflater;
	private WindowManager wm;
	private int height;
	private int width;
	private ListView lv;
	public MenuCategoryAdapter(Context context, List<CategoryInfo> classInfos,ListView lv) {
		this.classInfos = classInfos;
		this.context = context;
		this.mInflater = LayoutInflater.from(context);
		wm = (WindowManager) context.getSystemService(context.WINDOW_SERVICE);
		this.lv = lv;
		height = lv.getHeight();
		width = lv.getWidth();
		LogUtils.d("testLocal", "width:"+width);
	}
	@Override
	public int getCount() {
		return classInfos.size();
	}

	@Override
	public Object getItem(int position) {
		return null;
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder vh = null;
		
		if(convertView == null) {
			vh = new ViewHolder();
			convertView = mInflater.inflate(R.layout.menu_category_item, null);
			vh.tv = (TextView) convertView.findViewById(R.id.category_menu);
			vh.click_menu = (ImageView) convertView.findViewById(R.id.click_menu);
			convertView.setTag(vh);
			convertView.setLayoutParams(new LayoutParams(width, height/classInfos.size()));
		} else {
			vh = (ViewHolder) convertView.getTag();
		}
	if(position == 0) {
		convertView.setBackgroundColor(context.getResources().getColor(R.color.classiv_cloor));
		vh.click_menu.setVisibility(View.VISIBLE);
		vh.tv.setTextColor(context.getResources().getColor(R.color.myprobar));
		}
		vh.tv.setText(classInfos.get(position).getName());
		return convertView;
	}
	 
	static class ViewHolder {
		private TextView tv;
		private ImageView click_menu;
	}

}
