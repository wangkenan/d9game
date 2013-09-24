package me.key.appmarket;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import me.key.appmarket.MyListView.OnLoadMoreListener;
import me.key.appmarket.MyListView.OnRefreshListener;
import me.key.appmarket.adapter.AppAdapter;
import me.key.appmarket.adapter.CategoryAdapter;
import me.key.appmarket.adapter.LocalCategoryAdapter;
import me.key.appmarket.adapter.ManagerAdapter;
import me.key.appmarket.adapter.ManagerUpdateAdapter;
import me.key.appmarket.adapter.RankAdapter;
import me.key.appmarket.adapter.TabPageAdapter;
import me.key.appmarket.adapter.TuiJianImageAdapter;
import me.key.appmarket.tool.DownloadService;
import me.key.appmarket.tool.ToolHelper;
import me.key.appmarket.tool.TxtReader;
import me.key.appmarket.update.UpdateApk;
import me.key.appmarket.utils.AppInfo;
import me.key.appmarket.utils.AppUtils;
import me.key.appmarket.utils.BannerInfo;
import me.key.appmarket.utils.CategoryInfo;
import me.key.appmarket.utils.Global;
import me.key.appmarket.utils.LogUtils;
import me.key.appmarket.utils.ToastUtils;
import me.key.appmarket.widgets.GalleryFlow;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.market.d9game.R;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ViewSwitcher;

public class MainActivity extends Activity {

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
	private LinkedList<AppInfo> appHomeInfos;
	private LinkedList<AppInfo> appHomeInfos_temp = new LinkedList<AppInfo>();
	private AppAdapter appHomeAdapter;
	private ProgressBar pHomeBar;
	private String curpos = "0";
	private LinearLayout ll_homeerror;
	private ArrayList<BannerInfo> bannerList = new ArrayList<BannerInfo>();
	private GalleryFlow tuijian_gallery;
	private TuiJianImageAdapter tuiJianAdapter;

	// game
	private boolean isLoading = false;
	private boolean isFirst = true;

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

	// 管理
	private ListView mManagerListView;
	private ProgressBar pro_bar;
	private ManagerAdapter mManagerAdapter;
	private ManagerUpdateAdapter mManagerUpdateAdapter;
	private ArrayList<AppInfo> appManagerInfos = new ArrayList<AppInfo>();
	private ArrayList<AppInfo> appManagerUpdateInfos = new ArrayList<AppInfo>();

	private Button install_app;
	private Button update_app;
	private boolean isShowingInstall = true;

	private String apknamelist;

	// app

	File cache;
	// Rank
	private ListView mRankListView;
	private List<AppInfo> appRankInfos;
	private RankAdapter appRankAdapter;
	private ProgressBar pRankBar;
	private LinearLayout ll_rankerror;

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
			case INMAIN:

				InitViewPager();
				initHomeView();
				initGameView();
				initRankView();
				initManagerView();
				initLocalGameView();
				new Thread(runHomeData).start();
				new Thread(runRankData).start();
				// new Thread(runBannerData).start();

				updateSelf(false);

				registerInstall();
				MarketApplication.getInstance().reflashAppList();
				ViewSwitcher vs = (ViewSwitcher) findViewById(R.id.main);
				vs.showNext();

