package com.tgl.newscan2rest.service;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import com.tgl.newscan2rest.Constant;
import com.tgl.newscan2rest.bean.ImageRecordSet;
import com.tgl.newscan2rest.bean.ScanConfig;
import com.tgl.newscan2rest.bean.ScannedImage;
import com.tgl.newscan2rest.bean.TiffRecord;
import com.tgl.newscan2rest.bean.UpdateRecordBean;
import com.tgl.newscan2rest.util.ImageRecordHelper;
import com.tgl.newscan2rest.util.ObjectsUtil;
import com.tgl.newscan2rest.util.ScanConfigUtil;

import jakarta.xml.bind.JAXBException;

@Service
public class UpdateService {

	private static final Logger logger = LogManager.getLogger(UpdateService.class);

	public void updateRecord(UpdateRecordBean updateRecordBean) throws Exception {

		int selectedIndex = updateRecordBean.getSelectedIndex();
		
		ImageRecordHelper recordSetHelper = ImageRecordHelper.getInstance();
		ImageRecordSet recordSet = recordSetHelper.unmarshalFromFile();
		List<TiffRecord> recordList = recordSet.getRecords().getRecordList();

		TiffRecord selectedRecord = recordList.get(selectedIndex);
		ScannedImage selectedItem = recordSetHelper.convert(selectedRecord);

//		if (logger.isDebugEnabled()) {
//			logger.debug("setRecordValues(), selectedIndex={}", selectedIndex);
//		}
		// 驗證欄位是否正確
		String errMsg = validateScannedImage(selectedItem, updateRecordBean);
		if (!errMsg.isBlank()) {
			throw new Exception(errMsg);
		}

		storeScannedImage(selectedIndex, recordSetHelper, recordSet, updateRecordBean);

		// 回寫 XML 檔案
		try {
			recordSetHelper.marshalToFile(recordSet);
		} catch (JAXBException e) {
			String errMsg2 = String.format("無法儲存設定檔 %s ！", ImageRecordHelper.RECORD_SET_FILE_NAME);
			logger.error(errMsg2, e);
			throw new Exception(errMsg2);
		}

	}

	private void storeScannedImage(int selectedIndex, ImageRecordHelper recordSetHelper, ImageRecordSet recordSet,
			UpdateRecordBean updateRecordBean) throws Exception {
		String config = ScanConfigUtil.readConfig();
		ScanConfig scanConfig = ScanConfigUtil.parseHtml(config);

		// ImageRecordHelper recordSetHelper = ImageRecordHelper.getInstance();
//		ImageRecordSet recordSet = recordSetHelper.unmarshalFromFile();
		List<TiffRecord> recordList = recordSet.getRecords().getRecordList();

		TiffRecord selectedRecord = recordList.get(selectedIndex);
		ScannedImage selectedItem = recordSetHelper.convert(selectedRecord);

//		if (logger.isDebugEnabled()) {
//			logger.debug("setRecordValues(), selectedIndex={}", selectedIndex);
//		}

		// 儲存畫面中的變更
		String fileType = null == updateRecordBean.getFileTypeValue() ? "" : updateRecordBean.getFileTypeValue();
		String oriFileCode = selectedItem.fileCodeProperty().getValue();
		String fileCode = updateRecordBean.getFileCode();
		String actionReplace = null == updateRecordBean.getActionReplace() ? null : updateRecordBean.getActionReplace();
		String actionInsert = null == updateRecordBean.getActionInsert() ? null : updateRecordBean.getActionInsert();
		String actionType = "";
		if (null != actionReplace && Constant.YN_YES.equals(actionReplace)) {
			actionType = "替換";
		} else if (null != actionInsert && Constant.YN_YES.equals(actionInsert)) {
			actionType = "插入";
		}

		selectedItem.orgNameProperty().setValue(updateRecordBean.getOrgName());
		selectedItem.deptNameProperty().setValue(updateRecordBean.getDeptName());
		selectedItem.mainFileTypeProperty().setValue(
				null == updateRecordBean.getMainFileTypeValue() ? "" : updateRecordBean.getMainFileTypeValue());
		selectedItem.mainFileTypeTextProperty()
				.setValue(null == updateRecordBean.getMainFileTypeText() ? "" : updateRecordBean.getMainFileTypeText());
		selectedItem.fileTypeProperty().setValue(fileType);
		selectedItem.fileTypeTextProperty()
				.setValue(null == updateRecordBean.getFileTypeText() ? "" : updateRecordBean.getFileTypeText());
		selectedItem.fileCodeProperty().setValue(fileCode);
		selectedItem.filePageProperty().setValue(updateRecordBean.getFilePage());
		selectedItem.boxNoProperty().setValue(updateRecordBean.getBoxNo());
		selectedItem.batchDepTypeProperty()
				.setValue(null == updateRecordBean.getBatchDepType() ? "" : updateRecordBean.getBatchDepType());
		selectedItem.batchDateProperty().setValue(updateRecordBean.getBatchDate());
		selectedItem.batchAreaProperty().setValue(updateRecordBean.getBatchArea());
		selectedItem.batchDocTypeProperty().setValue(updateRecordBean.getBatchDocType());
		selectedItem.companyCodeProperty().setValue(updateRecordBean.getCompanyCode());
		selectedItem.personalCodeProperty().setValue(updateRecordBean.getPersonalCode());
		selectedItem.actionReplaceProperty()
				.setValue(null == updateRecordBean.getActionReplace() ? "" : updateRecordBean.getActionReplace());
		selectedItem.actionInsertProperty()
				.setValue(null == updateRecordBean.getActionInsert() ? "" : updateRecordBean.getActionInsert());
		selectedItem.actionTypeProperty().setValue(actionType);
		selectedItem.sendEmailProperty()
				.setValue(null == updateRecordBean.getSendEmail() ? "" : updateRecordBean.getSendEmail());
		selectedItem.isRemoteProperty()
				.setValue(null == updateRecordBean.getIsRemote() ? "" : updateRecordBean.getIsRemote());
		selectedItem.remarkProperty().setValue(updateRecordBean.getRemark());
		selectedItem.recordStatusProperty().setValue("1");
		String maxPage = scanConfig.getMaxPageByCardCode(fileType);
		selectedItem.maxPageProperty().setValue(null == maxPage ? "" : maxPage);
//
//	    	ScanUtil.setLastBoxNo(cbbBoxNumber.getSelectionModel().getSelectedItem());
//	    	ScanUtil.setLastBatchArea(txtBatchArea.getText());

		recordSetHelper.saveTiffData(selectedItem, recordList.get(selectedIndex));

		// PCR 244580 BR-CMN-PIC-019 文件編號整批修改
		if ((scanConfig.isDeptPos() || "LA".equals(scanConfig.getBatchDepTypeValue()))
				&& !fileCode.equals(oriFileCode)) { // 避免改動非文件編號欄位,也會更新所有文件編號
//			if (logger.isDebugEnabled()) {
//				logger.debug("isDeptPos={}, batchDepTypeValue={}, newFileCode={}, oriFileCode={}",
//						scanConfig.isDeptPos(), scanConfig.getBatchDepTypeValue(), fileCode, oriFileCode);
//			}

			for (int i = (selectedIndex + 1); i < recordList.size(); i++) {
				ScannedImage item = recordSetHelper.convert(recordList.get(i));
				String itemFileType = item.fileTypeProperty().getValue();
				if (Constant.SEP_FILE_TYPE.equals(itemFileType)) {
//					if (logger.isDebugEnabled()) {
//						logger.debug("FileType is POSZ999, exist!");
//					}
					break;
				}

				// 調整為該筆以下的FILE_CODE全部更新
				String itemFileCode = item.fileCodeProperty().getValue();
				item.fileCodeProperty().setValue(fileCode);
//				if (logger.isDebugEnabled()) {
//					logger.debug("index={}, newFileCode={}, itemFileCode={}", i, fileCode, itemFileCode);
//				}
				recordSetHelper.saveTiffData(item, recordList.get(i));
			}
		}

		// 更新掃瞄序號
		recordSetHelper.resetScanOrder(recordList);
	}

