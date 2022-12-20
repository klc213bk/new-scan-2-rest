package com.tgl.newscan2rest.bean;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * 
 * @author Vincent Chang
 *
 */
public class ScannedImage {

	// Form 畫面中的資訊
	private StringProperty orgName = new SimpleStringProperty(""); //組織編碼 	private StringProperty 
	private StringProperty deptName = new SimpleStringProperty(""); //部室名稱
	private StringProperty mainFileType = new SimpleStringProperty(""); //影像主類型; // ex:UNB
	private StringProperty fileType = new SimpleStringProperty(""); //影像子類型；ex：UNBA040
	private StringProperty fileCode = new SimpleStringProperty(""); //文件編號
	private StringProperty filePage = new SimpleStringProperty(""); //此影像子類別中紀錄總頁數的所屬頁碼
	private StringProperty boxNo = new SimpleStringProperty(""); //箱號
	private StringProperty batchDepType = new SimpleStringProperty(""); //批次號碼-部門別; //ex:NB:NB
	private StringProperty batchDate = new SimpleStringProperty(""); //日期
	private StringProperty batchArea = new SimpleStringProperty(""); //分區
	private StringProperty batchDocType = new SimpleStringProperty(""); //文件別
	private StringProperty companyCode = new SimpleStringProperty(""); //公司碼(團險保單碼碼)
	private StringProperty personalCode = new SimpleStringProperty(""); //團險中個人碼
	private StringProperty actionReplace = new SimpleStringProperty(""); //此文件是否取代文件
	private StringProperty actionInsert = new SimpleStringProperty(""); //此文件是為插入文件
	private StringProperty sendEmail = new SimpleStringProperty(""); //是否發EMAIL
	private StringProperty isRemote = new SimpleStringProperty(""); //此文件是為視訊投保件
	private StringProperty remark = new SimpleStringProperty(""); //影像備註
	private StringProperty fileName = new SimpleStringProperty(""); //檔案名稱

	// TableView 畫面中的資訊
	private StringProperty scanOrder = new SimpleStringProperty(""); //掃描順序(序號)
	private StringProperty actionType = new SimpleStringProperty(""); //備註
	private StringProperty mainFileTypeText = new SimpleStringProperty(""); //影像主類型; // ex:UNB-新契約
	private StringProperty fileTypeText = new SimpleStringProperty(""); //影像子類型；ex：UNBA040-傳統型保險要保書

	// 登入後 Server 端回傳的資訊
	private StringProperty fromQueryPage = new SimpleStringProperty(""); //是否從查詢網頁帶起此控件; //1:是:true; //2:否:false
	private StringProperty imageSaveDir = new SimpleStringProperty(""); //影像掃瞄文件儲資料夾位置
	private StringProperty bizDept = new SimpleStringProperty(""); //商業部門; //ex: NB:NB
	private StringProperty isGID = new SimpleStringProperty(""); //是否為團險案件
	private StringProperty rocDate = new SimpleStringProperty(""); //紀錄日期
	private StringProperty orgCode = new SimpleStringProperty(""); //組織代碼ex:101
	private StringProperty updateRole = new SimpleStringProperty(""); //登入使用者是否有異動權限
	private StringProperty batchDepTypeValue = new SimpleStringProperty(""); //批次號碼-部門別; //ex:NB:NB
	private StringProperty recordStatus = new SimpleStringProperty(""); //文件紀錄狀態; //1:儲存; //0:未儲存
	private StringProperty empId = new SimpleStringProperty(""); //員工編號
	private StringProperty step = new SimpleStringProperty(""); //
	private StringProperty imageFormat = new SimpleStringProperty(""); //
	private StringProperty deptId = new SimpleStringProperty(""); //登入使用者所屬部門編號

	// 額外記錄的資訊
	private StringProperty signature = new SimpleStringProperty(""); //是否有簽名檔
	private StringProperty sigSeqNumber = new SimpleStringProperty(""); //此文件為紀錄截取簽名檔中的序號,
	private StringProperty maxPage = new SimpleStringProperty(""); //文件類別中最大頁數
	private StringProperty scanTime = new SimpleStringProperty(""); //掃瞄日期時間
	private StringProperty fileURL = new SimpleStringProperty(""); //實體tiff檔案位置

	// 暫存用
	private IntegerProperty indexNo = new SimpleIntegerProperty(); //TableView中的索引
	private IntegerProperty uploadStatus = new SimpleIntegerProperty(); //記錄上傳狀態
	private StringProperty pageAction = new SimpleStringProperty(""); //上傳用暫存資訊
	private StringProperty allowReserved = new SimpleStringProperty(""); //上傳用暫存資訊

