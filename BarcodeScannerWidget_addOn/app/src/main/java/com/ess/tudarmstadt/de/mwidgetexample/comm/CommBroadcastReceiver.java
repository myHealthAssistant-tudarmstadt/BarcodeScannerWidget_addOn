package com.ess.tudarmstadt.de.mwidgetexample.comm;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.ess.tudarmstadt.de.mwidgetexample.utils.Constants;

import de.tudarmstadt.dvs.myhealthassistant.myhealthhub.events.AbstractChannel;
import de.tudarmstadt.dvs.myhealthassistant.myhealthhub.events.Event;
import de.tudarmstadt.dvs.myhealthassistant.myhealthhub.events.management.JSONDataExchange;
import de.tudarmstadt.dvs.myhealthassistant.myhealthhub.events.management.ManagementEvent;

/**
 * Send and Receive messages from myHealthHub
 * @author HieuHa
 *
 */
public class CommBroadcastReceiver extends BroadcastReceiver {
	private static final String TAG = CommBroadcastReceiver.class
			.getSimpleName();
	private int evtCounter = 0;
	private final Context context;
	private final JSONResult jResult;

	public CommBroadcastReceiver(Context context, JSONResult r) {
		this.context = context;
		this.jResult = r;
	}

	// Other Activity must implement this to get the entries delivered from myHealthHub
	public static abstract class JSONResult {
		public abstract void gotResult(JSONArray arrayR);
	}

