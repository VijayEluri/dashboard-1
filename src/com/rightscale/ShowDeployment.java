package com.rightscale;

import java.util.List;

import net.xeger.rest.ui.ContentTransfer;

import com.rightscale.provider.Dashboard;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.*;

/**
 * Activity for viewing the servers in a Deployment. This activity expects to be started with an Intent
 * whose data points to the content-URI of the deployment the user is interested in.
 */
public class ShowDeployment extends DashboardListActivity {
	private static final Object SERVERS          = "servers";
	private static final Object DEPLOYMENT_TITLE = "deployment title";
	
	private static String[] FROM = {"Nickname", "State"};
	private static int[]    TO   = {R.id.server_name, R.id.server_state};

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.show_deployment);
        ContentTransfer.load(this, this, new Handler(), DEPLOYMENT_TITLE);
        ContentTransfer.load(this, this, new Handler(), SERVERS);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
    	Uri serverUri = Uri.withAppendedPath(Dashboard.SERVERS_URI, new Long(id).toString());
    	Intent i = new Intent(Intent.ACTION_VIEW, serverUri);
    	startActivity(i);
    }

    public Cursor produceContent(Object tag) {
    	ContentResolver cr = getContentResolver();
		String[] whereArgs = { getDeploymentId() };
    	
    	if(tag == SERVERS) {
	    	return cr.query(Dashboard.SERVERS_URI, Dashboard.SERVER_COLUMNS, "deployment_id = ?", whereArgs, null);
    	}
    	else if(tag == DEPLOYMENT_TITLE) {
	    	return cr.query(Dashboard.DEPLOYMENTS_URI, Dashboard.DEPLOYMENT_COLUMNS, "deployment_id = ?", whereArgs, null);
    	}
    	else {
    		return null;
    	}
    }
    
    public void consumeContent(Cursor cursor, Object tag) {
    	if(tag == SERVERS) {
	    	startManagingCursor(cursor);
	    	ServerItemAdapter adapter = new ServerItemAdapter(this, R.layout.server_item, cursor, FROM, TO);
	    	setListAdapter(adapter);
    	}
    	else if(tag == DEPLOYMENT_TITLE) {
	    	int col = cursor.getColumnIndex("nickname");
	    	cursor.moveToNext();
	    	String nickname = cursor.getString(col);
	    	this.setTitle(nickname);
    	}
    }

    private String getDeploymentId() {
        Intent intent      = getIntent();
    	Uri contentUri      = intent.getData();
		List<String> path   = contentUri.getPathSegments();
		return path.get(path.size() - 1);             	
    }
    
    class ServerItemAdapter extends SimpleCursorAdapter {
        private Context _context;
        
        public ServerItemAdapter (Context context, int layout, Cursor cursor, String[] from, int[] to) {
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
        	
        	if(state.equals("operational") || state.equals("running")) {
        		resourceId = R.drawable.state_operational;
        	}
        	else if(state.equals("booting")) {
        		resourceId = R.drawable.state_booting;
        	}
        	else if(state.equals("decommissioning") || state.equals("shutting-down ")) {
        		resourceId = R.drawable.state_decommissioning;
        	}
        	else if(state.equals("stranded")) {
        		resourceId = R.drawable.state_stranded;
        	}
        	else {
        		resourceId = R.drawable.state_inactive;        		
        	}
        	
        	return _context.getResources().getDrawable(resourceId);
		}
    }
    
}