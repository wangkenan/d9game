package me.key.appmarket.network;

import java.util.List;

import me.key.appmarket.utils.Global;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

public class CheckVersionRequest extends HttpRequestWithToken {
	private String mVersion;

	public CheckVersionRequest(String version) {
		mVersion = version;
	}

	@Override
	protected String getUrl() {
		return Global.MAIN_URL + Global.APPUPGRADE;
	}

	@Override
	protected void fillParams(List<NameValuePair> params) {
		super.fillParams(params);
		params.add(new BasicNameValuePair("version", mVersion));
	}

	@Override
	protected HttpResponse getResponse(byte[] rst, Object tag) {
		return new CheckVersionResponse(rst, tag);
	}

	@Override
	protected int postOrGet() {
		return HTTP_GET;
	}

}
