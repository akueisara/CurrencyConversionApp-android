package io.github.akueisara.currencyconversion.persistence.database;

import android.arch.persistence.room.TypeConverter;

import java.util.Date;

/**
 * Created by Kuei on 2019/3/4.
 */
class DateConverter {
    @TypeConverter
    public static Date toDate(Long timestamp) {
        return timestamp == null ? null : new Date(timestamp);
    }

    @TypeConverter
    public static Long toTimestamp(Date date) {
        return date == null ? null : date.getTime();
    }
}
