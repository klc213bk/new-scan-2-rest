package com.tgl.newscan2rest.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.tgl.newscan2rest.bean.BillCard;
import com.tgl.newscan2rest.bean.ScanConfig;
import com.tgl.newscan2rest.bean.SignatureImgRule;

import javafx.util.Pair;

public class ScanConfigUtil {

	private static final String CONFIG_DIR = System.getProperty("user.dir") + File.separator + "config";
	private static final String SCAN_CONFIG_FILE_NAME = "scanconfig.xml";

	public ScanConfigUtil() {
	}

	public static void writeConfig(String config) throws IOException {
        Path imageArchivePath = Paths.get(CONFIG_DIR);
        Path scanConfigPath = Paths.get(CONFIG_DIR + File.separator + SCAN_CONFIG_FILE_NAME);

        if (!Files.isDirectory(imageArchivePath))
            Files.createDirectory(imageArchivePath);

        try ( Writer out = Files.newBufferedWriter(scanConfigPath, StandardCharsets.UTF_8) ) {
        	out.write(config);
        }
	}

	public static String readConfig() throws IOException {
        Path scanConfigPath = Paths.get(CONFIG_DIR + File.separator + SCAN_CONFIG_FILE_NAME);
        String config = null;

        try ( BufferedReader reader = Files.newBufferedReader(scanConfigPath, StandardCharsets.UTF_8) ) {
        	config = reader.lines().collect(Collectors.joining(System.lineSeparator()));
        }

        return config;
	}

