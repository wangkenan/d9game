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

import me.key.appmarket.MyListView.OnLoadMoreListener;
import me.key.appmarket.adapter.AppAdapter;
import me.key.appmarket.adapter.DetaileAdapter;
import me.key.appmarket.adapter.ManagerAdapter;
import me.key.appmarket.adapter.ManagerUpdateAdapter;
import me.key.appmarket.adapter.MenuCategoryAdapter;
import me.key.appmarket.adapter.MyAdapter;
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
import me.key.appmarket.utils.LocalUtils;
import me.key.appmarket.utils.LogUtils;
import me.key.appmarket.utils.ToastUtils;
import me.key.appmarket.widgets.GalleryFlow;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.json.JSONArray;
import org.json.JSONObject;

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
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
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

public class MainActivity extends SlidingFragmentActivity { 

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

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		MarketApplication.getInstance().getAppLication().add(this);
		//侧滑菜单
		setBehindContentView(R.layout.slide_menu);
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

		LogUtils.d("Main1", menuCategoryAdapter + "");
		new AsyncTask<Void, Void, Void>() {

			@Override
			protected Void doInBackground(Void... params) {
				String str = ToolHelper.donwLoadToString(Global.MAIN_URL
						+ Global.APP_CATEGORY + "?type=" + type);
				Log.e("tag", "runCategoryData result =" + str);
				ParseCategoryJson(str);
				return null;
			}

			@Override
			protected void onPostExecute(Void result) {
				super.onPostExecute(result);
				menuCategoryAdapter = new MenuCategoryAdapter(
						MainActivity.this, gcategoryInfoList_temp,lv);
				LogUtils.d("Main", categoryInfoList_temp.size() + "ggg");
				LogUtils.d("Main", categoryInfoList_temp.size() + "");
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

		LinearLayout etSeacher = (LinearLayout) findViewById(R.id.menu_search);
		etSeacher.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setClass(MainActivity.this, SearchActivity.class);
				startActivity(intent);
				LogUtils.d("MAIN", "动画前");
				MainActivity.this.overridePendingTransition(R.anim.left_anim,
						R.anim.right_anim);
				LogUtils.d("MAIN", "动画后");
			}
		});
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
		activities.add(this);
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
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		registerPrecent();
		MobclickAgent.onResume(this);
		appHomeAdapter.notifyDataSetChanged();
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		unregisterPrecent();
		MobclickAgent.onPause(this);
		if(menu.isMenuShowing()) {
			menu.toggle();
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		 stopService(new Intent(this, DownloadService.class));
		unregisterInstall();

	}

	private void initManagerView() {
		appManagerInfos = AppUtils.getUserApps(this, 0);

		// install_app = (Button) managerView.findViewById(R.id.install_app);
		// update_app = (Button) managerView.findViewById(R.id.update_app);
		install_app.setPadding(40, 0, 40, 0);
		update_app.setPadding(40, 0, 40, 0);

		install_app.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!isShowingInstall) {
					isShowingInstall = true;
					pro_bar.setVisibility(View.GONE);

					install_app.setBackgroundResource(R.drawable.btn_bar_2);
					install_app.setPadding(40, 0, 40, 0);
					update_app.setBackgroundResource(0);

					mManagerListView.setVisibility(View.VISIBLE);
					appManagerInfos = AppUtils
							.getUserApps(MainActivity.this, 0);
					mManagerListView.setAdapter(mManagerAdapter);
					mManagerListView
							.setOnItemClickListener(new OnItemClickListener() {
								@Override
								public void onItemClick(AdapterView<?> parent,
										View view, int position, long id) {
									AppUtils.showInstalledAppDetails(
											MainActivity.this, appManagerInfos
													.get(position)
													.getPackageName());
								}
							});
				}
			}
		});
		update_app.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (isShowingInstall) {
					isShowingInstall = false;
					update_app.setBackgroundResource(R.drawable.btn_bar_2);
					update_app.setPadding(40, 0, 40, 0);
					install_app.setBackgroundResource(0);

					apknamelist = AppUtils
							.getInstallAppPackage(MainActivity.this);
					mManagerListView.setVisibility(View.GONE);

					pro_bar.setVisibility(View.VISIBLE);
					new Thread(runUpdateAppData).start();
				}
			}
		});

		pro_bar = (ProgressBar) managerView.findViewById(R.id.pro_bar);
		pro_bar.setVisibility(View.GONE);
		mManagerListView = (ListView) managerView.findViewById(R.id.mlist);
		mManagerAdapter = new ManagerAdapter(appManagerInfos,
				MainActivity.this, cache);
		mManagerUpdateAdapter = new ManagerUpdateAdapter(appManagerUpdateInfos,
				MainActivity.this, cache);
		mManagerListView.setAdapter(mManagerAdapter);
		mManagerListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				AppUtils.showInstalledAppDetails(MainActivity.this,
						appManagerInfos.get(position).getPackageName());
			}
		});
	}

	private void initLocalGameView() {
		mListGame = (ListView) logcalGmaeView.findViewById(R.id.list_app_game);
		LayoutInflater inflater = LayoutInflater.from(this);
		gameLinearLayout = (LinearLayout) inflater.inflate(
				R.layout.game_head_banner, null);
		mListGame.addHeaderView(gameLinearLayout);

		game_calss = (TextView) gameLinearLayout.findViewById(R.id.game_calss);
		game_boutique = (TextView) gameLinearLayout
				.findViewById(R.id.game_boutique);
		game_calss.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MainActivity.this,
						DetaileActivity.class);
				intent.putExtra("type", 2);
				startActivity(intent);
			}
		});
		game_boutique.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MainActivity.this,
						RecoTagsActivity.class);
				startActivity(intent);
			}
		});
		String Root = LocalUtils.getRoot(this);

		/*
		 * LocalCategoryAdapter mCategoryAdapter = new LocalCategoryAdapter(
		 * categoryInfoList, MainActivity.this, cache);
		 */
		List<PackageInfo> packages = MainActivity
				.this.getPackageManager().getInstalledPackages(0);
		List<AppInfo> mAppInfos = LocalUtils.InitHomePager("0", this, Root,packages);
		LogUtils.d("mAppInfos", mAppInfos.size() + "");
		MyAdapter adapter = new MyAdapter(MainActivity.this, mAppInfos);
		mListGame.setAdapter(adapter);

	}

	private void initRankView() {
		// TODO Auto-generated method stub
		mRankListView = (ListView) rankView.findViewById(R.id.list);
		pRankBar = (ProgressBar) rankView.findViewById(R.id.pro_bar);
		appRankInfos = new ArrayList<AppInfo>();
		ll_rankerror = (LinearLayout) rankView.findViewById(R.id.ll_error);
		Button btn_refresh = (Button) ll_rankerror.findViewById(R.id.btn_Refsh);
		btn_refresh.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				// appInfos.clear();
				appRankInfos.clear();
				pRankBar.setVisibility(View.VISIBLE);
				ll_rankerror.setVisibility(View.GONE);
				new Thread(runRankData).start();
			}
		});
		File cache = new File(Environment.getExternalStorageDirectory(),
				"cache");
		if (!cache.exists()) {
			cache.mkdirs();
		}
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
				Intent intent = new Intent(MainActivity.this,
						AppDetailActivity.class);
				intent.putExtra("appinfo", mAppInfo);
				intent.putExtra("appid", mAppInfo.getIdx());
				startActivity(intent);
			}
		});
	}

	public void setonLoadMoreListener(OnLoadMoreListener loadMoreListener) {
		this.loadMoreListener = loadMoreListener;
	}

	private void initGameView() {
		mListGame = (ListView) gameView.findViewById(R.id.list_app_game);
		topbar_title1 = (TextView) findViewById(R.id.topbar_title1);
		back_icon = (ImageView) findViewById(R.id.back_icon);
		logo_title = (ImageView) findViewById(R.id.logo_title);
		// type = getIntent().getIntExtra("type", 2);
		// new Thread(runCategoryData).start();

		new AsyncTask<Void, Void, Void>() {

			@Override
			protected Void doInBackground(Void... params) {
				String str = ToolHelper.donwLoadToString(Global.MAIN_URL
						+ Global.APP_CATEGORY + "?type=" + type);
				Log.e("tag", "runCategoryData result =" + str);
				if (str.equals("null")) {
					categoryDataHandler
							.sendEmptyMessage(Global.DOWN_DATA_HOME_SUCCESSFULL);
				} else if (str.equals("-1")) {
					categoryDataHandler
							.sendEmptyMessage(Global.DOWN_DATA_HOME_FAILLY);
				} else {
					Log.e("tag", "--------------1-------------");
					ParseCategoryJson(str);
				}
				return null;
			}

			@Override
			protected void onPostExecute(Void result) {
				super.onPostExecute(result);
				gcategoryInfoList.addAll(gcategoryInfoList_temp);
				gcategoryInfoList_temp.clear();
				mCategoryAdapter = new DetaileAdapter(gcategoryInfoList,
						MainActivity.this, mListGame);
				mListGame.setAdapter(mCategoryAdapter);
			}
		}.execute();

		mListGame.setDividerHeight(0);
		/*
		 * mGameListView = (MyListView) gameView.findViewById(R.id.game_list);
		 * mGameListView.setDivider(new ColorDrawable(Color.GRAY)); pGameBar =
		 * (ProgressBar) gameView.findViewById(R.id.game_pro_bar); ll_gameerror
		 * = (LinearLayout) gameView.findViewById(R.id.ll_error);
		 * 
		 * appGameInfos = new LinkedList<AppInfo>();
		 * 
		 * Button btn_refresh = (Button)
		 * ll_homeerror.findViewById(R.id.btn_Refsh); // tuijian_gallery =
		 * (GalleryFlow) // homeView.findViewById(R.id.tuijian_gallery);
		 * btn_refresh.setOnClickListener(new OnClickListener() {
		 * 
		 * @Override public void onClick(View arg0) { // TODO Auto-generated
		 * method stub appGameInfos.clear(); appGameInfos_temp.clear();
		 * pGameBar.setVisibility(View.VISIBLE);
		 * ll_gameerror.setVisibility(View.GONE); new
		 * Thread(runGameData).start(); } }); cache = new
		 * File(Environment.getExternalStorageDirectory(), "cache"); if
		 * (!cache.exists()) { cache.mkdirs(); }
		 * 
		 * LayoutInflater inflater = LayoutInflater.from(this); gameLinearLayout
		 * = (LinearLayout) inflater.inflate( R.layout.game_head_banner, null);
		 * mGameListView.addHeaderView(gameLinearLayout);
		 * 
		 * game_calss = (TextView)
		 * gameLinearLayout.findViewById(R.id.game_calss); game_boutique =
		 * (TextView) gameLinearLayout .findViewById(R.id.game_boutique);
		 * game_calss.setOnClickListener(new OnClickListener() {
		 * 
		 * @Override public void onClick(View v) { Intent intent = new
		 * Intent(MainActivity.this, DetaileActivity.class);
		 * intent.putExtra("type", 2); startActivity(intent); } });
		 * game_boutique.setOnClickListener(new OnClickListener() {
		 * 
		 * @Override public void onClick(View v) { Intent intent = new
		 * Intent(MainActivity.this, MyTableHost.class); startActivity(intent);
		 * } });
		 * 
		 * // 加载更多 loadGameMoreView =
		 * getLayoutInflater().inflate(R.layout.loadmore, null);
		 * loadGameMoreButton = (Button) loadGameMoreView
		 * .findViewById(R.id.loadMoreButton); //
		 * loadGameMoreButton.setOnClickListener(new View.OnClickListener() { //
		 * 
		 * @Override // public void onClick(View v) { //
		 * loadGameMoreButton.setText("正在加载中..."); // game_page = game_page + 1;
		 * // new Thread(runGameData).start(); // } // });
		 * mGameListView.addFooterView(loadGameMoreView);
		 * loadGameMoreButton.setVisibility(View.GONE);
		 * 
		 * this.setonLoadMoreListener(new OnLoadMoreListener() {
		 * 
		 * @Override public void onLoadMore() { if (!isLoading && !isFirst) {
		 * isLoading = true;
		 * 
		 * loadGameMoreButton.setText("正在加载中..."); game_page = game_page + 1;
		 * new Thread(runGameData).start(); }
		 * 
		 * isFirst = false; } });
		 * 
		 * appGameAdapter = new AppAdapter(appGameInfos, MainActivity.this,
		 * cache, mGameListView); mGameListView.setAdapter(appGameAdapter);
		 * mGameListView.setOnScrollListener(new OnScrollListener() {
		 * 
		 * @Override public void onScrollStateChanged(AbsListView view, int
		 * scrollState) { switch (scrollState) { case SCROLL_STATE_FLING:
		 * appGameAdapter.isAsyn = true; break; case SCROLL_STATE_IDLE:
		 * appGameAdapter.isAsyn = false; appGameAdapter.notifyDataSetChanged();
		 * break; case SCROLL_STATE_TOUCH_SCROLL: appGameAdapter.isAsyn = false;
		 * break;
		 * 
		 * }
		 * 
		 * }
		 * 
		 * @Override public void onScroll(AbsListView view, int
		 * firstVisibleItem, int visibleItemCount, int totalItemCount) {
		 * firstItemIndex = firstVisibleItem; if ((firstVisibleItem +
		 * visibleItemCount == totalItemCount) && (totalItemCount != 0)) { if
		 * (loadMoreListener != null) { loadMoreListener.onLoadMore(); } } } });
		 * 
		 * mGameListView.setonRefreshListener(new OnRefreshListener() {
		 * 
		 * @Override public void onRefresh() { isFirst = true;
		 * appGameInfos.clear(); appGameInfos_temp.clear(); new
		 * Thread(runGameData).start(); } });
		 * 
		 * mGameListView.setOnItemClickListener(new OnItemClickListener() {
		 * 
		 * @Override public void onItemClick(AdapterView<?> parent, View view,
		 * int position, long id) { AppInfo mAppInfo = (AppInfo)
		 * mGameListView.getAdapter() .getItem(position); if (mAppInfo != null)
		 * { Intent intent = new Intent(MainActivity.this,
		 * AppDetailActivity.class); intent.putExtra("appid",
		 * mAppInfo.getIdx()); startActivity(intent); } } });
		 */
		// mListGame = (ListView) gameView.findViewById(R.id.list_game);
		// indexs_games = getResources().getStringArray(R.array.index_games);
		// ListAdapter gameAdapter = new ArrayAdapter<String>(MainActivity.this,
		// R.layout.simple_item, R.id.item, indexs_games);
		// mListGame.setAdapter(gameAdapter);
		// mListGame.setOnItemClickListener(new OnItemClickListener() {
		//
		// @Override
		// public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
		// long arg3) {
		// // TODO Auto-generated method stub
		// if (!netIsAvail(MainActivity.this)) {
		// Toast.makeText(MainActivity.this, "请检查网络设置",
		// Toast.LENGTH_SHORT).show();
		// return;
		// }
		// Intent intent = new Intent(MainActivity.this,
		// IndexDetaileActivity.class);
		// Bundle bundle = new Bundle();
		// bundle.putString("name", indexs_games[arg2]);
		// bundle.putInt("type1", 2);
		// bundle.putInt("type2", arg2 + 1);
		// intent.putExtra("value", bundle);
		// startActivity(intent);
		// }
		// });
	}

	/*
	 * Runnable runCategoryData = new Runnable() {
	 * 
	 * @Override public void run() { String str =
	 * ToolHelper.donwLoadToString(Global.MAIN_URL + Global.APP_CATEGORY +
	 * "?type=" + type); Log.e("tag", "runCategoryData result =" + str); if
	 * (str.equals("null")) { categoryDataHandler
	 * .sendEmptyMessage(Global.DOWN_DATA_HOME_SUCCESSFULL); } else if
	 * (str.equals("-1")) { categoryDataHandler
	 * .sendEmptyMessage(Global.DOWN_DATA_HOME_FAILLY); } else { Log.e("tag",
	 * "--------------1-------------"); ParseCategoryJson(str); } } };
	 */
	Handler categoryDataHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case Global.DOWN_DATA_HOME_FAILLY: {
			}
				break;
			case Global.DOWN_DATA_HOME_SUCCESSFULL: {
				if (gcategoryInfoList_temp.size() > 0) {
					gcategoryInfoList.addAll(gcategoryInfoList_temp);
					gcategoryInfoList_temp.clear();
					LogUtils.d("asdasda", categoryInfoList.size() + "");
				}
				mCategoryAdapter.notifyDataSetChanged();
			}
			default:
				break;
			}
			super.handleMessage(msg);
		}
	};

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

	private void initHomeView() {
		mHomeListView = (ListView) homeView.findViewById(R.id.list_home);
		// mHomeListView.setDividerHeight(20);
		pHomeBar = (ProgressBar) homeView.findViewById(R.id.pro_bar_home);
		pHomeBar.setVisibility(View.VISIBLE);
		ll_homeerror = (LinearLayout) homeView.findViewById(R.id.ll_error);
		appHomeInfos = new ArrayList<AppInfo>();
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

		LayoutInflater inflater = LayoutInflater.from(this);
		tuijian_gallery = (GalleryFlow) inflater.inflate(
				R.layout.home_head_banner, null);
		mHomeListView.addHeaderView(tuijian_gallery);

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

		appHomeAdapter = new NewRecommnAdapter(appHomeInfos, MainActivity.this,
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
					Intent intent = new Intent(MainActivity.this,
							BannerActivity.class);
					startActivity(intent);

				} else {
					AppInfo mAppInfo = (AppInfo) mHomeListView.getAdapter()
							.getItem(position);
					// Log.d("YTL", "mAppInfo.getIdx() = " + mAppInfo.getIdx());
					Intent intent = new Intent(MainActivity.this,
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

	/*
	 * Runnable runBannerData = new Runnable() {
	 * 
	 * @Override public void run() { String str =
	 * ToolHelper.donwLoadToString(Global.MAIN_URL + Global.RECOMMEDNBANNER);
	 * Log.e("tag", "result111 =" + str); if (str.equals("null")) { //
	 * homeDataHandler.sendEmptyMessage(Global.DOWN_DATA_HOME_SUCCESSFULL); }
	 * else if (str.equals("-1")) { //
	 * homeDataHandler.sendEmptyMessage(Global.DOWN_DATA_HOME_FAILLY); } else {
	 * Log.e("tag", "--------------1-------------"); ParseBannerJson(str); } }
	 * };
	 */

	Runnable runGameData = new Runnable() {
		@Override
		public void run() {
			String str = ToolHelper.donwLoadToString(Global.MAIN_URL
					+ Global.GAME_PAGE + "?dataType=2" + "&page=" + game_page);
			// Log.e("tag", "result =" + str);
			if (str.equals("null")) {
				gameDataHandler
						.sendEmptyMessage(Global.DOWN_DATA_HOME_SUCCESSFULL);
			} else if (str.equals("-1")) {
				gameDataHandler.sendEmptyMessage(Global.DOWN_DATA_HOME_FAILLY);
			} else {
				// Log.e("tag", "--------------1-------------");
				ParseGameJson(str);
			}
		}
	};

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
			appHomeInfos.clear();
			List<AppInfo> appHome = new ArrayList<AppInfo>();
			appHome = MarketApplication.getInstance().getHomeAppInfos();
			appHomeInfos.addAll(appHome);
			pHomeBar.setVisibility(View.INVISIBLE);
			StringBuilder apknamelist = new StringBuilder();
			for (AppInfo ai : appHomeInfos) {
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

	Runnable runRankData = new Runnable() {
		@Override
		public void run() {
			// TODO Auto-generated method stub
			String str = ToolHelper.donwLoadToString(Global.GAME_MAIN_URL
					+ Global.RANK_PAGE);
			// Log.e("tag", "result =" + str);
			if (str.equals("null")) {
				rankHandler.sendEmptyMessage(Global.DOWN_DATA_HOME_SUCCESSFULL);
			} else if (str.equals("-1")) {
				rankHandler.sendEmptyMessage(Global.DOWN_DATA_HOME_FAILLY);
			} else {
				// Log.e("tag", "--------------1--------");
				ParseRankJson(str);
			}
		}
	};

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

	/*
	 * Handler bannerHandler = new Handler() {
	 * 
	 * @Override public void handleMessage(Message msg) { Log.e("tag",
	 * "--------bannerHandler--------" + msg.what); switch (msg.what) { case
	 * Global.DOWN_DATA_RANK_FAILLY: break; case
	 * Global.DOWN_DATA_HOME_SUCCESSFULL: Log.e("tag",
	 * "--------bannerHandler--------" + msg.what); Log.e("tag",
	 * "----bannerList.size() = " + bannerList.size());
	 * tuijian_gallery.setSelection(300); tuiJianAdapter.notifyDataSetChanged();
	 * myHandler.sendEmptyMessageDelayed(SHOWNEXT, DELAYTIME); break; default:
	 * break; } super.handleMessage(msg); } };
	 */

	Handler gameDataHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			isLoading = false;
			pGameBar.setVisibility(View.GONE);
			// Log.e("tag", "-------------gameDataHandler--------");
			switch (msg.what) {
			case Global.DOWN_DATA_HOME_FAILLY: {
				if (game_page == 0) {
					// Log.e("tag", "--------------5--------" + msg.what);
					ll_gameerror.setVisibility(View.VISIBLE);
					mGameListView.setVisibility(View.GONE);
					// mGameListView.onRefreshComplete();
				} else {
					loadGameMoreButton.setVisibility(View.GONE);
				}
			}
				break;
			case Global.DOWN_DATA_HOME_SUCCESSFULL: {
				mGameListView.setVisibility(View.VISIBLE);
				ll_gameerror.setVisibility(View.GONE);
				// mGameListView.onRefreshComplete();
				loadGameMoreButton.setVisibility(View.VISIBLE);
				// loadGameMoreButton.setText("加载更多...");

				// Log.d("YTL", "appGameInfos_temp = " +
				// appGameInfos_temp.size());
				if (appGameInfos_temp.size() > 0) {
					appGameInfos.addAll(appGameInfos_temp);
					appGameInfos_temp.clear();
				}
				// appGameAdapter.notifyDataSetChanged();
			}
			default:
				break;
			}
			super.handleMessage(msg);
		}
	};

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
								Intent intent = new Intent(MainActivity.this,
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

	Handler homeDataHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			pHomeBar.setVisibility(View.GONE);
			// Log.e("tag", "--------------4--------");
			switch (msg.what) {
			case Global.DOWN_DATA_HOME_FAILLY: {
				// Log.e("tag", "--------------5--------" + msg.what);
				ll_homeerror.setVisibility(View.VISIBLE);
				// mHomeListView.setVisibility(View.GONE);
				curpos = "0";
				// mHomeListView.onRefreshComplete();
			}
				break;
			case Global.DOWN_DATA_HOME_SUCCESSFULL: {
				// Log.e("tag", "--------------6--------" + msg.what);
				// Log.e("tag", "----len = " + appHomeInfos.size());
				mHomeListView.setVisibility(View.VISIBLE);
				ll_homeerror.setVisibility(View.GONE);
				// mHomeListView.onRefreshComplete();

				if (appHomeInfos_temp.size() > 0) {
					appHomeInfos.addAll(appHomeInfos_temp);
					appHomeInfos_temp.clear();
				}
				appHomeAdapter.notifyDataSetChanged();
				for (AppInfo ai : appHomeInfos) {
					DownStateBroadcast dsb = new DownStateBroadcast();
					IntentFilter filter = new IntentFilter();
					String fileName = DownloadService.CreatFileName(
							ai.getAppName()).getAbsolutePath();
					filter.addAction(fileName + "down");
					registerReceiver(dsb, filter);
				}
			}
			default:
				break;
			}
			super.handleMessage(msg);
		}
	};

	Handler rankHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			switch (msg.what) {
			case Global.DOWN_DATA_RANK_FAILLY: {
				ll_rankerror.setVisibility(View.VISIBLE);
				// mRankListView.setVisibility(View.GONE);
			}
				break;
			case Global.DOWN_DATA_RANK_SUCCESSFUL: {
				mRankListView.setVisibility(View.VISIBLE);
				// ll_rankerror.setVisibility(View.GONE);
				appRankInfos.addAll((List<AppInfo>) msg.obj);
				appRankAdapter.notifyDataSetChanged();
			}
			default:
				break;
			}
			super.handleMessage(msg);
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
						appName);
				appInfo.setPackageName(apppkgname);
				appInfo.setLastTime(Long.MAX_VALUE);
				if (recoPic == null) {
					String appimgurl = jsonObject.getString("appimgurl");
					String[] appImgurls = appimgurl.split(",");
					appInfo.setAppimgurl(appImgurls);
				}

				appInfo.setRecoPic(recoPic);
				appInfo.setInstalled(AppUtils.isInstalled(jsonObject.getString("apppkgname")));
				appHomeInfos_temp.add(appInfo);

				// Log.e("tag", "info = " + appInfo.toString());
			}
			// Log.e("tag", "--------------2--------");
			homeDataHandler.sendEmptyMessage(Global.DOWN_DATA_HOME_SUCCESSFULL);
		} catch (Exception ex) {
			// Log.e("tag", "error = " + ex.getMessage());
			homeDataHandler.sendEmptyMessage(Global.DOWN_DATA_HOME_FAILLY);
		}
	}

	private void ParseBannerJson(String str) {
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
				AppInfo appInfo = new AppInfo(idx, appName, appSize,
						Global.MAIN_URL + appiconurl, appurl, "", appdes,
						appName);

				appInfo.setInstalled(AppUtils.isInstalled(appName));
				bannerList.add(appInfo);
				// Log.e("tag", "info = " + appInfo.toString());
			}
			// Log.e("tag", "--------------2--------");
			homeDataHandler.sendEmptyMessage(Global.DOWN_DATA_HOME_SUCCESSFULL);
		} catch (Exception ex) {
			// Log.e("tag", "error = " + ex.getMessage());
			homeDataHandler.sendEmptyMessage(Global.DOWN_DATA_HOME_FAILLY);
		}
	}

	private void ParseGameJson(String str) {
		try {
			// Log.e("tag", "--------2--------");
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
						Global.MAIN_URL + appiconurl, appurl, "", "", appName);

				appInfo.setInstalled(AppUtils.isInstalled(appName));
				appGameInfos_temp.add(appInfo);
				// Log.e("tag", "info = " + appInfo.toString());
			}
			// Log.e("tag", "--------------2--------");
			gameDataHandler.sendEmptyMessage(Global.DOWN_DATA_HOME_SUCCESSFULL);
		} catch (Exception ex) {
			// Log.e("tag", "error = " + ex.getMessage());
			gameDataHandler.sendEmptyMessage(Global.DOWN_DATA_HOME_FAILLY);
		}
	}

	/*
	 * private void ParseBannerJson(String str) { try { Log.e("tag",
	 * "--------------ParseBannerJson--------"); JSONArray jsonArray = new
	 * JSONArray(str); int len = jsonArray.length(); for (int i = 0; i < len;
	 * i++) { JSONObject jsonObject = jsonArray.getJSONObject(i); String id =
	 * jsonObject.getString("id"); String title = jsonObject.getString("title");
	 * String picurl = jsonObject.getString("picurl"); String linkurl =
	 * jsonObject.getString("linkurl"); String appID =
	 * jsonObject.getString("appid"); BannerInfo bannerInfo = new BannerInfo(id,
	 * title, picurl, linkurl, appID); bannerList.add(bannerInfo); }
	 * Log.e("tag", "--------------ParseBannerJson 2--------");
	 * bannerHandler.sendEmptyMessage(Global.DOWN_DATA_HOME_SUCCESSFULL); }
	 * catch (Exception ex) { Log.e("tag", "ParseBannerJson error = " +
	 * ex.getMessage()); //
	 * homeDataHandler.sendEmptyMessage(Global.DOWN_DATA_HOME_FAILLY); } }
	 */

	private void ParseRankJson(String str) {
		List<AppInfo> appInfos = new ArrayList<AppInfo>();
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
				AppInfo appInfo = new AppInfo(idx, appName, appSize,
						Global.MAIN_URL + appiconurl, appurl, appDownCount, "",
						appName);

				appInfo.setInstalled(AppUtils.isInstalled(appName));
				appInfos.add(appInfo);
				// appRankInfos.add(appInfo);
				Log.e("tag", "info = " + appInfo.toString());
			}
			Log.e("tag", "--------------2--------");
			Message message = rankHandler.obtainMessage();
			message.obj = appInfos;
			message.what = Global.DOWN_DATA_RANK_SUCCESSFUL;
			rankHandler.sendMessage(message);
			// rankHandler.sendEmptyMessage(Global.DOWN_DATA_RANK_SUCCESSFUL);
		} catch (Exception ex) {
			Log.e("tag", "error = " + ex.getMessage());
			rankHandler.sendEmptyMessage(Global.DOWN_DATA_RANK_FAILLY);
		}
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

		mPager = (ViewPager) findViewById(R.id.vPager);
		listViews = new ArrayList<View>();
		LayoutInflater mInflater = getLayoutInflater();
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
		search_btn = (ImageButton) findViewById(R.id.search_btn);
		search_btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				menu.toggle();
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

	/**
	 * 检查更新
	 * 
	 * @param showToast
	 */
	private void updateSelf(boolean showToast) {
		Calendar c = Calendar.getInstance();
		int mDay = c.get(Calendar.DAY_OF_MONTH);// 获取当前月份的日期号码
		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(this);
		int lastDay = sp.getInt("day", 0);
		if (lastDay != mDay) {
			try {
				UpdateApk.checkUpdate(this, showToast, true);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		sp.edit().putInt("day", mDay).commit();
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
	/*		 ArrayList<Activity> appLication = MarketApplication.getInstance().getAppLication();
			 for(Activity at : appLication) {
				 at.finish();
			 }
			 System.exit(0);
			 android.os.Process.killProcess(android.os.Process.myPid());*/
			// this.finish();

		}
		return super.onKeyDown(keyCode, event);
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

				// 刷新管理界面
				appManagerInfos.clear();
				ArrayList<AppInfo> appManagerInfos1 = AppUtils.getUserApps(
						MainActivity.this, 4000);
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
									.getInstallAppPackage(MainActivity.this);
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
				PackageManager pm = this.getPackageManager();
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

}
