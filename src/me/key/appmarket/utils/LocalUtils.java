package me.key.appmarket.utils;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.key.appmarket.tool.TxtReader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.os.storage.StorageManager;
import android.util.DisplayMetrics;
import android.widget.Toast;

import com.market.d9game.R;

public class LocalUtils {
	
	
	private static JSONArray jsonArray;

	public static List<AppInfo> InitHomePager(String ItemId,Context context,String Root,List<PackageInfo> packages ) {
		InputStream inputStream;
		if (ItemId.equals("0")) {
			inputStream = context.getResources().openRawResource(R.raw.category_1);
		} else if (ItemId.equals("1")) {
			inputStream = context.getResources().openRawResource(R.raw.category_2);
		} else if(ItemId.equals("2")){
			inputStream = context.getResources().openRawResource(R.raw.category_3);
		} else if(ItemId.equals("3")){
			inputStream = context.getResources().openRawResource(R.raw.category_4);
		} else if(ItemId.equals("4")){
			inputStream = context.getResources().openRawResource(R.raw.category_5);
		} else if(ItemId.equals("5")){
			inputStream = context.getResources().openRawResource(R.raw.category_6);
		} else if(ItemId.equals("6")){
			inputStream = context.getResources().openRawResource(R.raw.category_7);
		} else if(ItemId.equals("7")){
			inputStream = context.getResources().openRawResource(R.raw.category_8);
		} else if(ItemId.equals("8")){
			inputStream = context.getResources().openRawResource(R.raw.category_9);
		} else {
			inputStream = context.getResources().openRawResource(R.raw.category_1);
		}
		String js = (String) TxtReader.getString(inputStream);
		List<AppInfo> mAppInfos = new ArrayList<AppInfo>();
		JSONArray jsonArray;
		LogUtils.d("id", ItemId);
		try {
			jsonArray = new JSONArray(js);
			int len = jsonArray.length();
			for (int i = 0; i < len; i++) {
				AppInfo mAppInfo = new AppInfo();
				JSONObject jsonObject = jsonArray.getJSONObject(i);
				String apkName = jsonObject.getString("apkName");
				File filee = new File(Root + apkName);
				LogUtils.d("apkName", Root +apkName);
				boolean exists = filee.exists();
				if(exists) {
					LogUtils.d("AppInfo", "sf");
				Map list = showUninstallAPKIcon(Root + apkName,context);
				LogUtils.d("apkName", ""+list.size());
				File file = new File(Root + apkName);
				long size = file.length();
				mAppInfo.setAppSize(size + "");
				mAppInfo.setAppName(list.get("label").toString());
				mAppInfo.setAppIcon((Drawable) list.get("icon"));
				mAppInfo.setApkName(apkName);
				mAppInfo.setRoot(Root);
				mAppInfo.setPackageName((String)list.get("pkgname"));
				mAppInfo.setLastTime(Long.MAX_VALUE);
				mAppInfo.setId((String)list.get("pkgname"));
				boolean isIns = AppUtils.isInstalled((String)list.get("pkgname"));
				mAppInfo.setInstalled(isIns);
				mAppInfos.add(mAppInfo);
				LogUtils.d("Local", "context"+context);
				for(PackageInfo pi : packages) {
					if(pi.packageName.equals((String)list.get("pkgname"))) {
						mAppInfos.remove(mAppInfos.get(mAppInfos.size()-1));
						break;
					}
				}
		
				list.clear();
				} 
			}
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return mAppInfos;
		
		
	}

	public static Map showUninstallAPKIcon(String apkPath,Context context) {
		String PATH_PackageParser = "android.content.pm.PackageParser";
		String PATH_AssetManager = "android.content.res.AssetManager";
		try {
			// apk����ļ�·��
			// ����һ��Package ������, �����ص�
			// ���캯��Ĳ���ֻ��һ��, apk�ļ���·��
			// PackageParser packageParser = new PackageParser(apkPath);
			Class pkgParserCls = Class.forName(PATH_PackageParser);
			Class[] typeArgs = new Class[1];
			typeArgs[0] = String.class;
			Constructor pkgParserCt = pkgParserCls.getConstructor(typeArgs);
			Object[] valueArgs = new Object[1];
			valueArgs[0] = apkPath;
			Object pkgParser = pkgParserCt.newInstance(valueArgs);
			// ���������ʾ�йص�, �����漰��һЩ������ʾ�ȵ�, ����ʹ��Ĭ�ϵ����
			DisplayMetrics metrics = new DisplayMetrics();
			metrics.setToDefaults();
			typeArgs = new Class[4];
			typeArgs[0] = File.class;
			typeArgs[1] = String.class;
			typeArgs[2] = DisplayMetrics.class;
			typeArgs[3] = Integer.TYPE;
			Method pkgParser_parsePackageMtd = pkgParserCls.getDeclaredMethod(
					"parsePackage", typeArgs);
			valueArgs = new Object[4];
			valueArgs[0] = new File(apkPath);
			valueArgs[1] = apkPath;
			valueArgs[2] = metrics;
			valueArgs[3] = 0;
			Object pkgParserPkg = pkgParser_parsePackageMtd.invoke(pkgParser,
					valueArgs);
			// Ӧ�ó�����Ϣ��, ���������, ������Щ����, ����û����
LogUtils.d("pkg", pkgParserPkg+"");
			java.lang.reflect.Field appInfoFld = pkgParserPkg.getClass()
					.getDeclaredField("applicationInfo");
			ApplicationInfo info = (ApplicationInfo) appInfoFld
					.get(pkgParserPkg);
			Class assetMagCls = Class.forName(PATH_AssetManager);
			Constructor assetMagCt = assetMagCls.getConstructor((Class[]) null);
			Object assetMag = assetMagCt.newInstance((Object[]) null);
			typeArgs = new Class[1];
			typeArgs[0] = String.class;
			Method assetMag_addAssetPathMtd = assetMagCls.getDeclaredMethod(
					"addAssetPath", typeArgs);
			valueArgs = new Object[1];
			valueArgs[0] = apkPath;
			assetMag_addAssetPathMtd.invoke(assetMag, valueArgs);
			Resources res = context.getResources();
			typeArgs = new Class[3];
			typeArgs[0] = assetMag.getClass();
			typeArgs[1] = res.getDisplayMetrics().getClass();
			typeArgs[2] = res.getConfiguration().getClass();
			Constructor resCt = Resources.class.getConstructor(typeArgs);
			valueArgs = new Object[3];
			valueArgs[0] = assetMag;
			valueArgs[1] = res.getDisplayMetrics();
			valueArgs[2] = res.getConfiguration();
			res = (Resources) resCt.newInstance(valueArgs);
			CharSequence label = null;
			Map<String, Object> list = new HashMap<String, Object>();
			if (info.labelRes != 0) {
				label = res.getText(info.labelRes);
				list.put("label", label);
			} else {
				 PackageManager pm = context.getPackageManager();  
				 label = info.loadLabel(pm);
				 list.put("label", label);
			}
			// ������Ƕ�ȡһ��apk�����ͼ��
			if (info.icon != 0) {
				Drawable icon = res.getDrawable(info.icon);
				list.put("icon", icon);
				if(info.packageName != null) {
					list.put("pkgname", info.packageName);
				}
				return list;
			} 
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	public static String getRoot(Context context) {
		String Root = null;
		StorageManager sm = (StorageManager) context
				.getSystemService(context.STORAGE_SERVICE);
		// 获取sdcard的路径：外置和内置
		try {
			String[] paths = (String[]) sm.getClass()
					.getMethod("getVolumePaths", null).invoke(sm, null);
			for(int i = 0;i<paths.length;i++){
				LogUtils.d("path", paths[i]);
			}
		if(paths.length > 1) {
			Root = paths[1]+"/";
		} else if(paths.length == 1){
			Root = paths[0]+"/";
		} else {
			Toast.makeText(context, "对不起，您没有sd卡", 1).show();
		}
			//Root = paths[0]+"/D9store/";
			LogUtils.d("path", paths.length+"'");
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			Root = Environment.getExternalStorageDirectory().getAbsolutePath()
					 + "/";
			e.printStackTrace();
		}
		return Root;
	}
	/**
	 * 读取gamelist列表里的游戏
	 * @param context
	 * @return
	 */
	public static List<AppInfo> readGameList(Context context ) {
		InputStream inputStream = context.getResources().openRawResource(R.raw.gamelist);
		String js = (String) TxtReader.getJsonStr(inputStream);
		List<AppInfo> gameList = new ArrayList<AppInfo>();
		AppInfo appInfo;
		try {
			jsonArray = new JSONArray(js);
			for(int i = 0;i<jsonArray.length();i++) {
				JSONObject jsonObject = jsonArray.getJSONObject(i);
				String idx = jsonObject.getString("idx");
				String apppkgname = jsonObject.getString("apppkgname");
				appInfo = new AppInfo();
				appInfo.setPackageName(apppkgname);
				appInfo.setIdx(idx);
				gameList.add(appInfo);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return gameList;
	}
}
