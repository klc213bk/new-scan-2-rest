package com.tgl.newscan2rest.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import com.tgl.newscan2rest.bean.ImageRecordSet;
import com.tgl.newscan2rest.bean.LoginStatus;
import com.tgl.newscan2rest.bean.PageWarning;
import com.tgl.newscan2rest.bean.ScanConfig;
import com.tgl.newscan2rest.bean.ScannedImage;
import com.tgl.newscan2rest.bean.TiffRecord;
import com.tgl.newscan2rest.bean.TiffRecords;
import com.tgl.newscan2rest.bean.UploadBean;
import com.tgl.newscan2rest.bean.User;
import com.tgl.newscan2rest.exception.Scan2Exception;
import com.tgl.newscan2rest.exception.Scan2Exception.ErrorCode;
import com.tgl.newscan2rest.http.EBaoException;
import com.tgl.newscan2rest.http.EbaoClient;
import com.tgl.newscan2rest.util.ImageRecordHelper;
import com.tgl.newscan2rest.util.ObjectsUtil;
import com.tgl.newscan2rest.util.PageNoValidator;
import com.tgl.newscan2rest.util.ScanConfigUtil;

import jakarta.xml.bind.JAXBException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.util.Duration;

@Service
public class UploadService {

	private static final Logger logger = LogManager.getLogger(UploadService.class);

