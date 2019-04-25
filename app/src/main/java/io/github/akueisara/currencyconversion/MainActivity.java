package io.github.akueisara.currencyconversion;

import android.arch.lifecycle.ViewModelProviders;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.Spinner;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.Logger;

import java.io.Serializable;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.github.akueisara.currencyconversion.adapters.ExchangeRateAdapter;
import io.github.akueisara.currencyconversion.api.CurrencyLayerApiManager;
import io.github.akueisara.currencyconversion.api.model.SupportedCurrencies;
import io.github.akueisara.currencyconversion.api.model.ExchangeRates;
import io.github.akueisara.currencyconversion.persistence.SharePreferences;
import io.github.akueisara.currencyconversion.persistence.database.AppDatabase;
import io.github.akueisara.currencyconversion.persistence.database.ExchangeRateEntry;
import io.github.akueisara.currencyconversion.utils.ErrorUtils;
import io.github.akueisara.currencyconversion.utils.TimeUtils;
import io.github.akueisara.currencyconversion.utils.ViewUtils;
import io.reactivex.Observer;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

    public static final String SUPPORTED_CURRENCY = "supported_currency";
    public static final String EXCHANGE_RATE_HASH_MAP = "exchange_rate_hash_map";

    @BindView(R.id.currency_spinner)
    Spinner mCurrencySpinner;
    @BindView(R.id.exchange_rate_recycler_view)
    RecyclerView mExchangeRateRecyclerView;
    @BindView(R.id.currency_amount_edit_text)
    TextInputEditText mCurrencyAmountEditText;
    @BindView(R.id.progress_bar_layout)
    RelativeLayout mProgressBarLayout;

    private ExchangeRateAdapter mExchangeRateAdapter;
    private Map<String, Double> mExchangeRateList;
    private String[] mCurrencyArray;

    private AppDatabase mDb;
    private String mCurrentAmount = "";
    private boolean mNetworkConnection = true;
    private Double mOldRate = 1.00;

    private final BroadcastReceiver mNetworkChangeReceiver = new BroadcastReceiver(){
        Context context;

        @Override
        public void onReceive(Context context, Intent intent) {
            this.context = context;
            if(isOnline()){
                if(!mNetworkConnection) {
                    mNetworkConnection = true;
                    Logger.d("Network connection change: %s", isOnline());
                    if (mCurrencyArray == null || mCurrencyArray.length == 1) {
                        getSupportedCurrenciesApiRequest();
                    }
                    if (mExchangeRateList.size() == 0 || TimeUtils.durationOverThirtyMinutes(SharePreferences.getLastUpdateRatesTimeInMillis(MainActivity.this)) ) {
                        getExchangeRateBaseOnCurrencyApiRequest(SharePreferences.getSelectedCurrency(MainActivity.this));
                    }
                }
            } else {
                mNetworkConnection = false;
            }
        }

        protected boolean isOnline() {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = cm.getActiveNetworkInfo();
            return netInfo != null && netInfo.isConnected();
        }
    };

    @Override
    protected void onStart() {
        super.onStart();

        registerReceiver(mNetworkChangeReceiver, new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        Logger.clearLogAdapters();
        Logger.addLogAdapter(new AndroidLogAdapter() {
            @Override public boolean isLoggable(int priority, String tag) {
                return BuildConfig.DEBUG;
            }
        });

        if (SharePreferences.getSelectedCurrency(this).isEmpty()) {
            SharePreferences.saveSelectedCurrency(this, CurrencyLayerApiManager.API_DEFAULT_CURRENCY);
        }

        initExchangeRateView();

        getSupportedCurrenciesApiRequest();

        setupCurrencyAmountEditTextListeners();

        mDb = AppDatabase.getInstance(getApplicationContext());
        setupViewModel();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (TimeUtils.durationOverThirtyMinutes(SharePreferences.getLastUpdateRatesTimeInMillis(MainActivity.this)) ) {
            getExchangeRateBaseOnCurrencyApiRequest(SharePreferences.getSelectedCurrency(MainActivity.this));
        }
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mExchangeRateList = (Map<String, Double>) savedInstanceState.getSerializable(EXCHANGE_RATE_HASH_MAP);
        refreshExchangeRateView(mExchangeRateList);
        mCurrencyArray = savedInstanceState.getStringArray(SUPPORTED_CURRENCY);
        setupCurrencySpinner(mCurrencyArray);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(EXCHANGE_RATE_HASH_MAP, (Serializable) mExchangeRateList);
        outState.putStringArray(SUPPORTED_CURRENCY, mCurrencyArray);
    }

    @Override
    protected void onStop() {
        super.onStop();

        unregisterReceiver(mNetworkChangeReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mDb.disposeCompositeDisposable();
    }

    private void setupViewModel() {
        MainViewModel viewModel = ViewModelProviders.of(this).get(MainViewModel.class);
        viewModel.getExchangeRates(new SingleObserver<ExchangeRateEntry>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onSuccess(ExchangeRateEntry exchangeRateEntry) {
                Logger.d("getExchangeRates success: %s", exchangeRateEntry.getSource());
                setExchangeRateViewWithLocalData(exchangeRateEntry);
            }

            @Override
            public void onError(Throwable e) {
//                Logger.e(e, e.getLocalizedMessage());
            }
        });
    }

    private void getExchangeRateBaseOnCurrency(String source) {
        mDb.exchangeRateTao().loadExchangeRateBySource(source)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<ExchangeRateEntry>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onSuccess(ExchangeRateEntry exchangeRateEntry) {
                        setExchangeRateViewWithLocalData(exchangeRateEntry);
                        if (TimeUtils.durationOverThirtyMinutes(exchangeRateEntry.getUpdatedAt() * 1000L)) {
                            getExchangeRateBaseOnCurrencyApiRequest(source);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        // No exchange rates in database
                        getExchangeRateBaseOnCurrencyApiRequest(source);
                    }
                });

    }

    private void getExchangeRateBaseOnCurrencyApiRequest(String source) {
        CurrencyLayerApiManager.getInstance().getExchangeRateData(source, new Observer<ExchangeRates>() {
            @Override
            public void onSubscribe(Disposable d) {
                mExchangeRateList = Collections.emptyMap();
                mProgressBarLayout.setVisibility(View.VISIBLE);
            }

            @Override
            public void onNext(ExchangeRates value) {
                if(value.getSuccess()) {
                    Logger.d("get ExchangeRate from API: %s", value);
                    int time = (int) (System.currentTimeMillis() / 1000L);
                    refreshExchangeRateView(value.getQuotes());
                    SharePreferences.saveSelectedCurrency(MainActivity.this, value.getSource());

                    Gson gson = new GsonBuilder().create();
                    String quoteJsonString = gson.toJson(value.getQuotes());
                    ExchangeRateEntry exchangeRateEntry = new ExchangeRateEntry(time, value.getSource(), quoteJsonString);
                    mDb.loadExchangeRates(MainActivity.this, value.getSource(), exchangeRateEntry);
                } else {
                    Logger.d(value.getErrorMessage().toString());
                    if(mExchangeRateList.size() == 0) {
                        mExchangeRateRecyclerView.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onError(Throwable e) {
                ErrorUtils.parseError(MainActivity.this, e);
                mProgressBarLayout.setVisibility(View.GONE);
                if(mExchangeRateList.size() == 0) {
                    mExchangeRateRecyclerView.setVisibility(View.GONE);
                }
            }

            @Override
            public void onComplete() {
                mProgressBarLayout.setVisibility(View.GONE);
            }
        });
    }

    private void getSupportedCurrenciesApiRequest() {
        CurrencyLayerApiManager.getInstance().getCurrencyList(new Observer<SupportedCurrencies>() {
            @Override
            public void onSubscribe(Disposable d) {
                mProgressBarLayout.setVisibility(View.VISIBLE);
            }

            @Override
            public void onNext(SupportedCurrencies value) {
                if(value.getSuccess()) {
                    Object[] currencyArray = value.getCurrencies().keySet().toArray();
                    if (currencyArray != null) {
                        setupCurrencySpinner(Arrays.copyOf(currencyArray, currencyArray.length, String[].class));
                    }
                } else {
                    Logger.d(value.getErrorMessage().toString());
                    if(mCurrencyArray == null) {
                        setupLocalCurrencySpinner();
                    }
                }
            }

            @Override
            public void onError(Throwable e) {
                ErrorUtils.parseError(MainActivity.this, e);
                mProgressBarLayout.setVisibility(View.GONE);
                if(mCurrencyArray == null) {
                    setupLocalCurrencySpinner();
                }
            }

            @Override
            public void onComplete() {
                mProgressBarLayout.setVisibility(View.GONE);
            }
        });
    }

    private void setupCurrencyAmountEditTextListeners() {
        mCurrencyAmountEditText.setOnFocusChangeListener((v, hasFocus) -> {
            if(hasFocus) {
                mOldRate = getPureCurrencyNumber();
                if(getPureCurrencyNumber() == 1) {
                    mCurrencyAmountEditText.setText(getString(R.string.zero_currency_amount));
                }
            } else {
                if(getPureCurrencyNumber() == 0) {
                    mCurrencyAmountEditText.setText(getString(R.string.default_currency_amount));
                }
            }
        });

        mCurrencyAmountEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(!s.toString().equals(mCurrentAmount)){
                    mCurrencyAmountEditText.removeTextChangedListener(this);

                    String cleanString = s.toString().replaceAll("[$,.]", "");

                    double parsed = Double.parseDouble(cleanString);
                    String formatted = NumberFormat.getCurrencyInstance(Locale.US).format((parsed/100)).replace("$", "");

                    mCurrentAmount = formatted;
                    mCurrencyAmountEditText.setText(formatted);
                    mCurrencyAmountEditText.setSelection(formatted.length());

                    mCurrencyAmountEditText.addTextChangedListener(this);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        mCurrencyAmountEditText.setOnKeyListener((v, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_UP) {
                mCurrencyAmountEditText.clearFocus();
                ViewUtils.hideKeyboard(this, mCurrencyAmountEditText);
                refreshExchangeRateView(mExchangeRateList);
                return true;
            }
            return false;
        });
    }

    private Double getPureCurrencyNumber() {
        String cleanString = mCurrencyAmountEditText.getText().toString().replaceAll("[$,.]", "");
        return  Double.parseDouble(cleanString) / 100;
    }

    private void setupLocalCurrencySpinner() {
        String[] array = {SharePreferences.getSelectedCurrency(MainActivity.this)};
        setupCurrencySpinner(array);
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
                    mOldRate = 1.00;
                    mCurrencyAmountEditText.setText(MainActivity.this.getString(R.string.default_currency_amount));
                    String selectedCurrency = currencyArray[position];
                    getExchangeRateBaseOnCurrency(selectedCurrency);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
        }
    }

    private void initExchangeRateView() {
        mExchangeRateList = Collections.emptyMap();
        mExchangeRateAdapter = new ExchangeRateAdapter(this, mExchangeRateList);
        mExchangeRateRecyclerView.setAdapter(mExchangeRateAdapter);
        mExchangeRateRecyclerView.setHasFixedSize(true);
    }

    private void setExchangeRateViewWithLocalData(ExchangeRateEntry exchangeRateEntry) {
        SharePreferences.saveSelectedCurrency(MainActivity.this, exchangeRateEntry.getSource());
        mExchangeRateList = new Gson().fromJson(exchangeRateEntry.getQuotes(), Map.class);
        refreshExchangeRateView(mExchangeRateList);
    }

    private void refreshExchangeRateView(Map<String, Double> exchangeRateList) {
        if(exchangeRateList != null) {
            calculateExchangeRates(exchangeRateList);
            mExchangeRateRecyclerView.setVisibility(View.VISIBLE);
            mExchangeRateRecyclerView.setLayoutManager(new GridLayoutManager(this, ViewUtils.calculateNoOfColumns(this, mExchangeRateList.size(), (int) getResources().getDimension(R.dimen.exchange_rate_item_width))));
        }
    }

    private void calculateExchangeRates(Map<String, Double> exchangeRateList) {
        mExchangeRateList = exchangeRateList;
        for (Map.Entry<String, Double> stringDoubleEntry : mExchangeRateList.entrySet()) {
            Map.Entry pair = stringDoubleEntry;
            mExchangeRateList.put((String) pair.getKey(), (Double) pair.getValue() / mOldRate * getPureCurrencyNumber());
            mExchangeRateAdapter.setExchangeRate(mExchangeRateList);
        }
    }
}