	/**
	 * Send GET-Request over to myHealthHub to get all encoded JSONObject of
	 * scanned codes
	 */
	public void getJSONEntryList() {
		// send GetRequest over to myHealthHub through management channel
		evtCounter++;
		JSONObject jEncodedData = new JSONObject();

		try {
			// request to store encoded data to db
			jEncodedData.putOpt(JSONDataExchange.JSON_REQUEST,
					JSONDataExchange.JSON_GET);

			JSONDataExchange eData = new JSONDataExchange(TAG + evtCounter,
					getTimestamp(), TAG, context.getPackageName(),
					JSONDataExchange.EVENT_TYPE, jEncodedData.toString());

			// Publishes a management event to myHealthHub
			publishEvent(eData, AbstractChannel.MANAGEMENT);

		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Store scanned entry in database by sending encoded JSONObject to
	 * myHealthHub with STORE-Request
	 */
	public void storeEntry(String date, String time, String title,
			String contents, String location, double longitude, double latitude) {
		controlEntry(JSONDataExchange.JSON_STORE, -1, date, time, title,
				contents, location, longitude, latitude);
	}


	/**
	 * Edit scanned entry in database by sending encoded JSONObject to
	 * myHealthHub with Edit-Request
	 */
	public void editEntry(int id, String date, String time, String title,
			String contents, String location, double longitude, double latitude) {
		controlEntry(Constants.JSON_REQUEST_EDIT, id, date, time, title,
				contents, location, longitude, latitude);
	}

	/**
	 * Delete scanned entry in database by sending encoded JSONObject to
	 * myHealthHub with Delete-Request
	 */
	public void deleteEntry(int id) {
		controlEntry(Constants.JSON_REQUEST_DELETE, id, null, null, null,
				"notImportant!", null, 0.0, 0.0);
	}

	public void massDelEntries(JSONArray jObjArray) {
		evtCounter++;
		try {
			Log.e(TAG, "massDelete: " + jObjArray.length());
			JSONObject jEncodedData = new JSONObject();
			jEncodedData.putOpt(JSONDataExchange.JSON_REQUEST,
					Constants.JSON_REQUEST_DELETE);
			jEncodedData
					.putOpt(Constants.JSON_REQUEST_CONTENT_ARRAY, jObjArray); // fixme

			JSONDataExchange eData = new JSONDataExchange(TAG + evtCounter,
					getTimestamp(), TAG, context.getPackageName(),
					JSONDataExchange.EVENT_TYPE, jEncodedData.toString());

			// Publishes a management event to myHealthHub
			publishEvent(eData, AbstractChannel.MANAGEMENT);
		} catch (JSONException ignored) {

		}

	}

	private void controlEntry(String jsonRequest, int id, String date,
			String time, String title, String contents, String location,
			double longitude, double latitude) {
		Log.e(TAG, "broadcast request: " + jsonRequest);
		if (contents == null || contents.isEmpty()) {
			Toast.makeText(context, "empty code", Toast.LENGTH_LONG).show();
			return;
		}
		evtCounter++;
		JSONArray jObjArray = new JSONArray();
		JSONObject jEncodedData = new JSONObject();
		try {
			JSONObject jObj = new JSONObject();

			jObj.putOpt(Constants.JSON_OBJECT_ID, id);
			jObj.putOpt(Constants.JSON_OBJECT_CONTENT, contents);
			jObj.putOpt(Constants.JSON_OBJECT_TITLE, title);
			jObj.putOpt(Constants.JSON_OBJECT_DATE, date);
			jObj.putOpt(Constants.JSON_OBJECT_TIME, time);
			jObj.putOpt(Constants.JSON_OBJECT_LONGITUDE, longitude);
			jObj.putOpt(Constants.JSON_OBJECT_LATITUDE, latitude);
			jObj.putOpt(Constants.JSON_OBJECT_LOCATION, location);

			jObjArray.put(jObj);
			// request to store encoded data as a json Array to db
			jEncodedData.putOpt(JSONDataExchange.JSON_REQUEST, jsonRequest);
			jEncodedData
					.putOpt(Constants.JSON_REQUEST_CONTENT_ARRAY, jObjArray); // fixme

			JSONDataExchange eData = new JSONDataExchange(TAG + evtCounter,
					getTimestamp(), TAG, context.getPackageName(),
					JSONDataExchange.EVENT_TYPE, jEncodedData.toString());

			// Publishes a management event to myHealthHub
			publishEvent(eData, AbstractChannel.MANAGEMENT);
			//
			// Toast.makeText(context, "save successful!",
			// Toast.LENGTH_LONG).show();

		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Store encoded JSONObject in database by sending JSONObject to myHealthHub
	 * with STORE-Request
	 */
	public void storeEntry(JSONObject jObj) {
		messageContrll(JSONDataExchange.JSON_STORE, jObj);
	}

	/**
	 * Edit scanned entry in database by sending encoded JSONObject to
	 * myHealthHub with Edit-Request
	 */
	public void editEntry(JSONObject jObj) {
		messageContrll(Constants.JSON_REQUEST_EDIT, jObj);
	}

	private void messageContrll(String jsonRequest, JSONObject jObj) {
		Log.e(TAG, "broadcast request: " + jsonRequest);

		evtCounter++;
		JSONArray jObjArray = new JSONArray();
		JSONObject jEncodedData = new JSONObject();
		try {
			jObjArray.put(jObj);
			// request to store encoded data as a json Array to db
			jEncodedData.putOpt(JSONDataExchange.JSON_REQUEST, jsonRequest);
			jEncodedData
					.putOpt(Constants.JSON_REQUEST_CONTENT_ARRAY, jObjArray); // fixme

			JSONDataExchange eData = new JSONDataExchange(TAG + evtCounter,
					getTimestamp(), TAG, context.getPackageName(),
					JSONDataExchange.EVENT_TYPE, jEncodedData.toString());

			// Publishes a management event to myHealthHub
			publishEvent(eData, AbstractChannel.MANAGEMENT);

			Toast.makeText(context, "save successful!", Toast.LENGTH_SHORT)
					.show();

		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Publishes an event on a specific myHealthHub channel.
	 * 
	 * @param event
	 *            that shall be published.
	 * @param channel
	 *            on which the event shall be published.
	 */
	private void publishEvent(Event event, String channel) {
		Intent i = new Intent();
		// add event
		i.putExtra(Event.PARCELABLE_EXTRA_EVENT_TYPE, event.getEventType());
		i.putExtra(Event.PARCELABLE_EXTRA_EVENT, event);

		// set channel as Management
		i.setAction(channel);

		// set receiver package
		i.setPackage("de.tudarmstadt.dvs.myhealthassistant.myhealthhub");

		// sent intent
		context.sendBroadcast(i);
	}

	@Override
	public void onReceive(Context arg0, Intent intent) {
		if (intent == null)
			return;

		Event evt = intent.getParcelableExtra(Event.PARCELABLE_EXTRA_EVENT);
		// String type = evt.getEventType();
		// Log.e(TAG, type);
		// JSONDataExchange
		if (evt.getEventType().equals(ManagementEvent.JSON_DATA_EXCHANGE)) {
			Log.e(TAG, "JSON Data Exchange event from: " + evt.getProducerID());

			String jsonDataString = ((JSONDataExchange) evt)
					.getJSONEncodedData();

			try {
				JSONObject jsonData = new JSONObject(jsonDataString);
				String json_request = jsonData.optString(
						JSONDataExchange.JSON_REQUEST, "null");
				JSONArray jObjArray = jsonData
						.optJSONArray(Constants.JSON_REQUEST_CONTENT_ARRAY);

				if (json_request.equalsIgnoreCase(JSONDataExchange.JSON_GET)) {
					// received jsonEncodeData from myHealthHub
					if (jObjArray != null) {

						Constants.logDebug(TAG,
								"nr Obj Get:" + jObjArray.length());
						// Log.e(TAG, "JSON encoded data " +
						// jObjArray.toString());
						//
						// String text = "";
						// String contents = jObj.optString(
						// JSONDataExchange.JSON_CONTENTS, "null");
						// String contentDate = jObj.optString(
						// JSONDataExchange.JSON_DATE, "null");
						// String contentExtra = jObj.optString(
						// JSONDataExchange.JSON_EXTRA, "############");
						// text += contentDate + ": " + contents + "\n" +
						// contentExtra + "\n";
						// Log.e(TAG, "date:" + contentDate + "; content:"
						// + contents + contentExtra);
						// }
						// }
						// TODO
						jResult.gotResult(jObjArray);
					}
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Returns the current time
	 * 
	 * @return timestamp
	 */
	private String getTimestamp() {
		return (String) android.text.format.DateFormat.format(
				"yyyy-MM-dd\nkk:mm:ss", new java.util.Date());
	}
}
