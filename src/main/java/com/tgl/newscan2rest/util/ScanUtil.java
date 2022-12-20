package com.tgl.newscan2rest.util;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asprise.imaging.core.Imaging;
import com.asprise.imaging.core.Request;
import com.asprise.imaging.core.RequestOutputItem;
import com.asprise.imaging.core.ResultImageItem;
import com.asprise.imaging.core.scan.twain.Source;
import com.asprise.imaging.core.scan.twain.TwainConstants;
import com.asprise.imaging.core.scan.twain.TwainException;
import com.tgl.newscan2rest.Constant;
import com.tgl.newscan2rest.StarterConst;
import com.tgl.newscan2rest.bean.ScanConfig;
import com.tgl.newscan2rest.bean.SignatureImgRule;
import com.tgl.newscan2rest.bean.TiffRecord;

public class ScanUtil {

	private static final Logger logger = LoggerFactory.getLogger(ScanUtil.class);

	public static final String SCAN_TEMP_DIR = ImageRecordHelper.IMAGE_ARCHIVE_DIR + File.separator + "temp";
	//public static final String SCAN_TEMP_DIR =  "C:\\TGL-Scan-2-Test" + File.separator + "image-archive" + File.separator + "temp";
	
	private static final String DEFAULT_APP_ID = StarterConst.APP_ID;
	private static final int DEFAULT_WIN_HANDLE = 0;
	private static final double BLANK_PAGE_THRESHOLD = 0.000001d;

	private static int stScanType;
	private static int stScanDuplex;
	private static String stLastBoxNo;
	private static String stLastBatchArea;
	private static List<String> stMultiPolicyCodes;
	private static boolean stIsMultiPolicy;
	private static int stFileCodeCount;
	private static String stMainFileType;
	private static String stLastFileCode;
	private static String stLastFileType;
	private static String stLastCompanyCode;
	private static String stLastPersonalCode;
	private static int stCurrentMultiPolicySeq;
	private static int stLastFilePage;
	private static String stLastTotalPage;
	private static int stLastScanOrder;

	private static SimpleDateFormat formatter1 = new SimpleDateFormat("yyyyMMddHHmmssSSS");
	private static SimpleDateFormat formatter2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	static {
		// 
		byte[] textBytes = new byte[] { 73, 73, 73, 77, 67, 67, 67, 69, 65, 65, 65, 85, 103, 103, 103, 51, 73, 73, 73, 81, 67, 70, 67, 85, 66, 82, 66, 85, 66, 121, 66, 116, 85, 89, 85, 81, 48, 87, 48, 48, 78, 53, 78, 74, 66, 122, 66, 66, 84, 82, 84, 81, 108, 50, 108, 122, 57, 120, 57, 65, 77, 118, 77, 116, 83, 89, 83, 82, 85, 109, 85, 68, 78, 86, 78, 107, 70, 68, 70, 51, 84, 98, 84, 81, 108, 50, 108, 107, 78, 49, 78, 89, 70, 85, 70, 116, 88, 100, 88, 77, 48, 121, 48, 122, 53, 49, 78, 90, 66, 70, 80, 66, 84, 84, 82, 82, 85, 108, 69, 84, 85, 81, 85, 85 };
		String k1 = DigestUtils.base64Decode( DigestUtils.getText(DigestUtils.getBytes(textBytes, 0))).trim();
		String v1 = DigestUtils.base64Decode( DigestUtils.getText(DigestUtils.getBytes(textBytes, 1))).trim();
		String k2 = DigestUtils.base64Decode( DigestUtils.getText(DigestUtils.getBytes(textBytes, 2))).trim();
		String v2 = DigestUtils.base64Decode( DigestUtils.getText(DigestUtils.getBytes(textBytes, 3))).trim();
		System.setProperty(k1, v1);
		System.setProperty(k2, v2);

		stLastBoxNo = null;
		stLastBatchArea = null;
		stIsMultiPolicy = false;
		stFileCodeCount = 0;
		stLastScanOrder = 0;

		stMultiPolicyCodes = new ArrayList<String>();
		stMainFileType = null;
		stLastFileCode = null;
		stLastFileType = null;
		stLastCompanyCode = null;
		stLastPersonalCode = null;
		stCurrentMultiPolicySeq = 0;
		stLastFilePage = 0;
		stLastTotalPage = null;
		stScanType = TwainConstants.TWPT_BW;
		stScanDuplex = TwainConstants.TWDX_NONE;

		if (logger.isDebugEnabled()) {
			Imaging.configureNativeLogging(Imaging.LOG_LEVEL_DEBUG , "log" + File.separator + "asprise.log");
		}
	}

	public static int getScanType() {
		return stScanType;
	}

	public static int getScanDuplex() {
		return stScanDuplex;
	}

	public static void setScanDuplex(int scanDuplex) {
		ScanUtil.stScanDuplex = scanDuplex;
	}

	public static void setLastBoxNo(String stLastBoxNo) {
		ScanUtil.stLastBoxNo = stLastBoxNo;
	}

	public static void setLastBatchArea(String stLastBatchArea) {
		ScanUtil.stLastBatchArea = stLastBatchArea;
	}

	public static void setMultiPolicy(boolean stIsMultiPolicy) {
		ScanUtil.stIsMultiPolicy = stIsMultiPolicy;
	}

	public static void setFileCodeCount(int stFileCodeCount) {
		ScanUtil.stFileCodeCount = stFileCodeCount;
	}

	public static void setLastScanOrder(int stLastScanOrder) {
		ScanUtil.stLastScanOrder = stLastScanOrder;
	}

