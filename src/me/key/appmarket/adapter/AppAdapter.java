package me.key.appmarket.adapter;

import java.io.File;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.market.d9game.R;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

import me.key.appmarket.MarketApplication;
import me.key.appmarket.MyListView;
import me.key.appmarket.ImageNet.AsyncImageLoader;
import me.key.appmarket.ImageNet.AsyncImageLoader.ImageCallback;
import me.key.appmarket.tool.DownloadService;
import me.key.appmarket.tool.ToolHelper;
import me.key.appmarket.utils.AppInfo;
import me.key.appmarket.utils.AppUtils;
import me.key.appmarket.utils.Global;
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
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class AppAdapter extends BaseAdapter {

	private LinkedList<AppInfo> appInfos;
	private LayoutInflater lay;
	private File cache;
	private Context mContext;
	AsyncImageLoader asyncImageLoader;
	private ListView mylistView;
	// 是否暂停
	private boolean isPause;
	// 是否是下载状态
	private boolean isDownLoading;
	// 是否异步加载图片
	public boolean isAsyn;
	private Map<String, Drawable> drawMap = new HashMap<String, Drawable>();
	
	private DisplayImageOptions options = new DisplayImageOptions.Builder()  
    .showImageForEmptyUri(R.drawable.tempicon).showStubImage(R.drawable.tempicon)  
    .resetViewBeforeLoading(false)  // default  
    .delayBeforeLoading(1000)  
    .cacheInMemory(true)           // default 不缓存至内存  
    .cacheOnDisc(true)             // default 不缓存至手机SDCard  
    .imageScaleType(ImageScaleType.IN_SAMPLE_INT)// default  
    .bitmapConfig(Bitmap.Config.RGB_565)              // default  
    .build(); 


	public AppAdapter(LinkedList<AppInfo> appInfos, Context context,
			File cache, ListView mylistView) {
		super();
		this.appInfos = appInfos;
		this.cache = cache;
		this.mylistView = mylistView;
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
		final ViewHolder viewHolder;
		Drawable mDrawable;
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

		viewHolder.name.setText(appInfos.get(position).getAppName());
		viewHolder.size.setText(ToolHelper.Kb2Mb(appInfos.get(position)
				.getAppSize()));
		ImageLoader.getInstance().displayImage(appInfos.get(position)
				.getIconUrl(),  viewHolder.icon,options);
		// 给view设置唯一tag
/*		viewHolder.icon.setTag(appInfos.get(position).getIconUrl());
		final Drawable drawable;
		if (!isAsyn) {
			drawable = getDrawable(asyncImageLoader, appInfos.get(position)
					.getIconUrl(), viewHolder.icon);
			if(viewHolder.icon.getTag().equals(appInfos.get(position).getIconUrl())){
		
			ImageLoader.getInstance().displayImage(appInfos.get(position)
					.getIconUrl(),  viewHolder.icon,options);
			} else {
				viewHolder.icon.setImageResource(R.drawable.tempicon);
			}
		} else {
			String imageUrl = appInfos.get(position).getIconUrl();
			HashMap<String, SoftReference<Drawable>> imageCache = asyncImageLoader.imageCache;
			if (imageCache.containsKey(imageUrl)) {
				SoftReference<Drawable> softReference = imageCache
						.get(imageUrl);
				Drawable icon = softReference.get();
				viewHolder.icon.setImageDrawable(icon);
				drawable = icon; 
			} else {
				drawable = null;
			}
			drawable = null;
			ImageLoader.getInstance().displayImage(appInfos.get(position)
					.getIconUrl(),  viewHolder.icon,options);
		}
		drawMap.clear();*/
		/*if (drawable != null) {
			viewHolder.icon.setImageBitmap(DownloadService
					.drawable2Bitmap(drawable));
			// 如果图片为Null,则设置默认图片
		} else {
		
				//viewHolder.icon.setImageResource(R.drawable.tempicon);
		}*/
		// TODO Auto-generated method stub

		// asyncloadImage(viewHolder.icon, appInfos.get(position).getIconUrl());

		viewHolder.progress_view.setProgress(0);
		viewHolder.progress_view.setVisibility(View.VISIBLE);
		File tempFile = new File(Environment.getExternalStorageDirectory(),
				"/market/" + appInfos.get(position).getAppName() + ".apk");
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
			if (length != 0 && DownloadService.isExist(appInfos.get(
				position).getAppName())) {
				LogUtils.d("test", "已经存在");
				viewHolder.tvdown.setText("暂停");
			
				long count = sp.getLong(tempFile.getAbsolutePath()+"precent", 0);
				viewHolder.progress_view.setProgress(count);
			} else if(length != 0 && !DownloadService.isExist(appInfos.get(
					position).getAppName())){
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
							.getAppName());
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
					Log.e("tag",
							"appurl = " + Global.MAIN_URL
									+ appInfos.get(position).getAppUrl());
					Log.e("tag",
							"appIdx = "
									+ Integer.parseInt(appInfos.get(position)
											.getIdx()));
					/*
					 * Log.e("tag", "appname = " +
					 * appInfos.get(position).getAppName());
					 */
					SharedPreferences sp = mContext.getSharedPreferences(
							"down", mContext.MODE_PRIVATE);
					File tempFile = new File(Environment
							.getExternalStorageDirectory(), "/market/"
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

	private static class ViewHolder {
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
			try {
				if (iv_header != null && result != null) {
					// ContentResolver resolver = mContext.getContentResolver();
					// byte[] mContent =
					// readStream(resolver.openInputStream(Uri.parse(result.toString())));
					// //将字节数组转换为ImageView可调用的Bitmap对象
					// Bitmap myBitmap = getPicFromBytes(mContent, null);
					// ////把得到的图片绑定在控件上显示
					// iv_header.setImageBitmap(myBitmap);

					iv_header.setImageURI(result);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * @param newsitem
	 */
	public void addNewsItem(AppInfo newsitem) {
		appInfos.add(newsitem);
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
		drawMap.put(imageUrl, drawable);
		return drawable;
	}
}
