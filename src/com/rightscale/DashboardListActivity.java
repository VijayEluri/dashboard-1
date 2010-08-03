package com.rightscale;

import com.rightscale.provider.Dashboard;
import com.rightscale.provider.DashboardError;

import net.xeger.rest.RestAuthException;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public abstract class DashboardListActivity extends ListActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);        
        new LoadContentThread(this, new Handler()).start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	super.onCreateOptionsMenu(menu);
    	MenuInflater inflater = getMenuInflater();
    	inflater.inflate(R.menu.main, menu);
    	return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	Intent i = null;
    	
    	switch(item.getItemId()) {
    	case R.id.menu_deployments:
        	i = new Intent(Intent.ACTION_VIEW, Dashboard.DEPLOYMENTS_URI);
        	break;
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
        
	abstract protected Cursor loadContent();
	abstract protected void consumeContent(Cursor c);

	protected void consumeError(Throwable t) {
		if(t instanceof DashboardError) {
			Throwable cause = t.getCause();
			Uri uri = Uri.fromParts("content", cause.getClass().getName(), null);
			finish();
			startActivity(new Intent(Settings.ACTION_NOTIFY_ERROR, null, this, Settings.class));
		}
		else if(t instanceof RuntimeException) {
			throw (RuntimeException)t;
		}
		else {
			throw new Error(t);
		}					
	}
	
	private class LoadContentThread extends Thread {
		DashboardListActivity _activity;
		Handler               _handler;
		
		public LoadContentThread(DashboardListActivity a, Handler h) {
			_activity = a;
			_handler  = h;
		}
		
		public void run() {
			try {
				_handler.post( new ContentConsumer(_activity, loadContent()) );
			}
			catch(Throwable t) {
				_handler.post( new ErrorConsumer(_activity, t) );
			}
		}
	}
	
	private class ErrorConsumer implements Runnable {
		DashboardListActivity _activity;
		Throwable             _throwable;
		
		public ErrorConsumer(DashboardListActivity a, Throwable t) {
			_activity = a;
			_throwable = t;
		}
		
		public void run() {
			_activity.consumeError(_throwable);
		}
	}
	
	private class ContentConsumer implements Runnable {
		DashboardListActivity _activity;
		Cursor      _content = null;
		
		public ContentConsumer(DashboardListActivity a, Cursor content) {
			_activity = a;
			_content  = content;
		}
		
		public void run() {
			if(_content != null) {
				_activity.consumeContent(_content);
			}
		}
	}
}
