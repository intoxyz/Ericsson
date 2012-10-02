package com.ericsson.contextpush;

import static com.ericsson.contextpush.CommonUtilities.SENDER_ID;

import com.google.android.gcm.*;

import android.app.*;
import android.content.*;
import android.os.*;
import android.util.*;

public class GCMIntentService extends GCMBaseIntentService {
	
	private String TAG = "** GCMIntentService **";
	public static String gcm_msg;
	public static String checkId;
    private static final int HELLO_ID = 1;
    Context _context;
	
    public GCMIntentService() {
		super(SENDER_ID);
	}

	private static PowerManager.WakeLock sWakeLock;
    private static final Object LOCK = GCMIntentService.class;

    static void runIntentInService(Context context, Intent intent) {

        synchronized(LOCK) {
            if (sWakeLock == null) {
               PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
               sWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "my_wakelock");
            }
        }

        sWakeLock.acquire();
        intent.setClassName(context, GCMIntentService.class.getName());
        context.startService(intent);
    }

	@Override
    protected void onMessage(Context context, Intent intent) {
    	Log.i(TAG, "CONTEXT : = " + context);
    	Log.i(TAG, "new message= ");
    	   if (intent.getAction().equals("com.google.android.c2dm.intent.RECEIVE")) {    
    		   gcm_msg = intent.getExtras().getString("msg");
	    	    GET_GCM(gcm_msg);
	    	    Log.i(TAG,gcm_msg);
    	   }
    	_context = context;
    }

    public void GET_GCM(String msg) { 
                
        //for notification
        String ns = Context.NOTIFICATION_SERVICE;
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(ns);
        
        int icon = R.drawable.ini;
        CharSequence tickerText = "Public Safety Alert";
        long when = System.currentTimeMillis();
        
        Notification notification = new Notification(icon,tickerText,when);
        //notification cleared after user select
        notification.flags |= Notification.FLAG_AUTO_CANCEL|Notification.FLAG_SHOW_LIGHTS;
        
        Context thiscontext = getApplicationContext();
        CharSequence contentTitle = "You've got the Alert Message";
        CharSequence contentText = "File Type "+ msg;
        
        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        notification.setLatestEventInfo(thiscontext, contentTitle, contentText, contentIntent);
        mNotificationManager.notify(HELLO_ID, notification);
        
        //
        Vibrator vib = (Vibrator) _context.getSystemService(Context.VIBRATOR_SERVICE);
        vib.vibrate(500);
        
    } 
    	
    /**Error Handling*/
    @Override
    protected void onError(Context context, String errorId) {
        Log.d(TAG, "onError. errorId : "+errorId);
    }
    /**GCM Registration**/
    @Override
    protected void onRegistered(Context context, String regId) {
    	Log.d(TAG, "onRegistered. regId : "+regId);
    	checkId = regId;
    }
    /**GCM Un-registration**/
    @Override
    protected void onUnregistered(Context context, String regId) {
        Log.d(TAG, "onUnregistered. regId : "+regId);
        checkId = null;
    }
}