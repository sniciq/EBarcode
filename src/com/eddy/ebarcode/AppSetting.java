package com.eddy.ebarcode;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;

import android.content.Context;
import android.os.Environment;

public class AppSetting {
	
	public static final String PictureSize = "PictureSize";
	public static final String TakePicPeriod = "TakePicPeriod";
	
	private Properties prop = new Properties();
	private Context context;
	public AppSetting(Context context) {
		this.context = context;
	}
	
	public void init() {
		try {
			String filePath = Environment.getExternalStorageDirectory().getPath() + "/" + context.getResources().getString(R.string.dir_name) + "/setting.properties";
			File f = new File(filePath);
			if(f.exists()) {
				prop.load(new FileInputStream(filePath));
			}
			else {
				if(!f.getParentFile().exists()) {
					f.getParentFile().mkdirs();
				}
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public int getTakePicPeriod() {
		String per = (String) prop.get("TakePicPeriod");
		if(per != null && !per.trim().equals("")) {
			return Integer.parseInt(per);
		}
		else {
			return 3;
		}
	}
	
	public int[] getConfigPictureSize() {
		int[] sizeArr = new int[2];
		String size = (String) prop.get("PictureSize");
		if(size != null && !size.trim().equals("")) {
			String[] ss = size.split("X");
			sizeArr[0] = Integer.parseInt(ss[0]);
			sizeArr[1] = Integer.parseInt(ss[1]);
		}
		else {
			sizeArr[0] = 320;
			sizeArr[1] = 240;
		}
		return sizeArr;
	}

	public void save(String picSize, int pre) {
		try {
			prop.put(PictureSize, picSize);
			prop.put(TakePicPeriod, pre + "");
			String filePath = Environment.getExternalStorageDirectory().getPath() + "/" + context.getResources().getString(R.string.dir_name) + "/setting.properties";
			
			prop.store(new FileOutputStream(new File(filePath)), "");
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
}