	private String validateScannedImage(ScannedImage selectedItem, UpdateRecordBean updateRecordBean) {
		String errorMessage = "";
		String signature = selectedItem.signatureProperty().getValue();
		String mainFileType = updateRecordBean.getMainFileTypeValue();
		String fileType = updateRecordBean.getFileTypeValue();
		String fileCode = updateRecordBean.getFileCode();
		String filePage = updateRecordBean.getFilePage();
		String companyCode = updateRecordBean.getCompanyCode();
		String personalCode = updateRecordBean.getPersonalCode();

		if (logger.isDebugEnabled()) {
			logger.debug(
					"validateScannedImage(), signature={}, mainFileType={}, fileType={}, fileCode={}, filePage={}, companyCode={}, personalCode={}",
					signature, mainFileType, fileType, fileCode, filePage, companyCode, personalCode);
		}

		if (Constant.MAINFILETYPE_GID.equals(mainFileType)) {
			if (ObjectsUtil.isEmpty(companyCode) && ObjectsUtil.isEmpty(personalCode)) { // 公司碼、個人碼為空值
				errorMessage += "請輸入公司碼或個人碼!\n";
			} else {
				if (ObjectsUtil.isNotEmpty(companyCode) && companyCode.length() != 8) {
					errorMessage += "請輸入完整公司碼，長度應為8碼!\n";
				}
				if (ObjectsUtil.isNotEmpty(personalCode) && personalCode.length() != 6) {
					errorMessage += "請輸入完整個人碼，長度應為6碼!\n";
				}
			}
		}

		if (ObjectsUtil.isEmpty(mainFileType)) {
			errorMessage += "請輸入 影像主類型\n";
		}
		if (ObjectsUtil.isEmpty(fileType)) {
			errorMessage += "請輸入 影像子類型\n";
		}
		if (ObjectsUtil.isEmpty(fileCode)) {
			errorMessage += "請輸入 文件編號\n";
		}
		if (ObjectsUtil.isEmpty(filePage) && "N".equals(signature)) {
			errorMessage += "請輸入 頁碼\n";
		}
		if (ObjectsUtil.isNotEmpty(filePage) && "Y".equals(signature)) {
			errorMessage += "<簽名檔影像>請勿輸入頁碼!\n";
		}

		return errorMessage;
	}
}
