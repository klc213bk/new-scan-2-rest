package com.tgl.newscan2rest.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.ListIterator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import com.tgl.newscan2rest.bean.DeleteRecord;
import com.tgl.newscan2rest.bean.ImageRecordSet;
import com.tgl.newscan2rest.bean.LoginStatus;
import com.tgl.newscan2rest.bean.TiffRecord;
import com.tgl.newscan2rest.dto.LoginRequest;
import com.tgl.newscan2rest.util.ImageRecordHelper;

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
			errorMessage = String.format("序號:%s，影像檔 %s 不存在！", deleteRecord.getScanOrder(), deleteRecord.getFileURL());
			logger.error(errorMessage, e);

			throw new Exception(errorMessage, e);
		} catch (IOException e) {
			errorMessage = String.format("序號:%s，無法刪除影像檔 %s ！", deleteRecord.getScanOrder(), deleteRecord.getFileURL());
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
}
