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

	private MediaPlayer player = null;
	private Intent currentIntent;

	@Override
	public void onCreate() {
		super.onCreate();

		player = new MediaPlayer();
		player.setAudioStreamType(AudioManager.STREAM_MUSIC);
		player.setOnPreparedListener(this);

		currentIntent = null;
	}

	public int onStartCommand(Intent intent, int flags, int startId) {

		currentIntent = intent;

		if (Actions.STOP.equals(intent.getAction())) {
			player.stop();
			stopForeground(true);
			return START_NOT_STICKY;
		}

		if (Actions.PAUSE.equals(intent.getAction())) {
			showNotification(false);
			player.pause();
			return START_NOT_STICKY;
		} else {
			player.reset();
		}

		showNotification(true);
		playStation(intent.getStringExtra("station.url"));

		return START_NOT_STICKY;
	}

	private void showNotification(boolean progress) {

		boolean pause = Actions.PAUSE.equals(currentIntent.getAction());

		PendingIntent pStationsIntent = PendingIntent.getActivity(this, 0,
				new Intent(this, StationsActivity_.class),
				PendingIntent.FLAG_UPDATE_CURRENT);

		Intent currentStationIntent = new Intent(this, PlayService_.class);
		currentStationIntent.putExtra("station.name", currentIntent.getStringExtra("station.name"));
		currentStationIntent.putExtra("station.url", currentIntent.getStringExtra("station.url"));

		if (pause) {
			currentStationIntent.setAction(Actions.PLAY);
		} else {
			currentStationIntent.setAction(Actions.PAUSE);
		}
		PendingIntent pPauseIntent = PendingIntent.getService(this, 0,
				currentStationIntent,
				PendingIntent.FLAG_UPDATE_CURRENT);

		currentStationIntent.setAction(Actions.STOP);
		PendingIntent pStopIntent = PendingIntent.getService(this, 0,
				currentStationIntent,
				PendingIntent.FLAG_UPDATE_CURRENT);


		NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
				.setContentTitle("Radio")
				.setContentText(currentIntent.getStringExtra("station.name"))
				.setSmallIcon(R.drawable.ic_action_volume_on)
				.setContentIntent(pStationsIntent)
				.setOngoing(true)
				.addAction(pause ? R.drawable.ic_action_play : R.drawable.ic_action_pause, pause ? "Play" : "Pause", pPauseIntent)
				.addAction(R.drawable.ic_action_stop, "Stop", pStopIntent);

		if (progress) {
			builder.setProgress(0, 0, true);
		}

		startForeground(1, builder.build());
	}

	private void playStation(String url) {

		if (!TextUtils.isEmpty(url)) {

			try {
				player.setDataSource(url);
				player.prepareAsync();
			} catch (IOException e) {
				L.error("Wrong url!", e);
			}
		} else {
			L.warn("No url for streaming");
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	public void onPrepared(MediaPlayer player) {
		showNotification(false);

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
