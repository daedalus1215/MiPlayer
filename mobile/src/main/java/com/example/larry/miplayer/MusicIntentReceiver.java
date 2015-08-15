package com.example.larry.miplayer;

import android.content.Context;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;

public class MusicIntentReceiver extends android.content.BroadcastReceiver {
	

	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals(
				android.media.AudioManager.ACTION_AUDIO_BECOMING_NOISY)) {
			context.sendBroadcast(new Intent(AudioPlayingService.ACTION_UNPLUGGED_HEADPHONES));
		}		
	}
}
