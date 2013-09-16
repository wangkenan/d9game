package me.key.appmarket.network;

import java.util.List;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import me.key.appmarket.utils.Global;

public class AppDetailRequest extends HttpRequestWithToken {

	private String mAppid;

	public AppDetailRequest(String appid) {
		this.mAppid = appid;
	}

	@Override
	protected String getUrl() {
		return Global.MAIN_URL + Global.APP_DETAIL;
	}

	@Override
	protected HttpResponse getResponse(byte[] rst, Object tag) {
		return new AppDetailResponse(rst, tag);
	}

	@Override
	protected int postOrGet() {
		return HTTP_GET;
	}

	@Override
	protected void fillParams(List<NameValuePair> params) {
		super.fillParams(params);
		params.add(new BasicNameValuePair("appid", mAppid));
	}

}
