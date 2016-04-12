package com.neckache.neckachedefender;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

public class NeckService extends Service implements SensorEventListener{

	static final String TAG ="NeckService";
	private SensorManager mSensorManager;
	private long mLastTime = 0;
	private long mNowTime = 0;
	private long mDiffTime =0;
	private float mAccAverage[] = new float[3];
	//
	private Timer mTimer = null;
	private TimerTask mTimerTask = null;
	private Handler mHandler = null;
	private float mLaptime = 0;
	private int mCounter = 0;
	private float mPitch=0.0f;
	private float mWeight = 0.0f;
	//
	private Notification mNotification;
	private NotificationManager mNotificationManager;
	private int NOTIFICATION_ID = 1;
	
	@Override
	public void onCreate(){
		Log.v(TAG,"onCreate");
		initNotification();
		
		IntentFilter filter = new IntentFilter();  
        filter.addAction(Intent.ACTION_SCREEN_OFF);  
        registerReceiver(mReceiver, filter);  		
        initSensor();
        startTimer();
	}
	
	
	@Override
    public int onStartCommand(Intent intent, int flags, int startId) {
    	Log.i(TAG, "onStartCommand Received start id " + startId + ": " + intent);
        Toast.makeText(this, "MyService#onStartCommand " + intent, Toast.LENGTH_SHORT).show();
		return START_STICKY;
    	
    }
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub

