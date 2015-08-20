package com.ess.tudarmstadt.de.mwidgetexample.JSON;

import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by lukas on 06.08.15.
 */
public class SurveyItem {
    private String formattedDate;
    private String time;
    private int survey;
    private int[] values;
    public SurveyItem(int survey, int[] values) {
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy", java.util.Locale.getDefault());
        formattedDate = df.format(c.getTime());
        SimpleDateFormat sdf = new SimpleDateFormat("HH-mm", java.util.Locale.getDefault());
        time = sdf.format(c.getTime());
        this.survey = survey;
        this.values = new int[values.length];
        this.values = values;
    }

    public String getFormattedDate() {
        return formattedDate;
    }

    public String getTime() {
        return time;
    }

    public int getSurvey() {
        return survey;
    }

    public int[] getValues() {
        return values;
    }
}
