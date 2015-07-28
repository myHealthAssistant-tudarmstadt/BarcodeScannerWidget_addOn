package com.ess.tudarmstadt.de.mwidgetexample.fragments;

import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.support.v4.app.Fragment;
import android.widget.TextView;

import com.ess.tudarmstadt.de.mwidgetexample.R;
import com.ess.tudarmstadt.de.mwidgetexample.utils.DBHelper;

import java.util.ArrayList;

/**
 * Created by lukas on 22.06.15.
 */
public class SurveyListFragment extends Fragment {

    ListView listView = null;
    DBHelper dbHelper = null;
    ArrayAdapter<String> arrayAdapter = null;
    // 0 shows date, 1 shows survey, 2 show results.
    int mode = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(
                R.layout.fragment_survey_list, container, false);

        listView = (ListView) rootView.findViewById(R.id.surveyListView);
        final TextView textView = (TextView) rootView.findViewById(R.id.surveyListTextView);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String item = ((TextView) view).getText().toString();
                mode++;
                if (mode == 1) {
                    textView.setText(item);
                    showTimes(item);
                } else if (mode == 2) {
                    String date = textView.getText().toString();
                    textView.setText(date + "/" + item);
                    showSurveys(date, item);
                } else {
                    String[] old = textView.getText().toString().split("/");
                    textView.setText(old[0] + "/" + old[1] + "/" + item);
                    showResults(old[0], old[1].split(" ")[0], item);
                }
            }
        });
        dbHelper = new DBHelper(getActivity().getApplicationContext());
        dbHelper.getReadableDatabase();
        showDates();
        return rootView;
    }

    private void showDates() {
        ArrayList<String> dates;
        dates = dbHelper.getAllDates();
        arrayAdapter = new ArrayAdapter<>(getActivity().getApplicationContext(), R.layout.custom_listview, dates);
        listView.setAdapter(arrayAdapter);
    }

    private void showTimes(String date) {
        ArrayList<String> time;
        time = dbHelper.getAllTimes(date);
        arrayAdapter = new ArrayAdapter<>(getActivity().getApplicationContext(), R.layout.custom_listview, time);
        listView.setAdapter(arrayAdapter);
    }

    private void showSurveys(String date, String time) {
        ArrayList<String> surveys;
        surveys = dbHelper.getAllSurveys(date, time);
        arrayAdapter = new ArrayAdapter<>(getActivity().getApplicationContext(), R.layout.custom_listview, surveys);
        listView.setAdapter(arrayAdapter);
    }

    private void showResults(String date, String time, String item) {
        ArrayList<String> questions;
        questions = dbHelper.getAllResults(date,time, item);
        arrayAdapter = new ArrayAdapter<>(getActivity().getApplicationContext(), R.layout.custom_listview, questions);
        listView.setAdapter(arrayAdapter);
    }
}