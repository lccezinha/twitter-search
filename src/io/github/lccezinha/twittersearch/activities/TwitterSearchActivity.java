package io.github.lccezinha.twittersearch.activities;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import io.github.lccezinha.twittersearch.R;
import io.github.lccezinha.twittersearch.utils.HttpRequest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;


public class TwitterSearchActivity extends Activity {
	private ListView listView;
	private EditText editText;
	private String accessToken;
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		new AuthToken().execute();
		
		initializeViewComponents();
	}
	
	public void search(View v){
		String filter = editText.getText().toString();
		
		if(accessToken == null){
			Toast.makeText(this, "Token não disponível", Toast.LENGTH_SHORT).show();
		}else{
			new TwitterSearch().execute(filter);
		}
	}
	
	private void initializeViewComponents(){
		listView = (ListView) findViewById(R.id.list);
		editText = (EditText) findViewById(R.id.text_search);
	}
	
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
	
	private class TwitterSearch extends AsyncTask<String, Void, String[]>{

		ProgressDialog dialog;
		
		@Override
		protected void onPreExecute(){
			dialog = new ProgressDialog(TwitterSearchActivity.this);
			dialog.setMessage("Aguarde...");
			dialog.show();
		}
		
		@Override
		protected String[] doInBackground(String... params) {
			try{
				String filter = params[0];
				
				if(TextUtils.isEmpty(filter)){
					return null;
				}
				
				String urlTwitter = "https://api.twitter.com/1.1/search/tweets.json?q=";
				String url = Uri.parse(urlTwitter + filter).toString();
				
				Log.i(getPackageName(), url);
				
				String content = HttpRequest
									.get(url)
									.authorization("Bearer " + accessToken)
									.body();
				
				Log.e(content, content);
				
				JSONObject jsonObject = new JSONObject(content);
				JSONArray results = jsonObject.getJSONArray("statuses");
				
				String[] tweets = new String[results.length()];
				
				for(int i = 0; i < results.length(); i++) {
					JSONObject tweet = results.getJSONObject(i);
					String text = tweet.getString("text");
					String user = tweet.getJSONObject("user").getString("screen_name");
					
					tweets[i] = user + " - " + text;
					
					Log.i(getPackageName(), tweets[i]);
				}
				
				return tweets;
				
			}catch(Exception e){
				Log.e(getPackageName(), e.getMessage(), e);
				throw new RuntimeException(e);
			}
		}
		
		protected void onPostExecute(String[] result) {
			if(result != null){
				ArrayAdapter<String> adapter = 
						new ArrayAdapter<String>(
								getBaseContext(), 
								android.R.layout.simple_list_item_1, 
								result
							);
				listView.setAdapter(adapter);
			}
			dialog.dismiss();
		}
	}
}
