package com.rightscale;

import net.xeger.rest.RestException;
import net.xeger.rest.ui.ContentConsumer;
import net.xeger.rest.ui.ContentProducer;
import net.xeger.rest.ui.ContentTransfer;
import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.rightscale.service.DashboardFeed;

public abstract class AbstractDashboardActivity extends ListActivity implements ContentProducer, ContentConsumer {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ContentTransfer.load(this, this, new Handler());        
    }

    public void onStart() {
    	super.onStart();
        Intent feedIntent = new Intent(this, DashboardFeed.class);
        startService(feedIntent);    	
    }

    public void onStop() {
    	super.onStop();
        Intent feedIntent = new Intent(this, DashboardFeed.class);
    	this.stopService(feedIntent);
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
        	i = new Intent(Intent.ACTION_VIEW, Routes.indexDeployments());
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

	abstract public Cursor produceContent(String tag) throws RestException;
	abstract public void consumeContent(Cursor c, String tag);

	public void consumeContentError(Throwable t, String tag) {
		Settings.handleError(t, this);
		finish();
	}	
}
