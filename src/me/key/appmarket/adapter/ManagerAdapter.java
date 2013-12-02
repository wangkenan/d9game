package me.key.appmarket.adapter;

import java.io.File;
import java.util.ArrayList;

import me.key.appmarket.tool.ToolHelper;
import me.key.appmarket.utils.AppInfo;
import me.key.appmarket.utils.AppUtils;
import me.key.appmarket.widgets.ProgressView;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.market.d9game.R;

public class ManagerAdapter extends BaseAdapter {

	private ArrayList<AppInfo> appInfos;
	private LayoutInflater lay;
	private File cache;
	private Context mContext;

	public ManagerAdapter(ArrayList<AppInfo> appInfos, Context context,
			File cache) {
		super();
		this.appInfos = appInfos;
		this.cache = cache;
		mContext = context;
		lay = LayoutInflater.from(context);
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return appInfos.size();
	}

	@Override
	public Object getItem(int arg0) {
		// TODO Auto-generated method stub
		return appInfos.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		// TODO Auto-generated method stub
		return arg0;
	}

	@Override
	public View getView(final int position, View convertvView, ViewGroup arg2) {
		// TODO Auto-generated method stub
		ViewHolder viewHolder;
		if (convertvView == null) {
			viewHolder = new ViewHolder();
			convertvView = lay.inflate(R.layout.app_list_recomm_item, null);
			viewHolder.icon = (ImageView) convertvView.findViewById(R.id.icon2);
			viewHolder.name = (TextView) convertvView
					.findViewById(R.id.app_name2);
			viewHolder.size = (TextView) convertvView
					.findViewById(R.id.appsize2);
			viewHolder.tvdown = (TextView) convertvView
					.findViewById(R.id.tv_down2);
//			viewHolder.progress_view = (ProgressView) convertvView
//					.findViewById(R.id.progress_view2);
			convertvView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertvView.getTag();
		}
		// asyncloadImage(viewHolder.icon,appInfos.get(position).getIconUrl());

		if (appInfos.get(position).getAppIcon() != null) {
			viewHolder.icon.setImageDrawable(appInfos.get(position)
					.getAppIcon());
		}

		viewHolder.progress_view.setVisibility(View.GONE);

		viewHolder.name.setText(appInfos.get(position).getAppName());
		if (appInfos.get(position).getAppSize() != null
				&& !appInfos.get(position).getAppSize().equals("null")) {
			viewHolder.size.setText(ToolHelper.Kb2Mb(appInfos.get(position)
					.getAppSize()));
		}

		viewHolder.tvdown.setText("卸载");
		Drawable mDrawable = mContext.getResources().getDrawable(
				R.drawable.account_logout);
		viewHolder.tvdown.setCompoundDrawablesWithIntrinsicBounds(null,
				mDrawable, null, null);

		viewHolder.tvdown.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				AppUtils.uninstallApp(mContext, appInfos.get(position)
						.getPackageName());
			}
		});
		return convertvView;
	}

	private class ViewHolder {
		private ImageView icon;
		private TextView name;
		private TextView size;
		private TextView tvdown;
		private ProgressView progress_view;
	}

	private void asyncloadImage(ImageView iv_header, String path) {
		AsyncImageTask task = new AsyncImageTask(iv_header);
		task.execute(path);
	}

	private final class AsyncImageTask extends AsyncTask<String, Integer, Uri> {

		private ImageView iv_header;

		public AsyncImageTask(ImageView iv_header) {
			this.iv_header = iv_header;
		}

		@Override
		protected Uri doInBackground(String... params) {
			try {
				return ToolHelper.getImageURI(params[0], cache);
			} catch (Exception e) {
				return null;
			}
		}

		@Override
		protected void onPostExecute(Uri result) {
			super.onPostExecute(result);
			if (iv_header != null && result != null) {
				iv_header.setImageURI(result);
			}
		}
	}

	/**
	 * @param newsitem
	 */
	public void addNewsItem(AppInfo newsitem) {
		appInfos.add(newsitem);
	}
}