	public ScannedImage() {
	}

	public ScannedImage(String _orgName, String _deptName, String _mainFileType, String _fileType, String _fileCode,
			String _filePage, String _boxNo, String _batchDepType, String _batchDate, String _batchArea,
			String _batchDocType, String _companyCode, String _personalCode, String _actionReplace,
			String _actionInsert, String _sendEmail, String _isRemote, String _remark, String _fileName, String _scanOrder,
			String _actionType, String _mainFileTypeText, String _fileTypeText, String _fromQueryPage,
			String _imageSaveDir, String _bizDept, String _isGID, String _rocDate, String _orgCode, String _updateRole,
			String _batchDepTypeValue, String _recordStatus, String _empId, String _step, String _imageFormat,
			String _deptId, String _signature, String _sigSeqNumber, String _maxPage, String _scanTime, String _fileURL,
			Integer _indexNo, String _pageAction, String _allowReserved) {
		this.remark.set(_remark);
		this.fileName.set(_fileName);
		this.mainFileType.set(_mainFileType);
		this.batchDate.set(_batchDate);
		this.actionReplace.set(_actionReplace);
		this.batchDepType.set(_batchDepType);
		this.orgName.set(_orgName);
		this.deptName.set(_deptName);
		this.boxNo.set(_boxNo);
		this.personalCode.set(_personalCode);
		this.fileType.set(_fileType);
		this.fileCode.set(_fileCode);
		this.actionInsert.set(_actionInsert);
		this.filePage.set(_filePage);
		this.companyCode.set(_companyCode);
		this.batchArea.set(_batchArea);
		this.batchDocType.set(_batchDocType);
		this.scanOrder.set(_scanOrder);
		this.mainFileTypeText.set(_mainFileTypeText);
		this.fileTypeText.set(_fileTypeText);
		this.actionType.set(_actionType);
		this.fromQueryPage.set(_fromQueryPage);
		this.imageSaveDir.set(_imageSaveDir);
		this.bizDept.set(_bizDept);
		this.isGID.set(_isGID);
		this.rocDate.set(_rocDate);
		this.orgCode.set(_orgCode);
		this.updateRole.set(_updateRole);
		this.batchDepTypeValue.set(_batchDepTypeValue);
		this.recordStatus.set(_recordStatus);
		this.empId.set(_empId);
		this.step.set(_step);
		this.imageFormat.set(_imageFormat);
		this.deptId.set(_deptId);
		this.signature.set(_signature);
		this.sigSeqNumber.set(_sigSeqNumber);
		this.maxPage.set(_maxPage);
		this.scanTime.set(_scanTime);
		this.fileURL.set(_fileURL);
		this.indexNo.set(_indexNo);
		this.pageAction.set(_pageAction);
		this.allowReserved.set(_allowReserved);
		this.sendEmail.set(_sendEmail);
		this.isRemote.set(_isRemote);
		this.remark.set(_remark);
	}

	public StringProperty remarkProperty() {
		return remark;
	}

	public StringProperty fileNameProperty() {
		return fileName;
	}

	public StringProperty mainFileTypeProperty() {
		return mainFileType;
	}

	public StringProperty batchDateProperty() {
		return batchDate;
	}

	public StringProperty actionReplaceProperty() {
		return actionReplace;
	}

	public StringProperty batchDepTypeProperty() {
		return batchDepType;
	}

	public StringProperty orgNameProperty() {
		return orgName;
	}

	public StringProperty deptNameProperty() {
		return deptName;
	}

	public StringProperty boxNoProperty() {
		return boxNo;
	}

	public StringProperty personalCodeProperty() {
		return personalCode;
	}

	public StringProperty fileTypeProperty() {
		return fileType;
	}

	public StringProperty fileCodeProperty() {
		return fileCode;
	}

	public StringProperty actionInsertProperty() {
		return actionInsert;
	}

	public StringProperty filePageProperty() {
		return filePage;
	}

	public StringProperty companyCodeProperty() {
		return companyCode;
	}

	public StringProperty batchAreaProperty() {
		return batchArea;
	}

	public StringProperty batchDocTypeProperty() {
		return batchDocType;
	}

	public StringProperty sendEmailProperty() {
		return sendEmail;
	}

	public StringProperty isRemoteProperty() {
		return isRemote;
	}

	public StringProperty scanOrderProperty() {
		return scanOrder;
	}

	public StringProperty mainFileTypeTextProperty() {
		return mainFileTypeText;
	}

	public StringProperty fileTypeTextProperty() {
		return fileTypeText;
	}

	public StringProperty actionTypeProperty() {
		return actionType;
	}

	public StringProperty fromQueryPageProperty() {
		return fromQueryPage;
	}

