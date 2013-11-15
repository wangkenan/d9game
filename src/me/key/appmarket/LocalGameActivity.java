package me.key.appmarket;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.TimerTask;

import me.key.appmarket.adapter.MenuCategoryAdapter;
import me.key.appmarket.adapter.MyAdapter;
import me.key.appmarket.adapter.SDGameAdapter;
import me.key.appmarket.tool.DownloadService;
import me.key.appmarket.tool.ToolHelper;
import me.key.appmarket.utils.AppInfo;
import me.key.appmarket.utils.AppUtils;
import me.key.appmarket.utils.CategoryInfo;
import me.key.appmarket.utils.Global;
import me.key.appmarket.utils.LocalAppInfo;
import me.key.appmarket.utils.LocalUtils;
import me.key.appmarket.utils.LogUtils;
import me.key.appmarket.utils.ToastUtils;
import net.tsz.afinal.FinalDb;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.market.d9game.R;
import com.slidingmenu.lib.app2.SlidingFragmentActivity;
import com.slidingmenu.lib2.SlidingMenu;
import com.slidingmenu.lib2.SlidingMenu.OnCloseListener;
import com.slidingmenu.lib2.SlidingMenu.OnOpenedListener;
import com.umeng.analytics.MobclickAgent;

/**
 * 本地游戏界面
 * 
 * @author Administrator
 * 
 */
