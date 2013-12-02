package me.key.appmarket.utils;

import me.key.appmarket.network.NetworkUtils;
import android.content.Context;
import android.os.AsyncTask;
import android.view.View;

/**
 * 没有网络的情况下显示错误界面
 * 
 * @author Administrator
 * 
 */
public class MyAsynTask extends AsyncTask<Void, Void, Void> {
	private Context context;
	private View errorview;

	public MyAsynTask(Context context, View errorview) {
		this.errorview = errorview;
		this.context = context;
	}

	@Override
	protected Void doInBackground(Void... params) {

		return null;
	}

	public void exe() {
		if (NetworkUtils.isNetworkConnected(context)) {
			if (errorview != null) {
				errorview.setVisibility(View.GONE);
			}
			this.execute();
		} else {
			if (errorview != null) {
				errorview.setVisibility(View.VISIBLE);
			}
		}
	}
}
