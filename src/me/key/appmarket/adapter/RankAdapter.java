package me.key.appmarket.adapter;

import java.io.File;
import java.util.List;

import me.key.appmarket.MarketApplication;
import me.key.appmarket.ImageNet.AsyncImageLoader;
import me.key.appmarket.ImageNet.AsyncImageLoader.ImageCallback;
import me.key.appmarket.tool.DownloadService;
import me.key.appmarket.tool.ToolHelper;
import me.key.appmarket.utils.AppInfo;
import me.key.appmarket.utils.AppUtils;
import me.key.appmarket.utils.Global;
import me.key.appmarket.utils.LocalUtils;
import me.key.appmarket.utils.LogUtils;
import me.key.appmarket.widgets.ProgressView;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.market.d9game.R;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

public class RankAdapter extends BaseAdapter {
	private List<AppInfo> appInfos;
	private LayoutInflater lay;
	private File cache;
	private Context mContext;
	private AsyncImageLoader asyncImageLoader;
	// 是否异步加载图片
	public boolean isAsyn;
	// 是否暂停
	private boolean isPause;
	// 是否是下载状态
	private boolean isDownLoading;

	// 设置ImageLoade初始化信息
	private DisplayImageOptions options = new DisplayImageOptions.Builder()
			.showImageForEmptyUri(R.drawable.tempicon)
			.showStubImage(R.drawable.tempicon).resetViewBeforeLoading(false)
			.delayBeforeLoading(100).cacheInMemory(true).cacheOnDisc(true)
			.imageScaleType(ImageScaleType.IN_SAMPLE_INT)
			.bitmapConfig(Bitmap.Config.RGB_565).build();

