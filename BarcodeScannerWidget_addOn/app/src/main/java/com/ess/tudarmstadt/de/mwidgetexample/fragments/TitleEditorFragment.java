package com.ess.tudarmstadt.de.mwidgetexample.fragments;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.SeekBar;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.ess.tudarmstadt.de.mwidgetexample.MainActivity;
import com.ess.tudarmstadt.de.mwidgetexample.R;
import com.ess.tudarmstadt.de.mwidgetexample.utils.Constants;
import com.ess.tudarmstadt.de.mwidgetexample.utils.MyLocation;
import com.ess.tudarmstadt.de.mwidgetexample.utils.MyLocation.LocationResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * Showing the Title Fragment for each entry, along with google GPS map
 * @author HieuHa
 *
 */
public class TitleEditorFragment extends Fragment {
	private static final String TAG = TitleEditorFragment.class.getSimpleName();
	private String pop_title = "";
	private String pop_content = "";
	private double longitude, latitude;
	private int id;
	private String pop_uri = "";
	private String address = "";
	private GoogleMap googleMap;
	private MyLocation location;
	private int amount = -1;
	private NumberPicker numberPicker;
	private ToggleButton showImg;
	private ToggleButton showMap;
	private boolean mapActive;

	public static int _EDIT = 2;
	public static int _DELETE_TOKEN = 1;

	private HandleCallbackListener mCallback;

	public interface HandleCallbackListener {
		void onTitleEditorCallbackListener(int token, JSONObject jObj);
	}

	public TitleEditorFragment() {
		// just an empty constructor
	}

