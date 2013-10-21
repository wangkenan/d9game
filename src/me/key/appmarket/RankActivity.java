package me.key.appmarket;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import me.key.appmarket.IndexDetaileActivity.PrecentReceiver;
import me.key.appmarket.adapter.NewRankAdapter;
import me.key.appmarket.tool.DownloadService;
import me.key.appmarket.tool.ToolHelper;
import me.key.appmarket.utils.AppInfo;
import me.key.appmarket.utils.AppUtils;
import me.key.appmarket.utils.Global;
import me.key.appmarket.utils.LogUtils;
import me.key.appmarket.utils.ToastUtils;

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
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.ViewSwitcher;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView.OnItemClickListener;

/**
 * 排行
 * 
 * @author Administrator
 * 
 */
public class RankActivity extends Activity {
	private ListView mRankListView;
	private List<AppInfo> appRankInfos;
	private NewRankAdapter appRankAdapter;
	private ProgressBar rank_pb;
	private LinearLayout ll_rankerror;
	private boolean mPreparedQuit = false;
	private static final int DELAYTIME = 5000;
	private static final int RESETQUIT = 0;
	private static final int SHOWNEXT = 1;
	private static final int INMAIN = 2;
	private PrecentReceiver mPrecentReceiver;
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
		setContentView(R.layout.rank);
		mRankListView = (ListView) findViewById(R.id.list_rank);
		rank_pb = (ProgressBar) findViewById(R.id.rank_pb);
		appRankInfos = new ArrayList<AppInfo>();
		ll_rankerror = (LinearLayout) findViewById(R.id.ll_error);
		Button btn_refresh = (Button) ll_rankerror.findViewById(R.id.btn_Refsh);
		File cache = new File(Environment.getExternalStorageDirectory(),
				"cache");
		if (!cache.exists()) {
			cache.mkdirs();
		}
		new AsyncTask<Void, Void, Void>() {
			@Override
			protected void onPreExecute() {
				rank_pb.setVisibility(View.VISIBLE);
			};
			protected Void doInBackground(Void... params) {
				String str = ToolHelper.donwLoadToString(Global.GAME_MAIN_URL
						+ Global.RANK_PAGE);
					ParseRankJson(str);
					for(AppInfo ai : appRankInfos) {
						DownStateBroadcast dsb = new DownStateBroadcast();
						IntentFilter filter = new IntentFilter();
						String fileName =  DownloadService.CreatFileName(ai.getAppName()).getAbsolutePath();
						filter.addAction(fileName+"down");
						registerReceiver(dsb, filter);
						}
					return null;
			}
			protected void onPostExecute(Void result) {
				appRankAdapter.notifyDataSetChanged();
				rank_pb.setVisibility(View.INVISIBLE);
			};
		}.execute();
		appRankAdapter = new NewRankAdapter(appRankInfos, this, cache);
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
				Intent intent = new Intent(RankActivity.this,
						AppDetailActivity.class);
				intent.putExtra("appid", mAppInfo.getIdx());
				intent.putExtra("appinfo", mAppInfo);
				startActivity(intent);
			}
		});
	}
	private void ParseRankJson(String str) {
		try {
			Log.e("tag", "--------------2--------");
			JSONArray jsonArray = new JSONArray(str);
			int len = jsonArray.length();
			for (int i = 0; i < len; i++) {
				JSONObject jsonObject = jsonArray.getJSONObject(i);
				String appName = jsonObject.getString("appname");
				String appiconurl = jsonObject.getString("appiconurl");
				String appSize = jsonObject.getString("appsize");
				String idx = jsonObject.getString("idx");
				String appurl = jsonObject.getString("appurl");
				String appDownCount = jsonObject.getString("appdowncount");
				String apppkgname = jsonObject.getString("apppkgname");
				AppInfo appInfo = new AppInfo(idx, appName, appSize,
						Global.MAIN_URL + appiconurl, appurl, appDownCount, "",appName);
				appInfo.setPackageName(apppkgname);
				appInfo.setInstalled(AppUtils.isInstalled(appName));
				appInfo.setLastTime(Long.MAX_VALUE);
				appRankInfos.add(appInfo);
				// appRankInfos.add(appInfo);
				Log.e("tag", "info = " + appInfo.toString());
			}
			Log.e("tag", "--------------2--------");
			// rankHandler.sendEmptyMessage(Global.DOWN_DATA_RANK_SUCCESSFUL);
		} catch (Exception ex) {
			Log.e("tag", "error = " + ex.getMessage());
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
	@Override
	protected void onResume() {
		super.onResume();
		registerPrecent();
		MobclickAgent.onResume(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		unregisterPrecent();
		MobclickAgent.onPause(this);
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
			LogUtils.d("RankActivity", "我接受到了暂停广播");
			for(AppInfo ai : appRankInfos) {
				fileName =  DownloadService.CreatFileName(ai.getAppName()).getAbsolutePath()+"down";
				if(fileName.equals(intent.getAction())) {
					boolean downState = intent.getBooleanExtra("isPause", false);
					ai.setIspause(downState);
					appRankAdapter.notifyDataSetChanged();
					LogUtils.d("RankActivity", "我更新了ui"+ai.getAppName()+ai.isIspause());
					break;
				}
			}
		}
		
	}
}