			default:
				break;
			}
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		startService(new Intent(this, DownloadService.class));
		myHandler.sendEmptyMessageDelayed(INMAIN, 500);
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

	@Override
	protected void onDestroy() {
		super.onDestroy();

		unregisterInstall();
		
	}

	private void initManagerView() {
		appManagerInfos = AppUtils.getUserApps(this);

		install_app = (Button) managerView.findViewById(R.id.install_app);
		update_app = (Button) managerView.findViewById(R.id.update_app);
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
					appManagerInfos = AppUtils.getUserApps(MainActivity.this);
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
		mManagerListView = (ListView) managerView
				.findViewById(R.id.manager_list);
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
		InputStream inputStream = getResources().openRawResource(
				R.raw.categorydata);
		String js = (String) TxtReader.getString(inputStream);
		try {
			JSONArray jsonArray = new JSONArray(js);
			int len = jsonArray.length();
			for (int i = 0; i < len; i++) {
				JSONObject jsonObject = jsonArray.getJSONObject(i);
				String id = jsonObject.getString("id");
				String name = jsonObject.getString("name");
				String appUrl = jsonObject.getString("appiconurl");
				CategoryInfo mCategoryInfo = new CategoryInfo(id, name, "", "",
						appUrl);
				categoryInfoList_temp.add(mCategoryInfo);

			}
			if (categoryInfoList_temp.size() > 0) {
				categoryInfoList.addAll(categoryInfoList_temp);
				categoryInfoList_temp.clear();
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		mListGame = (ListView) logcalGmaeView.findViewById(R.id.list_app_game);
		LocalCategoryAdapter mCategoryAdapter = new LocalCategoryAdapter(
				categoryInfoList, MainActivity.this, cache);
		mListGame.setAdapter(mCategoryAdapter);

		mListGame.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				CategoryInfo mCategoryInfo = categoryInfoList.get(arg2);
				if (mCategoryInfo != null) {
					Intent intent = new Intent(MainActivity.this,
							LocalIndexDetaileActivity.class);
					Bundle bundle = new Bundle();
					bundle.putString("id", mCategoryInfo.getId());
					bundle.putString("name", mCategoryInfo.getName());
					intent.putExtra("value", bundle);
					startActivity(intent);
				}
			}
		});
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
		appRankAdapter = new RankAdapter(appRankInfos, this, cache);
		mRankListView.setAdapter(appRankAdapter);
		//注册滑动监听事件，快速滑动时，不异步加载图片，而是从缓存中获取
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
				intent.putExtra("appid", mAppInfo.getIdx());
				startActivity(intent);
			}
		});
	}

	public void setonLoadMoreListener(OnLoadMoreListener loadMoreListener) {
		this.loadMoreListener = loadMoreListener;
	}

	private void initGameView() {
		mGameListView = (MyListView) gameView.findViewById(R.id.game_list);
		pGameBar = (ProgressBar) gameView.findViewById(R.id.game_pro_bar);
		ll_gameerror = (LinearLayout) gameView.findViewById(R.id.ll_error);

		appGameInfos = new LinkedList<AppInfo>();

		Button btn_refresh = (Button) ll_homeerror.findViewById(R.id.btn_Refsh);
		// tuijian_gallery = (GalleryFlow)
		// homeView.findViewById(R.id.tuijian_gallery);
		btn_refresh.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				appGameInfos.clear();
				appGameInfos_temp.clear();
				pGameBar.setVisibility(View.VISIBLE);
				ll_gameerror.setVisibility(View.GONE);
				new Thread(runGameData).start();
			}
		});
		cache = new File(Environment.getExternalStorageDirectory(), "cache");
		if (!cache.exists()) {
			cache.mkdirs();
		}

		LayoutInflater inflater = LayoutInflater.from(this);
		gameLinearLayout = (LinearLayout) inflater.inflate(
				R.layout.game_head_banner, null);
		mGameListView.addHeaderView(gameLinearLayout);

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

		// 加载更多
		loadGameMoreView = getLayoutInflater().inflate(R.layout.loadmore, null);
		loadGameMoreButton = (Button) loadGameMoreView
				.findViewById(R.id.loadMoreButton);
		// loadGameMoreButton.setOnClickListener(new View.OnClickListener() {
		// @Override
		// public void onClick(View v) {
		// loadGameMoreButton.setText("正在加载中...");
		// game_page = game_page + 1;
		// new Thread(runGameData).start();
		// }
		// });
		mGameListView.addFooterView(loadGameMoreView);
		loadGameMoreButton.setVisibility(View.GONE);

		this.setonLoadMoreListener(new OnLoadMoreListener() {
			@Override
			public void onLoadMore() {
				if (!isLoading && !isFirst) {
					isLoading = true;

					loadGameMoreButton.setText("正在加载中...");
					game_page = game_page + 1;
					new Thread(runGameData).start();
				}

				isFirst = false;
			}
		});

		appGameAdapter = new AppAdapter(appGameInfos, MainActivity.this, cache,
				mGameListView);
		mGameListView.setAdapter(appGameAdapter);
		mGameListView.setOnScrollListener(new OnScrollListener() {

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				switch (scrollState) {
				case SCROLL_STATE_FLING:
					appGameAdapter.isAsyn = true;
					break;
				case SCROLL_STATE_IDLE:
					appGameAdapter.isAsyn = false;
					appGameAdapter.notifyDataSetChanged();
					break;
				case SCROLL_STATE_TOUCH_SCROLL:
					appGameAdapter.isAsyn = false;
					break;

				}

			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				firstItemIndex = firstVisibleItem;
				if ((firstVisibleItem + visibleItemCount == totalItemCount)
						&& (totalItemCount != 0)) {
					if (loadMoreListener != null) {
						loadMoreListener.onLoadMore();
					}
				}
			}
		});

		mGameListView.setonRefreshListener(new OnRefreshListener() {
			@Override
			public void onRefresh() {
				isFirst = true;
				appGameInfos.clear();
				appGameInfos_temp.clear();
				new Thread(runGameData).start();
			}
		});

		mGameListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				AppInfo mAppInfo = (AppInfo) mGameListView.getAdapter()
						.getItem(position);
				if (mAppInfo != null) {
					Intent intent = new Intent(MainActivity.this,
							AppDetailActivity.class);
					intent.putExtra("appid", mAppInfo.getIdx());
					startActivity(intent);
				}
			}
		});

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

	private void initHomeView() {
		mHomeListView = (ListView) homeView.findViewById(R.id.list);
		pHomeBar = (ProgressBar) homeView.findViewById(R.id.pro_bar);
		ll_homeerror = (LinearLayout) homeView.findViewById(R.id.ll_error);
		appHomeInfos = new LinkedList<AppInfo>();
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

		appHomeAdapter = new AppAdapter(appHomeInfos, MainActivity.this, cache,
				mHomeListView);
		mHomeListView.setAdapter(appHomeAdapter);
		appHomeAdapter.notifyDataSetChanged();
		// 注册滑动监听事件
		mHomeListView.setOnScrollListener(new OnScrollListener() {

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				switch (scrollState) {
				case SCROLL_STATE_FLING:
					appHomeAdapter.isAsyn = true;
					break;
				case SCROLL_STATE_IDLE:
					appHomeAdapter.isAsyn = false;
					appHomeAdapter.notifyDataSetChanged();
					break;
				case SCROLL_STATE_TOUCH_SCROLL:
					appHomeAdapter.isAsyn = false;
					break;

				}

			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {

			}
		});
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
				AppInfo mAppInfo = (AppInfo) mHomeListView.getAdapter()
						.getItem(position);
				// Log.d("YTL", "mAppInfo.getIdx() = " + mAppInfo.getIdx());
				Intent intent = new Intent(MainActivity.this,
						AppDetailActivity.class);
				intent.putExtra("appid", mAppInfo.getIdx());
				startActivity(intent);

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
			String str = ToolHelper.donwLoadToString(Global.GAME_MAIN_URL
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
				appGameAdapter.notifyDataSetChanged();
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
				mHomeListView.setVisibility(View.GONE);
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
			pRankBar.setVisibility(View.GONE);
			switch (msg.what) {
			case Global.DOWN_DATA_RANK_FAILLY: {
				ll_rankerror.setVisibility(View.VISIBLE);
				mRankListView.setVisibility(View.GONE);
			}
				break;
			case Global.DOWN_DATA_RANK_SUCCESSFUL: {
				mRankListView.setVisibility(View.VISIBLE);
				ll_rankerror.setVisibility(View.GONE);
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
						Global.MAIN_URL + appiconurl, appurl, "");

				appInfo.setPackageName(jsonObject.getString("apppkgname"));
				appInfo.setVersion(jsonObject.getString("version"));

				appInfo.setInstalled(AppUtils.isInstalled(appName));
				tempList.add(appInfo);
			}

			ArrayList<AppInfo> appManagerUpdateInfos_1 = AppUtils
					.getCanUpadateApp(appManagerInfos, tempList);
			appManagerUpdateInfos.clear();
			appManagerUpdateInfos.addAll(appManagerUpdateInfos_1);
			homeUpdateHandler
					.sendEmptyMessage(Global.DOWN_DATA_HOME_SUCCESSFULL);
		} catch (Exception ex) {
			ex.printStackTrace();
			// Log.e("tag", "error = " + ex.getMessage());
			homeUpdateHandler.sendEmptyMessage(Global.DOWN_DATA_HOME_FAILLY);
		}
	}

	private void ParseHomeJson(String str) {
		try {
			// Log.e("tag", "--------2--------");
			JSONArray jsonArray = new JSONArray(str);
			int len = jsonArray.length();
			LogUtils.d("len", len + "ge");
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
						Global.MAIN_URL + appiconurl, appurl, "");

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
						Global.MAIN_URL + appiconurl, appurl, appDownCount);

				appInfo.setInstalled(AppUtils.isInstalled(appName));
				appRankInfos.add(appInfo);
				Log.e("tag", "info = " + appInfo.toString());
			}
			Log.e("tag", "--------------2--------");
			rankHandler.sendEmptyMessage(Global.DOWN_DATA_RANK_SUCCESSFUL);
		} catch (Exception ex) {
			Log.e("tag", "error = " + ex.getMessage());
			rankHandler.sendEmptyMessage(Global.DOWN_DATA_RANK_FAILLY);
		}
	}

	private void InitViewPager() {
		// TODO Auto-generated method stub\

		t1 = (TextView) findViewById(R.id.text1);
		t2 = (TextView) findViewById(R.id.text2);
		t3 = (TextView) findViewById(R.id.text3);
		t4 = (TextView) findViewById(R.id.text4);
		t5 = (TextView) findViewById(R.id.text5);
		t1.setSelected(true);
		t1.setOnClickListener(new MyOnClickListener(0));
		t2.setOnClickListener(new MyOnClickListener(1));
		t3.setOnClickListener(new MyOnClickListener(2));
		t4.setOnClickListener(new MyOnClickListener(3));
		t5.setOnClickListener(new MyOnClickListener(4));

		mPager = (ViewPager) findViewById(R.id.vPager);
		listViews = new ArrayList<View>();

		LayoutInflater mInflater = getLayoutInflater();
		homeView = mInflater.inflate(R.layout.home, null);
		gameView = mInflater.inflate(R.layout.game, null);
		rankView = mInflater.inflate(R.layout.rank, null);
		logcalGmaeView = mInflater.inflate(R.layout.type, null);
		managerView = mInflater.inflate(R.layout.app_managemer, null);
		listViews.add(homeView);
		listViews.add(gameView);
		listViews.add(logcalGmaeView);
		listViews.add(rankView);
		listViews.add(managerView);

		mPager.setAdapter(new TabPageAdapter(listViews));
		mPager.setCurrentItem(0);
		mPager.setOnPageChangeListener(new MyOnPageChangeListener());

		// 搜索按钮点击事件
		search_btn = (ImageButton) findViewById(R.id.search_btn);
		search_btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MainActivity.this,
						SearchActivity.class);
				startActivity(intent);
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
				t2.setSelected(false);
				t3.setSelected(false);
				t4.setSelected(false);
				t5.setSelected(false);
				break;
			case 1:
				/*if (appGameInfos == null || appGameInfos.size() <= 0) {
					appGameInfos.clear();
					appGameAdapter.notifyDataSetChanged();
					pGameBar.setVisibility(View.VISIBLE);
					ll_gameerror.setVisibility(View.GONE);
					new Thread(runGameData).start();
				}*/
				t1.setSelected(false);
				t2.setSelected(true);
				t3.setSelected(false);
				t4.setSelected(false);
				t5.setSelected(false);
				break;
			case 2:

				t1.setSelected(false);
				t2.setSelected(false);
				t3.setSelected(true);
				t4.setSelected(false);
				t5.setSelected(false);
				break;
			case 3:
				t1.setSelected(false);
				t2.setSelected(false);
				t3.setSelected(false);
				t4.setSelected(true);
				t5.setSelected(false);
				break;
			case 4:
				t1.setSelected(false);
				t2.setSelected(false);
				t3.setSelected(false);
				t4.setSelected(false);
				t5.setSelected(true);
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
			Intent cancalNt  = new Intent();
			cancalNt.setAction("duobaohui.cancalnotifition");
			this.sendBroadcast(cancalNt);
			 //this.finish();
		
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
				ArrayList<AppInfo> appManagerInfos1 = AppUtils
						.getUserApps(MainActivity.this);
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
							if (installAppName != null
									&& installAppName.equals(mAppInfo
											.getAppName())) {
								mAppInfo.setInstalled(true);
								break;
							}
						}

						appHomeAdapter.notifyDataSetChanged();
						break;
					case 1:// 游戏
						for (AppInfo mAppInfo : appGameInfos) {
							if (installAppName != null
									&& installAppName.equals(mAppInfo
											.getAppName())) {
								mAppInfo.setInstalled(true);
								break;
							}
						}

						appGameAdapter.notifyDataSetChanged();
					case 2:// 应用
						break;
					case 3:// 排行
						for (AppInfo mAppInfo : appRankInfos) {
							if (installAppName != null
									&& installAppName.equals(mAppInfo
											.getAppName())) {
								mAppInfo.setInstalled(true);
								break;
							}
						}
						appRankAdapter.notifyDataSetChanged();
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
						appGameAdapter.notifyDataSetChanged();
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

}
