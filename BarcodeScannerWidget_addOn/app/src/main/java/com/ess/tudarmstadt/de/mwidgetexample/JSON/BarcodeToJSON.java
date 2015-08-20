package com.ess.tudarmstadt.de.mwidgetexample.JSON;

import com.ess.tudarmstadt.de.mwidgetexample.utils.Constants;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by lukas on 12.08.15.
 */
public class BarcodeToJSON {

    public static JSONObject getJSONfromBarcode(Barcode barcode) throws JSONException {

        JSONObject jsonObj = new JSONObject();
        jsonObj.putOpt(Constants.JSON_OBJECT_BARCODE, 1);
        jsonObj.putOpt(Constants.JSON_OBJECT_BARCODE_BARCODE, barcode.getBarcode());
        jsonObj.putOpt(Constants.JSON_OBJECT_BARCODE_NAME, barcode.getName());
        return jsonObj;
    }
}
