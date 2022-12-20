package com.tgl.newscan2rest.http;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.Consts;
import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HeaderElementIterator;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeaderElementIterator;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.Args;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.tgl.newscan2rest.Constant;
import com.tgl.newscan2rest.bean.ConnectionInfo;
import com.tgl.newscan2rest.bean.ScanConfig;
import com.tgl.newscan2rest.bean.ScannedImage;
import com.tgl.newscan2rest.bean.UploadStatus;
import com.tgl.newscan2rest.util.ObjectsUtil;
import com.tgl.newscan2rest.util.ScanConfigUtil;

public class EbaoClient {

	private static final Logger logger = LogManager.getLogger(EbaoClient.class);

	private static final long DEFAULT_KEEP_ALIVE_TIME = 5 * 1000; // 5 seconds
	private static final String LOGIN_PAGE_PATH = "/loginPage.do";
	private static final String LOGIN_PATH = "/login.do";
	private static final String SYS_MENU_PATH = "/sys.menu.do";
	private static final String IMAGE_SCAN_PATH = "/image/ImageScan.do";

	private static EbaoClient instance  = new EbaoClient();

    private void EbaoClient() {
    }

    public static EbaoClient getInstance() {
        return EbaoClient.instance;
    }

	private String hostUrl;
	private String userName;
	private String password;
    private String requestToken;
    private boolean initialed = false;
    private boolean scanAllowed = false;

	public EbaoClient setHostUrl(String hostUrl) {
		this.hostUrl = hostUrl;
		return this;
	}

	public EbaoClient setUserName(String userName) {
		this.userName = userName;
		return this;
	}

	public EbaoClient setPassword(String password) {
		this.password = password;
		return this;
	}

	public boolean configChanged(String hostUrl, String userName, String password) {
		String tmp = "" + hostUrl + userName + password;
		String current = "" + this.hostUrl + this.userName + this.password;
		return !tmp.equals(current);
	}

	public boolean allowScan() {
		return this.scanAllowed;
	}

	private PoolingHttpClientConnectionManager connManager = null;
	private CloseableHttpClient httpClient = null;
	private BasicHttpContext localContext = null;
	private BasicCookieStore cookieStore = null;

	public EbaoClient init() {
		if (this.initialed) {
			this.requestToken = null;
	        this.scanAllowed = false;
			this.localContext.clear();
			this.cookieStore.clear();
		} else {
	        this.requestToken = null;
	        this.scanAllowed = false;

			this.localContext = new BasicHttpContext();
			this.cookieStore = new BasicCookieStore();
			this.connManager = new PoolingHttpClientConnectionManager();
			this.httpClient = HttpClients.custom()
				.setKeepAliveStrategy(new DefaultConnectionKeepAliveStrategy())
				.setConnectionManager(this.connManager)
				.setDefaultCookieStore(this.cookieStore)
				.build();

			this.initialed = true;
		}

		if (logger.isDebugEnabled()) {
			logger.debug("localContext={}", localContext.toString());
			logger.debug("cookieStore={}", cookieStore.toString());
		}

		return this;
	}

