package com.tgl.newscan2rest.bean;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ImportRecord {

	private String importFilePath;
	
	private boolean queryFromPage;

	public String getImportFilePath() {
		return importFilePath;
	}

	public void setImportFilePath(String importFilePath) {
		this.importFilePath = importFilePath;
	}

	public boolean isQueryFromPage() {
		return queryFromPage;
	}

	public void setQueryFromPage(boolean queryFromPage) {
		this.queryFromPage = queryFromPage;
	}


}