	public void validateBasic(UploadBean uploadBean, User user) throws Scan2Exception {

		// 檢查selectedIndex
		int selectedIndex = uploadBean.getSelectedIndex();
		if (selectedIndex < 0) {
			throw new Scan2Exception(ErrorCode.NO_UPLOAD_SELECTION);
		} 

		ImageRecordHelper recordSetHelper = ImageRecordHelper.getInstance();
		// 檢查 imagerecordset.xml 是否被更動過
		if (recordSetHelper.xmlFileChanged()) {
			ImageRecordSet recordSet = recordSetHelper.loadImageRecordSet();
			user.setRecordSet(recordSet);

			// need to refresh ui
			throw new Scan2Exception(ErrorCode.IMAGERECORDSET_FILE_CHANGED);
		}

		ImageRecordSet recordSet = user.getRecordSet();
		List<TiffRecord> recordList = recordSet.getRecords().getRecordList();
		TiffRecord tiffRecord = recordList.get(selectedIndex);
		ScannedImage selectedItem = recordSetHelper.convert(tiffRecord);

		//檢查影像檔是否存在
		if (null != selectedItem) {
			String fileURL = selectedItem.fileURLProperty().getValue();
			boolean notExist = Files.notExists(Paths.get(fileURL));
			if (notExist) {
				throw new Scan2Exception(ErrorCode.IMAGE_FILE_REMOVED);
			}
		}

		// 2022/03/22 UNB Emma 來信要求 1. 發生2次點選【上傳】、【上傳全部】無反應，經你確認原因為File page及Max page寫入異常值(K、%)，導致系統卡住無法進行上傳。
		// 因 ASPRISE 辨識出的條碼偶爾會出現文件類別未2碼非數字，故於上傳前檢查 FilePage 及 MaxPage 應為數字
		validatePageNumberChar(selectedItem);

		// 欄位檢核，檢核 FilePage 應為數字
		// 不需要實作，TextFormatter 已過濾

		// BR-CMN-PIC-016 檢核是否可替換, 由Server Side檢核
		// BR-CMN-PIC-017 檢核是否輸入批次號碼	
		boolean isBatchNoValid = validateBatchNo(selectedItem);
		if (!isBatchNoValid) {
			throw new Scan2Exception(ErrorCode.VALIDATE_BATCH_NO_ERROR);
		}

		// BR-CMN-PIC-XXX 檢核是否輸入箱號
		boolean isBoxNoValid = validateBoxNo(selectedItem);
		if (!isBoxNoValid) {
			throw new Scan2Exception(ErrorCode.VALIDATE_BOX_NO_ERROR);
		}
		ScanConfig scanConfig = user.getScanConfig();
		if (scanConfig.isDeptNB()) {
			// PCR_386372 - 掃瞄上傳啟動核保完成收到補全件Email通知
			validateSendEmail(selectedItem);

			// PCR_268354 - 自動核保及核保功能優化需求(視訊投保件)
			// 單筆不檢驗
		}  

	}
	public boolean checkLogin(User user) {
		boolean isSuccess = false;
		int loginStatus = user.getLoginStatus();
		ScanConfig scanConfig = user.getScanConfig();
		if (loginStatus == LoginStatus.STATUS_LOGGED_IN) {
			EbaoClient eBaoClient = EbaoClient.getInstance();
			try {
				scanConfig = eBaoClient.getScanConfig();
//				if (launchParamQueryFromPage) {
//					this.scanConfig.resetDefValues(launchParamBoxNo, launchParamBatchDeptType, launchParamBatchDate, launchParamBatchArea, launchParamBatchDocType);
//				}
				isSuccess = true;
			} catch (EBaoException e) {
				user.setLoginStatus(LoginStatus.STATUS_OFF_LINE);
				logger.error(e);
			}
		} 
	
		return isSuccess;
	}
	private void validateAllIsRemote(List<TiffRecord> recordList) throws Scan2Exception {

		ImageRecordHelper recordSetHelper = ImageRecordHelper.getInstance();
		boolean isValid = true;
		Map<String, String> errorMessageMap = new LinkedHashMap<String, String>();


		List<String> fileCodeList = new ArrayList<String>();
		for (TiffRecord record : recordList) {
			ScannedImage item = recordSetHelper.convert(record);
			String fileCode = item.fileCodeProperty().getValue();
			if (ObjectsUtil.isNotEmpty(fileCode) && !fileCodeList.contains(fileCode)) {
				fileCodeList.add(fileCode);
			}
		}

		if (logger.isDebugEnabled()) {
			logger.debug("fileCodeList={}", String.join(",", fileCodeList));
		}

		for (String fileCode : fileCodeList) {
			boolean isRemoteValid = true;
			String scanOrder = null;
			String isRemote = null;
			String firstIsRemote = null;
			Map<String, String> scanOrderMap = new LinkedHashMap<String, String>();

			for (TiffRecord record : recordList) {
				ScannedImage item = recordSetHelper.convert(record);
				if (!fileCode.equals(item.fileCodeProperty().getValue())) {
					continue;
				}

				scanOrder = item.scanOrderProperty().getValue();
				isRemote = item.isRemoteProperty().getValue();
				if (ObjectsUtil.isNotEmpty(isRemote)) {
					if (logger.isDebugEnabled()) {
						logger.debug("fileCode={}, scanOrder={}, isRemote={}", fileCode, scanOrder, isRemote);
					}
					scanOrderMap.put(scanOrder, isRemote);

					if (firstIsRemote == null) {
						firstIsRemote = isRemote;
					} else {
						if (!isRemote.equals(firstIsRemote)) {
							isRemoteValid = false;
						}
					}
				}
			}
			if (scanOrderMap.size() > 0) {
				if (isRemoteValid) {
					for (TiffRecord record : recordList) {
						ScannedImage item = recordSetHelper.convert(record);
						if (!fileCode.equals(item.fileCodeProperty().getValue())) {
							continue;
						}
						item.isRemoteProperty().setValue(firstIsRemote);
					}
				} else {
					errorMessageMap.put(fileCode, String.format("序號 %s 視訊投保件 設置不一致！", String.join("、", scanOrderMap.keySet())));
				}
			}
		}


		int errorCount = errorMessageMap.size();
		if (errorCount > 0) {
			isValid = false;
			String errorMessage = "";
			int i = 0;
			for (String fileCode : errorMessageMap.keySet()) {
				errorMessage += (i==0 ? "" : "\r\n") + (errorCount==1 ? "" : ("保單 " + fileCode + "，")) + errorMessageMap.get(fileCode);
				i++;
			}
			ErrorCode errorCode = ErrorCode.VALIDATE_IS_REMOTE_ERROR;
			errorCode.setMessage(errorMessage);
			throw new Scan2Exception(errorCode);
		}

	}
	private void validateAllSendEmail(List<TiffRecord> recordList) throws Scan2Exception {
		ImageRecordHelper recordSetHelper = ImageRecordHelper.getInstance();
		Map<String, String> scanOrderMap = new LinkedHashMap<String, String>();
		String firstSendEmail = null;
		boolean isValid = true;
		for (TiffRecord record : recordList) {
			ScannedImage item = recordSetHelper.convert(record);
			String scanOrder = item.scanOrderProperty().getValue();
			String sendEmail = item.sendEmailProperty().getValue();
			if (ObjectsUtil.isNotEmpty(sendEmail)) {
				if (logger.isDebugEnabled()) {
					logger.debug("scanOrder={}, sendEmail={}", scanOrder, sendEmail);
				}
				scanOrderMap.put(scanOrder, sendEmail);

				if (firstSendEmail == null) {
					firstSendEmail = sendEmail;
				} else {
					if (!sendEmail.equals(firstSendEmail)) {
						isValid = false;
					}
				}
			}
		}
		if (scanOrderMap.size() > 0) {
			if (isValid) {
				for (TiffRecord record : recordList) {
					ScannedImage item = recordSetHelper.convert(record);
					item.sendEmailProperty().setValue(firstSendEmail);
				}
			} else {
				String errMsg = String.format("序號 %s 是否發EMAIL 設置不一致！", String.join("、", scanOrderMap.keySet()));
				ErrorCode errorCode = ErrorCode.SEND_EMAIL_NOT_CONSISTENT_ERROR;
				errorCode.setMessage(errMsg);
				throw new Scan2Exception(errorCode);
			}
		} else {
			throw new Scan2Exception(ErrorCode.NO_SEND_EMAIL_ERROR);
		}
	}
	private void validateSendEmail(ScannedImage selectedItem) throws Scan2Exception {
		String sendEmail = selectedItem.sendEmailProperty().getValue();
		if (logger.isDebugEnabled()) {
			logger.debug("sendEmail={}", sendEmail);
		}
		if (ObjectsUtil.isEmpty(sendEmail)) {
			throw new Scan2Exception(ErrorCode.NO_SEND_EMAIL_ERROR);
		}
	}
	private void validateAllPageNumberChar(List<TiffRecord> recordList) throws Scan2Exception {
		ImageRecordHelper recordSetHelper = ImageRecordHelper.getInstance();
		try {
			for (TiffRecord record : recordList) {
				ScannedImage selectedItem = recordSetHelper.convert(record);
				validatePageNumberChar(selectedItem);
			}
		} catch(Scan2Exception e) {
			throw e;
		}
	}
	private void validatePageNumberChar(ScannedImage selectedItem) throws Scan2Exception {
		String scanOrder = null;
		String maxPageStr = null;
		String filePageStr = null;
		boolean isValid = true;
		String errMsg = null;

		if (selectedItem!=null) {
			scanOrder = selectedItem.scanOrderProperty().getValue();
			maxPageStr = selectedItem.maxPageProperty().getValue(); // MaxPage --> 數字一碼0~9 或 空白
			// IR-482394 Max Page(總頁數)檢核0~9，未排除「空白」情形，造成影像無法上傳
			if (ObjectsUtil.isNotEmpty(maxPageStr) && (maxPageStr.charAt(0) < '0' || maxPageStr.charAt(0) > '9' )) {
				errMsg = String.format("序號:%s，總頁數值異常{%s}，請刪除後重新掃描！", scanOrder, maxPageStr);
				ErrorCode errorCode = ErrorCode.VALIDATE_PAGE_NUMBER_CHAR_ERROR;
				errorCode.setMessage(errMsg);
				throw new Scan2Exception(errorCode);
			}
			filePageStr = selectedItem.filePageProperty().getValue(); // FilePage --> 空白或數字一至多碼1以上(僅排除一碼0)
			if (ObjectsUtil.isNotEmpty(filePageStr)) {
				try {
					int filePage = Integer.parseInt(filePageStr);
					if (filePage==0) {
						errMsg = (ObjectsUtil.isEmpty(errMsg) ? "" : errMsg) + String.format("序號:%s，頁碼值異常{%s}，請確認後重新設置！", scanOrder, filePageStr);
						ErrorCode errorCode = ErrorCode.VALIDATE_PAGE_NUMBER_CHAR_ERROR;
						errorCode.setMessage(errMsg);
						throw new Scan2Exception(errorCode);
					}
				} catch (NumberFormatException e) {
					errMsg = (ObjectsUtil.isEmpty(errMsg) ? "" : errMsg) + String.format("序號:%s，頁碼值異常{%s}，請確認後重新設置！", scanOrder, filePageStr);
					ErrorCode errorCode = ErrorCode.VALIDATE_PAGE_NUMBER_CHAR_ERROR;
					errorCode.setMessage(errMsg);
					throw new Scan2Exception(errorCode);
				}
			}
		}
	}
	//	public void upload(UploadBean uploadBean) throws Exception {
	//
	//		int selectedIndex = uploadBean.getSelectedIndex();
	//		
	//		ImageRecordHelper recordSetHelper = ImageRecordHelper.getInstance();
	//		ImageRecordSet recordSet = recordSetHelper.unmarshalFromFile();
	//		
	//		List<TiffRecord> recordList = recordSet.getRecords().getRecordList();
	//		ScannedImage selectedItem = recordSetHelper.convert(recordList.get(selectedIndex));
	//		
	//		String config = ScanConfigUtil.readConfig();
	//		ScanConfig scanConfig = ScanConfigUtil.parseHtml(config);
	//		
	//		validateFilePage(recordSetHelper, scanConfig, recordList);
	//
	//		selectedItem.stepProperty().set("upload");
	//		
	//		// 將 ScanConfig 資訊寫回 imagerecord
	//		//fillScanConfig(scanConfig, selectedItem); // UAT-IR-478019 掃描帳號與上傳帳號不同時，上傳人員、部室名稱、批次號碼應帶入掃描人員資料
	//		recordSetHelper.saveTiffData(selectedItem, recordList.get(selectedIndex));
	//
	//		// 回寫 XML 檔案
	//		try {
	//			recordSetHelper.marshalToFile(recordSet);
	//		} catch (JAXBException e) {
	//			String errMsg = String.format("無法儲存設定檔 %s ！", ImageRecordHelper.RECORD_SET_FILE_NAME);
	//			DialogUtil.showErrorMessageAndWait(MainView.this.getScene().getWindow(), errMsg);
	//			logger.error(errMsg, e);
	//			return;
	//		}
	//
	//		// 檢查必需登入 eBao Server
	//		if (!checkLogin()) {
	//			this.showSnackbar("未登入或離線使用時無法上傳檔案，請登入 eBao Server！", true, Duration.seconds(5.0));
	//			return;
	//		}
	//
	//		UploadProcessSummary uploadSummary = DialogUtil.showUploadDialog(MainView.this, this.scanConfig, imageTableView.getItems(), selectedIndex);
	//		if (logger.isDebugEnabled()) {
	//			logger.debug("dialogTitle={}, dialogMessage={}, cntSuccess={}, cntUpload={}, cntFailed={}", uploadSummary.getDialogTitle(), uploadSummary.getDialogMessage(), uploadSummary.getCntSuccess(), uploadSummary.getCntUpload(), uploadSummary.getCntFailed());
	//		}
	//
	//    	removeUploadedItems();
	//
	//		String dialogTitle = uploadSummary.getDialogTitle()==null ? "訊息" : uploadSummary.getDialogTitle();
	//		String dialogMessage = uploadSummary.getDialogMessage()==null ? 
	//				String.format("上傳完成！成功合計：%s，上傳合計：%s，失敗合計：%s", uploadSummary.getCntSuccess(), uploadSummary.getCntUpload(), uploadSummary.getCntFailed()) : 
	//				uploadSummary.getDialogMessage();
	//		DialogUtil.showMessage(MainView.this.getScene().getWindow(), dialogTitle, dialogMessage, true);
	//	}

