package com.ess.tudarmstadt.de.mwidgetexample.fragments;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.ess.tudarmstadt.de.mwidgetexample.R;
import com.ess.tudarmstadt.de.mwidgetexample.utils.Constants;
import com.ess.tudarmstadt.de.mwidgetexample.utils.JSONArrayParser;

/**
 * Showing the list of all Entries (bar-code and photo) that taken
 * @author HieuHa
 *
 */
public class PhotoAlbumListFragment extends ListFragment {

	public static final String JSON_PARSER = "jsonParser";

	private mArrayAdapter mAdapter;

	// false when albumListFragment, true when SurveyFragment.
	private int mode;

	private OnListItemClickListener mCallback;

	public interface OnListItemClickListener {
		void onPhotoListItemClickListener(int id, String title,
										  String content, double longitude, double latitude, String objUri, int amount);
		void onSurveyListItemClickListener(String time, String date, int survey, JSONArray results);
	}

	public PhotoAlbumListFragment() {
		// empty constructor
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		// This makes sure that the container activity has implemented
		// the callback interface. If not, it throws an exception
		try {
			mCallback = (OnListItemClickListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement OnListItemClickListener");
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		ArrayList<JSONObject> mListObj = new ArrayList<>();
		// get the list of objects from parent activity
		Bundle args = this.getArguments();
		if (args != null && args.containsKey(JSON_PARSER) && args.containsKey("mode")) {
			JSONArrayParser jParser = args.getParcelable(JSON_PARSER);
			mListObj = jParser.getJsonArrayList();
			mode = args.getInt("mode");
		}

		mAdapter = new mArrayAdapter(this.getActivity(), this.getActivity()
				.getApplicationContext());
		mAdapter.addAll(mListObj);
		this.setListAdapter(mAdapter);

	}


	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		// return the object to its activity
		JSONObject key = mAdapter.getItem(position);
		if (mode == 0) {
			double longitude = key.optDouble(Constants.JSON_OBJECT_LONGITUDE, -1);
			double latitude = key.optDouble(Constants.JSON_OBJECT_LATITUDE, -1);
			String title = key.optString(Constants.JSON_OBJECT_TITLE, "");
			String content = key.optString(Constants.JSON_OBJECT_CONTENT, "");
			String obj_uri = key.optString(Constants.JSON_OBJECT_URI, "");
			int obj_id = key.optInt(Constants.JSON_OBJECT_ID, -1);
			int amount = key.optInt(Constants.JSON_OBJECT_AMOUNT);

			mCallback.onPhotoListItemClickListener(obj_id, title, content,
					longitude, latitude, obj_uri, amount);
		} else {
			String time = key.optString(Constants.JSON_OBJECT_SURVEY_TIME, "");
			String date = key.optString(Constants.JSON_OBJECT_SURVEY_DATE, "");
			int survey = key.optInt(Constants.JSON_OBJECT_SURVEY_SURVEY, -1);
			JSONArray results = key.optJSONArray(Constants.JSON_OBJECT_SURVEY_RESULT);

			mCallback.onSurveyListItemClickListener(time, date, survey, results);
		}
	}

	private class mArrayAdapter extends ArrayAdapter<JSONObject> {
		private final LayoutInflater mInflater;

		public mArrayAdapter(Activity activity, Context ctx) {
			super(ctx, 0);
			mInflater = LayoutInflater.from(activity);

		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			final View view;
			if (convertView == null) {
				if(mode == 0)
					view = mInflater.inflate(R.layout.list_row, parent, false);
				else
					view = mInflater.inflate(R.layout.survey_item, parent, false);
			} else {
				view = convertView;
			}

			JSONObject key = getItem(position);
			if (mode == 0) {
				ViewHolder holder = new ViewHolder();
				holder.title = (TextView) view.findViewById(R.id.stuff_title);
				holder.date = (TextView) view.findViewById(R.id.stuff_date);
				holder.time = (TextView) view.findViewById(R.id.stuff_time);
				holder.content = (TextView) view.findViewById(R.id.stuff_content);
				holder.location = (TextView) view.findViewById(R.id.stuff_location);

				holder.date.setText(key.optString(Constants.JSON_OBJECT_DATE));
				holder.time.setText(key.optString(Constants.JSON_OBJECT_TIME));
				holder.location.setText(key
						.optString(Constants.JSON_OBJECT_LOCATION));

				String content = key.optString(Constants.JSON_OBJECT_CONTENT)
						+ key.optString(Constants.JSON_OBJECT_URI);
				holder.content.setText(content);
				int amount = key.optInt(Constants.JSON_OBJECT_AMOUNT);
				holder.title.setText(key.optString(Constants.JSON_OBJECT_TITLE) + " - " + amount + "ml");
			} else {
				ViewHolder holder = new ViewHolder();
				holder.title = (TextView) view.findViewById(R.id.stuff_title);
				holder.date = (TextView) view.findViewById(R.id.stuff_date);
				holder.time = (TextView) view.findViewById(R.id.stuff_time);

				holder.title.setText("Fragebogen: " + key.optString(Constants.JSON_OBJECT_SURVEY_SURVEY));
				holder.date.setText(key.optString(Constants.JSON_OBJECT_SURVEY_DATE));
				holder.time.setText(key.optString(Constants.JSON_OBJECT_SURVEY_TIME));
			}
			return view;
		}
	}

	private class ViewHolder {
		private TextView title;
		private TextView date;
		private TextView time;
		private TextView content;
		private TextView location;
	}
}
