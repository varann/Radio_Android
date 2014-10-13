package org.gigahub.radio.android;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.widget.SimpleCursorAdapter;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.ItemClick;
import org.androidannotations.annotations.Receiver;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.rest.RestService;
import org.gigahub.radio.android.api.RestApiClient;
import org.gigahub.radio.dao.Station;

@EActivity(R.layout.activity_stations)
public class StationsActivity extends Activity {

	@ViewById ListView list;
	@ViewById TextView empty;
	@ViewById TextView stationName;
	@ViewById ProgressBar progressBar;
	@ViewById ImageView playPause;

	@RestService RestApiClient apiClient;
	@Bean RadioDB db;

	@Extra("station.uuid") String stationUuid;
	@Extra String action;

	private SimpleCursorAdapter adapter;

	@AfterViews
	void afterViews() {

		list.setEmptyView(empty);

		adapter = db.getAllStationsAdapter();
		list.setAdapter(adapter);

		DataIntentService_.intent(this).updateStationsAction(false).start();
	}

	@AfterViews
	void openFromNotification() {
		if (stationUuid == null) return;
		updateState(stationUuid, action);
	}

	@Receiver(actions = Actions.DB_UPDATE, local = true, registerAt = Receiver.RegisterAt.OnResumeOnPause)
	void updateStationList() {
		adapter.changeCursor(db.getStationsCursor());
		adapter.notifyDataSetChanged();
	}

	@ItemClick
	void listItemClicked(int position) {
		long stationId = list.getItemIdAtPosition(position);
		Intent intent = new Intent(this, PlayService_.class);
		stationUuid = db.getStationById(stationId).getUuid();
		intent.putExtra("station.uuid", stationUuid);
		startService(intent);
	}

	@Click
	void playPauseClicked() {
		if (TextUtils.isEmpty(stationUuid)) return;

		Intent intent = new Intent(this, PlayService_.class);
		intent.putExtra("station.uuid", stationUuid);

		intent.setAction(Actions.PLAY_PAUSE);

		startService(intent);
	}

	@Receiver(actions = {Actions.STATE_PLAY, Actions.STATE_PAUSE, Actions.STATE_STOP, Actions.STATE_PREPARE, Actions.STATE_ERROR}, local = true, registerAt = Receiver.RegisterAt.OnResumeOnPause)
	void updateStateOnResumeOnPause(Intent intent) {
		updateState(intent.getStringExtra("station.uuid"), intent.getAction());
	}

	private void updateState(String uuid, String action) {

		Station station = db.getStationByUuid(uuid);
		String name = station.getName();

		if (Actions.STATE_PLAY.equals(action)) {
			stationName.setText(name);
			playPause.setImageResource(R.drawable.ic_action_pause);
			progressBar.setVisibility(View.GONE);
		}

		if (Actions.STATE_PAUSE.equals(action)) {
			stationName.setText(name);
			playPause.setImageResource(R.drawable.ic_action_play);
			progressBar.setVisibility(View.GONE);
		}

		if (Actions.STATE_STOP.equals(action)) {
			stationUuid = null;

			stationName.setText("");
			playPause.setImageResource(R.drawable.ic_action_play);
			progressBar.setVisibility(View.GONE);
		}

		if (Actions.STATE_PREPARE.equals(action)) {
			stationName.setText(name);
			playPause.setImageResource(R.drawable.ic_action_pause);
			progressBar.setVisibility(View.VISIBLE);
		}

		if (Actions.STATE_ERROR.equals(action)) {
			stationUuid = null;

			stationName.setText("");
			playPause.setImageResource(R.drawable.ic_action_play);
			progressBar.setVisibility(View.GONE);

			Toast.makeText(this, "Play error, choose another station", Toast.LENGTH_LONG).show();
		}

	}

}
