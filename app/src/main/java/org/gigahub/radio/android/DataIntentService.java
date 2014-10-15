package org.gigahub.radio.android;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EIntentService;
import org.androidannotations.annotations.ServiceAction;
import org.androidannotations.annotations.rest.RestService;
import org.androidannotations.annotations.sharedpreferences.Pref;
import org.gigahub.radio.android.api.ApiStation;
import org.gigahub.radio.android.api.RestApiClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@EIntentService
public class DataIntentService extends IntentService {

	private static final Logger L = LoggerFactory.getLogger(DataIntentService.class.getSimpleName());

	@Bean RadioDB db;
	@RestService RestApiClient apiClient;
	@Pref Preferences_ pref;

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
		if (force || isDataOutdated() || db.getStations().isEmpty()) {
			sendDBState(Actions.DB_STATE.PROGRESS);
			try {
				List<ApiStation> stations = apiClient.getStations();
				db.setStations(stations);
				pref.edit().lastUpdated().put(System.currentTimeMillis()).apply();

				sendDBState(Actions.DB_STATE.DONE);

			} catch (Exception e) {
				sendDBState(Actions.DB_STATE.ERROR);
			}

		}
	}

	private boolean sendDBState(Actions.DB_STATE state) {
		Intent intent = new Intent(Actions.UPDATE_DB_STATE);
		intent.putExtra("db.state", state);
		return localBroadcastManager.sendBroadcast(intent);
	}

	private boolean isDataOutdated() {
		// Перевод миллисекунд в часы. Если прошло больше суток, то данные устарели.
		return (System.currentTimeMillis() - pref.lastUpdated().getOr(0)) / 1000 / 60 / 60 > 24;
	}

}
