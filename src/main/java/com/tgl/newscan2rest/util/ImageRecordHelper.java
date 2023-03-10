package com.tgl.newscan2rest.util;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tgl.newscan2rest.Constant;
import com.tgl.newscan2rest.bean.BillCard;
import com.tgl.newscan2rest.bean.ImageRecordSet;
import com.tgl.newscan2rest.bean.RecordField;
import com.tgl.newscan2rest.bean.ScanConfig;
import com.tgl.newscan2rest.bean.ScannedImage;
import com.tgl.newscan2rest.bean.TiffField;
import com.tgl.newscan2rest.bean.TiffRecord;
import com.tgl.newscan2rest.bean.TiffRecords;
import com.tgl.newscan2rest.exception.Scan2Exception;
import com.tgl.newscan2rest.exception.Scan2Exception.ErrorCode;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.util.Pair;

public class ImageRecordHelper {

    private static final Logger logger = LoggerFactory.getLogger(ImageRecordHelper.class);

    public static final String CONFIG_DIR = System.getProperty("user.dir") + File.separator + "config";
    public static final String IMAGE_ARCHIVE_DIR = System.getProperty("user.dir") + File.separator + "image-archive";
    public static final String RECORD_SET_FILE_NAME = "imagerecordset.xml";
    public static final String RECORD_SET_FILE_FULL_NAME = CONFIG_DIR + File.separator + RECORD_SET_FILE_NAME;

	private static String currentMD5;

	private static ImageRecordHelper instance  = new ImageRecordHelper();

	public static ImageRecordHelper getInstance() {
		return ImageRecordHelper.instance;
	}

	private ImageRecordHelper() {
		ImageRecordHelper.currentMD5 = null;
	}

	public void removeUnusedFiles(List<String> imageRecordList) {
		if ( imageRecordList == null) {
			imageRecordList = new ArrayList<String>();
		}

		if ( logger.isDebugEnabled() ) {
			logger.debug("Remove unused images!");
		}

		List<File> files = null;
		try {
			files = Files.list(Paths.get(IMAGE_ARCHIVE_DIR))
		        .filter(Files::isRegularFile)
		        .filter(path -> ( path.toString().toLowerCase().endsWith(Constant.FILE_EXT_TIF) || 
		        				  path.toString().toLowerCase().endsWith(Constant.FILE_EXT_TIFF) || 
		        				  path.toString().toLowerCase().endsWith(Constant.FILE_EXT_JPG) || 
		        				  path.toString().toLowerCase().endsWith(Constant.FILE_EXT_JPEG) ) )
		        .map(Path::toFile)
		        .collect(Collectors.toList());
		} catch (IOException e) {
			logger.error(String.format("????????? %s ??????????????????????????????", IMAGE_ARCHIVE_DIR), e);
		}

		if ( files == null || imageRecordList == null) {
			return;
		}

		for ( File file : files ) {
			String fileName = file.getName();
			if ( !imageRecordList.contains(fileName) ) {
				try {
					logger.debug("Remove image file " + file.toPath());
					Files.delete(file.toPath());
				} catch (IOException e) {
					logger.error(String.format("Failed to delete  %s !", file.getAbsolutePath()), e);
				}
			}
		}
	}

	public boolean xmlFileExists() {
		Path xmlFile = Paths.get(RECORD_SET_FILE_FULL_NAME);
		return Files.exists(xmlFile);
	}

	public String backupXmlFile() throws IOException {
		Path xmlFile = Paths.get(RECORD_SET_FILE_FULL_NAME);
		Path newXmlFile = null;
		int index = 1;

		while (true) {
			newXmlFile = Paths.get(xmlFile.toString() + "." + index++);
			if (Files.notExists(newXmlFile)) {
				break;
			}
		}
		Files.copy(xmlFile, newXmlFile);

		return newXmlFile.toString();
	}

	public String getXmlFileMD5() {
		return DigestUtils.getXmlFileMD5(RECORD_SET_FILE_FULL_NAME);
	}

	public boolean xmlFileChanged() {
		String md5 = getXmlFileMD5();
		return ObjectsUtil.isNotEquals(md5, currentMD5);
	}

