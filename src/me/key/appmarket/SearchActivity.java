package me.key.appmarket;

import java.io.File;
import java.net.URLEncoder;
import java.util.ArrayList;

import me.key.appmarket.adapter.AppAdapter;
import me.key.appmarket.adapter.HotSearchAdapter;
import me.key.appmarket.tool.DownloadService;
import me.key.appmarket.tool.ToolHelper;
import me.key.appmarket.utils.AppInfo;
import me.key.appmarket.utils.AppUtils;
import me.key.appmarket.utils.Global;
import me.key.appmarket.utils.HotSearchInfo;
import me.key.appmarket.utils.LogUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.market.d9game.R;
import com.umeng.analytics.MobclickAgent;

/**
 * 搜索页
 */
public class SearchActivity extends Activity implements OnClickListener {

	private Button btn_search;
	private FrameLayout search;
	private EditText edit_search;
	private String search_text;

	private TextView total_size;
	private ListView mSearchListView;
	private ArrayList<AppInfo> appSearchInfos;
	private ArrayList<AppInfo> appSearchInfos_temp = new ArrayList<AppInfo>();
	private ArrayList<AppInfo> appManagerUpdateInfos_t = new ArrayList<AppInfo>();
	private ArrayList<AppInfo> appManagerUpdateInfos = new ArrayList<AppInfo>();
	private AppAdapter appSearchAdapter;
	private ProgressBar searchBar;
	// private LinearLayout ll_searcherror;

	// private ImageView back_icon;
	// private ImageView logo_title;
	private ImageView ivBack;

	private int page = 0; // 最后的可视项索引
	private View loadMoreView;
	private Button loadMoreButton;
	private int totalCount;// 搜索结果个数

	File cache;

	private boolean isLoading = false;
	private boolean isFirst = true;

	private ArrayList<HotSearchInfo> hotList = new ArrayList<HotSearchInfo>();
	private ArrayList<HotSearchInfo> hotListTemp = new ArrayList<HotSearchInfo>();
	private ArrayList<HotSearchInfo> historyList = new ArrayList<HotSearchInfo>();
	private HotSearchAdapter mHotSearchAdapter;
	private HotSearchAdapter mHistoryAdapter;

	// private LinearLayout search_linear;
	// private Button searchHot;
	// private Button searchHistory;
	// private ListView search_HotList;
	private boolean isShowingHot = true;
	private boolean isLoadedAllData;

	private TextView text_delete;
	private ImageView iv_operate_search;
	
	private String searchStr;
	private MyInstalledReceiver installedReceiver;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.setContentView(R.layout.search);
		MarketApplication.getInstance().getAppLication().add(this);
		cache = new File(Environment.getExternalStorageDirectory(), "cache");
		if (!cache.exists()) {
			cache.mkdirs();
		}

		initSearchView();
		installedReceiver = new MyInstalledReceiver();
		IntentFilter filter = new IntentFilter();

		filter.addAction("android.intent.action.PACKAGE_ADDED");
		filter.addDataScheme("package");

