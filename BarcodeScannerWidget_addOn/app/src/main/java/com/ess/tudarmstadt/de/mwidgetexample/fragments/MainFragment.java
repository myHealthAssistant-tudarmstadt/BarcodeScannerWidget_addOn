package com.ess.tudarmstadt.de.mwidgetexample.fragments;

import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.ess.tudarmstadt.de.mwidgetexample.R;

/**
 * Showing all the Buttons that user interact at main screen
 * @author HieuHa
 *
 */
public class MainFragment extends Fragment {
	private OnButtonClickListener mCallback;

	public static final int BARCODE_CAPTURE_TOKEN = 2;
	public static final int PHOTO_CAPTURE_TOKEN = 1;
	public static final int SHOW_ALBUM_TOKEN = 0;
	public static final int SHOW_SURVEY_TOKEN = 4;

	/** make sure the buttons being clicked only once**/
	private boolean buttonFreeze = false;
	
	// Container Activity must implement this interface
	public interface OnButtonClickListener {
		void onButtonClickListener(int token);
	}

	public MainFragment() {
	}
	
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        
        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mCallback = (OnButtonClickListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnButtonClickListener");
        }
    }


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragmen_main, container,
				false);

		buttonFreeze = false;
		Button cap_image = (Button) rootView.findViewById(R.id.main_cap_photo);
		cap_image.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				invokeCallback(PHOTO_CAPTURE_TOKEN);
			}
		});

		Button cap_barcode = (Button) rootView
				.findViewById(R.id.main_cap_barcode);
		cap_barcode.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				invokeCallback(BARCODE_CAPTURE_TOKEN);
			}
		});

		TextView verInfo = (TextView) rootView.findViewById(R.id.version_nme);
		verInfo.setText(getVersionInfo());

		return rootView;
	}
	
	@Override
    public void onDestroyView() {
        super.onDestroyView();
        buttonFreeze = false;
        if (this.getView() != null) {
            ViewGroup parentViewGroup = (ViewGroup) this.getView().getParent();
            if (parentViewGroup != null) {
                parentViewGroup.removeAllViews();
            }
        }
    }
	
	/** Version name of this Application **/
	private String getVersionInfo() {
		String strVersion = "Version:";
		PackageInfo packageInfo;
		try {
			packageInfo = this
					.getActivity()
					.getApplicationContext()
					.getPackageManager()
					.getPackageInfo(
							this.getActivity().getApplicationContext()
									.getPackageName(), 0);
			strVersion += packageInfo.versionName;
		} catch (NameNotFoundException e) {
			strVersion += "Unknown";
		}

		return strVersion;
	}

	/** back to parent activity **/
	private void invokeCallback(int token) {
		if (!buttonFreeze){
			buttonFreeze = true;
			mCallback.onButtonClickListener(token);
		}
	}
}
