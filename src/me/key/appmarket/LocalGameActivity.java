package me.key.appmarket;

import java.util.List;

import me.key.appmarket.adapter.MyAdapter;
import me.key.appmarket.utils.AppInfo;
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
		setContentView(R.layout.applist);
		mListGame = (ListView) this.findViewById(R.id.list_app_game);
		LayoutInflater inflater = LayoutInflater.from(this);
		
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
		MyAdapter adapter = new MyAdapter(this, mAppInfos);
		mListGame.setAdapter(adapter);
		new AsyncTask<Void, Void, Void>(){

			@Override
			protected Void doInBackground(Void... params) {
				// TODO Auto-generated method stub
				return null;
			}
			
		};

	}
}
