package com.rightscale;

import com.rightscale.provider.Dashboard;

import net.xeger.rest.RestException;
import net.xeger.rest.ui.ContentTransfer;
import android.content.ContentResolver;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

public class ShowServerScripts extends AbstractServerActivity {
	static public final String SCRIPTS = "scripts";

	private static String[] FROM = {"name"};
	private static int[]    TO   = {android.R.id.text1};

	protected Cursor _currentExecutables = null;
	protected int    _currentServerTemplateId = 0;
	
	public void onCreate(Bundle savedState) {
		super.onCreate(savedState);
		setContentView(R.layout.show_server_scripts);
	}
	
	public void consumeContent(Cursor cursor, String tag) {
		super.consumeContent(cursor, tag);
		
		if(tag == SERVER) {			
			ContentTransfer.load(this, this, new Handler(), SCRIPTS);
		}
		else if(tag == SCRIPTS) {
			if(_currentExecutables != null) {
				stopManagingCursor(_currentExecutables);
			}
			
			startManagingCursor(cursor);

	    	SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, android.R.layout.simple_list_item_1, cursor, FROM, TO);
			ListView list = (ListView)findViewById(R.id.show_server_scripts_list);
			list.setAdapter(adapter);
		}
	}
	
	public Cursor produceContent(String tag)
		throws RestException
	{
		if(tag == SCRIPTS) {
	    	ContentResolver cr = getContentResolver();
	    	
			int colSTID = _currentServer.getColumnIndexOrThrow("server_template_id");
			String[] whereArgs = { new Integer(_currentServer.getInt(colSTID)).toString(), "operational" };
	    	return cr.query(Dashboard.SERVER_TEMPLATE_EXECUTABLES_URI, Dashboard.SERVER_TEMPLATE_EXECUTABLE_COLUMNS, "server_template_id = ? and apply = ?", whereArgs, null);		
		}
		else {
			return super.produceContent(tag);
		}
	}
}
