package me.key.appmarket;

import java.io.File;
import java.util.LinkedList;

import me.key.appmarket.adapter.AppAdapter;
import me.key.appmarket.tool.ToolHelper;
import me.key.appmarket.utils.AppInfo;
import me.key.appmarket.utils.AppUtils;
import me.key.appmarket.utils.Global;
import me.key.appmarket.utils.LogUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.market.d9game.R;
import com.umeng.analytics.MobclickAgent;

@SuppressLint("HandlerLeak")
public class IndexDetaileActivity extends Activity implements OnScrollListener {

	private ListView mListView;
	private ProgressBar pBar;
	private AppAdapter appAdapter;
	private LinkedList<AppInfo> appDatainfos;
	private LinkedList<AppInfo> appDatainfos_temp;

	private int type1 = 1;
	private int type2 = 1;
	private TextView tv_empty;

	private boolean isLoading = false;
	private boolean isFirst = false;

	// loadmore
	private View loadMoreView;
	private Button loadMoreButton;
	private Handler handler = new Handler();

	private boolean isRecoTag = false;
	private int tagid = 0;

	private int page = 0; // 最后的可视项索引

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.indexdetail);
		initActivity();
		Bundle bundle = getIntent().getBundleExtra("value");
		TextView tv_tiltle = (TextView) findViewById(R.id.topbar_title);
		tv_tiltle.setText(bundle.getString("name"));

		isRecoTag = bundle.getBoolean("isRecoTag", false);

		if (!isRecoTag) {
			type1 = bundle.getInt("type1");
			type2 = bundle.getInt("type2");

			new AsyncTask<Void, Void, Void>(){

				@Override
				protected Void doInBackground(Void... params) {
					String str = ToolHelper.donwLoadToString(Global.MAIN_URL
							+ Global.INDEX_PAGE + "?type1=" + type1 + "&type2=" + type2
							+ "&page=" + page);
					Log.e("tag", "indexDetaile result = " + str);
					if (str.equals("null")) {
						mHandler.sendEmptyMessage(Global.DOWN_DATA_EMPTY);
					} else if (str.equals("-1")) {
						mHandler.sendEmptyMessage(Global.DOWN_DATA_FAILLY);
					} else {
						ParseJson(str);
					}
					return null;
				}
				protected void onPostExecute(Void result) {
					File cache = new File(Environment.getExternalStorageDirectory(),
							"cache");
					if (!cache.exists()) {
						cache.mkdirs();
					}
					appAdapter = new AppAdapter(appDatainfos, IndexDetaileActivity.this, cache,mListView);
					mListView.setAdapter(appAdapter);
				};
			}.execute();
		} else {
			tagid = bundle.getInt("tagid");
			new Thread(recoTagRunnable).start();
		}

		registerInstall();
	}

	private void initActivity() {
		// TODO Auto-generated method stub
		mListView = (ListView) findViewById(R.id.mlist);
		pBar = (ProgressBar) findViewById(R.id.pro_bar);
		appDatainfos = new LinkedList<AppInfo>();
		appDatainfos_temp = new LinkedList<AppInfo>();
	
		pBar.setVisibility(View.VISIBLE);
		tv_empty = (TextView) findViewById(R.id.empty);
		tv_empty.setVisibility(View.GONE);
		ImageView btnBack = (ImageView) findViewById(R.id.back_icon);
		btnBack.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				IndexDetaileActivity.this.finish();
			}
		});
		loadMoreView = getLayoutInflater().inflate(R.layout.loadmore, null);
		loadMoreButton = (Button) loadMoreView
				.findViewById(R.id.loadMoreButton);
		// loadMoreButton.setOnClickListener(new View.OnClickListener() {
		// @Override
		// public void onClick(View v) {
		// loadMoreButton.setText("正在加载中...");
		// handler.postDelayed(new Runnable() {
		// @Override
		// public void run() {
		// loadMoreData();
		// appAdapter.notifyDataSetChanged();
		// loadMoreButton.setText("加载更多...");
		// }
		// },2000);
		// }
		// });
		mListView.addFooterView(loadMoreView);
		mListView.setOnScrollListener(this);
		
		loadMoreButton.setVisibility(View.GONE);

		// 为列表添加监听
		mListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				AppInfo mAppInfo = (AppInfo) mListView.getAdapter().getItem(
						position);
				Intent intent = new Intent(IndexDetaileActivity.this,
						AppDetailActivity.class);
				intent.putExtra("appid", mAppInfo.getIdx());
				startActivity(intent);
			}
		});
	}

	Runnable runnable = new Runnable() {
		@Override
		public void run() {
			String str = ToolHelper.donwLoadToString(Global.MAIN_URL
					+ Global.INDEX_PAGE + "?type1=" + type1 + "&type2=" + type2
					+ "&page=" + page);
			Log.e("tag", "indexDetaile result = " + str);
			if (str.equals("null")) {
				mHandler.sendEmptyMessage(Global.DOWN_DATA_EMPTY);
			} else if (str.equals("-1")) {
				mHandler.sendEmptyMessage(Global.DOWN_DATA_FAILLY);
			} else {
				ParseJson(str);
			}
		}
	};

	private void ParseJson(String str) {
		try {
			JSONArray jsonArray = new JSONArray(str);
			int len = jsonArray.length();
			for (int i = 0; i < len; i++) {
				JSONObject jsonObject = jsonArray.getJSONObject(i);
				String appName = jsonObject.getString("appname");
				String appiconurl = jsonObject.getString("appiconurl");
				String appSize = jsonObject.getString("appsize");
				String idx = jsonObject.getString("idx");
				String appurl = jsonObject.getString("appurl");
				AppInfo appInfo = new AppInfo(idx, appName, appSize,
						Global.MAIN_URL + appiconurl, appurl, "");

				appInfo.setInstalled(AppUtils.isInstalled(appName));
				appDatainfos.add(appInfo);
				//appDatainfos_temp.add(appInfo);
			}
			//mHandler.sendEmptyMessage(Global.DOWN_DATA_SUCCESSFULL);
		} catch (Exception ex) {
			mHandler.sendEmptyMessage(Global.DOWN_DATA_FAILLY);
		}
	}

	Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			pBar.setVisibility(View.GONE);
			isLoading = false;
			switch (msg.what) {
			case Global.DOWN_DATA_FAILLY: {
				// mListView.setVisibility(View.GONE);
				Toast.makeText(IndexDetaileActivity.this, "网络异常",
						Toast.LENGTH_SHORT).show();
				IndexDetaileActivity.this.finish();
			}
				break;
			case Global.DOWN_DATA_SUCCESSFULL: {
				loadMoreButton.setVisibility(View.GONE);
				if (appDatainfos_temp != null && appDatainfos_temp.size() > 0) {
					/*appDatainfos.addAll(appDatainfos_temp);
					appDatainfos_temp.clear();*/
				}
				//appAdapter.notifyDataSetChanged();
			}
				break;
			case Global.DOWN_DATA_EMPTY: {
				mListView.setVisibility(View.GONE);
				tv_empty.setVisibility(View.VISIBLE);
			}
				break;
			default:
				break;
			}
			super.handleMessage(msg);
		}
	};

	@Override
	public void onScroll(AbsListView arg0, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		// TODO Auto-generated method stub
		if ((firstVisibleItem + visibleItemCount == totalItemCount)
				&& (totalItemCount != 0)) {
			if (!isLoading && !isFirst) {
				isLoading = true;
				if (!isRecoTag) {
					loadMoreButton.setText("正在加载中...");
					loadMoreButton.setVisibility(View.VISIBLE);
					page = page + 1;
					//new Thread(runnable).start();
					new AsyncTask<Void, Void, Void>(){

						@Override
						protected Void doInBackground(Void... params) {
							String str = ToolHelper.donwLoadToString(Global.MAIN_URL
									+ Global.INDEX_PAGE + "?type1=" + type1 + "&type2=" + type2
									+ "&page=" + page);
							Log.e("tag", "indexDetaile result = " + str);
							if (str.equals("null")) {
								mHandler.sendEmptyMessage(Global.DOWN_DATA_EMPTY);
							} else if (str.equals("-1")) {
								mHandler.sendEmptyMessage(Global.DOWN_DATA_FAILLY);
							} else {
								ParseJson(str);
							}
							return null;
						}
						protected void onPostExecute(Void result) {
							loadMoreButton.setVisibility(View.GONE);
							appAdapter.notifyDataSetChanged();
							pBar.setVisibility(View.GONE);
							isLoading = false;
						};
					}.execute();
				}
			}

			isFirst = false;
		}
	}

	@Override
	public void onScrollStateChanged(AbsListView arg0, int scrollState) {
		// TODO Auto-generated method stub
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();

		unregisterInstall();
	}

	MyInstalledReceiver installedReceiver;

	private void registerInstall() {
		installedReceiver = new MyInstalledReceiver();
		IntentFilter filter = new IntentFilter();

		filter.addAction("android.intent.action.PACKAGE_ADDED");
		filter.addAction("android.intent.action.PACKAGE_REMOVED");
		filter.addDataScheme("package");

		this.registerReceiver(installedReceiver, filter);
	}

	private void unregisterInstall() {
		if (installedReceiver != null) {
			this.unregisterReceiver(installedReceiver);
		}
	}

	class MyInstalledReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			// 接收安装广播
			if (intent.getAction()
					.equals("android.intent.action.PACKAGE_ADDED")) {
				String packageName = intent.getDataString().substring(8);
				LogUtils.d("YTL", "安装了:" + packageName + "包名的程序");

				MarketApplication.getInstance().reflashAppList();
				String installAppName = AppUtils.getAppName(context,
						packageName);
				for (AppInfo mAppInfo : appDatainfos) {
					if (installAppName != null
							&& installAppName.equals(mAppInfo.getAppName())) {
						mAppInfo.setInstalled(true);
						break;
					}
				}

				appAdapter.notifyDataSetChanged();
			}
			// 接收卸载广播
			if (intent.getAction().equals(
					"android.intent.action.PACKAGE_REMOVED")) {
				MarketApplication.getInstance().reflashAppList();
				String packageName = intent.getDataString();
				LogUtils.d("YTL", "卸载了:" + packageName + "包名的程序");
			}
		}
	}

	Handler recomHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			pBar.setVisibility(View.GONE);
			isLoading = false;
			switch (msg.what) {
			case Global.DOWN_DATA_FAILLY: {
				 mListView.setVisibility(View.GONE);
				Toast.makeText(IndexDetaileActivity.this, "网络异常",
						Toast.LENGTH_SHORT).show();
				IndexDetaileActivity.this.finish();
			}
				break;
			case Global.DOWN_DATA_SUCCESSFULL: {
				loadMoreButton.setVisibility(View.GONE);
				if (appDatainfos_temp != null && appDatainfos_temp.size() > 0) {
					appDatainfos.addAll(appDatainfos_temp);
					appDatainfos_temp.clear();
				}
				appAdapter.notifyDataSetChanged();
			}
				break;
			case Global.DOWN_DATA_EMPTY: {
				mListView.setVisibility(View.GONE);
				tv_empty.setVisibility(View.VISIBLE);
			}
				break;
			default:
				break;
			}
			super.handleMessage(msg);
		}
	};
	Runnable recoTagRunnable = new Runnable() {
		@Override
		public void run() {
			String str = ToolHelper.donwLoadToString(Global.MAIN_URL
					+ Global.TAGLIST + "?tagid=" + tagid);
			Log.e("tag", "indexDetaile recoTagRunnable = " + str);
			if (str.equals("null")) {
				recomHandler.sendEmptyMessage(Global.DOWN_DATA_EMPTY);
			} else if (str.equals("-1")) {
				recomHandler.sendEmptyMessage(Global.DOWN_DATA_FAILLY);
			} else {
				ParseRecoJson(str);
			}
		}
	};

	private void ParseRecoJson(String str) {
		try {
			JSONArray jsonArray = new JSONArray(str);
			int len = jsonArray.length();
			for (int i = 0; i < len; i++) {
				JSONObject jsonObject = jsonArray.getJSONObject(i);
				String appName = jsonObject.getString("appname");
				String appiconurl = jsonObject.getString("appiconurl");
				String appSize = jsonObject.getString("appsize");
				String idx = jsonObject.getString("idx");
				String appurl = jsonObject.getString("appurl");
				AppInfo appInfo = new AppInfo(idx, appName, appSize,
						Global.MAIN_URL + appiconurl, appurl, "");

				appInfo.setInstalled(AppUtils.isInstalled(appName));
				appDatainfos_temp.add(appInfo);
			}
			recomHandler.sendEmptyMessage(Global.DOWN_DATA_SUCCESSFULL);
		} catch (Exception ex) {
			recomHandler.sendEmptyMessage(Global.DOWN_DATA_FAILLY);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		registerPrecent();
		MobclickAgent.onResume(this);
	}

	@Override
	protected void onPause() {
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
			if (intent.getAction().equals(MarketApplication.PRECENT)) {
				// 下载中刷新界面进度
				if (appAdapter != null) {
					appAdapter.notifyDataSetChanged();
				}
			}
		}
	}
}
