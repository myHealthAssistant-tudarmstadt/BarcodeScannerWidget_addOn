package com.ess.tudarmstadt.de.mwidgetexample;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.v4.app.FragmentManager.OnBackStackChangedListener;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.ess.tudarmstadt.de.mwidgetexample.comm.CommBroadcastReceiver;
import com.ess.tudarmstadt.de.mwidgetexample.comm.CommBroadcastReceiver.JSONResult;
import com.ess.tudarmstadt.de.mwidgetexample.fragments.AmountFragment;
import com.ess.tudarmstadt.de.mwidgetexample.fragments.MainFragment;
import com.ess.tudarmstadt.de.mwidgetexample.fragments.PhotoAlbumListFragment;
import com.ess.tudarmstadt.de.mwidgetexample.fragments.SurveyFragment;
import com.ess.tudarmstadt.de.mwidgetexample.fragments.SurveyListFragment;
import com.ess.tudarmstadt.de.mwidgetexample.fragments.TitleEditorFragment;
import com.ess.tudarmstadt.de.mwidgetexample.utils.Constants;
import com.ess.tudarmstadt.de.mwidgetexample.utils.DBHelper;
import com.ess.tudarmstadt.de.mwidgetexample.utils.JSONArrayParser;
import com.google.zxing.client.android.CaptureActivity;


import de.tudarmstadt.dvs.myhealthassistant.myhealthhub.events.AbstractChannel;

/**
 * The one and only activity that control all fragments
 * @author HieuHa
 *
 */

