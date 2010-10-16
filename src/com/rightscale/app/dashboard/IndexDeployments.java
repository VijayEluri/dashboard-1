// Dashboard: an Android front-end to the RightScale dashboard
// Copyright (C) 2009 Tony Spataro <code@tracker.xeger.net>
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program.  If not, see <http://www.gnu.org/licenses/>.

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