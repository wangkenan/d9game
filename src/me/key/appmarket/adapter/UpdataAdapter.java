package me.key.appmarket.adapter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import me.key.appmarket.MarketApplication;
import me.key.appmarket.tool.DownloadService;
import me.key.appmarket.utils.AppInfo;
import me.key.appmarket.widgets.ProgressView;

import com.market.d9game.R;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 可升级应用adapter
 * @author Administrator
 *
 */
public class UpdataAdapter extends BaseAdapter {
	private LayoutInflater mInflater;
	private Context context;
	private List<AppInfo> updataList;
	private DisplayImageOptions options = new DisplayImageOptions.Builder()  
    .showImageForEmptyUri(R.drawable.tempicon).showStubImage(R.drawable.tempicon)  
    .resetViewBeforeLoading(false) 
    .delayBeforeLoading(100)  
    .cacheInMemory(true)           
    .cacheOnDisc(true)              
    .imageScaleType(ImageScaleType.IN_SAMPLE_INT)
    .bitmapConfig(Bitmap.Config.RGB_565)               
    .build(); 
	public UpdataAdapter(List<AppInfo> updataList,Context context){
		this.updataList = updataList;
		mInflater = LayoutInflater.from(context);
		this.context = context;
	}
	@Override
	public int getCount() {
		return updataList.size();
	}

	@Override
	public Object getItem(int position) {
		return null;
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		final AppInfo sdappInfo = updataList.get(position);;
		if(convertView == null) {
			holder = new ViewHolder();
			convertView = mInflater.inflate(R.layout.list_item, null);
			holder.info = (TextView) convertView.findViewById(R.id.info);
			holder.icon = (ImageView) convertView.findViewById(R.id.icon);
			holder.operate = (ImageView) convertView.findViewById(R.id.iv_oprate_state);
			holder.btn = (TextView) convertView.findViewById(R.id.install);
			holder.progress_view = (ProgressView) convertView
					.findViewById(R.id.progress_view_local);

			holder.appsize = (TextView) convertView
					.findViewById(R.id.appsize);
			convertView.setTag(holder);
		} else {
			holder= ((ViewHolder) convertView.getTag());
			
		}
		boolean isUpdate = false;
		
		holder.btn.setText("升级");
		holder.progress_view.setProgress(100);
		holder.operate.setImageResource(R.drawable.update_btn);
		/*
		 * Drawable mDrawable1 = this.cnt.getResources().getDrawable(
		 * R.drawable.action_type_software_update);
		 * v2.btn.setCompoundDrawablesWithIntrinsicBounds(null, mDrawable1,
		 * null, null);
		 */
		/*
		 * else { sdappInfo = mData .get((newposition - appManaInfos.size()
		 * - downList .size()));
		 * 
		 * Drawable mDrawable2 = cnt.getResources().getDrawable(
		 * R.drawable.downloaded);
		 * v2.btn.setCompoundDrawablesWithIntrinsicBounds(null, mDrawable2,
		 * null, null);
		 * 
		 * v2.btn.setText("安装"); v2.progress_view.setProgress(0); String mb
		 * = ToolHelper.Kb2Mb(sdappInfo.getAppSize());
		 * v2.appsize.setText(mb); }
		 */
		holder.btn.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					List<AppInfo> down_temp = new ArrayList<AppInfo>();
					File tempFile = new File(Environment
							.getExternalStorageDirectory(), "/market/"
							+ sdappInfo.getAppName() + ".apk");
					if (tempFile.exists()) {
						tempFile.delete();
					}
					DownloadService.downNewFile(sdappInfo, 0, 0, null);
					MarketApplication.getInstance().getDownApplist().add(sdappInfo);
					notifyDataSetChanged();
					sdappInfo.setDown(true);
					sdappInfo.setIspause(false);
					Intent intent = new Intent();
					Intent downState = new Intent();

					downState.setAction(tempFile.getAbsolutePath() + "down");
					downState.putExtra("isPause", sdappInfo.isIspause());
					context.sendBroadcast(downState);
					intent.setAction(MarketApplication.PRECENT);
					context.sendBroadcast(intent);
					Toast.makeText(context,
							sdappInfo.getAppName() + " 开始下载...",
							Toast.LENGTH_SHORT).show();

				}
			});
		holder.progress_view.setProgress(0);
		// v2.progress_view.setVisibility(View.VISIBLE);
		holder.info.setText(sdappInfo.getAppName());
		holder.icon.setVisibility(View.VISIBLE);
		ImageLoader.getInstance().displayImage(sdappInfo.getIconUrl(), holder.icon, options);
		return convertView;
	}
	static class ViewHolder {
		public TextView appsize;
		public TextView btn;
		public ImageView icon,operate;
		public TextView info;
		private ProgressView progress_view;
	}
}
