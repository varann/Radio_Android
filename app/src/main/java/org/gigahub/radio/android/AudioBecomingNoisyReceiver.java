package org.gigahub.radio.android;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;

public class AudioBecomingNoisyReceiver extends BroadcastReceiver {
    public AudioBecomingNoisyReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {

		if (AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(intent.getAction())) {
			Intent stopIntent = new Intent(context, PlayService_.class);
			stopIntent.setAction(Actions.STOP);
			context.stopService(stopIntent);
		}
    }
}
