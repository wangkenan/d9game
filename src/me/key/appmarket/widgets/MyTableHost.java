package me.key.appmarket.widgets;

import java.util.ArrayList;

import me.key.appmarket.LocalGameActivity;
import me.key.appmarket.MainActivity;
import me.key.appmarket.ManagerActivity;
import me.key.appmarket.network.NetworkUtils;
import me.key.appmarket.utils.AppInfo;
import me.key.appmarket.utils.AppUtils;
import me.key.appmarket.utils.LogUtils;
import android.app.TabActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.ViewSwitcher;
import android.widget.TabHost.OnTabChangeListener;

import com.baidu.android.pushservice.PushConstants;
import com.baidu.android.pushservice.PushManager;
import com.market.d9game.R;
import com.umeng.analytics.MobclickAgent;

/**
 * 底部导航菜单栏
 * 
 * @author Administrator
 * 
 */
public class MyTableHost extends TabActivity {
	private LayoutInflater from;
	private static final int INMAIN = 2;
	private Handler myHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case INMAIN:

				// MarketApplication.getInstance().reflashAppList();
				ViewSwitcher vs = (ViewSwitcher) findViewById(R.id.main_bottom_vs);
				vs.showNext();
			default:
				break;
			}
		}
	};
	//寻找游戏
	private TextView tv1;
	private TextView tv2;
	private TextView tv3;
	//管理游戏
	private TextView tv4;
	//本地游戏
	private TextView tv5;
	private View bottomView1;
	private View bottomView2;
	private View bottomView3;
	private View bottomView4;
	private View bottomView5;
	private TabHost tabHost;
	private Drawable findGame_normal;
	private Drawable findGame_focue;
	private Drawable local_focue;
	private Drawable local_normal;
	private Drawable manager_focue;
	private Drawable manager_normal;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_bottom);
		tabHost = getTabHost();
		tabHost.setup();
		from = LayoutInflater.from(this);
		bottomView1 = from.inflate(R.layout.item_main_bottom, null);
		bottomView2 = from.inflate(R.layout.item_main_bottom, null);
		bottomView3 = from.inflate(R.layout.item_main_bottom, null);
		bottomView4 = from.inflate(R.layout.item_main_bottom, null);
		bottomView5 = from.inflate(R.layout.item_main_bottom_local, null);
		bottomView2.setVisibility(View.INVISIBLE);
		bottomView3.setVisibility(View.INVISIBLE);
		findview();
		init();
		int currentTab = tabHost.getCurrentTab();
		LogUtils.d("asfsaf", currentTab+"");
		switch (currentTab) {
		case 0:
			tv1.setCompoundDrawablesWithIntrinsicBounds(null,findGame_focue , null, null);
			break;
		case 3 :
			tv4.setCompoundDrawablesWithIntrinsicBounds(null,manager_focue , null, null);
			break;
		case 4:
			tv5.setCompoundDrawablesWithIntrinsicBounds(null,local_focue , null, null);
			break;
		}
		PushManager.startWork(this, PushConstants.LOGIN_TYPE_API_KEY, "RYganXncxIQeKDe8tsOzUdZp");
		
	}
	private void init() {
		MobclickAgent.setDebugMode( true );
		findGame_normal = getResources().getDrawable(R.drawable.main_tab_recommand_icon_normal);
		findGame_focue = getResources().getDrawable(R.drawable.main_tab_recommand_icon_selected);
		local_focue = getResources().getDrawable(R.drawable.main_tab_play_selected);
		local_normal = getResources().getDrawable(R.drawable.main_tab_play_normal);
		manager_focue = getResources().getDrawable(R.drawable.main_tab_top_icon_selected);
		manager_normal = getResources().getDrawable(R.drawable.main_tab_top_icon_normal);
		tv4.setCompoundDrawablesWithIntrinsicBounds(null,manager_normal , null, null);
		tv4.setText("管理");
		
		tabHost.addTab(tabHost.newTabSpec("findgame").setIndicator(bottomView1)
				.setContent(new Intent(this, MainActivity.class)));
	/*	tabHost.addTab(tabHost.newTabSpec("222").setIndicator(bottomView2)
				.setContent(new Intent(this, MainActivity.class)));*/
		tabHost.addTab(tabHost.newTabSpec("localgame")
				.setIndicator(bottomView5)
				.setContent(new Intent(this, LocalGameActivity.class)));
		/*tabHost.addTab(tabHost.newTabSpec("333").setIndicator(bottomView3)
				.setContent(new Intent(this, MainActivity.class)));*/
		tabHost.addTab(tabHost.newTabSpec("manger").setIndicator(bottomView4)
				.setContent(new Intent(this, ManagerActivity.class)));
		/*Intent tabIntent = new Intent(MyTableHost.this, ManagerActivity.class);
		Bundle bundle = new Bundle();
		ArrayList<AppInfo> userApps = AppUtils.getUserApps(this);
		bundle.putSerializable("manager", userApps);
		tabIntent.putExtras(bundle);
		tabHost.addTab(tabHost.newTabSpec("manger").setIndicator(bottomView4).setContent(tabIntent));*/
		if(NetworkUtils.isNetworkConnected(this)) {
			tabHost.setCurrentTab(0);
		} else {
			tabHost.setCurrentTab(1);
		}
		myHandler.sendEmptyMessageDelayed(INMAIN, 2500);
		tabHost.setOnTabChangedListener(new OnTabChangeListener() {
			@Override
			public void onTabChanged(String tabId) {
				if (tabId.equals("findgame")) {
					tv1.setCompoundDrawablesWithIntrinsicBounds(null,findGame_focue , null, null);
					tv4.setCompoundDrawablesWithIntrinsicBounds(null,manager_normal , null, null);
					tv5.setCompoundDrawablesWithIntrinsicBounds(null,local_normal , null, null);
					LogUtils.d("tv1", "diasnle");
				} else if (tabId.equals("localgame")) {
					tv1.setCompoundDrawablesWithIntrinsicBounds(null,findGame_normal, null, null);
					tv4.setCompoundDrawablesWithIntrinsicBounds(null,manager_normal , null, null);
					tv5.setCompoundDrawablesWithIntrinsicBounds(null,local_focue , null, null);
				} else if (tabId.equals("manger")) {
					tv1.setCompoundDrawablesWithIntrinsicBounds(null,findGame_normal , null, null);
					tv4.setCompoundDrawablesWithIntrinsicBounds(null,manager_focue , null, null);
					tv5.setCompoundDrawablesWithIntrinsicBounds(null,local_normal , null, null);
				}
			}
		});
	}

	private void findview() {
		tv1 = (TextView) bottomView1.findViewById(R.id.main_bottom_tv);
		tv2 = (TextView) bottomView2.findViewById(R.id.main_bottom_tv);
		tv3 = (TextView) bottomView3.findViewById(R.id.main_bottom_tv);
		tv4 = (TextView) bottomView4.findViewById(R.id.main_bottom_tv);
		tv5 = (TextView) bottomView5.findViewById(R.id.main_bottom_local_tv);
	}
	
	
}
