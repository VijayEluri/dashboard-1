package com.rightscale.dashboard;

import com.rightscale.provider.Dashboard;

import net.xeger.rest.RestException;
import net.xeger.rest.ui.ContentConsumer;
import net.xeger.rest.ui.ContentProducer;
import net.xeger.rest.ui.ContentTransfer;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;

public class IndexAccounts extends Activity implements ContentProducer, ContentConsumer
{
	static public final String ACCOUNTS = "accounts";
	
    static private final String[] FROM = {"nickname"};
    static private final int[]    TO   = {android.R.id.text1};

	protected Helper         _helper;
	
	protected Cursor         _cursor;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		setContentView(R.layout.index_accounts);
		_helper = new Helper(this, null);	
		_helper.onCreate(); //only show progress dialog on the first load    			
	}

	@Override
	public void onStart() {
    	super.onStart();
		_helper.onStart();
		loadContent();		
	}
	
	@Override
	public void onPause() {
		super.onPause();
		_helper.onPause();
	}
	
	@Override
	public void onResume() {
		super.onResume();
		_helper.onResume();
	}

	@Override
	public void onStop() {
		super.onStop();
		_helper.onStop();
	}
    
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	super.onCreateOptionsMenu(menu);
    	MenuInflater inflater = getMenuInflater();
    	inflater.inflate(R.menu.index_accounts, menu);
    	return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	Intent i = null;
    	
    	switch(item.getItemId()) {
    	case R.id.menu_settings:
    		i = new Intent(this, Settings.class);
    		break;
    	}
    	
    	if(i != null) {
    		startActivity(i);
    		return true;
    	}
    	else {
    		return false;
    	}
    }

    public void loadContent() {
    	_helper.onLoadContent();
		ContentTransfer.load(this, this, new Handler(), ACCOUNTS);
    }
    
	public Cursor produceContent(String tag) throws RestException {
		if(tag == ACCOUNTS) {
	    	ContentResolver cr = getContentResolver();
	    	Uri indexAccounts = Uri.withAppendedPath(Dashboard.BASE_CONTENT_URI, "accounts");
	    	return cr.query(indexAccounts, Dashboard.ACCOUNT_COLUMNS, null, null, null);			
		}
		else {
			return null;
		}
	}

	public void consumeContent(Cursor cursor, String tag) {
		_helper.onConsumeContent();
		
		if(tag == ACCOUNTS) {
			_cursor = cursor;
			startManagingCursor(cursor);
	    	SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, android.R.layout.simple_spinner_item, cursor, FROM, TO);
	    	adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

	    	Spinner spinner = (Spinner)findViewById(R.id.index_accounts_spinner);
	    	spinner.setAdapter(adapter);

			if(_cursor.getCount() == 1) {
				_cursor.moveToFirst();
				int idxId = _cursor.getColumnIndexOrThrow(Dashboard.ID);
	        	Intent i = new Intent(Intent.ACTION_VIEW, Routes.indexDeployments(_cursor.getString(idxId)));
	        	startActivity(i);						    	
	    	}
	    	else {
		    	Button button = (Button)findViewById(R.id.index_accounts_connect);
		    	button.setOnClickListener(new OnClickListener() {
					public void onClick(View arg0) {
				    	Spinner spinner = (Spinner)findViewById(R.id.index_accounts_spinner);
						_cursor.moveToPosition(spinner.getSelectedItemPosition());
						int idxId = _cursor.getColumnIndexOrThrow(Dashboard.ID);
			        	Intent i = new Intent(Intent.ACTION_VIEW, Routes.indexDeployments(_cursor.getString(idxId)));
			        	startActivity(i);					
					}	    		
		    	});	    		
	    	}
		}		
	}

	public void consumeContentError(Throwable throwable, String tag) {
		_helper.onConsumeContentError(throwable);
		finish();
	}
	

}
