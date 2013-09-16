package me.key.appmarket.ImageNet;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;

/***
 * 异步加载图片 缓存的实现
 * 
 * @author jia
 * 
 */
public class AsyncImageLoader {
	// 软引用
	private HashMap<String, SoftReference<Drawable>> imageCache;

	public AsyncImageLoader() {
		imageCache = new HashMap<String, SoftReference<Drawable>>();
	}

	/***
	 * 下载图片
	 * 
	 * @param imageUrl
	 *            图片地址
	 * @param imageCallback
	 *            回调接口
	 * @return
	 */
	public Drawable loadDrawable(final String imageUrl,
			final ImageCallback imageCallback) {
		if (imageCache.containsKey(imageUrl)) {
			SoftReference<Drawable> softReference = imageCache.get(imageUrl);
			Drawable drawable = softReference.get();
			if (drawable != null) {
				return drawable;
			}
		}
		final Handler handler = new Handler() {
			public void handleMessage(Message message) {
				imageCallback.imageLoaded((Drawable) message.obj, imageUrl);
			}
		};
		// 开启线程下载图片
		new Thread() {
			@Override
			public void run() {
				Drawable drawable = loadImageFromUrl(imageUrl);
				// 将下载的图片保存至缓存中
				imageCache.put(imageUrl, new SoftReference<Drawable>(drawable));
				Message message = handler.obtainMessage(0, drawable);
				handler.sendMessage(message);
			}
		}.start();
		return null;
	}

	/***
	 * 根据URL下载图片（这里要进行判断，先去本地sd中查找，没有则根据URL下载，有则返回该drawable）
	 * 
	 * @param url
	 * @return
	 */
	public static Drawable loadImageFromUrl(String imageURL) {
		URI mURL;
		Drawable drawable = null;
		try {
			mURL = new URI(imageURL);
			Bitmap bitmap = getBitmap(mURL);
			drawable = new BitmapDrawable(bitmap);
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		return drawable;
	}

	// 回调接口
	public interface ImageCallback {
		public void imageLoaded(Drawable imageDrawable, String imageUrl);
	}

	public static Bitmap getBitmap(URI imageUrl) {
		Bitmap bitmap = null;
		// httpGet连接对象
		HttpGet httpRequest = new HttpGet(imageUrl);
		// 取得HttpClient 对象
		HttpClient httpclient = new DefaultHttpClient();
		try {
			// 请求httpClient ，取得HttpRestponse
			HttpResponse httpResponse = httpclient.execute(httpRequest);
			if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				// 取得相关信息 取得HttpEntiy
				HttpEntity httpEntity = httpResponse.getEntity();
				// 获得一个输入流
				InputStream is = httpEntity.getContent();
				bitmap = BitmapFactory.decodeStream(is);
				is.close();
			}

		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return bitmap;
	}
}