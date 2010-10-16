// Dashboard: an Android front-end to the RightScale dashboard
// Copyright (C) 2009 Tony Spataro <code@tracker.xeger.net>
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program.  If not, see <http://www.gnu.org/licenses/>.

package com.rightscale.app.dashboard;

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

public abstract class AbstractAccountActivity extends ListActivity implements ContentProducer, ContentConsumer {
	private static String HARDCODED_ACCOUNT_ID = "2951"; // 2951 = DEMO

	protected Helper            _helper   = null;
	protected boolean           _loaded   = false;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    	if(getIntent().getData() != null) {
    		_helper = new Helper(this, Routes.getAccountId(getIntent().getData()));
    	}
    	else {
    		//HACK if someone launches us without a data-ful intent (really we should hit up a dashboard here!)
    		_helper = new Helper(this, HARDCODED_ACCOUNT_ID);
    	}    	

    	_helper.onCreate();
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
    	inflater.inflate(R.menu.main, menu);
    	return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	Intent i = null;
    	
    	switch(item.getItemId()) {
    	case R.id.menu_accounts:
        	i = new Intent(Intent.ACTION_VIEW, Routes.indexAccounts());
        	break;
    	case R.id.menu_deployments:
        	i = new Intent(Intent.ACTION_VIEW, Routes.indexDeployments(_helper.getAccountId()));
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

    public void loadContent() {
    	if(!_loaded) {
    		_helper.onLoadContent();
    		ContentTransfer.load(this, this, new Handler(), null);
    	}
    }
    
	abstract public Cursor produceContent(String tag) throws RestException;

	public void consumeContent(Cursor c, String tag) {
		_loaded = true;
		_helper.onConsumeContent();
	}

	public void consumeContentError(Throwable t, String tag) {
		_helper.onConsumeContentError(t);
		finish();
	}	
}