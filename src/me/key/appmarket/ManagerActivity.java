package me.key.appmarket;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import me.key.appmarket.MainActivity.PrecentReceiver;
import me.key.appmarket.adapter.AppAdapter;
import me.key.appmarket.adapter.ManagerAdapter;
import me.key.appmarket.adapter.ManagerUpdateAdapter;
import me.key.appmarket.tool.ToolHelper;
import me.key.appmarket.utils.AppInfo;
import me.key.appmarket.utils.AppUtils;
import me.key.appmarket.utils.Global;
import me.key.appmarket.utils.LogUtils;

import com.market.d9game.R;
import com.umeng.analytics.MobclickAgent;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView.OnItemClickListener;
/**
 * 管理界面
 * @author Administrator
 *
 */
public class ManagerActivity extends Activity implements OnScrollListener{
	// 管理
		private ListView mManagerListView;
		private ManagerInstalledReceiver receiver;
		private ManagerAdapter mManagerAdapter;
		private ManagerUpdateAdapter mManagerUpdateAdapter;
		private ArrayList<AppInfo> appManagerInfos = new ArrayList<AppInfo>();
		private ArrayList<AppInfo> appManagerUpdateInfos = new ArrayList<AppInfo>();
		private ArrayList<AppInfo> appManagerUpdateInfos_t = new ArrayList<AppInfo>();
		private boolean isLoading = false;
		private boolean isFirst = false;
		private Button install_app;
		private Button update_app;
		private boolean isShowingInstall = true;
		private ProgressBar pBar;
		private LinkedList<AppInfo> appDatainfos;
		private View loadMoreView;
		private Button loadMoreButton;
		private String apknamelist;
		private TextView tv_empty;
		private int page = 1; // 最后的可视项索引
		public static List<Activity> activities = new ArrayList<Activity>();
		private PrecentReceiver mPrecentReceiver;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.app_managemer);
		registerPrecent();
		final File cache = new File(Environment.getExternalStorageDirectory(), "cache");
		if (!cache.exists()) {
			cache.mkdirs();
		}
		// TODO Auto-generated method stub
		mManagerListView = (ListView) findViewById(R.id.manalist);
		pBar = (ProgressBar) findViewById(R.id.pro_bar);
		appDatainfos = new LinkedList<AppInfo>();
	
		pBar.setVisibility(View.VISIBLE);
		tv_empty = (TextView) findViewById(R.id.empty);
		tv_empty.setVisibility(View.GONE);
		ImageView btnBack = (ImageView) findViewById(R.id.back_icon);
		loadMoreView = getLayoutInflater().inflate(R.layout.loadmore, null);
		loadMoreButton = (Button) loadMoreView
				.findViewById(R.id.loadMoreButton);
		IntentFilter filter = new IntentFilter();

		filter.addAction("android.intent.action.PACKAGE_ADDED");
		filter.addAction("android.intent.action.PACKAGE_REMOVED");
		filter.addDataScheme("package");
		receiver = new ManagerInstalledReceiver();
		registerReceiver(receiver, filter);
		// loadMoreButton.setOnClickListener(new View.OnClickListener() {
		// @Override
		// public void onClick(View v) {
		// loadMoreButton.setText("正在加载中...");
		// handler.postDelayed(new Runnable() {
		// @Override
		// public void run() {
		// loadMoreData();
		// appAdapter.notifyDataSetChanged();
		// loadMoreButton.setText("加载更多...");
		// }
		// },2000);
		// }
		// });
		mManagerListView.addFooterView(loadMoreView);
		//mManagerListView.setOnScrollListener(this);
		
		loadMoreButton.setVisibility(View.GONE);
		mManagerAdapter = new ManagerAdapter(appManagerInfos,
				ManagerActivity.this, cache);
		mManagerListView.setAdapter(mManagerAdapter);
		new AsyncTask<Void, Void, Void>(){

			@Override
			protected Void doInBackground(Void... params) {
				ArrayList<AppInfo> appManaInfos_temp = new ArrayList<AppInfo>();
				appManaInfos_temp = AppUtils.getUserApps(ManagerActivity.this,4000);
				appManagerInfos.clear();
				appManagerInfos.addAll(appManaInfos_temp);
				return null;
			}
			protected void onPostExecute(Void result) {
				mManagerAdapter.notifyDataSetChanged();
				pBar.setVisibility(View.GONE);
			};
			
		}.execute();
		mManagerUpdateAdapter = new ManagerUpdateAdapter(appManagerUpdateInfos, this, cache);
		new AsyncTask<Void, Void, Void>(){

			@Override
			protected Void doInBackground(Void... params) {
				ArrayList<AppInfo> userApps = AppUtils.getUserApps(ManagerActivity.this, 4000);
				apknamelist = AppUtils
						.getInstallAppPackage(ManagerActivity.this);
				String str = ToolHelper.donwLoadToString(Global.MAIN_URL
						+ Global.UPGRADEVERSION + "?apknamelist=" + apknamelist);
				ParseUpdateJson(str);
				appManagerUpdateInfos_t = AppUtils.getCanUpadateApp(userApps, appManagerUpdateInfos_t);
				appManagerUpdateInfos.clear();
				appManagerUpdateInfos.addAll(appManagerUpdateInfos_t);
				LogUtils.d("Mana", "appUpdate"+appManagerUpdateInfos.size());
				return null;
			}
			protected void onPostExecute(Void result) {
				mManagerUpdateAdapter.notifyDataSetChanged();
			};
			
		}.execute();
		//appManagerInfos =(ArrayList<AppInfo>) getIntent().getExtras().getSerializable("manager");
	
		install_app = (Button) this.findViewById(R.id.install_app);
		update_app = (Button) this.findViewById(R.id.update_app);
		install_app.setPadding(40, 0, 40, 0);
		update_app.setPadding(40, 0, 40, 0);

		install_app.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

					install_app.setBackgroundResource(R.drawable.btn_bar_2);
					install_app.setPadding(40, 0, 40, 0);
					update_app.setBackgroundResource(0);
					mManagerListView.setAdapter(mManagerAdapter);
					mManagerListView
							.setOnItemClickListener(new OnItemClickListener() {
								@Override
								public void onItemClick(AdapterView<?> parent,
										View view, int position, long id) {
									AppUtils.showInstalledAppDetails(
											ManagerActivity.this, appManagerInfos
													.get(position)
													.getPackageName());
								}
							});
			}
		});
		update_app.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
					update_app.setBackgroundResource(R.drawable.btn_bar_2);
					update_app.setPadding(40, 0, 40, 0);
					install_app.setBackgroundResource(0);
					mManagerListView.setAdapter(mManagerUpdateAdapter);
			}
		});
		
