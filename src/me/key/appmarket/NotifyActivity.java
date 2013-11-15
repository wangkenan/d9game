package me.key.appmarket;

import android.app.Activity;
import android.os.Bundle;

import com.market.d9game.R;

public class NotifyActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.app_comment_item);
		MarketApplication.getInstance().getAppLication().add(this);
	}
}
