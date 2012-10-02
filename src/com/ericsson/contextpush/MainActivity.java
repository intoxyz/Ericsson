package com.ericsson.contextpush;

import com.ericsson.contextpush.R;
import com.google.android.gcm.GCMRegistrar;
import android.os.Bundle;
import android.os.Handler;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends Activity {
	
	private String LOG_TAG = "** Mainaivity Regista **";
	private TextView mDisplay, mtextStart;
	private EditText mPhoneField, mEmailField;
	private Button nextBtn;
	private String defaultPhoneNum, defaultEmailAdd;
	public static String setPhoneNum, setEmailAdd;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        findViewById(R.id.nextbtnmain).setOnClickListener(mClickListener);
        
        nextBtn = (Button)findViewById(R.id.nextbtnmain);
        mPhoneField = (EditText)findViewById(R.id.phoneNum);
        mEmailField = (EditText)findViewById(R.id.emailAdd);
        mtextStart = (TextView)findViewById(R.id.textView1);
        mDisplay = (TextView)findViewById(R.id.textView4);
        
        defaultPhoneNum = fetchPhoneNum();
        defaultEmailAdd = fetchEmailAdd();
        
        mPhoneField.setText(defaultPhoneNum);
        mEmailField.setText(defaultEmailAdd);
        
        mDisplay.setText("Click Next button");
        mtextStart.setText("Check your login info");
        
    }
    
    Button.OnClickListener mClickListener = new View.OnClickListener() {
    	
		public void onClick(View v) {
						
			switch (v.getId()){
				case R.id.nextbtnmain:
					
					setPhoneNum = (String)mPhoneField.getText().toString();
					setEmailAdd = (String)mEmailField.getText().toString();
					
					Intent intent = new Intent(getApplicationContext(),MaplableActivity.class);
					startActivity(intent);
					
					break;
			}
		}
	};
	
	private String fetchPhoneNum(){
		
		TelephonyManager manager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
		String phoneNum = manager.getLine1Number();
		
		return phoneNum;
	}
	
	private String fetchEmailAdd(){
		String emailAdd = null;
		
        AccountManager Am = AccountManager.get(this);
        Account[] accts = Am.getAccounts();
        final int count = accts.length;
        Account acct = null;
             
        for(int i=0;i<count;i++) {
           acct = accts[i];
           Log.d(LOG_TAG, "Account - name="+acct.name+", type="+acct.type);
           if(acct.type.equals("com.google")){
        	   emailAdd = acct.name;
           }
        }      
		
		return emailAdd;		
	}
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
    
}
