package me.key.appmarket.utils;

import java.util.Comparator;

public class PinyinComparator implements Comparator{

	@Override
	public int compare(Object o1, Object o2) {
		if(o1 == null)
			o1 = "#";
		if (o2 == null)
			o2 = "#";
		 String str1 = PingYinUtil.getPingYin((String) o1);
	     String str2 = PingYinUtil.getPingYin((String) o2);
	     return str1.compareTo(str2);
	}

}
