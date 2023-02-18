package com.tgl.newscan2rest.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.tgl.newscan2rest.bean.ScannedImage;

public class PageNoValidator {

	private static final Logger logger = LogManager.getLogger(PageNoValidator.class);

	private int maxPageNo;
	private int index;
	private boolean multiPolicy;
	private List<ScannedImage> data;

	public PageNoValidator() {
		this.maxPageNo = 0;
		this.index = -1;
		this.multiPolicy = false;
		this.data = new ArrayList<ScannedImage>();
	}

	public void add(ScannedImage scannedImage) {
		this.data.add(scannedImage);
		this.index = data.size() - 1;
		if (this.index == 0) {
			String _maxPage = ObjectsUtil.isEmpty(scannedImage.maxPageProperty().getValue()) ? "0" : scannedImage.maxPageProperty().getValue();
			this.maxPageNo = Integer.parseInt(_maxPage);
		}
	}

	public void clear() {
		this.data.clear();
	}

	public int size() {
		return this.data.size();
	}

	public void setMultiPolicy(boolean multiPolicy) {
		this.multiPolicy = multiPolicy;
	}

	public ScannedImage get(int index) {
		return this.data.get(index);
	}

	public boolean validate() {
		int rowCount = data.size();
		int remainder = 0;
		int multiCnt = 1;
		int refPageNo = 1;

		if (rowCount<=0 || maxPageNo<=0) {
			return true;
		}

		// 1. Check Record size is Matched MaxPageNo

		remainder = rowCount % this.maxPageNo;

		if (logger.isDebugEnabled()) {
			logger.debug("rowCount={}, maxPageNo={}, remainder={}, multiPolicy={}", rowCount, this.maxPageNo, remainder, this.multiPolicy);
		}

		if (remainder!=0) {
			if (logger.isDebugEnabled()) {
				logger.debug("remainder!=0, return false! (remainder={})", remainder);
			}
			return false;
		}
		if (this.multiPolicy) {
			multiCnt = rowCount / this.maxPageNo;
		}

		if (logger.isDebugEnabled()) {
			logger.debug("multiCnt={}", multiCnt);
			logger.debug("Start to check page no...");
		}

		// 2. Check pageNo is sequence

		for (int i=0; i<rowCount; i++) {
			ScannedImage scannedImage = data.get(i);
			String _filePage = scannedImage.filePageProperty().getValue();
			int filePage = Integer.parseInt(_filePage);

			if (logger.isDebugEnabled()) {
				logger.debug("filePage={}, refPageNo={}", filePage, refPageNo);
			}

			if (filePage != refPageNo) {
				// IR:263559,同一保單號,同一影像類型第2..N份
				int mod = refPageNo % this.maxPageNo;
				if (filePage!=mod && mod!=0) {
					if (logger.isDebugEnabled()) {
						logger.debug("filePage != refPageNo, return false!");
					}
					return false;
				} else if (this.maxPageNo == 1 && filePage != 1) {
					if (logger.isDebugEnabled()) {
						logger.debug("this.maxPageNo == 1 && filePage != 1, return false!");
					}
					return false;
				}
			}

			if (this.multiPolicy) {
				remainder = (i+1) % multiCnt;
				if (remainder == 0) {
					refPageNo++; // Change Page No
				}
			} else {
				refPageNo++;
			}
		}

		return true;
	}

}