	public ScannedImage convert(TiffRecord tiffRecord) {
		ScannedImage imageTiff = new ScannedImage();
		String fileUrl = null;

		for (TiffField field : tiffRecord.getFields()) {
			String fieldName = field.getName();
			if ( RecordField.OrgName.name().equals(fieldName) ) {
				imageTiff.orgNameProperty().setValue(field.getText());
			} else if ( RecordField.DeptName.name().equals(fieldName) ) {
				imageTiff.deptNameProperty().setValue(field.getText());
			} else if ( RecordField.MainFileType.name().equals(fieldName) ) {
				imageTiff.mainFileTypeProperty().setValue(field.getValue());
				imageTiff.mainFileTypeTextProperty().setValue(field.getText());
			} else if ( RecordField.FileType.name().equals(fieldName) ) {
				imageTiff.fileTypeProperty().setValue(field.getValue());
				imageTiff.fileTypeTextProperty().setValue(field.getText());
			} else if ( RecordField.FileCode.name().equals(fieldName) ) {
				imageTiff.fileCodeProperty().setValue(field.getText());
			} else if ( RecordField.FilePage.name().equals(fieldName) ) {
				imageTiff.filePageProperty().setValue(field.getText());
			} else if ( RecordField.BoxNo.name().equals(fieldName) ) {
				imageTiff.boxNoProperty().setValue(field.getText());
			} else if ( RecordField.BatchDepType.name().equals(fieldName) ) {
				imageTiff.batchDepTypeProperty().setValue(field.getText());
			} else if ( RecordField.BatchDate.name().equals(fieldName) ) {
				imageTiff.batchDateProperty().setValue(field.getText());
			} else if ( RecordField.BatchArea.name().equals(fieldName) ) {
				imageTiff.batchAreaProperty().setValue(field.getText());
			} else if ( RecordField.BatchDocType.name().equals(fieldName) ) {
				imageTiff.batchDocTypeProperty().setValue(field.getText());
			} else if ( RecordField.CompanyCode.name().equals(fieldName) ) {
				imageTiff.companyCodeProperty().setValue(field.getText());
			} else if ( RecordField.PersonalCode.name().equals(fieldName) ) {
				imageTiff.personalCodeProperty().setValue(field.getText());
			} else if ( RecordField.ActionReplace.name().equals(fieldName) ) {
				imageTiff.actionReplaceProperty().setValue(field.getText());
			} else if ( RecordField.ActionInsert.name().equals(fieldName) ) {
				imageTiff.actionInsertProperty().setValue(field.getText());
			} else if ( RecordField.SendEmail.name().equals(fieldName) ) {
				imageTiff.sendEmailProperty().setValue(field.getText());
			} else if ( RecordField.IsRemote.name().equals(fieldName) ) {
				imageTiff.isRemoteProperty().setValue(field.getText());
			} else if ( RecordField.Remark.name().equals(fieldName) ) {
				imageTiff.remarkProperty().setValue(field.getText());
			} else if ( RecordField.fileName.name().equals(fieldName) ) {
				imageTiff.fileNameProperty().setValue(field.getText());
				fileUrl = IMAGE_ARCHIVE_DIR + File.separator + field.getText();
				imageTiff.fileURLProperty().setValue(fileUrl);
			} else if ( RecordField.ScanOrder.name().equals(fieldName) ) {
				imageTiff.scanOrderProperty().setValue(field.getText());
			} else if ( RecordField.actionType.name().equals(fieldName) ) {
				imageTiff.actionTypeProperty().setValue(field.getText());
			} else if ( RecordField.fromQueryPage.name().equals(fieldName) ) {
				imageTiff.fromQueryPageProperty().setValue(field.getText());
			} else if ( RecordField.imageSaveDir.name().equals(fieldName) ) {
				imageTiff.imageSaveDirProperty().setValue(field.getText());
			} else if ( RecordField.bizDept.name().equals(fieldName) ) {
				imageTiff.bizDeptProperty().setValue(field.getText());
			} else if ( RecordField.isGID.name().equals(fieldName) ) {
				imageTiff.isGIDProperty().setValue(field.getText());
			} else if ( RecordField.RocDate.name().equals(fieldName) ) {
				imageTiff.rocDateProperty().setValue(field.getText());
			} else if ( RecordField.orgCode.name().equals(fieldName) ) {
				imageTiff.orgCodeProperty().setValue(field.getText());
			} else if ( RecordField.updateRole.name().equals(fieldName) ) {
				imageTiff.updateRoleProperty().setValue(field.getText());
			} else if ( RecordField.batchDepTypeValue.name().equals(fieldName) ) {
				imageTiff.batchDepTypeValueProperty().setValue(field.getText());
			} else if ( RecordField.recordStatus.name().equals(fieldName) ) {
				imageTiff.recordStatusProperty().setValue(field.getText());
			} else if ( RecordField.empId.name().equals(fieldName) ) {
				imageTiff.empIdProperty().setValue(field.getText());
			} else if ( RecordField.step.name().equals(fieldName) ) {
				imageTiff.stepProperty().setValue(field.getText());
			} else if ( RecordField.ImageFormat.name().equals(fieldName) ) {
				imageTiff.imageFormatProperty().setValue(field.getText());
			} else if ( RecordField.deptId.name().equals(fieldName) ) {
				imageTiff.deptIdProperty().setValue(field.getText());
			} else if ( RecordField.Signature.name().equals(fieldName) ) {
				imageTiff.signatureProperty().setValue(field.getText());
			} else if ( RecordField.SigSeqNumber.name().equals(fieldName) ) {
				imageTiff.sigSeqNumberProperty().setValue(field.getText());
			} else if ( RecordField.MaxPage.name().equals(fieldName) ) {
				imageTiff.maxPageProperty().setValue(field.getText());
			} else if ( RecordField.ScanTime.name().equals(fieldName) ) {
				imageTiff.scanTimeProperty().setValue(field.getText());
			}
		}

		return imageTiff;
	}

