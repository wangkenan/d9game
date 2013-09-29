package me.key.appmarket;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import me.key.appmarket.adapter.ManagerAdapter;
import me.key.appmarket.adapter.ManagerUpdateAdapter;
import me.key.appmarket.tool.ToolHelper;
import me.key.appmarket.utils.AppInfo;
import me.key.appmarket.utils.AppUtils;
import me.key.appmarket.utils.Global;

import com.market.d9game.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.AdapterView.OnItemClickListener;
/**
 * 管理界面
 * @author Administrator
 *
 */
public class ManagerActivity extends Activity {
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
		public static List<Activity> activities = new ArrayList<Activity>();
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.app_managemer);
		appManagerInfos = AppUtils.getUserApps(this);

		install_app = (Button) this.findViewById(R.id.install_app);
		update_app = (Button) this.findViewById(R.id.update_app);
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
		});
		File cache = new File(Environment.getExternalStorageDirectory(), "cache");
		if (!cache.exists()) {
			cache.mkdirs();
		}
		pro_bar = (ProgressBar) this.findViewById(R.id.pro_bar);
		pro_bar.setVisibility(View.GONE);
		mManagerListView = (ListView) this
				.findViewById(R.id.manager_list);
		mManagerAdapter = new ManagerAdapter(appManagerInfos,
				ManagerActivity.this, cache);
		mManagerUpdateAdapter = new ManagerUpdateAdapter(appManagerUpdateInfos,
				ManagerActivity.this, cache);
		mManagerListView.setAdapter(mManagerAdapter);
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
}