	private LocationResult locationResult = new LocationResult() {

		@Override
		public void gotLocation(Location location) {
			if (location != null) {
				latitude = location.getLatitude();
				longitude = location.getLongitude();
				showLocationOnMap();
			} else {
				latitude = -1;
				longitude = -1;
			}
		}
	};

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
				R.layout.fragment_input_change_title_map, container, false);

		Bundle extras = getArguments(); // getting data of entry from the p arent activity for display
		if (extras != null) {
			id = extras.getInt(Constants.JSON_OBJECT_ID, -1);
			pop_title = extras.getString(Constants.JSON_OBJECT_TITLE, "");
			pop_content = extras.getString(Constants.JSON_OBJECT_CONTENT, "");
			pop_uri = extras.getString(Constants.JSON_OBJECT_URI, "");
			longitude = extras.getDouble(Constants.JSON_OBJECT_LONGITUDE, -1);
			latitude = extras.getDouble(Constants.JSON_OBJECT_LATITUDE, -1);
			amount = extras.getInt(Constants.JSON_OBJECT_AMOUNT, -1);
			Log.e(TAG, "had loc:" + longitude + "; " + latitude);
		}

		final EditText edTxt = (EditText) rootView.findViewById(R.id.et_un);
		edTxt.setText(pop_title);

		TextView texCnt = (TextView) rootView.findViewById(R.id.text_content);
		texCnt.setText(pop_content + "\n" + pop_uri);

		if (longitude == 0.0 && latitude == 0.0 || longitude == -1.0
				&& latitude == -1.0) {
			// get the location of scan activity
			if (location == null) {
				location = new MyLocation();
			}
			location.getLocation(this.getActivity().getApplicationContext(),
					locationResult);
		} else {
			showLocationOnMap();
		}

		Button onSave = (Button) rootView.findViewById(R.id.scan_save_btn);
		onSave.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				saveAndDismiss(edTxt.getText().toString());
			}
		});

		Button onDel = (Button) rootView.findViewById(R.id.scan_del_btn);
		onDel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				deleteEntry();
			}
		});

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
		numberPicker.setValue(amount);

		mapActive = true;
		showMap = (ToggleButton) rootView.findViewById(R.id.map_btn);
		showMap.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (!mapActive) {
					showMap.setChecked(true);
					showImg.setChecked(false);
					mapActive = true;
					rootView.findViewById(R.id.location_map).setVisibility(rootView.VISIBLE);
					rootView.findViewById(R.id.image_view).setVisibility(rootView.GONE);
				}
			}
		});

		showImg = (ToggleButton) rootView.findViewById(R.id.image_btn);
		showImg.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (mapActive) {
					showMap.setChecked(false);
					showImg.setChecked(true);
					mapActive = false;
					rootView.findViewById(R.id.location_map).setVisibility(rootView.GONE);
					rootView.findViewById(R.id.image_view).setVisibility(rootView.VISIBLE);
				}
			}
		});
		if(!pop_uri.equals("")) {
			showMap.setChecked(true);
			showImg.setChecked(false);
			try {
				ImageView image = (ImageView) rootView.findViewById(R.id.image_view);
				InputStream is = new URL(pop_uri).openStream();
				Bitmap bitmap = BitmapFactory.decodeStream(is);
				image.setImageBitmap(bitmap);
				is.close();
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} {

			}
		} else {
			showMap.setVisibility(rootView.GONE);
			showImg.setVisibility(rootView.GONE);
		}

		return rootView;
	}

	private void saveAndDismiss(String title) {
		if (googleMap != null)
			googleMap.setMyLocationEnabled(false);
		if (location != null)
			location.stopMe();

		String obj_date = MainActivity.getTimestamp("dd-MM-yyyy");
		String obj_time = MainActivity.getTimestamp("kk:mm:ss");
		int amount = numberPicker.getValue();
		JSONObject key = new JSONObject();
		try {
			key.putOpt(Constants.JSON_OBJECT_ID, id);
			key.putOpt(Constants.JSON_OBJECT_TITLE, title);
			key.putOpt(Constants.JSON_OBJECT_CONTENT, pop_content);
			key.putOpt(Constants.JSON_OBJECT_DATE, obj_date);
			key.putOpt(Constants.JSON_OBJECT_TIME, obj_time);
			key.putOpt(Constants.JSON_OBJECT_LONGITUDE, longitude);
			key.putOpt(Constants.JSON_OBJECT_LATITUDE, latitude);
			key.putOpt(Constants.JSON_OBJECT_LOCATION, address);
			key.putOpt(Constants.JSON_OBJECT_URI, pop_uri);
			key.putOpt(Constants.JSON_OBJECT_AMOUNT, amount);

			Log.e(TAG, key.toString());
		} catch (org.json.JSONException e) {
			Constants.logDebug(TAG, e.getMessage());
			e.printStackTrace();
		}

		// back to parent activity
		mCallback.onTitleEditorCallbackListener(_EDIT, key);
	}

	private void deleteEntry() {
		JSONObject key = new JSONObject();
		try {
			key.putOpt(Constants.JSON_OBJECT_ID, id);
			key.putOpt(Constants.JSON_OBJECT_URI, pop_uri);
		} catch (JSONException e) {
			Constants.logDebug(TAG, e.getMessage());
			e.printStackTrace();
		}

		if (googleMap != null)
			googleMap.setMyLocationEnabled(false);
		if (location != null)
			location.stopMe();

		// back to parent activity
		mCallback.onTitleEditorCallbackListener(_DELETE_TOKEN, key);

	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		if (this.getView() != null) {
			ViewGroup parentViewGroup = (ViewGroup) this.getView().getParent();
			if (parentViewGroup != null) {
				parentViewGroup.removeAllViews();
			}
		}
	}

	@Override
	public void onDestroy() {
		Log.e(TAG, "onDestroy");
		if (googleMap != null)
			googleMap.setMyLocationEnabled(false);
		if (location != null)
			location.stopMe();

		SupportMapFragment fm = (SupportMapFragment) this.getActivity()
				.getSupportFragmentManager()
				.findFragmentById(R.id.location_map);
		if (fm != null)
			getActivity().getSupportFragmentManager().beginTransaction().remove(fm).commit();
		super.onDestroy();
	}

	private void showLocationOnMap() {
		address = getAddress(latitude, longitude);

		// Getting Google Play availability status
		int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this
				.getActivity().getBaseContext());

		// Showing status
		if (status != ConnectionResult.SUCCESS) { // Google Play Services are
			// not available
			int requestCode = 10;
			Dialog dialog = GooglePlayServicesUtil.getErrorDialog(status,
					this.getActivity(), requestCode);
			dialog.show();

		} else { // Google Play Services are available

			// Getting reference to the SupportMapFragment of xml file
			SupportMapFragment fm = (SupportMapFragment) this.getChildFragmentManager()
					.findFragmentById(R.id.location_map);

			// Getting GoogleMap object from the fragment
			if (fm != null) {
				googleMap = fm.getMap();

				// Enabling MyLocation Layer of Google Map
				googleMap.setMyLocationEnabled(true);
				googleMap.getUiSettings().setMyLocationButtonEnabled(true);

				// Creating a LatLng object for the current location
				LatLng latLng = new LatLng(latitude, longitude);

				// Showing the current location in Google Map
				googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));


				// googleMap.setOnMarkerClickListener(this);

				googleMap
						.addMarker(new MarkerOptions()
								.position(latLng)
								.title(pop_title)
								.snippet(pop_content)
								.icon(BitmapDescriptorFactory
										.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
			}
		}
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
			Constants.logDebug(TAG, e.getMessage());
			Log.e("tag", e.getMessage());
		}

		return result.toString();
	}



	/*

	private class MyTask extends AsyncTask<String, String, String> {
		private Context mContext;
		private View rootView;
		private String content;
		public MyTask(Context context, View rootView, String pop_content) {
			this.mContext = context;
			this.rootView = rootView;
			this.content = pop_content;
		}
		@Override
		protected String doInBackground(String... params) {
			String XmlData = "";
			HttpURLConnection httpURLConnection;
			BufferedReader bufferedReader;
			StringBuilder sb = new StringBuilder();
			sb.append("http://api.upcdatabase.org/xml/8f3d9ef8f378f007cf6c5fc4a93dd463/");
			sb.append(content);
			String link = sb.toString();
			try {
				URL url = new URL(link);
				httpURLConnection = (HttpURLConnection) url.openConnection();
				InputStream inputStream = httpURLConnection.getInputStream();
				bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
				String line;
				while ((line = bufferedReader.readLine()) != null) {
					XmlData += line += "::";
				}
				if (httpURLConnection != null) {
					httpURLConnection.disconnect();
				}
				if (bufferedReader != null) {
					bufferedReader.close();
				}
			} catch(IOException e) {
				Log.e("x", "x");
			}
			String[] XmlDataArray = XmlData.split("::");
			String[] firstBrace = XmlDataArray[6].split(">");
			String[] lastBrace = firstBrace[1].split("<");
			Log.e("x", XmlDataArray[6]);
			return lastBrace[0];
		}
		@Override
		protected void onPostExecute(String result) {
			final EditText edTxt = (EditText) rootView.findViewById(R.id.et_un);
			edTxt.setText(result); // txt.setText(result);
			// might want to change "executed" for the returned string passed
			// into onPostExecute() but that is upto you
		}
	}		*/
}