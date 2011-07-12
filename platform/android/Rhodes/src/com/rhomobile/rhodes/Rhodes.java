/*
 ============================================================================
 Author	    : Anton Antonov
 Version	: 1.0
 Copyright  : Copyright (C) 2008 Rhomobile. All rights reserved.

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ============================================================================
 */
package com.rhomobile.rhodes;

import android.content.Intent;
import android.os.Bundle;
import com.rhomobile.rhodes.bluetooth.RhoBluetoothManager;

public class Rhodes extends BaseActivity {

	//private static final String TAG = "Rhodes";
	
	//private static Rhodes instance = null;
	
	/*
	public static Rhodes getInstance() {
		return instance;
	}
	*/
	
	//private RhoMenu appMenu = null;
	
	//private ViewGroup mOuterFrame = null;
	//private Bundle mSavedBundle = null;
	//private SplashScreen mSplashScreen = null;
	//private Handler mHandler = null;
	
	//private Object mStartParams = null;
	
	/*
	private void showSplashScreen() {
		mSplashScreen = new SplashScreen(getApplicationContext());
		mSplashScreen.start(mOuterFrame);
	}
	*/

	/*
	public void hideSplashScreen() {
		PerformOnUiThread.exec(new Runnable() {
			public void run() {
				if (mSplashScreen != null) {
					mSplashScreen.hide(mOuterFrame);
					mSplashScreen = null;
				}
				View view = RhoService.getInstance().getMainView().getView();
				view.setVisibility(View.VISIBLE);
				view.requestFocus();
			}
		}, false);
	}
	*/
		
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		//Utils.platformLog("Rhodes", "onStart()");
		
		super.onCreate(savedInstanceState);
		//mHandler = new Handler();
		
		// Here Log should be used, not Logger. It is because Logger is not initialized yet.
		//Log.v(TAG, "+++ onCreate");
		
		//mStartParams = getIntent().getExtras();
		//Log.d(TAG, "start parameters: " + mStartParams);
		
		//instance = this;
		
		//FrameLayout v = new FrameLayout(this);
		//mOuterFrame = v;
		
		//showSplashScreen();
		
		//mSavedBundle = savedInstanceState;

		//getWindow().setFlags(RhodesService.WINDOW_FLAGS, RhodesService.WINDOW_MASK);

		
		//this.requestWindowFeature(Window.FEATURE_PROGRESS);

		//getWindow().setFeatureInt(Window.FEATURE_PROGRESS, 10000);
		
		//setContentView(mOuterFrame, new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));


	}
	
	/*
	public void onStart() {
		super.onStart();
		Utils.platformLog("Rhodes", "onStart()");
	}
	*/
	
	/*
	public void onCreatePosponed() {
		RhoService.platformLog("Rhodes", "onCreatePosponed()");
		
		RhoService service = RhoService.getInstance();
		if (service == null) {
			//mSplashScreen.rho_start();
			Log.v(TAG, "Starting rhodes service...");
			service = new RhoService(this, mOuterFrame, mStartParams);
		}
		else
			Log.v(TAG, "Rhodes service already started...");
		
		Thread ct = Thread.currentThread();
		ct.setPriority(Thread.MAX_PRIORITY);
		service.setInfo(this, ct.getId(), mHandler);


		boolean disableScreenRotation = RhoConf.getBool("disable_screen_rotation");
		this.setRequestedOrientation(disableScreenRotation ? ActivityInfo.SCREEN_ORIENTATION_PORTRAIT :
			ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
		
		//if (RhodesService.ENABLE_LOADING_INDICATION)
		//	this.requestWindowFeature(Window.FEATURE_PROGRESS);

		service.setRootWindow(mOuterFrame);
	
		RhoService.getInstance().activityStarted();
		
		if (mTimerPostponeCreate != null) {
			mTimerPostponeCreate.cancel();
			mTimerPostponeCreate = null;
		}
	}
	*/
	
	//private Timer mTimerPostponeCreate = null;
	
	/*
	@Override
	public void onResume() {
		super.onResume();
		instance = this;
		Utils.platformLog("Rhodes", "onResume()");
	}
	*/
	
	/*
	public static void runPostponedSetup() {
		final Rhodes r = Rhodes.getInstance();
		r.mHandler.post( new Runnable() {
				public void run() {
					Utils.platformLog("Rhodes", "postponed Create UIThread.run()");
					r.onCreatePosponed();
				}
			});
	}
	*/
	
	/*
	@Override
	public void onPause() {
		instance = null;
	}
	*/
	
	/*
	@Override
	public void onStop() {
		instance = null;
	}
	*/
	
	/*
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		Logger.T(TAG, "+++ onConfigurationChanged");
		super.onConfigurationChanged(newConfig);
		RhoService.getInstance().rereadScreenProperties();
	}
	*/

	/*
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:
			RhodesService r = RhodesService.getInstance();
			MainView v = r.getMainView();
			v.back(v.activeTab());
			return true;
		}
		
		return super.onKeyDown(keyCode, event);
	}
	*/
	
	/*
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
		appMenu = new RhoMenu(menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (appMenu == null)
			return false;
		return appMenu.onMenuItemSelected(item);
	}
	*/
	
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		RhoBluetoothManager.onActivityResult(requestCode, resultCode, data);
	}
	
}
