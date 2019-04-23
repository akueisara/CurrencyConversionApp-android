package io.github.akueisara.currencyconversion.api;

import io.github.akueisara.currencyconversion.api.model.SupportedCurrencies;
import io.github.akueisara.currencyconversion.api.model.ExchangeRates;
import io.reactivex.Observable;
import okhttp3.ResponseBody;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by Kuei on 2019-04-21.
 */
public interface CurrencyLayerApiService {

    @GET("live")
    Observable<ExchangeRates> getDefaultExchangeRatesData(@Query("access_key") String accessKey);

    @GET("live")
    Observable<ExchangeRates> getExchangeRateData(@Query("access_key") String accessKey, @Query("source") String currency);

    @GET("list")
    Observable<SupportedCurrencies> getSupportedCurrencies(@Query("access_key") String accessKey);

    @GET("convert")
    Observable<ResponseBody> convertCurrencyAmount(@Query("access_key") String accessKey, @Query("from") String from, @Query("to") String to,  @Query("amount") double amount);

}
