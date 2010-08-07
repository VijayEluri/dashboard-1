package com.rightscale;

import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Gallery;

//<monitors>
//<monitor>
//  <graph-name>apache/scoreboard</graph-name>
//  <href>https://moo1.rightscale.com/sketchy1-110/hosts/i-07d7146d/plugins/apache/views/scoreboard.png?period=day&size=thumb&tz=America%2FLos_Angeles&tok=pWcKCpDAct4uxz1ODkmQBEg</href>
//</monitor>
//</monitoring>
public class ShowServerMonitoring extends Activity {
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		setContentView(R.layout.show_server_monitoring);
	}
	
    private String getServerId() {
        Intent intent      = getIntent();
    	Uri contentUri      = intent.getData();
		List<String> path   = contentUri.getPathSegments();
		return path.get(path.size() - 1);             	
    }
}
