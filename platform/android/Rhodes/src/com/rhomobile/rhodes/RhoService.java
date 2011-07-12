package com.rhomobile.rhodes;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;
import java.util.UUID;
import java.util.Vector;

import com.rhomobile.rhodes.alert.Alert;
import com.rhomobile.rhodes.event.EventStore;
import com.rhomobile.rhodes.file.RhoFileApi;
import com.rhomobile.rhodes.geolocation.GeoLocation;
import com.rhomobile.rhodes.mainview.MainView;
import com.rhomobile.rhodes.ui.AboutDialog;
import com.rhomobile.rhodes.ui.LogOptionsDialog;
import com.rhomobile.rhodes.ui.LogViewDialog;
import com.rhomobile.rhodes.uri.ExternalHttpHandler;
import com.rhomobile.rhodes.uri.MailUriHandler;
import com.rhomobile.rhodes.uri.SmsUriHandler;
import com.rhomobile.rhodes.uri.TelUriHandler;
import com.rhomobile.rhodes.uri.UriHandler;
import com.rhomobile.rhodes.uri.VideoUriHandler;
import com.rhomobile.rhodes.util.PerformOnUiThread;
import com.rhomobile.rhodes.webview.ChromeClientOld;
import com.rhomobile.rhodes.webview.RhoWebSettings;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.Process;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.RemoteViews;

public class RhoService extends Service {
	
	private final static String TAG = "RhodesService";

	public static final String INTENT_EXTRA_PREFIX = "com.rhomobile.rhodes.";
	
	private static final int DOWNLOAD_PACKAGE_ID = 1;
	
	private static final String ACTION_ASK_CANCEL_DOWNLOAD = "com.rhomobile.rhodes.DownloadManager.ACTION_ASK_CANCEL_DOWNLOAD";
	private static final String ACTION_CANCEL_DOWNLOAD = "com.rhomobile.rhodes.DownloadManager.ACTION_CANCEL_DOWNLOAD";
	
	public static final int RHO_SPLASH_VIEW = 1;
	public static final int RHO_MAIN_VIEW = 2;
	public static final int RHO_TOOLBAR_VIEW = 3;
	
	private static final String RHO_START_PARAMS_KEY = "RhoStartParams";
	
	private static RhoService instance = null;
	
	public static RhoService getInstance() {
		return instance;
	}
	
	private class LocalBinder extends Binder {
		RhoService getService() {
			return RhoService.this;
		}
	};
	
	private final IBinder mBinder = new LocalBinder();
	
	@SuppressWarnings("unchecked")
	private static final Class[] mStartForegroundSignature = new Class[] {int.class, Notification.class};
	@SuppressWarnings("unchecked")
	private static final Class[] mStopForegroundSignature = new Class[] {boolean.class};
	
	private Method mStartForeground;
	private Method mStopForeground;
	
	private NotificationManager mNM;
	
	private Activity ctx;
	
	public Context getContext() {
		return ctx;
	}
	
	public Activity getMainActivity() {
		return ctx;
	}
	
	public static void platformLog(String _tag, String _message) {
		StringBuilder s = new StringBuilder();
		s.append("ms[");
		s.append(System.currentTimeMillis());
		s.append("] ");
		s.append(_message);
		android.util.Log.v(_tag, s.toString());
	}
	
	private RhoLogConf m_rhoLogConf = new RhoLogConf();
	public RhoLogConf getLogConf() {
		return m_rhoLogConf;
	}
	
	private boolean needGeoLocationRestart = false;
	
	private int activitiesActive = 0;
	
	public static int WINDOW_FLAGS = WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN;
	public static int WINDOW_MASK = WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN;
	
	public static boolean ENABLE_LOADING_INDICATION = true;
	
	public static int MAX_PROGRESS = 10000;
	
	private long uiThreadId = 0;
	
	public long getUiThreadId() {
		return uiThreadId;
	}
	
	public void setInfo(Activity c, long id, Handler handler) {
		ctx = c;
		uiThreadId = id;
		uiHandler = handler;
		//RhoBluetoothManager.sharedInstance();
	}
	
	private Handler uiHandler;
	
	public void post(Runnable r) {
		uiHandler.post(r);
	}
	
	public void post(Runnable r, int delay) {
		uiHandler.postDelayed(r, delay);
	}
	
	private static int screenWidth;
	private static int screenHeight;
	private static int screenOrientation;
	
	private static float screenPpiX;
	private static float screenPpiY;
	
	private static boolean isCameraAvailable;
	
	private WebChromeClient chromeClient;
	private RhoWebSettings webSettings;
	
	private ViewGroup outerFrame = null;
	private MainView mainView;
	
	private native void initClassLoader(ClassLoader c);
	
	private native void createRhodesApp();
	private native void startRhodesApp();
	
	public native void doSyncAllSources(boolean v);
	public native void doSyncSource(String source);
	
