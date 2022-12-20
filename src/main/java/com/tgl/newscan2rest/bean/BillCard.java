package com.tgl.newscan2rest.bean;

/**
 * 文件類別-對映ScanConfig.xml 內<billcards>資料
 * 
 * @author Jim Chin
 *
 */
public class BillCard {

	private String mainCard;  // 部門代碼，ex:"UNB"
	private String cardId;    // 文件編號，ex:"11001"
	private String cardCode;  // 部門文件編號，ex:"UNBA001"
	private String cardDesc;  // 文件中文描述，ex:"傳統型保險要保書"
	private String maxPage;   // 文件最大頁數，ex:"6"

	public String getMainCard() {
		return mainCard;
	}

	public void setMainCard(String mainCard) {
		this.mainCard = mainCard;
	}

	public String getCardId() {
		return cardId;
	}

	public void setCardId(String cardId) {
		this.cardId = cardId;
	}

	public String getCardCode() {
		return cardCode;
	}

	public void setCardCode(String cardCode) {
		this.cardCode = cardCode;
	}

	public String getCardDesc() {
		return cardDesc;
	}

	public void setCardDesc(String cardDesc) {
		this.cardDesc = cardDesc;
	}

	public String getMaxPage() {
		return maxPage;
	}

	public void setMaxPage(String maxPage) {
		this.maxPage = maxPage;
	}

}
