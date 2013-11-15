package me.key.appmarket;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.TimerTask;

import org.json.JSONArray;
import org.json.JSONObject;

import me.key.appmarket.LocalGameActivity.DownStateBroadcast;
import me.key.appmarket.LocalGameActivity.LocalInstallBroadcast;
import me.key.appmarket.LocalGameActivity.PrecentReceiver;
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

import com.market.d9game.R;
import com.slidingmenu.lib2.SlidingMenu;
import com.umeng.analytics.MobclickAgent;

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
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class LocalGameFragment extends Fragment implements OnClickListener{
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
	// 我的积分
	private ImageView onkey_myjifen;
	// 免流
	private ImageView onkey_mianliu;
	// 本机已安装
	private List<AppInfo> appManaInfos_temp;
	private List<AppInfo> downApplist = new ArrayList<AppInfo>();
	private List<AppInfo> appManagerUpdateInfos_t = new ArrayList<AppInfo>();
	private List<AppInfo> appManagerUpdateInfos = new ArrayList<AppInfo>();
	private ImageView banner_local;
	private int width;
	private int height;
	private int gapPy;
	private int bigImHeight;
	private int gapPx;
	private FinalDb db;
	private static final int RESETQUIT = 0;
	private boolean mPreparedQuit = false;
	private View inflate;
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
	private PackageManager packageManager;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		inflate = inflater.inflate(R.layout.locat_applist, container, false);
		return inflate;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		packageManager = getActivity().getPackageManager();
		db = FinalDb.create(getActivity());
		/*
		 * iv.setOnClickListener(new OnClickListener() {
		 * 
		 * @Override public void onClick(View v) { Intent intent = new
		 * Intent(LocalGameActivity.this, AppDetailActivity.class);
		 * intent.putExtra("appid", 15603+""); startActivity(intent); } });
		 */
		pBar = (ProgressBar) inflate.findViewById(R.id.pro_bar_loacl);
		onkey_localapplist = (ImageView) inflate.findViewById(R.id.onkey_localapplist);
		onkey_myjifen = (ImageView)inflate.findViewById(R.id.onkey_myjifen);
		onkey_mianliu = (ImageView) inflate.findViewById(R.id.onkey_mianliu);
		onkey_text = (TextView) inflate.findViewById(R.id.onkey_text);
		root = LocalUtils.getRoot(getActivity());
		pBar.setVisibility(View.VISIBLE);
		mygamebar = (LinearLayout) inflate.findViewById(R.id.mygamebar);
		mListReco = (ListView) inflate.findViewById(R.id.mlist);
		banner_local = (ImageView)inflate.findViewById(R.id.banner_local);
		setImagePosition(R.drawable.a20131008174300, banner_local);
		lv = (ListView) inflate.findViewById(R.id.category_lv1);
		banner_local.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getActivity(),
						AppDetailActivity.class);
				intent.putExtra("appid", 15603 + "");
				startActivity(intent);
			}

		});

		mygame = (Button) inflate.findViewById(R.id.mygame);
		sdgame = (Button) inflate.findViewById(R.id.sdgame);

		mygame.setOnClickListener(this);
		sdgame.setOnClickListener(this);
		onkey_localapplist.setOnClickListener(this);
		onkey_myjifen.setOnClickListener(this);
		onkey_mianliu.setOnClickListener(this);
		LogUtils.d("Local", width + "Local");
		new AsyncTask<Void, Void, Void>() {

			@Override
			protected Void doInBackground(Void... params) {
				mAppInfos = MarketApplication.getInstance()
						.getmAppInfos();
				appManaInfos_temp = MarketApplication.getInstance().getAppManaInfos_temp();
				downApplist = MarketApplication.getInstance().getDownApplist();
				appManagerUpdateInfos = MarketApplication.getInstance().getAppManagerUpdateInfos();
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
				adapter = new MyAdapter(getActivity(), mAppInfos,
						appManaInfos_temp, downApplist, appManagerUpdateInfos);
				sdAdapter = new SDGameAdapter(getActivity(), mAppInfos);
				mListReco.setAdapter(adapter);
				TextView tv = (TextView)inflate
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
				final ActivityManager am = (ActivityManager) getActivity().getSystemService(getActivity().ACTIVITY_SERVICE);
				TimerTask task = new TimerTask() {
					public void run() {
						DownloadService.watchDog(appManaInfos_temp, am);

					}
				};
				timer.schedule(task, 1000, 3000);
			}

		}.execute();
		Log.v("nano", "nano" + mListReco);
		IntentFilter filter = new IntentFilter();

		filter.addAction("android.intent.action.PACKAGE_ADDED");
		filter.addAction("android.intent.action.PACKAGE_REMOVED");
		filter.addDataScheme("package");
		receiver = new LocalInstallBroadcast();
		getActivity().registerReceiver(receiver, filter);
		gapPx = convertDipOrPx(getActivity(), 5);
		gapPy = convertDipOrPx(getActivity(), 10);
		bigImHeight = (int) ((width - gapPy) / 2 / 1.27f);
	}

	@Override
	public void onResume() {
		super.onResume();
		List<AppInfo> down_temp = new ArrayList<AppInfo>();
		down_temp = db.findAll(AppInfo.class);
		downApplist.clear();
		downApplist.addAll(down_temp);
		Collections.reverse(downApplist);
		if (adapter != null) {
			adapter.notifyDataSetChanged();
		}
		registerPrecent();
		SharedPreferences sp = getActivity().getSharedPreferences("onkey", getActivity().MODE_PRIVATE);
		boolean onkey = sp.getBoolean("onkey", false);
		if (onkey) {
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
			getActivity().registerReceiver(dsb, filter);
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
						//appManaInfos_temp.remove(appManaInfos_temp.get(i));
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
		LogUtils.d("Local", width + "");
		//width = getActivity().getIntent().getExtras().getInt("width");
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
		case R.id.onkey_localapplist:
			SharedPreferences sp = getActivity().getSharedPreferences("onkey", getActivity().MODE_PRIVATE);
			boolean onkey = sp.getBoolean("onkey", false);
			if (!onkey) {
				Intent intent = new Intent();
				intent.setClass(getActivity(), OneKeyInstallActivity.class);
				startActivity(intent);
			} else {
				onkey_text.setText("装机必备");
				Intent mlintent = new Intent();
				mlintent.setClass(getActivity(), NeceasyActivity.class);
				startActivity(mlintent);
			}
			break;
		case R.id.onkey_myjifen:
			Intent msintent = new Intent();
			msintent.setClass(getActivity(), MyScoreActivity.class);
			startActivity(msintent);
			break;
		case R.id.onkey_mianliu:
			Intent mlintent = new Intent();
			mlintent.setClass(getActivity(), MianLiuActivity.class);
			startActivity(mlintent);
			break;
		}
	}

	public boolean isDownLoaded(String name) {
		boolean result = false;
		SharedPreferences sp = getActivity().getSharedPreferences("down", getActivity().MODE_PRIVATE);
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

	


}
