package com.rightscale.dashboard;

import net.xeger.rest.ProtocolError;
import net.xeger.rest.RestAuthException;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;

import com.rightscale.provider.DashboardError;

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
