package com.tgl.newscan2rest.service;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import com.tgl.newscan2rest.Constant;
import com.tgl.newscan2rest.bean.BatchDeleteBean;
import com.tgl.newscan2rest.bean.CopyRecord;
import com.tgl.newscan2rest.bean.DeleteRecord;
import com.tgl.newscan2rest.bean.ImageRecordSet;
import com.tgl.newscan2rest.bean.LoginStatus;
import com.tgl.newscan2rest.bean.ScannedImage;
import com.tgl.newscan2rest.bean.TiffField;
import com.tgl.newscan2rest.bean.TiffRecord;
import com.tgl.newscan2rest.dto.LoginRequest;
import com.tgl.newscan2rest.util.ImageRecordHelper;
import com.tgl.newscan2rest.util.ImageUtil;
import com.tgl.newscan2rest.util.TiffRecordUtil;

import jakarta.xml.bind.JAXBException;

@Service
public class CopyService {

	private static final Logger logger = LogManager.getLogger(CopyService.class);

	public void copy(CopyRecord copyRecord) throws Exception {
		ImageRecordHelper recordSetHelper = ImageRecordHelper.getInstance();
		ImageRecordSet recordSet = recordSetHelper.unmarshalFromFile();
		List<TiffRecord> recordList = recordSet.getRecords().getRecordList();
		int selectedIndex = copyRecord.getSelectedIndex();
		TiffRecord tiffRecord = recordList.get(selectedIndex);
		
		ScannedImage copyItem = recordSetHelper.convert(tiffRecord);

		String errorMessage = null;
		Path newFilePath = null;
		TiffRecord newTiffRecord = null;
		
		String fileName = copyItem.fileNameProperty().getValue().toLowerCase();
		String copyFileName = "_copy" + Constant.FILE_EXT_TIFF;
		if ( fileName.endsWith(Constant.FILE_EXT_JPG) || fileName.endsWith(Constant.FILE_EXT_JPEG) ) {
			copyFileName = "_copy" + Constant.FILE_EXT_JPG;
		}
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
		String newFileName = sdf.format(new Date())+copyFileName;
		String newFileURL = ImageRecordHelper.IMAGE_ARCHIVE_DIR + File.separator + newFileName;

		try {
			newFilePath = ImageUtil.copyImageItem(copyItem, newFileURL);
		} catch (NoSuchFileException e) {
			errorMessage = String.format("複製失敗！找不到影像檔 %s 。", copyItem.fileNameProperty().getValue());
			logger.error(errorMessage, e);
		} catch (IOException e) {
			errorMessage = String.format("無法複製影像檔 %s ！", copyItem.fileNameProperty().getValue());
			logger.error(errorMessage, e);
		}
		
		if (null == errorMessage) {
			// 複製記錄並回寫 XML 檔案
    		try {
    			newTiffRecord = recordSetHelper.cloneTiffRecord(tiffRecord, newFilePath.getFileName().toString());
    			recordList.add(selectedIndex+1, newTiffRecord);
    			recordSetHelper.marshalToFile(recordSet);
    		} catch (IllegalAccessException e) {
				errorMessage = "影像設定讀取失敗！";
				logger.error(errorMessage, e);
			} catch (InvocationTargetException e) {
				errorMessage = "影像設定抄寫失敗！";
				logger.error(errorMessage, e);
    		} catch (JAXBException e) {
				errorMessage = String.format("無法儲存設定檔 %s ！", ImageRecordHelper.RECORD_SET_FILE_NAME);
				logger.error(errorMessage, e);
			}
		}

		if (null != errorMessage) {
			throw new Exception(errorMessage);
		}

	}
	
}


