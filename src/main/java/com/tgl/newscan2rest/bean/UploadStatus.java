package com.tgl.newscan2rest.bean;

public class UploadStatus {

	private Integer code;
	private String description;

	public UploadStatus(String response) {
		super();
		int idx = response.indexOf("^");
		if (idx<0) {
			this.code = null;
			this.description = description;
		} else {
			this.code = Integer.valueOf(response.substring(0, idx));
			this.description = response.substring(idx+1, response.length());
		}
	}

	public UploadStatus(Integer code, String description) {
		super();
		this.code = code;
		this.description =description;
	}

	public Integer getCode() {
		return code;
	}

	public void setCode(Integer code) {
		this.code = code;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public String toString() {
		return "UploadStatus [code=" + code + ", description=" + description + "]";
	}

}
