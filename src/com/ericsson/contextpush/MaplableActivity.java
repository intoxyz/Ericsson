package com.ericsson.contextpush;

import java.util.ArrayList;
import java.util.List;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.OverlayItem;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

public class MaplableActivity extends MapActivity implements RadioGroup.OnCheckedChangeListener, OnGestureListener{
		 
		private MapView map = null;
		private MyLocationOverlay me = null;
		private RadioGroup mRadio;
		private RadioButton mHomeRd, mWorkRd, mOtherRd;
		private Button mNextBtn;
		private GestureDetector gd = null;
		
		private String locaName;
		private int locaNameIndex;
		private int pinCnt;
		private boolean[] locaChecked = {false, false, false};
		
		private InterestArea[] interestAreas; 
		private boolean flags[] = {false, false , false, false };// default set or policy
		
		LocationManager lm;
		Location location;
			
		GeoPoint p;
	 
		@Override
		protected void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			setContentView(R.layout.maplableactivity);
			
			//map view setting
			locaName = "Home";
			locaNameIndex = 0;
			pinCnt = 0;
			mapInit();
		
			//Radio Button Set - default home
			mRadio = (RadioGroup)findViewById(R.id.radio);
			mRadio.setOnCheckedChangeListener(MaplableActivity.this);
			
			mHomeRd = (RadioButton)findViewById(R.id.homeradioBt);
			mWorkRd = (RadioButton)findViewById(R.id.workradioBt);
			mOtherRd = (RadioButton)findViewById(R.id.otherradioBt);
			mHomeRd.setOnClickListener(mRadioListener);
			mWorkRd.setOnClickListener(mRadioListener);
			mOtherRd.setOnClickListener(mRadioListener);
			
			
			mNextBtn = (Button)findViewById(R.id.nextBtn);
			mNextBtn.setOnClickListener(mClickListener);
	        						