public class MainActivity extends ActionBarActivity implements
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

	private CommBroadcastReceiver commUnit;
	private String photo_OutputFileUri = "";

	public static final String setAlarmsString = "com.ess.tudarmstadt.utils.prefs.alarms";
	private static final String setVersionString = "com.ess.tudarmstadt.utils.version";

	SharedPreferences prefs;

	public static DBHelper mydb;

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
	boolean isConnectedToMhh;
	/**
	 * Service connection to myHealthHub remote service. This connection is
	 * needed in order to start myHealthHub. Furthermore, it is used inform the
	 * application about the connection status.
	 */
	private ServiceConnection myHealthAssistantRemoteConnection = new ServiceConnection() {
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

	private JSONResult jResult = new JSONResult() {
		@Override
		public void gotResult(JSONArray jObjArray) {
			// Make a list of object entries from JSONArray
			ArrayList<JSONObject> mListObj = new ArrayList<>();
			boolean addToList;
			
			JSONArray jObjToDel = new JSONArray(); // list of duplicates to be removed
			for (int i = 0; i < jObjArray.length(); i++) {
				JSONObject jObj = jObjArray.optJSONObject(i);
				if (jObj != null){
					addToList = true;
					String objDate = jObj.optString(Constants.JSON_OBJECT_DATE);
					String objTime = jObj.optString(Constants.JSON_OBJECT_TIME);
					
					for (JSONObject jCompare : mListObj){
						String dateCompare = jCompare.optString(Constants.JSON_OBJECT_DATE);
						String timeCompare = jCompare.optString(Constants.JSON_OBJECT_TIME);
						
						if (objDate.equals(dateCompare) && objTime.equals(timeCompare)){
							addToList = false;
							// all entries with same date and time will be put here to delete
							jObjToDel.put(jObj);
							break;
						}
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

			if (progressDialog != null)
				progressDialog.dismiss();

			// Start show list by create a new ListFragment
			Bundle args = new Bundle();
			JSONArrayParser jParser = new JSONArrayParser();
			jParser.setJsonArrayList(mListObj);
			args.putParcelable(PhotoAlbumListFragment.JSON_PARSER, jParser);

			PhotoAlbumListFragment fragment = new PhotoAlbumListFragment();
			fragment.setArguments(args);

			getSupportFragmentManager().popBackStack();
			FragmentTransaction transaction = getSupportFragmentManager()
					.beginTransaction();
			// transaction.remove(arg0)
			transaction.replace(R.id.container, fragment);
			transaction.addToBackStack(null);
			transaction.commit();
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
		mydb = new DBHelper(this);
	}

	@Override
	protected void onDestroy() {
		Log.e(TAG, "onDestroy");
		disconnectMHH();
		this.getApplicationContext().unregisterReceiver(commUnit);

		super.onDestroy();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public void onButtonClickListener(int token) {
		// Handle button click from MainFragment
		Log.e(TAG, "buttonClicking: " + token);
		if (token == MainFragment.BARCODE_CAPTURE_TOKEN) {
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
			if (commUnit != null) {
				if (progressDialog != null)
					progressDialog.show();
				commUnit.getJSONEntryList();
			}
		} else if (token == MainFragment.SHOW_SURVEY_TOKEN) {
			SurveyListFragment surveyListFragment = new SurveyListFragment();

			FragmentTransaction transaction = getSupportFragmentManager()
					.beginTransaction();
			transaction.replace(R.id.container, surveyListFragment);
			transaction.addToBackStack(null);
			// transaction.commit();
			transaction.commitAllowingStateLoss();
		}
	}


    @Override
    public void onAmountCallbackListener(final JSONObject key) {
        if (commUnit != null) {
            if (progressDialog != null)
                progressDialog.show();

            commUnit.storeEntry(key);
            updateAppWidget(key);
			String barcode = "";
			String title = "";
			barcode = key.optString(Constants.JSON_OBJECT_CONTENT, "");
			title = key.optString(Constants.JSON_OBJECT_TITLE, "");
			if (!(barcode.equals("")) && !(title.equals(""))) {
				mydb.insertBarcode(barcode, title);
			}
			Handler mHandler = new Handler();
            mHandler.postDelayed(new Runnable() {

				@Override
				public void run() {
					if (progressDialog != null)
						progressDialog.dismiss();
					getSupportFragmentManager().popBackStack();
					finish();
				}
			}, 8000);
        }
    }

	@Override
	public void onSurveyCallbackListener (int survey, int[] values) {
		Calendar c = Calendar.getInstance();
		SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
		String formattedDate = df.format(c.getTime());
		SimpleDateFormat sdf = new SimpleDateFormat("HH-mm");
		String time = sdf.format(c.getTime());
		for (int i = 0; i < values.length; i++) {
			mydb.insertValue(formattedDate, time, survey, i + 1, values[i]);
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
		}
	}

	@Override
	public void onTitleEditorCallbackListener(int token, final JSONObject key) {
		// Handle the Save/Delete button click from TitleEditorFragment
		Log.e(TAG, "onCallback");
		if (token == TitleEditorFragment._EDIT) {
			if (commUnit != null) {
				commUnit.editEntry(key);
				updateAppWidget(key);
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
				String obj_date = getTimestamp("dd-MM-yyyy");
				String obj_time = getTimestamp("kk:mm:ss");
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
		// transaction.commit();
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
		// transaction.commit();
		transaction.commitAllowingStateLoss();
	}

	/**
	 * Update this app's Widget at homescreen
	 */
	private void updateAppWidget(JSONObject key) {
		String date = key.optString(Constants.JSON_OBJECT_DATE);
		String obj_content = key.optString(Constants.JSON_OBJECT_CONTENT, "");
		String obj_uri = key.optString(Constants.JSON_OBJECT_URI, "");
		String contents = obj_content + obj_uri;
		if (date != null) {
			AppWidgetManager appWidgetManager = AppWidgetManager
					.getInstance(this.getApplicationContext());

			// Get all ids
			ComponentName thisWidget = new ComponentName(
					getApplicationContext(), MyWidgetProvider.class);
			int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
			for (int widgetId : allWidgetIds) {

				RemoteViews remoteViews = new RemoteViews(
						getApplicationContext().getPackageName(),
						R.layout.widget_layout);

				// Set the text
				String text = "";
				if (contents.length() > 40)
					text = contents.substring(0, 40) + "...\n" + date;
				else
					text = contents;
				remoteViews.setTextViewText(R.id.scan_info, text);

				// Create an Intent to launch PhotoLaunchActivity
				Intent intent = new Intent(this.getApplicationContext(),
						MainActivity.class);
				PendingIntent pendingIntent = PendingIntent.getActivity(
						this.getApplicationContext(), 0, intent, 0);
				remoteViews.setOnClickPendingIntent(R.id.wgt_cam_cap_btn,
						pendingIntent);

				appWidgetManager.updateAppWidget(widgetId, remoteViews);
			}
		}
	}

	/**
	 * Returns the current time
	 * 
	 * @param timeFormat
	*            the format time you want to return; for example: "dd-MM-yyyy"
			* @return
			*/
	public static String getTimestamp(String timeFormat) {
		return (String) android.text.format.DateFormat.format(timeFormat,
				new java.util.Date());
	}

	@Override
	public void onBackStackChanged() {
		shouldDisplayHomeUp();
	}

	public void shouldDisplayHomeUp() {
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
	public void alarmForSurvey() {
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
