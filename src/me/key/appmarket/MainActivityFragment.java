package me.key.appmarket;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.tsz.afinal.FinalDb;

import org.json.JSONArray;
import org.json.JSONObject;

import me.key.appmarket.MainActivity.DownStateBroadcast;
import me.key.appmarket.MainActivity.MyInstalledReceiver;
import me.key.appmarket.MainActivity.MyOnPageChangeListener;
import me.key.appmarket.MainActivity.PrecentReceiver;
import me.key.appmarket.MyListView.OnLoadMoreListener;
import me.key.appmarket.adapter.AppAdapter;
import me.key.appmarket.adapter.DetaileAdapter;
import me.key.appmarket.adapter.ManagerAdapter;
import me.key.appmarket.adapter.ManagerUpdateAdapter;
import me.key.appmarket.adapter.MenuCategoryAdapter;
import me.key.appmarket.adapter.NewRankAdapter;
import me.key.appmarket.adapter.NewRecommnAdapter;
import me.key.appmarket.adapter.TabPageAdapter;
import me.key.appmarket.adapter.TuiJianImageAdapter;
import me.key.appmarket.tool.DownloadService;
import me.key.appmarket.tool.ToolHelper;
import me.key.appmarket.update.UpdateApk;
import me.key.appmarket.utils.AppInfo;
import me.key.appmarket.utils.AppUtils;
import me.key.appmarket.utils.CategoryInfo;
import me.key.appmarket.utils.Global;
import me.key.appmarket.utils.LogUtils;
import me.key.appmarket.widgets.GalleryFlow;

import com.market.d9game.R;
import com.slidingmenu.lib2.SlidingMenu;
import com.umeng.analytics.MobclickAgent;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView; 
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout.LayoutParams;

public class MainActivityFragment extends Fragment implements OnClickListener {
	private ViewPager mPager;
	private List<View> listViews;
	private TextView t1, t2, t3, t4, t5;
	private View homeView, gameView, rankView, managerView, logcalGmaeView;
	
	private boolean mPreparedQuit = false;
	private int headContentWidth;
	private int headContentHeight;

	private int startY;
	private int firstItemIndex;
	private OnLoadMoreListener loadMoreListener;
	private int state;

	private boolean isBack;
	private ImageButton search_btn;

	// home
	private ListView mHomeListView;
	private ArrayList<AppInfo> appHomeInfos;
	private List<AppInfo> appHomeInfos_temp = new LinkedList<AppInfo>();
	private NewRecommnAdapter appHomeAdapter;
	private ProgressBar pHomeBar;
	private String curpos = "0";
	private LinearLayout ll_homeerror;
	private ArrayList<AppInfo> bannerList = new ArrayList<AppInfo>();
	private ArrayList<AppInfo> appManagerUpdateInfos_t = new ArrayList<AppInfo>();
	private ArrayList<AppInfo> appManagerUpdateInfos = new ArrayList<AppInfo>();
	private GalleryFlow tuijian_gallery;
	private TuiJianImageAdapter tuiJianAdapter;
	// game
	private boolean isLoading = false;
	private boolean isFirst = true;
	private int x;
	private int y;
	private FinalDb db;
	private TextView updata_num;
	// 设置按钮
		private ImageButton setting;
	private PopupWindow pw;
	//下载和更新
		private TextView downandupdata;
		//检查更新
		private TextView checkupdata_pop;
		//退出
		private TextView getout_pop;
		private TextView about;
	private SlidingMenu menu;
	private MyListView mGameListView;
	private ProgressBar pGameBar;
	private LinearLayout ll_gameerror;
	private LinkedList<AppInfo> appGameInfos;
	private LinkedList<AppInfo> appGameInfos_temp = new LinkedList<AppInfo>();
	private AppAdapter appGameAdapter;
	private int game_page = 0; // 最后的可视项索引
	private LinearLayout gameLinearLayout;
	private TextView game_calss;
	private TextView game_boutique;
	private View loadGameMoreView;
	private Button loadGameMoreButton;
	private ArrayList<CategoryInfo> categoryInfoList = new ArrayList<CategoryInfo>();
	private ArrayList<CategoryInfo> categoryInfoList_temp = new ArrayList<CategoryInfo>();
	private ListView mListGame;
	private ArrayList<CategoryInfo> gcategoryInfoList = new ArrayList<CategoryInfo>();
	private ArrayList<CategoryInfo> gcategoryInfoList_temp = new ArrayList<CategoryInfo>();
	private int type = 2;
	private DetaileAdapter mCategoryAdapter;
	private TextView topbar_title1;
	private ImageView back_icon;
	private ImageView logo_title;
	private ImageView banner;
	// 管理
	private ListView mManagerListView;
	private ProgressBar pro_bar;
	private ManagerAdapter mManagerAdapter;
	private ManagerUpdateAdapter mManagerUpdateAdapter;
	private ArrayList<AppInfo> appManagerInfos = new ArrayList<AppInfo>();

