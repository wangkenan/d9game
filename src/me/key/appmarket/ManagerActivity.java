package me.key.appmarket;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

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
import android.content.Intent;
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
		private ProgressBar pro_bar;
		private ManagerAdapter mManagerAdapter;
		private ManagerUpdateAdapter mManagerUpdateAdapter;
		private ArrayList<AppInfo> appManagerInfos = new ArrayList<AppInfo>();
		private ArrayList<AppInfo> appManagerUpdateInfos = new ArrayList<AppInfo>();
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
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.app_managemer);
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
		new AsyncTask<Void, Void, Void>(){

			@Override
			protected Void doInBackground(Void... params) {
				appManagerInfos = AppUtils.getUserApps(ManagerActivity.this,4000);
				return null;
			}
			protected void onPostExecute(Void result) {
				mManagerAdapter = new ManagerAdapter(appManagerInfos,
						ManagerActivity.this, cache);
				mManagerUpdateAdapter = new ManagerUpdateAdapter(appManagerUpdateInfos,
						ManagerActivity.this, cache);
				mManagerListView.setAdapter(mManagerAdapter);
			};
			
		}.execute();
		//appManagerInfos =(ArrayList<AppInfo>) getIntent().getExtras().getSerializable("manager");
	
		//install_app = (Button) this.findViewById(R.id.install_app);
		//update_app = (Button) this.findViewById(R.id.update_app);
		/*install_app.setPadding(40, 0, 40, 0);
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
					appManagerInfos = AppUtils.getUserApps(ManagerActivity.this);
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
							.getInstallAppPackage(ManagerActivity.this);
					mManagerListView.setVisibility(View.GONE);

					pro_bar.setVisibility(View.VISIBLE);
					
				}
			}
		});*/
		
		pro_bar = (ProgressBar) this.findViewById(R.id.pro_bar);
		pro_bar.setVisibility(View.GONE);
		
		mManagerListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				AppUtils.showInstalledAppDetails(ManagerActivity.this,
						appManagerInfos.get(position).getPackageName());
			}
		});
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
}