/*		
		mManagerListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				AppUtils.showInstalledAppDetails(ManagerActivity.this,
						appManagerInfos.get(position).getPackageName());
			}
		});*/
	}
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
	Handler homeUpdateHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			switch (msg.what) {
			case Global.DOWN_DATA_HOME_FAILLY: {
			}
				break;
			case Global.DOWN_DATA_HOME_SUCCESSFULL: {
				pBar.setVisibility(View.GONE);
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
								Intent intent = new Intent(ManagerActivity.this,
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
						Global.MAIN_URL + appiconurl, appurl, "","",appName);

				appInfo.setPackageName(jsonObject.getString("apppkgname"));
				appInfo.setVersion(jsonObject.getString("version"));

				appInfo.setInstalled(AppUtils.isInstalled(appName));
				tempList.add(appInfo);
			}
			LogUtils.d("Mana", "temp:"+tempList.size());
			appManagerUpdateInfos_t.clear();
			appManagerUpdateInfos_t.addAll(tempList);
			/*homeUpdateHandler
					.sendEmptyMessage(Global.DOWN_DATA_HOME_SUCCESSFULL);*/
		} catch (Exception ex) {
			ex.printStackTrace();
			// Log.e("tag", "error = " + ex.getMessage());
			homeUpdateHandler.sendEmptyMessage(Global.DOWN_DATA_HOME_FAILLY);
		}
	}
	@Override
	public void onScroll(AbsListView arg0, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		// TODO Auto-generated method stub
		if ((firstVisibleItem + visibleItemCount == totalItemCount)
				&& (totalItemCount != 0)) {
			if (!isLoading && !isFirst) {
				isLoading = true;
					loadMoreButton.setText("正在加载中...");
					loadMoreButton.setVisibility(View.VISIBLE);
					page = page + 1;
					//new Thread(runnable).start();
					new AsyncTask<Void, Void, Void>(){

						@Override
						protected Void doInBackground(Void... params) {
							ArrayList<AppInfo> appManagerInfos_temp = new ArrayList<AppInfo>();
							appManagerInfos_temp = AppUtils.getUserApps(ManagerActivity.this,10 *page);
							appManagerInfos.clear();
							appManagerInfos.addAll(appManagerInfos_temp);
							LogUtils.d("page", page+""+"aaaaaa"+appManagerInfos.size());
							return null;
						}
						protected void onPostExecute(Void result) {
							loadMoreButton.setVisibility(View.GONE);
							LogUtils.d("post", "我运行了");
							mManagerAdapter.notifyDataSetChanged();
							pBar.setVisibility(View.GONE);
							isLoading = false;
						};
					}.execute();
			}

			isFirst = false;
		}
	}
	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		// TODO Auto-generated method stub
		
	}
	@Override
	protected void onResume() {
		super.onResume();
		MobclickAgent.onResume(this);
	}
	@Override
	protected void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	}
	class ManagerInstalledReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			// 接收安装广播
			if (intent.getAction()
					.equals("android.intent.action.PACKAGE_ADDED")) {
				String packageName = intent.getDataString().substring(8);
				LogUtils.d("Manager", "安装了:" + packageName + "包名的程序");

				MarketApplication.getInstance().reflashAppList();
				/*String installAppName = AppUtils.getAppName(context,
						packageName);*/

				// 刷新管理界面
				new AsyncTask<Void, Void, Void>(){

					private ArrayList<AppInfo> appManagerInfos1;

					@Override
					protected Void doInBackground(Void... params) {
						appManagerInfos1 = AppUtils
								.getUserApps(ManagerActivity.this,4000);
						return null;
					}
					protected void onPostExecute(Void result) {
						appManagerInfos.clear();
						appManagerInfos.addAll(appManagerInfos1);
						if (mManagerAdapter != null) {
							LogUtils.d("Manager", "我被刷新了"+appManagerInfos1.size());
							mManagerAdapter.notifyDataSetChanged();
						}
					};
					
				}.execute();
				new AsyncTask<Void, Void, Void>(){

					private ArrayList<AppInfo> appManagerupdatainfo_t;

					@Override
					protected Void doInBackground(Void... params) {
						ArrayList<AppInfo> userApps = AppUtils.getUserApps(ManagerActivity.this, 4000);
						apknamelist = AppUtils
								.getInstallAppPackage(ManagerActivity.this);
						String str = ToolHelper.donwLoadToString(Global.MAIN_URL
								+ Global.UPGRADEVERSION + "?apknamelist=" + apknamelist);
						ParseUpdateJson(str);
						appManagerUpdateInfos_t = AppUtils.getCanUpadateApp(userApps, appManagerUpdateInfos_t);
						appManagerUpdateInfos.clear();
						appManagerUpdateInfos.addAll(appManagerUpdateInfos_t);
						return null;
					}
					protected void onPostExecute(Void result) {
						if (mManagerUpdateAdapter != null) {
							LogUtils.d("Manager", "我被刷新了"+appManagerUpdateInfos.size());
							mManagerUpdateAdapter.notifyDataSetChanged();
						}
					};
					
				}.execute();
			
			

						/*if (!isShowingInstall) {
							update_app
									.setBackgroundResource(R.drawable.btn_bar_2);
							update_app.setPadding(40, 0, 40, 0);
							install_app.setBackgroundResource(0);

							apknamelist = AppUtils
									.getInstallAppPackage(ManagerActivity.this);
							mManagerListView.setVisibility(View.GONE);

							pro_bar.setVisibility(View.VISIBLE);
							new Thread(runUpdateAppData).start();
						}*/
					}
			// 接收卸载广播
			if (intent.getAction().equals(
					"android.intent.action.PACKAGE_REMOVED")) {
				MarketApplication.getInstance().reflashAppList();
				String packageName = intent.getDataString().substring(8);
				LogUtils.d("YTL", "卸载了:" + packageName + "包名的程序");

						LogUtils.d("YTL", "当前显示的是推荐界面");
						for (AppInfo mAppInfo : appManagerInfos) {
							if (packageName != null
									&& packageName.equals(mAppInfo
											.getPackageName())) {
								appManagerInfos.remove(mAppInfo);
								break;
							}
						}
						for (AppInfo mAppInfo : appManagerUpdateInfos) {
							if (packageName != null
									&& packageName.equals(mAppInfo
											.getPackageName())) {
								appManagerUpdateInfos.remove(mAppInfo);
								break;
							}
						}
						
						mManagerAdapter.notifyDataSetChanged();
						mManagerUpdateAdapter.notifyDataSetChanged();
				}
		}
	}
	@Override
	protected void onDestroy() {
		super.onDestroy();
			if (receiver != null) {
				this.unregisterReceiver(receiver);
			}
			unregisterPrecent();
	}
	class PrecentReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(MarketApplication.PRECENT)) {
				// 下载中刷新界面进度
				if (mManagerUpdateAdapter != null) {
					mManagerUpdateAdapter.notifyDataSetChanged();
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
}