	public native String getOptionsUrl();
	public native String getStartUrl();
	public native String getCurrentUrl();
	public native String getAppBackUrl();
	
	public static native String getBlobPath();
	
	public native String normalizeUrl(String url);
	
	public static native String getBuildConfig(String key);
	
	public static native void loadUrl(String url);
	
	public static native void navigateBack();
	
	public native void doRequest(String url);
	
	public native void callActivationCallback(boolean active);
	
	public native static void makeLink(String src, String dst);
	
	public static native void onScreenOrientationChanged(int width, int height, int angle);
	
	private String rootPath = null;
	private native void nativeInitPath(String rootPath, String sqliteJournalsPath, String apkPath);
	
	static PowerManager.WakeLock wakeLockObject = null;
	static boolean wakeLockEnabled = false;
	
	public static int rho_sys_set_sleeping(int enable) {
		Logger.I(TAG, "rho_sys_set_sleeping("+enable+")");
		int wasEnabled = 1;
		if (wakeLockObject != null) {
			wasEnabled = 0;
		}
		if (enable != 0) {
			// disable lock device
			PerformOnUiThread.exec( new Runnable() {
				public void run() {
					if (wakeLockObject != null) {
						wakeLockObject.release();
						wakeLockObject = null;
						wakeLockEnabled = false;
					}
				}
			}, false);
		}
		else {
			// lock device from sleep
			PerformOnUiThread.exec( new Runnable() {
				public void run() {
					if (wakeLockObject == null) {
						PowerManager pm = (PowerManager)getInstance().getContext().getSystemService(Context.POWER_SERVICE);
						if (pm != null) {
							wakeLockObject = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, TAG);
							wakeLockObject.acquire();
							wakeLockEnabled = true;
						}
						else {
							Logger.E(TAG, "rho_sys_set_sleeping() - Can not get PowerManager !!!");
						}
					}
				}
			}, false);
		}
		return wasEnabled;
	}
	
	private void initRootPath() {
		ApplicationInfo appInfo = getAppInfo();
		String dataDir = appInfo.dataDir;
		
		rootPath = dataDir + "/rhodata/";
		Log.d(TAG, "Root path: " + rootPath);
		
		String sqliteJournalsPath = dataDir + "/sqlite_stmt_journals/";
		Log.d(TAG, "Sqlite journals path: " + sqliteJournalsPath);
		
		File f = new File(rootPath);
		f.mkdirs();
		f = new File(f, "db/db-files");
		f.mkdirs();
		f = new File(sqliteJournalsPath);
		f.mkdirs();
		
		String apkPath = appInfo.sourceDir;
		
		nativeInitPath(rootPath, sqliteJournalsPath, apkPath);
	}
	
	private ApplicationInfo getAppInfo() {
		String pkgName = ctx.getPackageName();
		try {
			ApplicationInfo info = ctx.getPackageManager().getApplicationInfo(pkgName, 0);
			return info;
		} catch (NameNotFoundException e) {
			throw new RuntimeException("Internal error: package " + pkgName + " not found: " + e.getMessage());
		}
	}
	
	public String getRootPath() {
		return rootPath;
	}
	
	private Vector<UriHandler> uriHandlers = new Vector<UriHandler>();
	
	private boolean handleUrlLoading(String url) {
		Enumeration<UriHandler> e = uriHandlers.elements();
		while (e.hasMoreElements()) {
			UriHandler handler = e.nextElement();
			try {
				if (handler.handle(url))
					return true;
			}
			catch (Exception ex) {
				Logger.E(TAG, ex.getMessage());
				continue;
			}
		}
		
		return false;
	}

	private static int mGeoLocationInactivityTimeout;

	public static int getGeoLocationInactivityTimeout() {
		return mGeoLocationInactivityTimeout;
	}
	
	private static boolean mSplashHidden = false;
	
	private static void hideSplashScreenIfNeeded(Rhodes ra, String url) {
		if (ra == null)
			return;
		
		if (!mSplashHidden && url.startsWith("http://")) {
			ra.hideSplashScreen();
			mSplashHidden = true;
		}
	}
	
	public static WebView createLoadingWebView(Context ctx) {
		WebView w = new WebView(ctx);
		
		//webSettings.setWebSettings(w);
		
		w.clearCache(true);

		w.setWebViewClient(new WebViewClient() {
			
			private boolean setupExecuted = false;
			
			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				super.onPageStarted(view, url, favicon);
			}
			
			@Override
			public void onPageFinished(WebView view, String url) {
				Rhodes ra = Rhodes.getInstance();
				hideSplashScreenIfNeeded(ra, url);
				if (ra != null)
					ra.getWindow().setFeatureInt(Window.FEATURE_PROGRESS, MAX_PROGRESS);
				super.onPageFinished(view, url);
				
				if (!setupExecuted) {
					Rhodes.runPostponedSetup();
					setupExecuted = true;
				}
				
			}

		});
		
		//w.setWebChromeClient(chromeClient);
		
		return w;
	
	}
	
	public WebView createWebView() {
		WebView w = new WebView(ctx);
		
		webSettings.setWebSettings(w);
		
		w.clearCache(true);

		w.setWebViewClient(new WebViewClient() {
			
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				return handleUrlLoading(url);
			}
			
			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				Rhodes r = Rhodes.getInstance();
				if (ENABLE_LOADING_INDICATION)
					r.getWindow().setFeatureInt(Window.FEATURE_PROGRESS, 0);
				super.onPageStarted(view, url, favicon);
			}
			
			@Override
			public void onPageFinished(WebView view, String url) {
				// Set title
				Rhodes ra = Rhodes.getInstance();
				hideSplashScreenIfNeeded(ra, url);
				
				if (ra != null) {
					String title = view.getTitle();
					ra.setTitle(title);
					ra.getWindow().setFeatureInt(Window.FEATURE_PROGRESS, MAX_PROGRESS);
				}
				
				super.onPageFinished(view, url);
			}

		});
		
		w.setWebChromeClient(chromeClient);
		
		return w;
	}
	
	private void initWebStuff() {
		String ccName;
		String wsName;
		int sdkVersion = Integer.parseInt(Build.VERSION.SDK);
		if (sdkVersion < Build.VERSION_CODES.ECLAIR_MR1) {
			ccName = "ChromeClientOld";
			wsName = "RhoWebSettingsOld";
		}
		else {
			ccName = "ChromeClientNew";
			wsName = "RhoWebSettingsNew";
		}
		
		try {
			String pkgname = ChromeClientOld.class.getPackage().getName();
			String fullName = pkgname + "." + ccName;
			Class<? extends WebChromeClient> ccClass =
				Class.forName(fullName).asSubclass(WebChromeClient.class);
			chromeClient = ccClass.newInstance();
			
			pkgname = RhoWebSettings.class.getPackage().getName();
			fullName = pkgname + "." + wsName;
			Class<? extends RhoWebSettings> wsClass =
				Class.forName(fullName).asSubclass(RhoWebSettings.class);
			webSettings = wsClass.newInstance();
		}
		catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}
	
	public void setMainView(MainView v) {
		View main = outerFrame.findViewById(RHO_MAIN_VIEW);
		if (main != null)
			outerFrame.removeView(main);
		mainView = v;
		main = mainView.getView();
		if (outerFrame.findViewById(RHO_SPLASH_VIEW) != null)
			main.setVisibility(View.INVISIBLE);
		outerFrame.addView(main);
	}
	
	public MainView getMainView() {
		return mainView;
	}
	
	public void setRootWindow(ViewGroup rootWindow) {
		if (rootWindow == outerFrame)
			return;
		if (outerFrame != null)
			outerFrame.removeAllViews();
		outerFrame = rootWindow;
		outerFrame.addView(mainView.getView());
	}
	
	public static boolean isCreated() {
		return instance != null;
	}
	
	public void startServiceForeground(int id, Notification notification) {
		if (mStartForeground != null) {
			try {
				mStartForeground.invoke(this, new Object[] {Integer.valueOf(id), notification});
			}
			catch (InvocationTargetException e) {
				Log.e(TAG, "Unable to invoke startForeground", e);
			}
			catch (IllegalAccessException e) {
				Log.e(TAG, "Unable to invoke startForeground", e);
			}
			return;
		}
		
		setForeground(true);
		mNM.notify(id, notification);
	}
	
	public void stopServiceForeground(int id) {
		if (mStopForeground != null) {
			try {
				mStopForeground.invoke(this, new Object[] {Integer.valueOf(id)});
			}
			catch (InvocationTargetException e) {
				Log.e(TAG, "Unable to invoke stopForeground", e);
			}
			catch (IllegalAccessException e) {
				Log.e(TAG, "Unable to invoke stopForeground", e);
			}
			return;
		}
		
		mNM.cancel(id);
		setForeground(false);
	}
	
	@Override
	public IBinder onBind(Intent arg0) {
		return mBinder;
	}
	
	@Override
	public void onCreate() {
		// TODO:
		try {
			mStartForeground = getClass().getMethod("startForeground", mStartForegroundSignature);
			mStopForeground = getClass().getMethod("stopForeground", mStopForegroundSignature);
		}
		catch (NoSuchMethodException e) {
			mStartForeground = null;
			mStopForeground = null;
		}
		
		mNM = (NotificationManager)getContext().getSystemService(Context.NOTIFICATION_SERVICE);
	}
	
	private void handleCommand(Intent intent) {
		// TODO:
	}
	
	@Override
	public void onStart(Intent intent, int startId) {
		handleCommand(intent);
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		handleCommand(intent);
		return Service.START_STICKY;
	}
	
	public RhoService(Activity c, ViewGroup rootWindow, Object params) {
	
		ctx = c;
		instance = this;
		outerFrame = rootWindow;

		initClassLoader(ctx.getClassLoader());

		try {
			initRootPath();
			RhoFileApi.init();
		} catch (IOException e) {
			Log.e(TAG, e.getMessage());
			exitApp();
			return;
		}
		
		if (Utils.isAppHashChanged()) {
			try {
				Log.i(TAG, "Application hash was changed, so remove files");
				String[] folders = {"apps", "lib"};
				for (String folder : folders) {
					File f = new File(rootPath, folder);
					if (!f.exists())
						continue;
					Utils.deleteRecursively(f);
				}
				initRootPath();
				RhoFileApi.init();
				RhoFileApi.copy("hash");
			} catch (IOException e) {
				Log.e(TAG, e.getMessage());
				exitApp();
				return;
			}
		}
		
		createRhodesApp();

		/*
		boolean rhoGalleryApp = false;
		if (params != null && params instanceof Bundle) {
			Bundle startParams = (Bundle)params;
			String v = startParams.getString("rhogallery_app");
			if (v != null && v.equals("1"))
				rhoGalleryApp = true;
		}
		if (!rhoGalleryApp && RhoConf.getBool("rhogallery_only_app")) {
			Logger.E(TAG, "This is RhoGallery only app and can be started only from RhoGallery");
			exitApp();
		}
		*/
		boolean can_start = true;
		String security_token = getBuildConfig("security_token"); 
		if (security_token != null) {
			if (security_token.length() > 0) {
				can_start = false;
				if (params != null && params instanceof Bundle) {
					Bundle startParams = (Bundle)params;
					String rho_start_params = startParams.getString(RHO_START_PARAMS_KEY);
					if (rho_start_params != null) {
						String security_token_key = "sequrity_token=";
						int sec_index = rho_start_params.indexOf(security_token_key);
						if (sec_index >= 0) {
							String tmp = rho_start_params.substring(sec_index + security_token_key.length(), rho_start_params.length() - sec_index - security_token_key.length());
							int end_of_token = tmp.indexOf(",");
							if (end_of_token >= 0) {
								tmp = tmp.substring(0, end_of_token);
							}
							end_of_token = tmp.indexOf(" ");
							if (end_of_token >= 0) {
								tmp = tmp.substring(0, end_of_token);
							}
							if (tmp.equals(security_token)) {
								can_start = true;
							}
						}
					}
				}
			}
		}
		if (!can_start) {
			Logger.E(TAG, "SECURITY_TOKEN parameter is not valid for this application !");
			exitApp();
		}
		
		boolean fullScreen = true;
		if (RhoConf.isExist("full_screen"))
			fullScreen = RhoConf.getBool("full_screen");
		if (fullScreen) {
			WINDOW_FLAGS = WindowManager.LayoutParams.FLAG_FULLSCREEN;
			WINDOW_MASK = WindowManager.LayoutParams.FLAG_FULLSCREEN;
		}
		
		ENABLE_LOADING_INDICATION = !RhoConf.getBool("disable_loading_indication");
		
		initWebStuff();

		Logger.I("Rhodes", "Loading...");
		//showSplashScreen();
		//if (splashScreen != null) {
		//	splashScreen.rho_start();
		//}
		
		// Increase WebView rendering priority
		WebView w = new WebView(ctx);
		WebSettings webSettings = w.getSettings();
		webSettings.setRenderPriority(WebSettings.RenderPriority.HIGH);
		
		// Get screen width/height
		WindowManager wm = (WindowManager)ctx.getSystemService(Context.WINDOW_SERVICE);
		Display d = wm.getDefaultDisplay();
		screenHeight = d.getHeight();
		screenWidth = d.getWidth();
		screenOrientation = d.getOrientation();
		
		DisplayMetrics metrics = new DisplayMetrics();
		d.getMetrics(metrics);
		screenPpiX = metrics.xdpi;
		screenPpiY = metrics.ydpi;
		
		// TODO: detect camera availability
		isCameraAvailable = true;
		
		mGeoLocationInactivityTimeout = RhoConf.getInt("geo_location_inactivity_timeout");
		if (mGeoLocationInactivityTimeout == 0)
			mGeoLocationInactivityTimeout = 25*1000; // 25s
		
		// Register custom uri handlers here
		uriHandlers.addElement(new ExternalHttpHandler(ctx));
		uriHandlers.addElement(new MailUriHandler(ctx));
		uriHandlers.addElement(new TelUriHandler(ctx));
		uriHandlers.addElement(new SmsUriHandler(ctx));
		uriHandlers.addElement(new VideoUriHandler(ctx));
		
		mNM = (NotificationManager)ctx.getSystemService(Context.NOTIFICATION_SERVICE);
		
		try {
			if (Capabilities.PUSH_ENABLED)
				PushService.register();
		} catch (IllegalAccessException e) {
			Log.e(TAG, e.getMessage());
			exitApp();
			return;
		}
		
		Thread init = new Thread(new Runnable() {

			public void run() {
				startRhodesApp();
			}
			
		});
		init.start();
	}
	
	public void startActivity(Intent intent) {
		ctx.startActivity(intent);
	}

	public void exitApp() {
		try {
			if (Capabilities.PUSH_ENABLED)
				PushService.unregister();
		} catch (IllegalAccessException e) {
			Log.e(TAG, e.getMessage());
		}
		
		PerformOnUiThread.exec( new Runnable() {
			public void run() {
				if (wakeLockObject != null) {
					wakeLockObject.release();
					wakeLockObject = null;
					wakeLockEnabled = false;
				}
			}
		}, false);
		getMainActivity().finish();
		Process.killProcess(Process.myPid());
	}
	
	public void activityStarted() {
		if (activitiesActive == 0) {
			if (needGeoLocationRestart) {
				GeoLocation.isKnownPosition();
				needGeoLocationRestart = false;
			}
			PerformOnUiThread.exec( new Runnable() {
				public void run() {
					if (wakeLockEnabled) {
						if (wakeLockObject == null) {
							PowerManager pm = (PowerManager)getInstance().getContext().getSystemService(Context.POWER_SERVICE);
							if (pm != null) {
								Logger.I(TAG, "activityStarted() restore wakeLock object");
								wakeLockObject = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, TAG);
								wakeLockObject.acquire();
							}
							else {
								Logger.E(TAG, "activityStarted() - Can not get PowerManager !!!");
							}
						}
					}
				}
			}, false);
			callActivationCallback(true);
		}
		++activitiesActive;
	}
	
	public void activityStopped() {
		--activitiesActive;
		if (activitiesActive == 0) {
			PerformOnUiThread.exec( new Runnable() {
				public void run() {
					if (wakeLockObject != null) {
						Logger.I(TAG, "activityStopped() temporary destroy wakeLock object");
						wakeLockObject.release();
						wakeLockObject = null;
					}
				}
			}, false);
			needGeoLocationRestart = GeoLocation.isAvailable();
			GeoLocation.stop();
			callActivationCallback(false);
		}
	}
	
	public void rereadScreenProperties() {
		// check for orientarion changed
		// Get screen width/height
		WindowManager wm = (WindowManager)ctx.getSystemService(Context.WINDOW_SERVICE);
		Display d = wm.getDefaultDisplay();
		screenHeight = d.getHeight();
		screenWidth = d.getWidth();
		int newScreenOrientation = d.getOrientation();
		if (newScreenOrientation != screenOrientation) {
			onScreenOrientationChanged(screenWidth, screenHeight, 90);
			screenOrientation = newScreenOrientation; 				
		}
	}
	
	public static void showAboutDialog() {
		PerformOnUiThread.exec(new Runnable() {
			public void run() {
				final AboutDialog aboutDialog = new AboutDialog(Rhodes.getInstance());
				aboutDialog.setTitle("About");
				aboutDialog.setCanceledOnTouchOutside(true);
				aboutDialog.setCancelable(true);
				aboutDialog.show();
			}
		}, false);
	}
	
	public static void showLogView() {
		PerformOnUiThread.exec(new Runnable() {
			public void run() {
				final LogViewDialog logViewDialog = new LogViewDialog(Rhodes.getInstance());
				logViewDialog.setTitle("Log View");
				logViewDialog.setCancelable(true);
				logViewDialog.show();
			}
		}, false);
	}
	
	public static void showLogOptions() {
		PerformOnUiThread.exec(new Runnable() {
			public void run() {
				final LogOptionsDialog logOptionsDialog = new LogOptionsDialog(Rhodes.getInstance());
				logOptionsDialog.setTitle("Logging Options");
				logOptionsDialog.setCancelable(true);
				logOptionsDialog.show();
			}
		}, false);
	}
	
	// Called from native code
	public static void deleteFilesInFolder(String folder) {
		try {
			String[] children = new File(folder).list();
			for (int i = 0; i != children.length; ++i)
				Utils.deleteRecursively(new File(folder, children[i]));
		}
		catch (Exception e) {
			Logger.E(TAG, e);
		}
	}
	
	private static boolean hasNetwork() {
		if (!Capabilities.NETWORK_STATE_ENABLED) {
			Logger.E(TAG, "HAS_NETWORK: Capability NETWORK_STATE disabled");
			return false;
		}
		
		Context ctx = RhoService.getInstance().getContext();
		ConnectivityManager conn = (ConnectivityManager)ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (conn == null)
		{
			Logger.E(TAG, "HAS_NETWORK: cannot create ConnectivityManager");
			return false;
		}
		 
		NetworkInfo[] info = conn.getAllNetworkInfo();
		if (info == null)
		{
			Logger.E(TAG, "HAS_NETWORK: cannot issue getAllNetworkInfo");
			return false;
		}
		
		for (int i = 0, lim = info.length; i < lim; ++i) 
		{
		    //Logger.I(TAG, "HAS_NETWORK: " + info[i].toString() );
			if (info[i].getState() == NetworkInfo.State.CONNECTED)
				return true;
		}

    	Logger.I(TAG, "HAS_NETWORK: all networks are disconnected");
		
		return false;
	}
	
	private static String getCurrentLocale() {
		String locale = Locale.getDefault().getLanguage();
		if (locale.length() == 0)
			locale = "en";
		return locale;
	}
	
	private static String getCurrentCountry() {
		String cl = Locale.getDefault().getCountry();
		return cl;
	}
	
	public static int getScreenWidth() {
		return screenWidth;
	}
	
	public static int getScreenHeight() {
		return screenHeight;
	}
	
	public static Object getProperty(String name) {
		try {
			if (name.equalsIgnoreCase("platform"))
				return "ANDROID";
			else if (name.equalsIgnoreCase("locale"))
				return getCurrentLocale();
			else if (name.equalsIgnoreCase("country"))
				return getCurrentCountry();
			else if (name.equalsIgnoreCase("screen_width"))
				return new Integer(getScreenWidth());
			else if (name.equalsIgnoreCase("screen_height"))
				return new Integer(getScreenHeight());
			else if (name.equalsIgnoreCase("has_camera"))
				return new Boolean(isCameraAvailable);
			else if (name.equalsIgnoreCase("has_network"))
				return hasNetwork();
			else if (name.equalsIgnoreCase("ppi_x"))
				return new Float(screenPpiX);
			else if (name.equalsIgnoreCase("ppi_y"))
				return new Float(screenPpiY);
			else if (name.equalsIgnoreCase("phone_number")) {
				TelephonyManager manager = (TelephonyManager)RhoService.getInstance().
					getContext().getSystemService(Context.TELEPHONY_SERVICE);
				String number = manager.getLine1Number();
				return number;
			}
			else if (name.equalsIgnoreCase("device_name")) {
				return Build.DEVICE;
			}
			else if (name.equalsIgnoreCase("is_emulator")) {
			    String strDevice = Build.DEVICE;
				return new Boolean(strDevice != null && strDevice.equalsIgnoreCase("generic"));
			}
			else if (name.equalsIgnoreCase("os_version")) {
				return Build.VERSION.RELEASE;
			}
			else if (name.equalsIgnoreCase("has_calendar")) {
				return new Boolean(EventStore.hasCalendar());
			}
		}
		catch (Exception e) {
			Logger.E(TAG, "Can't get property \"" + name + "\": " + e);
		}
		
		return null;
	}
	
	public static void exit() {
		RhoService.getInstance().exitApp();
	}
	
	public static String getTimezoneStr() {
		Calendar cal = Calendar.getInstance();
		TimeZone tz = cal.getTimeZone();
		return tz.getDisplayName();
	}
	
	public static void runApplication(String appName, Object params) {
		try {
			Context ctx = RhoService.getInstance().getContext();
			PackageManager mgr = ctx.getPackageManager();
			PackageInfo info = mgr.getPackageInfo(appName, PackageManager.GET_ACTIVITIES);
			if (info.activities.length == 0) {
				Logger.E(TAG, "No activities found for application " + appName);
				return;
			}
			ActivityInfo ainfo = info.activities[0];
			String className = ainfo.name;
			if (className.startsWith("."))
				className = ainfo.packageName + className;

			Intent intent = new Intent();
			intent.setClassName(appName, className);
			if (params != null) {
				Bundle startParams = new Bundle();
				if (params instanceof String) {
					startParams.putString(RHO_START_PARAMS_KEY, (String)params);
				}
				/*
				else if (params instanceof List<?>) {
					for (Object obj : (List<?>)params) {
						startParams.putInt(obj.toString(), 1);
					}
				}
				else if (params instanceof Map<?,?>) {
					Map<?,?> mp = (Map<?,?>)params;
					for (Iterator<?> it = mp.keySet().iterator(); it.hasNext();) {
						Object key = it.next();
						Object value = mp.get(key);
						startParams.putString(key.toString(), value == null ? null : value.toString());
					}
				}
				*/
				else
					throw new IllegalArgumentException("Unknown type of incoming parameter");

				intent.putExtras(startParams);
			}
			ctx.startActivity(intent);
		}
		catch (Exception e) {
			Logger.E(TAG, "Can't run application " + appName + ": " + e.getMessage());
		}
	}
	
	public static boolean isAppInstalled(String appName) {
		try {
			try {
				RhoService.getInstance().getContext().getPackageManager().getPackageInfo(appName, 0);
				return true;
			}
			catch (NameNotFoundException ne) {
				return false;
			}
		}
		catch (Exception e) {
			Logger.E(TAG, "Can't check is app " + appName + " installed: " + e.getMessage());
			return false;
		}
	}
	
	private void updateDownloadNotification(String url, int totalBytes, int currentBytes) {
		Notification n = new Notification();
		n.icon = android.R.drawable.stat_sys_download;
		n.flags |= Notification.FLAG_ONGOING_EVENT;
		
		RemoteViews expandedView = new RemoteViews(ctx.getPackageName(),
				AndroidR.layout.status_bar_ongoing_event_progress_bar);
		
		StringBuilder newUrl = new StringBuilder();
		if (url.length() < 17)
			newUrl.append(url);
		else {
			newUrl.append(url.substring(0, 7));
			newUrl.append("...");
			newUrl.append(url.substring(url.length() - 7, url.length()));
		}
		expandedView.setTextViewText(AndroidR.id.title, newUrl.toString());
		
		StringBuffer downloadingText = new StringBuffer();
		if (totalBytes > 0) {
			long progress = currentBytes*100/totalBytes;
			downloadingText.append(progress);
			downloadingText.append('%');
		}
		expandedView.setTextViewText(AndroidR.id.progress_text, downloadingText.toString());
		expandedView.setProgressBar(AndroidR.id.progress_bar,
				totalBytes < 0 ? 100 : totalBytes,
				currentBytes,
				totalBytes < 0);
		n.contentView = expandedView;
		
		Context context = getContext();
		Intent intent = new Intent(ACTION_ASK_CANCEL_DOWNLOAD);
		n.contentIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
		intent = new Intent(ACTION_CANCEL_DOWNLOAD);
		n.deleteIntent = PendingIntent.getBroadcast(context, 0, intent, 0);

		mNM.notify(DOWNLOAD_PACKAGE_ID, n);
	}
	
	private File downloadPackage(String url) throws IOException {
		final Context ctx = getContext();
		
		final Thread thisThread = Thread.currentThread();
		
		final Runnable cancelAction = new Runnable() {
			public void run() {
				thisThread.interrupt();
			}
		};
		
		BroadcastReceiver downloadReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				String action = intent.getAction();
				if (action.equals(ACTION_ASK_CANCEL_DOWNLOAD)) {
					AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
					builder.setMessage("Cancel download?");
					AlertDialog dialog = builder.create();
					dialog.setButton(AlertDialog.BUTTON_POSITIVE, ctx.getText(android.R.string.yes),
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int which) {
									cancelAction.run();
								}
					});
					dialog.setButton(AlertDialog.BUTTON_NEGATIVE, ctx.getText(android.R.string.no),
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int which) {
									// Nothing
								}
							});
					dialog.show();
				}
				else if (action.equals(ACTION_CANCEL_DOWNLOAD)) {
					cancelAction.run();
				}
			}
		};
		IntentFilter filter = new IntentFilter();
		filter.addAction(ACTION_ASK_CANCEL_DOWNLOAD);
		filter.addAction(ACTION_CANCEL_DOWNLOAD);
		ctx.registerReceiver(downloadReceiver, filter);
		
		File tmpFile = null;
		InputStream is = null;
		OutputStream os = null;
		try {
			updateDownloadNotification(url, -1, 0);
			
			File tmpRootFolder = new File(Environment.getExternalStorageDirectory(), "rhodownload");
			File tmpFolder = new File(tmpRootFolder, ctx.getPackageName());
			if (tmpFolder.exists())
				deleteFilesInFolder(tmpFolder.getAbsolutePath());
			else
				tmpFolder.mkdirs();
			tmpFile = new File(tmpFolder, UUID.randomUUID().toString() + ".apk");
			
			Logger.D(TAG, "Download " + url + " to " + tmpFile.getAbsolutePath() + "...");
			
			URL u = new URL(url);
			URLConnection conn = u.openConnection();
			int totalBytes = -1;
			if (conn instanceof HttpURLConnection) {
				HttpURLConnection httpConn = (HttpURLConnection)conn;
				totalBytes = httpConn.getContentLength();
			}
			is = conn.getInputStream();
			os = new FileOutputStream(tmpFile);
			
			int downloaded = 0;
			updateDownloadNotification(url, totalBytes, downloaded);
			
			long prevProgress = 0;
			byte[] buf = new byte[65536];
			for (;;) {
				if (thisThread.isInterrupted()) {
					tmpFile.delete();
					Logger.D(TAG, "Download of " + url + " was canceled");
					return null;
				}
				int nread = is.read(buf);
				if (nread == -1)
					break;
				
				//Logger.D(TAG, "Downloading " + url + ": got " + nread + " bytes...");
				os.write(buf, 0, nread);
				
				downloaded += nread;
				if (totalBytes > 0) {
					// Update progress view only if current progress is greater than
					// previous by more than 10%. Otherwise, if update it very frequently,
					// user will no have chance to click on notification view and cancel if need
					long progress = downloaded*10/totalBytes;
					if (progress > prevProgress) {
						updateDownloadNotification(url, totalBytes, downloaded);
						prevProgress = progress;
					}
				}
			}
			
			Logger.D(TAG, "File stored to " + tmpFile.getAbsolutePath());
			
			return tmpFile;
		}
		catch (IOException e) {
			if (tmpFile != null)
				tmpFile.delete();
			throw e;
		}
		finally {
			try {
				if (is != null)
					is.close();
			} catch (IOException e) {}
			try {
				if (os != null)
					os.close();
			} catch (IOException e) {}
			
			mNM.cancel(DOWNLOAD_PACKAGE_ID);
			ctx.unregisterReceiver(downloadReceiver);
		}
	}
	
	public static void installApplication(final String url) {
		Thread bgThread = new Thread(new Runnable() {
			public void run() {
				try {
					final RhoService r = RhoService.getInstance();
					final File tmpFile = r.downloadPackage(url);
					if (tmpFile != null) {
						PerformOnUiThread.exec(new Runnable() {
							public void run() {
								try {
									Logger.D(TAG, "Install package " + tmpFile.getAbsolutePath());
									Uri uri = Uri.fromFile(tmpFile);
									Intent intent = new Intent(Intent.ACTION_VIEW);
									intent.setDataAndType(uri, "application/vnd.android.package-archive");
									r.getContext().startActivity(intent);
								}
								catch (Exception e) {
									Log.e(TAG, "Can't install file from " + tmpFile.getAbsolutePath(), e);
									Logger.E(TAG, "Can't install file from " + tmpFile.getAbsolutePath() + ": " + e.getMessage());
								}
							}
						}, false);
					}
				}
				catch (IOException e) {
					Log.e(TAG, "Can't download package from " + url, e);
					Logger.E(TAG, "Can't download package from " + url + ": " + e.getMessage());
				}
			}
		});
		bgThread.setPriority(Thread.MIN_PRIORITY);
		bgThread.start();
	}
	
	public static void uninstallApplication(String appName) {
		try {
			Uri packageUri = Uri.parse("package:" + appName);
			Intent intent = new Intent(Intent.ACTION_DELETE, packageUri);
			RhoService.getInstance().startActivity(intent);
		}
		catch (Exception e) {
			Logger.E(TAG, "Can't uninstall application " + appName + ": " + e.getMessage());
		}
	}

	public static void openExternalUrl(String url) {
		try {
			Context ctx = RhoService.getInstance().getContext();
            Uri uri = Uri.parse(url);			
            
		    Intent intent = new Intent(Intent.ACTION_VIEW);
		    intent.setData(uri);
		    
		    ctx.startActivity(Intent.createChooser(intent, "Open in..."));
		}
		catch (Exception e) {
			Logger.E(TAG, "Can't open url :" + url + ": " + e.getMessage());
		}
	}

	public native void setPushRegistrationId(String id);
	
	private native boolean callPushCallback(String data);
	
	public void handlePushMessage(Intent intent) {
		Logger.D(TAG, "Receive PUSH message");
		
		Bundle extras = intent.getExtras();
		if (extras == null) {
			Logger.W(TAG, "Empty PUSH message received");
			return;
		}
		
		StringBuilder builder = new StringBuilder();
		
		Set<String> keys = extras.keySet();
		// Remove system related keys
		keys.remove("collapse_key");
		keys.remove("from");
		
		for (String key : keys) {
			Logger.D(TAG, "PUSH item: " + key);
			Object value = extras.get(key);
			if (builder.length() > 0)
				builder.append("&");
			builder.append(key);
			builder.append("=");
			if (value != null)
				builder.append(value.toString());
		}
		
		String data = builder.toString();
		Logger.D(TAG, "Received PUSH message: " + data);
		if (callPushCallback(data))
			return;
		
		String alert = extras.getString("alert");
		if (alert != null) {
			Logger.D(TAG, "PUSH: Alert: " + alert);
			Alert.showPopup(alert);
		}
		String sound = extras.getString("sound");
		if (sound != null) {
			Logger.D(TAG, "PUSH: Sound file name: " + sound);
			Alert.playFile("/public/alerts/" + sound, null);
		}
		String vibrate = extras.getString("vibrate");
		if (vibrate != null) {
			Logger.D(TAG, "PUSH: Vibrate: " + vibrate);
			int duration;
			try {
				duration = Integer.parseInt(vibrate);
			}
			catch (NumberFormatException e) {
				duration = 5;
			}
			Logger.D(TAG, "Vibrate " + duration + " seconds");
			Alert.vibrate(duration);
		}
		
		String syncSources = extras.getString("do_sync");
		if (syncSources != null) {
			Logger.D(TAG, "PUSH: Sync:");
			boolean syncAll = false;
			for (String source : syncSources.split(",")) {
				Logger.D(TAG, "url = " + source);
				if (source.equalsIgnoreCase("all"))
					syncAll = true;
				else {
					doSyncSource(source.trim());
				}
			}
			
			if (syncAll)
				doSyncAllSources(true);
		}
	}

}
