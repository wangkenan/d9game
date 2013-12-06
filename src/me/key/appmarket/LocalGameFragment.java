package me.key.appmarket;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.TimerTask;

import me.key.appmarket.adapter.MenuCategoryAdapter;
import me.key.appmarket.adapter.MyAdapter;
import me.key.appmarket.adapter.SDGameAdapter;
import me.key.appmarket.network.NetworkUtils;
import me.key.appmarket.tool.DownloadService;
import me.key.appmarket.update.UpdateApk;
import me.key.appmarket.utils.AppInfo;
import me.key.appmarket.utils.AppUtils;
import me.key.appmarket.utils.CategoryInfo;
import me.key.appmarket.utils.LocalAppInfo;
import me.key.appmarket.utils.LocalUtils;
import me.key.appmarket.utils.LogUtils;
import net.tsz.afinal.FinalDb;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.market.d9game.R;
import com.slidingmenu.lib2.SlidingMenu;

public class LocalGameFragment extends Fragment implements OnClickListener {
	private LinearLayout gameLinearLayout;
	private ListView mListReco;
	private ProgressBar pBar;
	private String ItemId;
	private ImageView iv;
	private ListView downmanager_lv;
	private PrecentReceiver mPrecentReceiver;
	// SD卡游戏
	private static List<AppInfo> mAppInfos = new ArrayList<AppInfo>();
	private static MyAdapter adapter;
	private LocalInstallBroadcast receiver;
	private String root;
	private ArrayList<CategoryInfo> gcategoryInfoList_temp = new ArrayList<CategoryInfo>();
	private List<AppInfo> localtopList;
	private SlidingMenu menu;
	private MenuCategoryAdapter menuCategoryAdapter;
	private String apknamelist;
	private TextView updata_num;
	// 我的游戏和内置游戏栏
	private RelativeLayout mygamebar;
	private static SDGameAdapter sdAdapter;

	// 设置按钮
	private ImageButton setting;

	// 一键安装按钮
	private ImageView onkey_localapplist;
	// 我的积分
	private ImageView onkey_myjifen;
	// 免流
	private ImageView onkey_mianliu;
	// 本机已安装
	private static List<AppInfo> appManaInfos_temp = new ArrayList<AppInfo>();
	private static List<AppInfo> downApplist = new ArrayList<AppInfo>();
	private List<AppInfo> appManagerUpdateInfos_t = new ArrayList<AppInfo>();
	private List<AppInfo> appManagerUpdateInfos = new ArrayList<AppInfo>();
	private List<AppInfo> sortTemp = new ArrayList<AppInfo>();
	// private ImageView banner_local;
	private int width;
	private int height;
	private int gapPy;
	private int bigImHeight;
	private int gapPx;
	private static FinalDb db;
	private static final int RESETQUIT = 0;
	private boolean mPreparedQuit = false;
	private View inflate;
	public static TextView mygame;
	public static TextView sdgame;

	private int x;
	private int y;
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
	private PopupWindow pw;
	private static PackageManager packageManager;
	// 下载和更新
	private TextView downandupdata;
	// 检查更新
	private TextView checkupdata_pop;
	// 退出
	private TextView getout_pop;
	// 关于
	private TextView about;
	private ImageButton search_btn;
	private ActivityManager am;

	private View footView;

	// 拼音索引栏
	// private DisapearThread disapearThread;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		inflate = inflater.inflate(R.layout.locat_applist, container, false);
		return inflate;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		Context context = getActivity();
		if (context == null) {

		}
		long currentTimeMillis = System.currentTimeMillis();
		packageManager = context.getPackageManager();
		am = (ActivityManager) context
				.getSystemService(context.ACTIVITY_SERVICE);
		db = FinalDb.create(context);
		// disapearThread = new DisapearThread();
		/*
		 * iv.setOnClickListener(new OnClickListener() {
		 * 
		 * @Override public void onClick(View v) { Intent intent = new
		 * Intent(LocalGameActivity.this, AppDetailActivity.class);
		 * intent.putExtra("appid", 15603+""); startActivity(intent); } });
		 */
		pBar = (ProgressBar) inflate.findViewById(R.id.pro_bar_loacl);
		root = LocalUtils.getRoot(getActivity());
		
