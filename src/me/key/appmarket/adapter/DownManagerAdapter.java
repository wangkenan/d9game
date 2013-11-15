package me.key.appmarket.adapter;

import java.util.ArrayList;

import me.key.appmarket.utils.AppInfo;
import me.key.appmarket.widgets.ProgressView;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.market.d9game.R;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
/**
 * 下载管理天重启
 * @author Administrator
 *
 */
public class DownManagerAdapter extends BaseAdapter {
	private LayoutInflater mInflater;
	private ArrayList<AppInfo> applist;
	private Context context;
	private DisplayImageOptions options = new DisplayImageOptions.Builder()  
    .showImageForEmptyUri(R.drawable.tempicon).showStubImage(R.drawable.tempicon)  
    .resetViewBeforeLoading(false) 
    .delayBeforeLoading(100)  
    .cacheInMemory(true)           
    .cacheOnDisc(true)              
    .imageScaleType(ImageScaleType.IN_SAMPLE_INT)
    .bitmapConfig(Bitmap.Config.RGB_565)               
    .build(); 
	public DownManagerAdapter(ArrayList<AppInfo> applist,Context context){
		this.applist = applist;
		mInflater = LayoutInflater.from(context);
	}
	@Override
	public int getCount() {
		return applist.size();
	}

	@Override
	public Object getItem(int position) {
		return applist.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder vh = null;
		AppInfo ai = applist.get(position);
		if(convertView == null) {
			convertView = mInflater.inflate(R.layout.item_downmanager, null);
			vh = new ViewHolder();
			vh.icon_dm = (ImageView) convertView.findViewById(R.id.icon_dm);
			vh.install_dm = (TextView) convertView.findViewById(R.id.install_dm);
			vh.progress_view_local_dm = (ProgressView) convertView.findViewById(R.id.progress_view_local_dm);
			vh.progressbar_updown = (ProgressBar) convertView.findViewById(R.id.progressbar_updown);
			convertView.setTag(vh);
		} else {
			vh = (ViewHolder) convertView.getTag();
		}
		ImageLoader.getInstance().displayImage(ai.getIconUrl(),vh.icon_dm , options);
		return convertView;
	}
	
	static class ViewHolder {
		ImageView icon_dm;
		ProgressBar progressbar_updown;
		TextView install_dm;
		ProgressView progress_view_local_dm;
	}

}
