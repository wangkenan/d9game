package me.key.appmarket.adapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import me.key.appmarket.IndexDetaileActivity;
import me.key.appmarket.ImageNet.AsyncImageLoader;
import me.key.appmarket.utils.CategoryInfo;
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

import com.market.d9game.R;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

/**
 * 分类信息adapter
 * 
 * @author Administrator
 * 
 */
public class DetaileAdapter extends BaseAdapter {
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
	//final int TYPE_1 = 0;
	//final int TYPE_2 = 1;
	//final int TYPE_3 = 2;
	//屏幕的宽高
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
//			.imageScaleType(ImageScaleType.IN_SAMPLE_INT)
			.bitmapConfig(Bitmap.Config.RGB_565).build();

	public DetaileAdapter(ArrayList<CategoryInfo> categoryInfo,
			Context context, ListView mylistView) {
		super();
		this.categoryInfo = categoryInfo;
		this.mylistView = mylistView;
		mContext = context;
		lay = LayoutInflater.from(context);
		wm = (WindowManager) mContext.getSystemService(mContext.WINDOW_SERVICE);
		Display defaultDisplay = wm.getDefaultDisplay();
		DisplayMetrics dm=new DisplayMetrics();  
		((Activity) mContext).getWindowManager().getDefaultDisplay().getMetrics(dm);   
		width=dm.widthPixels;   
		height=dm.heightPixels;   
	}

	@Override
	public int getCount() {
		return categoryInfo.size()/3;
	}

	@Override
	public Object getItem(int arg0) {
		return null;
	}

	@Override
	public long getItemId(int arg0) {
		return 0;
	}
/*//计算有多少类型的item
	@Override
	public int getItemViewType(int position) {
		int p = position % 2;
		if (position == 0) {
			return TYPE_1;
		} else if (p == 0) {
			return TYPE_2;
		} else {
			return TYPE_3;
		}

	}*/

