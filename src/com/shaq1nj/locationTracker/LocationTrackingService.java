package com.shaq1nj.locationTracker;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;

public class LocationTrackingService extends Service {

	private static String TAG = "LocationService";
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.i(TAG, "I am alive.");
		
		IntentFilter filter = new IntentFilter();
		filter.addAction("android.provider.Telephony.SMS_RECEIVED");
		registerReceiver(receiver, filter);
		
		
		return START_STICKY;
	}
	
	@Override
	public void onDestroy() {
		Log.i(TAG, "I am dead.");
		unregisterReceiver(receiver);
		super.onDestroy();
	}

	private final BroadcastReceiver receiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if(action.equals("android.provider.Telephony.SMS_RECEIVED"))
			{
				Log.v(TAG, "SMS Received.");
				
				Object messages[] = (Object[]) intent.getExtras().get("pdus");
				SmsMessage smsMessage[] = new SmsMessage[messages.length];
				
				for (int n = 0; n < messages.length; n++) 
				{
					smsMessage[n] = SmsMessage.createFromPdu((byte[]) messages[n]);
				}
				
				String messageBody = smsMessage[0].getMessageBody();
				
				SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
				String smsKey = prefs.getString("keySMS", "");
				
				Log.v(TAG, "SMS Key: " + smsKey);
				
				if (messageBody.equals(smsKey))
				{
					Location location = getLocation();
					String result = "";
					
					if (location != null)
					{
						result = "MAP: http://www.google.com/search?hl=en&source=hp&biw=1366&bih=642&q="
								+ location.getLatitude() + "%2C+" + location.getLongitude() + "&aq=f&aqi=&aql=&oq=";
					}
					else
					{
						result = "Location not found.";
					}
					
					
					sendSMS(result, smsMessage[0].getOriginatingAddress());
				}
			}   
		}
	};
	
	private Location getLocation()
	{
		Location location = null;
		
		try
		{
			location = LocationTrackingManager.getActiveInstance(getApplicationContext()).getCurrentLocation(120);
		}
		catch (Exception e)
		{
			Log.e(TAG, "Location not found..", e);
		}
		
		return location;
	}
	
	private void sendSMS(String message, String numberToSendTo)
	{
		SmsManager smsManager = SmsManager.getDefault();
		smsManager.sendTextMessage(numberToSendTo, null, message, null, null);
	}
}
