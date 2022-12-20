package com.tgl.newscan2rest.bean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.asprise.imaging.core.scan.twain.TwainConstants;
import com.tgl.newscan2rest.util.ScanUtil;

import javafx.util.Pair;

/**
 * 對映由server 端取回的ScanConfig.xml內容
 * 
 * @author Jim Chin
 *
 */
public class ScanConfig {

	private static final Logger logger = LogManager.getLogger(ScanConfig.class);

	private String uploadURL;
	private String orgName;           // 組織編碼 value="101-台北總公司"
	private String deptName;          // 部室名稱" columnnum="2" value="契約行政科">
	private String mainFileType;      // 影像主類型" columnnum="1">
	private String fileType;          // 影像子類型" columnnum="3" value="">
	private String fileCode;          // "文件編號" columnnum="2">
	private String filePage;          // 頁碼" columnnum="2">
	private String boxNo;             // 箱號" columnnum="4">
	private String batchDepType;      // 批次號碼-部門別" columnnum="1" value="">
	private String batchDate;         // "日期" width="70" columnnum="1" value="">//長度小於7補0，大於7取字串前7碼
	private String batchArea;         // 分區" columnnum="1"> //長度為1,十位數補零;長度大於2，取前字串面2碼
	private String batchDocType;      // 文件別" columnnum="1">//長度為1,十位數補零;長度大於2，取字串前面2碼
	private String defBoxNo;
	private String defBatchDepType;
	private String defBatchDate;
	private String defBatchArea;
	private String defBatchDocType;
	private String companyCode;       // 公司碼(團險保單號碼)" columnnum="2">//預設個人碼=公司碼
	private String personalCode;      // 個人碼" columnnum="2">//預設個人碼=公司碼;長度小於6補0，大於6取字串前7碼
	private String actionReplace;     // 替換" columnnum="1" value="N">
	private String actionInsert;      // 插入" columnnum="1" value="N">
	private String remark;            // 影像備註" columnnum="4">
	private String orgCode;           // "101" columnnum="1"/>
	private String empId;             // 100423" columnnum="1"/>
	private String deptId;            // 2266" columnnum="1"/>
	private String actionType;
	private String step;
	private String imageSaveDir;      // D:\ImageArchive" columnnum="1"/>
	private String recordStatus;
	private String imageFormat;
	private String rocDate;           // 1091105" columnnum="1"/>
	private String updateRole;        // value="Y" columnnum="1"/>
	private String fromQueryPage;     // value="false" columnnum="1"/>
	private String isGID;             // value="null" columnnum="1"/>
	private String bizDept;           // value="NB" columnnum="1"/>
	private String batchDepTypeValue; // value="NB" columnnum="1"/>

	private boolean isFromQueryPage = false;
	private boolean isDeptNB = false;
	private boolean isDeptPos = false;
	private boolean isDeptGid = false;

	// billcards 影像子類型中文文件類型
	private Map<String, BillCard> billCards = new HashMap<String, BillCard>();
	private Map<String, Map<String, BillCard>> mainBillCards = new HashMap<String, Map<String, BillCard>>();
	// 箱號
	private List<String> boxNos = new ArrayList<String>();

	// stringtable 控件i18N文字
	private Map<String, String> stringtables = new HashMap<String, String>();

	// imagerecordcolumns 控件i18N影像佇列文字
	private Map<String, String> imagerecordcolumns = new HashMap<String, String>();

	// signaturerule紀錄截取簽名檔位置
	// Map<subfilecode-pageno, List<SignatureImgRule>>
	private Map<String, List<SignatureImgRule>> signaturerule = new HashMap<String, List<SignatureImgRule>>();

	// FileType
	private Map<String, List<Pair<String, String>>> totalFileTypes = new HashMap<String, List<Pair<String, String>>>();

	private List<Pair<String, String>> mainFileTypeList = new ArrayList<Pair<String, String>>();

	private List<Pair<String, String>> batchDepTypeList = new ArrayList<Pair<String, String>>();

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

