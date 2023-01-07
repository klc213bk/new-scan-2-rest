package com.tgl.newscan2rest.service;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asprise.imaging.core.Result;
import com.asprise.imaging.core.ResultImageItem;
import com.tgl.newscan2rest.bean.ScanConfig;
import com.tgl.newscan2rest.bean.TiffRecord;
import com.tgl.newscan2rest.util.ScanUtil;

public class ScanResultConvertService {
	static final Logger logger = LoggerFactory.getLogger(ScanResultConvertService.class);
			
	public static List<TiffRecord> converToTiffRecords(Integer idx,  Result result, boolean queryFromPage, ScanConfig scanConfig) {
		List<TiffRecord> tiffRecords = new ArrayList<>();
		int i = idx.intValue();
		File acquireFile = result.getImageFiles().get(i);
		if (logger.isDebugEnabled()) {
			logger.debug("Acquire image {}", acquireFile.getName());
		}
		
		ResultImageItem imageItem = result.getImageItems().get(i);
		double inkCoverage = imageItem.getInkCoverage();
		BufferedImage image = null;
		String barcodes = null;

		if (inkCoverage >= ScanUtil.BLANK_PAGE_THRESHOLD) {
			try {
				image = result.getImage(i);
			} catch (Exception e) {
				String errorMessage = String.format("影像檔讀取失敗 %s ！", acquireFile.getName());
				logger.error(errorMessage, e);
			}
		} else {
			logger.debug("inkCoverage={} is less than {}, is a blank page!", inkCoverage, ScanUtil.BLANK_PAGE_THRESHOLD);
		}
		if ( image != null ) {
			List<Map<String, Object>> barcodeList = imageItem.getBarcodes();
			Map<String, Object> extAttributes = imageItem.getExtendedImageAttrs();
			barcodes = ScanUtil.getBarcodes(barcodeList, extAttributes);

			List<TiffRecord> records = ScanUtil.saveToFiles(barcodes, acquireFile, image, "01", queryFromPage, scanConfig);
			if (records != null && records.size() > 0) {
				tiffRecords.addAll(records);
			}
		}

		// 移除掃描暫存檔
		try {
			Files.delete(acquireFile.toPath());
		} catch (IOException e) {
			logger.error("移除掃描暫存檔錯誤", e);
		}
		
	    return tiffRecords;
	}
}
