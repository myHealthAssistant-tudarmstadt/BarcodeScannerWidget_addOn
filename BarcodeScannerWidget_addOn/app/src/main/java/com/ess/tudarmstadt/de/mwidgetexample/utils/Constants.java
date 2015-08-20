package com.ess.tudarmstadt.de.mwidgetexample.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.os.Environment;

public class Constants {

	// Object.
	public static final String JSON_OBJECT_ID = "_ID";
	public static final String JSON_OBJECT_TITLE = "OB_TITLE";
	public static final String JSON_OBJECT_DATE = "OB_DATE";
	public static final String JSON_OBJECT_TIME = "OB_TIME";
	public static final String JSON_OBJECT_LOCATION = "OB_LOCATION"; 
	public static final String JSON_OBJECT_LONGITUDE = "OB_LONGITUDE";
	public static final String JSON_OBJECT_LATITUDE = "OB_LATITUDE";
	public static final String JSON_OBJECT_CONTENT = "OB_CONTENT";
	public static final String JSON_OBJECT_URI = "OB_URI";
	public static final String JSON_OBJECT_AMOUNT = "OB_AMOUNT";
	public static final String JSON_REQUEST_CONTENT_ARRAY ="jArray";
	public static final String JSON_REQUEST_EDIT = "_edit";
	public static final String JSON_REQUEST_DELETE = "_dele";

	// Survey.
	public static final String JSON_OBJECT_SURVEY_DATE = "OB_SURVEY_DATE";
	public static final String JSON_OBJECT_SURVEY_TIME = "OB_SURVEY_TIME";
	public static final String JSON_OBJECT_SURVEY_SURVEY = "OB_SURVEY_SURVEY";
	public static final String JSON_OBJECT_SURVEY_RESULT = "OB_SURVEY_RESULT";
	public static final String JSON_OBJECT_SURVEY = "IS_SURVEY";

	// Barcode.
	public static final String JSON_OBJECT_BARCODE_BARCODE = "OB_BARCODE_BARCODE";
	public static final String JSON_OBJECT_BARCODE_NAME = "OB_BARCODE_NAME";
	public static final String JSON_OBJECT_BARCODE = "IS_BARCODE";


	public static final String Survey_Intent = "OPEN_NEW_SURVEY";

	public static void logDebug(String tag, String text){
		String str = getCurrentDate() + tag + ":\n" + text + "\n";
		writeStringToLogFile(str);
	}
	

	private static String getCurrentDate() {
		SimpleDateFormat sdfDate = new SimpleDateFormat("dd-MM-yyyy kk:mm ", java.util.Locale.getDefault());
		Date now = new Date();
		return sdfDate.format(now);
	}

	private static void writeStringToLogFile(String text) {
		File exportDir = new File(Environment.getExternalStorageDirectory() + "/BarcodeScannerWidget/data/", "");
		File file = new File(exportDir, "BarcodeScannerWidget.log.txt");
		
		if (!exportDir.exists()) {
			exportDir.mkdirs();
		}
		
		try {
			if (!file.exists())
				file.createNewFile();
			FileWriter filewriter = new FileWriter(file, true);
			filewriter.append(text);
			filewriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