	        gd = new GestureDetector(MaplableActivity.this);
	        interestAreas = new InterestArea[3];
	        
		}
		
		private void mapInit(){
			
			map = (MapView) findViewById(R.id.mapview);
			map.getController().setZoom(13);
			
			//default coordinate 37.411312, 122.059743
	        String coordinates[] = {"37.411312", "122.059743"};

	        double lat = Double.parseDouble(coordinates[0]);
	        double lng = Double.parseDouble(coordinates[1]);
			p = new GeoPoint((int)(lat*1E6), (int)(lng*1E6));
			
			me = new MyLocationOverlay(this, map);
			
	        me.enableMyLocation();
		    
	        me.runOnFirstFix(new Runnable() {
	        	  
	        	   @Override
	        	   public void run() {
	        		   p = me.getMyLocation();
	        	   }
	        });
	        
	        //add current location
	        map.getOverlays().add(me);

			//TODO: this is a workaround
			moveMarker(p);
			pinCnt--;
			
	
			map.postInvalidate();			
		}
	 
		@Override
		public void onResume() {
			super.onResume();
			me.enableMyLocation();
			
			lm = (LocationManager)getSystemService(LOCATION_SERVICE);
			Location loc = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
			
			if(loc == null){
				loc = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
			}

			p = getPoint(loc.getLatitude(), loc.getLongitude());
			map.getController().setCenter(p);

			//me.enableCompass();
		}
	 
		@Override
		public void onPause() {
			super.onPause();
			me.disableMyLocation();
			//me.disableCompass();
		}
	 
		@Override
		protected boolean isLocationDisplayed() {
			return false;
		}
		protected boolean isRouteDisplayed() {
			return false;
		}
	 
		private GeoPoint getPoint(double lat, double lng) {
			return (new GeoPoint((int) (lat * 1000000.0), (int) (lng * 1000000.0)));
		}
	 
		private class SitesOverlay extends ItemizedOverlay<OverlayItem> {
	 
			private List<OverlayItem> items = new ArrayList<OverlayItem>();
			private Drawable marker = null;
	 
			public SitesOverlay(Drawable marker, int lat, int lng) {	 
				super(marker);
				this.marker = marker;
	 
				items.add(new OverlayItem(getPoint((float)lat/1000000, (float)lng/1000000), locaName, String.valueOf(locaNameIndex)));
				populate();
				pinCnt++;
				
			}
	 
			@Override
			protected OverlayItem createItem(int i) {
				return (items.get(i));
			}
	 
			@Override
			public void draw(Canvas canvas, MapView mapView, boolean shadow) {
				//false : remove shadow of marker
				super.draw(canvas, mapView, false);
	 
				//boundCenterBottom(marker);
				boundCenter(marker);
	        }


	        @Override
	        public boolean onTouchEvent(MotionEvent event, MapView mapView) {   
	        	
	        	p = mapView.getProjection().fromPixels((int)event.getX(),(int)event.getY());

	        	if(gd!=null) {
	        		return gd.onTouchEvent(event);
	        	}
	        	return super.onTouchEvent(event, mapView);
	        }
	 
			@Override
			protected boolean onTap(int i) {
				
				OverlayItem layItem = items.get(i);	 
				onShowDialog(layItem, null);
				return true;
			}
	 
			@Override
			public int size() {
				return (items.size());
			}
			
		}//SitesOverlay
		
		private void moveMarker(GeoPoint loc){
			map.getController().setCenter(loc);
			addMarker(loc.getLatitudeE6(), loc.getLongitudeE6());
		}
		
		private void addMarker(int lat, int lng){
			Drawable marker = null;
			
			switch(locaNameIndex){
				case 0:
					marker = getResources().getDrawable(R.drawable.a_4);
					break;
				case 1:
					marker = getResources().getDrawable(R.drawable.b_4);
					break;
				case 2:
					marker = getResources().getDrawable(R.drawable.c_4);
					break;
			}
			
			marker.setBounds(0, 0, marker.getIntrinsicWidth(), marker
					.getIntrinsicHeight());
			map.getOverlays().add(new SitesOverlay(marker, lat, lng));			
		}
		
		private void removeMarker(OverlayItem item){
			
			String selectedItemNum;
			int selectedItemIndex;
			
			selectedItemNum = item.getSnippet();
			selectedItemIndex = Integer.valueOf(selectedItemNum);
			
			if(pinCnt == 1){
				map.getOverlays().remove(2);
			}
			if(pinCnt == 2){
				if(!locaChecked[0]){
					map.getOverlays().remove(2+selectedItemIndex-1);
				}
				if(!locaChecked[1]){
					if(selectedItemIndex == 2){
						map.getOverlays().remove(2+selectedItemIndex-1);
					}
					if(selectedItemIndex == 0){
						map.getOverlays().remove(2+selectedItemIndex);
					}
				}
				if(!locaChecked[2]){
					map.getOverlays().remove(2+selectedItemIndex);
				}
			}
			if(pinCnt == 3){
				map.getOverlays().remove(2+selectedItemIndex);
			}
			locaChecked[selectedItemIndex] = false;
			pinCnt--;
		}
	 	
		