	public ConnectionInfo connect() throws EBaoException {
        Args.notNull(this.hostUrl, "hostUrl");

		if (!this.initialed) {
			throw new EBaoException(EBaoException.Code.CONNECTION_NOT_INIT);
		}

		// 檢查 eBao Server 連線資訊是否正確
        String server = null;
        String token = null;
        String uri = this.hostUrl + EbaoClient.LOGIN_PAGE_PATH;
		HttpGet request = null;

		if (logger.isDebugEnabled()) {
			logger.debug("uri={}", uri);
		}

		try {
			request = new HttpGet(uri);
		} catch (Exception e) {
			throw new EBaoException(e);
		}

		// 連接 eBao Server 取得 syskey_request_token
		try (
			CloseableHttpResponse response = httpClient.execute(request, this.localContext);
		) {
            int statusCode = response.getStatusLine().getStatusCode();
			String result = EntityUtils.toString(response.getEntity());

			if (logger.isDebugEnabled()) {
				logger.debug("statusCode={}", statusCode);
				logger.debug("result={}", result);
				logger.debug("localContext={}", localContext.toString());
				logger.debug("cookieStore={}", cookieStore.toString());
			}

			if (statusCode == 200) {
				Document doc = Jsoup.parse(result);
				for (Element row : doc.select("input[name=syskey_request_token]")) {
					token = row.val();
					break;
				}
				for (Element row : doc.select("div[class=b_server]")) {
					server = row.ownText();
					break;
				}
			}
		} catch (ClientProtocolException cpe) {
			throw new EBaoException(cpe);
		} catch (IOException ioe) {
			throw new EBaoException(ioe);
		} catch (ParseException pe) {
			throw new EBaoException(pe);
		} catch (Exception e) {
			throw new EBaoException(e);
		}

		if (logger.isDebugEnabled()) {
			logger.debug("token={}", token);
		}

		if (null == token) {
			throw new EBaoException(EBaoException.Code.LOGIN_PAGE_ERROR);
		}

		return new ConnectionInfo(server, (this.requestToken = token));
	}

	public boolean login() throws EBaoException {
		Args.notNull(this.userName, "userName");
        Args.notNull(this.password, "userPassword");
        Args.notNull(this.requestToken, "token");

		if (!this.initialed) {
			throw new EBaoException(EBaoException.Code.CONNECTION_NOT_INIT);
		}

        List<NameValuePair> form = new ArrayList<>();
		form.add(new BasicNameValuePair("userName", this.userName));
		form.add(new BasicNameValuePair("userPassword", this.password));
		form.add(new BasicNameValuePair("syskey_request_token", this.requestToken));
		UrlEncodedFormEntity entity = new UrlEncodedFormEntity(form, Consts.UTF_8);

        // Step 1: 開始登入
        String uri = this.hostUrl + EbaoClient.LOGIN_PATH;
        HttpPost request = new HttpPost(uri);
        request.setEntity(entity);

		if (logger.isDebugEnabled()) {
			logger.debug("uri={}", uri);
		}

        CloseableHttpResponse response = null;
		int statusCode = -1;
		boolean redirect = false;
		String result = null;

		try {
			while (true) {
				response = this.httpClient.execute(request, this.localContext);
	            statusCode = response.getStatusLine().getStatusCode();
				result = EntityUtils.toString(response.getEntity());

				if (logger.isDebugEnabled()) {
					logger.debug("statusCode={}", statusCode);
					logger.debug("result={}", result);
					logger.debug("localContext={}", localContext.toString());
					logger.debug("cookieStore={}", cookieStore.toString());
				}

				if (statusCode == 302) {
			        // Step 2: 登入成功後會轉址
					redirect = true;
		            Header header = response.getFirstHeader("location"); // 跳轉的目標位址是在 HTTP-HEAD 中的 location
		            uri = header.getValue(); // 再向這個新位址發出新申請，以便得到跳轉後的內容。

		    		if (logger.isDebugEnabled()) {
		    			logger.debug("redirect to uri={}", uri);
		    		}

		            request = new HttpPost(uri);
				} else {
					break;
				}
			}

			if (!redirect) {
				throw new EBaoException(EBaoException.Code.INCORRECT_ID_PASSWORD);
			}

    		if (logger.isDebugEnabled()) {
    			logger.debug("result={}", result);
    		}

			// Step 3: 取得有使用權限的功能選單
			if (null != result) {
		        uri = this.hostUrl + EbaoClient.SYS_MENU_PATH;

		        request = new HttpPost(uri);
		        //request.setEntity(entity);
		        List<NameValuePair> xform = new ArrayList<>();
		        xform.add(new BasicNameValuePair("syskey_request_token", this.requestToken));
		        UrlEncodedFormEntity xentity = new UrlEncodedFormEntity(xform, Consts.UTF_8);
		        request.setEntity(xentity);
	            response = this.httpClient.execute(request, this.localContext);
	            statusCode = response.getStatusLine().getStatusCode();
				result = EntityUtils.toString(response.getEntity());

				if (statusCode == 200 && result != null && result.indexOf("id:'400001'") > 0) { // 影像掃描功能
					this.scanAllowed = true;
				}

				if (logger.isDebugEnabled()) {
	    			logger.debug("Get system menu: uri={}", uri);
					logger.debug("statusCode={}", statusCode);
					logger.debug("result={}", result);
					logger.debug("localContext={}", localContext.toString());
					logger.debug("cookieStore={}", cookieStore.toString());
					logger.debug("scanAllowed={}", scanAllowed);
	    		}
			}
		} catch (ClientProtocolException cpe) {
			throw new EBaoException(cpe);
		} catch (IOException ioe) {
			throw new EBaoException(ioe);
		} catch (ParseException pe) {
			throw new EBaoException(pe);
		} catch (Exception e) {
			throw new EBaoException(e);
		} finally {
			if (null != response) {
				try {
					response.close();
				} catch (IOException e) {
					logger.error("Close response error!", e);
				}
			}
		}

		if (!this.scanAllowed) {
			throw new EBaoException(EBaoException.Code.PERMISSION_DENY);
		}

		return this.scanAllowed;
	}

