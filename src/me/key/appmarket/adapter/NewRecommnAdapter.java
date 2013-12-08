package me.key.appmarket.adapter;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.key.appmarket.AppDetailActivity;
import me.key.appmarket.MarketApplication;
import me.key.appmarket.ImageNet.AsyncImageLoader;
import me.key.appmarket.ImageNet.AsyncImageLoader.ImageCallback;
import me.key.appmarket.tool.DownloadService;
import me.key.appmarket.tool.ToolHelper;
import me.key.appmarket.utils.AppInfo;
import me.key.appmarket.utils.AppUtils;
import me.key.appmarket.utils.Global;
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
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.market.d9game.R;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

public class NewRecommnAdapter extends BaseAdapter {

	private ArrayList<AppInfo> appInfos;
	private LayoutInflater lay;
	private File cache;
	private Context mContext;
	AsyncImageLoader asyncImageLoader;
	private ListView mylistView;
	// 是否暂停
	private boolean isPause;
	// 是否是下载状态
	private boolean isDownLoading;
	private boolean isDownLoadingRight;
	// 是否异步加载图片
	public boolean isAsyn;
	private WindowManager wm;
	private int gapPy;
	private int bigImHeight;
	private int gapPx;
	private int width;
	private int height;
	private String desc;
	private FinalDb db;
	
	private Map<String, Drawable> drawMap = new HashMap<String, Drawable>();
	// 设置ImageLoade初始化信息
	private DisplayImageOptions options = new DisplayImageOptions.Builder()
			.showImageForEmptyUri(R.drawable.tempicon)
			.showStubImage(R.drawable.tempicon).resetViewBeforeLoading(false)
			.delayBeforeLoading(200).cacheInMemory(true).cacheOnDisc(true)
			.imageScaleType(ImageScaleType.IN_SAMPLE_INT)
			.bitmapConfig(Bitmap.Config.RGB_565).build();

	private static final int TYPE_1 = 0;
	private static final int TYPE_2 = 1;
	private static final int SETIMAGE = 3;
	private static final int SETTEXT = 4;
	private static final int TYPE_3 = 2;
	private Map<Integer, String> bigImageMap = new HashMap<Integer, String>();

