package net.xeger.rest.ui;

import android.database.Cursor;

public interface ContentConsumer {
	public void consumeContent(Cursor cursor, String tag);
	public void consumeContentError(Throwable throwable, String tag);
}
