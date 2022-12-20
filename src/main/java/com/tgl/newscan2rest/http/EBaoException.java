package com.tgl.newscan2rest.http;

public class EBaoException extends Exception {

    private static final long serialVersionUID = 7490694120461502561L;

    public enum Code {

    	COMMON(""),
    	CONNECTION_NOT_INIT("網路連線尚未初始化！"),
    	LOGIN_PAGE_ERROR("系統錯誤！登入頁內容有誤，請通知系統管理者。"),
    	INCORRECT_ID_PASSWORD("使用者帳號或密碼不正確！"),
    	PERMISSION_DENY("使用者帳號「%s」無使用權限！"),
    	NO_SCAN_CONFIG_URL("系統錯誤！網頁內容中查無掃描組態位址，請通知系統管理者。"),
    	NO_SCAN_CONFIG("系統錯誤！查無掃描組態設定，請通知系統管理者。");

        private String message;

        private Code(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }

    }

    private Code code = null;

    public EBaoException(Code code) {
        super();
		this.setCode(code);
    }

	public Code getCode() {
		return code;
	}

	private void setCode(Code code) {
		this.code = code;
	}

	public EBaoException(Throwable cause) {
		super(cause);
		setCode(Code.COMMON);
	}

	@Override
	public String getMessage() {
		return Code.COMMON.equals(code) ? 
			this.getCause().getMessage() : 
			code.getMessage();
	}

}
