package com.rightscale.service;

import org.xml.sax.Attributes;

class AtomParser extends org.xml.sax.helpers.DefaultHandler {
	FeedScraper  _scraper    = null;
	StringBuffer _content    = null;
	int          _numEntries = 0;

	boolean      _inEntry    = false,
	             _inContent  = false;
	
	public AtomParser(FeedScraper scraper) {
		_scraper = scraper;
	}
	
	public int getNumEntries() {
		return _numEntries;
	}
	
	public void startElement(String uri, String localName, String qName, Attributes attributes) {
		if(localName == null) { 
			return;
		}
		
		if(localName.equals("entry")) {
			_inEntry = true;
		}
		else if(_inEntry && localName.equals("content")) {
			_inContent = true;
			_content = new StringBuffer();
		}
	}
	
	public void characters (char[] chars, int start, int length) {
		if(_inContent && _content != null) {
			_content.append(chars, start, length);
		}
	}
	
	public void endElement(String uri, String localName, String qName) {
		if(localName == null) { 
			return;
		}
		
		if(localName.equals("entry")) {
			_inEntry = false;
		}
		else if(_inEntry && localName.equals("content")) {
			_inContent = false;
			
			String htmlContent = _content.toString();
			//TODO parse out subject & summary
			_scraper.reportFeedEvent(htmlContent, htmlContent);
			_numEntries += 1;
		}
	}
}