		this.registerReceiver(installedReceiver, filter);
		startSearch();
		new Thread(runHotData).start();
		// 添加下载状态广播，遗迹增添应用更新信息

	}

	private void startSearch() {
		search_text = getIntent().getStringExtra("Search");
		edit_search.setText(search_text);
		new Thread(searchData).start();
		appSearchAdapter.notifyDataSetChanged();
	}

	private void initSearchView() {
		// back_icon = (ImageView) findViewById(R.id.back_icon);
		// logo_title = (ImageView) findViewById(R.id.logo_title);

		// search_linear = (LinearLayout) findViewById(R.id.search_linear);
		// searchHot = (Button) findViewById(R.id.search_hot);
		// searchHistory = (Button) findViewById(R.id.search_history);
		// search_HotList = (ListView) findViewById(R.id.search_history_list);
		// searchHot.setOnClickListener(this);
		// searchHistory.setOnClickListener(this);
		;
		// back_icon.setOnClickListener(this);
		// logo_title.setOnClickListener(this);

		ivBack = (ImageView) findViewById(R.id.iv_back_search);

		// btn_search = (Button) findViewById(R.id.btn_search);
		search = (FrameLayout) findViewById(R.id.search_btn_search);
		edit_search = (EditText) findViewById(R.id.edit_search);
		total_size = (TextView) findViewById(R.id.total_size);

		mSearchListView = (ListView) findViewById(R.id.list);
		searchBar = (ProgressBar) findViewById(R.id.pro_bar);
		appSearchInfos = new ArrayList<AppInfo>();
		// ll_searcherror = (LinearLayout) findViewById(R.id.ll_error);

		mHotSearchAdapter = new HotSearchAdapter(hotList, this, cache);
		mHistoryAdapter = new HotSearchAdapter(historyList, this, cache);

		LayoutInflater inflater = LayoutInflater.from(this);
		text_delete = (TextView) findViewById(R.id.text_delete);
		text_delete.setOnClickListener(this);
		ivBack.setOnClickListener(this);
		iv_operate_search = (ImageView) findViewById(R.id.iv_operate_search);
		iv_operate_search.setOnClickListener(this);
		/*
		 * search_HotList.setAdapter(mHotSearchAdapter);
		 * search_HotList.setOnItemClickListener(new OnItemClickListener() {
		 * 
		 * @Override public void onItemClick(AdapterView<?> arg0, View arg1, int
		 * arg2, long arg3) { if (isShowingHot) { HotSearchInfo mHotSearchInfo =
		 * hotList.get(arg2); edit_search.setText(mHotSearchInfo.getWord()); }
		 * else { HotSearchInfo mHotSearchInfo = historyList.get(arg2);
		 * edit_search.setText(mHotSearchInfo.getWord()); }
		 * search.performClick(); } });
		 */

		// searchHot.setPadding(40, 0, 40, 0);
		// searchHistory.setPadding(40, 0, 40, 0);

		search.setOnClickListener(this);

		// 加载更多
		loadMoreView = getLayoutInflater().inflate(R.layout.loadmore, null);
		loadMoreButton = (Button) loadMoreView
				.findViewById(R.id.loadMoreButton);

		mSearchListView.addFooterView(loadMoreView);
		loadMoreButton.setVisibility(View.GONE);

		mSearchListView.setOnScrollListener(new OnScrollListener() {
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {

			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				if ((firstVisibleItem + visibleItemCount == totalItemCount)
						&& (totalItemCount != 0)) {
					if (!isLoading && !isFirst && totalCount > 0) {
						isLoading = true;
						if(isLoadedAllData){
							loadMoreButton.setText("已加载完毕");
						}else{
							
							loadMoreButton.setText("正在加载中...");
							loadMoreButton.setVisibility(View.VISIBLE);
							page = page + 1;
							
							new Thread(searchData).start();
						}
					}

					isFirst = false;
				}
			}
		});

		appSearchAdapter = new AppAdapter(appSearchInfos, this, cache,
				mSearchListView);
		mSearchListView.setAdapter(appSearchAdapter);

		mSearchListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				AppInfo mAppInfo = (AppInfo) mSearchListView.getAdapter()
						.getItem(position);
				Intent intent = new Intent(SearchActivity.this,
						AppDetailActivity.class);
				intent.putExtra("appid", mAppInfo.getIdx());
				startActivity(intent);
			}
		});
	}

	Runnable searchData = new Runnable() {
		@Override
		public void run() {
			if (search_text != null && !search_text.equals("")) {
				String str = ToolHelper.donwLoadToString(Global.MAIN_URL
						+ Global.SEARCH + "?searchKey="
						+ URLEncoder.encode(search_text) + "&page=" + page);
				System.out.println("search result =" + str);
				if (str.equals("null")) {
					searchHandler
							.sendEmptyMessage(Global.DOWN_DATA_HOME_SUCCESSFULL);
				} else if (str.equals("-1")) {
					searchHandler
							.sendEmptyMessage(Global.DOWN_DATA_HOME_FAILLY);
				} else {
					searchHandler
							.sendEmptyMessage(Global.DOWN_DATA_RANK_SUCCESSFUL);
					LogUtils.i("search", "获得了搜索数据");
					ParseSearchJson(str);
				}
			} else {
				searchHandler.sendEmptyMessage(Global.DOWN_DATA_HOME_FAILLY);
			}
		}
	};

	Handler searchHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			searchBar.setVisibility(View.GONE);
			isLoading = false;
			switch (msg.what) {
			case Global.DOWN_DATA_RANK_FAILLY: {
				isLoadedAllData=true;
				loadMoreButton.setVisibility(View.GONE);
				// ll_searcherror.setVisibility(View.VISIBLE);
				//mSearchListView.setVisibility(View.GONE);
				LogUtils.d("SearchActivity", "网络出错了吗？");
			}
				break;

			case Global.DOWN_DATA_SEARCH_EMPTY: {
				if (page == 0) {
					// ll_searcherror.setVisibility(View.GONE);
					mSearchListView.setVisibility(View.GONE);
					total_size.setText("找到0项符合的软件");
					total_size.setVisibility(View.VISIBLE);
					Toast.makeText(getApplicationContext(), "没有搜索到结果",
							Toast.LENGTH_SHORT).show();
				} else {
					loadMoreButton.setVisibility(View.GONE);
				}
			}
				break;
			case Global.DOWN_DATA_RANK_SUCCESSFUL: {
				if (appSearchInfos_temp.size() > 0) {
					appSearchInfos.addAll(appSearchInfos_temp);
					appSearchInfos_temp.clear();
				}

				total_size.setText("找到" + totalCount + "项符合的软件");
				total_size.setVisibility(View.VISIBLE);
				mSearchListView.setVisibility(View.VISIBLE);
				// ll_searcherror.setVisibility(View.GONE);
				loadMoreButton.setVisibility(View.VISIBLE);
				appSearchAdapter.notifyDataSetChanged();
			}
			default:
				break;
			}
			super.handleMessage(msg);
		}
	};

	private void ParseSearchJson(String str) {
		try {
			JSONObject jsonObjcet = new JSONObject(str);
			totalCount = jsonObjcet.getInt("totalCount");
			JSONArray jsonArray = jsonObjcet.getJSONArray("list");
			int len = jsonArray.length();
			if (len > 0) {

				for (int i = 0; i < len; i++) {
					JSONObject jsonObject = jsonArray.getJSONObject(i);
					String appName = jsonObject.getString("appname");
					String appiconurl = jsonObject.getString("appiconurl");
					String appSize = jsonObject.getString("appsize");
					String idx = jsonObject.getString("idx");
					String appurl = jsonObject.getString("appurl");
					String apppkgname = jsonObject.getString("apppkgname");
					AppInfo appInfo = new AppInfo(idx, appName, appSize,
							Global.MAIN_URL + appiconurl, appurl, "", "",
							apppkgname);
					appInfo.setId(apppkgname);
					appInfo.setPackageName(apppkgname);
					appInfo.setLastTime(Long.MAX_VALUE);
					appInfo.setInstalled(AppUtils.isInstalled(apppkgname));
					appSearchInfos_temp.add(appInfo);

				}
				StringBuilder apknamelist = new StringBuilder();
				for (AppInfo ai : appSearchInfos) {
					DownStateBroadcast dsb = new DownStateBroadcast();
					IntentFilter filter = new IntentFilter();
					String fileName = DownloadService.CreatFileName(
							ai.getAppName()).getAbsolutePath();
					filter.addAction(fileName + "down");
					registerReceiver(dsb, filter);
					apknamelist.append(ai.getPackageName() + ",");
					try {
						PackageManager pm = getPackageManager();
						if (ai.isInstalled()) {
							PackageInfo packInfo = pm.getPackageInfo(
									ai.getPackageName(), 0);
							String name = packInfo.versionName;
							ai.setVersion(name);
						} else {
							ai.setVersion("9999999999999");
						}

					} catch (NameNotFoundException e) {
						e.printStackTrace();
					}
				}
				String uris = apknamelist.toString();
				if (uris.length() > 0) {
					uris = uris.substring(0, uris.length() - 1);
				}
				/**
				 * 检查应用是否能更新
				 */
				String strList = ToolHelper.donwLoadToString(Global.MAIN_URL
						+ Global.UPGRADEVERSION + "?apknamelist=" + uris);
				ParseUpdateJson(strList);
				appManagerUpdateInfos_t = AppUtils.getCanUpadateApp(
						appSearchInfos, appManagerUpdateInfos_t);
				appManagerUpdateInfos.clear();
				appManagerUpdateInfos.addAll(appManagerUpdateInfos_t);
				LogUtils.d("Main", "appUpdate" + appManagerUpdateInfos.size());
				for (AppInfo appInfo : appManagerUpdateInfos) {
					LogUtils.d("Main", "我可以升级" + appInfo.getPackageName());
					for (AppInfo appManaInfo : appSearchInfos) {
						if (appManaInfo.getPackageName().equals(
								appInfo.getPackageName())) {
							appManaInfo.setCanUpdate(true);
							LogUtils.d("Main",
									"我可以升级" + appManaInfo.getPackageName());
						}
					}
				}
				if (totalCount == 0 || len == 0) {
					searchHandler
							.sendEmptyMessage(Global.DOWN_DATA_SEARCH_EMPTY);
				} else {
					searchHandler
							.sendEmptyMessage(Global.DOWN_DATA_RANK_SUCCESSFUL);
				}
			}else{
				searchHandler.sendEmptyMessage(Global.DOWN_DATA_RANK_FAILLY);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	Handler homeHotHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case Global.DOWN_DATA_HOME_FAILLY: {
			}
				break;
			case Global.DOWN_DATA_HOME_SUCCESSFULL: {
				if (hotListTemp != null && hotListTemp.size() > 0) {
					hotList.addAll(hotListTemp);
					hotListTemp.clear();
				}
				mHotSearchAdapter.notifyDataSetChanged();
			}
			default:
				break;
			}
			super.handleMessage(msg);
		}
	};

	Runnable runHotData = new Runnable() {
		@Override
		public void run() {
			String str = ToolHelper.donwLoadToString(Global.MAIN_URL
					+ Global.HOTSEARCH);

			if (str.equals("null")) {
				homeHotHandler
						.sendEmptyMessage(Global.DOWN_DATA_HOME_SUCCESSFULL);
			} else if (str.equals("-1")) {
				homeHotHandler.sendEmptyMessage(Global.DOWN_DATA_HOME_FAILLY);
			} else {
				ParseHotJson(str);
			}
		}
	};

	private void ParseHotJson(String str) {
		try {

			JSONArray jsonArray = new JSONArray(str);
			int len = jsonArray.length();
			for (int i = 0; i < len; i++) {
				JSONObject jsonObject = jsonArray.getJSONObject(i);
				String id = jsonObject.getString("id");
				String word = jsonObject.getString("word");
				HotSearchInfo mHotSearchInfo = new HotSearchInfo(id, word);

				hotListTemp.add(mHotSearchInfo);
			}

			homeHotHandler.sendEmptyMessage(Global.DOWN_DATA_HOME_SUCCESSFULL);
		} catch (Exception ex) {
			ex.printStackTrace();
			homeHotHandler.sendEmptyMessage(Global.DOWN_DATA_HOME_FAILLY);
		}
	}

	private void saveSearchHistory(String word) {
		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(this);

		String historyStr = sp.getString("historyList", "");
		String[] historys = historyStr.split(",");
		if (historys.length == 0) {
			sp.edit().putString("historyList", word).commit();
		} else if (historys.length < 10) {
			boolean flag = false;
			for (String str : historys) {
				if (str.equals(word)) {
					flag = true;
				}
			}
			if (!flag) {
				sp.edit().putString("historyList", word + "," + historyStr)
						.commit();
			}
		} else {
			boolean flag = false;
			for (String str : historys) {
				if (str.equals(word)) {
					flag = true;
				}
			}
			if (!flag) {
				String tempStr = "";
				for (int i = 0; i < historys.length - 1; i++) {
					tempStr = tempStr + "," + historys[i];
				}

				tempStr = word + tempStr;
				sp.edit().putString("historyList", tempStr).commit();
			}
		}
	}

	private ArrayList<HotSearchInfo> getHistoryList() {
		ArrayList<HotSearchInfo> mHistoryList = new ArrayList<HotSearchInfo>();
		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(this);

		String historyStr = sp.getString("historyList", "");
		Log.d("YTL", "historyStr  " + historyStr);
		String[] historys = historyStr.split(",");
		for (int i = 0; i < historys.length; i++) {
			if (!"".equals(historys[i])) {
				HotSearchInfo mHotSearchInfo = new HotSearchInfo(i + "",
						historys[i]);
				mHistoryList.add(mHotSearchInfo);
			}
		}
		return mHistoryList;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.iv_back_search:
			finish();
			// case R.id.logo_title:
			// SearchActivity.this.finish();
			// break;
		case R.id.iv_operate_search:
			search_text = edit_search.getText().toString();
			appSearchInfos.clear();
			appSearchAdapter.notifyDataSetChanged();
			total_size.setText("找到0项符合的软件");

			InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
			inputMethodManager.hideSoftInputFromWindow(SearchActivity.this
					.getCurrentFocus().getWindowToken(),
					InputMethodManager.HIDE_NOT_ALWAYS);

			if (!TextUtils.isEmpty(search_text)) {
				// 记录
				saveSearchHistory(search_text);

				loadMoreButton.setVisibility(View.GONE);

				searchBar.setVisibility(View.VISIBLE);
				// ll_searcherror.setVisibility(View.GONE);
				page = 0;
				totalCount = 0;
				total_size.setVisibility(View.GONE);

				text_delete.setVisibility(View.GONE);

				// search_linear.setVisibility(View.GONE);
				// search_HotList.setVisibility(View.GONE);
				new Thread(searchData).start();
			} else {
				loadMoreButton.setVisibility(View.GONE);
				Toast.makeText(getApplicationContext(), "输入关键字",
						Toast.LENGTH_SHORT).show();
			}
			new AsyncTask<Void, Void, Void>() {

				@Override
				protected Void doInBackground(Void... params) {

					return null;
				}

			}.execute();
			break;
		case R.id.loadMoreButton:
			if(isLoadedAllData){
				loadMoreButton.setText("已加载完毕");
			}else{
				loadMoreButton.setText("正在加载中...");
				page = page + 1;
				new Thread(searchData).start();
			}
			break;
		// case R.id.search_hot:
		// if (!isShowingHot) {
		// isShowingHot = true;
		// search_HotList.setAdapter(mHotSearchAdapter);
		// text_delete.setVisibility(View.GONE);
		//
		// searchHot.setBackgroundResource(R.drawable.btn_bar_2);
		// searchHot.setPadding(40, 0, 40, 0);
		// searchHistory.setBackgroundResource(0);
		// }
		// break;
		// case R.id.search_history:
		// if (isShowingHot) {
		// isShowingHot = false;
		//
		// text_delete.setVisibility(View.VISIBLE);
		//
		// ArrayList<HotSearchInfo> historyList_temp = getHistoryList();
		// historyList.clear();
		// historyList.addAll(historyList_temp);
		// search_HotList.setAdapter(mHistoryAdapter);
		//
		// searchHistory.setBackgroundResource(R.drawable.btn_bar_2);
		// searchHistory.setPadding(40, 0, 40, 0);
		// searchHot.setBackgroundResource(0);
		// }
		// break;
		case R.id.text_delete:
			SharedPreferences sp = PreferenceManager
					.getDefaultSharedPreferences(this);
			sp.edit().putString("historyList", "").commit();
			historyList.clear();
			mHistoryAdapter.notifyDataSetChanged();
			break;
		default:
			break;
		}
	}

	// @Override
	// public boolean onKeyDown(int keyCode, KeyEvent event) {
	// if (keyCode == KeyEvent.KEYCODE_BACK) {
	// // if (search_linear.getVisibility() == View.GONE) {
	// // edit_search.setText("");
	// // search_linear.setVisibility(View.VISIBLE);
	// search_HotList.setVisibility(View.VISIBLE);
	//
	// if (!isShowingHot) {
	// ArrayList<HotSearchInfo> historyList_temp = getHistoryList();
	// historyList.clear();
	// historyList.addAll(historyList_temp);
	// search_HotList.setAdapter(mHistoryAdapter);
	//
	// text_delete.setVisibility(View.VISIBLE);
	// }
	//
	// total_size.setVisibility(View.GONE);
	// mSearchListView.setVisibility(View.GONE);
	// ll_searcherror.setVisibility(View.GONE);
	// loadMoreButton.setVisibility(View.GONE);
	// return true;
	// // }
	// }
	// return super.onKeyDown(keyCode, event);
	// }

	@Override
	protected void onResume() {
		super.onResume();
		registerPrecent();
		MobclickAgent.onResume(this);
		appSearchAdapter.notifyDataSetChanged();
	}

	@Override
	protected void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterPrecent();
	};

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
		this.unregisterReceiver(installedReceiver);
	}

	class PrecentReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(MarketApplication.PRECENT)) {
				// 下载中刷新界面进度
				if (appSearchAdapter != null) {
					appSearchAdapter.notifyDataSetChanged();
				}
			}
		}
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

				for (AppInfo mAppInfo : appSearchInfos) {
					if (packageName != null
							&& packageName.equals(mAppInfo.getPackageName())) {
						mAppInfo.setInstalled(true);
						mAppInfo.setCanUpdate(false);
						LogUtils.d("Search", "我接收到了安装" + packageName);
						break;
					}
				}

				appSearchAdapter.notifyDataSetChanged();
			}
		}
	}

	private void ParseUpdateJson(String str) {
		try {

			ArrayList<AppInfo> tempList = new ArrayList<AppInfo>();
			JSONArray jsonArray = new JSONArray(str);
			int len = jsonArray.length();
			for (int i = 0; i < len; i++) {
				JSONObject jsonObject = jsonArray.getJSONObject(i);
				String idx = jsonObject.getString("idx");
				String appName = jsonObject.getString("appname");
				String appiconurl = jsonObject.getString("appiconurl");
				String appSize = jsonObject.getString("appsize");

				String appurl = jsonObject.getString("appurl");
				AppInfo appInfo = new AppInfo(idx, appName, appSize,
						Global.MAIN_URL + appiconurl, appurl, "", "", appName);

				appInfo.setPackageName(jsonObject.getString("apppkgname"));
				appInfo.setVersion(jsonObject.getString("version"));

				appInfo.setInstalled(AppUtils.isInstalled(appName));
				tempList.add(appInfo);
			}
			LogUtils.d("Mana", "temp:" + tempList.size());
			appManagerUpdateInfos_t.clear();
			appManagerUpdateInfos_t.addAll(tempList);
		} catch (Exception ex) {
			ex.printStackTrace();
			// Log.e("tag", "error = " + ex.getMessage());
		}
	}

	class DownStateBroadcast extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			String fileName = null;
			LogUtils.d("RankActivity", "我接受到了暂停广播");
			for (AppInfo ai : appSearchInfos) {
				fileName = DownloadService.CreatFileName(ai.getAppName())
						.getAbsolutePath() + "down";
				if (fileName.equals(intent.getAction())) {
					boolean downState = intent
							.getBooleanExtra("isPause", false);
					ai.setIspause(downState);
					appSearchAdapter.notifyDataSetChanged();
					LogUtils.d("RankActivity",
							"我更新了ui" + ai.getAppName() + ai.isIspause());
					break;
				}
			}
		}

	}
}
