package io.github.akueisara.currencyconversion;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.github.akueisara.currencyconversion.api.CurrencyLayerApiManager;
import io.github.akueisara.currencyconversion.api.model.SupportedCurrencies;
import io.github.akueisara.currencyconversion.api.model.ExchangeRates;
import io.github.akueisara.currencyconversion.data.SharePreferences;
import io.github.akueisara.currencyconversion.data.database.AppDatabase;
import io.github.akueisara.currencyconversion.data.database.ExchangeRateEntry;
import io.github.akueisara.currencyconversion.utils.ErrorUtils;
import io.github.akueisara.currencyconversion.utils.Utils;
import io.reactivex.Completable;
import io.reactivex.CompletableObserver;
import io.reactivex.Observer;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

public class MainActivity extends AppCompatActivity {

    public static final String SUPPORTED_CURRENCY = "supported_currency";
    public static final String EXCHANGE_RATE_HASH_MAP = "exchange_rate_hash_map";

    @BindView(R.id.currency_spinner)
    Spinner mCurrencySpinner;
    @BindView(R.id.exchange_rate_recycler_view)
    RecyclerView mExchangeRateRecyclerView;

    private ExchangeRateAdapter mExchangeRateAdapter;
    private String[] mCurrencyArray;
    private Map<String, Double> mExchangeRateList;

