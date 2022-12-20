package com.tgl.newscan2rest.bean;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

/**
 * servers.xml <Server name="UPRODUAT" url="http://10.67.67.8:9080/ls" />
 * 
 * @author Vincent Chang
 *
 */
@XmlRootElement(name = "Server")
@XmlType(propOrder = { "name", "url" })
@XmlAccessorType(XmlAccessType.FIELD)
public class Server {

	@XmlAttribute
	private String name;
	@XmlAttribute
	private String url;

	public Server() {
	}

	public Server(String name, String url) {
		this.name = name;
		this.url = url;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

}
