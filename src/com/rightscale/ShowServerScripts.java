package com.rightscale;

import net.xeger.rest.RestException;
import net.xeger.rest.ui.ContentTransfer;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import com.rightscale.provider.Dashboard;

public class ShowServerScripts extends AbstractServerActivity {
	abstract class ServerAction implements Runnable {
		String _exe;
		
		ServerAction(String exe) {
			_exe = exe;
		}
	}
	static public final String SCRIPTS = "scripts";

	private static String[] FROM = {"name"};
	private static int[]    TO   = {android.R.id.text1};

	protected Cursor _currentExecutables = null;
	protected int    _currentServerTemplateId = 0;
	
	@Override
	public void onCreate(Bundle savedState) {
		super.onCreate(savedState);
		setContentView(R.layout.show_server_scripts);
		ListView view = (ListView)findViewById(R.id.show_server_scripts_list);
		
		view.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				parent.setSelected(false);

				ServerAction action = null;
				
				if(_currentExecutables == null) {
					return;				
				}
				
				int colRightScriptId = _currentExecutables.getColumnIndexOrThrow("right_script_id");

				_currentExecutables.moveToPosition(position);		    	
		    	String executableId = new Integer(_currentExecutables.getInt(colRightScriptId)).toString();
	    		
	    		action = new ServerAction(executableId) {
	    			public void run() {
	    	    		Dashboard.performAction(getBaseContext(), getServerUri(), _helper.getAccountId(), Dashboard.ACTION_RUN_SCRIPT, _exe);		    			
	    	            //TODO reload content            				
	    			}
	    		};

	    		new Thread(action).start();
		    	Toast.makeText(ShowServerScripts.this, R.string.notify_execution_requested, Toast.LENGTH_LONG).show();		    					
			}
		});
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
			
			_currentExecutables = cursor;
			startManagingCursor(cursor);
			
			String state = _currentServer.getString(_currentServer.getColumnIndexOrThrow("state"));
	    	SimpleCursorAdapter adapter = new ScriptAdapter(state, this, android.R.layout.simple_list_item_1, cursor, FROM, TO);
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
			String[] whereArgs = { _helper.getAccountId(), new Integer(_currentServer.getInt(colSTID)).toString(), "operational" };
	    	return cr.query(_helper.getContentRoute("server_template_executables"), Dashboard.SERVER_TEMPLATE_EXECUTABLE_COLUMNS, "account_id = ? AND server_template_id = ? AND apply = ?", whereArgs, null);		
		}
		else {
			return super.produceContent(tag);
		}
	}
	
	class ScriptAdapter extends SimpleCursorAdapter {
		String _serverState = null;
		
		public ScriptAdapter(String serverState, Context context, int layout, Cursor c,
				String[] from, int[] to) {
			super(context, layout, c, from, to);
			_serverState = serverState;
		}
		
		public boolean isEnabled(int position) {
			if(!_serverState.equals("operational")) {
				return false;
			}
			
			Cursor c = this.getCursor();
			c.moveToPosition(position);
			int colRightScriptId = c.getColumnIndexOrThrow("right_script_id");
			
			//Scripts are enabled if we know their right_script_id (recipes don't work)
			return !c.isNull(colRightScriptId);
		}
	}
}
