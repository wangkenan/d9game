package me.key.appmarket;

import java.util.ArrayList;
import java.util.List;

import me.key.appmarket.adapter.DetaileAdapter;
import me.key.appmarket.adapter.LocalDetailAdapter;
import me.key.appmarket.adapter.MyAdapter;
import me.key.appmarket.utils.AppInfo;
import me.key.appmarket.utils.CategoryInfo;
import me.key.appmarket.utils.LocalUtils;
import me.key.appmarket.utils.LogUtils;

import com.market.d9game.R;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
/**
 * 本地游戏界面
 * @author Administrator
 *
 */
public class LocalGameActivity extends Activity {
	private ListView mListGame;
	private LinearLayout gameLinearLayout;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.locat_applist);
		mListGame = (ListView) this.findViewById(R.id.list_app_game);
		LayoutInflater inflater = LayoutInflater.from(this);
		ImageButton search_btn = (ImageButton) findViewById(R.id.search_btn);
		search_btn = (ImageButton) findViewById(R.id.search_btn);
		search_btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(LocalGameActivity.this,
						SearchActivity.class);
				startActivity(intent);
			}
		});
		
		/*
		 * game_calss.setOnClickListener(new OnClickListener() {
		 * 
		 * @Override public void onClick(View v) { Intent intent = new
		 * Intent(MainActivity.this, DetaileActivity.class);
		 * intent.putExtra("type", 2); startActivity(intent); } });
		 * game_boutique.setOnClickListener(new OnClickListener() {
		 * 
		 * @Override public void onClick(View v) { Intent intent = new
		 * Intent(MainActivity.this, RecoTagsActivity.class);
		 * startActivity(intent); } });
		 */
		String Root = LocalUtils.getRoot(this);

		/*
		 * LocalCategoryAdapter mCategoryAdapter = new LocalCategoryAdapter(
		 * categoryInfoList, MainActivity.this, cache);
		 */
		List<AppInfo> mAppInfos = LocalUtils.InitHomePager("0", this, Root);
		LogUtils.d("mAppInfos", mAppInfos.size() + ""); 
		ArrayList<CategoryInfo> categoryInfo = new ArrayList<CategoryInfo>();
		categoryInfo.add(new CategoryInfo("0", "休闲益智", null, null, null));
		categoryInfo.add(new CategoryInfo("1", "角色冒险", null, null, null));
		categoryInfo.add(new CategoryInfo("2", "动作格斗", null, null, null));
		categoryInfo.add(new CategoryInfo("3", "策略游戏", null, null, null));
		categoryInfo.add(new CategoryInfo("4", "飞行射击", null, null, null));
		categoryInfo.add(new CategoryInfo("5", "体育竞技", null, null, null));
		categoryInfo.add(new CategoryInfo("6", "卡牌棋牌", null, null, null));
		categoryInfo.add(new CategoryInfo("7", "经营养成", null, null, null));
		categoryInfo.add(new CategoryInfo("8", "其他游戏", null, null, null));
		LocalDetailAdapter mCategoryAdapter = new LocalDetailAdapter(categoryInfo, this, mListGame);
		mListGame.setAdapter(mCategoryAdapter);

	}
}
