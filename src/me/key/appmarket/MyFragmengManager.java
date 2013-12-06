package me.key.appmarket;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import me.key.appmarket.adapter.MenuCategoryAdapter;
import me.key.appmarket.tool.DownloadService;
import me.key.appmarket.tool.ToolHelper;
import me.key.appmarket.utils.AppInfo;
import me.key.appmarket.utils.AppUtils;
import me.key.appmarket.utils.CategoryInfo;
import me.key.appmarket.utils.Global;
import me.key.appmarket.utils.HttpClientUtil;
import me.key.appmarket.utils.LocalUtils;
import me.key.appmarket.utils.LogUtils;
import me.key.appmarket.utils.ToastUtils;
import net.tsz.afinal.FinalDb;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.ViewSwitcher;

import com.market.d9game.R;
import com.slidingmenu.lib.app2.SlidingFragmentActivity;
import com.slidingmenu.lib2.SlidingMenu;
import com.slidingmenu.lib2.SlidingMenu.OnCloseListener;
import com.slidingmenu.lib2.SlidingMenu.OnOpenedListener;

/**
 * 侧滑菜单按钮
 * 
 * @author Administrator
 * 
 */
public class MyFragmengManager extends SlidingFragmentActivity implements
		OnClickListener {
	private static final int RESETQUIT = 0;
	private static final int INMAIN = 2;
	private boolean mPreparedQuit = false;
	private ListView lv;
	// 寻找游戏
	private ImageView findgame;
	private RelativeLayout findApp;
	// 管理游戏
	private TextView localgame;

	// 排行游戏
	private ImageView rankgame;
	private RelativeLayout rankApp;
	private MenuCategoryAdapter menuCategoryAdapter;
	private FinalDb db;
	// 本地sd卡地址
	private String root;
	// SD卡游戏
	private List<AppInfo> mAppInfos = new ArrayList<AppInfo>();
	// Rank
	private ArrayList<AppInfo> appRankInfos;
	// Home
	private List<AppInfo> appHomeInfos_temp;
	// 本机已安装
	private ArrayList<AppInfo> appManaInfos_temp;
	private ArrayList<AppInfo> appManagerUpdateInfos_t = new ArrayList<AppInfo>();
	private String apknamelist;
	private ArrayList<AppInfo> appManagerUpdateInfos = new ArrayList<AppInfo>();
	private ArrayList<AppInfo> downApplist = new ArrayList<AppInfo>();
	private List<CategoryInfo> gcategoryInfoList_temp = new ArrayList<CategoryInfo>();
	//本地游戏派讯
	private List<AppInfo> localtopList;
	private RankFragment f1;
	public SlidingMenu menu;
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
	private FragmentTransaction ft;
	private FragmentManager fm;
	private LocalGameFragment lf;
	private MainActivityFragment mf;
	private FragmentTransaction fragmentTransaction;
	private Drawable findGame_normal;
	private Drawable findGame_focue;
	private Drawable local_focue;
	private Drawable local_normal;
	private Drawable manager_focue;
	private Drawable manager_normal;
	private FrameLayout localApp;
	private DownStateBroadcast dsb;
	private DownStateBroadcastRank dsbRank;
	private View errorview;
	private LayoutInflater inflater;
	private TextView findgameTv;
	private TextView rankTv;
	private View btnRefsh;

	@Override
	public void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setContentView(R.layout.main_bottom);
		setBehindContentView(R.layout.slide_menu);
		SharedPreferences sp = getSharedPreferences("cleandb", MODE_PRIVATE);
		boolean cleanDb = sp.getBoolean("db", false);
		if (!cleanDb) {
			cleanDatabases(this);
			Editor edit = sp.edit();
			edit.putBoolean("db", true);
			edit.commit();
		}
		MarketApplication.getInstance().getAppLication().add(this);
		startService(new Intent(this, DownloadService.class));
		appRankInfos = MarketApplication.getInstance().getRankappinfos();
		appHomeInfos_temp = MarketApplication.getInstance().getHomeAppInfos();
		db = FinalDb.create(this);
		findgame = (ImageView) findViewById(R.id.findgame);
		findApp = (RelativeLayout) findViewById(R.id.rl_findapp_main_bottom);
		localgame = (TextView) findViewById(R.id.localgame);
		localgame.setTextColor(getResources().getColor(R.color.focus));

		findgameTv = (TextView) findViewById(R.id.tv_mainbottom_findgame);
		rankTv = (TextView) findViewById(R.id.tv_mainbottom_rank);
		rankgame = (ImageView) findViewById(R.id.rankgame);
		rankApp = (RelativeLayout) findViewById(R.id.rl_rankapp_main_bottom);
		inflater = LayoutInflater.from(this);
		errorview = findViewById(R.id.errorview);
		btnRefsh = errorview.findViewById(R.id.btn_Refsh);
		btnRefsh.setOnClickListener(this);
		localApp = (FrameLayout) findViewById(R.id.fl_localapp_main_bottom);
		findApp.setOnClickListener(this);
		rankApp.setOnClickListener(this);
		localApp.setOnClickListener(this);
		fm = getSupportFragmentManager();
		// 获取sd卡地址
		root = LocalUtils.getRoot(this);
		fragmentTransaction = getSupportFragmentManager().beginTransaction();
		findGame_normal = getResources().getDrawable(R.drawable.findgame);
		findGame_focue = getResources().getDrawable(R.drawable.findgame_focus);
		local_focue = getResources().getDrawable(R.drawable.localgame_fouce);
		local_normal = getResources().getDrawable(R.drawable.local);
		manager_focue = getResources().getDrawable(R.drawable.rank_selected);
		manager_normal = getResources().getDrawable(R.drawable.rank);
		localgame.setCompoundDrawablesWithIntrinsicBounds(null, local_focue,
				null, null);
		// 预加载内容
		new AsyncTask<Void, Void, Void>() {

			@Override
			protected Void doInBackground(Void... params) {
				String str = ToolHelper.donwLoadToString(Global.GAME_MAIN_URL
						+ Global.RANK_PAGE);
				if (str.isEmpty()) {
					appRankInfos = new ArrayList<AppInfo>();
				} else {
					ParseRankJson(str);
				}
				String str2 = ToolHelper.donwLoadToString(Global.GAME_MAIN_URL
						+ Global.HOME_PAGE);
				if (str2.isEmpty()) {
					appHomeInfos_temp = new ArrayList<AppInfo>();
				} else {
					ParseHomeJson(str2);
				}
				appManaInfos_temp = AppUtils.getUserApps(
						MyFragmengManager.this, 4000);
				List<AppInfo> mAppInfos_temp = new ArrayList<AppInfo>();
				List<PackageInfo> packages = getPackageManager()
						.getInstalledPackages(0);
				mAppInfos_temp = LocalUtils.InitHomePager("0",
						MyFragmengManager.this, root+"d9dir/", packages);
				mAppInfos.addAll(mAppInfos_temp);
				ArrayList<AppInfo> userApps = AppUtils.getUserApps(
						MyFragmengManager.this, 4000);
				apknamelist = AppUtils
						.getInstallAppPackage(MyFragmengManager.this);
				if (apknamelist == null) {
					appManagerUpdateInfos_t = new ArrayList<AppInfo>();
				} else {
					String str3 = ToolHelper.donwLoadToString(Global.MAIN_URL
							+ Global.UPGRADEVERSION + "?apknamelist="
							+ apknamelist);
					ParseUpdateJson(str3);
				}
				if (userApps == null) {
					appManagerUpdateInfos = new ArrayList<AppInfo>();
				} else {
					appManagerUpdateInfos_t = AppUtils.getCanUpadateApp(
							userApps, appManagerUpdateInfos_t);
					appManagerUpdateInfos.clear();
					appManagerUpdateInfos.addAll(appManagerUpdateInfos_t);
				}
				List<AppInfo> down_temp = new ArrayList<AppInfo>();
				down_temp = db.findAll(AppInfo.class);
				downApplist.clear();
				downApplist.addAll(down_temp);
				Collections.reverse(downApplist);
				String str4 = ToolHelper.donwLoadToString(Global.MAIN_URL
						+ Global.APP_CATEGORY + "?type=" + 2);
				LogUtils.d("Local", "runCategoryData" + str4);
				if (str4.isEmpty()) {
					gcategoryInfoList_temp = null;
					LogUtils.d("Local", "runCategoryData" + str4 + "str4");

				} else {
					ParseCategoryJson(str4);
				}
				loadLocaltopList();
				return null;
			}

			protected void onPostExecute(Void result) {
				MarketApplication.getInstance().setmAppInfos(mAppInfos);
				MarketApplication.getInstance().setAppManaInfos_temp(
						appManaInfos_temp);
				MarketApplication.getInstance().setDownApplist(downApplist);
				MarketApplication.getInstance().setRankAppInfos(appRankInfos);
				MarketApplication.getInstance().setAppManagerUpdateInfos(
						appManagerUpdateInfos);
				MarketApplication.getInstance().setHomeAppInfos(
						appHomeInfos_temp);
				myHandler.sendEmptyMessage(INMAIN);
				lf = new LocalGameFragment();
				mf = new MainActivityFragment();
				ft = fm.beginTransaction();
				f1 = new RankFragment();
				// 把对应的view对象区域替换成f1
				// ft.replace(R.id.tabcontent, lf);
				ft.add(R.id.tabcontent, mf);
				ft.add(R.id.tabcontent, f1);
				ft.add(R.id.tabcontent, lf);
				ft.show(lf);
				ft.commitAllowingStateLoss();
				if (appHomeInfos_temp != null && appRankInfos != null) {
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

				final MenuFragment menuFragment = new MenuFragment();
				fragmentTransaction.replace(R.id.slide_content, menuFragment);
				// fragmentTransaction.replace(R.id.content, new
				// ContentFragment());
				fragmentTransaction.commitAllowingStateLoss();

				LogUtils.d("Main", "我已经被加载了哟");
				lv = (ListView) findViewById(R.id.category_lv);
				lv.setDividerHeight(0);
				LogUtils.d("Main", lv + "");
				/*
				 * ImageButton search_btn = (ImageButton)
				 * findViewById(R.id.search_btn);
				 * search_btn.setOnClickListener(new OnClickListener() {
				 * 
				 * @Override public void onClick(View v) { menu.toggle(); } });
				 */
				MyInstalledReceiver installedReceiver = new MyInstalledReceiver();
				IntentFilter filter = new IntentFilter();

				filter.addAction("android.intent.action.PACKAGE_ADDED");
				filter.addDataScheme("package");
				registerReceiver(installedReceiver, filter);
				LogUtils.d("Main1", menuCategoryAdapter + "");
				menu = getSlidingMenu();
				menu.setMode(SlidingMenu.LEFT);
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
						/*
						 * Intent intent = new Intent();
						 * intent.setAction("open.menu"); sendBroadcast(intent);
						 */
					}
				});
				final EditText etSearcher = (EditText)findViewById(R.id.et_search);
				etSearcher.setOnEditorActionListener(new OnEditorActionListener(){

					@Override
					public boolean onEditorAction(TextView tv, int antionId,
							KeyEvent event) {
						// TODO Auto-generated method stub
						Intent intent = new Intent();
						intent.putExtra("Search", etSearcher.getText().toString());
						etSearcher.setText("");
						intent.setClass(MyFragmengManager.this,
								SearchActivity.class);
						startActivity(intent);
						LogUtils.d("MAIN", "动画前");
						MyFragmengManager.this.overridePendingTransition(
								R.anim.left_anim, R.anim.right_anim);
						LogUtils.d("MAIN", "动画后");
						return false;
					}
					
				});
				/*LinearLayout etSeacher = (LinearLayout) findViewById(R.id.menu_search);
				etSeacher.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						Intent intent = new Intent();
						intent.setClass(MyFragmengManager.this,
								SearchActivity.class);
						startActivity(intent);
						LogUtils.d("MAIN", "动画前");
						MyFragmengManager.this.overridePendingTransition(
								R.anim.left_anim, R.anim.right_anim);
						LogUtils.d("MAIN", "动画后");
					}
				});*/
				if (gcategoryInfoList_temp == null) {
					errorview.setVisibility(View.VISIBLE);
				} else {
					menuCategoryAdapter = new MenuCategoryAdapter(
							MyFragmengManager.this, gcategoryInfoList_temp, lv);

					lv.setAdapter(menuCategoryAdapter);

					// lv.getChildAt(0).setBackgroundColor(getResources().getColor(R.color.classiv_cloor));
					// lv.getChildAt(0).findViewById(R.id.click_menu).setVisibility(View.VISIBLE);
					LogUtils.d("Main", lv.getChildCount() + "");

					lv.setOnItemClickListener(new OnItemClickListener() {

						@Override
						public void onItemClick(AdapterView<?> parent,
								View view, int position, long id) {

							menuFragment.updata(position);
							view.setBackgroundResource(R.drawable.slidingmenu_left_background_focus);
							for (int i = 0; i < lv.getChildCount(); i++) {
								if (i == position) {
									continue;
								}
								LogUtils.d("Main", i + "");
								lv.getChildAt(i).setBackgroundResource(
										R.drawable.slidingmenu_left_background);
							}
						}
					});
				}
			};

		}.execute();

	}

	// 解析Rank
	private void ParseRankJson(String str) {
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
			}
			// rankHandler.sendEmptyMessage(Global.DOWN_DATA_RANK_SUCCESSFUL);
		} catch (Exception ex) {
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

	private void ParseCategoryJson(String str) {
		try {
			JSONArray jsonArray = new JSONArray(str);
			int len = jsonArray.length();
			for (int i = 0; i < len; i++) {
				JSONObject jsonObject = jsonArray.getJSONObject(i);
				String id = jsonObject.getString("id");
				String name = jsonObject.getString("name");
				String type1 = jsonObject.getString("type1");
				String type2 = jsonObject.getString("type2");
				String appUrl = jsonObject.getString("appiconurl");
				String totalNum = jsonObject.getString("total_num");
				CategoryInfo mCategoryInfo = new CategoryInfo(id, name, type1,
						type2, Global.MAIN_URL + appUrl);
				mCategoryInfo.setTotalNum(totalNum);
				gcategoryInfoList_temp.add(mCategoryInfo);
			}
			if (len == 0) {
				CategoryInfo mCategoryInfo = new CategoryInfo("1", "失误", "1",
						"2", Global.MAIN_URL + "no");
				gcategoryInfoList_temp.add(mCategoryInfo);
			}
			/*
			 * categoryDataHandler
			 * .sendEmptyMessage(Global.DOWN_DATA_HOME_SUCCESSFULL);
			 */
		} catch (Exception ex) {
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

				for (AppInfo mAppInfo : appRankInfos) {
					if (packageName != null
							&& packageName.equals(mAppInfo.getPackageName())) {
						mAppInfo.setInstalled(true);
						mAppInfo.setCanUpdate(false);
						LogUtils.d("Search", "我接收到了安装" + packageName);
						break;
					}
				}

			}
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.rl_findapp_main_bottom:
			FragmentTransaction ft = fm.beginTransaction();
			// ft.replace(R.id.tabcontent, mf);
			// ft.addToBackStack(null);
			if (lf.isAdded()) {
				ft.hide(lf);
			}
			if (f1.isAdded()) {
				ft.hide(f1);
			}
			ft.show(mf);
			ft.commit();
			findgame.setImageResource(R.drawable.findgame_focus);
			findgameTv.setTextColor(getResources().getColor(R.color.focus));
			localgame.setCompoundDrawablesWithIntrinsicBounds(null,
					local_normal, null, null);
			localgame.setTextColor(getResources().getColor(R.color.normal));
			rankgame.setImageResource(R.drawable.rank);
			rankTv.setTextColor(getResources().getColor(R.color.normal));
			break;
		case R.id.fl_localapp_main_bottom:
			findgame.setImageResource(R.drawable.findgame);
			findgameTv.setTextColor(getResources().getColor(R.color.normal));
			localgame.setCompoundDrawablesWithIntrinsicBounds(null,
					local_focue, null, null);
			localgame.setTextColor(getResources().getColor(R.color.focus));
			rankgame.setImageResource(R.drawable.rank);
			rankTv.setTextColor(getResources().getColor(R.color.normal));
			FragmentTransaction ft2 = fm.beginTransaction();
			// ft2.replace(R.id.tabcontent, lf);
			// ft2.addToBackStack(null);
			if (f1.isAdded()) {
				ft2.hide(f1);
			}
			if (mf.isAdded()) {
				ft2.hide(mf);
			}
			ft2.show(lf);
			ft2.commit();
			break;
		case R.id.rl_rankapp_main_bottom:
			findgame.setImageResource(R.drawable.findgame);
			findgameTv.setTextColor(getResources().getColor(R.color.normal));
			localgame.setCompoundDrawablesWithIntrinsicBounds(null,
					local_normal, null, null);
			localgame.setTextColor(getResources().getColor(R.color.normal));
			rankgame.setImageResource(R.drawable.rank_selected);
			rankTv.setTextColor(getResources().getColor(R.color.focus));
			FragmentTransaction ft1 = fm.beginTransaction();
			// ft1.replace(R.id.tabcontent, f1);
			// ft1.addToBackStack(null);
			if (lf.isAdded()) {
				ft1.hide(lf);
			}
			if (mf.isAdded()) {
				ft1.hide(mf);
			}
			ft1.show(f1);
			ft1.commit();
			break;
		case R.id.btn_Refsh :
			new AsyncTask<Void, Void, Void>() {

				@Override
				protected Void doInBackground(Void... params) {
					String str4 = ToolHelper.donwLoadToString(Global.MAIN_URL
							+ Global.APP_CATEGORY + "?type=" + 2);
					LogUtils.d("Local", "runCategoryData" + str4);
					if (str4.isEmpty()) {
						gcategoryInfoList_temp = null;
						LogUtils.d("Local", "runCategoryData" + str4 + "str4");
						
					} else {
						gcategoryInfoList_temp = new ArrayList<CategoryInfo>();
						ParseCategoryJson(str4);
					}
					return null;
				}

				protected void onPostExecute(Void result) {
					errorview.setVisibility(View.INVISIBLE);
					final MenuFragment menuFragment = new MenuFragment();
					fragmentTransaction = getSupportFragmentManager().beginTransaction();
					fragmentTransaction.replace(R.id.slide_content, menuFragment);
					fragmentTransaction.commitAllowingStateLoss();
					// fragmentTransaction.replace(R.id.content, new
					// ContentFragment());

					LogUtils.d("Main", "我已经被加载了哟");
					lv = (ListView) findViewById(R.id.category_lv);
					lv.setDividerHeight(0);
					LogUtils.d("Main", lv + "");
					/*
					 * ImageButton search_btn = (ImageButton)
					 * findViewById(R.id.search_btn);
					 * search_btn.setOnClickListener(new OnClickListener() {
					 * 
					 * @Override public void onClick(View v) { menu.toggle(); } });
					 */
					MyInstalledReceiver installedReceiver = new MyInstalledReceiver();
					IntentFilter filter = new IntentFilter();

					filter.addAction("android.intent.action.PACKAGE_ADDED");
					filter.addDataScheme("package");
					registerReceiver(installedReceiver, filter);
					LogUtils.d("Main1", menuCategoryAdapter + "");
					menu = getSlidingMenu();
					menu.setMode(SlidingMenu.LEFT);
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
							/*
							 * Intent intent = new Intent();
							 * intent.setAction("open.menu"); sendBroadcast(intent);
							 */
						}
					});
					LinearLayout etSeacher = (LinearLayout) findViewById(R.id.menu_search);
					etSeacher.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							Intent intent = new Intent();
							intent.setClass(MyFragmengManager.this,
									SearchActivity.class);
							startActivity(intent);
							LogUtils.d("MAIN", "动画前");
							MyFragmengManager.this.overridePendingTransition(
									R.anim.left_anim, R.anim.right_anim);
							LogUtils.d("MAIN", "动画后");
						}
					});
					if (gcategoryInfoList_temp == null) {
						errorview.setVisibility(View.VISIBLE);
						LogUtils.d("Main",  "gcategoryInfoList_temp我还是空的");
					} else {
						menuCategoryAdapter = new MenuCategoryAdapter(
								
								MyFragmengManager.this, gcategoryInfoList_temp, lv);

						lv.setAdapter(menuCategoryAdapter);
						menuCategoryAdapter.notifyDataSetChanged();
						// lv.getChildAt(0).setBackgroundColor(getResources().getColor(R.color.classiv_cloor));
						// lv.getChildAt(0).findViewById(R.id.click_menu).setVisibility(View.VISIBLE);
						LogUtils.d("Main", lv.getChildCount() + "lv.getChildCount()"+gcategoryInfoList_temp.size());

						lv.setOnItemClickListener(new OnItemClickListener() {

							@Override
							public void onItemClick(AdapterView<?> parent,
									View view, int position, long id) {

								menuFragment.updata(position);
								view.setBackgroundResource(R.drawable.slidingmenu_left_background_focus);
								for (int i = 0; i < lv.getChildCount(); i++) {
									if (i == position) {
										continue;
									}
									LogUtils.d("Main", i + "");
									lv.getChildAt(i).setBackgroundResource(
											R.drawable.slidingmenu_left_background);
								}
							}
						});
					}
				};

			}.execute();

			break;
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
			LogUtils.d("Main", "我发出了取消广播");

			stopService(new Intent(this, DownloadService.class));
	//		finish();
