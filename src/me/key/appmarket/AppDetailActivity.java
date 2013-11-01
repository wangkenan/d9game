package me.key.appmarket;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;

import me.key.appmarket.MainActivity.PrecentReceiver;
import me.key.appmarket.RankActivity.MyInstalledReceiver;
import me.key.appmarket.adapter.CommentAdapter;
import me.key.appmarket.network.AppDetailRequest;
import me.key.appmarket.network.AppDetailResponse;
import me.key.appmarket.network.HttpRequest.OnResponseListener;
import me.key.appmarket.network.HttpResponse;
import me.key.appmarket.tool.DownloadService;
import me.key.appmarket.tool.ToolHelper;
import me.key.appmarket.utils.AppInfo;
import me.key.appmarket.utils.AppUtils;
import me.key.appmarket.utils.CommentInfo;
import me.key.appmarket.utils.Global;
import me.key.appmarket.utils.LogUtils;
import me.key.appmarket.widgets.CustomScrollView;
import me.key.appmarket.widgets.EllipsizingTextView;

import org.json.JSONArray;
import org.json.JSONObject;

import com.market.d9game.R;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.umeng.analytics.MobclickAgent;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 应用详情页
 */
public class AppDetailActivity extends Activity implements OnClickListener {

	private static final String TAG = "AppDetailActivity";

	private ImageView backIcon;
	private ImageView appIcon;
	private TextView appName;
	private TextView appDeveloper;
	private TextView appDownloadCounts;

	private LinearLayout appImgGallery;

	private TextView appVersion;
	private TextView appSize;
	private TextView appUpdateTime;

	private EllipsizingTextView appDes;
	private ImageButton appDesExpand;
	private Button appDownload;

	private CustomScrollView scrollView;

	private String idx;
	private String name;
	private boolean isInstalled = false;
	private boolean isDowned = false;
	private boolean isDowning = false;
	private ProgressDialog myDialog;
	private TextView t1, t2;
	private AppInfo appInfo;
	private String appid;
	private ArrayList<CommentInfo> commentList = new ArrayList<CommentInfo>();
	private ListView commentListView;
	private CommentAdapter mCommentAdapter;
	// 是否是下载状态
		private boolean isDownLoading;
	private long count;
	// 设置ImageLoade初始化信息
	private DisplayImageOptions options = new DisplayImageOptions.Builder()
			.showImageForEmptyUri(R.drawable.tempicon)
			.showStubImage(R.drawable.tempicon).resetViewBeforeLoading(false)
			.delayBeforeLoading(50).cacheInMemory(false).cacheOnDisc(true)
			.imageScaleType(ImageScaleType.IN_SAMPLE_INT)
			.bitmapConfig(Bitmap.Config.RGB_565)
			.build();
	File cache = new File(Environment.getExternalStorageDirectory(),
			"detail_cache");
	private SharedPreferences sp;
	private File tempFile;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (!cache.exists()) {
			cache.mkdirs();
		}
		
		setContentView(R.layout.app_detail_main);
		MarketApplication.getInstance().getAppLication().add(this);
		backIcon = (ImageView) findViewById(R.id.back_icon);
		appIcon = (ImageView) findViewById(R.id.app_icon);
		appName = (TextView) findViewById(R.id.app_name);
		appDeveloper = (TextView) findViewById(R.id.app_developer);
		appDownloadCounts = (TextView) findViewById(R.id.app_download_counts);

		appImgGallery = (LinearLayout) findViewById(R.id.app_des_pic);

		appVersion = (TextView) findViewById(R.id.app_version);
		appSize = (TextView) findViewById(R.id.app_size);
		appUpdateTime = (TextView) findViewById(R.id.app_update_time);

		appDes = (EllipsizingTextView) findViewById(R.id.app_description);
		appDesExpand = (ImageButton) findViewById(R.id.app_des_expand);
		appDownload = (Button) findViewById(R.id.app_download);

		scrollView = (CustomScrollView) findViewById(R.id.scroll_app);

		appDownloadCounts.setText(getString(
				R.string.app_detail_download_counts, ""));
		appVersion.setText(getString(R.string.app_detail_version, ""));
		appUpdateTime.setText(getString(R.string.app_detail_update_time, ""));
		appSize.setText(getString(R.string.app_detail_size, ""));

