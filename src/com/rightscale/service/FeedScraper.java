package com.rightscale.service;

import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import net.xeger.rest.AbstractResource;
import net.xeger.rest.Session;

import org.apache.http.HttpEntity;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import com.rightscale.Routes;

import android.net.Uri;
import android.util.Log;

class FeedScraper extends AbstractResource implements Runnable {
	static final int DEFAULT_POLL_PERIOD = 15000;
	static final int MAX_POLL_PERIOD     = 60000;
	static final int ERROR_POLL_PERIOD   = MAX_POLL_PERIOD;
	
	static final SimpleDateFormat ATOM_DATE_FORMAT = new SimpleDateFormat("yyyy-mm-dd'T'HH:mm:ss'Z'");	
	static final Pattern          RESOURCE_REGEX   = Pattern.compile("Resource:.*<a href=\"/acct/([0-9]+)?path=%2F([a-z]+)%2F([0-9]+)>([^<]*)</a");
	static final Pattern          EVENT_REGEX      = Pattern.compile("Event:.*<a.*>([^<]*)</a>");	

	DashboardFeed _context = null;
	URI           _uri = null;
	boolean       _shouldStop = false;
	
	public FeedScraper(DashboardFeed context, Session session, URI uri)
		throws URISyntaxException
	{
		super(session);
		_context = context;
		_uri     = uri;  
	}
	
	protected URI getBaseURI() {
		return _uri;
	}	

    protected URI getResourceURI(String relativePath, String query) {
		return _uri;
    }    

    public void stop() {
    	_shouldStop = true;
    }
    
    public void run() {
    	int pollPeriod = DEFAULT_POLL_PERIOD;
    	
    	while(false == _shouldStop) {
    		boolean error   = false;
    		int     entries = 0;
    		
	    	try {
	    		HttpEntity response = getEntity(null, null);
	
	    		SAXParserFactory spf = SAXParserFactory.newInstance();
	            SAXParser        sp  = spf.newSAXParser();
	            XMLReader        xr  = sp.getXMLReader();
	
	            AtomParser parser = new AtomParser(this); 
	            xr.setContentHandler(parser);
	    		xr.parse(new InputSource(response.getContent()));
	    		response.consumeContent();
	    		entries = parser.getNumEntries();
	    	}
	    	catch(Exception e) {
	    		Log.e("FeedScraper", e.getClass().getName());
				e.printStackTrace();
				error = true;
	    	}
	    	
	    	if(error) {
	    		pollPeriod = ERROR_POLL_PERIOD;
	    	}
	    	else if(entries == 0) {
	    		pollPeriod = (int) (pollPeriod * 1.5);
	    		pollPeriod = Math.max(pollPeriod, MAX_POLL_PERIOD);
	    	}
	    	else {
	    		pollPeriod = DEFAULT_POLL_PERIOD;
	    	}
	    	
	    	try {
	    		Thread.sleep(pollPeriod);
	    	}
	    	catch(InterruptedException e) {
	    		//someone may have set _shouldStop on us...
	    	}
    	}
	}
    
    void reportFeedEvent(String updated, String htmlContent) {
    	Log.i("FeedScraper", updated);
    	Log.i("FeedScraper", htmlContent);

    	Date   when;
    	Uri    subjectUri;
    	String subjectName;
    	String summary;
    	
    	try {
    		when = ATOM_DATE_FORMAT.parse(updated);
    	}
    	catch (ParseException e) {
			e.printStackTrace();
    		Log.e("FeedScraper", htmlContent);
			return;
		}

    	Matcher m = RESOURCE_REGEX.matcher(htmlContent);
    	if(m.find()) {
    		String accountId    = m.group(1);
    		String resource     = m.group(2);
    		String resourceId   = m.group(3);
    		subjectName         = m.group(4);

    		if(resource.equals("servers")) {
	    		//TODO actually construct uri once we have proper routes
    			subjectUri = Routes.showServer(accountId, resourceId);
	    		//subjectUri = Uri.parse(accountId + resource + resourceId + subjectName);
    		}
    		else {
        		Log.w("FeedScraper", "Unknown resource type:\n" + htmlContent);
    			return;    			
    		}
    	}
    	else {
    		Log.w("FeedScraper", "No RESOURCE_REGEX:\n" + htmlContent);
			return;
    	}

    	m = EVENT_REGEX.matcher(htmlContent);
    	if(m.find()) {
    		summary = m.group(1);
    	}
    	else {
    		Log.w("FeedScraper", "No EVENT_REGEX:\n" + htmlContent);
			return;    		
    	}
    	
    	_context.onDashboardEvent(when, subjectUri, subjectName, summary);    	
    }
}