    private AppDatabase mDb;
    private CompositeDisposable mCompositeDisposable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }

        if (SharePreferences.getSelectedCurrency(this).isEmpty()) {
            SharePreferences.saveSelectedCurrency(this, CurrencyLayerApiManager.API_DEFAULT_CURRENCY);
        }

        if(savedInstanceState != null) {
            mExchangeRateList = (Map<String, Double>) savedInstanceState.getSerializable(EXCHANGE_RATE_HASH_MAP);
            mCurrencyArray = savedInstanceState.getStringArray(SUPPORTED_CURRENCY);
            setupCurrencySpinner(mCurrencyArray);
            setupExchangeRateView(mExchangeRateList);
        } else {
            getSupportedCurrenciesApiRequest();
        }

        mDb = AppDatabase.getInstance(getApplicationContext());
        setupViewModel();

        mCompositeDisposable = new CompositeDisposable();
    }

    private void setupViewModel() {
        MainViewModel viewModel = ViewModelProviders.of(this).get(MainViewModel.class);
        viewModel.getExchangeRates(new SingleObserver<ExchangeRateEntry>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onSuccess(ExchangeRateEntry exchangeRateEntry) {
                Timber.d("getExchangeRates success: %s", exchangeRateEntry.getQuotes());
                mExchangeRateList = new Gson().fromJson(exchangeRateEntry.getQuotes(), Map.class);
                setupExchangeRateView(mExchangeRateList);
            }

            @Override
            public void onError(Throwable e) {
                Timber.d(e, e.getLocalizedMessage());
            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();

        if(durationOverThirtyMinutes(SharePreferences.getLastUpdateRatesTimeInMillis(this))) {
            getExchangeRateBaseOnCurrencyApiRequest(SharePreferences.getSelectedCurrency(this));
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(EXCHANGE_RATE_HASH_MAP, (Serializable) mExchangeRateList);
        outState.putStringArray(SUPPORTED_CURRENCY, mCurrencyArray);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mCompositeDisposable.dispose();
    }

    private void getSupportedCurrenciesApiRequest() {
        CurrencyLayerApiManager.getInstance().getCurrencyList(this, new Observer<SupportedCurrencies>() {
            @Override
            public void onSubscribe(Disposable d) {}

            @Override
            public void onNext(SupportedCurrencies value) {
                if(value.getSuccess()) {
                    Object[] currencyArray = value.getCurrencies().keySet().toArray();
                    if (currencyArray != null) {
                        setupCurrencySpinner(Arrays.copyOf(currencyArray, currencyArray.length, String[].class));
                    }
                } else {
                    Timber.d(value.getErrorMessage().toString());
                }

            }

            @Override
            public void onError(Throwable e) {
                ErrorUtils.parseError(MainActivity.this, e);
            }

            @Override
            public void onComplete() {}
        });
    }

    private void setupCurrencySpinner(String[] currencyArray) {
        if(currencyArray != null) {
            mCurrencyArray = currencyArray;
            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_spinner_dropdown_item, mCurrencyArray);
            mCurrencySpinner.setAdapter(arrayAdapter);
            int spinnerPosition = arrayAdapter.getPosition(SharePreferences.getSelectedCurrency(this));
            mCurrencySpinner.setSelection(spinnerPosition, false);
            mCurrencySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    String selectedCurrency = currencyArray[position];
                    getExchangeRateBaseOnCurrencyApiRequest(selectedCurrency);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
        }
    }

    private void getExchangeRateBaseOnCurrencyApiRequest(String source) {
        CurrencyLayerApiManager.getInstance().getExchangeRateData(this, source, new Observer<ExchangeRates>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(ExchangeRates value) {
                if(value.getSuccess()) {
                    Timber.d("get ExchangeRate from API: %s", value);
                    int time = (int) (System.currentTimeMillis() / 1000L);
                    setupExchangeRateView(value.getQuotes());
                    SharePreferences.saveSelectedCurrency(MainActivity.this, value.getSource());

                    Gson gson = new GsonBuilder().create();
                    String quoteJsonString = gson.toJson(value.getQuotes());
                    ExchangeRateEntry exchangeRateEntry = new ExchangeRateEntry(time, value.getSource(), quoteJsonString);
                    loadExchangeRates(source, exchangeRateEntry);

                } else {
                    Timber.d(value.getErrorMessage().toString());
                }
            }

            @Override
            public void onError(Throwable e) {
                ErrorUtils.parseError(MainActivity.this, e);
            }

            @Override
            public void onComplete() {

            }
        });
    }

    private void loadExchangeRates(String source, ExchangeRateEntry exchangeRateEntry) {
        Disposable disposable = mDb.exchangeRateTao().loadExchangeRateBySource(source)
                .subscribeOn(Schedulers.io())
                .subscribe(exchangeRateEntry1 -> {
                    Timber.d("Last Updated Time for %s: %s, %s", source, String.valueOf(exchangeRateEntry1.getUpdatedAt() * 1000L), new Date(exchangeRateEntry1.getUpdatedAt() * 1000L));
                    if(durationOverThirtyMinutes(exchangeRateEntry1.getUpdatedAt() * 1000L)) {
                        exchangeRateEntry1.setQuotes(exchangeRateEntry.getQuotes());
                        exchangeRateEntry1.setUpdatedAt(exchangeRateEntry.getUpdatedAt());
                        updateExchangeRates(exchangeRateEntry1);
                        Timber.d("loadExchangeRateBySource success %s", exchangeRateEntry1.getQuotes());
                    }
                }, throwable -> {
                    Timber.d(throwable, throwable.getLocalizedMessage());
                    insertExchangeRates(exchangeRateEntry);
                });
        mCompositeDisposable.add(disposable);
    }

    private void updateExchangeRates(ExchangeRateEntry exchangeRateEntry1) {
        Completable.fromAction(() -> mDb.exchangeRateTao().updateExchangeRate(exchangeRateEntry1))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new CompletableObserver() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onComplete() {
                        Timber.d("updateExchangeRate success");
                        SharePreferences.saveLastUpdateRatesTime(MainActivity.this, exchangeRateEntry1.getUpdatedAt() * 1000L);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Timber.d(e, e.getLocalizedMessage());
                    }
                });
    }

    private void insertExchangeRates(ExchangeRateEntry exchangeRateEntry) {
        Completable.fromAction(() -> mDb.exchangeRateTao().insertExchangeRate(exchangeRateEntry)).observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new CompletableObserver() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onComplete() {
                        Timber.d("insertExchangeRate success");
                        SharePreferences.saveLastUpdateRatesTime(MainActivity.this, exchangeRateEntry.getUpdatedAt() * 1000L);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Timber.d(e, e.getLocalizedMessage());
                    }
                });
    }

    private void setupExchangeRateView(Map<String, Double> exchangeRateList) {
        if(exchangeRateList != null) {
            mExchangeRateList = exchangeRateList;
            mExchangeRateAdapter = new ExchangeRateAdapter(this, mExchangeRateList);
            mExchangeRateRecyclerView.setAdapter(mExchangeRateAdapter);
            mExchangeRateRecyclerView.setLayoutManager(new GridLayoutManager(this, Utils.calculateNoOfColumns(this, mExchangeRateList.size(), (int) getResources().getDimension(R.dimen.exchange_rate_item_width))));
        }
    }

    private boolean durationOverThirtyMinutes(long time) {
        Timber.d("Current time: %s", System.currentTimeMillis());
        Timber.d(String.valueOf(TimeUnit.MINUTES.toMillis(30)));
        return System.currentTimeMillis() - time > TimeUnit.MINUTES.toMillis(30);
    }
}
