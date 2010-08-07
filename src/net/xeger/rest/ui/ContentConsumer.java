package net.xeger.rest.ui;

import android.database.Cursor;

public interface ContentConsumer {
	public void consumeContent(Cursor c, Object tag);
	public void consumeContentError(Throwable t, Object tag);
}
