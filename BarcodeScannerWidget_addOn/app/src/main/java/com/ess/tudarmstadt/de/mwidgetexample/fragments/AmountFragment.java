package com.ess.tudarmstadt.de.mwidgetexample.fragments;

import android.app.Activity;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.Toast;

import com.ess.tudarmstadt.de.mwidgetexample.MainActivity;
import com.ess.tudarmstadt.de.mwidgetexample.R;
import com.ess.tudarmstadt.de.mwidgetexample.utils.Constants;
import com.ess.tudarmstadt.de.mwidgetexample.utils.MyLocation;
import com.ess.tudarmstadt.de.mwidgetexample.utils.DBHelper;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Locale;


public class AmountFragment extends Fragment {

    private double latitude, longitude;
    private String pop_content = "";
    private String pop_uri = "";
    private NumberPicker numberPicker;

    private HandleCallbackListener mCallback;
    private EditText editText;
    private EditText editTextAmount;


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

        editTextAmount = (EditText) rootView.findViewById(R.id.edit_amount);

        editText = (EditText) rootView.findViewById(R.id.name);

        MyLocation location = new MyLocation();
        location.getLocation(this.getActivity().getApplicationContext(), locationResult);

        Button onSave = (Button) rootView.findViewById(R.id.scan_save_btn);
        onSave.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                String amount = editTextAmount.getText().toString();
                if (TextUtils.isEmpty(amount)) {
                    editTextAmount.setError("Menge eingeben!");
                    return;
                }
                String title = editText.getText().toString();
                if (TextUtils.isEmpty(title)) {
                    editText.setError("Name eingeben!");
                    return;
                }
                saveAndDismiss(title);
            }
        });

        String title = MainActivity.mydb.getTitle(pop_content);
        if (!title.equals("")) {
            editText.setText(title);
        } else if (isNetworkAvailable()) {
            new MyTask().execute();
        }
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
        String address = getAddress(latitude, longitude);
        int amount = Integer.parseInt(editTextAmount.getText().toString());
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

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getActivity().getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

	private class MyTask extends AsyncTask<String, String, String> {
		public MyTask() {
		}
		@Override
		protected String doInBackground(String... params) {
            // if the barcode is 8 Bits long the data has one new line more.
            int ean;
            if(pop_content.length() == 8) {
                ean = 0;
            } else if (pop_content.length() == 13) {
                ean = 1;
            } else {
                return "";
            }
            HttpURLConnection urlConnection = null;
            URL url = null;
            String[] data = new String[19];
            for (int i = 0; i < 19; i++) {
                data[i] = "";

            }
            data[2] = "error=1";
            InputStream inStream = null;
            StringBuilder sb = new StringBuilder();
            sb.append("http://opengtindb.org/?ean=");
            sb.append(pop_content);
            sb.append("&cmd=query&queryid=477909028");
            try {
                url = new URL(sb.toString());
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.setDoOutput(true);
                urlConnection.setDoInput(true);
                urlConnection.connect();
                inStream = urlConnection.getInputStream();
                BufferedReader bReader = new BufferedReader(new InputStreamReader(inStream));
                String temp;
                int i = 0;
                while ((temp = bReader.readLine()) != null) {
                    data[i] = temp;
                    i++;
                }
            } catch (Exception e) {

            } finally {
                if (inStream != null) {
                    try {
                        // this will close the bReader as well
                        inStream.close();
                    } catch (IOException ignored) {
                    }
                }
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }
            if (data[1 + ean].equals("")) {
                return "";
            }
            String error = data[1 + ean].split("=")[1];
            if (!error.equals("0")) {
                return "";
            }
            String name = data[5 + ean].split("=")[1];
            if (name.equals("")) {
                String detailName = data[4 + ean].split("=")[1];
                return detailName;
            }
            return name;
		}
		@Override
		protected void onPostExecute(String result) {
            if (!result.equals("")) {
                editText.setText(result);
            }
		}
	}

}
