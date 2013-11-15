package me.key.appmarket.adapter;

import java.io.File;
import java.util.ArrayList;

import me.key.appmarket.ImageNet.AsyncImageLoader;
import me.key.appmarket.ImageNet.AsyncImageLoader.ImageCallback;
import me.key.appmarket.tool.ToolHelper;
import me.key.appmarket.utils.CategoryInfo;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.market.d9game.R;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

public class CategoryAdapter extends BaseAdapter {

	private ArrayList<CategoryInfo> mCategoryInfos;
	private LayoutInflater lay;
	private File cache;
	private Context mContext;
	private DisplayImageOptions options = new DisplayImageOptions.Builder()  
    .showImageForEmptyUri(R.drawable.tempicon).showStubImage(R.drawable.tempicon)  
    .resetViewBeforeLoading(false) 
    .delayBeforeLoading(100)  
    .cacheInMemory(true)           
    .cacheOnDisc(true)              
    .imageScaleType(ImageScaleType.IN_SAMPLE_INT)
    .bitmapConfig(Bitmap.Config.RGB_565)               
    .build(); 
	AsyncImageLoader asyncImageLoader;

	public CategoryAdapter(ArrayList<CategoryInfo> mCategoryInfos,
			Context context, File cache) {
		super();
		this.mCategoryInfos = mCategoryInfos;
		this.cache = cache;
		mContext = context;
		lay = LayoutInflater.from(context);
		asyncImageLoader = new AsyncImageLoader();
	}

	@Override
	public int getCount() {
		return mCategoryInfos.size();
	}

	@Override
	public Object getItem(int arg0) {
		return mCategoryInfos.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		return arg0;
	}

	@Override
	public View getView(final int position, View convertvView, ViewGroup arg2) {
		ViewHolder viewHolder;
		if (convertvView == null) {
			viewHolder = new ViewHolder();
			convertvView = lay.inflate(R.layout.category_list_item, null);
			viewHolder.icon = (ImageView) convertvView
					.findViewById(R.id.icon_image);
			viewHolder.name = (TextView) convertvView
					.findViewById(R.id.category_name);
			convertvView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertvView.getTag();
		}
		ImageLoader.getInstance().displayImage(mCategoryInfos.get(position).getAppIcon(), viewHolder.icon, options);

		viewHolder.name.setText(mCategoryInfos.get(position).getName());
		return convertvView;
	}

	private class ViewHolder {
		private ImageView icon;
		private TextView name;
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
	public void addNewsItem(CategoryInfo newsitem) {
		mCategoryInfos.add(newsitem);
	}

	public Drawable getDrawable(AsyncImageLoader asyncImageLoader,
			String imageUrl, final ImageView imageView) {
		Drawable drawable = asyncImageLoader.loadDrawable(imageUrl,
				new ImageCallback() {
					@Override
					public void imageLoaded(Drawable imageDrawable,
							String imageUrl) {
						if (imageDrawable != null)
							imageView.setImageDrawable(imageDrawable);
						else
							imageView.setImageResource(R.drawable.tempicon);
					}
				});
		return drawable;
	}
}
