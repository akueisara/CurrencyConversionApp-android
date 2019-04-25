package io.github.akueisara.currencyconversion.api.model;

import com.google.gson.annotations.SerializedName;

import java.util.Map;

/**
 * Created by Kuei on 2019-04-23.
 */
public class ExchangeRates extends CurrencyLayerBasicResponse {

    @SerializedName("terms")
    private String mTerms;

    @SerializedName("privacy")
    private String mPrivacy;

    @SerializedName("timestamp")
    private int mTimeStamp;

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

    public int getTimeStamp() {
        return mTimeStamp;
    }

    public String getSource() {
        return mSource;
    }

    public Map<String, Double> getQuotes() {
        return mQuotes;
    }

    @Override
    public String toString() {
        return "ExchangeRates{" + "mTerms='" + mTerms + '\'' + ", mPrivacy='" + mPrivacy + '\'' + ", mTimeStamp=" + mTimeStamp + ", mSource='" + mSource + '\'' + ", mQuotes=" + mQuotes + '}';
    }
}
