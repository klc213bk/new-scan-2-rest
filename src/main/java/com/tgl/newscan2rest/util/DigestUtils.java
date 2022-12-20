package com.tgl.newscan2rest.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class DigestUtils {

	private static final Logger logger = LoggerFactory.getLogger(DigestUtils.class);

	public static String getXmlFileMD5(String fileName) {
		String md5 = null;
		try (InputStream is = Files.newInputStream(Paths.get(fileName))) {
		    md5 = org.apache.commons.codec.digest.DigestUtils.md5Hex(is);
		} catch (IOException e) {
			logger.error("getXmlFileMD5 錯誤", e);
		}
		return md5;
	}

	public static String base64Encode(String text) {
		Base64.Encoder encoder = Base64.getEncoder();
		byte[] textByte = text.getBytes(StandardCharsets.US_ASCII);
		String encodedText = encoder.encodeToString(textByte);
		return encodedText;
	}

	public static String base64Decode(String encodedText) {
		Base64.Decoder decoder = Base64.getDecoder();
		String text = new String(decoder.decode(encodedText), StandardCharsets.US_ASCII);
		return text;
	}

	public static byte[] getBytes(String text) {
		byte[] textByte = text.getBytes(StandardCharsets.US_ASCII);
		return textByte;
	}

	public static byte[] getBytes(byte[] bytes, int offset) {
		byte[] textByte = new byte[bytes.length/4+1];
		int max=textByte.length-1;
		for ( int i=0; i<max; i++) {
			textByte[i] = bytes[i*4+offset];
		}
		textByte[max] = 61;
		return textByte;
	}

	public static String getText(byte[] textByte) {
		String text = new String(textByte, StandardCharsets.US_ASCII);
		return text;
	}

}
