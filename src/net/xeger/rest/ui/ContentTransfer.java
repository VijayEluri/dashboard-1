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

package net.xeger.rest.ui;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Handler;

public class ContentTransfer {
	public static void load(ContentConsumer consumer, ContentProducer producer, Handler handler, String tag) {
		ContentTransferThread job = new ContentTransferThread(consumer, producer, handler, tag);
		job.start();
	}

	public static void load(ContentConsumer consumer, ContentProducer producer, Handler handler) {
		load(consumer, producer, handler, null);
	}
	
	public static void loadImage(ImageConsumer consumer, ImageProducer producer, Handler handler, String tag) {
		ImageTransferThread job = new ImageTransferThread(consumer, producer, handler, tag);
		job.start();
	}

	public static void loadImage(ImageConsumer consumer, ImageProducer producer, Handler handler) {
		loadImage(consumer, producer, handler, null);
	}
}

class ContentTransferThread implements Runnable {
	protected ContentConsumer _consumer;
	protected ContentProducer _producer;
	protected Handler         _handler;
	protected String          _tag;
	
	protected Thread          _thread;
	
	protected Cursor          _content;
	protected Throwable       _error;
	protected boolean         _contentOrErrorProduced = false;         
	
	public ContentTransferThread(ContentConsumer consumer, ContentProducer producer, Handler handler, String tag) {
		_consumer = consumer;
		_producer = producer;
		_handler = handler;
		_tag      = tag;
	}

	public void start() {
		_thread = new Thread(this);
		_thread.start();
	}

	public void interrupt() {
		_thread.interrupt();
	}
	
	public void run() {
		if(_contentOrErrorProduced && _error == null) {
			//We're in the Handler thread; callback to the consumer
			_consumer.consumeContent(_content, _tag);
		}
		else if(_contentOrErrorProduced && _error != null) {
			//We're in the Handler thread; callback to the consumer
			_consumer.consumeContentError(_error, _tag);
		}
		else {
			//We're in the worker thread; produce the content and post the callback
			try {
				_content = _producer.produceContent(_tag);
				_contentOrErrorProduced = true;
				_handler.post(this);
			}
			catch(Throwable t) {
				_error = t;
				_contentOrErrorProduced = true;
				_handler.post(this);
			}
			
		}
	}
}	

class ImageTransferThread implements Runnable {
	protected ImageConsumer   _consumer;
	protected ImageProducer   _producer;
	protected Handler         _handler;
	protected String          _tag;
	
	protected Thread          _thread;

	protected Bitmap          _bitmap;
	protected Throwable       _error;
	protected boolean         _imageOrErrorProduced = false;         
	
	public ImageTransferThread(ImageConsumer consumer, ImageProducer producer, Handler handler, String tag) {
		_consumer = consumer;
		_producer = producer;
		_handler  = handler;
		_tag      = tag;
	}
	
	public void start() {
		_thread = new Thread(this);
		_thread.start();
	}

	public void interrupt() {
		_thread.interrupt();
	}
	
	public void run() {
		if(_imageOrErrorProduced && _error == null) {
			//We're in the Handler thread; callback to the consumer
			_consumer.consumeImage(_bitmap, _tag);
		}
		else if(_imageOrErrorProduced && _error != null) {
			//We're in the Handler thread; callback to the consumer
			_consumer.consumeImageError(_error, _tag);
		}
		else {
			//We're in the worker thread; produce the content and post the callback
			try {
				_bitmap = _producer.produceImage(_tag);
				_imageOrErrorProduced = true;
				_handler.post(this);
			}
			catch(Throwable t) {
				_error = t;
				_imageOrErrorProduced = true;
				_handler.post(this);
			}
			
		}		
	}
}