		appDesExpand.setOnClickListener(this);
		//appDownload.setOnClickListener(this);
		backIcon.setOnClickListener(this);
		appIcon.setOnClickListener(this);
		MyInstalledReceiver installedReceiver = new MyInstalledReceiver();
		IntentFilter filter = new IntentFilter();

		filter.addAction("android.intent.action.PACKAGE_ADDED");
		filter.addDataScheme("package");
		this.registerReceiver(installedReceiver, filter);
		appid = getIntent().getStringExtra("appid");
		appInfo = (AppInfo) getIntent().getSerializableExtra("appinfo");
		if (appid != null) {
			myDialog = ProgressDialog.show(this, "正在连接服务器..", "连接中,请稍后..",
					true, true);
			autoLoad(appid);
		}
		if(appInfo == null) {
			appInfo = new AppInfo();
		}
		sp =getSharedPreferences("down",
				MODE_PRIVATE);
	tempFile = new File(Environment.getExternalStorageDirectory(),
			"/market/" + appInfo.getAppName() + ".apk");
		t1 = (TextView) findViewById(R.id.text1);
		t2 = (TextView) findViewById(R.id.text2);
		t1.setSelected(true);
		t1.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				t1.setSelected(true);
				t2.setSelected(false);

				commentListView.setVisibility(View.GONE);
				scrollView.setVisibility(View.VISIBLE);
			}
		});
		t2.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				t2.setSelected(true);
				t1.setSelected(false);

				scrollView.setVisibility(View.GONE);
				commentListView.setVisibility(View.VISIBLE);
			}
		});

		commentListView = (ListView) findViewById(R.id.list_comment);
		mCommentAdapter = new CommentAdapter(commentList,
				AppDetailActivity.this, cache);
		commentListView.setAdapter(mCommentAdapter);

		// scrollView.setVisibility(View.GONE);
		// commentListView.setVisibility(View.VISIBLE);
		new Thread(runCommentData).start();
	}

	private void autoLoad(String appid) {
		new AppDetailRequest(appid).execute(new OnResponseListener() {

			@Override
			public void onGetResponse(HttpResponse resp) {
				final AppDetailResponse response = (AppDetailResponse) resp;
				if (myDialog != null) {
					myDialog.dismiss();
				}
				if (response != null) {
					idx = response.getIdx();
					name = response.getAppName();

					isInstalled = AppUtils.isInstalled(name);
					// 是否已经下载完
					isDowned = DownloadService.isDownLoaded(name);
					// 是否正在下载
					isDowning = DownloadService.isDownLoading(Integer
							.parseInt(idx));

					appDesExpand.setSelected(true);

					if (response.getAppName() != null) {
						appName.setText(response.getAppName());
						appInfo.setAppName(response.getAppName());
						
					}
					if (response.getAppDownloadCounts() != null) {
						appDownloadCounts.setText(getString(
								R.string.app_detail_download_counts,
								response.getAppDownloadCounts()));
						appInfo.setAppDownCount(response.getAppDownloadCounts());
					}
					if (response.getAppVersion() != null) {
						appVersion.setText(getString(
								R.string.app_detail_version,
								response.getAppVersion()));
						appInfo.setVersion(response.getAppVersion());
					}
					if (response.getAppUpdateTime() != null) {
						appUpdateTime.setText(getString(
								R.string.app_detail_update_time,
								response.getAppUpdateTime()));
					}
					if (response.getAppSize() != null) {
						appSize.setText(getString(R.string.app_detail_size,
								formetFileSize(Long.parseLong(response
										.getAppSize()))));
						appInfo.setAppSize(response.getAppSize());
					}
					if (response.getAppUrl() != null) {
						appDownload.setTag(response.getAppUrl());
						appInfo.setAppUrl(response.getAppUrl());
						
					}
					if (response.getAppDes() != null) {
						appDes.setText(response.getAppDes());
						appInfo.setAppDescri(response.getAppDes());
					}
					if (response.getAppIconUrl() != null) {
						asyncloadImage(appIcon,
								Global.MAIN_URL + response.getAppIconUrl());
						if(appInfo == null) {
						appInfo.setIconUrl(response.getAppImgUrl()[0]);
						}
					} if(response.getIdx() != null) {
						appInfo.setIdx(response.getIdx());
					} if(response.getAppPackageName() !=null) {
						if(appInfo == null) {
							appInfo.setId(response.getAppPackageName());
							}
					}
					File tempFile = new File(Environment.getExternalStorageDirectory(),
							"/market/" + appInfo.getAppName() + ".apk");
					SharedPreferences sp = getSharedPreferences("down",
							MODE_PRIVATE);
					boolean isDownLoaded = DownloadService.isDownLoaded(appInfo.getAppName());
					int idx = Integer.parseInt(appInfo.getIdx());
					isDownLoading = DownloadService.isDownLoading(idx);
				/*	if (isInstalled) {
						appDownload.setText("打开");
					} else if (isDowning) {
						appDownload.setText("下载中");
					} else if (isDowned) {
						appDownload.setText("安装");
					} else {
						appDownload.setText("下载");
					}*/
					setDownState(0);
					appDownload.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.btn_icon_download_disable), null, null, null);
				/*	appDownload.setText("下载("+	formetFileSize(Long.parseLong(response
							.getAppSize()))+")");*/
					if (response.getAppImgUrl() != null) {
						for (int i = 0; i < response.getAppImgUrl().length; i++) {
							ImageView iv = new ImageView(AppDetailActivity.this);
							iv.setRotation(-90.0f);
							iv.setScaleType(ScaleType.FIT_XY);
							//asyncloadImage(iv, response.getAppImgUrl()[i]);
							ImageLoader.getInstance().displayImage(response.getAppImgUrl()[i], iv, options);
							// LayoutParams params = new LayoutParams(200,
							// LayoutParams.MATCH_PARENT);
							MarginLayoutParams params = new MarginLayoutParams(
									LayoutParams.WRAP_CONTENT, 200);
							// params.setMargins(0, 0, 20, 0);
							iv.setPadding(0, 0, 20, 0);
							iv.setLayoutParams(params);
							appImgGallery.addView(iv);
						}
					}
				} else {
					Toast.makeText(AppDetailActivity.this, "获取失败",
							Toast.LENGTH_SHORT).show();
				}
			}
		});
	}

	/**
	 * 下载图片，并显示
	 * 
	 * @param iv_header
	 * @param path
	 */
	private void asyncloadImage(ImageView iv_header, String path) {
		Log.d(TAG, "download pic url: " + path);
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
				//iv_header.setImageURI(result);
				ImageLoader.getInstance().displayImage(result.toString(), iv_header, options);
			}
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.app_des_expand:
			if (appDesExpand.isSelected()) {
				appDesExpand.setSelected(false);
				appDes.setMaxLines(100);
			} else {
				appDesExpand.setSelected(true);
				appDes.setMaxLines(5);
			}
			break;
