package me.key.appmarket.adapter;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import com.market.d9game.R;

import me.key.appmarket.tool.ToolHelper;
import me.key.appmarket.utils.CommentInfo;
import me.key.appmarket.utils.HotSearchInfo;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class HotSearchAdapter extends BaseAdapter {

	private ArrayList<HotSearchInfo> mHotSearchInfos;
	private LayoutInflater lay;
	private File cache;
	private Context mContext;

	public HotSearchAdapter(ArrayList<HotSearchInfo> mHotSearchInfo,
			Context context, File cache) {
		super();
		this.mHotSearchInfos = mHotSearchInfo;
		this.cache = cache;
		mContext = context;
		lay = LayoutInflater.from(context);
	}

	@Override
	public int getCount() {
		return mHotSearchInfos.size();
	}

	@Override
	public Object getItem(int arg0) {
		return mHotSearchInfos.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		return arg0;
	}

	@Override
	public View getView(final int position, View convertvView, ViewGroup arg2) {
		// TODO Auto-generated method stub
		ViewHolder viewHolder;
		if (convertvView == null) {
			viewHolder = new ViewHolder();
			convertvView = lay.inflate(R.layout.search_hot_item, null);
			viewHolder.name = (TextView) convertvView
					.findViewById(R.id.tv_name);
			convertvView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertvView.getTag();
		}

		HotSearchInfo mHotSearchInfo = mHotSearchInfos.get(position);
		viewHolder.name.setText(mHotSearchInfo.getWord());

		return convertvView;
	}

	private class ViewHolder {
		private TextView name;
	}

	public void addNewsItem(HotSearchInfo newsitem) {
		mHotSearchInfos.add(newsitem);
	}
}
