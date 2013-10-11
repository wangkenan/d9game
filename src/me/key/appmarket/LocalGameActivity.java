package me.key.appmarket;

import java.util.ArrayList;
import java.util.List;

import me.key.appmarket.ManagerActivity.ManagerInstalledReceiver;
import me.key.appmarket.adapter.DetaileAdapter;
import me.key.appmarket.adapter.LocalDetailAdapter;
import me.key.appmarket.adapter.MyAdapter;
import me.key.appmarket.utils.AppInfo;
import me.key.appmarket.utils.AppUtils;
import me.key.appmarket.utils.CategoryInfo;
import me.key.appmarket.utils.LocalUtils;
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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

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
	private List<AppInfo> mAppInfos;
	private MyAdapter adapter;
	private LocalInstallBroadcast receiver;
	private String root;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.locat_applist);
		Activity parent = getParent();
		parent.getIntent();
		mListReco = (ListView) this.findViewById(R.id.mlist);
		LayoutInflater inflater = LayoutInflater.from(this);
		// iv = (ImageView) findViewById(R.id.banner_local);
		ImageButton search_btn = (ImageButton) findViewById(R.id.search_btn);
		search_btn = (ImageButton) findViewById(R.id.search_btn);
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

		root = LocalUtils.getRoot(this);

		mAppInfos = LocalUtils.InitHomePager("0", this, root);
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
		TextView tv = (TextView) this.findViewById(R.id.wushju);
		if (mAppInfos.size() == 0) {
			tv.setVisibility(View.VISIBLE);
		} else {
			tv.setVisibility(View.GONE);
		}
		pBar = (ProgressBar) findViewById(R.id.pro_bar);
		adapter = new MyAdapter(this, mAppInfos);
		mListReco.setAdapter(adapter);
		Log.v("nano", "nano" + mListReco);
		LogUtils.d("mAppInfos", mAppInfos.size() + "");
		mListReco.setAdapter(adapter);
		pBar.setVisibility(View.GONE);
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
	}

	@Override
	protected void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
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
				for (int i = 0;i<mAppInfos.size();i++) {
					LogUtils.d("wojieshou",mAppInfos.get(i).getPackageName()+"");
					if (packageName != null
							&& packageName.equals(mAppInfos.get(i)
									.getPackageName())) {
						List<AppInfo> mAppInfos1 = new ArrayList<AppInfo>();
						mAppInfos.clear();
						LogUtils.d("wojieshou", "我接受到了本地安装广播");
						mAppInfos1 = LocalUtils.InitHomePager("0", LocalGameActivity.this, root);
						mAppInfos.addAll(mAppInfos1);
						break;
					}
				 }
				adapter.notifyDataSetChanged();
				//接受卸载广播
			} if (intent.getAction().equals(
					"android.intent.action.PACKAGE_REMOVED")) {
				MarketApplication.getInstance().reflashAppList();
				String packageName = intent.getDataString().substring(8);
				LogUtils.d("YTL", "卸载了:" + packageName + "包名的程序");
				for (int i = 0;i<mAppInfos.size();i++) {
					LogUtils.d("wojieshou",mAppInfos.get(i).getPackageName()+"");
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
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
			if (receiver != null) {
				this.unregisterReceiver(receiver);
			}
	}
}
