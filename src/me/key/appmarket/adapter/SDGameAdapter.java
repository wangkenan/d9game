package me.key.appmarket.adapter;

import java.io.File;
import java.util.List;

import me.key.appmarket.tool.ToolHelper;
import me.key.appmarket.utils.AppInfo;
import me.key.appmarket.utils.LocalUtils;
import me.key.appmarket.utils.LogUtils;
import me.key.appmarket.widgets.ProgressView;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.market.d9game.R;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
/**
 * 可安装游戏（Sd卡的）
 * @author Administrator
 *
 */
public class SDGameAdapter extends BaseAdapter {
	private LayoutInflater mInflater;
	private List<AppInfo> mData;
	private Context context;
	//sd卡目录
	private String ROOT;
	private DisplayImageOptions options = new DisplayImageOptions.Builder()
	.showImageForEmptyUri(R.drawable.tempicon)
	.showStubImage(R.drawable.tempicon).resetViewBeforeLoading(false)
	.delayBeforeLoading(100).cacheInMemory(true).cacheOnDisc(true)
	.imageScaleType(ImageScaleType.IN_SAMPLE_INT)
	.bitmapConfig(Bitmap.Config.RGB_565).build();
	
	public SDGameAdapter(Context context,List<AppInfo> mData){
		this.context = context;
		this.mData = mData;
		this.mInflater = LayoutInflater.from(context);
		ROOT = LocalUtils.getRoot(context);
	}
	@Override
	public int getCount() {
		return mData.size();
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
		ViewHolder holder;
		final AppInfo sdappInfo;
		if(convertView == null) {
			holder = new ViewHolder();
			convertView = mInflater.inflate(R.layout.list_item, null);
			holder.info = (TextView) convertView.findViewById(R.id.info);
			holder.icon = (ImageView) convertView.findViewById(R.id.icon);
			holder.btn = (TextView) convertView.findViewById(R.id.install);
			holder.progress_view = (ProgressView) convertView
					.findViewById(R.id.progress_view_local);

			holder.appsize = (TextView) convertView
					.findViewById(R.id.appsize);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		sdappInfo = mData.get(position);
		/*
		 * Drawable mDrawable2 = cnt.getResources().getDrawable(
		 * R.drawable.downloaded);
		 * v2.btn.setCompoundDrawablesWithIntrinsicBounds(null,
		 * mDrawable2, null, null);
		 */
		holder.btn.setText("安装");
		holder.progress_view.setProgress(0);
		String mb = ToolHelper.Kb2Mb(sdappInfo.getAppSize());
		holder.appsize.setText(mb);
		holder.progress_view.setProgress(0);
		// v2.progress_view.setVisibility(View.VISIBLE);
		holder.info.setText(sdappInfo.getAppName());
		holder.icon.setVisibility(View.VISIBLE);
		Drawable appIcon = sdappInfo.getAppIcon();
		if(appIcon!= null) {
			holder.icon.setImageDrawable(appIcon);
		} else {
			ImageLoader.getInstance().displayImage(sdappInfo.getIconUrl(), holder.icon, options);
		}
		
		holder.btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				LogUtils.d("MyAdapter", "我安装了" + sdappInfo.getAppName());
				installApp(sdappInfo);
			}
		});
		return convertView;
	}
	static class ViewHolder {
		public TextView appsize;
		public TextView btn;
		public ImageView icon;
		public TextView info;
		private ProgressView progress_view;
	}
	/**
	 * 安装某个应用
	 * 
	 * @param mAppInfo
	 */
	private void installApp(AppInfo mAppInfo) {
		String str = mAppInfo.getApkName();
		String fileName = ROOT + str;
		Intent intent = new Intent(Intent.ACTION_VIEW);
		Log.v("tag", "nano ROOT" + fileName);
		intent.setDataAndType(Uri.fromFile(new File(fileName)),
				"application/vnd.android.package-archive");
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		this.context.startActivity(intent);
	}
}
