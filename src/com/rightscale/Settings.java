package com.rightscale;

import net.xeger.rest.RestException;

import com.rightscale.provider.DashboardError;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;

public class Settings extends PreferenceActivity {
	public static final String ACTION_NOTIFY_ERROR = "notify_error";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.settings);
		
		if(getIntent().getAction() == ACTION_NOTIFY_ERROR) {
			this.showDialog(R.layout.error_dialog);
		}
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		return new ErrorDialog(getBaseContext());
	}
	
	public static String getEmail(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context).getString("email", null);
	}
	
	public static String getPassword(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context).getString("password", null);		
	}
	
	public static void handleError(Throwable t, Context context) {
		if(t instanceof DashboardError || t instanceof RestException) {
			Throwable cause = t.getCause();
			Log.e("DashboardError", cause.toString());
			Intent intent = new Intent(Settings.ACTION_NOTIFY_ERROR, null, context, Settings.class);
			intent.putExtra("error", t);
			intent.putExtra("cause", cause);
			
			context.startActivity(intent);
		}
		else if(t instanceof RuntimeException) {
			throw (RuntimeException)t;
		}
		else {
			throw new Error(t);
		}
	}
}
