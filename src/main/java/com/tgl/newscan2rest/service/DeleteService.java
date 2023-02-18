package com.tgl.newscan2rest.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import com.tgl.newscan2rest.bean.BatchDeleteBean;
import com.tgl.newscan2rest.bean.DeleteRecord;
import com.tgl.newscan2rest.bean.ImageRecordSet;
import com.tgl.newscan2rest.bean.LoginStatus;
import com.tgl.newscan2rest.bean.TiffField;
import com.tgl.newscan2rest.bean.TiffRecord;
import com.tgl.newscan2rest.dto.LoginRequest;
import com.tgl.newscan2rest.util.ImageRecordHelper;
import com.tgl.newscan2rest.util.TiffRecordUtil;

import jakarta.xml.bind.JAXBException;

@Service
public class DeleteService {

	private static final Logger logger = LogManager.getLogger(DeleteService.class);

	public void delete(DeleteRecord deleteRecord) throws Exception {

		String errorMessage = null;
		Path file = Paths.get(deleteRecord.getFileURL());
		try {
			Files.delete(file);

		} catch (NoSuchFileException e) {
			errorMessage = String.format("序號:%s，影像檔 %s 不存在！", deleteRecord.getFileURL());
			logger.error(errorMessage, e);

			throw new Exception(errorMessage, e);
		} catch (IOException e) {
			errorMessage = String.format("序號:%s，無法刪除影像檔 %s ！", deleteRecord.getFileURL());
			logger.error(errorMessage, e);

			throw new Exception(errorMessage, e);
		}



		ImageRecordHelper recordSetHelper = ImageRecordHelper.getInstance();
		ImageRecordSet recordSet = recordSetHelper.unmarshalFromFile();

		// 刪除記錄並回寫 XML 檔案
		List<TiffRecord> recordList = recordSet.getRecords().getRecordList();
		int selectedIndex = deleteRecord.getDeleteIndex();
		recordList.remove(selectedIndex);
		try {
			recordSetHelper.marshalToFile(recordSet);
		} catch (JAXBException e) {
			errorMessage = String.format("無法儲存設定檔 %s ！", ImageRecordHelper.RECORD_SET_FILE_NAME);
			logger.error(errorMessage, e);

			throw new Exception(errorMessage, e);
		}

	}
	public void batchDelete(BatchDeleteBean batchDeleteBean) throws Exception{
//		logger.debug("service batchDelete is called");
		int fromScanOrderInt = 
				Integer.parseInt(batchDeleteBean.getFromScanOrder());
		int toScanOrderInt = 
				Integer.parseInt(batchDeleteBean.getToScanOrder());
	
		ImageRecordHelper recordSetHelper = ImageRecordHelper.getInstance();
		ImageRecordSet recordSet;
		try {
			recordSet = recordSetHelper.unmarshalFromFile();
		} catch (JAXBException e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		}
//		logger.debug("aaa");
		List<TiffRecord> recordList = recordSet.getRecords().getRecordList();
		ListIterator<TiffRecord> it = recordList.listIterator();
		int removeCount = 0;
		List<String> removeFailedItems = new ArrayList<>();
		while (it.hasNext()) {
			TiffRecord tr = it.next();
			TiffRecord removeRecord = null;
			Integer scanOrder = null;
			for (TiffField tf: tr.getFields()) {
				if ("ScanOrder".equals(tf.getName())) {
					scanOrder = Integer.parseInt(tf.getValue());
					if (scanOrder < fromScanOrderInt || scanOrder > toScanOrderInt) {
						// do nothing
					} else {
						removeRecord = tr;
					}
					break;
				}
			}
			if (removeRecord != null) {
				TiffField imageSaveDirField = TiffRecordUtil.getTiffField(removeRecord, "imageSaveDir");
				TiffField fileNameField = TiffRecordUtil.getTiffField(removeRecord, "fileName");
				TiffField fileCodeField = TiffRecordUtil.getTiffField(removeRecord, "fileCode");
				String imagePath = imageSaveDirField.getValue() + fileNameField.getValue();
				Path file = Paths.get(imagePath);
				String itemDetail = null;
	    		try {
					Files.delete(file);
//					logger.debug("已刪除 scanOrder:" + scanOrder + ", 檔案:" + imagePath);
				} catch (NoSuchFileException e) {
					itemDetail = String.format("● 檔案不存在，序號:%s 文件編號:%s 影像檔:%s", scanOrder.toString(), fileCodeField.getValue(), fileNameField.getValue());
//					logger.error("NoSuchFileException", e);
				} catch (IOException e) {
					itemDetail = String.format("● 序號:%s 文件編號:%s 影像檔:%s", scanOrder.toString(), fileCodeField.getValue(), fileNameField.getValue());
//					logger.error(itemDetail, e);	
				}
	    		
	    		if (null == itemDetail) {
	    			removeCount++;
	    			it.remove();
	    		} else {
	    			removeFailedItems.add(itemDetail);
	    		}
	    		
			}
//			logger.debug("刪除成功檔案數: " + removeCount + ", 檔案刪除失敗檔案數:" + removeFailedItems.size());
		}
//		logger.debug("bbbb");
		String errorMessage = "";
		for (int i=0; i < removeFailedItems.size(); i++) {
			String itemDetail = removeFailedItems.get(i);
			errorMessage += itemDetail + "\n";
		}
		if (errorMessage.length() > 0) {
			errorMessage = "無法刪除下列影像檔：\n\n" + errorMessage;
		}
		if (removeCount > 0) {
			try {
				// 刪除記錄並回寫 XML 檔案
				recordSetHelper.marshalToFile(recordSet);
			} catch (JAXBException e) {
				errorMessage = errorMessage + "\n" + String.format("無法儲存設定檔 %s ！", ImageRecordHelper.RECORD_SET_FILE_NAME);
//				logger.error(errorMessage, e);
			}
		}
//		logger.debug("ccc");
		if (errorMessage.length() > 0) {
			throw new Exception(errorMessage);
		}
//		logger.debug("ddd");
	}
}


