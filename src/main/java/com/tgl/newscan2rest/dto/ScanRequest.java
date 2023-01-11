package com.tgl.newscan2rest.dto;

public class ScanRequest {
	private String sourceName;
	private String colorMode;
	private String duplexMode;
	private boolean queryFromPage;
	private String lastScanOrder;
	
	public String getSourceName() {
		return sourceName;
	}
	public void setSourceName(String sourceName) {
		this.sourceName = sourceName;
	}
	public String getColorMode() {
		return colorMode;
	}
	public void setColorMode(String colorMode) {
		this.colorMode = colorMode;
	}
	public String getDuplexMode() {
		return duplexMode;
	}
	public void setDuplexMode(String duplexMode) {
		this.duplexMode = duplexMode;
	}
	public boolean isQueryFromPage() {
		return queryFromPage;
	}
	public void setQueryFromPage(boolean queryFromPage) {
		this.queryFromPage = queryFromPage;
	}
	public String getLastScanOrder() {
		return lastScanOrder;
	}
	public void setLastScanOrder(String lastScanOrder) {
		this.lastScanOrder = lastScanOrder;
	}
	
}
