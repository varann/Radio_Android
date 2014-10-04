package org.gigahub.radio.android;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.rest.RestService;

import java.util.List;

import org.gigahub.radio.android.api.RestApiClient;
import org.gigahub.radio.android.api.Station;

@EActivity(R.layout.activity_stations)
public class StationsActivity extends Activity {

    @ViewById ListView list;

    @RestService RestApiClient apiClient;

    private ArrayAdapter<Station> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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

    }

	@Override
	protected void onResume() {
		super.onResume();

		list.setAdapter(adapter);
		getBackgroundStations();
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
    void getBackgroundStations() {
        List<Station> stations = apiClient.getStations();

		updateStationList(stations);
    }

	@UiThread
	void updateStationList(List<Station> stations) {

		adapter.addAll(stations);
	}
}
