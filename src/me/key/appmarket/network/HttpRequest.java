package me.key.appmarket.network;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import me.key.appmarket.network.AsyncHttpCloudClient.HttpCallBack;
import me.key.appmarket.utils.LogUtils;

import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.protocol.HTTP;

abstract public class HttpRequest implements HttpCallBack {
	private final static String TAG = "HttpRequest";

	public final static int HTTP_POST = 0;
	public final static int HTTP_GET = 1;

	protected Object mTag;

	protected OnResponseListener mOnResponseListener;
	protected HttpUriRequest mRequest;

	protected AsyncHttpCloudClient mClient;

	public void clearListener() {
		mOnResponseListener = null;
	}

	public void cancel() {
		if (mClient != null) {
			mClient.shutdown();
		}
		if (mRequest != null) {
			mRequest.abort();
		}
	}

	public void execute(final OnResponseListener l) {
		mOnResponseListener = l;

		List<NameValuePair> params = new ArrayList<NameValuePair>();
		fillParams(params);
		logPostParams(getUrl(), params);
		try {
			if (postOrGet() == HTTP_POST) {
				mRequest = fetchPost(params);
			} else {
				mRequest = fetchGet(params);
			}
		} catch (UnsupportedEncodingException e) {
			if (l != null) {
				HttpResponse resp = getResponse(null, mTag);
				resp.error = HttpResponse.NETWORK_ERROR;
				l.onGetResponse(resp);
				mOnResponseListener = null;
			}
			e.printStackTrace();
			return;
		}
		mClient = new AsyncHttpCloudClient(this, mTag, mRequest);
		mClient.start();
	}

	public byte[] syncExecute() {
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		fillParams(params);
		logPostParams(getUrl(), params);
		try {
			if (postOrGet() == HTTP_POST) {
				mRequest = fetchPost(params);
			} else {
				mRequest = fetchGet(params);
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return null;
		}
		byte[] resp = null;
		try {
			resp = new HttpCloudClient().excuteHttpRequest(mRequest);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return resp;
	}

	protected HttpUriRequest fetchPost(List<NameValuePair> params)
			throws UnsupportedEncodingException {
		HttpPost post = new HttpPost(getUrl());
		post.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
		return post;
	}

	protected HttpUriRequest fetchGet(List<NameValuePair> params)
			throws UnsupportedEncodingException {
		StringBuilder sb = new StringBuilder(getUrl());
		sb.append("?");
		for (NameValuePair param : params) {
			try {
				sb.append(URLEncoder.encode(param.getName(), HTTP.UTF_8));
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			sb.append("=");
			try {
				if (param != null) {
					sb.append(URLEncoder.encode(param.getValue(), HTTP.UTF_8));
				}

			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} catch (NullPointerException e) {
				throw new UnsupportedEncodingException();
			}
			sb.append("&");
		}
		sb.deleteCharAt(sb.length() - 1);
		LogUtils.d(TAG, "get url:" + sb.toString());

		HttpGet get = new HttpGet(sb.toString());
		return get;
	}

	protected void logPostParams(String url, List<NameValuePair> params) {
		LogUtils.d(TAG, "url: " + url);
		StringBuilder sb = new StringBuilder();
		for (NameValuePair param : params) {
			sb.append(param.getName() + ":" + param.getValue() + ", ");
		}
		LogUtils.d(TAG, "params: " + sb.toString());
	}

	@Override
	public void call(byte[] rst, Object tag) {
		final OnResponseListener l = mOnResponseListener;
		if (l != null) {
			HttpResponse resp = getResponse(rst, tag);
			if (resp instanceof HttpJSONResponse
					&& resp.error == HttpResponse.SESSION_KEY_INVALID) {
				// reconnect(mRequest);
			} else {
				l.onGetResponse(resp);
				mOnResponseListener = null;
			}

		}
	}

	protected void fillParams(List<NameValuePair> params) {
		// String language = Locale.getDefault().getLanguage();
		// if(!TextUtils.isEmpty(language)){
		// params.add(new BasicNameValuePair("written_language", language));
		// }
	}

	abstract protected String getUrl();

	abstract protected HttpResponse getResponse(byte[] rst, Object tag);

	abstract protected int postOrGet();

	public interface OnResponseListener {
		public void onGetResponse(HttpResponse resp);
	}
}
