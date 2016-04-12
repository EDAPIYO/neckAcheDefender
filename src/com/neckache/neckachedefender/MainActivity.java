package com.neckache.neckachedefender;


import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.widget.CompoundButton;
import android.widget.ToggleButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class MainActivity extends Activity {
	private static Context sContext;
	private ToggleButton mButton;
	private String TAG="MainActivity"; 
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		startService(new Intent(MainActivity.this, NeckService.class));
		/*
		mButton = (ToggleButton)findViewById(R.id.toggleButton1);
		mButton.setOnCheckedChangeListener(new OnCheckedChangeListener() {
	        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
	            Log.d(TAG,"call OnCheckdChangeListener" + isChecked);
	            if(isChecked == true){
	            	Log.v(TAG,"Service Started");
	            	 startService(new Intent(MainActivity.this, NeckService.class));
	            }else{
	            	Log.v(TAG,"Service Stopped");
	            	stopService(new Intent(MainActivity.this, NeckService.class));
	            }
	        }
	    });
	    */
		sContext = this;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	public static Context getContext(){
		return sContext;
	}
}
