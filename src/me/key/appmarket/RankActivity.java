package me.key.appmarket;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import me.key.appmarket.IndexDetaileActivity.PrecentReceiver;
import me.key.appmarket.MainActivity.MyInstalledReceiver;
import me.key.appmarket.adapter.MenuCategoryAdapter;
import me.key.appmarket.adapter.NewRankAdapter;
import me.key.appmarket.tool.DownloadService;
import me.key.appmarket.tool.ToolHelper;
import me.key.appmarket.utils.AppInfo;
import me.key.appmarket.utils.AppUtils;
import me.key.appmarket.utils.CategoryInfo;
import me.key.appmarket.utils.Global;
import me.key.appmarket.utils.LogUtils;
import me.key.appmarket.utils.ToastUtils;

import com.market.d9game.R;
import com.slidingmenu.lib.app2.SlidingFragmentActivity;
import com.slidingmenu.lib2.SlidingMenu;
import com.slidingmenu.lib2.SlidingMenu.OnCloseListener;
import com.slidingmenu.lib2.SlidingMenu.OnOpenedListener;
import com.umeng.analytics.MobclickAgent;

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
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ViewSwitcher;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView.OnItemClickListener;

/**
 * 排行
 * 
 * @author Administrator
 * 
 */
public class RankActivity extends SlidingFragmentActivity {
	private ListView mRankListView;
	private MenuCategoryAdapter menuCategoryAdapter;
	private List<AppInfo> appRankInfos;
	private NewRankAdapter appRankAdapter;
	private ProgressBar rank_pb;
	private LinearLayout ll_rankerror;
	private boolean mPreparedQuit = false;
	private static final int DELAYTIME = 5000;
	private static final int RESETQUIT = 0;
	private static final int SHOWNEXT = 1;
	private static final int INMAIN = 2;
	private PrecentReceiver mPrecentReceiver;
	private ListView lv;
	private ArrayList<CategoryInfo> gcategoryInfoList_temp = new ArrayList<CategoryInfo>();
	private SlidingMenu menu;
	private Handler myHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case RESETQUIT:
				mPreparedQuit = false;
				break;
			default:
				break;
			}
		}
	};
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		MarketApplication.getInstance().getAppLication().add(this);
		setContentView(R.layout.rank);
		setBehindContentView(R.layout.slide_menu);
		mRankListView = (ListView) findViewById(R.id.list_rank);
		rank_pb = (ProgressBar) findViewById(R.id.rank_pb);
		appRankInfos = new ArrayList<AppInfo>();
		ll_rankerror = (LinearLayout) findViewById(R.id.ll_error);
		Button btn_refresh = (Button) ll_rankerror.findViewById(R.id.btn_Refsh);
		FragmentTransaction fragmentTransaction = getSupportFragmentManager()
				.beginTransaction();
		final MenuFragment menuFragment = new MenuFragment();
		fragmentTransaction.replace(R.id.slide_content, menuFragment);
		// fragmentTransaction.replace(R.id.content, new ContentFragment());
		fragmentTransaction.commit();
		startService(new Intent(this, DownloadService.class));
		LogUtils.d("Main", "我已经被加载了哟");
		lv = (ListView) findViewById(R.id.category_lv);
		LogUtils.d("Main", lv + "");
		ImageButton search_btn = (ImageButton) findViewById(R.id.search_btn);
		search_btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				menu.toggle();
			}
		});
		MyInstalledReceiver installedReceiver = new MyInstalledReceiver();
		IntentFilter filter = new IntentFilter();

		filter.addAction("android.intent.action.PACKAGE_ADDED");
		filter.addDataScheme("package");
		this.registerReceiver(installedReceiver, filter);
		LogUtils.d("Main1", menuCategoryAdapter + "");
		new AsyncTask<Void, Void, Void>() {

			@Override
			protected Void doInBackground(Void... params) {
				String str = ToolHelper.donwLoadToString(Global.MAIN_URL
						+ Global.APP_CATEGORY + "?type=" + 2);
				Log.e("tag", "runCategoryData result =" + str);
				ParseCategoryJson(str);
				return null;
			}

			@Override
			protected void onPostExecute(Void result) {
				super.onPostExecute(result);
				menuCategoryAdapter = new MenuCategoryAdapter(
						RankActivity.this, gcategoryInfoList_temp,lv);
				lv.setAdapter(menuCategoryAdapter);
				// lv.getChildAt(0).setBackgroundColor(getResources().getColor(R.color.classiv_cloor));
				// lv.getChildAt(0).findViewById(R.id.click_menu).setVisibility(View.VISIBLE);
				LogUtils.d("Main", lv.getChildCount() + "");

				lv.setOnItemClickListener(new OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> parent, View view,
							int position, long id) {

						menuFragment.updata(position);
						view.setBackgroundColor(getResources().getColor(
								R.color.classiv_cloor));
						view.findViewById(R.id.click_menu).setVisibility(
								View.VISIBLE);
						TextView title = (TextView) view
								.findViewById(R.id.category_menu);
						title.setTextColor(getResources().getColor(
								R.color.myprobar));
						for (int i = 0; i < lv.getChildCount(); i++) {
							if (i == position) {
								continue;
							}
							LogUtils.d("Main", i + "");
							lv.getChildAt(i).setBackgroundColor(
									getResources().getColor(R.color.white));
							lv.getChildAt(i).findViewById(R.id.click_menu)
									.setVisibility(View.INVISIBLE);
							TextView tv = (TextView) lv.getChildAt(i)
									.findViewById(R.id.category_menu);
							tv.setTextColor(getResources().getColor(
									R.color.black));
						}
					}
				});
			}
		}.execute();
		menu = getSlidingMenu();
		menu.setMode(SlidingMenu.RIGHT);
		menu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
		/*
		 * menu.setShadowWidthRes(R.dimen.shadow_width);
		 * menu.setShadowDrawable(R.drawable.shadow);
		 */
		menu.setBehindOffsetRes(R.dimen.slidingmenu_offset);
		menu.setFadeDegree(0.35f);
		menu.setOnCloseListener(new OnCloseListener() {

			@Override
			public void onClose() {
				LogUtils.d("Main", "close");
			}
		});
		menu.setOnOpenedListener(new OnOpenedListener() {

			@Override
			public void onOpened() {
				LogUtils.d("Main", "open");
			/*	Intent intent = new Intent();
				intent.setAction("open.menu");
				sendBroadcast(intent);*/
			}
		});
		LinearLayout etSeacher = (LinearLayout) findViewById(R.id.menu_search);
		etSeacher.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setClass(RankActivity.this, SearchActivity.class);
				startActivity(intent);
				LogUtils.d("MAIN", "动画前");
				RankActivity.this.overridePendingTransition(R.anim.left_anim,
						R.anim.right_anim);
				LogUtils.d("MAIN", "动画后");
			}
		});
		File cache = new File(Environment.getExternalStorageDirectory(),
				"cache");
		if (!cache.exists()) {
			cache.mkdirs();
		}
		new AsyncTask<Void, Void, Void>() {
			@Override
			protected void onPreExecute() {
				rank_pb.setVisibility(View.VISIBLE);
			};
			protected Void doInBackground(Void... params) {
			/*	String str = ToolHelper.donwLoadToString(Global.GAME_MAIN_URL
						+ Global.RANK_PAGE);
					ParseRankJson(str);*/
				appRankInfos.clear();
				List<AppInfo> appRankInfos_temp = new ArrayList<AppInfo>();
				appRankInfos_temp = MarketApplication.getInstance().getRankappinfos();
				appRankInfos.addAll(appRankInfos_temp);
					for(AppInfo ai : appRankInfos) {
						DownStateBroadcast dsb = new DownStateBroadcast();
						IntentFilter filter = new IntentFilter();
						String fileName =  DownloadService.CreatFileName(ai.getAppName()).getAbsolutePath();
						filter.addAction(fileName+"down");
						registerReceiver(dsb, filter);
						}
					return null;
			}
			protected void onPostExecute(Void result) {
				appRankAdapter.notifyDataSetChanged();
				rank_pb.setVisibility(View.INVISIBLE);
			};
		}.execute();
		appRankAdapter = new NewRankAdapter(appRankInfos, this, cache);
		mRankListView.setAdapter(appRankAdapter);
		// 注册滑动监听事件，快速滑动时，不异步加载图片，而是从缓存中获取
		mRankListView.setOnScrollListener(new OnScrollListener() {

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				switch (scrollState) {
				case SCROLL_STATE_FLING:
					appRankAdapter.isAsyn = true;
					break;
				case SCROLL_STATE_IDLE:
					appRankAdapter.isAsyn = false;
					appRankAdapter.notifyDataSetChanged();
					break;
				case SCROLL_STATE_TOUCH_SCROLL:
					appRankAdapter.isAsyn = false;
					break;
				}
			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {

			}
		});
		mRankListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				AppInfo mAppInfo = (AppInfo) mRankListView.getAdapter()
						.getItem(position);
				Log.d("YTL", "mAppInfo.getIdx() = " + mAppInfo.getIdx());
				Intent intent = new Intent(RankActivity.this,
						AppDetailActivity.class);
				intent.putExtra("appid", mAppInfo.getIdx());
				intent.putExtra("appinfo", mAppInfo);
				startActivity(intent);
			}
		});
	}
	private void ParseRankJson(String str) {
		try {
			Log.e("tag", "--------------2--------");
			JSONArray jsonArray = new JSONArray(str);
			int len = jsonArray.length();
			for (int i = 0; i < len; i++) {
				JSONObject jsonObject = jsonArray.getJSONObject(i);
				String appName = jsonObject.getString("appname");
				String appiconurl = jsonObject.getString("appiconurl");
				String appSize = jsonObject.getString("appsize");
				String idx = jsonObject.getString("idx");
				String appurl = jsonObject.getString("appurl");
				String appDownCount = jsonObject.getString("appdowncount");
				String apppkgname = jsonObject.getString("apppkgname");
				AppInfo appInfo = new AppInfo(idx, appName, appSize,
						Global.MAIN_URL + appiconurl, appurl, appDownCount, "",appName);
				appInfo.setPackageName(apppkgname);
				appInfo.setInstalled(AppUtils.isInstalled(apppkgname));
				appInfo.setLastTime(Long.MAX_VALUE);
				appRankInfos.add(appInfo);
				// appRankInfos.add(appInfo);
				Log.e("tag", "info = " + appInfo.toString());
			}
			Log.e("tag", "--------------2--------");
			// rankHandler.sendEmptyMessage(Global.DOWN_DATA_RANK_SUCCESSFUL);
		} catch (Exception ex) {
			Log.e("tag", "error = " + ex.getMessage());
		}
	}
	class PrecentReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(MarketApplication.PRECENT)) {
				// 下载中刷新界面进度
				if (appRankAdapter != null) {
					appRankAdapter.notifyDataSetChanged();
				}
			}
		}
	}
	@Override
	protected void onResume() {
		super.onResume();
		registerPrecent();
		MobclickAgent.onResume(this);
		appRankAdapter.notifyDataSetChanged();
	}

	@Override
	protected void onPause() {
		super.onPause();
		unregisterPrecent();
		MobclickAgent.onPause(this);
		if(menu.isMenuShowing()) {
			menu.toggle();
		}
	}
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
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (!mPreparedQuit) {
				ToastUtils.show(R.string.quit_alert);
				mPreparedQuit = true;
				myHandler.sendEmptyMessageDelayed(RESETQUIT, 3000);
				return true;
			}
			Intent cancalNt = new Intent();
			cancalNt.setAction("duobaohui.cancalnotifition");
			this.sendBroadcast(cancalNt);
		/*	 ArrayList<Activity> appLication = MarketApplication.getInstance().getAppLication();
			 for(Activity at : appLication) {
				 at.finish();
			 }
			 System.exit(0);
			 android.os.Process.killProcess(android.os.Process.myPid());*/
			// this.finish();

		}
		return super.onKeyDown(keyCode, event);
	}
	
	class DownStateBroadcast extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			String fileName = null;
			LogUtils.d("RankActivity", "我接受到了暂停广播");
			for(AppInfo ai : appRankInfos) {
				fileName =  DownloadService.CreatFileName(ai.getAppName()).getAbsolutePath()+"down";
				if(fileName.equals(intent.getAction())) {
					boolean downState = intent.getBooleanExtra("isPause", false);
					ai.setIspause(downState);
					appRankAdapter.notifyDataSetChanged();
					LogUtils.d("RankActivity", "我更新了ui"+ai.getAppName()+ai.isIspause());
					break;
				}
			}
		}
		
	}
	private void ParseCategoryJson(String str) {
		try {
			Log.e("tag", "--------------ParseCategoryJson--------");
			JSONArray jsonArray = new JSONArray(str);
			int len = jsonArray.length();
			for (int i = 0; i < len; i++) {
				JSONObject jsonObject = jsonArray.getJSONObject(i);
				String id = jsonObject.getString("id");
				String name = jsonObject.getString("name");
				String type1 = jsonObject.getString("type1");
				String type2 = jsonObject.getString("type2");
				String appUrl = jsonObject.getString("appiconurl");
				CategoryInfo mCategoryInfo = new CategoryInfo(id, name, type1,
						type2, Global.MAIN_URL + appUrl);
				gcategoryInfoList_temp.add(mCategoryInfo);
			}
			Log.e("tag", "--------------ParseCategoryJson 2--------");
			/*
			 * categoryDataHandler
			 * .sendEmptyMessage(Global.DOWN_DATA_HOME_SUCCESSFULL);
			 */
		} catch (Exception ex) {
			Log.e("tag", "ParseBannerJson error = " + ex.getMessage());
		}
	}
	@Override
	protected void onDestroy() {
		super.onDestroy();
		 stopService(new Intent(this, DownloadService.class));
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

				for (AppInfo mAppInfo : appRankInfos) {
					if (packageName != null
							&& packageName.equals(mAppInfo.getPackageName())) {
						mAppInfo.setInstalled(true);
						LogUtils.d("Search", "我接收到了安装"+packageName);
						break;
					}
				}

				appRankAdapter.notifyDataSetChanged();
			}
		}
	}
}