		mygamebar = (RelativeLayout) inflate.findViewById(R.id.local_tab);
		mygame = (TextView) mygamebar.findViewById(R.id.mygame);
		sdgame = (TextView) mygamebar.findViewById(R.id.sdgame_tv);
		mListReco = (ListView) inflate.findViewById(R.id.mlist);
		mListReco.setDivider(null);
		// mListReco.setDivider(getResources().getDrawable(R.drawable.driver1));
		// banner_local = (ImageView) inflate.findViewById(R.id.banner_local);
		updata_num = (TextView) inflate.findViewById(R.id.updata_num);
		// setImagePosition(R.drawable.a20131008174300, banner_local);
		View contentView = View.inflate(getActivity(), R.layout.popup_item,
				null);
		pw = new PopupWindow(contentView, LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT, true);
		ColorDrawable cd = new ColorDrawable(-0000);
		pw.setBackgroundDrawable(cd);
		pw.setOutsideTouchable(true);
		downandupdata = (TextView) contentView.findViewById(R.id.downandupdata);
		checkupdata_pop = (TextView) contentView
				.findViewById(R.id.checkupdata_pop);
		getout_pop = (TextView) contentView.findViewById(R.id.getout_pop);
		about = (TextView) contentView.findViewById(R.id.about);
		downandupdata.setOnClickListener(this);
		checkupdata_pop.setOnClickListener(this);
		getout_pop.setOnClickListener(this);
		about.setOnClickListener(this);
		mygame.setOnClickListener(this);
		sdgame.setOnClickListener(this);

