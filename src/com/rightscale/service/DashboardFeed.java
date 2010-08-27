package com.rightscale.service;

import java.net.URI;
import java.net.URISyntaxException;

import net.xeger.rest.RestException;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.rightscale.Settings;
import com.rightscale.provider.DashboardSession;

public class DashboardFeed extends Service {
	public static final String FEED_HOST   = "moo1.rightscale.com"; //TODO make this configurable
	public static final String FEED_PREFIX = "https://" + FEED_HOST + "/user_notifications/feed.atom?feed_token=";	
	public static final String HARDCODED_TOKEN = "38ecd5ab72d787d323e837ee2064a3334d1f5139"; //TODO make this configurable
	
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
			DashboardSession session = new DashboardSession(Settings.getEmail(this), Settings.getPassword(this));
			session.login();

			URI uri = new URI(FEED_PREFIX + HARDCODED_TOKEN);

			_scraper       = new FeedScraper(session, uri);
			_thread = new Thread(_scraper);
			_thread.start();			
		}
		catch(URISyntaxException e) {
    		Log.e("FeedScraper", e.getMessage());
			e.printStackTrace();
		}
		catch(RestException e) {
			Log.e("DashboardFeed", e.getMessage());
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

    /**
     * Show a notification while this service is running.
     */
//    private void showNotification() {
//        // In this sample, we'll use the same text for the ticker and the expanded notification
//        CharSequence text = getText(R.string.local_service_started);
//
//        // Set the icon, scrolling text and timestamp
//        Notification notification = new Notification(R.drawable.stat_sample, text,
//                System.currentTimeMillis());
//
//        // The PendingIntent to launch our activity if the user selects this notification
//        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
//                new Intent(this, LocalServiceActivities.Controller.class), 0);
//
//        // Set the info for the views that show in the notification panel.
//        notification.setLatestEventInfo(this, getText(R.string.local_service_label),
//                       text, contentIntent);
//
//        // Send the notification.
//        // We use a layout id because it is a unique number.  We use it later to cancel.
//        mNM.notify(R.string.local_service_started, notification);
//    }

}
