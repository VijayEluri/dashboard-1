package net.xeger.rest.ui;

import android.database.Cursor;
import android.os.Handler;

class ContentTransferThread implements Runnable {
	protected ContentConsumer _consumer;
	protected ContentProducer _producer;
	protected Handler         _handler;
	protected Object          _tag;
	
	protected Thread          _thread;
	protected Cursor          _content;
	protected Throwable       _error;
	
	public ContentTransferThread(ContentConsumer consumer, ContentProducer producer, Handler handler, Object tag) {
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
		if(_content != null) {
			//We're in the Handler thread; callback to the consumer
			_consumer.consumeContent(_content, _tag);
		}
		else if(_error != null) {
			//We're in the Handler thread; callback to the consumer
			_consumer.consumeContentError(_error, _tag);
		}
		else {
			//We're in the worker thread; produce the content and post the callback
			try {
				_content = _producer.produceContent(_tag);
				_handler.post(this);
			}
			catch(Throwable t) {
				_error = t;
				_handler.post(this);
			}
			
		}
	}
}	
