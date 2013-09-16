package me.key.appmarket.network;

abstract public class HttpResponse {

	public static final int OK = 0;
	public static final int NETWORK_ERROR = -1;
	public static final int LOGIN_ERROR = -2;

	public static final int UNLAWFULNESS_WORD_ERROR = 106;

	public static final int SESSION_KEY_INVALID = 110;

	public static final int FAILED = 88;
	public final String SUCCEDD_RESULT = "ok";
	public int error;
	public String errorMsg;
	protected Object mTag;

	public HttpResponse(byte[] rst, Object tag) {
		if (rst == null) {
			error = NETWORK_ERROR;
			return;
		}
		mTag = tag;
	}

}
