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

import com.rightscale.app.dashboard.Routes;

import android.net.Uri;
import android.util.Log;

class FeedScraper extends AbstractResource implements Runnable {
	static final int DEFAULT_POLL_PERIOD = 5000;
	static final int MAX_POLL_PERIOD     = 30000;
	static final int ERROR_POLL_PERIOD   = MAX_POLL_PERIOD;
	
	static final SimpleDateFormat ATOM_DATE_FORMAT = new SimpleDateFormat("yyyy-mm-dd'T'HH:mm:ss'Z'");	
	static final Pattern          RESOURCE_REGEX   = Pattern.compile("Resource:.*<a href=\"/acct/([0-9]+)\\?path=%2F([a-z]+)%2F([0-9]+)\">([^<]*)</a");
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
    		int interesting = 0;
    		
	    	try {
	    		HttpEntity response = getEntity(null, null);
	
	    		SAXParserFactory spf = SAXParserFactory.newInstance();
	            SAXParser        sp  = spf.newSAXParser();
	            XMLReader        xr  = sp.getXMLReader();
	
	            AtomParser parser = new AtomParser(this); 
	            xr.setContentHandler(parser);
	    		xr.parse(new InputSource(response.getContent()));
	    		response.consumeContent();
	    		interesting = parser.getNumInteresting();
	    	}
	    	catch(Exception e) {
	    		//Log.e("FeedScraper", e.getClass().getName());
				e.printStackTrace();
				error = true;
	    	}
	    	
	    	if(error) {
	    		pollPeriod = ERROR_POLL_PERIOD;
	    	}
	    	else if(interesting == 0) {
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
    
    boolean reportFeedEvent(String updated, String htmlContent) {
    	Date   when;
    	Uri    subjectUri;
    	String subjectName;
    	String summary;
    	
    	try {
    		when = ATOM_DATE_FORMAT.parse(updated);
    	}
    	catch (ParseException e) {
			e.printStackTrace();
    		//Log.e("FeedScraper", htmlContent);
			return false;
		}

    	Matcher m = RESOURCE_REGEX.matcher(htmlContent);
    	if(m.find()) {
    		String accountId    = m.group(1);
    		String resource     = m.group(2);
    		String resourceId   = m.group(3);
    		subjectName         = m.group(4);

    		if(resource.equals("servers")) {
    			subjectUri = Routes.showServer(accountId, resourceId);
    		}
    		else {
        		//Log.w("FeedScraper", "Unknown resource type:\n" + htmlContent);
    			return false;    			
    		}
    	}
    	else {
    		////Log.w("FeedScraper", "No RESOURCE_REGEX:\n" + htmlContent);
			return false;
    	}

    	m = EVENT_REGEX.matcher(htmlContent);
    	if(m.find()) {
    		summary = m.group(1);
    	}
    	else {
    		////Log.w("FeedScraper", "No EVENT_REGEX:\n" + htmlContent);
			return false;    		
    	}

    	return _context.onDashboardEvent(when, subjectUri, subjectName, summary);
    }
}
