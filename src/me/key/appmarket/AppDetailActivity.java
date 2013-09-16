package me.key.appmarket;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;

import me.key.appmarket.MainActivity.PrecentReceiver;
import me.key.appmarket.adapter.CommentAdapter;
import me.key.appmarket.network.AppDetailRequest;
import me.key.appmarket.network.AppDetailResponse;
import me.key.appmarket.network.HttpRequest.OnResponseListener;
import me.key.appmarket.network.HttpResponse;
import me.key.appmarket.tool.DownloadService;
import me.key.appmarket.tool.ToolHelper;
import me.key.appmarket.utils.AppUtils;
import me.key.appmarket.utils.CommentInfo;
import me.key.appmarket.utils.Global;
import me.key.appmarket.widgets.CustomScrollView;
import me.key.appmarket.widgets.EllipsizingTextView;

import org.json.JSONArray;
import org.json.JSONObject;

import com.market.d9game.R;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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

	private String appid;
	private ArrayList<CommentInfo> commentList = new ArrayList<CommentInfo>();
	private ListView commentListView;
	private CommentAdapter mCommentAdapter;

	File cache = new File(Environment.getExternalStorageDirectory(),
			"detail_cache");

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (!cache.exists()) {
			cache.mkdirs();
		}
		setContentView(R.layout.app_detail_main);

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
		appDownload.setOnClickListener(this);
		backIcon.setOnClickListener(this);
		appIcon.setOnClickListener(this);

		appid = getIntent().getStringExtra("appid");
		if (appid != null) {
			myDialog = ProgressDialog.show(this, "正在连接服务器..", "连接中,请稍后..",
					true, true);
			autoLoad(appid);
		}

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
					}
					if (response.getAppDownloadCounts() != null) {
						appDownloadCounts.setText(getString(
								R.string.app_detail_download_counts,
								response.getAppDownloadCounts()));
					}
					if (response.getAppVersion() != null) {
						appVersion.setText(getString(
								R.string.app_detail_version,
								response.getAppVersion()));
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
					}
					if (response.getAppUrl() != null) {
						appDownload.setTag(response.getAppUrl());
					}
					if (response.getAppDes() != null) {
						appDes.setText(response.getAppDes());
					}
					if (response.getAppIconUrl() != null) {
						asyncloadImage(appIcon,
								Global.MAIN_URL + response.getAppIconUrl());
					}

					if (isInstalled) {
						appDownload.setText("打开");
					} else if (isDowning) {
						appDownload.setText("下载中");
					} else if (isDowned) {
						appDownload.setText("安装");
					} else {
						appDownload.setText("下载");
					}

					if (response.getAppImgUrl() != null) {
						for (int i = 0; i < response.getAppImgUrl().length; i++) {
							ImageView iv = new ImageView(AppDetailActivity.this);
							asyncloadImage(iv, response.getAppImgUrl()[i]);
							// LayoutParams params = new LayoutParams(200,
							// LayoutParams.MATCH_PARENT);
							MarginLayoutParams params = new MarginLayoutParams(
									200, LayoutParams.MATCH_PARENT);
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
				iv_header.setImageURI(result);
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
		case R.id.app_download:
			if (isInstalled) {
				AppUtils.launchApp(AppDetailActivity.this, name);
			} else if (isDowning) {

			} else if (isDowned) {
				DownloadService.Instanll(name, AppDetailActivity.this);
			} else {
				if (appDownload.getTag() != null) {
					DownloadService.downNewFile((String) appDownload.getTag(),
							idx != null ? Integer.parseInt(idx) : 0, name);
					Toast.makeText(AppDetailActivity.this, name + " 开始下载...",
							Toast.LENGTH_SHORT).show();
					Log.d(TAG, "download apk name=" + name + "  idx=" + idx
							+ " url=" + appDownload.getTag());
				}
			}
			break;
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
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		unregisterPrecent();
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
			if (intent.getAction().equals(MarketApplication.PRECENT)) {
				isInstalled = AppUtils.isInstalled(name);
				// 是否已经下载完
				isDowned = DownloadService.isDownLoaded(name);
				// 是否正在下载
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
				}
			}
		}
	}

}
