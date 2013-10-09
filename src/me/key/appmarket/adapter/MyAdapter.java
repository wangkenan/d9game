package me.key.appmarket.adapter;

import java.io.File;
import java.util.List;

import me.key.appmarket.AppDetailActivity;
import me.key.appmarket.tool.ToolHelper;
import me.key.appmarket.utils.AppInfo;
import me.key.appmarket.utils.AppUtils;
import me.key.appmarket.utils.LogUtils;
import me.key.appmarket.widgets.ProgressView;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.market.d9game.R;

/**
 * ListVeiw的适配器类
 * 
 * @author yutinglong
 */
public class MyAdapter extends BaseAdapter {
	private LayoutInflater mInflater;
	private List<AppInfo> mData;
	private Context cnt;
	private String ROOT;
	private int width;
	private int height;
	private int gapPy;
	private int bigImHeight;
	private int gapPx;

	public MyAdapter(Context context, List<AppInfo> mData) {
		this.mInflater = LayoutInflater.from(context);
		this.cnt = context;
		this.mData = mData;
		cnt = context;
		DisplayMetrics dm = new DisplayMetrics();
		((Activity) cnt).getWindowManager().getDefaultDisplay().getMetrics(dm);
		width = dm.widthPixels;
		height = dm.heightPixels;
		gapPx = convertDipOrPx(cnt, 5);
		gapPy = convertDipOrPx(cnt, 10);
		bigImHeight = (int) ((width - gapPy) / 2 / 1.27f);
	}

	@Override
	public int getCount() {
		return mData.size()+1;
	}

	@Override
	public Object getItem(int arg0) {
		return null;
	}

	@Override
	public long getItemId(int arg0) {
		return 0;
	}

	public int getItemViewType(int position) {

		if (position == 0) {
			return 0;
		} else {
			return 1;
		}
	}

