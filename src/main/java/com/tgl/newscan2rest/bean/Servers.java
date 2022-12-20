package com.tgl.newscan2rest.bean;

import java.util.List;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

/**
 *  servers.xml最上層類別
 * 
 * @author Vincent Chang
 *
 */
@XmlRootElement(name = "Servers")
@XmlAccessorType(XmlAccessType.FIELD)
public class Servers {

	@XmlElement(name = "Server")
	private List<Server> serverList = null;
 
	public List<Server> getServerList() {
		return serverList;
	}

	public void setServerList(List<Server> serverList) {
		this.serverList = serverList;
	}

}
