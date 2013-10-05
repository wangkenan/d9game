package me.key.appmarket.adapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.key.appmarket.IndexDetaileActivity;
import me.key.appmarket.LocalIndexDetaileActivity;
import me.key.appmarket.ImageNet.AsyncImageLoader;
import me.key.appmarket.utils.AppInfo;
import me.key.appmarket.utils.CategoryInfo;
import me.key.appmarket.utils.LocalUtils;
import me.key.appmarket.utils.LogUtils;
import me.key.appmarket.widgets.MyImageView;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.market.d9game.R;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

/**
 * 分类信息adapter
 * 
 * @author Administrator
 * 
 */
public class LocalDetailAdapter extends BaseAdapter {
	private ArrayList<CategoryInfo> categoryInfo;
	private Context mContext;
	AsyncImageLoader asyncImageLoader;
	private ListView mylistView;
	// 是否暂停
	private boolean isPause;
	// 是否是下载状态
	private boolean isDownLoading;
	// 是否异步加载图片
	public boolean isAsyn;
	private LayoutInflater lay;
	// final int TYPE_1 = 0;
	// final int TYPE_2 = 1;
	// final int TYPE_3 = 2;
	// 屏幕的宽高
	private int width;
	private int height;
	WindowManager wm;
	private static final String DetaileAdapter = "DetaileAdapter";
	private Map<String, Drawable> drawMap = new HashMap<String, Drawable>();
	// 设置ImageLoade初始化信息
	private DisplayImageOptions options = new DisplayImageOptions.Builder()
			.showImageForEmptyUri(R.drawable.tempicon)
			.showStubImage(R.drawable.tempicon).resetViewBeforeLoading(false)
			.delayBeforeLoading(100).cacheInMemory(true).cacheOnDisc(true)
			// .imageScaleType(ImageScaleType.IN_SAMPLE_INT)
			.bitmapConfig(Bitmap.Config.RGB_565).build();

	public LocalDetailAdapter(ArrayList<CategoryInfo> categoryInfo,
			Context context, ListView mylistView) {
		super();
		this.categoryInfo = categoryInfo;
		this.mylistView = mylistView;
		mContext = context;
		lay = LayoutInflater.from(context);
		wm = (WindowManager) mContext.getSystemService(mContext.WINDOW_SERVICE);
		Display defaultDisplay = wm.getDefaultDisplay();
		DisplayMetrics dm = new DisplayMetrics();
		((Activity) mContext).getWindowManager().getDefaultDisplay()
				.getMetrics(dm);
		width = dm.widthPixels;
		height = dm.heightPixels;
	}

	@Override
	public int getCount() {
		return categoryInfo.size() / 3;
	}

	@Override
	public Object getItem(int arg0) {
		return null;
	}

	@Override
	public long getItemId(int arg0) {
		return 0;
	}

	/*
	 * //计算有多少类型的item
	 * 
	 * @Override public int getItemViewType(int position) { int p = position %
	 * 2; if (position == 0) { return TYPE_1; } else if (p == 0) { return
	 * TYPE_2; } else { return TYPE_3; }
	 * 
	 * }
	 */

