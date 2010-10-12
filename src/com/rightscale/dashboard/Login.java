package com.rightscale.dashboard;

import net.xeger.rest.ProtocolError;
import net.xeger.rest.RestAuthException;

import com.rightscale.provider.DashboardError;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class Login extends Activity {
	public static final Uri SIGN_UP_URI = Uri.parse("http://www.rightscale.com/products/free_edition.php");
	
	public static final String ACTION_NOTIFY_ERROR = "notify_error";	
	public static final int DIALOG_ERROR_ID      = 0;

	private Throwable _error             = null;
	private boolean   _errorAcknowledged = false; 
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		setContentView(R.layout.login);

		Button settings = (Button)findViewById(R.id.settings);
		Button sign_up  = (Button)findViewById(R.id.sign_up);

		settings.setOnClickListener(new OnClickListener() {
			public void onClick(View view) {
				Intent intent = new Intent(Login.this, Settings.class);
				startActivity(intent);
				_errorAcknowledged = true;
			}
		});
		
		sign_up.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				Intent intent = new Intent(Intent.ACTION_VIEW, SIGN_UP_URI);
				intent.addCategory(Intent.CATEGORY_BROWSABLE);
				startActivity(intent);
				_errorAcknowledged = true;
			}			
		});
		
		explainErrors();		
	}

	@Override
	public void onResume() {
		super.onResume();
		if(_errorAcknowledged) {
			startActivity(new Intent(this, IndexAccounts.class));
			finish();			
		}
	}
	
	protected void explainErrors() {
		boolean hasCreds = (Settings.getEmail(this) != null && Settings.getPassword(this) != null);
		
		if( (getIntent() != null) && (getIntent().getAction() != null) && getIntent().getAction().equals(ACTION_NOTIFY_ERROR) ) {
			_error = (Throwable)getIntent().getExtras().get("error");
		}
				
		boolean wasError     = (_error != null);
		boolean wasAuthError = (wasError && _error.getCause() instanceof RestAuthException);

		TextView text     = (TextView)findViewById(R.id.message);
		Button   settings = (Button)findViewById(R.id.settings);
		Button   signUp   = (Button)findViewById(R.id.sign_up);
		
		if(hasCreds && wasAuthError) {
			setTitle(getString(R.string.authentication_error_title));
			text.setText(getString(R.string.authentication_error_message));
			settings.setVisibility(View.VISIBLE);
			signUp.setVisibility(View.GONE);
		}
		else if(hasCreds && wasError) {
			setTitle(getString(R.string.generic_error_title));
			text.setText(getString(R.string.generic_error_message));
			settings.setVisibility(View.VISIBLE);
			signUp.setVisibility(View.GONE);
		}
		else {
			text.setText(getString(R.string.initial_login_greeting));
			settings.setVisibility(View.VISIBLE);
			signUp.setVisibility(View.VISIBLE);
		}
	}
	
	public static void handleError(Throwable t, Context context) {
		if(t instanceof DashboardError || t instanceof ProtocolError) {
			Intent intent = new Intent(ACTION_NOTIFY_ERROR, null, context, Login.class);
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
