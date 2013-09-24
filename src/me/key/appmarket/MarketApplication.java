package me.key.appmarket;

import java.util.ArrayList;

import me.key.appmarket.utils.AppInfo;
import me.key.appmarket.utils.AppUtils;
import me.key.appmarket.utils.ToastUtils;
import android.app.Application;

import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;

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
		//注册未捕获异常处理方式
		 CrashHandler crashHandler = CrashHandler.getInstance();  
	        crashHandler.init(getApplicationContext());  
		DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
        .cacheInMemory(true)
        .cacheOnDisc(true)
        .build();
		//配置imageLoager初始化
     ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this)
      .defaultDisplayImageOptions(defaultOptions)
        .threadPriority(Thread.NORM_PRIORITY - 2)
        .denyCacheImageMultipleSizesInMemory()
        .discCacheFileNameGenerator(new Md5FileNameGenerator())
        .tasksProcessingOrder(QueueProcessingType.LIFO).discCacheSize(50 * 1024 * 1024).discCacheFileCount(100)
        .build();
       ImageLoader.getInstance().init(config);
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
