package com.ess.tudarmstadt.de.mwidgetexample.JSON;

import android.provider.SyncStateContract;
import android.util.Log;

import com.ess.tudarmstadt.de.mwidgetexample.utils.Constants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by lukas on 06.08.15.
 */
public class SurveyItemToJSON {

    public static JSONObject getJSONfromSurvey(SurveyItem surveyItem) throws JSONException {

        JSONObject jsonObj = new JSONObject();
        jsonObj.putOpt(Constants.JSON_OBJECT_SURVEY, 1);
        jsonObj.putOpt(Constants.JSON_OBJECT_SURVEY_TIME, surveyItem.getTime());
        jsonObj.putOpt(Constants.JSON_OBJECT_SURVEY_DATE, surveyItem.getFormattedDate());
        jsonObj.putOpt(Constants.JSON_OBJECT_SURVEY_SURVEY, surveyItem.getSurvey());
        JSONArray jsonArray = new JSONArray();
        for (int i = 0; i < surveyItem.getValues().length; i++) {
            jsonArray.put(surveyItem.getValues()[i]);
        }
        jsonObj.putOpt(Constants.JSON_OBJECT_SURVEY_RESULT, jsonArray);
        return jsonObj;
    }
}