	public ScanConfig getScanConfig() throws EBaoException {
		// 進入點 1: 從本機 [開始] --> [程式集] --> [全球人壽] --> [新版影像掃描]，進入 ScanApp 後取得 ScanConfig
		// 進入點 2: 從 eBao 功能選單 [影像] --> [影像掃描] --> [新版影像掃描]，進入 ScanApp 後取得 ScanConfig
		// 進入點 3: 從 eBao 功能選單 [影像] --> [影像查詢] 查詢頁中點選箱號進入 ScanApp 後取得 ScanConfig，與前2項登入時的內容不太相同

		Args.notNull(this.requestToken, "token");

		if (!this.initialed) {
			throw new EBaoException(EBaoException.Code.CONNECTION_NOT_INIT);
		}

		HttpGet request = null;
        CloseableHttpResponse response = null;
		int statusCode = -1;
		String result = null;
		String scanConfigUrl = null;
		Document doc = null;
		ScanConfig scanConfig = null;

		try {

			// 取得影像掃描網址
			String uri = this.hostUrl + EbaoClient.IMAGE_SCAN_PATH + String.format("?syskey_request_token=%s&current_module_id=%s", this.requestToken, "400001");
	        request = new HttpGet(uri);

			if (logger.isDebugEnabled()) {
				logger.debug("uri={}", uri);
			}

            response = this.httpClient.execute(request, this.localContext);
            statusCode = response.getStatusLine().getStatusCode();
			result = EntityUtils.toString(response.getEntity());

			if (logger.isDebugEnabled()) {
				logger.debug("statusCode={}", statusCode);
				logger.debug("result={}", result);
				logger.debug("localContext={}", localContext.toString());
				logger.debug("cookieStore={}", cookieStore.toString());
			}

			if (statusCode == 200 && result != null) {
				doc = Jsoup.parse(result);
				for (Element row : doc.select("object")) {
					for (Element param : row.children()) {
						String tagName = param.tagName();
						String tagValue = param.attr("value");
						if (logger.isDebugEnabled()) {
							logger.debug("{}={}", tagName, tagValue);
						}
						if ( "param".equals(tagName)) {
							scanConfigUrl = tagValue;
						}
					}
				}
			}

			if (null == scanConfigUrl) {
				throw new EBaoException(EBaoException.Code.NO_SCAN_CONFIG_URL);
			}

	        // 取得 ScanConfig
			request = new HttpGet(scanConfigUrl);
            response = this.httpClient.execute(request, this.localContext);
            statusCode = response.getStatusLine().getStatusCode();
			result = EntityUtils.toString(response.getEntity());
			String config = null;

			if (statusCode == 200 && result != null) {
				doc = Jsoup.parse(result);
				for (Element row : doc.select("body")) {
					for (Element param : row.children()) {
						config = param.toString();
					}
				}
			}

			if (logger.isDebugEnabled()) {
				logger.debug("scanConfigUrl={}", scanConfigUrl);
				logger.debug("statusCode={}", statusCode);
				logger.debug("result={}", result);
				logger.debug("localContext={}", localContext.toString());
				logger.debug("cookieStore={}", cookieStore.toString());
				logger.debug("config={}", config);
			}

			if (null == config) {
				throw new EBaoException(EBaoException.Code.NO_SCAN_CONFIG);
			}

			ScanConfigUtil.writeConfig(config);
			scanConfig = ScanConfigUtil.parseHtml(config);
			
		} catch (ClientProtocolException cpe) {
			throw new EBaoException(cpe);
		} catch (IOException ioe) {
			throw new EBaoException(ioe);
		} catch (ParseException pe) {
			throw new EBaoException(pe);
		} catch (EBaoException ebe) {
			throw ebe;
		} catch (Exception e) {
			throw new EBaoException(e);
		} finally {
			if (null != response) {
				try {
					response.close();
				} catch (IOException e) {
					logger.error("Close response error!", e);
				}
			}
		}

        return scanConfig;
	}