	public static List<TiffRecord> saveToFiles(String barcodeString, File tiffFile, BufferedImage image, String splitDiv, boolean queryFromPage, ScanConfig scanConfig) {
		if (logger.isDebugEnabled()) {
			logger.debug("saveToFiles(), barcodeString={}, tiffFile={}, splitDiv={}, lastScanOrder={}, queryFromPage={}", barcodeString, tiffFile.getAbsolutePath(), splitDiv, stLastScanOrder, queryFromPage);
		}

		List<TiffRecord> tiffRecords = new ArrayList<TiffRecord>();
		List<String> fileCodes = new ArrayList<String>();
		List<String> otherArgs = new ArrayList<String>();
		List<String> policyCodes = new ArrayList<String>();

		String fileName = null;
		int fileCodeCount = 0;
		List<SignatureImgRule> imageRules = new ArrayList<SignatureImgRule>();

		if (ObjectsUtil.isEmpty(barcodeString) && !stIsMultiPolicy) {
			fileName = saveImage(tiffFile, splitDiv);
			fileCodes.add("");

			TiffRecord scannedRecord = createRecord(fileName, fileCodes, imageRules, stIsMultiPolicy, stFileCodeCount, queryFromPage, scanConfig, String.valueOf(++stLastScanOrder));
			tiffRecords.add(scannedRecord);
			if ( imageRules.size() > 0 ) {
				if (logger.isDebugEnabled()) {
					logger.debug("imageRules.size() = {}, Start to cutImage ...", imageRules.size());
				}
				List<TiffRecord> sigRecords = cutImage(fileCodes, imageRules, image, "01", stIsMultiPolicy, queryFromPage, scanConfig);
				tiffRecords.addAll(sigRecords);
				imageRules.clear();
				if (logger.isDebugEnabled()) {
					logger.debug("cutImage done, imageRules.clear().");
				}
			}
		} else {
			//【文件類別Barcode】固定為9位(含英數字) ，編碼規則為：
			//	  XXX      X       XXX       X      X
			//	部門別  文件識別 流水編號  總頁數 第幾頁
			//  main_file_type is first 3 chars
			//  sub_file_type is first 7 chars
			
			//【文件編號Barcode】
			// 新契約	8,10,11	保單號碼
			// 理賠		12,15	理賠案號
			// POS		12		保全受理契變案號
			// if main_file_type is "GID"
			// 團險		 8		公司碼(團險保單號碼), ex:NN123456
			// 團險		 6		個人碼

			String[] barcodes = barcodeString.split(Constant.BARCODE_SEPARATOR);
			for (String barcode : barcodes) {
				if (logger.isDebugEnabled()) {
					logger.debug("barcode={}", barcode);
				}
				if (ObjectsUtil.isEmpty(barcode)) {
					continue;
				}

				if (barcode.length() == 9) {
					stMainFileType = barcode.substring(0, 3);
					otherArgs.add(barcode);
					if (logger.isDebugEnabled()) {
						logger.debug("mainFileType={}", stMainFileType);
					}
				} else if (("UNB".equals(stMainFileType) || "POS".equals(stMainFileType) ) && 
						   (barcode.length() == 8 ||   // 8碼舊保單號
						    barcode.length() == 10 ||  // 10碼舊保單號
						    barcode.length() == 11)) { // 11碼新保單號
					if (logger.isDebugEnabled()) {
						logger.debug("UNB/POS PolicyCode:{}", barcode);
					}
					fileCodes.add(barcode);
					fileCodeCount++;
				} else if ("POS".equals(stMainFileType) && barcode.length() == 12) {
					if (logger.isDebugEnabled()) {
						logger.debug("POS ApplyCode:{}", barcode);
					}
					fileCodes.add(barcode); // 受理編號
					fileCodeCount++;
				} else if ("CLM".equals(stMainFileType) && 
						  (barcode.length() == 12 || barcode.length() == 15)) {
					if (logger.isDebugEnabled()) {
						logger.debug("CLAIM No:{}", barcode);
					}
					otherArgs.add(barcode); // CLAIM No
				} else if ("GID".equals(stMainFileType) && 
						  !(barcode.length() == 6 || barcode.length() == 8)) {
					if (logger.isDebugEnabled()) {
						logger.debug("Undefine Barcode Length:{}", barcode);
					}
				} else if (barcode.length() == 6 || barcode.length() == 8 || barcode.length() == 10 || 
						   barcode.length() == 11 || barcode.length() == 12 || barcode.length() == 15) {
					if (logger.isDebugEnabled()) {
						logger.debug("Validate BarCode:{}", barcode);
					}
					otherArgs.add(barcode); // Sub_File_Type or GID's Company/Personal Code
				} else {
					if (logger.isDebugEnabled()) {
						logger.debug("Undefine Barcode Length:{}", barcode);
					}
				}
			}

			if (fileCodeCount == 0 && stIsMultiPolicy) {
				fileCodes.addAll(stMultiPolicyCodes);
				fileCodeCount = fileCodes.size();
				if (logger.isDebugEnabled()) {
					logger.debug("Is MultiPolicy, Copy FileCodes!");
				}
			}

			// 掃到多個BarCode為FileCode的情況: 
			// 1.一張要保書多個policyNumber
			// 2.團險:公司碼、個人碼
			// 3.首頁 FileType, FileCode

			stMultiPolicyCodes.clear(); // 先清除暫存的3合1保單號,後面再塞入,避免多份3合1連著掃會被複製

			if (fileCodeCount > 1) {
				if (logger.isDebugEnabled()) {
					logger.debug("fileCodeCount={}, set stFileCodeCount=true", fileCodeCount);
				}
				stIsMultiPolicy = true;
				stFileCodeCount = fileCodeCount;

				for (int i=0; i<fileCodeCount; i++) {
					String fileCode = fileCodes.get(i);
					if (logger.isDebugEnabled()) {
						logger.debug("FileCode:{}", fileCode);
					}
					fileName = saveImage(tiffFile, splitDiv+"_"+fileCode);
					policyCodes.addAll(otherArgs);
					policyCodes.add(fileCode);
					// 組合保單
					stMultiPolicyCodes.add(fileCode);
					TiffRecord scannedRecord = createRecord(fileName, policyCodes, imageRules, stIsMultiPolicy, stFileCodeCount, queryFromPage, scanConfig, String.valueOf(++stLastScanOrder));
					tiffRecords.add(scannedRecord);
					policyCodes.clear();
				}

				if (imageRules.size() > 0){
					List<TiffRecord> sigRecords = cutImage(fileCodes, imageRules, image, "01", stIsMultiPolicy, queryFromPage, scanConfig);
					tiffRecords.addAll(sigRecords);
					imageRules.clear();
				} else {
					if (logger.isDebugEnabled()) {
						logger.debug("Signature Rules not matched!");
					}
				}
			} else {
				if (logger.isDebugEnabled()) {
					logger.debug("Single FileCode, Set stFileCodeCount=false");
					logger.debug("otherArgs:[{}], fileCodes:[{}]", (otherArgs==null ? "" : String.join(",", otherArgs)), (fileCodes==null ? "" : String.join(",", fileCodes)));
				}
				stIsMultiPolicy = false;
				if (otherArgs.size() > 0) {
					policyCodes.addAll(otherArgs);
				}
				if (fileCodes.size() > 0) {
					policyCodes.add(fileCodes.get(0));
				}

				int maxIndex = policyCodes.size() - 1;
				if (maxIndex >= 0) {
					fileName = saveImage(tiffFile, splitDiv+"_"+policyCodes.get(maxIndex));
				} else {
					fileName = saveImage(tiffFile, splitDiv+"_NoPolicyCode");
				}

				TiffRecord scannedRecord = createRecord(fileName, policyCodes, imageRules, stIsMultiPolicy, stFileCodeCount, queryFromPage, scanConfig, String.valueOf(++stLastScanOrder));
				tiffRecords.add(scannedRecord);
				if (imageRules.size() > 0) {
					List<TiffRecord> sigRecords = cutImage(policyCodes, imageRules, image, "01", stIsMultiPolicy, queryFromPage, scanConfig);
					tiffRecords.addAll(sigRecords);
					imageRules.clear();;
				} else {
					if (logger.isDebugEnabled()) {
						logger.debug("Signature Rules not matched!");
					}
				}
				policyCodes.clear();
			}
		}

		fileCodes.clear();
		otherArgs.clear();
		policyCodes.clear();

		return tiffRecords;
	}

	private static String saveImage(File tiffFile, String splitDiv) {
		String fileFormat = Constant.FILE_EXT_TIFF;
		if (stScanType == TwainConstants.TWPT_RGB) {
			fileFormat = Constant.FILE_EXT_JPG;
		}
		String imageSaveDir = ImageRecordHelper.IMAGE_ARCHIVE_DIR + File.separator;
		String fileName = formatter1.format(new Date()) + splitDiv + fileFormat;
		String destName = imageSaveDir + fileName;

		if (logger.isDebugEnabled()) {
			logger.debug("saveImage(): tiffFile={}, destName={}", tiffFile.getAbsolutePath(), destName);
		}

		//複製檔案
		try {
			ImageUtil.copyImageFile(tiffFile, destName);
		} catch (IOException e) {
			logger.error(String.format("無法複製影像檔 %s！", fileName), e);
		}

		return fileName;
	}


	private static String saveImage(BufferedImage image, String splitDiv) {
		String fileFormat = Constant.FILE_EXT_TIFF;
		if (stScanType == TwainConstants.TWPT_RGB) {
			fileFormat = Constant.FILE_EXT_JPG;
		}
		String imageSaveDir = ImageRecordHelper.IMAGE_ARCHIVE_DIR + File.separator;
		String fileName = formatter1.format(new Date()) + splitDiv + fileFormat;
		String destName = imageSaveDir + fileName;

		if (logger.isDebugEnabled()) {
			logger.debug("saveImage(): BufferedImage, destName={}", destName);
		}

		//儲存檔案
		try {
			if (stScanType == TwainConstants.TWPT_RGB) {
				ImageUtil.writeJpgFile(image, destName);
	        } else {
	        	ImageUtil.writeTiffFile(image, destName);
	        }
		} catch (IOException e) {
			logger.error(String.format("無法儲存影像檔 %s！", fileName), e);
		}

		return fileName;
	}


