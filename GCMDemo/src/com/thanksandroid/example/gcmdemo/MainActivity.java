package com.thanksandroid.example.gcmdemo;

import java.io.IOException;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

public class MainActivity extends Activity {

	private final String PREF_GCM_REG_ID = "PREF_GCM_REG_ID";
	private final String PREF_APP_VERSION = "PREF_APP_VERSION";

	private final String TAG = "GCM Demo Activity";
	private final int ACTION_PLAY_SERVICES_DIALOG = 100;

	private GoogleCloudMessaging gcm;
	private SharedPreferences prefs;
	private String gcmRegId;

	private int currentAppVersion;

	private TextView tvStatus;
	private EditText etMessage;
	private Button btnSend;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		tvStatus = (TextView) findViewById(R.id.tv_status);
		etMessage = (EditText) findViewById(R.id.et_msg);
		btnSend = (Button) findViewById(R.id.btn_send);

		// Check device for Play Services APK.
		if (checkPlayServices()) {
			gcm = GoogleCloudMessaging.getInstance(this);

			// Read saved registration id from shared preferences.
			gcmRegId = getSharedPreferences().getString(PREF_GCM_REG_ID, "");

			// Read saved app version
			int savedAppVersion = getSharedPreferences().getInt(
					PREF_APP_VERSION, Integer.MIN_VALUE);

			// Get current Application version
			currentAppVersion = getAppVersion(getApplicationContext());

			// register if saved registration id not found
			if (gcmRegId.isEmpty() || savedAppVersion != currentAppVersion) {
				registerInBackground();
			} else {
				tvStatus.setText("Got GCM Registartion Id from Shared Preferences.");
				etMessage.setVisibility(View.VISIBLE);
				btnSend.setVisibility(View.VISIBLE);
			}
		} else {
			tvStatus.setText("Google Play Services not available");
		}
	}

	/**
	 * Check the device to make sure it has the Google Play Services APK. If it
	 * doesn't, display a dialog that allows users to download the APK from the
	 * Google Play Store or enable it in the device's system settings.
	 */
	private boolean checkPlayServices() {
		int resultCode = GooglePlayServicesUtil
				.isGooglePlayServicesAvailable(this);
		if (resultCode != ConnectionResult.SUCCESS) {
			if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
				GooglePlayServicesUtil.getErrorDialog(resultCode, this,
						ACTION_PLAY_SERVICES_DIALOG).show();
			} else {
				Log.i(TAG, "This device is not supported.");
				finish();
			}
			return false;
		}
		return true;
	}

	/**
	 * Registers the application with GCM servers asynchronously.
	 * <p>
	 * Stores the registration ID and app versionCode in the application's
	 * shared preferences.
	 */
	private void registerInBackground() {
		new RegistrationTask().execute(null, null, null);
	}

	// Async task to register in background
	private class RegistrationTask extends AsyncTask<Void, Void, String> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			tvStatus.setText("Registering for GCM..");
		}

		@Override
		protected String doInBackground(Void... params) {
			String msg = "";
			try {
				if (gcm == null) {
					gcm = GoogleCloudMessaging
							.getInstance(getApplicationContext());
				}
				gcmRegId = gcm.register(Constants.GCM_SENDER_ID);

				/*
				 * At this point, you have gcm registration id. Save this
				 * registration id in your users table against this user. This
				 * id will be used to send push notifications from server.
				 */

				msg = "Successfully Registered for GCM";

			} catch (IOException ex) {
				ex.printStackTrace();
				msg = "Error while registering for GCM.";
			}
			return msg;
		}

		@Override
		protected void onPostExecute(String msg) {
			tvStatus.setText(msg);
			if (!gcmRegId.isEmpty()) {
				etMessage.setVisibility(View.VISIBLE);
				btnSend.setVisibility(View.VISIBLE);

				// save gcm reg id and registered app version in shared
				// preferences
				Editor editor = getSharedPreferences().edit();
				editor.putString(PREF_GCM_REG_ID, gcmRegId);
				editor.putInt(PREF_APP_VERSION, currentAppVersion);
				editor.commit();
			}

		}
	}

	// Async task to send massage in background
	private class MessageTask extends AsyncTask<String, Void, String> {

		String url = "http://api.thanksandroid.com/gcmdemo/post-data.php";

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			tvStatus.setText("Sending message...");
		}

		@Override
		protected String doInBackground(String... params) {
			String msg = params[0];
			String keys[] = new String[] { "message", "regid" };
			String values[] = new String[] { msg, gcmRegId };

			return NetworkHelper.doPost(url, keys, values,
					getApplicationContext());
		}

		@Override
		protected void onPostExecute(String msg) {
			tvStatus.setText(msg);
		}
	}

	private SharedPreferences getSharedPreferences() {
		if (prefs == null) {
			prefs = getApplicationContext().getSharedPreferences("GcmDemoApp",
					Context.MODE_PRIVATE);
		}
		return prefs;
	}

	/**
	 * @return Application's version code from the {@code PackageManager}.
	 */
	private static int getAppVersion(Context context) {
		try {
			PackageInfo packageInfo = context.getPackageManager()
					.getPackageInfo(context.getPackageName(), 0);
			return packageInfo.versionCode;
		} catch (NameNotFoundException e) {
			// should never happen
			throw new RuntimeException("Could not get package name: " + e);
		}
	}

	public void sendMessage(View view) {
		String msg = etMessage.getText().toString().trim();
		if (msg.isEmpty()) {
			msg = "Your message is blank";
		}

		new MessageTask().execute(msg);
	}

}
