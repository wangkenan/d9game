package me.key.appmarket;

import java.util.ArrayList;

import me.key.appmarket.utils.AppInfo;
import me.key.appmarket.utils.AppUtils;
import me.key.appmarket.utils.ToastUtils;
import android.app.Application;

public class MarketApplication extends Application {

	public static final String PRECENT = "me.key.appmarket.precent";

	private static MarketApplication mInstance;

	private ArrayList<AppInfo> appList;

	public static MarketApplication getInstance() {
		return mInstance;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		mInstance = this;

		ToastUtils.init(this);
	}

	public ArrayList<AppInfo> getAppList() {
		if (appList == null) {
			appList = AppUtils.getInstallApps(this);
		}
		return appList;
	}

	public void reflashAppList() {
		appList = AppUtils.getInstallApps(this);
	}
}