	/**
	 * 解析 scanconfig.xml，使用 JSOP 元件
	 * @throws IOException
	 */
	public static ScanConfig parseHtml(String htmlBody) throws IOException {
		ScanConfig scanConfig = new ScanConfig();

//		Document doc = Jsoup.parse(htmlBody, "UTF-8", "");
		Document doc = Jsoup.parse(htmlBody);
		
		String uploadURL = "";
		for (Element row : doc.select("setting")) {
			uploadURL = row.textNodes().get(0).text();
		}
		scanConfig.setUploadURL(uploadURL.trim());

		// tag名稱:stringtable，內含畫面要顯示i18N文字(目前全部使用中文顯示)//
		Map<String, String> stringTablesMap = new HashMap<String, String>();
		for (Element row : doc.select("stringtable")) {
			for (Element param : row.children()) {
				stringTablesMap.put(param.attr("id"), param.attr("caption"));
			}
		}
		scanConfig.setStringtables(stringTablesMap);
		
		// tag名稱:imagerecordcolumns，影像佇列i18N文字
		Map<String, String> imageRecordColumnsMap = new HashMap<String, String>();
		for (Element row : doc.select("imagerecordcolumns")) {
			for (Element param : row.children()) {
				imageRecordColumnsMap.put(param.attr("name"), param.attr("caption"));
			}
		}
		scanConfig.setImagerecordcolumns(imageRecordColumnsMap);
		
		// tag名稱:signaturerule，紀錄每個文件需要截取簽名檔的頁次/位置(x,y,length,width)[會有一個頁次需要截截取多筆]
		// Map<subfilecode-pageno, List<SignatureImgRule>>
		// Map<文件編號-頁次, List<SignatureImgRule文件>>
		// example:Map<UNBA040-6, List<SignatureImgRule>>
		Map<String, List<SignatureImgRule>> signatureRuleMap = new HashMap<String, List<SignatureImgRule>>();
		for (Element row : doc.select("signaturerule")) {
			for (Element param : row.children()) {
				String subFileCode = param.attr("subfilecode");
				String pageNo = param.attr("pageno");
				SignatureImgRule signatureImgRule = new SignatureImgRule(
					subFileCode,
					pageNo, 
					param.attr("posx"), 
					param.attr("posy"), 
					param.attr("length"),
					param.attr("width")
				);

				String key = String.format("%s-%s", subFileCode, pageNo);
				List<SignatureImgRule> rules = signatureRuleMap.get(key);
				if (null==rules) {
					rules = new ArrayList<SignatureImgRule>();
					signatureRuleMap.put(key, rules);
				}
				rules.add(signatureImgRule);
			}
		} 
		scanConfig.setSignatureRules(signatureRuleMap);
		
		// BillCards影像主檔關連所屬的影像子類型檔案
		// Map<MainCard, Map<String, BillCard>>
		// billcards 影像子類型中文文件類型
		Map<String, Map<String, BillCard>> mainBillCardMap = new HashMap<String, Map<String, BillCard>>();
		Map<String, BillCard> allBillCardMap = new HashMap<String, BillCard>();
		for (Element row : doc.select("BillCards")) {
			for (Element param : row.children()) {
				String maincard = param.attr("maincard");
				String cardCode = param.attr("cardCode");

				BillCard billCard = new BillCard();
				billCard.setMainCard(maincard);
				billCard.setCardCode(cardCode);
				billCard.setCardId(param.attr("cardId"));
				billCard.setCardDesc(param.attr("cardDesc"));
				billCard.setMaxPage(param.attr("maxPage"));

				Map<String, BillCard> billCardMap = mainBillCardMap.get(maincard);
				if (null==billCardMap) {
					billCardMap = new HashMap<String, BillCard>();
					mainBillCardMap.put(maincard, billCardMap);
				}
				if (!billCardMap.containsKey(cardCode)) {
					billCardMap.put(cardCode, billCard);
				}

				allBillCardMap.put(cardCode, billCard);
			}
		}
		scanConfig.setBillCards(allBillCardMap);
		scanConfig.setMainBillCards(mainBillCardMap);

		// 索引編輯區
		for (Element row : doc.select("Editors")) {
			for (Element param : row.children()) {
				String paraName = param.attr("name");
				String paraValue = param.attr("value");
				if (paraName.equals("OrgName")) {
					scanConfig.setOrgName(paraValue);
				} 
				else if (paraName.equals("DeptName")) {
					scanConfig.setDeptName(paraValue);
				}
				else if (paraName.equals("empId")) {
					scanConfig.setEmpId(paraValue);
				}
				else if (paraName.equals("deptId")) {
					scanConfig.setDeptId(paraValue);
				}
				else if (paraName.equals("actionType")) {
					scanConfig.setActionType(paraValue);
				}
				else if (paraName.equals("step")) {
					scanConfig.setStep(paraValue);
				}
				else if (paraName.equals("imageSaveDir")) {
					scanConfig.setImageSaveDir(paraValue);
				}
				else if (paraName.equals("recordStatus")) {
					scanConfig.setRecordStatus(paraValue);
				}
				else if (paraName.equals("ImageFormat")) {
					scanConfig.setImageFormat(paraValue);
				}
				else if (paraName.equals("RocDate")) {
					scanConfig.setRocDate(paraValue);
				}
				else if (paraName.equals("updateRole")) {
					scanConfig.setUpdateRole(paraValue);
				}
				else if (paraName.equals("fromQueryPage")) {
					scanConfig.setFromQueryPage(paraValue);
				}
				else if (paraName.equals("isGID")) {
					scanConfig.setIsGID(paraValue);
				}
				else if (paraName.equals("bizDept")) {
					scanConfig.setBizDept(paraValue);
				}
				else if (paraName.equals("orgCode")) {
					scanConfig.setOrgCode(paraValue);
				}
				else if (paraName.equals("batchDepTypeValue")) {
					scanConfig.setBatchDepTypeValue(paraValue);
				}
				else if (paraName.equals("bizDept")) {
					scanConfig.setBizDept(paraValue);
				}
				else if (paraName.equals("defBoxNo")) {
					scanConfig.setDefBoxNo(paraValue);
				}
				else if (paraName.equals("defBatchDepType")) {
					scanConfig.setDefBatchDepType(paraValue);
				}
				else if (paraName.equals("defBatchDate")) {
					scanConfig.setDefBatchDate(paraValue);
				}
				else if (paraName.equals("defBatchArea")) {
					scanConfig.setDefBatchArea(paraValue);
				}
				else if (paraName.equals("defBatchDocType")) {
					scanConfig.setDefBatchDocType(paraValue);
				}
			}
		}

		// script billcard連動的script設定
		String[] str = doc.select("script").html().replace("<![CDATA[", "").trim().replaceAll("[\\t]+", "").split("\r|\n");
		
		String[] fileTypeNames = null;//用來儲放在ScaneConfig.xml中
		//<script><![CDATA[	
		//function getFileTypeName(fileType){
		//解析此區域內檔案對應文件名稱處理

		Pattern pattern = Pattern.compile("\"([^\"]*)\"");

		str = Arrays.stream(str).filter(value -> value != null && value.length() > 0)
				.toArray(size -> new String[size]);
		for (String value : str) {
			if (value.contains("var fileTypeNames")) {
				Matcher matcher = pattern.matcher(value);
				if (matcher.find())
					fileTypeNames = matcher.group().replace("\"", "").split("\\.");
			}
		}

		// initscript控制相關欄位初始化資料設定
		str = doc.select("oninitscript").html().replace("<![CDATA[", "").trim().
				replaceAll("[\\t]+", "").split("\r|\n");
		str = Arrays.stream(str).filter(value -> value != null && value.length() > 0).toArray(size -> new String[size]);

		for (String value : str) {
			if (value.contains("BatchDepType.addItem")) {// 批次號碼-部門別
				Matcher matcher = pattern.matcher(value);
				String[] keyValuePairStr = new String[2];
				int count = 0;
				while (matcher.find()) {
					keyValuePairStr[count] = matcher.group(1);
					count++;
				}
				scanConfig.getBatchDepTypeList().add(new Pair<String, String>(keyValuePairStr[0], keyValuePairStr[1]));
			}
			// 批次號碼-部門別
			if (value.contains("BatchDepType.setValue")) {
				String tagValue = getTagValue(pattern, value);
			    scanConfig.setBatchDepType(tagValue);
			}
			//日期
			else if (value.contains("BatchDate.value")) {
				String tagValue = getTagValue(pattern, value);
			    scanConfig.setBatchDate(tagValue);
			}
			// 影像主類型
			else if (value.contains("MainFileType.addItem")) {
				Matcher matcher = pattern.matcher(value);
				matcher.groupCount();
				String[] keyValuePairStr = new String[2];
				int count = 0;
				while (matcher.find()) {
					keyValuePairStr[count] = matcher.group(1);
					count++;
				}
				scanConfig.getMainFileTypeList().add(new Pair<String, String>(keyValuePairStr[0], keyValuePairStr[1]));
			}
			// mainFileTypes影像主類型與子類型文件對映關係
			else if (value.contains("var mainFileTypes")) {
				Matcher matcher = pattern.matcher(value);
				matcher.find();
				String[] mainFileTypes = matcher.group().split(";");
				for (int j = 0; j < mainFileTypes.length; j++) {
					String[] c = mainFileTypes[j].split(":");

					if (c.length == 2) {
						String mainFileType = c[0].replace("\"", "");
						List<Pair<String, String>> fileDocs = scanConfig.getTotalFileTypes().get(mainFileType);
						if (null==fileDocs) {
							fileDocs = new ArrayList<Pair<String, String>>();
							scanConfig.getTotalFileTypes().put(mainFileType, fileDocs);
						}
						String[] fileTypes = c[1].split(","); //子影像名稱集合
						for (int i = 0; i < fileTypes.length; i++) {
							fileDocs.add(new Pair<String, String>(fileTypes[i] , fileTypes[i] + "-" + getFileTypeName(fileTypeNames, fileTypes[i])));
						}
					}
				}
			}
			// 箱號
			else if (value.contains("BoxNo.addItem")) {
				String tagValue = getTagValue(pattern, value);
				if (!"".equals(tagValue)) {
					scanConfig.getBoxNos().add(tagValue);
				}
			}
			//取代初始值
			else if (value.contains("ActionReplace.setValue")) {
				String tagValue = getTagValue(pattern, value);
			    scanConfig.setActionReplace(tagValue);
			}	
			//插入初始值
			else if (value.contains("ActionInsert.setValue")) {
				String tagValue = getTagValue(pattern, value);
			    scanConfig.setActionInsert(tagValue);
			}	
			// 判斷啟動控制來源是否為網頁
			else if (value.contains("fromQueryPage.value=")) {
				String tagValue = getTagValue(pattern, value);
				scanConfig.setFromQueryPage(tagValue);
			}
			// 判斷啟動控制使用者有替換/插入權限
			else if (value.contains("updateRole.value=")) {
				String tagValue = getTagValue(pattern, value);
				scanConfig.setUpdateRole(tagValue);
			}
			//組織編號
			else if (value.contains("OrgName.value")) {// 
				String tagValue = getTagValue(pattern, value);
			    scanConfig.setOrgName(tagValue);
			}
			//部室名稱
			else if (value.contains("DeptName.value")) {// 
				String tagValue = getTagValue(pattern, value);
				scanConfig.setDeptName(tagValue);
			}
			
		}

		return scanConfig;
	}

	// TODO: 如有時間改寫這段
	private static String getFileTypeName(String[] fileTypeNames, String fileType) {
		String fileTypeName = "";
		for (int i = 0; i < fileTypeNames.length; i++) {
			String[] c = fileTypeNames[i].split("-");
			if (c[0].equals(fileType)) {
				if (c.length == 4) {
					String tmp = c[1] + "-" + c[2];
					fileTypeName = tmp.substring(0, tmp.indexOf("&"));
				} else {
					fileTypeName = c[1].substring(0, c[1].indexOf("&"));
				}
			}
		}
		return fileTypeName;
	}

	private static String getTagValue(Pattern pattern, String str) {
		String value = null;
		Matcher matcher = pattern.matcher(str);
		if (matcher.find()) {
			value = matcher.group(1);
		}
		return value;
	}

}
