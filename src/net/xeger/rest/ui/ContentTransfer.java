package net.xeger.rest.ui;

import android.os.Handler;

public class ContentTransfer {
	Handler _handler;
	
	public static void load(ContentConsumer consumer, ContentProducer producer, Handler handler, Object tag) {
		ContentTransferThread job = new ContentTransferThread(consumer, producer, handler, tag);
		job.start();
	}

	public static void load(ContentConsumer consumer, ContentProducer producer, Handler handler) {
		load(consumer, producer, handler, null);
	}
}
