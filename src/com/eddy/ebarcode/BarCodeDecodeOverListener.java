package com.eddy.ebarcode;

import java.util.Map;

/**
 * 解码完毕
 *
 */
public interface BarCodeDecodeOverListener {

	public void deCodeOver(Map<String, Object> decodeMap); 
}