	public void uploadAll() throws Exception {

	}

	public void saveTiffData(int selectedIndex, User user) throws Scan2Exception {

		ImageRecordHelper recordSetHelper = ImageRecordHelper.getInstance();
		
		ImageRecordSet recordSet = user.getRecordSet();

		List<TiffRecord> recordList = recordSet.getRecords().getRecordList();
		ScannedImage selectedItem = recordSetHelper.convert(recordList.get(selectedIndex));

		selectedItem.stepProperty().set("upload");
		// 將 ScanConfig 資訊寫回 imagerecord
		// fillScanConfig(scanConfig, selectedItem); // UAT-IR-478019
		// 掃描帳號與上傳帳號不同時，上傳人員、部室名稱、批次號碼應帶入掃描人員資料
		recordSetHelper.saveTiffData(selectedItem, recordList.get(selectedIndex));

		// 回寫 XML 檔案
		try {
			recordSetHelper.marshalToFile(recordSet);
		} catch (JAXBException e) {
			String errMsg = String.format("無法儲存設定檔 %s ！", ImageRecordHelper.RECORD_SET_FILE_NAME);
			logger.error(errMsg, e);
			ErrorCode errorCode = ErrorCode.SAVE_TIFF_DATA_ERROR;
			errorCode.setMessage(errMsg);
			throw new Scan2Exception(errorCode);
		}
	}
	private boolean validateAllBoxNo(List<TiffRecord> recordList) {
		ImageRecordHelper recordSetHelper = ImageRecordHelper.getInstance();
		boolean isValid = false;

		for(TiffRecord record : recordList) {
			ScannedImage item = recordSetHelper.convert(record);
			if (validateBatchNo(item)) {
				isValid = true;
				break;
			}
		}
		if (isValid) {
			String boxNo = null;
			for(TiffRecord record : recordList) {
				ScannedImage item = recordSetHelper.convert(record);
				boxNo = item.boxNoProperty().getValue();
				item.boxNoProperty().setValue(boxNo);
				//item.scanTimeProperty().setValue(scanTime);
			}
		}
		return isValid;
	}
	private boolean validateBoxNo(ScannedImage selectedItem) {
		String boxNo = selectedItem.boxNoProperty().getValue();
		if (logger.isDebugEnabled()) {
			logger.debug("boxNo={}", boxNo);
		}
		if (ObjectsUtil.isNotEmpty(boxNo)) {
			return true;
		}
		return false;
	}
	private boolean validateAllBatchNo(List<TiffRecord> recordList) {

		String batchDepType = null;
		String batchDate = null;
		String batchArea = null;
		String batchDocType = null;


		boolean isValid = false;
		ImageRecordHelper recordSetHelper = ImageRecordHelper.getInstance();

		for(TiffRecord record : recordList) {
			ScannedImage item = recordSetHelper.convert(record);
			if (validateBatchNo(item)) {
				isValid = true;
				break;
			}
		}
		if (isValid) {
			for(TiffRecord record : recordList) {
				ScannedImage item = recordSetHelper.convert(record);
				item.batchDepTypeProperty().setValue(batchDepType);
				item.batchDateProperty().setValue(batchDate);
				item.batchAreaProperty().setValue(batchArea);
				item.batchDocTypeProperty().setValue(batchDocType);
			}
		}
		return isValid;

	}
	private boolean validateBatchNo(ScannedImage selectedItem) {

		String batchDepType = null;
		String batchDate = null;
		String batchArea = null;
		String batchDocType = null;

		batchDepType = selectedItem.batchDepTypeProperty().getValue();
		batchDate = selectedItem.batchDateProperty().getValue();
		batchArea = selectedItem.batchAreaProperty().getValue();
		batchDocType = selectedItem.batchDocTypeProperty().getValue();
		if (logger.isDebugEnabled()) {
			logger.debug("batchDepType={}, batchDate={}, batchArea={}, batchDocType={}", batchDepType, batchDate, batchArea, batchDocType);
		}
		if (ObjectsUtil.isNotEmpty(batchDepType) && ObjectsUtil.isNotEmpty(batchDate) && ObjectsUtil.isNotEmpty(batchArea) && ObjectsUtil.isNotEmpty(batchDocType)) {
			return true;
		}

		return false;
	}
	public List<PageWarning> validateFilePage(ScanConfig scanConfig, List<TiffRecord> recordList) {
		boolean isSuccess = true;
		boolean needToCheck = false;
		String lastFileType = null;
		String lastFileCode = null;
		String lastFilePage = null;
		String lastMaxPage = null;
		String mainFileType = null;
		String fileType = null;
		String fileCode = null;
		String filePage = null;
		String maxPage = null;
		String signature = null;
		
		ImageRecordHelper recordSetHelper = ImageRecordHelper.getInstance();
		
		PageNoValidator pageNoValidator = new PageNoValidator();
		List<PageWarning> pageWarningList = new ArrayList<>();
		int rowCount = recordList.size();

		// Sort 會改變上傳時間
		// sort by FileCode & PageNo
		//m_irsRS.SortRecords();
		//SyncWithXML();

		if (logger.isDebugEnabled()) {
			logger.debug("validateFilePage(), rowCount={}", rowCount);
		}

		for (int i=0; i<rowCount; i++) {
			ScannedImage item = recordSetHelper.convert(recordList.get(i));
			
			needToCheck = false;
			mainFileType = item.mainFileTypeProperty().getValue();
			fileType = item.fileTypeProperty().getValue();
			fileCode = item.fileCodeProperty().getValue();
			filePage = item.filePageProperty().getValue();
			maxPage = item.maxPageProperty().getValue();
			signature = item.signatureProperty().getValue();

			if (logger.isDebugEnabled()) {
				logger.debug("mainFileType={}, fileType={}, fileCode={}, filePage={}, maxPage={}, signature={}", mainFileType, fileType, fileCode, filePage, maxPage, signature);
			}

			if ("0".equals(maxPage) || "Y".equals(signature)) { // 最大頁數=0 || 切簽名影像,不檢核
				lastMaxPage = maxPage;
				if (logger.isDebugEnabled()) {
					logger.debug("maxPage=0 or is signature, Ignore pageNo check!");
				}
				continue;
			} else if ("1".equals(maxPage) && "1".equals(filePage)) { // 最大頁數=1,頁碼=1,不檢核
				if (pageNoValidator.size()<=0) {
					lastFileType = fileType;
					lastFileCode = fileCode;
					lastFilePage = filePage;	
					lastMaxPage  = maxPage;	
					if (logger.isDebugEnabled()) {
						logger.debug("maxPage=1 && filePage=1, Ignore pageNo check!");
					}
					continue;
				}
			}

			if (
					(pageNoValidator.size()<=0) || 
					( 
							ObjectsUtil.isNotEmpty(lastFileType) && ObjectsUtil.isNotEmpty(fileCode) && 
							ObjectsUtil.isEquals(fileType, lastFileType) &&  ObjectsUtil.isEquals(fileCode, lastFileCode)
							) )
			{
				pageNoValidator.setMultiPolicy(false);
				pageNoValidator.add(item);
				if (logger.isDebugEnabled()) {
					logger.debug("第一筆或非組合保單,同FileType/FileCode, AddRecord");
				}
			} else if (
					("UNB".equals(mainFileType) || "POS".equals(mainFileType)) &&
					ObjectsUtil.isEquals(fileType, lastFileType) && 
					fileCode.length()==11 && 
					ObjectsUtil.isEquals(ObjectsUtil.left(fileCode, 10), ObjectsUtil.left(lastFileCode, 10))
					) {
				pageNoValidator.setMultiPolicy(true);
				pageNoValidator.add(item);
				if (logger.isDebugEnabled()) {
					logger.debug("組合保單,同FileType,同FileCode(10), AddRecord");
				}
			} else {
				// FileCode/FileType changed  
				needToCheck = true;
				if (logger.isDebugEnabled()) {
					logger.debug("FileType||FileCode改變");
				}
			}

			if (needToCheck) {
				if (!pageNoValidator.validate()) {
					if (logger.isDebugEnabled()) {
						logger.debug("pageNoValidator.validate(): Failed!");
					}
					int cnt = pageNoValidator.size();
					for (int j=0; j<cnt; j++) {
						ScannedImage validImage = pageNoValidator.get(j);
						String scanOrder = validImage.scanOrderProperty().getValue();
						String errorMainFileType = validImage.mainFileTypeProperty().getValue();
						String errorFileType = validImage.fileTypeProperty().getValue();
						String errorFileCode = validImage.fileCodeProperty().getValue();
						String errorFilePage = validImage.filePageProperty().getValue();
						String errorCompanyCode = validImage.companyCodeProperty().getValue();
						String errorPersonalCode = validImage.personalCodeProperty().getValue();
						String errorActionInsert = validImage.actionInsertProperty().getValue();
						String errorActionReplace = validImage.actionReplaceProperty().getValue();
						String errorScanTime = validImage.scanTimeProperty().getValue();
						String errorRemark = "";
						String errorCardDesc = scanConfig.getDescByCardCode(errorFileType);
						Integer errorIndexNo = validImage.indexNoProperty().getValue();

						if ("Y".equals(errorActionInsert)) {
							errorRemark = "插入";
						} else if ("Y".equals(errorActionReplace)) {
							errorRemark = "替換";
						}
						if (logger.isDebugEnabled()) {
							logger.debug("PageNo is invalid, append error msg... ");
							logger.debug("scanOrder={}, errorMainFileType={}, errorFileType={}, errorFileCode={}, errorFilePage={}, errorCompanyCode={}, errorPersonalCode={}, errorActionInsert={}, errorActionReplace={}, errorScanTime={}, errorRemark={}, errorCardDesc={}, errorIndexNo={}, ", scanOrder, errorMainFileType, errorFileType, errorFileCode, errorFilePage, errorCompanyCode, errorPersonalCode, errorActionInsert, errorActionReplace, errorScanTime, errorRemark, errorCardDesc, errorIndexNo);
						}

						PageWarning pageWarning = new PageWarning(
								scanOrder, 
								errorFileCode, 
								errorMainFileType, 
								errorFileType + "-" + errorCardDesc, 
								errorCompanyCode, 
								errorPersonalCode, 
								errorFilePage, 
								errorScanTime, 
								errorRemark, 
								errorIndexNo 
								);
						pageWarningList.add(pageWarning);

						isSuccess = false;
					}
				} else {
					if (logger.isDebugEnabled()) {
						logger.debug("pageNoValidator.validate(): Passed!");
					}
				}

				pageNoValidator.clear();

				if ("1".equals(maxPage) && "1".equals(filePage)) { // 最大頁數=1,頁碼=1,不檢核
					if (logger.isDebugEnabled()) {
						logger.debug("maxPage==1 && filePage==1, DO NOT AddRecord");
					}
				} else {
					if (logger.isDebugEnabled()) {
						logger.debug("AddRecord");
					}
					pageNoValidator.add(item);
				}
			}

			lastFileType = fileType;
			lastFileCode = fileCode;
			lastFilePage = filePage;	
			lastMaxPage  = maxPage;		

			if (logger.isDebugEnabled()) {
				logger.debug("lastFileType={}, lastFileCode={}, lastFilePage={}, lastMaxPage={}", lastFileType, lastFileCode, lastFilePage, lastMaxPage);
			}
		}

		// Last Record
		if (pageNoValidator.size()>0) {
			if (!pageNoValidator.validate()) {
				if (logger.isDebugEnabled()) {
					logger.debug("pageNoValidator.validate(): Failed!");
				}
				int cnt = pageNoValidator.size();
				for (int j=0; j<cnt; j++) {
					ScannedImage validImage = pageNoValidator.get(j);
					String scanOrder = validImage.scanOrderProperty().getValue();
					String errorMainFileType = validImage.mainFileTypeProperty().getValue();
					String errorFileType = validImage.fileTypeProperty().getValue();
					String errorFileCode = validImage.fileCodeProperty().getValue();
					String errorFilePage = validImage.filePageProperty().getValue();
					String errorCompanyCode = validImage.companyCodeProperty().getValue();
					String errorPersonalCode = validImage.personalCodeProperty().getValue();
					String errorActionInsert = validImage.actionInsertProperty().getValue();
					String errorActionReplace = validImage.actionReplaceProperty().getValue();
					String errorScanTime = validImage.scanTimeProperty().getValue();
					String errorRemark = "";
					String errorCardDesc = scanConfig.getDescByCardCode(errorFileType);
					Integer errorIndexNo = validImage.indexNoProperty().getValue();

					if ("Y".equals(errorActionInsert)) {
						errorRemark = "插入";
					} else if ("Y".equals(errorActionReplace)) {
						errorRemark = "替換";
					}
					if (logger.isDebugEnabled()) {
						logger.debug("PageNo is invalid, append error msg ... ");
						logger.debug("scanOrder={}, errorMainFileType={}, errorFileType={}, errorFileCode={}, errorFilePage={}, errorCompanyCode={}, errorPersonalCode={}, errorActionInsert={}, errorActionReplace={}, errorScanTime={}, errorRemark={}, errorCardDesc={}, errorIndexNo={}, ", scanOrder, errorMainFileType, errorFileType, errorFileCode, errorFilePage, errorCompanyCode, errorPersonalCode, errorActionInsert, errorActionReplace, errorScanTime, errorRemark, errorCardDesc, errorIndexNo);
					}

					PageWarning pageWarning = new PageWarning(
							scanOrder, 
							errorFileCode, 
							errorMainFileType, 
							errorFileType + "-" + errorCardDesc, 
							errorCompanyCode, 
							errorPersonalCode, 
							errorFilePage, 
							errorScanTime, 
							errorRemark, 
							errorIndexNo 
							);
					pageWarningList.add(pageWarning);

					isSuccess = false;
				}
			} else {
				if (logger.isDebugEnabled()) {
					logger.debug("pageNoValidator.validate(): Passed!");
				}
			}

			pageNoValidator.clear();
			pageNoValidator = null;
		}

		if (logger.isDebugEnabled()) {
			logger.debug("isSuccess={}", isSuccess);
		}
		
		return pageWarningList;

//		if (!isSuccess) {
//			boolean stillUpload = false;
//			Integer result = DialogUtil.showPageNoWarningDialog(MainView.this, pageWarningList);
//			if (logger.isDebugEnabled()) {
//				logger.debug("validateFilePage() --> showPageNoWarningDialog(), result={}", result);
//			}
//			if (null!=result) { // 返回檢查
//				if (Integer.valueOf("-1").equals(result)) {
//					// 仍需上傳
//					stillUpload =  true;
//				} else {
//					// 返回檢查: 有選擇一筆
//					imageTableView.getSelectionModel().clearSelection();
//					for (int i=0; i< imageTableView.getItems().size(); i++) {
//						ScannedImage item = imageTableView.getItems().get(i);
//						if (result.equals(item.indexNoProperty().getValue())) {
//							imageTableView.getSelectionModel().select(i);
//							imageTableView.scrollTo(i);
//							break;
//						}
//					}
//				}
//			} else {
//				// 返回檢查: 未選擇任一筆
//			}
//
//			pageWarningList.clear();
//			pageWarningList = null;
//
//			return stillUpload;
//		}
//
//		return true;
	}

}