	@Override
	public View getView(final int position, View convertView, ViewGroup arg2) {
		final CategoryInfo cif = categoryInfo.get(position);
		// 两张小图之间的间隙
		int gapPx = convertDipOrPx(mContext, 5);
		int gapPy = convertDipOrPx(mContext, 20);
		int bigImHeight = (int) ((width / 2 - gapPy) / 1.3f);
		if (position % 2 == 0) {
			convertView = lay.inflate(R.layout.item_2_detailelistview, null);
			ImageView ib1 = (ImageView) convertView
					.findViewById(R.id.item2_detail_ib1);
			ImageView ib2 = (ImageView) convertView
					.findViewById(R.id.item2_detail_ib2);
			ImageView ib3 = (ImageView) convertView
					.findViewById(R.id.item2_detail_ib3);
			TextView item3_detail_name1 = (TextView) convertView
					.findViewById(R.id.item3_detail_name1);
			TextView item3_detail_name2 = (TextView) convertView
					.findViewById(R.id.item3_detail_name2);
			TextView item3_detail_name3 = (TextView) convertView
					.findViewById(R.id.item3_detail_name3);
			switch (position) {
			case 0:
				Bitmap bm = BitmapFactory.decodeResource(
						mContext.getResources(), R.drawable.category3);
				Bitmap newbitmap = Bitmap.createBitmap(width / 2 - gapPy,
						bigImHeight, bm.getConfig());
				getNewBitMap(bm, newbitmap);
				ib1.setImageBitmap(newbitmap);
				item3_detail_name1.setText(categoryInfo.get(2).getName());
				item3_detail_name3.setText(categoryInfo.get(0).getName());
				item3_detail_name2.setText(categoryInfo.get(1).getName());
				Bitmap bm2 = BitmapFactory.decodeResource(
						mContext.getResources(), R.drawable.category2);
				Bitmap newbitmap2 = Bitmap.createBitmap(width / 2 - gapPy,
						bigImHeight / 2 - gapPx / 2, bm2.getConfig());
				getNewBitMap(bm2, newbitmap2);
				ib2.setImageBitmap(newbitmap2);
				Bitmap bm3 = BitmapFactory.decodeResource(
						mContext.getResources(), R.drawable.m_127976003046);
				Bitmap newbitmap3 = Bitmap.createBitmap(width / 2 - gapPy,
						bigImHeight / 2 - gapPx / 2, bm3.getConfig());
				getNewBitMap(bm3, newbitmap3);
				ib3.setImageBitmap(newbitmap3);
				break;

			case 1:

				break;

			case 2:
				Bitmap bm4 = BitmapFactory.decodeResource(
						mContext.getResources(), R.drawable.kapai);
				Bitmap newbitmap4 = Bitmap.createBitmap(width / 2 - gapPy,
						bigImHeight, bm4.getConfig());
				getNewBitMap(bm4, newbitmap4);
				ib1.setImageBitmap(newbitmap4);
				Bitmap bm5 = BitmapFactory.decodeResource(
						mContext.getResources(), R.drawable.category1);
				Bitmap newbitmap5 = Bitmap.createBitmap(width / 2 - gapPy,
						bigImHeight / 2 - gapPx / 2, bm5.getConfig());
				getNewBitMap(bm5, newbitmap5);
				ib2.setImageBitmap(newbitmap5);
				Bitmap bm6 = BitmapFactory.decodeResource(
						mContext.getResources(), R.drawable.cata_game_4);
				Bitmap newbitmap6 = Bitmap.createBitmap(width / 2 - gapPy,
						bigImHeight / 2 - gapPx / 2, bm6.getConfig());
				getNewBitMap(bm6, newbitmap6);
				ib3.setImageBitmap(newbitmap6);
				item3_detail_name1.setText(categoryInfo.get(6).getName());
				item3_detail_name3.setText(categoryInfo.get(8).getName());
				item3_detail_name2.setText(categoryInfo.get(7).getName());
				break;
			}
			/*
			 * ImageLoader.getInstance().displayImage(cif.getAppIcon(), ib1,
			 * options);
			 */

			// ib1.setImageResource(R.drawable.te1st);
			/*
			 * ImageLoader.getInstance().displayImage(cif.getAppIcon(), ib2,
			 * options);
			 */
			/*
			 * ImageLoader.getInstance().displayImage(cif.getAppIcon(), ib3,
			 * options);
			 */
			ib1.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					Intent intent = new Intent(mContext,
							IndexDetaileActivity.class);
					Bundle bundle = new Bundle();
					CategoryInfo cif;
					if (position == 0) {
						cif = categoryInfo.get(position+2);
					}/*
					 * else cif = categoryInfo.get(position*3); String Root =
					 * LocalUtils.getRoot(mContext); List<AppInfo> mAppInfos =
					 * LocalUtils.InitHomePager("0", mContext, Root);
					 * bundle.putString("name", cif.getName());
					 * bundle.putInt("type1", Integer.parseInt(cif.getType1()));
					 * bundle.putInt("type2", Integer.parseInt(cif.getType2()));
					 * intent.putExtra("value", bundle);
					 * mContext.startActivity(intent);
					 */
					else {
						cif = categoryInfo.get(position * 3);
					}
					Intent intent1 = new Intent();
					intent1.setClass(mContext, LocalIndexDetaileActivity.class);
					Bundle bundle1 = new Bundle();
					bundle1.putString("id", position * 3 + "");
					bundle1.putString("name", cif.getName() + "");
					intent1.putExtra("value", bundle1);
					mContext.startActivity(intent1);
				}
			});
			ib2.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					Intent intent = new Intent(mContext,
							IndexDetaileActivity.class);
					Bundle bundle = new Bundle();
					CategoryInfo cif;
					if (position == 0) {
						cif = categoryInfo.get(position + 1);
					}/*
					 * else cif = categoryInfo.get(position*3+1);
					 * bundle.putString("name", cif.getName());
					 * bundle.putInt("type1", Integer.parseInt(cif.getType1()));
					 * bundle.putInt("type2", Integer.parseInt(cif.getType2()));
					 * intent.putExtra("value", bundle);
					 * mContext.startActivity(intent);
					 */
					else {
						cif = categoryInfo.get(position * 3 + 1);
					}
					Intent intent2 = new Intent();
					intent2.setClass(mContext, LocalIndexDetaileActivity.class);
					Bundle bundle2 = new Bundle();
					bundle2.putString("id", position * 3 + 1 + "");
					bundle2.putString("name", cif.getName() + "");
					intent2.putExtra("value", bundle2);
					mContext.startActivity(intent2);
				}
			});
			ib3.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					Intent intent = new Intent(mContext,
							IndexDetaileActivity.class);
					Bundle bundle = new Bundle();
					CategoryInfo cif;
					if (position == 0) {
						cif = categoryInfo.get(position );
					} else {
						cif = categoryInfo.get(position * 3 + 2);
					}
					/*
					 * bundle.putString("name", cif.getName());
					 * bundle.putInt("type1", Integer.parseInt(cif.getType1()));
					 * bundle.putInt("type2", Integer.parseInt(cif.getType2()));
					 * intent.putExtra("value", bundle);
					 * mContext.startActivity(intent);
					 */
					Intent intent3 = new Intent();
					intent3.setClass(mContext, LocalIndexDetaileActivity.class);
					Bundle bundle3 = new Bundle();
					bundle3.putString("id", position * 3 + 2 + "");
					bundle3.putString("name", cif.getName() + "");
					intent3.putExtra("value", bundle3);
					mContext.startActivity(intent3);
				}
			});
		} else {
			convertView = lay.inflate(R.layout.item3_detailelistview, null);
			ImageView ib1 = (ImageView) convertView
					.findViewById(R.id.item3_detail_ib1);
			ImageView ib2 = (ImageView) convertView
					.findViewById(R.id.item3_detail_ib2);
			ImageView ib3 = (ImageView) convertView
					.findViewById(R.id.item3_detail_ib3);
			TextView item3_detail_name1 = (TextView) convertView
					.findViewById(R.id.item3_detail_name1);
			TextView item3_detail_name2 = (TextView) convertView
					.findViewById(R.id.item3_detail_name2);
			TextView item3_detail_name3 = (TextView) convertView
					.findViewById(R.id.item3_detail_name3);
			switch (position) {
			case 1:
				Bitmap bm = BitmapFactory.decodeResource(
						mContext.getResources(), R.drawable.cata_game_2);
				Bitmap newbitmap = Bitmap.createBitmap(width / 2 - gapPy,
						bigImHeight / 2 - gapPx / 2, bm.getConfig());
				getNewBitMap(bm, newbitmap);
				item3_detail_name1.setText(categoryInfo.get(3).getName());
				item3_detail_name2.setText(categoryInfo.get(4).getName());
				item3_detail_name3.setText(categoryInfo.get(5).getName());
				ib1.setImageBitmap(newbitmap);
				Bitmap bm2 = BitmapFactory.decodeResource(
						mContext.getResources(), R.drawable.cata_game_5);
				Bitmap newbitmap2 = Bitmap.createBitmap(width / 2 - gapPy,
						bigImHeight / 2 - gapPx / 2, bm2.getConfig());
				getNewBitMap(bm2, newbitmap2);
				ib2.setImageBitmap(newbitmap2);
				Bitmap bm3 = BitmapFactory.decodeResource(
						mContext.getResources(), R.drawable.xiuxian);
				Bitmap newbitmap3 = Bitmap.createBitmap(width / 2 - gapPy,
						bigImHeight, bm3.getConfig());
				getNewBitMap(bm3, newbitmap3);
				ib3.setImageBitmap(newbitmap3);
				break;
			}
			ib1.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					/*
					 * Intent intent = new Intent(mContext,
					 * IndexDetaileActivity.class); Bundle bundle = new
					 * Bundle(); CategoryInfo cif =
					 * categoryInfo.get(position*3); bundle.putString("name",
					 * cif.getName()); bundle.putInt("type1",
					 * Integer.parseInt(cif.getType1())); bundle.putInt("type2",
					 * Integer.parseInt(cif.getType2()));
					 * intent.putExtra("value", bundle);
					 * mContext.startActivity(intent);
					 */
					CategoryInfo cif = categoryInfo.get(position * 3);
					Intent intent = new Intent();
					intent.setClass(mContext, LocalIndexDetaileActivity.class);
					Bundle bundle = new Bundle();
					bundle.putString("id", position * 3 + "");
					bundle.putString("name", cif.getName() + "");
					intent.putExtra("value", bundle);
					mContext.startActivity(intent);
				}
			});
			ib2.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					/*
					 * Intent intent = new Intent(mContext,
					 * IndexDetaileActivity.class); Bundle bundle = new
					 * Bundle(); CategoryInfo cif =
					 * categoryInfo.get(position*3+1); bundle.putString("name",
					 * cif.getName()); bundle.putInt("type1",
					 * Integer.parseInt(cif.getType1())); bundle.putInt("type2",
					 * Integer.parseInt(cif.getType2()));
					 * intent.putExtra("value", bundle);
					 * mContext.startActivity(intent);
					 */
					CategoryInfo cif = categoryInfo.get(position * 3 + 1);
					Intent intent = new Intent();
					intent.setClass(mContext, LocalIndexDetaileActivity.class);
					Bundle bundle = new Bundle();
					bundle.putString("id", position * 3 + 1 + "");
					bundle.putString("name", cif.getName() + "");
					intent.putExtra("value", bundle);
					mContext.startActivity(intent);
				}
			});
			ib3.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					/*
					 * Intent intent = new Intent(mContext,
					 * IndexDetaileActivity.class); Bundle bundle = new
					 * Bundle(); CategoryInfo cif =
					 * categoryInfo.get(position*3+2); bundle.putString("name",
					 * cif.getName()); bundle.putInt("type1",
					 * Integer.parseInt(cif.getType1())); bundle.putInt("type2",
					 * Integer.parseInt(cif.getType2()));
					 * intent.putExtra("value", bundle);
					 * mContext.startActivity(intent);
					 */
					CategoryInfo cif = categoryInfo.get(position * 3 + 1);
					Intent intent = new Intent();
					intent.setClass(mContext, LocalIndexDetaileActivity.class);
					Bundle bundle = new Bundle();
					bundle.putString("id", position * 3 + 2 + "");
					bundle.putString("name", cif.getName() + "");
					intent.putExtra("value", bundle);
					mContext.startActivity(intent);
				}
			});
			/*
			 * ib1.setImageResource(R.drawable.
			 * f738bd4b31c870120211571277f9e2f0608ff96);
			 * ib3.setImageResource(R.drawable.te1st);
			 * ImageLoader.getInstance().displayImage(cif.getAppIcon(), ib1,
			 * options);
			 * ImageLoader.getInstance().displayImage(cif.getAppIcon(), ib2,
			 * options);
			 */
			/*
			 * ImageLoader.getInstance().displayImage(cif.getAppIcon(), ib3,
			 * options);
			 */
		}

		return convertView;
	}

	public void getNewBitMap(Bitmap bm, Bitmap newbitmap) {
		Paint paint = new Paint();
		Canvas canvas = new Canvas(newbitmap);
		Matrix matrix = new Matrix();
		double newWidth = 1.00;
		double newHeight = 2.6;
		int gapPy = convertDipOrPx(mContext, 20);
		// matrix.setRotate(30, bm.getWidth()/2, bm.getHeight()/2);
		float scaleWidth = (float) (width / 2 - 20) / width;
		LogUtils.d("scaleWidth+scaleWidth", scaleWidth + ":" + scaleWidth
				+ "++" + width + "PPP" + 2 / 3);
		matrix.postScale(scaleWidth, scaleWidth);
		// 使用画布将原图片，矩阵，画笔进行新图片的绘画
		canvas.drawBitmap(bm, matrix, paint);
	}

	static class ViewHolder1 {
		private static ImageView ib1;
		private static ImageView ib2;
		private static ImageView ib3;

	}

	static class ViewHolder2 {
		private static ImageView ib1;
		private static ImageView ib2;
		private static ImageView ib3;
	}

	public static int convertDipOrPx(Context context, int dip) {
		float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dip * scale + 0.5f * (dip >= 0 ? 1 : -1));
	}

}
