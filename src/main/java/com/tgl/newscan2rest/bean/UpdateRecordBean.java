package com.tgl.newscan2rest.bean;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UpdateRecordBean {

	private int selectedIndex;
	private String mainFileTypeValue;
	private String mainFileTypeText;
	private String fileTypeValue;
	private String fileTypeText;
	private String fileCode;
	private String filePage;
	private String companyCode;
	private String personalCode;
	private String actionReplace;
	private String actionInsert;
	private String orgName; 
	private String deptName;
	private String boxNo;
	private String batchDepType;
	private String batchDate;
	private String batchArea;
	private String batchDocType;
	private String sendEmail;
	private String isRemote;
	private String remark;
	
	
	public int getSelectedIndex() {
		return selectedIndex;
	}
	public void setSelectedIndex(int selectedIndex) {
		this.selectedIndex = selectedIndex;
	}
	public String getMainFileTypeValue() {
		return mainFileTypeValue;
	}
	public void setMainFileTypeValue(String mainFileTypeValue) {
		this.mainFileTypeValue = mainFileTypeValue;
	}
	public String getMainFileTypeText() {
		return mainFileTypeText;
	}
	public void setMainFileTypeText(String mainFileTypeText) {
		this.mainFileTypeText = mainFileTypeText;
	}
	public String getFileTypeValue() {
		return fileTypeValue;
	}
	public void setFileTypeValue(String fileTypeValue) {
		this.fileTypeValue = fileTypeValue;
	}
	public String getFileTypeText() {
		return fileTypeText;
	}
	public void setFileTypeText(String fileTypeText) {
		this.fileTypeText = fileTypeText;
	}
	public String getFileCode() {
		return fileCode;
	}
	public void setFileCode(String fileCode) {
		this.fileCode = fileCode;
	}
	public String getFilePage() {
		return filePage;
	}
	public void setFilePage(String filePage) {
		this.filePage = filePage;
	}
	public String getCompanyCode() {
		return companyCode;
	}
	public void setCompanyCode(String companyCode) {
		this.companyCode = companyCode;
	}
	public String getPersonalCode() {
		return personalCode;
	}
	public void setPersonalCode(String personalCode) {
		this.personalCode = personalCode;
	}
	public String getActionReplace() {
		return actionReplace;
	}
	public void setActionReplace(String actionReplace) {
		this.actionReplace = actionReplace;
	}
	public String getActionInsert() {
		return actionInsert;
	}
	public void setActionInsert(String actionInsert) {
		this.actionInsert = actionInsert;
	}
	public String getOrgName() {
		return orgName;
	}
	public void setOrgName(String orgName) {
		this.orgName = orgName;
	}
	public String getDeptName() {
		return deptName;
	}
	public void setDeptName(String deptName) {
		this.deptName = deptName;
	}
	public String getBoxNo() {
		return boxNo;
	}
	public void setBoxNo(String boxNo) {
		this.boxNo = boxNo;
	}
	public String getBatchDepType() {
		return batchDepType;
	}
	public void setBatchDepType(String batchDepType) {
		this.batchDepType = batchDepType;
	}
	public String getBatchDate() {
		return batchDate;
	}
	public void setBatchDate(String batchDate) {
		this.batchDate = batchDate;
	}
	public String getBatchArea() {
		return batchArea;
	}
	public void setBatchArea(String batchArea) {
		this.batchArea = batchArea;
	}
	public String getBatchDocType() {
		return batchDocType;
	}
	public void setBatchDocType(String batchDocType) {
		this.batchDocType = batchDocType;
	}
	public String getSendEmail() {
		return sendEmail;
	}
	public void setSendEmail(String sendEmail) {
		this.sendEmail = sendEmail;
	}
	public String getIsRemote() {
		return isRemote;
	}
	public void setIsRemote(String isRemote) {
		this.isRemote = isRemote;
	}
	public String getRemark() {
		return remark;
	}
	public void setRemark(String remark) {
		this.remark = remark;
	}
	

}
