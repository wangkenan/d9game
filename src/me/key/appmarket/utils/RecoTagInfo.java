package me.key.appmarket.utils;

/**
 * 标签个体封装类
 * 
 * @author yutinglong
 */
public class RecoTagInfo {

	private String text_name;
	private int tagid;
	private String tagtypeObj;

	public RecoTagInfo(String text_name, int tagid, String tagtypeObj) {
		this.text_name = text_name;
		this.tagid = tagid;
		this.tagtypeObj = tagtypeObj;
	}

	public String getText_name() {
		return text_name;
	}

	public void setText_name(String text_name) {
		this.text_name = text_name;
	}

	public int getTagid() {
		return tagid;
	}

	public void setTagid(int tagid) {
		this.tagid = tagid;
	}

}
