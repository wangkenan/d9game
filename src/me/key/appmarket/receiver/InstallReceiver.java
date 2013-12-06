package me.key.appmarket.receiver;

import java.io.File;

import me.key.appmarket.utils.AppInfo;
import me.key.appmarket.utils.AppUtils;
import me.key.appmarket.utils.InstallAppInfo;
import net.tsz.afinal.FinalDb;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;

public class InstallReceiver extends BroadcastReceiver {
	private FinalDb db;
	private static PackageManager packageManager;
	@Override
	public void onReceive(Context context, Intent intent) {
		//db.create(context);
		packageManager = context.getPackageManager();
		if (intent.getAction()
				.equals("android.intent.action.PACKAGE_ADDED")) {
			String packageName = intent.getDataString().substring(8);
			PackageInfo packageInfo;
			try {
				packageInfo = packageManager.getPackageInfo(
						packageName, 0);
				InstallAppInfo appInfo = new InstallAppInfo();
				appInfo.setPkgname(packageName);
				appInfo.setAppName(packageInfo.applicationInfo.loadLabel(
						packageManager).toString());
				appInfo.setId(packageName);
				appInfo.setLastTime(Long.MAX_VALUE);
				//db.save(appInfo);
			} catch (NameNotFoundException e) {
				e.printStackTrace();
			}
			
		}
	}

}
