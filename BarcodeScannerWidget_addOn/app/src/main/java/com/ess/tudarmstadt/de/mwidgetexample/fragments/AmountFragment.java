package com.ess.tudarmstadt.de.mwidgetexample.fragments;

import android.app.Activity;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;

import com.ess.tudarmstadt.de.mwidgetexample.MainActivity;
import com.ess.tudarmstadt.de.mwidgetexample.R;
import com.ess.tudarmstadt.de.mwidgetexample.utils.Constants;
import com.ess.tudarmstadt.de.mwidgetexample.utils.MyLocation;

import org.json.JSONObject;

import java.util.List;
import java.util.Locale;


public class AmountFragment extends Fragment {

    private double latitude, longitude;
    private String address;
    private MyLocation location;
    private String pop_content = "";
    private String pop_uri = "";
    private NumberPicker numberPicker;

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
        void onAmountCallbackListener(JSONObject jObj);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setRetainInstance(true);
        super.onCreate(savedInstanceState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Bundle extras = getArguments(); // getting data of entry from the p arent activity for display
        if (extras != null) {
            pop_content = extras.getString(Constants.JSON_OBJECT_CONTENT, "");
            pop_uri = extras.getString(Constants.JSON_OBJECT_URI, "");
        }

        View rootView = inflater.inflate(
                R.layout.fragment_amount, container, false);

        numberPicker = (NumberPicker) rootView.findViewById(R.id.numberPicker);
        numberPicker.setEnabled(true);
        String[] values=new String[9];
        for(int i=0; i<values.length; i++) {
            StringBuilder sb = new StringBuilder();
            sb.append(Integer.toString(i*250));
            sb.append("ml");
            values[i]=sb.toString();
        }
        numberPicker.setMaxValue(values.length - 1);
        numberPicker.setMinValue(0);
        numberPicker.setDisplayedValues(values);
        numberPicker.setValue(1);

        final EditText editText = (EditText) rootView.findViewById(R.id.name);

        location = new MyLocation();
        location.getLocation(this.getActivity().getApplicationContext(), locationResult);

        Button onSave = (Button) rootView.findViewById(R.id.scan_save_btn);
        onSave.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                String title = editText.getText().toString();
                saveAndDismiss(title);
            }
        });
        return rootView;
    }

    private MyLocation.LocationResult locationResult = new MyLocation.LocationResult() {
        @Override
        public void gotLocation(Location location) {
            if (location != null) {
                latitude = location.getLatitude();
                longitude = location.getLongitude();
            } else {
                latitude = -1;
                longitude = -1;
            }
        }
    };

    private void saveAndDismiss(String title) {
        String obj_date = MainActivity.getTimestamp("dd-MM-yyyy");
        String obj_time = MainActivity.getTimestamp("kk:mm:ss");
        address = getAddress(latitude, longitude);
        int amount = numberPicker.getValue();
        JSONObject key = new JSONObject();
        try {
            key.putOpt(Constants.JSON_OBJECT_ID, -1);
            key.putOpt(Constants.JSON_OBJECT_TITLE, title);
            key.putOpt(Constants.JSON_OBJECT_CONTENT, pop_content);
            key.putOpt(Constants.JSON_OBJECT_DATE, obj_date);
            key.putOpt(Constants.JSON_OBJECT_TIME, obj_time);
            key.putOpt(Constants.JSON_OBJECT_LONGITUDE, longitude);
            key.putOpt(Constants.JSON_OBJECT_LATITUDE, latitude);
            key.putOpt(Constants.JSON_OBJECT_LOCATION, address);
            key.putOpt(Constants.JSON_OBJECT_URI, pop_uri);
            key.putOpt(Constants.JSON_OBJECT_AMOUNT, amount);

        } catch (org.json.JSONException e) {
            Constants.logDebug("error", e.getMessage());
            e.printStackTrace();
        }

        // back to parent activity
        mCallback.onAmountCallbackListener(key);
    }

    /**
     * get local address base on longitude and latitude *
     */
    private String getAddress(double latitude, double longitude) {
        StringBuilder result = new StringBuilder("");
        try {
            Geocoder geocoder = new Geocoder(this.getActivity(),
                    Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(latitude,
                    longitude, 4);
            if (addresses != null) {
                Address address = addresses.get(0);
                result.append(address.getSubLocality());
                result.append(", ");
                result.append(address.getLocality());

            }
        } catch (Exception e) {
            Log.e("tag", e.getMessage());
        }

        return result.toString();
    }

}
