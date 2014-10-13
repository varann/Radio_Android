package org.gigahub.radio.android;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EIntentService;
import org.androidannotations.annotations.ServiceAction;
import org.androidannotations.annotations.rest.RestService;
import org.gigahub.radio.android.api.RestApiClient;
import org.gigahub.radio.dao.Station;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@EIntentService
public class DataIntentService extends IntentService {

	private static final Logger L = LoggerFactory.getLogger(DataIntentService.class.getSimpleName());

	@Bean RadioDB db;
	@RestService RestApiClient apiClient;

	private LocalBroadcastManager localBroadcastManager;

    public DataIntentService() {
        super(DataIntentService.class.getName());

		localBroadcastManager = LocalBroadcastManager.getInstance(this);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
    }

	@ServiceAction
	void updateStationsAction(boolean force) {
		L.debug("Update stations service action");

		List<Station> stations = db.getStations();

		if (stations.isEmpty() || force) {
			db.setStations(apiClient.getStations());

			localBroadcastManager.sendBroadcast(new Intent(Actions.DB_UPDATE));
		}
	}

}
