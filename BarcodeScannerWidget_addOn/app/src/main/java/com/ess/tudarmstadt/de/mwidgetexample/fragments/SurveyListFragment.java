package com.ess.tudarmstadt.de.mwidgetexample.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.app.Fragment;
import android.widget.TextView;

import com.ess.tudarmstadt.de.mwidgetexample.R;
import com.ess.tudarmstadt.de.mwidgetexample.utils.Constants;

import java.util.ArrayList;

/**
 * Created by lukas on 22.06.15.
 */
public class SurveyListFragment extends Fragment {
    private int[] results;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setRetainInstance(true);
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // super.onCreate(savedInstanceState);
        final View rootView = inflater.inflate(
                R.layout.fragment_survey_list, container, false);

        Bundle extras = getArguments(); // getting data of entry from the p arent activity for display
        if (extras != null) {
            results = extras.getIntArray(Constants.JSON_OBJECT_SURVEY_RESULT);
        }

        TextView text = (TextView) rootView.findViewById(R.id.textViewSurvey);

        for (int i = 0; i < results.length; i++) {
            text.append("Frage " + (i+1) + ": ");
            text.append(String.valueOf(results[i]));
            text.append("\n");
        }

        return rootView;
    }
}