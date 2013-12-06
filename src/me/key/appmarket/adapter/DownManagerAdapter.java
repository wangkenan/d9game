package me.key.appmarket.adapter;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import me.key.appmarket.MarketApplication;
import me.key.appmarket.adapter.MyAdapter.ViewHolder;
import me.key.appmarket.adapter.MyAdapter.ViewHolder1;
import me.key.appmarket.tool.DownloadService;
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
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.market.d9game.R;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.download.ImageDownloader;
/**
 * 下载管理天重启
 * @author Administrator
 *
 */
public class DownManagerAdapter extends BaseAdapter {
	private LayoutInflater mInflater;
	private List<AppInfo> applist;
	private Context context;
	private List<AppInfo> updataList;
	private DisplayImageOptions options = new DisplayImageOptions.Builder()  
    .showImageForEmptyUri(R.drawable.tempicon).showStubImage(R.drawable.tempicon)  
    .resetViewBeforeLoading(false) 
    .delayBeforeLoading(100)  
    .cacheInMemory(true)           
    .cacheOnDisc(true)              
    .imageScaleType(ImageScaleType.IN_SAMPLE_INT)
    .bitmapConfig(Bitmap.Config.RGB_565)               
    .build(); 
	public DownManagerAdapter(List<AppInfo> applist,Context context){
		this.applist = applist;
		mInflater = LayoutInflater.from(context);
		this.context = context;
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
		ViewHolder1 vh = null;
		AppInfo ai = null;
		if(convertView == null) {
			
				convertView = mInflater
				.inflate(R.layout.item_downmanager, null);
				vh = new ViewHolder1();
				vh.icon_dm = (ImageView) convertView
						.findViewById(R.id.icon_dm);
				vh.appOprate=(ImageView) convertView
						.findViewById(R.id.iv_operate_downmanager);
				vh.install_dm = (TextView) convertView
						.findViewById(R.id.install_item_downmanager);
//				vh.progress_view_local_dm = (ProgressView) convertView
//						.findViewById(R.id.progress_view_local_dm);
				vh.progressbar_updown = (ProgressBar) convertView
						.findViewById(R.id.progressbar_updown);
				vh.name_down = (TextView) convertView
						.findViewById(R.id.name_down);
				vh.size_pro_downmanager = (TextView) convertView
						.findViewById(R.id.size_pro_downmanager);
				vh.press_pro_downmanager = (TextView) convertView
						.findViewById(R.id.press_pro_downmanager);
				convertView.setTag(vh);
	
		} else {
				vh = (ViewHolder1) convertView.getTag();
		}
		ai = applist.get(position);
		setData(position, convertView);
		ImageLoader.getInstance().displayImage(ai.getIconUrl(),vh.icon_dm , options);
		return convertView;
	}
	public void setData(int position, View convertView) {
		final AppInfo sdappInfo;
		sdappInfo = applist.get(position);
		sdappInfo.setDown(false);
		final ViewHolder1 v1 = ((ViewHolder1) convertView.getTag());
		ImageLoader.getInstance().displayImage(sdappInfo.getIconUrl(),
				v1.icon_dm, options);
		File tempFile = new File(LocalUtils.getRoot(context),
				"market/" + sdappInfo.getAppName() + ".apk");
		SharedPreferences sp = context.getSharedPreferences("down",
				context.MODE_PRIVATE);
		boolean isDownLoaded = DownloadService.isDownLoaded(sdappInfo
				.getAppName());
		int idx = Integer.parseInt(sdappInfo.getIdx());
		boolean isDownLoading = DownloadService.isDownLoading(idx);
		v1.name_down.setText(sdappInfo.getAppName());
		if (sdappInfo.isIspause()) {
			LogUtils.d("tureMy", sdappInfo.isIspause() + "");
			v1.install_dm.setText("暂停");
//			v1.progress_view_local_dm.setProgress(DownloadService
//					.getPrecent(idx));
		} else {
			v1.install_dm.setText("下载");
		}
		/*
		 * if (sdappInfo.isInstalled()) { v1.install_dm.setText("打开");
		 * 
		 * v1.progress_view_local_dm.setProgress(100);
		 * 
		 * Drawable mDrawableicon = cnt.getResources().getDrawable(
		 * R.drawable.action_type_software_update);
		 * v1.install_dm.setCompoundDrawablesWithIntrinsicBounds(null,
		 * mDrawableicon, null, null);
		 * 
		 * } else
		 */if (sdappInfo.isDown()) {

//			v1.progress_view_local_dm.setProgress(DownloadService
//					.getPrecent(idx));
			LogUtils.d("ture", isDownLoading + "isDown");

			v1.install_dm.setText("下载");
			/*
			 * Drawable mDrawableicon = cnt.getResources().getDrawable(
			 * R.drawable.downloading);
			 * v1.install_dm.setCompoundDrawablesWithIntrinsicBounds(null,
			 * mDrawableicon, null, null);
			 */
		} else if (isDownLoaded) {
			/*
			 * Drawable mDrawableicon = cnt.getResources().getDrawable(
			 * R.drawable.downloaded);
			 * v1.install_dm.setCompoundDrawablesWithIntrinsicBounds(null,
			 * mDrawableicon, null, null);
			 */
			v1.install_dm.setText("安装");
//			v1.progress_view_local_dm.setProgress(100);
			v1.progressbar_updown.setProgress(100);
			v1.size_pro_downmanager.setText(Long.parseLong(sdappInfo
					.getAppSize())
					/ 1000
					/ 1000
					+ "MB"
					+ "/"
					+ Long.parseLong(sdappInfo.getAppSize())
					/ 1000
					/ 1000
					+ "MB");
			v1.press_pro_downmanager.setText("100%");
		} else if (!isDownLoading) {
			v1.install_dm.setText("下载");
			/*
			 * mDrawable = cnt.getResources().getDrawable(
			 * R.drawable.downloading);
			 * v1.install_dm.setCompoundDrawablesWithIntrinsicBounds(null,
			 * mDrawable, null, null);
			 */
			// 获取将要下载的文件名，如果本地存在该文件，则取出该文件

			// LogUtils.d("sa", tempFile.getAbsolutePath());

			long length = sp.getLong(tempFile.getAbsolutePath(), 0);
			// LogUtils.d("sa", length+"");
			if (length != 0
					&& DownloadService.isExist(sdappInfo.getAppName())) {
				LogUtils.d("test", "已经存在" + sdappInfo.getAppName());
				v1.install_dm.setText("暂停");

				long count = sp.getLong(tempFile.getAbsolutePath()
						+ "precent", 0);
//				v1.progress_view_local_dm.setProgress(count);
			} else if (length != 0
					&& !DownloadService.isExist(sdappInfo.getAppName())) {
				Editor edit = sp.edit();
				edit.remove(tempFile.getAbsolutePath());
				edit.commit();
			}
		}
		long count = sp.getLong(tempFile.getAbsolutePath() + "precent", 0);
		v1.progressbar_updown.setProgress((int) count);
		DecimalFormat df = new DecimalFormat("0.00");
		String current = df.format(Float.parseFloat(sdappInfo.getAppSize())
				/ 100 * count / 1000 / 1000);
		String total = df
				.format(Float.parseFloat(sdappInfo.getAppSize()) / 1000 / 1000);
		v1.size_pro_downmanager
				.setText(current + "MB" + "/" + total + "MB");
		v1.press_pro_downmanager.setText(count + "%");
		if (count == 99) {
			String formatcurrent = df.format(Float.parseFloat(sdappInfo
					.getAppSize()) / 1000 / 1000);
			String formattotal = df.format(Float.parseFloat(sdappInfo
					.getAppSize()) / 1000 / 1000);
			v1.progressbar_updown.setProgress(100);
			v1.size_pro_downmanager.setText(formatcurrent + "MB" + "/"
					+ formattotal + "MB");
			v1.press_pro_downmanager.setText("100%");
		}
		v1.appOprate.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				LogUtils.d("MYADAPTER", "我被点击了");
				/*
				 * if (sdappInfo.isInstalled()) { AppUtils.launchApp(cnt,
				 * sdappInfo.getAppName()); } else
				 */if (DownloadService.isDownLoading(Integer
						.parseInt(sdappInfo.getIdx()))) {
					LogUtils.d("test", "暂停");
					File tempFile = DownloadService.CreatFileName(sdappInfo
							.getAppName());
					Intent intent = new Intent();
					intent.setAction(tempFile.getAbsolutePath());
					context.sendBroadcast(intent);
					Intent downState = new Intent();
					downState.setAction(tempFile.getAbsolutePath() + "down");
					downState.putExtra("isPause", !sdappInfo.isIspause());
					context.sendBroadcast(downState);
					LogUtils.d("myppclcik",
							"我发出了暂停中下载广播d" + sdappInfo.isIspause());
					if (!sdappInfo.isIspause()) {
						v1.install_dm.setText("暂停");
						sdappInfo.setDown(false);
					} else {
						v1.install_dm.setText("下载");
						sdappInfo.setDown(true);
					}
					LogUtils.d("down", sdappInfo.isDown() + "");
					LogUtils.d("test", sdappInfo.isIspause() + "1");
					sdappInfo.setIspause(!sdappInfo.isIspause());
					LogUtils.d("test", sdappInfo.isIspause() + "2");
				} else if (DownloadService.isDownLoaded(sdappInfo
						.getAppName())) {
					// 已经下载
					DownloadService.Instanll(sdappInfo.getAppName(), context);
				} else {
					/*
					 * Log.e("tag", "appname = " +
					 * appInfos.get(position).getAppName());
					 */
					SharedPreferences sp = context.getSharedPreferences("down",
							context.MODE_PRIVATE);
					File tempFile = new File(Environment
							.getExternalStorageDirectory(), "/market/"
							+ sdappInfo.getAppName() + ".apk");
					v1.install_dm.setText("下载");

					long length = sp.getLong(tempFile.getAbsolutePath(), 0);
					LogUtils.d("myppp",
							"我发出了暂停中下载广播dsgsdg" + !sdappInfo.isIspause());
					/*
					 * DownloadService.downNewFile(appInfos.get(position)
					 * .getAppUrl(), Integer.parseInt(appInfos.get(
					 * position).getIdx()), appInfos.get(position)
					 * .getAppName(),length,0);
					 */
					DownloadService.downNewFile(sdappInfo, length, 0, null);
					sdappInfo.setDown(true);
					sdappInfo.setIspause(false);
					Intent intent = new Intent();
					Intent downState = new Intent();
					downState.setAction(tempFile.getAbsolutePath() + "down");
					downState.putExtra("isPause", sdappInfo.isIspause());
					context.sendBroadcast(downState);
					intent.setAction(MarketApplication.PRECENT);
					context.sendBroadcast(intent);
					Toast.makeText(context,
							sdappInfo.getAppName() + " 开始下载...",
							Toast.LENGTH_SHORT).show();
				}

			}

		});
	}
	
	static class ViewHolder1 {
		ImageView icon_dm,appOprate;
		ProgressBar progressbar_updown;
		TextView install_dm;
//		ProgressView progress_view_local_dm;
		TextView name_down;
		TextView size_pro_downmanager;
		TextView press_pro_downmanager;

	}


}
