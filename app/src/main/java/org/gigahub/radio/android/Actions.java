package org.gigahub.radio.android;

/**
 * Created by asavinova on 06/10/14.
 */
public interface Actions {

	enum STATE {
		PLAY,
		PAUSE,
		STOP,
		PREPARE,
		ERROR
	}

	public static final String PREFIX = "org.gigahub.radio.android.";

	public static final String PLAY_PAUSE = PREFIX + "PLAY_PAUSE";
	public static final String STOP = PREFIX + "STOP";
	public static final String UPDATE_STATE = PREFIX + "UPDATE_STATE";
	public static final String CHANGE_FAVOURITE = PREFIX + "CHANGE_FAVOURITE";
	public static final String DB_UPDATE = PREFIX + "DB_UPDATE";
}
