package io.github.akueisara.currencyconversion.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.orhanobut.logger.Logger;

import java.util.Locale;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.github.akueisara.currencyconversion.R;

/**
 * Created by Kuei on 2019-04-24.
 */
public class ExchangeRateAdapter extends RecyclerView.Adapter<ExchangeRateAdapter.ViewHolder> {

    private Map<String, Double> mExchangeRateList;
    private Object[] mExchangeRateKeyArray;
    private Context mContext;

    public ExchangeRateAdapter(Context context, Map<String, Double> exchangeRateList) {
        mContext = context;
        mExchangeRateList = exchangeRateList;
        mExchangeRateKeyArray = exchangeRateList.keySet().toArray();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        View taskView = inflater.inflate(R.layout.item_exchange_rate, viewGroup, false);
        return new ExchangeRateAdapter.ViewHolder(taskView);
    }

    @Override
    public void onBindViewHolder(@NonNull ExchangeRateAdapter.ViewHolder viewHolder, int i) {
        String currencyName = (String) mExchangeRateKeyArray[i];
        Double currencyRate = mExchangeRateList.get(currencyName);
        int resId = mContext.getResources().getIdentifier("flag_" + currencyName.substring(3).toLowerCase(), "drawable", mContext.getPackageName());
        if (resId != 0) {
            viewHolder.mCurrencyFlagImageView.setImageDrawable(mContext.getResources().getDrawable(resId));
        }
        viewHolder.mCurrencyNameText.setText(currencyName.substring(3));
        viewHolder.mCurrencyExchangeRateText.setText(String.format(Locale.US, "%.2f", currencyRate));
    }

    @Override
    public int getItemCount() {
        return mExchangeRateList.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.currency_flag_image_view)
        ImageView mCurrencyFlagImageView;
        @BindView(R.id.currency_name_text)
        TextView mCurrencyNameText;
        @BindView(R.id.currency_exchange_rate_text)
        TextView mCurrencyExchangeRateText;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    public void setExchangeRate(Map<String, Double> exchangeRateList) {
        mExchangeRateList = exchangeRateList;
        mExchangeRateKeyArray = exchangeRateList.keySet().toArray();
        notifyDataSetChanged();
    }

}
