package com.tgl.newscan2rest.bean;

/**
 * ImageRecordSet.xml(掃描文件紀錄檔)內每筆文件metadata屬性名稱
 * @author Jim Chin
 *
 */
public enum RecordField {

	// Form 畫面中的資訊
	OrgName,//組織編碼 	
	DeptName,//部室名稱
	MainFileType,//影像主類型; ex:UNB
	FileType,//影像子類型；ex：UNBA040
	FileCode,//文件編號
	FilePage,//此影像子類別中紀錄總頁數的所屬頁碼
	BoxNo,//箱號
	BatchDepType,//批次號碼-部門別;ex:NB
	BatchDate,//日期
	BatchArea,//分區
	BatchDocType,//文件別
	CompanyCode,//公司碼(團險保單碼碼)
	PersonalCode,//團險中個人碼
	ActionReplace,//此文件是否替換文件
	ActionInsert,//此文件是為插入文件
	SendEmail,//是否發EMAIL
	IsRemote,//視訊投保件
	Remark,//影像備註
	fileName,//檔案名稱

	// TableView 畫面中的資訊
	ScanOrder,//掃描順序(序號)
	actionType,//備註

	// 登入後 Server 端回傳的資訊
	fromQueryPage,//是否從查詢網頁帶起此控件;1:是:true;2:否:false
	imageSaveDir,//影像掃瞄文件儲資料夾位置
	bizDept,//商業部門;ex: NB:NB
	isGID,//是否為團險案件
	RocDate,//紀錄日期
	orgCode,//組織代碼ex:101
	updateRole,//登入使用者是否有異動權限
	batchDepTypeValue,//批次號碼-部門別;ex:NB:NB
	recordStatus,//文件紀錄狀態;1:儲存;0:未儲存
	empId,//員工編號
	step,//
	ImageFormat,//
	deptId,//登入使用者所屬部門編號

	// 額外記錄的資訊
	Signature,//是否有簽名檔
	SigSeqNumber,//此文件為紀錄截取簽名檔中的序號,
	MaxPage,//文件類別中最大頁數
	ScanTime//掃瞄日期時間

}
