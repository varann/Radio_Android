package org.gigahub.radio.android;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.widget.Toast;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EService;
import org.androidannotations.annotations.SystemService;
import org.gigahub.radio.dao.Station;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

@EService
public class PlayService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, AudioManager.OnAudioFocusChangeListener {

	private static final Logger L = LoggerFactory.getLogger(PlayService.class.getSimpleName());
	private static final int NOTI_ID = 1;

	@Bean RadioDB db;

	@SystemService AudioManager audioManager;
	@SystemService NotificationManager notificationManager;
	private LocalBroadcastManager localBroadcastManager;

	private MediaPlayer player = null;
	private boolean isPausePressed = false;
	private Station station;

	@Override
	public void onCreate() {
		super.onCreate();

		localBroadcastManager = LocalBroadcastManager.getInstance(this);

		initMediaPlayer();

		int result = audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC,
				AudioManager.AUDIOFOCUS_GAIN);

		if (result != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
			stopSelf();
			Toast.makeText(this, "Audio output used by another application", Toast.LENGTH_LONG).show();
		}

	}

	private void initMediaPlayer() {
		player = new MediaPlayer();
		player.setAudioStreamType(AudioManager.STREAM_MUSIC);
		player.setOnPreparedListener(this);
		player.setOnErrorListener(this);
	}

	public int onStartCommand(Intent intent, int flags, int startId) {

		station = db.getStationByUuid(intent.getStringExtra("station.uuid"));

		if (Actions.CHANGE_FAVOURITE.equals(intent.getAction())) {
			station.setFavourite(!station.getFavourite());
			db.updateStation(station);
			notificationManager.notify(NOTI_ID, createNotification(isPausePressed ? false : player.isPlaying() ? false : true));
			localBroadcastManager.sendBroadcast(getUpdateStateIntent(isPausePressed ? Actions.STATE.PAUSE : player.isPlaying() ? Actions.STATE.PLAY : Actions.STATE.PREPARE));
			localBroadcastManager.sendBroadcast(new Intent(Actions.DB_UPDATE));
			return START_NOT_STICKY;
		}

		if (Actions.STOP.equals(intent.getAction())) {
			stopSelf();
			return START_NOT_STICKY;
		}

		if (Actions.PLAY_PAUSE.equals(intent.getAction())) {
			if (isPausePressed) {
				isPausePressed = false;
			} else {
				isPausePressed = true;
				player.reset();
				notificationManager.notify(NOTI_ID, createNotification(false));
				localBroadcastManager.sendBroadcast(getUpdateStateIntent(Actions.STATE.PAUSE));
				return START_NOT_STICKY;
			}
		}

		startForeground(NOTI_ID, createNotification(true));
		localBroadcastManager.sendBroadcast(getUpdateStateIntent(Actions.STATE.PREPARE));

		playStation();

		return START_NOT_STICKY;
	}

	private Notification createNotification(boolean progress) {
		notificationManager.cancel(NOTI_ID);

		Intent stationsIntent = new Intent(this, StationsActivity_.class);
		stationsIntent.putExtra("station.uuid", station.getUuid());
		stationsIntent.putExtra("state", isPausePressed ? Actions.STATE.PAUSE : player.isPlaying() ? Actions.STATE.PLAY : Actions.STATE.PREPARE);

		PendingIntent pStationsIntent = PendingIntent.getActivity(this, 0,
				stationsIntent,
				PendingIntent.FLAG_UPDATE_CURRENT);

		Intent currentStationIntent = new Intent(this, PlayService_.class);
		currentStationIntent.putExtra("station.uuid", station.getUuid());

		currentStationIntent.setAction(Actions.PLAY_PAUSE);
		PendingIntent pPauseIntent = PendingIntent.getService(this, 0,
				currentStationIntent,
				PendingIntent.FLAG_UPDATE_CURRENT);

		currentStationIntent.setAction(Actions.STOP);
		PendingIntent pStopIntent = PendingIntent.getService(this, 0,
				currentStationIntent,
				PendingIntent.FLAG_UPDATE_CURRENT);

		currentStationIntent.setAction(Actions.CHANGE_FAVOURITE);
		PendingIntent pFavouriteIntent = PendingIntent.getService(this, 0,
				currentStationIntent,
				PendingIntent.FLAG_UPDATE_CURRENT);

		NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
				.setContentTitle("Radio")
				.setContentText(station.getName())
				.setSmallIcon(R.drawable.ic_action_volume_on)
				.setContentIntent(pStationsIntent)
				.addAction(station.getFavourite() ? R.drawable.ic_action_important : R.drawable.ic_action_not_important, null, pFavouriteIntent)
				.addAction(isPausePressed ? R.drawable.ic_action_play : R.drawable.ic_action_pause, isPausePressed ? "Play" : "Pause", pPauseIntent)
				.addAction(R.drawable.ic_action_stop, "Stop", pStopIntent);

		if (progress) {
			builder.setProgress(0, 0, true);
		}

		return builder.build();
	}

	private void playStation() {
		player.reset();

		String url = station.getStreams().get(0).getUrl();
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
		player.start();
		notificationManager.notify(NOTI_ID, createNotification(false));
		localBroadcastManager.sendBroadcast(getUpdateStateIntent(Actions.STATE.PLAY));
	}

	private Intent getUpdateStateIntent(Actions.STATE state) {
		Intent intent = new Intent(Actions.UPDATE_STATE);
		intent.putExtra("station.uuid", station.getUuid());
		intent.putExtra("state", state);
		return intent;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		localBroadcastManager.sendBroadcast(getUpdateStateIntent(Actions.STATE.STOP));
		player.release();
		audioManager.abandonAudioFocus(this);
		stopForeground(true);
	}

	@Override
	public boolean onError(MediaPlayer mediaPlayer, int i, int i2) {
		L.error("Play error. MediaPlayer will reset.");

		localBroadcastManager.sendBroadcast(getUpdateStateIntent(Actions.STATE.ERROR));
		stopSelf();
		return false;
	}


	@Override
	public void onAudioFocusChange(int focusChange) {
		switch (focusChange) {
			case AudioManager.AUDIOFOCUS_GAIN:
				if (player == null) initMediaPlayer();
				else if (!player.isPlaying()) player.start();
				player.setVolume(1.0f, 1.0f);
				break;

			case AudioManager.AUDIOFOCUS_LOSS:
				if (player.isPlaying()) player.stop();
				onDestroy();
				break;

			case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
				if (player.isPlaying()) player.pause();
				break;

			case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
				if (player.isPlaying()) player.setVolume(0.1f, 0.1f);
				break;
		}
	}
}
