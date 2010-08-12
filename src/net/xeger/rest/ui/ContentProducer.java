package net.xeger.rest.ui;

import net.xeger.rest.RestException;
import android.database.Cursor;

public interface ContentProducer {
	public Cursor produceContent(String tag) throws RestException;
}
