package com.theark.alert;

import com.theark.chathead.HeadService;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.provider.ContactsContract.PhoneLookup;
import android.sax.StartElementListener;
import android.telephony.gsm.SmsMessage;
import android.widget.Toast;

public class ReadSMS extends BroadcastReceiver {

	String msg_from;
	Double lati=0.0,longi=0.0;
	@SuppressLint("ShowToast") @Override
	public void onReceive(Context arg0, Intent intent) {

        if(intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")){
            Bundle bundle = intent.getExtras();           //---get the SMS message passed in---
            SmsMessage[] msgs = null;
            
            if (bundle != null){
                //---retrieve the SMS message received---
                try{
                    Object[] pdus = (Object[]) bundle.get("pdus");
                    msgs = new SmsMessage[pdus.length];
                    for(int i=0; i<msgs.length; i++){
                        msgs[i] = SmsMessage.createFromPdu((byte[])pdus[i]);
                        msg_from = msgs[i].getOriginatingAddress();
                        String msgBody = msgs[i].getMessageBody();
                        
                        if(msgBody.contains("Emergency_:")||
                        		msgBody.contains("MeetMe_:")||
                        		msgBody.contains("FollowMe_:")){
                        	Toast.makeText(arg0, msgBody, Toast.LENGTH_LONG).show();
                            
                        	 AlarmManager am = (AlarmManager) arg0.getSystemService(Context.ALARM_SERVICE);
                        	 
                            Vibrator vib = (Vibrator) arg0.getSystemService(Context.VIBRATOR_SERVICE);  
            			     	vib.vibrate(8000);
            			     	startHeadService(arg0);
            			     	if(msgBody.contains("Lat:") || msgBody.contains("long:")){
            			     	try {
									String[] tem = msgBody.split("Lat:");
									lati = Double.parseDouble(tem[1].substring(0, 9));
									String[] tem1 = msgBody.split("long:");
									longi = Double.parseDouble(tem1[1].substring(1, 9));
									Uri uri = Uri.parse("geo:" + lati  + "," + longi +"?z=10");
									Toast.makeText(arg0, "lat->"+lati+"\nlong->"+longi, 0).show();
									Intent mapintent = new Intent(android.content.Intent.ACTION_VIEW, uri);
									mapintent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
									arg0.startActivity(mapintent);
								} catch (Exception e) {
									e.printStackTrace();
								}
            			     	}
                             
                        }
                        ///////
          /*              double latitude = 40.714728;
                        double longitude = -73.998672;
                        String uri = "geo:"+ latitude + "," + longitude + "?q=my+street+address";
                        arg0.startActivity(new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(uri)));
*/
                        //////
                        
                    }
                }catch(Exception e){
//                            Log.d("Exception caught",e.getMessage());
                }
            }
        }
	}
	
	private void startHeadService(Context c) {
        //Context context = getActivity();
		MediaPlayer mediaPlayer = MediaPlayer.create(c, R.raw.aud);
		mediaPlayer.start();
		SharedPreferences sp = c.getSharedPreferences("sms", 0);
		Editor ed = sp.edit();
		ed.putString("sms_name", getContactName(c, getContactName(c, msg_from)));
		ed.putString("sms_lat", ""+lati);
		ed.putString("sms_long", ""+longi);
		ed.putString("sms_num", ""+msg_from);
		ed.commit();
		Intent hi = new Intent(c, com.theark.chathead.HeadService.class);
		c.startService(hi);
    }
	
	
	public static String getContactName(Context context, String phoneNumber) {
	    ContentResolver cr = context.getContentResolver();
	    Uri uri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
	    Cursor cursor = cr.query(uri, new String[]{PhoneLookup.DISPLAY_NAME}, null, null, null);
	    if (cursor == null) {
	        return null;
	    }
	    String contactName = null;
	    if(cursor.moveToFirst()) {
	        contactName = cursor.getString(cursor.getColumnIndex(PhoneLookup.DISPLAY_NAME));
	    }

	    if(cursor != null && !cursor.isClosed()) {
	        cursor.close();
	    }

	    return contactName;
	}

}
