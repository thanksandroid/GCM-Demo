package com.thanksandroid.example.gcmdemo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public class NetworkHelper {
	// tag for logs
	public static final String TAG = NetworkHelper.class.getSimpleName();
	// Connection and read timeout values in milliseconds.
	public static final int CONNECTION_TIMEOUT = 10000;
	public static final int WAIT_RESPONSE_TIMEOUT = 30000;

	/**
	 * Executes an HTTP post request with form params.
	 * 
	 * @param serviceUrl
	 * @param keys
	 * @param values
	 * @param context
	 * @return
	 */
	public static String doPost(String serviceUrl, String[] keys,
			String[] values, Context context) {

		String response = "";

		if (!isOnline(context)) {
			// no network connection
			return "Network connection not available";
		}
		try {
			// set basic http params
			HttpParams httpParams = new BasicHttpParams();
			// set connection timeout
			HttpConnectionParams.setConnectionTimeout(httpParams,
					CONNECTION_TIMEOUT);
			// set socket timeout
			HttpConnectionParams
					.setSoTimeout(httpParams, WAIT_RESPONSE_TIMEOUT);
			// create http client object
			HttpClient httpClient = new DefaultHttpClient(httpParams);
			// build an HttpPost object
			HttpPost httpPost = new HttpPost(serviceUrl);

			// make a list of name value pairs
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
			for (int i = 0; i < keys.length; i++) {
				BasicNameValuePair nameValuePair = new BasicNameValuePair(
						keys[i], values[i]);
				nameValuePairs.add(nameValuePair);
			}
			// set name value pairs into a form encoded entity
			UrlEncodedFormEntity formEntity = new UrlEncodedFormEntity(
					nameValuePairs);

			// set the url encoded entity on http post and execute request
			httpPost.setEntity(formEntity);
			HttpResponse httpReposne = httpClient.execute(httpPost);

			// read response into a string buffer
			String line = null;
			StringBuffer stringBuffer = new StringBuffer();
			InputStreamReader streamReader = new InputStreamReader(httpReposne
					.getEntity().getContent());
			BufferedReader reader = new BufferedReader(streamReader);
			while ((line = reader.readLine()) != null) {
				stringBuffer.append(line);
			}
			reader.close();

			// set response data
			response = stringBuffer.toString();
		} catch (SocketTimeoutException ex) {
			response = "Request Timeout";
			Log.e(TAG, ex.getMessage(), ex);
		} catch (ConnectTimeoutException ex) {
			response = "Request Timeout";
			Log.e(TAG, ex.getMessage(), ex);
		} catch (ClientProtocolException ex) {
			response = "Protocol Error";
			Log.e(TAG, ex.getMessage(), ex);
		} catch (IOException ex) {
			response = "IO Error";
			Log.e(TAG, ex.getMessage(), ex);
		} catch (Exception ex) {
			response = "Request Error";
			Log.e(TAG, ex.getMessage(), ex);
		}
		return response;
	}

	/**
	 * Check data connection is available or not.
	 * 
	 * @param context
	 * @return
	 */
	public static boolean isOnline(Context context) {
		ConnectivityManager connectivity = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivity.getActiveNetworkInfo();
		if (activeNetworkInfo != null) {
			boolean networkAvailable = activeNetworkInfo.isAvailable();
			boolean networkConnected = activeNetworkInfo.isConnected();
			if (networkAvailable && networkConnected) {
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}
}
