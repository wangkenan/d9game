package me.key.appmarket;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import me.key.appmarket.RankActivity.DownStateBroadcast;
import me.key.appmarket.RankActivity.MyInstalledReceiver;
import me.key.appmarket.RankActivity.PrecentReceiver;
import me.key.appmarket.adapter.MenuCategoryAdapter;
import me.key.appmarket.adapter.NewRankAdapter;
import me.key.appmarket.tool.DownloadService;
import me.key.appmarket.tool.ToolHelper;
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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
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
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView.OnItemClickListener;
/**
 * 分类信息
 * @author Administrator
 *
 */
public class RankFragment extends Fragment {
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
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		inflate = inflater.inflate(R.layout.rank, container,false);
		return inflate;
	}
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mRankListView = (ListView) inflate.findViewById(R.id.list_rank);
		rank_pb = (ProgressBar) inflate.findViewById(R.id.rank_pb);
		appRankInfos = new ArrayList<AppInfo>();
		ll_rankerror = (LinearLayout) inflate.findViewById(R.id.ll_error);
		Button btn_refresh = (Button) ll_rankerror.findViewById(R.id.btn_Refsh);
		

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
				rank_pb.setVisibility(View.VISIBLE);
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
				rank_pb.setVisibility(View.INVISIBLE);
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

			}
		});
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
	}
	@Override
	public void onDestroy() {
		super.onDestroy();
		unregisterPrecent();
	}
}
