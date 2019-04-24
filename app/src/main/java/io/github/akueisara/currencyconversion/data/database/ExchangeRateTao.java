package io.github.akueisara.currencyconversion.data.database;


import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import io.reactivex.Single;

/**
 * Created by Kuei on 2019-04-24.
 */

@Dao
public interface ExchangeRateTao {

    @Insert
    void insertExchangeRate(ExchangeRateEntry exchangeRateEntry);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void updateExchangeRate(ExchangeRateEntry exchangeRateEntry);

    @Delete
    void deleteExchangeRate(ExchangeRateEntry exchangeRateEntry);

    @Query("SELECT * FROM exchange_rates WHERE source = :source")
    Single<ExchangeRateEntry> loadExchangeRateBySource(String source);

    @Query("DELETE FROM exchange_rates")
    void deleteAllExchangeRates();
}
