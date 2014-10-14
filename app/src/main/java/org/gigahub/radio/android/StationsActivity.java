package org.gigahub.radio.android;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.Receiver;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.rest.RestService;
import org.gigahub.radio.android.api.RestApiClient;
import org.gigahub.radio.dao.Station;

import java.util.ArrayList;
import java.util.List;

@EActivity(R.layout.activity_stations)
public class StationsActivity extends FragmentActivity implements StationsFragment.OnFragmentInteractionListener {

	@ViewById TextView stationName;
	@ViewById ProgressBar progressBar;
	@ViewById ImageView playPause;
	@ViewById ImageView favourite;
	@ViewById LinearLayout infoPanel;
	@ViewById TextView chooseStation;
	@ViewById ViewPager pager;

	@RestService RestApiClient apiClient;

	@Bean RadioDB db;

	@Extra("station.uuid") String stationUuid;
	@Extra Actions.STATE state;

	@AfterViews
	void afterViews() {
		final ActionBar actionBar = getActionBar();

		pager.setAdapter(new StationsPagerAdapter(getSupportFragmentManager()));
		pager.setOnPageChangeListener(
				new ViewPager.SimpleOnPageChangeListener() {
					@Override
					public void onPageSelected(int position) {
						actionBar.setSelectedNavigationItem(position);
					}
				});

		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		ActionBar.TabListener tabListener = new ActionBar.TabListener() {
			@Override
			public void onTabSelected(ActionBar.Tab tab, android.app.FragmentTransaction fragmentTransaction) {
				pager.setCurrentItem(tab.getPosition());
			}

			@Override
			public void onTabUnselected(ActionBar.Tab tab, android.app.FragmentTransaction fragmentTransaction) {
			}

			@Override
			public void onTabReselected(ActionBar.Tab tab, android.app.FragmentTransaction fragmentTransaction) {
			}
		};

		actionBar.addTab(actionBar.newTab().setText("All").setTabListener(tabListener));
		actionBar.addTab(actionBar.newTab().setText("Favourite").setTabListener(tabListener));

		DataIntentService_.intent(this).updateStationsAction(false).start();
	}

	@AfterViews
	void openFromNotification() {
		if (stationUuid == null) return;
		updateState(stationUuid);
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
		state = (Actions.STATE) intent.getSerializableExtra("state");
		getIntent().putExtra("state", state);
		updateState(intent.getStringExtra("station.uuid"));
	}

	private void updateState(String uuid) {
		Station station = db.getStationByUuid(uuid);

		stationName.setText(station.getName());
		favourite.setImageResource(station.getFavourite() ? R.drawable.ic_action_important : R.drawable.ic_action_not_important);
		playPause.setImageResource(
				Actions.STATE.PLAY.equals(state) || Actions.STATE.PREPARE.equals(state)
						? R.drawable.ic_action_pause
						: R.drawable.ic_action_play);

		boolean showInfo = !(Actions.STATE.ERROR.equals(state) || Actions.STATE.STOP.equals(state));
		infoPanel.setVisibility(showInfo ? View.VISIBLE : View.INVISIBLE);
		chooseStation.setVisibility(showInfo ? View.INVISIBLE : View.VISIBLE);

		progressBar.setVisibility(Actions.STATE.PREPARE.equals(state) ? View.VISIBLE : View.GONE);

		if (Actions.STATE.STOP.equals(state)) {
			stationUuid = null;
		}

		if (Actions.STATE.ERROR.equals(state)) {
			stationUuid = null;
			Toast.makeText(this, "Play error, choose another station", Toast.LENGTH_LONG).show();
		}

	}

	@Override
	public void onChangeStation(String uuid) {
		getIntent().putExtra("station.uuid", uuid);
		stationUuid = uuid;
		Intent intent = new Intent(this, PlayService_.class);
		intent.putExtra("station.uuid", stationUuid);
		startService(intent);
	}

	class StationsPagerAdapter extends FragmentPagerAdapter {

		private List<StationsFragment> fragments = new ArrayList<StationsFragment>();

		public StationsPagerAdapter(FragmentManager fm) {
			super(fm);
			fragments.add(new StationsFragment_());

			Bundle args = new Bundle();
			StationsFragment_ fragment = new StationsFragment_();
			args.putBoolean(StationsFragment.FAVOURITE, true);
			fragment.setArguments(args);
			fragments.add(fragment);
		}

		@Override
		public Fragment getItem(int i) {
			return fragments.get(i);
		}

		@Override
		public int getCount() {
			return 2;
		}

	}
}