/*		case R.id.app_download:
			if (isInstalled) {
				AppUtils.launchApp(AppDetailActivity.this, name);
			} else if (isDowning) {

			} else if (isDowned) {
				DownloadService.Instanll(name, AppDetailActivity.this);
			} else {
				if (appDownload.getTag() != null) {
					DownloadService.downNewFile((String) appDownload.getTag(),
							idx != null ? Integer.parseInt(idx) : 0, name,0,0);
					Toast.makeText(AppDetailActivity.this, name + " 开始下载...",
							Toast.LENGTH_SHORT).show();
					Log.d(TAG, "download apk name=" + name + "  idx=" + idx
							+ " url=" + appDownload.getTag());
					DownloadService.downNewFile(appInfo, 0, 0, null);
				}
			}
			break;*/
		case R.id.app_icon:// 返回
		case R.id.back_icon:
			AppDetailActivity.this.finish();
			break;
		default:
			break;
		}
	}

	private String formetFileSize(long fileS) {
		DecimalFormat df = new DecimalFormat("#.00");
		String fileSizeString = "";
		if (fileS < 1024) {
			fileSizeString = df.format((double) fileS) + "B";
		} else if (fileS < 1048576) {
			fileSizeString = df.format((double) fileS / 1024) + "K";
		} else if (fileS < 1073741824) {
			fileSizeString = df.format((double) fileS / 1048576) + "M";
		} else {
			fileSizeString = df.format((double) fileS / 1073741824) + "G";
		}
		return fileSizeString;
	}

	Runnable runCommentData = new Runnable() {
		@Override
		public void run() {
			String str = ToolHelper.donwLoadToString(Global.MAIN_URL
					+ Global.APPCOMMENT + "?appid=" + appid);
			Log.e("tag", "result =" + str);
			if (str.equals("null")) {
				mHandler.sendEmptyMessage(Global.DOWN_DATA_HOME_SUCCESSFULL);
			} else if (str.equals("-1")) {
				mHandler.sendEmptyMessage(Global.DOWN_DATA_HOME_FAILLY);
			} else {
				Log.e("tag", "--------------1-------------");
				ParseCommentJson(str);
			}
		}
	};

	Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			Log.e("tag", "--------------4--------");
			switch (msg.what) {
			case Global.DOWN_DATA_HOME_FAILLY: {
			}
				break;
			case Global.DOWN_DATA_HOME_SUCCESSFULL: {
				mCommentAdapter.notifyDataSetChanged();
			}
			default:
				break;
			}
			super.handleMessage(msg);
		}
	};

	private void ParseCommentJson(String str) {
		try {
			JSONArray jsonArray = new JSONArray(str);
			int len = jsonArray.length();
			for (int i = 0; i < len; i++) {
				JSONObject jsonObject = jsonArray.getJSONObject(i);
				String content = jsonObject.getString("content");
				String send_time = jsonObject.getString("send_time");
				String user_name = jsonObject.getString("user_name");
				String score = jsonObject.getString("score");
				
				CommentInfo mCommentInfo = new CommentInfo(content, send_time,
						user_name, score);

				commentList.add(mCommentInfo);
			}
			mHandler.sendEmptyMessage(Global.DOWN_DATA_HOME_SUCCESSFULL);
		} catch (Exception ex) {
			Log.e("tag", "ParseBannerJson error = " + ex.getMessage());
			// homeDataHandler.sendEmptyMessage(Global.DOWN_DATA_HOME_FAILLY);
		}
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		registerPrecent();
		MobclickAgent.onResume(this);
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		unregisterPrecent();
		MobclickAgent.onPause(this);
	}

	PrecentReceiver mPrecentReceiver;

	private void registerPrecent() {
		mPrecentReceiver = new PrecentReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(MarketApplication.PRECENT);
		this.registerReceiver(mPrecentReceiver, filter);
	}

	private void unregisterPrecent() {
		if (mPrecentReceiver != null) {
			this.unregisterReceiver(mPrecentReceiver);
		}
	}

	class PrecentReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			count = sp.getLong(tempFile.getAbsolutePath() + "precent",
					0);
			if (intent.getAction().equals(MarketApplication.PRECENT)) {
				if(appInfo.isIspause()) {
					appDownload.setText("暂停"+"("+count+"%)");
				} else {
					appDownload.setText("下载中"+"("+count+"%)");
				}if(DownloadService.isDownLoaded(appInfo.getAppName())){
					appDownload.setText("安装");
				}
			/*	if(idx != null) 
					isDowning = DownloadService
							.isDownLoading(Integer.parseInt(idx));*/
		/*		isInstalled = AppUtils.isInstalled(name);
				// 是否已经下载完
				isDowned = DownloadService.isDownLoaded(name);
				// 是否正在下载
				if(idx != null) {
				isDowning = DownloadService
						.isDownLoading(Integer.parseInt(idx));

				if (isInstalled) {
					appDownload.setText("打开");
				} else if (isDowning) {
					appDownload.setText("下载中");
			} else if (isDowned) {
					appDownload.setText("安装");
				} else {
					appDownload.setText("下载");
				}*/
			}
		}
	}
	public void setDownState(final int position) {
		
		Drawable mDrawable;
		//v1.progress_view.setProgress(0);
		// v1.progress_view.setVisibility(View.VISIBLE);
		final File tempFile = new File(Environment
				.getExternalStorageDirectory(), "/market/"
				+ appInfo.getAppName() + ".apk");
		count = sp.getLong(tempFile.getAbsolutePath() + "precent",
				0);
		
		boolean isDownLoaded = DownloadService.isDownLoaded(appInfo.getAppName());
		int idx = Integer.parseInt(appInfo.getIdx());
		isDownLoading = DownloadService.isDownLoading(idx);
		if (appInfo.isIspause()) {
			LogUtils.d("ture", appInfo.isIspause() + "");
			appDownload.setText("暂停"+"("+count+"%)");
		//	v1.progress_view.setProgress(DownloadService.getPrecent(idx));
			LogUtils.d("new", "我是下载中暂停"+appInfo.getAppName());
			if (!isDownLoaded) {
				LogUtils.d("new", "我执行了下载中暂停"+appInfo.getAppName());
				//v1.progress_view.setVisibility(View.INVISIBLE);
				//v1.tvdown.setVisibility(View.VISIBLE);
			}
		} else {
			appDownload.setText("下载中"+"("+count+"%)");
			LogUtils.d("new", "我是暂停中下载"+appInfo.getAppName());
			if (!isDownLoaded) {
				LogUtils.d("new", "我执行了暂停中下载"+appInfo.getAppName());
			//	v1.progress_view.setVisibility(View.VISIBLE);
				//v1.tvdown.setVisibility(View.INVISIBLE);
				//v1.progress_view.setProgress(DownloadService.getPrecent(idx));
				LogUtils.d("ture", isDownLoading + "isDown");
				LogUtils.d("newdowndown", "我变成下载中了"+appInfo.getAppName());
			}
		}
		if (appInfo.isInstalled()) {
			appDownload.setText("打开");
		/*	v1.progress_view.setVisibility(View.INVISIBLE);
			v1.tvdown.setVisibility(View.VISIBLE);
			v1.progress_view.setProgress(100);*/
			/*
			 * Drawable mDrawableicon = mContext.getResources().getDrawable(
			 * R.drawable.action_type_software_update);
			 * v1.tvdown.setCompoundDrawablesWithIntrinsicBounds(null,
			 * mDrawableicon, null, null);
			 */
		} else if (appInfo.isDown()) {
			//v1.progress_view.setProgress(DownloadService.getPrecent(idx));
			LogUtils.d("ture", isDownLoading + "isDown");
			LogUtils.d("newdowndown", "我变成下载中了"+appInfo.getAppName());
		/*	//v1.tvdown.setText("下载中");
			v1.progress_view.setVisibility(View.VISIBLE);
			v1.tvdown.setVisibility(View.INVISIBLE);
			
			  Drawable mDrawableicon = mContext.getResources().getDrawable(
			  R.drawable.downloading);
			  v1.tvdown.setCompoundDrawablesWithIntrinsicBounds(null,
			  mDrawableicon, null, null);*/
			 
		} else if (isDownLoaded) {
			/*
			 * Drawable mDrawableicon = mContext.getResources().getDrawable(
			 * R.drawable.downloaded);
			 * v1.tvdown.setCompoundDrawablesWithIntrinsicBounds(null,
			 * mDrawableicon, null, null);
			 */
			appDownload.setText("安装");
		/*	v1.progress_view.setProgress(100);
			v1.progress_view.setVisibility(View.INVISIBLE);
			v1.tvdown.setVisibility(View.VISIBLE);*/
		} else if (!isDownLoading) {
			appDownload.setText("下载");
			/*
			 * mDrawable = mContext.getResources().getDrawable(
			 * R.layout.mydown_buton);
			 * v1.tvdown.setCompoundDrawablesWithIntrinsicBounds(null,
			 * mDrawable, null, null);
			 */
			// 获取将要下载的文件名，如果本地存在该文件，则取出该文件

			// LogUtils.d("sa", tempFile.getAbsolutePath());
		/*	v1.progress_view.setVisibility(View.INVISIBLE);
			v1.tvdown.setVisibility(View.VISIBLE);*/
			long length = sp.getLong(tempFile.getAbsolutePath(), 0);
			// LogUtils.d("sa", length+"");
			if (length != 0
					&& DownloadService.isExist(appInfo
							.getAppName())) {
				LogUtils.d("test", "已经存在");
				appDownload.setText("暂停"+"("+count+"%)");

				
				//v1.progress_view.setProgress(count);
			} else if (length != 0
					&& !DownloadService.isExist(appInfo
							.getAppName())) {
				Editor edit = sp.edit();
				edit.remove(tempFile.getAbsolutePath());
				edit.commit();
			}
		}
	/*	v1.progress_view.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				v1.tvdown.setVisibility(View.VISIBLE);
				v1.progress_view.setVisibility(View.INVISIBLE);
				v1.tvdown.setText("暂停");
				appInfo.setDown(false);
				LogUtils.d("test", "暂停");
				File tempFile = DownloadService.CreatFileName(appInfo.getAppName());
				Intent intent = new Intent();
				intent.setAction(tempFile.getAbsolutePath());
				sendBroadcast(intent);
				Intent downState = new Intent();
				downState.setAction(tempFile.getAbsolutePath() + "down");
				downState.putExtra("isPause", !appInfo
						.isIspause());
				sendBroadcast(downState);
				LogUtils.d("pro", "我发出了下载中暂停广播");
				appInfo.setIspause(
						!appInfo.isIspause());
			}
		});*/
		appDownload.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (appInfo.isInstalled()) {
					AppUtils.launchApp(AppDetailActivity.this, appInfo
							.getAppName());
				} else if (DownloadService.isDownLoading(Integer
						.parseInt(appInfo.getIdx()))) {
					LogUtils.d("test", "暂停");
					File tempFile = DownloadService.CreatFileName(appInfo.getAppName());
					Intent intent = new Intent();
					intent.setAction(tempFile.getAbsolutePath());
					sendBroadcast(intent);
					Intent downState = new Intent();
					downState.setAction(tempFile.getAbsolutePath() + "down");
					downState.putExtra("isPause", !appInfo
							.isIspause());
					sendBroadcast(downState);
					LogUtils.d("pro", "我发出了暂停中下载广播");
					if (!appInfo.isIspause()) {
						appDownload.setText("暂停"+"("+count+"%)");
						appInfo.setDown(false);
					} else {
						appDownload.setText("下载中"+"("+count+"%)");
						appInfo.setDown(true);
					/*	v1.progress_view.setVisibility(View.VISIBLE);
						v1.tvdown.setVisibility(View.INVISIBLE);*/
					}
					appInfo.setIspause(
							!appInfo.isIspause());
					LogUtils.d("APPDETAIL", "我现在是"+appInfo.isIspause()+"状态");
				} else if (DownloadService.isDownLoaded(appInfo
						.getAppName())) {
					// 已经下载
					DownloadService.Instanll(appInfo
							.getAppName(), AppDetailActivity.this);
				} else if (!appInfo.isInstalled()) {
					Log.e("tag",
							"appurl = " + Global.MAIN_URL
									+ appInfo.getAppUrl());
					Log.e("tag",
							"appIdx = "
									+ Integer.parseInt(appInfo
											.getIdx()));
					/*
					 * Log.e("tag", "appname = " +
					 * appInfos.get(position).getAppName());
					 */
			
					Intent downState = new Intent();
					downState.setAction(tempFile.getAbsolutePath() + "down");
					downState.putExtra("isPause", appInfo
							.isIspause());
					sendBroadcast(downState);
					LogUtils.d("pro", "我发出了暂停中下载广播safdasfasf");
					long length = sp.getLong(tempFile.getAbsolutePath(), 0);
					/*
					 * DownloadService.downNewFile(appInfos.get(position)
					 * .getAppUrl(), Integer.parseInt(appInfos.get(
					 * position).getIdx()), appInfos.get(position)
					 * .getAppName(),length,0);
					 */
					DownloadService.downNewFile(appInfo, length,
							0, null);
					appInfo.setDown(true);
					Intent intent = new Intent();
					intent.setAction(MarketApplication.PRECENT);
					sendBroadcast(intent);
					LogUtils.d("pro", "我发出了暂停中下载广播but");
					Toast.makeText(AppDetailActivity.this,
							appInfo.getAppName() + " 开始下载...",
							Toast.LENGTH_SHORT).show();
					appDownload.setText("下载中"+"("+count+"%)");
				/*	v1.progress_view.setVisibility(View.VISIBLE);
					v1.tvdown.setVisibility(View.INVISIBLE);*/
				}

			}
		});
	}
	class MyInstalledReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			// 接收安装广播
			if (intent.getAction()
					.equals("android.intent.action.PACKAGE_ADDED")) {
				String packageName = intent.getDataString().substring(8);
				LogUtils.d("Search", "安装了:" + packageName + "包名的程序");

				MarketApplication.getInstance().reflashAppList();
				String installAppName = AppUtils.getAppName(context,
						packageName);
				appInfo.setInstalled(true);
				appDownload.setText("打开");
				appDownload.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						AppUtils.launchApp(AppDetailActivity.this, appInfo
								.getAppName());
					}
				});
			}
		}
	}
}
