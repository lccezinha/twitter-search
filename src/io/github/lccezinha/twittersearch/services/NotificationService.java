package io.github.lccezinha.twittersearch.services;

import io.github.lccezinha.twittersearch.utils.HttpRequest;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.json.JSONArray;
import org.json.JSONObject;

import io.github.lccezinha.twittersearch.R;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Base64;
import android.util.Log;

public class NotificationService extends Service {
	
	private String accessToken;
	
	private class AuthToken extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			try{
				Map<String, String> data = new HashMap<String, String>();
				data.put("grant_type", "client_credentials");
				
				String json = HttpRequest
						.post("https://api.twitter.com/oauth2/token")
						.authorization("Basic " + generateToken())
						.form(data)
						.body();
				
				JSONObject token = new JSONObject(json);
				accessToken = token.getString("access_token");
			}catch(Exception e){
				return null;
			}
			return null;
		}
		
		private String generateToken() throws UnsupportedEncodingException {
			String key = "oQj6jtqWRwI6aegwPnVxU5S85";
			String secret = "9utyJqCIl7XpopQSRm63hW7SulXlLgGNIuXNJeEjyGuSHZvDmR";
			String token = key + ":" + secret;
			String base64 = Base64.encodeToString(token.getBytes(), Base64.NO_WRAP);
			
			return base64;
		}
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId){
		ScheduledThreadPoolExecutor pool = new ScheduledThreadPoolExecutor(1);
		long startDelay = 0;
		long refresh_time = 10;
		
		TimeUnit unit = TimeUnit.MINUTES;
		pool.scheduleAtFixedRate(new NotificationTask(), startDelay, refresh_time, unit);
		
		return START_STICKY;
	}
	
	private boolean isConnected() {
		ConnectivityManager manager = (ConnectivityManager) 
										getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = manager.getActiveNetworkInfo();
		
		return info.isConnected();
	}
	
	private class NotificationTask implements Runnable{
		private String baseUrl = "https://api.twitter.com/1.1/search/tweets.json";
		private String refreshUrl = "?q=@lccezinha";
		
		@Override
		public void run() {
			if(!isConnected()){
				return;
			}
			try{
				String content = HttpRequest
						.get(baseUrl + refreshUrl)
						.authorization("Bearer " + accessToken)
						.body();
				
			JSONObject jsonObject = new JSONObject(content);
			refreshUrl = jsonObject.getString("refresh_url");
			JSONArray results = jsonObject.getJSONArray("statuses");
			
			for(int i = 0; i < results.length(); i++){
				JSONObject tweet = results.getJSONObject(i);
				
				String text = tweet.getString("text");
				String user = tweet.getJSONObject("user").getString("screen_name");
				
				createNotification(user, text, i);
			}
				
			}catch(Exception e){
				Log.e(getPackageName(), e.getMessage(), e);
			}
		}
		
		private void createNotification(String user, String text, int id){
			int icon = R.drawable.ic_launcher;
			String warning = getString(R.string.warning);
			long data = System.currentTimeMillis();
			String title = user + " " + getString(R.string.title);
			
			Context context = getApplicationContext();
			Intent intent = new Intent(context, TweetActivity.class);
			intent.putExtra(TweetActivity.USER, user.toString());
			intent.putExtra(TweetActivity.TEXT, text.toString());
			
			PendingIntent pendingIntent = PendingIntent
								.getActivity(context, id, intent, Intent.FLAG_ACTIVITY_NEW_TASK);
			
			Notification notification = new Notification(icon, warning, data);
			notification.flags = Notification.FLAG_AUTO_CANCEL;
			notification.defaults |= Notification.DEFAULT_VIBRATE;
			notification.defaults |= Notification.DEFAULT_LIGHTS;
			notification.defaults |= Notification.DEFAULT_SOUND;
			notification.setLatestEventInfo(context, title, text, pendingIntent);
			
			String ns = Context.NOTIFICATION_SERVICE;
			NotificationManager notificationManager = (NotificationManager) getSystemService(ns);
			notificationManager.notify(id, notification);
			
			
		}
	}
}
