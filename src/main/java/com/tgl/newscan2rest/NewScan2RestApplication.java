package com.tgl.newscan2rest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class NewScan2RestApplication {

	public static void main(String[] args) {
		System.setProperty("ASCAN_LICENSE_NAME", "TransGlobeComTw-ENT");
		System.setProperty("ASCAN_LICENSE_CODE", "0E7AE-CBAC0-D97BF-36AE5");
		
		SpringApplication.run(NewScan2RestApplication.class, args);
	}

}
