package com.rightscale.service;

import java.net.URI;
import java.net.URISyntaxException;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import net.xeger.rest.AbstractResource;
import net.xeger.rest.Session;

import org.apache.http.HttpEntity;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import android.util.Log;

class FeedScraper extends AbstractResource implements Runnable {
	static final int DEFAULT_POLL_PERIOD = 2000;
	static final int MAX_POLL_PERIOD     = 20000;
	static final int ERROR_POLL_PERIOD   = MAX_POLL_PERIOD;
	
	URI     _uri;
	boolean _shouldStop = false;
	
	public FeedScraper(Session session, URI uri)
		throws URISyntaxException
	{
		super(session);
		_uri = uri;  
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
	    		Log.d("FeedScraper", "Scraped " + entries + " feed entries.");
	    	}
	    	catch(Exception e) {
	    		Log.e("FeedScraper", e.getMessage());
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
    
    void reportFeedEvent(String subject, String summary) {
    	//TODO send a broadcast (or a system notification?)
    }
}
