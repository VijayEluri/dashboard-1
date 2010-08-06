package com.rightscale;

import java.util.List;
import com.rightscale.provider.Dashboard;
import com.rightscale.provider.DashboardError;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.*;

/**
 * Activity for viewing the servers in a Deployment. This activity expects to be started with an Intent
 * whose data points to the content-URI of the deployment the user is interested in. Example:
 *   <code>
 *   Intent i = new Intent(Intent.ACTION_VIEW, new Uri("content://com.rightscale.provider.dashboard/deployments/12345"));
 *   startActivity(
 *   </code>
 */
public class IndexDeploymentServers extends DashboardListActivity {
	private static String[] FROM = {Dashboard.SERVER_COLUMN_NICKNAME, Dashboard.SERVER_COLUMN_STATE};
	private static int[]    TO   = {R.id.server_name, R.id.server_state};

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.index_deployment_servers);
        loadTitle();
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
    	//Uri serverUri = Uri.withAppendedPath(Dashboard.SERVERS_URI, new Long(id).toString());
    	//TODO WHY THE ?!@$(& CAN'T I USE A PROPER CONTENT URI HERE?!?!?!
    	Uri serverUri = Uri.withAppendedPath(Uri.parse("content://stupid_hack/servers"), new Long(id).toString());
    	Intent i = new Intent(Intent.ACTION_VIEW, serverUri);
    	startActivity(i);
    }

    protected Cursor loadContent() {
    	ContentResolver cr = getContentResolver();

		String[] whereArgs = { getDeploymentId() };
    	return cr.query(Dashboard.SERVERS_URI, Dashboard.SERVER_COLUMNS, "deployment_id = ?", whereArgs, null);
    }
    
    protected void consumeContent(Cursor cursor) {
    	startManagingCursor(cursor);
    	ServersArrayAdapter adapter = new ServersArrayAdapter(this, R.layout.server_item, cursor, FROM, TO);
    	setListAdapter(adapter);    	
    }

    private String getDeploymentId() {
        Intent intent      = getIntent();
    	Uri contentUri      = intent.getData();
		List<String> path   = contentUri.getPathSegments();
		return path.get(path.size() - 1);             	
    }
    
    private void loadTitle() {
    	try {
	    	ContentResolver cr = getContentResolver();
	
			String[] whereArgs = { getDeploymentId() };
	    	Cursor cursor = cr.query(Dashboard.DEPLOYMENTS_URI, Dashboard.DEPLOYMENT_COLUMNS, "deployment_id = ?", whereArgs, null);
	    	int col = cursor.getColumnIndex("nickname");
	    	if(cursor.moveToNext()) {
		    	String nickname = cursor.getString(col);
		    	this.setTitle(nickname);
	    	}
    	}
    	catch(DashboardError e) {
    		//Need to handle errors manually since we're outside of loadContent
    		consumeError(e);
    	}
    }
    
    class ServersArrayAdapter extends SimpleCursorAdapter {
        private Context _context;
        
        public ServersArrayAdapter (Context context, int layout, Cursor cursor, String[] from, int[] to) {
                super(context, layout, cursor, from, to);                
                _context = context;
        }

        public void  setViewText(TextView v, String text) {
        	v.setText(text);
        }
        
        public void setViewImage(ImageView v, String text) {
        	v.setImageDrawable(getDrawableForState(text));
        }

		private Drawable getDrawableForState(String state) {
        	int resourceId = 0;
        	
        	if(state.equals("operational")) {
        		resourceId = R.drawable.state_operational;
        	}
        	else if(state.equals("booting")) {
        		resourceId = R.drawable.state_booting;
        	}
        	else if(state.equals("decommissioning")) {
        		resourceId = R.drawable.state_decommissioning;
        	}
        	else {
        		resourceId = R.drawable.state_inactive;        		
        	}
        	
        	return _context.getResources().getDrawable(resourceId);
		}
    }
    
}