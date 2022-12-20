package com.tgl.newscan2rest.bean;

import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

/**
 * imagerecords.xml  <Field name="fileName" value="2020100512220156201.tiff" text="2020100512220156201.tiff"/> 
 * 
 * @author Jim Chin
 *
 */
@XmlRootElement(name = "Field")
@XmlType(propOrder={"name", "value", "text"})
@XmlAccessorType(XmlAccessType.FIELD)
public class TiffField { 
	 
	@XmlAttribute
	private String name;
	@XmlAttribute
	private String value;
	@XmlAttribute
	private String text;
	
	public TiffField() {
		
	}
	
	public TiffField(String name, String value, String text) {
		this.name = name;
		this.value = value;
		this.text = text;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}

}