	public void resetScanOrder(List<TiffRecord> recordList) {
		if (logger.isDebugEnabled() ) {
			logger.debug("resetScanOrder(), recordList={}", (null==recordList ? "null" : recordList.size()));
		}

		if (null==recordList || recordList.size()==0 ) {
			return;
		}
		int i = 0;
		for (TiffRecord tiffRecord : recordList) {
			i++;
			for (TiffField tiffField : tiffRecord.getFields()) {
				if ( tiffField.getName().equals(RecordField.ScanOrder.name()) ) {
					tiffField.setValue(String.valueOf(i));
					tiffField.setText(String.valueOf(i)); 
				}
			}
		}
	}

	public void saveTiffData(ScannedImage seletedItem, TiffRecord record) {
		List<TiffField> fields = record.getFields();

		// ??????????????????
		List<String> allFields = new ArrayList<String>();
		for (RecordField field : RecordField.values()) {
			allFields.add(field.name());
		}
		for (TiffField tiffField : fields) {
			String fieldName = tiffField.getName();
			allFields.remove(fieldName);
		}
		for (String fieldName : allFields) {
			TiffField tiffField = new TiffField();
			tiffField.setName(fieldName);
			fields.add(tiffField);
		}

		// ?????????????????????
		for (TiffField tiffField : fields) {
			String fieldName = tiffField.getName();

			if ( RecordField.OrgName.name().equals(fieldName) ) {
				tiffField.setValue(seletedItem.orgNameProperty().getValue()); 
				tiffField.setText(seletedItem.orgNameProperty().getValue()); 
			}
			else if ( RecordField.DeptName.name().equals(fieldName) ) {
				tiffField.setValue(seletedItem.deptNameProperty().getValue());
				tiffField.setText(seletedItem.deptNameProperty().getValue());
			}
			else if ( RecordField.MainFileType.name().equals(fieldName) ) {
				tiffField.setValue(seletedItem.mainFileTypeProperty().getValue());
				tiffField.setText(seletedItem.mainFileTypeTextProperty().getValue());
				allFields.remove(fieldName);
			}
			else if ( RecordField.FileType.name().equals(fieldName) ) {
				tiffField.setValue(seletedItem.fileTypeProperty().getValue());
				tiffField.setText(seletedItem.fileTypeTextProperty().getValue()); 
			}
			else if ( RecordField.FileCode.name().equals(fieldName) ) {
				tiffField.setValue(seletedItem.fileCodeProperty().getValue()); 
				tiffField.setText(seletedItem.fileCodeProperty().getValue()); 
			}
			else if ( RecordField.FilePage.name().equals(fieldName) ) {
				tiffField.setValue(seletedItem.filePageProperty().getValue()); 
				tiffField.setText(seletedItem.filePageProperty().getValue()); 
			}
			else if ( RecordField.BoxNo.name().equals(fieldName) ) {
				tiffField.setValue(seletedItem.boxNoProperty().getValue());
				tiffField.setText(seletedItem.boxNoProperty().getValue()); 
			}
			else if ( RecordField.BatchDepType.name().equals(fieldName) ) {
				tiffField.setValue(seletedItem.batchDepTypeProperty().getValue()); 
				tiffField.setText(seletedItem.batchDepTypeProperty().getValue());
			}
			else if ( RecordField.batchDepTypeValue.name().equals(fieldName) ) {
				seletedItem.batchDepTypeValueProperty().setValue(seletedItem.batchDepTypeProperty().getValue());
				tiffField.setValue(seletedItem.batchDepTypeValueProperty().getValue()); 
				tiffField.setText(seletedItem.batchDepTypeValueProperty().getValue());
			}
			else if ( RecordField.BatchDate.name().equals(fieldName) ) {
				tiffField.setValue(seletedItem.batchDateProperty().getValue()); 
				tiffField.setText(seletedItem.batchDateProperty().getValue()); 
			}
			else if ( RecordField.BatchArea.name().equals(fieldName) ) {
				tiffField.setValue(seletedItem.batchAreaProperty().getValue()); 
				tiffField.setText(seletedItem.batchAreaProperty().getValue()); 
			}
			else if ( RecordField.BatchDocType.name().equals(fieldName) ) {
				tiffField.setValue(seletedItem.batchDocTypeProperty().getValue()); 
				tiffField.setText(seletedItem.batchDocTypeProperty().getValue());						
			}
			else if ( RecordField.CompanyCode.name().equals(fieldName) ) {
				tiffField.setValue(seletedItem.companyCodeProperty().getValue()); 
				tiffField.setText(seletedItem.companyCodeProperty().getValue()); 
			}
			else if ( RecordField.PersonalCode.name().equals(fieldName) ) {
				tiffField.setValue(seletedItem.personalCodeProperty().getValue()); 
				tiffField.setText(seletedItem.personalCodeProperty().getValue()); 
			}
			else if ( RecordField.ActionReplace.name().equals(fieldName) ) {
				tiffField.setValue(seletedItem.actionReplaceProperty().getValue()); 
				tiffField.setText(seletedItem.actionReplaceProperty().getValue()); 
			}
			else if ( RecordField.ActionInsert.name().equals(fieldName) ) {
				tiffField.setValue(seletedItem.actionInsertProperty().getValue()); 
				tiffField.setText(seletedItem.actionInsertProperty().getValue()); 
			}
			else if ( RecordField.actionType.name().equals(fieldName) ) {
				tiffField.setValue(seletedItem.actionTypeProperty().getValue()); 
				tiffField.setText(seletedItem.actionTypeProperty().getValue()); 
			}
			else if ( RecordField.Remark.name().equals(fieldName) ) {
				tiffField.setValue(seletedItem.remarkProperty().getValue()); 
				tiffField.setText(seletedItem.remarkProperty().getValue()); 						
			}
			else if ( RecordField.SendEmail.name().equals(fieldName) ) {
				tiffField.setValue(seletedItem.sendEmailProperty().getValue()); 
				tiffField.setText(seletedItem.sendEmailProperty().getValue()); 
			}
			else if ( RecordField.IsRemote.name().equals(fieldName) ) {
				tiffField.setValue(seletedItem.isRemoteProperty().getValue()); 
				tiffField.setText(seletedItem.isRemoteProperty().getValue()); 
			}
		}

		record.setFields(fields);
	}

