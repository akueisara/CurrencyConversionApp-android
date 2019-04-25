package io.github.akueisara.currencyconversion.persistence;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import io.github.akueisara.currencyconversion.api.CurrencyLayerApiManager;

/**
 * Created by Kuei on 2019-04-23.
 */
public final class SharePreferences {
    private static final String PREF_SELECTED_CURRENCY = "selected_currency";

    public static void saveSelectedCurrency(Context context, String currency) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(PREF_SELECTED_CURRENCY, currency);
        editor.apply();
    }

    public static String getSelectedCurrency(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getString(PREF_SELECTED_CURRENCY, CurrencyLayerApiManager.API_DEFAULT_CURRENCY);
    }
}
