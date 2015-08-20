package com.ess.tudarmstadt.de.mwidgetexample;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.v4.app.FragmentManager.OnBackStackChangedListener;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.ess.tudarmstadt.de.mwidgetexample.JSON.Barcode;
import com.ess.tudarmstadt.de.mwidgetexample.JSON.BarcodeToJSON;
import com.ess.tudarmstadt.de.mwidgetexample.comm.CommBroadcastReceiver;
import com.ess.tudarmstadt.de.mwidgetexample.comm.CommBroadcastReceiver.JSONResult;
import com.ess.tudarmstadt.de.mwidgetexample.fragments.AmountFragment;
import com.ess.tudarmstadt.de.mwidgetexample.fragments.MainFragment;
import com.ess.tudarmstadt.de.mwidgetexample.fragments.PhotoAlbumListFragment;
import com.ess.tudarmstadt.de.mwidgetexample.fragments.SurveyFragment;
import com.ess.tudarmstadt.de.mwidgetexample.fragments.SurveyListFragment;
import com.ess.tudarmstadt.de.mwidgetexample.fragments.TitleEditorFragment;
import com.ess.tudarmstadt.de.mwidgetexample.utils.Constants;
import com.ess.tudarmstadt.de.mwidgetexample.utils.JSONArrayParser;
import com.google.zxing.client.android.CaptureActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import de.tudarmstadt.dvs.myhealthassistant.myhealthhub.events.AbstractChannel;

/**
 * The one and only activity that control all fragments
 * @author HieuHa
 *
 */

