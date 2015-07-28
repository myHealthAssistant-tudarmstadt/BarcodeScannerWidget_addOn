package com.ess.tudarmstadt.de.mwidgetexample.fragments;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import com.ess.tudarmstadt.de.mwidgetexample.R;
import com.ess.tudarmstadt.de.mwidgetexample.utils.Constants;

import org.json.JSONObject;

/**
 * Created by lukas on 05.06.15.
 */
public class SurveyFragment extends Fragment {
    private TextView leftText;
    private TextView rightText;
    private TextView question;
    private RadioGroup decicion;
    private RadioGroup.OnCheckedChangeListener listener;
    private int currentValue = 0;
    private int surveyNumber;
    private int questionNumber = 0;
    private String[] question1;
    private String[] question2;
    private String[] question3;
    private String[] question4;
    private CharSequence[] question5;
    private String[] question6Left;
    private String[] question6Right;
    private int[] answer;

    private HandleCallbackListener mCallback;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mCallback = (HandleCallbackListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement HandleCallbackListener");
        }
    }

    public interface HandleCallbackListener {
        void onSurveyCallbackListener(int survey, int[] values);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setRetainInstance(true);
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Resources res = getResources();
        question1 = res.getStringArray(R.array.survey1);
        question2 = res.getStringArray(R.array.survey2);
        question3 = res.getStringArray(R.array.survey3);
        question4 = res.getStringArray(R.array.survey4);
        question5 = res.getTextArray(R.array.survey5);
        question6Left = res.getStringArray(R.array.survey6left);
        question6Right = res.getStringArray(R.array.survey6right);

        final View rootView = inflater.inflate(
                R.layout.fragment_survey, container, false);
        int time = this.getArguments().getInt("time", -1);

        question = (TextView) rootView.findViewById(R.id.question);
        decicion = (RadioGroup) rootView.findViewById(R.id.radioGroup);
        RadioButton radioButton1 = (RadioButton) rootView.findViewById(R.id.radioButton1);
        RadioButton radioButton2 = (RadioButton) rootView.findViewById(R.id.radioButton2);
        RadioButton radioButton3 = (RadioButton) rootView.findViewById(R.id.radioButton3);
        RadioButton radioButton4 = (RadioButton) rootView.findViewById(R.id.radioButton4);
        RadioButton radioButton5 = (RadioButton) rootView.findViewById(R.id.radioButton5);
        RadioButton radioButton6 = (RadioButton) rootView.findViewById(R.id.radioButton6);
        RadioButton radioButton7 = (RadioButton) rootView.findViewById(R.id.radioButton7);

        final NumberPicker minutePicker = (NumberPicker) rootView.findViewById(R.id.minutePicker);
        minutePicker.setVisibility(View.GONE);
        Button next = (Button) rootView.findViewById(R.id.nextQuestionButton);
        next.setVisibility(View.GONE);

        final SeekBar seekBar = (SeekBar) rootView.findViewById(R.id.seekBar);
        seekBar.setVisibility(View.GONE);

        leftText = (TextView) rootView.findViewById(R.id.leftText);
        leftText.setVisibility(View.GONE);

        rightText = (TextView) rootView.findViewById(R.id.rightText);
        rightText.setVisibility(View.GONE);

        listener = new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.radioButton1:
                        currentValue = 1;
                        break;
                    case R.id.radioButton2:
                        currentValue = 2;
                        break;
                    case R.id.radioButton3:
                        currentValue = 3;
                        break;
                    case R.id.radioButton4:
                        currentValue = 4;
                        break;
                    case R.id.radioButton5:
                        currentValue = 5;
                        break;
                    case R.id.radioButton6:
                        currentValue = 6;
                        break;
                    case R.id.radioButton7:
                        currentValue = 7;
                        break;
                }
                newQuestion();
            }
        };

        surveyNumber = time;

        Button.OnClickListener buttonListener = new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (surveyNumber == 4) {
                    currentValue = minutePicker.getValue();
                } else if (surveyNumber == 5) {
                    currentValue = seekBar.getProgress();
                }
                seekBar.setProgress(3);
                newQuestion();
            }
        };
        next.setOnClickListener(buttonListener);

        decicion.setOnCheckedChangeListener(listener);
        switch (surveyNumber) {
            case 0:
                radioButton1.setText("1 (Völlig unzutreffend)");
                radioButton7.setText("7 (Völlig zutreffend)");
                break;
            case 1:
                radioButton1.setText("1 (Stimme gar nicht zu)");
                radioButton5.setText("5 (Stimme stark zu)");
                radioButton6.setVisibility(View.INVISIBLE);
                radioButton7.setVisibility(View.INVISIBLE);
                break;
            case 2:
                radioButton1.setText("1 (gar nicht)");
                radioButton5.setText("5 (sehr)");
                radioButton6.setVisibility(View.INVISIBLE);
                radioButton7.setVisibility(View.INVISIBLE);
                break;
            case 3:
                radioButton1.setText("1 (nein)");
                radioButton2.setText("2 (ja)");
                radioButton3.setVisibility(View.INVISIBLE);
                radioButton4.setVisibility(View.INVISIBLE);
                radioButton5.setVisibility(View.INVISIBLE);
                radioButton6.setVisibility(View.INVISIBLE);
                radioButton7.setVisibility(View.INVISIBLE);
                break;
            case 4:
                decicion.setVisibility(View.GONE);
                minutePicker.setVisibility(View.VISIBLE);
                minutePicker.setEnabled(true);
                String[] values=new String[20];
                for(int i=0; i<values.length; i++) {
                    StringBuilder sb = new StringBuilder();
                    sb.append(Integer.toString(i*10));
                    sb.append("min");
                    values[i] = sb.toString();
                }
                minutePicker.setMaxValue(values.length - 1);
                minutePicker.setMinValue(0);
                minutePicker.setDisplayedValues(values);
                next.setVisibility(View.VISIBLE);
                break;
            case 5:
                decicion.setVisibility(View.GONE);
                leftText.setVisibility(View.VISIBLE);
                rightText.setVisibility(View.VISIBLE);
                seekBar.setVisibility(View.VISIBLE);
                next.setVisibility(View.VISIBLE);
                seekBar.setMax(6);
                seekBar.setProgress(3);
                question.setText("In diesem Moment fühle ich mich...");
        }
        newQuestion();
        return rootView;
    }

    private void newQuestion() {
        switch (surveyNumber) {
            case 0:
                if (questionNumber == 0) {
                    answer = new int[question1.length];
                }
                if ((0 < questionNumber) && (questionNumber < question1.length + 1)) {
                    answer[questionNumber -1] = currentValue;
                }
                if (questionNumber > answer.length - 1) {
                    saveAndDissmiss();
                    break;
                } else {
                    question.setText(question1[questionNumber]);
                    break;
                }
            case 1:
                if (questionNumber == 0) {
                    answer = new int[question2.length];
                }
                if ((0 < questionNumber) && (questionNumber < question2.length + 1)) {
                    answer[questionNumber -1] = currentValue;
                }
                if (questionNumber > answer.length -1) {
                    saveAndDissmiss();
                    break;
                } else {
                    question.setText(question2[questionNumber]);
                    break;
                }
            case 2:
                if (questionNumber == 0) {
                    answer = new int[question3.length];
                }
                if ((0 < questionNumber) && (questionNumber < question3.length + 1)) {
                    answer[questionNumber -1] = currentValue;
                }
                if (questionNumber > answer.length -1) {
                    saveAndDissmiss();
                    break;
                } else {
                    question.setText(question3[questionNumber]);
                    break;
                }
            case 3:
                if (questionNumber == 0) {
                    answer = new int[question4.length];
                }
                if ((0 < questionNumber) && (questionNumber < question4.length + 1)) {
                    answer[questionNumber -1] = currentValue;
                }
                if (questionNumber > answer.length -1) {
                    saveAndDissmiss();
                    break;
                } else {
                    question.setText(question4[questionNumber]);
                    break;
                }
            case 4:
                if (questionNumber == 0) {
                    answer = new int[question5.length];
                }
                if ((0 < questionNumber) && (questionNumber < question5.length + 1)) {
                    answer[questionNumber -1] = currentValue;
                }
                if (questionNumber > answer.length -1) {
                    saveAndDissmiss();
                    break;
                } else {
                    question.setText(question5[questionNumber]);
                    break;
                }
            case 5:
                if (questionNumber == 0) {
                    answer = new int[question6Right.length];
                }
                if ((0 < questionNumber) && (questionNumber < question6Right.length + 1)) {
                    answer[questionNumber -1] = currentValue;
                }
                if (questionNumber > answer.length -1) {
                    saveAndDissmiss();
                    break;
                } else {
                    rightText.setText(question6Right[questionNumber]);
                    leftText.setText(question6Left[questionNumber]);
                    break;
                }
        }
        decicion.setOnCheckedChangeListener(null);
        decicion.clearCheck();
        decicion.setOnCheckedChangeListener(listener);
        questionNumber++;
    }

    private void saveAndDissmiss() {
        mCallback.onSurveyCallbackListener(surveyNumber + 1,answer);
    }
}
