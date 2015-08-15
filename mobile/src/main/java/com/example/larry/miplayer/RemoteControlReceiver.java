package com.example.larry.miplayer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.KeyEvent;

public class RemoteControlReceiver extends BroadcastReceiver{

	@Override
	public void onReceive(Context context, Intent intent) {
		 if (Intent.ACTION_MEDIA_BUTTON.equals(intent.getAction())) {
	            KeyEvent event = (KeyEvent) intent .getParcelableExtra(Intent.EXTRA_KEY_EVENT);

	            if (event == null) {
	                return;
	            }

	            if (event.getAction() == KeyEvent.ACTION_DOWN) {
	                context.sendBroadcast(new Intent(AudioPlayingService.ACTION_PLAYER_PAUSE));
	            }
	        }

	}

}
