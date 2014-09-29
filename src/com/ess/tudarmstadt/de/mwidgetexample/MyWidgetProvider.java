package com.ess.tudarmstadt.de.mwidgetexample;

import com.ess.tudarmstadt.de.mwidgetexample.R;

import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

/**
 * Create the Widget for Home Screen
 * @author HieuHa
 *
 */
public class MyWidgetProvider extends AppWidgetProvider {

	private static final String TAG = MyWidgetProvider.class.getSimpleName();

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {

		// To prevent any ANR (Application Not Responding) timeouts, we perform
		// the update in a service
		 context.startService(new Intent(context, UpdateWidgetService.class));

	}

	public static class UpdateWidgetService extends Service {
		@Override
		public void onStart(Intent intent, int startId) {
			Log.d(TAG, "onStart()");

			// Build the widget update for today
			RemoteViews updateViews = buildUpdate(this);
			
			// Push update for this widget to the home screen
			ComponentName thisWidget = new ComponentName(this,
					MyWidgetProvider.class);
			AppWidgetManager manager = AppWidgetManager.getInstance(this);
			manager.updateAppWidget(thisWidget, updateViews);

			Toast.makeText(getApplicationContext(), "Widget updated!",
					Toast.LENGTH_LONG).show();

			stopSelf();
		}

		@Override
		public IBinder onBind(Intent intent) {
			return null;
		}

		/**
		 * Build a widget update
		 */
		public RemoteViews buildUpdate(Context context) {

			RemoteViews views = null;

			// Build an update that holds the updated widget contents
			views = new RemoteViews(context.getPackageName(),
					R.layout.widget_layout);

			// Create an Intent to launch PhotoLaunchActivity
			Intent intent = new Intent(context, MainActivity.class);
			PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
					intent, 0);
			views.setOnClickPendingIntent(R.id.wgt_cam_cap_btn, pendingIntent);

			views.setTextViewText(R.id.scan_info, "BarCode Scanner's ready!");

			return views;
		}
	}
}
