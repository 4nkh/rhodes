/*
 ============================================================================
 Author	    : Dmitry Moskalchuk
 Version	: 1.5
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

import com.rhomobile.rhodes.util.PerformOnUiThread;

import android.net.Uri;
import android.webkit.CookieManager;

public class WebView {
	
	private static final String TAG = "WebView";
	
	private static void reportFail(String name, Exception e) {
		Logger.E(TAG, "Call of \"" + name + "\" failed: " + e.getMessage());
	}
	
	private static class NavigateTask implements Runnable {
		private String url;
		private int index;
		
		public NavigateTask(String u, int i) {
			url = u;
			index = i;
		}
		
		public void run() {
			RhodesService r = RhodesService.getInstance();
			r.getMainView().navigate(url, index);
		}
	};
	
	private static class NavigateBackTask implements Runnable {
		public void run() {
			RhodesService r = RhodesService.getInstance();
			r.getMainView().goBack();
		}
	};
	
	private static class RefreshTask implements Runnable {
		private int index;
		
		public RefreshTask(int i) {
			index = i;
		}
		
		public void run() {
			RhodesService r = RhodesService.getInstance();
			r.getMainView().reload(index);
		}
	};
	
	private static class SetCookieTask implements Runnable {
		private String url;
		private String cookie;
		
		public SetCookieTask(String u, String c) {
			Uri uri = Uri.parse(u);
			url = uri.getScheme() + "://" + uri.getHost() + "/" + uri.getPath();
			cookie = c;
		}
		
		public void run() {
			CookieManager mgr = CookieManager.getInstance();
			mgr.setCookie(url, cookie);
		}
	};

	public static void navigate(String url, int index) {
		try {
			PerformOnUiThread.exec(new NavigateTask(url, index), false);
		}
		catch (Exception e) {
			reportFail("navigate", e);
		}
	}
	
	public static void navigateBack() {
		try {
			PerformOnUiThread.exec(new NavigateBackTask(), false);
		}
		catch (Exception e) {
			reportFail("navigateBack", e);
		}
	}
	
	public static void refresh(int index) {
		try {
			PerformOnUiThread.exec(new RefreshTask(index), false);
		}
		catch (Exception e) {
			reportFail("refresh", e);
		}
	}
	
	public static int activeTab() {
		try {
			RhodesService r = RhodesService.getInstance();
			return r.getMainView().activeTab();
		}
		catch (Exception e) {
			reportFail("activeTab", e);
		}
		
		return 0;
	}
	
	public static void executeJs(String js, int index) {
		try {
			PerformOnUiThread.exec(new NavigateTask("javascript:" + js, index), false);
		}
		catch (Exception e) {
			reportFail("executeJs", e);
		}
	}
	
	public static void setCookie(String url, String cookie) {
		try {
			PerformOnUiThread.exec(new SetCookieTask(url, cookie), false);
		}
		catch (Exception e) {
			reportFail("setCookie", e);
		}
	}
}
