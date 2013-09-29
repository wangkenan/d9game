package me.key.appmarket.widgets;

import me.key.appmarket.LocalGameActivity;
import me.key.appmarket.MainActivity;
import me.key.appmarket.ManagerActivity;
import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.ViewSwitcher;
import android.widget.TabHost.OnTabChangeListener;

import com.market.d9game.R;

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

				//MarketApplication.getInstance().reflashAppList();
				ViewSwitcher vs = (ViewSwitcher) findViewById(R.id.main_bottom_vs);
				vs.showNext();
			default:
				break;
			}
		}
	};
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_bottom);
		TabHost tabHost = getTabHost();
		tabHost.setup();
		from = LayoutInflater.from(this);
		final View bottomView1 = from.inflate(R.layout.item_main_bottom, null);
		final View bottomView2 = from.inflate(R.layout.item_main_bottom, null);
		final View bottomView3 = from.inflate(R.layout.item_main_bottom, null);
		final View bottomView4 = from.inflate(R.layout.item_main_bottom, null);
		final View bottomView5 = from.inflate(R.layout.item_main_bottom_local, null);
		bottomView2.setVisibility(View.INVISIBLE);
		bottomView3.setVisibility(View.INVISIBLE);
		TextView tv = (TextView) bottomView4.findViewById(R.id.main_bottom_tv);
		tv.setText("管理");
		tabHost.addTab(tabHost.newTabSpec("taba").setIndicator(bottomView1)
				.setContent(new Intent(this, MainActivity.class)));
		tabHost.addTab(tabHost.newTabSpec("tabb").setIndicator(bottomView2)
				.setContent(new Intent(this, MainActivity.class)));
		tabHost.addTab(tabHost.newTabSpec("tabd").setIndicator(bottomView5)
				.setContent(new Intent(this, LocalGameActivity.class)));
		tabHost.addTab(tabHost.newTabSpec("tabc").setIndicator(bottomView3)
				.setContent(new Intent(this, MainActivity.class)));
		tabHost.addTab(tabHost.newTabSpec("tabd").setIndicator(bottomView4)
				.setContent(new Intent(this, ManagerActivity.class)));
		myHandler.sendEmptyMessageDelayed(INMAIN, 500);
	}

}
