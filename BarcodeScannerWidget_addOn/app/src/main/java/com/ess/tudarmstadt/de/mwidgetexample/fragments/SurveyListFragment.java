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

        Bundle extras = getArguments(); // getting data of entry from the p arent activity for display

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
                    showSurveys(item);
                } else if (mode == 2) {
                    showResults(String.valueOf(textView.getText()), item);
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

    private void showSurveys(String item) {
        ArrayList<String> surveys;
        surveys = dbHelper.getAllSurveys(item);
        arrayAdapter = new ArrayAdapter<>(getActivity().getApplicationContext(), R.layout.custom_listview, surveys);
        listView.setAdapter(arrayAdapter);
    }

    private void showResults(String date, String item) {
        ArrayList<String> questions;
        questions = dbHelper.getAllResults(date, item);
        arrayAdapter = new ArrayAdapter<>(getActivity().getApplicationContext(), R.layout.custom_listview, questions);
        listView.setAdapter(arrayAdapter);
    }
}