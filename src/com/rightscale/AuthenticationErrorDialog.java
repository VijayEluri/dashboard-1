package com.rightscale;

import android.app.Dialog;
import android.content.Context;

public class AuthenticationErrorDialog extends Dialog {
	public AuthenticationErrorDialog(Context context) {
		super(context);
		setTitle("Authentication Error");
		setContentView(R.layout.authentication_error);
	}
}
