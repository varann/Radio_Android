package org.gigahub.radio.android;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.widget.SimpleCursorAdapter;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.androidannotations.annotations.AfterViews;
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
	@ViewById ImageView favourite;
	@ViewById LinearLayout infoPanel;
	@ViewById TextView chooseStation;

	@RestService RestApiClient apiClient;
	@Bean RadioDB db;

	@Extra("station.uuid") String stationUuid;
	@Extra Actions.STATE state;

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
		updateState(stationUuid, state);
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

	@Click
	void favouriteClicked() {
		Intent intent = new Intent(this, PlayService_.class);
		intent.putExtra("station.uuid", stationUuid);
		intent.setAction(Actions.CHANGE_FAVOURITE);

		startService(intent);
	}

	@Receiver(actions = Actions.UPDATE_STATE, local = true, registerAt = Receiver.RegisterAt.OnResumeOnPause)
	void updateStateOnResumeOnPause(Intent intent) {
		updateState(intent.getStringExtra("station.uuid"), (Actions.STATE) intent.getSerializableExtra("state"));
	}

	private void updateState(String uuid, Actions.STATE action) {

		Station station = db.getStationByUuid(uuid);

		stationName.setText(station.getName());
		favourite.setImageResource(station.getFavourite() ? R.drawable.ic_action_important : R.drawable.ic_action_not_important);
		playPause.setImageResource(
				Actions.STATE.PLAY.equals(action) || Actions.STATE.PREPARE.equals(action)
						? R.drawable.ic_action_pause
						: R.drawable.ic_action_play);

		boolean showInfo = ! (Actions.STATE.ERROR.equals(action) || Actions.STATE.STOP.equals(action));
		infoPanel.setVisibility(showInfo ? View.VISIBLE : View.INVISIBLE);
		chooseStation.setVisibility(showInfo ? View.INVISIBLE : View.VISIBLE);

		progressBar.setVisibility(Actions.STATE.PREPARE.equals(action) ? View.VISIBLE : View.GONE);

		if (Actions.STATE.STOP.equals(action)) {
			stationUuid = null;
		}

		if (Actions.STATE.ERROR.equals(action)) {
			stationUuid = null;
			Toast.makeText(this, "Play error, choose another station", Toast.LENGTH_LONG).show();
		}

	}

}
