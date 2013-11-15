package me.key.appmarket.adapter;

import java.util.ArrayList;
import java.util.List;

import me.key.appmarket.tool.ToolHelper;
import me.key.appmarket.utils.AppInfo;
import me.key.appmarket.utils.LocalUtils;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.market.d9game.R;

/**
 * 一键安装填充器
 * 
 * @author Administrator
 * 
 */
public class OnKeyAdapter extends BaseAdapter {
	private LayoutInflater mInflater;
	private List<AppInfo> mData;
	private List<AppInfo> checkApp;
	private Context context;
	private String ROOT;
	public ArrayList<Boolean> checkState;

	public OnKeyAdapter(Context context, List<AppInfo> mData,
			List<AppInfo> checkApp) {
		this.context = context;
		this.mData = mData;
		this.checkApp = checkApp;
		this.mInflater = LayoutInflater.from(context);
		ROOT = LocalUtils.getRoot(context);
		checkState = new ArrayList<Boolean>();
		for (int i = 0;i<mData.size();i++) {
			AppInfo mAppInfo = mData.get(i);
			checkState.add(false);
			for (AppInfo check : checkApp) {
				if (check.getPackageName().equals(mAppInfo.getPackageName())) {
					checkState.set(i, true);
				}
				
			}
		}

	}

	@Override
	public int getCount() {
		return mData.size();
	}

	@Override
	public Object getItem(int position) {
		return mData.get(position);
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		final AppInfo sdappInfo;
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = mInflater.inflate(R.layout.item_onkey, null);
			holder.info = (TextView) convertView.findViewById(R.id.app_name2);
			holder.icon = (ImageView) convertView.findViewById(R.id.icon2);
			holder.appsize = (TextView) convertView.findViewById(R.id.appsize2);
			holder.ck_onkey_item = (CheckBox) convertView
					.findViewById(R.id.ck_onkey_item);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		sdappInfo = mData.get(position);
		/*
		 * Drawable mDrawable2 = cnt.getResources().getDrawable(
		 * R.drawable.downloaded);
		 * v2.btn.setCompoundDrawablesWithIntrinsicBounds(null, mDrawable2,
		 * null, null);
		 */
	/*	holder.ck_onkey_item
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						if (isChecked) {
							checkApp.add(sdappInfo);
							checkState.set(position, true);

						} else {
							checkApp.remove(sdappInfo);
							checkState.set(position, false);
						}
					}
				});*/
		String mb = ToolHelper.Kb2Mb(sdappInfo.getAppSize());
		holder.appsize.setText(mb);
		// v2.progress_view.setVisibility(View.VISIBLE);
		holder.info.setText(sdappInfo.getAppName());
		holder.icon.setVisibility(View.VISIBLE);
		holder.icon.setImageDrawable(sdappInfo.getAppIcon());
		holder.ck_onkey_item.setChecked(checkState.get(position));
		return convertView;
	}

	static class ViewHolder {
		public TextView appsize;
		public ImageView icon;
		public TextView info;
		public CheckBox ck_onkey_item;
	}

}
