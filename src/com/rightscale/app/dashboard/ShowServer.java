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

import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.TabHost;

public class ShowServer extends TabActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.show_server);
        
        TabHost tabHost = getTabHost();
        Resources res   = getResources();
        
        TabHost.TabSpec tabSpec;        
        Intent          intent;

        intent = new Intent(this, ShowServerInfo.class);
        intent.setData(getIntent().getData());
        tabSpec = tabHost.newTabSpec("info").setIndicator(null, res.getDrawable(android.R.drawable.ic_menu_info_details)).setContent(intent); 
        tabHost.addTab(tabSpec);
        
        intent = new Intent(this, ShowServerScripts.class);
        intent.setData(getIntent().getData());
        tabSpec = tabHost.newTabSpec("scripts").setIndicator(null, res.getDrawable(android.R.drawable.ic_menu_manage)).setContent(intent); 
        tabHost.addTab(tabSpec);
        
        intent = new Intent(this, ShowServerMonitoring.class);
        intent.setData(getIntent().getData());
        tabSpec = tabHost.newTabSpec("monitoring").setIndicator(null, res.getDrawable(android.R.drawable.ic_menu_gallery)).setContent(intent); 
        tabHost.addTab(tabSpec);
    }
}
