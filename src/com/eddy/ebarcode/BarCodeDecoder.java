package com.eddy.ebarcode;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import android.graphics.Bitmap;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;

/**
 * 条码解析
 *
 */
public class BarCodeDecoder {
	
	/**
	 * key, value<br/>
	 * result, true/false<br/>
	 * info, 信息(true时为条码内容false)
	 * @param bitmap
	 * @return
	 */
	public Map<String, Object> decode(Bitmap bitmap) {
		Map<String, Object> retMap = new HashMap<String, Object>();
		retMap.put("result", false);
		retMap.put("info", "FFFFFFFFFF");
		int width = bitmap.getWidth();
	    int height = bitmap.getHeight();
	    int[] pixels = new int[width * height];
	    bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
	    RGBLuminanceSource source = new RGBLuminanceSource(width, height, pixels);
	    
	    try {
	    	Map<DecodeHintType, Object> hints = new HashMap<DecodeHintType, Object>();  
            hints.put(DecodeHintType.CHARACTER_SET, "utf-8");  
	    	BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(source));
		    MultiFormatReader multiFormatReader = new MultiFormatReader();
		    Result result = multiFormatReader.decode(binaryBitmap, hints);
		    BarcodeFormat format = result.getBarcodeFormat();
		    String text = result.getText();
		    retMap.put("result", true);
		    retMap.put("info", "条码格式: " + format + "; 内容：" + text);
	    }
	    catch(Exception e) {
	    	retMap.put("result", false);
			StringWriter writer = new StringWriter();
			e.printStackTrace(new PrintWriter(writer, true));
			retMap.put("info", writer.toString());
	    	e.printStackTrace();
	    }
		return retMap;
	}
	
}
