package io.github.akueisara.currencyconversion.persistence.database;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;
import android.content.Context;

import com.orhanobut.logger.Logger;

import java.util.Date;

import io.github.akueisara.currencyconversion.persistence.SharePreferences;
import io.github.akueisara.currencyconversion.utils.TimeUtils;
import io.reactivex.Completable;
import io.reactivex.CompletableObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by Kuei on 2019-04-25.
 */
@Database(entities = {ExchangeRateEntry.class}, version = 1, exportSchema = false)
@TypeConverters(DateConverter.class)
public abstract class AppDatabase extends RoomDatabase {

    private static final String LOG_TAG = AppDatabase.class.getSimpleName();
    private static final Object LOCK = new Object();
    private static final String DATABASE_NAME = "currency_conversion.db";
    private static AppDatabase sInstance;
    private static CompositeDisposable mCompositeDisposable;

    public static AppDatabase getInstance(Context context) {
        if (sInstance == null) {
            synchronized (LOCK) {
                sInstance = Room.databaseBuilder(context.getApplicationContext(),
                        AppDatabase.class, AppDatabase.DATABASE_NAME)
                        .fallbackToDestructiveMigration()
                        .build();
                mCompositeDisposable = new CompositeDisposable();
            }
        }
        return sInstance;
    }

    public abstract ExchangeRateTao exchangeRateTao();

    public void loadExchangeRates(Context context, String source, ExchangeRateEntry exchangeRateEntry) {
        Disposable disposable = sInstance.exchangeRateTao().loadExchangeRateBySource(source)
                .subscribeOn(Schedulers.io())
                .subscribe(exchangeRateEntry1 -> {
                    Logger.d("Last Updated Time for %s: %s, %s", source, String.valueOf(exchangeRateEntry1.getUpdatedAt() * 1000L), new Date(exchangeRateEntry1.getUpdatedAt() * 1000L));
                    if(TimeUtils.durationOverThirtyMinutes(exchangeRateEntry1.getUpdatedAt() * 1000L)) {
                        exchangeRateEntry1.setQuotes(exchangeRateEntry.getQuotes());
                        exchangeRateEntry1.setUpdatedAt(exchangeRateEntry.getUpdatedAt());
                        updateExchangeRates(context, exchangeRateEntry1);
                        Logger.d("loadExchangeRateBySource success %s", exchangeRateEntry1.getQuotes());
                    }
                }, throwable -> {
//                    Logger.e(throwable, throwable.getLocalizedMessage());
                    insertExchangeRates(context, exchangeRateEntry);
                });
        mCompositeDisposable.add(disposable);
    }

    private void updateExchangeRates(Context context, ExchangeRateEntry exchangeRateEntry1) {
        Completable.fromAction(() -> sInstance.exchangeRateTao().updateExchangeRate(exchangeRateEntry1))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new CompletableObserver() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onComplete() {
                        Logger.d("updateExchangeRate success");
                        SharePreferences.saveLastUpdateRatesTime(context, exchangeRateEntry1.getUpdatedAt() * 1000L);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Logger.e(e, e.getLocalizedMessage());
                    }
                });
    }

    private void insertExchangeRates(Context context, ExchangeRateEntry exchangeRateEntry) {
        Completable.fromAction(() -> sInstance.exchangeRateTao().insertExchangeRate(exchangeRateEntry)).observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new CompletableObserver() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onComplete() {
                        Logger.d("insertExchangeRate success");
                        SharePreferences.saveLastUpdateRatesTime(context, exchangeRateEntry.getUpdatedAt() * 1000L);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Logger.e(e, e.getLocalizedMessage());
                    }
                });
    }

    public void disposeCompositeDisposable() {
        mCompositeDisposable.dispose();
    }
}