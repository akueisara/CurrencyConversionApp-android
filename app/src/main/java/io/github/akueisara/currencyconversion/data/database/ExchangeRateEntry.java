package io.github.akueisara.currencyconversion.data.database;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;

/**
 * Created by Kuei on 2019-04-25.
 */
@Entity(tableName = "exchange_rates")
public class ExchangeRateEntry {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "update_time")
    private int updatedAt;

    @ColumnInfo(name = "source")
    private String source;

    @ColumnInfo(name = "quotes")
    private String quotes;

    @Ignore
    public ExchangeRateEntry(int updatedAt, String source, String quotes) {
        this.updatedAt = updatedAt;
        this.source = source;
        this.quotes = quotes;
    }

    public ExchangeRateEntry(int id, int updatedAt, String source, String quotes) {
        this.id = id;
        this.updatedAt = updatedAt;
        this.source = source;
        this.quotes = quotes;
    }

    public String getQuotes() {
        return quotes;
    }

    public void setQuotes(String quotes) {
        this.quotes = quotes;
    }

    public int getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(int updatedAt) {
        this.updatedAt = updatedAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }


}
