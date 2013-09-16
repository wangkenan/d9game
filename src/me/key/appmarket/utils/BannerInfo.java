package me.key.appmarket.utils;

public class BannerInfo {
	private String id;
	private String title;
	private String picurl;
	private String linkurl;
	private String appID;

	public BannerInfo(String id, String title, String picurl, String linkurl,
			String appID) {
		this.id = id;
		this.title = title;
		this.picurl = picurl;
		this.linkurl = linkurl;
		this.appID = appID;
	}

	public String getAppID() {
		return appID;
	}

	public void setAppID(String appID) {
		this.appID = appID;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getPicurl() {
		return picurl;
	}

	public void setPicurl(String picurl) {
		this.picurl = picurl;
	}

	public String getLinkurl() {
		return linkurl;
	}

	public void setLinkurl(String linkurl) {
		this.linkurl = linkurl;
	}
}
