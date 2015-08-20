package com.ess.tudarmstadt.de.mwidgetexample.JSON;

import com.ess.tudarmstadt.de.mwidgetexample.utils.Constants;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by lukas on 06.08.15.
 */
public class BarcodeItemToJSON {

    public static JSONObject getJSONfromBarcode(BarcodeItem barcodeItem) throws JSONException {

        JSONObject jsonObj = new JSONObject();
        jsonObj.putOpt(Constants.JSON_OBJECT_ID, -1);
        jsonObj.putOpt(Constants.JSON_OBJECT_TITLE, barcodeItem.getTitle());
        jsonObj.putOpt(Constants.JSON_OBJECT_CONTENT, barcodeItem.getPop_content());
        jsonObj.putOpt(Constants.JSON_OBJECT_DATE, barcodeItem.getObj_date());
        jsonObj.putOpt(Constants.JSON_OBJECT_TIME, barcodeItem.getObj_time());
        jsonObj.putOpt(Constants.JSON_OBJECT_LONGITUDE, barcodeItem.getLongitude());
        jsonObj.putOpt(Constants.JSON_OBJECT_LATITUDE, barcodeItem.getLatitude());
        jsonObj.putOpt(Constants.JSON_OBJECT_LOCATION, barcodeItem.getAddress());
        jsonObj.putOpt(Constants.JSON_OBJECT_URI, barcodeItem.getPop_uri());
        jsonObj.putOpt(Constants.JSON_OBJECT_AMOUNT, barcodeItem.getAmount());
        return jsonObj;
    }
}
