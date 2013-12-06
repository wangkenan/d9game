package me.key.appmarket.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;

public class FileIOUtil {
	public static ArrayList<AppInfo> readFileByLines(String fileName) {
		File file = new File(fileName);
		BufferedReader reader = null;
		ArrayList<AppInfo> list = new ArrayList<AppInfo>();
		try {
			System.out.println("以行为单位读取文件内容，一次读一整行：");
			reader = new BufferedReader(new FileReader(file));
			String tempString = null;
			int line = 1;
			// 一次读入一行，直到读入null为文件结束
			while ((tempString = reader.readLine()) != null) {
				// 显示行号
				System.out.println("line " + line + ": " + tempString);
				line++;
				AppInfo appInfo = new AppInfo();
				appInfo.setPackageName(tempString);
				list.add(appInfo);
			}
			reader.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e1) {
				}
			}
		}
		return list;
	}
	public static void writeFileByLines(List<AppInfo> appList,Context context) {
		try {
			PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(LocalUtils.getRoot(context)+"localGames.txt")),true); 
			for(AppInfo appInfo :appList) {
				String packageName = appInfo.getPackageName();
				pw.println(packageName); 
				pw.flush();
			}
			pw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}  
	}
}
