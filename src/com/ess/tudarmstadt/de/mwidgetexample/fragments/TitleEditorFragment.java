package com.ess.tudarmstadt.de.mwidgetexample.fragments;

import java.util.List;
import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.Dialog;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

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

	public static int _SAVE = 0;
	public static int _EDIT = 2;
	public static int _DELETE_TOKEN = 1;
	public static int _CANCEL_TOKEN = -1;

	private HandleCallbackListener mCallback;

	public interface HandleCallbackListener {
		public void onTitleEditorCallbackListener(int token, JSONObject jObj);
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
		View rootView = inflater.inflate(
				R.layout.fragment_input_change_title_map, container, false);
		
		Bundle extras = getArguments(); // getting data of entry from the parent activity for display
		if (extras != null) {
			id = extras.getInt(Constants.JSON_OBJECT_ID, -1);
			pop_title = extras.getString(Constants.JSON_OBJECT_TITLE, "");
			pop_content = extras.getString(Constants.JSON_OBJECT_CONTENT, "");
			pop_uri = extras.getString(Constants.JSON_OBJECT_URI, "");
			longitude = extras.getDouble(Constants.JSON_OBJECT_LONGITUDE, -1);
			latitude = extras.getDouble(Constants.JSON_OBJECT_LATITUDE, -1);
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

		return rootView;
	}

	private void saveAndDismiss(String title) {
		if (googleMap != null)
			googleMap.setMyLocationEnabled(false);
		if (location != null)
			location.stopMe();

		String obj_date = MainActivity.getTimestamp("dd-MM-yyyy");
		String obj_time = MainActivity.getTimestamp("kk:mm:ss");
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

			Log.e(TAG, key.toString());
		} catch (org.json.JSONException e) {
			Constants.logDebug(TAG, e.getMessage());
			e.printStackTrace();
		}

		// back to parent activity
		if (id >= 0) {
			mCallback.onTitleEditorCallbackListener(_EDIT, key);
		} else {
			mCallback.onTitleEditorCallbackListener(_SAVE, key);
		}

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
			SupportMapFragment fm = (SupportMapFragment) this.getActivity()
					.getSupportFragmentManager()
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
				googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));

				// Zoom in the Google Map
				googleMap.animateCamera(CameraUpdateFactory.zoomTo(15));

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

	/** get local address base on longitude and latitude **/
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
				result.append(", " + address.getLocality());

			}
		} catch (Exception e) {
			Constants.logDebug(TAG, e.getMessage());
			Log.e("tag", e.getMessage());
		}

		return result.toString();
	}
}