	private static TiffRecord createRecord(String fileName, List<String> barcodes, List<SignatureImgRule> imageRules, boolean isMultiPolicy, int fileCodeCount, boolean queryFromPage, ScanConfig scanConfig, String lastScanOrder) {
		if (logger.isDebugEnabled()) {
			logger.debug("fileName={}, barcodes={}, imageRules.size={}, isMultiPolicy={}, fileCodeCount={}, queryFromPage={}", fileName, (barcodes==null ? "null" : String.join(",", barcodes)), (imageRules==null ? "null" : imageRules.size()), isMultiPolicy, fileCodeCount, queryFromPage);
			logger.debug("stLastFileCode={}, stLastFileType={}, stLastTotalPage={}, stLastFilePage={}", stLastFileCode, stLastFileType, stLastTotalPage, stLastFilePage);
		}

		String lastFilePage = String.format("%s", stLastFilePage);

		if (isMultiPolicy) {
			stCurrentMultiPolicySeq++;
		} else {
			stCurrentMultiPolicySeq = 0;
			fileCodeCount = 0;
		}
		if (logger.isDebugEnabled()) {
			logger.debug("isMultiPolicy={}, stCurrentMultiPolicySeq={}, fileCodeCount={}", isMultiPolicy, stCurrentMultiPolicySeq, fileCodeCount);
		}

		/* 
		文件類別 FileType(9) format 
			XXX X XXX X X
			MainFileType(3) + DocType(1) + SeqNo(3) + TotalPage(1) + CurrentPage(1) <= Total Length = 9
			MainFileType(3) + DocType(1) + SeqNo(3) = FileType(7)
			ex: UNBA001 - 傳統型保險要保書、POSN008 - 保單借款約定書

		文件編號 FileCode(11) or (12)
			UNB: PolicyNumber(11,10,8)	ex:00000218400
			CLM: CaseNumber(12,15)		ex:eeeTPXXXXXXX 105TP0000024	
			POS: ApplyCode(12)			ex:eeeMMddXXXXX 105033112345
		    GID: 公司碼(2+6)				ex:	AA123456 = GID's PolicyNumber
		         個人碼(6)				ex:123456
		
		4 types of Barcode, Length= 9, 11~12, 8, 6
		*/

		String barcode = null;
		String currentPage = null;
		String totalPage = null; 
		String mainFileType = null;
		String currentFileType = null;
		String currentFileCode = null;
		String companyCode = null;
		String personalCode = null;

		boolean isSignature = false;
		boolean isFileTypeChanged = false;
		boolean isFileCodeChanged = false;
		boolean isCompanyCodeChanged = false;

		// 判斷是否為簽名影像
		if (fileName.endsWith(Constant.SIGNATURE_SUFFIX + Constant.FILE_EXT_TIFF) || 
			fileName.endsWith(Constant.SIGNATURE_SUFFIX + Constant.FILE_EXT_JPG)) {
			isSignature = true;
			if (logger.isDebugEnabled()) {
				logger.debug("This is a signature file!");
			}
		}

		// 處理多個條碼
		if ( barcodes != null && barcodes.size() > 0 ) {
			for ( int i=0; i<barcodes.size(); i++ ) {
				barcode = barcodes.get(i);
				int barcodeLen = barcode.length();
				if (logger.isDebugEnabled()) {
					logger.debug("barcode={}, length={}", barcode, barcodeLen);
				}

				switch (barcodeLen) {
				case 9: {
					// 文件類別(9)
					// MainFileType(1-3), FileType(1-7), TotalPage(8), CurPage(9)
					mainFileType = barcode.substring(0, 3);

					if ("UNB".equals(mainFileType) || "POS".equals(mainFileType) || 
						"CLM".equals(mainFileType) || "GID".equals(mainFileType)) {
						currentFileType = barcode.substring(0, 7);
						totalPage = barcode.substring(7, 8);
						currentPage = barcode.substring(8, 9);
						if (logger.isDebugEnabled()) {
							logger.debug("currentFileType={}, totalPage={}, currentPage={}", currentFileType, totalPage, currentPage);
						}

						// PCR:244580 POSZ99911 is Separator Page
						if ( Constant.SEP_BARCODE.equals(barcode) ) {
							if (logger.isDebugEnabled()) {
								logger.debug("Clear Last Page Information!");
							}
							stLastFileType = "";
							stLastFileCode = "";
							stLastCompanyCode = "";
							stLastPersonalCode = "";
							stLastFilePage = 0;
							stLastTotalPage = "";

							currentFileCode = Constant.SEP_FILE_CODE;
						}
					} else {
						if (logger.isDebugEnabled()) {
							logger.debug("currentFileType barcode error -> {}", currentFileType);
						}
						mainFileType = "";
						currentFileType = "";
						totalPage = "";
						currentPage = "";
					}

					break;
				}
				case 10: // 舊保單號 10碼
				case 11: // 新保單號 11碼
				case 12: // 受理編號、理賠案號 12碼
				case 15: // 舊理賠案號 PCR:56912
				{
					String tmp = barcode.substring(0, 3);
					if ("UNB".equals(tmp) || "POS".equals(tmp) || 
						"CLM".equals(tmp) || "GID".equals(tmp)) {
						if (logger.isDebugEnabled()) {
							logger.debug("Barcode distinguish error -> {}", barcode);
						}
						currentFileCode = "";
					} else if (ObjectsUtil.isNotEmpty(mainFileType) && "CLM".equals(mainFileType)) {
						// IR:262072, Filter CLM File Code, only 12|15 length
						if (barcodeLen == 12 || barcodeLen == 15) {
							currentFileCode = barcode;
							if (logger.isDebugEnabled()) {
								logger.debug("mainFileType is CLM! FileCode length in (12|15) -> {}", currentFileCode);
							}
						} else {
							currentFileCode = "";
							if (logger.isDebugEnabled()) {
								logger.debug("mainFileType is CLM! FileCode length not in (12|15), clear currentFileCode!");
							}
						}
					} else {
						currentFileCode = barcode;
						if (logger.isDebugEnabled()) {
							logger.debug("currentFileCode(10|11|12|15) -> {}", currentFileCode);
						}
					}

					break;
				}
				case 8: {
					// 公司碼(2+6) or 舊保單號 8碼
					String tmp = barcode.substring(0, 3);
					if ("UNB".equals(tmp) || "POS".equals(tmp) || 
						"CLM".equals(tmp) || "GID".equals(tmp)) {
						if (logger.isDebugEnabled()) {
							logger.debug("Barcode distinguish error -> {}", barcode);
						}
						currentFileCode = "";
						companyCode = "";
					} else {
						if ("GID".equalsIgnoreCase(mainFileType) || "GID".equalsIgnoreCase(currentFileType==null ? "" : currentFileType.substring(0, 3)) || 
							(ObjectsUtil.isEmpty(currentFileType) && "GID".equalsIgnoreCase(stLastFileType==null ? "" : stLastFileType.substring(0, 3)))) {
							companyCode = barcode;
							if (logger.isDebugEnabled()) {
								logger.debug("companyCode -> {}", companyCode);
							}
						} else {
							currentFileCode = barcode;
							if (logger.isDebugEnabled()) {
								logger.debug("currentFileCode(8) -> {}", currentFileCode);
							}
						}
					}

					break;
				}
				case 6: {
					// 個人碼(6)
					String tmp = barcode.substring(0, 3);
					if ("UNB".equals(tmp) || "POS".equals(tmp) || 
						"CLM".equals(tmp) || "GID".equals(tmp)) {
						if (logger.isDebugEnabled()) {
							logger.debug("Barcode distinguish error -> {}", barcode);
						}
						personalCode = "";
					} else {
						personalCode = barcode;
						if (logger.isDebugEnabled()) {
							logger.debug("personalCode -> {}", personalCode);
						}
					}

					break;
				}
				default: {
					// Exception
					totalPage = "0";
					currentPage = "0";
					if (logger.isDebugEnabled()) {
						logger.debug("Exception: Barcode length is undefined!");
						logger.debug("barcode={}, mainFileType={}, currentFileType={}, totalPage={}, currentPage={}", barcode, mainFileType, currentFileType, totalPage, currentPage);
					}
					break;
				}

				}
			}
		}

		if (logger.isDebugEnabled()) {
			logger.debug("currentFileType={}, stLastFileType={}, currentFileCode={}, stLastFileCode={}, stLastTotalPage={}", currentFileType, stLastFileType, currentFileCode, stLastFileCode, stLastTotalPage);
		}

		isFileTypeChanged = false;
		isFileCodeChanged = false;
		isCompanyCodeChanged = false;

		if ( ObjectsUtil.isNotEmpty(stLastFileType) && ObjectsUtil.isNotEmpty(currentFileType) && !currentFileType.equals(stLastFileType) ) {
			isFileTypeChanged = true;
		} else if ( ObjectsUtil.isEmpty(stLastFileType) && ObjectsUtil.isNotEmpty(currentFileType) ) {
			isFileTypeChanged = true;
		} else {
		}

		if (logger.isDebugEnabled()) {
			logger.debug("isFileTypeChanged={}", isFileTypeChanged);
		}

		if ( ObjectsUtil.isNotEmpty(currentFileCode) && ObjectsUtil.isNotEmpty(stLastFileCode) ) {
			if ( isMultiPolicy && !isFileTypeChanged ) {
				if ( currentFileCode.length() == 11 && !currentFileCode.substring(0, 10).equals(stLastFileCode.substring(0, 10)) ) {
					// IR:277395 前一頁非組合保單
					isFileCodeChanged = true;
				} else {
					// IR:220509 三合一保單不檢核保單號碼,視為保單號碼未改變
					// 若文件類型沒有改變,就直接複製上一筆資訊
					isFileCodeChanged = false;
				}
				if (logger.isDebugEnabled()) {
					logger.debug("isMultiPolicy={}, isFileTypeChanged={}, isFileCodeChanged={}", isMultiPolicy, isFileTypeChanged, isFileCodeChanged);
				}
			//} else if ( currentFileCode.length() == 11 ) {
			//	// IR:259353 組合保單可能單一保單號掃入
			//	if ( !currentFileCode.substring(0, 10).equals(stLastFileCode.substring(0, 10)) ) {
			//		isFileCodeChanged = true;
			//	} else {
			//	}
			//	if (logger.isDebugEnabled()) {
			//		logger.debug("isFileCodeChanged={}, policyCode!", isFileCodeChanged);
			//	}
			} else if ( !currentFileCode.equals(stLastFileCode) ) {
				isFileCodeChanged = true;
				if (logger.isDebugEnabled()) {
					logger.debug("FileCode changed!");
				}
			}
		} else if ( ObjectsUtil.isEmpty(currentFileCode) && ObjectsUtil.isNotEmpty(stLastFileCode) ) {
			currentFileCode = stLastFileCode;		
			if (logger.isDebugEnabled()) {
				logger.debug("currentFileCode is Empty, copy from stLastFileCode! isFileCodeChanged=false");
			}
		} else if ( ObjectsUtil.isNotEmpty(currentFileCode) && ObjectsUtil.isEmpty(stLastFileCode) ) {
			isFileCodeChanged = true;
			if (logger.isDebugEnabled()) {
				logger.debug("isFileCodeChanged=true, NULL change to  {}", currentFileCode);
			}
		}

		if ( ( isSignature && ObjectsUtil.isEmpty(currentFileType) && ObjectsUtil.isNotEmpty(stLastFileType) ) || // 切簽名影像沒有影像類型 BARCODE
			 ( !isFileCodeChanged && 
			   ObjectsUtil.isEmpty(currentFileType) && ObjectsUtil.isNotEmpty(stLastFileType) && 
			   ( "0".equals(stLastTotalPage) || isSignature ) ) ) {
			currentFileType = stLastFileType;
			totalPage = stLastTotalPage;
			if (logger.isDebugEnabled()) {
				logger.debug("currentFileType is Empty, copy from stLastFileType!");
			}
		}
		if (logger.isDebugEnabled()) {
			logger.debug("currentFileType={}", currentFileType);
		}

		// get MainFileType from FileType Left(3)
		if( ObjectsUtil.isNotEmpty(currentFileType) && currentFileType.length() >=3) {
			mainFileType = currentFileType.substring(0, 3);
		} else {
			mainFileType = "";		
		}
		if (logger.isDebugEnabled()) {
			logger.debug("mainFileType={}", mainFileType);
		}

		// comanyCode
		if (logger.isDebugEnabled()) {
			logger.debug("companyCode={}, stLastCompanyCode={}", companyCode, stLastCompanyCode);
		}
		if ( ObjectsUtil.isEmpty(companyCode) && ObjectsUtil.isNotEmpty(stLastCompanyCode)){
			companyCode = stLastCompanyCode;
		}
		// companyCode is the same with fileCode
		if ( ObjectsUtil.isNotEmpty(companyCode) ){
			if (logger.isDebugEnabled()) {
				logger.debug("companyCode is equals to fileCode!");
			}
			currentFileCode = companyCode;
		}	
		if ( ObjectsUtil.isNotEquals(companyCode, stLastCompanyCode) ) {
			isCompanyCodeChanged = true;
		}

		// personalCode
		if (logger.isDebugEnabled()) {
			logger.debug("personalCode={}, stLastPersonalCode={}", personalCode, stLastPersonalCode);
		}
		if ( !isCompanyCodeChanged && ObjectsUtil.isEmpty(personalCode) && ObjectsUtil.isNotEmpty(stLastPersonalCode) ) {
			personalCode = stLastPersonalCode;
		}

		// if FileType|FileCode changed, reset FilePage=1
		if (logger.isDebugEnabled()) {
			logger.debug("currentFileCode={}, stLastFilePage={}, currentPage={}", currentFileCode, stLastFilePage, currentPage);
		}

		// 組合保單			
		if (logger.isDebugEnabled()) {
			logger.debug("isMultiPolicy={}", isMultiPolicy);
		}
		if (isMultiPolicy) {
			// 組合保單			

			if ( "0".equals(totalPage) ) {
				int modNumber = stCurrentMultiPolicySeq % fileCodeCount;

				if ( ( isFileTypeChanged || isFileCodeChanged ) && "0".equals(totalPage) ) {
					// 保單號碼改變或文件類型改變,且最大頁碼0,強制設為1
					currentPage = "1";
					if (logger.isDebugEnabled()) {
						logger.debug("FileType||FileCode Change && FileType end with 00, reset currentPage = 1");
					}
				} else if ( modNumber == 1 ) {
					// 組合保單的第一個保單號碼,用上一頁的+1
					// currentPage ++

					// UAT IR-462838，fileType 尾碼=0，若為簽名檔時頁數不累加
					///////////////////////////////////////////////////////////////////////////////////////////
					//currentPage = String.format("%s", stLastFilePage + 1);
					//if (logger.isDebugEnabled()) {
					//	logger.debug("FileType's MaxPage=0, currentPage = last Page + 1 = {}", currentPage);
					//}
					if (isSignature) {
						currentPage = String.format("%s", stLastFilePage);
						if (logger.isDebugEnabled()) {
							logger.debug("FileType's MaxPage=0, isSignature, currentPage = last Page = {}", currentPage);
						}
					} else {
						currentPage = String.format("%s", stLastFilePage + 1);
						if (logger.isDebugEnabled()) {
							logger.debug("FileType's MaxPage=0, currentPage = last Page + 1 = {}", currentPage);
						}
					}
					///////////////////////////////////////////////////////////////////////////////////////////
				} else {
					// 非組合保單的第一頁,保持同一個頁碼
					// currentPage = last Page
					currentPage = String.format("%s", stLastFilePage);
					if (logger.isDebugEnabled()) {
						logger.debug("currentPage = last Page = {}", currentPage);
					}
				}
			} else {
				// currentPage 依 FileType 條碼編號,不需 Reset
				if (logger.isDebugEnabled()) {
					logger.debug("currentPage 依 FileType 條碼編號,不需 Reset! currentPage={}, stLastFilePage={}", currentPage, stLastFilePage);
				}
			}
		} else {
			// 非組合保單

			// fileType 尾碼=0,同一影像子類型,不限頁數,等於上一頁的頁碼自動+1
			if ( "0".equals(totalPage) ) {
				// 若文件類別||文件編號跟上一頁的不同,直接Reset成1
				if ( isFileTypeChanged || isFileCodeChanged || isCompanyCodeChanged || 
					 ( lastFilePage.equals(stLastTotalPage) && !isSignature ) ) { // 上一個影像類型已掃達最大頁數
					currentPage = "1";
					if (logger.isDebugEnabled()) {
						logger.debug("FileType||FileCode||isCompanyCodeChanged changed, currentPage={}", currentPage);
					}
				} else {
					// UAT IR-462838，fileType 尾碼=0，若為簽名檔時頁數不累加
					///////////////////////////////////////////////////////////////////////////////////////////
					//currentPage = String.format("%s", stLastFilePage + 1);
					//if (logger.isDebugEnabled()) {
					//	logger.debug("FileType's MaxPage=0,  currentPage = last Page + 1 = {}", currentPage);
					//}
					if (isSignature) {
						currentPage = String.format("%s", stLastFilePage);
						if (logger.isDebugEnabled()) {
							logger.debug("FileType's MaxPage=0, isSignature, currentPage = last Page = {}", currentPage);
						}
					} else {
						currentPage = String.format("%s", stLastFilePage + 1);
						if (logger.isDebugEnabled()) {
							logger.debug("FileType's MaxPage=0, currentPage = last Page + 1 = {}", currentPage);
						}
					}
					///////////////////////////////////////////////////////////////////////////////////////////
				}
			} else {
				// 非尾數碼為0的,保持原頁碼
				if (logger.isDebugEnabled()) {
					logger.debug("totalPage != 0 , currentPage={}", currentPage);
				}
			}
		}

		String tmpFilePage = null;
		String tmpSignature = null;
		String tmpSigSeqNumber = null;

		// Check signature tiff file
		if (isSignature) {
			// IR:340723 三合一保單切簽名需複製sigSeqNumber,不應為0
			String sigSeqNumber = String.format("%s", stLastFilePage);
			if (logger.isDebugEnabled()) {
				logger.debug("isSignature=true, sigSeqNumber={}", sigSeqNumber);
			}
			tmpSignature = "Y";
			tmpFilePage = ""; // Signature pageNo is NULL
			tmpSigSeqNumber = sigSeqNumber; // This Signature belone to which PageNo
		} else {
			if (logger.isDebugEnabled()) {
				logger.debug("isSignature=false, sigSeqNumber={}", "");
			}
			tmpSignature = "N";
			tmpFilePage = currentPage;
			tmpSigSeqNumber = "";
		}	

		// Barcode辨識率不良時,避免誤將影像判斷為GID
		if ( ObjectsUtil.isNotEmpty(mainFileType) && !"GID".equals(mainFileType) ) { // 非團險
			if ( ObjectsUtil.isNotEmpty(companyCode) ) {
				if (logger.isDebugEnabled()) {
					logger.debug("NOT GID But CompanyCode -> {}", companyCode);
				}
				companyCode = "";
				if ( ObjectsUtil.isNotEmpty(currentFileCode) && currentFileCode.length() == 8 ) { // 公司碼有值,文件編號長度=8,先清空
					currentFileCode = "";
					if ( "0".equals(totalPage) ) { // 若最大頁數為0,Reset 本頁頁碼=1
						currentPage = "1";
						tmpFilePage = currentPage;
						if (logger.isDebugEnabled()) {
							logger.debug("reset FilePage=1");
						}
					}
				}
			}

			if ( ObjectsUtil.isNotEmpty(personalCode) ) {
				if (logger.isDebugEnabled()) {
					logger.debug("NOT GID But personalCode -> {}", personalCode);
				}
				personalCode = "";
				if ( ObjectsUtil.isNotEmpty(currentFileCode) ) { // 個人碼有值,可能為文件編號汙損,先清空
					currentFileCode = "";
					if ( ObjectsUtil.isNotEmpty(totalPage) && "0".equals(totalPage) ) { // 若最大頁數為0,Reset 本頁頁碼=1
						currentPage = "1";
						tmpFilePage = currentPage;
						if (logger.isDebugEnabled()) {
							logger.debug("reset FilePage=1");
						}
					}
				}
			}
		}

		String imageSaveDir = ImageRecordHelper.IMAGE_ARCHIVE_DIR + File.separator;
		String imageFormat = Constant.FILE_TYPE_TIFF;
		if (stScanType == TwainConstants.TWPT_RGB) {
			imageFormat = Constant.FILE_TYPE_JPG;
		}

		TiffRecord tiffRecord = ImageRecordHelper.createTiffRecord(
			queryFromPage, scanConfig, imageSaveDir, fileName, imageFormat, mainFileType, currentFileType, currentFileCode,
			totalPage, tmpFilePage, stLastBoxNo, stLastBatchArea, companyCode,
			personalCode, tmpSignature, tmpSigSeqNumber, formatter2.format(new Date()), "0", lastScanOrder
		);

		// BR-CMN-PIC-003 自動切簽名檔影像
		// 1.	影像主類型=POS時,改為同UNB。
		// 2.	影像主類型=UNB時,按照預定義的圖像區域切簽名檔並記錄影像的子類型為原影像子類型,標記為簽名影像。
		// Check FileCode&PageNo. If there is a signature, Call cutImage()

		Map<String, List<SignatureImgRule>> rules = scanConfig.getSignatureRules();
		if ( !isSignature && null != rules && rules.size() > 0 ) {
			if (logger.isDebugEnabled()) {
				logger.debug("Check signature rule ...");
			}

			if ( scanConfig.isDeptNB() &&  
				 ( "UNB".equalsIgnoreCase(mainFileType) || 
				   "POS".equalsIgnoreCase(mainFileType) ) ) {
				if (logger.isDebugEnabled()) {
					logger.debug("{} Signature. currentFileType={}, currentPage={}", mainFileType, currentFileType, currentPage);
				}

				String key = currentFileType + "-" + currentPage;
				if ( null != rules.get(key) ) {
					for ( SignatureImgRule imageRule : rules.get(key) ) {
						imageRules.add(imageRule);
						if (logger.isDebugEnabled()) {
							logger.debug("match signature rule, cut image! imageRule: {}", imageRule.toString());
						}
					}
				}
			} else {
				if (logger.isDebugEnabled()) {
					logger.debug("Not need to cut image! isDeptNB={}, mainFileType={}", scanConfig.isDeptNB(), mainFileType);
				}
			}
		}

		if ( !Constant.SEP_BARCODE.equals(barcode) ) {
			// keep last value
			stLastFileType = currentFileType;
			stLastFileCode = currentFileCode;
			stLastCompanyCode = companyCode;
			stLastPersonalCode = personalCode;
			try {
				int tmp = Integer.valueOf(currentPage);
				if ( tmp > 0 ) {
					stLastFilePage = tmp;
				}
			} catch (Exception e) {
			}
			stLastTotalPage = totalPage;
		}

		return tiffRecord;
	}

