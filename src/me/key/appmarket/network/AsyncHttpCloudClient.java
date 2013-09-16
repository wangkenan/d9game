package me.key.appmarket.network;

import java.io.IOException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpUriRequest;
import android.os.Handler;
import android.os.Looper;

public class AsyncHttpCloudClient extends Thread {

	private HttpCallBack mCallBack;
	private Object mTag;
	protected HttpCloudClient mClient;
	private Handler mHandler;
	protected HttpUriRequest mReq;

	public AsyncHttpCloudClient(HttpCallBack callBack, Object tag,
			HttpUriRequest req) {
		mCallBack = callBack;
		Looper looper = Looper.getMainLooper();
		mHandler = new Handler(looper);
		mReq = req;
		mTag = tag;
	}

	public interface HttpCallBack {
		public void call(byte[] rst, Object tag);
	}

	public void shutdown() {
		if (mClient != null) {
			mClient.cancel();
		}

	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		super.run();
		byte[] resp = getRes();
		procResult(resp);
		mHandler = null;
	}

	protected byte[] getRes() {
		byte[] resp;
		try {
			mClient = new HttpCloudClient();
			resp = mClient.excuteHttpRequest(mReq);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

		return resp;
	}

	protected void procResult(final byte[] result) {
		if (mCallBack != null) {
			mHandler.post(new Runnable() {
				@Override
				public void run() {
					mCallBack.call(result, mTag);
					mCallBack = null;
				}
			});
		}
	}
}
