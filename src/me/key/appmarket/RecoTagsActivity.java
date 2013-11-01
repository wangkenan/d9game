package me.key.appmarket;

import java.util.ArrayList;
import java.util.List;

import me.key.appmarket.tool.ToolHelper;
import me.key.appmarket.utils.Global;
import me.key.appmarket.utils.RecoTagInfo;
import me.key.appmarket.utils.RecoTags;
import me.key.appmarket.widgets.FlowLayout;

import org.json.JSONArray;
import org.json.JSONObject;

import com.market.d9game.R;
import com.umeng.analytics.MobclickAgent;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * 精选标签Activity
 */
public class RecoTagsActivity extends Activity {
	LinearLayout recotags_all;
	private LayoutInflater lay;

	private static final int TEXTSIZE = 18;
	private ImageView back_icon;
	private ImageView logo_title;
	private TextView reotag_title;

	private boolean isApp;
	private int type;

	private List<RecoTags> tagList = new ArrayList<RecoTags>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		lay = LayoutInflater.from(this);
		MarketApplication.getInstance().getAppLication().add(this);
		this.setContentView(R.layout.recotags_layout);

		recotags_all = (LinearLayout) findViewById(R.id.recotags_all);

		reotag_title = (TextView) findViewById(R.id.reotag_title);

		isApp = getIntent().getBooleanExtra("isApp", false);
		if (isApp) {
			reotag_title.setText("应用标签");
			type = 1;
		} else {
			reotag_title.setText("游戏标签");
			type = 2;
		}

		back_icon = (ImageView) findViewById(R.id.back_icon);
		logo_title = (ImageView) findViewById(R.id.logo_title);
		logo_title.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				RecoTagsActivity.this.finish();
			}
		});
		back_icon.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				RecoTagsActivity.this.finish();
			}
		});

		new Thread(runRecoTagData).start();

	}

	private void reflashView() {
		for (int j = 0; j < tagList.size(); j++) {
			RecoTags mRecoTags = tagList.get(j);

			View recotagsTitle = lay.inflate(R.layout.recotags_title, null);
			TextView recotagsText = (TextView) recotagsTitle
					.findViewById(R.id.recotags_textview);
			recotagsText.setText(mRecoTags.getType_name());
			recotags_all.addView(recotagsTitle);
			FlowLayout mFlowLayout = new FlowLayout(this);
			mFlowLayout.setPadding(10, 10, 10, 10);
			for (int i = 0; i < mRecoTags.getTagLists().size(); i++) {
				final Button mButton = new Button(this);
				mButton.setBackgroundResource(R.drawable.recotag_bg_selector);
				mButton.setTextSize(TEXTSIZE);
				mButton.setText(mRecoTags.getTagLists().get(i).getText_name());
				mButton.setPadding(20, 0, 20, 0);
				mButton.setTag(mRecoTags.getTagLists().get(i).getTagid());

				mButton.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						Integer tagid = (Integer) mButton.getTag();
						String name = (String) mButton.getText();

						Intent intent = new Intent(RecoTagsActivity.this,
								IndexDetaileActivity.class);
						Bundle bundle = new Bundle();
						bundle.putBoolean("isRecoTag", true);
						bundle.putString("name", name);
						bundle.putInt("tagid", tagid);
						intent.putExtra("value", bundle);
						startActivity(intent);
					}
				});

				mFlowLayout.addView(mButton);
			}
			recotags_all.addView(mFlowLayout);
		}
	}

	Runnable runRecoTagData = new Runnable() {
		@Override
		public void run() {
			String str = ToolHelper.donwLoadToString(Global.MAIN_URL
					+ Global.RECOTAGS + "?type=" + type);
			if (str.equals("null")) {
			} else if (str.equals("-1")) {
			} else {
				ParseRecoJson(str);
			}
		}
	};

	private void ParseRecoJson(String str) {
		try {
			JSONArray jsonArray = new JSONArray(str);
			int len = jsonArray.length();
			for (int i = 0; i < len; i++) {
				List<RecoTagInfo> tagLists = new ArrayList<RecoTagInfo>();

				JSONObject jsonObject = jsonArray.getJSONObject(i);
				String tagname = jsonObject.getString("tagname");
				JSONArray jsonArrayList = jsonObject.getJSONArray("list");

				for (int j = 0; j < jsonArrayList.length(); j++) {
					JSONObject tagObject = jsonArrayList.getJSONObject(j);

					String tagnameObj = tagObject.getString("tagname");
					Integer tagidObj = tagObject.getInt("tagid");
					String tagtypeObj = tagObject.getString("tagtype");
					RecoTagInfo mRecoTagInfo = new RecoTagInfo(tagnameObj,
							tagidObj, tagtypeObj);

					tagLists.add(mRecoTagInfo);
				}

				RecoTags mRecoTags = new RecoTags(tagname, tagLists);

				tagList.add(mRecoTags);
			}
			recoTagHandler.sendEmptyMessage(Global.DOWN_DATA_RANK_SUCCESSFUL);
		} catch (Exception ex) {
			Log.e("tag", "error = " + ex.getMessage());
			recoTagHandler.sendEmptyMessage(Global.DOWN_DATA_RANK_FAILLY);
		}
	}

	Handler recoTagHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case Global.DOWN_DATA_RANK_FAILLY:
				break;
			case Global.DOWN_DATA_SEARCH_EMPTY:
				break;
			case Global.DOWN_DATA_RANK_SUCCESSFUL:
				reflashView();
			default:
				break;
			}
			super.handleMessage(msg);
		}
	};
	protected void onResume() {
		super.onResume();
		MobclickAgent.onResume(this);
	};
	
	@Override
	protected void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	}
}
