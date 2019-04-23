package io.github.akueisara.currencyconversion.api.model;

import com.google.gson.annotations.SerializedName;

import java.util.Map;

import io.github.akueisara.currencyconversion.api.model.CurrencyLayerApiError.CurrencyLayerErrorResponse;

/**
 * Created by Kuei on 2019-04-23.
 */
public class ExchangeRates extends CurrencyLayerErrorResponse {

    @SerializedName("terms")
    private String mTerms;

    @SerializedName("privacy")
    private String mPrivacy;

    @SerializedName("timestamp")
    private long mTimeStamp;

    @SerializedName("source")
    private String mSource;

    @SerializedName("quotes")
    private Map<String, Double> mQuotes;

    public String getTerms() {
        return mTerms;
    }

    public String getPrivacy() {
        return mPrivacy;
    }

    public long getTimeStamp() {
        return mTimeStamp;
    }

    public String getSource() {
        return mSource;
    }

    public Map<String, Double> getQuotes() {
        return mQuotes;
    }
}
