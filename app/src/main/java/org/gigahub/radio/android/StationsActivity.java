package org.gigahub.radio.android;

import android.app.Activity;
import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.ItemClick;
import org.androidannotations.annotations.Receiver;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.rest.RestService;
import org.gigahub.radio.android.api.RestApiClient;
import org.gigahub.radio.android.api.Station;

import java.util.List;

@EActivity(R.layout.activity_stations)
public class StationsActivity extends Activity {

	@ViewById ListView list;
	@ViewById TextView stationName;
	@ViewById ProgressBar progressBar;
	@ViewById ImageView playPause;

	@RestService RestApiClient apiClient;

	@Extra("station.name") String name;
	@Extra("station.url") String url;
	@Extra String action;

	private StationsAdapter adapter;

	@AfterViews
	void afterViews() {
		adapter = new StationsAdapter(this);
		list.setAdapter(adapter);
	}

	@AfterViews
	void openFromNotification() {
		updateState(name, action);
	}

	@Override
	protected void onResume() {
		super.onResume();
		getStationsInBackground();
	}

	@Background
	void getStationsInBackground() {
		List<Station> stations = apiClient.getStations();
		updateStationList(stations);
	}

	@UiThread
	void updateStationList(List<Station> stations) {
		adapter.clear();
		adapter.addAll(stations);
		adapter.notifyDataSetChanged();
	}

	@ItemClick
	void listItemClicked(Station station) {
		name = station.getName();
		url = station.getStreams().get(0).getUrl();

		Intent intent = new Intent(this, PlayService_.class);
		intent.putExtra("station.name", name);
		intent.putExtra("station.url", url);

		startService(intent);
	}

	@Click
	void playPauseClicked() {
		if (TextUtils.isEmpty(url)) return;

		Intent intent = new Intent(this, PlayService_.class);
		intent.putExtra("station.name", name);
		intent.putExtra("station.url", url);

		intent.setAction(Actions.PLAY_PAUSE);

		startService(intent);
	}

	@Receiver(actions = {Actions.STATE_PLAY, Actions.STATE_PAUSE, Actions.STATE_STOP, Actions.STATE_PREPARE, Actions.STATE_ERROR}, local = true, registerAt = Receiver.RegisterAt.OnResumeOnPause)
	void updateStateOnResumeOnPause(Intent intent) {
		updateState(intent.getStringExtra("station.name"), intent.getAction());
	}

	private void updateState(String name, String action) {

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
			url = null;

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
			url = null;

			stationName.setText("");
			playPause.setImageResource(R.drawable.ic_action_play);
			progressBar.setVisibility(View.GONE);

			Toast.makeText(this, "Play error, choose another station", Toast.LENGTH_LONG).show();
		}

	}

}
