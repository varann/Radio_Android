package org.gigahub.radio.android;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.gigahub.radio.android.api.Station;

/**
 * Created by asavinova on 10/10/14.
 */
public class StationsAdapter extends ArrayAdapter<Station> {

	static class ViewHolder {
		TextView name;
	}

	private LayoutInflater layoutInflater;

	public StationsAdapter(Context context) {
		super(context, R.layout.station_list_item);
		layoutInflater = LayoutInflater.from(context);
	}

	@Override
	public View getView(int position, View view, ViewGroup parent) {

		ViewHolder viewHolder;

		if (view == null) {
			view = layoutInflater.inflate(R.layout.station_list_item, parent, false);
			viewHolder = new ViewHolder();
			viewHolder.name = (TextView) view.findViewById(R.id.name);
			view.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) view.getTag();

		}

		viewHolder.name.setText(getItem(position).getName());

		return view;
	}

}
