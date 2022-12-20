package com.tgl.newscan2rest.util;

import java.io.File;
import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.tgl.newscan2rest.bean.Server;
import com.tgl.newscan2rest.bean.Servers;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;

public class ServersRecordHelper {

    private static final Logger logger = LogManager.getLogger(ServersRecordHelper.class);

    public static final String CONFIG_DIR = System.getProperty("user.dir") + File.separator + "config";
    public static final String SERVERS_FILE_NAME = "servers.xml";
    public static final String SERVERS_FILE_FULL_NAME = CONFIG_DIR + File.separator + SERVERS_FILE_NAME;

	private static ServersRecordHelper instance  = new ServersRecordHelper();

	public static ServersRecordHelper getInstance() {
		return ServersRecordHelper.instance;
	}

	private ServersRecordHelper() {
	}

	public Servers unmarshalFromFile() {
		logger.debug("Load servers.xml...");

		JAXBContext jaxbContext;
		Servers servers = null;
		try {
			jaxbContext = new org.glassfish.jaxb.runtime.v2.JAXBContextFactory()
			    .createContext(new Class[] {Servers.class}, null);
			Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
			servers = (Servers) jaxbUnmarshaller.unmarshal(new File(SERVERS_FILE_FULL_NAME));
		} catch (JAXBException e) {
			logger.error("Load servers.xml error!", e);
		}
		if ( null==servers || null==servers.getServerList() ) {
			logger.debug("Not servers!");
			servers.setServerList(new ArrayList<Server>());
		}

		return servers;
	}


}