	@Override
	public int getViewTypeCount() {
		return 2;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		/*
		 * View view = mInflater.inflate(R.layout.item_banner_loacl, null);
		 * ImageView banner = (ImageView) view.findViewById(R.id.banner);
		 * setImagePosition(R.drawable.a20131008174300, banner);
		 * banner.setOnClickListener(new OnClickListener() {
		 * 
		 * @Override public void onClick(View v) { Intent intent = new
		 * Intent(cnt, AppDetailActivity.class); intent.putExtra("appid",
		 * 15603+""); cnt.startActivity(intent); }
		 * 
		 * } );
		 */
		final int newposition = position - 1;
		final ViewHolder holder;
		final ViewHolder1 viewHolder1;
		int type = getItemViewType(position);

		if (convertView == null) {
			switch (type) {
			case 1:
				holder = new ViewHolder();

				convertView = mInflater.inflate(R.layout.list_item, null);
				holder.info = (TextView) convertView.findViewById(R.id.info);
				holder.icon = (ImageView) convertView.findViewById(R.id.icon);
				holder.btn = (TextView) convertView.findViewById(R.id.install);
				holder.progress_view = (ProgressView) convertView
						.findViewById(R.id.progress_view_local);

				holder.appsize = (TextView) convertView
						.findViewById(R.id.appsize);
				convertView.setTag(holder);
				break;
			case 0:
				viewHolder1 = new ViewHolder1();
				convertView = mInflater.inflate(R.layout.item_banner_loacl,
						null);
				viewHolder1.ib1 = (ImageView) convertView
						.findViewById(R.id.banner);
				convertView.setTag(viewHolder1);
				break;

			}
		} else {
			switch (type) {
			case 0:
				viewHolder1 = (ViewHolder1) convertView.getTag();
				break;

			case 1:
				holder = (ViewHolder) convertView.getTag();
				break;
			}
		}
		switch (type) {
		case 0:
			final ViewHolder1 v1 = ((ViewHolder1) convertView.getTag());
			  setImagePosition(R.drawable.a20131008174300, v1.ib1);
			  v1.ib1.setOnClickListener(new OnClickListener() {
			 
			  @Override public void onClick(View v) { Intent intent = new
 Intent(cnt, AppDetailActivity.class); intent.putExtra("appid",
			  15603+""); cnt.startActivity(intent); }
			  
			  } );
			break;
		case 1:
			final ViewHolder v2 = ((ViewHolder) convertView.getTag());
			v2.progress_view.setProgress(0);
			v2.progress_view.setVisibility(View.VISIBLE);
			v2.info.setText(mData.get(newposition).getAppName());
			String mb = ToolHelper.Kb2Mb(mData.get(newposition).getAppSize());
			v2.appsize.setText(mb);
			v2.icon.setVisibility(View.VISIBLE);
			ROOT = mData.get(newposition).getRoot();
			v2.icon.setImageDrawable(mData.get(newposition).getAppIcon());
			if (mData.get(newposition).isInstalled()) {
				v2.btn.setText("试玩");
				v2.progress_view.setProgress(100);
				Drawable mDrawable = this.cnt.getResources().getDrawable(
						R.drawable.action_type_software_update);
				v2.btn.setCompoundDrawablesWithIntrinsicBounds(null, mDrawable,
						null, null);
			} else {
				Drawable mDrawable = cnt.getResources().getDrawable(
						R.drawable.downloaded);
				v2.btn.setCompoundDrawablesWithIntrinsicBounds(null, mDrawable,
						null, null);
				v2.btn.setText("安装");
				v2.progress_view.setProgress(0);
			}
			v2.btn.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					AppInfo mAppInfo = mData.get(newposition);
					if (mAppInfo.isInstalled()) {
						AppUtils.launchApp(cnt, mAppInfo.getAppName());
					} else {
						installApp(mAppInfo);
					}
				}

			});
			break;
		}
		return convertView;

	}

	static class ViewHolder {
		public TextView appsize;
		public TextView tvdown;
		public TextView appSize;
		public TextView btn;
		public ImageView icon;
		public TextView info;
		private ProgressView progress_view;
	}

	static class ViewHolder1 {
		public static ImageView ib1;

	}

	/**
	 * 安装某个应用
	 * 
	 * @param mAppInfo
	 */
	private void installApp(AppInfo mAppInfo) {
		String str = mAppInfo.getApkName();
		String fileName = ROOT + str;
		Intent intent = new Intent(Intent.ACTION_VIEW);
		Log.v("tag", "nano ROOT" + fileName);
		intent.setDataAndType(Uri.fromFile(new File(fileName)),
				"application/vnd.android.package-archive");
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		this.cnt.startActivity(intent);
	}

	private void setImagePosition(int resId, ImageView banner) {
		Bitmap bm = BitmapFactory.decodeResource(cnt.getResources(), resId);
		Bitmap newbitmap = Bitmap.createBitmap((width - gapPy),
				(int) ((width - gapPy) / 5.34), bm.getConfig());
		getNewBitMapPos(bm, newbitmap);
		banner.setImageBitmap(newbitmap);
	}

	private void getNewBitMapPos(Bitmap bm, Bitmap newbitmap) {
		Paint paint = new Paint();
		Canvas canvas = new Canvas(newbitmap);
		Matrix matrix = new Matrix();
		double newWidth = 1.00;
		double newHeight = 2.6;
		int gapPx = convertDipOrPx(cnt, 5);
		// matrix.setRotate(30, bm.getWidth()/2, bm.getHeight()/2);
		float scaleWidth = (float) ((width - gapPx)) / bm.getWidth();
		float scaleHeight = (float) (newbitmap.getHeight()) / bm.getHeight();
		LogUtils.d("scaleWidth+scaleWidth", scaleWidth + ":" + scaleWidth
				+ "++" + width + "PPP" + 2 / 3);
		matrix.postScale(scaleWidth, scaleHeight);
		// 使用画布将原图片，矩阵，画笔进行新图片的绘画
		canvas.drawBitmap(bm, matrix, paint);
	}

	public static int convertDipOrPx(Context context, int dip) {
		float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dip * scale + 0.5f * (dip >= 0 ? 1 : -1));
	}
}
