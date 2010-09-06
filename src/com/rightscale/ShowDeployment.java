package com.rightscale;

import java.util.List;

import net.xeger.rest.RestException;
import net.xeger.rest.ui.ContentTransfer;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.rightscale.provider.Dashboard;

/**
 * Activity for viewing the servers in a Deployment. This activity expects to be started with an Intent
 * whose data points to the content-URI of the deployment the user is interested in.
 */
public class ShowDeployment extends AbstractDashboardActivity {
	private static final String SERVERS          = "servers";
	private static final String DEPLOYMENT_TITLE = "deployment title";
	
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
    	Intent i = new Intent(Intent.ACTION_VIEW, Routes.showServer(_helper.getAccountId(), new Long(id).toString()));
    	startActivity(i);
    }

    public Cursor produceContent(String tag)
    	throws RestException
    {
    	ContentResolver cr = getContentResolver();
		String[] whereArgs = { _helper.getAccountId(), getDeploymentId() };
    	
    	if(tag == SERVERS) {
	    	return cr.query(_helper.getRelativeRoute("servers"), Dashboard.SERVER_COLUMNS, "account_id = ? AND deployment_id = ?", whereArgs, null);
    	}
    	else if(tag == DEPLOYMENT_TITLE) {
	    	return cr.query(_helper.getRelativeRoute("deployments"), Dashboard.DEPLOYMENT_COLUMNS, "account_id = ? AND id = ?", whereArgs, null);
    	}
    	else {
    		return null;
    	}
    }
    
    public void consumeContent(Cursor cursor, String tag) {
    	if(tag == SERVERS) {
	    	startManagingCursor(cursor);
	    	ServerItemAdapter adapter = new ServerItemAdapter(this, R.layout.server_item, cursor, FROM, TO);
	    	setListAdapter(adapter);
    	}
    	else if(tag == DEPLOYMENT_TITLE) {
	    	int col = cursor.getColumnIndexOrThrow("nickname");
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