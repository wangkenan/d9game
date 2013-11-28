package me.key.appmarket.utils;

public class Info {

	private boolean is_Checked;
	private String number;
//	private String name;
	public Info (boolean b, String n)
	{
		this.is_Checked = b;
		this.number = n;
	}
	public boolean isIs_Checked() {
		return is_Checked;
	}
	public void setIs_Checked(boolean is_Checked) {
		this.is_Checked = is_Checked;
	}
	public String getNumber() {
		return number;
	}
	public void setNumber(String number) {
		this.number = number;
	}
	
	
}
