package io.github.akueisara.currencyconversion.api;

import java.io.IOException;
import java.lang.annotation.Annotation;

import io.github.akueisara.currencyconversion.BuildConfig;
import io.github.akueisara.currencyconversion.api.model.CurrencyLayerApiError.CurrencyLayerErrorResponse;
import io.github.akueisara.currencyconversion.api.model.SupportedCurrencies;
import io.github.akueisara.currencyconversion.api.model.ExchangeRates;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Converter;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by Kuei on 2019-04-21.
 */
public final class CurrencyLayerApiManager {

    private static final String API_BASE_URL = "http://apilayer.net/api/";
    public static final String API_DEFAULT_CURRENCY = "USD";

    private static volatile CurrencyLayerApiManager sharedInstance = new CurrencyLayerApiManager();
    private CurrencyLayerApiService mCurrencyLayerApiService;

    private Retrofit.Builder mRetrofit;

    private CurrencyLayerApiManager() {
        if (sharedInstance != null) {
            throw new RuntimeException("Use getInstance() method to get the single instance of this class.");
        }

        mRetrofit = new Retrofit.Builder()
                .baseUrl(API_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create());

        if(BuildConfig.DEBUG) {
            HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY);
            OkHttpClient client = new OkHttpClient.Builder()
                    .addNetworkInterceptor(interceptor)
                    .build();
            mRetrofit.client(client);
        }

        mCurrencyLayerApiService = mRetrofit.build().create(CurrencyLayerApiService.class);
    }

    public Retrofit.Builder getRetrofitBuilder() {
        return mRetrofit;
    }

    public static CurrencyLayerApiManager getInstance() {
        if (sharedInstance == null) {
            synchronized (CurrencyLayerApiManager.class) {
                if (sharedInstance == null) sharedInstance = new CurrencyLayerApiManager();
            }
        }

        return sharedInstance;
    }


    public void getExchangeRateData(String source, Observer<ExchangeRates> observer) {
        Observable<ExchangeRates> observable;
        if(source.equals(API_DEFAULT_CURRENCY)) {
            observable = mCurrencyLayerApiService.getDefaultExchangeRatesData(BuildConfig.CURRENCYLAYER_API_KEY);
        } else {
            observable = mCurrencyLayerApiService.getExchangeRateData(BuildConfig.CURRENCYLAYER_API_KEY, source);
        }
        observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer);
    }

    public void getCurrencyList(Observer<SupportedCurrencies> observer) {
        mCurrencyLayerApiService.getSupportedCurrencies(BuildConfig.CURRENCYLAYER_API_KEY)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer);
    }

    public CurrencyLayerErrorResponse parseCurrencyLayerErrorResponse(Response<?> response) {
        Converter<ResponseBody, CurrencyLayerErrorResponse> converter = mRetrofit.build().responseBodyConverter(CurrencyLayerErrorResponse.class, new Annotation[0]);

        CurrencyLayerErrorResponse error;

        try {
            error = converter.convert(response.errorBody());
        } catch (IOException e) {
            return new CurrencyLayerErrorResponse();
        }

        return error;
    }
}
