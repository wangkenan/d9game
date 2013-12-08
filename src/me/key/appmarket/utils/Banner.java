package me.key.appmarket.utils;

import java.io.Serializable;

public class Banner implements Serializable{
	private String picurl;
	private String appid;
	public String getPicurl() {
		return picurl;
	}
	public void setPicurl(String picurl) {
		this.picurl = picurl;
	}
	public String getAppid() {
		return appid;
	}
	public void setAppid(String appid) {
		this.appid = appid;
	}
}
