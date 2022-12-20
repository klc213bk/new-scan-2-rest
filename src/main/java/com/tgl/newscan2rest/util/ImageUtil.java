package com.tgl.newscan2rest.util;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageOutputStream;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import com.tgl.newscan2rest.Constant;
import com.tgl.newscan2rest.bean.ScannedImage;
import com.tgl.newscan2rest.bean.SignatureImgRule;

//import com.google.zxing.BarcodeFormat;
//import com.google.zxing.BinaryBitmap;
//import com.google.zxing.DecodeHintType;
//import com.google.zxing.LuminanceSource;
//import com.google.zxing.MultiFormatReader;
//import com.google.zxing.NotFoundException;
//import com.google.zxing.Result;
//import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
//import com.google.zxing.common.HybridBinarizer;
//import com.google.zxing.multi.GenericMultipleBarcodeReader;
//import com.tgl.scan.main.Constant;
//import com.tgl.scan.main.bean.ScannedImage;
//import com.tgl.scan.main.bean.SignatureImgRule;
//
//import javafx.embed.swing.SwingFXUtils;
//import javafx.scene.image.Image;

public class ImageUtil {

	private static final Logger logger = LoggerFactory.getLogger(ImageUtil.class);

	public static BufferedImage readBufferedImage(String fileURL) throws FileNotFoundException, IOException {
		if (logger.isDebugEnabled()) {
			logger.debug("readBufferedImage(): fileURL={}", fileURL);
		}

		BufferedImage bufferedImage = null;
		try ( InputStream is = new FileInputStream(fileURL) ) {
			bufferedImage = ImageIO.read(is);
		}
		return bufferedImage;
	}

//	public static Image bufferedImageToFxImage(BufferedImage bufferedImage) {
//		Image image = SwingFXUtils.toFXImage(bufferedImage, null);
//		if (logger.isDebugEnabled()) {
//			String logMsg = "readTiffImageX(): " + image == null ? "image=null" : String.format("image.width=%s, image.height=%s", image.getWidth(), image.getHeight());
//			logger.debug(logMsg);
//		}
//		return image;
//	}
//
//	public static Image createFxImage(String fileURL) throws FileNotFoundException, IOException {
//		BufferedImage bufferedImage = readBufferedImage(fileURL);
//		Image image = bufferedImageToFxImage(bufferedImage);
//		return image;
//	}

	public static void writeTiffFile(BufferedImage image, String fileURL) throws IOException {
        try (
        	ImageOutputStream ios = ImageIO.createImageOutputStream(new File(fileURL))
        ) {
            ImageTypeSpecifier imageTypeSpecifier = ImageTypeSpecifier.createFromRenderedImage(image);
            ImageWriter writer = ImageIO.getImageWriters(imageTypeSpecifier, "TIFF").next();
            writer.setOutput(ios);

            ImageWriteParam param = writer.getDefaultWriteParam();
            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            param.setCompressionType("CCITT T.6");
            param.setCompressionQuality(0.5f);

            IIOMetadata metadata = writer.getDefaultImageMetadata(imageTypeSpecifier, param);
            String formatName = metadata.getNativeMetadataFormatName();
            Element tree = (Element) metadata.getAsTree(formatName);
            if (logger.isDebugEnabled()) {
                String treeStr = elementToString(tree);
                logger.debug("formatName:{}, Tree node:\r\n{}", formatName, treeStr);
            }

            Element ifd = (Element) tree.getElementsByTagName("TIFFIFD").item(0);
            ifd.appendChild(createTiffResolutionNode("282", "XResolution", "300/1"));
            ifd.appendChild(createTiffResolutionNode("283", "YResolution", "300/1"));
            ifd.appendChild(createTiffResolutionUnitNode("296", "ResolutionUnit", "2"));
            metadata.mergeTree(formatName, tree);

            if (logger.isDebugEnabled()) {
    			logger.debug("Reset ifd attributes...");
                String treeStr = elementToString(tree);
                logger.debug("Tree node:\r\n{}", treeStr);
    			logger.debug("Write tiff image to " + fileURL);
    		}

    		writer.write(metadata, new IIOImage(image, null, metadata), param);
        }
	}

	private static IIOMetadataNode createTiffResolutionNode(String tagNumber, String tagName, String tagValue) {
	    IIOMetadataNode res = new IIOMetadataNode("TIFFField");
	    res.setAttribute("number", tagNumber);
	    res.setAttribute("name", tagName); // Tag name is optional

	    IIOMetadataNode value = new IIOMetadataNode("TIFFRational");
	    value.setAttribute("value", tagValue);

	    IIOMetadataNode rationals = new IIOMetadataNode("TIFFRationals");
	    rationals.appendChild(value);
	    res.appendChild(rationals);

	    return res;
	}