	public TiffRecord cloneTiffRecord(TiffRecord tiffRecord, String newFileName) throws IllegalAccessException, InvocationTargetException {
		TiffRecord newTiffRecord = new TiffRecord();
		newTiffRecord.setFileName(newFileName);
		List<TiffField> newFields = new ArrayList<TiffField>();

		for (TiffField field : tiffRecord.getFields()) {
			TiffField newField = new TiffField();
			String fieldName = field.getName();
			newField.setName(fieldName.toString());

			if ( RecordField.fileName.name().equals(fieldName) ) {
				newField.setValue(newFileName);
				newField.setText(newFileName);
			} else {
				newField.setValue(field.getValue()!=null ? field.getValue().toString() : null);
				newField.setText(field.getText()!=null ? field.getText().toString() : null);
			}

			newFields.add(newField);
		}

		newTiffRecord.setFields(newFields);
		return newTiffRecord;
	}

	public void sortRecordSet(ObservableList<ScannedImage> data) {
		if (null==data || data.size()==0) 
			return;
		FXCollections.sort(data, new Comparator<ScannedImage>() {
			@Override
			public int compare(ScannedImage o1, ScannedImage o2) {
				return Integer.compare(o1.indexNoProperty().get(), o2.indexNoProperty().get());
			}
		});		
	}

