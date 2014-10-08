package org.gigahub.radio.android;

import android.app.Activity;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ItemClick;
import org.androidannotations.annotations.Receiver;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.rest.RestService;

import java.io.IOException;
import java.util.List;

import org.gigahub.radio.android.api.RestApiClient;
import org.gigahub.radio.android.api.Station;

@EActivity(R.layout.activity_stations)
public class StationsActivity extends Activity {

    @ViewById ListView list;
	@ViewById TextView stationName;
	@ViewById ImageView pause;
	@ViewById ImageView stop;
	@ViewById ProgressBar progressBar;

    @RestService RestApiClient apiClient;

    private ArrayAdapter<Station> adapter;
	private Station currentStation;

	@AfterViews
	void afterViews() {

		adapter = new ArrayAdapter<Station>(this, R.layout.station_list_item) {
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {

				if (convertView == null) {
					LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
					convertView = inflater.inflate(R.layout.station_list_item, parent, false);
				}

				TextView nameView = (TextView) convertView.findViewById(R.id.item);
				nameView.setText(adapter.getItem(position).getName());

				return convertView;
			}
		};

		list.setAdapter(adapter);

	}

	@Override
	protected void onResume() {
		super.onResume();
		getStationsInBackground();
	}

	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.stations, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
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
	}

	@ItemClick
	void listItemClicked(Station station) {
		currentStation = station;

		Intent intent = new Intent(this, PlayService_.class);
		intent.putExtra("station.name", station.getName());
		intent.putExtra("station.url", station.getStreams().get(0).getUrl());

		startService(intent);
	}

	@Click
	void pauseClicked() {
		if (currentStation == null) return;

		Intent intent = new Intent(this, PlayService_.class);
		intent.putExtra("station.name", currentStation.getName());
		intent.putExtra("station.url", currentStation.getStreams().get(0).getUrl());

		intent.setAction(Actions.PLAY_PAUSE);

		startService(intent);
	}

	@Click
	void stopClicked() {
		stopService(new Intent(Actions.STOP, null, this, PlayService_.class));
	}

	@Receiver(actions = {Actions.STATE_PLAY, Actions.STATE_PAUSE, Actions.STATE_STOP, Actions.STATE_PREPARE, Actions.STATE_ERROR}, local = true, registerAt = Receiver.RegisterAt.OnResumeOnPause)
	void updateStateOnResumeOnPause(Intent intent) {

		if(Actions.STATE_PLAY.equals(intent.getAction())) {
			pause.setImageResource(R.drawable.ic_action_pause);
			progressBar.setVisibility(View.GONE);
		}

		if(Actions.STATE_PAUSE.equals(intent.getAction())) {
			pause.setImageResource(R.drawable.ic_action_play);
			progressBar.setVisibility(View.GONE);
		}

		if(Actions.STATE_STOP.equals(intent.getAction())) {
			currentStation = null;

			stationName.setText("");
			pause.setImageResource(R.drawable.ic_action_play);
			progressBar.setVisibility(View.GONE);
		}

		if(Actions.STATE_PREPARE.equals(intent.getAction())) {
			stationName.setText(intent.getStringExtra("station.name"));
			pause.setImageResource(R.drawable.ic_action_pause);
			progressBar.setVisibility(View.VISIBLE);
		}

		if(Actions.STATE_ERROR.equals(intent.getAction())) {
			currentStation = null;

			stationName.setText("");
			pause.setImageResource(R.drawable.ic_action_play);
			progressBar.setVisibility(View.GONE);

			Toast.makeText(this, "Play error, choose another station", Toast.LENGTH_LONG).show();
		}

	}

}
