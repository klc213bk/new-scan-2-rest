package com.tgl.newscan2rest;

public class StarterConst {

	private static final String ATTR_APP_TYPE = "com.tgl.scan.app.type";
	private static final String ATTR_APP_ID = "com.tgl.scan.app.id";
	private static final String ATTR_APP_SCHEME_ID = "com.tgl.scan.app.scheme.id";

	public static String APP_TYPE;
	public static String APP_ID;
	public static String APP_SCHEME_ID;
	public static String APP_TITLE;

	static {
		APP_TYPE = System.getProperty(ATTR_APP_TYPE, "prod");
		APP_ID = System.getProperty(ATTR_APP_ID, "TGL-Scan");
		APP_SCHEME_ID = System.getProperty(ATTR_APP_SCHEME_ID, "tgl-scan");
		APP_TITLE = "新版影像掃描";
		if ("dev".equals(APP_TYPE)) {
			APP_TITLE += "開發版";
		} else if ("uat".equals(APP_TYPE)) {
			APP_TITLE += "測試版";
		}
	}

}