	public String getMainFileType() {
		return mainFileType;
	}

	public void setMainFileType(String mainFileType) {
		this.mainFileType = mainFileType;
	}

	public List<Pair<String, String>> getMainFileTypeList() {
		return mainFileTypeList;
	}

	public void setMainFileTypeList(List<Pair<String, String>> list) {
		this.mainFileTypeList = list;
	}

	public String getFileType() {
		return fileType;
	}

	public void setFileType(String fileType) {
		this.fileType = fileType;
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

	public String getDefBoxNo() {
		return defBoxNo;
	}

	public void setDefBoxNo(String defBoxNo) {
		this.defBoxNo = defBoxNo;
	}

	public String getDefBatchDepType() {
		return defBatchDepType;
	}

	public void setDefBatchDepType(String defBatchDepType) {
		this.defBatchDepType = defBatchDepType;
	}

	public String getDefBatchDate() {
		return defBatchDate;
	}

	public void setDefBatchDate(String defBatchDate) {
		this.defBatchDate = defBatchDate;
	}

	public String getDefBatchArea() {
		return defBatchArea;
	}

	public void setDefBatchArea(String defBatchArea) {
		this.defBatchArea = defBatchArea;
	}

	public String getDefBatchDocType() {
		return defBatchDocType;
	}

	public void setDefBatchDocType(String defBatchDocType) {
		this.defBatchDocType = defBatchDocType;
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

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public String getOrgCode() {
		return orgCode;
	}

	public void setOrgCode(String orgCode) {
		this.orgCode = orgCode;
	}

	public String getEmpId() {
		return empId;
	}

	public void setEmpId(String empId) {
		this.empId = empId;
	}

	public String getDeptId() {
		return deptId;
	}

	public void setDeptId(String deptId) {
		this.deptId = deptId;
		if ( "2116".equals(deptId) || "2117".equals(deptId) ) {
			isDeptPos = true;
		} else {
			isDeptPos = false;
		}
	}

	public boolean isDeptPos() {
		return isDeptPos;
	}

	public String getActionType() {
		return actionType;
	}

	public void setActionType(String actionType) {
		this.actionType = actionType;
	}

	public String getStep() {
		return step;
	}

	public void setStep(String step) {
		this.step = step;
	}

	public String getImageSaveDir() {
		return imageSaveDir;
	}

	public void setImageSaveDir(String imageSaveDir) {
		this.imageSaveDir = imageSaveDir;
	}

	public String getRecordStatus() {
		return recordStatus;
	}

	public void setRecordStatus(String recordStatus) {
		this.recordStatus = recordStatus;
	}

	public String getImageFormat() {
		return imageFormat;
	}

	public void setImageFormat(String imageFormat) {
		this.imageFormat = imageFormat;
	}

	public String getRocDate() {
		return rocDate;
	}

	public void setRocDate(String rocDate) {
		this.rocDate = rocDate;
	}

	public String getUpdateRole() {
		return updateRole;
	}

	public void setUpdateRole(String updateRole) {
		this.updateRole = updateRole;
	}

	public String getFromQueryPage() {
		return fromQueryPage;
	}

	public void setFromQueryPage(String fromQueryPage) {
		this.fromQueryPage = fromQueryPage;
		if ( fromQueryPage != null ) {
			isFromQueryPage = Boolean.valueOf(fromQueryPage);
		}
	}

	public boolean isFromQueryPage() {
		return isFromQueryPage;
	}

	public String getIsGID() {
		return isGID;
	}

	public void setIsGID(String isGID) {
		if ( "Y".equals(isGID) ) {
			this.isGID = isGID;
			isDeptGid = true;
		} else {
			this.isGID = "N";
			isDeptGid = false;
		}
	}

	public boolean isDeptGid() {
		return isDeptGid;
	}

	public String getBizDept() {
		return bizDept;
	}

	public void setBizDept(String bizDept) {
		this.bizDept = bizDept;
		if ( "NB".equals(bizDept) ) {
			ScanUtil.setScanDuplex(TwainConstants.TWDX_2PASSDUPLEX);
			isDeptNB = true;
		} else {
			ScanUtil.setScanDuplex(TwainConstants.TWDX_NONE);
			isDeptNB = false;
		}
	}

	public boolean isDeptNB() {
		return isDeptNB;
	}

	public String getBatchDepTypeValue() {
		return batchDepTypeValue;
	}

	public void setBatchDepTypeValue(String batchDepTypeValue) {
		this.batchDepTypeValue = batchDepTypeValue;
	}

	public Map<String, BillCard> getBillCards() {
		return billCards;
	}

	public void setBillCards(Map<String, BillCard> billCards) {
		this.billCards = billCards;
	}

	public String getDescByCardCode(String cardCode) {
		String cardDesc = null;
		if (null!=this.billCards && this.billCards.size()>0) {
			BillCard billCard = this.billCards.get(cardCode);
			if (null!=billCard) {
				cardDesc = billCard.getCardDesc();
			}
		}
		return cardDesc;
	}

	public String getMaxPageByCardCode(String cardCode) {
		String maxPage = null;
		if (null!=this.billCards && this.billCards.size()>0) {
			BillCard billCard = this.billCards.get(cardCode);
			if (null!=billCard) {
				maxPage = billCard.getMaxPage();
			}
		}
		return maxPage;
	}

	public Map<String, Map<String, BillCard>> getMainBillCards() {
		return mainBillCards;
	}

	public void setMainBillCards(Map<String, Map<String, BillCard>> mainBillCards) {
		this.mainBillCards = mainBillCards;
	}

	public List<String> getBoxNos() {
		return boxNos;
	}

	public void setBoxNos(List<String> boxNos) {
		this.boxNos = boxNos;
	}

	public List<Pair<String, String>> getBatchDepTypeList() {
		return batchDepTypeList;
	}

	public void setBatchDepTypeList(List<Pair<String, String>> list) {
		batchDepTypeList = list;
	}

	public Map<String, String> getStringtables() {
		return stringtables;
	}

	public String getValueInTable(String key, String defaultValue) {
		String value = null;
		if (this.stringtables != null && this.stringtables.containsKey(key)) {
			value = this.stringtables.get(key);
		} else {
			value = defaultValue;
		}
		return value;
	}

	public void setStringtables(Map<String, String> stringtables) {
		this.stringtables = stringtables;
	}

	public Map<String, List<SignatureImgRule>> getSignatureRules() {
		return signaturerule;
	}

	public void setSignatureRules(Map<String, List<SignatureImgRule>> signaturerule) {
		this.signaturerule = signaturerule;
	}

	public Map<String, String> getImagerecordcolumns() {
		return imagerecordcolumns;
	}

	public void setImagerecordcolumns(Map<String, String> imagerecordcolumns) {
		this.imagerecordcolumns = imagerecordcolumns;
	}

	public String getUploadURL() {
		return uploadURL;
	}

	public void setUploadURL(String uploadURL) {
		this.uploadURL = uploadURL;
	}

	public Map<String, List<Pair<String, String>>> getTotalFileTypes() {
		return totalFileTypes;
	}

	public void setTotalFileTypes(Map<String, List<Pair<String, String>>> totalFileTypes) {
		this.totalFileTypes = totalFileTypes;
	}

	public void resetDefValues(String defBoxNo, String defBatchDeptType, String defBatchDate, String defBatchArea, String defBatchDocType) {
		if (logger.isDebugEnabled()) {
			logger.debug("defBoxNo={}, defBatchDeptType={}, defBatchDate={}, defBatchArea={}, defBatchDocType={}", defBoxNo, defBatchDeptType, defBatchDate, defBatchArea, defBatchDocType);
		}

		setDefBoxNo(defBoxNo);
		setDefBatchDepType(defBatchDeptType);
		setDefBatchDate(defBatchDate);
		setDefBatchArea(defBatchArea);
		setDefBatchDocType(defBatchDocType);
	}

}
