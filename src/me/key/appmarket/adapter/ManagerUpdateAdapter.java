package me.key.appmarket.adapter;

import java.io.File;
import java.util.ArrayList;

import com.market.d9game.R;

import me.key.appmarket.MarketApplication;
import me.key.appmarket.tool.DownloadService;
import me.key.appmarket.tool.ToolHelper;
import me.key.appmarket.utils.AppInfo;
import me.key.appmarket.utils.AppUtils;
import me.key.appmarket.utils.Global;
import me.key.appmarket.widgets.ProgressView;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class ManagerUpdateAdapter extends BaseAdapter {

	private ArrayList<AppInfo> appInfos;
	private LayoutInflater lay;
	private File cache;
	private Context mContext;

	public ManagerUpdateAdapter(ArrayList<AppInfo> appInfos, Context context,
			File cache) {
		super();
		this.appInfos = appInfos;
		this.cache = cache;
		mContext = context;
		lay = LayoutInflater.from(context);
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return appInfos.size();
	}

	@Override
	public Object getItem(int arg0) {
		// TODO Auto-generated method stub
		return appInfos.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		// TODO Auto-generated method stub
		return arg0;
	}

	@Override
	public View getView(final int position, View convertvView, ViewGroup arg2) {
		// TODO Auto-generated method stub
		ViewHolder viewHolder;
		if (convertvView == null) {
			viewHolder = new ViewHolder();
			convertvView = lay.inflate(R.layout.app_list_recomm_item, null);
			viewHolder.icon = (ImageView) convertvView.findViewById(R.id.icon);
			viewHolder.name = (TextView) convertvView
					.findViewById(R.id.app_name);
			viewHolder.size = (TextView) convertvView
					.findViewById(R.id.appsize);
			viewHolder.tvdown = (TextView) convertvView
					.findViewById(R.id.tv_down);
			viewHolder.progress_view = (ProgressView) convertvView
					.findViewById(R.id.progress_view);
			convertvView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertvView.getTag();
		}
		asyncloadImage(viewHolder.icon, appInfos.get(position).getIconUrl());

		viewHolder.name.setText(appInfos.get(position).getAppName());
		if (appInfos.get(position).getAppSize() != null
				&& !appInfos.get(position).getAppSize().equals("null")) {
			viewHolder.size.setText(ToolHelper.Kb2Mb(appInfos.get(position)
					.getAppSize()));
		}

		viewHolder.tvdown.setText("升级");
		Drawable mDrawable = mContext.getResources().getDrawable(
				R.drawable.downloading);
		viewHolder.tvdown.setCompoundDrawablesWithIntrinsicBounds(null,
				mDrawable, null, null);

		viewHolder.progress_view.setVisibility(View.VISIBLE);
		int idx = Integer.parseInt(appInfos.get(position).getIdx());
		boolean isDownLoading = DownloadService.isDownLoading(idx);
		if (isDownLoading) {
			viewHolder.progress_view.setProgress(DownloadService
					.getPrecent(idx));
			viewHolder.tvdown.setText("下载中");
		} else {
			viewHolder.progress_view.setProgress(0);
		}

		viewHolder.tvdown.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				DownloadService.downNewFile(appInfos.get(position),0,0);

				Intent intent = new Intent();
				intent.setAction(MarketApplication.PRECENT);
				mContext.sendBroadcast(intent);
				Toast.makeText(mContext,
						appInfos.get(position).getAppName() + " 开始下载...",
						Toast.LENGTH_SHORT).show();
			}
		});
		return convertvView;
	}

	private class ViewHolder {
		private ImageView icon;
		private TextView name;
		private TextView size;
		private TextView tvdown;
		private ProgressView progress_view;
	}

	private void asyncloadImage(ImageView iv_header, String path) {
		AsyncImageTask task = new AsyncImageTask(iv_header);
		task.execute(path);
	}

	private final class AsyncImageTask extends AsyncTask<String, Integer, Uri> {

		private ImageView iv_header;

		public AsyncImageTask(ImageView iv_header) {
			this.iv_header = iv_header;
		}

		@Override
		protected Uri doInBackground(String... params) {
			try {
				return ToolHelper.getImageURI(params[0], cache);
			} catch (Exception e) {
				return null;
			}
		}

		@Override
		protected void onPostExecute(Uri result) {
			super.onPostExecute(result);
			if (iv_header != null && result != null) {
				iv_header.setImageURI(result);
			}
		}
	}

	/**
	 * @param newsitem
	 */
	public void addNewsItem(AppInfo newsitem) {
		appInfos.add(newsitem);
	}
}
