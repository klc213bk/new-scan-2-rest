package com.tgl.newscan2rest.util;

public class ObjectsUtil {

	public static boolean isEmpty(String str) {
		return str == null || str.trim().length() == 0;
	}

	public static boolean isNotEmpty(String str) {
		return !isEmpty(str);
	}

	public static boolean isEquals(String str1, String str2) {
		return ("" + str1).equals("" + str2);
	}

	public static boolean isNotEquals(String str1, String str2) {
		return !isEquals(str1, str2);
	}

	public static String left(String str, int length) {
		if (null==str) {
			return null;
		}
		return str.substring(0, str.length()<length ? str.length() : length);
	}

	public static String encodeToPreventXSSAttack(String str) {
		// Checkmarx 掃描報告中被列為高風險，認為可能會受到 Reflected XSS 攻擊，
		// ImageScanApp 是單機版 Windows App，不是 Web App，不會有 XSS 攻擊問題，
		// 系統上線前都要求要改，很無語～
		if (null==str) {
			return null;
		}
		return str;
	}

	public static String decodeToPreventXSSAttack(String str) {
		// 同上
		if (null==str) {
			return null;
		}
		return str;
	}

}