//-- Dialog
		
		private void onShowDialog(final OverlayItem item, String title) {
			
			boolean templateFlags[];
			int index;
			
			LayoutInflater inflater = (LayoutInflater)getApplicationContext().getSystemService(LAYOUT_INFLATER_SERVICE);
	        final View layout = inflater.inflate(R.layout.policysetting, (ViewGroup)findViewById(R.id.explaintxt));
	        
	        final TextView txtExplanation, txtSummary;
			txtSummary = (TextView) layout.findViewById(R.id.summarytxt);
			txtExplanation = (TextView) layout.findViewById(R.id.explaintxt);
			
			txtSummary.setTextColor(Color.BLACK);
			txtExplanation.setTextColor(Color.BLACK);
	        
			final String policies[] = {"Warning", "Minor", "Medium", "Major"};// item shown
	        	 
	        AlertDialog.Builder aDialog = new AlertDialog.Builder(MaplableActivity.this);
	        if(item != null){
	        	aDialog.setTitle("Policy Setting - "+item.getTitle());
	        }
	        else{
	        	aDialog.setTitle("Policy Setting - "+title);
	        }
	        aDialog.setView(layout);
	        
	        index = Integer.valueOf(item.getSnippet());
	        
	        if(locaChecked[index]){
	        	templateFlags = flags;
			}
	        else{
	        	templateFlags = interestAreas[index].getSeverity();
	        }
	        
	        	aDialog.setMultiChoiceItems(policies, templateFlags, new DialogInterface.OnMultiChoiceClickListener(){
				@Override
				public void onClick(DialogInterface dialog, int which,boolean isChecked) {
					String level = null;
					
					Toast.makeText(MaplableActivity.this, policies[which]+" which = "+which+" isChecked="+isChecked, 0).show();
					
					switch(which){
					
						case 0:
							level = "10";
							break;
							
						case 1:
							level = "20";
							break;
							
						case 2:
							level = "30";
							break;
							
						case 3:
							level = "40";
							break;
					}
					
					if(isChecked){
						txtExplanation.setText(policies[which]+" Explanation > "+level);
					}
					else{
						if(flags[0]){
							txtExplanation.setText("Warning Explanation > 10");
							return;
						}
						if(flags[1]){
							txtExplanation.setText("Minor Explanation > 20");
							return;
						}
						if(flags[2]){
							txtExplanation.setText("Medium Explanation > 30");
							return;
						}
						else{
							txtExplanation.setText("");
						}
					}
				}
			});
	        
			aDialog.setNeutralButton("REMOVE THIS PIN", new DialogInterface.OnClickListener() {
				
					@Override
					public void onClick(DialogInterface dialog, int which) {	
						removeMarker(item);
						return;
						
					}
			});
			
	        aDialog.setPositiveButton("SAVE", new DialogInterface.OnClickListener() {
	   
		           @Override
		           public void onClick(DialogInterface dialog, int which) {	
		        	   int index;
		        	   
		        	   index = Integer.valueOf(item.getSnippet());
		        	   interestAreas[index] = new InterestArea(item.getTitle(), new LocationInfo((double)p.getLatitudeE6()/1000000, (double)p.getLongitudeE6()/1000000, 5), flags);
	        	   
		        	   return;
		           }
		    });
	       
	        aDialog.setNegativeButton("BACK", new DialogInterface.OnClickListener() {
	   
		           @Override
		           public void onClick(DialogInterface dialog, int which) {
		        	   
		        	   return;    
		           }
		    });
	       
	        AlertDialog ad = aDialog.create();
	        ad.show();
	 
		}// Dialog			
		
		@Override
		public boolean onDown(MotionEvent e) {

			return false;
		}

		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
				float velocityY) {

			return false;
		}

		@Override
		public void onLongPress(MotionEvent e) {
			// convert touched pixel to location coordinates 
           	Toast.makeText(MaplableActivity.this, "lat: "+p.getLatitudeE6()+", long:"+p.getLongitudeE6(),0).show();
           	if(!locaChecked[locaNameIndex]){
           		moveMarker(p);
           		locaChecked[locaNameIndex] = true;
           	}
			else{
				Toast.makeText(MaplableActivity.this, "Already Set "+ locaName+"! To config setting, tap on the icon on the Map", 0).show();
				return;
			}
		}

		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2,
				float distanceX, float distanceY) {

			return false;
		}

		@Override
		public void onShowPress(MotionEvent e) {
			
		}

		//cannot use this since we have tap method
		@Override
		public boolean onSingleTapUp(MotionEvent e) {
			return false;
		}

		@Override
		public void onCheckedChanged(RadioGroup group, int checkedID) {
			if(mRadio.getCheckedRadioButtonId() == R.id.homeradioBt){
				locaName = "Home";
				locaNameIndex = 0;
			}
			else if(mRadio.getCheckedRadioButtonId() == R.id.workradioBt){
				locaName = "Work";	
				locaNameIndex = 1;
			}
			else if(mRadio.getCheckedRadioButtonId() == R.id.otherradioBt){
				locaName = "Other";
				locaNameIndex = 2;
			}			
		}
	    
		Button.OnClickListener mClickListener = new View.OnClickListener() {
	    	
			public void onClick(View v) {
							
				switch (v.getId()){
					case R.id.nextBtn:
						Intent intent = new Intent(getApplicationContext(), SummaryActivity.class);
						startActivity(intent);
						
						
						break;
				}
			}
		};
		
		RadioButton.OnClickListener mRadioListener = new View.OnClickListener() {
	    	
			public void onClick(View v) {
				Toast.makeText(MaplableActivity.this, "Tap on the map to put the Marker for 2secs", Toast.LENGTH_LONG).show();
			}
		};
			
}