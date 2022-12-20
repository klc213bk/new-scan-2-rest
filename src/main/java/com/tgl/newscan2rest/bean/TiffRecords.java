package com.tgl.newscan2rest.bean;

import java.util.List;

import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

/**
 *  * imagerecords.xml <Records>
 * 
 * @author Jim Chin
 *
 */
@XmlRootElement(name = "Records")
@XmlAccessorType(XmlAccessType.FIELD)
public class TiffRecords {
 
	@XmlElement(name = "Record")
	private List<TiffRecord> recordList = null;
 
	public List<TiffRecord> getRecordList() {
		return recordList;
	}

	public void setRecordList(List<TiffRecord> recordList) {
		this.recordList = recordList;
	}

}