	public UploadStatus upload(String uploadUrl, ScannedImage imageItem) throws EBaoException {
		Args.notNull(uploadUrl, "uploadUrl");
		Args.notNull(imageItem, "imageItem");

		if (!this.initialed) {
			throw new EBaoException(EBaoException.Code.CONNECTION_NOT_INIT);
		}

		long startTime = System.currentTimeMillis();

		String fileURL = imageItem.fileURLProperty().get();
		String imageFormat = "image/" + imageItem.imageFormatProperty().get();
		String pageAction = imageItem.pageActionProperty().get();
		String allowReserved = imageItem.allowReservedProperty().get();
		String scanOrder = imageItem.scanOrderProperty().get();
		String orgCode = imageItem.orgCodeProperty().get();
		String deptId = imageItem.deptIdProperty().get();
		String empId = imageItem.empIdProperty().get();
		String mainFileType = imageItem.mainFileTypeProperty().get();
		String fileType = imageItem.fileTypeProperty().get();
		String fileCode = imageItem.fileCodeProperty().get();
		String filePage = imageItem.filePageProperty().get();
		String boxNo = imageItem.boxNoProperty().get();
		String batchDepType = imageItem.batchDepTypeProperty().get();
		String batchDate = imageItem.batchDateProperty().get();
		String batchArea = imageItem.batchAreaProperty().get();
		String batchDocType = imageItem.batchDocTypeProperty().get();
		String companyCode = imageItem.companyCodeProperty().get();
		String personalCode = imageItem.personalCodeProperty().get();
		String actionReplace = imageItem.actionReplaceProperty().get();
		String actionInsert = imageItem.actionInsertProperty().get();
		String sendEmail = imageItem.sendEmailProperty().get();
		String isRemote = imageItem.isRemoteProperty().get();
		String remark = imageItem.remarkProperty().get();
		String scanTime = imageItem.scanTimeProperty().get();
		String signature = imageItem.signatureProperty().get();
		String sigSeqNumber = imageItem.sigSeqNumberProperty().get();
		String updateRole = imageItem.updateRoleProperty().get();
		String fileName = imageItem.fileNameProperty().get();
		String actionType = imageItem.actionTypeProperty().get();
		String fromQueryPage = imageItem.fromQueryPageProperty().get();
		String imageSaveDir = imageItem.imageSaveDirProperty().get();
		String bizDept = imageItem.bizDeptProperty().get();
		String isGID = imageItem.isGIDProperty().get();
		String rocDate = imageItem.rocDateProperty().get();
		String step = imageItem.stepProperty().get();
		String maxPage = imageItem.maxPageProperty().get();
		String recordStatus = imageItem.recordStatusProperty().get();

		String tmpMainFileType = ObjectsUtil.isEmpty(mainFileType) ? "" : mainFileType;
		String tmpFileType = ObjectsUtil.isEmpty(fileType) ? "" : fileType;
		String tmpFileCode = ObjectsUtil.isEmpty(fileCode) ? "" : fileCode;
		String tmpFilePage = ObjectsUtil.isEmpty(filePage) ? "" : filePage;
		String tmpMaxPage = ObjectsUtil.isEmpty(maxPage) ? "" : maxPage;
		String tmpSendEmail = ObjectsUtil.isEmpty(sendEmail) ? "N" : sendEmail;
		String tmpIsRemote = ObjectsUtil.isEmpty(isRemote) ? "N" : isRemote;
		String tmpRemark = ObjectsUtil.isEmpty(remark) ? "" : remark;
		String tmpCompanyCode = ObjectsUtil.isEmpty(companyCode) ? "" : companyCode;
		String tmpPersonalCode = ObjectsUtil.isEmpty(personalCode) ? "" : personalCode;

		if (logger.isDebugEnabled()) {
			logger.debug("uploadUrl={}", uploadUrl);
			logger.debug("imageFormat={}, pageAction={}, allowReserved={}, orgCode={}, deptId={}", imageFormat, pageAction, allowReserved, orgCode, deptId);
			logger.debug("empId={}, tmpMainFileType={}, tmpFileType={}, tmpFileCode={}, tmpFilePage={}", empId, tmpMainFileType, tmpFileType, tmpFileCode, tmpFilePage);
			logger.debug("boxNo={}, batchDepType={}, batchDate={}, batchArea={}, batchDocType={}", boxNo, batchDepType, batchDate, batchArea, batchDocType);
			logger.debug("scanOrder={}, scanTime={}, signature={}, sigSeqNumber={}", scanOrder, scanTime, signature, sigSeqNumber);
			logger.debug("updateRole={}, fileName={}, actionType={}, fromQueryPage={}, imageSaveDir={}", updateRole, fileName, actionType, fromQueryPage, imageSaveDir);
			logger.debug("bizDept={}, isGID={}, rocDate={}, step={}, tmpMaxPage={}", bizDept, isGID, rocDate, step, tmpMaxPage);
			logger.debug("actionReplace={}, actionInsert={}, recordStatus={}", actionReplace, actionInsert, recordStatus);
			logger.debug("");
			logger.debug("sendEmail={}, tmpSendEmail={}", sendEmail, tmpSendEmail);
			logger.debug("isRemote={}, tmpIsRemote={}", isRemote, tmpIsRemote);
			logger.debug("remark={}, tmpRemark={}", remark, tmpRemark);
			logger.debug("companyCode={}, tmpCompanyCode={}", companyCode, tmpCompanyCode);
			logger.debug("personalCode={}, tmpPersonalCode={}", personalCode, tmpPersonalCode);
		}

		File file = new File(fileURL);

		MultipartEntityBuilder builder = MultipartEntityBuilder.create()
			.setMode(HttpMultipartMode.BROWSER_COMPATIBLE)
			.addPart("imageFile", new FileBody(file, ContentType.DEFAULT_BINARY))
			.addPart("ImageFormat", new StringBody(imageFormat, ContentType.MULTIPART_FORM_DATA))
			.addPart("imageFormat", new StringBody(imageFormat, ContentType.MULTIPART_FORM_DATA))
			.addPart("PageAction", new StringBody(pageAction, ContentType.MULTIPART_FORM_DATA))
			.addPart("AllowReserved", new StringBody(allowReserved, ContentType.MULTIPART_FORM_DATA))
			.addPart("OrgCode", new StringBody(orgCode, ContentType.MULTIPART_FORM_DATA))
			.addPart("deptId", new StringBody(deptId, ContentType.MULTIPART_FORM_DATA))
			.addPart("empId", new StringBody(empId, ContentType.MULTIPART_FORM_DATA))
			.addPart("MainFileType", new StringBody(tmpMainFileType, ContentType.MULTIPART_FORM_DATA))
			.addPart("FileType", new StringBody(tmpFileType, ContentType.MULTIPART_FORM_DATA))
			.addPart("FileCode", new StringBody(tmpFileCode, ContentType.MULTIPART_FORM_DATA))
			.addPart("FilePage", new StringBody(tmpFilePage, ContentType.MULTIPART_FORM_DATA))
			.addPart("BoxNo", new StringBody(boxNo, ContentType.MULTIPART_FORM_DATA))
			.addPart("BatchDepType", new StringBody(batchDepType, ContentType.MULTIPART_FORM_DATA))
			.addPart("BatchDate", new StringBody(batchDate, ContentType.MULTIPART_FORM_DATA))
			.addPart("BatchArea", new StringBody(batchArea, ContentType.MULTIPART_FORM_DATA))
			.addPart("BatchDocType", new StringBody(batchDocType, ContentType.MULTIPART_FORM_DATA))
			.addPart("ActionReplace", new StringBody(actionReplace, ContentType.MULTIPART_FORM_DATA))
			.addPart("ActionInsert", new StringBody(actionInsert, ContentType.MULTIPART_FORM_DATA))
			.addPart("SendEmail", new StringBody(tmpSendEmail, ContentType.MULTIPART_FORM_DATA))
			.addPart("IsRemote", new StringBody(tmpIsRemote, ContentType.MULTIPART_FORM_DATA))
			.addPart("Remark", new StringBody(tmpRemark, ContentType.create("multipart/form-data", Consts.UTF_8)))
			.addPart("CompanyCode", new StringBody(tmpCompanyCode, ContentType.MULTIPART_FORM_DATA))
			.addPart("PersonalCode", new StringBody(tmpPersonalCode, ContentType.MULTIPART_FORM_DATA))
			.addPart("ScanOrder", new StringBody(scanOrder, ContentType.MULTIPART_FORM_DATA))
			.addPart("ScanTime", new StringBody(scanTime, ContentType.MULTIPART_FORM_DATA))
			.addPart("Signature", new StringBody(signature, ContentType.MULTIPART_FORM_DATA))
			.addPart("SigSeqNumber", new StringBody(sigSeqNumber, ContentType.MULTIPART_FORM_DATA))
			.addPart("updateRole", new StringBody(updateRole, ContentType.MULTIPART_FORM_DATA))
			.addPart("fileName", new StringBody(fileName, ContentType.MULTIPART_FORM_DATA))
			.addPart("actionType", new StringBody(actionType, ContentType.MULTIPART_FORM_DATA))
			.addPart("fromQueryPage", new StringBody(fromQueryPage, ContentType.MULTIPART_FORM_DATA))
			.addPart("imageSaveDir", new StringBody(imageSaveDir, ContentType.MULTIPART_FORM_DATA))
			.addPart("bizDept", new StringBody(bizDept, ContentType.MULTIPART_FORM_DATA))
			.addPart("isGID", new StringBody(isGID, ContentType.MULTIPART_FORM_DATA))
			.addPart("RocDate", new StringBody(rocDate, ContentType.MULTIPART_FORM_DATA))
			.addPart("step", new StringBody(step, ContentType.MULTIPART_FORM_DATA))
			.addPart("MaxPage", new StringBody(tmpMaxPage, ContentType.MULTIPART_FORM_DATA))
			.addPart("recordStatus", new StringBody(recordStatus, ContentType.MULTIPART_FORM_DATA));

		HttpEntity entity = builder.build();

		HttpPost post = new HttpPost(uploadUrl);
		post.setEntity(entity);

        CloseableHttpResponse response = null;
		int statusCode = -1;
		String result = null;
		UploadStatus status = null;

		try {
            response = this.httpClient.execute(post, this.localContext);
            statusCode = response.getStatusLine().getStatusCode();
            result = EntityUtils.toString(response.getEntity());

			if (logger.isDebugEnabled()) {
				logger.debug("statusCode={}", statusCode);
				logger.debug("result={}", result);
				logger.debug("localContext={}", localContext.toString());
				logger.debug("cookieStore={}", cookieStore.toString());
			}

			if (statusCode == 200 && result != null) {
				status = new UploadStatus(result);
			}
		} catch (IOException ioe) {
			throw new EBaoException(ioe);
		} catch (Exception e) {
			throw new EBaoException(e);
		} finally {
			if (null != response) {
				try {
					response.close();
				} catch (IOException e) {
					logger.error("Close response error!", e);
				}
			}
		}

		if (logger.isDebugEnabled()) {
			double processTime = (double)(System.currentTimeMillis()-startTime)/1000;
			logger.debug("Upload spend time: {} s", processTime);
			logger.debug("Delete Upload file: {}", fileURL);
		}

		// UAT-IR-479059，改在上傳後就刪除實體檔案
		if (status!=null && status.getCode()!=null && status.getCode()==0) {
			if (logger.isDebugEnabled()) {
				logger.debug("Delete Upload file: {}", fileURL);
			}
			Path filePath = Paths.get(fileURL);
			try {
				Files.delete(filePath);
			} catch (Exception e) {
				String msg = String.format("無法刪除已上傳的影像檔 %s ！", fileURL);
				logger.error(msg, e);
			}
		}

		return status;
	}