	private static IIOMetadataNode createTiffResolutionUnitNode(String tagNumber, String tagName, String tagValue) {
	    IIOMetadataNode res = new IIOMetadataNode("TIFFField");
	    res.setAttribute("number", tagNumber);
	    res.setAttribute("name", tagName); // Tag name is optional

	    IIOMetadataNode value = new IIOMetadataNode("TIFFShort");
	    value.setAttribute("value", tagValue);
	    value.setAttribute("description", "None");

	    IIOMetadataNode rationals = new IIOMetadataNode("TIFFShorts");
	    rationals.appendChild(value);
	    res.appendChild(rationals);

	    return res;
	}

	private static String elementToString(Element element) {
		String str = null;
		try {
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(element);
			StreamResult result = new StreamResult(new StringWriter());
			transformer.transform(source, result);
			str = result.getWriter().toString();
		} catch (TransformerConfigurationException e) {
		} catch (TransformerException e) {
		}

		return str;
	}

	public static void writeJpgFile(BufferedImage image, String fileURL) throws IOException {
        try (
        	ImageOutputStream ios = ImageIO.createImageOutputStream(new File(fileURL))
        ) {
        	ImageWriter writer = ImageIO.getImageWritersByFormatName("jpg").next();
        	writer.setOutput(ios);

        	ImageWriteParam param = writer.getDefaultWriteParam();
            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            param.setCompressionType("JPEG");
            param.setCompressionQuality(0.75f);

            ImageTypeSpecifier typeSpecifier = ImageTypeSpecifier.createFromBufferedImageType(BufferedImage.TYPE_INT_RGB);
            IIOMetadata metadata = writer.getDefaultImageMetadata(typeSpecifier, param);
            String formatName = metadata.getNativeMetadataFormatName();
            Element tree = (Element) metadata.getAsTree(formatName);
            if (logger.isDebugEnabled()) {
                String treeStr = elementToString(tree);
                logger.debug("formatName:{}, Tree node:\r\n{}", formatName, treeStr);
            }

            Element jfif = (Element) tree.getElementsByTagName("app0JFIF").item(0);
            jfif.setAttribute("Xdensity", "300");
            jfif.setAttribute("Ydensity", "300");
            jfif.setAttribute("resUnits", "1");
            metadata.setFromTree(formatName, tree);

            if (logger.isDebugEnabled()) {
    			logger.debug("Reset jfif attributes...");
                String treeStr = elementToString(tree);
                logger.debug("Tree node:\r\n{}", treeStr);
    			logger.debug("Write jpg image to " + fileURL);
    		}

    		writer.write(metadata, new IIOImage(image, null, metadata), param);
        }
	}

	public static BufferedImage rotateAndWriteImage(ScannedImage simage, int rotate) throws IOException {
		String fileURL = simage.fileURLProperty().getValue();
		String imageFormat = simage.imageFormatProperty().getValue();

		if (logger.isDebugEnabled()) {
			logger.debug("writeRotateImage(): fileURL={}, imageFormat={}, rotate={}", fileURL, imageFormat, rotate);
		}

		BufferedImage buffImage = readBufferedImage(fileURL);
        BufferedImage rotateBuffImage = rotateImage(buffImage, rotate);

        if ( Constant.FILE_TYPE_JPG.equals(imageFormat) ) {
        	writeJpgFile(rotateBuffImage, fileURL);
        } else {
            writeTiffFile(rotateBuffImage, fileURL);
        }

        return rotateBuffImage;
	}

    public static BufferedImage rotateImage(BufferedImage bufferedimage, int angle) {
		int width = bufferedimage.getWidth();
		int height = bufferedimage.getHeight();

		BufferedImage dstImage = null;
		AffineTransform affineTransform = new AffineTransform();

		if (angle == 180) {
			affineTransform.translate(width, height);
			dstImage = new BufferedImage(width, height, bufferedimage.getType());
		} else if (angle == 90) {
			affineTransform.translate(height, 0);
			dstImage = new BufferedImage(height, width, bufferedimage.getType());
		} else if (angle == 270) {
			affineTransform.translate(0, width);
			dstImage = new BufferedImage(height, width, bufferedimage.getType());
		}

		affineTransform.rotate(java.lang.Math.toRadians(angle));
		AffineTransformOp affineTransformOp = new AffineTransformOp(affineTransform,
				AffineTransformOp.TYPE_NEAREST_NEIGHBOR);

		return affineTransformOp.filter(bufferedimage, dstImage);
	}

	public static Path copyImageItem(ScannedImage copyItem, String newFileURL) throws IOException {
		// 複製實體影像檔案
		String fileURL = copyItem.fileURLProperty().getValue();
		Path file = Paths.get(fileURL);
		Path newFile = Paths.get(newFileURL);
        CopyOption[] options = new CopyOption[] {
                StandardCopyOption.REPLACE_EXISTING,
                StandardCopyOption.COPY_ATTRIBUTES
	        };
		if (logger.isDebugEnabled()) {
			logger.debug("Copy image to {}", newFileURL);
		}
		return Files.copy(file, newFile, options);
	}

	public static void copyImageFile(File srcFile, String destFile) throws IOException {
		if (logger.isDebugEnabled()) {
			logger.debug("Copy image to {}", destFile);
		}
		Path dest = Paths.get(destFile);
	    Path src = srcFile.toPath();
		Files.copy(src, dest, StandardCopyOption.REPLACE_EXISTING);
	}

