package com.tgl.newscan2rest.bean;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class PageWarning {

	private StringProperty scanOrder = new SimpleStringProperty(""); //序號
	private StringProperty fileCode = new SimpleStringProperty(""); //文件編號
	private StringProperty mainFileType = new SimpleStringProperty(""); //影像主類型
	private StringProperty fileType = new SimpleStringProperty(""); //影像子類型
	private StringProperty companyCode = new SimpleStringProperty(""); //公司碼(團險保單號碼)
	private StringProperty personalCode = new SimpleStringProperty(""); //個人碼
	private StringProperty filePage = new SimpleStringProperty(""); //頁碼
	private StringProperty scanTime = new SimpleStringProperty(""); //掃描日期/時間
	private StringProperty remark = new SimpleStringProperty(""); //備註
	private IntegerProperty indexNo = new SimpleIntegerProperty(); //隱藏的索引欄位

	public PageWarning() {
	}

	public PageWarning(String _scanOrder, String _fileCode, String _mainFileType, String _fileType, String _companyCode, 
					   String _personalCode, String _filePage, String _scanTime, String _remark, Integer _indexNo) {
		this.scanOrder.set(_scanOrder);
		this.fileCode.set(_fileCode);
		this.mainFileType.set(_mainFileType);
		this.fileType.set(_fileType);
		this.companyCode.set(_companyCode);
		this.personalCode.set(_personalCode);
		this.filePage.set(_filePage);
		this.scanTime.set(_scanTime);
		this.remark.set(_remark);
		this.indexNo.set(_indexNo);
	}

	public StringProperty scanOrderProperty() {
		return scanOrder;
	}

	public StringProperty fileCodeProperty() {
		return fileCode;
	}

	public StringProperty mainFileTypeProperty() {
		return mainFileType;
	}

	public StringProperty fileTypeProperty() {
		return fileType;
	}

	public StringProperty companyCodeProperty() {
		return companyCode;
	}

	public StringProperty personalCodeProperty() {
		return personalCode;
	}

	public StringProperty filePageProperty() {
		return filePage;
	}

	public StringProperty scanTimeProperty() {
		return scanTime;
	}

	public StringProperty remarkProperty() {
		return remark;
	}

	public IntegerProperty indexNoProperty() {
		return indexNo;
	}
	
}
