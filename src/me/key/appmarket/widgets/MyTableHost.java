package me.key.appmarket.widgets;

import java.lang.reflect.Field;
import java.util.List;

import me.key.appmarket.LocalGameActivity;
import me.key.appmarket.MainActivity;
import me.key.appmarket.MarketApplication;
import me.key.appmarket.RankActivity;
import me.key.appmarket.tool.DownloadService;
import me.key.appmarket.tool.ToolHelper;
import me.key.appmarket.utils.AppInfo;
import me.key.appmarket.utils.AppUtils;
import me.key.appmarket.utils.Global;
import me.key.appmarket.utils.LogUtils;
import me.key.appmarket.utils.ToastUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.TabActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabWidget;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.baidu.android.pushservice.PushConstants;
import com.baidu.android.pushservice.PushManager;
import com.market.d9game.R;
import com.umeng.analytics.MobclickAgent;

/**
 * 底部导航菜单栏
 * 
 * @author Administrator
 * 
 */
public class MyTableHost extends TabActivity {
	private LayoutInflater from;
	private static final int RESETQUIT = 0;
	private static final int INMAIN = 2;
	private boolean mPreparedQuit = false;
	private ImageView down_anim;
	private Handler myHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case INMAIN:

				// MarketApplication.getInstance().reflashAppList();
				ViewSwitcher vs = (ViewSwitcher) findViewById(R.id.main_bottom_vs);
				vs.showNext();
				break;
			case RESETQUIT:
				mPreparedQuit = false;
				break;
			default:
				break;
			}
		}
	};
	// 寻找游戏
	private TextView tv1;
	private TextView tv2;
	private TextView tv3;
	// 管理游戏
	private TextView tv4;
	// 本地游戏
	private TextView tv5;
	private View bottomView1;
	private View bottomView2;
	private View bottomView3;
	private View bottomView4;
	private View bottomView5;
	private TabHost tabHost;
	private Drawable findGame_normal;
	private Drawable findGame_focue;
	private Drawable local_focue;
	private Drawable local_normal;
	private Drawable manager_focue;
	private Drawable manager_normal;
	private int tadid = 1;
	private TabWidget tw;
	private DownStateBroadcast dsb;
	private DownStateBroadcastRank dsbRank;
	private int width;
	private int height;
	// Rank
	private List<AppInfo> appRankInfos;
	// Home
	private List<AppInfo> appHomeInfos_temp;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_bottom);
		startService(new Intent(this, DownloadService.class));
		tabHost = getTabHost();
		tabHost.setup();
		appRankInfos = MarketApplication.getInstance().getRankappinfos();
		appHomeInfos_temp = MarketApplication.getInstance().getHomeAppInfos();
		from = LayoutInflater.from(this);
		MarketApplication.getInstance().getAppLication().add(this);
		tw = (TabWidget) findViewById(android.R.id.tabs);
		bottomView1 = from.inflate(R.layout.item_main_bottom, null);
		bottomView2 = from.inflate(R.layout.item_main_bottom, null);
		bottomView3 = from.inflate(R.layout.item_main_bottom, null);
		bottomView4 = from.inflate(R.layout.item_main_bottom, null);
		bottomView5 = from.inflate(R.layout.item_main_bottom_local, null);
		bottomView2.setVisibility(View.INVISIBLE);
		bottomView3.setVisibility(View.INVISIBLE);
		findview();
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		width = dm.widthPixels;
		height = dm.heightPixels;
		LogUtils.d("Local", width+"tab");
		init();
		IntentFilter downBroadcast = new IntentFilter();
		downBroadcast.addAction("startanim");
		PlayAnimBroadcast pb = new PlayAnimBroadcast();
		registerReceiver(pb, downBroadcast);
		/*
		 * IntentFilter openBroadcast = new IntentFilter();
		 * openBroadcast.addAction("open.menu"); OpenMenuBroadcast ob = new
		 * OpenMenuBroadcast(); registerReceiver(ob, openBroadcast);
		 */
		int currentTab = tabHost.getCurrentTab();
		LogUtils.d("asfsaf", currentTab + "");
		switch (currentTab) {
		case 0:
			tv1.setCompoundDrawablesWithIntrinsicBounds(null, findGame_focue,
					null, null);
			break;
		case 3:
			tv4.setCompoundDrawablesWithIntrinsicBounds(null, manager_focue,
					null, null);
			break;
		case 4:
			tv5.setCompoundDrawablesWithIntrinsicBounds(null, local_focue,
					null, null);
			break;
		}
		PushManager.startWork(this, PushConstants.LOGIN_TYPE_API_KEY,
				"RYganXncxIQeKDe8tsOzUdZp");
		int height = tabHost.getHeight();
		LogUtils.d("MyTabHost", height + "height");
	}

	private void init() {
		MobclickAgent.setDebugMode(true);
		findGame_normal = getResources().getDrawable(
				R.drawable.main_tab_recommand_icon_normal);
		findGame_focue = getResources().getDrawable(
				R.drawable.main_tab_recommand_icon_selected);
		local_focue = getResources().getDrawable(
				R.drawable.main_tab_play_selected);
		local_normal = getResources().getDrawable(
				R.drawable.main_tab_play_normal);
		manager_focue = getResources().getDrawable(
				R.drawable.main_tab_top_icon_selected);
		manager_normal = getResources().getDrawable(
				R.drawable.main_tab_top_icon_normal);
		tv4.setCompoundDrawablesWithIntrinsicBounds(null, manager_normal, null,
				null);
		tv4.setText("排行");
		down_anim = (ImageView) findViewById(R.id.down_anim);
		try {
			Field idcurrent = tabHost.getClass()
					.getDeclaredField("mCurrentTab");
			idcurrent.setAccessible(true);
			idcurrent.setInt(tabHost, -2);
		} catch (Exception e) {
			e.printStackTrace();
		}

		tabHost.addTab(tabHost.newTabSpec("findgame").setIndicator(bottomView1)
				.setContent(new Intent(this, MainActivity.class)));
		/*
		 * tabHost.addTab(tabHost.newTabSpec("222").setIndicator(bottomView2)
		 * .setContent(new Intent(this, MainActivity.class)));
		 */

		Intent tabIntent = new Intent(MyTableHost.this, LocalGameActivity.class);
		Bundle bundle = new Bundle();
		bundle.putInt("width", width);
		tabIntent.putExtras(bundle);
		tabHost.addTab(tabHost.newTabSpec("localgame").setIndicator(bottomView5)
				.setContent(tabIntent));
		/*tabHost.addTab(tabHost.newTabSpec("localgame")
				.setIndicator(bottomView5)
				.setContent(new Intent(this, LocalGameActivity.class)));*/
		/*
		 * tabHost.addTab(tabHost.newTabSpec("333").setIndicator(bottomView3)
		 * .setContent(new Intent(this, MainActivity.class)));
		 */
		tabHost.addTab(tabHost.newTabSpec("manger").setIndicator(bottomView4)
				.setContent(new Intent(this, RankActivity.class)));
		try {
			Field idcurrent = tabHost.getClass()
					.getDeclaredField("mCurrentTab");
			idcurrent.setAccessible(true);
			if (tadid == 0) {
				idcurrent.setInt(tabHost, 1);
			} else {
				idcurrent.setInt(tabHost, 0);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		tabHost.setCurrentTab(1);


		/*
		 * if(NetworkUtils.isNetworkConnected(this)) { tabHost.setCurrentTab(0);
		 * } else { tabHost.setCurrentTab(1); }
		 */
		// 预加载内容
		new AsyncTask<Void, Void, Void>() {

			@Override
			protected Void doInBackground(Void... params) {
				String str = ToolHelper.donwLoadToString(Global.GAME_MAIN_URL
						+ Global.RANK_PAGE);
				ParseRankJson(str);
				String str2 = ToolHelper.donwLoadToString(Global.GAME_MAIN_URL
						+ Global.HOME_PAGE);
				ParseHomeJson(str2);
				return null;
			}

			protected void onPostExecute(Void result) {
				MarketApplication.getInstance().setRankAppInfos(appRankInfos);
				MarketApplication.getInstance().setHomeAppInfos(
						appHomeInfos_temp);
				myHandler.sendEmptyMessage(INMAIN);
				for (AppInfo ai : appHomeInfos_temp) {
					dsb = new DownStateBroadcast();
					IntentFilter filter = new IntentFilter();
					String fileName = DownloadService.CreatFileName(
							ai.getAppName()).getAbsolutePath();
					filter.addAction(fileName + "down");
					registerReceiver(dsb, filter);
				}
				for (AppInfo ai : appRankInfos) {
					dsbRank = new DownStateBroadcastRank();
					IntentFilter filter = new IntentFilter();
					String fileName = DownloadService.CreatFileName(
							ai.getAppName()).getAbsolutePath();
					filter.addAction(fileName + "down");
					registerReceiver(dsbRank, filter);
				}
			};

		}.execute();

		tv5.setCompoundDrawablesWithIntrinsicBounds(null, local_focue, null,
				null);
		tabHost.setOnTabChangedListener(new OnTabChangeListener() {
			@Override
			public void onTabChanged(String tabId) {
				if (tabId.equals("findgame")) {
					tv1.setCompoundDrawablesWithIntrinsicBounds(null,
							findGame_focue, null, null);
					tv4.setCompoundDrawablesWithIntrinsicBounds(null,
							manager_normal, null, null);
					tv5.setCompoundDrawablesWithIntrinsicBounds(null,
							local_normal, null, null);
					LogUtils.d("tv1", "diasnle");
				} else if (tabId.equals("localgame")) {
					tv1.setCompoundDrawablesWithIntrinsicBounds(null,
							findGame_normal, null, null);
					tv4.setCompoundDrawablesWithIntrinsicBounds(null,
							manager_normal, null, null);
					tv5.setCompoundDrawablesWithIntrinsicBounds(null,
							local_focue, null, null);
				} else if (tabId.equals("manger")) {
					tv1.setCompoundDrawablesWithIntrinsicBounds(null,
							findGame_normal, null, null);
					tv4.setCompoundDrawablesWithIntrinsicBounds(null,
							manager_focue, null, null);
					tv5.setCompoundDrawablesWithIntrinsicBounds(null,
							local_normal, null, null);
				}
			}
		});

	}

	private void findview() {
		tv1 = (TextView) bottomView1.findViewById(R.id.main_bottom_tv);
		tv2 = (TextView) bottomView2.findViewById(R.id.main_bottom_tv);
		tv3 = (TextView) bottomView3.findViewById(R.id.main_bottom_tv);
		tv4 = (TextView) bottomView4.findViewById(R.id.main_bottom_tv);
		tv5 = (TextView) bottomView5.findViewById(R.id.main_bottom_local_tv);
	}

	// 播放下载动画广播
	class PlayAnimBroadcast extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			down_anim.setVisibility(View.VISIBLE);
			AnimationDrawable ad = (AnimationDrawable) down_anim.getDrawable();
			ad.start();
			int duration = 0;

			for (int i = 0; i < ad.getNumberOfFrames(); i++) {

				duration += ad.getDuration(i);
			}
			Handler handler = new Handler();

			handler.postDelayed(new Runnable() {

				public void run() {
					down_anim.setVisibility(View.INVISIBLE);
				}

			}, duration);
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
			/*
			 * ArrayList<Activity> appLication =
			 * MarketApplication.getInstance().getAppLication(); for(Activity at
			 * : appLication) { at.finish(); } System.exit(0);
			 * android.os.Process.killProcess(android.os.Process.myPid());
			 * finish();
			 */
			// this.finish();
		}
		return super.onKeyDown(keyCode, event);
	}

	/*
	 * //打开分页菜单广播 class OpenMenuBroadcast extends BroadcastReceiver {
	 * 
	 * @Override public void onReceive(Context context, Intent intent) { // TODO
	 * Auto-generated method stub tw.setVisibility(View.INVISIBLE); }
	 * 
	 * }
	 */
	// 解析Rank
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
						Global.MAIN_URL + appiconurl, appurl, appDownCount, "",
						apppkgname);
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

	private void ParseHomeJson(String str) {
		try {
			// Log.e("tag", "--------2--------");
			JSONArray jsonArray = new JSONArray(str);
			LogUtils.d("descr", str);
			int len = jsonArray.length();
			LogUtils.d("len", len + "ge");
			for (int i = 0; i < len; i++) {
				JSONObject jsonObject = jsonArray.getJSONObject(i);
				String appName = jsonObject.getString("appname");
				String appiconurl = jsonObject.getString("appiconurl");
				String appSize = jsonObject.getString("appsize");
				String idx = jsonObject.getString("idx");
				String appurl = jsonObject.getString("appurl");
				String appdes = jsonObject.getString("appdes");
				String recoPic = jsonObject.getString("recoPic");
				String apppkgname = jsonObject.getString("apppkgname");
				AppInfo appInfo = new AppInfo(idx, appName, appSize,
						Global.MAIN_URL + appiconurl, appurl, "", appdes,
						apppkgname);
				appInfo.setPackageName(apppkgname);
				appInfo.setLastTime(Long.MAX_VALUE);
				if (recoPic == null) {
					String appimgurl = jsonObject.getString("appimgurl");
					String[] appImgurls = appimgurl.split(",");
					appInfo.setAppimgurl(appImgurls);
				}

				appInfo.setRecoPic(recoPic);
				appInfo.setInstalled(AppUtils.isInstalled(jsonObject
						.getString("apppkgname")));
				appHomeInfos_temp.add(appInfo);

				// Log.e("tag", "info = " + appInfo.toString());
			}
			// Log.e("tag", "--------------2--------");
		} catch (Exception ex) {
			Log.e("tag", "error = " + ex.getMessage());
		}
	}

	class DownStateBroadcast extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			String fileName = null;
			LogUtils.d("MAINActivity", "我接受到了暂停广播");
			for (AppInfo ai : appHomeInfos_temp) {
				fileName = DownloadService.CreatFileName(ai.getAppName())
						.getAbsolutePath() + "down";
				if (fileName.equals(intent.getAction())) {
					boolean downState = intent
							.getBooleanExtra("isPause", false);
					ai.setIspause(downState);
					// appHomeAdapter.notifyDataSetChanged();
					LogUtils.d("Mainctivity",
							"我更新了ui" + ai.getAppName() + ai.isIspause());
					break;
				}
			}
		}

	}

	class DownStateBroadcastRank extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			String fileName = null;
			LogUtils.d("MAINActivity", "我接受到了暂停广播");
			for (AppInfo ai : appRankInfos) {
				fileName = DownloadService.CreatFileName(ai.getAppName())
						.getAbsolutePath() + "down";
				if (fileName.equals(intent.getAction())) {
					boolean downState = intent
							.getBooleanExtra("isPause", false);
					ai.setIspause(downState);
					// appHomeAdapter.notifyDataSetChanged();
					LogUtils.d("Mainctivity",
							"我更新了ui" + ai.getAppName() + ai.isIspause());
					break;
				}
			}
		}

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		for (AppInfo ai : appHomeInfos_temp) {
			dsb = new DownStateBroadcast();
			IntentFilter filter = new IntentFilter();
			String fileName = DownloadService.CreatFileName(
					ai.getAppName()).getAbsolutePath();
			filter.addAction(fileName + "down");
			registerReceiver(dsb, filter);
		}
		for (AppInfo ai : appRankInfos) {
			dsbRank = new DownStateBroadcastRank();
			IntentFilter filter = new IntentFilter();
			String fileName = DownloadService.CreatFileName(
					ai.getAppName()).getAbsolutePath();
			filter.addAction(fileName + "down");
			registerReceiver(dsbRank, filter);
		}
	}
}
