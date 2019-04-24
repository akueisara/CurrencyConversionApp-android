package io.github.akueisara.currencyconversion.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by Kuei on 2019-04-23.
 */
public final class SharePreferences {
    private static final String PREF_LAST_UPDATE_RATES_TIME = "last_update_rates_time";
    private static final String PREF_SELECTED_CURRENCY = "selected_currency";

    public static void saveLastUpdateRatesTime(Context context, long timeOfRatesUpdate) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sp.edit();
        editor.putLong(PREF_LAST_UPDATE_RATES_TIME, timeOfRatesUpdate);
        editor.apply();
    }

    public static long getLastUpdateRatesTimeInMillis(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getLong(PREF_LAST_UPDATE_RATES_TIME, 0);
    }

    public static void saveSelectedCurrency(Context context, String currency) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(PREF_SELECTED_CURRENCY, currency);
        editor.apply();
    }

    public static String getSelectedCurrency(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getString(PREF_SELECTED_CURRENCY, "");
    }
}
