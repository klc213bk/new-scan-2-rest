package com.tgl.newscan2rest.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class BigDecimalUntils {

    public static BigDecimal cmToPixel(String cm) throws NumberFormatException {
    	// <rule subfilecode="UNBA040" pageno="6" posx="0.5" posy="22.3" length="6.8" width="20" />
		// A4 + 300 DPI = w:2480px	h:3508px
		//              = w:210mm	h:297mm
		//              = w:8.27inch h:11.69inch
		// w --> 1mm = 2480/210 = 11.8px
		// h --> 1mm = 3508/297 = 11.8px

    	return new BigDecimal(cm).multiply(new BigDecimal(10)).multiply(new BigDecimal(11.8d)).setScale(0, RoundingMode.HALF_UP);
    }

}
