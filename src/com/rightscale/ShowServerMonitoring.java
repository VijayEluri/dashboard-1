package com.rightscale;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.xeger.rest.RestException;
import net.xeger.rest.RestServerException;
import net.xeger.rest.Session;
import net.xeger.rest.ui.ContentConsumer;
import net.xeger.rest.ui.ContentProducer;
import net.xeger.rest.ui.ContentTransfer;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import com.rightscale.ShowDeployment.ServerItemAdapter;
import com.rightscale.provider.Dashboard;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;

public class ShowServerMonitoring extends Activity implements ContentConsumer, ContentProducer {
	static public final String[] SIZES   = {"thumb"};
	static public final String[] PERIODS = {"day"};
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		setContentView(R.layout.show_server_monitoring);

		//lifted from https://moo1.rightscale.com/api/acct/2951/servers/648120/monitoring.xml
		//String img = "http://moo1.rightscale.com/sketchy1-110/hosts/i-07d7146d/plugins/cpu-0/views/cpu_overview.png?size=thumb&period=day&tok=pWcKCpDAct4uxz1ODkmQBEg&tz=America%2FLos_Angeles";
		//showGraph(img, "thumb", "day");
		ContentTransfer.load(this, this, new Handler());
	}
	
	public void showGraph(String template, String size, String period) {
		URI uri = URI.create(template);
		Map<String, String> query = parseQueryParams(uri);
		query.put("size", size);
		query.put("period", period);
		
		StringBuffer qstr = new StringBuffer();
		for(String key : query.keySet()) {
			if(qstr.length() > 0) qstr.append('&');
			qstr.append(key);
			qstr.append('=');
			qstr.append(query.get(key));
		}

		int qmark = template.indexOf('?'); 
		if(qmark < 0) {
			throw new IllegalArgumentException("URI does not contain ? query-string marker: " + template);
			
		}
		
		String prefix = template.substring(0, qmark);
		uri = URI.create(prefix + '?' + qstr);		
		new Thread(new LoadImage(uri, new Handler())).start();
	}
	
	private Map<String, String> parseQueryParams(URI uri) {
		Map<String, String> queryParams = new HashMap<String, String>();
		
		String query = uri.getRawQuery();
		String[] items = query.split("&");
		for(String item : items) {
			String[] pair = item.split("=");
			if(pair.length >= 2) {
				queryParams.put(pair[0], pair[1]);
			}
		}
		
		return queryParams;
	}
	
    private String getServerId() {
        Intent intent      = getIntent();
    	Uri contentUri      = intent.getData();
		List<String> path   = contentUri.getPathSegments();
		return path.get(path.size() - 1);             	
    }
    
    protected void consumeImage(Bitmap bitmap) {
    	ImageView view = (ImageView)findViewById(R.id.show_server_monitoring_graph);
    	
    	if(bitmap != null) {
    		view.setImageBitmap(bitmap);
    	}
    	else {
    		view.setImageDrawable(getBaseContext().getResources().getDrawable(android.R.drawable.ic_menu_close_clear_cancel));
    	}
    }

    private Bitmap produceImage(URI uri)
    	throws IOException, URISyntaxException, RestException
    {       
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 1;

        Bitmap bitmap = null;
        InputStream in = null;       
        in = openHttpConnection(uri);
        bitmap = BitmapFactory.decodeStream(in, null, options);
        in.close();
        return bitmap;               
    }

    private InputStream openHttpConnection(URI uri)
    	throws RestException
    {
		try {
			Session session = com.rightscale.provider.Dashboard.createSession(getBaseContext()); 
			
			DefaultHttpClient client = (DefaultHttpClient)session.createClient();
			//don't send basic auth headers
			client.clearRequestInterceptors();
			client.getCredentialsProvider().clear();
			
			HttpGet        get      = session.createGet(uri);
			HttpResponse   response = client.execute(get);			
			int status              = response.getStatusLine().getStatusCode();
			
			if(status == 200) {
				return response.getEntity().getContent();
			}
			else {
				StatusLine sl = response.getStatusLine();				
				throw new RestServerException(sl.getReasonPhrase(), sl.getStatusCode());
			}
		}
		catch(IOException e) {
			throw new net.xeger.rest.RestNetworkException(e);
		}

	}
    
    class ConsumeImage implements Runnable {
    	Bitmap _bitmap;
    	
    	public ConsumeImage(Bitmap bitmap) {
    		_bitmap = bitmap;
    	}
    	
    	public void run() {
    		consumeImage(_bitmap);
    	}
    }
    
    class LoadImage implements Runnable {
    	URI  _uri;
    	Handler _handler;
    	
    	public LoadImage(URI uri, Handler handler) {
    		_uri     = uri;
    		_handler = handler;
    	}
    	
    	public void run() {
    		try {
    			Bitmap bitmap = produceImage(_uri);
        		ConsumeImage callback = new ConsumeImage(bitmap);
        		_handler.post(callback);
    		}
    		catch(Exception e) {
    			Log.e("ShowServerMonitoring", e.toString());
    			ConsumeImage blank = new ConsumeImage(null);
    			_handler.post(blank);
    		}
    		
    	}
    }

    static private final String[] FROM = {"graph_name"};
    static private final int[]    TO   = {R.id.graph_name};
    
	public void consumeContent(Cursor c, Object tag) {
    	SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.server_monitor_item, c, FROM, TO);
    	adapter.setDropDownViewResource(R.layout.server_monitor_item);
    	Spinner spinner = (Spinner)findViewById(R.id.show_server_monitoring_spinner);
    	spinner.setAdapter(adapter);
	}

	public void consumeContentError(Throwable t, Object tag) {
		Settings.handleError(t, this);
		finish();
	}

	public Cursor produceContent(Object tag) {
    	ContentResolver cr = getContentResolver();
		String[] whereArgs = { getServerId() };    	
    	return cr.query(Dashboard.SERVER_MONITORS_URI, Dashboard.SERVER_MONITORS_COLUMNS, "server_id = ?", whereArgs, null);
	}
}
