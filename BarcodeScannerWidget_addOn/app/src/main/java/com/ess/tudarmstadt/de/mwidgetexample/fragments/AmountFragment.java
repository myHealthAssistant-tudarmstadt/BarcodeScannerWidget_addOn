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
import android.widget.ProgressBar;

import com.ess.tudarmstadt.de.mwidgetexample.MainActivity;
import com.ess.tudarmstadt.de.mwidgetexample.R;
import com.ess.tudarmstadt.de.mwidgetexample.utils.Constants;
import com.ess.tudarmstadt.de.mwidgetexample.utils.MyLocation;
import com.ess.tudarmstadt.de.mwidgetexample.JSON.*;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class AmountFragment extends Fragment {

    private double latitude, longitude;
    private String pop_content = "";
    private String pop_uri = "";

    private HandleCallbackListener mCallback;
    private EditText editText;
    private EditText editTextAmount;

    private ProgressBar bar;

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

        editTextAmount.setText("250");

        editText = (EditText) rootView.findViewById(R.id.name);

        MyLocation location = new MyLocation();
        location.getLocation(this.getActivity().getApplicationContext(), locationResult);

        Button plus = (Button) rootView.findViewById(R.id.plus_btn);
        plus.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                int ml = Integer.parseInt(editTextAmount.getText().toString());
                ml += 10;
                editTextAmount.setText(String.valueOf(ml));
            }
        });

        Button minus = (Button) rootView.findViewById(R.id.minus_btn);
        minus.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                int ml = Integer.parseInt(editTextAmount.getText().toString());
                ml -= 10;
                editTextAmount.setText(String.valueOf(ml));
            }
        });

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
                try {
                    saveAndDismiss(title);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        bar = (ProgressBar) rootView.findViewById(R.id.progressBar);

        String title = "";
        if (MainActivity.barcodes.containsKey(pop_content)) {
            title = MainActivity.barcodes.get(pop_content);
        }
        if (!title.equals("")) {
            editText.setText(title);
        } else if (isNetworkAvailable() && pop_uri.equals("")) {
            new MyTask().execute();
        }
        return rootView;
    }

    private final MyLocation.LocationResult locationResult = new MyLocation.LocationResult() {
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

    private void saveAndDismiss(String title) throws JSONException {
        String obj_date = MainActivity.getTimestamp("dd-MM-yyyy");
        String obj_time = MainActivity.getTimestamp("kk:mm:ss");
        String address = getAddress(latitude, longitude);
        int amount = Integer.parseInt(editTextAmount.getText().toString());
        BarcodeItem barcodeItem = new BarcodeItem(
                -1, title, pop_content,obj_date,obj_time,longitude,latitude,address,pop_uri,amount);
        JSONObject key = BarcodeItemToJSON.getJSONfromBarcode(barcodeItem);
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
        protected void onPreExecute(){
            bar.setVisibility(View.VISIBLE);
        }

        @Override
		protected String doInBackground(String... params) {
            HttpURLConnection urlConnection = null;
            URL url;
            String[] data = new String[19];
            for (int i = 0; i < 19; i++) {
                data[i] = "";

            }
            int errorLine = 0;
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
                    if (temp.contains("error")) {
                        errorLine = i;
                    }
                    data[i] = temp;
                    i++;
                }
                if (errorLine == 0) {
                    return "";
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
            if (!data[errorLine].split("=")[1].equals("0")) {
                return "";
            }
            String name = data[errorLine + 4].split("=")[1];
            if (name.equals("")) {
                return data[errorLine + 5].split("=")[1];
            }
            return name;
		}
		@Override
		protected void onPostExecute(String result) {
            bar.setVisibility(View.GONE);
            if (!result.equals("")) {
                editText.setText(result);
            }
		}
	}

}
