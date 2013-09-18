package me.key.appmarket.tool;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.util.Log;

public class ToolHelper {
	public static boolean isExist(String path) {
		File file = new File(path);
		// 判断文件夹是否存在,如果不存在则创建文件夹
		if (!file.exists()) {
			return false;
		}
		return true;
	}

	public static String getPath() {
		try {
			Runtime runtime = Runtime.getRuntime();
			Process proc = runtime.exec("mount");
			InputStream is = proc.getInputStream();
			InputStreamReader isr = new InputStreamReader(is);
			String line;
			String mount = new String();
			BufferedReader br = new BufferedReader(isr);
			while ((line = br.readLine()) != null) {
				if (line.contains("secure"))
					continue;
				if (line.contains("asec"))
					continue;
				if (line.contains("fat")) {
					String columns[] = line.split(" ");
					if (columns != null && columns.length > 1) {
						mount = mount.concat(columns[1]);
					}
				}
			}
			return mount;

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "";
	}

	public static String donwLoadToString(String urlStr) {
		StringBuffer sb = new StringBuffer();
		BufferedReader bReader = null;
		String line = null;
		Log.e("tag", "url = " + urlStr);
		try {
			InputStream inputStream = postIsFromUrl(urlStr);
			if (inputStream == null) {
				return "";
			}
			bReader = new BufferedReader(new InputStreamReader(inputStream,
					"GBK"));
			while ((line = bReader.readLine()) != null) {
				sb.append(line);
			}
			bReader.close();
			return sb.toString();
		} catch (Exception e) {
			Log.e("tag", "error = " + e.getMessage());
			return "";
		}
	}

	/*
	 * 　　* 获取当前程序的版本号 　　
	 */
	public static String getVersionName(Context context) {
		try {
			PackageManager packageManager = context.getPackageManager();
			PackageInfo packInfo = packageManager.getPackageInfo(
					context.getPackageName(), 0);
			return packInfo.versionName;
		} catch (Exception e) {
			// TODO: handle exception
			return "";
		}
	}

	public static InputStream postIsFromUrl(String urlStr) {
		try {
			URL url = new URL(urlStr);
			HttpURLConnection urlConnection = (HttpURLConnection) url
					.openConnection();
			urlConnection.setConnectTimeout(5000);
			urlConnection.setRequestMethod("POST");
			return urlConnection.getInputStream();
		} catch (Exception ex) {
			Log.e("tag", "error = " + ex.getMessage());
			return null;
		}
	}

	public static InputStream getIsFromUrl(String urlStr) {
		try {
			URL url = new URL(urlStr);
			HttpURLConnection urlConnection = (HttpURLConnection) url
					.openConnection();
			urlConnection.setConnectTimeout(5000);
			urlConnection.setRequestMethod("GET");
			return urlConnection.getInputStream();
		} catch (Exception ex) {
			Log.e("tag", "error = " + ex.getMessage());
			return null;
		}
	}

	public static Uri getImageURI(String path, File cache) throws Exception {
		String name = MD5.getMD5(path) + path.substring(path.lastIndexOf("."));
		File file = new File(cache, name);
		if (file.exists()) {
			return Uri.fromFile(file);
		} else {
			URL url = new URL(path);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setConnectTimeout(5000);
			conn.setRequestMethod("GET");
			conn.setDoInput(true);
			if (conn.getResponseCode() == 200) {
				InputStream is = conn.getInputStream();
				FileOutputStream fos = new FileOutputStream(file);
				byte[] buffer = new byte[1024];
				int len = 0;
				while ((len = is.read(buffer)) != -1) {
					fos.write(buffer, 0, len);
				}
				is.close();
				fos.close();
				return Uri.fromFile(file);
			}
		}
		return null;
	}

	@SuppressLint("SimpleDateFormat")
	public static String time2Str() {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMDDHHmmss");
		Date curDate = new Date(System.currentTimeMillis());
		return formatter.format(curDate);
	}

	public static String Kb2Mb(String kbString) {
		StringBuilder sb = new StringBuilder();
		int b = Integer.parseInt(kbString);
		if (b > 1024) {
			int kb = b / 1024;
			if (kb > 1024) {
				int mb = kb / 1024;
				kb = (kb - mb * 1024) * 100 / 1024;
				sb.append(mb + "." + kb + "MB");
			} else {
				sb.append(kb + "KB");
			}
		} else {
			sb.append(b + "B");
		}
		return sb.toString();
	}

	public static boolean netIsAvail(Context context) {
		ConnectivityManager manager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (manager == null) {
			return false;
		}
		NetworkInfo networkinfo = manager.getActiveNetworkInfo();
		if (networkinfo == null || !networkinfo.isAvailable()) {
			return false;
		}
		return true;
	}

	public static void log2File(String str) {
		try {
			RandomAccessFile rAccessFile = new RandomAccessFile(
					"/sdcard/log.txt", "rw");
			long len = rAccessFile.length();
			rAccessFile.seek(len);
			rAccessFile.writeBytes(str);
			rAccessFile.writeBytes("\n");
			rAccessFile.close();
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
}