	private static List<TiffRecord> cutImage(List<String> barcodes, List<SignatureImgRule> imageRules, BufferedImage image, String splitDiv, boolean isMultiPolicy, boolean queryFromPage, ScanConfig scanConfig) {
		if (logger.isDebugEnabled()) {
			logger.debug("cutImage: barcodes=[{}], imageRules.size=[{}], BufferedImage, splitDiv={}, isMultiPolicy={}, queryFromPage={}, stLastScanOrder={}", (barcodes==null ? "" : String.join(",", barcodes)), imageRules.size(), splitDiv, isMultiPolicy, queryFromPage, stLastScanOrder);
		}

		List<TiffRecord> sigRecords = new ArrayList<TiffRecord>();
		String fileName = null;
		List<String> policyCodes = new ArrayList<String>();

		if ( imageRules != null && imageRules.size() > 0 && barcodes.size() > 0 ) {
			for ( int i=0; i<imageRules.size(); i++ ) {
				SignatureImgRule imageRule = imageRules.get(i);

				BufferedImage sigImage = null;
				try {
					sigImage = ImageUtil.cropImage(image, imageRule);
				} catch (Exception e) {
					logger.error("Crop image error!", e);
				}

				if ( sigImage != null ) {
					for ( String barcode : barcodes ) {
						if (logger.isDebugEnabled()) {
							logger.debug("barcode={}", barcode);
						}

						// eBao UNB PolicyCode is 11
						// Old PolicyCode is 8 or 10
						// FileType is 9
						if ( barcodes.size() == 1 || 
							 ( barcode.length() == 8 || barcode.length() == 9 || barcode.length() == 10 || barcode.length() == 11) ) {
							String fileSurrfix = String.format("%s_%s_%s%s", splitDiv, barcode, (i+1), Constant.SIGNATURE_SUFFIX);
							fileName = saveImage(sigImage, fileSurrfix);
							policyCodes.add(barcode);
							TiffRecord sigRecord = createRecord(fileName, policyCodes, null, isMultiPolicy, stFileCodeCount, queryFromPage, scanConfig, String.valueOf(++stLastScanOrder));
							sigRecords.add(sigRecord);
							policyCodes.clear();
							// IR:263553 影像重覆切簽名
							if ( barcode.length() == 9 ) {
								if (logger.isDebugEnabled()) {
									logger.debug("barcode.length() == 9, break");
								}
								break;
							}
						} else {
							if (logger.isDebugEnabled()) {
								logger.debug("Error!This is not UNB Signature Image!");
							}
						}
					}
					break; // TODO: 在此 break 很奇怪, 是否應改為 continue
				}

			}
		}

		return sigRecords;
	}

//	public static long getHwnd(javafx.stage.Window win) {
//		if (logger.isDebugEnabled()) {
//			logger.debug("");
//		}
//		try {
//			final Method getPeer = javafx.stage.Window.class.getDeclaredMethod("getPeer", null);
//			getPeer.setAccessible(true);
//			final Object tkStage = getPeer.invoke(win);
//			final Method getRawHandle = tkStage.getClass().getMethod("getRawHandle");
//			getRawHandle.setAccessible(true);
//			Long rowHandle = (Long) getRawHandle.invoke(tkStage);
//			if (logger.isDebugEnabled()) {
//				logger.debug("rowHandle={}", rowHandle);
//			}
//			return rowHandle;
//		} catch (Exception ex) {
//			logger.error("Unable to determine native handle for window");
//			return 0l;
//		}
//	}

//	public static int getHwnd() {
//		//--add-exports javafx.graphics/com.sun.glass.ui=ALL-UNNAMED
////		List<com.sun.glass.ui.Window> windows = com.sun.glass.ui.Window.getWindows();
////		int nh = (int)windows.get(0).getNativeHandle();
////		long nw = windows.get(0).getNativeWindow();
////		if (logger.isDebugEnabled()) {
////			logger.debug("getHwnd(), nativeHandle={}, nativeWindow={}", nh, nw);
////		}
////		return nh;
//
////		com.asprise.imaging.core.util.system.Utils.getHwnd();
//		
//		//TODO: 以上取得的 Window Handle 傳給 Image 使用時似乎有問題，先乎略
//		return 0;
//	}

