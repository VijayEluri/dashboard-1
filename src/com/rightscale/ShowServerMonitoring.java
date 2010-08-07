package com.rightscale;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.security.InvalidParameterException;
import java.util.List;

import net.xeger.rest.RestAuthException;
import net.xeger.rest.RestException;
import net.xeger.rest.RestNetworkException;
import net.xeger.rest.RestServerException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Gallery;
import android.widget.ImageView;

public class ShowServerMonitoring extends Activity {
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		setContentView(R.layout.show_server_monitoring);

		String img = "https://moo1.rightscale.com/sketchy1-110/hosts/i-07d7146d/plugins/users/views/users.png?size=thumb&period=day&tok=pWcKCpDAct4uxz1ODkmQBEg&tz=America%2FLos_Angeles";
		new Thread(new LoadImage(img, new Handler())).start();
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

    private Bitmap produceImage(String url)
    	throws IOException, URISyntaxException, RestException
    {       
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 1;

        Bitmap bitmap = null;
        InputStream in = null;       
        in = openHttpConnection(url);
        bitmap = BitmapFactory.decodeStream(in, null, options);
        in.close();
        return bitmap;               
    }

    private InputStream openHttpConnection(String url)
    	throws RestException
    {
		try {
			URI uri = new URI(url);
			DefaultHttpClient client = (DefaultHttpClient)com.rightscale.provider.Dashboard.createHttpClient(getBaseContext());
			client.clearRequestInterceptors();
			client.getCredentialsProvider().clear();
			
			HttpGet        get          = new HttpGet(uri.toString());
			HttpResponse   response;		

			response = client.execute(get);
			
			int status = response.getStatusLine().getStatusCode();
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
		catch(URISyntaxException e) {
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
    	String  _url;
    	Handler _handler;
    	
    	public LoadImage(String url, Handler handler) {
    		_url     = url;
    		_handler = handler;
    	}
    	
    	public void run() {
    		try {
    			Bitmap bitmap = produceImage(_url);
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
}
