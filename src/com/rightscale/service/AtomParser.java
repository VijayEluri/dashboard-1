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

import org.xml.sax.Attributes;

class AtomParser extends org.xml.sax.helpers.DefaultHandler {
	FeedScraper  _scraper    = null;
	StringBuffer _content    = null;
	StringBuffer _updated    = null;
	int          _numEntries = 0;
	int          _numInteresting = 0;
	
	boolean      _inEntry    = false,
	             _inContent  = false,
                 _inUpdated  = false;	
	
	public AtomParser(FeedScraper scraper) {
		_scraper = scraper;
	}
	
	public int getNumEntries() {
		return _numEntries;
	}
	
	public int getNumInteresting() {
		return _numInteresting;
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
		else if(_inEntry && localName.equals("updated")) {
			_inUpdated = true;
			_updated = new StringBuffer();
		}
	}
	
	public void characters (char[] chars, int start, int length) {
		if(_inContent && _content != null) {
			_content.append(chars, start, length);
		}
		else if(_inUpdated && _updated != null) {
			_updated.append(chars, start, length);
		}
	}
	
	public void endElement(String uri, String localName, String qName) {
		if(localName == null) { 
			return;
		}
		
		if(localName.equals("entry")) {
			_inEntry = false;
			String updated = _updated.toString();
			String htmlContent = _content.toString();
			if(_scraper.reportFeedEvent(updated, htmlContent)) {
				_numInteresting += 1;
			}
			_numEntries += 1;
		}
		else if(_inContent && localName.equals("content")) {
			_inContent = false;			
		}
		else if(_inUpdated && localName.equals("updated")) {
			_inUpdated = false;						
		}
	}
}