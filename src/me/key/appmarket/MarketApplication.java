package me.key.appmarket;


import java.lang.Thread.UncaughtExceptionHandler;
import java.util.ArrayList;
import java.util.List;

import me.key.appmarket.utils.AppInfo;
import me.key.appmarket.utils.AppUtils;
import me.key.appmarket.utils.LogUtils;
import me.key.appmarket.utils.ToastUtils;
import android.app.Activity;
import android.app.Application;
import android.content.Intent;

import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;

public class MarketApplication extends Application {

	public static final String PRECENT = "me.key.appmarket.precent";

	private static MarketApplication mInstance;

	private ArrayList<AppInfo> appList;
	private ArrayList<AppInfo> downApplist = new ArrayList<AppInfo>();
	private final static ArrayList<Activity> activitys = new ArrayList<Activity>();
	
	private List<AppInfo> mAppInfos = new ArrayList<AppInfo>();
	private List<AppInfo> appManaInfos_temp = new ArrayList<AppInfo>();
	private List<AppInfo> appManagerUpdateInfos = new ArrayList<AppInfo>();
	private List<AppInfo> localtopList = new ArrayList<AppInfo>();
	
	public List<AppInfo> getAppManaInfos_temp() {
		return appManaInfos_temp;
	}

	public List<AppInfo> getLocaltopList() {
		return localtopList;
	}

	public void setLocaltopList(List<AppInfo> localtopList) {
		this.localtopList = localtopList;
	}

	public void setAppManaInfos_temp(List<AppInfo> appManaInfos_temp) {
		this.appManaInfos_temp = appManaInfos_temp;
	}

	public List<AppInfo> getAppManagerUpdateInfos() {
		return appManagerUpdateInfos;
	}

	public void setAppManagerUpdateInfos(List<AppInfo> appManagerUpdateInfos) {
		this.appManagerUpdateInfos = appManagerUpdateInfos;
	}

	public List<AppInfo> getmAppInfos() {
		return mAppInfos;
	}

	public void setmAppInfos(List<AppInfo> mAppInfos) {
		this.mAppInfos = mAppInfos;
	}

	public static ArrayList<AppInfo> getRankAppInfos() {
		return rankAppInfos;
	}

	public static void setRankAppInfos(ArrayList<AppInfo> rankAppInfos) {
		MarketApplication.rankAppInfos = rankAppInfos;
	}

	// Rank
	private static ArrayList<AppInfo> rankAppInfos = new ArrayList<AppInfo>();
	private static List<AppInfo> homeAppInfos = new ArrayList<AppInfo>();

	public static List<AppInfo> getHomeAppInfos() {
		return homeAppInfos;
	}

	public static void setHomeAppInfos(List<AppInfo> homeAppInfos) {
		MarketApplication.homeAppInfos = homeAppInfos;
	}

	public static MarketApplication getInstance() {
		return mInstance;
	}

	public static ArrayList<AppInfo> getRankappinfos() {
		return rankAppInfos;
	}

	public ArrayList<AppInfo> getDownApplist() {
		return downApplist;
	}

	public void setDownApplist(ArrayList<AppInfo> downApplist) {
		this.downApplist = downApplist;
	}

	public ArrayList<Activity> getAppLication() {
		return activitys;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		mInstance = this;
		ToastUtils.init(this);
		// 注册未捕获异常处理方式SZAcvxz
		
		CrashHandler crashHandler = CrashHandler.getInstance();
		crashHandler.init(getApplicationContext());
		DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
				.cacheInMemory(true).cacheOnDisc(true).build();
		// 配置imageLoager初始化
		Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
			public void uncaughtException(Thread thread, Throwable ex) {
				LogUtils.d("Local", "我崩溃了");
				
				Intent i = getBaseContext().getPackageManager() 
				        .getLaunchIntentForPackage(getBaseContext().getPackageName()); 
				i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); 
				startActivity(i);
				android.os.Process.killProcess(android.os.Process.myPid());
				System.exit(0);
			}
			});
		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
				this).defaultDisplayImageOptions(defaultOptions)
				.threadPriority(Thread.NORM_PRIORITY - 2)
				.denyCacheImageMultipleSizesInMemory()
				.discCacheFileNameGenerator(new Md5FileNameGenerator())
				.tasksProcessingOrder(QueueProcessingType.LIFO)
				.discCacheSize(50 * 1024 * 1024).discCacheFileCount(100)
				.build();
		ImageLoader.getInstance().init(config);

	}

	public ArrayList<AppInfo> getAppList() {
		if (appList == null) {
			appList = AppUtils.getInstallApps(this);
		}
		return appList;
	}


	@Override
	public void onLowMemory() {
		LogUtils.d("Local", "我被清理内存了onLowMemory");
	}

	public void reflashAppList() {
		appList = AppUtils.getInstallApps(this);
	}

	@Override
	public void onTrimMemory(int level) {
		LogUtils.d("Local", "我被清理内存了");
	}
	
}
