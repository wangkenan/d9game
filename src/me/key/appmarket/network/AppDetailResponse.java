package me.key.appmarket.network;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class AppDetailResponse extends HttpJSONResponse {

	private String idx;
	/** 应用名称 */
	private String appName;
	/** 应用包名 */
	private String appPackageName;
	/** 应用图标url */
	private String appIconUrl;
	/** 应用大小 */
	private String appSize;
	/** 应用类型1：1-应用软件 2-游戏软件 */
	private String appType1;
	/**
	 * 应用类型2: 应用类型1=1，则 1-系统安全 2-壁纸美化 3-聊天通讯 4-生活实用 5-书籍阅读 6-学习办公 7-其他软件
	 * 应用类型1=2，则 1-休闲益智 2-角色冒险 3-动作格斗 4-策略游戏 5-飞行射击 6-体育竞速 7-其他游戏
	 * */
	private String appType2;
	/** 下载链接 */
	private String appUrl;
	/** 应用描述 */
	private String appDes;
	/** 下载次数 */
	private String appDownloadCounts;
	/** 描述图片，多张 */
	private String[] appImgUrl;
	private String appKey;
	private String appPageUrl;
	/** 应用版本号 */
	private String appVersion;
	/** 更新时间 */
	private String appUpdateTime;

	public AppDetailResponse(byte[] rst, Object tag) {
		super(rst, tag);
	}

	@Override
	protected void parse(JSONObject json) throws JSONException {

		setIdx(json.getString("idx"));
		setAppName(json.getString("appname"));
		setAppPackageName(json.getString("apppkgname"));
		setAppIconUrl(json.getString("appiconurl"));
		setAppSize(json.getString("appsize"));
		setAppType1(json.getString("apptype1"));
		setAppType2(json.getString("apptype2"));
		setAppUrl(json.getString("appurl"));
		setAppDes(json.getString("appdes"));
		setAppDownloadCounts(json.getString("appdowncount"));
		JSONArray array = json.getJSONArray("appimgurl");
		List<String> list = new ArrayList<String>();
		if (array != null && array.length() > 0) {
			for (int i = 0; i < array.length(); i++) {
				JSONArray array1 = array.getJSONArray(i);
				for (int j = 0; j < array1.length(); j++) {
					list.add(array1.getString(j));
				}
			}

			String[] result = new String[list.size()];
			for (int i = 0; i < list.size(); i++) {
				result[i] = list.get(i);
			}
			setAppImgUrl(result);
		}

		setAppKey(json.getString("appkey"));
		setAppPageUrl(json.getString("pageurl"));
		setAppVersion(json.getString("version"));
		setAppUpdateTime(json.getString("updatetime"));

	}

	public String getIdx() {
		return idx;
	}

	public void setIdx(String idx) {
		this.idx = idx;
	}

	public String getAppName() {
		return appName;
	}

	public void setAppName(String appName) {
		this.appName = appName;
	}

	public String getAppPackageName() {
		return appPackageName;
	}

	public void setAppPackageName(String appPackageName) {
		this.appPackageName = appPackageName;
	}

	public String getAppIconUrl() {
		return appIconUrl;
	}

	public void setAppIconUrl(String appIconUrl) {
		this.appIconUrl = appIconUrl;
	}

	public String getAppSize() {
		return appSize;
	}

	public void setAppSize(String appSize) {
		this.appSize = appSize;
	}

	public String getAppType1() {
		return appType1;
	}

	public void setAppType1(String appType1) {
		this.appType1 = appType1;
	}

	public String getAppType2() {
		return appType2;
	}

	public void setAppType2(String appType2) {
		this.appType2 = appType2;
	}

	public String getAppUrl() {
		return appUrl;
	}

	public void setAppUrl(String appUrl) {
		this.appUrl = appUrl;
	}

	public String getAppDes() {
		return appDes;
	}

	public void setAppDes(String appDes) {
		this.appDes = appDes;
	}

	public String getAppDownloadCounts() {
		return appDownloadCounts;
	}

	public void setAppDownloadCounts(String appDownloadCounts) {
		this.appDownloadCounts = appDownloadCounts;
	}

	public String[] getAppImgUrl() {
		return appImgUrl;
	}

	public void setAppImgUrl(String[] appImgUrl) {
		this.appImgUrl = appImgUrl;
	}

	public String getAppKey() {
		return appKey;
	}

	public void setAppKey(String appKey) {
		this.appKey = appKey;
	}

	public String getAppPageUrl() {
		return appPageUrl;
	}

	public void setAppPageUrl(String appPageUrl) {
		this.appPageUrl = appPageUrl;
	}

	public String getAppVersion() {
		return appVersion;
	}

	public void setAppVersion(String appVersion) {
		this.appVersion = appVersion;
	}

	public String getAppUpdateTime() {
		return appUpdateTime;
	}

	public void setAppUpdateTime(String appUpdateTime) {
		this.appUpdateTime = appUpdateTime;
	}

}
