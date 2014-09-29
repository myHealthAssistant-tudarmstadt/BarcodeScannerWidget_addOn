package com.ess.tudarmstadt.de.mwidgetexample.fragments;

import java.util.ArrayList;

import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
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

	private static String TAG = PhotoAlbumListFragment.class.getSimpleName();
	public static final String JSON_PARSER = "jsonParser";

	private mArrayAdapter mAdapter;

	private OnListItemClickListener mCallback;
	ListView listView;

	public interface OnListItemClickListener {
		public void onPhotoListItemClickListener(int id, String title,
				String content, double longitude, double latitude, String objUri);
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
		Log.e(TAG, "onCreate");

		ArrayList<JSONObject> mListObj = new ArrayList<JSONObject>();
		// get the list of objects from parent activity
		Bundle args = this.getArguments();
		if (args != null && args.containsKey(JSON_PARSER)) {
			JSONArrayParser jParser = args.getParcelable(JSON_PARSER);
			mListObj = jParser.getJsonArrayList();
		}

		mAdapter = new mArrayAdapter(this.getActivity(), this.getActivity()
				.getApplicationContext());
		mAdapter.addAll(mListObj);
		this.setListAdapter(mAdapter);

	}

	// @Override
	// public View onCreateView(LayoutInflater inflater, ViewGroup container,
	// Bundle savedInstanceState) {
	// View rootView = inflater.inflate(R.layout.photo_album_list_layout,
	// container, false);
	//
	// // this.setListAdapter(mAdapter);
	//
	// mAdapter = new mArrayAdapter(this.getActivity(), this.getActivity()
	// .getApplicationContext());
	// mAdapter.addAll(mListObj);
	//
	// listView = (ListView) rootView.findViewById(android.R.id.list);
	// listView.setAdapter(mAdapter);
	// listView.setOnItemClickListener(this);
	// return rootView;
	// }

	// @Override
	// public void onActivityCreated(Bundle savedInstanceState) {
	// super.onActivityCreated(savedInstanceState);
	//
	//
	// }

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		// return the object to its activity
		JSONObject key = mAdapter.getItem(position);
		double longitude = key.optDouble(Constants.JSON_OBJECT_LONGITUDE, -1);
		double latitude = key.optDouble(Constants.JSON_OBJECT_LATITUDE, -1);
		String title = key.optString(Constants.JSON_OBJECT_TITLE, "");
		String content = key.optString(Constants.JSON_OBJECT_CONTENT, "");
		String obj_uri = key.optString(Constants.JSON_OBJECT_URI, "");
		int obj_id = key.optInt(Constants.JSON_OBJECT_ID, -1);

		mCallback.onPhotoListItemClickListener(obj_id, title, content,
				longitude, latitude, obj_uri);
	}

	private class mArrayAdapter extends ArrayAdapter<JSONObject> {
		private LayoutInflater mInflater;

		public mArrayAdapter(Activity activity, Context ctx) {
			super(ctx, 0);
			mInflater = (LayoutInflater) LayoutInflater.from(activity);

		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			final View view;
			if (convertView == null) {
				view = mInflater.inflate(R.layout.list_row, parent, false);
			} else {
				view = convertView;
			}

			JSONObject key = getItem(position);

			ViewHolder holder = new ViewHolder();
			holder.title = (TextView) view.findViewById(R.id.stuff_title);
			holder.date = (TextView) view.findViewById(R.id.stuff_date);
			holder.time = (TextView) view.findViewById(R.id.stuff_time);
			holder.content = (TextView) view.findViewById(R.id.stuff_content);
			holder.location = (TextView) view.findViewById(R.id.stuff_location);
//			holder.image = (ImageView) view.findViewById(R.id.stuff_photo);

			holder.title.setText(key.optString(Constants.JSON_OBJECT_TITLE));
			holder.date.setText(key.optString(Constants.JSON_OBJECT_DATE));
			holder.time.setText(key.optString(Constants.JSON_OBJECT_TIME));
			holder.location.setText(key
					.optString(Constants.JSON_OBJECT_LOCATION));

			String content = key.optString(Constants.JSON_OBJECT_CONTENT)
					+ key.optString(Constants.JSON_OBJECT_URI);
			holder.content.setText(content);

			return view;
		}
	}

	private class ViewHolder {
		private TextView title;
		private TextView date;
		private TextView time;
		private TextView content;
		private TextView location;
//		private ImageView image;
	}
}
