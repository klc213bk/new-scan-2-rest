package com.tgl.newscan2rest.exception;


public class Scan2Exception extends Exception {
	private static final long serialVersionUID = 7490694120461502561L;

    public enum ErrorCode {

    	
    	COMMON(-1000,""),
    	
    	CONNECTION_NOT_INIT(-1100, "網路連線尚未初始化！"),
    	LOGIN_PAGE_ERROR(-1101, "系統錯誤！登入頁內容有誤，請通知系統管理者。"),
    	INCORRECT_ID_PASSWORD(-1102, "使用者帳號或密碼不正確！"),
    	PERMISSION_DENY(-1103, "使用者帳號「%s」無使用權限！"),
    	NO_SCAN_CONFIG_URL(-1104, "系統錯誤！網頁內容中查無掃描組態位址，請通知系統管理者。"),
    	NO_SCAN_CONFIG(-1105, "系統錯誤！查無掃描組態設定，請通知系統管理者。"),
    	
    	// upload
    	NO_UPLOAD_SELECTION(-2001, "請選擇欲上傳的影像"),
    	IMAGERECORDSET_FILE_CHANGED(-2002, "影像掃描設定檔被其他程式更動了，將重新載入設定檔！"),
    	IMAGE_FILE_REMOVED(-2003, "影像檔已經被移除，無法上傳檔案！\nScan files have been removed from disk! It can not be uploaded!"),
    	VALIDATE_PAGE_NUMBER_CHAR_ERROR(-2004, "頁數檢查錯誤"),
    	VALIDATE_BATCH_NO_ERROR(-2004, "批次號碼未輸入！"),
    	VALIDATE_BOX_NO_ERROR(-2005, "箱號未輸入！"),
    	NO_SEND_EMAIL_ERROR(-2006, "是否發EMAIL 未設置！"),
    	SEND_EMAIL_NOT_CONSISTENT_ERROR(-2007, "是否發EMAIL 未設置！"),
    	VALIDATE_IS_REMOTE_ERROR(-2008, "視訊投保件 設置不一致！"),
    	SAVE_TIFF_DATA_ERROR(-2009, "無法儲存設定檔"),
    	NOT_LOGIN(-2010, "未登入或離線使用時無法上傳檔案，請登入 eBao Server！"),
    	
    	
    	// imagerecordset file
    	CANNOT_PARSE_IMAGERECORDSET_FILE(-3001, "檔案內容有誤，無法解析！原檔案將備份並更名為 %s 以利除錯追蹤。"),
    	;
    	
    	private int code;
        private String message;

        private ErrorCode(int code, String message) {
        	this.code = code;
            this.message = message;
        }
        public int getCode() {
            return code;
        }
        public String getMessage() {
            return message;
        }
		public void setMessage(String message) {
			this.message = message;
		}

    }

    private ErrorCode errorCode = null;

    public Scan2Exception(ErrorCode errorCode) {
        super();
		this.setErrorCode(errorCode);
    }
    

	public ErrorCode getErrorCode() {
		return errorCode;
	}

	private void setErrorCode(ErrorCode errorCode) {
		this.errorCode = errorCode;
	}
	

	public Scan2Exception(Throwable cause) {
		super(cause);
		setErrorCode(ErrorCode.COMMON);
	}

	@Override
	public String getMessage() {
		return ErrorCode.COMMON.equals(errorCode) ? 
			this.getCause().getMessage() : 
				errorCode.getMessage();
	}
}
