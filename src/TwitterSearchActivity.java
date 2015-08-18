import io.github.lccezinha.twittersearch.R;
import android.app.Activity;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ListView;


public class TwitterSearchActivity extends Activity {
	private ListView listView;
	private EditText editText;
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.main);
		
		initializeViewComponents();
	}
	
	private void initializeViewComponents(){
		listView = (ListView) findViewById(R.id.list);
		editText = (EditText) findViewById(R.id.text_search);
	}
}