	@Override
	public View getView(final int position, View convertView, ViewGroup arg2) {
			final CategoryInfo cif = categoryInfo.get(position);
		//两张小图之间的间隙
		int gapPx = convertDipOrPx(mContext, 5);
	if (position % 2 == 0) {
			convertView = lay.inflate(R.layout.item_2_detailelistview, null);
			ImageView ib1 = (ImageView) convertView
					.findViewById(R.id.item2_detail_ib1);
			ImageView ib2 = (ImageView) convertView
					.findViewById(R.id.item2_detail_ib2);
			ImageView ib3 = (ImageView) convertView
					.findViewById(R.id.item2_detail_ib3);
	        Bitmap bm = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.category3);
            Bitmap newbitmap = Bitmap.createBitmap(width/2-20, (int)(width/2.6f), bm.getConfig());
	        getNewBitMap(bm, newbitmap);
	        ib1.setImageBitmap(newbitmap);
			bm = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.category1);
			newbitmap = Bitmap.createBitmap(width/2-20, (int)(width/5.2f)-gapPx/2, bm.getConfig());
			 getNewBitMap(bm, newbitmap);
			ib2.setImageBitmap(newbitmap);
			bm = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.cata_game_2);
			newbitmap = Bitmap.createBitmap(width/2-20, (int)(width/5.2f)-gapPx/2, bm.getConfig());
			 getNewBitMap(bm, newbitmap);
			ib3.setImageBitmap(newbitmap);
			
			 /* ImageLoader.getInstance().displayImage(cif.getAppIcon(), ib1,
			  options);*/
			 
			//ib1.setImageResource(R.drawable.te1st);
			/*ImageLoader.getInstance().displayImage(cif.getAppIcon(), ib2,
					options);*/
			/*ImageLoader.getInstance().displayImage(cif.getAppIcon(), ib3,
					options);*/
			ib1.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					Intent intent = new Intent(mContext,
							IndexDetaileActivity.class);
					Bundle bundle = new Bundle();
					CategoryInfo cif;
					if(position ==0 ) {
						cif = categoryInfo.get(position);
					}else
						cif   = categoryInfo.get(position*3);
					bundle.putString("name", cif.getName());
					bundle.putInt("type1", Integer.parseInt(cif.getType1()));
					bundle.putInt("type2", Integer.parseInt(cif.getType2()));
					intent.putExtra("value", bundle);
					mContext.startActivity(intent);
				}
			});
			ib2.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					Intent intent = new Intent(mContext,
							IndexDetaileActivity.class);
					Bundle bundle = new Bundle();
					CategoryInfo cif;
					if(position ==0 ) {
						cif = categoryInfo.get(position+1);
					}else
						cif   = categoryInfo.get(position*3+1);
					bundle.putString("name", cif.getName());
					bundle.putInt("type1", Integer.parseInt(cif.getType1()));
					bundle.putInt("type2", Integer.parseInt(cif.getType2()));
					intent.putExtra("value", bundle);
					mContext.startActivity(intent);
				}
			});
			ib3.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					Intent intent = new Intent(mContext,
							IndexDetaileActivity.class);
					Bundle bundle = new Bundle();
					CategoryInfo cif;
					if(position ==0 ) {
						cif = categoryInfo.get(position+2);
					}else
						cif   = categoryInfo.get(position*3+2);
					bundle.putString("name", cif.getName());
					bundle.putInt("type1", Integer.parseInt(cif.getType1()));
					bundle.putInt("type2", Integer.parseInt(cif.getType2()));
					intent.putExtra("value", bundle);
					mContext.startActivity(intent);
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
			int bigImHeight = (int)((width/2-20)/1.3f);
		    Bitmap bm = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.cata_game_1);
            Bitmap newbitmap = Bitmap.createBitmap(width/2-20,bigImHeight/2-gapPx/2, bm.getConfig());
	        getNewBitMap(bm, newbitmap);
	        ib1.setImageBitmap(newbitmap);
			bm = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.cata_game_2);
			newbitmap = Bitmap.createBitmap(width/2-20,bigImHeight/2-gapPx/2, bm.getConfig());
			 getNewBitMap(bm, newbitmap);
			ib2.setImageBitmap(newbitmap);
			bm = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.category3);
			newbitmap = Bitmap.createBitmap(width/2-20, bigImHeight, bm.getConfig());
			 getNewBitMap(bm, newbitmap);
			ib3.setImageBitmap(newbitmap);
			ib1.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					Intent intent = new Intent(mContext,
							IndexDetaileActivity.class);
					Bundle bundle = new Bundle();
					CategoryInfo cif = categoryInfo.get(position*3);
					bundle.putString("name", cif.getName());
					bundle.putInt("type1", Integer.parseInt(cif.getType1()));
					bundle.putInt("type2", Integer.parseInt(cif.getType2()));
					intent.putExtra("value", bundle);
					mContext.startActivity(intent);
				}
			});
			ib2.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					Intent intent = new Intent(mContext,
							IndexDetaileActivity.class);
					Bundle bundle = new Bundle();
					CategoryInfo cif = categoryInfo.get(position*3+1);
					bundle.putString("name", cif.getName());
					bundle.putInt("type1", Integer.parseInt(cif.getType1()));
					bundle.putInt("type2", Integer.parseInt(cif.getType2()));
					intent.putExtra("value", bundle);
					mContext.startActivity(intent);
				}
			});
			ib3.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					Intent intent = new Intent(mContext,
							IndexDetaileActivity.class);
					Bundle bundle = new Bundle();
					CategoryInfo cif = categoryInfo.get(position*3+2);
					bundle.putString("name", cif.getName());
					bundle.putInt("type1", Integer.parseInt(cif.getType1()));
					bundle.putInt("type2", Integer.parseInt(cif.getType2()));
					intent.putExtra("value", bundle);
					mContext.startActivity(intent);
				}
			});
		/*	ib1.setImageResource(R.drawable.f738bd4b31c870120211571277f9e2f0608ff96);
			ib3.setImageResource(R.drawable.te1st);
			ImageLoader.getInstance().displayImage(cif.getAppIcon(), ib1,
					options);
			ImageLoader.getInstance().displayImage(cif.getAppIcon(), ib2,
					options);*/
		/*	ImageLoader.getInstance().displayImage(cif.getAppIcon(), ib3,
					options);*/
		}

		return convertView;
	}

	public void getNewBitMap(Bitmap bm, Bitmap newbitmap) {
		Paint paint = new Paint();
		Canvas canvas = new Canvas(newbitmap);
		Matrix matrix = new Matrix();
		double newWidth = 1.00;
		double newHeight = 2.6;
      // matrix.setRotate(30, bm.getWidth()/2, bm.getHeight()/2);
		float scaleWidth =(float) (newWidth/2.0);
		LogUtils.d("scaleWidth+scaleWidth", scaleWidth+":"+scaleWidth+"++"+width+"PPP"+2/3);
		matrix.postScale(scaleWidth, scaleWidth);
//使用画布将原图片，矩阵，画笔进行新图片的绘画
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
	    return (int)(dip*scale + 0.5f*(dip>=0?1:-1)); 
	} 

}
