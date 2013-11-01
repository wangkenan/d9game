package me.key.appmarket.tool;

import java.io.File;
import java.io.Serializable;

import me.key.appmarket.utils.LogUtils;
import net.tsz.afinal.FinalHttp;
import net.tsz.afinal.http.AjaxCallBack;
import net.tsz.afinal.http.HttpHandler;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Message;

public class DownLoad implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Context context;
	private File tempFile;
	private String url;
	
	public DownLoad(Context context, File tempFile, String url) {
		super();
		this.context = context;
		this.tempFile = tempFile;
		this.url = url;
	}

	public void downFinle() {
		
	}
}
