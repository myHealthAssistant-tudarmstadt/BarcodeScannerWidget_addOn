package com.ess.tudarmstadt.de.mwidgetexample.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.os.Environment;

public class Constants {

	public static final String JSON_OBJECT_ID = "_ID";
	public static final String JSON_OBJECT_TITLE = "OB_TITLE";
	public static final String JSON_OBJECT_DATE = "OB_DATE";
	public static final String JSON_OBJECT_TIME = "OB_TIME";
	public static final String JSON_OBJECT_LOCATION = "OB_LOCATION"; 
	public static final String JSON_OBJECT_LONGITUDE = "OB_LONGITUDE";
	public static final String JSON_OBJECT_LATITUDE = "OB_LATITUDE";
	public static final String JSON_OBJECT_CONTENT = "OB_CONTENT";
	public static final String JSON_OBJECT_URI = "OB_URI";
	
	public static final String JSON_REQUEST_CONTENT_ARRAY ="jArray";
	public static final String JSON_REQUEST_EDIT = "_edit";
	public static final String JSON_REQUEST_DELETE = "_dele";
	

	public static void logDebug(String tag, String text){
		String str = getCurrentDate("dd-MM-yyyy kk:mm ") + tag + ":\n" + text + "\n";
		writeStringToLogFile("BarcodeScannerWidget.log.txt", str);
	}
	

	public static String getCurrentDate(String format) {
		SimpleDateFormat sdfDate = new SimpleDateFormat(format);
		Date now = new Date();
		String strDate = sdfDate.format(now);
		return strDate;
	}

	public static boolean writeStringToLogFile(String outFileName,
			String text) {
		boolean returnCode = false;
		File exportDir = new File(Environment.getExternalStorageDirectory() + "/BarcodeScannerWidget/data/", "");
		File file = new File(exportDir, outFileName);
		
		if (!exportDir.exists()) {
			exportDir.mkdirs();
		}
		
		try {
			if (!file.exists())
				file.createNewFile();
			FileWriter filewriter = new FileWriter(file, true);
			filewriter.append(text);
			filewriter.close();
			returnCode = true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return returnCode;
	}
}
