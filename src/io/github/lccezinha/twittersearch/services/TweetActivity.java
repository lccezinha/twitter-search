package io.github.lccezinha.twittersearch.services;

import io.github.lccezinha.twittersearch.R;
import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class TweetActivity extends Activity {
	public static final String TEXT = "text";
	public static final String USER = "user";
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tweet);
		
		initializeViewComponents();
	}
	
	private void initializeViewComponents(){
		TextView userTextView = (TextView) findViewById(R.id.user);
		TextView textTextView = (TextView) findViewById(R.id.text);
		
		String user = getIntent().getStringExtra(USER);
		String text = getIntent().getStringExtra(TEXT);
		
		userTextView.setText(user);
		textTextView.setText(text);
	}
}
