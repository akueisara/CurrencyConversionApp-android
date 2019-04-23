package io.github.akueisara.currencyconversion.api;

import android.arch.core.executor.testing.InstantTaskExecutorRule;
import android.support.annotation.NonNull;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.github.akueisara.currencyconversion.BuildConfig;
import io.github.akueisara.currencyconversion.api.model.ExchangeRates;
import io.github.akueisara.currencyconversion.api.model.SupportedCurrencies;
import io.reactivex.Scheduler;
import io.reactivex.android.plugins.RxAndroidPlugins;
import io.reactivex.disposables.Disposable;
import io.reactivex.internal.schedulers.ExecutorScheduler;
import io.reactivex.observers.TestObserver;
import io.reactivex.plugins.RxJavaPlugins;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import okio.BufferedSource;
import okio.Okio;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsNull.notNullValue;

/**
 * Created by Kuei on 2019-04-22.
 */

@RunWith(JUnit4.class)
public class CurrencyLayerApiServiceTest {

    @Rule
    public InstantTaskExecutorRule instantExecutorRule = new InstantTaskExecutorRule();

    @Mock
    private CurrencyLayerApiService mService;

    private MockWebServer mMockWebServer;

    @BeforeClass
    public static void setUpRxSchedulers() {
        Scheduler immediate = new Scheduler() {
            @Override
            public Disposable scheduleDirect(@NonNull Runnable run, long delay, @NonNull TimeUnit unit) {
                return super.scheduleDirect(run, 0, unit);
            }

            @Override
            public Worker createWorker() {
                return new ExecutorScheduler.ExecutorWorker(Runnable::run);
            }
        };

        RxJavaPlugins.setInitIoSchedulerHandler(scheduler -> immediate);
        RxJavaPlugins.setInitComputationSchedulerHandler(scheduler -> immediate);
        RxJavaPlugins.setInitNewThreadSchedulerHandler(scheduler -> immediate);
        RxJavaPlugins.setInitSingleSchedulerHandler(scheduler -> immediate);
        RxAndroidPlugins.setInitMainThreadSchedulerHandler(scheduler -> immediate);
    }

    @Before
    public  void createService() {
        mMockWebServer = new MockWebServer();
        mService = new Retrofit.Builder()
                .baseUrl(mMockWebServer.url("/"))
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build()
                .create(CurrencyLayerApiService.class);
    }

    @After
    public  void stopService() throws IOException {
        mMockWebServer.shutdown();
    }

    @Test
    public void getSupportedCurrenciesTest() throws IOException, InterruptedException {
        enqueueResponse("currency_list.json");
        RecordedRequest request = mMockWebServer.takeRequest();
        assertThat(request.getPath(), is("/list?access_key=" + BuildConfig.CURRENCYLAYER_API_KEY));

        TestObserver<SupportedCurrencies> observer = mService.getSupportedCurrencies(BuildConfig.CURRENCYLAYER_API_KEY)
                .test();

        observer.assertValueCount(1);
        assertThat(observer.values().get(0), notNullValue());
        SupportedCurrencies supportedCurrencies = observer.values().get(0);
        assertThat("getSuccess", supportedCurrencies.getSuccess());
        assertThat(supportedCurrencies.getTerms(), is("https://currencylayer.com/terms"));
        assertThat(supportedCurrencies.getPrivacy(), is("https://currencylayer.com/privacy"));
        assertThat(supportedCurrencies.getSupportedCurrencyList().get(0).getShortName(), is("AED"));
        assertThat(supportedCurrencies.getSupportedCurrencyList().get(supportedCurrencies.getSupportedCurrencyList().size()-1).getCompleteName(), is("Zimbabwean Dollar"));
    }

    @Test
    public void getDefaultExchangeRatesTest() throws IOException, InterruptedException {
        enqueueResponse("default_exchange_rates.json");
        RecordedRequest request = mMockWebServer.takeRequest();
        assertThat(request.getPath(), is("/live?access_key=" + BuildConfig.CURRENCYLAYER_API_KEY));
        TestObserver<ExchangeRates> observer = mService.getDefaultExchangeRatesData(BuildConfig.CURRENCYLAYER_API_KEY).test();

        observer.assertValueCount(1);
        assertThat(observer.values().get(0), notNullValue());
        ExchangeRates exchangeRates = observer.values().get(0);
        assertThat("getSuccess", exchangeRates.getSuccess());
        assertThat(exchangeRates.getTerms(), is("https://currencylayer.com/terms"));
        assertThat(exchangeRates.getPrivacy(), is("https://currencylayer.com/privacy"));
        assertThat(exchangeRates.getSource(), is("USD"));
        assertThat(exchangeRates.getTimeStamp(), is(1556039344));
        assertThat(exchangeRates.getQuotes().get("USDBYN"), is(2.08595));
        Iterator it = exchangeRates.getQuotes().entrySet().iterator();
        if (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            assertThat(pair.getKey(), is("USDAED"));
            assertThat(pair.getValue(), is(3.673097));
        }

    }

    private  void enqueueResponse(String fileName) throws IOException {
        enqueueResponse(fileName, new HashMap<>());
    }

    private  void enqueueResponse(String fileName, Map<String, String> headers) throws IOException {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("api-response/" + fileName);
        BufferedSource source = Okio.buffer(Okio.source(inputStream));
        MockResponse mockResponse = new MockResponse();
        for (Map.Entry<String, String> header : headers.entrySet()) {
            mockResponse.addHeader(header.getKey(), header.getValue());
        }
        mMockWebServer.enqueue(mockResponse.setBody(source.readString(StandardCharsets.UTF_8)));
    }
}