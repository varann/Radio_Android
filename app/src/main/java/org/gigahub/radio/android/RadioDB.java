package org.gigahub.radio.android;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.widget.SimpleCursorAdapter;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.gigahub.radio.android.api.ApiStation;
import org.gigahub.radio.android.api.ApiStream;
import org.gigahub.radio.dao.DaoMaster;
import org.gigahub.radio.dao.DaoSession;
import org.gigahub.radio.dao.Station;
import org.gigahub.radio.dao.StationDao;
import org.gigahub.radio.dao.Stream;
import org.gigahub.radio.dao.StreamDao;

import java.util.List;
import java.util.UUID;

import de.greenrobot.dao.query.QueryBuilder;

/**
 * Created by asavinova on 12/10/14.
 */
@EBean(scope = EBean.Scope.Singleton)
public class RadioDB {

	private SQLiteDatabase db;
	@RootContext Context context;

	private DaoMaster.DevOpenHelper helper;
	private DaoMaster daoMaster;
	private DaoSession daoSession;

	@AfterInject
	void init() {
		helper = new DaoMaster.DevOpenHelper(context, "radio-db", null);
		db = helper.getWritableDatabase();
		daoMaster = new DaoMaster(db);
		daoSession = daoMaster.newSession();
	}

	public List<Station> getStations() {
		return daoSession.getStationDao().loadAll();
	}

	public void setStations(List<ApiStation> stations) {

		StationDao stationDao = daoSession.getStationDao();
		List<Station> oldStations = stationDao.loadAll();

		for (ApiStation station : stations) {
			Station find = null;
			for (Station oldStation : oldStations) {
				if (oldStation.getUuid().equals(station.getUuid())) {
					find = oldStation;
					return;
				}
			}

			if (find != null) {
				oldStations.remove(find);
			}
		}
		stationDao.deleteInTx(oldStations);

		StreamDao streamDao = daoSession.getStreamDao();
		streamDao.deleteAll();

		for (ApiStation station : stations) {
			Station dbStation = new Station();
			dbStation.setName(station.getName());
			dbStation.setUuid(station.getUuid());

			long id = stationDao.insertOrReplace(dbStation);

			for (ApiStream stream : station.getStreams()) {
				Stream dbStream = new Stream();
				dbStream.setStationId(id);
				dbStream.setUrl(stream.getUrl());

				streamDao.insert(dbStream);
			}
		}
	}

	public SimpleCursorAdapter getAllStationsAdapter() {
		StationDao stationDao = daoSession.getStationDao();
		Cursor cursor = db.query(stationDao.getTablename(), stationDao.getAllColumns(), null, null, null, null, null);
		String[] from = { StationDao.Properties.Name.columnName };
		int[] to = { R.id.name };
		return new SimpleCursorAdapter(context, R.layout.station_list_item, cursor, from, to, 0);
	}

	public Cursor getStationsCursor() {
		StationDao stationDao = daoSession.getStationDao();
		return db.query(stationDao.getTablename(), stationDao.getAllColumns(), null, null, null, null, null);
	}

	public Station getStationById(long id) {
		return daoSession.getStationDao().load(id);
	}

	public Station getStationByUuid(String uuid) {
		QueryBuilder<Station> builder = daoSession.getStationDao().queryBuilder();
		return builder.where(StationDao.Properties.Uuid.eq(uuid)).build().unique();
	}

	public void updateStation(Station station) {
		daoSession.getStationDao().update(station);
	}
}
