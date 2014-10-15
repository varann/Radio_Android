package org.gigahub.radio.android;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v4.widget.SwipeRefreshLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ItemClick;
import org.androidannotations.annotations.Receiver;
import org.androidannotations.annotations.ViewById;

@EFragment(R.layout.fragment_stations)
public class StationsFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

	public static final String FAVOURITE = "FAVOURITE";

	@ViewById ListView list;
	@ViewById TextView empty;
	@ViewById SwipeRefreshLayout refresh;

	@Bean RadioDB db;

	private OnFragmentInteractionListener listener;
	private SimpleCursorAdapter adapter;

	@AfterViews
	void afterViews() {
		refresh.setOnRefreshListener(this);
		refresh.setColorSchemeResources(R.color.refresh_color, R.color.bg, R.color.refresh_color, R.color.bg);

		list.setEmptyView(empty);

		adapter = isFavourite() ? db.getFavouriteStationsAdapter() : db.getAllStationsAdapter();
		list.setAdapter(adapter);
	}

	@ItemClick
	void listItemClicked(int position) {
		long stationId = list.getItemIdAtPosition(position);
		listener.onChangeStation(db.getStationById(stationId).getUuid());
	}

	@Receiver(actions = Actions.UPDATE_DB_STATE, local = true, registerAt = Receiver.RegisterAt.OnResumeOnPause)
	void updateStationList(Intent intent) {
		Actions.DB_STATE state = (Actions.DB_STATE) intent.getSerializableExtra("db.state");

		if (Actions.DB_STATE.PROGRESS.equals(state)) {
			refresh.setRefreshing(true);
		} else {
			refresh.setRefreshing(false);
		}

		if (Actions.DB_STATE.DONE.equals(state)) {
			adapter.changeCursor(isFavourite() ? db.getFavouriteStationsCursor() : db.getStationsCursor());
			adapter.notifyDataSetChanged();
		}

		if (Actions.DB_STATE.ERROR.equals(state)) {
			Toast.makeText(getActivity(), "Error update stations", Toast.LENGTH_LONG).show();
		}
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			listener = (OnFragmentInteractionListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement OnFragmentInteractionListener");
		}
	}

	@Override
	public void onDetach() {
		super.onDetach();
		listener = null;
	}

	private boolean isFavourite() {
		Bundle args = getArguments();
		return args != null && args.getBoolean(FAVOURITE, false);
	}

	@Override
	public void onRefresh() {
		refresh.setRefreshing(true);
		DataIntentService_.intent(getActivity()).updateStationsAction(true).start();
	}

	public interface OnFragmentInteractionListener {

		public void onChangeStation(String uuid);

	}

}
