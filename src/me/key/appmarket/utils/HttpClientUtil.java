package me.key.appmarket.utils;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

/**
 * HttpClient������
 * 
 * @author Administrator
 * 
 */
public class HttpClientUtil {

	private static InputStream is;

	public static InputStream getInputStream(Map<String, String> params,
			String url) {
		HttpClient hc = new DefaultHttpClient();
		HttpPost post = new HttpPost(url);
		List<NameValuePair> parameters = new ArrayList<NameValuePair>();
		if (params != null) {
			Set<Entry<String, String>> entrySet = params.entrySet();
			Iterator<Entry<String, String>> iterator = entrySet.iterator();
			while (iterator.hasNext()) {
				Entry<String, String> next = iterator.next();
				String key = next.getKey();
				String value = next.getValue();
				NameValuePair nv1 = new BasicNameValuePair(key, value);
				parameters.add(nv1);

			}
		}
		try {
			post.setEntity(new UrlEncodedFormEntity(parameters));
			HttpResponse response = hc.execute(post);
			int code = response.getStatusLine().getStatusCode();
			if (code == 200) {
				is = response.getEntity().getContent();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return is;
	}

	/**
	 * ������������ַ�
	 * 
	 * @return
	 */
	public static String getString(InputStream is) {
		if (is != null) {
			BufferedInputStream bis = new BufferedInputStream(is);
			byte[] buffer = new byte[1024];
			int len = 0;
			StringBuilder builder = new StringBuilder();
			try {
				while ((len = bis.read(buffer)) != -1) {
					builder.append(new String(buffer, 0, len));
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			return builder.toString();
		} else {
			return null;
		}
	}
}
