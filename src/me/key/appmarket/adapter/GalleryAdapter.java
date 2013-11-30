package me.key.appmarket.adapter;

import me.key.appmarket.utils.Global;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

import com.nostra13.universalimageloader.core.ImageLoader;

public class GalleryAdapter extends BaseAdapter {

	private Context context;
	private String[] images;

	public GalleryAdapter(Context context,String[] images){
		this.context = context;
		this.images = images;
	}
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return images.length;
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return images[position];
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		ImageView img = new ImageView(context);
		//�˴�ÿ��ImageView��Ҫռȫ���ռ�
		img.setLayoutParams(new Gallery.LayoutParams(Gallery.LayoutParams.FILL_PARENT, 
				Gallery.LayoutParams.FILL_PARENT));
		ImageLoader.getInstance().displayImage(images[position], img, Global.options);
		img.setScaleType(ScaleType.FIT_XY);
		return img;
	}
	
}
