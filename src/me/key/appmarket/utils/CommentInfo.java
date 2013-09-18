package me.key.appmarket.utils;

public class CommentInfo {
	private String content;
	private String send_time;
	private String user_name;
	private String score;

	public CommentInfo(String content, String send_time, String user_name,
			String score) {
		this.content = content;
		this.send_time = send_time;
		this.user_name = user_name;
		this.score = score;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getSend_time() {
		return send_time;
	}

	public void setSend_time(String send_time) {
		this.send_time = send_time;
	}

	public String getUser_name() {
		return user_name;
	}

	public void setUser_name(String user_name) {
		this.user_name = user_name;
	}

	public String getScore() {
		return score;
	}

	public void setScore(String score) {
		this.score = score;
	}

}
