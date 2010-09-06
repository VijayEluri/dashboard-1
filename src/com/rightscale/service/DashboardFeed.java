package com.rightscale.service;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.xeger.rest.RestException;
import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.rightscale.Settings;
import com.rightscale.provider.DashboardSession;

public class DashboardFeed extends Service {
	public static final String CATEGORY_EVENT = "com.rightscale.service.DashboardFeed.category.event";
	
	public static final String HARDCODED_DEBUG_URL="https://my.rightscale.com/user_notifications/feed.atom?feed_token=fe2c2fbdbbe634c8ecae7237222b50f4011935a2";
	
    /**
     * Class for clients to access.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with
     * IPC.
     */
    public class LocalBinder extends Binder {
        DashboardFeed getService() {
            return DashboardFeed.this;
        }
    }

    Thread      _thread = null;
    FeedScraper _scraper       = null;    
    
    @Override
    public void onCreate() {
    }

    @Override
    public void onStart(Intent intent, int startId) {
        Log.i("LocalService", "Received start id " + startId + ": " + intent);

        if(_scraper != null) {
        	return;
        }
        
        try {
			DashboardSession session = new DashboardSession(Settings.getEmail(this), Settings.getPassword(this), Settings.getSystem(this));
			session.login();

			//TODO undo hack
			URI uri = new URI(HARDCODED_DEBUG_URL);

			_scraper       = new FeedScraper(this, session, uri);
			_thread = new Thread(_scraper);
			_thread.start();			
		}
		catch(URISyntaxException e) {
			e.printStackTrace();
		}
		catch(RestException e) {
			e.printStackTrace();
			//TODO: send an error broadcast/notification (launch Settings activity - failed auth?)
		}
    }

    @Override
    public void onDestroy() {
    	_scraper.stop();
    	_thread.interrupt();
    	_scraper = null;
    	_thread = null;
    }
    
    // This is the object that receives interactions from clients.  See
    // RemoteService for a more complete example.
    private final IBinder _binder = new LocalBinder();

    @Override
    public IBinder onBind(Intent intent) {
        return _binder;
    }

	//HACK we remember a fixed # of things, could have horrible consequences!
    //TODO remember up until a certain time ago (5 min?)
    private Set<String>  _seenSet  = new HashSet<String>();
    private List<String> _seenList = new ArrayList<String>();
    
    synchronized boolean onDashboardEvent(Date when, Uri subject, String subjectName, String summary) {
    	StringBuffer sb = new StringBuffer(subject.toString());
    	sb.append(":");
    	sb.append(new Long(when.getTime()).toString());
    	String hash = sb.toString();

    	boolean likedIt = false;

		if(!_seenSet.contains(hash)) { 
			_seenSet.add(hash);
			_seenList.add(hash);
	    	Intent intent = new Intent(Intent.ACTION_VIEW, subject);
	    	intent.addCategory(CATEGORY_EVENT);
	    	intent.putExtra("name", subjectName);
	    	intent.putExtra("summary", subjectName);
	    	sendBroadcast(intent);
	    	likedIt = true;
		}

		while(_seenSet.size() > 100) {
			_seenSet.remove(_seenList.get(0));
			_seenList.remove(0);
		}
		
		return likedIt;
    }
}
