package me.key.appmarket.utils;

import java.io.File;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import me.key.appmarket.MarketApplication;
import me.key.appmarket.tool.ToolHelper;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

public class AppUtils {
	private static int gameSize = 0;
	private static final String SCHEME = "package";
	/**
	 * 调用系统InstalledAppDetails界面所需的Extra名称(用于Android 2.1及之前版本)
	 */
	private static final String APP_PKG_NAME_21 = "com.android.settings.ApplicationPkgName";
	/**
	 * 调用系统InstalledAppDetails界面所需的Extra名称(用于Android 2.2)
	 */
	private static final String APP_PKG_NAME_22 = "pkg";
	/**
	 * InstalledAppDetails所在包名
	 */
	private static final String APP_DETAILS_PACKAGE_NAME = "com.android.settings";
	/**
	 * InstalledAppDetails类名
	 */
	private static final String APP_DETAILS_CLASS_NAME = "com.android.settings.InstalledAppDetails";

	public static ArrayList<AppInfo> getUserApps(Context mContext, int mysize) {
		ArrayList<PackageInfo> appList = new ArrayList<PackageInfo>(); // 用来存储获取的应用信息数据
		List<PackageInfo> packages = mContext.getPackageManager()
				.getInstalledPackages(0);
		LogUtils.d("AppUtil", "packagessize" + packages.size());
		if (mysize > packages.size()) {
			mysize = packages.size();
		}
		for (int i = 0; i < mysize; i++) {
			PackageInfo packageInfo = packages.get(i);
			
		/*	  AppInfo tmpInfo = new AppInfo();
			  tmpInfo.setAppName(packageInfo.applicationInfo.loadLabel(
			  mContext.getPackageManager()).toString());
			  tmpInfo.setPackageName(packageInfo.packageName);
			  
			  String dir = packageInfo.applicationInfo.publicSourceDir; int
			  size = Integer.valueOf((int) new File(dir).length());
			  tmpInfo.setAppSize(size + "");
			  
			  tmpInfo.setVersion(packageInfo.versionName);
			  tmpInfo.setAppIcon(packageInfo.applicationInfo.loadIcon(mContext
			 .getPackageManager()));*/
			 
			if ((packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
				appList.add(packageInfo);// 如果非系统应用，则添加至appList
			}
		}
		// TODO Auto-generated method stub
		StringBuilder sb = new StringBuilder();
		for (PackageInfo ai : appList) {
			sb.append(ai.packageName + ",");
		}
		String uris = sb.toString();
		String str = null;
		if (uris.length() > 0) {
			String newString = uris.substring(0, uris.length() - 1);
			str = ToolHelper.donwLoadToString(Global.FILTERGAME
					+ "?apknamelist=" + newString);
			LogUtils.d("Local", "AppUtil" + str);
		}
		if (str.isEmpty()) {
			return new ArrayList<AppInfo>();
		}
		LogUtils.d("AppUtil", "刷新的str" + str);
		ArrayList<AppInfo> gameList = new ArrayList<AppInfo>();
		JSONArray jsonArray;
		for (int ii = 0; ii < appList.size(); ii++) {
			PackageInfo appInfo = appList.get(ii);
			try {
				jsonArray = new JSONArray(str);
				int len = jsonArray.length();
				for (int i = 0; i < len; i++) {
					JSONObject jsonObject = jsonArray.getJSONObject(i);
					String appName = jsonObject.getString("appname");
					String appiconurl = jsonObject.getString("appiconurl");
					String appSize = jsonObject.getString("appsize");
					String idx = jsonObject.getString("idx");
					String appurl = jsonObject.getString("appurl");
					// String appDownCount =
					// jsonObject.getString("appdowncount");
					String apppkgname = jsonObject.getString("apppkgname");
					String pkgname = jsonObject.getString("apppkgname");

					if (appInfo.packageName.equals(pkgname)) {
						AppInfo tmpInfo = new AppInfo(idx, appName, appSize,
								Global.MAIN_URL + appiconurl, appurl, null, "",
								apppkgname);
						tmpInfo.setPackageName(apppkgname);
						tmpInfo.setInstalled(AppUtils.isInstalled(apppkgname));
						tmpInfo.setLastTime(Long.MAX_VALUE);
						tmpInfo.setAppName(appInfo.applicationInfo.loadLabel(
								mContext.getPackageManager()).toString());
						tmpInfo.setPackageName(appInfo.packageName);
						String dir = appInfo.applicationInfo.publicSourceDir;
						int size = Integer
								.valueOf((int) new File(dir).length());
						tmpInfo.setAppSize(size + "");
						tmpInfo.setId(pkgname);
						tmpInfo.setVersion(appInfo.versionName);
						tmpInfo.setAppIcon(appInfo.applicationInfo
								.loadIcon(mContext.getPackageManager()));
						tmpInfo.setIdx(idx);
						Long max = Long.MAX_VALUE;
						if (max < 0) {
							max *= -1;
						}
						tmpInfo.setLastTime(max);
						gameList.add(tmpInfo);
					}

				}

			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		LogUtils.d("applist", gameList.size() + "");
		return gameList;
	}

	public static ArrayList<AppInfo> getInstallApps(Context mContext) {
		ArrayList<AppInfo> appList = new ArrayList<AppInfo>(); // 用来存储获取的应用信息数据
		List<PackageInfo> packages = mContext.getPackageManager()
				.getInstalledPackages(0);
		for (int i = 0; i < packages.size(); i++) {
			PackageInfo packageInfo = packages.get(i);
			AppInfo tmpInfo = new AppInfo();
			tmpInfo.setPackageName(packageInfo.packageName);
			appList.add(tmpInfo);
		}

		return appList;
	}

	public static boolean isInstalled(String packName) {
		boolean result = false;
		ArrayList<AppInfo> appList = MarketApplication.getInstance()
				.getAppList();
		for (AppInfo mAppInfo : appList) {
			if (mAppInfo.getPackageName() != null && packName != null
					&& mAppInfo.getPackageName().equals(packName)) {
				result = true;
				break;
			}
		}
		return result;
	}

	public static void uninstallAPK(Context context, String packageName) {
		Uri uri = Uri.parse("package:" + packageName);
		Intent intent = new Intent(Intent.ACTION_DELETE, uri);
		context.startActivity(intent);
	}

	public static String getAppName(Context context, String packageName) {
		String result = "";

		PackageManager packageManager = context.getPackageManager();
		List<PackageInfo> packages = packageManager.getInstalledPackages(0);
		PackageInfo pa = null;
		for (int i = 0; i < packages.size(); i++) {
			pa = packages.get(i);
			// 获得应用名
			String packageNameA = pa.packageName;

			if (packageName != null
					&& packageNameA.trim().equals(packageName.trim())) {
				result = packageManager.getApplicationLabel(pa.applicationInfo)
						.toString();
				break;
			}
		}

		return result;
	}

	public static String getPackageName(Context context, String appName) {
		String result = "";

		PackageManager packageManager = context.getPackageManager();

		List<PackageInfo> packages = packageManager.getInstalledPackages(0);
		PackageInfo pa = null;
		for (int i = 0; i < packages.size(); i++) {
			pa = packages.get(i);
			// 获得应用名
			String appLabel = packageManager.getApplicationLabel(
					pa.applicationInfo).toString();
			if (appName != null && appLabel.equals(appName)) {
				// 获得包名
				result = pa.packageName;
				break;
			}
		}

		return result;
	}

	public static void launchApp(Context context, String packageName) {
		PackageManager packageManager = context.getPackageManager();
		Intent intent = packageManager.getLaunchIntentForPackage(packageName);
		if (intent != null) {
			context.startActivity(intent);
		}
	}

	public static void uninstallApp(Context context, String packageName) {
		// Uri packageURI = Uri.parse("package:com.demo.CanavaCancel");
		Uri packageURI = Uri.parse("package:" + packageName);
		Intent uninstallIntent = new Intent(Intent.ACTION_DELETE, packageURI);
		context.startActivity(uninstallIntent);
	}

	/**
	 * 调用系统InstalledAppDetails界面显示已安装应用程序的详细信息。 对于Android 2.3（Api Level
	 * 9）以上，使用SDK提供的接口； 2.3以下，使用非公开的接口（查看InstalledAppDetails源码）。
	 * 
	 * @param context
	 * 
	 * @param packageName
	 *            应用程序的包名
	 */
	public static void showInstalledAppDetails(Context context,
			String packageName) {
		Intent intent = new Intent();
		final int apiLevel = Build.VERSION.SDK_INT;
		if (apiLevel >= 9) { // 2.3（ApiLevel 9）以上，使用SDK提供的接口
			intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
			Uri uri = Uri.fromParts(SCHEME, packageName, null);
			intent.setData(uri);
		} else { // 2.3以下，使用非公开的接口（查看InstalledAppDetails源码）
			// 2.2和2.1中，InstalledAppDetails使用的APP_PKG_NAME不同。
			final String appPkgName = (apiLevel == 8 ? APP_PKG_NAME_22
					: APP_PKG_NAME_21);
			intent.setAction(Intent.ACTION_VIEW);
			intent.setClassName(APP_DETAILS_PACKAGE_NAME,
					APP_DETAILS_CLASS_NAME);
			intent.putExtra(appPkgName, packageName);
		}
		context.startActivity(intent);
	}

	public static String getInstallAppPackage(Context mContext) {
		ArrayList<PackageInfo> appList = new ArrayList<PackageInfo>(); // 用来存储获取的应用信息数据
		List<PackageInfo> packages = mContext.getPackageManager()
				.getInstalledPackages(0);
		LogUtils.d("AppUtil", "packagessize" + packages.size());
		for (int i = 0; i < packages.size(); i++) {
			PackageInfo packageInfo = packages.get(i);
			/*
			 * AppInfo tmpInfo = new AppInfo();
			 * tmpInfo.setAppName(packageInfo.applicationInfo.loadLabel(
			 * mContext.getPackageManager()).toString());
			 * tmpInfo.setPackageName(packageInfo.packageName);
			 * 
			 * String dir = packageInfo.applicationInfo.publicSourceDir; int
			 * size = Integer.valueOf((int) new File(dir).length());
			 * tmpInfo.setAppSize(size + "");
			 * 
			 * tmpInfo.setVersion(packageInfo.versionName);
			 * tmpInfo.setAppIcon(packageInfo.applicationInfo.loadIcon(mContext
			 * .getPackageManager()));
			 */
			if ((packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
				appList.add(packageInfo);// 如果非系统应用，则添加至appList
			}
		}
		// TODO Auto-generated method stub
		StringBuilder sb = new StringBuilder();
		for (PackageInfo ai : appList) {
			sb.append(ai.packageName + ",");
		}
		String uris = sb.toString();
		String str = null;
		String result = "";
		if (uris.length() > 0) {
			String newString = uris.substring(0, uris.length() - 1);
			str = ToolHelper.donwLoadToString(Global.FILTERGAME
					+ "?apknamelist=" + newString);
		}
		if (str.isEmpty()) {
			return null;
		}
		try {
			JSONArray jsonArray = new JSONArray(str);
			int len = jsonArray.length();
			for (int i = 0; i < len; i++) {
				LogUtils.d("APPuTIL", "len:" + len);
				JSONObject jsonObject = jsonArray.getJSONObject(i);
				String pkgname = jsonObject.getString("apppkgname");
				result = result + "," + pkgname;
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return result;
	}

	public static ArrayList<AppInfo> getCanUpadateApp(
			ArrayList<AppInfo> allApp, ArrayList<AppInfo> serverApps) {
		ArrayList<AppInfo> tempList = new ArrayList<AppInfo>();
		LogUtils.d("AppUtil", "allapplist:" + allApp.size());
		int testCount = 0;
		for (AppInfo mAppInfo : allApp) {
			String packageName = mAppInfo.getPackageName();
			String appVersion = mAppInfo.getVersion();
			for (AppInfo serverApp : serverApps) {
				String spackageName = serverApp.getPackageName();
				String sappVersion = serverApp.getVersion();
				if (spackageName.equals(packageName)) {
					for (int i = 0; i < sappVersion.length(); i++) {
						if (i < appVersion.length()) {
							char tempchar = appVersion.charAt(i);
							char schar = sappVersion.charAt(i);
							if (schar > tempchar) {
								testCount++;
								tempList.add(serverApp);
								break;
							}
						}
					}
				}
			}
		}
		LogUtils.d("count", testCount + "testcount");
		/*
		 * for (AppInfo mAppInfo : allApp) { String appVersion =
		 * mAppInfo.getVersion(); String packageName =
		 * mAppInfo.getPackageName(); LogUtils.d("appVersion", appVersion);
		 * String[] appVersions = appVersion.split("\\."); for (AppInfo
		 * serverApp : serverApps) { if (serverApp.getPackageName() != null &&
		 * !serverApp.getPackageName().equals("") &&
		 * !serverApp.getPackageName().equals("null") &&
		 * serverApp.getPackageName().equals(packageName)) { String
		 * serverVersion = serverApp.getVersion();
		 * 
		 * String[] serverVersions = serverVersion.split("\\."); if
		 * (serverVersions.length > 0 && appVersions.length > 0) { int
		 * tempServer = Integer.parseInt(serverVersions[0]); int tempApp =
		 * Integer.parseInt(appVersions[0]); if (tempServer > tempApp) {
		 * tempList.add(serverApp); break; } else if (tempServer < tempApp) {
		 * break; } }
		 * 
		 * if (serverVersions.length > 1 && appVersions.length > 1) { int
		 * tempServer = Integer.parseInt(serverVersions[1]); int tempApp =
		 * Integer.parseInt(appVersions[1]); if (tempServer > tempApp) {
		 * tempList.add(serverApp); break; } else if (tempServer < tempApp) {
		 * break; } }
		 * 
		 * if (serverVersions.length > 2 && appVersions.length > 2) { int
		 * tempServer = Integer.parseInt(serverVersions[2]);
		 * LogUtils.d("ApUtil", appVersions[2]); int tempApp =
		 * Integer.parseInt(appVersions[2]); if (tempServer > tempApp) {
		 * tempList.add(serverApp); break; } else if (tempServer < tempApp) {
		 * break; } } } } }
		 */
		LogUtils.d("APPuTil", "temp.size" + tempList.size());
		return tempList;
	}

	public static List<AppInfo> getAppList(Context mContext) {
		ArrayList<PackageInfo> packList = new ArrayList<PackageInfo>(); // 用来存储获取的应用信息数据
		List<PackageInfo> packages = mContext.getPackageManager()
				.getInstalledPackages(0);
		List<AppInfo> appLists = new ArrayList<AppInfo>();
		LogUtils.d("AppUtil", "packagessize" + packages.size());
		for (int i = 0; i < packages.size(); i++) {
			PackageInfo packageInfo = packages.get(i);
			/*
			 * AppInfo tmpInfo = new AppInfo();
			 * tmpInfo.setAppName(packageInfo.applicationInfo.loadLabel(
			 * mContext.getPackageManager()).toString());
			 * tmpInfo.setPackageName(packageInfo.packageName);
			 * 
			 * String dir = packageInfo.applicationInfo.publicSourceDir; int
			 * size = Integer.valueOf((int) new File(dir).length());
			 * tmpInfo.setAppSize(size + "");
			 * 
			 * tmpInfo.setVersion(packageInfo.versionName);
			 * tmpInfo.setAppIcon(packageInfo.applicationInfo.loadIcon(mContext
			 * .getPackageManager()));
			 */
			if ((packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
				packList.add(packageInfo);// 如果非系统应用，则添加至appList
				AppInfo tmpInfo = new AppInfo();
				tmpInfo.setAppName(packageInfo.applicationInfo.loadLabel(
						mContext.getPackageManager()).toString());
				tmpInfo.setPackageName(packageInfo.packageName);

				String dir = packageInfo.applicationInfo.publicSourceDir;
				int size = Integer.valueOf((int) new File(dir).length());
				tmpInfo.setAppSize(size + "");

				tmpInfo.setVersion(packageInfo.versionName);
				tmpInfo.setAppIcon(packageInfo.applicationInfo
						.loadIcon(mContext.getPackageManager()));
				tmpInfo.setId(packageInfo.packageName);
				tmpInfo.setLastTime(System.currentTimeMillis());
				appLists.add(tmpInfo);
			}

		}
		return appLists;
	}
}
