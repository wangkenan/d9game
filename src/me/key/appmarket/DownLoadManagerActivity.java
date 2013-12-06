package me.key.appmarket;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.market.d9game.R;

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
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import me.key.appmarket.LocalGameFragment.LocalInstallBroadcast;
import me.key.appmarket.adapter.DownManagerAdapter;
import me.key.appmarket.adapter.UpdataAdapter;
import me.key.appmarket.utils.AppInfo;
import me.key.appmarket.utils.LogUtils;
import net.tsz.afinal.FinalActivity;
import net.tsz.afinal.FinalDb;
import net.tsz.afinal.annotation.view.ViewInject;
/**
 * 下载管理界面
 * @author Administrator
 *
 */
public class DownLoadManagerActivity extends FinalActivity {
	private FinalDb db;
	private List<AppInfo> downApplist = new ArrayList<AppInfo>();
	private List<AppInfo> down_temp = new ArrayList<AppInfo>();
	private List<AppInfo> updataList = new ArrayList<AppInfo>();
	private DownManagerAdapter dmAdapter;
	private UpdataAdapter upAdapter;
	@ViewInject(id=R.id.down_manager_lv)
	private ListView downLv;
	@ViewInject(id=R.id.iv_back_downmanager,click="onClick")
	private ImageView back_downmanager;
	@ViewInject(id=R.id.tv_manager_downmanager,click="onClick")
	private TextView downlist;
	@ViewInject(id=R.id.tv_update_downmanager,click="onClick")
	private TextView updatalist;
	//进度更新广播
	private PrecentReceiver mPrecentReceiver;
	private PackageManager packageManager;
	private LocalInstallBroadcast lBroadcast;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_downmanager);
		MarketApplication.getInstance().getAppLication().add(this);
		db = FinalDb.create(this);
		init();
		IntentFilter filter = new IntentFilter();

		filter.addAction("android.intent.action.PACKAGE_ADDED");
		filter.addAction("android.intent.action.PACKAGE_REMOVED");
		filter.addDataScheme("package");
		lBroadcast = new LocalInstallBroadcast();
		registerReceiver(lBroadcast, filter);
	}
	
	private void init() {
		new AsyncTask<Void, Void, Void>() {
			@Override
			protected Void doInBackground(Void... params) {
				downApplist = db.findAll(AppInfo.class);
				updataList = MarketApplication.getInstance().getAppManagerUpdateInfos();
				return null;
			}
			@Override
			protected void onPostExecute(Void result) {
				super.onPostExecute(result);
				dmAdapter = new DownManagerAdapter(downApplist,DownLoadManagerActivity.this);
				upAdapter = new UpdataAdapter(updataList, DownLoadManagerActivity.this);
				downLv.setAdapter(dmAdapter);
			}
		}.execute();
		packageManager = getPackageManager();
	}
	@Override
	protected void onResume() {
		super.onResume();
		List<AppInfo> down_temp = new ArrayList<AppInfo>();
		down_temp = db.findAll(AppInfo.class);
		downApplist.clear();
		downApplist.addAll(down_temp);
		Collections.reverse(downApplist);
		if (dmAdapter != null) {
			dmAdapter.notifyDataSetChanged();
		}
		registerPrecent();
	}
	@Override
	protected void onStop() {
		super.onStop();
		unregisterPrecent();
	}
	private void registerPrecent() {mPrecentReceiver
		 = new PrecentReceiver();
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
				if (dmAdapter != null) {
					dmAdapter.notifyDataSetChanged();
				}
			}
		}
	}
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.iv_back_downmanager:
			finish();
			break;
		case R.id.tv_manager_downmanager:
			downLv.setAdapter(dmAdapter);
			downlist.setBackgroundResource(R.drawable.bk_navigate_tv_appdetail);
			updatalist.setBackgroundResource(0);
			List<AppInfo> down_temp = new ArrayList<AppInfo>();
			down_temp = db.findAll(AppInfo.class);
			downApplist.clear();
			downApplist.addAll(down_temp);
			dmAdapter.notifyDataSetChanged();
			break;
		case R.id.tv_update_downmanager:
			downLv.setAdapter(upAdapter);
			updatalist.setBackgroundResource(R.drawable.bk_navigate_tv_appdetail);
			downlist.setBackgroundResource(0);
			break;
		}
	}
	public class LocalInstallBroadcast extends BroadcastReceiver {
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

				for (int i = 0; i < updataList.size(); i++) {
					LogUtils.d("wojieshou2", updataList.get(i)
							.getPackageName() + "");
					if (packageName != null
							&& packageName.equals(updataList.get(i)
									.getPackageName())) {
						// appManaInfos_temp.add(downApplist.get(i));
						updataList.remove(updataList.get(i));
						
						break;
					}
				}

				dmAdapter.notifyDataSetChanged();
				// 接受卸载广播

		}
	}
		
}
	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(lBroadcast);
	}
}