	public StringProperty imageSaveDirProperty() {
		return imageSaveDir;
	}

	public StringProperty bizDeptProperty() {
		return bizDept;
	}

	public StringProperty isGIDProperty() {
		return isGID;
	}

	public StringProperty rocDateProperty() {
		return rocDate;
	}

	public StringProperty orgCodeProperty() {
		return orgCode;
	}

	public StringProperty updateRoleProperty() {
		return updateRole;
	}

	public StringProperty batchDepTypeValueProperty() {
		return batchDepTypeValue;
	}

	public StringProperty recordStatusProperty() {
		return recordStatus;
	}

	public StringProperty empIdProperty() {
		return empId;
	}

	public StringProperty stepProperty() {
		return step;
	}

	public StringProperty imageFormatProperty() {
		return imageFormat;
	}

	public StringProperty deptIdProperty() {
		return deptId;
	}

	public StringProperty signatureProperty() {
		return signature;
	}

	public StringProperty sigSeqNumberProperty() {
		return sigSeqNumber;
	}

	public StringProperty maxPageProperty() {
		return maxPage;
	}

	public StringProperty scanTimeProperty() {
		return scanTime;
	}

	public StringProperty fileURLProperty() {
		return fileURL;
	}

	public IntegerProperty indexNoProperty() {
		return indexNo;
	}

	public IntegerProperty uploadStatusProperty() {
		return uploadStatus;
	}

	public StringProperty pageActionProperty() {
		return pageAction;
	}

	public StringProperty allowReservedProperty() {
		return allowReserved;
	}

	public String toLogString() {
		StringBuffer sb = new StringBuffer();

		sb.append("  Image File : " + fileName.get() + "\r\n");
		sb.append("  orgCode : " + orgCode.get() + "\r\n");
		sb.append("  orgName : " + orgName.get() + "\r\n");
		sb.append("  deptId : " + deptId.get() + "\r\n");
		sb.append("  deptName : " + deptName.get() + "\r\n");
		sb.append("  mainFileType : " + mainFileType.get() + "\r\n");
		sb.append("  mainFileTypeText : " + mainFileTypeText.get() + "\r\n");
		sb.append("  fileType : " + fileType.get() + "\r\n");
		sb.append("  fileTypeText : " + fileTypeText.get() + "\r\n");
		sb.append("  fileCode : " + fileCode.get() + "\r\n");
		sb.append("  filePage : " + filePage.get() + "\r\n");
		sb.append("  boxNo : " + boxNo.get() + "\r\n");
		sb.append("  batchDepType : " + batchDepType.get() + "\r\n");
		sb.append("  batchDepTypeValue : " + batchDepTypeValue.get() + "\r\n");
		sb.append("  batchDate : " + batchDate.get() + "\r\n");
		sb.append("  batchArea : " + batchArea.get() + "\r\n");
		sb.append("  batchDocType : " + batchDocType.get() + "\r\n");
		sb.append("  companyCode : " + companyCode.get() + "\r\n");
		sb.append("  personalCode : " + personalCode.get() + "\r\n");
		sb.append("  actionReplace : " + actionReplace.get() + "\r\n");
		sb.append("  actionInsert : " + actionInsert.get() + "\r\n");
		sb.append("  isRemote : " + isRemote.get() + "\r\n");
		sb.append("  sendEmail : " + sendEmail.get() + "\r\n");
		sb.append("  remark : " + remark.get() + "\r\n");
		sb.append("  scanOrder : " + scanOrder.get() + "\r\n");
		sb.append("  bizDept : " + bizDept.get() + "\r\n");
		sb.append("  isGID : " + isGID.get() + "\r\n");
		sb.append("  empId : " + empId.get() + "\r\n");
		sb.append("  rocDate : " + rocDate.get() + "\r\n");
		sb.append("  updateRole : " + updateRole.get() + "\r\n");
		sb.append("  step : " + step.get() + "\r\n");
		sb.append("  recordStatus : " + recordStatus.get() + "\r\n");
		sb.append("  fromQueryPage : " + fromQueryPage.get() + "\r\n");
		sb.append("  imageFormat : " + imageFormat.get() + "\r\n");
		sb.append("  imageSaveDir : " + imageSaveDir.get() + "\r\n");
		sb.append("  signature : " + signature.get() + "\r\n");
		sb.append("  sigSeqNumber : " + sigSeqNumber.get() + "\r\n");
		sb.append("  maxPage : " + maxPage.get() + "\r\n");
		sb.append("  scanTime : " + scanTime.get() + "\r\n");
		sb.append("  actionType : " + actionType.get() + "\r\n");

		return sb.toString();
	}

}