	private UploadStatus resultToUploadStatus(String result, ScanConfig scanConfig) {
		UploadStatus status = null;
		if (Constant.ERRCODE_IMAGE_DUPLICATE.equals(result)) {
			result = scanConfig.getValueInTable(Constant.IMAGE_DUPLICATE, result);
		} else if (Constant.ERRCODE_PARA_NOT_ENOUGH.equals(result)) {
			result = scanConfig.getValueInTable(Constant.PARA_NOT_ENOUGH, result);
		}

		status = new UploadStatus(result);

		if (15 == status.getCode()) {
			status.setDescription("未找到可被替換的影像");
		} else if (16 == status.getCode()) {
			status.setDescription("未找到可被插入的影像");
		} else if (13712 == status.getCode()) {
			status.setDescription("保單號碼不存在!");
		} else if (12324 == status.getCode()) {
			status.setDescription("參數傳輸不全!");
		} else if (400 == status.getCode()) {
			status.setDescription("保全還原件!");
		}

		return status;
	}

	public void close() {
		if (!this.initialed) {
			return;
		}

		this.localContext = null;
		this.cookieStore = null;
		try {
			this.httpClient.close();
			this.connManager.close();
		} catch (IOException e) {
			logger.error("Close httpClient|connManager error!", e);
		}

		this.initialed = false;
	}

	private class DefaultConnectionKeepAliveStrategy implements ConnectionKeepAliveStrategy {

		@Override
		public long getKeepAliveDuration(HttpResponse response, HttpContext context) {
			HeaderElementIterator it = new BasicHeaderElementIterator(response.headerIterator(HTTP.CONN_KEEP_ALIVE));
			while (it.hasNext()) {
				HeaderElement he = it.nextElement();
				String param = he.getName();
				String value = he.getValue();
				if (value != null && param.equalsIgnoreCase("timeout")) {
					return Long.parseLong(value) * 1000;
				}
			}
			return DEFAULT_KEEP_ALIVE_TIME;
		}

	}

}