	/*
	public static Long getWindowPointer(Stage stage) {
		//--add-exports javafx.graphics/com.sun.javafx.tk=ALL-UNNAMED
		//--add-opens javafx.graphics/com.sun.glass.ui=ALL-UNNAMED
		//--add-opens javafx.graphics/javafx.stage=ALL-UNNAMED
		//--add-opens javafx.graphics/com.sun.javafx.tk.quantum=ALL-UNNAMED
        try {
            Method tkStageGetter;
            try {
                // java 9
                tkStageGetter = stage.getClass().getSuperclass().getDeclaredMethod("getPeer");
            } catch (NoSuchMethodException ex) {
                // java 8
                tkStageGetter = stage.getClass().getMethod("impl_getPeer");
            }
            tkStageGetter.setAccessible(true);
            TKStage tkStage = (TKStage) tkStageGetter.invoke(stage);
            Method getPlatformWindow = tkStage.getClass().getDeclaredMethod("getPlatformWindow");
            getPlatformWindow.setAccessible(true);
            Object platformWindow = getPlatformWindow.invoke(tkStage);
            Method getNativeHandle = platformWindow.getClass().getMethod("getNativeHandle");
            getNativeHandle.setAccessible(true);
            Object nativeHandle = getNativeHandle.invoke(platformWindow);
    		if (logger.isDebugEnabled()) {
				logger.debug("getWindowPointer(Stage), nativeHandle={}", nativeHandle);
			}

            return (long) nativeHandle;
        } catch (Throwable e) {
            System.err.println("Error getting Window Pointer");
            e.printStackTrace();
            return null;
        }
    }
    */

