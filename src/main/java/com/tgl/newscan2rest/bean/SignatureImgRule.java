package com.tgl.newscan2rest.bean;

/**
 * 簽名規則-對映ScanConfig.xml 內<signaturerule>資料
 * 
 * @author Jim Chin
 *
 */
public class SignatureImgRule {

	String subfilecode;//影像主類別編號
	String pageno;//頁次
	String posx;//影像截取-x軸定位點
	String posy;//影像截取-y軸定位點
	String length;//影像截取-長度
	String width;//影像截取-寬度
	
	public SignatureImgRule() {
	}
	
	public SignatureImgRule(String subfilecode,	String pageno,	String posx, String posy, String length,	String width) {
		this.subfilecode = subfilecode;
		this.pageno = pageno;
		this.posx = posx;
		this.posy = posy; 
		this.length = length;
		this.width = width;
	}

	public String getSubfilecode() {
		return subfilecode;
	}

	public void setSubfilecode(String subfilecode) {
		this.subfilecode = subfilecode;
	}

	public String getPageno() {
		return pageno;
	}

	public void setPageno(String pageno) {
		this.pageno = pageno;
	}

	public String getPosx() {
		return posx;
	}

	public void setPosx(String posx) {
		this.posx = posx;
	}

	public String getPosy() {
		return posy;
	}

	public void setPosy(String posy) {
		this.posy = posy;
	}

	public String getLength() {
		return length;
	}

	public void setLength(String length) {
		this.length = length;
	}

	public String getWidth() {
		return width;
	}

	public void setWidth(String width) {
		this.width = width;
	}

	@Override
	public String toString() {
		return "SignatureImgRule [subfilecode=" + subfilecode + ", pageno=" + pageno + ", posx=" + posx + ", posy="
				+ posy + ", length=" + length + ", width=" + width + "]";
	}

}
