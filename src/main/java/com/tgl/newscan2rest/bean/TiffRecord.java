package com.tgl.newscan2rest.bean;

import java.util.List;

import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

/**
 *  * imagerecords.xml  <Record fileName="2020100512210103101.tiff">
 * 
 * @author Jim Chin
 *
 */
@XmlRootElement(name = "Record")
@XmlAccessorType(XmlAccessType.FIELD)
public class TiffRecord { 

	@XmlAttribute(name = "fileName")
	private String fileName;
	
	@XmlElement(name = "Field")
	private List<TiffField> Fields = null;

	public List<TiffField> getFields() {
		return Fields;
	}

	public void setFields(List<TiffField> field) {
		Fields = field;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

}