	private Button install_app;
	private Button update_app;
	private boolean isShowingInstall = true;

	private String apknamelist;
	public static List<Activity> activities = new ArrayList<Activity>();
	private ListView lv;
	private View inflate;
	private Context context;
	// app

	File cache;
	// Rank
	private ListView mRankListView;
	private List<AppInfo> appRankInfos;
	private NewRankAdapter appRankAdapter;
	private ProgressBar pRankBar;
	private LinearLayout ll_rankerror;
	private MenuCategoryAdapter menuCategoryAdapter;
	private static final int DELAYTIME = 5000;
	private static final int RESETQUIT = 0;
	private static final int SHOWNEXT = 1;
	private static final int INMAIN = 2;
	private Handler myHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case RESETQUIT:
				mPreparedQuit = false;
				break;
			case SHOWNEXT:
				tuijian_gallery.setSelection(tuijian_gallery
						.getSelectedItemPosition() + 1);
				myHandler.sendEmptyMessageDelayed(SHOWNEXT, DELAYTIME);
				break;
			// 进入主界面
			}
		}
	};

	public View onCreateView(android.view.LayoutInflater inflater, android.view.ViewGroup container, android.os.Bundle savedInstanceState) {
		inflate = inflater.inflate(R.layout.main, container,false);
		return inflate;
	};
	public void onActivityCreated(android.os.Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		context = getActivity();
		db = FinalDb.create(context);
		// 自动升级，不能删
				updateSelf(true);
				InitViewPager();
				initHomeView();
				// initGameView();
				// initRankView();
				// initManagerView();
				// initLocalGameView();
				new Thread(runHomeData).start();
				// new Thread(runRankData).start();
				// new Thread(runBannerData).start();
				registerInstall();
			
	};
	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		registerPrecent();
		if(appHomeAdapter != null) {
		appHomeAdapter.notifyDataSetChanged();
		List<AppInfo> downList_temp = new ArrayList<AppInfo>();
		downList_temp = db.findAll(AppInfo.class);
		updata_num.setText(downList_temp.size()+MarketApplication.getInstance().getAppManagerUpdateInfos().size()+"");
		}
	}
	/**
	 * 检查更新
	 * 
	 * @param showToast
	 */
	private void updateSelf(boolean showToast) {
		Calendar c = Calendar.getInstance();
		int mDay = c.get(Calendar.DAY_OF_MONTH);// 获取当前月份的日期号码
		SharedPreferences sp = PreferenceManager.
				getDefaultSharedPreferences(getActivity());
		int lastDay = sp.getInt("day", 0); 
		if (lastDay != mDay) {
			try { 
				UpdateApk.checkUpdate(getActivity(), showToast, true);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		sp.edit().putInt("day", mDay).commit();
	}
	MyInstalledReceiver installedReceiver;
	private void registerInstall() {
		installedReceiver = new MyInstalledReceiver();
		IntentFilter filter = new IntentFilter();

		filter.addAction("android.intent.action.PACKAGE_ADDED");
		filter.addAction("android.intent.action.PACKAGE_REMOVED");
		filter.addDataScheme("package");

		getActivity().registerReceiver(installedReceiver, filter);
	}
	private void InitViewPager() {
		// TODO Auto-generated method stub\

		// t1 = (TextView) findViewById(R.id.text1);
		// t2 = (TextView) findViewById(R.id.text2);
		// t3 = (TextView) findViewById(R.id.text3);
		// t4 = (TextView) findViewById(R.id.text4);
		// t5 = (TextView) findViewById(R.id.text5);
		// t1.setSelected(true);
		// t1.setOnClickListener(new MyOnClickListener(0));
		// t2.setOnClickListener(new MyOnClickListener(1));
		// t3.setOnClickListener(new MyOnClickListener(2));
		// t4.setOnClickListener(new MyOnClickListener(3));
		// t5.setOnClickListener(new MyOnClickListener(4));

		mPager = (ViewPager)  inflate.findViewById(R.id.vPager);
		listViews = new ArrayList<View>();
		LayoutInflater mInflater = getActivity().getLayoutInflater();
		homeView = mInflater.inflate(R.layout.home, null);
		gameView = mInflater.inflate(R.layout.applist, null);
		rankView = mInflater.inflate(R.layout.rank, null);
		logcalGmaeView = mInflater.inflate(R.layout.type, null);
		managerView = mInflater.inflate(R.layout.app_managemer, null);
		listViews.add(homeView);
		// listViews.add(gameView);
		// listViews.add(logcalGmaeView);
		// listViews.add(rankView);
		// listViews.add(managerView);
		mPager.setOffscreenPageLimit(2);
		mPager.setAdapter(new TabPageAdapter(listViews));
		mPager.setCurrentItem(0);
		mPager.setOnPageChangeListener(new MyOnPageChangeListener());

		// 搜索按钮点击事件
		search_btn = (ImageButton) inflate.findViewById(R.id.search_btn);
		search_btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				MyFragmengManager myFragment = (MyFragmengManager) getActivity();
				myFragment.menu.toggle();
			}
		});
	}

	public class MyOnClickListener implements View.OnClickListener {
		private int index = 0;

		public MyOnClickListener(int i) {
			index = i;
		}

		public void onClick(View v) {
			mPager.setCurrentItem(index);
		}
	};

	public class MyOnPageChangeListener implements OnPageChangeListener {
		public void onPageSelected(int arg0) {
			switch (arg0) {
			case 0:
				t1.setSelected(true);
				// t2.setSelected(false);
				// t3.setSelected(false);
				// t4.setSelected(false);
				// t5.setSelected(false);
				break;
			case 1:
				/*
				 * if (appGameInfos == null || appGameInfos.size() <= 0) {
				 * appGameInfos.clear(); appGameAdapter.notifyDataSetChanged();
				 * pGameBar.setVisibility(View.VISIBLE);
				 * ll_gameerror.setVisibility(View.GONE); new
				 * Thread(runGameData).start(); }
				 */
				t1.setSelected(false);
				// t2.setSelected(true);
				// t3.setSelected(false);
				// t4.setSelected(false);
				// t5.setSelected(false);
				break;
			case 2:

				t1.setSelected(false);
				// t2.setSelected(false);
				// t3.setSelected(true);
				// t4.setSelected(true);
				// t5.setSelected(false);
				break;
			}
		}

		public void onPageScrolled(int arg0, float arg1, int arg2) {
		}

		public void onPageScrollStateChanged(int arg0) {
		}
	}

	public static boolean netIsAvail(Context context) {
		ConnectivityManager manager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (manager == null) {
			return false;
		}
		NetworkInfo networkinfo = manager.getActiveNetworkInfo();
		if (networkinfo == null || !networkinfo.isAvailable()) {
			return false;
		}
		return true;
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

				// 刷新管理界面
				appManagerInfos.clear();
				ArrayList<AppInfo> appManagerInfos1 = AppUtils.getUserApps(context
						, 4000);
				appManagerInfos.addAll(appManagerInfos1);
				appManagerInfos1.clear();
				if (mManagerAdapter != null) {
					mManagerAdapter.notifyDataSetChanged();
				}

				if (mPager != null) {
					int selectItem = mPager.getCurrentItem();
					switch (selectItem) {
					case 0:// 当前显示的是推荐界面
						for (AppInfo mAppInfo : appHomeInfos) {
							if (packageName != null
									&& packageName.equals(mAppInfo
											.getPackageName())) {
								mAppInfo.setInstalled(true);
								mAppInfo.setCanUpdate(false);
								break;
							}
						}

						appHomeAdapter.notifyDataSetChanged();
						break;
					case 1:// 游戏
						if (appGameInfos != null) {
							for (AppInfo mAppInfo : appGameInfos) {
								if (installAppName != null
										&& installAppName.equals(mAppInfo
												.getAppName())) {
									mAppInfo.setInstalled(true);
									break;
								}
							}
						}
						// appGameAdapter.notifyDataSetChanged();
					case 2:// 应用
						break;
					case 3:// 排行
						if (appRankInfos != null) {
							for (AppInfo mAppInfo : appRankInfos) {
								if (installAppName != null
										&& installAppName.equals(mAppInfo
												.getAppName())) {
									mAppInfo.setInstalled(true);
									break;
								}
							}
							appRankAdapter.notifyDataSetChanged();
						}
						break;
					case 4:// 管理
						if (!isShowingInstall) {
							update_app
									.setBackgroundResource(R.drawable.btn_bar_2);
							update_app.setPadding(40, 0, 40, 0);
							install_app.setBackgroundResource(0);

							apknamelist = AppUtils
									.getInstallAppPackage(getActivity());
							mManagerListView.setVisibility(View.GONE);

							pro_bar.setVisibility(View.VISIBLE);
							new Thread(runUpdateAppData).start();
						}
						break;
					}
				}
			}
			// 接收卸载广播
			if (intent.getAction().equals(
					"android.intent.action.PACKAGE_REMOVED")) {
				MarketApplication.getInstance().reflashAppList();
				String packageName = intent.getDataString().substring(8);
				LogUtils.d("YTL", "卸载了:" + packageName + "包名的程序");

				if (mPager != null) {
					int selectItem = mPager.getCurrentItem();
					switch (selectItem) {
					case 4:// 当前显示的是推荐界面
						LogUtils.d("YTL", "当前显示的是推荐界面");
						for (AppInfo mAppInfo : appManagerInfos) {
							if (packageName != null
									&& packageName.equals(mAppInfo
											.getPackageName())) {
								appManagerInfos.remove(mAppInfo);
								break;
							}
						}

						mManagerAdapter.notifyDataSetChanged();
						break;
					}
				}
			}
		}
	}

	PrecentReceiver mPrecentReceiver;
	Runnable runUpdateAppData = new Runnable() {
		@Override
		public void run() {
			String str = ToolHelper.donwLoadToString(Global.MAIN_URL
					+ Global.UPGRADEVERSION + "?apknamelist=" + apknamelist);

			// Log.e("tag", "runUpdateAppData result =" + str);
			if (str.equals("null")) {
				homeUpdateHandler
						.sendEmptyMessage(Global.DOWN_DATA_HOME_SUCCESSFULL);
			} else if (str.equals("-1")) {
				homeUpdateHandler
						.sendEmptyMessage(Global.DOWN_DATA_HOME_FAILLY);
			} else {
				ParseUpdateJson(str);
			}
		}
	};
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
	Handler homeUpdateHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			switch (msg.what) {
			case Global.DOWN_DATA_HOME_FAILLY: {
			}
				break;
			case Global.DOWN_DATA_HOME_SUCCESSFULL: {
				pro_bar.setVisibility(View.GONE);
				// Log.d("YTL", "appManagerUpdateInfos.size()  ="
				// + appManagerUpdateInfos.size());
				mManagerListView.setVisibility(View.VISIBLE);
				mManagerListView.setAdapter(mManagerUpdateAdapter);
				mManagerUpdateAdapter.notifyDataSetChanged();
				mManagerListView
						.setOnItemClickListener(new OnItemClickListener() {
							@Override
							public void onItemClick(AdapterView<?> parent,
									View view, int position, long id) {
								AppInfo mAppInfo = appManagerUpdateInfos
										.get(position);
								Intent intent = new Intent(getActivity(),
										AppDetailActivity.class);
								intent.putExtra("appid", mAppInfo.getIdx());
								startActivity(intent);
							}
						});
			}
			default:
				break;
			}
			super.handleMessage(msg);
		}
	};

	private void registerPrecent() {
		mPrecentReceiver = new PrecentReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(MarketApplication.PRECENT);
		getActivity().registerReceiver(mPrecentReceiver, filter);
	}

	private void unregisterPrecent() {
		if (mPrecentReceiver != null) {
			getActivity().unregisterReceiver(mPrecentReceiver);
		}
	}

	class PrecentReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(MarketApplication.PRECENT)) {
				// 下载中刷新界面进度
				if (mPager != null) {
					int selectItem = mPager.getCurrentItem();
					switch (selectItem) {
					case 0:// 当前显示的是推荐界面
						appHomeAdapter.notifyDataSetChanged();
						break;
					case 1:// 游戏
							// appGameAdapter.notifyDataSetChanged();
					case 2:// 应用
						break;
					case 3:// 排行
						appRankAdapter.notifyDataSetChanged();
						break;
					case 4:// 管理
						if (!isShowingInstall && mManagerUpdateAdapter != null) {
							mManagerUpdateAdapter.notifyDataSetChanged();
						}
						break;
					}
				}
			}
		}
	}

	public Map showUninstallAPKIcon(String apkPath) {
		String PATH_PackageParser = "android.content.pm.PackageParser";
		String PATH_AssetManager = "android.content.res.AssetManager";
		try {
			// apk����ļ�·��
			// ����һ��Package ������, �����ص�
			// ���캯��Ĳ���ֻ��һ��, apk�ļ���·��
			// PackageParser packageParser = new PackageParser(apkPath);
			Class pkgParserCls = Class.forName(PATH_PackageParser);
			Class[] typeArgs = new Class[1];
			typeArgs[0] = String.class;
			Constructor pkgParserCt = pkgParserCls.getConstructor(typeArgs);
			Object[] valueArgs = new Object[1];
			valueArgs[0] = apkPath;
			Object pkgParser = pkgParserCt.newInstance(valueArgs);
			// ���������ʾ�йص�, �����漰��һЩ������ʾ�ȵ�, ����ʹ��Ĭ�ϵ����
			DisplayMetrics metrics = new DisplayMetrics();
			metrics.setToDefaults();
			typeArgs = new Class[4];
			typeArgs[0] = File.class;
			typeArgs[1] = String.class;
			typeArgs[2] = DisplayMetrics.class;
			typeArgs[3] = Integer.TYPE;
			Method pkgParser_parsePackageMtd = pkgParserCls.getDeclaredMethod(
					"parsePackage", typeArgs);
			valueArgs = new Object[4];
			valueArgs[0] = new File(apkPath);
			valueArgs[1] = apkPath;
			valueArgs[2] = metrics;
			valueArgs[3] = 0;
			Object pkgParserPkg = pkgParser_parsePackageMtd.invoke(pkgParser,
					valueArgs);
			// Ӧ�ó�����Ϣ��, ���������, ������Щ����, ����û����
			LogUtils.d("pkg", pkgParserPkg + "");
			java.lang.reflect.Field appInfoFld = pkgParserPkg.getClass()
					.getDeclaredField("applicationInfo");
			ApplicationInfo info = (ApplicationInfo) appInfoFld
					.get(pkgParserPkg);
			Class assetMagCls = Class.forName(PATH_AssetManager);
			Constructor assetMagCt = assetMagCls.getConstructor((Class[]) null);
			Object assetMag = assetMagCt.newInstance((Object[]) null);
			typeArgs = new Class[1];
			typeArgs[0] = String.class;
			Method assetMag_addAssetPathMtd = assetMagCls.getDeclaredMethod(
					"addAssetPath", typeArgs);
			valueArgs = new Object[1];
			valueArgs[0] = apkPath;
			assetMag_addAssetPathMtd.invoke(assetMag, valueArgs);
			Resources res = getResources();
			typeArgs = new Class[3];
			typeArgs[0] = assetMag.getClass();
			typeArgs[1] = res.getDisplayMetrics().getClass();
			typeArgs[2] = res.getConfiguration().getClass();
			Constructor resCt = Resources.class.getConstructor(typeArgs);
			valueArgs = new Object[3];
			valueArgs[0] = assetMag;
			valueArgs[1] = res.getDisplayMetrics();
			valueArgs[2] = res.getConfiguration();
			res = (Resources) resCt.newInstance(valueArgs);
			CharSequence label = null;
			Map<String, Object> list = new HashMap<String, Object>();
			if (info.labelRes != 0) {
				label = res.getText(info.labelRes);
				list.put("label", label);
			} else {
				PackageManager pm = getActivity().getPackageManager();
				label = info.loadLabel(pm);
				list.put("label", label);
			}
			// ������Ƕ�ȡһ��apk�����ͼ��
			if (info.icon != 0) {
				Drawable icon = res.getDrawable(info.icon);
				list.put("icon", icon);
				return list;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	class DownStateBroadcast extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			String fileName = null;
			LogUtils.d("MAINActivity", "我接受到了暂停广播");
			for (AppInfo ai : appHomeInfos) {
				fileName = DownloadService.CreatFileName(ai.getAppName())
						.getAbsolutePath() + "down";
				if (fileName.equals(intent.getAction())) {
					boolean downState = intent
							.getBooleanExtra("isPause", false);
					ai.setIspause(downState);
					appHomeAdapter.notifyDataSetChanged();
					LogUtils.d("Mainctivity",
							"我更新了ui" + ai.getAppName() + ai.isIspause());
					break;
				}
			}
		}

	}
	Runnable runHomeData = new Runnable() {
		@Override
		public void run() {
		/*	String str = ToolHelper.donwLoadToString(Global.GAME_MAIN_URL
					+ Global.HOME_PAGE);
			// Log.e("tag", "result =" + str);
			if (str.equals("null")) {
				homeDataHandler
						.sendEmptyMessage(Global.DOWN_DATA_HOME_SUCCESSFULL);
			} else if (str.equals("-1")) {
				homeDataHandler.sendEmptyMessage(Global.DOWN_DATA_HOME_FAILLY);
			} else {
				// Log.e("tag", "--------------1-------------");
				ParseHomeJson(str);
			}*/
			pHomeBar.setVisibility(View.INVISIBLE);
			StringBuilder apknamelist = new StringBuilder();
			for (AppInfo ai : appHomeInfos) {
				DownStateBroadcast dsb = new DownStateBroadcast();
				IntentFilter filter = new IntentFilter();
				String fileName = DownloadService.CreatFileName(
						ai.getAppName()).getAbsolutePath();
				filter.addAction(fileName + "down");
				getActivity().registerReceiver(dsb, filter);
				apknamelist.append(ai.getPackageName() + ",");
				try {
					PackageManager pm = getActivity().getPackageManager();
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
			String str = ToolHelper.donwLoadToString(Global.MAIN_URL
					+ Global.UPGRADEVERSION + "?apknamelist=" + uris);
			ParseUpdateJson(str);
			appManagerUpdateInfos_t = AppUtils.getCanUpadateApp(
					appHomeInfos, appManagerUpdateInfos_t);
			appManagerUpdateInfos.clear();
			appManagerUpdateInfos.addAll(appManagerUpdateInfos_t);
			LogUtils.d("Main", "appUpdate" + appManagerUpdateInfos.size());
			for (AppInfo appInfo : appManagerUpdateInfos) {
				LogUtils.d("Main", "我可以升级" + appInfo.getPackageName());
				for (AppInfo appManaInfo : appHomeInfos) {
					if (appManaInfo.getPackageName().equals(
							appInfo.getPackageName())) {
						appManaInfo.setCanUpdate(true);
						LogUtils.d("Main",
								"我可以升级" + appManaInfo.getPackageName());
					}
				}
			}
		}
	};
	private View recomnView;
	private void initHomeView() {
		mHomeListView = (ListView) homeView.findViewById(R.id.list_home);
		
		//添加广告、导航栏等
		View testView = inflate.inflate(getActivity(), R.layout.ranktest, null);
		testView.setPadding(0, 1, 0, 1);
		View advertBanner = inflate.inflate(getActivity(), R.layout.advert_banner, null);
//		View tabRank = inflate.inflate(getActivity(), R.layout.tab_localgame, null);
		recomnView = (View)inflate.findViewById(R.id.tab_recomn2);
		//tabRank.setPadding(0, 5, 0, 10);
//		advertBanner.setPadding(0, 5, 0, 5);
		mHomeListView.addHeaderView(testView,null,false);
		mHomeListView.addHeaderView(advertBanner,null,false);
//		mHomeListView.addHeaderView(tabRank,null,false);
		// mHomeListView.setDividerHeight(20);
		pHomeBar = (ProgressBar) homeView.findViewById(R.id.pro_bar_home);
		pHomeBar.setVisibility(View.VISIBLE);
		ll_homeerror = (LinearLayout) homeView.findViewById(R.id.ll_error);
		appHomeInfos = new ArrayList<AppInfo>();
		View contentView = View.inflate(getActivity(),
				R.layout.popup_item, null);
		pw = new PopupWindow(contentView, LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT, true);
		ColorDrawable cd = new ColorDrawable(-0000);
		pw.setBackgroundDrawable(cd);
		pw.setOutsideTouchable(true);
		
		downandupdata = (TextView) contentView.findViewById(R.id.downandupdata);
		checkupdata_pop = (TextView) contentView.findViewById(R.id.checkupdata_pop);
		getout_pop = (TextView) contentView.findViewById(R.id.getout_pop);
		about = (TextView) contentView.findViewById(R.id.about);
		downandupdata.setOnClickListener(this);
		checkupdata_pop.setOnClickListener(this);
		getout_pop.setOnClickListener(this);
		about.setOnClickListener(this);
		setting = (ImageButton) inflate.findViewById(R.id.setting);
		updata_num = (TextView) inflate.findViewById(R.id.updata_num);
		setting.setOnClickListener(this);
		Button btn_refresh = (Button) ll_homeerror.findViewById(R.id.btn_Refsh);
		// tuijian_gallery = (GalleryFlow)
		// homeView.findViewById(R.id.tuijian_gallery);
		btn_refresh.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				appHomeInfos.clear();
				pHomeBar.setVisibility(View.VISIBLE);
				ll_homeerror.setVisibility(View.GONE);
				new Thread(runHomeData).start();

			}
		});
		cache = new File(Environment.getExternalStorageDirectory(), "cache");
		if (!cache.exists()) {
			cache.mkdirs();
		}

		LayoutInflater inflater = LayoutInflater.from(getActivity());
		tuijian_gallery = (GalleryFlow) inflater.inflate(
				R.layout.home_head_banner, null);
		//mHomeListView.addHeaderView(tuijian_gallery);

		tuijian_gallery.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					myHandler.removeMessages(SHOWNEXT);
				} else if (event.getAction() == MotionEvent.ACTION_UP) {
					myHandler.sendEmptyMessageDelayed(SHOWNEXT, DELAYTIME);
				} else if (event.getAction() == MotionEvent.ACTION_MOVE) {

				}
				return false;
			}
		});
		appHomeInfos.clear();
		List<AppInfo> appHome = new ArrayList<AppInfo>();
		appHome = MarketApplication.getInstance().getHomeAppInfos();
		if(appHome == null) {
			
		} else {
		updata_num.setText(MarketApplication.getInstance().getDownApplist().size()+MarketApplication.getInstance().getAppManagerUpdateInfos().size()+"");
		appHomeInfos.addAll(appHome);
		appHomeAdapter = new NewRecommnAdapter(appHomeInfos, getActivity(),
				cache, mHomeListView);
		mHomeListView.setAdapter(appHomeAdapter);
		appHomeAdapter.notifyDataSetChanged();
		/*
		 * // 注册滑动监听事件 mHomeListView.setOnScrollListener(new OnScrollListener()
		 * {
		 * 
		 * @Override public void onScrollStateChanged(AbsListView view, int
		 * scrollState) { switch (scrollState) { case SCROLL_STATE_FLING:
		 * appHomeAdapter.isAsyn = true; break; case SCROLL_STATE_IDLE:
		 * appHomeAdapter.isAsyn = false; appHomeAdapter.notifyDataSetChanged();
		 * break; case SCROLL_STATE_TOUCH_SCROLL: appHomeAdapter.isAsyn = false;
		 * break;
		 * 
		 * }
		 * 
		 * }
		 * 
		 * @Override public void onScroll(AbsListView view, int
		 * firstVisibleItem, int visibleItemCount, int totalItemCount) {
		 * 
		 * } });
		 */
		/*
		 * mHomeListView.setonRefreshListener(new OnRefreshListener() {
		 * 
		 * @Override public void onRefresh() {
		 * myHandler.removeMessages(SHOWNEXT); appHomeInfos.clear(); new
		 * Thread(runHomeData).start(); // new Thread(runBannerData).start(); }
		 * });
		 */

		mHomeListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				if (position == 1) {
					Intent intent = new Intent(getActivity(),
							BannerActivity.class);
					startActivity(intent);

				} else {
					AppInfo mAppInfo = (AppInfo) mHomeListView.getAdapter()
							.getItem(position);
					// Log.d("YTL", "mAppInfo.getIdx() = " + mAppInfo.getIdx());
					Intent intent = new Intent(getActivity(),
							AppDetailActivity.class);
					// LogUtils.d("error", position+"");
					intent.putExtra("appid", mAppInfo.getIdx());
					intent.putExtra("appinfo", mAppInfo);
					startActivity(intent);
				}
				// if(appHomeInfos != null && appHomeInfos.size() > position){
				// AppInfo mAppInfo = appHomeInfos.get(position);
				// Log.d("YTL", "mAppInfo.getIdx() = " + mAppInfo.getIdx());
				// Intent intent = new Intent(MainActivity.this,
				// AppDetailActivity.class);
				// intent.putExtra("appid", mAppInfo.getIdx());
				// startActivity(intent);
				// }
			}
		});

		// 　创建用于描述图像数据的ImageAdapter对象
		/*
		 * tuiJianAdapter = new TuiJianImageAdapter(this, bannerList, cache);
		 * tuijian_gallery.setAdapter(tuiJianAdapter);
		 * tuijian_gallery.setmPager(mPager); tuijian_gallery
		 * .setOnItemClickListener(new GalleryFlow.IOnItemClickListener() {
		 * 
		 * @Override public void onItemClick(int position) { int tempPos =
		 * position % bannerList.size(); BannerInfo mBanner =
		 * bannerList.get(tempPos); if (mBanner.getLinkurl() != null &&
		 * !mBanner.getLinkurl().equals("") &&
		 * !mBanner.getLinkurl().equals("null")) { try { Intent intent = new
		 * Intent(); intent.setAction("android.intent.action.VIEW"); Uri
		 * content_url = Uri.parse(mBanner .getLinkurl());
		 * intent.setData(content_url); startActivity(intent); } catch
		 * (Exception e) { e.printStackTrace(); ToastUtils.show("请先安装浏览器"); } }
		 * else { Intent intent = new Intent(MainActivity.this,
		 * AppDetailActivity.class); intent.putExtra("appid",
		 * mBanner.getAppID()); startActivity(intent); } } });
		 */
		}
	}
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.setting:
			// 获取view在当前窗体的位置
			int location[] = new int[2];
			setting.getLocationInWindow(location);
			x= location[0] + px2dip(getActivity(), 60);
			y = location[1]+ px2dip(getActivity(), 120);
			pw.showAtLocation(inflate, Gravity.LEFT | Gravity.TOP, x, y);
			break;
		case R.id.downandupdata:
			Intent intent = new Intent();
			intent.setClass(getActivity(), DownLoadManagerActivity.class);
			startActivity(intent);
			break;
		case R.id.checkupdata_pop:
			updateSelf(true);
			break;
		case R.id.getout_pop:
			Intent cancalNt = new Intent();
			cancalNt.setAction("duobaohui.cancalnotifition");
			getActivity().sendBroadcast(cancalNt);
			LogUtils.d("Main", "我发出了取消广播");

			ArrayList<Activity> appLication = MarketApplication.getInstance()
					.getAppLication();
			for (Activity at : appLication) {
				at.finish();
			}
			getActivity().stopService(new Intent(getActivity(), LocalGameFragment.class));
			System.exit(0);
			android.os.Process.killProcess(android.os.Process.myPid());
			break;
		case R.id.about:
			Intent mysc = new Intent();
			mysc.setClass(getActivity(), MyScoreActivity.class);
			startActivity(mysc);
			break;
		}
	}
	 public static int px2dip(Context context, float pxValue){ 
         final float scale = context.getResources().getDisplayMetrics().density; 
         return (int)(pxValue / scale + 0.5f); 
 } 
}
