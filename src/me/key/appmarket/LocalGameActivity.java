package me.key.appmarket;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import net.tsz.afinal.FinalDb;

import me.key.appmarket.ManagerActivity.ManagerInstalledReceiver;
import me.key.appmarket.RankActivity.DownStateBroadcast;
import me.key.appmarket.RankActivity.PrecentReceiver;
import me.key.appmarket.adapter.DetaileAdapter;
import me.key.appmarket.adapter.DownManagerAdapter;
import me.key.appmarket.adapter.LocalDetailAdapter;
import me.key.appmarket.adapter.MyAdapter;
import me.key.appmarket.tool.DownloadService;
import me.key.appmarket.utils.AppInfo;
import me.key.appmarket.utils.AppUtils;
import me.key.appmarket.utils.CategoryInfo;
import me.key.appmarket.utils.LocalAppInfo;
import me.key.appmarket.utils.LocalUtils;
import me.key.appmarket.utils.LogUtils;
import me.key.appmarket.utils.Test;
import me.key.appmarket.utils.ToastUtils;

import com.market.d9game.R;
import com.umeng.analytics.MobclickAgent;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ViewSwitcher;

/**
 * 本地游戏界面
 * 
 * @author Administrator
 * 
 */
public class LocalGameActivity extends Activity {
	private LinearLayout gameLinearLayout;
	private ListView mListReco;
	private ProgressBar pBar;
	private String ItemId;
	private ImageView iv;
	private ListView downmanager_lv;
	private PrecentReceiver mPrecentReceiver;
	// SD卡游戏
	private List<AppInfo> mAppInfos;
	private MyAdapter adapter;
	private LocalInstallBroadcast receiver;
	private String root;
	// 本机已安装
	private ArrayList<AppInfo> appManaInfos_temp;
	private ArrayList<AppInfo> downApplist = new ArrayList<AppInfo>();
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
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.locat_applist);
		Activity parent = getParent();
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		width = dm.widthPixels;
		height = dm.heightPixels;
		gapPx = convertDipOrPx(this, 5);
		gapPy = convertDipOrPx(this, 10);
		bigImHeight = (int) ((width - gapPy) / 2 / 1.27f);
		parent.getIntent();
		mListReco = (ListView) this.findViewById(R.id.mlist);
		banner_local = (ImageView) findViewById(R.id.banner_local);
		LayoutInflater inflater = LayoutInflater.from(this);
		// iv = (ImageView) findViewById(R.id.banner_local);
		ImageButton search_btn = (ImageButton) findViewById(R.id.search_btn);
		search_btn = (ImageButton) findViewById(R.id.search_btn);
		downmanager_lv = (ListView) findViewById(R.id.downmanager_lv);
		setImagePosition(R.drawable.a20131008174300, banner_local);
		db = FinalDb.create(this);
		startService(new Intent(this, DownloadService.class));

		banner_local.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(LocalGameActivity.this,
						AppDetailActivity.class);
				intent.putExtra("appid", 15603 + "");
				startActivity(intent);
			}

		});
		search_btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(LocalGameActivity.this,
						SearchActivity.class);
				startActivity(intent);
			}
		});
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
		new AsyncTask<Void, Void, Void>() {

			@Override
			protected Void doInBackground(Void... params) {
				appManaInfos_temp = AppUtils.getUserApps(
						LocalGameActivity.this, 4000);
				mAppInfos = LocalUtils.InitHomePager("0",
						LocalGameActivity.this, root);

				return null;
			}

			protected void onPostExecute(Void result) {
				adapter = new MyAdapter(LocalGameActivity.this, mAppInfos,
						appManaInfos_temp, downApplist);
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
					if(findById2 != null && findById2.getLastTime() != Long.MAX_VALUE) {
						Long lastTime = findById2.getLastTime();
						if(lastTime == Long.MAX_VALUE) {
							ai.setLastTime(lastTime);
						} else {
							long currentTimeMillis = System.currentTimeMillis();
							ai.setLastTime(currentTimeMillis - lastTime);
						}
						adapter.notifyDataSetChanged();
					}
					if (findById != null) {
						Long lastTime = findById.getLastTime();
						if(lastTime == Long.MAX_VALUE) {
							ai.setLastTime(lastTime);
						} else {
							long currentTimeMillis = System.currentTimeMillis();
							ai.setLastTime(currentTimeMillis - lastTime);
						}
						adapter.notifyDataSetChanged();
						LogUtils.d("maxTime", lastTime+"");
					}
				}
				//按照玩的时间进行排序
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

		for (AppInfo ai : downApplist) {
			DownStateBroadcast dsb = new DownStateBroadcast();
			IntentFilter filter = new IntentFilter();
			String fileName = DownloadService.CreatFileName(ai.getAppName())
					.getAbsolutePath();
			filter.addAction(fileName + "down");
			this.registerReceiver(dsb, filter);
		}
		if (appManaInfos_temp != null && adapter != null) {
			for (AppInfo ai : appManaInfos_temp) {
				LocalAppInfo findById = db.findById(ai.getId(),
						LocalAppInfo.class);
				AppInfo findById2 = db.findById(ai.getId(), AppInfo.class);
				if(findById2 != null && findById2.getLastTime() != Long.MAX_VALUE) {
					Long lastTime = findById2.getLastTime();
					if(lastTime == Long.MAX_VALUE) {
						ai.setLastTime(lastTime);
					} else {
						long currentTimeMillis = System.currentTimeMillis();
						ai.setLastTime(currentTimeMillis - lastTime);
					}
					adapter.notifyDataSetChanged();
				}
				if (findById != null) {
					Long lastTime = findById.getLastTime();
					if(lastTime == Long.MAX_VALUE) {
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
	

	}

	@Override
	protected void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
		unregisterPrecent();
	}

	class LocalInstallBroadcast extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			// 接收安装广播
			if (intent.getAction()
					.equals("android.intent.action.PACKAGE_ADDED")) {
				String packageName = intent.getDataString().substring(8);
				LogUtils.d("LocalGmae", "安装了:" + packageName + "包名的程序");

				MarketApplication.getInstance().reflashAppList();
				for (int i = 0; i < mAppInfos.size(); i++) {
					LogUtils.d("wojieshou", mAppInfos.get(i).getPackageName()
							+ "");
					if (packageName != null
							&& packageName.equals(mAppInfos.get(i)
									.getPackageName())) {
						appManaInfos_temp.add(mAppInfos.get(i));
						mAppInfos.remove(mAppInfos.get(i));
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
						appManaInfos_temp.add(downApplist.get(i));
						// downApplist.remove(downApplist.get(i));
						break;
					}
				}
				adapter.notifyDataSetChanged();
				// 接受卸载广播
			}
			if (intent.getAction().equals(
					"android.intent.action.PACKAGE_REMOVED")) {
				MarketApplication.getInstance().reflashAppList();
				String packageName = intent.getDataString().substring(8);
				LogUtils.d("YTL", "卸载了:" + packageName + "包名的程序");
				for (int i = 0; i < mAppInfos.size(); i++) {
					LogUtils.d("wojieshou", mAppInfos.get(i).getPackageName()
							+ "");
					if (packageName != null
							&& packageName.equals(mAppInfos.get(i)
									.getPackageName())) {
						mAppInfos.get(i).setInstalled(false);
						break;
					}
				}
				adapter.notifyDataSetChanged();
			}

		}
	}

	private void setImagePosition(int resId, ImageView banner) {
		Bitmap bm = BitmapFactory.decodeResource(this.getResources(), resId);
		Bitmap newbitmap = Bitmap.createBitmap((width - gapPy),
				(int) ((width - gapPy) / 5.34), bm.getConfig());
		getNewBitMapPos(bm, newbitmap);
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
			// this.finish();

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
					return app1.getLastTime().compareTo(
							app2.getLastTime());
				}
			}

		});
	};
}
