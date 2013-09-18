package me.key.appmarket.adapter;

import java.io.File;
import java.util.List;

import com.market.d9game.R;

import me.key.appmarket.MarketApplication;
import me.key.appmarket.ImageNet.AsyncImageLoader;
import me.key.appmarket.ImageNet.AsyncImageLoader.ImageCallback;
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

public class RankAdapter extends BaseAdapter {
	private List<AppInfo> appInfos;
	private LayoutInflater lay;
	private File cache;
	private Context mContext;
	private AsyncImageLoader asyncImageLoader;

	public RankAdapter(List<AppInfo> appInfos, Context context, File cache) {
		super();
		this.appInfos = appInfos;
		this.cache = cache;
		mContext = context;
		lay = LayoutInflater.from(context);

		asyncImageLoader = new AsyncImageLoader();
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
			convertvView = lay.inflate(R.layout.app_list_item, null);
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

		Drawable drawable = getDrawable(asyncImageLoader, appInfos
				.get(position).getIconUrl(), viewHolder.icon);
		if (drawable != null)
			viewHolder.icon.setImageDrawable(drawable);

		asyncloadImage(viewHolder.icon, appInfos.get(position).getIconUrl());
		viewHolder.name.setText(appInfos.get(position).getAppName());
		viewHolder.size.setText(appInfos.get(position).getAppDownCount()
				+ "+下载        "
				+ ToolHelper.Kb2Mb(appInfos.get(position).getAppSize()));

		viewHolder.progress_view.setProgress(0);
		viewHolder.progress_view.setVisibility(View.VISIBLE);

		boolean isDownLoaded = DownloadService.isDownLoaded(appInfos.get(
				position).getAppName());
		int idx = Integer.parseInt(appInfos.get(position).getIdx());
		boolean isDownLoading = DownloadService.isDownLoading(idx);

		if (appInfos.get(position).isInstalled()) {
			viewHolder.tvdown.setText("打开");

			viewHolder.progress_view.setProgress(100);
			Drawable mDrawable = mContext.getResources().getDrawable(
					R.drawable.action_type_software_update);
			viewHolder.tvdown.setCompoundDrawablesWithIntrinsicBounds(null,
					mDrawable, null, null);
		} else if (isDownLoading) {
			viewHolder.progress_view.setProgress(DownloadService
					.getPrecent(idx));
			viewHolder.tvdown.setText("下载中");
			Drawable mDrawable = mContext.getResources().getDrawable(
					R.drawable.downloading);
			viewHolder.tvdown.setCompoundDrawablesWithIntrinsicBounds(null,
					mDrawable, null, null);
		} else if (isDownLoaded) {
			Drawable mDrawable = mContext.getResources().getDrawable(
					R.drawable.downloaded);
			viewHolder.tvdown.setCompoundDrawablesWithIntrinsicBounds(null,
					mDrawable, null, null);
			viewHolder.tvdown.setText("安装");
			viewHolder.progress_view.setProgress(100);
		} else {
			viewHolder.tvdown.setText("下载");
			Drawable mDrawable = mContext.getResources().getDrawable(
					R.drawable.downloading);
			viewHolder.tvdown.setCompoundDrawablesWithIntrinsicBounds(null,
					mDrawable, null, null);
		}

		viewHolder.tvdown.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (appInfos.get(position).isInstalled()) {
					AppUtils.launchApp(mContext, appInfos.get(position)
							.getAppName());
				} else if (DownloadService.isDownLoading(Integer
						.parseInt(appInfos.get(position).getIdx()))) {

				} else if (DownloadService.isDownLoaded(appInfos.get(position)
						.getAppName())) {
					// 已经下载
					DownloadService.Instanll(appInfos.get(position)
							.getAppName(), mContext);
				} else if (!appInfos.get(position).isInstalled()) {
					Log.e("tag",
							"appurl = " + Global.MAIN_URL
									+ appInfos.get(position).getAppUrl());
					Log.e("tag",
							"appIdx = "
									+ Integer.parseInt(appInfos.get(position)
											.getIdx()));
					Log.e("tag", "appname = "
							+ appInfos.get(position).getAppName());
					DownloadService.downNewFile(appInfos.get(position),0,0);

					Intent intent = new Intent();
					intent.setAction(MarketApplication.PRECENT);
					mContext.sendBroadcast(intent);

					Toast.makeText(mContext,
							appInfos.get(position).getAppName() + " 开始下载...",
							Toast.LENGTH_SHORT).show();
				}
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

	public Drawable getDrawable(AsyncImageLoader asyncImageLoader,
			String imageUrl, final ImageView imageView) {
		Drawable drawable = asyncImageLoader.loadDrawable(imageUrl,
				new ImageCallback() {
					@Override
					public void imageLoaded(Drawable imageDrawable,
							String imageUrl) {
						if (imageDrawable != null)
							imageView.setImageDrawable(imageDrawable);
						else
							imageView.setImageResource(R.drawable.tempicon);
					}
				});
		return drawable;
	}
}
