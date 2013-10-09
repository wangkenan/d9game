package me.key.appmarket.network;

import android.content.Context;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

public class NetworkUtils {
	public static final String CTWAP = "ctwap";
	public static final String CMWAP = "cmwap";
	public static final String WAP_3G = "3gwap";
	public static final String UNIWAP = "uniwap";
	public static final int TYPE_NET_WORK_DISABLED = 0;// 网络不可用
	public static final int TYPE_CM_CU_WAP = 4;// 移动联通wap10.0.0.172
	public static final int TYPE_CT_WAP = 5;// 电信wap 10.0.0.200
	public static final int TYPE_OTHER_NET = 6;// 电信,移动,联通,wifi 等net网络 //add by
												// lijing24:包含EDGE.CMNET的2G网络和3G及Wifi
	public static final Uri PREFERRED_APN_URI = Uri
			.parse("content://telephony/carriers/preferapn");

	public static boolean isNeedProxyWap(Context cx) {
		int type = checkNetworkType(cx);
		switch (type) {
		case TYPE_CM_CU_WAP:
			return true;
		default:
			return false;
		}
	}

	public static int checkNetworkType(Context cx) {
		try {
			final ConnectivityManager connectivityManager = (ConnectivityManager) cx
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			final NetworkInfo mobNetInfoActivity = connectivityManager
					.getActiveNetworkInfo();
			if (mobNetInfoActivity == null || !mobNetInfoActivity.isAvailable()) {

				// 注意一：
				// NetworkInfo 为空或者不可以用的时候正常情况应该是当前没有可用网络，
				// 但是有些电信机器，仍可以正常联网，
				// 所以当成net网络处理依然尝试连接网络。
				// （然后在socket中捕捉异常，进行二次判断与用户提示）。

				return TYPE_OTHER_NET;
			} else {

				// NetworkInfo不为null开始判断是网络类型
				int netType = mobNetInfoActivity.getType();
				if (netType == ConnectivityManager.TYPE_WIFI) {
					// wifi net处理
					return TYPE_OTHER_NET;
				} else if (netType == ConnectivityManager.TYPE_MOBILE) {
					// 注意二：
					// 判断是否电信wap:
					// 不要通过getExtraInfo获取接入点名称来判断类型，
					// 因为通过目前电信多种机型测试发现接入点名称大都为#777或者null，
					// 电信机器wap接入点中要比移动联通wap接入点多设置一个用户名和密码,
					// 所以可以通过这个进行判断！

					final Cursor c = cx.getContentResolver().query(
							PREFERRED_APN_URI, null, null, null, null);
					if (c != null) {
						c.moveToFirst();
						final String user = c.getString(c
								.getColumnIndex("user"));
						if (!TextUtils.isEmpty(user)) {
							if (user.startsWith(CTWAP)) {
								return TYPE_CT_WAP;
							}
						}
					}
					c.close();

					// 注意三：
					// 判断是移动联通wap:
					// 其实还有一种方法通过getString(c.getColumnIndex("proxy")获取代理ip
					// 来判断接入点，10.0.0.172就是移动联通wap，10.0.0.200就是电信wap，但在
					// 实际开发中并不是所有机器都能获取到接入点代理信息，例如魅族M9 （2.2）等...
					// 所以采用getExtraInfo获取接入点名字进行判断

					String netMode = mobNetInfoActivity.getExtraInfo();
					if (netMode != null) {
						// 通过apn名称判断是否是联通和移动wap
						netMode = netMode.toLowerCase();
						if (netMode.equals(CMWAP) || netMode.equals(WAP_3G)
								|| netMode.equals(UNIWAP)) {
							return TYPE_CM_CU_WAP;
						}
					}
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			return TYPE_OTHER_NET;
		}

		return TYPE_OTHER_NET;
	}

	public static boolean is3GorWifi(Context cx) {
		int type = TYPE_OTHER_NET;
		try {
			type = getNetType(cx);
		} catch (Exception e) {
			e.printStackTrace();
			type = TYPE_OTHER_NET;
		}
		if (type == WIFI_NETWORK || type == MOBILE3G_NETWORK) {
			return true;
		} else
			return false;
	}

	public static boolean isWifi(Context cx) {
		int type = TYPE_OTHER_NET;
		try {
			type = getNetType(cx);
		} catch (Exception e) {
			e.printStackTrace();
			type = TYPE_OTHER_NET;
		}
		if (type == WIFI_NETWORK) {
			return true;
		}

		return false;
	}

	public static final int UNAVAILABLE_NETWORK = 0;
	public static final int WIFI_NETWORK = 1;

	public static final int MOBILE2G_NETWORK = 2;
	public static final int MOBILE3G_NETWORK = 3;

	public static int getNetType(Context cx) {
		final ConnectivityManager connectivityManager = (ConnectivityManager) cx
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		final NetworkInfo NetInfo = connectivityManager.getActiveNetworkInfo();
		if (NetInfo == null) {
			return UNAVAILABLE_NETWORK;
		}
		int netType = NetInfo.getType();
		int netSubtype = NetInfo.getSubtype();

		if (netType == ConnectivityManager.TYPE_WIFI && NetInfo.isConnected()) {
			return WIFI_NETWORK;
		} else {
			if (netSubtype == TelephonyManager.NETWORK_TYPE_EDGE
					|| netSubtype == TelephonyManager.NETWORK_TYPE_CDMA
					|| netSubtype == TelephonyManager.NETWORK_TYPE_GPRS
					|| netSubtype == TelephonyManager.NETWORK_TYPE_IDEN
					|| netSubtype == TelephonyManager.NETWORK_TYPE_1xRTT) {
				return MOBILE2G_NETWORK;
			} else if (netSubtype != TelephonyManager.NETWORK_TYPE_UNKNOWN) {
				return MOBILE3G_NETWORK;
			}
		}
		return TYPE_OTHER_NET;
	}
	//是否有网络
	 public static boolean isNetworkConnected(Context context) {  
		      if (context != null) {  
		          ConnectivityManager mConnectivityManager = (ConnectivityManager) context  
		                  .getSystemService(Context.CONNECTIVITY_SERVICE);  
		          NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();  
		          if (mNetworkInfo != null) {  
		              return mNetworkInfo.isAvailable();  
		          }  
		      }  
		     return false;  
		 }  
}
