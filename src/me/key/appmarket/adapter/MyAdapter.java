package me.key.appmarket.adapter;

import java.io.File;
import java.util.List;

import com.market.d9game.R;

import me.key.appmarket.tool.ToolHelper;
import me.key.appmarket.utils.AppInfo;
import me.key.appmarket.utils.AppUtils;
import me.key.appmarket.widgets.ProgressView;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * ListVeiw的适配器类
 * 
 * @author yutinglong
 */
public class MyAdapter extends BaseAdapter {
	private LayoutInflater mInflater;
	private List<AppInfo> mData;
	private Context cnt;
	private String ROOT;

	public MyAdapter(Context context, List<AppInfo> mData) {
		this.mInflater = LayoutInflater.from(context);
		this.cnt = context;
		this.mData = mData;
		cnt = context;
	}

	@Override
	public int getCount() {
		return mData.size();
	}

	@Override
	public Object getItem(int arg0) {
		return null;
	}

	@Override
	public long getItemId(int arg0) {
		return 0;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {

		ViewHolder holder = null;
		if (convertView == null) {

			holder = new ViewHolder();

			convertView = mInflater.inflate(R.layout.list_item, null);
			holder.info = (TextView) convertView.findViewById(R.id.info);
			holder.icon = (ImageView) convertView.findViewById(R.id.icon);
			holder.btn = (TextView) convertView.findViewById(R.id.install);
			holder.progress_view = (ProgressView) convertView
					.findViewById(R.id.progress_view_local);

			holder.appsize = (TextView) convertView.findViewById(R.id.appsize);
			convertView.setTag(holder);

		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		holder.progress_view.setProgress(0);
		holder.progress_view.setVisibility(View.VISIBLE);
		holder.info.setText(mData.get(position).getAppName());
		String mb = ToolHelper.Kb2Mb(mData.get(position).getAppSize());
		holder.appsize.setText(mb);
		holder.icon.setVisibility(View.VISIBLE);
		ROOT = mData.get(position).getRoot();
		holder.icon.setImageDrawable(mData.get(position).getAppIcon());
		if (mData.get(position).isInstalled()) {
			holder.btn.setText("打开");
			holder.progress_view.setProgress(100);
			Drawable mDrawable = this.cnt.getResources().getDrawable(
					R.drawable.action_type_software_update);
			holder.btn.setCompoundDrawablesWithIntrinsicBounds(null, mDrawable,
					null, null);
		} else {
			Drawable mDrawable = cnt.getResources().getDrawable(
					R.drawable.downloaded);
			holder.btn.setCompoundDrawablesWithIntrinsicBounds(null, mDrawable,
					null, null);
			holder.btn.setText("安装");
			holder.progress_view.setProgress(0);
		}
		holder.btn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				AppInfo mAppInfo = mData.get(position);
				if (mAppInfo.isInstalled()) {
					AppUtils.launchApp(cnt, mAppInfo.getAppName());
				} else {
					installApp(mAppInfo);
				}
			}

		});
		return convertView;
	}

	public class ViewHolder {
		public TextView appsize;
		public TextView tvdown;
		public TextView appSize;
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
		String str = "/" + mAppInfo.getAppName();
		String fileName = ROOT + str;
		Intent intent = new Intent(Intent.ACTION_VIEW);
		Log.v("tag", "nano ROOT" + fileName);
		intent.setDataAndType(Uri.fromFile(new File(fileName)),
				"application/vnd.android.package-archive");
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		this.cnt.startActivity(intent);
	}

}
