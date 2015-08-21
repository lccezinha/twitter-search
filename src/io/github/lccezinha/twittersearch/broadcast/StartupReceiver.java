package io.github.lccezinha.twittersearch.broadcast;

import io.github.lccezinha.twittersearch.services.NotificationService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class StartupReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent i) {
		Intent intent = new Intent(context, NotificationService.class);
		context.startService(intent);
	}
}
