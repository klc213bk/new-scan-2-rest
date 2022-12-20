package com.tgl.newscan2rest.bean;

import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

/**
 * imagerecords.xml最上層類別<scanconfig xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"> 
 * @author ASDTEMP23
 *
 */
@XmlRootElement(name = "ImageRecordSet")
@XmlAccessorType(XmlAccessType.FIELD)
public class ImageRecordSet {

	@XmlElement(name = "Records")
	private TiffRecords records = null;

	public TiffRecords getRecords() {
		return records;
	}

	public void setRecords(TiffRecords records) {
		this.records = records;
	}

}
