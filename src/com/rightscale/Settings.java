package com.rightscale;

import net.xeger.rest.RestAuthException;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;

import com.rightscale.provider.DashboardError;

public class Settings extends PreferenceActivity {
	public static final String ACTION_NOTIFY_ERROR = "notify_error";
	
	public static final int DIALOG_ERROR_ID      = 0;

	DashboardError _error = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.settings);
		if(getIntent().getAction().equals(ACTION_NOTIFY_ERROR)) {
			_error = (DashboardError)getIntent().getExtras().get("error");
			this.showDialog(DIALOG_ERROR_ID);
		}
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		AlertDialog.Builder builder  = new AlertDialog.Builder(this);
		
		if(id == DIALOG_ERROR_ID && _error != null) {
			if(_error.getCause() instanceof RestAuthException) {
				builder.setTitle(R.string.auth_error_dialog_title)
				   .setMessage(R.string.auth_error_dialog_message);				
			}
			else {
				builder.setTitle(R.string.error_dialog_title)
				   .setMessage(R.string.error_dialog_message);
				
			}
		}
		else {
			return super.onCreateDialog(id);			
		}

       builder.setCancelable(false)
       .setNeutralButton("OK", new DialogInterface.OnClickListener() {
           public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
           }
       });
       
       return builder.create();
	}
	
	public static String getEmail(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context).getString("email", null);
	}
	
	public static String getPassword(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context).getString("password", null);		
	}
	
	public static void handleError(Throwable t, Context context) {
		if(t instanceof DashboardError) {
			Log.e("DashboardError", t.getCause().toString());
			Intent intent = new Intent(Settings.ACTION_NOTIFY_ERROR, null, context, Settings.class);
			intent.putExtra("error", t);
			
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);			
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
