package io.github.akueisara.currencyconversion.api.model;

/**
 * Created by Kuei on 2019-04-22.
 */
public class Currency {
    private String mShortName;
    private String mCompleteName;

    Currency(String showName, String completeName) {
        mShortName = showName;
        mCompleteName = completeName;
    }

    public String getShortName() {
        return mShortName;
    }

    public String getCompleteName() {
        return mCompleteName;
    }
}
