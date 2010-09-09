package com.rightscale;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import net.xeger.rest.RestException;
import net.xeger.rest.RestServerException;
import net.xeger.rest.Session;
import net.xeger.rest.ui.ContentTransfer;
import net.xeger.rest.ui.ImageConsumer;
import net.xeger.rest.ui.ImageProducer;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.content.ContentResolver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.AdapterView.OnItemSelectedListener;

import com.rightscale.provider.Dashboard;

public class ShowServerMonitoring extends AbstractServerActivity implements ImageProducer, ImageConsumer {
    static private final String[] FROM = {"graph_name"};
    static private final int[]    TO   = {android.R.id.text1};

	public static final String MONITORS = "monitors";
	
	public static final String THUMB = "thumb";
	public static final String SMALL = "small";
	public static final String LARGE = "large";

	public static final String DAY   = "day";
	public static final String NOW   = "now";

	static public final String[] SIZES   = {THUMB, SMALL, LARGE};
	static public final String[] PERIODS = {DAY, NOW};
	
	static public final String DEFAULT_SIZE   = SMALL;
	static public final String DEFAULT_PERIOD = NOW;

	private URI _selectedGraphUri = null;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		setContentView(R.layout.show_server_monitoring);
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
			throw new IllegalArgumentException("URI does not contain query-string marker: " + template);
			
		}
		
		String prefix = template.substring(0, qmark);
    	//TODO thread safety
		_selectedGraphUri = URI.create(prefix + '?' + qstr);
		ContentTransfer.loadImage(this, this, new Handler());
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

	private Cursor _cursor;
	
	public void consumeContent(Cursor cursor, String tag) {
    	super.consumeContent(cursor, tag);

    	if(tag == MONITORS) {
			_cursor = cursor;
			startManagingCursor(cursor);
	    	SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, android.R.layout.simple_spinner_item, cursor, FROM, TO);
	    	adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	    	Spinner spinner = (Spinner)findViewById(R.id.show_server_monitoring_spinner);
	    	spinner.setAdapter(adapter);
	    	spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
				public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
					int colHref = _cursor.getColumnIndexOrThrow("href");
					_cursor.moveToPosition(position);
					String href = _cursor.getString(colHref);				
					showGraph(href, DEFAULT_SIZE, DEFAULT_PERIOD);
				}
	
				public void onNothingSelected(AdapterView<?> arg0) {
					//TODO: clear the monitoring graph (fade out, oooh!)
				}
	    	});
		}		
	}

    public void loadContent()
    {
		ContentTransfer.load(this, this, new Handler(), MONITORS);
    }
    
	public Cursor produceContent(String tag)
		throws RestException
	{
		if(tag == MONITORS) {
	    	ContentResolver cr = getContentResolver();
			String[] whereArgs = { _helper.getAccountId(), getServerId() };    	
	    	return cr.query(_helper.getContentRoute("server_monitors"), Dashboard.SERVER_MONITORS_COLUMNS, "account_id = ? AND server_id = ?", whereArgs, null);			
		}
		else {
			return super.produceContent(tag);
		}
	}
	
    public void consumeImage(Bitmap bitmap, String tag) {
    	ImageView view = (ImageView)findViewById(R.id.show_server_monitoring_graph);
    	
    	if(bitmap != null) {
    		view.setImageBitmap(bitmap);
    	}
    	else {
    		view.setScaleType(ImageView.ScaleType.CENTER);
    		view.setImageDrawable(getBaseContext().getResources().getDrawable(android.R.drawable.ic_menu_close_clear_cancel));
    	}
    }

    public Bitmap produceImage(String tag) throws RestException
    {
    	//TODO thread safety
    	if(_selectedGraphUri == null) {
    		return null;
    	}
    	
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 1;

        try {
	        Bitmap bitmap = null;
	        InputStream in = null;       
	        in = openHttpConnection(_selectedGraphUri);
	        bitmap = BitmapFactory.decodeStream(in, null, options);
	        in.close();
	        return bitmap;
        }
        catch(IOException e) {
        	throw new RestException(e);
        }
    }

    public InputStream openHttpConnection(URI uri)
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

	public void consumeContentError(Throwable t, String tag) {
		//Our super would normally call this; call it ourselves since we don't call super
		_helper.onConsumeContentError(t);

		//Monitoring API returns a 403 if monitoring isn't enabled. Treat this as a simple failure
		//rather than yanking the user into Preferences (base class impl).
		consumeImage(null, null);
	}
    
	public void consumeImageError(Throwable error, String tag) {
		consumeContentError(error, tag);
	}    
}
