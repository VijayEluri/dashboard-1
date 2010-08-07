package com.rightscale;

import android.app.Dialog;
import android.content.Context;

public class ErrorDialog extends Dialog {
	public ErrorDialog(Context context) {
		super(context);
		
		setTitle("Error");
		setContentView(R.layout.error_dialog);
	}
}
