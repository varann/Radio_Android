package org.gigahub.radio.android;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;

import org.androidannotations.annotations.EService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

@EService
public class PlayService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener {

	private static final Logger L = LoggerFactory.getLogger(PlayService.class.getSimpleName());
	MediaPlayer player = null;

	@Override
	public void onCreate() {
		super.onCreate();

		player = new MediaPlayer();
		player.setAudioStreamType(AudioManager.STREAM_MUSIC);
		player.setOnPreparedListener(this);
	}

	public int onStartCommand(Intent intent, int flags, int startId) {

		PendingIntent pIntent = PendingIntent.getActivity(this, 1,
				new Intent(this, StationsActivity_.class),
				 PendingIntent.FLAG_UPDATE_CURRENT);

		NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
				.setContentTitle("Radio")
				.setContentText(intent.getStringExtra("station.name"))
				.setSmallIcon(R.drawable.ic_launcher)
				.setContentIntent(pIntent)
				.setOngoing(true);


		startForeground(1, builder.build());

		String url = intent.getStringExtra("station.url");

		if (!TextUtils.isEmpty(url)) {

			try {
				player.setDataSource(url);
				player.prepareAsync();
			} catch (IOException e) {
				L.error("Wrong url!", e);			}
		} else {
			L.warn("No url for streaming");
		}


		return START_NOT_STICKY;
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	public void onPrepared(MediaPlayer player) {
		player.start();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		player.release();
		stopForeground(true);
	}

	@Override
	public boolean onError(MediaPlayer mediaPlayer, int i, int i2) {
		L.error("Play error. MediaPlayer will reset.");
		mediaPlayer.reset();
		return false;
	}
}