	public static List<String> getSourceNameList() {
		Imaging imaging = new Imaging(DEFAULT_APP_ID, DEFAULT_WIN_HANDLE);

		// 顯示所有掃描器名稱
		// Imaging.scanListSources(boolean nameOnly,java.lang.String capsToRetrieve,boolean detectDeviceType,boolean excludeTwainDsOnWia) 
		List<Source> sourcesNameOnly = imaging.scanListSources(true, "all", true, true);
		List<String> nameList = sourcesNameOnly == null ? null : new ArrayList<String>();

		String sourceStr = "";
		for ( Source source : sourcesNameOnly ) {
			String productName = source.getProductName();
			if (logger.isDebugEnabled()) {
				sourceStr += (sourceStr.length()==0 ? "" : "\r\n") + String.format("source=%s", source.toString());
			}
			nameList.add(productName);
		}
		if (logger.isDebugEnabled()) {
			logger.debug("All sources with names only: {}", sourceStr);
		}

		return nameList;
	}

	public static String getDefaultSourceName() {
		String sourceName = null;

		Imaging imaging = new Imaging(DEFAULT_APP_ID, DEFAULT_WIN_HANDLE);

		// 顯示目前預設掃描器名稱
		String tmp = imaging.scanGetDefaultSourceName();
		if ( ObjectsUtil.isNotEmpty(tmp) ) {
			if ( tmp.startsWith("<error: ") ) {
				logger.error("getDefaultSourceName() -> " + tmp);
			} else {
				sourceName = tmp;
				if (logger.isDebugEnabled()) {
					logger.debug("getDefaultSourceName() -> {}", sourceName);
				}
			}
		} else {
			if (logger.isDebugEnabled()) {
				logger.debug("getDefaultSourceName() -> {}", tmp);
			}
		}

		return sourceName;
	}

	public static String setDefaultSourceName(String sourceName) {
		String defaultSourceName = null;
		if ( ObjectsUtil.isEmpty(sourceName) ) {
			return null;
		}

		Imaging imaging = new Imaging(DEFAULT_APP_ID, DEFAULT_WIN_HANDLE);

		// 設定預設掃描器名稱
		String tmp = imaging.scanSelectDefaultSource(sourceName); // 若設定失敗時, SDK 會跳出預設的掃描器選擇視窗
		if ( ObjectsUtil.isNotEmpty(tmp) ) {
			if ( tmp.startsWith("<error: ") ) {
				logger.error("setDefaultSourceName({}) -> {}", sourceName, tmp);
			} else {
				defaultSourceName = tmp;
				if (logger.isDebugEnabled()) {
					logger.debug("setDefaultSourceName({}) -> {}", sourceName, defaultSourceName);
				}
			}
		} else {
			if (logger.isDebugEnabled()) {
				logger.debug("setDefaultSourceName({}) -> {}", sourceName, tmp);
			}
		}

		return defaultSourceName;
	}

	public static String showSelectScanerDialog() {
		return showSelectScanerDialog(DEFAULT_APP_ID, 0);
	}

//	public static String showSelectScanerDialog(javafx.stage.Window window) {
//		return showSelectScanerDialog(DEFAULT_APP_ID, (int)getHwnd(window));
//	}

	public static String showSelectScanerDialog(java.awt.Window window) {
		Imaging imaging = new Imaging(window)
			    .setUseAspriseSourceSelectUI(false);

		return showSelectScanerDialog(imaging);
	}

	public static String showSelectScanerDialog(String appId, int windowHandle) {
		if (logger.isDebugEnabled()) {
			logger.debug("appId={}, windowHandle={}", appId, windowHandle);
		}

		Imaging imaging = new Imaging(appId, windowHandle)
			    .setUseAspriseSourceSelectUI(false);

		return showSelectScanerDialog(imaging);
	}

	private static String showSelectScanerDialog(Imaging imaging) {
		String sourceName = null;

		// TODO: 預設的掃描器選擇視窗有時會被遮蔽, 改善方式是改寫為自己的掃描器選擇視窗
		String tmp = imaging.scanSelectDefaultSource("select");
		if ( ObjectsUtil.isNotEmpty(tmp) ) {
			if ( tmp.startsWith("<error: ") ) {
				logger.error("showSelectScanerDialog() -> " + tmp);
			} else {
				sourceName = tmp;
				if (logger.isDebugEnabled()) {
					logger.debug("showSelectScanerDialog() -> {}", sourceName);
				}
			}
		} else {
			if (logger.isDebugEnabled()) {
				logger.debug("showSelectScanerDialog() -> {}", tmp);
			}
		}

		return sourceName;
	}

	public static com.asprise.imaging.core.Result setupSource(String sourceName) throws TwainException {
		return setupSource(DEFAULT_APP_ID, 0, sourceName);
	}

//	public static com.asprise.imaging.core.Result setupSource(javafx.stage.Window window, String sourceName) throws TwainException {
//		return setupSource(DEFAULT_APP_ID, (int)getHwnd(window), sourceName);
//	}

	public static com.asprise.imaging.core.Result setupSource(java.awt.Window window, String sourceName) throws TwainException {
		Imaging imaging = new Imaging(window)
			    .setUseAspriseSourceSelectUI(false);

		return setupSource(imaging, sourceName);
	}

	public static com.asprise.imaging.core.Result setupSource(String appId, int windowHandle, String sourceName) throws TwainException {
		if (logger.isDebugEnabled()) {
			logger.debug("appId={}, windowHandle={}", appId, windowHandle);
		}

		Imaging imaging = new Imaging(appId, windowHandle)
			    .setUseAspriseSourceSelectUI(false);

		return setupSource(imaging, sourceName);
	}

	public static com.asprise.imaging.core.Result setupSource(Imaging imaging, String sourceName) throws TwainException {
		com.asprise.imaging.core.Request request = new Request();
		com.asprise.imaging.core.Result result = imaging.scan(request, sourceName, true, true);
		return result;
	}

	public static List<TiffRecord> scan(String sourceName, String colorModeStr, String duplexModeStr, boolean queryFromPage, ScanConfig scanConfig) {
		return scan(DEFAULT_APP_ID, 0, sourceName, colorModeStr, duplexModeStr, queryFromPage, scanConfig);
	}

//	public static List<TiffRecord> scan(javafx.stage.Window window, String sourceName, String colorModeStr, String duplexModeStr, boolean queryFromPage, ScanConfig scanConfig) {
//		return scan(DEFAULT_APP_ID, (int)getHwnd(window), sourceName, colorModeStr, duplexModeStr, queryFromPage, scanConfig);
//	}

	public static List<TiffRecord> scan(java.awt.Window window, String sourceName, String colorModeStr, String duplexModeStr, boolean queryFromPage, ScanConfig scanConfig) {
		Imaging imaging = new Imaging(window)
			    .setUseAspriseSourceSelectUI(false);

		return scan(imaging, sourceName, colorModeStr, duplexModeStr, queryFromPage, scanConfig);
	}

	public static List<TiffRecord> scan(String appId, int windowHandle, String sourceName, String colorModeStr, String duplexModeStr, boolean queryFromPage, ScanConfig scanConfig) {
		if (logger.isDebugEnabled()) {
			logger.debug("appId={}, windowHandle={}", appId, windowHandle);
		}

		Imaging imaging = new Imaging(appId, windowHandle)
			    .setUseAspriseSourceSelectUI(false);

		return scan(imaging, sourceName, colorModeStr, duplexModeStr, queryFromPage, scanConfig);
	}

