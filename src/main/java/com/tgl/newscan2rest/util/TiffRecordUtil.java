package com.tgl.newscan2rest.util;

import com.tgl.newscan2rest.bean.TiffField;
import com.tgl.newscan2rest.bean.TiffRecord;

public class TiffRecordUtil {

	public static TiffField getTiffField(TiffRecord record, String fieldName) {
		TiffField tiffField = null;
		for (TiffField tf: record.getFields()) {
			if (tf.getName().equals(fieldName)) {
				tiffField = tf;
				break;
			}
		}
		return tiffField;
	}
}
