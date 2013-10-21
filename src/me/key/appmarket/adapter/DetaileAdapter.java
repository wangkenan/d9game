package me.key.appmarket.adapter;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import me.key.appmarket.IndexDetaileActivity;
import me.key.appmarket.ImageNet.AsyncImageLoader;
import me.key.appmarket.utils.CategoryInfo;
import me.key.appmarket.utils.LogUtils;
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
import android.os.Environment;
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
	final int TYPE_1 = 0;
	final int TYPE_2 = 1;
	final int TYPE_3 = 2;
	// 屏幕的宽高
	private int width;
	private int height;
	private int[] imageResIds = new int[] { R.drawable.a20131008173440,
			R.drawable.a20131008173628, R.drawable.a20131008173532,
			R.drawable.a20131008174008, R.drawable.a20131008174001,
			R.drawable.a20131008211144, R.drawable.a20131008174149,
			R.drawable.a20131008174138, R.drawable.a20131008211149,
			R.drawable.a20131008211149, R.drawable.a20131008174138,
			R.drawable.a20131008174149 };

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
	private int gapPy;
	private int bigImHeight;
	private int gapPx;

	public DetaileAdapter(ArrayList<CategoryInfo> categoryInfo,
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
		gapPx = convertDipOrPx(mContext, 5);
		gapPy = convertDipOrPx(mContext, 10);
		bigImHeight = (int) ((width - gapPy) / 2 / 1.27f);
		for (CategoryInfo ci : categoryInfo) {
			LogUtils.d("TAG", ci.getName());
		}
	}

	@Override
	public int getCount() {
		return categoryInfo.size() / 3 + 1;
	}

	@Override
	public Object getItem(int arg0) {
		return null;
	}

	@Override
	public long getItemId(int arg0) {
		return 0;
	}

	// 计算有多少类型的item
	@Override
	public int getItemViewType(int position) {
		int p = position % 2;
		if (position == 0) {
			return TYPE_3;
		} else if (p == 0) {
			return TYPE_1;
		} else {
			return TYPE_2;
		}

	}

	@Override
	public int getViewTypeCount() {
		return 3;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup arg2) {
		final ViewHolder1 viewHolder1;
		final ViewHolder2 viewHolder2;
		final ViewHolder3 viewHolder3;
		int type = getItemViewType(position);
		if (convertView == null) {
			switch (type) {
			case TYPE_1:
				viewHolder1 = new ViewHolder1();
				convertView = lay
						.inflate(R.layout.item3_detailelistview, null);
				viewHolder1.ib1 = (ImageView) convertView
						.findViewById(R.id.item3_detail_ib1);
				viewHolder1.ib2 = (ImageView) convertView
						.findViewById(R.id.item3_detail_ib2);
				viewHolder1.ib3 = (ImageView) convertView
						.findViewById(R.id.item3_detail_ib3);
				LayoutParams par1 = viewHolder1.ib1.getLayoutParams();
				par1.height =  (bigImHeight - gapPx) / 2;
				par1.width = (width - gapPy) / 2;
				viewHolder1.ib1.setLayoutParams(par1);
				LayoutParams par2 = viewHolder1.ib2.getLayoutParams();
				par2.height = (bigImHeight - gapPx) / 2;
				par2.width = (width - gapPy) / 2;
				viewHolder1.ib2.setLayoutParams(par2);
				LayoutParams par3 = viewHolder1.ib3.getLayoutParams();
				par3.height = bigImHeight;
				par3.width = (width - gapPy) / 2;
				viewHolder1.ib3.setLayoutParams(par3);
				convertView.setTag(viewHolder1);
				break;
			case TYPE_2:
				viewHolder2 = new ViewHolder2();
				convertView = lay.inflate(R.layout.item_2_detailelistview, null);
				viewHolder2.ib1 = (ImageView) convertView
						.findViewById(R.id.item2_detail_ib1);
				viewHolder2.ib2 = (ImageView) convertView
						.findViewById(R.id.item2_detail_ib2);
				viewHolder2.ib3 = (ImageView) convertView
						.findViewById(R.id.item2_detail_ib3);
				LayoutParams par11 = viewHolder2.ib1.getLayoutParams();
				par11.height =bigImHeight;
				par11.width = (width - gapPy) / 2;
				viewHolder2.ib1.setLayoutParams(par11);
				LayoutParams par22 = viewHolder2.ib2.getLayoutParams();
				par22.height = (bigImHeight - gapPx) / 2;
				par22.width = (width - gapPy) / 2;
				viewHolder2.ib2.setLayoutParams(par22);
				LayoutParams par33 = viewHolder2.ib3.getLayoutParams();
				par33.height = (bigImHeight - gapPx) / 2;
				par33.width = (width - gapPy) / 2;
				viewHolder2.ib3.setLayoutParams(par33);
				break;
			case TYPE_3:
				viewHolder3 = new ViewHolder3();
				convertView = lay.inflate(R.layout.item_banner, null);
				viewHolder3.iv1 = (ImageView) convertView
						.findViewById(R.id.banner);
				convertView.setTag(viewHolder3);
				break;
			}
		} else {
			switch (type) {
			case TYPE_1:
				viewHolder1 = (ViewHolder1) convertView.getTag();
				break;

			case TYPE_2:
				viewHolder2 = (ViewHolder2) convertView.getTag();
				break;
			case TYPE_3:
				viewHolder3 = (ViewHolder3) convertView.getTag();
				break;
			}
		}

		switch (type) {
		case TYPE_3:

			final ViewHolder3 v3 = ((ViewHolder3) convertView.getTag());
			setImagePosition(R.drawable.a20131008173322, v3.iv1);
			v3.iv1.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					Intent intent = new Intent(mContext,
							IndexDetaileActivity.class);
					Bundle bundle = new Bundle();
					CategoryInfo cif;
					cif = categoryInfo.get(5);
					bundle.putString("name", "每周热门盘点");
					bundle.putInt("type1", Integer.parseInt(cif.getType1()));
					bundle.putInt("type2", Integer.parseInt(cif.getType2()));
					intent.putExtra("value", bundle);
					mContext.startActivity(intent);
				}
			});
			break;

		case TYPE_2:
			final int newPosition2 = position - 1;
			final ViewHolder2 v2 = ((ViewHolder2) convertView.getTag());
			setImage(imageResIds[newPosition2 * 3], v2.ib1);
			setImage(imageResIds[newPosition2 * 3 + 1], v2.ib2);
			setImage(imageResIds[newPosition2 * 3 + 2], v2.ib3);
			v2.ib1.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					if (categoryInfo.size() != 0) {
						Intent intent = new Intent(mContext,
								IndexDetaileActivity.class);
						Bundle bundle = new Bundle();
						CategoryInfo cif;
						if (newPosition2 == 0) {
							cif = categoryInfo.get(newPosition2 + 2);
						} else
							cif = categoryInfo.get(5);
						bundle.putString("name", cif.getName());
						bundle.putInt("type1", Integer.parseInt(cif.getType1()));
						bundle.putInt("type2", Integer.parseInt(cif.getType2()));
						intent.putExtra("value", bundle);
						mContext.startActivity(intent);
					}
				}
			});
			v2.ib2.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					if (categoryInfo.size() != 0) {
						Intent intent = new Intent(mContext,
								IndexDetaileActivity.class);
						Bundle bundle = new Bundle();
						CategoryInfo cif;
						if (position == 0) {
							cif = categoryInfo.get(newPosition2 + 1);
						} else
							cif = categoryInfo.get(newPosition2 * 3 + 1);
						bundle.putString("name", cif.getName());
						bundle.putInt("type1", Integer.parseInt(cif.getType1()));
						bundle.putInt("type2", Integer.parseInt(cif.getType2()));
						intent.putExtra("value", bundle);
						mContext.startActivity(intent);
					}
				}
			});
			v2.ib3.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					if (categoryInfo.size() != 0) {
						Intent intent = new Intent(mContext,
								IndexDetaileActivity.class);
						Bundle bundle = new Bundle();
						CategoryInfo cif;
						if (newPosition2 == 0) {
							cif = categoryInfo.get(newPosition2);
						} else
							cif = categoryInfo.get(newPosition2 * 3);
						bundle.putString("name", cif.getName());
						bundle.putInt("type1", Integer.parseInt(cif.getType1()));
						bundle.putInt("type2", Integer.parseInt(cif.getType2()));
						intent.putExtra("value", bundle);
						mContext.startActivity(intent);
					}
				}
			});
			break;
		case TYPE_1:
			final int newPosition = position - 1;
			final ViewHolder1 v1 = ((ViewHolder1) convertView.getTag());
			setImage(imageResIds[newPosition * 3], v1.ib1);
			setImage(imageResIds[newPosition * 3 + 1], v1.ib2);
			setImage(imageResIds[newPosition * 3 + 2], v1.ib3);

			v1.ib1.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					if (categoryInfo.size() != 0) {
						Intent intent = new Intent(mContext,
								IndexDetaileActivity.class);
						Bundle bundle = new Bundle();
						if (newPosition == 3) {
							CategoryInfo cif = categoryInfo
									.get(newPosition * 2);
							bundle.putString("name", cif.getName());
							bundle.putInt("type1",
									Integer.parseInt(cif.getType1()));
							bundle.putInt("type2",
									Integer.parseInt(cif.getType2()));
							intent.putExtra("value", bundle);
							mContext.startActivity(intent);
						} else {
							CategoryInfo cif = categoryInfo
									.get(newPosition * 3);
							bundle.putString("name", cif.getName());
							bundle.putInt("type1",
									Integer.parseInt(cif.getType1()));
							bundle.putInt("type2",
									Integer.parseInt(cif.getType2()));
							intent.putExtra("value", bundle);
							mContext.startActivity(intent);
						}
					}
				}
			});
			v1.ib2.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					if (categoryInfo.size() != 0) {
						Intent intent = new Intent(mContext,
								IndexDetaileActivity.class);
						Bundle bundle = new Bundle();
						if (newPosition == 3) {
							CategoryInfo cif = categoryInfo
									.get(newPosition * 2);
							bundle.putString("name", cif.getName());
							bundle.putInt("type1",
									Integer.parseInt(cif.getType1()));
							bundle.putInt("type2",
									Integer.parseInt(cif.getType2()));
							intent.putExtra("value", bundle);
							mContext.startActivity(intent);
						} else {
							CategoryInfo cif = categoryInfo.get(8);
							bundle.putString("name", "必玩大作");
							bundle.putInt("type1",
									Integer.parseInt(cif.getType1()));
							bundle.putInt("type2",
									Integer.parseInt(cif.getType2()));
							intent.putExtra("value", bundle);
							mContext.startActivity(intent);
						}
					}
				}
			});
			v1.ib3.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					if (categoryInfo.size() != 0) {
						Intent intent = new Intent(mContext,
								IndexDetaileActivity.class);
						Bundle bundle = new Bundle();
						if (newPosition == 3) {
							CategoryInfo cif = categoryInfo
									.get(newPosition * 2);
							bundle.putString("name", cif.getName());
							bundle.putInt("type1",
									Integer.parseInt(cif.getType1()));
							bundle.putInt("type2",
									Integer.parseInt(cif.getType2()));
							intent.putExtra("value", bundle);
							mContext.startActivity(intent);
						} else {
							CategoryInfo cif = categoryInfo
									.get(newPosition * 3 + 1);
							bundle.putString("name", cif.getName());
							bundle.putInt("type1",
									Integer.parseInt(cif.getType1()));
							bundle.putInt("type2",
									Integer.parseInt(cif.getType2()));
							intent.putExtra("value", bundle);
							mContext.startActivity(intent);
						}
					}
				}
			});
			break;
		}

		// 两张小图之间的间隙
		if (position == 0) {

		} else {

			// final CategoryInfo cif = categoryInfo.get(newPosition);

			/*
			 * TextView item3_detail_name1 = (TextView)
			 * convertView.findViewById(R.id.item3_detail_name1); TextView
			 * item3_detail_name2 = (TextView)
			 * convertView.findViewById(R.id.item3_detail_name2); TextView
			 * item3_detail_name3 = (TextView)
			 * convertView.findViewById(R.id.item3_detail_name3);
			 */

			/*
			 * item3_detail_name1.setText(categoryInfo.get(2).getName());
			 * item3_detail_name3.setText(categoryInfo.get(0).getName());
			 * item3_detail_name2.setText(categoryInfo.get(1).getName());
			 */
			/*
			 * Bitmap bm2 =
			 * BitmapFactory.decodeResource(mContext.getResources(),
			 * R.drawable.category2); Bitmap newbitmap2 =
			 * Bitmap.createBitmap((width-gapPy)/2,(bigImHeight-gapPx)/2,
			 * bm2.getConfig()); getNewBitMap(bm2, newbitmap2);
			 * ib2.setImageBitmap(newbitmap2); Bitmap bm3 =
			 * BitmapFactory.decodeResource(mContext.getResources(),
			 * R.drawable.m_127976003046); Bitmap newbitmap3 =
			 * Bitmap.createBitmap((width-gapPy)/2,(bigImHeight-gapPx)/2,
			 * bm3.getConfig()); getNewBitMap(bm3, newbitmap3);
			 * ib3.setImageBitmap(newbitmap3);
			 */

			/*
			 * Bitmap bm = BitmapFactory.decodeResource(mContext.getResources(),
			 * R.drawable.category3); Bitmap newbitmap =
			 * Bitmap.createBitmap(width/2-gapPy,bigImHeight, bm.getConfig());
			 * getNewBitMap(bm, newbitmap); ib1.setImageBitmap(newbitmap); bm =
			 * BitmapFactory.decodeResource(mContext.getResources(),
			 * R.drawable.cata_game_3); newbitmap =
			 * Bitmap.createBitmap(width/2-gapPy, bigImHeight/2-gapPx/2,
			 * bm.getConfig()); getNewBitMap(bm, newbitmap);
			 * ib2.setImageBitmap(newbitmap); bm =
			 * BitmapFactory.decodeResource(mContext.getResources(),
			 * R.drawable.cata_game_2); newbitmap =
			 * Bitmap.createBitmap(width/2-gapPy, bigImHeight/2-gapPx/2,
			 * bm.getConfig()); getNewBitMap(bm, newbitmap);
			 * ib3.setImageBitmap(newbitmap);
			 */

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

			/*
			 * TextView item3_detail_name1 = (TextView)
			 * convertView.findViewById(R.id.item3_detail_name1); TextView
			 * item3_detail_name2 = (TextView)
			 * convertView.findViewById(R.id.item3_detail_name2); TextView
			 * item3_detail_name3 = (TextView)
			 * convertView.findViewById(R.id.item3_detail_name3);
			 */
			/*
			 * switch (position) { case 1: Bitmap bm =
			 * BitmapFactory.decodeResource(mContext.getResources(),
			 * R.drawable.cata_game_2); Bitmap newbitmap =
			 * Bitmap.createBitmap((width-gapPy)/2,bigImHeight/2-gapPx/2,
			 * bm.getConfig()); getNewBitMap(bm, newbitmap);
			 * ib1.setImageBitmap(newbitmap); Bitmap bm2 =
			 * BitmapFactory.decodeResource(mContext.getResources(),
			 * R.drawable.cata_game_5); Bitmap newbitmap2 =
			 * Bitmap.createBitmap((width-gapPy)/2,bigImHeight/2-gapPx/2,
			 * bm2.getConfig()); getNewBitMap(bm2, newbitmap2);
			 * ib2.setImageBitmap(newbitmap2); Bitmap bm3 =
			 * BitmapFactory.decodeResource(mContext.getResources(),
			 * R.drawable.xiuxian); Bitmap newbitmap3 =
			 * Bitmap.createBitmap((width-gapPy)/2,bigImHeight,
			 * bm3.getConfig()); getNewBitMap(bm3, newbitmap3);
			 * ib3.setImageBitmap(newbitmap3); break; }
			 */

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

	private void setImagePosition(int resId, ImageView banner) {
		Bitmap bm = BitmapFactory
				.decodeResource(mContext.getResources(), resId);
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
		int gapPx = convertDipOrPx(mContext, 5);
		// matrix.setRotate(30, bm.getWidth()/2, bm.getHeight()/2);
		float scaleWidth = (float) ((width - gapPx)) / bm.getWidth();
		float scaleHeight = (float) (newbitmap.getHeight()) / bm.getHeight();
		LogUtils.d("scaleWidth+scaleWidth", scaleWidth + ":" + scaleWidth
				+ "++" + width + "PPP" + 2 / 3);
		matrix.postScale(scaleWidth, scaleHeight);
		// 使用画布将原图片，矩阵，画笔进行新图片的绘画
		canvas.drawBitmap(bm, matrix, paint);
	}

	public void getNewBitMap(Bitmap bm, Bitmap newbitmap) {
		Paint paint = new Paint();
		Canvas canvas = new Canvas(newbitmap);
		Matrix matrix = new Matrix();
		double newWidth = 1.00;
		double newHeight = 2.6;
		int gapPx = convertDipOrPx(mContext, 5);
		// matrix.setRotate(30, bm.getWidth()/2, bm.getHeight()/2);
		float scaleWidth = (float) ((width - gapPx) / 2) / bm.getWidth();
		float scaleHeight = (float) (newbitmap.getHeight()) / bm.getHeight();
		LogUtils.d("scaleWidth+scaleWidth", scaleWidth + ":" + scaleWidth
				+ "++" + width + "PPP" + 2 / 3);
		matrix.postScale(scaleWidth, scaleHeight);
		// 使用画布将原图片，矩阵，画笔进行新图片的绘画
		canvas.drawBitmap(bm, matrix, paint);
		// 保存全部图层
		canvas.save(Canvas.ALL_SAVE_FLAG);
		canvas.restore();
		// 存储路径
		File file = new File(Environment.getExternalStorageDirectory()
				+ "/d9catch/");
		if (!file.exists())
			file.mkdirs();
		try {
			FileOutputStream fileOutputStream = new FileOutputStream(
					file.getPath() + "" + "/.jpg");
			newbitmap.compress(Bitmap.CompressFormat.JPEG, 100,
					fileOutputStream);
			fileOutputStream.close();
			System.out.println("saveBmp is here");
		} catch (Exception e) {
			e.printStackTrace();
		}
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

	static class ViewHolder3 {
		private static ImageView iv1;
	}

	public static int convertDipOrPx(Context context, int dip) {
		float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dip * scale + 0.5f * (dip >= 0 ? 1 : -1));
	}

	public void setImage(int resId, ImageView iv) {
		// Bitmap bm = BitmapFactory.decodeResource(mContext.getResources(),
		// resId);
		// Bitmap newbitmap = Bitmap.createBitmap((width-gapPy)/2,bigImHeight,
		// bm.getConfig());
		// getNewBitMap(bm, newbitmap);
		// iv.setImageBitmap(newbitmap);
		iv.setImageResource(resId);

	}
}