	public static List<TiffRecord> scan(Imaging imaging, String sourceName, String colorModeStr, String duplexModeStr, boolean queryFromPage, ScanConfig scanConfig) {
		long startTime = System.currentTimeMillis();

		if (logger.isDebugEnabled()) {
			logger.debug("Scan start! sourceName={}, colorModeStr={}, duplexModeStr={}, queryFromPage={}, stLastScanOrder={}", sourceName, colorModeStr, duplexModeStr, queryFromPage, stLastScanOrder);
		}

		RequestOutputItem requestOutput = null;
		int scanType = TwainConstants.TWPT_BW;
		int scanDuplex = TwainConstants.TWDX_NONE;
		boolean duplexEnabled = false;

		if ( Constant.COLOR_MODE_BLACK_WHITE.equals(colorModeStr) ) {
			requestOutput = new RequestOutputItem(Imaging.OUTPUT_SAVE, Imaging.FORMAT_TIF);
			requestOutput.setTiffCompression(Imaging.TIFF_COMPRESSION_CCITT_G4);
		} else if ( Constant.COLOR_MODE_COLOR.equals(colorModeStr) ) {
			scanType = TwainConstants.TWPT_RGB;
			requestOutput = new RequestOutputItem(Imaging.OUTPUT_SAVE, Imaging.FORMAT_JPG);
		}

		requestOutput.setSavePath(SCAN_TEMP_DIR + "\\${TMS}${EXT}");
		requestOutput.setForceSinglePage(true);
		
		System.out.println("SCAN_TEMP_DIR:" + (SCAN_TEMP_DIR + "\\${TMS}${EXT}"));

		if ( Constant.DUPLEX_MODE_SINGLE_PAGE.equals(duplexModeStr) ) {
//			scanDuplex = TwainConstants.TWDX_1PASSDUPLEX;
			scanDuplex = TwainConstants.TWDX_NONE;
		} else if ( Constant.DUPLEX_MODE_DOUBLE_PAGE.equals(duplexModeStr) ) {
			scanDuplex = TwainConstants.TWDX_2PASSDUPLEX;
			duplexEnabled = true;
		}

		stScanType = scanType;
		stScanDuplex = scanDuplex;

		List<TiffRecord> recordList = new ArrayList<TiffRecord>();

		Map<String, String> algoMap = new HashMap<String, String>();
		algoMap.put("type", "B"); // Types to try: DEFAULT, A, B, C 

		Request request = new Request()
			.addOutputItem(requestOutput)
			.setRecognizeBarcodes(true)
			.setBarcodesSettings(algoMap)
//			.setDetectBlankPages("false") 不偵測空白頁，由掃描器設定[空白頁檢測]控制
			.setBlankPageThreshold(BLANK_PAGE_THRESHOLD) // 因部份條碼貼紙或自行列印的條碼辨識不到，故由 0.02 降低至 0.000001 (與舊版掃描設定相同)
			.setTwainCap(TwainConstants.ICAP_PIXELTYPE, stScanType)
			.setTwainCap(TwainConstants.CAP_DUPLEXENABLED, duplexEnabled)
			.setTwainCap(TwainConstants.CAP_DUPLEX, scanDuplex)
			;

		if (logger.isDebugEnabled()) {
			retrieveExtAttributes(request);
		}

		if (logger.isDebugEnabled()) {
			logger.debug("imaging.scan()");
			logger.debug("request = {}", request.toJson(true));
		}

		// 開始掃描
		com.asprise.imaging.core.Result result = imaging.scan(request, sourceName, false, true);

		if (logger.isDebugEnabled()) {
			logger.debug("result = {}", result == null ? "(null)" : result.toJson(true));
		}

		List<File> acquireFiles = result.getImageFiles();
		if ( acquireFiles==null || acquireFiles.size()==0 ) {
			return null;
		}

		List<ResultImageItem> imageItemList = result.getImageItems();

		for ( int i=0; i<acquireFiles.size(); i++ ) {
	    	File acquireFile = acquireFiles.get(i);
			if (logger.isDebugEnabled()) {
				logger.debug("Acquire image {}", acquireFile.getName());
			}

			ResultImageItem imageItem = imageItemList.get(i);
			double inkCoverage = imageItem.getInkCoverage();
			BufferedImage image = null;
			String barcodes = null;

			if (inkCoverage>=BLANK_PAGE_THRESHOLD) {
				try {
					image = result.getImage(i);
				} catch (Exception e) {
					String errorMessage = String.format("影像檔讀取失敗 %s ！", acquireFile.getName());
					logger.error(errorMessage, e);
				}
			} else {
				logger.debug("inkCoverage={} is less than {}, is a blank page!", inkCoverage, BLANK_PAGE_THRESHOLD);
			}

			if ( image != null ) {
				List<Map<String, Object>> barcodeList = imageItem.getBarcodes();
				Map<String, Object> extAttributes = imageItem.getExtendedImageAttrs();
				barcodes = getBarcodes(barcodeList, extAttributes);

				List<TiffRecord> records = ScanUtil.saveToFiles(barcodes, acquireFile, image, "01", queryFromPage, scanConfig);
				if (records != null && records.size() > 0) {
					recordList.addAll(records);
				}
			}

			// 移除掃描暫存檔
			try {
				Files.delete(acquireFile.toPath());
			} catch (IOException e) {
				logger.error("移除掃描暫存檔錯誤", e);
			}
		}

		Date endTime = new Date();

		if (logger.isDebugEnabled()) {
			long processTime = (System.currentTimeMillis() - startTime) / 1000;
			logger.debug("Scan finish! processTime={} seconds", processTime);
		}

	    return recordList;
	}

	private static void retrieveExtAttributes(Request request) {
		int[] capAttrs = { 
			TwainConstants.ICAP_BARCODEDETECTIONENABLED, 
			TwainConstants.ICAP_SUPPORTEDBARCODETYPES, 
			TwainConstants.ICAP_BARCODEMAXSEARCHPRIORITIES, 
			TwainConstants.ICAP_BARCODESEARCHPRIORITIES, 
			TwainConstants.ICAP_BARCODESEARCHMODE, 
			TwainConstants.ICAP_BARCODEMAXRETRIES, 
			TwainConstants.ICAP_BARCODETIMEOUT
		};
		String[] capAttrNames = { 
			"ICAP_BARCODEDETECTIONENABLED", 
			"ICAP_SUPPORTEDBARCODETYPES", 
			"ICAP_BARCODEMAXSEARCHPRIORITIES", 
			"ICAP_BARCODESEARCHPRIORITIES", 
			"ICAP_BARCODESEARCHMODE", 
			"ICAP_BARCODEMAXRETRIES", 
			"ICAP_BARCODETIMEOUT"
		};
		int[] extAttrs = { 
			TwainConstants.TWEI_BARCODEX, 
			TwainConstants.TWEI_BARCODEY, 
			TwainConstants.TWEI_BARCODETEXT, 
			TwainConstants.TWEI_BARCODETYPE, 
			TwainConstants.TWEI_BARCODECOUNT, 
			TwainConstants.TWEI_BARCODECONFIDENCE, 
			TwainConstants.TWEI_BARCODEROTATION, 
			TwainConstants.TWEI_BARCODETEXTLENGTH
		};
		String[] extAttrNames = { 
			"TWEI_BARCODEX", 
			"TWEI_BARCODEY", 
			"TWEI_BARCODETEXT", 
			"TWEI_BARCODETYPE", 
			"TWEI_BARCODECOUNT", 
			"TWEI_BARCODECONFIDENCE", 
			"TWEI_BARCODEROTATION", 
			"TWEI_BARCODETEXTLENGTH"
		};

		for (int i=0; (i<capAttrs.length && i<=160); i++) {
			int attr = capAttrs[i];
			String attrName = capAttrNames[i];
			logger.debug("{}={}", attrName, attr);
			request.retrieveCap(attr);
		}
		for (int j=0; j<extAttrs.length; j++) {
			int attr = extAttrs[j];
			String attrName = extAttrNames[j];
			logger.debug("{}={}", attrName, attr);
			request.retrieveExtendedImageAttributes(attr);
		}

	}

