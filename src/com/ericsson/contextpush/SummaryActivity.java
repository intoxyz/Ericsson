package com.ericsson.contextpush;

import static com.ericsson.contextpush.CommonUtilities.SENDER_ID;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

import com.ericsson.contextpush.R;
import com.google.android.gcm.GCMRegistrar;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class SummaryActivity extends Activity{
	private String LOG_TAG = "** Mainaivity Regista **";
	private EditText mEmailField;
	private String msgfromURL;
	private ProgressDialog mProgressDialog;
	private Button subBtn, unsubBtn;
	private Handler mHandler;
	private String providedEmailAdd, providedPhoneNum;
	private TextView mDisplay,mtextStart;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.summaryactivity);
        
        findViewById(R.id.Subscription).setOnClickListener(mClickListener);
        findViewById(R.id.UnSubscription).setOnClickListener(mClickListener);

        subBtn = (Button)findViewById(R.id.Subscription);
        unsubBtn = (Button)findViewById(R.id.UnSubscription);
        mEmailField = (EditText)findViewById(R.id.emailAdd);
        mDisplay = (TextView)findViewById(R.id.displayView);
        mtextStart = (TextView)findViewById(R.id.explanationView);
        
        msgfromURL = com.ericsson.contextpush.GCMIntentService.gcm_msg;
        providedEmailAdd = com.ericsson.contextpush.MainActivity.setEmailAdd;
        providedPhoneNum = com.ericsson.contextpush.MainActivity.setPhoneNum;
    	
        checkNotNull(SENDER_ID, "SENDER_ID");
        
        mEmailField.setText(providedEmailAdd);
        

        //Will cause an error if the device doesn't support Google API 
        GCMRegistrar.checkDevice(getApplicationContext());
        GCMRegistrar.checkManifest(getApplicationContext());
        
        final String regId = GCMRegistrar.getRegistrationId(this);
        
        if (regId.equals("")) {
        	//only Sub button enabled
        	unsubBtn.setEnabled(false);
        	subBtn.setEnabled(true);	
        	unsubBtn.setVisibility(View.INVISIBLE);
        	mDisplay.setText("You have to subscribe first to receive the Alert Push");
        	mtextStart.setText("Review your Email and Policies have selected");
        }
        else{
        	//only UnSub button enabled  
        	if(msgfromURL == null){
        		mtextStart.setText("Wait for Push Notification");	
        	}
        	
			mEmailField.setEnabled(false);
        	unsubBtn.setEnabled(true);
        	subBtn.setEnabled(false);
        	
        	if(sendnstoreID(regId)){
        		Log.d(LOG_TAG, "Send regId successfully");
        	}
        	else{
        		Log.d(LOG_TAG, "Send regId error");
        	}
        }
        
    }
    
    Button.OnClickListener mClickListener = new View.OnClickListener() {
    	
		public void onClick(View v) {
						
			switch (v.getId()){
				case R.id.Subscription:
					getRegID();
					mEmailField.setEnabled(false);			
					subBtn.setVisibility(View.INVISIBLE);
					unsubBtn.setVisibility(View.VISIBLE);
					
					infoStore();
					break;
				case R.id.UnSubscription:
					dismissRedID();
					subBtn.setVisibility(View.VISIBLE);
					unsubBtn.setVisibility(View.INVISIBLE);
					mEmailField.setEnabled(true);
					break;					
			}
		}
	};
	
	private void infoStore(){
		
        String sdPath, fileName;
        
		//Check status of SD CARD
        String externalState = Environment.getExternalStorageState();
        if(externalState.equals(Environment.MEDIA_MOUNTED)){
        //SD CARD READY(mounted)
        	sdPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        }else{
        //SD CARD NOT READY(unmounted)
        	sdPath=Environment.MEDIA_UNMOUNTED;
        }
        
        fileName = "/userinfo.txt";
		
        File file = new File(sdPath+fileName);
        String emailAdd = (String)mEmailField.getText().toString();
        
        String contents = providedPhoneNum+"   "+emailAdd;
 
        FileOutputStream fos = null;
 
        try { 
            fos = new FileOutputStream(file); 
            fos.write((contents).getBytes()); 
            fos.close(); 
 
        } catch (IOException e) {
            e.printStackTrace();
        }
	}
	
    private void getRegID(){
		GCMRegistrar.register(this, SENDER_ID);
		
		mHandler = new Handler(); 
    	
    	runOnUiThread(new Runnable(){
    		@Override
            public void run()
            {
                mProgressDialog = ProgressDialog.show(SummaryActivity.this,"",
                        "Registration in Progress",true);
                mHandler.postDelayed( new Runnable()
                {
                    @Override
                    public void run()
                    {
                        try
                        {
                            if (mProgressDialog!=null&&mProgressDialog.isShowing()){
                                mProgressDialog.dismiss();
                            }
                        }
                        catch ( Exception e )
                        {
                            e.printStackTrace();
                        }
                    }
                }, 4000);
            }    		    		
    	});
    	// Waiting for the server response and then check
    	mHandler.postDelayed(new Runnable() { 
             public void run() { 
            	 if(GCMRegistrar.isRegistered(SummaryActivity.this)){
            			mDisplay.setText("This device is registered");
            			mtextStart.setText("Wait for the Push Notification");
            	    	unsubBtn.setEnabled(true);
            	    	subBtn.setEnabled(false);
            	 }
            	 else{
	         			mDisplay.setText("Registration failed");
	         			mtextStart.setText("Try to Subscribe Again");
            	 }
             } 
        }, 3000); 

    }
    private void dismissRedID(){
    	
        GCMRegistrar.unregister(this);
        
		mHandler = new Handler(); 
    	
    	runOnUiThread(new Runnable(){
    		@Override
            public void run()
            {
                mProgressDialog = ProgressDialog.show(SummaryActivity.this,"",
                        "UnRegistration in Progress",true);
                mHandler.postDelayed( new Runnable()
                {
                    @Override
                    public void run()
                    {
                        try
                        {
                            if (mProgressDialog!=null&&mProgressDialog.isShowing()){
                                mProgressDialog.dismiss();
                            }
                        }
                        catch ( Exception e )
                        {
                            e.printStackTrace();
                        }
                    }
                }, 4000);
            }    		    		
    	});
    	
    	mHandler.postDelayed(new Runnable() { 
             public void run() { 
            	 //if(com.gemalto.android.pivandroid.GCMIntentService.checkId == null){
            	 if(!GCMRegistrar.isRegistered(SummaryActivity.this)){
	         	        mDisplay.setText("This device has unregistered");
	         	        mtextStart.setText("You have to register first to start Demo");
	         	    	unsubBtn.setEnabled(false);
	         	    	subBtn.setEnabled(true);
            	 }
            	 else{
	                 	mDisplay.setText("Unregistration failed");
	                 	mtextStart.setText("Try to UnSubscribe Again");
            	 }
             } 
        }, 3000); 

    }
    
    private void checkNotNull(Object reference, String name){
    	if(reference == null){
    		throw new NullPointerException(
    				getString(R.string.error_config, name));
    	}
    }
    
	public boolean sendnstoreID(String regid){
		
		String Server_URL = "";
				
    	try{
    		AsyncHttpClient client = new AsyncHttpClient();
    		
    		RequestParams param = new RequestParams();
    		param.put("regid", regid);

    		client.get(Server_URL, param, new JsonHttpResponseHandler() {
    			
    		    @Override
    		    public void onSuccess(JSONObject response) {
    		    	
    		        try {
						Boolean res = response.getBoolean("success");
						
						if (res.booleanValue()) {
							
						}
					} catch (JSONException e) {
						e.printStackTrace();
					}
    		        
    		    }
    		});
            
        }catch(Exception e){
            Log.d("execption", "server error", e);
        } 	
        
        return true;
	}
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
}