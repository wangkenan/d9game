package me.key.appmarket.network;

import java.util.List;

import org.apache.http.NameValuePair;

public abstract class HttpRequestWithToken extends HttpRequest {

	@Override
	protected void fillParams(List<NameValuePair> params) {
		super.fillParams(params);
	}
}
