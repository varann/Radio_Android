package org.gigahub.radio.android;

import org.androidannotations.annotations.sharedpreferences.SharedPref;

import java.util.Date;

/**
 * Created by asavinova on 14/10/14.
 */
@SharedPref(value=SharedPref.Scope.UNIQUE)
public interface Preferences {

	long lastUpdated();

}