	/*
	public static void cropImageToFile(BufferedImage srcImage, SignatureImgRule imageRule, String destFile) throws NumberFormatException, IOException {
		BufferedImage destImage = cropImage(srcImage, imageRule);
		if (logger.isDebugEnabled()) {
			logger.debug("Save image to {}", destFile);
		}
		writeTiffFile(destImage, destFile);
	}
	*/

	public static BufferedImage cropImage(BufferedImage srcImage, SignatureImgRule imageRule) throws NumberFormatException {
		int x = BigDecimalUntils.cmToPixel(imageRule.getPosx()).intValue();
		int y = BigDecimalUntils.cmToPixel(imageRule.getPosy()).intValue();
		int w = BigDecimalUntils.cmToPixel(imageRule.getWidth()).intValue();
		int h = BigDecimalUntils.cmToPixel(imageRule.getLength()).intValue();

		if (logger.isDebugEnabled()) {
			logger.debug("Crop image, x={}, y={}, w={}, h={}", x, y, w, h);
		}

		BufferedImage destImage = new BufferedImage(w, h, srcImage.getType());
		Graphics2D g = destImage.createGraphics();
		g.drawImage(srcImage, 0, 0, w, h, x, y, x+w, y+h, null);
		g.dispose();

		return destImage;
	}

//	public static String decodeBarcode(BufferedImage image) {
//		if (logger.isDebugEnabled()) {
//			logger.debug("image is {}", image==null ? "null" : "not null");
//		}
//
//		String barcodeString = "";
//		Result[] zResults = null;
//
//		if (null != image) {
//			try {
//				MultiFormatReader multiFormatReader = new MultiFormatReader();
//				GenericMultipleBarcodeReader multiBarcodeReader = new GenericMultipleBarcodeReader(multiFormatReader);
//				LuminanceSource source = new BufferedImageLuminanceSource(image);
//				BinaryBitmap bBitmap = new BinaryBitmap(new HybridBinarizer(source));
//				Map<DecodeHintType, Object> hints = new LinkedHashMap<DecodeHintType, Object>();
//				//hints.put(DecodeHintType.CHARACTER_SET, "utf-8");
//				// 優化精度
//				hints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
//				// 設定Barcode格式
//				hints.put(DecodeHintType.POSSIBLE_FORMATS, Arrays.asList(BarcodeFormat.CODE_39));
//				// 複雜模式，開啟PURE_BARCODE模式
//				hints.put(DecodeHintType.PURE_BARCODE, Boolean.TRUE);
//
//				//會識別多筆 Barcode
//				zResults = multiBarcodeReader.decodeMultiple(bBitmap, hints);
//			} catch (NotFoundException e) {
//				if (logger.isDebugEnabled()) {
//					logger.debug("Barcode not found.");
//				}
//			}
//		}
//
//		if (null != zResults) {
//			for (int j = 0; j < zResults.length; j++) {
//				if (null != zResults[j]) {
//					BarcodeFormat barcodeFormat = zResults[j].getBarcodeFormat();
//					String barcodeText = zResults[j].getText();
//					if (logger.isDebugEnabled()) {
//						logger.debug("Barcode format={}, text={}", barcodeFormat.toString(), barcodeText);
//					}
//					if (barcodeFormat == null || !BarcodeFormat.CODE_39.equals(barcodeFormat)) {
//						if (logger.isDebugEnabled()) {
//							logger.debug("Incorrect barcode format -> {}", barcodeFormat.toString());
//						}
//					} else if (ObjectsUtil.isEmpty(barcodeText) || barcodeText.length() < 6 || barcodeText.length() > 15) {
//						if (logger.isDebugEnabled()) {
//							logger.debug("Invalid Length -> {}", barcodeText);
//						}
//					} else if (barcodeText.length() >= 3 && barcodeText.startsWith("OBJ") ) {
//						if (logger.isDebugEnabled()) {
//							logger.debug("Ignore Old Barcode -> {}", barcodeText);
//						}
//					} else if (barcodeText.length() == 9 && (barcodeText.startsWith("UNB") || barcodeText.startsWith("CLM")|| barcodeText.startsWith("POS")|| barcodeText.startsWith("GID"))) {
//						if (barcodeString.indexOf(barcodeText)==-1) {
//							// 文件類別放前面
//							barcodeString = barcodeText + (barcodeString.length()>0 ? Constant.BARCODE_SEPARATOR + barcodeString : "");
//						}
//					} else {
//						if (barcodeString.indexOf(barcodeText)==-1) {
//							// 文件編號放後面
//							barcodeString = (barcodeString.length()>0 ? barcodeString + Constant.BARCODE_SEPARATOR : "") + barcodeText;
//						}
//					}
//				}
//			}
//		}
//
//		if (logger.isDebugEnabled()) {
//			logger.debug("barcodeString = {}", barcodeString);
//		}
//
//		return barcodeString;
//	}

}
