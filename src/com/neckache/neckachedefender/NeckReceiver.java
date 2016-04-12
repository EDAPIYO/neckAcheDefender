package com.neckache.neckachedefender;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;


public class NeckReceiver extends BroadcastReceiver {
	public static final String TAG = "NeckReceiver";
	private Intent mIntent;
	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
        String action = intent.getAction();  
  	  
        Log.d(TAG, "mReceiver received : " + action);  

        if(action.equals(Intent.ACTION_USER_PRESENT)){ 
        	Log.v(TAG,"startService");
        	mIntent = new Intent(context, NeckService.class);
        	context.startService(mIntent);
        }  
        else if(action.equals(Intent.ACTION_SCREEN_OFF)){  
        	Log.v(TAG,"stopService");
        	mIntent = new Intent(context, NeckService.class);
        	context.stopService(mIntent);
        }else if(action.equals(Intent.ACTION_BOOT_COMPLETED)){
        	Log.v(TAG,"startService");
        	mIntent = new Intent(context, NeckService.class);
        	context.startService(mIntent);            	
        }
	}

}