		return null;
	}

	@Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy");
        Toast.makeText(this, "MyService#onDestroy", Toast.LENGTH_SHORT).show(); 
        unregisterReceiver(mReceiver); 
        killSensor();
        stopTimer();
        killNotification();
    }
	
	
	 BroadcastReceiver mReceiver = new BroadcastReceiver() {  
		  
	        @Override  
	        public void onReceive(Context context, Intent intent) {  
	            String action = intent.getAction();  
	            Log.d(TAG, "mReceiver received : " + action);  
	            if(action.equals(Intent.ACTION_SCREEN_OFF)){  
	            	Log.v(TAG,"stopService");
	            	stopService(new Intent(NeckService.this, NeckService.class));	            
	            }
	        }  
	    };


	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void onSensorChanged(SensorEvent event) {
		// TODO Auto-generated method stub
		//Log.v(TAG,"onSensorChanged");
		
		mNowTime = System.currentTimeMillis();
		mDiffTime = (mNowTime -mLastTime);
		
		mAccAverage[0] += event.values[0];
		mAccAverage[1] += event.values[1];
		mAccAverage[2] += event.values[2];

		float Ax,Ay,Az,invA;
		mLastTime =mNowTime;
		mCounter++;
		if(mCounter >5){
			mAccAverage[0] = mAccAverage[0]/mCounter;
			mAccAverage[1] = mAccAverage[1]/mCounter;
			mAccAverage[2] = mAccAverage[2]/mCounter;
			Ax = mAccAverage[0];
			Ay = mAccAverage[1];
			Az = mAccAverage[2];
			invA = 1.0f / (float)Math.sqrt(Ax*Ax + Ay*Ay + Az*Az);
			Ax *= invA;
			Ay *= invA;
			Az *= invA;
			float rawpitch =   (float) ((float)Math.asin(-Ay) * 180 /(double)Math.PI); // pitch 
			if(Az < 0){
				mPitch = -( 90 + rawpitch);
			}else{
				mPitch = 90 + rawpitch;
			}
			mPitch = (float)((int)(mPitch *100)/100.0f);
			mWeight = (float)((int)( (0.92 * mPitch + 4.536) * 100)) /100.0f;
			
			//roll = (float) ((float)Math.atan2(-Ax, Az) * 180/(double)Math.PI); //roll
			Log.v(TAG,"Ax:" + Ax +  ",Ay:" + Ay + ",Az:" + Az);
			Log.v(TAG,"pitch:" + mPitch + "(rawpitch "+rawpitch+ ")");
			Log.i(TAG,"首の角度は"+mPitch+"°");
			if(mPitch>0){
				Log.i(TAG,"脊髄にかかる負荷は\n"+ mWeight  +"kg（推定）です");
			}else{
				Log.i(TAG,"脊髄にかかる負荷を推定できません");				
			}
			updateNotificaiton();
			stopSensor();
		}else{
		}
				
	}
	protected void startTimer(){
		
		mTimer = new Timer();
		mHandler = new Handler();
		mTimerTask = new TimerTask(){
	        @Override
	        public void run() {
	        	if(mHandler != null){
		            mHandler.post( new Runnable() {
		                public void run() {	 
		                    mLaptime +=  1.0;
		                    Log.v(TAG,"outputValue:" + mLaptime);
		                    mCounter = 0;
		                    mAccAverage[0]=0.0f;
		                    mAccAverage[1]=0.0f;
		                    mAccAverage[2]=0.0f;

		                    startSensor();
		                }
		            });
	        	}
	        }
	    };
	    
		if(mTimer != null){
			mTimer.schedule( mTimerTask, 10, 2500); //execute task  each 1sec.
		}else{
			Log.e(TAG,"mTimer is null");
		}
	}
	protected void stopTimer(){
		Log.v(TAG,"stopTimer");
		//mTimer = new Timer();
		//mHandler = new Handler();
		if(mTimer != null){
			Log.v(TAG,"TimerIsStopped");
			mTimer.cancel();
			mTimer = null;
		}else{
			Log.e(TAG,"stopTimer is already null");
		}
		
		
	}
	public synchronized void sleep(long msec)
    {	
		Log.v(TAG,"start sleep");
		try
    	{
    		wait(msec);	
    	}catch(InterruptedException e){
    		
    	}
		Log.v(TAG,"stop wait");
    }
	
	protected void startSensor(){
		Log.v(TAG,"startSensor");
		if(mSensorManager != null){
			//Log.v(TAG,"registerListener");
			mSensorManager.registerListener(
				this,
				mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
				SensorManager.SENSOR_DELAY_FASTEST);
		}else{
			Log.e(TAG,"sensorManager is null");
		}
		
	}
	protected void stopSensor(){
		Log.v(TAG,"stopSensor");
		if(mSensorManager != null){
			//Log.v(TAG,"registerListener");
			mSensorManager.unregisterListener(this);
		}else{
			Log.e(TAG,"sensor is already killed");
		}
	}
	protected void initSensor(){
		Log.v(TAG,"initSensor");
		mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE); 
	}
	protected void killSensor(){
		Log.v(TAG,"killSensor");
		if(mSensorManager != null){
			mSensorManager = null;
		}else{
			Log.v(TAG,"sensor is already null");
		}
	}
	
	protected void updateNotificaiton(){
		Log.v(TAG,"updateNotification");
		if(mNotification != null){
			mNotification.icon = R.drawable.ic_launcher;
			Intent intent = new Intent(this, MainActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
			mNotification.setLatestEventInfo(this, "首の推定角度" + mPitch, "脊髄にかかる負荷は"+mWeight + "kg(推定)です", pendingIntent);
			mNotificationManager.notify(NOTIFICATION_ID,mNotification);
		}
	}
	protected void initNotification(){
		Log.v(TAG,"initNotificaiton");
		//Notification Area
		mNotification = new Notification();
		mNotification.icon = R.drawable.ic_launcher;
		// PendingIntentの生成
		Intent intent = new Intent(this, MainActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

		// 詳細情報の設定とPendingIntentの設定
		mNotification.setLatestEventInfo(this, "首の推定角度", "首にかかる負荷はXXkg(推定)です", pendingIntent);
		mNotificationManager = 
				   (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
				
		mNotification.flags = Notification.FLAG_ONGOING_EVENT;
		mNotificationManager.notify(NOTIFICATION_ID,mNotification);
		///////
	}
	protected void killNotification(){
		Log.v(TAG,"killNotification");
		mNotificationManager.cancel(NOTIFICATION_ID);
	}
}

