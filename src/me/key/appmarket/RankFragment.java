package me.key.appmarket;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import net.tsz.afinal.FinalDb;

import org.json.JSONArray;
import org.json.JSONObject;

import me.key.appmarket.RankActivity.DownStateBroadcast;
import me.key.appmarket.RankActivity.MyInstalledReceiver;
import me.key.appmarket.RankActivity.PrecentReceiver;
import me.key.appmarket.adapter.MenuCategoryAdapter;
import me.key.appmarket.adapter.NewRankAdapter;
import me.key.appmarket.tool.DownloadService;
import me.key.appmarket.tool.ToolHelper;
import me.key.appmarket.update.UpdateApk;
import me.key.appmarket.utils.AppInfo;
import me.key.appmarket.utils.AppUtils;
import me.key.appmarket.utils.CategoryInfo;
import me.key.appmarket.utils.Global;
import me.key.appmarket.utils.LogUtils;

import com.market.d9game.R;
import com.slidingmenu.lib2.SlidingMenu;
import com.slidingmenu.lib2.SlidingMenu.OnCloseListener;
import com.slidingmenu.lib2.SlidingMenu.OnOpenedListener;
import com.umeng.analytics.MobclickAgent;

import android.app.Activity;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout.LayoutParams;

/**
 * 分类信息
 * 
 * @author Administrator
 * 
 */
public class RankFragment extends Fragment implements OnClickListener {
	private ListView mRankListView;
	private MenuCategoryAdapter menuCategoryAdapter;
	private ArrayList<AppInfo> appRankInfos;
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
	private ArrayList<AppInfo> appManagerUpdateInfos_t = new ArrayList<AppInfo>();
	private ArrayList<AppInfo> appManagerUpdateInfos = new ArrayList<AppInfo>(); 
	