	public ObservableList<ScannedImage> cloneRecordSetToUpload(ObservableList<ScannedImage> data) {
		ObservableList<ScannedImage> newData = FXCollections.observableArrayList();
		if (null==data || data.size()==0) 
			return newData;
		newData.addAll(data);
		//newData.addAll(data.toArray(new ScannedImage[0]));

		// sort by FileCode & PageNo
		FXCollections.sort(newData, new Comparator<ScannedImage>() {
			@Override
			public int compare(ScannedImage o1, ScannedImage o2) {
			    int c;
			    c = o1.fileCodeProperty().get().compareTo(o2.fileCodeProperty().get());
			    if (c ==0)
				    c = o1.filePageProperty().get().compareTo(o2.filePageProperty().get());
			    return c;				
			}
		});		

		return newData;
	}

	public void marshalToFile(ImageRecordSet data) throws JAXBException {
		JAXBContext jaxbContext = new org.glassfish.jaxb.runtime.v2.JAXBContextFactory()
            .createContext(new Class[] {ImageRecordSet.class}, null);
		Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
		jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		jaxbMarshaller.marshal(data, new File(RECORD_SET_FILE_FULL_NAME));

		ImageRecordHelper.currentMD5 = getXmlFileMD5();
	}

	public ImageRecordSet unmarshalFromFile() throws JAXBException {
		JAXBContext jaxbContext = new org.glassfish.jaxb.runtime.v2.JAXBContextFactory()
            .createContext(new Class[] {ImageRecordSet.class}, null);
		Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
		ImageRecordSet recordSet = (ImageRecordSet) jaxbUnmarshaller.unmarshal(new File(RECORD_SET_FILE_FULL_NAME));
		if ( null==recordSet.getRecords() ) {
			recordSet.setRecords(new TiffRecords());
		}
		if ( null==recordSet.getRecords().getRecordList() ) {
			recordSet.getRecords().setRecordList(new ArrayList<TiffRecord>());
		}

		ImageRecordHelper.currentMD5 = getXmlFileMD5();

		return recordSet;
	}

