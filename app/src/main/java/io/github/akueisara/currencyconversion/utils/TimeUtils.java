package io.github.akueisara.currencyconversion.utils;

import java.util.concurrent.TimeUnit;

/**
 * Created by Kuei on 2019-04-25.
 */
public class TimeUtils {

    public static boolean durationOverThirtyMinutes(long time) {
        return System.currentTimeMillis() - time > TimeUnit.MINUTES.toMillis(30);
    }
}
