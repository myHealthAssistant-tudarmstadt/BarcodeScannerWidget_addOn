package com.ess.tudarmstadt.de.mwidgetexample.utils;

import java.util.ArrayList;

import org.json.JSONObject;

import android.os.Parcel;
import android.os.Parcelable;

public class JSONArrayParser implements Parcelable {
	
	private ArrayList<JSONObject> jsonArrayList;
	
	public JSONArrayParser(){
		// Empty constructor
	}

	private JSONArrayParser(Parcel in) {
	}

	public static final Creator<JSONArrayParser> CREATOR = new Creator<JSONArrayParser>() {
		@Override
		public JSONArrayParser createFromParcel(Parcel in) {
			return new JSONArrayParser(in);
		}

		@Override
		public JSONArrayParser[] newArray(int size) {
			return new JSONArrayParser[size];
		}
	};

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		// TODO Auto-generated method stub

	}

	public ArrayList<JSONObject> getJsonArrayList() {
		return jsonArrayList;
	}

	public void setJsonArrayList(ArrayList<JSONObject> jsonArrayList) {
		this.jsonArrayList = jsonArrayList;
	}

}
