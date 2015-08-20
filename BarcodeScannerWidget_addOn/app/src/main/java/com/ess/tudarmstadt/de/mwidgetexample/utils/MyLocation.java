package com.ess.tudarmstadt.de.mwidgetexample.utils;

import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;

/**
 * First of all I check what providers are enabled. Some may be disabled on
 * the device, some may be disabled in application manifest. If any provider
 * is available I start location listeners and timeout timer. It's 20
 * seconds in my example, may not be enough for GPS so you can enlarge it.
 * If I get update from location listener I use the provided value. I stop
 * listeners and timer. If I don't get any updates and timer elapses I have
 * to use last known values. I grab last known values from available
 * providers and choose the most recent of them.
 * 
 * @author Fedor (stackoverflow.com)
 * 
 */
public class MyLocation{
	private final Timer timer1 = new Timer();
	private LocationManager lm;
	private LocationResult locationResult;
	private boolean gps_enabled = false;
	private boolean network_enabled = false;
	

	public void getLocation(Context context, LocationResult result) {
		// I use LocationResult callback class to pass location value from
		// MyLocation to user code.
		locationResult = result;
		if (lm == null)
			lm = (LocationManager) context
					.getSystemService(Context.LOCATION_SERVICE);

		// exceptions will be thrown if provider is not permitted.
		try {
			gps_enabled = lm
					.isProviderEnabled(LocationManager.GPS_PROVIDER);
		} catch (Exception ex) {
		}
		try {
			network_enabled = lm
					.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
		} catch (Exception ex) {
		}

		// don't start listeners if no provider is enabled
		if (!gps_enabled && !network_enabled)
			return;

		if (gps_enabled)
			lm.requestSingleUpdate(LocationManager.GPS_PROVIDER,
					locationListenerGps, Looper.myLooper());
		// lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0,
		// locationListenerGps);
		if (network_enabled)
			lm.requestSingleUpdate(LocationManager.NETWORK_PROVIDER,
					locationListenerGps, Looper.myLooper());
		// lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0,
		// 0, locationListenerNetwork);
		int timeout = 70000;
		timer1.schedule(new GetLastLocation(), timeout);
	}

	public void stopMe(){
		timer1.cancel();
		lm.removeUpdates(locationListenerNetwork);
		lm.removeUpdates(locationListenerGps);
		
		
	}
	
	private final LocationListener locationListenerGps = new LocationListener() {
		public void onLocationChanged(Location location) {
			timer1.cancel();
			locationResult.gotLocation(location);
			lm.removeUpdates(this);
			lm.removeUpdates(locationListenerNetwork);
		}

		public void onProviderDisabled(String provider) {
		}

		public void onProviderEnabled(String provider) {
		}

		public void onStatusChanged(String provider, int status,
				Bundle extras) {
		}
	};

	private final LocationListener locationListenerNetwork = new LocationListener() {
		
		public void onLocationChanged(Location location) {
			timer1.cancel();
			locationResult.gotLocation(location);
			lm.removeUpdates(this);
			lm.removeUpdates(locationListenerGps);
		}

		public void onProviderDisabled(String provider) {
//			Toast.makeText(getApplicationContext(), "GPS disabled!",
//					Toast.LENGTH_SHORT).show();
		}

		public void onProviderEnabled(String provider) {
//			Toast.makeText(getApplicationContext(), "GPS enabled!",
//					Toast.LENGTH_SHORT).show();
		}

		public void onStatusChanged(String provider, int status,
				Bundle extras) {
//			Toast.makeText(getApplicationContext(), "GPS status changed!",
//					Toast.LENGTH_SHORT).show();
		}
	};

	private class GetLastLocation extends TimerTask {
		@Override
		public void run() {
			lm.removeUpdates(locationListenerGps);
			lm.removeUpdates(locationListenerNetwork);

			Location net_loc = null, gps_loc = null;
			if (gps_enabled)
				gps_loc = lm
						.getLastKnownLocation(LocationManager.GPS_PROVIDER);
			if (network_enabled)
				net_loc = lm
						.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

			// if there are both values use the latest one
			if (gps_loc != null && net_loc != null) {
				if (gps_loc.getTime() > net_loc.getTime())
					locationResult.gotLocation(gps_loc);
				else
					locationResult.gotLocation(net_loc);
				return;
			}

			if (gps_loc != null) {
				locationResult.gotLocation(gps_loc);
				return;
			}
			if (net_loc != null) {
				locationResult.gotLocation(net_loc);
				return;
			}
			locationResult.gotLocation(null);
		}
	}
	

	public static abstract class LocationResult {
		public abstract void gotLocation(Location location);
	}
}
