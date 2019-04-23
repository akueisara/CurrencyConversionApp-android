package io.github.akueisara.currencyconversion.api.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.github.akueisara.currencyconversion.api.model.CurrencyLayerApiError.CurrencyLayerErrorResponse;

/**
 * Created by Kuei on 2019-04-22.
 */
public class SupportedCurrencies extends CurrencyLayerErrorResponse {

    @SerializedName("terms")
    private String mTerms;

    @SerializedName("privacy")
    private String mPrivacy;

    @SerializedName("currencies")
    private Map<String, String> mCurrencies;

    private List<Currency> mSupportedCurrencyList;

    public SupportedCurrencies(String success, String terms, String privacy, Map<String, String> currencies) {
        this.mTerms = terms;
        this.mPrivacy = privacy;
        this.mCurrencies = currencies;
    }

    public String getTerms() {
        return mTerms;
    }

    public String getPrivacy() {
        return mPrivacy;
    }

    public Map<String, String> getCurrencies() {
        return mCurrencies;
    }

    public List<Currency> getSupportedCurrencyList() {
        if(mSupportedCurrencyList == null) {
            mSupportedCurrencyList = new ArrayList<>();
            for (Map.Entry<String, String> entry : mCurrencies.entrySet()) {
                mSupportedCurrencyList.add(new Currency(entry.getKey(), entry.getValue()));
            }
            return mSupportedCurrencyList;
        }
        return mSupportedCurrencyList;
    }
}