//			System.exit(0);
//			android.os.Process.killProcess(android.os.Process.myPid());

		}
		return super.onKeyDown(keyCode, event);
	}

	private static void deleteFilesByDirectory(File directory) {
		if (directory != null && directory.exists() && directory.isDirectory()) {
			for (File item : directory.listFiles()) {
				item.delete();
			}
		}
	}

	public static void cleanDatabases(Context context) {
		deleteFilesByDirectory(new File("/data/data/"
				+ context.getPackageName() + "/databases"));
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unRegister();
	}

	private void unRegister() {
		this.unregisterReceiver(dsb);
		this.unregisterReceiver(dsbRank);
	}

	public void getData() {
		String str = ToolHelper.donwLoadToString(Global.GAME_MAIN_URL
				+ Global.RANK_PAGE);
		if (str.isEmpty()) {
			appRankInfos = new ArrayList<AppInfo>();
		} else {
			ParseRankJson(str);
		}
		String str2 = ToolHelper.donwLoadToString(Global.GAME_MAIN_URL
				+ Global.HOME_PAGE);
		if (str2.isEmpty()) {
			appHomeInfos_temp = new ArrayList<AppInfo>();
		} else {
			ParseHomeJson(str2);
		}
		appManaInfos_temp = AppUtils.getUserApps(MyFragmengManager.this, 4000);
		List<AppInfo> mAppInfos_temp = new ArrayList<AppInfo>();
		List<PackageInfo> packages = getPackageManager()
				.getInstalledPackages(0);
		mAppInfos_temp = LocalUtils.InitHomePager("0", MyFragmengManager.this,
				root+"d9dir/", packages);
		mAppInfos.addAll(mAppInfos_temp);
		ArrayList<AppInfo> userApps = AppUtils.getUserApps(
				MyFragmengManager.this, 4000);
		apknamelist = AppUtils.getInstallAppPackage(MyFragmengManager.this);
		if (apknamelist == null) {
			appManagerUpdateInfos_t = new ArrayList<AppInfo>();
		} else {
			String str3 = ToolHelper.donwLoadToString(Global.MAIN_URL
					+ Global.UPGRADEVERSION + "?apknamelist=" + apknamelist);
			ParseUpdateJson(str3);
		}
		if (userApps == null) {
			appManagerUpdateInfos = new ArrayList<AppInfo>();
		} else {
			appManagerUpdateInfos_t = AppUtils.getCanUpadateApp(userApps,
					appManagerUpdateInfos_t);
			appManagerUpdateInfos.clear();
			appManagerUpdateInfos.addAll(appManagerUpdateInfos_t);
		}
		List<AppInfo> down_temp = new ArrayList<AppInfo>();
		down_temp = db.findAll(AppInfo.class);
		downApplist.clear();
		downApplist.addAll(down_temp);
		Collections.reverse(downApplist);
		String str4 = ToolHelper.donwLoadToString(Global.MAIN_URL
				+ Global.APP_CATEGORY + "?type=" + 2);
		LogUtils.d("Local", "runCategoryData" + str4);
		if (str4.isEmpty()) {
			gcategoryInfoList_temp = null;
			LogUtils.d("Local", "runCategoryData" + str4 + "str4");

		} else {
			ParseCategoryJson(str4);
		}
	}




	public void setLocaltopList(ArrayList<AppInfo> localtopList) {
		this.localtopList = localtopList;
	}

	public void loadLocaltopList() {
		try {
			
			InputStream is = HttpClientUtil.getInputStream(null,
					Global.LOCALTOPLISTURL);
			String body = HttpClientUtil.getString(is);
			if (body == null || body == "") {
			} else {
				localtopList =MarketApplication.getInstance().getLocaltopList();
				JSONArray jsonObjs = new JSONArray(body);
				for (int i = 0; i < jsonObjs.length(); i++) {
					AppInfo appInfo = new AppInfo();
					JSONObject jsonObj = jsonObjs.getJSONObject(i);
					appInfo.setPackageName(jsonObj.getString("apppkgname"));
					appInfo.setIdx(jsonObj.getString("idx"));
					localtopList.add(appInfo);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}