	private SlidingMenu menu;
	private View inflate;
	private int x;
	private int y;
	private FinalDb db;
	// 设置按钮
	private ImageButton setting;
	private PopupWindow pw;
	// 下载和更新
	private TextView downandupdata;
	// 检查更新
	private TextView checkupdata_pop;
	private TextView about;
	// 退出
	private TextView getout_pop;
	private ImageButton search_btn;
	private TextView updata_num;
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
	private ArrayList<Activity> appLication;
	private View tabRank2;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		inflate = inflater.inflate(R.layout.rank, container, false);
		return inflate;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		FragmentActivity factivity = getActivity();
		if(factivity == null) {
			for(Activity activity :appLication) {
				activity.finish();
			}
		}
		MarketApplication marketApplecation = (MarketApplication) getActivity().getApplication();
		appLication = marketApplecation.getAppLication();
		mRankListView = (ListView) inflate.findViewById(R.id.list_rank);
		//在rankListView中添加广告栏、导航栏等
		View testView = inflate.inflate(getActivity(), R.layout.ranktest, null);
		testView.setPadding(0, 1, 0, 1);
		View advertBanner = inflate.inflate(getActivity(), R.layout.advert_banner, null);
//		advertBanner.setPadding(0, 5, 0, 5);
		View tabRank = inflate.inflate(getActivity(), R.layout.tab_rank_layout, null);
		//tabRank.setPadding(0, 5, 0, 10);
		tabRank2 = (View)inflate.findViewById(R.id.ranktab2);
		mRankListView.addHeaderView(testView,null,false);
		mRankListView.addHeaderView(advertBanner,null,false);
		mRankListView.addHeaderView(tabRank,null,false);
		
		
		//////////////
		rank_pb = (ProgressBar) inflate.findViewById(R.id.rank_pb);
		appRankInfos = new ArrayList<AppInfo>();
		ll_rankerror = (LinearLayout) inflate.findViewById(R.id.ll_error);
		Button btn_refresh = (Button) ll_rankerror.findViewById(R.id.btn_Refsh);
		View contentView = View.inflate(getActivity(), R.layout.popup_item,
				null);
		db = FinalDb.create(getActivity());
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
		updata_num = (TextView) inflate.findViewById(R.id.updata_num);
		downandupdata.setOnClickListener(this);
		checkupdata_pop.setOnClickListener(this);
		getout_pop.setOnClickListener(this);
		about.setOnClickListener(this);
		setting = (ImageButton) inflate.findViewById(R.id.setting);
		setting.setOnClickListener(this);
		// 搜索按钮点击事件
		search_btn = (ImageButton) inflate.findViewById(R.id.search_btn);
		search_btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				MyFragmengManager myFragment = (MyFragmengManager) getActivity();
				myFragment.menu.toggle();
			}
		});
		File cache = new File(Environment.getExternalStorageDirectory(),
				"cache");
		if (!cache.exists()) {
			cache.mkdirs();
		}
		MyInstalledReceiver installedReceiver = new MyInstalledReceiver();
		IntentFilter filter = new IntentFilter();

		filter.addAction("android.intent.action.PACKAGE_ADDED");
		filter.addDataScheme("package");
		getActivity().registerReceiver(installedReceiver, filter);
		LogUtils.d("Main1", menuCategoryAdapter + "");
		new AsyncTask<Void, Void, Void>() {
			@Override
			protected void onPreExecute() {
				// rank_pb.setVisibility(View.VISIBLE);
			};

			protected Void doInBackground(Void... params) {
				/*
				 * String str = ToolHelper.donwLoadToString(Global.GAME_MAIN_URL
				 * + Global.RANK_PAGE); ParseRankJson(str);
				 */
				appRankInfos.clear();
				List<AppInfo> appRankInfos_temp = new ArrayList<AppInfo>();
				appRankInfos_temp = MarketApplication.getInstance()
						.getRankappinfos();
				appRankInfos.addAll(appRankInfos_temp);
				StringBuilder apknamelist = new StringBuilder();
				for (AppInfo ai : appRankInfos) {
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
						appRankInfos, appManagerUpdateInfos_t);
				appManagerUpdateInfos.clear();
				appManagerUpdateInfos.addAll(appManagerUpdateInfos_t);
				LogUtils.d("Main", "appUpdate" + appManagerUpdateInfos.size());
				for (AppInfo appInfo : appManagerUpdateInfos) {
					LogUtils.d("Main", "我可以升级" + appInfo.getPackageName());
					for (AppInfo appManaInfo : appRankInfos) {
						if (appManaInfo.getPackageName().equals(
								appInfo.getPackageName())) {
							appManaInfo.setCanUpdate(true);
							LogUtils.d("Main",
									"我可以升级" + appManaInfo.getPackageName());
						}
					}
				}
				return null;
			}

			protected void onPostExecute(Void result) {
				appRankAdapter.notifyDataSetChanged();
				updata_num.setText(MarketApplication.getInstance().getDownApplist().size()+MarketApplication.getInstance().getAppManagerUpdateInfos().size()+"");
				// rank_pb.setVisibility(View.INVISIBLE);
			};
		}.execute();
		appRankAdapter = new NewRankAdapter(appRankInfos, getActivity(), cache);
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
				if(firstVisibleItem >= 2) {
					tabRank2.setVisibility(View.VISIBLE);
				} else {
					tabRank2.setVisibility(View.INVISIBLE);
				}

			}
		});
		Log.d("YTL", "mAppInfo.getIdx() = ");
		mRankListView.setOnItemClickListener(new OnItemClickListener() {
			
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				AppInfo mAppInfo = (AppInfo) mRankListView.getAdapter()
						.getItem(position);
				Log.d("YTL", "mAppInfo.getIdx() = " + mAppInfo.getIdx());
				Intent intent = new Intent(getActivity(),
						AppDetailActivity.class);
				intent.putExtra("appid", mAppInfo.getIdx());
				intent.putExtra("appinfo", mAppInfo);
				startActivity(intent);
			}
		});
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

	class DownStateBroadcast extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			String fileName = null;
			LogUtils.d("RankActivity", "我接受到了暂停广播");
			for (AppInfo ai : appRankInfos) {
				fileName = DownloadService.CreatFileName(ai.getAppName())
						.getAbsolutePath() + "down";
				if (fileName.equals(intent.getAction())) {
					boolean downState = intent
							.getBooleanExtra("isPause", false);
					ai.setIspause(downState);
					appRankAdapter.notifyDataSetChanged();
					LogUtils.d("RankActivity",
							"我更新了ui" + ai.getAppName() + ai.isIspause());
					break;
				}
			}
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

				appRankAdapter.notifyDataSetChanged();
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
	public void onResume() {
		super.onResume();
		registerPrecent();
		appRankAdapter.notifyDataSetChanged();
		List<AppInfo> downList_temp = new ArrayList<AppInfo>();
		downList_temp = db.findAll(AppInfo.class);
		updata_num.setText(downList_temp.size()+MarketApplication.getInstance().getAppManagerUpdateInfos().size()+"");
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		unregisterPrecent();
	
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
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
