package me.key.appmarket.adapter;

import java.io.File;
import java.util.ArrayList;

import com.market.d9game.R;

import me.key.appmarket.tool.ToolHelper;
import me.key.appmarket.utils.BannerInfo;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.ImageView;

public class TuiJianImageAdapter extends BaseAdapter {
	int mGalleryItemBackground;
	private Context mContext;
	private ArrayList<BannerInfo> bannerList;
	private File cache;
	private LayoutInflater lay;

	public TuiJianImageAdapter(Context context,
			ArrayList<BannerInfo> bannerList, File cache) {
		mContext = context;
		this.bannerList = bannerList;
		this.cache = cache;
		lay = LayoutInflater.from(context);

		// // 获得Gallery组件的属性
		// TypedArray typedArray = obtainStyledAttributes(R.styleable.Gallery);
		// mGalleryItemBackground = typedArray.getResourceId(
		// R.styleable.Gallery_android_galleryItemBackground, 0);
	}

	// 返回图像总数
	public int getCount() {
		return Integer.MAX_VALUE;
	}

	public Object getItem(int position) {
		return position;
	}

	public long getItemId(int position) {
		return position;
	}

	// 返回具体位置的ImageView对象
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder;
		if (convertView == null) {
			viewHolder = new ViewHolder();
			convertView = lay.inflate(R.layout.banner_item, null);
			viewHolder.icon = (ImageView) convertView.findViewById(R.id.icon);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}

		LayoutParams para;
		para = viewHolder.icon.getLayoutParams();
		// para.height = 140;
		// para.width = 235;

		WindowManager wm = (WindowManager) mContext
				.getSystemService(Context.WINDOW_SERVICE);
		int width = wm.getDefaultDisplay().getWidth();
		int height = wm.getDefaultDisplay().getHeight();

		para.height = height / 7;
		para.width = width / 2 - 7;
		viewHolder.icon.setLayoutParams(para);

		if (bannerList.size() != 0) {
			int tempPos = position % bannerList.size();
			asyncloadImage(viewHolder.icon, bannerList.get(tempPos).getPicurl());
		}

		return convertView;
	}

	private class ViewHolder {
		private ImageView icon;
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
}
