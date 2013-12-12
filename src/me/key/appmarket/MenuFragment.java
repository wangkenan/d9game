package me.key.appmarket;

import java.util.ArrayList;
import java.util.List;

import me.key.appmarket.adapter.ClassifyAdapter;
import me.key.appmarket.tool.DownloadService;
import me.key.appmarket.tool.ToolHelper;
import me.key.appmarket.utils.AppInfo;
import me.key.appmarket.utils.AppUtils;
import me.key.appmarket.utils.CategoryInfo;
import me.key.appmarket.utils.Global;
import me.key.appmarket.utils.LogUtils;
import me.key.appmarket.utils.MyAsynTask;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.market.d9game.R;

//分类
public class MenuFragment extends Fragment implements OnClickListener {
	private View view;
	private ClassifyAdapter cAdapter;
	private ListView classLv;
	private List<AppInfo> list;
	private List<AppInfo> list_temp;
	private int type = 2;
	private AsyncTask<Void, Void, Void> at;
	private ArrayList<CategoryInfo> categoryInfoList = new ArrayList<CategoryInfo>();
	private Context context;
	private View errorview;
	private LayoutInflater inflater;
	private View loadMoreView;
	private int visibleLast = 0;
	private int visibleCount;
	private Button loadmore_btn;
	private Handler handler = new Handler();
	// 每次获取的数量
	private int page = 0;
	private boolean isLoading = false;
	private boolean isFirst = false;
	//刷新
	private View btnRefsh;
	private ProgressBar progressBar;

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
		context = getActivity().getApplicationContext();
		classLv = (ListView) view.findViewById(R.id.clasifys_lv);
		progressBar=(ProgressBar) view.findViewById(R.id.pb_classify_fragment);
		classLv.setDivider(null);
		classLv.setDividerHeight(0);
		inflater = LayoutInflater.from(context);
		loadMoreView = inflater.inflate(R.layout.loadmore, null);
		classLv.addFooterView(loadMoreView);
		loadmore_btn = (Button) loadMoreView.findViewById(R.id.loadMoreButton);
		MarketApplication.getInstance().getAppLication().add(getActivity());
		errorview = view.findViewById(R.id.error);
		btnRefsh = errorview.findViewById(R.id.btn_Refsh);
		btnRefsh.setOnClickListener(this);
		new MyAsynTask(context,errorview) {

			@Override
			protected Void doInBackground(Void... params) {
				String str = ToolHelper.donwLoadToString(Global.MAIN_URL
						+ Global.APP_CATEGORY + "?type=" + 2);
				LogUtils.d("Local", str+"menu");
				if (str.isEmpty()) {
					errorview.setVisibility(View.VISIBLE);
					list_temp = new ArrayList<AppInfo>();
				} else {
					ParseCategoryJson(str);
					if (categoryInfoList.size() > 0) {
						CategoryInfo cif = categoryInfoList.get(0);
						int type1 = Integer.parseInt(cif.getType1());
						int type2 = Integer.parseInt(cif.getType2());
						String str1 = ToolHelper
								.donwLoadToString(Global.MAIN_URL
										+ Global.INDEX_PAGE + "?type1=" + type1
										+ "&type2=" + type2 + "&page=" + 0);
						ParseJson(str1);
					}
				}
				return null;
			}

			@Override
			protected void onPostExecute(Void result) {
				super.onPostExecute(result);
				list = new ArrayList<AppInfo>();
				list.addAll(list_temp);
				cAdapter = new ClassifyAdapter(getActivity()
						.getApplicationContext(), list, classLv,
						categoryInfoList);
				classLv.setAdapter(cAdapter);
				LogUtils.d("Local", "listcategoryInfoList"+categoryInfoList.size()+list.size());
				classLv.setOnItemClickListener(new OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> parent, View view,
							int position, long id) {
						AppInfo mAppInfo = (AppInfo) classLv.getAdapter()
								.getItem(position);
						// Log.d("YTL", "mAppInfo.getIdx() = " +
						// mAppInfo.getIdx());
						Intent intent = new Intent(getActivity()
								.getApplicationContext(),
								AppDetailActivity.class);
						// LogUtils.d("error", position+"");
						intent.putExtra("appid", mAppInfo.getIdx());
						intent.putExtra("appinfo", mAppInfo);
						startActivity(intent);
					}
				});
				classLv.setOnScrollListener(new OnScrollListener() {

					@Override
					public void onScrollStateChanged(AbsListView view,
							int scrollState) {
						// TODO Auto-generated method stub
					}

					@Override
					public void onScroll(AbsListView view,
							int firstVisibleItem, int visibleItemCount,
							int totalItemCount) {
						// TODO Auto-generated method stub
						visibleCount = visibleItemCount;
						visibleLast = firstVisibleItem + visibleItemCount - 1;
						if ((firstVisibleItem + visibleItemCount == totalItemCount)
								&& (totalItemCount != 0)) {
							if (!isLoading && !isFirst) {
								isLoading = true;
								loadmore_btn.setText("正在加载中...");
								loadmore_btn.setVisibility(View.VISIBLE);
								page = page + 1;
								loadData();
							}
							isFirst = false;
						}
					}
				});
			}
		}.exe();

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		view = inflater.inflate(R.layout.classify_fragment, container, false);
		return view;
	}

	private void ParseCategoryJson(String str) {
		try {
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
			/*
			 * categoryDataHandler
			 * .sendEmptyMessage(Global.DOWN_DATA_HOME_SUCCESSFULL);
			 */
		} catch (Exception ex) {
		}
	}

	private void ParseJson(String str) {
		try {
			list_temp = new ArrayList<AppInfo>();
			JSONArray jsonArray = new JSONArray(str);
			int len = jsonArray.length();
			LogUtils.d("Local", len+"len");
			for (int i = 0; i < len; i++) {
				JSONObject jsonObject = jsonArray.getJSONObject(i);
				String appName = jsonObject.getString("appname");
				String apppkgname = jsonObject.getString("apppkgname");
				String appiconurl = jsonObject.getString("appiconurl");
				String appSize = jsonObject.getString("appsize");
				String idx = jsonObject.getString("idx");
				String appurl = jsonObject.getString("appurl");
				AppInfo appInfo = new AppInfo(idx, appName, appSize,
						Global.MAIN_URL + appiconurl, appurl, "", "",
						apppkgname);
				appInfo.setInstalled(AppUtils.isInstalled(apppkgname));
				list_temp.add(appInfo);
				
				// appDatainfos_temp.add(appInfo);
			}
		
			// mHandler.sendEmptyMessage(Global.DOWN_DATA_SUCCESSFULL);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void updata(final int type) {
		page = 0;
		if (at != null) {
			at.cancel(true);
		}
		at = new AsyncTask<Void, Void, Void>() {
			
			protected void onPreExecute() {
				progressBar.setVisibility(View.VISIBLE);
			};

			@Override
			protected Void doInBackground(Void... params) {
				String str = ToolHelper.donwLoadToString(Global.MAIN_URL
						+ Global.APP_CATEGORY + "?type=" + type);
				ParseCategoryJson(str);
				CategoryInfo cif = categoryInfoList.get(type);
				int type1 = Integer.parseInt(cif.getType1());
				int type2 = Integer.parseInt(cif.getType2());
				String str1 = ToolHelper.donwLoadToString(Global.MAIN_URL
						+ Global.INDEX_PAGE + "?type1=" + type1 + "&type2="
						+ type2 + "&page=" + page);
				ParseJson(str1);
				return null;
			}

			@Override
			protected void onPostExecute(Void result) {
				super.onPostExecute(result);
				list = new ArrayList<AppInfo>();
				list.addAll(list_temp);
				LogUtils.d("Local", "list.size"+list.size());
				LogUtils.d("Menu", list.size() + "sss");
				progressBar.setVisibility(View.GONE);
				errorview.setVisibility(View.INVISIBLE);
				cAdapter = new ClassifyAdapter(context, list, classLv,
						categoryInfoList);
				classLv.setAdapter(cAdapter);
				classLv.setDivider(null);
				classLv.setDividerHeight(0);
				classLv.setOnItemClickListener(new OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> parent, View view,
							int position, long id) {
						AppInfo mAppInfo = (AppInfo) classLv.getAdapter()
								.getItem(position);
						// Log.d("YTL", "mAppInfo.getIdx() = " +
						// mAppInfo.getIdx());
						Intent intent = new Intent(getActivity()
								.getApplicationContext(),
								AppDetailActivity.class);
						// LogUtils.d("error", position+"");
						intent.putExtra("appid", mAppInfo.getIdx());
						intent.putExtra("appinfo", mAppInfo);
						startActivity(intent);
					}
				});
				cAdapter.notifyDataSetChanged();
				classLv.setOnScrollListener(new OnScrollListener() {

					@Override
					public void onScrollStateChanged(AbsListView view,
							int scrollState) {
						// TODO Auto-generated method stub
						int itemsLastIndex = cAdapter.getCount() - 1;
						int lastIndex = itemsLastIndex + 1;
						if (scrollState == OnScrollListener.SCROLL_STATE_IDLE
								&& visibleLast == lastIndex) {
						}
					}

					@Override
					public void onScroll(AbsListView view,
							int firstVisibleItem, int visibleItemCount,
							int totalItemCount) {
						// TODO Auto-generated method stub
						if ((firstVisibleItem + visibleItemCount == totalItemCount)
								&& (totalItemCount != 0)) {
							if (!isLoading && !isFirst) {
								isLoading = true;
								loadmore_btn.setText("正在加载中...");
								loadmore_btn.setVisibility(View.VISIBLE);
								page = page + 1;
								loadData();
							}
							isFirst = false;
						}
					}
				});
			}
		}.execute();
	}

	protected void doUpdate() {
		// TODO Auto-generated method stub
		loadmore_btn.setText("loading ...");
		page = cAdapter.getCount();
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				loadData();
				cAdapter.notifyDataSetChanged();// 閫氱煡adapter鏁版嵁鍙樺寲
				classLv.setSelection(visibleLast - visibleCount + 1);
				loadmore_btn.setText("Load More");
			}

		}, 2000);
	}

	/**
	 * 获取数据
	 */
	private void loadData() {
		new MyAsynTask(context, errorview) {

			@Override
			protected Void doInBackground(Void... params) {
				String str = ToolHelper.donwLoadToString(Global.MAIN_URL
						+ Global.APP_CATEGORY + "?type=" + 2);
				if (str.isEmpty()) {
					errorview.setVisibility(View.VISIBLE);
				} else {
					ParseCategoryJson(str);
					if (categoryInfoList.size() > 0) {
						CategoryInfo cif = categoryInfoList.get(2);
						int type1 = Integer.parseInt(cif.getType1());
						int type2 = Integer.parseInt(cif.getType2());
						String str1 = ToolHelper
								.donwLoadToString(Global.MAIN_URL
										+ Global.INDEX_PAGE + "?type1=" + type1
										+ "&type2=" + type2 + "&page=" + page);
						ParseJsonUpdata(str1);
					}
				}

				return null;
			}

			private void ParseJsonUpdata(String str1) {
				try {
					list_temp = new ArrayList<AppInfo>();
					JSONArray jsonArray = new JSONArray(str1);
					int len = jsonArray.length();
					for (int i = 0; i < len; i++) {
						JSONObject jsonObject = jsonArray.getJSONObject(i);
						String appName = jsonObject.getString("appname");
						String apppkgname = jsonObject.getString("apppkgname");
						String appiconurl = jsonObject.getString("appiconurl");
						String appSize = jsonObject.getString("appsize");
						String idx = jsonObject.getString("idx");
						String appurl = jsonObject.getString("appurl");
						AppInfo appInfo = new AppInfo(idx, appName, appSize,
								Global.MAIN_URL + appiconurl, appurl, "", "",
								apppkgname);
						appInfo.setInstalled(AppUtils.isInstalled(apppkgname));
						list_temp.add(appInfo);
						// appDatainfos_temp.add(appInfo);
					}
					// mHandler.sendEmptyMessage(Global.DOWN_DATA_SUCCESSFULL);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}

			@Override
			protected void onPostExecute(Void result) {
				super.onPostExecute(result);
				loadmore_btn.setVisibility(View.GONE);
				list.addAll(list_temp);
				cAdapter.notifyDataSetChanged();
				isLoading = false;
				/*
				 * classLv.setOnItemClickListener(new OnItemClickListener() {
				 * 
				 * @Override public void onItemClick(AdapterView<?> parent, View
				 * view, int position, long id) { AppInfo mAppInfo = (AppInfo)
				 * classLv.getAdapter() .getItem(position); // Log.d("YTL",
				 * "mAppInfo.getIdx() = " + // mAppInfo.getIdx()); Intent intent
				 * = new Intent(getActivity() .getApplicationContext(),
				 * AppDetailActivity.class); // LogUtils.d("error",
				 * position+""); intent.putExtra("appid", mAppInfo.getIdx());
				 * intent.putExtra("appinfo", mAppInfo); startActivity(intent);
				 * } });
				 */
			}
		}.exe();
	}

	@Override
	public void onClick(View v) {
		LogUtils.d("Local", "我刷新了阿");
		switch (v.getId()) {
		case R.id.btn_Refsh:
			updata(2);
		}
	}
}
