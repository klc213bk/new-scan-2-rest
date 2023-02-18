package com.tgl.newscan2rest.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import com.tgl.newscan2rest.bean.ImageRecordSet;
import com.tgl.newscan2rest.bean.PageWarning;
import com.tgl.newscan2rest.bean.ScanConfig;
import com.tgl.newscan2rest.bean.ScannedImage;
import com.tgl.newscan2rest.bean.TiffRecord;
import com.tgl.newscan2rest.bean.UploadBean;
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
	public void uploadSaveTiffData(int selectedIndex) throws Exception {
		
		ImageRecordHelper recordSetHelper = ImageRecordHelper.getInstance();
		ImageRecordSet recordSet = recordSetHelper.unmarshalFromFile();
		
		List<TiffRecord> recordList = recordSet.getRecords().getRecordList();
		ScannedImage selectedItem = recordSetHelper.convert(recordList.get(selectedIndex));
		
		selectedItem.stepProperty().set("upload");
		// 將 ScanConfig 資訊寫回 imagerecord
		//fillScanConfig(scanConfig, selectedItem); // UAT-IR-478019 掃描帳號與上傳帳號不同時，上傳人員、部室名稱、批次號碼應帶入掃描人員資料
		recordSetHelper.saveTiffData(selectedItem, recordList.get(selectedIndex));

		// 回寫 XML 檔案
		try {
			recordSetHelper.marshalToFile(recordSet);
		} catch (JAXBException e) {
			String errMsg = String.format("無法儲存設定檔 %s ！", ImageRecordHelper.RECORD_SET_FILE_NAME);
			logger.error(errMsg, e);
			throw new Exception(errMsg);
		}
	}
	public List<PageWarning> validateUploadFilePage(UploadBean uploadBean) throws Exception {
		int selectedIndex = uploadBean.getSelectedIndex();
		
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
		PageNoValidator pageNoValidator = new PageNoValidator();
		List<PageWarning> pageWarningList = new ArrayList<>();
//		int rowCount = imageTableView.getItems().size();
		
		// Sort 會改變上傳時間
		// sort by FileCode & PageNo
		//m_irsRS.SortRecords();
		//SyncWithXML();

//		if (logger.isDebugEnabled()) {
//			logger.debug("validateFilePage(), isUploadAll={}, rowCount={}", isUploadAll, rowCount);
//		}

		
		ImageRecordHelper recordSetHelper = ImageRecordHelper.getInstance();
		ImageRecordSet recordSet = recordSetHelper.unmarshalFromFile();
		
		List<TiffRecord> recordList = recordSet.getRecords().getRecordList();
		ScannedImage selectedItem = recordSetHelper.convert(recordList.get(selectedIndex));
		int rowCount = recordList.size();
		
		String config = ScanConfigUtil.readConfig();
		ScanConfig scanConfig = ScanConfigUtil.parseHtml(config);
		
		for (int i=0; i < rowCount; i++) {
//		for (int i=0; i<rowCount; i++) {
			ScannedImage item = recordSetHelper.convert(recordList.get(i));
		//	ScannedImage item = imageTableView.getItems().get(i);
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

	}

}