	public static TiffRecord createTiffRecord(boolean queryFromPage, ScanConfig scanConfig, String imageSaveDir, String fileName,
			String imageFormat, String mainFileType, String fileType, String fileCode, String maxPage,
			String filePage, String boxNo, String batchArea, String companyCode,
			String personalCode, String signature, String sigSeqNumber, String scanTime, String recordStatus,
			String lastScanOrder) {

		if (logger.isDebugEnabled()) {
			logger.debug("queryFromPage={}, imageSaveDir={}, fileName={}, imageFormat={}, mainFileType={}, fileType={}, fileCode={}, maxPage={}, filePage={}, boxNo={}, batchArea={}, companyCode={}, personalCode={}, signature={}, sigSeqNumber={}, scanTime={}, recordStatus={}, lastScanOrder={}", queryFromPage, imageSaveDir, fileName, imageFormat, mainFileType, fileType, fileCode, maxPage, filePage, boxNo, batchArea, companyCode, personalCode, signature, sigSeqNumber, scanTime, recordStatus, lastScanOrder);
		}
		//=========================
		String mainFileTypeText = mainFileType;
		if ( ObjectsUtil.isNotEmpty(mainFileType) ) {
			for (Pair<String, String> mainFileTypePair : scanConfig.getMainFileTypeList()) {
				if (mainFileType.equals(mainFileTypePair.getKey())) {
					mainFileTypeText = mainFileTypePair.getValue();
					break;
				}
			}
		}
		
//		if (logger.isDebugEnabled()) {
//			logger.debug("mainFileType={}, mainFileTypeText={}", mainFileType, mainFileTypeText);
//		}

		String fileTypeText = fileType;
		if ( ObjectsUtil.isNotEmpty(mainFileType) && ObjectsUtil.isNotEmpty(fileType) ) {
			Map<String, BillCard> billCards = scanConfig.getMainBillCards().get(mainFileType);
			BillCard billCard = billCards.get(fileType);
			if ( billCard != null ) {
				fileTypeText = billCard.getCardCode() + "-" + billCard.getCardDesc();
			}
		}
		
//		if (logger.isDebugEnabled()) {
//			logger.debug("fileType={}, fileTypeText={}", fileType, fileTypeText);
//		}

		List<TiffField> newFields = new ArrayList<TiffField>();
		//========================= 
		TiffField orgNameParam = new TiffField(RecordField.OrgName.name(), scanConfig.getOrgName(), scanConfig.getOrgName());
		newFields.add(orgNameParam);//????????????

		TiffField deptNameParam = new TiffField(RecordField.DeptName.name(), scanConfig.getDeptName(), scanConfig.getDeptName());
		newFields.add(deptNameParam);//????????????

		TiffField mainFileTypeParam = new TiffField(RecordField.MainFileType.name(), mainFileType, mainFileTypeText);
		newFields.add(mainFileTypeParam);//???????????????;ex:UNB;UNB-?????????

		TiffField fileTypeParam = new TiffField(RecordField.FileType.name(), fileType, fileTypeText);
		newFields.add(fileTypeParam);//??????????????????ex???UNBA040:UNBA040-????????????????????????

		TiffField fileCodeParam = new TiffField(RecordField.FileCode.name(), fileCode, fileCode);
		newFields.add(fileCodeParam);//????????????

		TiffField filePageParame = new TiffField(RecordField.FilePage.name(), filePage, filePage);
		newFields.add(filePageParame);//???????????????????????????????????????????????????????????????

		String _boxNo, _batchDepType, _batchDate, _batchArea, _batchDocType, _batchDepTypeValue;
		if (queryFromPage) {
			_boxNo = scanConfig.getDefBoxNo();
			_batchDepType = scanConfig.getDefBatchDepType();
			_batchDepTypeValue = scanConfig.getBatchDepTypeValue();
			_batchDate = scanConfig.getDefBatchDate();
			_batchArea = scanConfig.getDefBatchArea();
			_batchDocType = scanConfig.getDefBatchDocType();
		} else {
			_boxNo = boxNo;
			_batchDepType = scanConfig.getBatchDepType();
			_batchDepTypeValue = scanConfig.getBatchDepTypeValue();
			_batchDate = scanConfig.getRocDate();
			_batchArea = batchArea;
			_batchDocType = "";
		}
		//========================= 
		if (logger.isDebugEnabled()) {
			logger.debug("_boxNo={}, _batchDepType={}, _batchDepTypeValue={}, _batchDate={}, _batchArea={}, _batchDocType={}", _boxNo, _batchDepType, _batchDepTypeValue, _batchDate, _batchArea, _batchDocType);
		}
		//========================= ++++
		TiffField boxNoParam = new TiffField(RecordField.BoxNo.name(), _boxNo, _boxNo);
		newFields.add(boxNoParam);//??????

		TiffField batchDepTypeParam = new TiffField(RecordField.BatchDepType.name(), _batchDepType, _batchDepType);
		newFields.add(batchDepTypeParam);//????????????-????????????;ex:NB:NB[ScanConfig.xml]

		TiffField batchDateParam = new TiffField(RecordField.BatchDate.name(), _batchDate, _batchDate);
		newFields.add(batchDateParam);//????????????[ScanConfig.xml]

		TiffField batchAreaParam = new TiffField(RecordField.BatchArea.name(), _batchArea, _batchArea);
		newFields.add(batchAreaParam);//??????

		TiffField batchDocTypeParam = new TiffField(RecordField.BatchDocType.name(), _batchDocType, _batchDocType);
		newFields.add(batchDocTypeParam);//?????????

		TiffField companyCodeParam = new TiffField(RecordField.CompanyCode.name(), companyCode, companyCode);
		newFields.add(companyCodeParam);//?????????(??????????????????)

		TiffField personalCodeParam = new TiffField(RecordField.PersonalCode.name(), personalCode, personalCode);
		newFields.add(personalCodeParam);//??????-?????????

		TiffField actionInsertParam = new TiffField(RecordField.ActionInsert.name(), scanConfig.getActionInsert(), scanConfig.getActionInsert());
		newFields.add(actionInsertParam);//??????????????????????????? xxx

		TiffField actionReplaceParam = new TiffField(RecordField.ActionReplace.name(), scanConfig.getActionReplace(), scanConfig.getActionReplace());
		newFields.add(actionReplaceParam);//??????????????????????????? xxx

		TiffField sendEmailParam = new TiffField(RecordField.SendEmail.name(), "", "");
		newFields.add(sendEmailParam );//?????????EMAIL

		TiffField isRemoteParam = new TiffField(RecordField.IsRemote.name(), "", "");
		newFields.add(isRemoteParam );//???????????????

		TiffField remarkParam = new TiffField(RecordField.Remark.name(), "", "");
		newFields.add(remarkParam);//????????????

		TiffField fileNameParam = new TiffField(RecordField.fileName.name(), fileName, fileName);
		newFields.add(fileNameParam);//????????????

		TiffField scanOrderParam = new TiffField(RecordField.ScanOrder.name(), lastScanOrder, lastScanOrder);
		newFields.add(scanOrderParam);//????????????(??????)

		TiffField actionTypeParam = new TiffField(RecordField.actionType.name(), "", "");
		newFields.add(actionTypeParam);//??????

		TiffField fromQueryPageParam = new TiffField(RecordField.fromQueryPage.name(), scanConfig.getFromQueryPage(), scanConfig.getFromQueryPage());
		newFields.add(fromQueryPageParam);//????????????????????????????????????;1:???:true;2:???:false(server?????????)[ScanConfig.xml]

		TiffField imageSaveDirParam = new TiffField(RecordField.imageSaveDir.name(), imageSaveDir, imageSaveDir);
		newFields.add(imageSaveDirParam);//????????????????????????????????????

		TiffField bizDeptParam = new TiffField(RecordField.bizDept.name(), scanConfig.getBizDept(), scanConfig.getBizDept());
		newFields.add(bizDeptParam);//????????????;ex: NB:NB(server?????????)[ScanConfig.xml]

		TiffField isGIDParam = new TiffField(RecordField.isGID.name(), scanConfig.getIsGID(), scanConfig.getIsGID());
		newFields.add(isGIDParam);//(server?????????)

		TiffField rocDateParam = new TiffField(RecordField.RocDate.name(), scanConfig.getRocDate(), scanConfig.getRocDate());
		newFields.add(rocDateParam);//????????????(server?????????)

		TiffField orgCode = new TiffField(RecordField.orgCode.name(), scanConfig.getOrgCode(), scanConfig.getOrgCode());
		newFields.add(orgCode);//????????????ex:101(server?????????)

		TiffField updateRoleParam = new TiffField(RecordField.updateRole.name(), scanConfig.getUpdateRole(), scanConfig.getUpdateRole());
		newFields.add(updateRoleParam);//????????????????????????????????????(server?????????)

		TiffField batchDepTypeValueParam = new TiffField(RecordField.batchDepTypeValue.name(), _batchDepTypeValue, _batchDepTypeValue);
		newFields.add(batchDepTypeValueParam);//????????????-??????????????????;ex:NB:NB(server?????????)

		TiffField recordStatusParam = new TiffField(RecordField.recordStatus.name(), recordStatus, recordStatus);
		newFields.add(recordStatusParam);//??????????????????;1:??????;0:?????????(server?????????)(??????tiff?????????"??????"??????????????????1)
		
		TiffField empIdParam = new TiffField(RecordField.empId.name(), scanConfig.getEmpId(), scanConfig.getEmpId());
		newFields.add(empIdParam );//????????????(server?????????)

		TiffField stepParam = new TiffField(RecordField.step.name(), scanConfig.getStep(), scanConfig.getStep());
		newFields.add(stepParam );//(????????????server?????????)

		TiffField imageFormatParam = new TiffField(RecordField.ImageFormat.name(), imageFormat, imageFormat);
		newFields.add(imageFormatParam);//(server?????????)(??????tiff?????????"??????"?????????????????????tag)

		TiffField deptIdParam = new TiffField(RecordField.deptId.name(), scanConfig.getDeptId(), scanConfig.getDeptId());
		newFields.add(deptIdParam );//?????????????????????ID(server?????????)

		TiffField signatureParam = new TiffField(RecordField.Signature.name(), signature, signature);
		newFields.add(signatureParam);//??????????????????

		TiffField SigSeqNumberParam = new TiffField(RecordField.SigSeqNumber.name(), sigSeqNumber, sigSeqNumber);
		newFields.add(SigSeqNumberParam);//?????????????????????????????????

		TiffField maxPageParam = new TiffField(RecordField.MaxPage.name(), maxPage, maxPage);
		newFields.add(maxPageParam);//?????????????????????

		TiffField scanTimeParam = new TiffField(RecordField.ScanTime.name(), scanTime, scanTime);
		newFields.add(scanTimeParam);//??????????????????

		
		TiffRecord newTiffRecord = new TiffRecord();
		newTiffRecord.setFileName(fileName);
		newTiffRecord.setFields(newFields);

		return newTiffRecord;
	}
	