	public RankAdapter(List<AppInfo> appInfos, Context context, File cache) {
		super();
		this.appInfos = appInfos;
		this.cache = cache;
		mContext = context;
		lay = LayoutInflater.from(context);

		//asyncImageLoader = new AsyncImageLoader();
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
		final ViewHolder viewHolder;
		Drawable mDrawable;
		if (convertvView == null) {
			viewHolder = new ViewHolder();
			convertvView = lay.inflate(R.layout.app_list_item, null);
			viewHolder.icon = (ImageView) convertvView.findViewById(R.id.icon);
			viewHolder.name = (TextView) convertvView
					.findViewById(R.id.app_name2);
			viewHolder.size = (TextView) convertvView
					.findViewById(R.id.appsize);
			viewHolder.tvdown = (TextView) convertvView
					.findViewById(R.id.tv_down);
//			viewHolder.progress_view = (ProgressView) convertvView
//					.findViewById(R.id.progress_view);
			convertvView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertvView.getTag();
		}

		viewHolder.name.setText(appInfos.get(position).getAppName());
		viewHolder.size.setText(ToolHelper.Kb2Mb(appInfos.get(position)
				.getAppSize()));
		// 给view设置唯一tag
		viewHolder.icon.setTag(appInfos.get(position).getIconUrl());
		ImageLoader.getInstance().displayImage(appInfos.get(position).getIconUrl(), viewHolder.icon,options);
//		final Drawable drawable;
//		if (!isAsyn) {
//			drawable = getDrawable(asyncImageLoader, appInfos.get(position)
//					.getIconUrl(), viewHolder.icon);
//		} else {
//			String imageUrl = appInfos.get(position).getIconUrl();
//			HashMap<String, SoftReference<Drawable>> imageCache = asyncImageLoader.imageCache;
//			if (imageCache.containsKey(imageUrl)) {
//				SoftReference<Drawable> softReference = imageCache
//						.get(imageUrl);
//				Drawable icon = softReference.get();
//				viewHolder.icon.setImageDrawable(icon);
//				drawable = icon;
//			} else {
//				drawable = null;
//			}
//		}
//		if (drawable != null) {
//			viewHolder.icon.setImageBitmap(DownloadService
//					.drawable2Bitmap(drawable));
//			// 如果图片为Null,则设置默认图片
//		} else {
//
//			viewHolder.icon.setImageResource(R.drawable.tempicon);
//		}
		// TODO Auto-generated method stub

		// asyncloadImage(viewHolder.icon, appInfos.get(position).getIconUrl());

		viewHolder.progress_view.setProgress(0);
		viewHolder.progress_view.setVisibility(View.VISIBLE);
		File tempFile = new File(LocalUtils.getRoot(mContext),
				"d9dir/" + appInfos.get(position).getAppName() + ".apk");
		SharedPreferences sp = mContext.getSharedPreferences("down",
				mContext.MODE_PRIVATE);
		boolean isDownLoaded = DownloadService.isDownLoaded(appInfos.get(
				position).getAppName());
		int idx = Integer.parseInt(appInfos.get(position).getIdx());
		isDownLoading = DownloadService.isDownLoading(idx);

		if (appInfos.get(position).isIspause()) {
			LogUtils.d("ture", appInfos.get(position).isIspause() + "");
			viewHolder.tvdown.setText("暂停");
			viewHolder.progress_view.setProgress(DownloadService
					.getPrecent(idx));
		} else {
			viewHolder.tvdown.setText("下载中");

		}
		if (appInfos.get(position).isInstalled()) {
			viewHolder.tvdown.setText("打开");

			viewHolder.progress_view.setProgress(100);
			Drawable mDrawableicon = mContext.getResources().getDrawable(
					R.drawable.action_type_software_update);
			viewHolder.tvdown.setCompoundDrawablesWithIntrinsicBounds(null,
					mDrawableicon, null, null);
		} else if (appInfos.get(position).isDown()) {

			viewHolder.progress_view.setProgress(DownloadService
					.getPrecent(idx));
			LogUtils.d("ture", isDownLoading + "isDown");

			viewHolder.tvdown.setText("下载中");
			Drawable mDrawableicon = mContext.getResources().getDrawable(
					R.drawable.downloading);
			viewHolder.tvdown.setCompoundDrawablesWithIntrinsicBounds(null,
					mDrawableicon, null, null);
		} else if (isDownLoaded) {
			Drawable mDrawableicon = mContext.getResources().getDrawable(
					R.drawable.downloaded);
			viewHolder.tvdown.setCompoundDrawablesWithIntrinsicBounds(null,
					mDrawableicon, null, null);
			viewHolder.tvdown.setText("安装");
			viewHolder.progress_view.setProgress(100);
		} else if (!isDownLoading) {
			viewHolder.tvdown.setText("下载");
			mDrawable = mContext.getResources().getDrawable(
					R.drawable.downloading);
			viewHolder.tvdown.setCompoundDrawablesWithIntrinsicBounds(null,
					mDrawable, null, null);
			// 获取将要下载的文件名，如果本地存在该文件，则取出该文件

			// LogUtils.d("sa", tempFile.getAbsolutePath());

			long length = sp.getLong(tempFile.getAbsolutePath(), 0);
			// LogUtils.d("sa", length+"");
			if (length != 0
					&& DownloadService.isExist(appInfos.get(position)
							.getAppName())) {
				LogUtils.d("test", "已经存在");
				viewHolder.tvdown.setText("已下载");

				long count = sp.getLong(tempFile.getAbsolutePath() + "precent",
						0);
				viewHolder.progress_view.setProgress(count);
			} else if (length != 0
					&& !DownloadService.isExist(appInfos.get(position)
							.getAppName())) {
				Editor edit = sp.edit();
				edit.remove(tempFile.getAbsolutePath());
				edit.commit();
			}
		}

		viewHolder.tvdown.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (appInfos.get(position).isInstalled()) {
					AppUtils.launchApp(mContext, appInfos.get(position)
							.getPackageName());
				} else if (DownloadService.isDownLoading(Integer
						.parseInt(appInfos.get(position).getIdx()))) {
					LogUtils.d("test", "暂停");
					File tempFile = DownloadService.CreatFileName(appInfos.get(
							position).getAppName());
					Intent intent = new Intent();
					intent.setAction(tempFile.getAbsolutePath());
					mContext.sendBroadcast(intent);
					if (!appInfos.get(position).isIspause()) {
						viewHolder.tvdown.setText("暂停");
						appInfos.get(position).setDown(false);
					} else {
						viewHolder.tvdown.setText("下载中");
						appInfos.get(position).setDown(true);
					}
					LogUtils.d("down", appInfos.get(position).isDown() + "");
					LogUtils.d("test", appInfos.get(position).isIspause() + "1");
					appInfos.get(position).setIspause(
							!appInfos.get(position).isIspause());
					LogUtils.d("test", appInfos.get(position).isIspause() + "2");
				} else if (DownloadService.isDownLoaded(appInfos.get(position)
						.getAppName())) {
					// 已经下载
					DownloadService.Instanll(appInfos.get(position)
							.getAppName(), mContext);
				} else if (!appInfos.get(position).isInstalled()) {
					/*
					 * Log.e("tag", "appname = " +
					 * appInfos.get(position).getAppName());
					 */
					SharedPreferences sp = mContext.getSharedPreferences(
							"down", mContext.MODE_PRIVATE);
					File tempFile = new File(LocalUtils.getRoot(mContext), "d9dir/"
							+ appInfos.get(position).getAppName() + ".apk");

					long length = sp.getLong(tempFile.getAbsolutePath(), 0);
					/*
					 * DownloadService.downNewFile(appInfos.get(position)
					 * .getAppUrl(), Integer.parseInt(appInfos.get(
					 * position).getIdx()), appInfos.get(position)
					 * .getAppName(),length,0);
					 */
					DownloadService.downNewFile(appInfos.get(position), length,
							0, null);
					appInfos.get(position).setDown(true);
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
						// 如果当前view的标记和draw的标记一致，则将图片设置
						if (imageDrawable != null
								&& imageView.getTag().equals(imageUrl))
							// imageView.setImageDrawable(imageDrawable);
							imageView.setImageBitmap(DownloadService
									.drawable2Bitmap(imageDrawable));
						/*
						 * else imageView.setImageResource(R.drawable.tempicon);
						 */
					}
				});
		return drawable;
	}
}
