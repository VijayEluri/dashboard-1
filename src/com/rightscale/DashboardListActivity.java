package com.rightscale;

import net.xeger.rest.ui.*;

import com.rightscale.provider.Dashboard;
import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public abstract class DashboardListActivity extends ListActivity implements ContentProducer, ContentConsumer {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);        
        ContentTransfer.load(this, this, new Handler());
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

	abstract public Cursor produceContent(Object tag);
	abstract public void consumeContent(Cursor c, Object tag);

	public void consumeContentError(Throwable t, Object tag) {
		Settings.handleError(t, this);
		finish();
	}	
}
