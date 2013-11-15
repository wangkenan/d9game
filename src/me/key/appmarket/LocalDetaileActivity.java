package me.key.appmarket;

import java.io.File;
import java.util.ArrayList;

import me.key.appmarket.adapter.CategoryAdapter;
import me.key.appmarket.tool.ToolHelper;
import me.key.appmarket.utils.CategoryInfo;
import me.key.appmarket.utils.Global;

import org.json.JSONArray;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.market.d9game.R;
import com.umeng.analytics.MobclickAgent;

@SuppressLint("HandlerLeak")
/**
 * 分类Activity
 * test
 */
public class LocalDetaileActivity extends Activity {
	private ArrayList<CategoryInfo> categoryInfoList = new ArrayList<CategoryInfo>();
	private ArrayList<CategoryInfo> categoryInfoList_temp = new ArrayList<CategoryInfo>();
	private CategoryAdapter mCategoryAdapter;

	File cache;

	private ListView mListGame;

	private TextView topbar_title1;
	private ImageView back_icon;
	private ImageView logo_title;

	private int type = 1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.locat_applist);

		cache = new File(Environment.getExternalStorageDirectory(), "cache");
		if (!cache.exists()) {
			cache.mkdirs();
		}

		type = getIntent().getIntExtra("type", 1);
		MarketApplication.getInstance().getAppLication().add(this);
		topbar_title1 = (TextView) findViewById(R.id.topbar_title1);
		back_icon = (ImageView) findViewById(R.id.back_icon);
		logo_title = (ImageView) findViewById(R.id.logo_title);
		logo_title.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				LocalDetaileActivity.this.finish();
			}
		});
		back_icon.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				LocalDetaileActivity.this.finish();
			}
		});

		initCategory();

		// 请求网络获取数据
		new Thread(runCategoryData).start();
	}

	private void initCategory() {
		if (type == 1) {
			logo_title.setImageResource(R.drawable.category_game);
			topbar_title1.setText("应用分类");
		} else {
			logo_title.setImageResource(R.drawable.category_app);
			topbar_title1.setText("游戏分类");
		}

		mListGame = (ListView) this.findViewById(R.id.list_app_game);

		mCategoryAdapter = new CategoryAdapter(categoryInfoList,
				LocalDetaileActivity.this, cache);
		mListGame.setAdapter(mCategoryAdapter);

		mListGame.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				if (!netIsAvail(LocalDetaileActivity.this)) {
					Toast.makeText(LocalDetaileActivity.this, "请检查网络设置",
							Toast.LENGTH_SHORT).show();
					return;
				}

				CategoryInfo mCategoryInfo = categoryInfoList.get(arg2);
				if (mCategoryInfo != null) {
					Intent intent = new Intent(LocalDetaileActivity.this,
							IndexDetaileActivity.class);
					Bundle bundle = new Bundle();
					bundle.putString("name", mCategoryInfo.getName());
					bundle.putInt("type1",
							Integer.parseInt(mCategoryInfo.getType1()));
					bundle.putInt("type2",
							Integer.parseInt(mCategoryInfo.getType2()));
					intent.putExtra("value", bundle);
					startActivity(intent);
				}
			}
		});
	}

	public static boolean netIsAvail(Context context) {
		ConnectivityManager manager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (manager == null) {
			return false;
		}
		NetworkInfo networkinfo = manager.getActiveNetworkInfo();
		if (networkinfo == null || !networkinfo.isAvailable()) {
			return false;
		}
		return true;
	}

	Runnable runCategoryData = new Runnable() {
		@Override
		public void run() {
			String str = ToolHelper.donwLoadToString(Global.MAIN_URL
					+ Global.APP_CATEGORY + "?type=" + type);
			Log.e("tag", "runCategoryData result =" + str);
			if (str.equals("null")) {
				categoryDataHandler
						.sendEmptyMessage(Global.DOWN_DATA_HOME_SUCCESSFULL);
			} else if (str.equals("-1")) {
				categoryDataHandler
						.sendEmptyMessage(Global.DOWN_DATA_HOME_FAILLY);
			} else {
				Log.e("tag", "--------------1-------------");
				ParseCategoryJson(str);
			}
		}
	};

	Handler categoryDataHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case Global.DOWN_DATA_HOME_FAILLY: {
			}
				break;
			case Global.DOWN_DATA_HOME_SUCCESSFULL: {
				if (categoryInfoList_temp.size() > 0) {
					categoryInfoList.addAll(categoryInfoList_temp);
					categoryInfoList_temp.clear();
				}
				mCategoryAdapter.notifyDataSetChanged();
			}
			default:
				break;
			}
			super.handleMessage(msg);
		}
	};

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
				categoryInfoList_temp.add(mCategoryInfo);
			}
			Log.e("tag", "--------------ParseCategoryJson 2--------");
			categoryDataHandler
					.sendEmptyMessage(Global.DOWN_DATA_HOME_SUCCESSFULL);
		} catch (Exception ex) {
			Log.e("tag", "ParseBannerJson error = " + ex.getMessage());
		}
	}
	@Override
	protected void onResume() {
		super.onResume();
		MobclickAgent.onResume(this);
	}
	@Override
	protected void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	}
}
