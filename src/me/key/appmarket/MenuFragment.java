package me.key.appmarket;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import me.key.appmarket.adapter.ClassifyAdapter;
import me.key.appmarket.adapter.DetaileAdapter;
import me.key.appmarket.tool.ToolHelper;
import me.key.appmarket.utils.AppInfo;
import me.key.appmarket.utils.AppUtils;
import me.key.appmarket.utils.CategoryInfo;
import me.key.appmarket.utils.Global;
import me.key.appmarket.utils.LogUtils;

import com.market.d9game.R;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;


//分类
public class MenuFragment extends Fragment {
	private View view;
	private ClassifyAdapter cAdapter;
	private ListView classLv;
	private List<AppInfo> list = new ArrayList<AppInfo>();
	private List<AppInfo> list_temp = new ArrayList<AppInfo>();
	private int type = 2;
	private ArrayList<CategoryInfo> categoryInfoList = new ArrayList<CategoryInfo>();
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
	}
	public ClassifyAdapter getcAdapter() {
		return cAdapter;
	}
	public void setcAdapter(ClassifyAdapter cAdapter) {
		this.cAdapter = cAdapter;
	}
	public List<AppInfo> getList() {
		return list;
	}
	public void setList(List<AppInfo> list) {
		this.list = list;
	}
	public ListView getClassLv() {
		return classLv;
	}
	public void setClassLv(ListView classLv) {
		this.classLv = classLv;
	}
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		classLv = (ListView) view.findViewById(R.id.clasifys_lv);
		new AsyncTask<Void, Void, Void>() {

			@Override
			protected Void doInBackground(Void... params) {
				String str = ToolHelper.donwLoadToString(Global.MAIN_URL
						+ Global.APP_CATEGORY + "?type=" + type);
				Log.e("tag", "runCategoryData result =" + str);
				ParseCategoryJson(str);
				CategoryInfo cif = categoryInfoList.get(2);
				int type1 = Integer.parseInt(cif.getType1());
				int type2 = Integer.parseInt(cif.getType2());
				String str1 = ToolHelper.donwLoadToString(Global.MAIN_URL
						+ Global.INDEX_PAGE + "?type1=" + type1 + "&type2=" + type2
						+ "&page=" + 0);
				ParseJson(str1);
				return null;
			}

			@Override
			protected void onPostExecute(Void result) {
				super.onPostExecute(result);
				LogUtils.d("Menu", list.size()+"sss");
				cAdapter = new ClassifyAdapter(getActivity().getApplicationContext(), list);
				classLv.setAdapter(cAdapter);
				classLv.setOnItemClickListener(new OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> parent, View view,
							int position, long id) {
							AppInfo mAppInfo = (AppInfo) classLv.getAdapter()
									.getItem(position);
							// Log.d("YTL", "mAppInfo.getIdx() = " + mAppInfo.getIdx());
							Intent intent = new Intent(getActivity().getApplicationContext(),
									AppDetailActivity.class);
							//LogUtils.d("error", position+"");
							intent.putExtra("appid", mAppInfo.getIdx());
							intent.putExtra("appinfo", mAppInfo);
							startActivity(intent);
					}
				});
			}
		}.execute();
		
	}
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		view = inflater.inflate(R.layout.classify_fragment, container, false);
		return view;
	}
	private void ParseCategoryJson(String str) {
		try {
			Log.e("tag", "--------------ParseCategoryJson--------");
			JSONArray jsonArray = new JSONArray(str);
			int len = jsonArray.length();
			for (int i = 0; i < len; i++) {
				JSONObject jsonObject = jsonArray.getJSONObject(i);
				String id = jsonObject.getString("id");
				String name = jsonObject.getString("name");
				String type1 = jsonObject.getString("type1");
				String type2 = jsonObject.getString("type2");
				String appUrl = jsonObject.getString("appiconurl");
				CategoryInfo mCategoryInfo = new CategoryInfo(id, name, type1,
						type2, Global.MAIN_URL + appUrl);
				categoryInfoList.add(mCategoryInfo);
			}
			Log.e("tag", "--------------ParseCategoryJson 2--------");
			/*
			 * categoryDataHandler
			 * .sendEmptyMessage(Global.DOWN_DATA_HOME_SUCCESSFULL);
			 */
		} catch (Exception ex) {
			Log.e("tag", "ParseBannerJson error = " + ex.getMessage());
		}
	}
	private void ParseJson(String str) {
		try {
			list_temp.clear();
			JSONArray jsonArray = new JSONArray(str);
			int len = jsonArray.length();
			for (int i = 0; i < len; i++) {
				JSONObject jsonObject = jsonArray.getJSONObject(i);
				String appName = jsonObject.getString("appname");
				String appiconurl = jsonObject.getString("appiconurl");
				String appSize = jsonObject.getString("appsize");
				String idx = jsonObject.getString("idx");
				String appurl = jsonObject.getString("appurl");
				AppInfo appInfo = new AppInfo(idx, appName, appSize,
						Global.MAIN_URL + appiconurl, appurl, "","",appName);
				
				appInfo.setInstalled(AppUtils.isInstalled(appName));
				list_temp.add(appInfo);
				//appDatainfos_temp.add(appInfo);
			}
			list.clear();
			list.addAll(list_temp);
			//mHandler.sendEmptyMessage(Global.DOWN_DATA_SUCCESSFULL);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public void updata(final int type) {
		new AsyncTask<Void, Void, Void>() {

			@Override
			protected Void doInBackground(Void... params) {
				String str = ToolHelper.donwLoadToString(Global.MAIN_URL
						+ Global.APP_CATEGORY + "?type=" + type);
				Log.e("tag", "runCategoryData result =" + str);
				ParseCategoryJson(str);
				CategoryInfo cif = categoryInfoList.get(type);
				int type1 = Integer.parseInt(cif.getType1());
				int type2 = Integer.parseInt(cif.getType2());
				String str1 = ToolHelper.donwLoadToString(Global.MAIN_URL
						+ Global.INDEX_PAGE + "?type1=" + type1 + "&type2=" + type2
						+ "&page=" + 0);
				ParseJson(str1);
				return null;
			}

			@Override
			protected void onPostExecute(Void result) {
				super.onPostExecute(result);
				LogUtils.d("Menu", list.size()+"sss");
				cAdapter = new ClassifyAdapter(getActivity().getApplicationContext(), list);
				classLv.setAdapter(cAdapter);
				classLv.setOnItemClickListener(new OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> parent, View view,
							int position, long id) {
							AppInfo mAppInfo = (AppInfo) classLv.getAdapter()
									.getItem(position);
							// Log.d("YTL", "mAppInfo.getIdx() = " + mAppInfo.getIdx());
							Intent intent = new Intent(getActivity().getApplicationContext(),
									AppDetailActivity.class);
							//LogUtils.d("error", position+"");
							intent.putExtra("appid", mAppInfo.getIdx());
							intent.putExtra("appinfo", mAppInfo);
							startActivity(intent);
					}
				});
				cAdapter.notifyDataSetChanged();
			}
		}.execute();
	}
	
}