public class LocalGameActivity extends SlidingFragmentActivity implements
		OnClickListener {
	private LinearLayout gameLinearLayout;
	private ListView mListReco;
	private ProgressBar pBar;
	private String ItemId;
	private ImageView iv;
	private ListView downmanager_lv;
	private PrecentReceiver mPrecentReceiver;
	private ListView lv;
	// SD卡游戏
	private List<AppInfo> mAppInfos = new ArrayList<AppInfo>();
	private MyAdapter adapter;
	private LocalInstallBroadcast receiver;
	private String root;
	private ArrayList<CategoryInfo> gcategoryInfoList_temp = new ArrayList<CategoryInfo>();
	private SlidingMenu menu;
	private MenuCategoryAdapter menuCategoryAdapter;
	private String apknamelist;

	// 我的游戏和内置游戏栏
	private LinearLayout mygamebar;
	private Button mygame;
	private Button sdgame;
	private SDGameAdapter sdAdapter;
	private TextView onkey_text;

	// 一键安装按钮
	private ImageView onkey_localapplist;
	//我的积分
	private ImageView onkey_myjifen;
	//免流
	private ImageView onkey_mianliu;
	// 本机已安装
	private ArrayList<AppInfo> appManaInfos_temp;
	private ArrayList<AppInfo> downApplist = new ArrayList<AppInfo>();
	private ArrayList<AppInfo> appManagerUpdateInfos_t = new ArrayList<AppInfo>();
	private ArrayList<AppInfo> appManagerUpdateInfos = new ArrayList<AppInfo>();
	private ImageView banner_local;
	private int width;
	private int height;
	private int gapPy;
	private int bigImHeight;
	private int gapPx;
	private FinalDb db;
	private static final int RESETQUIT = 0;
	private boolean mPreparedQuit = false;
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
		setContentView(R.layout.locat_applist);
		setBehindContentView(R.layout.slide_menu1);
		
		SharedPreferences sp = getSharedPreferences("cleandb", MODE_PRIVATE);
		boolean cleanDb = sp.getBoolean("db", false);
		if(!cleanDb) {
			cleanDatabases(this);
			Editor edit = sp.edit();
			edit.putBoolean("db", true);
			edit.commit();
		}
		
		 db = FinalDb.create(this);
		/*
		 * iv.setOnClickListener(new OnClickListener() {
		 * 
		 * @Override public void onClick(View v) { Intent intent = new
		 * Intent(LocalGameActivity.this, AppDetailActivity.class);
		 * intent.putExtra("appid", 15603+""); startActivity(intent); } });
		 */
		pBar = (ProgressBar) findViewById(R.id.pro_bar_loacl);
		root = LocalUtils.getRoot(this);
		pBar.setVisibility(View.VISIBLE);
		mygamebar = (LinearLayout) findViewById(R.id.mygamebar);
		banner_local = (ImageView) findViewById(R.id.banner_local);
		setImagePosition(R.drawable.a20131008174300, banner_local);
		lv = (ListView) findViewById(R.id.category_lv1);
		 banner_local.setOnClickListener(new OnClickListener() {
			  
			  @Override public void onClick(View v) { Intent intent = new
			  Intent(LocalGameActivity.this, AppDetailActivity.class);
			  intent.putExtra("appid", 15603 + ""); startActivity(intent); }
			  
			  });
		 
		mygame = (Button) findViewById(R.id.mygame);
		sdgame = (Button) findViewById(R.id.sdgame);
		
		mygame.setOnClickListener(this);
		sdgame.setOnClickListener(this);
		onkey_localapplist.setOnClickListener(this);
		onkey_myjifen.setOnClickListener(this);
		onkey_mianliu.setOnClickListener(this);
		LogUtils.d("Local", width+"Local");
		new AsyncTask<Void, Void, Void>() {

			@Override
			protected Void doInBackground(Void... params) {
				appManaInfos_temp = AppUtils.getUserApps(
						LocalGameActivity.this, 4000);
				List<AppInfo> mAppInfos_temp = new ArrayList<AppInfo>();
				List<PackageInfo> packages = LocalGameActivity.this.getPackageManager().getInstalledPackages(0);
				mAppInfos_temp = LocalUtils.InitHomePager("0",
						LocalGameActivity.this, root,packages);
				mAppInfos.addAll(mAppInfos_temp);
				ArrayList<AppInfo> userApps = AppUtils.getUserApps(
						LocalGameActivity.this, 4000);
				apknamelist = AppUtils
						.getInstallAppPackage(LocalGameActivity.this);
				String str = ToolHelper
						.donwLoadToString(Global.MAIN_URL
								+ Global.UPGRADEVERSION + "?apknamelist="
								+ apknamelist);
				ParseUpdateJson(str);
				appManagerUpdateInfos_t = AppUtils.getCanUpadateApp(userApps,
						appManagerUpdateInfos_t);
				appManagerUpdateInfos.clear();
				appManagerUpdateInfos.addAll(appManagerUpdateInfos_t);
				LogUtils.d("Mana", "appUpdate" + appManagerUpdateInfos.size());
				for (AppInfo appInfo : appManagerUpdateInfos) {
					for (AppInfo appManaInfo : appManaInfos_temp) {
						if (appManaInfo.getPackageName().equals(
								appInfo.getPackageName())) {
							appManaInfo.setCanUpdate(true);
						}
					}
				}
				return null;
			}

			protected void onPostExecute(Void result) {
				adapter = new MyAdapter(LocalGameActivity.this, mAppInfos,
						appManaInfos_temp, downApplist, appManagerUpdateInfos);
				sdAdapter = new SDGameAdapter(LocalGameActivity.this, mAppInfos);
				mListReco.setAdapter(adapter);
				TextView tv = (TextView) LocalGameActivity.this
						.findViewById(R.id.wushju);
				if (mAppInfos.size() == 0) {
					tv.setVisibility(View.VISIBLE);
				} else {
					tv.setVisibility(View.GONE);
				}
				pBar.setVisibility(View.GONE);
				for (AppInfo ai : appManaInfos_temp) {
					LocalAppInfo findById = db.findById(ai.getId(),
							LocalAppInfo.class);
					AppInfo findById2 = db.findById(ai.getId(), AppInfo.class);
					if (findById2 != null
							&& findById2.getLastTime() != Long.MAX_VALUE) {
						Long lastTime = findById2.getLastTime();
						if (lastTime == Long.MAX_VALUE) {
							ai.setLastTime(lastTime);
						} else {
							long currentTimeMillis = System.currentTimeMillis();
							ai.setLastTime(currentTimeMillis - lastTime);
						}
						adapter.notifyDataSetChanged();
					}
					if (findById != null) {
						Long lastTime = findById.getLastTime();
						if (lastTime == Long.MAX_VALUE) {
							ai.setLastTime(lastTime);
						} else {
							long currentTimeMillis = System.currentTimeMillis();
							ai.setLastTime(currentTimeMillis - lastTime);
						}
						adapter.notifyDataSetChanged();
						LogUtils.d("maxTime", lastTime + "");
					}
				}
				// 按照玩的时间进行排序
				sort2lastTime();
				java.util.Timer timer = new java.util.Timer(true);

				TimerTask task = new TimerTask() {
					public void run() {
						ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
						DownloadService.watchDog(appManaInfos_temp, am);

					}
				};
				timer.schedule(task, 1000, 3000);
			}

		}.execute();
		/*
		 * LogUtils.d("mAppInfos", mAppInfos.size() + "");
		 * ArrayList<CategoryInfo> categoryInfo = new ArrayList<CategoryInfo>();
		 * categoryInfo.add(new CategoryInfo("0", "休闲益智", null, null, null));
		 * categoryInfo.add(new CategoryInfo("1", "角色冒险", null, null, null));
		 * categoryInfo.add(new CategoryInfo("2", "动作格斗", null, null, null));
		 * categoryInfo.add(new CategoryInfo("3", "策略游戏", null, null, null));
		 * categoryInfo.add(new CategoryInfo("4", "飞行射击", null, null, null));
		 * categoryInfo.add(new CategoryInfo("5", "体育竞技", null, null, null));
		 * categoryInfo.add(new CategoryInfo("6", "卡牌棋牌", null, null, null));
		 * categoryInfo.add(new CategoryInfo("7", "经营养成", null, null, null));
		 * categoryInfo.add(new CategoryInfo("8", "其他游戏", null, null, null));
		 * LocalDetailAdapter mCategoryAdapter = new
		 * LocalDetailAdapter(categoryInfo, this, mListGame);
		 * mListGame.setAdapter(mCategoryAdapter);
		 */

		Log.v("nano", "nano" + mListReco);
		IntentFilter filter = new IntentFilter();

		filter.addAction("android.intent.action.PACKAGE_ADDED");
		filter.addAction("android.intent.action.PACKAGE_REMOVED");
		filter.addDataScheme("package");
		receiver = new LocalInstallBroadcast();
		registerReceiver(receiver, filter);

	}

	@Override
	public void onStart() {
		super.onStart();
		FragmentTransaction fragmentTransaction = getSupportFragmentManager()
				.beginTransaction();
		final MenuFragment menuFragment = new MenuFragment();
		fragmentTransaction.replace(R.id.slide_content1, menuFragment);
		// fragmentTransaction.replace(R.id.content, new ContentFragment());
		fragmentTransaction.commit();

		LogUtils.d("Main", "我已经被加载了哟");
	
		LogUtils.d("Main", lv + "");

		LogUtils.d("Main1", menuCategoryAdapter + "");

		downmanager_lv = (ListView) findViewById(R.id.downmanager_lv);
		/*
		 * mygame.setPadding(40, 0, 40, 0); sdgame.setPadding(40, 0, 40, 0);
		 */
		LinearLayout etSeacher = (LinearLayout) findViewById(R.id.menu_search1);
		etSeacher.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setClass(LocalGameActivity.this, SearchActivity.class);
				startActivity(intent);
				LogUtils.d("MAIN", "动画前");
				LocalGameActivity.this.overridePendingTransition(
						R.anim.left_anim, R.anim.right_anim);
				LogUtils.d("MAIN", "动画后");
			}
		});

		Activity parent = getParent();

		MarketApplication.getInstance().getAppLication().add(this);
		gapPx = convertDipOrPx(this, 5);
		gapPy = convertDipOrPx(this, 10);
		bigImHeight = (int) ((width - gapPy) / 2 / 1.27f);
		parent.getIntent();
		mListReco = (ListView) this.findViewById(R.id.mlist);
	
		LayoutInflater inflater = LayoutInflater.from(this);
		// iv = (ImageView) findViewById(R.id.banner_local);
		ImageButton search_btn = (ImageButton) findViewById(R.id.search_btn);
		search_btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				menu.toggle();
			}
		});
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
						LocalGameActivity.this, gcategoryInfoList_temp, lv);
				LogUtils.d("Main", gcategoryInfoList_temp.size()
						+ "gcategoryInfoList_temp");
				lv.setAdapter(menuCategoryAdapter);
				lv.setVisibility(View.VISIBLE);
				//lv.getChildAt(0).setBackgroundColor(getResources().getColor(R.color.classiv_cloor));
				 //lv.getChildAt(0).findViewById(R.id.click_menu).setVisibility(View.VISIBLE);
				LogUtils.d("Main", lv.getChildCount() + "child");

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
				setBehindContentView(R.layout.slide_menu1);
				FragmentTransaction fragmentTransaction = getSupportFragmentManager()
						.beginTransaction();
				final MenuFragment menuFragment = new MenuFragment();
				fragmentTransaction.replace(R.id.slide_content1, menuFragment);
				fragmentTransaction.commit();

				LogUtils.d("Main", "open__");
				final ListView lv1 = (ListView) findViewById(R.id.category_lv1);
				MenuCategoryAdapter menuCategoryAdapter = new MenuCategoryAdapter(
						LocalGameActivity.this, gcategoryInfoList_temp, lv);
				lv1.setAdapter(menuCategoryAdapter);
				menuCategoryAdapter.notifyDataSetChanged();
				lv1.setOnItemClickListener(new OnItemClickListener() {

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
						for (int i = 0; i < lv1.getChildCount(); i++) {
							if (i == position) {
								continue;
							}
							LogUtils.d("Main", i + "");
							lv1.getChildAt(i).setBackgroundColor(
									getResources().getColor(R.color.white));
							lv1.getChildAt(i).findViewById(R.id.click_menu)
									.setVisibility(View.INVISIBLE);
							TextView tv = (TextView) lv1.getChildAt(i)
									.findViewById(R.id.category_menu);
							tv.setTextColor(getResources().getColor(
									R.color.black));
						}
					}

				});

				LinearLayout etSeacher = (LinearLayout) findViewById(R.id.menu_search1);
				etSeacher.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						Intent intent = new Intent();
						intent.setClass(LocalGameActivity.this,
								SearchActivity.class);
						startActivity(intent);
						LogUtils.d("MAIN", "动画前");
						LocalGameActivity.this.overridePendingTransition(
								R.anim.left_anim, R.anim.right_anim);
						LogUtils.d("MAIN", "动画后");
					}
				});
				// menuCategoryAdapter.notifyDataSetChanged();
				
				 Intent intent = new Intent(); intent.setAction("open.menu");
				 sendBroadcast(intent);
				 
			}
		});

	}

	@Override
	protected void onResume() {
		super.onResume();
		MobclickAgent.onResume(this);
		List<AppInfo> down_temp = new ArrayList<AppInfo>();
		down_temp = db.findAll(AppInfo.class);
		downApplist.clear();
		downApplist.addAll(down_temp);
		Collections.reverse(downApplist);
		if (adapter != null) {
			adapter.notifyDataSetChanged();
		}
		registerPrecent();
		SharedPreferences sp = getSharedPreferences("onkey", MODE_PRIVATE);
		boolean onkey = sp.getBoolean("onkey", false);
		if(onkey) {
			onkey_text.setText("装机必备");
		}
		if (appManaInfos_temp != null && adapter != null) {
			for (AppInfo ai : appManaInfos_temp) {
				LocalAppInfo findById = db.findById(ai.getId(),
						LocalAppInfo.class);
				AppInfo findById2 = db.findById(ai.getId(), AppInfo.class);
				if (findById2 != null
						&& findById2.getLastTime() != Long.MAX_VALUE) {
					Long lastTime = findById2.getLastTime();
					if (lastTime == Long.MAX_VALUE) {
						ai.setLastTime(lastTime);
					} else {
						long currentTimeMillis = System.currentTimeMillis();
						ai.setLastTime(currentTimeMillis - lastTime);
					}
					adapter.notifyDataSetChanged();
				}
				if (findById != null) {
					Long lastTime = findById.getLastTime();
					if (lastTime == Long.MAX_VALUE) {
						ai.setLastTime(lastTime);
					} else {
						long currentTimeMillis = System.currentTimeMillis();
						ai.setLastTime(currentTimeMillis - lastTime);
					}
					adapter.notifyDataSetChanged();
				}
			}
			sort2lastTime();
		}
		for (AppInfo ai : downApplist) {
			DownStateBroadcast dsb = new DownStateBroadcast();
			IntentFilter filter = new IntentFilter();
			String fileName = DownloadService.CreatFileName(ai.getAppName())
					.getAbsolutePath();
			filter.addAction(fileName + "down");
			this.registerReceiver(dsb, filter);
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
		if (menu.isMenuShowing()) {
			menu.toggle();
		}
	}

	class LocalInstallBroadcast extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			// 接收安装广播
			if (intent.getAction()
					.equals("android.intent.action.PACKAGE_ADDED")) {
				String packageName = intent.getDataString().substring(8);
				LogUtils.d("LocalGmae", "安装了:" + packageName + "包名的程序");
				// List<AppInfo> down_temp = new ArrayList<AppInfo>();
				// down_temp = db.findAll(AppInfo.class);
				/*
				 * downApplist.clear(); downApplist.addAll(down_temp);
				 */
				MarketApplication.getInstance().reflashAppList();
				for (int i = 0; i < appManaInfos_temp.size(); i++) {
					LogUtils.d("wojieshou", appManaInfos_temp.get(i)
							.getPackageName() + "");
					if (packageName != null
							&& packageName.equals(appManaInfos_temp.get(i)
									.getPackageName())) {
						// appManaInfos_temp.add(mAppInfos.get(i));
						appManaInfos_temp.remove(appManaInfos_temp.get(i));
						appManaInfos_temp.get(i).setCanUpdate(false);
						sort2lastTime();
						break;
					}
				}

				for (int i = 0; i < downApplist.size(); i++) {
					LogUtils.d("wojieshou2", downApplist.get(i)
							.getPackageName() + "");
					if (packageName != null
							&& packageName.equals(downApplist.get(i)
									.getPackageName())) {
						// appManaInfos_temp.add(downApplist.get(i));
						db.delete(downApplist.get(i));
						downApplist.remove(downApplist.get(i));
						break;
					}
				}
				for (int i = 0; i < mAppInfos.size(); i++) {
					LogUtils.d("wojieshou2", mAppInfos.get(i)
							.getPackageName() + "");
					if (packageName != null
							&& packageName.equals(mAppInfos.get(i)
									.getPackageName())) {
						// appManaInfos_temp.add(downApplist.get(i));
						mAppInfos.remove(mAppInfos.get(i));
						break;
					}
				}
				PackageManager packageManager = LocalGameActivity.this.getPackageManager();
				try {
					PackageInfo packageInfo = packageManager.getPackageInfo(
							packageName, 0);
					AppInfo appInfo = new AppInfo();
					appInfo.setAppIcon(packageInfo.applicationInfo
							.loadIcon(packageManager));
					appInfo.setPackageName(packageName);
					appInfo.setAppName(packageInfo.applicationInfo.loadLabel(
							packageManager).toString());
					appInfo.setId(packageName);
					appInfo.setLastTime(Long.MAX_VALUE);
					appManaInfos_temp.add(appInfo);
					File tempFile = new File(Environment.getExternalStorageDirectory(),
							"/market/" + appInfo.getAppName() + ".apk");
					
					if(tempFile.exists()) {
						tempFile.delete();
					}
					db.delete(appInfo);
				} catch (NameNotFoundException e) {
					e.printStackTrace();
				}

				adapter.notifyDataSetChanged();
				sdAdapter.notifyDataSetChanged();
				// 接受卸载广播
			}
			if (intent.getAction().equals(
					"android.intent.action.PACKAGE_REMOVED")) {
				MarketApplication.getInstance().reflashAppList();
				String packageName = intent.getDataString().substring(8);
				LogUtils.d("YTL", "卸载了:" + packageName + "包名的程序");
				for (int i = 0; i < mAppInfos.size(); i++) {
					LogUtils.d("wojieshou", appManaInfos_temp.get(i)
							.getPackageName() + "");
					if (packageName != null
							&& packageName.equals(mAppInfos.get(i)
									.getPackageName())) {
						appManaInfos_temp.get(i).setInstalled(false);
						break;
					}
				}
				adapter.notifyDataSetChanged();
			}

		}
	}

	private void setImagePosition(int resId, ImageView banner) {
		
		Bitmap bm = BitmapFactory.decodeResource(this.getResources(), resId);
		LogUtils.d("Local", width+"");
		 width = getIntent().getExtras().getInt("width");
		 LogUtils.d("Local", width+"creat");
		Bitmap newbitmap = Bitmap.createBitmap((width - gapPy),
				(int) ((width - gapPy) / 5.34), bm.getConfig());
		getNewBitMapPos(bm, newbitmap);
		 LogUtils.d("Local", banner+"banner");
		 LogUtils.d("Local", newbitmap+"newbitmap");
		banner.setImageBitmap(newbitmap);
	}

	private void getNewBitMapPos(Bitmap bm, Bitmap newbitmap) {
		Paint paint = new Paint();
		Canvas canvas = new Canvas(newbitmap);
		Matrix matrix = new Matrix();
		double newWidth = 1.00;
		double newHeight = 2.6;
		int gapPx = convertDipOrPx(this, 5);
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

	class PrecentReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(MarketApplication.PRECENT)) {
				// 下载中刷新界面进度
				if (adapter != null) {
					adapter.notifyDataSetChanged();
				}
			}
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
			LogUtils.d("Main", "我发出了取消广播");
			/*
			 * // this.finish(); ArrayList<Activity> appLication =
			 * MarketApplication.getInstance().getAppLication(); for(Activity at
			 * : appLication) { at.finish(); } stopService(new Intent(this,
			 * DownloadService.class)); finish(); System.exit(0);
			 * android.os.Process.killProcess(android.os.Process.myPid());
			 */
		}
		return super.onKeyDown(keyCode, event);
	}

	class DownStateBroadcast extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			String fileName = null;
			LogUtils.d("Local", "我接受到了暂停广播");
			for (AppInfo ai : downApplist) {
				fileName = DownloadService.CreatFileName(ai.getAppName())
						.getAbsolutePath() + "down";
				if (fileName.equals(intent.getAction())) {
					boolean downState = intent
							.getBooleanExtra("isPause", false);
					ai.setIspause(downState);
					adapter.notifyDataSetChanged();
					LogUtils.d("Local",
							"我更新了ui" + ai.getAppName() + ai.isIspause());
					db.update(ai);
					break;
				}
			}
		}

	}

	public void sort2lastTime() {
		Collections.sort(appManaInfos_temp, new Comparator<AppInfo>() {

			@Override
			public int compare(AppInfo app1, AppInfo app2) {
				if (app1.getLastTime().compareTo(app2.getLastTime()) == 0) {
					return -1;
				} else {
					return app1.getLastTime().compareTo(app2.getLastTime());
				}
			}

		});
	}

	private void ParseCategoryJson(String str) {
		try {
			gcategoryInfoList_temp.clear();
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
		unregisterPrecent();
		LogUtils.d("Main", "我按了Home");
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

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.mygame:
			mListReco.setAdapter(adapter);
			mygame.setBackgroundResource(R.drawable.btn_bar_2);
			// mygame.setPadding(40, 0, 40, 0);
			sdgame.setBackgroundResource(0);
			break;
		case R.id.sdgame:
			mListReco.setAdapter(sdAdapter);
			sdgame.setBackgroundResource(R.drawable.btn_bar_2);
			// sdgame.setPadding(40, 0, 40, 0);
			mygame.setBackgroundResource(0);
			break;
		}
	}
	public  boolean isDownLoaded(String name) {
		boolean result = false;
		SharedPreferences sp = getSharedPreferences("down",
				MODE_PRIVATE);
		File tempFile = new File(Environment.getExternalStorageDirectory(),
				"/market/" + name + ".apk");

		if (tempFile.exists()) {
			result = true;
		}
		long temp = sp.getLong(tempFile.getAbsolutePath(), -1);
		if (temp != -1) {
			LogUtils.d("DownLoadService", "temp"+temp);
			result = false;
		}
		return result;
	}
	  public static void cleanDatabases(Context context) {
	        deleteFilesByDirectory(new File("/data/data/"
	                + context.getPackageName() + "/databases"));
	    }
	  private static void deleteFilesByDirectory(File directory) {
	        if (directory != null && directory.exists() && directory.isDirectory()) {
	            for (File item : directory.listFiles()) {
	                item.delete();
	            }
	        }
	    }
}
