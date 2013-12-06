package me.key.appmarket.adapter;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import me.key.appmarket.AppDetailActivity;
import me.key.appmarket.LocalGameFragment;
import me.key.appmarket.MarketApplication;
import me.key.appmarket.OneKeyInstallActivity;
import me.key.appmarket.network.NetworkUtils;
import me.key.appmarket.tool.DownloadService;
import me.key.appmarket.tool.ToolHelper;
import me.key.appmarket.tool.TxtReader;
import me.key.appmarket.utils.AppInfo;
import me.key.appmarket.utils.AppUtils;
import me.key.appmarket.utils.Global;
import me.key.appmarket.utils.LocalUtils;
import me.key.appmarket.utils.LogUtils;
import me.key.appmarket.widgets.ProgressView;
import net.tsz.afinal.FinalDb;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView.ScaleType;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SectionIndexer;
import android.widget.TextView;
import android.widget.Toast;

import com.market.d9game.R;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

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
	private int width;
	private int height;
	private int gapPy;
	private int bigImHeight;
	private int gapPx;
	private boolean isDownLoading;
	private List<AppInfo> appManaInfos;
	private List<AppInfo> downList;
	private List<AppInfo> updateList;
	private List<AppInfo> mAppInfos;
	private List<AppInfo> temp = new ArrayList<AppInfo>();
	private FinalDb db;
	public TabHolder tabHolder;
	private ListView lv;
	// 是我的游戏还是sd卡
	private boolean isLeft = true;
	private DisplayImageOptions options = new DisplayImageOptions.Builder()
			.showImageForEmptyUri(R.drawable.tempicon)
			.showStubImage(R.drawable.tempicon).resetViewBeforeLoading(false)
			.delayBeforeLoading(100).cacheInMemory(true).cacheOnDisc(true)
			.imageScaleType(ImageScaleType.IN_SAMPLE_INT)
			.bitmapConfig(Bitmap.Config.RGB_565).build();

	public MyAdapter(Context context, List<AppInfo> appManaInfos,
			List<AppInfo> mAppInfos, ListView lv) {
		this.mInflater = LayoutInflater.from(context);
		this.cnt = context;
		this.appManaInfos = appManaInfos;
		this.mAppInfos = mAppInfos;
		cnt = context;
		DisplayMetrics dm = new DisplayMetrics();
		((Activity) cnt).getWindowManager().getDefaultDisplay().getMetrics(dm);
		width = dm.widthPixels;
		height = dm.heightPixels;
		gapPx = convertDipOrPx(cnt, 5);
		gapPy = convertDipOrPx(cnt, 10);
		bigImHeight = (int) ((width - gapPy) / 2 / 1.27f);
		ROOT = LocalUtils.getRoot(context);
		db = FinalDb.create(context);
		this.lv = lv;
	}

	@Override
	public int getCount() {
		return appManaInfos.size() + 3;
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
	public int getItemViewType(int position) {

		if (position == 0) {
			return 0;
		} else if (position == 1) {
			return 3;

		} else if (position == 2) {
			return 2;
		} else {
			return 1;
		}
	}

	@Override
	public int getViewTypeCount() {
		return 4;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		final int newposition = position - 3;
		final ViewHolder holder;
		final ViewHolder2 viewHolder2;

		int type = getItemViewType(position);
		Drawable mDrawable;
		final AppInfo sdappInfo;
		if (convertView == null) {
			switch (type) {

			case 0:
				viewHolder2 = new ViewHolder2();
				convertView = mInflater.inflate(R.layout.ranktest, null);
				viewHolder2.banner = (ImageView) convertView
						.findViewById(R.id.iv_rank_test);
				viewHolder2.banner.setPadding(0, 1, 0, 1);
				convertView.setTag(viewHolder2);
				break;
			case 1:
				holder = new ViewHolder();
				convertView = mInflater.inflate(R.layout.list_item, null);
				holder.info = (TextView) convertView.findViewById(R.id.info);
				holder.icon = (ImageView) convertView.findViewById(R.id.icon);
				holder.btn = (TextView) convertView.findViewById(R.id.install);
				holder.progress_view = (ProgressView) convertView
						.findViewById(R.id.progress_view_local);

				holder.appsize = (TextView) convertView
						.findViewById(R.id.appsize);
				holder.ivOprateState = (ImageView) convertView
						.findViewById(R.id.iv_oprate_state);
				convertView.setTag(holder);
				break;
			case 2:
				tabHolder = new TabHolder();
				convertView = mInflater.inflate(R.layout.tab_localgame, null);
				tabHolder.mygame = (TextView) convertView
						.findViewById(R.id.mygame);
				tabHolder.sdgame = (TextView) convertView
						.findViewById(R.id.sdgame_tv);
				convertView.setTag(tabHolder);
				break;
			case 3:
				convertView = mInflater.inflate(R.layout.advert_banner, null);
				convertView.setPadding(0, 6, 0, 6);
				break;
			}
		} else {
			switch (type) {
			case 1:
				holder = (ViewHolder) convertView.getTag();
				break;
			case 0:
				viewHolder2 = (ViewHolder2) convertView.getTag();
				break;
			case 2:
				tabHolder = (TabHolder) convertView.getTag();
				break;
			}
		}
		switch (type) {

		case 0:
			final ViewHolder2 v3 = ((ViewHolder2) convertView.getTag());
			// setImagePosition(R.drawable.bannaer, v3.banner);
			v3.banner.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					Intent intent = new Intent();
					intent.setClass(cnt, OneKeyInstallActivity.class);
					cnt.startActivity(intent);
				}
			});
			break;

		/*
		 * case 0: sdappInfo = downList.get(newposition);
		 * sdappInfo.setDown(false); final ViewHolder1 v1 = ((ViewHolder1)
		 * convertView.getTag());
		 * ImageLoader.getInstance().displayImage(sdappInfo.getIconUrl(),
		 * v1.icon_dm, options); File tempFile = new
		 * File(Environment.getExternalStorageDirectory(), "/market/" +
		 * sdappInfo.getAppName() + ".apk"); SharedPreferences sp =
		 * cnt.getSharedPreferences("down", cnt.MODE_PRIVATE); boolean
		 * isDownLoaded = DownloadService.isDownLoaded(sdappInfo .getAppName());
		 * iintdx = Integer.parseInt(sdappInfo.getIdx()); isDownLoading =
		 * DownloadService.isDownLoading(idx);
		 * v1.name_down.setText(sdappInfo.getAppName()); if
		 * (sdappInfo.isIspause()) { LogUtils.d("tureMy", sdappInfo.isIspause()
		 * + ""); v1.install_dm.setText("暂停");
		 * v1.progress_view_local_dm.setProgress(DownloadService
		 * .getPrecent(idx)); } else { v1.install_dm.setText("下载中"); }
		 * 
		 * if (sdappInfo.isInstalled()) { v1.install_dm.setText("打开");
		 * 
		 * v1.progress_view_local_dm.setProgress(100);
		 * 
		 * Drawable mDrawableicon = cnt.getResources().getDrawable(
		 * R.drawable.action_type_software_update);
		 * v1.install_dm.setCompoundDrawablesWithIntrinsicBounds(null,
		 * mDrawableicon, null, null);
		 * 
		 * } else if (sdappInfo.isDown()) {
		 * 
		 * v1.progress_view_local_dm.setProgress(DownloadService
		 * .getPrecent(idx)); LogUtils.d("ture", isDownLoading + "isDown");
		 * 
		 * v1.install_dm.setText("下载中");
		 * 
		 * Drawable mDrawableicon = cnt.getResources().getDrawable(
		 * R.drawable.downloading);
		 * v1.install_dm.setCompoundDrawablesWithIntrinsicBounds(null,
		 * mDrawableicon, null, null);
		 * 
		 * } else if (isDownLoaded {)
		 * 
		 * Drawable mDrawableicon = cnt.getResources().getDrawable(
		 * R.drawable.downloaded);
		 * v1.install_dm.setCompoundDrawablesWithIntrinsicBounds(null,
		 * mDrawableicon, null, null);
		 * 
		 * v1.install_dm.setText("安装");
		 * v1.progress_view_local_dm.setProgress(100);
		 * v1.progressbar_updown.setProgress(100);
		 * v1.size_pro_downmanager.setText(Long.parseLong(sdappInfo
		 * .getAppSize()) / 1000 / 1000 + "MB" + "/" +
		 * Long.parseLong(sdappInfo.getAppSize()) / 1000 / 1000 + "MB");
		 * v1.press_pro_downmanager.setText("100%"); } else if (!isDownLoading)
		 * { v1.install_dm.setText("下载");
		 * 
		 * mDrawable = cnt.getResources().getDrawable( R.drawable.downloading);
		 * v1.install_dm.setCompoundDrawablesWithIntrinsicBounds(null,
		 * mDrawable, null, null);
		 * 
		 * // 获取将要下载的文件名，如果本地存在该文件，则取出该文件
		 * 
		 * // LogUtils.d("sa", tempFile.getAbsolutePath());
		 * 
		 * long length = sp.getLong(tempFile.getAbsolutePath(), 0); //
		 * LogUtils.d("sa", length+""); if (length != 0 &&
		 * DownloadService.isExist(sdappInfo.getAppName())) { LogUtils.d("test",
		 * "已经存在" + sdappInfo.getAppName()); v1.install_dm.setText("暂停");
		 * 
		 * long count = sp.getLong(tempFile.getAbsolutePath() + "precent", 0);
		 * v1.progress_view_local_dm.setProgress(count); } else if (length != 0
		 * && !DownloadService.isExist(sdappInfo.getAppName())) { Editor edit =
		 * sp.edit(); edit.remove(tempFile.getAbsolutePath()); edit.commit(); }
		 * } long count = sp.getLong(tempFile.getAbsolutePath() + "precent", 0);
		 * v1.progressbar_updown.setProgress((int) count); DecimalFormat df =
		 * new DecimalFormat("0.00"); String current =
		 * df.format(Float.parseFloat(sdappInfo.getAppSize()) / 100 * count /
		 * 1000 / 1000); String total = df
		 * .format(Float.parseFloat(sdappInfo.getAppSize()) / 1000 / 1000);
		 * v1.size_pro_downmanager .setText(current + "MB" + "/" + total +
		 * "MB"); v1.press_pro_downmanager.setText(count + "%"); if (count ==
		 * 99) { String formatcurrent = df.format(Float.parseFloat(sdappInfo
		 * .getAppSize()) / 1000 / 1000); String formattotal =
		 * df.format(Float.parseFloat(sdappInfo .getAppSize()) / 1000 / 1000);
		 * v1.progressbar_updown.setProgress(100);
		 * v1.size_pro_downmanager.setText(formatcurrent + "MB" + "/" +
		 * formattotal + "MB"); v1.press_pro_downmanager.setText("100%"); }
		 * v1.install_dm.setOnClickListener(new OnClickListener() {
		 * 
		 * @Override public void onClick(View arg0) { LogUtils.d("MYADAPTER",
		 * "我被点击了");
		 * 
		 * if (sdappInfo.isInstalled()) { AppUtils.launchApp(cnt,
		 * sdappInfo.getAppName()); } else if
		 * (DownloadService.isDownLoading(Integer
		 * .parseInt(sdappInfo.getIdx()))) { LogUtils.d("test", "暂停"); File
		 * tempFile = DownloadService.CreatFileName(sdappInfo .getAppName());
		 * Intent intent = new Intent();
		 * intent.setAction(tempFile.getAbsolutePath());
		 * cnt.sendBroadcast(intent); Intent downState = new Intent();
		 * downState.setAction(tempFile.getAbsolutePath() + "down");
		 * downState.putExtra("isPause", !sdappInfo.isIspause());
		 * cnt.sendBroadcast(downState); LogUtils.d("myppclcik", "我发出了暂停中下载广播d"
		 * + sdappInfo.isIspause()); if (!sdappInfo.isIspause()) {
		 * v1.install_dm.setText("暂停"); sdappInfo.setDown(false); } else {
		 * v1.install_dm.setText("下载中"); sdappInfo.setDown(true); }
		 * LogUtils.d("down", sdappInfo.isDown() + ""); LogUtils.d("test",
		 * sdappInfo.isIspause() + "1");
		 * sdappInfo.setIspause(!sdappInfo.isIspause()); LogUtils.d("test",
		 * sdappInfo.isIspause() + "2"); } else if
		 * (DownloadService.isDownLoaded(sdappInfo .getAppName())) { // 已经下载
		 * DownloadService.Instanll(sdappInfo.getAppName(), cnt); } else {
		 * Log.e("tag", "appurl = " + Global.MAIN_URL + sdappInfo.getAppUrl());
		 * Log.e("tag", "appIdx = " + Integer.parseInt(sdappInfo.getIdx()));
		 * 
		 * Log.e("tag", "appname = " + appInfos.get(position).getAppName());
		 * 
		 * SharedPreferences sp = cnt.getSharedPreferences("down",
		 * cnt.MODE_PRIVATE); File tempFile = new File(Environment
		 * .getExternalStorageDirectory(), "/market/" + sdappInfo.getAppName() +
		 * ".apk"); v1.install_dm.setText("下载中");
		 * 
		 * long length = sp.getLong(tempFile.getAbsolutePath(), 0);
		 * LogUtils.d("myppp", "我发出了暂停中下载广播dsgsdg" + !sdappInfo.isIspause());
		 * 
		 * DownloadService.downNewFile(appInfos.get(position) .getAppUrl(),
		 * Integer.parseInt(appInfos.get( position).getIdx()),
		 * appInfos.get(position) .getAppName(),length,0);
		 * 
		 * DownloadService.downNewFile(sdappInfo, length, 0, null);
		 * sdappInfo.setDown(true); sdappInfo.setIspause(false); Intent intent =
		 * new Intent(); Intent downState = new Intent();
		 * downState.setAction(tempFile.getAbsolutePath() + "down");
		 * downState.putExtra("isPause", sdappInfo.isIspause());
		 * cnt.sendBroadcast(downState);
		 * intent.setAction(MarketApplication.PRECENT);
		 * cnt.sendBroadcast(intent); Toast.makeText(cnt, sdappInfo.getAppName()
		 * + " 开始下载...", Toast.LENGTH_SHORT).show(); }
		 * 
		 * }
		 * 
		 * });
		 * 
		 * break;
		 */
		case 1:
			final ViewHolder v2 = ((ViewHolder) convertView.getTag());
			boolean isUpdate = false;
			sdappInfo = appManaInfos.get(newposition);
			isUpdate = sdappInfo.isCanUpdate();
			if (isLeft) {
				if (isUpdate) {
					v2.btn.setText("升级");
					v2.ivOprateState.setImageResource(R.drawable.update_btn);
				} else {

					LogUtils.d("MyUpdate", sdappInfo.getPackageName() + "打开");
					v2.btn.setText("打开");
					v2.ivOprateState.setImageResource(R.drawable.one_key);
				}
				/*
				 * Drawable mDrawable1 = this.cnt.getResources().getDrawable(
				 * R.drawable.action_type_software_update);
				 * v2.btn.setCompoundDrawablesWithIntrinsicBounds(null,
				 * mDrawable1, null, null);
				 */
				if (sdappInfo.getLastTime() == Long.MAX_VALUE) {
					v2.appsize.setText("您最近没有玩过哦");
				} else {
					Long lastTime = sdappInfo.getLastTime();
					long scond = lastTime / 1000;
					long minute = scond / 60;
					long houre = minute / 60;
					long day = houre / 24;
					if (scond < 60) {
						v2.appsize.setText("您上次玩是" + scond + "秒之前");
					} else if (minute < 60) {
						v2.appsize.setText("您上次玩是" + minute + "分之前");
					} else if (houre < 60) {
						v2.appsize.setText("您上次玩是" + houre + "小时之前");
					} else if (day < 60) {
						v2.appsize.setText("您上次玩是" + day + "天之前");
					}
				}
				/*
				 * else { sdappInfo = mData .get((newposition -
				 * appManaInfos.size() - downList .size()));
				 * 
				 * Drawable mDrawable2 = cnt.getResources().getDrawable(
				 * R.drawable.downloaded);
				 * v2.btn.setCompoundDrawablesWithIntrinsicBounds(null,
				 * mDrawable2, null, null);
				 * 
				 * v2.btn.setText("安装"); v2.progress_view.setProgress(0); String
				 * mb = ToolHelper.Kb2Mb(sdappInfo.getAppSize());
				 * v2.appsize.setText(mb); }
				 */
				v2.btn.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View arg0) {
						LogUtils.d("MyAdapter", "我运行了" + sdappInfo.getAppName());
						AppUtils.launchApp(cnt, sdappInfo.getPackageName());
					}

				});
				if (isUpdate) {
					v2.btn.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							List<AppInfo> down_temp = new ArrayList<AppInfo>();
							File tempFile = new File(Environment
									.getExternalStorageDirectory(), "/market/"
									+ sdappInfo.getAppName() + ".apk");
							if (tempFile.exists()) {
								tempFile.delete();
							}
							DownloadService.downNewFile(sdappInfo, 0, 0, null);
							// downList.add(sdappInfo);
							// notifyDataSetChanged();
							sdappInfo.setDown(true);
							sdappInfo.setIspause(false);
							Intent intent = new Intent();
							Intent downState = new Intent();

							downState.setAction(tempFile.getAbsolutePath()
									+ "down");
							downState.putExtra("isPause", sdappInfo.isIspause());
							cnt.sendBroadcast(downState);
							intent.setAction(MarketApplication.PRECENT);
							cnt.sendBroadcast(intent);
							Toast.makeText(cnt,
									sdappInfo.getAppName() + " 开始下载...",
									Toast.LENGTH_SHORT).show();
							/*
							 * v2.btn.setVisibility(View.GONE);
							 * v2.progress_view.setVisibility(View.VISIBLE);
							 */
						}
					});
				}
				/*
				 * int idx = Integer.parseInt(sdappInfo.getIdx());
				 * v2.progress_view
				 * .setProgress(DownloadService.getPrecent(idx)); boolean
				 * isDownLoaded = DownloadService.isDownLoaded(sdappInfo
				 * .getAppName()); if (isDownLoaded ){
				 * v2.progress_view.setVisibility(View.GONE);
				 * v2.btn.setVisibility(View.VISIBLE); }
				 */
				// v2.progress_view.setVisibility(View.VISIBLE);
				v2.info.setText(sdappInfo.getAppName());
				v2.icon.setVisibility(View.VISIBLE);
				v2.icon.setImageDrawable(sdappInfo.getAppIcon());
			} else {
				v2.btn.setText("安装");
				v2.progress_view.setProgress(0);
				String mb = ToolHelper.Kb2Mb(sdappInfo.getAppSize());
				v2.appsize.setText(mb);
				v2.progress_view.setProgress(0);
				// v2.progress_view.setVisibility(View.VISIBLE);
				v2.info.setText(sdappInfo.getAppName());
				v2.icon.setVisibility(View.VISIBLE);
				Drawable appIcon = sdappInfo.getAppIcon();
				if (appIcon != null) {
					v2.icon.setImageDrawable(appIcon);
				} else {
					ImageLoader.getInstance().displayImage(
							sdappInfo.getIconUrl(), v2.icon, options);
				}

				v2.btn.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						LogUtils.d("MyAdapter", "我安装了" + sdappInfo.getAppName());
						installApp(sdappInfo);
					}
				});
			}
			break;
		case 2:
			final TabHolder th = (TabHolder) convertView.getTag();
			th.mygame.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					setLeft(th);
				}

			});
			th.sdgame.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					setRight(th);
				}

			});
			break;
		}
		return convertView;

	}

	static class ViewHolder {
		public TextView appsize;
		public TextView btn;
		public ImageView icon;
		public TextView info;
		private ProgressView progress_view;
		private ImageView ivOprateState;
	}

	static class ViewHolder1 {
		ImageView icon_dm;
		ProgressBar progressbar_updown;
		TextView install_dm;
		ProgressView progress_view_local_dm;
		TextView name_down;
		TextView size_pro_downmanager;
		TextView press_pro_downmanager;

	}

	static class ViewHolder2 {
		ImageView banner;
	}

	static class TabHolder {
		TextView mygame;
		TextView sdgame;
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
		this.cnt.startActivity(intent);
	}

	/*
	 * private void setImagePosition(int resId, ImageView banner) { Bitmap bm =
	 * BitmapFactory.decodeResource(cnt.getResources(), resId); Bitmap newbitmap
	 * = Bitmap.createBitmap((width - gapPy), (int) ((width - gapPy) / 3.87),
	 * bm.getConfig()); getNewBitMapPos(bm, newbitmap);
	 * banner.setImageBitmap(newbitmap); }
	 */

	// 处理图片
	private void getNewBitMapPos(Bitmap bm, Bitmap newbitmap) {
		Paint paint = new Paint();
		Canvas canvas = new Canvas(newbitmap);
		Matrix matrix = new Matrix();
		double newWidth = 1.00;
		double newHeight = 2.6;
		int gapPx = convertDipOrPx(cnt, 5);
		// matrix.setRotate(30, bm.getWidth()/2, bm.getHeight()/2);
		float scaleWidth = (float) ((width - gapPx)) / bm.getWidth();
		float scaleHeight = (float) (newbitmap.getHeight()) / bm.getHeight();
		LogUtils.d("scaleWidth+scaleWidth", scaleWidth + ":" + scaleHeight
				+ "++" + width + "PPP" + 2 / 3);
		matrix.postScale(scaleWidth, scaleHeight);
		// 使用画布将原图片，矩阵，画笔进行新图片的绘画
		canvas.drawBitmap(bm, matrix, paint);
	}

	public static int convertDipOrPx(Context context, int dip) {
		float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dip * scale + 0.5f * (dip >= 0 ? 1 : -1));
	}

	public void setLeft(final TabHolder th) {
		if (!isLeft) {
			th.mygame.setBackgroundResource(R.drawable.mygame_btn);
			th.sdgame.setBackgroundResource(0);
			LocalGameFragment.mygame
					.setBackgroundResource(R.drawable.mygame_btn);
			LocalGameFragment.sdgame.setBackgroundResource(0);
			temp = appManaInfos;
			appManaInfos = mAppInfos;
			mAppInfos = temp;
			notifyDataSetChanged();
			isLeft = true;
			if (NetworkUtils.isNetworkConnected(cnt)) {
				lv.setOnItemClickListener(new OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> parent, View view,
							int position, long id) {
						if ((position + 1) != lv.getAdapter().getCount() && position >2) {
							// TODO Auto-generated method stub
							AppInfo mAppInfo = (AppInfo) appManaInfos
									.get(position - 3);
							Drawable appIcon = mAppInfo.getAppIcon();
							mAppInfo.setAppIcon(null);
							LogUtils.d("Local", "mAppInfo" + mAppInfo.getIdx());
							// Log.d("YTL", "mAppInfo.getIdx() = " +
							// mAppInfo.getIdx());
							Intent intent = new Intent(cnt,
									AppDetailActivity.class);
							intent.putExtra("appid", mAppInfo.getIdx());
							intent.putExtra("appinfo", mAppInfo);
							cnt.startActivity(intent);
							mAppInfo.setAppIcon(appIcon);
						}
					}

				});
			}
		}
	}

	public void setRight(final TabHolder th) {
		if (isLeft) {
			th.sdgame.setBackgroundResource(R.drawable.mygame_btn);
			th.mygame.setBackgroundResource(0);
			LocalGameFragment.sdgame
					.setBackgroundResource(R.drawable.mygame_btn);
			LocalGameFragment.mygame.setBackgroundResource(0);
			temp = mAppInfos;
			mAppInfos = appManaInfos;
			appManaInfos = temp;
			notifyDataSetChanged();
			isLeft = false;
			lv.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> parent, View view,
						int position, long id) {
				}

			});
		}
	}
}
