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
package com.rhomobile.rhodes.phonebook;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import android.os.Build;

import com.rhomobile.rhodes.Capabilities;
import com.rhomobile.rhodes.Logger;

//@RubyLevelClass(name="Phonebook")
public class Phonebook {

	private static final String TAG = "Phonebook";
	private static final boolean logging_enable = false;
	
	public static final String PB_ID = "id";
	public static final String PB_FIRST_NAME = "first_name";
	public static final String PB_LAST_NAME = "last_name";
	public static final String PB_MOBILE_NUMBER = "mobile_number";
	public static final String PB_HOME_NUMBER = "home_number";
	public static final String PB_BUSINESS_NUMBER = "business_number";
	public static final String PB_EMAIL_ADDRESS = "email_address";
	public static final String PB_COMPANY_NAME = "company_name";
	
	public static final int PB_FIELDS_COUNT = 8;
	public static final int PB_I_ID = 0;
	public static final int PB_I_FIRST_NAME = 1;
	public static final int PB_I_LAST_NAME = 2;
	public static final int PB_I_MOBILE_NUMBER = 3;
	public static final int PB_I_HOME_NUMBER = 4;
	public static final int PB_I_BUSINESS_NUMBER = 5;
	public static final int PB_I_EMAIL_ADDRESS = 6;
	public static final int PB_I_COMPANY_NAME = 7;
	
	
	
	
	
	private Map<String, Contact> contactList;
	private ContactAccessor accessor;
	private Iterator<Contact> iter = null;
	private boolean mIsFullListReceived = false;

	private boolean checkState() {
		if (!Capabilities.PIM_ENABLED)
			Logger.E(TAG, "Can not execute: PIM disabled");
		return Capabilities.PIM_ENABLED;
	}
	
	private ContactAccessor createAccessor() {
		String className;
		int sdkVersion = Integer.parseInt(Build.VERSION.SDK);
		if (sdkVersion < Build.VERSION_CODES.ECLAIR)
			className = "ContactAccessorOld";
		else
			className = "ContactAccessorNew";
		
		try {
			String pkgname = ContactAccessor.class.getPackage().getName();
			String fullName = pkgname + "." + className;
			Class<? extends ContactAccessor> klass =
				Class.forName(fullName).asSubclass(ContactAccessor.class);
			return klass.newInstance();
		}
		catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}
	
	public Phonebook() {
		try {
			if (!checkState())
				return;
			
			accessor = createAccessor();
			contactList = new HashMap<String, Contact>();
		}
		catch (Exception e) {
			Logger.E(TAG, e);
		}
	}
	
	public void prepareFullList() {
		if (!checkState())
			return;
		try {
			if (!mIsFullListReceived) {
				Logger.I(TAG, "Phonebook.prepareFullList()");
				contactList = accessor.getAll();
				mIsFullListReceived = true;
			}
			//moveToBegin();
		}
		catch (Exception e) {
			Logger.E(TAG, e);
		}
	}
	
	public void close() {
		try {
			if (!checkState())
				return;
			
			this.contactList.clear();
		}
		catch (Exception e) {
			Logger.E(TAG, e);
		}
	}

	public void moveToBegin() {
		try {
			if (!checkState())
				return;
			if (!mIsFullListReceived) {
				prepareFullList();
			}	
			iter = contactList.values().iterator();
		}
		catch (Exception e) {
			Logger.E(TAG, e);
		}
	}
	
	public boolean hasNext() {
		try {
			if (!checkState())
				return false;
			//if (!mIsFullListReceived) {
			//	prepareFullList();
			//}	
			
			return iter.hasNext();
		}
		catch (Exception e) {
			Logger.E(TAG, e);
			return false;
		}
	}
	
	public Object next() {
		try {
			if (!checkState())
				return null;
			//if (!mIsFullListReceived) {
			//	prepareFullList();
			//}	
			
			return iter.next();
		}
		catch (Exception e) {
			Logger.E(TAG, e);
			return null;
		}
	}
	
	public Contact getFirstRecord() {
		try {
			if (!checkState())
				return null;
			//if (!mIsFullListReceived) {
			//	prepareFullList();
			//}	
			
			moveToBegin();
			if (!iter.hasNext())
				return null;
			return iter.next();
		}
		catch (Exception e) {
			Logger.E(TAG, e);
			return null;
		}
	}
	
	public Contact getNextRecord() {
		try {
			if (!checkState())
				return null;
			//if (!mIsFullListReceived) {
			//	prepareFullList();
			//}	
			
			return iter.next();
		}
		catch (Exception e) {
			Logger.E(TAG, e);
			return null;
		}
	}
	
	public Contact getRecord(String idd) {
		if (logging_enable) Logger.I(TAG, "Phonebook.getRecord("+idd+")");
		try {
			//if (!checkState())
			//	return null;
			if (contactList == null) {
				if (logging_enable) Logger.I(TAG, "Phonebook.getRecord() contackList is null !");
			}
			if ((accessor != null) && (accessor instanceof ContactAccessorNew)) {
			//if (false) {
				if (logging_enable) Logger.I(TAG, "Phonebook.getRecord() new accessor !");
				Contact c = contactList.get(idd);
				if (c != null) {
					if (logging_enable) Logger.I(TAG, "Phonebook.getRecord() found in list");
					return c;
				}
				c = ((ContactAccessorNew)accessor).getContactByID(Contact.convertRhodeIDtoPlatformID(idd));
				if (c != null) {
					if (logging_enable) Logger.I(TAG, "Phonebook.getRecord() found in system");
					contactList.put(c.getField(Phonebook.PB_I_ID), c);
				}
				else {
					if (logging_enable) Logger.I(TAG, "Phonebook.getRecord() not found in system");
				}
				return c;
			}
			else {
				if (logging_enable) Logger.I(TAG, "Phonebook.getRecord() old accessor class");
				if (!mIsFullListReceived) {
					prepareFullList();
				}	
			}
			Contact cc = contactList.get(idd);
			if (cc != null) {
				if (logging_enable) Logger.I(TAG, "Phonebook.getRecord() return record");
			}
			else {
				if (logging_enable) Logger.I(TAG, "Phonebook.getRecord() return NULL");
			}
			return cc;
		}
		catch (Exception e) {
			Logger.E(TAG, e);
			return null;
		}
	}

	public void removeContact(Contact contact) {
		try {
			if (!checkState())
				return;
			
			accessor.remove(contact);
			contactList.remove(contact.getField(Phonebook.PB_I_ID));
		}
		catch (Exception e) {
			Logger.E(TAG, e);
		}
	}

	public void saveContact(Contact contact) {
		try {
			if (!checkState())
				return;
			
			accessor.save(contact);
			contactList.put(contact.getField(Phonebook.PB_I_ID), contact);
		}
		catch (Exception e) {
			Logger.E(TAG, e);
		}
	}

}
