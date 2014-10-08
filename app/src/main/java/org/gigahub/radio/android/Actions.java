package org.gigahub.radio.android;

/**
 * Created by asavinova on 06/10/14.
 */
public interface Actions {

	public static final String PREFIX = "org.gigahub.radio.android.";

	public static final String PLAY_PAUSE = PREFIX + "PLAY_PAUSE";
	public static final String STOP = PREFIX + "STOP";

	public static final String STATE_PLAY = PREFIX + "STATE_PLAY";
	public static final String STATE_PAUSE = PREFIX + "STATE_PAUSE";
	public static final String STATE_STOP = PREFIX + "STATE_STOP";
	public static final String STATE_PREPARE = PREFIX + "STATE_PREPARE";
	public static final String STATE_ERROR = PREFIX + "STATE_ERROR";

}