	public NewRecommnAdapter(ArrayList<AppInfo> appInfos, Context context,
			File cache, ListView mylistView) {
		super();
		this.appInfos = appInfos;
		this.cache = cache;
		this.mylistView = mylistView;
		mContext = context;
		lay = LayoutInflater.from(context);
		db = FinalDb.create(context);
		asyncImageLoader = new AsyncImageLoader();
		wm = (WindowManager) context.getSystemService(context.WINDOW_SERVICE);
		Display defaultDisplay = wm.getDefaultDisplay();
		DisplayMetrics dm = new DisplayMetrics();
		((Activity) mContext).getWindowManager().getDefaultDisplay()
				.getMetrics(dm);
		width = dm.widthPixels;
		height = dm.heightPixels;
		gapPx = convertDipOrPx(mContext, 5);
		gapPy = convertDipOrPx(mContext, 10);
		bigImHeight = (int) ((width - gapPy) / 2 / 1.27f);
	}

	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			if (msg.what == SETTEXT) {
				ArrayList<Object> al = (ArrayList<Object>) msg.obj;
				TextView tv = (TextView) al.get(0);
				String desc = (String) al.get(1);
				tv.setText(desc);
			}

		}
	};

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return appInfos.size() / 2;
	}

	@Override
	public Object getItem(int arg0) {
		return appInfos.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		// TODO Auto-generated method stub
		return arg0;
	}

	@Override
	public int getItemViewType(int position) {

		return TYPE_2;
	}

	@Override
	public int getViewTypeCount() {
		return 1;
	}

	@Override
	public View getView(final int position, View convertvView, ViewGroup arg2) {
		final ViewHolder viewHolder;
		final ViewHolder2 viewHolder2;
		final ViewHolder3 viewHolder3;
		Drawable mDrawable;
		AppInfo sdappInfo;
		AppInfo sdappInfoRight;
		int type = getItemViewType(position);

		if (convertvView == null) {
			switch (type) {
			/*
			 * case TYPE_1: viewHolder2 = new ViewHolder2(); convertvView =
			 * lay.inflate(R.layout.item_recomm_lisview, null);
			 * viewHolder2.progress_view = (ProgressView) convertvView
			 * .findViewById(R.id.recomm_progress_view);
			 * viewHolder2.recomm_bigiv = (ImageView) convertvView
			 * .findViewById(R.id.recomm_bigiv); viewHolder2.descr = (TextView)
			 * convertvView .findViewById(R.id.recomm_descr); viewHolder2.name =
			 * (TextView) convertvView .findViewById(R.id.recomm_name);
			 * viewHolder2.tvdown = (TextView) convertvView
			 * .findViewById(R.id.recomm_tv_down);
			 * convertvView.setTag(viewHolder2); break;
			 */
			case TYPE_2:
				viewHolder = new ViewHolder();
				convertvView = lay.inflate(R.layout.app_list_recomm_item, null);

				viewHolder.icon = (ImageView) convertvView
						.findViewById(R.id.icon2);
				viewHolder.name = (TextView) convertvView
						.findViewById(R.id.app_name2);
				viewHolder.size = (TextView) convertvView
						.findViewById(R.id.appsize2);
				viewHolder.tvdown = (TextView) convertvView
						.findViewById(R.id.tv_down2);
				viewHolder.iconRight = (ImageView) convertvView
						.findViewById(R.id.icon_right);
				viewHolder.nameRight = (TextView) convertvView
						.findViewById(R.id.app_name_right);
				viewHolder.sizeRight = (TextView) convertvView
						.findViewById(R.id.appsize_right);
				viewHolder.tvdownRight = (TextView) convertvView
						.findViewById(R.id.tv_down_right);
				viewHolder.LeftBar = (RelativeLayout) convertvView
						.findViewById(R.id.top_bar2);
				viewHolder.RightBar = (RelativeLayout) convertvView
						.findViewById(R.id.top_bar_right);
				/*
				 * viewHolder.progress_view = (ProgressView) convertvView
				 * .findViewById(R.id.progress_view2);
				 */
				convertvView.setTag(viewHolder);
				break;
			}

		} else {
			switch (type) {
			/*
			 * case TYPE_1: viewHolder2 = (ViewHolder2) convertvView.getTag();
			 * break;
			 */

			case TYPE_2:
				viewHolder = (ViewHolder) convertvView.getTag();
				break;
			}
		}
		switch (type) {
		/*
		 * case TYPE_1: final int newposition = position; sdappInfo =
		 * appInfos.get(newposition); AppInfo findApp = db
		 * .findById(sdappInfo.getAppName(), AppInfo.class); if (findApp !=
		 * null) { sdappInfo = findApp; LogUtils.d("NEWRECOMM",
		 * sdappInfo.getAppName()); } final ViewHolder2 v2 = ((ViewHolder2)
		 * convertvView.getTag()); setDownState(newposition, v2);
		 * v2.name.setText(sdappInfo.getAppName());
		 * v2.descr.setText(sdappInfo.getAppDescri());
		 * 
		 * if (sdappInfo.getRecoPic() != null) {
		 * ImageLoader.getInstance().displayImage(sdappInfo.getRecoPic(),
		 * v2.recomm_bigiv, options); } else {
		 * ImageLoader.getInstance().displayImage( sdappInfo.getAppimgurl()[0],
		 * v2.recomm_bigiv, options); } // String bigurl =
		 * bigImageMap.get(newposition); //
		 * ImageLoader.getInstance().displayImage(bigurl, v2.recomm_bigiv, //
		 * options); switch (newposition) { case 0: //
		 * v2.recomm_bigiv.setImageResource(R.drawable.reco_1); //
		 * setImage(v2.recomm_bigiv, R.drawable.reco_1); break;
		 * 
		 * case 3: // v2.recomm_bigiv.setImageResource(R.drawable.reco_4); //
		 * setImage(v2.recomm_bigiv, R.drawable.reco_4); break; case 6: //
		 * v2.recomm_bigiv.setImageResource(R.drawable.reco_7); //
		 * setImage(v2.recomm_bigiv, R.drawable.reco_7); break; case 9: //
		 * v2.recomm_bigiv.setImageResource(R.drawable.reco_10); //
		 * setImage(v2.recomm_bigiv, R.drawable.reco_10); break; }
		 * 
		 * break;
		 */
		case TYPE_2:
			final int newposition2 = position;
			sdappInfo = appInfos.get(newposition2 * 2);
			sdappInfoRight = appInfos.get(newposition2 * 2 + 1);
			AppInfo findApp1 = db.findById(sdappInfo.getAppName(),
					AppInfo.class);
			if (findApp1 != null) {
				sdappInfo = findApp1;
				LogUtils.d("NEWRECOMM", sdappInfo.getAppName());
			}
			AppInfo findApp2 = db.findById(sdappInfoRight.getAppName(),
					AppInfo.class);
			if (findApp2 != null) {
				sdappInfoRight = findApp2;
				LogUtils.d("NEWRECOMM", sdappInfo.getAppName());
			}
			final ViewHolder v1 = ((ViewHolder) convertvView.getTag());
			LogUtils.d("NewRecommn", appInfos.size() + "");

			v1.name.setText(sdappInfo.getAppName());
			v1.size.setText(ToolHelper.Kb2Mb(sdappInfo.getAppSize()));
			ImageLoader.getInstance().displayImage(sdappInfo.getIconUrl(),
					v1.icon, options);
			v1.nameRight.setText(sdappInfoRight.getAppName());
			v1.sizeRight.setText(ToolHelper.Kb2Mb(sdappInfoRight.getAppSize()));
			ImageLoader.getInstance().displayImage(sdappInfoRight.getIconUrl(),
					v1.iconRight, options);
			setDownState(newposition2, v1, sdappInfo, sdappInfoRight);
			final AppInfo sdappInfoF = sdappInfo;
			final AppInfo sdappInfoRightF = sdappInfoRight;
			v1.LeftBar.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					Intent intent = new Intent(mContext,
							AppDetailActivity.class);
					// LogUtils.d("error", position+"");
					intent.putExtra("appid", sdappInfoF.getIdx());
					intent.putExtra("appinfo", sdappInfoF);
					mContext.startActivity(intent);
				}
			});
			v1.RightBar.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					Intent intent = new Intent(mContext,
							AppDetailActivity.class);
					// LogUtils.d("error", position+"");
					intent.putExtra("appid", sdappInfoRightF.getIdx());
					intent.putExtra("appinfo", sdappInfoRightF);
					mContext.startActivity(intent);
				}
			});
			break;
		}

		return convertvView;
	}

	public void setDownState(final int position, final BaseHolder v1,
			final AppInfo sdappInfo, final AppInfo sdappInfoRight) {
		Drawable mDrawable;
		// v1.progress_view.setProgress(0);
		// v1.progress_view.setVisibility(View.VISIBLE);
		File tempFile = new File(Environment.getExternalStorageDirectory(),
				"/market/" + sdappInfo.getAppName() + ".apk");
		File tempFileRight = new File(
				Environment.getExternalStorageDirectory(), "/market/"
						+ sdappInfoRight.getAppName() + ".apk");
		SharedPreferences sp = mContext.getSharedPreferences("down",
				mContext.MODE_PRIVATE);
		boolean isDownLoaded = DownloadService.isDownLoaded(sdappInfo.getAppName()
				);
		boolean isDownLoadedRight = DownloadService.isDownLoaded(sdappInfoRight.getAppName()
				);
		int idx = Integer.parseInt(sdappInfo.getIdx());
		int idxRight = Integer.parseInt(sdappInfoRight.getIdx());
		isDownLoading = DownloadService.isDownLoading(idx);
		isDownLoadingRight = DownloadService.isDownLoading(idxRight);
		boolean isUpdate = false;
		boolean isUpdateRight = false;
		isUpdate = sdappInfo.isCanUpdate();
		isUpdateRight = sdappInfoRight.isCanUpdate();
		if (isUpdate) {
			v1.tvdown.setText("升级");
		} else {
			if (sdappInfo.isIspause()) {
				LogUtils.d("ture", appInfos.get(position).isIspause() + "");
				v1.tvdown.setText("暂停");
				// v1.progress_view.setVisibility(View.INVISIBLE);
				v1.tvdown.setVisibility(View.VISIBLE);
				// v1.progress_view.setProgress(DownloadService.getPrecent(idx));
				LogUtils.d("new", "我是下载中暂停"
						+ appInfos.get(position).getAppName());
				if (!isDownLoaded) {
					LogUtils.d("new", "我执行了下载中暂停"
							+ appInfos.get(position).getAppName());
					// v1.progress_view.setVisibility(View.INVISIBLE);
					v1.tvdown.setVisibility(View.VISIBLE);
				}
			} else {
				v1.tvdown.setText(DownloadService.getPrecent(idx)+"%");
				v1.tvdown.setVisibility(View.VISIBLE);
				LogUtils.d("new", "我是暂停中下载"
						+ appInfos.get(position).getAppName());
				if (!isDownLoaded) {
					LogUtils.d("new", "我执行了暂停中下载"
							+ appInfos.get(position).getAppName());
					// v1.progress_view.setVisibility(View.VISIBLE);
					//v1.tvdown.setVisibility(View.INVISIBLE);
					// v1.progress_view.setProgress(DownloadService
					// .getPrecent(idx));
					LogUtils.d("ture", isDownLoading + "isDown");
					LogUtils.d("newdowndown", "我变成下载中了"
							+ appInfos.get(position).getAppName());
				}

			}
			if (sdappInfo.isInstalled()) {
				v1.tvdown.setText("打开");
				// v1.progress_view.setVisibility(View.INVISIBLE);
				v1.tvdown.setVisibility(View.VISIBLE);
				// v1.progress_view.setProgress(100);
				/*
				 * Drawable mDrawableicon = mContext.getResources().getDrawable(
				 * R.drawable.action_type_software_update);
				 * v1.tvdown.setCompoundDrawablesWithIntrinsicBounds(null,
				 * mDrawableicon, null, null);
				 */
			} else if (sdappInfo.isDown()) {
				// v1.progress_view.setProgress(DownloadService.getPrecent(idx));
				LogUtils.d("ture", isDownLoading + "isDown");
				LogUtils.d("newdowndown", "我变成下载中了"
						+ appInfos.get(position).getAppName());
				/*
				 * //v1.tvdown.setText("下载中");
				 * v1.progress_view.setVisibility(View.VISIBLE);
				 * v1.tvdown.setVisibility(View.INVISIBLE);
				 * 
				 * Drawable mDrawableicon = mContext.getResources().getDrawable(
				 * R.drawable.downloading);
				 * v1.tvdown.setCompoundDrawablesWithIntrinsicBounds(null,
				 * mDrawableicon, null, null);
				 */
				v1.tvdown.setText(DownloadService.getPrecent(idx)+"%");
			} else if (isDownLoaded) {
				/*
				 * Drawable mDrawableicon = mContext.getResources().getDrawable(
				 * R.drawable.downloaded);
				 * v1.tvdown.setCompoundDrawablesWithIntrinsicBounds(null,
				 * mDrawableicon, null, null);
				 */
				v1.tvdown.setText("安装");
				// v1.progress_view.setProgress(100);
				// v1.progress_view.setVisibility(View.INVISIBLE);
				v1.tvdown.setVisibility(View.VISIBLE);
			} else if (!isDownLoading) {
				v1.tvdown.setText("下载");
				/*
				 * mDrawable = mContext.getResources().getDrawable(
				 * R.layout.mydown_buton);
				 * v1.tvdown.setCompoundDrawablesWithIntrinsicBounds(null,
				 * mDrawable, null, null);
				 */
				// 获取将要下载的文件名，如果本地存在该文件，则取出该文件

				// LogUtils.d("sa", tempFile.getAbsolutePath());
				// v1.progress_view.setVisibility(View.INVISIBLE);
				v1.tvdown.setVisibility(View.VISIBLE);
				long length = sp.getLong(tempFile.getAbsolutePath(), 0);
				// LogUtils.d("sa", length+"");
				if (length != 0
						&& DownloadService.isExist(sdappInfo.getAppName())) {
					LogUtils.d("test", "已经存在");
					v1.tvdown.setText("暂停");
					// v1.progress_view.setVisibility(View.INVISIBLE);
					v1.tvdown.setVisibility(View.VISIBLE);
					long count = sp.getLong(tempFile.getAbsolutePath()
							+ "precent", 0);
					// v1.progress_view.setProgress(count);
					//v1.tvdown.setText(count+"%");
				} else if (length != 0
						&& !DownloadService.isExist(appInfos.get(position)
								.getAppName())) {
					Editor edit = sp.edit();
					edit.remove(tempFile.getAbsolutePath());
					edit.commit();
				}
			}
		}
		/*
		 * v1.progress_view.setOnClickListener(new OnClickListener() {
		 * 
		 * @Override public void onClick(View v) {
		 * v1.tvdown.setVisibility(View.VISIBLE);
		 * v1.progress_view.setVisibility(View.INVISIBLE);
		 * v1.tvdown.setText("暂停"); appInfos.get(position).setDown(false);
		 * LogUtils.d("test", "暂停"); File tempFile =
		 * DownloadService.CreatFileName(appInfos.get( position).getAppName());
		 * Intent intent = new Intent();
		 * intent.setAction(tempFile.getAbsolutePath());
		 * mContext.sendBroadcast(intent); Intent downState = new Intent();
		 * downState.setAction(tempFile.getAbsolutePath() + "down");
		 * downState.putExtra("isPause", !appInfos.get(position) .isIspause());
		 * mContext.sendBroadcast(downState); LogUtils.d("pro", "我发出了下载中暂停广播");
		 * appInfos.get(position).setIspause(
		 * !appInfos.get(position).isIspause()); } });
		 */
		v1.tvdown.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (sdappInfo.isInstalled()) {
					AppUtils.launchApp(mContext, sdappInfo.getPackageName());
				} else if (DownloadService.isDownLoading(Integer
						.parseInt(sdappInfo.getIdx()))) {
					LogUtils.d("test", "暂停");
					File tempFile = DownloadService.CreatFileName(sdappInfo
							.getAppName());
					Intent intent = new Intent();
					intent.setAction(tempFile.getAbsolutePath());
					mContext.sendBroadcast(intent);
					Intent downState = new Intent();
					downState.setAction(tempFile.getAbsolutePath() + "down");
					downState.putExtra("isPause", !sdappInfo.isIspause());
					mContext.sendBroadcast(downState);
					LogUtils.d("pro", "我发出了暂停中下载广播");
					if (!sdappInfo.isIspause()) {
						v1.tvdown.setText("暂停");
						sdappInfo.setDown(false);

					} else {
						v1.tvdown.setText("下载中");
						sdappInfo.setDown(true);
						// v1.progress_view.setVisibility(View.VISIBLE);
						//v1.tvdown.setVisibility(View.INVISIBLE);
						v1.tvdown.setText(DownloadService.getPrecent(Integer.parseInt(sdappInfo.getIdx()))+"%");

					}
					LogUtils.d("down", appInfos.get(position).isDown() + "");
					LogUtils.d("test", appInfos.get(position).isIspause() + "1");
					sdappInfo.setIspause(!sdappInfo.isIspause());
					LogUtils.d("test", appInfos.get(position).isIspause() + "2");
				} else if (DownloadService.isDownLoaded(sdappInfo.getAppName())) {
					// 已经下载
					DownloadService.Instanll(sdappInfo.getAppName(), mContext);
				} else if (!sdappInfo.isInstalled()) {
					/*
					 * Log.e("tag", "appname = " +
					 * appInfos.get(position).getAppName());
					 */
					SharedPreferences sp = mContext.getSharedPreferences(
							"down", mContext.MODE_PRIVATE);
					File tempFile = new File(Environment
							.getExternalStorageDirectory(), "/market/"
							+ sdappInfo.getAppName() + ".apk");
					Intent downState = new Intent();
					downState.setAction(tempFile.getAbsolutePath() + "down");
					downState.putExtra("isPause", sdappInfo.isIspause());
					mContext.sendBroadcast(downState);
					LogUtils.d("pro", "我发出了暂停中下载广播safdasfasf");
					long length = sp.getLong(tempFile.getAbsolutePath(), 0);
					/*
					 * DownloadService.downNewFile(appInfos.get(position)
					 * .getAppUrl(), Integer.parseInt(appInfos.get(
					 * position).getIdx()), appInfos.get(position)
					 * .getAppName(),length,0);
					 */
					DownloadService.downNewFile(sdappInfo, length, 0, null);
					sdappInfo.setDown(true);
					Intent intent = new Intent();
					intent.setAction(MarketApplication.PRECENT);
					mContext.sendBroadcast(intent);
					LogUtils.d("pro", "我发出了暂停中下载广播but");
					Toast.makeText(mContext,
							sdappInfo.getAppName() + " 开始下载...",
							Toast.LENGTH_SHORT).show();
					// v1.progress_view.setVisibility(View.VISIBLE);
					//v1.tvdown.setVisibility(View.INVISIBLE);
				
				}

			}
		});
		if (isUpdate) {
			v1.tvdown.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					File tempFile = new File(Environment
							.getExternalStorageDirectory(), "/market/"
							+ sdappInfo.getAppName() + ".apk");
					List<AppInfo> down_temp = new ArrayList<AppInfo>();
					if (tempFile.exists()) {
						tempFile.delete();
					}
					DownloadService.downNewFile(sdappInfo, 0, 0, null);
					// downList.add(sdappInfo);
					notifyDataSetChanged();
					sdappInfo.setDown(true);
					sdappInfo.setIspause(false);
					Intent intent = new Intent();
					Intent downState = new Intent();

					downState.setAction(tempFile.getAbsolutePath() + "down");
					downState.putExtra("isPause", sdappInfo.isIspause());
					mContext.sendBroadcast(downState);
					intent.setAction(MarketApplication.PRECENT);
					mContext.sendBroadcast(intent);
					Toast.makeText(mContext,
							sdappInfo.getAppName() + " 开始下载...",
							Toast.LENGTH_SHORT).show();

				}
			});
		}
		if (isUpdateRight) {
			v1.tvdownRight.setText("升级");
		} else {
			if (sdappInfoRight.isIspause()) {
				LogUtils.d("ture", appInfos.get(position).isIspause() + "");
				v1.tvdownRight.setText("暂停");
				// v1.progress_view.setVisibility(View.INVISIBLE);
				v1.tvdownRight.setVisibility(View.VISIBLE);
				// v1.progress_view.setProgress(DownloadService.getPrecent(idx));
				if (!isDownLoadedRight) {
					// v1.progress_view.setVisibility(View.INVISIBLE);
					v1.tvdownRight.setVisibility(View.VISIBLE);
				}
			} else {
				//v1.tvdownRight.setText("下载中");
				LogUtils.d("new", "我是暂停中下载"
						+ appInfos.get(position).getAppName());
				if (!isDownLoadedRight) {
					LogUtils.d("new", "我执行了暂停中下载"
							+ appInfos.get(position).getAppName());
					// v1.progress_view.setVisibility(View.VISIBLE);
					//v1.tvdownRight.setVisibility(View.INVISIBLE);
					// v1.progress_view.setProgress(DownloadService
					// .getPrecent(idx));
					LogUtils.d("ture", isDownLoading + "isDown");
					LogUtils.d("newdowndown", "我变成下载中了"
							+ appInfos.get(position).getAppName());
				}

			}
			if (sdappInfoRight.isInstalled()) {
				v1.tvdownRight.setText("打开");
				// v1.progress_view.setVisibility(View.INVISIBLE);
				v1.tvdownRight.setVisibility(View.VISIBLE);
				// v1.progress_view.setProgress(100);
				/*
				 * Drawable mDrawableicon = mContext.getResources().getDrawable(
				 * R.drawable.action_type_software_update);
				 * v1.tvdown.setCompoundDrawablesWithIntrinsicBounds(null,
				 * mDrawableicon, null, null);
				 */
			} else if (sdappInfoRight.isDown()) {
				// v1.progress_view.setProgress(DownloadService.getPrecent(idx));
				LogUtils.d("ture", isDownLoading + "isDown");
				LogUtils.d("newdowndown", "我变成下载中了"
						+ appInfos.get(position).getAppName());
				v1.tvdownRight.setText(DownloadService.getPrecent(idxRight)+"%");
				/*
				 * //v1.tvdown.setText("下载中");
				 * v1.progress_view.setVisibility(View.VISIBLE);
				 * v1.tvdown.setVisibility(View.INVISIBLE);
				 * 
				 * Drawable mDrawableicon = mContext.getResources().getDrawable(
				 * R.drawable.downloading);
				 * v1.tvdown.setCompoundDrawablesWithIntrinsicBounds(null,
				 * mDrawableicon, null, null);
				 */

			} else if (isDownLoadedRight) {
				/*
				 * Drawable mDrawableicon = mContext.getResources().getDrawable(
				 * R.drawable.downloaded);
				 * v1.tvdown.setCompoundDrawablesWithIntrinsicBounds(null,
				 * mDrawableicon, null, null);
				 */
				v1.tvdownRight.setText("安装");
				// v1.progress_view.setProgress(100);
				// v1.progress_view.setVisibility(View.INVISIBLE);
				v1.tvdownRight.setVisibility(View.VISIBLE);
			} else if (!isDownLoadingRight) {
				v1.tvdownRight.setText("下载");
				/*
				 * mDrawable = mContext.getResources().getDrawable(
				 * R.layout.mydown_buton);
				 * v1.tvdown.setCompoundDrawablesWithIntrinsicBounds(null,
				 * mDrawable, null, null);
				 */
				// 获取将要下载的文件名，如果本地存在该文件，则取出该文件

				// LogUtils.d("sa", tempFile.getAbsolutePath());
				// v1.progress_view.setVisibility(View.INVISIBLE);
				v1.tvdownRight.setVisibility(View.VISIBLE);
				long length = sp.getLong(tempFileRight.getAbsolutePath(), 0);
				// LogUtils.d("sa", length+"");
				if (length != 0
						&& DownloadService.isExist(sdappInfoRight.getAppName())) {
					LogUtils.d("test", "已经存在");
					v1.tvdownRight.setText("暂停");
					// v1.progress_view.setVisibility(View.INVISIBLE);
					v1.tvdownRight.setVisibility(View.VISIBLE);
					long count = sp.getLong(tempFileRight.getAbsolutePath()
							+ "precent", 0);
					// v1.progress_view.setProgress(count);
					//v1.tvdownRight.setText(count+"%");
				} else if (length != 0
						&& !DownloadService
								.isExist(sdappInfoRight.getAppName())) {
					Editor edit = sp.edit();
					edit.remove(tempFileRight.getAbsolutePath());
					edit.commit();
				}
			}
		}
		/*
		 * v1.progress_view.setOnClickListener(new OnClickListener() {
		 * 
		 * @Override public void onClick(View v) {
		 * v1.tvdown.setVisibility(View.VISIBLE);
		 * v1.progress_view.setVisibility(View.INVISIBLE);
		 * v1.tvdown.setText("暂停"); appInfos.get(position).setDown(false);
		 * LogUtils.d("test", "暂停"); File tempFile =
		 * DownloadService.CreatFileName(appInfos.get( position).getAppName());
		 * Intent intent = new Intent();
		 * intent.setAction(tempFile.getAbsolutePath());
		 * mContext.sendBroadcast(intent); Intent downState = new Intent();
		 * downState.setAction(tempFile.getAbsolutePath() + "down");
		 * downState.putExtra("isPause", !appInfos.get(position) .isIspause());
		 * mContext.sendBroadcast(downState); LogUtils.d("pro", "我发出了下载中暂停广播");
		 * appInfos.get(position).setIspause(
		 * !appInfos.get(position).isIspause()); } });
		 */
		v1.tvdownRight.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (sdappInfoRight.isInstalled()) {
					AppUtils.launchApp(mContext,
							sdappInfoRight.getPackageName());
				} else if (DownloadService.isDownLoading(Integer
						.parseInt(sdappInfoRight.getIdx()))) {
					LogUtils.d("test", "暂停");
					File tempFile = DownloadService
							.CreatFileName(sdappInfoRight.getAppName());
					Intent intent = new Intent();
					intent.setAction(tempFile.getAbsolutePath());
					mContext.sendBroadcast(intent);
					Intent downState = new Intent();
					downState.setAction(tempFile.getAbsolutePath() + "down");
					downState.putExtra("isPause", !sdappInfoRight.isIspause());
					mContext.sendBroadcast(downState);
					LogUtils.d("pro", "我发出了暂停中下载广播");
					if (!sdappInfoRight.isIspause()) {
						v1.tvdownRight.setText("暂停");
						sdappInfoRight.setDown(false);

					} else {
						//v1.tvdownRight.setText("下载中");
						sdappInfoRight.setDown(true);
						// v1.progress_view.setVisibility(View.VISIBLE);
						//v1.tvdownRight.setVisibility(View.INVISIBLE);
					//	v1.tvdownRight.setText(DownloadService.getPrecent(idx)+"%");
						v1.tvdownRight.setText(DownloadService.getPrecent(Integer.parseInt(sdappInfoRight.getIdx()))+"%");

					}
					sdappInfoRight.setIspause(!sdappInfoRight.isIspause());
				} else if (DownloadService.isDownLoaded(sdappInfoRight.getAppName()
						)) {
					// 已经下载
					DownloadService.Instanll(sdappInfoRight.getAppName(),
							mContext);
				} else if (!sdappInfoRight.isInstalled()) {
					/*
					 * Log.e("tag", "appname = " +
					 * appInfos.get(position).getAppName());
					 */
					SharedPreferences sp = mContext.getSharedPreferences(
							"down", mContext.MODE_PRIVATE);
					File tempFile = new File(Environment
							.getExternalStorageDirectory(), "/market/"
							+ sdappInfoRight.getAppName() + ".apk");
					Intent downState = new Intent();
					downState.setAction(tempFile.getAbsolutePath() + "down");
					downState.putExtra("isPause", sdappInfoRight.isIspause());
					mContext.sendBroadcast(downState);
					LogUtils.d("pro", "我发出了暂停中下载广播safdasfasf");
					long length = sp.getLong(tempFile.getAbsolutePath(), 0);
					/*
					 * DownloadService.downNewFile(appInfos.get(position)
					 * .getAppUrl(), Integer.parseInt(appInfos.get(
					 * position).getIdx()), appInfos.get(position)
					 * .getAppName(),length,0);
					 */
					DownloadService
							.downNewFile(sdappInfoRight, length, 0, null);
					sdappInfoRight.setDown(true);
					Intent intent = new Intent();
					intent.setAction(MarketApplication.PRECENT);
					mContext.sendBroadcast(intent);
					LogUtils.d("pro", "我发出了暂停中下载广播but");
					Toast.makeText(mContext,
							sdappInfoRight.getAppName() + " 开始下载...",
							Toast.LENGTH_SHORT).show();
					// v1.progress_view.setVisibility(View.VISIBLE);
					//v1.tvdown.setVisibility(View.INVISIBLE);
				}

			}
		});
		if (isUpdateRight) {
			v1.tvdownRight.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					File tempFile = new File(Environment
							.getExternalStorageDirectory(), "/market/"
							+ sdappInfoRight.getAppName() + ".apk");
					List<AppInfo> down_temp = new ArrayList<AppInfo>();
					if (tempFile.exists()) {
						tempFile.delete();
					}
					DownloadService.downNewFile(sdappInfoRight, 0, 0, null);
					// downList.add(sdappInfo);
					notifyDataSetChanged();
					sdappInfoRight.setDown(true);
					sdappInfoRight.setIspause(false);
					Intent intent = new Intent();
					Intent downState = new Intent();

					downState.setAction(tempFile.getAbsolutePath() + "down");
					downState.putExtra("isPause", sdappInfoRight.isIspause());
					mContext.sendBroadcast(downState);
					intent.setAction(MarketApplication.PRECENT);
					mContext.sendBroadcast(intent);
					Toast.makeText(mContext,
							sdappInfoRight.getAppName() + " 开始下载...",
							Toast.LENGTH_SHORT).show();

				}
			});
		}
	}

	private static class ViewHolder extends BaseHolder {
	}

	private static class ViewHolder2 extends BaseHolder {
	}

	private static class ViewHolder3 extends BaseHolder {

	}

	static class BaseHolder {
		ImageView icon;
		TextView name;
		TextView size;
		TextView tvdown;
		ImageView iconRight;
		TextView nameRight;
		TextView sizeRight;
		TextView tvdownRight;
		// ProgressView progress_view;
		RelativeLayout LeftBar;
		RelativeLayout RightBar;
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

	private void setImage(ImageView iv, int imageId) {
		int height = wm.getDefaultDisplay().getHeight();
		int width = wm.getDefaultDisplay().getWidth();
		// 通过Options获得图片的高宽
		Options opts = new Options();
		// 设置 不去真正的解析位图 不把他加载到内存 只是获取这个图片的宽高信息
		opts.inJustDecodeBounds = true;
		BitmapFactory.decodeResource(mContext.getResources(), imageId);
		int bitmapWidth = opts.outWidth;
		int bitmapHeight = opts.outHeight;
		// 计算缩放比例
		int scalex = bitmapWidth / width;
		int scaley = bitmapHeight / height;
		// 计算缩放的方式
		if (scalex > scaley) {
			opts.inSampleSize = scalex;
		} else {
			opts.inSampleSize = scaley;
		}
		// 设置真正的解析图片
		opts.inJustDecodeBounds = false;
		Bitmap bitmap = BitmapFactory.decodeResource(mContext.getResources(),
				imageId, opts);
		iv.setImageBitmap(bitmap);
	}

	private void setImagePosition(int resId, ImageView banner) {
		Bitmap bm = BitmapFactory
				.decodeResource(mContext.getResources(), resId);
		// Bitmap newbitmap =
		// Bitmap.createBitmap((width-gapPy),(int)((width-gapPy)/5.34),
		// bm.getConfig());
		// getNewBitMapPos(bm, newbitmap);
		// banner.setImageBitmap(newbitmap);
		banner.setImageResource(resId);
	}

	private void getNewBitMapPos(Bitmap bm, Bitmap newbitmap) {
		Paint paint = new Paint();
		Canvas canvas = new Canvas(newbitmap);
		Matrix matrix = new Matrix();
		double newWidth = 1.00;
		double newHeight = 2.6;
		int gapPx = convertDipOrPx(mContext, 5);
		// matrix.setRotate(30, bm.getWidth()/2, bm.getHeight()/2);
		float scaleWidth = (float) ((width - gapPx)) / bm.getWidth();
		float scaleHeight = (float) (newbitmap.getHeight()) / bm.getHeight();
		LogUtils.d("scaleWidth+scaleWidth", scaleWidth + ":" + scaleWidth
				+ "++" + width + "PPP" + 2 / 3);
		matrix.postScale(scaleWidth, scaleHeight);
		// 使用画布将原图片，矩阵，画笔进行新图片的绘画
		canvas.drawBitmap(bm, matrix, paint);
	}

	public static int convertDipOrPx(Context context, int dip) {
		float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dip * scale + 0.5f * (dip >= 0 ? 1 : -1));
	}
}
