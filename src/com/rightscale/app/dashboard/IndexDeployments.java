package com.rightscale.app.dashboard;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import com.rightscale.provider.Dashboard;
	
public class IndexDeployments extends AbstractAccountActivity {
	private static String[] FROM = {"Nickname"};
	private static int[]    TO   = {android.R.id.text1};
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.index_deployments);
        setTitle(R.string.rightscale_deployments);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
    	Intent i = new Intent(Intent.ACTION_VIEW, Routes.showDeployment(_helper.getAccountId(), new Long(id).toString()));
    	startActivity(i);
    }

    public Cursor produceContent(String tag) {
		ContentResolver cr = getContentResolver();
		String[] whereArgs = { new Long(_helper.getAccountId()).toString() };
		return cr.query(_helper.getContentRoute("deployments"), Dashboard.DEPLOYMENT_COLUMNS, "account_id = ?", whereArgs, null);
    }
    
    public void consumeContent(Cursor cursor, String tag) {
    	super.consumeContent(cursor, tag);
		startManagingCursor(cursor);
    	SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, android.R.layout.simple_list_item_1, cursor, FROM, TO);
    	setListAdapter(adapter);
    }    
}