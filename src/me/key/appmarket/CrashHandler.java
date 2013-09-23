package me.key.appmarket;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.HashMap;
import java.util.Map;

import me.key.appmarket.utils.LogUtils;

import android.content.Context;
import android.os.Looper;
import android.widget.Toast;

/**
 * UncaughtException处理类,当程序发生未捕获异常的时候,由该类来接管程序
 * 
 * @author chaos
 * 
 */
public class CrashHandler implements UncaughtExceptionHandler {
	// 系统默认的UncaughtException处理类
	private Thread.UncaughtExceptionHandler mDefaultHandler;
	// CrashHandler实例
	private static CrashHandler INSTANCE = new CrashHandler();
	// 程序的Context对象
	private Context mContext;
	// 用来存储设备信息和异常信息
	private Map<String, String> infos = new HashMap<String, String>();

	private CrashHandler() {
	}

	public static CrashHandler getInstance() {
		return INSTANCE;
	}

	public void init(Context context) {
		mContext = context;
		// 获取系统默认的UncaughtException处理器
		mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
		// 设置该CrashHandler为程序的默认处理器
		Thread.setDefaultUncaughtExceptionHandler(this);
	}

	@Override
	public void uncaughtException(Thread thread, Throwable ex) {
		 if (!handleException(ex) && mDefaultHandler != null) {  
	            //如果用户没有处理则让系统默认的异常处理器来处理  
	            mDefaultHandler.uncaughtException(thread, ex); 
	            LogUtils.d("mDefaultHandler", "被我处理了");
	        }else {  
	            try {  
	                Thread.sleep(3000);  
	            } catch (InterruptedException e) {  
	                LogUtils.e("InterruptedException", e.toString());  
	            }  
	            android.os.Process.killProcess(android.os.Process.myPid());  
	            System.exit(1);  
	        }  
	}
	 private boolean handleException(Throwable ex) {  
	        if (ex == null) {  
	            return false;  
	        }  
	        //使用Toast来显示异常信息  
	        new Thread() {  
	            @Override  
	            public void run() {  
	                Looper.prepare();  
	                Toast.makeText(mContext, "很抱歉,程序出现异常,即将退出.", Toast.LENGTH_LONG).show();  
	                Looper.loop();  
	            }  
	        }.start();  
	        return true;  
	    }  

}
