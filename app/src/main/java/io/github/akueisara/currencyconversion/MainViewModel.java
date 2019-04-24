package io.github.akueisara.currencyconversion;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;

import io.github.akueisara.currencyconversion.data.SharePreferences;
import io.github.akueisara.currencyconversion.data.database.AppDatabase;
import io.github.akueisara.currencyconversion.data.database.ExchangeRateEntry;
import io.reactivex.Observer;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by Kuei on 2019-04-25.
 */
public class MainViewModel extends AndroidViewModel {

    private Application mApplication;

    public MainViewModel(Application application) {
        super(application);
        mApplication = application;
    }

    public void getExchangeRates(SingleObserver<ExchangeRateEntry> observer) {
        AppDatabase database = AppDatabase.getInstance(this.getApplication());
        database.exchangeRateTao().loadExchangeRateBySource(SharePreferences.getSelectedCurrency(mApplication))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer);
    }
}