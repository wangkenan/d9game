package me.key.appmarket.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * 标签封装类
 * 
 * @author yutinglong
 */
public class RecoTags {

	private String type_name;
	private List<RecoTagInfo> tagLists;

	public RecoTags(String type_name, List<RecoTagInfo> tagLists) {
		this.type_name = type_name;
		this.tagLists = tagLists;
	}

	public String getType_name() {
		return type_name;
	}

	public void setType_name(String type_name) {
		this.type_name = type_name;
	}

	public List<RecoTagInfo> getTagLists() {
		return tagLists;
	}

	public void setTagLists(List<RecoTagInfo> tagLists) {
		this.tagLists = tagLists;
	}

}
