package io.github.akueisara.currencyconversion;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.github.akueisara.currencyconversion.api.CurrencyLayerApiManager;
import io.github.akueisara.currencyconversion.api.model.SupportedCurrencies;
import io.github.akueisara.currencyconversion.api.model.ExchangeRates;
import io.github.akueisara.currencyconversion.data.CurrencyConversionPreferences;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import timber.log.Timber;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.currency_spinner)
    Spinner mCurrencySpinner;

    private boolean mInitSpinner = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }

        if (CurrencyConversionPreferences.getSelectedCurrency(this).isEmpty()) {
            CurrencyConversionPreferences.saveSelectedCurrency(this, CurrencyLayerApiManager.API_DEFAULT_CURRENCY);
        }
        getSupportedCurrenciesApiRequest();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (System.currentTimeMillis() - CurrencyConversionPreferences.getLastUpdateRatesTimeInMillis(this) > TimeUnit.MINUTES.toMillis(30)) {
            getExchangeRateBaseOnCurrencyApiRequest(CurrencyConversionPreferences.getSelectedCurrency(this));
        }
    }

    private void getSupportedCurrenciesApiRequest() {
        CurrencyLayerApiManager.getInstance().getCurrencyList(new Observer<SupportedCurrencies>() {
            @Override
            public void onSubscribe(Disposable d) {
                Timber.d("Disposable");
            }

            @Override
            public void onNext(SupportedCurrencies value) {
                if(value.getSuccess()) {
                    setupCurrencySpinner(value);
                } else {
                    Timber.d(value.getErrorMessage().toString());
                    Toast.makeText(MainActivity.this, value.getErrorMessage().toString(), Toast.LENGTH_LONG).show();
                }

            }

            @Override
            public void onError(Throwable e) {
                Timber.e(e, "error while calling supported currencies api");
            }

            @Override
            public void onComplete() {
                Timber.d("onComplete");
            }
        });
    }

    private void getExchangeRateBaseOnCurrencyApiRequest(String source) {
        CurrencyLayerApiManager.getInstance().getExchangeRateData(source, new Observer<ExchangeRates>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(ExchangeRates value) {
                if(value.getSuccess()) {
                    CurrencyConversionPreferences.saveLastUpdateRatesTime(MainActivity.this, System.currentTimeMillis());
                } else {
                    Timber.d(value.getErrorMessage().toString());
                    Toast.makeText(MainActivity.this, value.getErrorMessage().toString(), Toast.LENGTH_LONG).show();
                }

            }

            @Override
            public void onError(Throwable e) {
                Timber.e(e, "error while calling ExchangeRate api");
            }

            @Override
            public void onComplete() {

            }
        });
    }

    private void setupCurrencySpinner(SupportedCurrencies currencyListResponse) {
        Object[] currencyArray = currencyListResponse.getCurrencies().keySet().toArray();
        if (currencyArray != null) {
            ArrayAdapter<Object> arrayAdapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_spinner_dropdown_item, currencyArray);
            mCurrencySpinner.setAdapter(arrayAdapter);
            int spinnerPosition = arrayAdapter.getPosition(CurrencyConversionPreferences.getSelectedCurrency(this));
            mCurrencySpinner.setSelection(spinnerPosition);
            mCurrencySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if (!mInitSpinner) {
                        mInitSpinner = true;
                        return;
                    }
                    String selectedCurrency = (String) currencyArray[position];
                    CurrencyConversionPreferences.saveSelectedCurrency(MainActivity.this, selectedCurrency);
                    getExchangeRateBaseOnCurrencyApiRequest(selectedCurrency);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
        }
    }
}
