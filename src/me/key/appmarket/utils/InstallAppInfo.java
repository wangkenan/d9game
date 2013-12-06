package me.key.appmarket.utils;

import android.graphics.drawable.Drawable;

public class InstallAppInfo {
		private String idx;
		private String appName;
		private String appSize;
		private String iconUrl;
		private String appDescri;
		private String appUrl;
		private String appDownCount;
		private String id;
		private Long lastTime;
		private String pkgname;
		private Drawable drawable;
		public Drawable getDrawable() {
			return drawable;
		}
		public void setDrawable(Drawable drawable) {
			this.drawable = drawable;
		}
		public InstallAppInfo(AppInfo appInfo) {
			super();
			this.idx = appInfo.getIdx();
			this.appName = appInfo.getAppName();
			this.appSize = appInfo.getAppSize();
			this.iconUrl = appInfo.getIconUrl();
			this.appDescri = appInfo.getAppDescri();
			this.appUrl = appInfo.getAppUrl();
			this.appDownCount = appInfo.getAppDownCount();
			this.id = appInfo.getId();
			this.lastTime = appInfo.getLastTime();
			this.pkgname = appInfo.getPackageName();
		}
		public String getPkgname() {
			return pkgname;
		}
		public void setPkgname(String pkgname) {
			this.pkgname = pkgname;
		}
		public InstallAppInfo() {
			super();
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
		public String getAppSize() {
			return appSize;
		}
		public void setAppSize(String appSize) {
			this.appSize = appSize;
		}
		public String getIconUrl() {
			return iconUrl;
		}
		public void setIconUrl(String iconUrl) {
			this.iconUrl = iconUrl;
		}
		public String getAppDescri() {
			return appDescri;
		}
		public void setAppDescri(String appDescri) {
			this.appDescri = appDescri;
		}
		public String getAppUrl() {
			return appUrl;
		}
		public void setAppUrl(String appUrl) {
			this.appUrl = appUrl;
		}
		public String getAppDownCount() {
			return appDownCount;
		}
		public void setAppDownCount(String appDownCount) {
			this.appDownCount = appDownCount;
		}
		public String getId() {
			return id;
		}
		public void setId(String id) {
			this.id = id;
		}
		public Long getLastTime() {
			return lastTime;
		}
		public void setLastTime(Long lastTime) {
			this.lastTime = lastTime;
		}
	}