public class MainActivity extends AppCompatActivity implements
		MainFragment.OnButtonClickListener,
		TitleEditorFragment.HandleCallbackListener,
        AmountFragment.HandleCallbackListener,
		PhotoAlbumListFragment.OnListItemClickListener,
		SurveyFragment.HandleCallbackListener,
		OnBackStackChangedListener {
	private static final String TAG = MainActivity.class.getSimpleName();
	private ProgressDialog progressDialog;

	private static final int ScanReqCode = 9000;
	private static final int CameraPhotoReqCode = 7000;

	public boolean isRegistered = false;
	public CommBroadcastReceiver commUnit;
	public static HashMap<String, String> barcodes = new HashMap<>();
	private String photo_OutputFileUri = "";

	private static final String setAlarmsString = "com.ess.tudarmstadt.utils.prefs.alarms";
	private static final String setVersionString = "com.ess.tudarmstadt.utils.version";

	// false when albumListFragment, true when SurveyFragment.
	private int mode = 0;

	private SharedPreferences prefs;

	/** setting up the connection with myHealthHub */
	private void connectToMhh() {
		if (!isConnectedToMhh) {
			progressDialog = new ProgressDialog(this);
			progressDialog.setTitle("Connect to myHealthHub");
			progressDialog.setMessage("Loading...");
			progressDialog.setCancelable(false);
			progressDialog.setIndeterminate(true);
			progressDialog.show();

			myHealthHubIntent = new Intent("de.tudarmstadt.dvs.myhealthassistant.myhealthhub.IMyHealthHubRemoteService");
			myHealthHubIntent.setPackage("de.tudarmstadt.dvs.myhealthassistant.myhealthhub");
			this.getApplicationContext()
					.bindService(myHealthHubIntent,
							myHealthAssistantRemoteConnection,
							Context.BIND_AUTO_CREATE);
		} else {
			Constants.logDebug(TAG, "this's weird!");
		}
	}

	private void disconnectMHH() {
		if (isConnectedToMhh) {
			this.getApplicationContext().unbindService(
					myHealthAssistantRemoteConnection);
			isConnectedToMhh = false;
		}
		if (myHealthHubIntent != null)
			this.getApplicationContext().stopService(myHealthHubIntent);
	}

	// for connecting to the remote service of myHealthHub
	private Intent myHealthHubIntent;
	private boolean isConnectedToMhh;
	/**
	 * Service connection to myHealthHub remote service. This connection is
	 * needed in order to start myHealthHub. Furthermore, it is used inform the
	 * application about the connection status.
	 */
	private final ServiceConnection myHealthAssistantRemoteConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			Toast.makeText(getApplicationContext(),
					"Connected to myHealthAssistant", Toast.LENGTH_SHORT)
					.show();
			isConnectedToMhh = true;

			if (progressDialog != null) {
				progressDialog.dismiss();
			}
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			Toast.makeText(getApplicationContext(),
					"disconnected with myHealthAssistant", Toast.LENGTH_SHORT)
					.show();
			isConnectedToMhh = false;
		}
	};

	private final JSONResult jResult = new JSONResult() {
		@Override
		public void gotResult(JSONArray jObjArray) {
			if (mode != 2) {
				ArrayList<JSONObject> mListObj = new ArrayList<>();
				boolean addToList;
				JSONArray jObjToDel = new JSONArray(); // list of duplicates to be removed

				if (mode == 0) {
					// Make a list of object entries from JSONArray
					for (int i = 0; i < jObjArray.length(); i++) {
						JSONObject jObj = jObjArray.optJSONObject(i);
						if (jObj != null) {
							addToList = true;
							String objDate = jObj.optString(Constants.JSON_OBJECT_DATE);
							String objTime = jObj.optString(Constants.JSON_OBJECT_TIME);

							for (JSONObject jCompare : mListObj) {
								String dateCompare = jCompare.optString(Constants.JSON_OBJECT_DATE);
								String timeCompare = jCompare.optString(Constants.JSON_OBJECT_TIME);

								if (objDate.equals(dateCompare) && objTime.equals(timeCompare)) {
									addToList = false;
									// all entries with same date and time will be put here to delete
									jObjToDel.put(jObj);
									break;
								}
							}
							if (jObj.optInt(Constants.JSON_OBJECT_SURVEY) == 1) {
								addToList = false;
							}
							if (addToList)
								mListObj.add(jObj);
						}
					}
					if (jObjToDel.length() > 0) {
						Constants.logDebug(TAG, "Duplicated Entries found:" + jObjToDel.length());
						// send request delete duplicates to myHealthHub
						commUnit.massDelEntries(jObjToDel);
					}
				} else if (mode == 1) {
					for (int i = 0; i < jObjArray.length(); i++) {
						JSONObject jObj = jObjArray.optJSONObject(i);
						if (jObj != null) {
							addToList = true;
							String survey = jObj.optString(Constants.JSON_OBJECT_SURVEY_SURVEY);
							String objDate = jObj.optString(Constants.JSON_OBJECT_SURVEY_DATE);
							String objTime = jObj.optString(Constants.JSON_OBJECT_SURVEY_TIME);

							for (JSONObject jCompare : mListObj) {
								String surveyCompare = jCompare.optString(Constants.JSON_OBJECT_SURVEY_SURVEY);
								String dateCompare = jCompare.optString(Constants.JSON_OBJECT_SURVEY_DATE);
								String timeCompare = jCompare.optString(Constants.JSON_OBJECT_SURVEY_TIME);

								if (objDate.equals(dateCompare) && objTime.equals(timeCompare) && survey.equals(surveyCompare)) {
									addToList = false;
									// all entries with same date and time will be put here to delete
									jObjToDel.put(jObj);
									break;
								}
							}
							if (jObj.optInt(Constants.JSON_OBJECT_SURVEY) != 1)
								addToList = false;
							if (addToList)
								mListObj.add(jObj);
						}
					}
				}
				if (progressDialog != null)
					progressDialog.dismiss();

				// Start show list by create a new ListFragment
				Bundle args = new Bundle();
				JSONArrayParser jParser = new JSONArrayParser();
				jParser.setJsonArrayList(mListObj);
				args.putParcelable(PhotoAlbumListFragment.JSON_PARSER, jParser);
				args.putInt("mode", mode);

				PhotoAlbumListFragment fragment = new PhotoAlbumListFragment();
				fragment.setArguments(args);

				getSupportFragmentManager().popBackStack();
				FragmentTransaction transaction = getSupportFragmentManager()
						.beginTransaction();
				transaction.replace(R.id.container, fragment);
				transaction.addToBackStack(null);
				transaction.commit();
			} else {
				for (int i = 0; i < jObjArray.length(); i++) {
					JSONObject jObj = jObjArray.optJSONObject(i);
					int id = jObj.optInt(Constants.JSON_OBJECT_BARCODE);
					if (id == 1) {
						String barcode = jObj.optString(Constants.JSON_OBJECT_BARCODE_BARCODE);
						String name = jObj.optString(Constants.JSON_OBJECT_BARCODE_NAME);
						barcodes.put(barcode, name);
					}
				}
			}

		}
	};

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// No call for super(). Bug on API Level > 11.TitleEditorFragment.
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// Listen for changes in the back stack
		getSupportFragmentManager().addOnBackStackChangedListener(this);
		// Handle when activity is recreated like on orientation Change
		shouldDisplayHomeUp();

		connectToMhh();

		prefs = getSharedPreferences(
				"com.ess.tudarmstadt.utils.prefs", Context.MODE_PRIVATE);

		if (!prefs.getString(setVersionString, "").equals(getString((R.string.version)))) {
			prefs.edit().putInt(setAlarmsString, 0).apply();
			prefs.edit().putString(setVersionString, getString(R.string.version)).apply();

		}

		if (prefs.getInt(setAlarmsString, 0) == 0) {
			Log.e("eee",String.valueOf(prefs.getInt(setAlarmsString, 0)));
			alarmForSurvey();
		}

		// to exchange data with myHealthHub
		commUnit = new CommBroadcastReceiver(this.getApplicationContext(),
				jResult);
		this.getApplicationContext().registerReceiver(commUnit,
				new IntentFilter(AbstractChannel.MANAGEMENT));
		isRegistered = true;
		Bundle extras;
		extras = getIntent().getExtras();
		if (extras != null) {
			if (extras.getString("usage") != null) {
				SurveyFragment fragment = new SurveyFragment();
				Bundle bundle = new Bundle();
				int time = extras.getInt("time", -1);
				bundle.putInt("time", time);
				fragment.setArguments(bundle);
				FragmentTransaction transaction = getSupportFragmentManager()
						.beginTransaction();
				transaction.add(R.id.container, fragment);
				transaction.commitAllowingStateLoss();
			} else if (savedInstanceState == null) {
				MainFragment fragment = new MainFragment();
				FragmentTransaction transaction = getSupportFragmentManager()
						.beginTransaction();
				transaction.add(R.id.container, fragment);
				transaction.commitAllowingStateLoss();
			}
		} else if (savedInstanceState == null) {
			MainFragment fragment = new MainFragment();
			FragmentTransaction transaction = getSupportFragmentManager()
					.beginTransaction();
			transaction.add(R.id.container, fragment);
			transaction.commitAllowingStateLoss();
		}
	}

	@Override
	protected void onDestroy() {
		Log.e(TAG, "onDestroy");
		disconnectMHH();
		if (isRegistered) {
			this.getApplicationContext().unregisterReceiver(commUnit);
			isRegistered = false;
		}
		super.onDestroy();
	}

	@Override
	public void onButtonClickListener(int token) {
		// Handle button click from MainFragment
		Log.e(TAG, "buttonClicking: " + token);
		if (token == MainFragment.BARCODE_CAPTURE_TOKEN) {
			mode = 2;
			commUnit.getJSONEntryList();
			Intent intent = new Intent(this.getApplicationContext(),
					CaptureActivity.class);
			intent.setAction("com.google.zxing.client.android.SCAN");
			// intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
			// intent.putExtra("SCAN_FORMATS",
			// "CODABAR,EAN_13,QR_CODE,EAN_8");
			this.startActivityForResult(intent, ScanReqCode);

		} else if (token == MainFragment.PHOTO_CAPTURE_TOKEN) {
			File exportDir = new File(Environment.getExternalStorageDirectory()
					+ "/BarcodeScannerWidget/camera/", "");
			File file = new File(exportDir,
					getTimestamp("yyyyMMddkkmm") + ".jpg");
			if (!exportDir.exists()) {
				exportDir.mkdirs();
			}
			try {
				if (!file.exists())
					file.createNewFile();
				Uri uri = Uri.fromFile(file);
				photo_OutputFileUri = uri.toString();
				Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
				intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
				startActivityForResult(intent, CameraPhotoReqCode);
			} catch (IOException e) {
				e.printStackTrace();
				Constants.logDebug(TAG, e.toString());
			}
		} else if (token == MainFragment.SHOW_ALBUM_TOKEN) {
			mode = 0;
			if (commUnit != null) {
				commUnit.getJSONEntryList();
			}
		} else if (token == MainFragment.SHOW_SURVEY_TOKEN) {
			mode = 1;
			commUnit.getJSONEntryList();
		}
	}


    @Override
    public void onAmountCallbackListener(final JSONObject key) {
        if (commUnit != null) {
            commUnit.storeEntry(key);
			String barcode;
			String title;
			barcode = key.optString(Constants.JSON_OBJECT_CONTENT, "");
			title = key.optString(Constants.JSON_OBJECT_TITLE, "");
			Barcode barcodeItem = new Barcode(barcode, title);
			JSONObject jsonObj = new JSONObject();
			try {
				jsonObj = BarcodeToJSON.getJSONfromBarcode(barcodeItem);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			commUnit.storeEntry(jsonObj);
			getSupportFragmentManager().popBackStack();
			finish();
        }
    }

	@Override
	public void onSurveyCallbackListener (final JSONObject jsonObject) {
		commUnit.storeEntry(jsonObject);
		int survey = 0;
		try {
			survey = jsonObject.getInt(Constants.JSON_OBJECT_SURVEY_SURVEY);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		Intent intent = new Intent(this, com.ess.tudarmstadt.de.mwidgetexample.utils.AlarmReceiver.class);
		intent.putExtra("time", survey - 1);
		intent.putExtra("usage", "delete");
		PendingIntent pendingIntent = PendingIntent.getBroadcast(this, (int) System.currentTimeMillis(), intent, PendingIntent.FLAG_UPDATE_CURRENT);
		try {
			pendingIntent.send();
		} catch (PendingIntent.CanceledException e) {
			e.printStackTrace();
		}
		finish();

		if (survey < 6) {
			Intent openSurvey = new Intent(this, com.ess.tudarmstadt.de.mwidgetexample.MainActivity.class);
			openSurvey.setAction("com.ess.tudarmstadt.de.mwidgetexample.openSurvey");
			Bundle extras = new Bundle();
			extras.putInt("time", survey);
			extras.putString("usage", Constants.Survey_Intent);
			openSurvey.putExtras(extras);
			startActivity(openSurvey);
		} else {
			Toast.makeText(this, "survey saved", Toast.LENGTH_SHORT).show();
			//finish();
		}
	}

	@Override
	public void onTitleEditorCallbackListener(int token, final JSONObject key) {
		// Handle the Save/Delete button click from TitleEditorFragment
		Log.e(TAG, "onCallback");
		if (token == TitleEditorFragment._EDIT) {
			if (commUnit != null) {
				commUnit.editEntry(key);
			}
			this.getSupportFragmentManager().popBackStack();

			if (progressDialog != null)
				progressDialog.show();
			commUnit.getJSONEntryList();

		}

		else if (token == TitleEditorFragment._DELETE_TOKEN) {
			try {
				int obj_id = key.getInt(Constants.JSON_OBJECT_ID);
				String obj_uri = key.getString(Constants.JSON_OBJECT_URI);

				// check if content is the uri to image store in sd card
				if (obj_uri != null && obj_id != -1) {
					Uri uri = Uri.parse(obj_uri);
					File file = new File(uri.getPath());
					if (file.exists()) {
						if (file.delete()) {
							commUnit.deleteEntry(obj_id);
							Toast.makeText(this.getApplicationContext(),
									"delete successful!", Toast.LENGTH_SHORT)
									.show();
						}
					} else
						commUnit.deleteEntry(obj_id);

					this.getSupportFragmentManager().popBackStack();
					if (progressDialog != null)
						progressDialog.show();
					commUnit.getJSONEntryList();
				}
			} catch (JSONException e) {
				Log.e(TAG, e.toString());
			}
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode,
			Intent intent) {
		if (requestCode == ScanReqCode) {
			// result of scan bar code
			this.finishActivity(ScanReqCode);
			if (resultCode == RESULT_OK) {
				String obj_content = intent.getStringExtra("SCAN_RESULT");
				String format = intent.getStringExtra("SCAN_RESULT_FORMAT");
				// Handle successful scan
				Log.e(TAG, format + ":" + obj_content);

				addNewBarcodeItem(-1, "", obj_content, -1, -1, "", -1);
			}
		} else if (requestCode == CameraPhotoReqCode) {
			// result of photo capture
			this.finishActivity(CameraPhotoReqCode);
			if (!photo_OutputFileUri.isEmpty() && resultCode == RESULT_OK) {
				addNewBarcodeItem(-1, "", "", -1, -1, photo_OutputFileUri, -1);
			}
		}
	}

	@Override
	public void onPhotoListItemClickListener(int id, String title,
			String content, double longitude, double latitude, String objUri, int amount) {
		// Handle the click on item of PhotoAlbumListFragment
		openEditor(id, title, content, longitude, latitude, objUri, amount);

	}

	/** Open the title editor fragment **/
	private void openEditor(int id, String title, String content,
			double longitude, double latitude, String objUri, int amount) {
		Log.e(TAG, "OpenEditor:" + id + "; " + title + "; " + content + "; "
				+ longitude + "; " + latitude);

		Bundle args = new Bundle();
		args.putInt(Constants.JSON_OBJECT_ID, id);
		args.putString(Constants.JSON_OBJECT_TITLE, title);
		args.putDouble(Constants.JSON_OBJECT_LONGITUDE, longitude);
		args.putDouble(Constants.JSON_OBJECT_LATITUDE, latitude);
		args.putString(Constants.JSON_OBJECT_CONTENT, content);
		args.putString(Constants.JSON_OBJECT_URI, objUri);
		args.putInt(Constants.JSON_OBJECT_AMOUNT, amount);

		TitleEditorFragment fragment = new TitleEditorFragment();
		fragment.setArguments(args);

		FragmentTransaction transaction = getSupportFragmentManager()
				.beginTransaction();
		transaction.replace(R.id.container, fragment);
		transaction.addToBackStack(null);
		transaction.commitAllowingStateLoss();
	}

	@Override
	public void onSurveyListItemClickListener(String time, String date, int survey, JSONArray results) {
		showSurvey(time, date, survey, results);
	}

	private void showSurvey(String time, String date, int survey, JSONArray results) {
		Bundle args = new Bundle();
		args.putInt(Constants.JSON_OBJECT_SURVEY, 1);
		args.putString(Constants.JSON_OBJECT_SURVEY_TIME, time);
		args.putString(Constants.JSON_OBJECT_SURVEY_DATE, date);
		args.putInt(Constants.JSON_OBJECT_SURVEY_SURVEY, survey);

		int[] resultInt = new int[results.length()];
		for (int i = 0; i < results.length(); i++) {
			resultInt[i] = results.optInt(i);
		}

		args.putIntArray(Constants.JSON_OBJECT_SURVEY_RESULT, resultInt);

		SurveyListFragment fragment = new SurveyListFragment();
		fragment.setArguments(args);

		FragmentTransaction transaction = getSupportFragmentManager()
				.beginTransaction();
		transaction.replace(R.id.container, fragment);
		transaction.addToBackStack(null);
		transaction.commitAllowingStateLoss();
	}

	private void addNewBarcodeItem(int id, String title, String content,
								   double longitude, double latitude, String objUri, int amount) {
		Log.e(TAG, "OpenEditor:" + id + "; " + title + "; " + content + "; "
				+ longitude + "; " + latitude);

		Bundle args = new Bundle();
		args.putInt(Constants.JSON_OBJECT_ID, id);
		args.putString(Constants.JSON_OBJECT_TITLE, title);
		args.putDouble(Constants.JSON_OBJECT_LONGITUDE, longitude);
		args.putDouble(Constants.JSON_OBJECT_LATITUDE, latitude);
		args.putString(Constants.JSON_OBJECT_CONTENT, content);
		args.putString(Constants.JSON_OBJECT_URI, objUri);
		args.putInt(Constants.JSON_OBJECT_AMOUNT, amount);

		AmountFragment fragment = new AmountFragment();
		fragment.setArguments(args);

		FragmentTransaction transaction = getSupportFragmentManager()
				.beginTransaction();
		transaction.replace(R.id.container, fragment);
		transaction.addToBackStack(null);
		transaction.commitAllowingStateLoss();
	}


	/**
	 * Returns the current time
	 * 
	 * @param timeFormat
	*            the format time you want to return; for example: "dd-MM-yyyy"
	 */
	public static String getTimestamp(String timeFormat) {
		return (String) android.text.format.DateFormat.format(timeFormat,
				new java.util.Date());
	}

	@Override
	public void onBackStackChanged() {
		shouldDisplayHomeUp();
	}

	private void shouldDisplayHomeUp() {
		// Enable Up button only if there are entries in the back stack
		boolean canback = getSupportFragmentManager().getBackStackEntryCount() > 0;
		getSupportActionBar().setDisplayHomeAsUpEnabled(canback);
	}

	@Override
	public boolean onSupportNavigateUp() {
		// This method is called when the up button is pressed. Just the pop
		// back stack.
		getSupportFragmentManager().popBackStack();
		return true;
	}

	// Notification for survey
	private void alarmForSurvey() {
		AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		Calendar[] calendars = new Calendar[7];
		Intent[] intents = new Intent[7];
		for (int i = 0; i < 7; i++) {
			calendars[i] = Calendar.getInstance();
			calendars[i].setTimeInMillis(System.currentTimeMillis());
			switch(i) {
				case 0: calendars[i].set(Calendar.HOUR_OF_DAY, 8);
					break;
				case 1: calendars[i].set(Calendar.HOUR_OF_DAY, 10);
					break;
				case 2: calendars[i].set(Calendar.HOUR_OF_DAY, 12);
					break;
				case 3: calendars[i].set(Calendar.HOUR_OF_DAY, 14);
					break;
				case 4: calendars[i].set(Calendar.HOUR_OF_DAY, 16);
					break;
				case 5: calendars[i].set(Calendar.HOUR_OF_DAY, 18);
					break;
				case 6: calendars[i].set(Calendar.HOUR_OF_DAY, 20);
					break;
			}
			intents[i] = new Intent(getApplicationContext(), com.ess.tudarmstadt.de.mwidgetexample.utils.AlarmReceiver.class);
			intents[i].putExtra("time", i);
			intents[i].putExtra("usage", "create");
			PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), i, intents[i], PendingIntent.FLAG_UPDATE_CURRENT);
			alarmManager.cancel(pendingIntent);
			alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendars[i].getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);
		}
		prefs.edit().putInt(setAlarmsString, 1).apply();
	}
}