		// 搜索按钮点击事件
		search_btn = (ImageButton) inflate.findViewById(R.id.search_btn);
		search_btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				MyFragmengManager myFragment = (MyFragmengManager) getActivity();
				myFragment.menu.toggle();
			}
		});
		/*
		 * mListReco.setOnScrollListener(this);
		 * mListReco.setOnItemClickListener(this);
		 * mListReco.setOnItemLongClickListener(this);
		 */
		new AsyncTask<Void, Void, Void>() {
			
			@Override
			protected void onPreExecute() {
				// TODO Auto-generated method stub
				super.onPreExecute();
				pBar.setVisibility(View.VISIBLE);
			}

			@Override
			protected Void doInBackground(Void... params) {
				long current = System.currentTimeMillis();
				sortTemp = MarketApplication.getInstance().getmAppInfos();
				appManaInfos_temp = MarketApplication.getInstance()
						.getAppManaInfos_temp();
				downApplist = MarketApplication.getInstance().getDownApplist();
				appManagerUpdateInfos = MarketApplication.getInstance()
						.getAppManagerUpdateInfos();
				localtopList = MarketApplication.getInstance()
						.getLocaltopList();
				long now = System.currentTimeMillis();
				LogUtils.d("Local", "nownow"+(now-current));
				return null;
			}

			protected void onPostExecute(Void result) {
				// 如果没有网络或者本地没有游戏
				long current = System.currentTimeMillis();
				if (appManaInfos_temp.size() == 0) {
					List<AppInfo> localList = AppUtils
							.getAppList(getActivity());
					List<AppInfo> readGameList = LocalUtils
							.readGameList(getActivity());
					for (AppInfo gameListAppInfo : readGameList) {
						for (AppInfo localAppInfo : localList) {
							if (gameListAppInfo.getPackageName().equals(
									localAppInfo.getPackageName())) {
								localAppInfo.setIdx(gameListAppInfo.getIdx());
								appManaInfos_temp.add(localAppInfo);
							}
						}
					}
					LogUtils.d("Local", localList.size() + "localList");
				}
				if (localtopList.size() == 0) {
					mAppInfos.addAll(sortTemp);
				} else {
					for (int i = 0; i < localtopList.size(); i++) {
						for (int j = 0; j < sortTemp.size(); j++) {
							if (sortTemp
									.get(j)
									.getPackageName()
									.equals(localtopList.get(i)
											.getPackageName())) {
								mAppInfos.add(sortTemp.get(j));
								sortTemp.remove(sortTemp.get(j));
							}
						}
					}
					mAppInfos.addAll(sortTemp);
				}
				adapter = new MyAdapter(getActivity(), appManaInfos_temp,
						mAppInfos, mListReco);
				footView = inflate.inflate(getActivity(), R.layout.list_item,
						null);
				footView.findViewById(R.id.icon).setVisibility(View.INVISIBLE);
				footView.findViewById(R.id.info).setVisibility(View.INVISIBLE);
				footView.findViewById(R.id.appsize).setVisibility(
						View.INVISIBLE);
				footView.findViewById(R.id.state_btn).setVisibility(
						View.INVISIBLE);
				mListReco.addFooterView(footView);
				mListReco.setAdapter(adapter);
				long now = System.currentTimeMillis();
			LogUtils.d("Local", current - now+"");
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
						DownloadService.watchDog(appManaInfos_temp, am);

					}
				};
				timer.schedule(task, 1000, 3000);
				if (NetworkUtils.isNetworkConnected(getActivity())) {
					mListReco.setOnItemClickListener(new OnItemClickListener() {

						@Override
						public void onItemClick(AdapterView<?> parent,
								View view, int position, long id) {
							if ((position + 1) != mListReco.getAdapter()
									.getCount() && position > 2) {
								// TODO Auto-generated method stub
								AppInfo mAppInfo = (AppInfo) appManaInfos_temp
										.get(position - 3);
								Drawable appIcon = mAppInfo.getAppIcon();
								mAppInfo.setAppIcon(null);
								LogUtils.d("Local",
										"mAppInfo" + mAppInfo.getIdx());
								// Log.d("YTL", "mAppInfo.getIdx() = " +
								// mAppInfo.getIdx());
								Intent intent = new Intent(getActivity(),
										AppDetailActivity.class);
								intent.putExtra("appid", mAppInfo.getIdx());
								intent.putExtra("appinfo", mAppInfo);
								getActivity().startActivity(intent);
								mAppInfo.setAppIcon(appIcon);
							}
						}

					});
				}
			}

		}.execute();
		for (AppInfo appInfo : appManagerUpdateInfos) {
			for (AppInfo appManaInfo : appManaInfos_temp) {
				if (appManaInfo.getPackageName().equals(
						appInfo.getPackageName())) {
					appManaInfo.setCanUpdate(true);
				}
			}
		}
		Log.v("nano", "nano" + mListReco);

		LogUtils.d("LocaBro", "我注册了一次广播");
		gapPx = convertDipOrPx(getActivity(), 5);
		gapPy = convertDipOrPx(getActivity(), 10);
		bigImHeight = (int) ((width - gapPy) / 2 / 1.27f);
		setting = (ImageButton) inflate.findViewById(R.id.setting);
		setting.setOnClickListener(this);
		LogUtils.d("Local", width + "Local");

		// 添加选项点击监听

		mListReco.setOnScrollListener(new OnScrollListener() {

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				if (firstVisibleItem >= 2) {
					mygamebar.setVisibility(View.VISIBLE);

				} else {
					mygamebar.setVisibility(View.INVISIBLE);
				}
			}
		});
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		IntentFilter filter = new IntentFilter();

		filter.addAction("android.intent.action.PACKAGE_ADDED");
		filter.addAction("android.intent.action.PACKAGE_REMOVED");
		filter.addDataScheme("package");
		receiver = new LocalInstallBroadcast();
		getActivity().registerReceiver(receiver, filter);
	}

	@Override
	public void onResume() {
		super.onResume();
		downApplist.clear();
		List<AppInfo> downList_temp = new ArrayList<AppInfo>();
		downList_temp = db.findAll(AppInfo.class);
		downApplist.addAll(downList_temp);
		Collections.reverse(downApplist);
		registerPrecent();
		SharedPreferences sp = getActivity().getSharedPreferences("onkey",
				getActivity().MODE_PRIVATE);
		if (appManagerUpdateInfos != null) {
			updata_num.setText(downApplist.size()
					+ appManagerUpdateInfos.size() + "");
		}
		boolean onkey = sp.getBoolean("onkey", false);
		if (onkey) {
			// onkey_text.setText("装机必备");
		}
		if (appManaInfos_temp != null && adapter != null)
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
		for (AppInfo ai : downApplist) {
			DownStateBroadcast dsb = new DownStateBroadcast();
			IntentFilter filter = new IntentFilter();
			String fileName = DownloadService.CreatFileName(ai.getAppName())
					.getAbsolutePath();
			filter.addAction(fileName + "down");
			getActivity().registerReceiver(dsb, filter);
		}
	}

	public class LocalInstallBroadcast extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			// 接收安装广播
			if (intent.getAction()
					.equals("android.intent.action.PACKAGE_ADDED")) {
				String packageName = intent.getDataString().substring(8);
				// List<AppInfo> down_temp = new ArrayList<AppInfo>();
				// down_temp = db.findAll(AppInfo.class);
				/*
				 * downApplist.clear(); downApplist.addAll(down_temp);
				 */
				MarketApplication.getInstance().reflashAppList();

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
					LogUtils.d("wojieshou2", mAppInfos.get(i).getPackageName()
							+ "");
					if (packageName != null
							&& packageName.equals(mAppInfos.get(i)
									.getPackageName())) {
						// appManaInfos_temp.add(downApplist.get(i));
						mAppInfos.remove(mAppInfos.get(i));
						break;
					}
				}
				for (int i = 0; i < appManaInfos_temp.size(); i++) {
					LogUtils.d("wojieshou", appManaInfos_temp.get(i)
							.getPackageName() + "");
					if (packageName != null
							&& packageName.equals(appManaInfos_temp.get(i)
									.getPackageName())) {
						// appManaInfos_temp.add(mAppInfos.get(i));
						LogUtils.d("Local", "我删除了"
								+ appManaInfos_temp.get(i).getAppName());
						appManaInfos_temp.remove(appManaInfos_temp.get(i));
						sort2lastTime();
						break;
					}
				}
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
					MarketApplication.getInstance().setAppManaInfos_temp(
							appManaInfos_temp);
					sort2lastTime();
					LogUtils.d("Local", "我已经安装了" + appInfo.getAppName()
							+ "regedit" + appManaInfos_temp.size());
					File tempFile = new File(
							Environment.getExternalStorageDirectory(),
							"/market/" + appInfo.getAppName() + ".apk");

					if (tempFile.exists()) {
						tempFile.delete();
					}
					db.delete(appInfo);
				} catch (NameNotFoundException e) {
					e.printStackTrace();
				}

				for (int i = 0; i < appManaInfos_temp.size(); i++) {
					LogUtils.d("wojieshou", appManaInfos_temp.get(i)
							.getPackageName() + "");
					if (packageName != null
							&& packageName.equals(appManaInfos_temp.get(i)
									.getPackageName())) {
						appManaInfos_temp.get(i).setCanUpdate(false);
						sort2lastTime();
						break;
					}
				}
				adapter.notifyDataSetChanged();
				LogUtils.d("Local", "我更新了adapter" + adapter.getCount());
				// 接受卸载广播
			}
			if (intent.getAction().equals(
					"android.intent.action.PACKAGE_REMOVED")) {
				MarketApplication.getInstance().reflashAppList();
				String packageName = intent.getDataString().substring(8);
				LogUtils.d("YTL", "卸载了:" + packageName + "包名的程序");
				for (int i = 0; i < appManaInfos_temp.size(); i++) {
					LogUtils.d("wojieshou", appManaInfos_temp.get(i)
							.getPackageName() + "");
					if (packageName != null
							&& packageName.equals(appManaInfos_temp.get(i)
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
		LogUtils.d("Local", width + "");
		// width = getActivity().getIntent().getExtras().getInt("width");
		DisplayMetrics dm = new DisplayMetrics();
		getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
		width = dm.widthPixels;
		height = dm.heightPixels;
		LogUtils.d("Local", width + "creat");
		Bitmap newbitmap = Bitmap.createBitmap((width - gapPy),
				(int) ((width - gapPy) / 5.34), bm.getConfig());
		getNewBitMapPos(bm, newbitmap);
		LogUtils.d("Local", banner + "banner");
		LogUtils.d("Local", newbitmap + "newbitmap");
		banner.setImageBitmap(newbitmap);
	}

	private void getNewBitMapPos(Bitmap bm, Bitmap newbitmap) {
		Paint paint = new Paint();
		Canvas canvas = new Canvas(newbitmap);
		Matrix matrix = new Matrix();
		double newWidth = 1.00;
		double newHeight = 2.6;
		int gapPx = convertDipOrPx(getActivity(), 5);
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
		getActivity().registerReceiver(mPrecentReceiver, filter);
	}

	private void unregisterPrecent() {
		if (mPrecentReceiver != null) {
			getActivity().unregisterReceiver(mPrecentReceiver);
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		unregisterPrecent();
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

	public static void sort2lastTime() {
		if (appManaInfos_temp != null) {
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
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.mygame:
			adapter.setLeft(adapter.tabHolder);
			break;
		case R.id.sdgame_tv:
			adapter.setRight(adapter.tabHolder);
			break;
		case R.id.setting:
			// 获取view在当前窗体的位置
			int location[] = new int[2];
			setting.getLocationInWindow(location);
			x = location[0] + px2dip(getActivity(), 60);
			y = location[1] + px2dip(getActivity(), 120);
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
			getActivity().stopService(
					new Intent(getActivity(), DownloadService.class));
			break;
		case R.id.about:
			Intent mysc = new Intent();
			mysc.setClass(getActivity(), MyScoreActivity.class);
			startActivity(mysc);
			break;
		}
	}

	public boolean isDownLoaded(String name) {
		boolean result = false;
		SharedPreferences sp = getActivity().getSharedPreferences("down",
				getActivity().MODE_PRIVATE);
		File tempFile = new File(Environment.getExternalStorageDirectory(),
				"/market/" + name + ".apk");

		if (tempFile.exists()) {
			result = true;
		}
		long temp = sp.getLong(tempFile.getAbsolutePath(), -1);
		if (temp != -1) {
			LogUtils.d("DownLoadService", "temp" + temp);
			result = false;
		}
		return result;
	}

	/*
	 * private class DisapearThread implements Runnable { public void run() { //
	 * 避免在1.5s内，用户再次拖动时提示框又执行隐藏命令。 if (scrollState ==
	 * ListView.OnScrollListener.SCROLL_STATE_IDLE) {
	 * txtOverlay.setVisibility(View.INVISIBLE); } } }
	 * 
	 * @Override public void onItemClick(AdapterView<?> parent, View view, int
	 * position, long id) { String telNum =
	 * list.get(p2s.get(position)).getNumber(); if (telNum == null ||
	 * telNum.trim().equals("")) { //为空 } else { String [] str =
	 * telNum.split(";"); telNum = str[0]; Intent telI = new
	 * Intent(Intent.ACTION_CALL,Uri.parse("tel:"+telNum)); startActivity(telI);
	 * }
	 * 
	 * }
	 * 
	 * @Override public boolean onItemLongClick(AdapterView<?> parent, View
	 * view, int position, long id) { AlertDialog diamondAl = new
	 * AlertDialog.Builder(getActivity()).setMessage("更多功能")
	 * .setNegativeButton("确定", new DialogInterface.OnClickListener() {
	 * 
	 * @Override public void onClick(DialogInterface dialog, int which) {
	 * 
	 * } }).setPositiveButton("取消", new DialogInterface.OnClickListener() {
	 * 
	 * @Override public void onClick(DialogInterface dialog, int which) { }
	 * }).create(); diamondAl.show();
	 * 
	 * return false; }
	 * 
	 * @Override public void onScroll(AbsListView view, int firstVisibleItem,
	 * int visibleItemCount, int totalItemCount) { if (mNicks != null &&
	 * mNicks.length > 0)
	 * txtOverlay.setText(String.valueOf(PingYinUtil.converterToFirstSpell
	 * (mNicks[firstVisibleItem + (visibleItemCount >>
	 * 1)]).charAt(0)).toUpperCase()); }
	 * 
	 * @Override public void onScrollStateChanged(AbsListView view, int
	 * scrollState) { this.scrollState = scrollState; if (scrollState ==
	 * ListView.OnScrollListener.SCROLL_STATE_IDLE) {
	 * handler.removeCallbacks(disapearThread); // 提示延迟1s再消失
	 * handler.postDelayed(disapearThread, 1000); } else {
	 * txtOverlay.setVisibility(View.VISIBLE); } } private Map<String, Info>
	 * getContacts() { // 存放多个电话号码 Map<String, Info> map = new HashMap<String,
	 * Info>(); for(AppInfo appInfo : mAppInfos) { map.put(appInfo.getAppName(),
	 * new Info(false, appInfo.getPackageName())); } return map; }
	 */
	public static int px2dip(Context context, float pxValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (pxValue / scale + 0.5f);
	}

	/**
	 * 检查更新
	 * 
	 * @param showToast
	 */
	private void updateSelf(boolean showToast) {
		Calendar c = Calendar.getInstance();
		int mDay = c.get(Calendar.MILLISECOND);// 获取当前月份的日期号码
		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(getActivity());
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

}
