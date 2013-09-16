package me.key.appmarket.utils;

public class CategoryInfo {
	private String id;
	private String name;
	private String type1;
	private String type2;
	private String appIcon;

	public CategoryInfo(String id, String name, String type1, String type2,
			String appIcon) {
		this.id = id;
		this.name = name;
		this.type1 = type1;
		this.type2 = type2;
		this.appIcon = appIcon;
	}

	public String getAppIcon() {
		return appIcon;
	}

	public void setAppIcon(String appIcon) {
		this.appIcon = appIcon;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType1() {
		return type1;
	}

	public void setType1(String type1) {
		this.type1 = type1;
	}

	public String getType2() {
		return type2;
	}

	public void setType2(String type2) {
		this.type2 = type2;
	}

}
