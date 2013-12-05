package me.key.appmarket.adapter;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import me.key.appmarket.utils.Global;
import me.key.appmarket.utils.MyAsynTask;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Matrix;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

import com.market.d9game.R;
import com.nostra13.universalimageloader.core.ImageLoader;

public class GalleryAdapter extends BaseAdapter {

	private Context context;
	private LayoutInflater inflater;
	private ArrayList<Bitmap> bitmaps;

	public GalleryAdapter(Context context, ArrayList<Bitmap> bitmaps) {
		this.bitmaps = bitmaps;
		this.context = context;
		this.inflater = LayoutInflater.from(context);
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return bitmaps.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return bitmaps.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
//		final viewHolder holder;
//		if (convertView == null) {
//			holder = new viewHolder();
//			convertView = inflater.inflate(R.layout.item_gallary_appdetail,
//					null);
//			holder.ivPic = (ImageView) convertView
//					.findViewById(R.id.iv_pic_gallary_appdetail_adapter);
//			convertView.setTag(holder);
//		} else {
//			holder = (viewHolder) convertView.getTag();
//		}
//		holder.ivPic.setImageBitmap(bitmaps.get(position));
//
//		return convertView;
		
		ImageView img = new ImageView(context);
		img.setLayoutParams(new Gallery.LayoutParams(Gallery.LayoutParams.FILL_PARENT, 
				Gallery.LayoutParams.FILL_PARENT));
		img.setImageBitmap(bitmaps.get(position));
		//img.setScaleType(ScaleType.FIT_XY);
		return img;
	}

	class viewHolder {
		private ImageView ivPic;
	}

}