	public static List<TiffRecord> convertFile(File selectedFile, boolean queryFromPage, ScanConfig scanConfig) {
		long startTime = System.currentTimeMillis();

		String fileName = selectedFile.getName().toLowerCase();
		if (logger.isDebugEnabled()) {
			logger.debug("convertFile start! selectedFile={}, queryFromPage={}", selectedFile.getName(), queryFromPage);
		}

		RequestOutputItem requestOutput = null;
		int scanType = TwainConstants.TWPT_BW;
		int scanDuplex = TwainConstants.TWDX_NONE;
		boolean duplexEnabled = false;
		String tempFileExt = null;

		if ( fileName.endsWith(Constant.FILE_EXT_TIF) || fileName.endsWith(Constant.FILE_EXT_TIFF) ) {
			requestOutput = new RequestOutputItem(Imaging.OUTPUT_SAVE, Imaging.FORMAT_TIF);
			requestOutput.setTiffCompression(Imaging.TIFF_COMPRESSION_CCITT_G4);
			tempFileExt = Constant.FILE_EXT_TIF;
		} else if ( fileName.endsWith(Constant.FILE_EXT_JPG) || fileName.endsWith(Constant.FILE_EXT_JPEG) ) {
			scanType = TwainConstants.TWPT_RGB;
			requestOutput = new RequestOutputItem(Imaging.OUTPUT_SAVE, Imaging.FORMAT_JPG);
			tempFileExt = Constant.FILE_EXT_JPG;
		}

		requestOutput.setSavePath(SCAN_TEMP_DIR + "\\${TMS}${EXT}");
		requestOutput.setForceSinglePage(true);

		stScanType = scanType;
		stScanDuplex = scanDuplex;

		List<TiffRecord> recordList = new ArrayList<TiffRecord>();

		Request request = new Request()
			.addOutputItem(requestOutput)
			.setRecognizeBarcodes(true)
//			.setDetectBlankPages("false") 不偵測空白頁，由掃描器設定[空白頁檢測]控制
			.setBlankPageThreshold(BLANK_PAGE_THRESHOLD) // 因部份條碼貼紙或自行列印的條碼辨識不到，故由 0.02 降低至 0.000001 (與舊版掃描設定相同)
			.setTwainCap(TwainConstants.ICAP_PIXELTYPE, stScanType)
			.setTwainCap(TwainConstants.CAP_DUPLEXENABLED, duplexEnabled)
			.setTwainCap(TwainConstants.CAP_DUPLEX, scanDuplex)
			;

		//request.addImageFile(selectedFile);

		// Request.addImageFile() 只能接受ASCII的檔案路徑及名稱，調整如下
		File tempFile = null; // 導入暫存檔
		boolean allAscii = isAllASCII(selectedFile.getAbsolutePath());
		if (logger.isDebugEnabled()) {
			logger.debug("allAscii={}", allAscii);
		}
		if (allAscii) {
			request.addImageFile(selectedFile);
		} else {
			tempFile = new File(SCAN_TEMP_DIR + File.separator + "import_" + startTime + tempFileExt);
			try {
				if (logger.isDebugEnabled()) {
					logger.debug("複製導入的檔案 {} 至 {}", selectedFile.toString(), tempFile.toString());
				}
				Files.copy(selectedFile.toPath(), tempFile.toPath(), new CopyOption[] { StandardCopyOption.REPLACE_EXISTING });
			} catch (IOException e) {
	        	logger.error("複製導入檔案失敗！", e);
			}
			request.addImageFile(tempFile);
		}

		if (logger.isDebugEnabled()) {
			logger.debug("imaging.convert()");
			logger.debug("request = {}", request.toJson(true));
		}

		Imaging imaging = new Imaging(DEFAULT_APP_ID, DEFAULT_WIN_HANDLE)
		    .setUseAspriseSourceSelectUI(false);

		// 開始轉換
		com.asprise.imaging.core.Result result = imaging.convert(request);

		if (logger.isDebugEnabled()) {
			logger.debug("result = {}", result == null ? "(null)" : result.toJson(true));
		}

		List<File> acquireFiles = result.getImageFiles();
		if ( acquireFiles==null || acquireFiles.size()==0 ) {
			return null;
		}

		List<ResultImageItem> imageItemList = result.getImageItems();

		for ( int i=0; i<acquireFiles.size(); i++ ) {
	    	File acquireFile = acquireFiles.get(i);
			if (logger.isDebugEnabled()) {
				logger.debug("Acquire image " + acquireFile.getName());
			}

			ResultImageItem imageItem = imageItemList.get(i);
			BufferedImage image = null;
			String barcodes = null;

			try {
				image = result.getImage(i);
			} catch (Exception e) {
				String errorMessage = String.format("影像檔讀取失敗 %s ！", acquireFile.getName());
				logger.error(errorMessage, e);
			}

			if ( image != null ) {
				List<Map<String, Object>> barcodeList = imageItem.getBarcodes();
				barcodes = getBarcodes(barcodeList, null);

				List<TiffRecord> records = ScanUtil.saveToFiles(barcodes, acquireFile, image, "01", queryFromPage, scanConfig);
				if (records != null && records.size() > 0) {
					recordList.addAll(records);
				}
			}

			// 移除掃描暫存檔
			try {
				Files.delete(acquireFile.toPath());
			} catch (IOException e) {
				logger.error("移除掃描暫存檔錯誤", e);
			}
		}

		// 移除導入暫存檔
		if (tempFile != null) {
			try {
				Files.delete(tempFile.toPath());
			} catch (IOException e) {
				logger.error("移除掃描暫存檔錯誤", e);
			}
		}

		Date endTime = new Date();

		if (logger.isDebugEnabled()) {
			long processTime = (System.currentTimeMillis() - startTime) / 1000;
			logger.debug("convertFile finish! processTime={} seconds", processTime);
		}

	    return recordList;
	}

	private static boolean isAllASCII(String paramString) {
		boolean bool = true;
		for (byte b = 0; b < paramString.length();) {
			char c = paramString.charAt(b);
			if (c > '') {
				bool = false;
				break;
			}
			b++;
			continue;
		}
		return bool;
	}

	public static String getBarcodes(List<Map<String, Object>> barcodeList, Map<String, Object> extAttributes) {
		String barcodeString = "";
		List<String> extractedBarcodes = new ArrayList<String>();

		if (logger.isDebugEnabled()) {
			logger.debug("Extract ASPRISE barcode text...");
		}

		if ( barcodeList != null && barcodeList.size() > 0 ) {
			for ( Map<String, Object> map : barcodeList ) {
				String barcodeType = (String)map.getOrDefault("type", null);
				String barcodeText = (String)map.getOrDefault("data", null);

				if (logger.isDebugEnabled()) {
					logger.debug("Barcode type={}, data={}", barcodeType, barcodeText);
				}
				if (barcodeType == null || !"CODE-39".equals(barcodeType) ) {
					if (logger.isDebugEnabled()) {
						logger.debug("Incorrect barcode type -> " + barcodeType);
					}
				} else if (barcodeText.length() >= 3 && barcodeText.startsWith("OBJ") ) {
					if (logger.isDebugEnabled()) {
						logger.debug("Ignore Old Barcode -> " + barcodeText);
					}
				} else if (ObjectsUtil.isEmpty(barcodeText) || barcodeText.length() < 6 || barcodeText.length() > 15) {
					if (logger.isDebugEnabled()) {
						logger.debug("Invalid Length -> " + barcodeText);
					}
				} else {
					extractedBarcodes.add(barcodeText);
				}
			}
		}

		if (logger.isDebugEnabled()) {
			logger.debug("Extract extendedImageAttrs barcode text...");
		}

		if ( extAttributes != null && extAttributes.size() > 0 ) {
			int barcodeCount = 0;
			String barcodeCountStr = (String)extAttributes.getOrDefault("TWEI_BARCODECOUNT", null);
			String barcodeTypeStr = (String)extAttributes.getOrDefault("TWEI_BARCODETYPE", null);
			String barcodeTextStr = (String)extAttributes.getOrDefault("TWEI_BARCODETEXT", null);

			if (logger.isDebugEnabled()) {
				logger.debug("TWEI_BARCODECOUNT={}, TWEI_BARCODETYPE={}, TWEI_BARCODETEXT={}", barcodeCountStr, barcodeTypeStr, barcodeTextStr);
			}

			if (ObjectsUtil.isEmpty(barcodeCountStr) || "UNSUPPORTED".equals(barcodeCountStr) || 
				ObjectsUtil.isEmpty(barcodeTypeStr) || "UNSUPPORTED".equals(barcodeTypeStr) || 
				ObjectsUtil.isEmpty(barcodeTextStr) || "UNSUPPORTED".equals(barcodeTextStr)) {
				logger.debug("extendedImageAttrs is empty or unsupported!");
			} else {
				try {
					barcodeCount = Integer.parseInt(barcodeCountStr);
				} catch (Exception e) {
				}

				if (barcodeCount > 0) {
					String[] barcodeTypeItems = barcodeTypeStr.replaceAll("\\[", "").replaceAll("\\]", "").split(", ");
					String[] barcodeTextItems = barcodeTextStr.replaceAll("\\[", "").replaceAll("\\]", "").replaceAll("\\\"", "").split(", ");
					for (int i = 0; i < barcodeTypeItems.length; i++) {
						String barcodeType = barcodeTypeItems[i];
						String barcodeText = barcodeTextItems[i];

						if (logger.isDebugEnabled()) {
							logger.debug("Barcode type={}, data={}", barcodeType, barcodeText);
						}
						if (barcodeType == null || !"0".equals(barcodeType) ) {
							if (logger.isDebugEnabled()) {
								logger.debug("Incorrect barcode type -> " + barcodeType);
							}
						} else if (barcodeText.length() >= 3 && barcodeText.startsWith("OBJ") ) {
							if (logger.isDebugEnabled()) {
								logger.debug("Ignore Old Barcode -> " + barcodeText);
							}
						} else if (ObjectsUtil.isEmpty(barcodeText) || barcodeText.length() < 6 || barcodeText.length() > 15) {
							if (logger.isDebugEnabled()) {
								logger.debug("Invalid Length -> " + barcodeText);
							}
						} else {
							extractedBarcodes.add(barcodeText);
						}
					}
				}
			}
		}

		if (extractedBarcodes.size()==0) {
			if (logger.isDebugEnabled()) {
				logger.debug("No Barcode Symbols Founded!");
			}
		} else {
			for (String barcodeText : extractedBarcodes) {
				if (barcodeText.length() == 9 && (barcodeText.startsWith("UNB") || barcodeText.startsWith("CLM")|| barcodeText.startsWith("POS")|| barcodeText.startsWith("GID"))) {
					if (barcodeString.indexOf(barcodeText)==-1) {
						// 文件類別放前面
						barcodeString = barcodeText + (barcodeString.length()>0 ? Constant.BARCODE_SEPARATOR + barcodeString : "");
					}
				} else {
					if (barcodeString.indexOf(barcodeText)==-1) {
						// 文件編號放後面
						barcodeString = (barcodeString.length()>0 ? barcodeString + Constant.BARCODE_SEPARATOR : "") + barcodeText;
					}
				}
			}
		}

		if (logger.isDebugEnabled()) {
			logger.debug("barcodeString={}", barcodeString);
		}

		return barcodeString;
	}

}
