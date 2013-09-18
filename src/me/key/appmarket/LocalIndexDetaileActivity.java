package me.key.appmarket;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.key.appmarket.adapter.MyAdapter;
import me.key.appmarket.tool.ToolHelper;
import me.key.appmarket.tool.TxtReader;
import me.key.appmarket.utils.AppInfo;
import me.key.appmarket.utils.AppUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.market.d9game.R;

import android.app.Activity;
import android.content.pm.ApplicationInfo;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class LocalIndexDetaileActivity extends Activity {
	private String Root = "";
	private ListView mListReco;
	private ProgressBar pBar;
	private String ItemId;

	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.indexdetail);
		Bundle bundle = getIntent().getBundleExtra("value");
		TextView tv_tiltle = (TextView) findViewById(R.id.topbar_title);
		tv_tiltle.setText(bundle.getString("name"));
		ItemId = bundle.getString("id");

		ImageView btnBack = (ImageView) findViewById(R.id.back_icon);
		pBar = (ProgressBar) findViewById(R.id.pro_bar);

		Root = Environment.getExternalStorageDirectory().getAbsolutePath()
				+ "/D9store/";
		boolean isEx = ToolHelper.isExist(Root);
		if (!isEx) {
			Root = ToolHelper.getPath() + "/D9store/";
		}
		InitHomePager();
		btnBack.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				LocalIndexDetaileActivity.this.finish();
			}
		});
	}

	private void InitHomePager() {
		InputStream inputStream;
		inputStream = getResources().openRawResource(R.raw.category_1);
		if (ItemId.equals("1")) {
			inputStream = getResources().openRawResource(R.raw.category_1);
		} else if (ItemId.equals("2")) {
			inputStream = getResources().openRawResource(R.raw.category_2);
		}
		String js = (String) TxtReader.getString(inputStream);
		List<AppInfo> mAppInfos = new ArrayList<AppInfo>();
		JSONArray jsonArray;
		try {
			jsonArray = new JSONArray(js);
			int len = jsonArray.length();
			for (int i = 0; i < len; i++) {
				AppInfo mAppInfo = new AppInfo();
				JSONObject jsonObject = jsonArray.getJSONObject(i);
				String apkName = jsonObject.getString("apkName");
				Map list = showUninstallAPKIcon(Root + apkName);
				File file = new File(Root + apkName);
				long size = file.length();
				mAppInfo.setAppSize(size + "");
				mAppInfo.setAppName(list.get("label").toString());
				mAppInfo.setAppIcon((Drawable) list.get("icon"));
				mAppInfo.setApkName(apkName);
				mAppInfo.setRoot(Root);
				boolean isIns = AppUtils.isInstalled(list.get("label")
						.toString());
				mAppInfo.setInstalled(isIns);
				mAppInfos.add(mAppInfo);
				list.clear();
			}
			mListReco = (ListView) this.findViewById(R.id.mlist);
			Log.v("nano", "nano" + mListReco);
			MyAdapter adapter = new MyAdapter(LocalIndexDetaileActivity.this,
					mAppInfos);
			mListReco.setAdapter(adapter);
			pBar.setVisibility(View.GONE);

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public Map showUninstallAPKIcon(String apkPath) {
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
			Resources res = getResources();
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
			}
			// ������Ƕ�ȡһ��apk�����ͼ��
			if (info.icon != 0) {
				Drawable icon = res.getDrawable(info.icon);
				list.put("icon", icon);
				return list;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
