package org.gigahub.radio.android;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SimpleCursorAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ItemClick;
import org.androidannotations.annotations.Receiver;
import org.androidannotations.annotations.ViewById;

@EFragment(R.layout.fragment_stations)
public class StationsFragment extends Fragment {

	public static final String FAVOURITE = "FAVOURITE";

	@ViewById ListView list;
	@ViewById TextView empty;

	@Bean RadioDB db;

	private OnFragmentInteractionListener listener;
	private SimpleCursorAdapter adapter;

	@AfterViews
	void afterViews() {
		list.setEmptyView(empty);

		adapter = isFavourite() ? db.getFavouriteStationsAdapter() : db.getAllStationsAdapter();
		list.setAdapter(adapter);
	}

	@ItemClick
	void listItemClicked(int position) {
		long stationId = list.getItemIdAtPosition(position);
		listener.onChangeStation(db.getStationById(stationId).getUuid());
	}

	@Receiver(actions = Actions.DB_UPDATE, local = true, registerAt = Receiver.RegisterAt.OnResumeOnPause)
	void updateStationList() {
		adapter.changeCursor(isFavourite() ? db.getFavouriteStationsCursor() : db.getStationsCursor());
		adapter.notifyDataSetChanged();
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

	public interface OnFragmentInteractionListener {

		public void onChangeStation(String uuid);

	}

}
