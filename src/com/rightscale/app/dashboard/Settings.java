// Dashboard: an Android front-end to the RightScale dashboard
// Copyright (C) 2009 Tony Spataro <code@tracker.xeger.net>
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program.  If not, see <http://www.gnu.org/licenses/>.

package com.rightscale.app.dashboard;

import android.content.Context;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

public class Settings extends PreferenceActivity {
	public static final String DEFAULT_SYSTEM = "my.rightscale.com";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.settings);

		String email = getEmail(this);
		if( (email == null) || !email.endsWith("@rightscale.com")) {
			//Bit of obfuscation: hide the system unless the user's email ends with @rightscale.com
			Preference system = this.findPreference("system");
			getPreferenceScreen().removePreference(system);
		}		
	}
	
	public static String getEmail(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context).getString("email", null);
	}
	
	public static String getPassword(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context).getString("password", null);		
	}

	public static String getSystem(Context context) {
		String system = PreferenceManager.getDefaultSharedPreferences(context).getString("system", DEFAULT_SYSTEM);
		
		if(system != null && system.endsWith("rightscale.com")) {
			return system;
		}
		else {
			return DEFAULT_SYSTEM;
		}
	}
}
