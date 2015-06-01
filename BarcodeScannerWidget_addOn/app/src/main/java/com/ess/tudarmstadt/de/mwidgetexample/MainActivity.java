package com.ess.tudarmstadt.de.mwidgetexample;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
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
import android.view.MenuItem;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.ess.tudarmstadt.de.mwidgetexample.comm.CommBroadcastReceiver;
import com.ess.tudarmstadt.de.mwidgetexample.comm.CommBroadcastReceiver.JSONResult;
import com.ess.tudarmstadt.de.mwidgetexample.fragments.AmountFragment;
import com.ess.tudarmstadt.de.mwidgetexample.fragments.MainFragment;
import com.ess.tudarmstadt.de.mwidgetexample.fragments.PhotoAlbumListFragment;
import com.ess.tudarmstadt.de.mwidgetexample.fragments.TitleEditorFragment;
import com.ess.tudarmstadt.de.mwidgetexample.utils.Constants;
import com.ess.tudarmstadt.de.mwidgetexample.utils.JSONArrayParser;
import com.google.zxing.client.android.CaptureActivity;

import de.tudarmstadt.dvs.myhealthassistant.myhealthhub.IMyHealthHubRemoteService;
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
		OnBackStackChangedListener {
	private static final String TAG = MainActivity.class.getSimpleName();
	private ProgressDialog progressDialog;

	private static final int ScanReqCode = 9000;
	private static final int CameraPhotoReqCode = 7000;

	private CommBroadcastReceiver commUnit;
	private String photo_OutputFileUri = "";

	/** setting up the connection with myHealthHub */
	private void connectToMhh() {
		if (!isConnectedToMhh) {
			progressDialog = new ProgressDialog(this);
			progressDialog.setTitle("Connect to myHealthHub");
			progressDialog.setMessage("Loading...");
			progressDialog.setCancelable(false);
			progressDialog.setIndeterminate(true);
			progressDialog.show();

			myHealthHubIntent = new Intent(
					IMyHealthHubRemoteService.class.getName());
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
			ArrayList<JSONObject> mListObj = new ArrayList<JSONObject>();
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

		// to exchange data with myHealthHub
		commUnit = new CommBroadcastReceiver(this.getApplicationContext(),
				jResult);
		this.getApplicationContext().registerReceiver(commUnit,
                new IntentFilter(AbstractChannel.MANAGEMENT));

		if (savedInstanceState == null) {
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
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
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
					String.format(getTimestamp("yyyyMMddkkmm") + ".jpg"));
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

		}
	}


    @Override
    public void onAmountCallbackListener(final JSONObject key) {
        if (commUnit != null) {
            if (progressDialog != null)
                progressDialog.show();

            commUnit.storeEntry(key);
            updateAppWidget(key);

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
				final JSONObject key = new JSONObject();
				addNewBarcodeItem(-1, "", "", -1, -1, photo_OutputFileUri, -1);
				/*try {
					key.putOpt(Constants.JSON_OBJECT_ID, -1);
					key.putOpt(Constants.JSON_OBJECT_TITLE, "");
					key.putOpt(Constants.JSON_OBJECT_CONTENT, "");
					key.putOpt(Constants.JSON_OBJECT_DATE, obj_date);
					key.putOpt(Constants.JSON_OBJECT_TIME, obj_time);
					key.putOpt(Constants.JSON_OBJECT_LONGITUDE, "");
					key.putOpt(Constants.JSON_OBJECT_LATITUDE, "");
					key.putOpt(Constants.JSON_OBJECT_LOCATION, "");
					key.putOpt(Constants.JSON_OBJECT_URI, photo_OutputFileUri);

					Log.e(TAG, key.toString());

					if (progressDialog != null)
						progressDialog.show();
					if (commUnit != null) {
						commUnit.storeEntry(key);
						updateAppWidget(key);
					}
					photo_OutputFileUri = "";
					
					Handler mHandler = new Handler();
					mHandler.postDelayed(new Runnable() {

						@Override
						public void run() {
							if (progressDialog != null)
								progressDialog.dismiss();
							finish();
						}
					}, 8000);
				} catch (JSONException e) {
					Constants.logDebug(TAG, e.getMessage());
					e.printStackTrace();
				} */
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
		if (contents != null && date != null) {
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

}