	public ImageRecordSet loadImageRecordSet() throws Scan2Exception {
		ImageRecordSet tmpRecordSet = null;
		if ( xmlFileExists() ) {
			try {
				tmpRecordSet = unmarshalFromFile();
			} catch (JAXBException e) {
				logger.error(e.getMessage(), e);
				String backupFileName = null;
				try {
					backupFileName = backupXmlFile();
				} catch (IOException ioe) {
					logger.error("", ioe);
				}
				String loadErrorMessage = String.format("%s ?????????????????????????????????????????????????????????????????? %s ?????????????????????", ImageRecordHelper.RECORD_SET_FILE_NAME, backupFileName);
				ErrorCode errorCode = ErrorCode.CANNOT_PARSE_IMAGERECORDSET_FILE;
				errorCode.setMessage(loadErrorMessage);
				
				throw new Scan2Exception(errorCode);
			}
		}
		TiffRecords records = new TiffRecords();
		records.setRecordList(new ArrayList<TiffRecord>());
		ImageRecordSet recordSet = new ImageRecordSet();
		recordSet.setRecords(records);
		
		if ( tmpRecordSet!=null && tmpRecordSet.getRecords()!=null && tmpRecordSet.getRecords().getRecordList()!=null ) {
			for (TiffRecord tiffRecord : tmpRecordSet.getRecords().getRecordList()) {
				records.getRecordList().add(tiffRecord);
			}
		} 
		try {
			marshalToFile(recordSet);
		} catch (JAXBException e) {
			logger.error("", e);
			String loadErrorMessage = String.format("????????????????????? %s ???", ImageRecordHelper.RECORD_SET_FILE_NAME);
			ErrorCode errorCode = ErrorCode.CANNOT_PARSE_IMAGERECORDSET_FILE;
			errorCode.setMessage(loadErrorMessage);
			throw new Scan2Exception(errorCode);
		}
		return recordSet;
	}
}
